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
 *     Tomasz Wesolowski [bug 354556]
 *     Sergey Vladimirov - implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

public class OctalNotationChecker extends AbstractIndexAstChecker {

	public class OctalNotationVisitor extends ASTVisitor {

		public OctalNotationVisitor() {
			super(false);
			super.shouldVisitExpressions = true;
		}

		@Override
		public int visit(IASTExpression expression) {
			if (expression instanceof IASTLiteralExpression) {
				checkLiteralExpression((IASTLiteralExpression) expression);
			}
			return PROCESS_CONTINUE;
		}
	}

	private static final Pattern OCTAL = Pattern.compile("^0[1-7][0-7]*$"); //$NON-NLS-1$

	public static final String PROBLEM_ID = "org.eclipse.cdt.codan.internal.checkers.OctalNotationProblem"; //$NON-NLS-1$

	public final Matcher matcher = OCTAL.matcher(""); //$NON-NLS-1$

	public final ASTVisitor visitor = new OctalNotationVisitor();

	public void checkLiteralExpression(IASTLiteralExpression expression) {
		if (expression.getKind() != IASTLiteralExpression.lk_integer_constant) {
			return;
		}

		final String strValue = String.valueOf(expression.getValue());
		matcher.reset(strValue);
		if (matcher.matches()) {
			final Integer actualValue = Integer.valueOf(strValue, 8);
			reportProblem(PROBLEM_ID, expression, strValue, actualValue);
		}
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(visitor);
	}
}
