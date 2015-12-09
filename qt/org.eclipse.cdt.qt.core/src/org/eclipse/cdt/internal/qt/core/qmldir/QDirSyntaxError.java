/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.core.qmldir;

import org.eclipse.cdt.qt.core.qmldir.IQDirASTNode;
import org.eclipse.cdt.qt.core.qmldir.IQDirSyntaxError;
import org.eclipse.cdt.qt.core.qmldir.QMLDirectoryLexer.Token;
import org.eclipse.cdt.qt.core.qmldir.QMLDirectoryParser.SyntaxError;

public class QDirSyntaxError extends QDirASTNode implements IQDirSyntaxError {
	private SyntaxError exception;

	public QDirSyntaxError(SyntaxError exception) {
		this.exception = exception;
	}

	@Override
	public Token getOffendingToken() {
		return this.exception.getOffendingToken();
	}

	@Override
	public IQDirASTNode getIncompleteNode() {
		return this.exception.getIncompleteNode();
	}

	@Override
	public SyntaxError getSyntaxError() {
		return this.exception;
	}

}
