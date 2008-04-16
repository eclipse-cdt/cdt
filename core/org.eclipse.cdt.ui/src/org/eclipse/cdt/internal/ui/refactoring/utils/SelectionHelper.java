/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.utils;

import org.eclipse.jface.text.Region;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.CPPASTVisitor;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.Container;

/**
 * Helper class to suport operations conserning a selection.
 * 
 * @author Mirko Stocker
 *
 */
public class SelectionHelper {

	public static IASTSimpleDeclaration findFirstSelectedDeclaration(final Region textSelection, IASTTranslationUnit translationUnit) {

		final Container<IASTSimpleDeclaration> container = new Container<IASTSimpleDeclaration>();

		translationUnit.accept(new CPPASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}
			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof IASTSimpleDeclaration && CRefactoring.isSelectionOnExpression(textSelection, declaration)) {
					container.setObject((IASTSimpleDeclaration) declaration);
				}
				return super.visit(declaration);
			}
		});

		return container.getObject();
	}
}
