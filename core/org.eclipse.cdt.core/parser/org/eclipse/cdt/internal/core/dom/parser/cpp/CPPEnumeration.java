/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Dec 14, 2004
 */
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * @author aniefer
 */
public class CPPEnumeration implements IEnumeration, ICPPBinding {
    private IASTName enumName;
    /**
     * @param specifier
     */
    public CPPEnumeration( IASTName name ) {
        this.enumName = name;
        ((CPPASTName)name).setBinding( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDefinition()
     */
    public IASTNode getDefinition() {
        return enumName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
        return enumName.toString();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return enumName.toCharArray();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        return CPPVisitor.getContainingScope( enumName );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
     */
    public IASTNode getPhysicalNode() {
        return enumName;
    }
    
    public Object clone(){
        IType t = null;
   		try {
            t = (IType) super.clone();
        } catch ( CloneNotSupportedException e ) {
            //not going to happen
        }
        return t;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IEnumeration#getEnumerators()
     */
    public IEnumerator[] getEnumerators() {
        IASTEnumerationSpecifier.IASTEnumerator[] enums = ((IASTEnumerationSpecifier)enumName.getParent()).getEnumerators();
        IEnumerator [] bindings = new IEnumerator [ enums.length ];
        
        for( int i = 0; i < enums.length; i++ ){
            bindings[i] = (IEnumerator) enums[i].getName().resolveBinding();
        }
        return bindings;
    }

}
