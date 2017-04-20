/* 
* ParentVideo.java
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
package org.springfield.carl.fs;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.util.Series;

/**
 * ParentVideo.java
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2016
 * @package org.springfield.carl.fs
 * 
 */
public class ParentVideo {
	private String identifier;
	private String baseurl;
	private static final Logger LOG = Logger.getLogger(ParentVideo.class);
	
	public ParentVideo(String identifier, String baseurl) {
		this.identifier = identifier;
		this.baseurl = baseurl;
	}
	
	/**
	 * Determine if this video is marked as private
	 * in the Springfield file system and for that 
	 * reason requires a ticket
	 * 
	 * @param identifier
	 */
	public boolean isPrivate() {
		LOG.info("Checking video is Private for uri "+identifier);
		Request request = new Request(Method.GET, baseurl+"/smithers2"+identifier);
		Context context = new Context();
		Series<Parameter> parameters = context.getParameters();
		parameters.add("socketTimeout", "1000");
		context.setParameters(parameters);
		Client client = new Client(context, Protocol.HTTP);
		Response response = client.handle(request);
		
		LOG.debug("response = "+response);
		LOG.debug(response.getEntityAsText());
		
		boolean privateVideo = true;
		
		try {
			Document fsxml = DocumentHelper.parseText(response.getEntityAsText());
			privateVideo = fsxml.selectSingleNode("//properties/private") == null ? true : Boolean.parseBoolean(fsxml.selectSingleNode("//properties/private").getText());
		} catch (DocumentException e) {
			
		}
		request.release();
		response.release();
		
		LOG.debug("Is video private ? "+String.valueOf(privateVideo));
		
		return privateVideo;
	}
}
