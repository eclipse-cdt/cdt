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


public class TypeEntry extends NamedEntry implements ITypeEntry {

	int type_kind;
	IIndexEntry[] baseTypes;
	IIndexEntry[] friends;
    
	public TypeEntry(int type_kind, int entry_type, char[][] fullName, int modifiers, int fileNumber){
		super(IIndex.TYPE, entry_type, fullName, modifiers, fileNumber);
		this.type_kind = type_kind;
	}
	
	public void serialize(IIndexerOutput output) {
		output.addIndexEntry(this);
	}

	public int getTypeKind() {
		return type_kind;
	}
	
	public void setBaseTypes(IIndexEntry[] baseTypes) {
		this.baseTypes=baseTypes;
	}

	public IIndexEntry[] getBaseTypes() {
		return baseTypes;
	}

    public IIndexEntry[] getFriends() {
        return friends;
    }

    public void setFriends(IIndexEntry[] friends) {
        this.friends = friends;
    }

    /* BugZilla ID#124618 */   
    public void setTypeEntry(int type_kind, int entry_type, char[][] fullName, int modifiers, int fileNumber) {
		this.entry_type = entry_type; 
		this.fileNumber = fileNumber;
		this.fullName = fullName;
		this.modifiers = modifiers;
    	this.type_kind = type_kind;
    	// since we reuse TypeEntry instance, 
    	// the following vars should be cleared.
    	this.baseTypes = null;
    	this.friends = null;
    }
}
