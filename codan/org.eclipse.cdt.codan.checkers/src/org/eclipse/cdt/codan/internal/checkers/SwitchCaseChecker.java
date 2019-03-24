/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.codan.core.cxx.model.AbstractIndexAstChecker;
import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.ValueFactory;

public class SwitchCaseChecker extends AbstractIndexAstChecker {
	public static final String ERR_ID = "org.eclipse.cdt.codan.internal.checkers.SwitchCaseProblem"; //$NON-NLS-1$

	@Override
	public void processAst(IASTTranslationUnit ast) {
		ast.accept(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement expression) {
				if (expression instanceof IASTSwitchStatement) {
					IASTExpression controller = ((IASTSwitchStatement) expression).getControllerExpression();
					IASTStatement bodyStmt = ((IASTSwitchStatement) expression).getBody();
					IType type = controller.getExpressionType();
					Set<Number> enumValues = new HashSet<>();
					if (type instanceof IEnumeration) {
						IEnumerator[] enums = ((IEnumeration) type).getEnumerators();
						for (IEnumerator e : enums) {
							enumValues.add(e.getValue().numberValue());
						}
					} else
						return PROCESS_CONTINUE;
					final List<IASTStatement> statements;
					if (bodyStmt instanceof IASTCompoundStatement) {
						statements = Arrays.asList(((IASTCompoundStatement) bodyStmt).getStatements());
					} else {
						statements = Collections.singletonList(bodyStmt);
					}
					for (IASTStatement s : statements) {
						if (s instanceof IASTDefaultStatement)
							return PROCESS_CONTINUE;
						if (s instanceof IASTCaseStatement
								&& ((IASTCaseStatement) s).getExpression() instanceof IASTIdExpression) {
							IASTName name = ((IASTIdExpression) ((IASTCaseStatement) s).getExpression()).getName();
							IBinding binding = name.resolveBinding();
							if (binding instanceof IEnumerator) {
								enumValues.remove(((IEnumerator) binding).getValue().numberValue());
							}
						} else if (s instanceof IASTCaseStatement
								&& ((IASTCaseStatement) s).getExpression() instanceof IASTLiteralExpression) {
							Number value = ValueFactory
									.getConstantNumericalValue(((IASTCaseStatement) s).getExpression());
							if (value != null)
								enumValues.remove(value);
						}
					}
					if (enumValues.size() != 0)
						reportProblem(ERR_ID, expression);
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
