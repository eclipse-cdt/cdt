/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

/**
 * @author Doug Schaefer
 */
public class FunctionStyleMacro extends ObjectStyleMacro {

	public char[][] arglist;
	
	public FunctionStyleMacro(char[] name, char[] expansion, char[][] arglist) {
		super(name, expansion);
		this.arglist = arglist;
	}
	
	public class Expansion {
		
		public final CharArrayObjectMap definitions
			= new CharArrayObjectMap(FunctionStyleMacro.this.arglist.length);
		
	}
}
