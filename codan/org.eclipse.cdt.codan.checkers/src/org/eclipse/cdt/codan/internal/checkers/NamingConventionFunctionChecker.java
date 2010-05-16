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
package org.eclipse.cdt.codan.internal.checkers;

import java.util.regex.Pattern;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.ICheckerWithPreferences;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * This is style checker for function name code style. Pattern parameter is
 * regular expression defining the style.
 * 
 */
public class NamingConventionFunctionChecker extends AbstractIndexAstChecker
		implements ICheckerWithPreferences {
	private static final String ER_ID = "org.eclipse.cdt.codan.internal.checkers.NamingConventionFunctionChecker"; //$NON-NLS-1$
	public static final String PARAM_KEY = "pattern"; //$NON-NLS-1$

	public void processAst(IASTTranslationUnit ast) {
		final IProblem pt = getProblemById(ER_ID, getFile());
		try {
			ast.accept(new ASTVisitor() {
				{
					shouldVisitDeclarations = true;
				}

				public int visit(IASTDeclaration element) {
					if (element instanceof IASTFunctionDefinition) {
						String parameter = (String) getPreference(pt,PARAM_KEY);
						Pattern pattern = Pattern.compile(parameter);
						IASTName astName = ((IASTFunctionDefinition) element)
								.getDeclarator().getName();
						String name = astName.toString();
						if (!pattern.matcher(name).find()) {
							reportProblem(ER_ID, astName, name, parameter);
						}
					}
					return PROCESS_SKIP;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.core.model.ICheckerWithPreferences#initParameters
	 * (org.eclipse.cdt.codan.core.model.IProblemWorkingCopy)
	 */
	public void initPreferences(IProblemWorkingCopy problem) {
		addPreference(
				problem,
				PARAM_KEY,
				CheckersMessages.NamingConventionFunctionChecker_LabelNamePattern,
				"^[a-z]"); //$NON-NLS-1$
	}

	@Override
	public boolean runInEditor() {
		return true;
	}
}
