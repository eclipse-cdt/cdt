/*******************************************************************************
 * Copyright (c) 2004, 2009 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.ProblemBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Binding for a c++ function parameter
 */
public class CPPParameter extends PlatformObject implements ICPPParameter, ICPPInternalBinding, ICPPTwoPhaseBinding {
    public static class CPPParameterProblem extends ProblemBinding implements ICPPParameter {
        public CPPParameterProblem(IASTNode node, int id, char[] arg) {
            super(node, id, arg);
        }
        public IType getType() throws DOMException {
            throw new DOMException(this);
        }
        public boolean isStatic() throws DOMException {
            throw new DOMException(this);
        }
        public boolean isExtern() throws DOMException {
            throw new DOMException(this);
        }
        public boolean isAuto() throws DOMException {
            throw new DOMException(this);        
        }
        public boolean isRegister() throws DOMException {
            throw new DOMException(this);
        }
		public boolean hasDefaultValue() {
            return false;
		}
		public boolean isMutable() throws DOMException {
            throw new DOMException(this);
		}
		public String[] getQualifiedName() throws DOMException {
            throw new DOMException(this);
		}
		public char[][] getQualifiedNameCharArray() throws DOMException {
            throw new DOMException(this);
		}
		public boolean isGloballyQualified() throws DOMException {
            throw new DOMException(this);
		}
		public boolean isExternC() {
			return false;
		}
		public IValue getInitialValue() {
			return null;
		}
    }

	private IType type = null;
	private IASTName[] declarations = null;
	private int fPosition;
	
	
	public CPPParameter(IASTName name, int pos) {
		this.declarations = new IASTName[] { name };
		fPosition= pos;
	}
	
	public CPPParameter(IType type, int pos) {
	    this.type = type;
	    fPosition= pos;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPBinding#getDeclarations()
     */
    public IASTNode[] getDeclarations() {
        return declarations;
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
		if (declarations == null) {
	        declarations = new IASTName[] { name };
		} else {
	        //keep the lowest offset declaration in[0]
			if (declarations.length > 0 && ((ASTNode)node).getOffset() < ((ASTNode)declarations[0]).getOffset()) {
				declarations = (IASTName[]) ArrayUtil.prepend(IASTName.class, declarations, name);
			} else {
				declarations = (IASTName[]) ArrayUtil.append(IASTName.class, declarations, name);
			}
	    }
	}
	
	private IASTName getPrimaryDeclaration() {
	    if (declarations != null) {
	        for (int i = 0; i < declarations.length && declarations[i] != null; i++) {
	            IASTNode node = declarations[i].getParent();
	            while (!(node instanceof IASTDeclaration))
	                node = node.getParent();
	            
	            if (node instanceof IASTFunctionDefinition)
	                return declarations[i];
	        }
	        return declarations[0];
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
	    if (declarations != null)
	        return declarations[0];
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IVariable#getType()
	 */
	public IType getType() {
		if (type == null && declarations != null) {
			IType t= CPPVisitor.createType((IASTDeclarator) declarations[0].getParent());
			type= SemanticUtil.adjustParameterType(t, false);
		}
		return type;
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
			while (!(parent instanceof IASTParameterDeclaration))
				parent = parent.getParent();
			IASTDeclSpecifier declSpec = ((IASTParameterDeclaration) parent).getDeclSpecifier();
			if (declSpec.getStorageClass() == storage)
				return true;
		}
		return false;
	}

	public IASTInitializer getDefaultValue() {
		if (declarations == null)
			return null;
		for (int i = 0; i < declarations.length && declarations[i] != null; i++) {
			IASTNode parent = declarations[i].getParent();
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
	
	public IBinding getOwner() throws DOMException {
		return CPPVisitor.findEnclosingFunction(declarations[0]);
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
