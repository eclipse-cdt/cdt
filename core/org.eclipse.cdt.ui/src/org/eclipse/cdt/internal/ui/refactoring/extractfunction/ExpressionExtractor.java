/*******************************************************************************
 * Copyright (c) 2008, 2017 Institute for Software, HSR Hochschule fuer Technik
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.cdt.core.dom.rewrite.DeclarationGenerator;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.ui.refactoring.NameInformation;
import org.eclipse.text.edits.TextEditGroup;

/**
 * Handles the extraction of expression nodes, for example, return type determination.
 *
 * @author Mirko Stocker
 */
public class ExpressionExtractor extends FunctionExtractor {
	@Override
	public boolean canChooseReturnValue() {
		return false;
	}

	@Override
	public void constructMethodBody(IASTCompoundStatement compound, List<IASTNode> nodes,
			List<NameInformation> parameters, ASTRewrite rewrite, TextEditGroup group) {
		CPPASTReturnStatement statement = new CPPASTReturnStatement();
		statement.setReturnValue(getExpression(nodes));
		ASTRewrite subRewrite = rewrite.insertBefore(compound, null, statement, group);
		Map<IASTName, NameInformation> changedParameters = getChangedParameterReferences(parameters);
		INodeFactory nodeFactory = nodes.get(0).getTranslationUnit().getASTNodeFactory();
		adjustParameterReferences(statement, changedParameters, nodeFactory, subRewrite, group);
	}

	private IASTExpression getExpression(List<IASTNode> nodes) {
		if (nodes.size() > 1) {
			CPPASTBinaryExpression expression = new CPPASTBinaryExpression();
			expression.setParent(nodes.get(0).getParent());
			expression.setOperand1((IASTExpression) nodes.get(0).copy(CopyStyle.withLocations));
			expression.setOperator(((IASTBinaryExpression) nodes.get(1).getParent()).getOperator());
			expression.setOperand2(getExpression(nodes.subList(1, nodes.size())));
			return expression;
		} else {
			return (IASTExpression) nodes.get(0).copy(CopyStyle.withLocations);
		}
	}

	@Override
	public IASTDeclSpecifier determineReturnType(IASTNode extractedNode, NameInformation nameInfo,
			List<IASTPointerOperator> pointerOperators) {
		IType returnType = determineReturnType(extractedNode);
		INodeFactory factory = extractedNode.getTranslationUnit().getASTNodeFactory();
		DeclarationGenerator generator = DeclarationGenerator.create(factory);
		IASTDeclarator declarator = generator.createDeclaratorFromType(returnType, null);
		ArrayUtil.addAll(pointerOperators, declarator.getPointerOperators());
		return generator.createDeclSpecFromType(returnType);
	}

	private IType determineReturnType(IASTNode extractedNode) {
		List<ITypedef> typedefs = getTypedefs(extractedNode);
		if (extractedNode instanceof IASTExpression) {
			IType expressionType = ((IASTExpression) extractedNode).getExpressionType();
			for (ITypedef typedef : typedefs) {
				if (typedef.getType().isSameType(expressionType)) {
					return typedef;
				}
			}
			return expressionType;
		} else { // Fallback
			return new CPPBasicType(Kind.eVoid, 0);
		}
	}

	private List<ITypedef> getTypedefs(IASTNode extractedNode) {
		final ArrayList<ITypedef> typeDefs = new ArrayList<>();
		extractedNode.accept(new ASTVisitor() {
			{
				shouldVisitExpressions = true;
			}

			@Override
			public int visit(IASTExpression expression) {
				if (expression instanceof IASTIdExpression) {
					IASTIdExpression id = (IASTIdExpression) expression;
					IBinding binding = id.getName().resolveBinding();
					IType expressionType = null;
					if (binding instanceof IVariable) {
						expressionType = ((IVariable) binding).getType();
					}
					if (binding instanceof IType) {
						expressionType = (IType) binding;
					}
					if (expressionType != null && expressionType instanceof ITypedef) {
						ITypedef typdef = (ITypedef) expressionType;
						typeDefs.add(typdef);
					}
				}
				return PROCESS_CONTINUE;
			}
		});
		return typeDefs;
	}

	@Override
	public IASTNode createReturnAssignment(IASTNode node, IASTExpressionStatement stmt, IASTExpression callExpression) {
		return callExpression;
	}
}
