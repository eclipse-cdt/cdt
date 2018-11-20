/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import org.eclipse.cdt.core.browser.ITypeInfo;
import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

public class BaseClassInfo implements IBaseClassInfo {

	private ITypeInfo fType;
	private ASTAccessVisibility fAccess;
	private boolean fIsVirtual;

	public BaseClassInfo(ITypeInfo type, ASTAccessVisibility access, boolean isVirtual) {
		fType = type;
		fAccess = access;
		fIsVirtual = isVirtual;
	}

	@Override
	public ITypeInfo getType() {
		return fType;
	}

	@Override
	public ASTAccessVisibility getAccess() {
		return fAccess;
	}

	@Override
	public boolean isVirtual() {
		return fIsVirtual;
	}

	@Override
	public void setAccess(ASTAccessVisibility access) {
		fAccess = access;
	}

	@Override
	public void setVirtual(boolean isVirtual) {
		fIsVirtual = isVirtual;
	}
}
