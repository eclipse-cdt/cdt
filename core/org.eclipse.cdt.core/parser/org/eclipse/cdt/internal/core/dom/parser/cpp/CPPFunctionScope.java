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
    private CharArrayObjectMap<ILabel> labels = CharArrayObjectMap.emptyMap();
    
	/**
	 * @param physicalNode
	 */
	public CPPFunctionScope(IASTFunctionDeclarator physicalNode) {
		super(physicalNode);
	}
	
	@Override
	public EScopeKind getKind() {
		return EScopeKind.eLocal;
	}

	@Override
	public void addBinding(IBinding binding) {
	    // 3.3.4 only labels have function scope.
	    if (!(binding instanceof ILabel))
	        return;

	    if (labels == CharArrayObjectMap.EMPTY_MAP)
	        labels = new CharArrayObjectMap<>(2);

	    labels.put(binding.getNameCharArray(), (ILabel) binding);
	}

	@Override
	public IBinding[] find(String name) {
	    char[] n = name.toCharArray();
	    List<IBinding> bindings = new ArrayList<>();

	    for (int i = 0; i < labels.size(); i++) {
	    	char[] key = labels.keyAt(i);
	    	if (CharArrayUtils.equals(key, n)) {
	    		bindings.add(labels.get(key));
	    	}
	    }
	    
	    IBinding[] additional = super.find(name);
	    for (IBinding element : additional) {
	    	bindings.add(element);
	    }
	    
	    return bindings.toArray(new IBinding[bindings.size()]);
	}
	
	@Override
	public IScope getParent() {
	    // We can't just resolve the function and get its parent scope, since there are cases where that 
	    // could loop because resolving functions requires resolving their parameter types.
	    IASTFunctionDeclarator fdtor = (IASTFunctionDeclarator) getPhysicalNode();
	    IASTName name = fdtor.getName().getLastName();
	    return CPPVisitor.getContainingNonTemplateScope(name);
	}

    @Override
	public IScope getBodyScope() {
        IASTFunctionDeclarator fnDtor = (IASTFunctionDeclarator) getPhysicalNode();
        IASTNode parent = fnDtor.getParent();
        if (parent instanceof IASTFunctionDefinition) {
            IASTStatement body = ((IASTFunctionDefinition) parent).getBody();
            if (body instanceof IASTCompoundStatement)
                return ((IASTCompoundStatement) body).getScope();
        }
        return null;
    }

    @Override
	public IName getScopeName() {
        IASTNode node = getPhysicalNode();
        if (node instanceof IASTDeclarator) {
            return ((IASTDeclarator) node).getName();
        }
        return null;
    }
}
