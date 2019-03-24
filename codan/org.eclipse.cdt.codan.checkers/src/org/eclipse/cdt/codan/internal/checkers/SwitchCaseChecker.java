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
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
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
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;

@SuppressWarnings("restriction")
public class SwitchCaseChecker extends AbstractIndexAstChecker {
	public static final String MISS_CASE_ID = "org.eclipse.cdt.codan.internal.checkers.MissCaseProblem"; //$NON-NLS-1$
	public static final String MISS_DEFAULT_ID = "org.eclipse.cdt.codan.internal.checkers.MissDefaultProblem"; //$NON-NLS-1$
	public static final String PARAM_DEFAULT_ALL_ENUMS = "defaultWithAllEnums"; //$NON-NLS-1$
	private boolean defaultWithAllEnums = false;

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		if (problem.getId().equals(MISS_DEFAULT_ID))
			addPreference(problem, PARAM_DEFAULT_ALL_ENUMS, CheckersMessages.SwitchCaseChecker_ParameterDefaultAllEnums,
					Boolean.FALSE);
	}

	@Override
	public void processAst(IASTTranslationUnit ast) {
		final IProblem pt = getProblemById(MISS_DEFAULT_ID, getFile());
		defaultWithAllEnums = (Boolean) getPreference(pt, PARAM_DEFAULT_ALL_ENUMS);
		ast.accept(new ASTVisitor() {
			{
				shouldVisitStatements = true;
			}

			@Override
			public int visit(IASTStatement statement) {
				if (statement instanceof IASTSwitchStatement) {
					IASTExpression controller = ((IASTSwitchStatement) statement).getControllerExpression();
					IASTStatement bodyStmt = ((IASTSwitchStatement) statement).getBody();
					IType type = SemanticUtil.getUltimateType(controller.getExpressionType(), true);
					Set<Number> enumValues = new HashSet<>();
					boolean defaultFound = false;
					boolean isEnumSwitch = false;
					if (type instanceof IEnumeration) {
						IEnumerator[] enums = ((IEnumeration) type).getEnumerators();
						for (IEnumerator e : enums) {
							enumValues.add(e.getValue().numberValue());
						}
						isEnumSwitch = true;
					}
					final List<IASTStatement> statements;
					if (bodyStmt instanceof IASTCompoundStatement) {
						statements = Arrays.asList(((IASTCompoundStatement) bodyStmt).getStatements());
					} else {
						statements = Collections.singletonList(bodyStmt);
					}
					for (IASTStatement s : statements) {
						if (s instanceof IASTDefaultStatement) {
							defaultFound = true;
						} else if (s instanceof IASTCaseStatement
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
					if (!defaultFound) {
						if (isEnumSwitch && enumValues.size() != 0) {
							//switch not complete
							reportProblem(MISS_CASE_ID, statement);
							reportProblem(MISS_DEFAULT_ID, statement);
						} else if (isEnumSwitch && enumValues.size() == 0) {
							//switch complete but lack of default label
							if (defaultWithAllEnums)
								reportProblem(MISS_DEFAULT_ID, statement);
						} else {
							//switch is not an enum switch and lack of default label
							reportProblem(MISS_DEFAULT_ID, statement);
						}
					}
				}
				return PROCESS_CONTINUE;
			}
		});
	}
}
