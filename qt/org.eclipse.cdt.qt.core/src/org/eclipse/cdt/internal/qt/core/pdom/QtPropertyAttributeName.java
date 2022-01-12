/*
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.core.runtime.CoreException;

public class QtPropertyAttributeName extends ASTDelegatedName implements IQtASTName {

	private final QtASTImageLocation location;

	public QtPropertyAttributeName(IASTName ast, String name, QtASTImageLocation location) {
		super(ast);
		this.location = location;
	}

	@Override
	public IASTFileLocation getFileLocation() {
		return location;
	}

	@Override
	public QtPDOMBinding createPDOMBinding(QtPDOMLinkage linkage) throws CoreException {
		return null;
	}
}
