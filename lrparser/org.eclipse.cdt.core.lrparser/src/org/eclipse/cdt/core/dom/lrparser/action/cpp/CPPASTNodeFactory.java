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
package org.eclipse.cdt.core.dom.lrparser.action.cpp;

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
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
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
import org.eclipse.cdt.core.dom.ast.IASTPointer;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisiblityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.lrparser.action.ASTCompletionNode;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTASMDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTAmbiguousDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTAmbiguousExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTAmbiguousStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArrayModifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTArraySubscriptExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBaseSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBreakStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCaseStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCastExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCatchHandler;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConditionalExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConstructorChainInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConstructorInitializer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTContinueStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTConversionName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarationStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDefaultStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDeleteExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTDoStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTEnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTEnumerator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionList;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTForStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionCallExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionTryBlockDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTGotoStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIfStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTInitializerExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTInitializerList;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLabelStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLinkageSpecification;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamedTypeSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamespaceAlias;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNewExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNullStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTOperatorName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTPointer;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTPointerToMember;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTProblem;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTProblemDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTProblemExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTProblemStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTQualifiedName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReferenceOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplateSpecialization;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTryBlockStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeId;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypeIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTypenameExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUnaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUsingDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTUsingDirective;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTVisibilityLabel;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTWhileStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;


/**
 * Abstract factory implementation that creates C++ AST nodes.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings("restriction") // all AST node constructors are internal
public class CPPASTNodeFactory implements ICPPASTNodeFactory {

	public static final CPPASTNodeFactory DEFAULT_INSTANCE = new CPPASTNodeFactory();
	
	
	public ASTCompletionNode newCompletionNode(String prefix, IASTTranslationUnit tu) {
		return new ASTCompletionNode((prefix == null || prefix.length() == 0) ? null : prefix, tu);
	}

	public IASTName newName(char[] name) {
		return new CPPASTName(name);
	}
	
	public IASTName newName() {
		return new CPPASTName();
	}
	
	public ICPPASTOperatorName newCPPOperatorName(OverloadableOperator op) {
		return new CPPASTOperatorName(op);
	}

	public IASTProblem newProblem(int id, char[] arg, boolean error) {
		return new CPPASTProblem(id, arg, error);
	}

	public IASTProblemDeclaration newProblemDeclaration() {
		return new CPPASTProblemDeclaration();
	}

	public IASTProblemExpression newProblemExpression() {
		return new CPPASTProblemExpression();
	}

	public IASTProblemStatement newProblemStatement() {
		return new CPPASTProblemStatement();
	}

	public IASTLiteralExpression newLiteralExpression(int kind, String rep) {
		return new CPPASTLiteralExpression(kind, rep);
	}

	public IASTUnaryExpression newUnaryExpression(int operator, IASTExpression operand) {
		return new CPPASTUnaryExpression(operator, operand);
	}

	public IASTIdExpression newIdExpression(IASTName name) {
		return new CPPASTIdExpression(name);
	}

	public IASTArraySubscriptExpression newArraySubscriptExpression(IASTExpression arrayExpr, IASTExpression subscript) {
		return new CPPASTArraySubscriptExpression(arrayExpr, subscript);
	}

	public IASTExpressionList newExpressionList() {
		return new CPPASTExpressionList();
	}

	public IASTFunctionCallExpression newFunctionCallExpression(IASTExpression idExpr, IASTExpression argList) {
		return new CPPASTFunctionCallExpression(idExpr, argList);
	}

	public IASTCastExpression newCastExpression(int operator, IASTTypeId typeId, IASTExpression operand) {
		return new CPPASTCastExpression(operator, typeId, operand);
	}

	public ICPPASTNewExpression newCPPNewExpression(IASTExpression placement, IASTExpression initializer, IASTTypeId typeId) {
		return new CPPASTNewExpression(placement, initializer, typeId);
	}

	public IASTBinaryExpression newBinaryExpression(int op, IASTExpression expr1, IASTExpression expr2) {
		return new CPPASTBinaryExpression(op, expr1, expr2);
	}

	public IASTConditionalExpression newConditionalExpession(IASTExpression expr1, IASTExpression expr2, IASTExpression expr3) {
		return new CPPASTConditionalExpression(expr1, expr2, expr3);
	}

	public IASTFieldReference newFieldReference(IASTName name, IASTExpression owner, boolean isPointerDereference, boolean isTemplate) {
		return new CPPASTFieldReference(name, owner, isPointerDereference, isTemplate);
	}

	public ICPPASTTemplateId newCPPTemplateId(IASTName templateName) {
		return new CPPASTTemplateId(templateName);
	}

	public ICPPASTConversionName newCPPConversionName(char[] name, IASTTypeId typeId) {
		return new CPPASTConversionName(name, typeId);
	}

	public ICPPASTQualifiedName newCPPQualifiedName() {
		return new CPPASTQualifiedName();
	}

	public IASTCaseStatement newCaseStatement(IASTExpression expr) {
		return new CPPASTCaseStatement(expr);
	}

	public IASTDefaultStatement newDefaultStatement() {
		return new CPPASTDefaultStatement();
	}

	public IASTLabelStatement newLabelStatement(IASTName name, IASTStatement nestedStatement) {
		return new CPPASTLabelStatement(name, nestedStatement);
	}

	public IASTExpressionStatement newExpressionStatement(IASTExpression expression) {
		return new CPPASTExpressionStatement(expression);
	}

	public IASTNullStatement newNullStatement() {
		return new CPPASTNullStatement();
	}

	public IASTCompoundStatement newCompoundStatement() {
		return new CPPASTCompoundStatement();
	}

	public IASTIfStatement newIfStatement(IASTExpression condition, IASTStatement then, IASTStatement elseClause) {
		return new CPPASTIfStatement(condition, then, elseClause);
	}

	public IASTSwitchStatement newSwitchStatment(IASTExpression controller, IASTStatement body) {
		return new CPPASTSwitchStatement(controller, body);
	}

	public IASTIfStatement newIfStatement(IASTDeclaration condition, IASTStatement then, IASTStatement elseClause) {
		return new CPPASTIfStatement(condition, then, elseClause);
	}

	public IASTSwitchStatement newSwitchStatment(IASTDeclaration controller, IASTStatement body) {
		return new CPPASTSwitchStatement(controller, body);
	}

	public IASTWhileStatement newWhileStatement(IASTDeclaration condition, IASTStatement body) {
		return new CPPASTWhileStatement(condition, body);
	}

	public IASTDoStatement newDoStatement(IASTStatement body, IASTExpression condition) {
		return new CPPASTDoStatement(body, condition);
	}

	public IASTWhileStatement newWhileStatement(IASTExpression condition, IASTStatement body) {
		return new CPPASTWhileStatement(condition, body);
	}

	public IASTBreakStatement newBreakStatement() {
		return new CPPASTBreakStatement();
	}

	public IASTContinueStatement newContinueStatement() {
		return new CPPASTContinueStatement();
	}

	public IASTGotoStatement newGotoStatement(IASTName name) {
		return new CPPASTGotoStatement(name);
	}

	public IASTReturnStatement newReturnStatement(IASTExpression retValue) {
		return new CPPASTReturnStatement(retValue);
	}

	public IASTForStatement newForStatement(IASTStatement init, IASTExpression condition, 
			IASTExpression iterationExpr, IASTStatement body) {
		return new CPPASTForStatement(init, condition, iterationExpr, body);
	}

	public IASTDeclarationStatement newDeclarationStatement(IASTDeclaration declaration) {
		return new CPPASTDeclarationStatement(declaration);
	}

	public IASTTypeIdExpression newTypeIdExpression(int operator, IASTTypeId typeId) {
		return new CPPASTTypeIdExpression(operator, typeId);
	}

	public IASTDeclarator newDeclarator(IASTName name) {
		return new CPPASTDeclarator(name);
	}

	public IASTTypeId newTypeId(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator) {
		return new CPPASTTypeId(declSpecifier, declarator);
	}

	public ICPPASTDeleteExpression newDeleteExpression(IASTExpression operand) {
		return new CPPASTDeleteExpression(operand);
	}

	public IASTSimpleDeclaration newSimpleDeclaration(IASTDeclSpecifier declSpecifier) {
		return new CPPASTSimpleDeclaration(declSpecifier);
	}

	public IASTInitializerExpression newInitializerExpression(IASTExpression expression) {
		return new CPPASTInitializerExpression(expression);
	}

	public IASTFunctionDefinition newFunctionDefinition(IASTDeclSpecifier declSpecifier, IASTFunctionDeclarator declarator,
			IASTStatement bodyStatement) {
		return new CPPASTFunctionDefinition(declSpecifier, declarator, bodyStatement);
	}

	public IASTTranslationUnit newTranslationUnit() {
		return new CPPASTTranslationUnit();
	}

	public ICPPASTSimpleDeclSpecifier newCPPSimpleDeclSpecifier() {
		return new CPPASTSimpleDeclSpecifier();
	}

	public IASTStandardFunctionDeclarator newFunctionDeclarator(IASTName name) {
		return new CPPASTFunctionDeclarator(name);
	}

	public ICPPASTSimpleTypeConstructorExpression newCPPSimpleTypeConstructorExpression(
			int type, IASTExpression expression) {
		return new CPPASTSimpleTypeConstructorExpression(type, expression);
	}

	public ICPPASTTypenameExpression newCPPTypenameExpression(IASTName qualifiedName, IASTExpression expr, boolean isTemplate) {
		return new CPPASTTypenameExpression(qualifiedName, expr, isTemplate);
	}

	public IASTASMDeclaration newASMDeclaration(String assembly) {
		return new CPPASTASMDeclaration(assembly);
	}

	public ICPPASTNamespaceAlias newNamespaceAlias(IASTName alias, IASTName qualifiedName) {
		return new CPPASTNamespaceAlias(alias, qualifiedName);
	}

	public ICPPASTUsingDeclaration newUsingDeclaration(IASTName name) {
		return new CPPASTUsingDeclaration(name);
	}

	public ICPPASTUsingDirective newUsingDirective(IASTName name) {
		return new CPPASTUsingDirective(name);
	}

	public ICPPASTLinkageSpecification newLinkageSpecification(String name) {
		return new CPPASTLinkageSpecification(name);
	}

	public ICPPASTNamespaceDefinition newNamespaceDefinition(IASTName name) {
		return new CPPASTNamespaceDefinition(name);
	}

	public ICPPASTTemplateDeclaration newTemplateDeclaration(IASTDeclaration declaration) {
		return new CPPASTTemplateDeclaration(declaration);
	}

	public ICPPASTExplicitTemplateInstantiation newExplicitTemplateInstantiation(IASTDeclaration declaration) {
		return new CPPASTExplicitTemplateInstantiation(declaration);
	}

	public ICPPASTTemplateSpecialization newTemplateSpecialization(IASTDeclaration declaration) {
		return new CPPASTTemplateSpecialization(declaration);
	}

	public ICPPASTTryBlockStatement newTryBlockStatement(IASTStatement body) {
		return new CPPASTTryBlockStatement(body);
	}

	public ICPPASTCatchHandler newCatchHandler(IASTDeclaration decl, IASTStatement body) {
		return new CPPASTCatchHandler(decl, body);
	}

	public IASTEnumerationSpecifier newEnumerationSpecifier(IASTName name) {
		return new CPPASTEnumerationSpecifier(name);
	}

	public IASTEnumerator newEnumerator(IASTName name, IASTExpression value) {
		return new CPPASTEnumerator(name, value);
	}

	public ICPPASTVisiblityLabel newVisibilityLabel(int visibility) {
		return new CPPASTVisibilityLabel(visibility);
	}

	public ICPPASTBaseSpecifier newBaseSpecifier(IASTName name, int visibility, boolean isVirtual) {
		return new CPPASTBaseSpecifier(name, visibility, isVirtual);
	}

	public ICPPASTCompositeTypeSpecifier newCPPCompositeTypeSpecifier(int key, IASTName name) {
		return new CPPASTCompositeTypeSpecifier(key, name);
	}

	public ICPPASTNamedTypeSpecifier newCPPNamedTypeSpecifier(IASTName name, boolean typename) {
		return new CPPASTNamedTypeSpecifier(name, typename);
	}

	public IASTElaboratedTypeSpecifier newElaboratedTypeSpecifier(int kind, IASTName name) {
		return new CPPASTElaboratedTypeSpecifier(kind, name);
	}

	public IASTPointer newCPPPointer() {
		return new CPPASTPointer();
	}

	public ICPPASTReferenceOperator newReferenceOperator() {
		return new CPPASTReferenceOperator();
	}

	public ICPPASTPointerToMember newPointerToMember(IASTName name) {
		return new CPPASTPointerToMember(name);
	}

	public IASTInitializerList newInitializerList() {
		return new CPPASTInitializerList();
	}

	public ICPPASTConstructorInitializer newConstructorInitializer(IASTExpression exp) {
		return new CPPASTConstructorInitializer(exp);
	}

	public IASTArrayModifier newArrayModifier(IASTExpression expr) {
		return new CPPASTArrayModifier(expr);
	}

	public IASTArrayDeclarator newArrayDeclarator(IASTName name) {
		return new CPPASTArrayDeclarator(name);
	}

	public ICPPASTFunctionDeclarator newCPPFunctionDeclarator(IASTName name) {
		return new CPPASTFunctionDeclarator(name);
	}

	public IASTParameterDeclaration newParameterDeclaration(
			IASTDeclSpecifier declSpec, IASTDeclarator declarator) {
		return new CPPASTParameterDeclaration(declSpec, declarator);
	}

	public ICPPASTConstructorChainInitializer newConstructorChainInitializer(IASTName name, IASTExpression expr) {
		return new CPPASTConstructorChainInitializer(name, expr);
	}

	public ICPPASTFunctionTryBlockDeclarator newFunctionTryBlockDeclarator(IASTName name) {
		return new CPPASTFunctionTryBlockDeclarator(name);
	}

	public IASTFieldDeclarator newFieldDeclarator(IASTName name, IASTExpression bitFieldSize) {
		return new CPPASTFieldDeclarator(name, bitFieldSize);
	}

	public ICPPASTSimpleTypeTemplateParameter newSimpleTypeTemplateParameter(int type, IASTName name, IASTTypeId typeId) {
		return new CPPASTSimpleTypeTemplateParameter(type, name, typeId);
	}

	public ICPPASTTemplatedTypeTemplateParameter newTemplatedTypeTemplateParameter(IASTName name, IASTExpression idExpression) {
		return new CPPASTTemplatedTypeTemplateParameter(name, idExpression);
	}

	public IASTAmbiguousDeclaration newAmbiguousDeclaration(IASTDeclaration... declarations) {
		return new CPPASTAmbiguousDeclaration(declarations);
	}

	public IASTAmbiguousExpression newAmbiguousExpression(IASTExpression... expressions) {
		return new CPPASTAmbiguousExpression(expressions);
	}

	public IASTAmbiguousStatement newAmbiguousStatement(IASTStatement... statements) {
		return new CPPASTAmbiguousStatement(statements);
	}

	public IASTDeclSpecifier newSimpleDeclSpecifier() {
		return newCPPSimpleDeclSpecifier();
	}

}
