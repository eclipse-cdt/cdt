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

public class NamedEntry extends CIndexStorageEntry implements INamedEntry {
	
	char[][] fullName;
	int modifiers;
	
	public NamedEntry(int meta_kind, int entry_type,  char[][] fullName, int modifiers, int fileNumber){
		super(meta_kind, entry_type, fileNumber);
		this.fullName = fullName;
		this.modifiers = modifiers;
	}
	
	public  NamedEntry(int meta_kind, int entry_type,  String simpleName, int modifiers, int fileNumber){
		super(meta_kind, entry_type, fileNumber);
		this.fullName = new char[][]{simpleName.toCharArray()};
		this.modifiers = modifiers;
	}
	public char[][] getFullName(){
		return fullName;
	}
	
	public int getModifiers(){
		return modifiers;
	}
	
	public void serialize(IIndexerOutput output) {
		output.addIndexEntry(this);
	}

}
