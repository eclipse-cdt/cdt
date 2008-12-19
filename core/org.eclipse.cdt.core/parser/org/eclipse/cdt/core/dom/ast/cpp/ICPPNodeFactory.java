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
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTSimpleDeclSpecifier;

/**
 * Factory for AST nodes for the C++ programming language.
 * 
 * @author Mike Kucera
 * @since 5.1
 */
public interface ICPPNodeFactory extends INodeFactory {
	
	public ICPPASTTranslationUnit newTranslationUnit();
	
	public ICPPASTLiteralExpression newLiteralExpression(int kind, String rep);
	
	public ICPPASTUnaryExpression newUnaryExpression(int operator, IASTExpression operand);
	
	public ICPPASTCastExpression newCastExpression(int operator, IASTTypeId typeId, IASTExpression operand);
	
	public ICPPASTBinaryExpression newBinaryExpression(int op, IASTExpression expr1, IASTExpression expr2);
	
	public ICPPASTTypeIdExpression newTypeIdExpression(int operator, IASTTypeId typeId);
	
	public ICPPASTFunctionDefinition newFunctionDefinition(IASTDeclSpecifier declSpecifier,
			IASTFunctionDeclarator declarator, IASTStatement bodyStatement);
	
	public ICPPASTFunctionDeclarator newFunctionDeclarator(IASTName name);
	
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

	public ICPPASTDeleteExpression newDeleteExpression(IASTExpression operand);
	
	public IGPPASTSimpleDeclSpecifier newSimpleDeclSpecifierGPP();

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
	
	public ICPPASTReferenceOperator newReferenceOperator();
	
	public ICPPASTPointerToMember newPointerToMember(IASTName name);
	
	public IGPPASTPointerToMember newPointerToMemberGPP(IASTName name);

	public ICPPASTConstructorInitializer newConstructorInitializer(IASTExpression exp);
	
	public ICPPASTConstructorChainInitializer newConstructorChainInitializer(IASTName memberInitializerId, IASTExpression initializerValue);

	public ICPPASTFunctionWithTryBlock newFunctionTryBlock(IASTDeclSpecifier declSpecifier, IASTFunctionDeclarator declarator,
			IASTStatement bodyStatement);

	public ICPPASTSimpleTypeTemplateParameter newSimpleTypeTemplateParameter(int type, IASTName name, IASTTypeId typeId);

	public ICPPASTTemplatedTypeTemplateParameter newTemplatedTypeTemplateParameter(IASTName name, IASTExpression defaultValue);
	
	public IASTProblemTypeId newProblemTypeId(IASTProblem problem);
}
