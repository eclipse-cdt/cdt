/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.filetype;

import java.util.Comparator;

/**
 * Corresponds to an org.eclipse.cdt.core.CFileTypeAssociation entry.
 */
public interface ICFileTypeAssociation {

	public static Comparator Comparator = new Comparator() {
		public int compare(Object arg0, Object arg1) {
			ICFileTypeAssociation lhs = (ICFileTypeAssociation) arg0;
			ICFileTypeAssociation rhs = (ICFileTypeAssociation) arg1;
			return (lhs.getPattern().compareTo(rhs.getPattern()));
		}
	};

	/**
     * @return the file name pattern used for this file association
     */
	public String getPattern();
	
	/**
     * @return the ICFileType associated with the file name pattern
     */
	public ICFileType getType();
	
	/**
     * Determine if the file name pattern for this association
     * matches the provided name.
     *
     * @param fileName file name to match. 
     *
     * @return true if the file name pattern matches the provided name
     */
	public boolean matches(String fileName);
}
