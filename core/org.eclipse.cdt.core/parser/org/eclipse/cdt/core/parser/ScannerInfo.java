/*******************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Map;

/**
 * @author jcamelon
 *
 */
public class ScannerInfo implements IScannerInfo
{
	private Map definedSymbols = null; 
	private String [] includePaths = null; 
	
	public ScannerInfo()
	{
	}
	
	public ScannerInfo( Map d, String [] incs )
	{
		definedSymbols = d; 
		includePaths = incs;
	}
		
    /**
	 * @param definitions
	 */
	public ScannerInfo(Map definitions) {
		this( definitions, (String [])null);
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.IScannerInfo#getDefinedSymbols()
     */
    public Map getDefinedSymbols()
    {
        return definedSymbols;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.IScannerInfo#getIncludePaths()
     */
    public String[] getIncludePaths()
    {
        return includePaths;
    }
}
