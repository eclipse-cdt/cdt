/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.internal.qt.core.index.IQMethod;
import org.eclipse.core.runtime.CoreException;

public class QMethodName extends AbstractQObjectMemberName implements IQtASTName {

	private final IQMethod.Kind kind;
	private final String qtEncSignatures;
	private final Long revision;

	public QMethodName(QObjectName qobjName, IASTName cppName, IQMethod.Kind kind, String qtEncSignatures, Long revision) {
		super(qobjName, cppName, cppName.getLastName().toString(), cppName.getImageLocation());
		this.kind = kind;
		this.qtEncSignatures = qtEncSignatures;
		this.revision = revision;
	}

	@Override
	public QtPDOMBinding createPDOMBinding(QtPDOMLinkage linkage) throws CoreException {
		return new QtPDOMQMethod(linkage, getOwner(linkage), this, delegate, kind, qtEncSignatures, revision);
	}
}
