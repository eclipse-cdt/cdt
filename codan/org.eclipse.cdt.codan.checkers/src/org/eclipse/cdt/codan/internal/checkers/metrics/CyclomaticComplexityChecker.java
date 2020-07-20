/*******************************************************************************
 * Copyright (c) 2020 Sergey Vladimirov
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Sergey Vladimirov - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.metrics;

import org.eclipse.cdt.codan.core.cxx.model.AbstractAstFunctionChecker;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.model.ProblemPreference;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;

public class CyclomaticComplexityChecker extends AbstractAstFunctionChecker {

	public static final String ER_CYCLOMATIC_COMPLEXITY_EXCEEDED_ID = "org.eclipse.cdt.codan.internal.checkers.CyclomaticComplexityExceededProblem"; //$NON-NLS-1$

	@ProblemPreference(key = "countLogicalAnd", nls = MetricCheckersMessages.class)
	private boolean countLogicalAnd = true;

	@ProblemPreference(key = "countLogicalOr", nls = MetricCheckersMessages.class)
	private boolean countLogicalOr = true;

	@ProblemPreference(key = "countCase", nls = MetricCheckersMessages.class)
	private boolean countCase = true;

	@ProblemPreference(key = "countDo", nls = MetricCheckersMessages.class)
	private boolean countDo = true;

	@ProblemPreference(key = "countFor", nls = MetricCheckersMessages.class)
	private boolean countFor = true;

	@ProblemPreference(key = "countIf", nls = MetricCheckersMessages.class)
	private boolean countIf = true;

	@ProblemPreference(key = "countSwitch", nls = MetricCheckersMessages.class)
	private boolean countSwitch = false;

	@ProblemPreference(key = "countWhile", nls = MetricCheckersMessages.class)
	private boolean countWhile = true;

	@ProblemPreference(key = "maxCyclomaticComplexity", nls = MetricCheckersMessages.class)
	private int maxCyclomaticComplexity = 11;

	private boolean countAsPlusOne(IASTExpression expression) {
		if (expression instanceof IASTBinaryExpression) {
			int operator = ((IASTBinaryExpression) expression).getOperator();
			return this.countLogicalAnd && operator == IASTBinaryExpression.op_logicalAnd
					|| this.countLogicalOr && operator == IASTBinaryExpression.op_logicalOr;
		}
		return false;
	}

	private boolean countAsPlusOne(IASTStatement statement) {
		return (this.countCase && (statement instanceof IASTCaseStatement || statement instanceof IASTDefaultStatement))
				|| (this.countDo && statement instanceof IASTDoStatement)
				|| (this.countFor && statement instanceof IASTForStatement)
				|| (this.countIf && statement instanceof IASTIfStatement)
				|| (this.countSwitch && statement instanceof IASTSwitchStatement)
				|| (this.countWhile && statement instanceof IASTWhileStatement);
	}

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		super.addPreferencesForAnnotatedFields(problem);
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		super.loadPreferencesForAnnotatedFields(getProblemById(ER_CYCLOMATIC_COMPLEXITY_EXCEEDED_ID, getFile()));
		super.processAst(ast);
	}

	@Override
	protected void processFunction(IASTFunctionDefinition func) {
		func.accept(new ASTVisitor(false) {

			private int perFunctionCounter = 1;

			{
				super.shouldVisitExpressions = true;
				super.shouldVisitStatements = true;
			}

			private int checkAndReport(IASTNode node) {
				if (this.perFunctionCounter > maxCyclomaticComplexity) {
					// report problem only once per function
					reportProblem(ER_CYCLOMATIC_COMPLEXITY_EXCEEDED_ID, node, this.perFunctionCounter);
					return PROCESS_ABORT;
				}

				return PROCESS_CONTINUE;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (countAsPlusOne(expression)) {
					this.perFunctionCounter++;
				}
				return checkAndReport(expression);
			}

			@Override
			public int visit(IASTStatement statement) {
				if (countAsPlusOne(statement)) {
					this.perFunctionCounter++;
				}
				return checkAndReport(statement);
			}
		});
	}
}
