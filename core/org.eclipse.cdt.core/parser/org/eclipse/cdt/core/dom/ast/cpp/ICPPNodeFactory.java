/*******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mike Kucera (IBM Corporation) - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.ast.cpp;

import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemTypeId;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.INodeFactory;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTExplicitTemplateInstantiation;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointerToMember;
import org.eclipse.cdt.core.parser.IScanner;

/**
 * Factory for AST nodes for the C++ programming language.
 * 
 * @since 5.1
 * @noextend This interface is not intended to be extended by clients.
 * @noimplement This interface is not intended to be implemented by clients.
 */
public interface ICPPNodeFactory extends INodeFactory {
	
	/**
	 * Creates a new translation unit that cooperates with the given scanner in order
	 * to track macro-expansions and location information.
	 * @scanner the preprocessor the translation unit interacts with.
	 * @since 5.2
	 */
	public ICPPASTTranslationUnit newTranslationUnit(IScanner scanner);
	
	public ICPPASTLiteralExpression newLiteralExpression(int kind, String rep);
	
	public ICPPASTUnaryExpression newUnaryExpression(int operator, IASTExpression operand);
	
	public ICPPASTCastExpression newCastExpression(int operator, IASTTypeId typeId, IASTExpression operand);
	
	public ICPPASTBinaryExpression newBinaryExpression(int op, IASTExpression expr1, IASTExpression expr2);
	
	public ICPPASTTypeIdExpression newTypeIdExpression(int operator, IASTTypeId typeId);
	
	public ICPPASTFunctionDefinition newFunctionDefinition(IASTDeclSpecifier declSpecifier,
			IASTFunctionDeclarator declarator, IASTStatement bodyStatement);
	
	/**
	 * @since 5.2
	 */
	public ICPPASTDeclarator newDeclarator(IASTName name);

	public ICPPASTFunctionDeclarator newFunctionDeclarator(IASTName name);
	
	/**
	 * @since 5.2
	 */
	public ICPPASTArrayDeclarator newArrayDeclarator(IASTName name);

	/**
	 * @since 5.2
	 */
	public ICPPASTFieldDeclarator newFieldDeclarator(IASTName name, IASTExpression bitFieldSize);

	public ICPPASTElaboratedTypeSpecifier newElaboratedTypeSpecifier(int kind, IASTName name);
	
	public ICPPASTParameterDeclaration newParameterDeclaration(IASTDeclSpecifier declSpec, IASTDeclarator declarator);

	public ICPPASTSimpleDeclSpecifier newSimpleDeclSpecifier();
	
	public ICPPASTOperatorName newOperatorName(char[] name);

	public ICPPASTNewExpression newNewExpression(IASTExpression placement, IASTExpression initializer, IASTTypeId typeId);
	
	public ICPPASTFieldReference newFieldReference(IASTName name, IASTExpression owner);
	
	public ICPPASTTemplateId newTemplateId(IASTName templateName);

	public ICPPASTConversionName newConversionName(IASTTypeId typeId);

	public ICPPASTQualifiedName newQualifiedName();
	
	public ICPPASTSwitchStatement newSwitchStatement(IASTExpression controlloer, IASTStatement body);
	
	public ICPPASTSwitchStatement newSwitchStatement(IASTDeclaration controller, IASTStatement body);
	
	public ICPPASTSwitchStatement newSwitchStatement();

	public ICPPASTIfStatement newIfStatement(IASTExpression condition, IASTStatement then, IASTStatement elseClause);
	
	public ICPPASTIfStatement newIfStatement(IASTDeclaration condition, IASTStatement then, IASTStatement elseClause);
	
	public ICPPASTIfStatement newIfStatement();
	
	public ICPPASTForStatement newForStatement(IASTStatement init, IASTExpression condition,
			IASTExpression iterationExpression, IASTStatement body);
	
	public ICPPASTForStatement newForStatement(IASTStatement init, IASTDeclaration condition,
			IASTExpression iterationExpression, IASTStatement body);
	
	public ICPPASTForStatement newForStatement();
	
	public ICPPASTWhileStatement newWhileStatement(IASTExpression condition, IASTStatement body);
	
	public ICPPASTWhileStatement newWhileStatement(IASTDeclaration condition, IASTStatement body);
	
	public ICPPASTWhileStatement newWhileStatement();

	/**
	 * @since 5.2
	 */
	public ICPPASTTypeId newTypeId(IASTDeclSpecifier declSpecifier, IASTDeclarator declarator);

	public ICPPASTDeleteExpression newDeleteExpression(IASTExpression operand);
	
	public ICPPASTSimpleTypeConstructorExpression newSimpleTypeConstructorExpression(int type, IASTExpression expression);

	public ICPPASTTypenameExpression newTypenameExpression(IASTName qualifiedName, IASTExpression expr, boolean isTemplate);

	public ICPPASTNamespaceAlias newNamespaceAlias(IASTName alias, IASTName qualifiedName);
	
	public ICPPASTUsingDeclaration newUsingDeclaration(IASTName name);
	
	public ICPPASTUsingDirective newUsingDirective(IASTName name);

	public ICPPASTLinkageSpecification newLinkageSpecification(String literal);
	
	public ICPPASTNamespaceDefinition newNamespaceDefinition(IASTName name);

	public ICPPASTTemplateDeclaration newTemplateDeclaration(IASTDeclaration declaration);

	public ICPPASTExplicitTemplateInstantiation newExplicitTemplateInstantiation(IASTDeclaration declaration);
	
	public IGPPASTExplicitTemplateInstantiation newExplicitTemplateInstantiationGPP(IASTDeclaration declaration);

	public ICPPASTTemplateSpecialization newTemplateSpecialization(IASTDeclaration declaration);

	public ICPPASTTryBlockStatement newTryBlockStatement(IASTStatement body);

	public ICPPASTCatchHandler newCatchHandler(IASTDeclaration decl, IASTStatement body);
	
	public ICPPASTVisibilityLabel newVisibilityLabel(int visibility);
	
	public ICPPASTBaseSpecifier newBaseSpecifier(IASTName name, int visibility, boolean isVirtual);
	
	public ICPPASTCompositeTypeSpecifier newCompositeTypeSpecifier(int key, IASTName name);
	
	public ICPPASTNamedTypeSpecifier newTypedefNameSpecifier(IASTName name);
	
	public IGPPASTPointer newPointerGPP();
	
	/**
	 * Creates an lvalue or rvalue reference operator.
	 * @since 5.2
	 */
	public ICPPASTReferenceOperator newReferenceOperator(boolean isRValueReference);

	public ICPPASTPointerToMember newPointerToMember(IASTName name);
	
	public IGPPASTPointerToMember newPointerToMemberGPP(IASTName name);

	/**
	 * @since 5.2
	 */
	public ICPPASTInitializerExpression newInitializerExpression(IASTExpression expression);
	
	/**
	 * @since 5.2
	 */
	public ICPPASTInitializerList newInitializerList();

	public ICPPASTConstructorInitializer newConstructorInitializer(IASTExpression exp);
	
	public ICPPASTConstructorChainInitializer newConstructorChainInitializer(IASTName memberInitializerId, IASTExpression initializerValue);

	public ICPPASTFunctionWithTryBlock newFunctionTryBlock(IASTDeclSpecifier declSpecifier, IASTFunctionDeclarator declarator,
			IASTStatement bodyStatement);

	public ICPPASTSimpleTypeTemplateParameter newSimpleTypeTemplateParameter(int type, IASTName name, IASTTypeId typeId);

	public ICPPASTTemplatedTypeTemplateParameter newTemplatedTypeTemplateParameter(IASTName name, IASTExpression defaultValue);
	
	public IASTProblemTypeId newProblemTypeId(IASTProblem problem);
	
	public ICPPASTExpressionList newExpressionList();
	
	public ICPPASTArraySubscriptExpression newArraySubscriptExpression(IASTExpression arrayExpr, IASTExpression subscript);
	
	public ICPPASTFunctionCallExpression newFunctionCallExpression(IASTExpression idExpr, IASTExpression argList);

	/**
	 * Creates a new static assertion declaration with the given condition and message.
	 * @since 5.2
	 */
	public ICPPASTStaticAssertDeclaration newStaticAssertion(IASTExpression condition, ICPPASTLiteralExpression message);
	
	/**
	 * Creates a new pack expansion expression for the given pattern.
	 * @since 5.2
	 */
	public ICPPASTPackExpansionExpression newPackExpansionExpression(IASTExpression pattern);
	
	/**
	 * @deprecated Replaced by {@link #newReferenceOperator(boolean)}.
	 */
	@Deprecated	public ICPPASTReferenceOperator newReferenceOperator();
	/**
	 * @deprecated Replaced by {@link #newTranslationUnit(IScanner)}.
	 */
	@Deprecated
	public ICPPASTTranslationUnit newTranslationUnit();
	/**
	 * @deprecated Replaced by {@link #newSimpleDeclSpecifier()}
	 */
	@Deprecated
	public org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier newSimpleDeclSpecifierGPP();
}
