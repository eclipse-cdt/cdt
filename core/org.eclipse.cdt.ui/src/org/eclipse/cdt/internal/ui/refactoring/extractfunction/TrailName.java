/*******************************************************************************
 * Copyright (c) 2008, 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

class TrailName extends ASTNode {
	private int nameNumber;
	private IASTName realName;

	public TrailName(IASTName realName) {
		super();
		this.realName = realName;
	}

	public int getNameNumber() {
		return nameNumber;
	}

	public void setNameNumber(int nameNumber) {
		this.nameNumber = nameNumber;
	}

	public IASTName getRealName() {
		return realName;
	}

	public boolean isGloballyQualified() {
		IBinding bind = realName.resolveBinding();
		try {
			if (bind instanceof ICPPBinding) {
				ICPPBinding cppBind = (ICPPBinding) bind;
				return cppBind.isGloballyQualified();
			}
		} catch (DOMException e) {
		}
		return false;
	}

	@Override
	public IASTNode copy() {
		throw new UnsupportedOperationException();
	}

	@Override
	public IASTNode copy(CopyStyle style) {
		throw new UnsupportedOperationException();
	}
}
