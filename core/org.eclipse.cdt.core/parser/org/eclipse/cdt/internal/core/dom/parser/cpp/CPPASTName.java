/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;

/**
 * @author jcamelon
 */
public class CPPASTName extends CPPASTNode implements IASTName {

    private char[] name;

    private static final char[] EMPTY_CHAR_ARRAY = {};
	private static final String EMPTY_STRING = "";  //$NON-NLS-1$

    private IBinding binding = null;

    /**
     * @param name
     */
    public CPPASTName(char[] name) {
        this.name = name;
    }

    /**
     * 
     */
    public CPPASTName() {
        name = EMPTY_CHAR_ARRAY;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTName#resolveBinding()
     */
    public IBinding resolveBinding() {
        if (binding == null)
            binding = CPPVisitor.createBinding(this);

        return binding;
    }

    public IBinding[] resolvePrefix() {
    	return CPPSemantics.prefixLookup(this);
    }

    public void setBinding(IBinding binding) {
        this.binding = binding;
    }

    public IBinding getBinding() {
        return binding;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        if (name == EMPTY_CHAR_ARRAY)
            return EMPTY_STRING;
        return new String(name);
    }

    public char[] toCharArray() {
        return name;
    }

    public void setName(char[] name) {
        this.name = name;
    }

    public boolean accept(ASTVisitor action) {
        if (action.shouldVisitNames) {
            switch (action.visit(this)) {
            case ASTVisitor.PROCESS_ABORT:
                return false;
            case ASTVisitor.PROCESS_SKIP:
                return true;
            default:
                break;
            }
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTName#isDeclaration()
     */
    public boolean isDeclaration() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            switch (role) {
            case IASTNameOwner.r_reference:
            case IASTNameOwner.r_unclear:
                return false;
            default:
                return true;
            }
        }
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.cdt.core.dom.ast.IASTName#isReference()
     */
    public boolean isReference() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            switch (role) {
            case IASTNameOwner.r_reference:
                return true;
            default:
                return false;
            }
        }
        return false;
    }

    public boolean isDefinition() {
        IASTNode parent = getParent();
        if (parent instanceof IASTNameOwner) {
            int role = ((IASTNameOwner) parent).getRoleForName(this);
            switch (role) {
            case IASTNameOwner.r_definition:
                return true;
            default:
                return false;
            }
        }
        return false;
    }

}
