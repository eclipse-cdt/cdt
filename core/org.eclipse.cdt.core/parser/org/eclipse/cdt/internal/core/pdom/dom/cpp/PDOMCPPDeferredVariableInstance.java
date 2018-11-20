/*******************************************************************************
 * Copyright (c) 2017 Nathan Ridge.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.pdom.dom.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateArgument;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariableTemplate;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPDeferredVariableInstance;
import org.eclipse.cdt.internal.core.index.IIndexCPPBindingConstants;
import org.eclipse.cdt.internal.core.index.IIndexFragment;
import org.eclipse.cdt.internal.core.index.IIndexFragmentBinding;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.core.runtime.CoreException;

/**
 * PDOM implementation of ICPPDeferredVariableInstance.
 */
public class PDOMCPPDeferredVariableInstance extends CPPDeferredVariableInstance implements IIndexFragmentBinding {
	private final IIndexFragment fFragment;

	public PDOMCPPDeferredVariableInstance(IIndexFragment fragment, ICPPVariableTemplate template,
			ICPPTemplateArgument[] arguments) {
		super(template, arguments);
		fFragment = fragment;
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
		return IIndexCPPBindingConstants.CPP_DEFERRED_VARIABLE_INSTANCE;
	}

	@Override
	public IIndexScope getScope() {
		try {
			return (IIndexScope) super.getScope();
		} catch (DOMException e) {
			return null;
		}
	}

	@Override
	public IIndexFragmentBinding getOwner() {
		return (IIndexFragmentBinding) super.getOwner();
	}

	@Override
	public long getBindingID() {
		return 0;
	}
}
