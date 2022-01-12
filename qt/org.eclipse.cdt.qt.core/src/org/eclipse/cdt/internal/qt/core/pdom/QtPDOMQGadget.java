/*
 * Copyright (c) 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.core.runtime.CoreException;

public class QtPDOMQGadget extends AbstractQtPDOMClass {

	protected QtPDOMQGadget(QtPDOMLinkage linkage, long record) throws CoreException {
		super(linkage, record);
	}

	public QtPDOMQGadget(QtPDOMLinkage linkage, IASTName qtName, IASTName cppName) throws CoreException {
		super(linkage, qtName, cppName);
	}

	@Override
	public int getNodeType() {
		return QtPDOMNodeType.QGadget.Type;
	}
}
