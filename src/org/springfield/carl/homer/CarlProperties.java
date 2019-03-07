/* 
* CarlProperties.java
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

public class CarlProperties {
	private String ipnumber;
	private String name;
	private String status;
	private String defaultloglevel;
	private String preferedsmithers;
	private String basepath;
	private String contextpath;
	private String apiKey;
	
	public void setIpNumber(String i) {
		ipnumber = i;
	}
	
	public void setBasePath(String p) {
		basepath = p;
	}
	
	public void setContextPath(String p) {
	    contextpath = p;
	}
	
	public void setName(String n) {
		name = n;
	}
	
	public void setDefaultLogLevel(String l) {
		defaultloglevel = l;
	}
	
	public void setPreferedSmithers(String p) {
		preferedsmithers = p;
	}
	
	public void setStatus(String s) {
		status = s;
	}
	
	public void setApiKey(String k) {
		apiKey = k;
	}
	
	public String getName() {
		return name;
	}
	
	public String getIpNumber() {
		return ipnumber;
	}
	
	public String getBasePath() {
		return basepath;
	}
	
	public String getContextPath() {
	    	return contextpath;
	}
	
	public String getStatus() {
		return status;
	}
	
	
	public String getDefaultLogLevel() {
		return defaultloglevel;
	}
	
	
	public String getPreferedSmithers() {
		return preferedsmithers;
	}
	
	public String getApiKey() {
		return apiKey;
	}
	
}
