/* 
* CarlInitialListener.java
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

package org.springfield.carl.restlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.springfield.carl.homer.LazyHomer;

/**
 * Initial listener
 * 
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2017
 * @package org.springfield.carl.restlet
 *
 */
public class CarlInitialListener implements ServletContextListener {	
	
	public void contextInitialized(ServletContextEvent event) {
		System.out.println("Carl: context created");
		ServletContext servletContext = event.getServletContext();

		//load LazyHomer		
		LazyHomer lh = new LazyHomer();
		lh.init(servletContext.getRealPath("/"));
	}
	
	public void contextDestroyed(ServletContextEvent event) {
		System.out.println("Carl: context destroyed");
	}
}
