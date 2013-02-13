/*******************************************************************************
 * Copyright (c) 2013 Nathan Ridge.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nathan Ridge - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ILinkage;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.core.runtime.PlatformObject;

/**
 * Used in places where resolution of a name needs to be deferred until a later
 * point, to avoid recursion. The only valid operation on this binding is
 * resolve().
 */
public class DeferredResolutionBinding extends PlatformObject implements IBinding {
	private final IASTName fName;
	
	public DeferredResolutionBinding(IASTName name) {
		fName = name;
	}
	
	public IBinding resolve() {
		return fName.resolveBinding();
	}
	
	@Override
	public String getName() {
		throw new UnsupportedOperationException();
	}

	@Override
	public char[] getNameCharArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ILinkage getLinkage() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IBinding getOwner() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IScope getScope() throws DOMException {
		throw new UnsupportedOperationException();
	}
}
