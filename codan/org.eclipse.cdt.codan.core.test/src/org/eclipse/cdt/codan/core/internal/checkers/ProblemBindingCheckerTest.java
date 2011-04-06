/*******************************************************************************
 * Copyright (c) 2011 Marc-Andre Laperle and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.internal.checkers;

import org.eclipse.cdt.codan.core.test.CheckerTestCase;
import org.eclipse.cdt.codan.internal.checkers.ProblemBindingChecker;
import org.eclipse.core.resources.IMarker;

public class ProblemBindingCheckerTest extends CheckerTestCase {
	@Override
	public boolean isCpp() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.codan.core.test.CodanTestCase#setUp()
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();
		enableProblems(ProblemBindingChecker.ERR_ID_AmbiguousProblem, ProblemBindingChecker.ERR_ID_Candidates,
				ProblemBindingChecker.ERR_ID_CircularReferenceProblem, ProblemBindingChecker.ERR_ID_FieldResolutionProblem,
				ProblemBindingChecker.ERR_ID_FunctionResolutionProblem, ProblemBindingChecker.ERR_ID_InvalidArguments,
				ProblemBindingChecker.ERR_ID_InvalidTemplateArgumentsProblem, ProblemBindingChecker.ERR_ID_LabelStatementNotFoundProblem,
				ProblemBindingChecker.ERR_ID_MemberDeclarationNotFoundProblem, ProblemBindingChecker.ERR_ID_MethodResolutionProblem,
				ProblemBindingChecker.ERR_ID_OverloadProblem, ProblemBindingChecker.ERR_ID_RedeclarationProblem,
				ProblemBindingChecker.ERR_ID_RedefinitionProblem, ProblemBindingChecker.ERR_ID_TypeResolutionProblem,
				ProblemBindingChecker.ERR_ID_VariableResolutionProblem);
	}

	// int main () {
	//     struct X {} x;
	//     fun(x.y);
	// }
	public void testBug338683FieldInFunctionCall() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ProblemBindingChecker.ERR_ID_FieldResolutionProblem);
	}

	// int main () {
	//   struct X {} x;
	//   x.b(
	//       x.y(),
	//       x.y(
	//           x.y),
	//       x.y(
	//           x.y(
	//               a,
	//               fun(
	//                   x.b(),
	//                   x.y,
	//                   a.b()))));
	// }
	public void testBug338683VariousFieldMethodCombinations() {
		loadCodeAndRun(getAboveComment());
		checkErrorLine(3, ProblemBindingChecker.ERR_ID_MethodResolutionProblem);
		checkErrorLine(4, ProblemBindingChecker.ERR_ID_MethodResolutionProblem);
		checkErrorLine(5, ProblemBindingChecker.ERR_ID_MethodResolutionProblem);
		checkErrorLine(6, ProblemBindingChecker.ERR_ID_FieldResolutionProblem);
		checkErrorLine(7, ProblemBindingChecker.ERR_ID_MethodResolutionProblem);
		checkErrorLine(8, ProblemBindingChecker.ERR_ID_MethodResolutionProblem);
		checkErrorLine(9, ProblemBindingChecker.ERR_ID_VariableResolutionProblem);
		checkErrorLine(10, ProblemBindingChecker.ERR_ID_FunctionResolutionProblem);
		checkErrorLine(11, ProblemBindingChecker.ERR_ID_MethodResolutionProblem);
		checkErrorLine(12, ProblemBindingChecker.ERR_ID_FieldResolutionProblem);
		checkErrorLine(13, ProblemBindingChecker.ERR_ID_MethodResolutionProblem);
	}

	// #define MACRO(code) code
	// int main() {
	//   MACRO(foo());
	//   return 0;
	// }
	public void testBug341089DontUnderlineWholeMacro() {
		loadCodeAndRun(getAboveComment());
		IMarker marker = checkErrorLine(3, ProblemBindingChecker.ERR_ID_FunctionResolutionProblem);
		assertFalse(marker.getAttribute(IMarker.MESSAGE, "").contains("MACRO"));  //$NON-NLS-1$//$NON-NLS-2$
	}
}
