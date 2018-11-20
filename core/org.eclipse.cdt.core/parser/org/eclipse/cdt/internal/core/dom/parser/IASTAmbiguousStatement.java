/*******************************************************************************
 * Copyright (c) 2004, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTNodeProperty;
import org.eclipse.cdt.core.dom.ast.IASTStatement;

public interface IASTAmbiguousStatement extends IASTStatement {
	public static final ASTNodeProperty STATEMENT = new ASTNodeProperty(
			"IASTAmbiguousStatement.STATEMENT - Ambiguous statement."); //$NON-NLS-1$

	public void addStatement(IASTStatement s);

	public IASTStatement[] getStatements();
}
