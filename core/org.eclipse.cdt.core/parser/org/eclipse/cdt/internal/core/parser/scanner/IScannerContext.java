/*******************************************************************************
 * Copyright (c) 2003, 2004 IBM - Rational Software and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.core.parser.scanner;

/**
 * @author jcamelon
 *
 */
public interface IScannerContext {
	
	public static class ContextKind  
	{
		public static int SENTINEL =  0;
		public static int TOP = 1; 
		public static int INCLUSION = 2; 
		public static int MACROEXPANSION = 3; 
	}
	public int getKind(); 
	
	public int getChar();
	public void ungetChar(int undo);
	
	public boolean isFinal();
	public String getContextName();
	public int getOffset();   
	public void close();

}