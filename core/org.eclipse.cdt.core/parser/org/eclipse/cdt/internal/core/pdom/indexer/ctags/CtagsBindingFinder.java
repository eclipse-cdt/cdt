/*******************************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.indexer.ctags;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.IPDOMNode;
import org.eclipse.cdt.core.dom.IPDOMVisitor;
import org.eclipse.cdt.internal.core.pdom.dom.PDOMBinding;
import org.eclipse.core.runtime.CoreException;

/**
 * @author Doug Schaefer
 *
 */
public class CtagsBindingFinder implements IPDOMVisitor {

	private final String name;
	private final int[] types;
	private List bindings = new ArrayList();
	
	public CtagsBindingFinder(String name, int[] types) {
		this.name = name;
		this.types = types;
	}
	
	public boolean visit(IPDOMNode node) throws CoreException {
		PDOMBinding binding = (PDOMBinding)node;
		if (binding.getName().equals(name)) {
			int type = binding.getBindingType();
			for (int i = 0; i < types.length; ++i) {
				if (type == types[i]) {
					bindings.add(binding);
					break;
				}
			}
		}
		return false;
	}

	public PDOMBinding[] getBindings() {
		return (PDOMBinding[])bindings.toArray(new PDOMBinding[bindings.size()]);
	}
}
