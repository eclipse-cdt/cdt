/*******************************************************************************
 * Copyright (c) 2008, 2012 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.List;
import java.util.Map;

import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;

import org.eclipse.cdt.internal.ui.refactoring.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.utils.ASTHelper;

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
	public IASTDeclSpecifier determineReturnType(IASTNode extractedNode,
			NameInformation returnVariable) {
		if (returnVariable != null) {
			IASTNode decl = ASTHelper.getDeclarationForNode(returnVariable.getDeclarationName());
			IASTDeclSpecifier declSpec = ASTHelper.getDeclarationSpecifier(decl);
			return declSpec != null ? declSpec.copy(CopyStyle.withLocations) : null;
		}
		IASTSimpleDeclSpecifier declSpec = new CPPASTSimpleDeclSpecifier();
		declSpec.setType(IASTSimpleDeclSpecifier.t_void);
		return declSpec.copy(CopyStyle.withLocations);
	}

	@Override
	public IASTNode createReturnAssignment(IASTNode node, IASTExpressionStatement stmt,
			IASTExpression callExpression) {
		stmt.setExpression(callExpression);
		return stmt;
	}
}
