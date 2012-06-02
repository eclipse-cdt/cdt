/*******************************************************************************
 * Copyright (c) 2005, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Devin Steffler (IBM) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;

/**
 * The CPPImplicitTypedef is used to represent implicit typedefs that exist on the translation
 * unit but are not actually part of the physical AST created by CDT.
 * 
 * An example is the GCC built-in typedef:  typedef char * __builtin_va_list;
 */
public class CPPImplicitTypedef extends CPPTypedef {
    private IType type;
    private char[] name;
    private IScope scope;
    
    public CPPImplicitTypedef(IType type, char[] name, IScope scope) {
        super(null);
        this.type = type;
        this.name = name;
        this.scope = scope;
    }
    
    @Override
	public IType getType() {
        return type;
    }

    @Override
	public String getName() {
        return String.valueOf(name);
    }
    
    @Override
	public char[] getNameCharArray() {
        return name;
    }
    
    @Override
	public IScope getScope() {
        return scope;
    }
    
    @Override
	public boolean isSameType(IType t) {
		if (t == this)
			return true;
		if (t instanceof ITypedef) {
			IType temp = getType();
			if (temp != null)
				return temp.isSameType(((ITypedef) t).getType());
			return false;
		}

		IType temp;
		temp = getType();
		if (temp != null)
			return temp.isSameType(t);
		return false;
	}
    
    @Override
	public Object clone(){
        IType t = null;
        t = (IType) super.clone();
        return t;
    }
    
    /**
     * returns null
     */
    @Override
	public IASTNode[] getDeclarations() {
        return null;
    }
    
    /**
     * returns null
     */
    @Override
	public IASTNode getDefinition() {
        return null;
    }
        
    /**
     * does nothing
     */
    @Override
	public void addDefinition(IASTNode node) {
        // do nothing
    }
    
    /**
     * does nothing
     */
    @Override
	public void addDeclaration(IASTNode node) {
        // do nothing
    }
    
    @Override
	public String[] getQualifiedName() {
        String[] temp = new String[1];
        temp[0] = String.valueOf(name);
        
        return temp;
    }
    

    @Override
	public char[][] getQualifiedNameCharArray() {
        char[][] temp = new char[1][];
        temp[0] = name;
        
        return temp;
    }
    
    /**
     * returns true
     */
    @Override
	public boolean isGloballyQualified() {
        return true;
    }
}
