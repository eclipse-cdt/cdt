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
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.ui.AbstractAstRewriteQuickFix;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.ValueFactory;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.SemanticUtil;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ltk.core.refactoring.Change;

@SuppressWarnings("restriction")
public class QuickFixAddCaseSwitch extends AbstractAstRewriteQuickFix {

	@Override
	public String getLabel() {
		return QuickFixMessages.QuickFixAddCaseSwitch_add_cases_to_switch;
	}

	@Override
	public void modifyAST(IIndex index, IMarker marker) {
		IASTTranslationUnit ast;
		try {
			ITranslationUnit tu = getTranslationUnitViaEditor(marker);
			ast = tu.getAST(index, ITranslationUnit.AST_SKIP_INDEXED_HEADERS);
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
			return;
		}
		IASTNode astNode = null;
		if (isCodanProblem(marker)) {
			astNode = getASTNodeFromMarker(marker, ast);
		}
		if (astNode == null || !(astNode instanceof IASTSwitchStatement)) {
			return;
		}
		ASTRewrite r = ASTRewrite.create(ast);
		INodeFactory factory = ast.getASTNodeFactory();
		Map<String, Number> missingEnums = getMissingCases((IASTSwitchStatement) astNode);
		Set<Number> existing = new HashSet<>();
		missingEnums = missingEnums.entrySet().stream().filter(entry -> existing.add(entry.getValue()))
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		List<IASTCaseStatement> caseStatements = new ArrayList<>();
		for (Map.Entry<String, Number> e : missingEnums.entrySet()) {
			IASTName newName = factory.newName(e.getKey());
			caseStatements.add(factory.newCaseStatement(factory.newIdExpression(newName)));
		}
		IASTBreakStatement breakStatement = factory.newBreakStatement();
		IASTNode[] children = astNode.getChildren();
		IASTCompoundStatement compound = null;
		IASTNullStatement nullStatement = null;
		for (int i = 0; i < children.length; ++i) {
			if (children[i] instanceof IASTCompoundStatement) {
				compound = (IASTCompoundStatement) children[i];
				break;
			} else if (children[i] instanceof IASTNullStatement)
				nullStatement = (IASTNullStatement) children[i];
		}
		if (compound == null && nullStatement != null) {
			compound = factory.newCompoundStatement();
			for (IASTCaseStatement caseStatement : caseStatements)
				compound.addStatement(caseStatement);
			compound.addStatement(breakStatement);
			r.replace(nullStatement, compound, null);
		} else if (compound != null) {
			for (IASTCaseStatement caseStatement : caseStatements)
				r.insertBefore(compound, null, caseStatement, null);
			r.insertBefore(compound, null, breakStatement, null);
		} else
			return;
		Change c = r.rewriteAST();
		try {
			c.perform(new NullProgressMonitor());
			marker.delete();
		} catch (CoreException e) {
			CheckersUiActivator.log(e);
		}
	}

	private Map<String, Number> getMissingCases(IASTSwitchStatement statement) {
		IASTExpression controller = statement.getControllerExpression();
		IASTStatement bodyStmt = statement.getBody();
		IType type = SemanticUtil.getUltimateType(controller.getExpressionType(), true);
		Map<String, Number> enumValues = new HashMap<>();
		if (type instanceof IEnumeration) {
			IEnumerator[] enums = ((IEnumeration) type).getEnumerators();
			String prefix = ""; //$NON-NLS-1$
			if (type instanceof ICPPEnumeration) {
				String[] qualName = CPPVisitor.getQualifiedName((IEnumeration) type);
				StringBuilder builder = new StringBuilder();
				for (int i = 0; i < qualName.length - 1; ++i) {
					builder.append(qualName[i]);
					builder.append("::"); //$NON-NLS-1$
				}
				prefix = builder.toString();
			}
			for (IEnumerator e : enums) {
				enumValues.put(prefix + e.getName(), e.getValue().numberValue());
			}
		} else
			return enumValues;
		final List<IASTStatement> statements;
		if (bodyStmt instanceof IASTCompoundStatement) {
			statements = Arrays.asList(((IASTCompoundStatement) bodyStmt).getStatements());
		} else {
			statements = Collections.singletonList(bodyStmt);
		}
		for (IASTStatement s : statements) {
			if (s instanceof IASTCaseStatement && ((IASTCaseStatement) s).getExpression() instanceof IASTIdExpression) {
				IASTName name = ((IASTIdExpression) ((IASTCaseStatement) s).getExpression()).getName();
				IBinding binding = name.resolveBinding();
				if (binding instanceof IEnumerator) {
					enumValues.entrySet().removeIf(
							entry -> entry.getValue().equals(((IEnumerator) binding).getValue().numberValue()));
				}
			} else if (s instanceof IASTCaseStatement
					&& ((IASTCaseStatement) s).getExpression() instanceof IASTLiteralExpression) {
				Number value = ValueFactory.getConstantNumericalValue(((IASTCaseStatement) s).getExpression());
				if (value != null)
					enumValues.entrySet().removeIf(entry -> entry.getValue().equals(value));
			}
		}
		return enumValues;
	}
}
