/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser.action;

import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerList;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;


/**
 * Abstract factory interface for creating AST node objects.
 * 
 * @author Mike Kucera
 */
public interface IASTNodeFactory {

	public IASTName newName(char[] name);

	public IASTName newName();

	// TODO this should return IASTCompletionNode
	public ASTCompletionNode newCompletionNode(String prefix, IASTTranslationUnit tu);

	public IASTLiteralExpression newLiteralExpression(int kind, String rep);
	
	public IASTUnaryExpression newUnaryExpression(int operator, IASTExpression operand);
	
	public IASTIdExpression newIdExpression(IASTName name);
	
	public IASTArraySubscriptExpression newArraySubscriptExpression(IASTExpression arrayExpr, IASTExpression subscript);

	public IASTFunctionCallExpression newFunctionCallExpression(IASTExpression idExpr, IASTExpression argList);

	public IASTExpressionList newExpressionList();
	
	public IASTCastExpression newCastExpression(int operator, IASTTypeId typeId, IASTExpression operand);
	
	public IASTBinaryExpression newBinaryExpression(int op, IASTExpression expr1, IASTExpression expr2);

	public IASTConditionalExpression newConditionalExpession(IASTExpression expr1, IASTExpression expr2, IASTExpression expr3);

	public IASTLabelStatement newLabelStatement(IASTName name, IASTStatement nestedStatement);

	public IASTCaseStatement newCaseStatement(IASTExpression expr);

	public IASTDefaultStatement newDefaultStatement();
	
	public IASTExpressionStatement newExpressionStatement(IASTExpression expression);

	public IASTNullStatement newNullStatement();
	
	public IASTCompoundStatement newCompoundStatement();
	
	public IASTSwitchStatement newSwitchStatment(IASTExpression controller, IASTStatement body);

	public IASTIfStatement newIfStatement(IASTExpression condition, IASTStatement then, IASTStatement elseClause);

	public IASTWhileStatement newWhileStatement(IASTExpression condition, IASTStatement body);

	public IASTDoStatement newDoStatement(IASTStatement body, IASTExpression condition);

	public IASTGotoStatement newGotoStatement(IASTName name);

	public IASTContinueStatement newContinueStatement();

	public IASTBreakStatement newBreakStatement();

	public IASTReturnStatement newReturnStatement(IASTExpression retValue);
	
	public IASTForStatement newForStatement(IASTStatement init, IASTExpression condition,
			IASTExpression iterationExpression, IASTStatement body);
	
	public IASTDeclarationStatement newDeclarationStatement(IASTDeclaration declaration);
	
	public IASTTypeIdExpression newTypeIdExpression(int operator, IASTTypeId typeId);
	
	public IASTTypeId newTypeId(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator);

	public IASTDeclarator newDeclarator(IASTName name);
	
	public IASTSimpleDeclaration newSimpleDeclaration(IASTDeclSpecifier declSpecifier);
	
	public IASTInitializerExpression newInitializerExpression(IASTExpression expression);
	
	public IASTInitializerList newInitializerList();
	
	public IASTFunctionDefinition newFunctionDefinition(IASTDeclSpecifier declSpecifier,
			IASTFunctionDeclarator declarator, IASTStatement bodyStatement);

	public IASTTranslationUnit newTranslationUnit();
	
	public IASTStandardFunctionDeclarator newFunctionDeclarator(IASTName name);
	
	public IASTASMDeclaration newASMDeclaration(String assembly);
	
	public IASTProblemDeclaration newProblemDeclaration();

	public IASTProblemStatement newProblemStatement();

	public IASTProblemExpression newProblemExpression();

	public IASTProblem newProblem(int id, char[] arg, boolean warn, boolean error);

	public IASTEnumerationSpecifier newEnumerationSpecifier(IASTName name);
	
	public IASTEnumerator newEnumerator(IASTName name, IASTExpression value);
	
	public IASTElaboratedTypeSpecifier newElaboratedTypeSpecifier(int kind, IASTName name);
	
	public IASTArrayModifier newArrayModifier(IASTExpression expr);
	
	public IASTArrayDeclarator newArrayDeclarator(IASTName name);
	
	public IASTParameterDeclaration newParameterDeclaration(IASTDeclSpecifier declSpec, IASTDeclarator declarator);
	
	public IASTFieldDeclarator newFieldDeclarator(IASTName name, IASTExpression bitFieldSize);
	
}
