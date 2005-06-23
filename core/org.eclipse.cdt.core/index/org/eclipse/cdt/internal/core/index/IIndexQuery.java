/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

/**
 * Generic indexer query interface to allow for queries of any existing indexer storage mechanism
 * To be considered in development.
 * @author bgheorgh
 * @since 3.0
 */
public interface IIndexQuery {
	
	/**
	 * Metakind bit field constants
	 */
	final static int CLASS = 1;
	final static int STRUCT = 2;
	final static int UNION = 4;
	final static int ENUM = 8;
	final static int VAR = 16;
	final static int TYPEDEF = 32;
	final static int FUNCTION = 64;
	final static int METHOD = 128;
	final static int FIELD = 256;
	final static int MACRO = 512;
	final static int NAMESPACE = 1024;
	final static int ENUMTOR = 2048;
	final static int INCLUDE = 4096;

	/**
	 * Type bit field
	 */
    final static int DECLARATION = 1;
    final static int DEFINITION = 2;
    final static int REFERENCE= 4;
	
	/**
	 * Returns the entries in the index corresponding to the passed in:
	 *  
	 * @param metakind - bit field that indicates the kinds to retrieve 
	 * @param type - bit field that indiciates what type of kinds to look for
	 * @param pattern - String array that contains the elements of a pattern;
	 * the interpretation of this array is left up to the implementor; can be left null 
	 * in which case a match is attempted based on the metakind and type fields
	 * @param path - an IPath array that is used to limit the query; can be null to indicate
	 * entire workspace
	 * 
	 * @return IIndexEntry 
	 */
	IIndexEntry[] getIndexEntries(int metakind, int type, String[] pattern, IPath[] paths);
	
	/**
	 * Returns the entries in the index corresponding to the passed in:
	 * 
	 * @param metakind - bit field that indicates the kinds to retrieve 
	 * @param type - bit field that indiciates what type of kinds to look for
	 * @param pattern - String array that contains the elements of a pattern;
	 * the interpretation of this array is left up to the implementor; can be left null 
	 * in which case a match is attempted based on the metakind and type fields
	 * @param projects - an IProject array that contains the projects that are to be queried
	 * 
	 * @return IIndexEntry 
	 */
	IIndexEntry[] getIndexEntries(int metakind, int type, String[] pattern, IProject[] projects);
	
	/**
	 * Returns the entries in the index corresponding to the passed in:
	 * 
	 * @param metakind - bit field that indicates the kinds to retrieve 
	 * @param type - bit field that indiciates what type of kinds to look for
	 * @param pattern - String array that contains the elements of a pattern;
	 * the interpretation of this array is left up to the implementor; can be left null 
	 * in which case a match is attempted based on the metakind and type fields
	 * @param projects - an IProject array that contains the projects that are to be queried
	 * @param additionalPaths - an array for additional paths to query
	 * 
	 * @return IIndexEntry 
	 */
	IIndexEntry[] getIndexEntries(int metakind, int type, String[] pattern, IProject[] projects, IPath[] additionalPaths);

}
