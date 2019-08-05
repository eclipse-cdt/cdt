/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marco Stornelli - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;

@SuppressWarnings("restriction")
public class SyntaxProblemChecker extends AbstractIndexAstChecker {
	public static String ERR_ID = "org.eclipse.cdt.codan.internal.checkers.SyntaxProblem"; //$NON-NLS-1$

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		// This checker should not run on full or incremental build
		getLaunchModePreference(problem).setRunningMode(CheckerLaunchMode.RUN_ON_FULL_BUILD, false);
		getLaunchModePreference(problem).setRunningMode(CheckerLaunchMode.RUN_ON_INC_BUILD, false);
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		final IASTProblem[] ppProblems = ast.getPreprocessorProblems();
		IASTProblem[] problems = ppProblems;
		for (IASTProblem problem : problems) {
			if (problem.isPartOfTranslationUnitFile()) {
				reportProblem(ERR_ID, problem);
			}
		}
		problems = CPPVisitor.getProblems(ast);
		for (IASTProblem problem : problems) {
			if (problem.isPartOfTranslationUnitFile()) {
				reportProblem(ERR_ID, problem);
			}
		}
	}
}
