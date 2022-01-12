/*******************************************************************************
 * Copyright (c) 2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Markus Schorn - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownMethod;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.core.runtime.CoreException;

public class PDOMCPPUnknownMethod extends CPPUnknownMethod implements IIndexFragmentBinding {
	private final IIndexFragment fFragment;

	public PDOMCPPUnknownMethod(IIndexFragment frag, IType owner, char[] name) {
		super(owner, name);
		fFragment = frag;
	}

	@Override
	public boolean isFileLocal() throws CoreException {
		return false;
	}

	@Override
	public IIndexFile getLocalToFile() throws CoreException {
		return null;
	}

	@Override
	public IIndexFragment getFragment() {
		return fFragment;
	}

	@Override
	public boolean hasDefinition() throws CoreException {
		return false;
	}

	@Override
	public boolean hasDeclaration() throws CoreException {
		return true;
	}

	@Override
	public int getBindingConstant() {
		return IIndexCPPBindingConstants.CPP_UNKNOWN_METHOD;
	}

	@Override
	public long getBindingID() {
		return 0;
	}

	@Override
	public IIndexFragmentBinding getOwner() {
		return (IIndexFragmentBinding) super.getOwner();
	}

	@Override
	public IIndexScope getScope() {
		try {
			return (IIndexScope) super.getScope();
		} catch (DOMException e) {
			return null;
		}
	}
}
