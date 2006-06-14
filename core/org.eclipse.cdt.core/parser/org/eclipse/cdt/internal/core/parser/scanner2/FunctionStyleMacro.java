/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.parser.scanner2;

import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;

/**
 * @author Doug Schaefer
 */
public class FunctionStyleMacro extends ObjectStyleMacro {

	private static final char[] VA_ARGS_CHARARRAY = "__VA_ARGS__".toCharArray(); //$NON-NLS-1$
	private static final char[] ELLIPSIS_CHARARRAY = "...".toString().toCharArray(); //$NON-NLS-1$
	public char[][] arglist;
	private char[] sig = null;
	private boolean hasVarArgs = false;
	private boolean hasGCCVarArgs = false;
	private int varArgsPosition = -1;
	
	public FunctionStyleMacro(char[] name, char[] expansion, char[][] arglist) {
		super(name, expansion);
		this.arglist = arglist;

		// determine if there's an argument with "..."
		if (arglist != null && arglist[0]!= null && arglist.length > 0) {
			int last = -1;
			
			// if the last element in the list is null then binary search for the last non-null element
			if (arglist[arglist.length-1] == null) { 
				int largest = arglist.length - 1;
				int smallest = 0;
				for (int j=arglist.length/2; last == -1; ) {
					if (arglist[j] == null) {
						largest = j;
						j=smallest + (largest-smallest)/2;
					} else {
						smallest = j;
						j=smallest + (largest - smallest)/2;
						if ((j+1 == arglist.length && arglist[j] != null) || (arglist[j] != null && arglist[j+1] == null))
							last = j;
					}
				}
			} else 
				last = arglist.length-1; 
			
			if (arglist[last] != null && CharArrayUtils.equals(arglist[last], ELLIPSIS_CHARARRAY)) {
				this.hasVarArgs = true;
				varArgsPosition = last;
				// change the arg to __VA_ARGS__ so this will be replaced properly later on...
				arglist[last] = VA_ARGS_CHARARRAY;
			} else if (arglist[last] != null && CharArrayUtils.equals(arglist[last], arglist[last].length - ELLIPSIS_CHARARRAY.length, ELLIPSIS_CHARARRAY.length, ELLIPSIS_CHARARRAY)) { // if the last 3 are '...' 
				this.hasGCCVarArgs = true;
				varArgsPosition = last;
				// change the arg to "argname" instead of "argname..." so argname will be replaced properly later on...
				char[] swap = new char[arglist[last].length - ELLIPSIS_CHARARRAY.length];
				System.arraycopy(arglist[last], 0, swap, 0, swap.length);
				arglist[last] = swap;
			}
		}
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
	
	public boolean hasVarArgs() {
		return hasVarArgs;
	}
	
	public boolean hasGCCVarArgs() {
		return hasGCCVarArgs;
	}
	
	public int getVarArgsPosition() {
		return varArgsPosition;
	}
}
