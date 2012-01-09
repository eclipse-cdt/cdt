/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Binding for a c++ enumerator.
 */
public class CPPEnumerator extends PlatformObject implements IEnumerator, ICPPInternalBinding {
    private IASTName enumName;

    /**
     * @param enumerator
     */
    public CPPEnumerator(IASTName enumerator) {
        this.enumName = enumerator;
        enumerator.setBinding(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDeclarations()
     */
    @Override
	public IASTNode[] getDeclarations() {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDefinition()
     */
    @Override
	public IASTNode getDefinition() {
        return enumName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    @Override
	public String getName() {
        return new String(getNameCharArray());
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    @Override
	public char[] getNameCharArray() {
        return enumName.getSimpleID();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    @Override
	public IScope getScope() {
        return CPPVisitor.getContainingScope(enumName);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
     */
    public IASTNode getPhysicalNode() {
        return enumName;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IEnumerator#getType()
	 */
	@Override
	public IType getType() {
	    IASTEnumerator etor = (IASTEnumerator) enumName.getParent();
		IASTEnumerationSpecifier enumSpec = (IASTEnumerationSpecifier) etor.getParent();
		return (IType) enumSpec.getName().resolveBinding();
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedName()
     */
    @Override
	public String[] getQualifiedName() {
        return CPPVisitor.getQualifiedName(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedNameCharArray()
     */
    @Override
	public char[][] getQualifiedNameCharArray() {
        return CPPVisitor.getQualifiedNameCharArray(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
     */
    @Override
	public boolean isGloballyQualified() throws DOMException {
        IScope scope = getScope();
        while (scope != null) {
            if (scope instanceof ICPPBlockScope)
                return false;
            scope = scope.getParent();
        }
        return true;
    }

	@Override
	public void addDefinition(IASTNode node) {
	}

	@Override
	public void addDeclaration(IASTNode node) {
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	@Override
	public IBinding getOwner() {
		return CPPVisitor.findDeclarationOwner(enumName, true);
	}

	@Override
	public IValue getValue() {
		final IASTNode parent= enumName.getParent();
		if (parent instanceof ASTEnumerator)
			return ((ASTEnumerator) parent).getIntegralValue();
		
		return Value.UNKNOWN;
	}

	@Override
	public String toString() {
		return getName();
	}
}
