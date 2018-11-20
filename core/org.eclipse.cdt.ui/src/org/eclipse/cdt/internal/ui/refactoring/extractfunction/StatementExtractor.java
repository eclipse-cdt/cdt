/*******************************************************************************
 * Copyright (c) 2008, 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.ui.refactoring.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.utils.ASTHelper;
import org.eclipse.text.edits.TextEditGroup;

/**
 * @author Mirko Stocker
 */
public class StatementExtractor extends FunctionExtractor {
	@Override
	public boolean canChooseReturnValue() {
		return true;
	}

	@Override
	public void constructMethodBody(IASTCompoundStatement compound, List<IASTNode> nodes,
			List<NameInformation> parameters, ASTRewrite rewrite, TextEditGroup group) {
		Map<IASTName, NameInformation> changedParameters = getChangedParameterReferences(parameters);
		INodeFactory nodeFactory = nodes.get(0).getTranslationUnit().getASTNodeFactory();
		for (IASTNode node : nodes) {
			ASTRewrite subRewrite = rewrite.insertBefore(compound, null, node, group);
			adjustParameterReferences(node, changedParameters, nodeFactory, subRewrite, group);
		}
	}

	@Override
	public IASTDeclSpecifier determineReturnType(IASTNode extractedNode, NameInformation returnVariable,
			List<IASTPointerOperator> pointerOperators) {
		if (returnVariable != null) {
			IASTName declarationName = returnVariable.getDeclarationName();
			IASTDeclarator declarator = ASTHelper.getDeclaratorForNode(declarationName);
			for (IASTPointerOperator pointerOperator : declarator.getPointerOperators()) {
				pointerOperators.add(pointerOperator.copy(CopyStyle.withLocations));
			}
			IASTNode decl = ASTHelper.getDeclarationForNode(declarationName);
			IASTDeclSpecifier declSpec = ASTHelper.getDeclarationSpecifier(decl);
			return declSpec != null ? declSpec.copy(CopyStyle.withLocations) : null;
		}
		IASTSimpleDeclSpecifier declSpec = new CPPASTSimpleDeclSpecifier();
		declSpec.setType(IASTSimpleDeclSpecifier.t_void);
		return declSpec.copy(CopyStyle.withLocations);
	}

	@Override
	public IASTNode createReturnAssignment(IASTNode node, IASTExpressionStatement stmt, IASTExpression callExpression) {
		stmt.setExpression(callExpression);
		return stmt;
	}
}
