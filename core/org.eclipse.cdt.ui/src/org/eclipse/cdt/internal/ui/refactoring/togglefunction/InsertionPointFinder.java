/*******************************************************************************
 * Copyright (c) 2011, 2016 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Martin Schwab & Thomas Kallenberg - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.togglefunction;

import java.util.ArrayList;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.ASTQueries;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.ui.refactoring.Container;

public class InsertionPointFinder {
	private static ArrayList<ICPPASTFunctionDeclarator> allafterdeclarations;
	private static ArrayList<ICPPASTFunctionDefinition> alldefinitionsoutside;
	private static IASTDeclaration position;

	public static IASTDeclaration findInsertionPoint(IASTTranslationUnit classunit, IASTTranslationUnit functiondefunit,
			IASTFunctionDeclarator funcDecl) {
		position = null;
		findAllDeclarationsAfterInClass(classunit, funcDecl);
		findAllDefinitionsoutSideClass(functiondefunit);
		findRightPlace();
		return position;
	}

	private static void findRightPlace() {
		if (allafterdeclarations == null || alldefinitionsoutside == null)
			return;
		for (ICPPASTFunctionDeclarator decl : allafterdeclarations) {
			if (decl.getName() == null) {
				// Could be a lambda expression
				continue;
			}
			String decl_name = decl.getName().toString();
			for (ICPPASTFunctionDefinition def : alldefinitionsoutside) {
				String def_name = null;
				IASTName name = def.getDeclarator().getName();
				if (name != null)
					def_name = name.getLastName().toString();

				if (decl_name.equals(def_name)) {
					if (def.getParent() != null && def.getParent() instanceof ICPPASTTemplateDeclaration) {
						position = (IASTDeclaration) def.getParent();
					} else {
						position = def;
					}
					return;
				}
			}
		}
	}

	private static void findAllDeclarationsAfterInClass(IASTTranslationUnit classunit,
			IASTFunctionDeclarator funcDecl) {
		ICPPASTCompositeTypeSpecifier klass = getklass(classunit);
		if (klass != null)
			allafterdeclarations = getDeclarationsInClass(klass, funcDecl);
	}

	/**
	 * @param ast the translation unit where to find the definitions
	 */
	private static void findAllDefinitionsoutSideClass(IASTTranslationUnit ast) {
		final ArrayList<ICPPASTFunctionDefinition> definitions = new ArrayList<>();
		if (ast == null) {
			alldefinitionsoutside = definitions;
			return;
		}
		ast.accept(new ASTVisitor() {
			{
				shouldVisitDeclarations = true;
			}

			@Override
			public int visit(IASTDeclaration declaration) {
				if (declaration instanceof ICPPASTFunctionDefinition) {
					if (declaration.getParent() != null && ASTQueries.findAncestorWithType(declaration,
							CPPASTCompositeTypeSpecifier.class) != null) {
						return PROCESS_CONTINUE;
					}
					definitions.add((ICPPASTFunctionDefinition) declaration);
				}
				return super.visit(declaration);
			}
		});
		alldefinitionsoutside = definitions;
	}

	private static ArrayList<ICPPASTFunctionDeclarator> getDeclarationsInClass(ICPPASTCompositeTypeSpecifier klass,
			final IASTFunctionDeclarator selected) {
		final ArrayList<ICPPASTFunctionDeclarator> declarations = new ArrayList<>();

		klass.accept(new ASTVisitor() {
			{
				shouldVisitDeclarators = true;
			}

			boolean got = false;

			@Override
			public int visit(IASTDeclarator declarator) {
				if (declarator instanceof ICPPASTFunctionDeclarator) {
					if (((ICPPASTFunctionDeclarator) declarator) == selected) {
						got = true;
					}
					if (got) {
						declarations.add((ICPPASTFunctionDeclarator) declarator);
					}
				}
				return super.visit(declarator);
			}
		});

		return declarations;
	}

	private static ICPPASTCompositeTypeSpecifier getklass(IASTTranslationUnit unit) {
		final Container<ICPPASTCompositeTypeSpecifier> result = new Container<>();

		unit.accept(new ASTVisitor() {
			{
				shouldVisitDeclSpecifiers = true;
			}

			@Override
			public int visit(IASTDeclSpecifier declSpec) {
				if (declSpec instanceof ICPPASTCompositeTypeSpecifier) {
					result.setObject((ICPPASTCompositeTypeSpecifier) declSpec);
					return PROCESS_ABORT;
				}
				return super.visit(declSpec);
			}
		});
		return result.getObject();
	}
}