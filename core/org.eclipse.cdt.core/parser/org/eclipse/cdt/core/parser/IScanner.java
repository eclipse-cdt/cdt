/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

import java.util.Map;

import org.eclipse.cdt.core.dom.IMacroCollector;
import org.eclipse.cdt.core.parser.ast.IASTFactory;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.internal.core.parser.scanner2.ILocationResolver;

/**
 * @author jcamelon
 * 
 * <p>
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will
 * work or that it will remain the same. Please do not use this API without
 * consulting with the CDT team.
 * </p>
 *
 */
public interface IScanner extends IMacroCollector {
	
	/** @deprecated */ public static final int tPOUNDPOUND = IToken.tPOUNDPOUND;
	/** @deprecated */ public static final int tPOUND      = IToken.tPOUND;

    public void setOffsetBoundary( int offset );
	public void setContentAssistMode( int offset );
	public void setASTFactory( IASTFactory f );
	/**
	 * Turns on/off comment parsing.
	 * @since 4.0
	 */
	public void setScanComments(boolean val);
	
	public IMacro addDefinition(char[] key, char[] value); 
	public IMacro addDefinition(char[] name, char[][] params, char[] expansion);
	public void addDefinition(IMacro macro);
	
	public Map 				getDefinitions();
	public String[] getIncludePaths();

	public IToken nextToken() throws EndOfFileException;
			
	public int  getCount();
	public boolean isOnTopContext();
	public CharArrayObjectMap getRealDefinitions();
	public void cancel();
	public char[] getMainFilename();
	
	public ILocationResolver getLocationResolver();
}
