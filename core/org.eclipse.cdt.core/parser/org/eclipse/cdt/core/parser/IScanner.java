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
import org.eclipse.cdt.internal.core.parser.scanner.IScannerContext;

/**
 * @author jcamelon
 *
 */
public interface IScanner  extends IFilenameProvider {

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
	public void addDefinition(String key, IMacroDescriptor macroToBeAdded );
	public void addDefinition(String key, String value); 
	public IMacroDescriptor getDefinition(String key);
	public Map 				getDefinitions();

	public String[] getIncludePaths();
	public void overwriteIncludePath( String [] newIncludePaths );
	
	public IToken nextToken() throws ScannerException, EndOfFileException;
	public IToken nextToken( boolean next ) throws ScannerException, EndOfFileException;
			
	public int  getCount();
	public int  getDepth();

	public IToken nextTokenForStringizing() throws ScannerException, EndOfFileException;
	public void setTokenizingMacroReplacementList(boolean b);
	public void setThrowExceptionOnBadCharacterRead( boolean throwOnBad );

	/**
	 * @return
	 */
	public boolean isOnTopContext();
	public void setScannerContext(IScannerContext context);

}
