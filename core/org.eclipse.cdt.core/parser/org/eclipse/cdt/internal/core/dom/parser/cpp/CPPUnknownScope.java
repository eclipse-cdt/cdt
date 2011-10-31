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
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * Models the scope represented by an unknown binding such (e.g.: template type parameter). Used within
 * the context of templates, only.
 * For safe usage in index bindings, all fields need to be final or used in a thread-safe manner otherwise.
 */
public class CPPUnknownScope implements ICPPInternalUnknownScope {
    private final ICPPUnknownBinding binding;
    private final IASTName scopeName;
    /**
     * This field needs to be protected when used in PDOMCPPUnknownScope, 
     * don't use it outside of {@link #getOrCreateBinding(IASTName, int)}
     */
    private CharArrayObjectMap map;

    public CPPUnknownScope(ICPPUnknownBinding binding, IASTName name) {
        super();
        this.scopeName = name;
        this.binding = binding;
    }

	@Override
	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getScopeName()
     */
    @Override
	public IName getScopeName() {
        return scopeName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
     */
    @Override
	public IScope getParent() throws DOMException {
        return binding.getScope();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
     */
    @Override
	public IBinding[] find(String name) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getPhysicalNode()
     */
    @Override
	public IASTNode getPhysicalNode() {
        return scopeName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#addName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    @Override
	public void addName(IASTName name) {
    }

	@Override
	public final IBinding getBinding(IASTName name, boolean resolve) {
		return getBinding(name, resolve, IIndexFileSet.EMPTY);
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getBinding(org.eclipse.cdt.core.dom.ast.IASTName, boolean)
     */
    @Override
	public IBinding getBinding(final IASTName name, boolean resolve, IIndexFileSet fileSet) {
    	boolean type= false;
    	boolean function= false;

    	if (name.getPropertyInParent() == CPPSemantics.STRING_LOOKUP_PROPERTY) {
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

    	IBinding result = getOrCreateBinding(name, idx);
        return result;
    }

	protected IBinding getOrCreateBinding(final IASTName name, int idx) {
		if (map == null)
            map = new CharArrayObjectMap(2);

        final char[] c = name.getLookupKey();
		IBinding[] o = (IBinding[]) map.get(c);
		if (o == null) {
			o = new IBinding[3];
			map.put(c, o);
		}
        
        IBinding result= o[idx];
        if (result == null) {
        	switch (idx) {
        	case 0:
        		result= new CPPUnknownClass(binding, name.getSimpleID());
        		break;
        	case 1:
        		result= new CPPUnknownFunction(binding, name.getSimpleID());
        		break;
        	case 2:
        		result= new CPPUnknownBinding(binding, name.getSimpleID());
        		break;
        	}
        	o[idx]= result;
        }
		return result;
	}

	@Override
	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) {
		return getBindings(name, resolve, prefix, IIndexFileSet.EMPTY);
	}

    @Override
	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet fileSet) {
    	return getBindings(name, resolve, prefixLookup, fileSet, true);
    }

    @Override
	public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup, IIndexFileSet acceptLocalBindings, boolean checkPointOfDecl) {
    	if (prefixLookup) {
    		if (binding instanceof ICPPDeferredClassInstance) {
	    		ICPPDeferredClassInstance instance = (ICPPDeferredClassInstance) binding;
				IScope scope = instance.getClassTemplate().getCompositeScope();
				if (scope != null) {
					return scope.getBindings(name, resolve, prefixLookup, acceptLocalBindings);
				}
    		}
    		return IBinding.EMPTY_BINDING_ARRAY;
    	}
    	
    	return new IBinding[] {getBinding(name, resolve, acceptLocalBindings)};
	}

	@Override
	public void addBinding(IBinding binding) {
		// do nothing, this is part of template magic and not a normal scope
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownScope#getUnknownBinding()
	 */
	@Override
	public ICPPBinding getScopeBinding() {
		return binding;
	}

	/* (non-Javadoc)
	 * For debug purposes only
	 */
	@Override
	public String toString() {
		return scopeName.toString();
	}

	@Override
	public void populateCache() {}

	@Override
	public void removeNestedFromCache(IASTNode container) {}
}
