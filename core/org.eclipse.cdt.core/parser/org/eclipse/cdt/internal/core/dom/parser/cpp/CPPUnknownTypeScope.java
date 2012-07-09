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
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.index.IIndexFileSet;

/**
 * Models the scope represented by an unknown type (e.g.: typeof(template type parameter)). Used within
 * the context of templates, only.
 * For safe usage in index bindings, all fields need to be final or used in a thread-safe manner otherwise.
 */
public class CPPUnknownTypeScope implements ICPPScope {
    private final IASTName fName;
	private final ICPPBinding fScopeBinding;

	public CPPUnknownTypeScope(IASTName name, ICPPBinding scopeBinding) {
    	fName= name;
    	fScopeBinding= scopeBinding;
    }

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}

	public IASTNode getPhysicalNode() {
        return fName;
    }

    @Override
	public IName getScopeName() {
        return fName;
    }

    @Override
	public IScope getParent() throws DOMException {
        return fScopeBinding == null ? null : fScopeBinding.getScope();
    }

    @Override
	public IBinding[] find(String name) {
        return IBinding.EMPTY_BINDING_ARRAY;
    }

	@Override
	public final IBinding getBinding(IASTName name, boolean resolve) {
		return getBinding(name, resolve, IIndexFileSet.EMPTY);
	}

    @Override
	public IBinding getBinding(final IASTName name, boolean resolve, IIndexFileSet fileSet) {
    	boolean type= false;
    	boolean function= false;

    	if (name.getPropertyInParent() == null) {
    		type= true;
    	} else {
    		IASTName n= name;
    		IASTNode parent= name.getParent();
    		if (parent instanceof ICPPASTTemplateId) {
    			n= (IASTName) parent;
    			parent= n.getParent();
    		}
    		if (parent instanceof ICPPASTQualifiedName) {
    			ICPPASTQualifiedName qname= (ICPPASTQualifiedName) parent;
    			if (qname.getLastName() != n) {
    				type= true;
    			} else {
    				parent= qname.getParent();
    			}
    		}
    		if (!type) {
    			if (parent instanceof ICPPASTBaseSpecifier ||
    				parent instanceof ICPPASTConstructorChainInitializer) {
    					type= true;
    			} else if (parent instanceof ICPPASTNamedTypeSpecifier) {
    				ICPPASTNamedTypeSpecifier nts= (ICPPASTNamedTypeSpecifier) parent;
    				type= nts.isTypename();
    			} else if (parent instanceof ICPPASTUsingDeclaration) {
    				ICPPASTUsingDeclaration ud= (ICPPASTUsingDeclaration) parent;
    				type= ud.isTypename();
    			}
    			
    			if (!type && parent.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME) {
    				function=  true;
    			}
    		}
    	}
    	
    	int idx= type ? 0 : function ? 1 : 2;

    	IBinding result = getOrCreateBinding(name.getSimpleID(), idx);
        return result;
    }

	protected IBinding getOrCreateBinding(final char[] name, int idx) {
		IBinding result= null;
		switch (idx) {
		case 0:
			result= new CPPUnknownClass(fScopeBinding, name);
			break;
		case 1:
			result= new CPPUnknownFunction(fScopeBinding, name);
			break;
		case 2:
			result= new CPPUnknownBinding(fScopeBinding, name);
			break;
		}
		return result;
	}

	@Override @Deprecated
	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) {
		return getBindings(name, resolve, prefix, IIndexFileSet.EMPTY);
	}

	@Override @Deprecated
	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
		return getBindings(new ScopeLookupData(name, resolve, prefixLookup));
	}

	@Override
	public final IBinding[] getBindings(ScopeLookupData lookup) {
    	if (lookup.isPrefixLookup()) {
    		if (fScopeBinding instanceof ICPPDeferredClassInstance) {
	    		ICPPDeferredClassInstance instance = (ICPPDeferredClassInstance) fScopeBinding;
				IScope scope = instance.getClassTemplate().getCompositeScope();
				if (scope != null) {
					return scope.getBindings(lookup);
				}
    		}
    		return IBinding.EMPTY_BINDING_ARRAY;
    	}
    	IASTName lookupName= lookup.getLookupName();
    	if (lookupName != null)
    		return new IBinding[] {getBinding(lookupName, lookup.isResolve(), lookup.getIncludedFiles())};
    	
    	// When dealing with dependent expressions we always create an unknown class. That is because
    	// unknown objects are not used within the expressions, they are attached to names only.
    	return new IBinding[] {getOrCreateBinding(lookup.getLookupKey(), 0)};
	}

	public ICPPBinding getScopeBinding() {
		return fScopeBinding;
	}

	@Override
	public String toString() {
		return fName.toString();
	}
}
