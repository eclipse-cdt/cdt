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

package org.eclipse.cdt.internal.core.index.composite.cpp;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.index.IIndexBinding;
import org.eclipse.cdt.core.index.IIndexFile;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownMemberClass;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPUnknownTypeScope;
import org.eclipse.cdt.internal.core.index.IIndexScope;
import org.eclipse.core.runtime.CoreException;

public class CompositeCPPUnknownMemberClass extends CPPUnknownMemberClass implements IIndexBinding {
	public CompositeCPPUnknownMemberClass(IType owner, char[] name) {
		super(owner, name);
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
	public IIndexBinding getOwner() {
		return (IIndexBinding) super.getOwner();
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
	protected CPPUnknownTypeScope createScope() {
		return new CompositeCPPUnknownScope(this, new CPPASTName(getNameCharArray()));
	}
}
