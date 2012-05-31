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

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.parser.util.CharArrayObjectMap;

/**
 * Models the scope represented by an unknown binding such (e.g.: template type parameter). Used within
 * the context of templates, only.
 * For safe usage in index bindings, all fields need to be final or used in a thread-safe manner otherwise.
 */
public class CPPUnknownScope extends CPPUnknownTypeScope implements ICPPInternalUnknownScope {
    /**
     * This field needs to be protected when used in PDOMCPPUnknownScope, 
     * don't use it outside of {@link #getOrCreateBinding(IASTName, int)}
     */
    private CharArrayObjectMap<IBinding[]> map;

    public CPPUnknownScope(ICPPUnknownBinding binding, IASTName name) {
        super(name, binding);
    }


    @Override
	public void addName(IASTName name) {
    }

	@Override
	protected IBinding getOrCreateBinding(final char[] name, int idx) {
		if (map == null)
            map = new CharArrayObjectMap<IBinding[]>(2);

        IBinding[] o = map.get(name);
		if (o == null) {
			o = new IBinding[3];
			map.put(name, o);
		}
        
        IBinding result= o[idx];
        if (result == null) {
        	result= super.getOrCreateBinding(name, idx);
        	o[idx]= result;
        }
		return result;
	}

	@Override
	public void addBinding(IBinding binding) {
		// do nothing, this is part of template magic and not a normal scope
	}

	@Override
	public void populateCache() {}

	@Override
	public void removeNestedFromCache(IASTNode container) {}
}
