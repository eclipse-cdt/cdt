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
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

/**
 * Scope of a function, containing labels.
 */
public class CPPFunctionScope extends CPPScope implements ICPPFunctionScope {

    private CharArrayObjectMap labels = CharArrayObjectMap.EMPTY_MAP;
    
	/**
	 * @param physicalNode
	 */
	public CPPFunctionScope(IASTFunctionDeclarator physicalNode) {
		super(physicalNode);
	}
	
	public EScopeKind getKind() {
		return EScopeKind.eLocal;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#addBinding(org.eclipse.cdt.core.dom.ast.IBinding)
	 */
	@Override
	public void addBinding(IBinding binding) {
	    //3.3.4 only labels have function scope
	    if (!(binding instanceof ILabel))
	        return;
	    
	    if (labels == CharArrayObjectMap.EMPTY_MAP)
	        labels = new CharArrayObjectMap(2);
	    
	    labels.put(binding.getNameCharArray(), binding);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getBinding(int, char[])
	 */
	public IBinding getBinding(IASTName name) {
	    return (IBinding) labels.get(name.getLookupKey());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.dom.ast.IScope#find(java.lang.String)
	 */
	@Override
	public IBinding[] find(String name) {
	    char[] n = name.toCharArray();
	    List<IBinding> bindings = new ArrayList<IBinding>();
	    
	    for (int i = 0; i < labels.size(); i++) {
	    	char[] key = labels.keyAt(i);
	    	if (CharArrayUtils.equals(key, n)) {
	    		bindings.add((IBinding) labels.get(key));
	    	}
	    }
	    
	    IBinding[] additional = super.find(name);
	    for (IBinding element : additional) {
	    	bindings.add(element);
	    }
	    
	    return bindings.toArray(new IBinding[bindings.size()]);
	}
	
	@Override
	public IScope getParent() throws DOMException {
	    //we can't just resolve the function and get its parent scope, since there are cases where that 
	    //could loop since resolving functions requires resolving their parameter types
	    IASTFunctionDeclarator fdtor = (IASTFunctionDeclarator) getPhysicalNode();
	    IASTName name = fdtor.getName().getLastName();
	    return CPPVisitor.getContainingNonTemplateScope(name);
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionScope#getBodyScope()
     */
    public IScope getBodyScope() {
        IASTFunctionDeclarator fnDtor = (IASTFunctionDeclarator) getPhysicalNode();
        IASTNode parent = fnDtor.getParent();
        if (parent instanceof IASTFunctionDefinition) {
            IASTStatement body = ((IASTFunctionDefinition)parent).getBody();
            if (body instanceof IASTCompoundStatement)
                return ((IASTCompoundStatement)body).getScope();
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.cpp.ICPPScope#getScopeName()
     */
    @Override
	public IName getScopeName() {
        IASTNode node = getPhysicalNode();
        if (node instanceof IASTDeclarator) {
            return ((IASTDeclarator)node).getName();
        }
        return null;
    }
}
