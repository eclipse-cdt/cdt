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
 * Constants that identify base attribute and language IDs
 */
public interface ICFileTypeConstants {

	public static String ID_PREFIX		= "org.eclipse.cdt.core."; //$NON-NLS-1$
	
	// Default languages known to CDT
	
	public static String LANG_PREFIX	= ID_PREFIX + "language."; //$NON-NLS-1$

	public static String LANG_UNKNOWN	= LANG_PREFIX + "unknown"; //$NON-NLS-1$
	
	public static String LANG_C			= LANG_PREFIX + "c"; //$NON-NLS-1$
	
	public static String LANG_CXX		= LANG_PREFIX + "cxx"; //$NON-NLS-1$
	
	public static String LANG_ASM		= LANG_PREFIX + "asm"; //$NON-NLS-1$

	// Default file types known to CDT
	
	public static String FT_PREFIX		= ID_PREFIX + "fileType."; //$NON-NLS-1$
	
	public static String FT_UNKNOWN		= FT_PREFIX + "unknown"; //$NON-NLS-1$

	public static String FT_C_SOURCE	= FT_PREFIX + "c_source"; //$NON-NLS-1$
	
	public static String FT_C_HEADER	= FT_PREFIX + "c_header"; //$NON-NLS-1$
	
	public static String FT_CXX_SOURCE	= FT_PREFIX + "cxx_source"; //$NON-NLS-1$

	public static String FT_CXX_HEADER	= FT_PREFIX + "cxx_header"; //$NON-NLS-1$

	public static String FT_ASM_SOURCE	= FT_PREFIX + "asm_source"; //$NON-NLS-1$
}
