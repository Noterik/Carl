/* 
* CarlResource.java
* 
* Copyright (c) 2017 Noterik B.V.
* 
* This file is part of Carl, related to the Noterik Springfield project.
*
* Carl is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Carl is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Carl.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.springfield.carl.homer;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.CacheDirective;
import org.restlet.data.CharacterSet;
import org.restlet.data.Form;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.ServerInfo;
import org.restlet.data.Status;
import org.restlet.engine.adapter.HttpRequest;
import org.restlet.engine.header.Header;
import org.restlet.engine.header.HeaderConstants;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.util.Series;
import org.springfield.carl.fs.ParentVideo;
import org.springfield.carl.homer.CarlResource;

/**
 * File server with ticketing support
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2017
 * @package org.springfield.carl.server
 *
 */

public class CarlResource extends ServerResource {
	private static final Logger LOG = Logger.getLogger(CarlResource.class);
	private static final String SERVER_INFO = "Carl/0.1.0";
	private static final String[] SUPPORTED_EXTENSIONS = {"vtt", "jpg"};
	private static final String os = System.getProperty("os.name").toLowerCase();
	
	/**
	 * Handle GET request
	 */ 
	@Get
	public void handleGet() {
	    	CarlProperties conf = LazyHomer.getMyCarlProperties();
	    	String basePath = conf.getBasePath();
		
	    	HttpServletRequest req = ServletUtils.getRequest(getRequest());
	    	String baseurl = req.getScheme() + "://" + req.getServerName() + ":" + req.getLocalPort();
	    	LOG.debug("base url = "+baseurl);
	    	
		String fileIdentifier = getIdentifier(Request.getCurrent().getResourceRef().getPath(), conf.getContextPath());
		LOG.info("identifier = "+fileIdentifier);
		
		// set server info
		ServerInfo serverInfo = new ServerInfo();
		serverInfo.setAgent(SERVER_INFO);			
		getResponse().setServerInfo(serverInfo);
		
		File resource = new File(basePath+fileIdentifier);
		
		// check if requested file exists
		if (resource.exists() && resource.isFile()) {
			// check if we support this type
			String extension = fileIdentifier.lastIndexOf(".") == -1 ? "" : fileIdentifier.substring(fileIdentifier.lastIndexOf(".")+1);
			if (!supportedExtension(extension)) {
				getResponse().setStatus(Status.SERVER_ERROR_NOT_IMPLEMENTED);
				return;
			}
			
			// get query
			Form queryForm = getRequest().getResourceRef().getQueryAsForm(CharacterSet.UTF_8);
						
			String filePath = null;
			Status status = null;
			
			status = Status.SUCCESS_OK;
			filePath = basePath+fileIdentifier;
			
			getResponse().setStatus(status);
						
			if (!status.equals(Status.SUCCESS_OK)) {
				return;
			}
			
			Series<Header> series = ((HttpRequest) getRequest()).getHeaders();
			Header range = series.getFirst("range");
			
			File file = new File(filePath);
			
			String parentVideo = "";
			
			Pattern pattern = Pattern.compile("/domain/.*/video/[^/]*/");
			Matcher matcher = pattern.matcher(fileIdentifier);
			if (matcher.find()) {
			    parentVideo = matcher.group(0);
			}
			
			if (new ParentVideo(parentVideo, baseurl).isPrivate()) {
				String apiKey = conf.getApiKey();

				/** TODO: Abstract ticket handling in separate class **/
				String ticket = queryForm.getFirstValue("ticket", true, "").toLowerCase();
				LOG.debug("ticket = "+ticket);

				//Range request
				if (range != null) {
					String byteRange = range.getValue();
					LOG.debug("Requested byte range "+byteRange);
					byteRange = byteRange.substring(byteRange.indexOf("=")+1);
					long start = Long.parseLong(byteRange.substring(0, byteRange.indexOf("-")));
					long end = -1;
					if (byteRange.indexOf("-") < byteRange.length()-1) {
						end = Long.parseLong(byteRange.substring(byteRange.indexOf("-")+1));
					}
					
					if (end == -1l) {
						end = file.length();
					}
					
					LOG.debug("start = "+start+" end = "+end);
					
					//Safari fix that does multiple 0-1 requests
					if (start == 0l && end > 1l) {
						//only allowed with ticket

						//get ticket
						StringRepresentation entity = new StringRepresentation("<fsxml><properties><uri>"+fileIdentifier+"</uri></properties></fsxml>");
						entity.setMediaType(MediaType.TEXT_XML);
						Request request = new Request(Method.PUT, baseurl+"/lenny/acl/ticketaccess/"+ticket, entity);
						if (apiKey != null && !apiKey.equals("")) {
							Series<Header> headers = (Series<Header>)request.getAttributes().get("org.restlet.http.headers");
							if (headers == null) {
								headers = new Series<Header>(Header.class);
								request.getAttributes().put("org.restlet.http.headers", headers);
							}
							headers.set("X-Api-Key", apiKey);
						}
						Context context = new Context();
						Series<Parameter> parameters = context.getParameters();
						parameters.add("socketTimeout", "1000");
						context.setParameters(parameters);
						Client client = new Client(context, Protocol.HTTP);
						Response response = client.handle(request);
					
						LOG.debug("response = "+response);
						LOG.debug(response.getEntityAsText());
						
						try {
							Document fsxml = DocumentHelper.parseText(response.getEntityAsText());
							boolean allowed = fsxml.selectSingleNode("//properties/allowed") == null ? false : Boolean.parseBoolean(fsxml.selectSingleNode("//properties/allowed").getText());
							
							if (!allowed) {
								status = Status.CLIENT_ERROR_FORBIDDEN;
								getResponse().setStatus(status);
								return;
							}
						} catch (DocumentException e) {
							status = Status.SERVER_ERROR_INTERNAL;
							getResponse().setStatus(status);
							return;
						}
						request.release();
						response.release();
					} else {
						//for now unlimited allowed
					}				
				} else {
					//entire request only allowed once
					
					//get ticket
					StringRepresentation entity = new StringRepresentation("<fsxml><properties><uri>"+fileIdentifier+"</uri></properties></fsxml>");
					entity.setMediaType(MediaType.TEXT_XML);
					Request request = new Request(Method.PUT, baseurl+"/lenny/acl/ticketaccess/"+ticket, entity);
					if (apiKey != null && !apiKey.equals("")) {
						Series<Header> headers = (Series<Header>)request.getAttributes().get("org.restlet.http.headers");
						if (headers == null) {
							headers = new Series<Header>(Header.class);
							request.getAttributes().put("org.restlet.http.headers", headers);
						}
						headers.set("X-Api-Key", apiKey);
					}
					Context context = new Context();
					Series<Parameter> parameters = context.getParameters();
					parameters.add("socketTimeout", "1000");
					context.setParameters(parameters);
					Client client = new Client(context, Protocol.HTTP);
					Response response = client.handle(request);
					
					LOG.debug("response = "+response);
					LOG.debug(response.getEntityAsText());
						
					try {
						Document fsxml = DocumentHelper.parseText(response.getEntityAsText());
						boolean allowed = fsxml.selectSingleNode("//properties/allowed") == null ? false : Boolean.parseBoolean(fsxml.selectSingleNode("//properties/allowed").getText());
						
						if (!allowed) {
							status = Status.CLIENT_ERROR_FORBIDDEN;
							getResponse().setStatus(status);
							return;
						}
					} catch (DocumentException e) {
						status = Status.SERVER_ERROR_INTERNAL;
						getResponse().setStatus(status);
						return;
					}
					request.release();
					response.release();
				}
			}
			
			MediaType type = MediaType.ALL;
			
			if (extension.toLowerCase().equals("jpg")) {
			    type = MediaType.IMAGE_JPEG;
			}
			
			FileRepresentation rep = new FileRepresentation(file, type);
			
			getResponse().setEntity(rep);
			
			if (new ParentVideo(parentVideo, baseurl).isPrivate()) {			
				Series<Header> responseHeaders = (Series<Header>) getResponse().getAttributes().get(HeaderConstants.ATTRIBUTE_HEADERS);
				if (responseHeaders == null) {
					responseHeaders = new Series(Header.class); 
					getResponse().getAttributes().put(HeaderConstants.ATTRIBUTE_HEADERS, responseHeaders); 
				}

				responseHeaders.add(new Header("Pragma", "no-cache"));
				
				getResponse().getEntity().setExpirationDate(new Date(0));
				getResponse().setCacheDirectives(new ArrayList<CacheDirective>());
				getResponse().getCacheDirectives().add(CacheDirective.noCache());				
			}				        	        
			return;
		} else if (resource.exists() && resource.isDirectory()) {					
			//resource is a folder
			LOG.debug("FORBIDDEN");
			getResponse().setStatus(Status.CLIENT_ERROR_FORBIDDEN);
			return;
		} else {
			LOG.debug("NOT FOUND");
			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND);
			return;
		}
	}
	
	/**
	 * get file identifier from uri
	 * 
	 * @param uri - the requested uri
	 * @param contextPath - the context path
	 * @return file identifier
	 */
	private String getIdentifier(String uri, String contextPath) {
		//running windows
		if(os.contains("windows")){
			contextPath = contextPath.replace("\\", "/");
		}
		
		LOG.debug("Uri = "+uri+" contextPath  = "+contextPath);
		
		String[] path = contextPath.split("/");

		return uri.substring(uri.indexOf(path[path.length-1])+path[path.length-1].length());
	}
	
	/**
	 * check if this file type is in the list of supported extensions
	 * 
	 * @param extension - extension of the file
	 * @return true if supported, otherwise false
	 */
	private boolean supportedExtension(String extension) {
		extension = extension.toLowerCase();
		
		for (int i = 0; i < SUPPORTED_EXTENSIONS.length; i++) {
			if (SUPPORTED_EXTENSIONS[i].equals(extension)) { 
				return true;
			}
		}		
		return false;
	}
}