/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/ 

package org.eclipse.cdt.internal.core.index;

import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.index.IndexFilter;
import org.eclipse.core.runtime.CoreException;

public class DeclaredBindingsFilter extends IndexFilter {
	public boolean acceptBinding(IBinding binding) throws CoreException {
		if (binding instanceof IIndexFragmentBinding) {
			return ((IIndexFragmentBinding) binding).hasDeclaration();
		}
		return true;	// composite bindings don't support that kind of check.
	}
}
