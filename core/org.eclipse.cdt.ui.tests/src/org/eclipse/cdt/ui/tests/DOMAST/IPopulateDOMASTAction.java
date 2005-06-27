/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
	public void mergePreprocessorStatements(IASTPreprocessorStatement[] statements);
	public void mergePreprocessorProblems(IASTProblem[] problems);
	public void groupIncludes(IASTPreprocessorStatement[] statements);
}
