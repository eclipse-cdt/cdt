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

import org.eclipse.cdt.internal.core.index.IIndexerOutput;


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

	static void addNameDecl(IIndexerOutput indexerOutput,
							  char[][] name,
							  EntryType entryType,
							  int fileNumber,
							  int offset,
							  int length,
							  int offsetType) {
		//TODO temporary until all bindings are completed
		if (name == null) 
			name = new char[][] {"NPE".toCharArray()}; //$NON-NLS-1$
		switch (entryType.toInt()) {
			case CLASS_CONST:
				indexerOutput.addClassDecl(fileNumber, name, offset, length, offsetType);
				break;
			case STRUCT_CONST:
				indexerOutput.addStructDecl(fileNumber, name, offset, length, offsetType);
				break;
			case UNION_CONST:
				indexerOutput.addUnionDecl(fileNumber, name, offset, length, offsetType);
				break;
			case ENUM_CONST:
				indexerOutput.addEnumDecl(fileNumber, name, offset, length, offsetType);
				break;
			case VAR_CONST:
				indexerOutput.addVariableDecl(fileNumber, name, offset, length, offsetType);
				break;
			case TYPEDEF_CONST:
				indexerOutput.addTypedefDecl(fileNumber, name, offset, length, offsetType);
				break;
			case DERIVED_CONST:
				indexerOutput.addDerivedDecl(fileNumber, name, offset, length, offsetType);
				break;
			case FRIEND_CONST:
				indexerOutput.addFriendDecl(fileNumber, name, offset, length, offsetType);
				break;
			case FWD_CLASS_CONST:
				indexerOutput.addFwd_ClassDecl(fileNumber, name, offset, length, offsetType);
				break;
			case FWD_STRUCT_CONST:
				indexerOutput.addFwd_StructDecl(fileNumber, name, offset, length, offsetType);
				break;
			case FWD_UNION_CONST:
				indexerOutput.addFwd_UnionDecl(fileNumber, name, offset, length, offsetType);
				break;
			case NAMESPACE_CONST:
				indexerOutput.addNamespaceDecl(fileNumber, name, offset, length, offsetType);
				break;
			case ENUMERATOR_CONST:
				indexerOutput.addEnumtorDecl(fileNumber, name, offset, length, offsetType);
				break;
			case FIELD_CONST:
				indexerOutput.addFieldDecl(fileNumber, name, offset, length, offsetType);
				break;
			case METHOD_CONST:
				indexerOutput.addMethodDecl(fileNumber, name, offset, length, offsetType);
				break;
			case FUNCTION_CONST:
				indexerOutput.addFunctionDecl(fileNumber, name, offset, length, offsetType);
				break;
			case MACRO_CONST:
				indexerOutput.addMacroDecl(fileNumber, name, offset, length, offsetType);
				break;
		}
	}
	
	static void addNameRef(IIndexerOutput indexerOutput,
 						   char[][] name,
						   EntryType entryType,
						   int fileNumber,
						   int offset,
						   int length,
						   int offsetType) {
		//TODO temporary until all bindings are completed
		if (name == null) 
			name = new char[][] {"NPE".toCharArray()}; //$NON-NLS-1$
		switch (entryType.toInt()) {
			case CLASS_CONST:
				indexerOutput.addClassRef(fileNumber, name, offset, length, offsetType);
				break;
			case STRUCT_CONST:
				indexerOutput.addStructRef(fileNumber, name, offset, length, offsetType);
				break;
			case UNION_CONST:
				indexerOutput.addUnionRef(fileNumber, name, offset, length, offsetType);
				break;
			case ENUM_CONST:
				indexerOutput.addEnumRef(fileNumber, name, offset, length, offsetType);
				break;
			case VAR_CONST:
				indexerOutput.addVariableRef(fileNumber, name, offset, length, offsetType);
				break;
			case TYPEDEF_CONST:
				indexerOutput.addTypedefRef(fileNumber, name, offset, length, offsetType);
				break;
			case DERIVED_CONST:
				indexerOutput.addDerivedRef(fileNumber, name, offset, length, offsetType);
				break;
			case FRIEND_CONST:
				indexerOutput.addFriendRef(fileNumber, name, offset, length, offsetType);
				break;
			case FWD_CLASS_CONST:
				indexerOutput.addFwd_ClassRef(fileNumber, name, offset, length, offsetType);
				break;
			case FWD_STRUCT_CONST:
				indexerOutput.addFwd_StructRef(fileNumber, name, offset, length, offsetType);
				break;
			case FWD_UNION_CONST:
				indexerOutput.addFwd_UnionRef(fileNumber, name, offset, length, offsetType);
				break;
			case NAMESPACE_CONST:
				indexerOutput.addNamespaceRef(fileNumber, name, offset, length, offsetType);
				break;
			case ENUMERATOR_CONST:
				indexerOutput.addEnumtorRef(fileNumber, name, offset, length, offsetType);
				break;
			case FIELD_CONST:
				indexerOutput.addFieldRef(fileNumber, name, offset, length, offsetType);
				break;
			case METHOD_CONST:
				indexerOutput.addMethodRef(fileNumber, name, offset, length, offsetType);
				break;
			case FUNCTION_CONST:
				indexerOutput.addFunctionRef(fileNumber, name, offset, length, offsetType);
				break;
			case INCLUDE_CONST:
				indexerOutput.addIncludeRef(fileNumber, name, offset, length, offsetType);
				break;
		}
	}
}
