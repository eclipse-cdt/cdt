/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;

/**
 * Used to represent built-in variables that exist on the translation
 * unit but are not actually part of the physical AST created by CDT.
 * 
 * An example is the built-in variable __func__.
 */
public class CPPBuiltinVariable extends CPPVariable {
    private IType type=null;
    private char[] name=null;
    private IScope scope=null;
    
    public CPPBuiltinVariable(IType type, char[] name, IScope scope) {
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

	@Override
	public IBinding getOwner() {
		return null;
	}
}
