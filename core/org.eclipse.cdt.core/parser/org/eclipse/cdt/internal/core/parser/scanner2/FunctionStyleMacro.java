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
	private char[] sig = null;
	
	public FunctionStyleMacro(char[] name, char[] expansion, char[][] arglist) {
		super(name, expansion);
		this.arglist = arglist;
	}
	
	public char[] getSignature(){
	    if( sig != null )
	        return sig;
	    
	    int len = name.length + 2 /*()*/;
	    for( int i = 0; i < arglist.length && arglist[i] != null; i++ ){
            if( i + 1 < arglist.length && arglist[i+1] != null) 
                len += 1; /*,*/
            len += arglist[i].length;
	    }
	    sig = new char[len];
	    System.arraycopy( name, 0, sig, 0, name.length );
	    sig[name.length] = '(';
	    int idx = name.length + 1;
	    for( int i = 0; i < arglist.length && arglist[i] != null; i++ ){
	        System.arraycopy( arglist[i], 0, sig, idx, arglist[i].length );
	        idx += arglist[i].length;
	        if( i + 1 < arglist.length && arglist[i+1] != null )
	            sig[idx++] = ',';
	    }
	    sig[idx] = ')';
	    return sig;
	}
	
	public class Expansion {
		
		public final CharArrayObjectMap definitions
			= new CharArrayObjectMap(FunctionStyleMacro.this.arglist.length);
		
	}
}
