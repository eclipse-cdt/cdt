/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Map;

import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;

/**
 * @author jcamelon
 *
 */
public interface IScanner  {

	public static final String __CPLUSPLUS = "__cplusplus"; //$NON-NLS-1$
	public static final String __STDC_VERSION__ = "__STDC_VERSION__"; //$NON-NLS-1$
	public static final String __STDC_HOSTED__ = "__STDC_HOSTED__"; //$NON-NLS-1$
	public static final String __STDC__ = "__STDC__"; //$NON-NLS-1$
	public static final String __FILE__ = "__FILE__"; //$NON-NLS-1$
	public static final String __TIME__ = "__TIME__"; //$NON-NLS-1$
	public static final String __DATE__ = "__DATE__"; //$NON-NLS-1$
	public static final String __LINE__ = "__LINE__"; //$NON-NLS-1$
	
	public static final int tPOUNDPOUND = -6;
	public static final int tPOUND      = -7;
	
	public void setOffsetBoundary( int offset );
	public void setASTFactory( IASTFactory f );
	
	public void addDefinition(String key, String value); 
	public Map 				getDefinitions();
	public String[] getIncludePaths();

	public IToken nextToken() throws EndOfFileException;
			
	public int  getCount();
	public boolean isOnTopContext();
	public CharArrayObjectMap getRealDefinitions();

}
