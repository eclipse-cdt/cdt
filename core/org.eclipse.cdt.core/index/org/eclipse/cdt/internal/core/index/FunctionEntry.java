/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.index;

public class FunctionEntry extends NamedEntry implements IFunctionEntry {
	
	char[][] signature;
	char[]	 returnString;
	
	public FunctionEntry(int metakind, int entry_type, char[][] fullName, int modifiers,int fileNumber){
		super(metakind,entry_type,fullName, modifiers, fileNumber);
	}
	
	public void setSignature(char[][] signature) {
		this.signature = signature;
	}

	public char[][] getSignature() {
		return signature;
	}

    public void setReturnType(char[] returnType) {
        this.returnString = returnType;
    }
    
    public char[] getReturnType() {
        return returnString;
    }

	public void serialize(IIndexerOutput output) {
		output.addIndexEntry(this);
	}
	
    /* BugZilla ID#124618 */   
	public void setFunctionEntry(int meta_kind, int entry_type,  char[][] fullName, int modifiers, int fileNumber) {
		setNamedEntry(meta_kind, entry_type, fullName, modifiers, fileNumber);
	   	// since we reuse FunctionEntry instance, 
    	// the following vars should be cleared.
		this.signature = null;
		this.returnString = null;
	}
}
