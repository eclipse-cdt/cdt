/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;

/**
 * Models the scope represented by an unknown binding such (e.g.: template type parameter). Used within
 * the context of templates, only.
 */
public class CPPUnknownScope implements ICPPScope, ICPPInternalUnknownScope {
    private final ICPPUnknownBinding binding;
    private final IASTName scopeName;
    private CharArrayObjectMap map;

    public CPPUnknownScope(ICPPUnknownBinding binding, IASTName name) {
        super();
        this.scopeName = name;
        this.binding = binding;
    }

	public EScopeKind getKind() {
		return EScopeKind.eClassType;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getScopeName()
     */
    public IName getScopeName() {
        return scopeName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getParent()
     */
    public IScope getParent() throws DOMException {
        return binding.getScope();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
     */
    public IBinding[] find(String name) {
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getPhysicalNode()
     */
    public IASTNode getPhysicalNode() {
        return scopeName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#addName(org.eclipse.cdt.core.dom.ast.IASTName)
     */
    public void addName(IASTName name) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#removeBinding(org.eclipse.cdt.core.dom.ast.IBinding)
     */
    public void removeBinding(IBinding binding) {
    }

	public final IBinding getBinding(IASTName name, boolean resolve) throws DOMException {
		return getBinding(name, resolve, IIndexFileSet.EMPTY);
	}

	public final IBinding[] getBindings(IASTName name, boolean resolve, boolean prefix) throws DOMException {
		return getBindings(name, resolve, prefix, IIndexFileSet.EMPTY);
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#getBinding(org.eclipse.cdt.core.dom.ast.IASTName, boolean)
     */
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
    			if (parent instanceof ICPPASTNamedTypeSpecifier ||
    				parent instanceof ICPPASTBaseSpecifier ||
    				parent instanceof ICPPASTConstructorChainInitializer) {
    					type= true;
    			} else if (parent.getPropertyInParent() == IASTFunctionCallExpression.FUNCTION_NAME) {
    				function=  true;
    			}
    		}
    	}
    	
        if (map == null)
            map = new CharArrayObjectMap(2);

        char[] c = name.toCharArray();
        IBinding[] o= (IBinding[]) map.get(c);
        if (o == null) {
        	o= new IBinding[3];
        	map.put(c, o);
        }
        
        int idx= type ? 0 : function ? 1 : 2;
        IBinding result= o[idx];
        if (result == null) {
        	if (type) {
        		result= new CPPUnknownClass(binding, name.getLastName());
        	} else if (function) {
        		result= new CPPUnknownFunction(binding, name.getLastName());
        	} else {
        		result= new CPPUnknownBinding(binding, name.getLastName());
        	}
        	o[idx]= result;
        }
        return result;
    }

    public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup,	IIndexFileSet fileSet) {
    	if (prefixLookup)
    		return IBinding.EMPTY_BINDING_ARRAY;
    	
    	return new IBinding[] {getBinding(name, resolve, fileSet)};
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#setFullyCached(boolean)
     */
    public void setFullyCached(boolean b) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#isFullyCached()
     */
    public boolean isFullyCached() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IScope#flushCache()
     */
    public void flushCache() {
    }

	public void addBinding(IBinding binding) {
		// do nothing, this is part of template magic and not a normal scope
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalUnknownScope#getUnknownBinding()
	 */
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
}
