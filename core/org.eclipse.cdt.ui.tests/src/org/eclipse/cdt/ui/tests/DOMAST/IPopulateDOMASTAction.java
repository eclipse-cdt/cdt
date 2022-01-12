/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;

/**
 * @author dsteffle
 */
public interface IPopulateDOMASTAction {
	public DOMASTNodeParent getTree();

	public DOMASTNodeLeaf[] mergePreprocessorStatements(IASTPreprocessorStatement[] statements);

	public void mergePreprocessorProblems(IASTProblem[] problems);

	public void groupIncludes(DOMASTNodeLeaf[] statements);
}
