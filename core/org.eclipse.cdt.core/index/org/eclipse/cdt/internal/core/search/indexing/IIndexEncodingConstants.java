/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.internal.core.search.indexing;


public interface IIndexEncodingConstants {
    public class EntryType {
        public int toInt() {
            return type;
        }
        private EntryType(int type) {
            this.type = type;
        }
        private int type;
        
    }
    // entry types
    final static EntryType CLASS = new EntryType(1);
    final static EntryType STRUCT = new EntryType(2);
    final static EntryType UNION = new EntryType(3);
    final static EntryType ENUM = new EntryType(4);
    final static EntryType VAR = new EntryType(5);
    final static EntryType TYPEDEF = new EntryType(6);
    final static EntryType DERIVED = new EntryType(7);
    final static EntryType FRIEND = new EntryType(8);
    final static EntryType FWD_CLASS = new EntryType(9);
    final static EntryType FWD_STRUCT = new EntryType(10);
    final static EntryType FWD_UNION = new EntryType(11);
    final static EntryType NAMESPACE = new EntryType(12);
    final static EntryType ENUMERATOR = new EntryType(13);
    final static EntryType FIELD = new EntryType(14);
    final static EntryType METHOD = new EntryType(15);
    final static EntryType FUNCTION = new EntryType(16);
    final static EntryType MACRO = new EntryType(17);
    final static EntryType INCLUDE = new EntryType(18);

    final static char[][] encodedTypeNames_Decl = {
        new char[] {' '},   // not used
        "typeDecl/C/".toCharArray(),    // CLASS        //$NON-NLS-1$
        "typeDecl/S/".toCharArray(),    // STRUCT       //$NON-NLS-1$
        "typeDecl/U/".toCharArray(),    // UNION        //$NON-NLS-1$
        "typeDecl/E/".toCharArray(),    // ENUM         //$NON-NLS-1$
        "typeDecl/V/".toCharArray(),    // VAR          //$NON-NLS-1$
        "typeDecl/T/".toCharArray(),    // TYPEDEF      //$NON-NLS-1$
        "typeDecl/D/".toCharArray(),    // DERIVED      //$NON-NLS-1$
        "typeDecl/F/".toCharArray(),    // FIREND       //$NON-NLS-1$
        "typeDecl/G/".toCharArray(),    // FWD_CLASS    //$NON-NLS-1$
        "typeDecl/H/".toCharArray(),    // FWD_STRUCT   //$NON-NLS-1$
        "typeDecl/I/".toCharArray(),    // FWD_UNION    //$NON-NLS-1$
        "namespaceDecl/".toCharArray(), // NAMESPACE    //$NON-NLS-1$
        "enumtorDecl/".toCharArray(),   // ENUMERATOR   //$NON-NLS-1$
        "fieldDecl/".toCharArray(),     // FIELD        //$NON-NLS-1$
        "methodDecl/".toCharArray(),    // METHOD       //$NON-NLS-1$
        "functionDecl/".toCharArray(),  // FUNCTION     //$NON-NLS-1$
        "macroDecl/".toCharArray(),     // MACRO        //$NON-NLS-1$
        "includeDecl/".toCharArray()    // INCLUDE-unused //$NON-NLS-1$
    };
    
    final static char[][] encodedTypeNames_Ref = {
        new char[] {' '},   // not used
        "typeRef/C/".toCharArray(),    // CLASS        //$NON-NLS-1$
        "typeRef/S/".toCharArray(),    // STRUCT       //$NON-NLS-1$
        "typeRef/U/".toCharArray(),    // UNION        //$NON-NLS-1$
        "typeRef/E/".toCharArray(),    // ENUM         //$NON-NLS-1$
        "typeRef/V/".toCharArray(),    // VAR          //$NON-NLS-1$
        "typeRef/T/".toCharArray(),    // TYPEDEF      //$NON-NLS-1$
        "typeRef/D/".toCharArray(),    // DERIVED      //$NON-NLS-1$
        "typeRef/F/".toCharArray(),    // FIREND       //$NON-NLS-1$
        "typeRef/G/".toCharArray(),    // FWD_CLASS    //$NON-NLS-1$
        "typeRef/H/".toCharArray(),    // FWD_STRUCT   //$NON-NLS-1$
        "typeRef/I/".toCharArray(),    // FWD_UNION    //$NON-NLS-1$
        "namespaceRef/".toCharArray(), // NAMESPACE    //$NON-NLS-1$
        "enumtorRef/".toCharArray(),   // ENUMERATOR   //$NON-NLS-1$
        "fieldRef/".toCharArray(),     // FIELD        //$NON-NLS-1$
        "methodRef/".toCharArray(),    // METHOD       //$NON-NLS-1$
        "functionRef/".toCharArray(),  // FUNCTION     //$NON-NLS-1$
        "macroRef/".toCharArray(),     // MACRO-unused //$NON-NLS-1$
        "includeRef/".toCharArray()    // INCLUDE      //$NON-NLS-1$
    };
        
    final static char SEPARATOR= '/';
    
}
