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
 * Constants that identify base attribute and language IDs
 */
public interface ICFileTypeConstants {

	public static String ID_PREFIX		= "org.eclipse.cdt.core.fileType."; //$NON-NLS-1$
	
	public static String UNKNOWN		= ID_PREFIX + "unknown"; //$NON-NLS-1$

	public static String C_SOURCE		= ID_PREFIX + "c_source"; //$NON-NLS-1$
	
	public static String C_HEADER		= ID_PREFIX + "c_header"; //$NON-NLS-1$
	
	public static String CXX_HEADER		= ID_PREFIX + "cxx_source"; //$NON-NLS-1$
	
	public static String CXX_SOURCE		= ID_PREFIX + "cxx_header"; //$NON-NLS-1$
	
	public static String ASM_SOURCE		= ID_PREFIX + "asm_source"; //$NON-NLS-1$
}
