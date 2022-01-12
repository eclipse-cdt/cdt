/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.qmljs;

/**
 * A JavaScript for in statement from the <a href="https://github.com/estree/estree/blob/master/spec.md#forinstatement">ESTree
 * Specification</a>
 */
public interface IJSForInStatement extends IJSStatement {
	@Override
	default String getType() {
		return "ForInStatement"; //$NON-NLS-1$
	}

	/**
	 * @return {@link IJSVariableDeclaration}, or {@link IJSExpression}
	 */
	public IQmlASTNode getRight();

	public IJSExpression getLeft();

	public IJSStatement getBody();
}
