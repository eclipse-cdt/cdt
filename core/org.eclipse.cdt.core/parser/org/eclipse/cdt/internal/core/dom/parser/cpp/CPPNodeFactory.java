/*******************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mike Kucera (IBM) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.dom.parser.cpp;

import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTBreakStatement;
import org.eclipse.cdt.core.dom.ast.IASTCaseStatement;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTContinueStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTDefaultStatement;
import org.eclipse.cdt.core.dom.ast.IASTDoStatement;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNullStatement;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTProblemExpression;
import org.eclipse.cdt.core.dom.ast.IASTProblemStatement;
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTExpressionList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFieldReference;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTForStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionWithTryBlock;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTIfStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTInitializerList;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPackExpansionExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTReferenceOperator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTStaticAssertDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateSpecialization;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplatedTypeTemplateParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTryBlockStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeId;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.internal.core.dom.parser.NodeFactory;


/**
 * Abstract factory implementation that creates C++ AST nodes.
 */
public class CPPNodeFactory extends NodeFactory implements ICPPNodeFactory {

	private static final CPPNodeFactory DEFAULT_INSTANCE = new CPPNodeFactory();
	
	public static CPPNodeFactory getDefault() {
		return DEFAULT_INSTANCE;
	}
	
	public ICPPASTTranslationUnit newTranslationUnit() {
		return newTranslationUnit(null);
	}
	
	public ICPPASTTranslationUnit newTranslationUnit(IScanner scanner) {
		CPPASTTranslationUnit tu = new CPPASTTranslationUnit();
		
		if (scanner != null) {
			tu.setLocationResolver(scanner.getLocationResolver());
		}
		tu.setASTNodeFactory(this);
		return tu;
	}

	public IASTName newName(char[] name) {
		return new CPPASTName(name);
	}
	
	public IASTName newName() {
		return new CPPASTName();
	}
	
	public ICPPASTOperatorName newOperatorName(char[] name) {
		return new CPPASTOperatorName(name);
	}

	public IASTProblem newProblem(int id, char[] arg, boolean error) {
		return new CPPASTProblem(id, arg, error);
	}

	public IASTProblemDeclaration newProblemDeclaration(IASTProblem problem) {
		return new CPPASTProblemDeclaration(problem);
	}

	public IASTProblemExpression newProblemExpression(IASTProblem problem) {
		return new CPPASTProblemExpression(problem);
	}

	public IASTProblemStatement newProblemStatement(IASTProblem problem) {
		return new CPPASTProblemStatement(problem);
	}

	public ICPPASTLiteralExpression newLiteralExpression(int kind, String rep) {
		return new CPPASTLiteralExpression(kind, rep.toCharArray());
	}

	public ICPPASTUnaryExpression newUnaryExpression(int operator, IASTExpression operand) {
		return new CPPASTUnaryExpression(operator, operand);
	}

	public IASTIdExpression newIdExpression(IASTName name) {
		return new CPPASTIdExpression(name);
	}

	public ICPPASTArraySubscriptExpression newArraySubscriptExpression(IASTExpression arrayExpr, IASTExpression subscript) {
		return new CPPASTArraySubscriptExpression(arrayExpr, subscript);
	}

	public ICPPASTExpressionList newExpressionList() {
		return new CPPASTExpressionList();
	}

	public ICPPASTFunctionCallExpression newFunctionCallExpression(IASTExpression idExpr, IASTExpression argList) {
		return new CPPASTFunctionCallExpression(idExpr, argList);
	}

	public ICPPASTCastExpression newCastExpression(int operator, IASTTypeId typeId, IASTExpression operand) {
		return new CPPASTCastExpression(operator, typeId, operand);
	}

	public ICPPASTNewExpression newNewExpression(IASTExpression placement, IASTExpression initializer, IASTTypeId typeId) {
		return new CPPASTNewExpression(placement, initializer, typeId);
	}

	public ICPPASTBinaryExpression newBinaryExpression(int op, IASTExpression expr1, IASTExpression expr2) {
		return new CPPASTBinaryExpression(op, expr1, expr2);
	}

	public IASTConditionalExpression newConditionalExpession(IASTExpression expr1, IASTExpression expr2, IASTExpression expr3) {
		return new CPPASTConditionalExpression(expr1, expr2, expr3);
	}
	
	public IASTTypeIdInitializerExpression newTypeIdInitializerExpression(IASTTypeId typeId, IASTInitializer initializer) {
		return new CPPASTTypeIdInitializerExpression(typeId, initializer);
	}

	public ICPPASTFieldReference newFieldReference(IASTName name, IASTExpression owner) {
		return new CPPASTFieldReference(name, owner);
	}
	
	public ICPPASTTemplateId newTemplateId(IASTName templateName) {
		return new CPPASTTemplateId(templateName);
	}

	public ICPPASTConversionName newConversionName(IASTTypeId typeId) {
		return new CPPASTConversionName(typeId);
	}

	public ICPPASTQualifiedName newQualifiedName() {
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

	public ICPPASTIfStatement newIfStatement(IASTExpression condition, IASTStatement then, IASTStatement elseClause) {
		return new CPPASTIfStatement(condition, then, elseClause);
	}

	public ICPPASTIfStatement newIfStatement(IASTDeclaration condition, IASTStatement then, IASTStatement elseClause) {
		return new CPPASTIfStatement(condition, then, elseClause);
	}
	
	public ICPPASTIfStatement newIfStatement() {
		return new CPPASTIfStatement();
	}
	
	public ICPPASTSwitchStatement newSwitchStatement(IASTExpression controller, IASTStatement body) {
		return new CPPASTSwitchStatement(controller, body);
	}

	public ICPPASTSwitchStatement newSwitchStatement(IASTDeclaration controller, IASTStatement body) {
		return new CPPASTSwitchStatement(controller, body);
	}
	
	public ICPPASTSwitchStatement newSwitchStatement() {
		return new CPPASTSwitchStatement();
	}

	public ICPPASTWhileStatement newWhileStatement(IASTDeclaration condition, IASTStatement body) {
		return new CPPASTWhileStatement(condition, body);
	}

	public ICPPASTWhileStatement newWhileStatement(IASTExpression condition, IASTStatement body) {
		return new CPPASTWhileStatement(condition, body);
	}
	
	public ICPPASTWhileStatement newWhileStatement() {
		return new CPPASTWhileStatement();
	}
	
	public IASTDoStatement newDoStatement(IASTStatement body, IASTExpression condition) {
		return new CPPASTDoStatement(body, condition);
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

	public ICPPASTForStatement newForStatement(IASTStatement init, IASTExpression condition, 
			IASTExpression iterationExpr, IASTStatement body) {
		return new CPPASTForStatement(init, condition, iterationExpr, body);
	}

	public ICPPASTForStatement newForStatement(IASTStatement init, IASTDeclaration condition, 
			IASTExpression iterationExpression, IASTStatement body) {
		return new CPPASTForStatement(init, condition, iterationExpression, body);
	}
	
	public ICPPASTForStatement newForStatement() {
		return new CPPASTForStatement();
	}
	
	public IASTDeclarationStatement newDeclarationStatement(IASTDeclaration declaration) {
		return new CPPASTDeclarationStatement(declaration);
	}

	public ICPPASTTypeIdExpression newTypeIdExpression(int operator, IASTTypeId typeId) {
		return new CPPASTTypeIdExpression(operator, typeId);
	}

	public ICPPASTDeclarator newDeclarator(IASTName name) {
		return new CPPASTDeclarator(name);
	}

	public ICPPASTTypeId newTypeId(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator) {
		return new CPPASTTypeId(declSpecifier, declarator);
	}

	public ICPPASTDeleteExpression newDeleteExpression(IASTExpression operand) {
		return new CPPASTDeleteExpression(operand);
	}

	public IASTSimpleDeclaration newSimpleDeclaration(IASTDeclSpecifier declSpecifier) {
		return new CPPASTSimpleDeclaration(declSpecifier);
	}

	public ICPPASTInitializerExpression newInitializerExpression(IASTExpression expression) {
		return new CPPASTInitializerExpression(expression);
	}
	
	public ICPPASTFunctionDefinition newFunctionDefinition(IASTDeclSpecifier declSpecifier, IASTFunctionDeclarator declarator,
			IASTStatement bodyStatement) {
		return new CPPASTFunctionDefinition(declSpecifier, declarator, bodyStatement);
	}

	public ICPPASTSimpleDeclSpecifier newSimpleDeclSpecifier() {
		return new CPPASTSimpleDeclSpecifier();
	}
	
	public IGPPASTSimpleDeclSpecifier newSimpleDeclSpecifierGPP() {
		return new GPPASTSimpleDeclSpecifier();
	}

	public ICPPASTFunctionDeclarator newFunctionDeclarator(IASTName name) {
		return new CPPASTFunctionDeclarator(name);
	}

	public ICPPASTSimpleTypeConstructorExpression newSimpleTypeConstructorExpression(int type, IASTExpression expression) {
		return new CPPASTSimpleTypeConstructorExpression(type, expression);
	}

	public ICPPASTTypenameExpression newTypenameExpression(IASTName qualifiedName, IASTExpression expr, boolean isTemplate) {
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

	public ICPPASTLinkageSpecification newLinkageSpecification(String literal) {
		return new CPPASTLinkageSpecification(literal);
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
	
	public IGPPASTExplicitTemplateInstantiation newExplicitTemplateInstantiationGPP(IASTDeclaration declaration) {
		return new GPPASTExplicitTemplateInstantiation(declaration);
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

	public ICPPASTVisibilityLabel newVisibilityLabel(int visibility) {
		return new CPPASTVisibilityLabel(visibility);
	}

	public ICPPASTBaseSpecifier newBaseSpecifier(IASTName name, int visibility, boolean isVirtual) {
		return new CPPASTBaseSpecifier(name, visibility, isVirtual);
	}

	public ICPPASTCompositeTypeSpecifier newCompositeTypeSpecifier(int key, IASTName name) {
		return new CPPASTCompositeTypeSpecifier(key, name);
	}

	public ICPPASTNamedTypeSpecifier newTypedefNameSpecifier(IASTName name) {
		return new CPPASTNamedTypeSpecifier(name);
	}

	public ICPPASTElaboratedTypeSpecifier newElaboratedTypeSpecifier(int kind, IASTName name) {
		return new CPPASTElaboratedTypeSpecifier(kind, name);
	}

	public IASTPointer newPointer() {
		return new CPPASTPointer();
	}

	public IGPPASTPointer newPointerGPP() {
		return new GPPASTPointer();
	}
	
	public ICPPASTReferenceOperator newReferenceOperator() {
		return new CPPASTReferenceOperator(false);
	}

	public ICPPASTReferenceOperator newReferenceOperator(boolean isRValueReference) {
		return new CPPASTReferenceOperator(isRValueReference);
	}

	public ICPPASTPointerToMember newPointerToMember(IASTName name) {
		return new CPPASTPointerToMember(name);
	}
	
	public IGPPASTPointerToMember newPointerToMemberGPP(IASTName name) {
		return new GPPASTPointerToMember(name);
	}

	public ICPPASTInitializerList newInitializerList() {
		return new CPPASTInitializerList();
	}

	public ICPPASTConstructorInitializer newConstructorInitializer(IASTExpression exp) {
		return new CPPASTConstructorInitializer(exp);
	}

	public IASTArrayModifier newArrayModifier(IASTExpression expr) {
		return new CPPASTArrayModifier(expr);
	}

	public ICPPASTArrayDeclarator newArrayDeclarator(IASTName name) {
		return new CPPASTArrayDeclarator(name);
	}

	public ICPPASTParameterDeclaration newParameterDeclaration(IASTDeclSpecifier declSpec, IASTDeclarator declarator) {
		return new CPPASTParameterDeclaration(declSpec, declarator);
	}
	
	public ICPPASTConstructorChainInitializer newConstructorChainInitializer(IASTName memberInitializerid, IASTExpression initializerValue) {
		return new CPPASTConstructorChainInitializer(memberInitializerid, initializerValue);
	}

	public ICPPASTFunctionWithTryBlock newFunctionTryBlock(IASTDeclSpecifier declSpecifier, IASTFunctionDeclarator declarator,
			IASTStatement bodyStatement) {
		return new CPPASTFunctionWithTryBlock(declSpecifier, declarator, bodyStatement);
	}

	public ICPPASTFieldDeclarator newFieldDeclarator(IASTName name, IASTExpression bitFieldSize) {
		return new CPPASTFieldDeclarator(name, bitFieldSize);
	}

	public ICPPASTSimpleTypeTemplateParameter newSimpleTypeTemplateParameter(int type, IASTName name, IASTTypeId typeId) {
		return new CPPASTSimpleTypeTemplateParameter(type, name, typeId);
	}

	public ICPPASTTemplatedTypeTemplateParameter newTemplatedTypeTemplateParameter(IASTName name, IASTExpression defaultValue) {
		return new CPPASTTemplatedTypeTemplateParameter(name, defaultValue);
	}

	public IGNUASTCompoundStatementExpression newGNUCompoundStatementExpression(IASTCompoundStatement compoundStatement) {
		return new CPPASTCompoundStatementExpression(compoundStatement);
	}

	public IASTProblemTypeId newProblemTypeId(IASTProblem problem) {
		return new CPPASTProblemTypeId(problem);
	}

	public ICPPASTStaticAssertDeclaration newStaticAssertion(IASTExpression condition,
			ICPPASTLiteralExpression message) {
		return new CPPASTStaticAssertionDeclaration(condition, message);
	}

	public ICPPASTPackExpansionExpression newPackExpansionExpression(IASTExpression pattern) {
		return new CPPASTPackExpansionExpression(pattern);
	}
}
