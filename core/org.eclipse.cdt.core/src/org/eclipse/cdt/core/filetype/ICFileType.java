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

/**
 * Corresponds to an org.eclipse.cdt.core.CFileType entry.
 */
public interface ICFileType {
	
	/** Value indicating an unknown file type **/
	static public final int TYPE_UNKNOWN	= 0;

	/** Value indicating a source file type **/
	static public final int TYPE_SOURCE		= 1;

	/** Value indicating a header file **/
	static public final int TYPE_HEADER		= 2;

	/**
     * @return Id associated with this file type.
     */
    public String getId();

	/**
     * @return Language associated with this file type.
     */
	public ICLanguage getLanguage();

	/**
     * @return Name of this file type.
     */
    public String getName();

	/**
     * Return the integer value indicating file type.
     *
     * @return the TYPE_* value indicating the file type.
     */
	public int getType();

	/**
	 * @return True if this is a known source file type.
	 */
	public boolean isSource();
	
	/**
	 * @return True if this is a known header file type.
	 */
	public boolean isHeader();
	
	/**
	 * @return True if this is a known source or header file type.
	 */
	public boolean isTranslationUnit();
}
