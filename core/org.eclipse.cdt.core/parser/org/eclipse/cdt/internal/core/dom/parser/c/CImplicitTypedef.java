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
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;

/**
 * The CImplicitTypedef is used to represent implicit typedefs that exist on the translation
 * unit but are not actually part of the physical AST created by CDT.
 * 
 * An example is the GCC built-in typedef:  typedef char * __builtin_va_list;
 *  
 * @author dsteffle
 */
public class CImplicitTypedef extends CTypedef implements ITypedef, ICInternalBinding {
    private IType type=null;
    private char[] name=null;
    private IScope scope=null;
    
    public CImplicitTypedef(IType type, char[] name, IScope scope) {
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
//    public boolean isSameType(IType t) {
//        if( t == this )
//            return true;
//        if( t instanceof ITypedef )
//            try {
//                IType temp = getType();
//                if( temp != null )
//                    return temp.isSameType( ((ITypedef)t).getType());
//                return false;
//            } catch ( DOMException e ) {
//                return false;
//            }
//            
//            IType temp;
//            temp = getType();
//            if( temp != null )
//                return temp.isSameType( t );
//            return false;
//    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#clone()
     */
//    public Object clone(){
//        IType t = null;
//        t = (IType) super.clone();
//        return t;
//    }
    
    /**
     * returns null
     * @return
     */
    public IASTNode getPhysicalNode() {
        return null;
    }
    
}
