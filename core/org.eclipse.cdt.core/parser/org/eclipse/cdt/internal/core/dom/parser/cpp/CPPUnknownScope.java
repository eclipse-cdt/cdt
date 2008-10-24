/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Bryan Wilkinson (QNX)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.index.IIndexFileSet;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPTemplates;

/**
 * @author aniefer
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
    public IBinding getBinding(IASTName name, boolean resolve, IIndexFileSet fileSet) {
        if (map == null)
            map = new CharArrayObjectMap(2);

        char[] c = name.toCharArray();
        if (map.containsKey(c)) {
            return (IBinding) map.get(c);
        }

        IBinding b;
        IASTNode parent = name.getParent();
        if (parent instanceof ICPPASTTemplateId) {
        	ICPPTemplateArgument[] arguments = CPPTemplates.createTemplateArgumentArray((ICPPASTTemplateId) parent);
        	b = new CPPUnknownClassInstance(binding, name, arguments);
        } else {
        	b = new CPPUnknownClass(binding, name);
        }

        name.setBinding(b);
        map.put(c, b);
        return b;
    }

    public IBinding[] getBindings(IASTName name, boolean resolve, boolean prefixLookup,
    		IIndexFileSet fileSet) {
        if (map == null)
            map = new CharArrayObjectMap(2);

        char[] c = name.toCharArray();

	    IBinding[] result = null;
	    if (prefixLookup) {
	    	Object[] keys = map.keyArray();
	    	for (Object key2 : keys) {
	    		char[] key = (char[]) key2;
	    		if (CharArrayUtils.equals(key, 0, c.length, c, true)) {
	    			result = (IBinding[]) ArrayUtil.append(IBinding.class, result, map.get(key));
	    		}
	    	}
	    } else {
	    	result = new IBinding[] { (IBinding) map.get(c) };
	    }

	    result = (IBinding[]) ArrayUtil.trim(IBinding.class, result);
	    return result;
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
