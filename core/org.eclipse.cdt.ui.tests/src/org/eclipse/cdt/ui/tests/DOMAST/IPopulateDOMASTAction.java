/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.DOMAST;

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblem;

/**
 * @author dsteffle
 */
public interface IPopulateDOMASTAction {
	public TreeParent getTree();
	public void mergePreprocessorStatements(IASTPreprocessorStatement[] statements);
	public void mergePreprocessorProblems(IASTProblem[] problems);
	public void groupIncludes(IASTPreprocessorStatement[] statements);
}
