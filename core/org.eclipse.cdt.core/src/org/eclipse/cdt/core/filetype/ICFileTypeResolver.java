/**********************************************************************
 * Copyright (c) 2004 TimeSys Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * TimeSys Corporation - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.filetype;


/**
 * Class responsible for resolving a file name into the
 * associated file type.
 *
 * Accessed by ICFileTypeResolver and file type management UI.
 */
public interface ICFileTypeResolver {
	/**
	 * @return array containing all known file types.
	 */
	public ICFileTypeAssociation[] getFileTypeAssociations();  

	/**
	 * Determine which file type corresponds to the given
	 * file name.
	 * 
	 * @param fileName file name to check.
	 * 
	 * @return file type for the provided file name
	 */
	public ICFileType getFileType(String fileName);

	/**
	 * Add a new file type association to the resolver's list.
	 * 
	 * @param pattern file name pattern to add.
     * @param type file type associated with pattern.
	 *
	 * @return true if the file type association was added.
	 */
	public boolean addAssociation(String pattern, ICFileType type);

	/**
	 * Remove a file type association from the resolver's list.
	 * 
	 * @param assoc file type association to remove.
	 *
	 * @return true if the file type association was removed.
	 */
	public boolean removeAssociation(ICFileTypeAssociation assoc);
}
