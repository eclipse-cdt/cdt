/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.internal.core.index.domsourceindexer;

import org.eclipse.cdt.internal.core.index.FunctionEntry;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;
import org.eclipse.cdt.internal.core.index.NamedEntry;
import org.eclipse.cdt.internal.core.index.TypeEntry;


/**
 * Wrapper for calls to IIndexerOutput 
 * (in anticipation that the interface is going to change)
 * 
 * @author vhirsl
 */
class IndexerOutputWrapper {
    static class EntryType {
        public int toInt() {
            return type;
        }
        private EntryType(int type) {
            this.type = type;
        }
        private int type;
        
    }
	private final static int CLASS_CONST = 1;
	private final static int STRUCT_CONST = 2;
	private final static int UNION_CONST = 3;
	private final static int ENUM_CONST = 4;
	private final static int VAR_CONST = 5;
	private final static int TYPEDEF_CONST = 6;
	private final static int DERIVED_CONST = 7;
	private final static int FRIEND_CONST = 8;
	private final static int FWD_CLASS_CONST = 9;
	private final static int FWD_STRUCT_CONST = 10;
	private final static int FWD_UNION_CONST = 11;
	private final static int NAMESPACE_CONST = 12;
	private final static int ENUMERATOR_CONST = 13;
	private final static int FIELD_CONST = 14;
	private final static int METHOD_CONST = 15;
	private final static int FUNCTION_CONST = 16;
	private final static int MACRO_CONST = 17;
	private final static int INCLUDE_CONST = 18;
	
	
    // entry types
    final static EntryType CLASS = new EntryType(CLASS_CONST);
    final static EntryType STRUCT = new EntryType(STRUCT_CONST);
    final static EntryType UNION = new EntryType(UNION_CONST);
    final static EntryType ENUM = new EntryType(ENUM_CONST);
    final static EntryType VAR = new EntryType(VAR_CONST);
    final static EntryType TYPEDEF = new EntryType(TYPEDEF_CONST);
    final static EntryType DERIVED = new EntryType(DERIVED_CONST);
    final static EntryType FRIEND = new EntryType(FRIEND_CONST);
    final static EntryType FWD_CLASS = new EntryType(FWD_CLASS_CONST);
    final static EntryType FWD_STRUCT = new EntryType(FWD_STRUCT_CONST);
    final static EntryType FWD_UNION = new EntryType(FWD_UNION_CONST);
    final static EntryType NAMESPACE = new EntryType(NAMESPACE_CONST);
    final static EntryType ENUMERATOR = new EntryType(ENUMERATOR_CONST);
    final static EntryType FIELD = new EntryType(FIELD_CONST);
    final static EntryType METHOD = new EntryType(METHOD_CONST);
    final static EntryType FUNCTION = new EntryType(FUNCTION_CONST);
    final static EntryType MACRO = new EntryType(MACRO_CONST);
    final static EntryType INCLUDE = new EntryType(INCLUDE_CONST);


	private IndexerOutputWrapper() {
	}

	static void addIndexEntry (IIndexerOutput indexerOutput,
							  char[][] name,
							  EntryType entryType,
							  int entryKind,
							  int fileNumber,
							  int offset,
							  int length,
							  int offsetType) {
		//TODO temporary until all bindings are completed
		if (name == null) 
			name = new char[][] {"NPE".toCharArray()}; //$NON-NLS-1$
		
		TypeEntry typeEntry;
		NamedEntry namedEntry;
		FunctionEntry functionEntry;
		
		switch (entryType.toInt()) {
			case CLASS_CONST:
				typeEntry = new TypeEntry(IIndex.TYPE_CLASS,entryKind, name, 0 /*getModifiers()*/, fileNumber);
				typeEntry.setNameOffset(offset, length, offsetType);
				//typeEntry.setBaseTypes(getInherits());
				typeEntry.serialize(indexerOutput);
				break;
			case STRUCT_CONST:
				typeEntry = new TypeEntry(IIndex.TYPE_STRUCT,entryKind, name, 0 /*getModifiers()*/, fileNumber);
				typeEntry.setNameOffset(offset, length, offsetType);
				//typeEntry.setBaseTypes(getInherits());
				typeEntry.serialize(indexerOutput);
				break;
			case UNION_CONST:
				typeEntry = new TypeEntry(IIndex.TYPE_UNION,entryKind, name, 0 /*getModifiers()*/, fileNumber);
				typeEntry.setNameOffset(offset, length, offsetType);
				//typeEntry.setBaseTypes(getInherits());
				typeEntry.serialize(indexerOutput);
				break;
			case ENUM_CONST:
				typeEntry = new TypeEntry(IIndex.TYPE_ENUM ,entryKind, name, 0 /*getModifiers()*/, fileNumber);
				typeEntry.setNameOffset(offset, length, offsetType);
				typeEntry.serialize(indexerOutput);
				break;
			case VAR_CONST:
				typeEntry = new TypeEntry(IIndex.TYPE_VAR ,entryKind, name, 0 /*getModifiers()*/, fileNumber);
				typeEntry.setNameOffset(offset, length, offsetType);
				typeEntry.serialize(indexerOutput);
				break;
			case TYPEDEF_CONST:
				typeEntry = new TypeEntry(IIndex.TYPE_TYPEDEF, entryKind, name, 0 /*getModifiers()*/, fileNumber);
				typeEntry.setNameOffset(offset, length, offsetType);
				typeEntry.serialize(indexerOutput);
				break;
			case DERIVED_CONST:
				typeEntry = new TypeEntry(IIndex.TYPE_DERIVED ,entryKind, name, 0 /*getModifiers()*/, fileNumber);
				typeEntry.setNameOffset(offset, length, offsetType);
				typeEntry.serialize(indexerOutput);
				break;
			case FRIEND_CONST:
				typeEntry = new TypeEntry(IIndex.TYPE_FRIEND ,entryKind, name, 0 /*getModifiers()*/, fileNumber);
				typeEntry.setNameOffset(offset, length, offsetType);
				typeEntry.serialize(indexerOutput);
				break;
			case FWD_CLASS_CONST:
				typeEntry = new TypeEntry(IIndex.TYPE_FWD_CLASS ,entryKind, name, 0 /*getModifiers()*/, fileNumber);
				typeEntry.setNameOffset(offset, length, offsetType);
				typeEntry.serialize(indexerOutput);
				break;
			case FWD_STRUCT_CONST:
				typeEntry = new TypeEntry(IIndex.TYPE_FWD_STRUCT ,entryKind, name, 0 /*getModifiers()*/, fileNumber);
				typeEntry.setNameOffset(offset, length, offsetType);
				typeEntry.serialize(indexerOutput);
				break;
			case FWD_UNION_CONST:
				typeEntry = new TypeEntry(IIndex.TYPE_FWD_UNION ,entryKind, name, 0 /*getModifiers()*/, fileNumber);
				typeEntry.setNameOffset(offset, length, offsetType);
				typeEntry.serialize(indexerOutput);
				break;
			case NAMESPACE_CONST:
			    namedEntry = new NamedEntry(IIndex.NAMESPACE, entryKind, name, 0 /*getModifiers()*/, fileNumber);
				namedEntry.setNameOffset(offset, length, offsetType);
				namedEntry.serialize(indexerOutput);
				break;
			case ENUMERATOR_CONST:
				 namedEntry = new NamedEntry(IIndex.ENUMTOR, entryKind, name, 0 /*getModifiers()*/, fileNumber);
				 namedEntry.setNameOffset(offset, length, offsetType);
				 namedEntry.serialize(indexerOutput);
				break;
			case FIELD_CONST:
				 namedEntry = new NamedEntry(IIndex.FIELD, entryKind, name, 0 /*getModifiers()*/, fileNumber);
				 namedEntry.setNameOffset(offset, length, offsetType);
				 namedEntry.serialize(indexerOutput);
				break;
			case METHOD_CONST:
				functionEntry = new FunctionEntry(IIndex.METHOD, entryKind,name,0 /*getModifiers()*/, fileNumber);
				//funEntry.setSignature(getFunctionSignature());
				functionEntry.setNameOffset(offset, length, offsetType);
				functionEntry.serialize(indexerOutput);
				break;
			case FUNCTION_CONST:
				functionEntry = new FunctionEntry(IIndex.FUNCTION, entryKind,name,0 /*getModifiers()*/, fileNumber);
				//funEntry.setSignature(getFunctionSignature());
				functionEntry.setNameOffset(offset, length, offsetType);
				functionEntry.serialize(indexerOutput);
				break;
			case MACRO_CONST:
				 namedEntry = new NamedEntry(IIndex.MACRO, entryKind, name, 0 /*getModifiers()*/, fileNumber);
				 namedEntry.setNameOffset(offset, length, offsetType);
				 namedEntry.serialize(indexerOutput);
				break;
				
			case INCLUDE_CONST:
				namedEntry = new NamedEntry(IIndex.INCLUDE, entryKind, name, 0, fileNumber);
				namedEntry.setNameOffset(offset, length, offsetType);
				namedEntry.serialize(indexerOutput);
				break;
		}
	}

}
