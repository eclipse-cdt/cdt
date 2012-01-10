/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.List;

import org.eclipse.text.edits.TextEditGroup;

import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.rewrite.ASTRewrite;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;

import org.eclipse.cdt.internal.ui.refactoring.NodeContainer.NameInformation;
import org.eclipse.cdt.internal.ui.refactoring.utils.ASTHelper;

/**
 * @author Mirko Stocker
 */
public class ExtractStatement extends ExtractedFunctionConstructionHelper {
	@Override
	public void constructMethodBody(IASTCompoundStatement compound, List<IASTNode> list,
			ASTRewrite rewrite, TextEditGroup group) {
		for (IASTNode node : list) {
			rewrite.insertBefore(compound, null, node, group);
		}
	}

	@Override
	public IASTDeclSpecifier determineReturnType(IASTNode extractedNode,
			NameInformation returnVariable) {
		if (returnVariable != null) {
			IASTNode decl = ASTHelper.getDeclarationForNode(returnVariable.getDeclaration());
			return ASTHelper.getDeclarationSpecifier(decl).copy(CopyStyle.withLocations);
		}
		IASTDeclSpecifier declSpec = new CPPASTSimpleDeclSpecifier();
		((IASTSimpleDeclSpecifier) declSpec).setType(IASTSimpleDeclSpecifier.t_void);
		return declSpec.copy(CopyStyle.withLocations);
	}

	@Override
	public IASTNode createReturnAssignment(IASTNode node, IASTExpressionStatement stmt,
			IASTExpression callExpression) {
		stmt.setExpression(callExpression);
		return stmt;
	}
}
