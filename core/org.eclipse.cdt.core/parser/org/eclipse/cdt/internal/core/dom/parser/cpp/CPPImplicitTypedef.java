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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;

/**
 * The CPPImplicitTypedef is used to represent implicit typedefs that exist on the translation
 * unit but are not actually part of the physical AST created by CDT.
 * 
 * An example is the GCC built-in typedef:  typedef char * __builtin_va_list;
 * 
 * @author dsteffle
 */
public class CPPImplicitTypedef extends CPPTypedef implements ITypedef, ICPPInternalBinding {
    private IType type=null;
    private char[] name=null;
    private IScope scope=null;
    
    public CPPImplicitTypedef(IType type, char[] name, IScope scope) {
        super(null);
        this.type = type;
        this.name = name;
        this.scope = scope;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.ITypedef#getType()
     */
    public IType getType() {
        return type;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    public String getName() {
        return String.valueOf(name);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return name;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() {
        return scope;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
     */
    public boolean isSameType(IType t) {
        if( t == this )
            return true;
        if( t instanceof ITypedef ) {
            IType temp = getType();
            if( temp != null )
                try {
                    return temp.isSameType( ((ITypedef)t).getType());
                } catch (DOMException e) {}
            return false;
        }
            
        IType temp;
        temp = getType();
        if( temp != null )
            return temp.isSameType( t );
        return false;
    }
    
    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    public Object clone(){
        IType t = null;
        t = (IType) super.clone();
        return t;
    }
    
    /**
     * returns null
     */
    public IASTNode[] getDeclarations() {
        return null;
    }
    
    /**
     * returns null
     */
    public IASTNode getDefinition() {
        return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#createDelegate(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public ICPPDelegate createDelegate(IASTName aName) {
        return new CPPTypedefDelegate( aName, this );
    }
    
    /**
     * does nothing
     */
    public void addDefinition(IASTNode node) {
        // do nothing
    }
    
    /**
     * does nothing
     */
    public void addDeclaration(IASTNode node) {
        // do nothing
    }
    
    /**
     * does nothing
     */
    public void removeDeclaration(IASTNode node) {
        // do nothing
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedName()
     */
    public String[] getQualifiedName() {
        String[] temp = new String[1];
        temp[0] = String.valueOf(name);
        
        return temp;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedNameCharArray()
     */
    public char[][] getQualifiedNameCharArray() {
        char[][] temp = new char[1][];
        temp[0] = name;
        
        return temp;
    }
    
    /**
     * returns true
     */
    public boolean isGloballyQualified() {
        return true;
    }
    
}
