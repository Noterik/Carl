/* 
* CarlRestlet.java
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

import org.restlet.Context;
import org.restlet.routing.Router;
import org.springfield.carl.homer.CarlResource;
import org.springfield.carl.logging.LoggingResource;

/**
 * Redirects based on uri
 *
 * @author Pieter van Leeuwen
 * @copyright Copyright: Noterik B.V. 2017
 * @package org.springfield.carl.restlet
 *
 */
public class CarlRestlet extends Router {

	public CarlRestlet(Context cx) {
		super(cx);
		
		this.attach("/logging", LoggingResource.class);
		
		HeaderFilter headerFilter = new HeaderFilter(cx);
		this.attachDefault(headerFilter);
		headerFilter.setNext(CarlResource.class);
	}
}
