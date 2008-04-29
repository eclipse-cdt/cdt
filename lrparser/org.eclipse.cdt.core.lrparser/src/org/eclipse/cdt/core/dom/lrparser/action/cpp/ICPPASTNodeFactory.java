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

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.lrparser.action.IASTNodeFactory;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguousDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;

/**
 * TODO remove CPP from method names.
 * 
 * @author Mike Kucera
 */
@SuppressWarnings("restriction")
public interface ICPPASTNodeFactory extends IASTNodeFactory {

	public ICPPASTOperatorName newCPPOperatorName(OverloadableOperator op);

	public ICPPASTNewExpression newCPPNewExpression(IASTExpression placement, IASTExpression initializer, IASTTypeId typeId);
	
	public IASTFieldReference newFieldReference(IASTName name, IASTExpression owner, boolean isPointerDereference, boolean isTemplate);
	
	public ICPPASTTemplateId newCPPTemplateId(IASTName templateName);

	public ICPPASTConversionName newCPPConversionName(char[] name, IASTTypeId typeId);

	public ICPPASTQualifiedName newCPPQualifiedName();
	
	public IASTSwitchStatement newSwitchStatment(IASTDeclaration controller, IASTStatement body);

	public IASTIfStatement newIfStatement(IASTDeclaration condition, IASTStatement then, IASTStatement elseClause);
	
	public IASTForStatement newForStatement(IASTStatement init, IASTDeclaration condition,
			IASTExpression iterationExpression, IASTStatement body);
	
	public IASTWhileStatement newWhileStatement(IASTDeclaration condition, IASTStatement body);

	public ICPPASTDeleteExpression newDeleteExpression(IASTExpression operand);
	
	public ICPPASTSimpleDeclSpecifier newCPPSimpleDeclSpecifier();

	public ICPPASTSimpleTypeConstructorExpression newCPPSimpleTypeConstructorExpression(int type, IASTExpression expression);

	public ICPPASTTypenameExpression newCPPTypenameExpression(IASTName qualifiedName, IASTExpression expr, boolean isTemplate);

	public ICPPASTNamespaceAlias newNamespaceAlias(IASTName alias, IASTName qualifiedName);
	
	public ICPPASTUsingDeclaration newUsingDeclaration(IASTName name);
	
	public ICPPASTUsingDirective newUsingDirective(IASTName name);

	public ICPPASTLinkageSpecification newLinkageSpecification(String name);
	
	public ICPPASTNamespaceDefinition newNamespaceDefinition(IASTName name);

	public ICPPASTTemplateDeclaration newTemplateDeclaration(IASTDeclaration declaration);

	public ICPPASTExplicitTemplateInstantiation newExplicitTemplateInstantiation(IASTDeclaration declaration);

	public ICPPASTTemplateSpecialization newTemplateSpecialization(IASTDeclaration declaration);

	public ICPPASTTryBlockStatement newTryBlockStatement(IASTStatement body);

	public ICPPASTCatchHandler newCatchHandler(IASTDeclaration decl, IASTStatement body);
	
	public ICPPASTVisibilityLabel newVisibilityLabel(int visibility);
	
	public ICPPASTBaseSpecifier newBaseSpecifier(IASTName name, int visibility, boolean isVirtual);
	
	public ICPPASTCompositeTypeSpecifier newCPPCompositeTypeSpecifier(int key, IASTName name);
	
	public ICPPASTNamedTypeSpecifier newCPPNamedTypeSpecifier(IASTName name, boolean typename);
	
	public IASTPointer newCPPPointer();
	
	public ICPPASTReferenceOperator newReferenceOperator();
	
	public ICPPASTPointerToMember newPointerToMember(IASTName name);

	public ICPPASTConstructorInitializer newConstructorInitializer(IASTExpression exp);

	public ICPPASTFunctionDeclarator newCPPFunctionDeclarator(IASTName name);

	public ICPPASTConstructorChainInitializer newConstructorChainInitializer(IASTName name, IASTExpression expr);

	public ICPPASTFunctionTryBlockDeclarator newFunctionTryBlockDeclarator(IASTName name);

	public ICPPASTSimpleTypeTemplateParameter newSimpleTypeTemplateParameter(int type, IASTName name, IASTTypeId typeId);

	public ICPPASTTemplatedTypeTemplateParameter newTemplatedTypeTemplateParameter(IASTName name, IASTExpression idExpression);
	
	public IASTAmbiguousDeclaration newAmbiguousDeclaration(IASTDeclaration... declarations);
}
