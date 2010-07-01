/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTGotoStatement;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTInitializerClause;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCatchHandler;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorChainInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConstructorInitializer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTEnumerationSpecifier;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDirective;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNodeFactory;
import org.eclipse.cdt.core.dom.ast.gnu.IGNUASTCompoundStatementExpression;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointerToMember;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.internal.core.dom.parser.NodeFactory;
import org.eclipse.cdt.internal.core.parser.scanner.CPreprocessor;


/**
 * Abstract factory implementation that creates C++ AST nodes.
 */
public class CPPNodeFactory extends NodeFactory implements ICPPNodeFactory {

	private static final CPPNodeFactory DEFAULT_INSTANCE = new CPPNodeFactory();
	
	public static CPPNodeFactory getDefault() {
		return DEFAULT_INSTANCE;
	}
	
	public ICPPASTArrayDeclarator newArrayDeclarator(IASTName name) {
		return new CPPASTArrayDeclarator(name);
	}
	
	public IASTArrayModifier newArrayModifier(IASTExpression expr) {
		return new CPPASTArrayModifier(expr);
	}

	public ICPPASTArraySubscriptExpression newArraySubscriptExpression(IASTExpression arrayExpr, IASTExpression subscript) {
		return new CPPASTArraySubscriptExpression(arrayExpr, subscript);
	}
	
	public ICPPASTArraySubscriptExpression newArraySubscriptExpression(IASTExpression arrayExpr,
			IASTInitializerClause subscript) {
		return new CPPASTArraySubscriptExpression(arrayExpr, subscript);
	}
	
	public IASTASMDeclaration newASMDeclaration(String assembly) {
		return new CPPASTASMDeclaration(assembly);
	}

	public ICPPASTBaseSpecifier newBaseSpecifier(IASTName name, int visibility, boolean isVirtual) {
		return new CPPASTBaseSpecifier(name, visibility, isVirtual);
	}

	public ICPPASTBinaryExpression newBinaryExpression(int op, IASTExpression expr1, IASTExpression expr2) {
		return new CPPASTBinaryExpression(op, expr1, expr2);
	}

	public ICPPASTBinaryExpression newBinaryExpression(int op, IASTExpression expr1, IASTInitializerClause expr2) {
		return new CPPASTBinaryExpression(op, expr1, expr2);
	}

	public IASTBreakStatement newBreakStatement() {
		return new CPPASTBreakStatement();
	}

	public IASTCaseStatement newCaseStatement(IASTExpression expr) {
		return new CPPASTCaseStatement(expr);
	}

	public ICPPASTCastExpression newCastExpression(int operator, IASTTypeId typeId, IASTExpression operand) {
		return new CPPASTCastExpression(operator, typeId, operand);
	}

	public ICPPASTCatchHandler newCatchHandler(IASTDeclaration decl, IASTStatement body) {
		return new CPPASTCatchHandler(decl, body);
	}

	public ICPPASTCompositeTypeSpecifier newCompositeTypeSpecifier(int key, IASTName name) {
		return new CPPASTCompositeTypeSpecifier(key, name);
	}

	public IASTCompoundStatement newCompoundStatement() {
		return new CPPASTCompoundStatement();
	}

	public IASTConditionalExpression newConditionalExpession(IASTExpression condition, IASTExpression positive, IASTExpression negative) {
		return new CPPASTConditionalExpression(condition, positive, negative);
	}

	@Deprecated
	public ICPPASTConstructorChainInitializer newConstructorChainInitializer(IASTName id, IASTExpression expression) {
		ICPPASTConstructorChainInitializer result= new CPPASTConstructorChainInitializer(id, null);
		result.setInitializerValue(expression);
		return result;
	}

	public ICPPASTConstructorChainInitializer newConstructorChainInitializer(IASTName id, IASTInitializer init) {
		return new CPPASTConstructorChainInitializer(id, init);
	}

	@Deprecated
	public ICPPASTConstructorInitializer newConstructorInitializer(IASTExpression exp) {
		ICPPASTConstructorInitializer result= new CPPASTConstructorInitializer(null);
		result.setExpression(exp);
		return result;
	}

	public ICPPASTConstructorInitializer newConstructorInitializer(IASTInitializerClause[] args) {
		return new CPPASTConstructorInitializer(args);
	}

	public IASTContinueStatement newContinueStatement() {
		return new CPPASTContinueStatement();
	}

	public ICPPASTConversionName newConversionName(IASTTypeId typeId) {
		return new CPPASTConversionName(typeId);
	}
	
	public IASTDeclarationStatement newDeclarationStatement(IASTDeclaration declaration) {
		return new CPPASTDeclarationStatement(declaration);
	}

	public ICPPASTDeclarator newDeclarator(IASTName name) {
		return new CPPASTDeclarator(name);
	}
	
	public IASTDefaultStatement newDefaultStatement() {
		return new CPPASTDefaultStatement();
	}

	public ICPPASTDeleteExpression newDeleteExpression(IASTExpression operand) {
		return new CPPASTDeleteExpression(operand);
	}

	public IASTDoStatement newDoStatement(IASTStatement body, IASTExpression condition) {
		return new CPPASTDoStatement(body, condition);
	}

	public ICPPASTElaboratedTypeSpecifier newElaboratedTypeSpecifier(int kind, IASTName name) {
		return new CPPASTElaboratedTypeSpecifier(kind, name);
	}

	public ICPPASTEnumerationSpecifier newEnumerationSpecifier(boolean isScoped, IASTName name,
			ICPPASTDeclSpecifier baseType) {
		return new CPPASTEnumerationSpecifier(isScoped, name, baseType);
	}

	public ICPPASTEnumerationSpecifier newEnumerationSpecifier(IASTName name) {
		return new CPPASTEnumerationSpecifier(false, name, null);
	}

	public IASTEnumerator newEnumerator(IASTName name, IASTExpression value) {
		return new CPPASTEnumerator(name, value);
	}

	public IASTEqualsInitializer newEqualsInitializer(IASTInitializerClause initClause) {
		return new CPPASTEqualsInitializer(initClause);
	}

	public ICPPASTExplicitTemplateInstantiation newExplicitTemplateInstantiation(IASTDeclaration declaration) {
		return new CPPASTExplicitTemplateInstantiation(declaration);
	}

	@Deprecated
	public org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTExplicitTemplateInstantiation newExplicitTemplateInstantiationGPP(IASTDeclaration declaration) {
		return new GPPASTExplicitTemplateInstantiation(declaration);
	}

	public ICPPASTExpressionList newExpressionList() {
		return new CPPASTExpressionList();
	}
	
	public IASTExpressionStatement newExpressionStatement(IASTExpression expression) {
		return new CPPASTExpressionStatement(expression);
	}
	
	public ICPPASTFieldDeclarator newFieldDeclarator(IASTName name, IASTExpression bitFieldSize) {
		return new CPPASTFieldDeclarator(name, bitFieldSize);
	}

	public ICPPASTFieldReference newFieldReference(IASTName name, IASTExpression owner) {
		return new CPPASTFieldReference(name, owner);
	}
	
	public ICPPASTForStatement newForStatement() {
		return new CPPASTForStatement();
	}

	public ICPPASTForStatement newForStatement(IASTStatement init, IASTDeclaration condition, 
			IASTExpression iterationExpression, IASTStatement body) {
		return new CPPASTForStatement(init, condition, iterationExpression, body);
	}

	public ICPPASTForStatement newForStatement(IASTStatement init, IASTExpression condition, 
			IASTExpression iterationExpr, IASTStatement body) {
		return new CPPASTForStatement(init, condition, iterationExpr, body);
	}
	
	@Deprecated
	public ICPPASTFunctionCallExpression newFunctionCallExpression(IASTExpression idExpr, IASTExpression argList) {
		CPPASTFunctionCallExpression result = new CPPASTFunctionCallExpression(idExpr, null);
		result.setParameterExpression(argList);
		return result;
	}

	public ICPPASTFunctionCallExpression newFunctionCallExpression(IASTExpression idExpr, IASTInitializerClause[] arguments) {
		return new CPPASTFunctionCallExpression(idExpr, arguments);
	}

	public ICPPASTFunctionDeclarator newFunctionDeclarator(IASTName name) {
		return new CPPASTFunctionDeclarator(name);
	}

	public ICPPASTFunctionDefinition newFunctionDefinition(IASTDeclSpecifier declSpecifier, IASTFunctionDeclarator declarator,
			IASTStatement bodyStatement) {
		return new CPPASTFunctionDefinition(declSpecifier, declarator, bodyStatement);
	}

	public ICPPASTFunctionWithTryBlock newFunctionTryBlock(IASTDeclSpecifier declSpecifier, IASTFunctionDeclarator declarator,
			IASTStatement bodyStatement) {
		return new CPPASTFunctionWithTryBlock(declSpecifier, declarator, bodyStatement);
	}

	public IGNUASTCompoundStatementExpression newGNUCompoundStatementExpression(IASTCompoundStatement compoundStatement) {
		return new CPPASTCompoundStatementExpression(compoundStatement);
	}
	
	public IASTGotoStatement newGotoStatement(IASTName name) {
		return new CPPASTGotoStatement(name);
	}
	
	public IASTIdExpression newIdExpression(IASTName name) {
		return new CPPASTIdExpression(name);
	}

	public ICPPASTIfStatement newIfStatement() {
		return new CPPASTIfStatement();
	}

	public ICPPASTIfStatement newIfStatement(IASTDeclaration condition, IASTStatement then, IASTStatement elseClause) {
		return new CPPASTIfStatement(condition, then, elseClause);
	}

	public ICPPASTIfStatement newIfStatement(IASTExpression condition, IASTStatement then, IASTStatement elseClause) {
		return new CPPASTIfStatement(condition, then, elseClause);
	}

	@Deprecated
	public org.eclipse.cdt.core.dom.ast.IASTInitializerExpression newInitializerExpression(IASTExpression expression) {
		return new CPPASTInitializerExpression(expression);
	}

	public ICPPASTInitializerList newInitializerList() {
		return new CPPASTInitializerList();
	}

	public IASTLabelStatement newLabelStatement(IASTName name, IASTStatement nestedStatement) {
		return new CPPASTLabelStatement(name, nestedStatement);
	}
	
	public ICPPASTLinkageSpecification newLinkageSpecification(String literal) {
		return new CPPASTLinkageSpecification(literal);
	}

	public ICPPASTLiteralExpression newLiteralExpression(int kind, String rep) {
		return new CPPASTLiteralExpression(kind, rep.toCharArray());
	}
	
	public IASTName newName() {
		return new CPPASTName();
	}

	public IASTName newName(char[] name) {
		return new CPPASTName(name);
	}

	public ICPPASTNamespaceAlias newNamespaceAlias(IASTName alias, IASTName qualifiedName) {
		return new CPPASTNamespaceAlias(alias, qualifiedName);
	}

	public ICPPASTNamespaceDefinition newNamespaceDefinition(IASTName name) {
		return new CPPASTNamespaceDefinition(name);
	}

	@Deprecated
	public ICPPASTNewExpression newNewExpression(IASTExpression placement, IASTExpression initializer, IASTTypeId typeId) {
		final ICPPASTNewExpression result = new CPPASTNewExpression(null, null, typeId);
		result.setNewPlacement(placement);
		result.setNewInitializer(initializer);
		return result;
	}

	public ICPPASTNewExpression newNewExpression(IASTInitializerClause[] placement, IASTInitializer initializer, IASTTypeId typeId) {
		return new CPPASTNewExpression(placement, initializer, typeId);
	}

	public IASTNullStatement newNullStatement() {
		return new CPPASTNullStatement();
	}

	public ICPPASTOperatorName newOperatorName(char[] name) {
		return new CPPASTOperatorName(name);
	}

	public ICPPASTPackExpansionExpression newPackExpansionExpression(IASTExpression pattern) {
		return new CPPASTPackExpansionExpression(pattern);
	}

	public ICPPASTParameterDeclaration newParameterDeclaration(IASTDeclSpecifier declSpec, IASTDeclarator declarator) {
		return new CPPASTParameterDeclaration(declSpec, declarator);
	}

	public IASTPointer newPointer() {
		return new CPPASTPointer();
	}

	public IGPPASTPointer newPointerGPP() {
		return new GPPASTPointer();
	}
	
	public ICPPASTPointerToMember newPointerToMember(IASTName name) {
		return new CPPASTPointerToMember(name);
	}

	public IGPPASTPointerToMember newPointerToMemberGPP(IASTName name) {
		return new GPPASTPointerToMember(name);
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

	public IASTProblemTypeId newProblemTypeId(IASTProblem problem) {
		return new CPPASTProblemTypeId(problem);
	}

	public ICPPASTQualifiedName newQualifiedName() {
		return new CPPASTQualifiedName();
	}

	public ICPPASTReferenceOperator newReferenceOperator() {
		return new CPPASTReferenceOperator(false);
	}

	public ICPPASTReferenceOperator newReferenceOperator(boolean isRValueReference) {
		return new CPPASTReferenceOperator(isRValueReference);
	}

	public IASTReturnStatement newReturnStatement(IASTExpression retValue) {
		return new CPPASTReturnStatement(retValue);
	}

	public IASTReturnStatement newReturnStatement(IASTInitializerClause retValue) {
		return new CPPASTReturnStatement(retValue);
	}
	
	public IASTSimpleDeclaration newSimpleDeclaration(IASTDeclSpecifier declSpecifier) {
		return new CPPASTSimpleDeclaration(declSpecifier);
	}

	public ICPPASTSimpleDeclSpecifier newSimpleDeclSpecifier() {
		return new CPPASTSimpleDeclSpecifier();
	}

	@Deprecated
	public org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier newSimpleDeclSpecifierGPP() {
		return new GPPASTSimpleDeclSpecifier();
	}
	
	public ICPPASTSimpleTypeConstructorExpression newSimpleTypeConstructorExpression(
			ICPPASTDeclSpecifier declSpec, IASTInitializer initializer) {
		return new CPPASTSimpleTypeConstructorExpression(declSpec, initializer);
	}

	@Deprecated
	public ICPPASTSimpleTypeConstructorExpression newSimpleTypeConstructorExpression(int type, IASTExpression expression) {
		CPPASTSimpleTypeConstructorExpression result = new CPPASTSimpleTypeConstructorExpression();
		result.setSimpleType(type);
		result.setInitialValue(expression);
		return result;
	}

	public ICPPASTSimpleTypeTemplateParameter newSimpleTypeTemplateParameter(int type, IASTName name, IASTTypeId typeId) {
		return new CPPASTSimpleTypeTemplateParameter(type, name, typeId);
	}

	public ICPPASTStaticAssertDeclaration newStaticAssertion(IASTExpression condition,
			ICPPASTLiteralExpression message) {
		return new CPPASTStaticAssertionDeclaration(condition, message);
	}

	public ICPPASTSwitchStatement newSwitchStatement() {
		return new CPPASTSwitchStatement();
	}

	public ICPPASTSwitchStatement newSwitchStatement(IASTDeclaration controller, IASTStatement body) {
		return new CPPASTSwitchStatement(controller, body);
	}

	public ICPPASTSwitchStatement newSwitchStatement(IASTExpression controller, IASTStatement body) {
		return new CPPASTSwitchStatement(controller, body);
	}

	public ICPPASTTemplateDeclaration newTemplateDeclaration(IASTDeclaration declaration) {
		return new CPPASTTemplateDeclaration(declaration);
	}
	
	public ICPPASTTemplatedTypeTemplateParameter newTemplatedTypeTemplateParameter(IASTName name, IASTExpression defaultValue) {
		return new CPPASTTemplatedTypeTemplateParameter(name, defaultValue);
	}

	public ICPPASTTemplateId newTemplateId(IASTName templateName) {
		return new CPPASTTemplateId(templateName);
	}

	public ICPPASTTemplateSpecialization newTemplateSpecialization(IASTDeclaration declaration) {
		return new CPPASTTemplateSpecialization(declaration);
	}

	public ICPPASTTranslationUnit newTranslationUnit() {
		return newTranslationUnit(null);
	}

	public ICPPASTTranslationUnit newTranslationUnit(IScanner scanner) {
		CPPASTTranslationUnit tu = new CPPASTTranslationUnit();
		
		if (scanner != null) {
			tu.setLocationResolver(scanner.getLocationResolver());
			if (scanner instanceof CPreprocessor) {
				tu.setIsForContentAssist(((CPreprocessor) scanner).isContentAssistMode());
			}
		}
		tu.setASTNodeFactory(this);
		return tu;
	}

	public ICPPASTTryBlockStatement newTryBlockStatement(IASTStatement body) {
		return new CPPASTTryBlockStatement(body);
	}

	public ICPPASTNamedTypeSpecifier newTypedefNameSpecifier(IASTName name) {
		return new CPPASTNamedTypeSpecifier(name);
	}

	public ICPPASTTypeId newTypeId(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator) {
		return new CPPASTTypeId(declSpecifier, declarator);
	}

	public ICPPASTTypeIdExpression newTypeIdExpression(int operator, IASTTypeId typeId) {
		return new CPPASTTypeIdExpression(operator, typeId);
	}

	public IASTTypeIdInitializerExpression newTypeIdInitializerExpression(IASTTypeId typeId, IASTInitializer initializer) {
		return new CPPASTTypeIdInitializerExpression(typeId, initializer);
	}
	
	@Deprecated
	public org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTypenameExpression newTypenameExpression(IASTName qualifiedName, IASTExpression expr, boolean isTemplate) {
		return new CPPASTTypenameExpression(qualifiedName, expr);
	}

	public ICPPASTUnaryExpression newUnaryExpression(int operator, IASTExpression operand) {
		return new CPPASTUnaryExpression(operator, operand);
	}

	public ICPPASTUsingDeclaration newUsingDeclaration(IASTName name) {
		return new CPPASTUsingDeclaration(name);
	}

	public ICPPASTUsingDirective newUsingDirective(IASTName name) {
		return new CPPASTUsingDirective(name);
	}

	public ICPPASTVisibilityLabel newVisibilityLabel(int visibility) {
		return new CPPASTVisibilityLabel(visibility);
	}

	public ICPPASTWhileStatement newWhileStatement() {
		return new CPPASTWhileStatement();
	}

	public ICPPASTWhileStatement newWhileStatement(IASTDeclaration condition, IASTStatement body) {
		return new CPPASTWhileStatement(condition, body);
	}

	public ICPPASTWhileStatement newWhileStatement(IASTExpression condition, IASTStatement body) {
		return new CPPASTWhileStatement(condition, body);
	}
}
