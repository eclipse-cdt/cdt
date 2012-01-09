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
package org.eclipse.cdt.internal.core.dom.parser.c;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.Value;
import org.eclipse.core.runtime.PlatformObject;

/**
 * C-specific binding for enumerators.
 */
public class CEnumerator extends PlatformObject implements IEnumerator {
    public static class CEnumeratorProblem extends ProblemBinding implements IEnumerator {
        public CEnumeratorProblem(IASTNode node, int id, char[] arg) {
            super(node, id, arg);
        }
		@Override
		public IValue getValue() {
			return Value.UNKNOWN;
		}
    }

    private final IASTName enumeratorName;

    public CEnumerator(IASTEnumerator enumtor) {
		this.enumeratorName = enumtor.getName();
		enumeratorName.setBinding(this);
	}
    
    public IASTNode getPhysicalNode() {
        return enumeratorName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    @Override
	public String getName() {
        return enumeratorName.toString();
    }

    @Override
	public char[] getNameCharArray() {
        return enumeratorName.toCharArray();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    @Override
	public IScope getScope() {
        return CVisitor.getContainingScope(enumeratorName.getParent());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IEnumerator#getType()
	 */
	@Override
	public IType getType() {
		return (IType) getOwner();
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.C_LINKAGE;
	}

	@Override
	public IBinding getOwner() {
	    IASTEnumerator etor = (IASTEnumerator) enumeratorName.getParent();
		IASTEnumerationSpecifier enumSpec = (IASTEnumerationSpecifier) etor.getParent();
		return enumSpec.getName().resolveBinding();
	}

	@Override
	public IValue getValue() {
		IASTNode parent= enumeratorName.getParent();
		if (parent instanceof ASTEnumerator) 
			return ((ASTEnumerator) parent).getIntegralValue();
		
		return Value.UNKNOWN;
	}

	@Override
	public String toString() {
		return getName();
	}
}
