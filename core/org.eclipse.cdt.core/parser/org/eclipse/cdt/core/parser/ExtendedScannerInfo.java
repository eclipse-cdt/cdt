/**********************************************************************
 * Copyright (c) 2002-2004 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Map;

/**
 * @author jcamelon
 *
 */
public class ExtendedScannerInfo extends ScannerInfo implements IExtendedScannerInfo {

	private static final String [] EMPTY_STRING_ARRAY = new String[ 0 ];
	private String [] m, i;
	
	public ExtendedScannerInfo()
	{
	}
	
	public ExtendedScannerInfo( Map d, String [] incs )
	{
		super(d,incs);
	}
	
	public ExtendedScannerInfo( Map d, String [] incs, String [] macros, String [] includes )
	{
		super(d,incs);
		m = macros;
		i = includes;
	}
	

	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IExtendedScannerInfo#getMacroFiles()
	 */
	public String[] getMacroFiles() {
		if( m == null ) return EMPTY_STRING_ARRAY;
		return m;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IExtendedScannerInfo#getIncludeFiles()
	 */
	public String[] getIncludeFiles() {
		if( i == null ) return EMPTY_STRING_ARRAY;
		return i;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IExtendedScannerInfo#getLocalIncludePath()
	 */
	public String[] getLocalIncludePath() {
		return EMPTY_STRING_ARRAY; //TODO add impl
	}
}
