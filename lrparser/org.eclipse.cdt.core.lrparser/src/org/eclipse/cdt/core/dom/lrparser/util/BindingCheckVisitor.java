/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.util;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;


/**
 * An AST visitor that asserts that all bindings have been resolved.
 * 
 * @author Mike Kucera
 */
class BindingCheckVisitor extends CASTVisitor {
	
	public static ASTVisitor VISITOR = new BindingCheckVisitor();
	
	private BindingCheckVisitor() {
		shouldVisitNames = true;
		shouldVisitDeclarations = true;
		shouldVisitInitializers = true;
		shouldVisitParameterDeclarations = true;
		shouldVisitDeclarators = true;
		shouldVisitDeclSpecifiers = true;
		shouldVisitExpressions = true;
		shouldVisitStatements = true;
		shouldVisitTypeIds = true;
		shouldVisitEnumerators = true;
		shouldVisitTranslationUnit = true;
		shouldVisitProblems = false;
	}
	
	@Override
	public int visit(IASTName name) {
		if(name.getBinding() == null)
			throw new AssertionError("Binding did not get pre-resolved: '" + name + "'"); //$NON-NLS-1$ //$NON-NLS-2$
		return PROCESS_CONTINUE;
	}

}
