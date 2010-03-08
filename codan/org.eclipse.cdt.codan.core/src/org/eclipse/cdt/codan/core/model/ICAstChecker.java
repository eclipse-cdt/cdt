/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.model;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * Checker that can/want to process C/C++ AST (Abstract Syntax Tree) of a program
 * Default implementation {@link AbstractIndexAstChecker}
 * 
 * Clients may implement and extend this interface.
 */
public interface ICAstChecker extends IChecker {
	/**
	 * Run this checker on a given ast.
	 * Ast locks would be obtained by the framework before calling this method. 
	 * @param ast 
	 */
	void processAst(IASTTranslationUnit ast);
}
