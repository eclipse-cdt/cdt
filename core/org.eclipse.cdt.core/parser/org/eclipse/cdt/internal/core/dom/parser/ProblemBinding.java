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
 *     Bryan Wilkinson (QNX)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.Linkage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.parser.ParserMessages;
import org.eclipse.core.runtime.PlatformObject;

import com.ibm.icu.text.MessageFormat;

/**
 * Implementation of problem bindings
 */
public class ProblemBinding extends PlatformObject implements IProblemBinding, IASTInternalScope {
	public static ProblemBinding NOT_INITIALIZED= new ProblemBinding(null, 0);
	
    protected final int id;
    protected char[] arg;
    protected IASTNode node;
    private final String message = null;
	private IBinding[] candidateBindings;
    
    public ProblemBinding(IASTName name, int id) {
    	this(name, id, null, null);
    }

    public ProblemBinding(IASTName name, int id, IBinding[] candidateBindings) {
    	this(name, id, null, candidateBindings);
    }

    public ProblemBinding(IASTNode node, int id, char[] arg) {
    	this(node, id, arg, null);
    }

    public ProblemBinding(IASTNode node, int id, char[] arg, IBinding[] candidateBindings) {
        this.id = id;
        this.arg = arg;
        this.node = node;
		this.candidateBindings = candidateBindings;
    }
    
	@Override
	public EScopeKind getKind() {
		return EScopeKind.eLocal;
	}

    @Override
	public IASTNode getASTNode() {
        return node;
    }

	@Override
	public IBinding[] getCandidateBindings() {
		return candidateBindings != null ? candidateBindings : IBinding.EMPTY_BINDING_ARRAY;
	}
	
	public void setCandidateBindings(IBinding[] foundBindings) {
		candidateBindings= foundBindings;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IProblemBinding#getID()
     */
    @Override
	public int getID() {
        return id;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IProblemBinding#getMessage()
     */
    @Override
	public String getMessage() {
        if (message != null)
            return message;

        String msg = ParserMessages.getProblemPattern(this);
        if (msg == null)
        	return ""; //$NON-NLS-1$
        
        if (arg == null && node instanceof IASTName)
        	arg= ((IASTName) node).toCharArray();
        
        if (arg != null) {
            msg = MessageFormat.format(msg, new Object[] { new String(arg) });
        }

		return msg;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getName()
     */
    @Override
	public String getName() {
        return node instanceof IASTName ? new String(((IASTName) node).getSimpleID()) : CPPSemantics.EMPTY_NAME;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    @Override
	public char[] getNameCharArray() {
        return node instanceof IASTName ? ((IASTName) node).getSimpleID() : CharArrayUtils.EMPTY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    @Override
	public IScope getScope() throws DOMException {
        throw new DOMException(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
     */
    @Override
	public IASTNode getPhysicalNode() {
        return getASTNode();
    }

    
    @Override
	public Object clone() {
    	// Don't clone problems
        return this;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
     */
    @Override
	public IScope getParent() throws DOMException {
        throw new DOMException(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
     */
    @Override
	public IBinding[] find(String name) {
        return IBinding.EMPTY_BINDING_ARRAY;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#getScopeName()
	 */
	@Override
	public IName getScopeName() {
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#addName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    @Override
	public void addName(IASTName name) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getBinding(org.eclipse.cdt.core.dom.ast.IASTName, boolean)
     */
    @Override
	public IBinding getBinding(IASTName name, boolean resolve) {
        return null;
    }

	@Override
	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) {
        return IBinding.EMPTY_BINDING_ARRAY;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getBinding(org.eclipse.cdt.core.dom.ast.IASTName, boolean)
     */
    @Override
	public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getBinding(org.eclipse.cdt.core.dom.ast.IASTName, boolean)
     */
    @Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
        return IBinding.EMPTY_BINDING_ARRAY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
     */
    @Override
	public boolean isSameType(IType type) {
        return type == this;
    }

	@Override
	public String getFileName() {
		if (node != null)
			return node.getContainingFilename();

		return ""; //$NON-NLS-1$
	}

	@Override
	public int getLineNumber() {
		if (node != null) {
			IASTFileLocation fileLoc = node.getFileLocation();
			if (fileLoc != null)
				return fileLoc.getStartingLineNumber();
		}
		return -1;
	}

	@Override
	public void addBinding(IBinding binding) {
	}

	@Override
	public ILinkage getLinkage() {
		return Linkage.NO_LINKAGE;
	}
	
	@Override
	public String toString() {
		return getMessage();
	}

	@Override
	public IBinding getOwner() {
		if (node instanceof IASTName) {
			IASTTranslationUnit tu= node.getTranslationUnit();
			if (tu instanceof ICPPASTTranslationUnit) {
				return CPPVisitor.findNameOwner((IASTName) node, true);
			}
		}
		return null;
	}

	public void setASTNode(IASTName name) {
		if (name != null) {
			this.node= name;
			this.arg= null;
		}
	}

	@Override
	public void populateCache() {
	}

	@Override
	public void removeNestedFromCache(IASTNode container) {}

	// Dummy methods for derived classes
    public IType getType() {
    	return new ProblemType(getID());
    }
    public boolean isStatic() {
    	return false;
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
    public boolean isMutable() {
    	return false;
    }
    public boolean isExtern() {
    	return false;
    }
    public boolean isExternC() {
    	return false;
    }
    public boolean isAuto() {
    	return false;
    }
    public boolean isRegister() {
    	return false;
    }
	public IValue getInitialValue() {
		return null;
	}
	public boolean isAnonymous() {
		return false;
	}
	public boolean isDeleted() {
		return false;
	}
    public boolean isInline() {
    	return false;
    }
    public boolean takesVarArgs() {
    	return false;
    }
	public IType[] getExceptionSpecification() {
        return null;
	}
	public boolean hasParameterPack() {
		return false;
	}
	public boolean isVirtual() {
		return false;
	}
	public boolean isPureVirtual() {
		return false;
	}
	public boolean isImplicit() {
		return false;
	}
    public boolean isExplicit() {
        return false;
    }
	public boolean hasDefaultValue() {
        return false;
	}
	public boolean isParameterPack() {
		return false;
	}
}
