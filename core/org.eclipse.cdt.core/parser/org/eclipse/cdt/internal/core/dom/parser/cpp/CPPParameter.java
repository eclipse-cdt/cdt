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
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameterPackType;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Binding for a c++ function parameter
 */
public class CPPParameter extends PlatformObject implements ICPPParameter, ICPPInternalBinding, ICPPTwoPhaseBinding {
    public static class CPPParameterProblem extends ProblemBinding implements ICPPParameter {
        public CPPParameterProblem(IASTNode node, int id, char[] arg) {
            super(node, id, arg);
        }
    }

	private IType fType = null;
	private IASTName[] fDeclarations = null;
	private int fPosition;
	
	
	public CPPParameter(IASTName name, int pos) {
		this.fDeclarations = new IASTName[] { name };
		fPosition= pos;
	}
	
	public CPPParameter(IType type, int pos) {
	    this.fType = type;
	    fPosition= pos;
	}
	
    public boolean isParameterPack() {
		return getType() instanceof ICPPParameterPackType;
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
        return fDeclarations;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDefinition()
     */
    public IASTNode getDefinition() {
        return null;
    }

	public void addDeclaration(IASTNode node) {
		if (!(node instanceof IASTName))
			return;
		IASTName name = (IASTName) node;
		if (fDeclarations == null) {
	        fDeclarations = new IASTName[] { name };
		} else {
	        //keep the lowest offset declaration in[0]
			if (fDeclarations.length > 0 && ((ASTNode)node).getOffset() < ((ASTNode)fDeclarations[0]).getOffset()) {
				fDeclarations = (IASTName[]) ArrayUtil.prepend(IASTName.class, fDeclarations, name);
			} else {
				fDeclarations = (IASTName[]) ArrayUtil.append(IASTName.class, fDeclarations, name);
			}
	    }
	}
	
	private IASTName getPrimaryDeclaration() {
	    if (fDeclarations != null) {
	        for (int i = 0; i < fDeclarations.length && fDeclarations[i] != null; i++) {
	            IASTNode node = fDeclarations[i].getParent();
	            while (!(node instanceof IASTDeclaration))
	                node = node.getParent();
	            
	            if (node instanceof IASTFunctionDefinition)
	                return fDeclarations[i];
	        }
	        return fDeclarations[0];
	    }
	    return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
	 */
	public String getName() {
		return new String(getNameCharArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
	 */
	public char[] getNameCharArray() {
	    IASTName name = getPrimaryDeclaration();
	    if (name != null)
	        return name.getSimpleID();
	    return CharArrayUtils.EMPTY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
	 */
	public IScope getScope() {
		return CPPVisitor.getContainingScope(getPrimaryDeclaration());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
	 */
	public IASTNode getPhysicalNode() {
	    if (fDeclarations != null)
	        return fDeclarations[0];
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */
	public IType getType() {
		if (fType == null && fDeclarations != null) {
			IASTNode parent= fDeclarations[0].getParent();
			while (parent != null) {
				if (parent instanceof ICPPASTParameterDeclaration) {
					fType= CPPVisitor.createParameterType((ICPPASTParameterDeclaration) parent, false);
					break;
				}
				parent= parent.getParent();
			}
		}
		return fType;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isStatic()
     */
    public boolean isStatic() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedName()
     */
    public String[] getQualifiedName() {
        return new String[] { getName() };
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getFullyQualifiedNameCharArray()
     */
    public char[][] getQualifiedNameCharArray() {
        return new char[][] { getNameCharArray() };
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding#isGloballyQualified()
     */
    public boolean isGloballyQualified() {
        return false;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding#addDefinition(org.eclipse.cdt.core.dom.ast.IASTNode)
	 */
	public void addDefinition(IASTNode node) {
		addDeclaration(node);
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isExtern()
     */
    public boolean isExtern() {
        //7.1.1-5 extern can not be used in the declaration of a parameter
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable#isMutable()
     */
    public boolean isMutable() {
        //7.1.1-8 mutable can only apply to class members
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isAuto()
     */
    public boolean isAuto() {
        return hasStorageClass(IASTDeclSpecifier.sc_auto);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IVariable#isRegister()
     */
    public boolean isRegister() {
        return hasStorageClass(IASTDeclSpecifier.sc_register);
    }
    
    public boolean hasStorageClass(int storage) {
		IASTNode[] ns = getDeclarations();
		if (ns == null)
			return false;

		for (int i = 0; i < ns.length && ns[i] != null; i++) {
			IASTNode parent = ns[i].getParent();
			while (parent != null && !(parent instanceof IASTParameterDeclaration))
				parent = parent.getParent();
			if (parent != null) {
				IASTDeclSpecifier declSpec = ((IASTParameterDeclaration) parent).getDeclSpecifier();
				if (declSpec.getStorageClass() == storage)
					return true;
			}
		}
		return false;
	}

	public IASTInitializer getDefaultValue() {
		if (fDeclarations == null)
			return null;
		for (int i = 0; i < fDeclarations.length && fDeclarations[i] != null; i++) {
			IASTNode parent = fDeclarations[i].getParent();
			while (parent.getPropertyInParent() == IASTDeclarator.NESTED_DECLARATOR)
				parent = parent.getParent();
			IASTInitializer init = ((IASTDeclarator)parent).getInitializer();
			if (init != null)
				return init;
		}
		return null;
	}
	
	public boolean hasDefaultValue() {
		return getDefaultValue() != null;
	}
	
	public ILinkage getLinkage() {
		return Linkage.CPP_LINKAGE;
	}

	public boolean isExternC() {
		return false;
	}
	
	@Override
	public String toString() {
		String name = getName();
		return name.length() != 0 ? name : "<unnamed>"; //$NON-NLS-1$
	}
	
	public IBinding getOwner() {
		return CPPVisitor.findEnclosingFunction(fDeclarations[0]);
	}

	public IValue getInitialValue() {
		return null;
	}

	public IBinding resolveFinalBinding(CPPASTNameBase name) {
		// check if the binding has been updated.
		IBinding current= name.getPreBinding();
		if (current != this)
			return current;
		
		IASTNode node= getPrimaryDeclaration();
		while (node != null && !(node instanceof IASTFunctionDeclarator)) {
			node= node.getParent();
		}
		if (node instanceof IASTFunctionDeclarator) {
			IASTName funcName= ASTQueries.findInnermostDeclarator((IASTFunctionDeclarator) node).getName();
			IBinding b= funcName.resolvePreBinding();
			if (b instanceof ICPPInternalFunction) {
				return ((ICPPInternalFunction) b).resolveParameter(this);
			}
		}
		return this;
	}

	public int getParameterPosition() {
		return fPosition;
	}
}
