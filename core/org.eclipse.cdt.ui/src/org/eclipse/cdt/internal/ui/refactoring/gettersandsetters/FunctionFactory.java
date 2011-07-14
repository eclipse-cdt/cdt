/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
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
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import java.util.Arrays;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode.CopyStyle;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.parser.Keywords;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTBinaryExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTCompoundStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTExpressionStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFieldReference;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDeclarator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTIdExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTLiteralExpression;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTName;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTReturnStatement;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclSpecifier;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTSimpleDeclaration;

public class FunctionFactory {

	public static IASTFunctionDefinition createGetterDefinition(IASTName fieldName,
			IASTSimpleDeclaration fieldDeclaration, ICPPASTQualifiedName name) {
		IASTFunctionDefinition getter = new CPPASTFunctionDefinition();
		
		getter.setDeclSpecifier(fieldDeclaration.getDeclSpecifier().copy(CopyStyle.withLocations));
		IASTDeclarator getterDeclarator = getGetterDeclarator(fieldName, fieldDeclaration, name);
		// IASTFunctionDefinition. expects the outermost IASTFunctionDeclarator in declarator hierarchy
		while (!(getterDeclarator instanceof IASTFunctionDeclarator)) {
			getterDeclarator = getterDeclarator.getNestedDeclarator();
		}
		getter.setDeclarator((IASTFunctionDeclarator) getterDeclarator);
		getter.setBody(getGetterBody(fieldName));
		return getter;
	}

	private static CPPASTCompoundStatement getGetterBody(IASTName fieldName) {
		CPPASTCompoundStatement compound = new CPPASTCompoundStatement();
		CPPASTReturnStatement returnStatement = new CPPASTReturnStatement();
		CPPASTIdExpression idExpr = new CPPASTIdExpression();
		CPPASTName returnVal = new CPPASTName();
		returnVal.setName(fieldName.toCharArray());
		idExpr.setName(returnVal);
		returnStatement.setReturnValue(idExpr);
		compound.addStatement(returnStatement);
		return compound;
	}

	private static IASTDeclarator getGetterDeclarator(IASTName fieldName,
			IASTSimpleDeclaration fieldDeclaration, ICPPASTQualifiedName name) {
		CPPASTName getterName = new CPPASTName();
		getterName.setName(GetterSetterNameGenerator.generateGetterName(fieldName).toCharArray());

		// copy declarator hierarchy
		IASTDeclarator topDeclarator = fieldDeclaration.getDeclarators()[0].copy(CopyStyle.withLocations);
		
		// find the innermost declarator in hierarchy
		IASTDeclarator innermost = topDeclarator;
		while (innermost.getNestedDeclarator() != null) {
			innermost = innermost.getNestedDeclarator();
		}
		
		// create a new innermost function declarator basing on the field declarator 
		CPPASTFunctionDeclarator functionDeclarator = new CPPASTFunctionDeclarator();
		functionDeclarator.setConst(true);
		if (name != null) {
			name.addName(getterName);
			functionDeclarator.setName(name);
		} else {
			functionDeclarator.setName(getterName);
		}
		for (IASTPointerOperator pointer : innermost.getPointerOperators()){
			functionDeclarator.addPointerOperator(pointer.copy(CopyStyle.withLocations));
		}
		
		// replace innermost with functionDeclarator and return the whole declarator tree
		if (innermost == topDeclarator) {
			// no tree
			return functionDeclarator;
		} else {
			IASTDeclarator parent = (IASTDeclarator) innermost.getParent();
			parent.setNestedDeclarator(functionDeclarator);
			return topDeclarator;
		}
	}

	public static IASTFunctionDefinition createSetterDefinition(IASTName fieldName,
			IASTSimpleDeclaration fieldDeclaration, ICPPASTQualifiedName name) {
		IASTFunctionDefinition setter = new CPPASTFunctionDefinition();
		setter.setDeclSpecifier(getVoidDeclSpec());		
		setter.setDeclarator(getSetterDeclarator(fieldName, fieldDeclaration, name));
		setter.setBody(getSetterBody(fieldDeclaration));
		return setter;
	}

	private static CPPASTCompoundStatement getSetterBody(IASTSimpleDeclaration fieldDeclaration) {
		CPPASTCompoundStatement compound = new CPPASTCompoundStatement();
		CPPASTExpressionStatement exprStmt = new CPPASTExpressionStatement();
		CPPASTBinaryExpression binExpr = new CPPASTBinaryExpression();
		IASTDeclarator innerDeclarator = fieldDeclaration.getDeclarators()[0];
		while (innerDeclarator.getNestedDeclarator() != null) {
			innerDeclarator = innerDeclarator.getNestedDeclarator();
		}
		IASTName fieldName = innerDeclarator.getName();
		CPPASTName parameterName = getSetterParameterName(fieldName);
		if (Arrays.equals(fieldName.getSimpleID(), parameterName.getSimpleID())) {
			CPPASTFieldReference fieldRef = new CPPASTFieldReference();
			CPPASTLiteralExpression litExpr = new CPPASTLiteralExpression();
			litExpr.setValue(Keywords.cTHIS); 
			fieldRef.setFieldOwner(litExpr);
			fieldRef.setIsPointerDereference(true);
			fieldRef.setFieldName(fieldName.copy(CopyStyle.withLocations));
			binExpr.setOperand1(fieldRef);
		} else {
			CPPASTIdExpression idExpr = new CPPASTIdExpression(fieldName.copy(CopyStyle.withLocations));
			binExpr.setOperand1(idExpr);
		}
		binExpr.setOperator(IASTBinaryExpression.op_assign);
		CPPASTIdExpression idExpr = new CPPASTIdExpression(parameterName);
		binExpr.setOperand2(idExpr);
		exprStmt.setExpression(binExpr);
		compound.addStatement(exprStmt);
		return compound;
	}

	private static CPPASTFunctionDeclarator getSetterDeclarator(IASTName fieldName,
			IASTSimpleDeclaration fieldDeclaration, ICPPASTQualifiedName name) {
		CPPASTName setterName = new CPPASTName();
		setterName.setName(GetterSetterNameGenerator.generateSetterName(fieldName).toCharArray());
		CPPASTFunctionDeclarator declarator = new CPPASTFunctionDeclarator();
		if (name != null) {
			name.addName(setterName);
			declarator.setName(name);
		} else {
			declarator.setName(setterName);
		}
		CPPASTParameterDeclaration parameterDeclaration = new CPPASTParameterDeclaration();
		IASTDeclarator parameterDeclarator = fieldDeclaration.getDeclarators()[0].copy(CopyStyle.withLocations);
		parameterDeclarator.setName(getSetterParameterName(fieldName));
		parameterDeclaration.setDeclarator(parameterDeclarator);
		parameterDeclaration.setDeclSpecifier(fieldDeclaration.getDeclSpecifier().copy(
				CopyStyle.withLocations));
		declarator.addParameterDeclaration(parameterDeclaration.copy(CopyStyle.withLocations));
		return declarator;
	}

	private static CPPASTName getSetterParameterName(IASTName fieldName) {
		String parameterName = GetterSetterNameGenerator.generateSetterParameterName(fieldName);
		return new CPPASTName(parameterName.toCharArray());
	}

	private static CPPASTSimpleDeclSpecifier getVoidDeclSpec() {
		CPPASTSimpleDeclSpecifier declSpecifier = new CPPASTSimpleDeclSpecifier();
		declSpecifier.setType(IASTSimpleDeclSpecifier.t_void);
		return declSpecifier;
	}

	public static IASTSimpleDeclaration createGetterDeclaration(IASTName fieldName,
			IASTSimpleDeclaration fieldDeclaration) {
		IASTSimpleDeclaration getter = new CPPASTSimpleDeclaration();
		getter.setDeclSpecifier(fieldDeclaration.getDeclSpecifier().copy(CopyStyle.withLocations));
		getter.addDeclarator(getGetterDeclarator(fieldName, fieldDeclaration, null));
		return getter;
	}

	public static IASTSimpleDeclaration createSetterDeclaration(IASTName fieldName,
			IASTSimpleDeclaration fieldDeclaration) {
		IASTSimpleDeclaration setter = new CPPASTSimpleDeclaration();
		setter.setDeclSpecifier(getVoidDeclSpec());		
		setter.addDeclarator(getSetterDeclarator(fieldName, fieldDeclaration, null));
		return setter;
	}
}
