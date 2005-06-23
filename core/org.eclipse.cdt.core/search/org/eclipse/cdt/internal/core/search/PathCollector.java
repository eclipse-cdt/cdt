/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.search;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.cdt.core.search.BasicSearchMatch;

	/**
	 * Collects the resource paths reported by a client to this search requestor.
	 */
	public class PathCollector implements IIndexSearchRequestor {
			
		public ArrayList matches = new ArrayList();
		
		/* a set of resource paths */
		public HashSet paths = new HashSet(5);
		
		public void acceptSearchMatch(BasicSearchMatch match) {
			matches.add(match);
		}
	
		public Iterator getMatches(){
			return matches.iterator();
		}
		
		public ArrayList getMatchesList(){
			return matches;
		}

		public void acceptIncludeDeclaration(String resourcePath, char[] decodedSimpleName) {
			this.paths.add(resourcePath);
		}
		/**
		 * Returns the paths that have been collected.
		 */
		public String[] getPaths() {
			String[] result = new String[this.paths.size()];
			int i = 0;
			for (Iterator iter = this.paths.iterator(); iter.hasNext();) {
				result[i++] = (String)iter.next();
			}
			return result;
		}

}
