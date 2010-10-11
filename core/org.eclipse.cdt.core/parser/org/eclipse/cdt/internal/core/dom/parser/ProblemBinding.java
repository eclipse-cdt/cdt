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
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.IValue;
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
    
	public EScopeKind getKind() {
		return EScopeKind.eLocal;
	}

    public IASTNode getASTNode() {
        return node;
    }

	public IBinding[] getCandidateBindings() {
		return candidateBindings != null ? candidateBindings : IBinding.EMPTY_BINDING_ARRAY;
	}
	
	public void setCandidateBindings(IBinding[] foundBindings) {
		candidateBindings= foundBindings;
	}

    private String getMessagePattern() {
    	String result= ParserMessages.getProblemPattern(this);
    	if (result != null)
    		return result;
    	
    	// mstodo remove after reworking problem ids.
    	String key= getMessageKey();
    	if (key != null)
    		return ParserMessages.getString(key);
    	return ""; //$NON-NLS-1$
    }
    
    private String getMessageKey() {
    	switch(id) {
    	case SEMANTIC_NAME_NOT_FOUND:   						return "ASTProblemFactory.error.semantic.nameNotFound"; //$NON-NLS-1$
    	case SEMANTIC_AMBIGUOUS_LOOKUP:   						return "ASTProblemFactory.error.semantic.pst.ambiguousLookup"; //$NON-NLS-1$ 
    	case SEMANTIC_INVALID_TYPE:   							return "ASTProblemFactory.error.semantic.pst.invalidType"; //$NON-NLS-1$ 
    	case SEMANTIC_CIRCULAR_INHERITANCE:   					return "ASTProblemFactory.error.semantic.pst.circularInheritance"; //$NON-NLS-1$
    	case SEMANTIC_INVALID_OVERLOAD:   						return "ASTProblemFactory.error.semantic.pst.invalidOverload"; //$NON-NLS-1$
    	case SEMANTIC_INVALID_USING:   							return "ASTProblemFactory.error.semantic.pst.invalidUsing"; //$NON-NLS-1$
    	case SEMANTIC_DEFINITION_NOT_FOUND:   					return "ASTProblemFactory.error.semantic.dom.definitionNotFound"; //$NON-NLS-1$
    	case SEMANTIC_KNR_PARAMETER_DECLARATION_NOT_FOUND:   	return "ASTProblemFactory.error.semantic.dom.knrParameterDeclarationNotFound"; //$NON-NLS-1$
    	case SEMANTIC_LABEL_STATEMENT_NOT_FOUND:   				return "ASTProblemFactory.error.semantic.dom.labelStatementNotFound"; //$NON-NLS-1$
    	case SEMANTIC_INVALID_REDEFINITION:   					return "ASTProblemFactory.error.semantic.dom.invalidRedefinition"; //$NON-NLS-1$
    	case SEMANTIC_INVALID_REDECLARATION:   					return "ASTProblemFactory.error.semantic.dom.invalidRedeclaration"; //$NON-NLS-1$
    	case SEMANTIC_BAD_SCOPE:   								return "ASTProblemFactory.error.semantic.dom.badScope"; //$NON-NLS-1$
    	case SEMANTIC_RECURSION_IN_LOOKUP:   					return "ASTProblemFactory.error.semantic.dom.recursionInResolution"; //$NON-NLS-1$
    	case SEMANTIC_MEMBER_DECLARATION_NOT_FOUND:   			return "ASTProblemFactory.error.semantic.dom.memberDeclNotFound"; //$NON-NLS-1$
    	case SEMANTIC_INVALID_TEMPLATE_ARGUMENTS:   			return "ASTProblemFactory.error.semantic.dom.invalidTemplateArgs"; //$NON-NLS-1$
    	}
    	return null;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IProblemBinding#getID()
     */
    public int getID() {
        return id;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IProblemBinding#getMessage()
     */
    public String getMessage() {
        if (message != null)
            return message;

        String msg = getMessagePattern();
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
    public String getName() {
        return node instanceof IASTName ? new String(((IASTName) node).getSimpleID()) : CPPSemantics.EMPTY_NAME;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return node instanceof IASTName ? ((IASTName) node).getSimpleID() : CharArrayUtils.EMPTY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getScope()
     */
    public IScope getScope() throws DOMException {
        throw new DOMException(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IBinding#getPhysicalNode()
     */
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
    public IScope getParent() throws DOMException {
        throw new DOMException(this);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
     */
    public IBinding[] find(String name) {
        return IBinding.EMPTY_BINDING_ARRAY;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#getScopeName()
	 */
	public IName getScopeName() {
		return null;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#addName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void addName(IASTName name) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getBinding(org.eclipse.cdt.core.dom.ast.IASTName, boolean)
     */
    public IBinding getBinding(IASTName name, boolean resolve) {
        return null;
    }

	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) {
        return IBinding.EMPTY_BINDING_ARRAY;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getBinding(org.eclipse.cdt.core.dom.ast.IASTName, boolean)
     */
    public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getBinding(org.eclipse.cdt.core.dom.ast.IASTName, boolean)
     */
    public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
        return IBinding.EMPTY_BINDING_ARRAY;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IType#isSameType(org.eclipse.cdt.core.dom.ast.IType)
     */
    public boolean isSameType(IType type) {
        return type == this;
    }

	public String getFileName() {
		if (node != null)
			return node.getContainingFilename();

		return ""; //$NON-NLS-1$
	}

	public int getLineNumber() {
		if (node != null) {
			IASTFileLocation fileLoc = node.getFileLocation();
			if (fileLoc != null)
				return fileLoc.getStartingLineNumber();
		}
		return -1;
	}

	public void addBinding(IBinding binding) {
	}

	public ILinkage getLinkage() {
		return Linkage.NO_LINKAGE;
	}
	
	@Override
	public String toString() {
		return getMessage();
	}

	public IBinding getOwner() {
		return node instanceof IASTName ? CPPVisitor.findNameOwner((IASTName) node, true) : null;
	}

	public void setASTNode(IASTName name) {
		if (name != null) {
			this.node= name;
			this.arg= null;
		}
	}

	public void populateCache() {
	}
	
	
	// Dummy methods for derived classes
    public IType getType() throws DOMException {
        throw new DOMException(this);
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
