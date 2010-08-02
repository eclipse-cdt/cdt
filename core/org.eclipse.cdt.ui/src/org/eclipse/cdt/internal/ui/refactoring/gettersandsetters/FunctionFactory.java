/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *    Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
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

import org.eclipse.cdt.internal.ui.refactoring.utils.NameHelper;

public class FunctionFactory {

	public static IASTFunctionDefinition createGetterDefinition(String varName, IASTSimpleDeclaration fieldDeclaration, ICPPASTQualifiedName name) {
		
		IASTFunctionDefinition getter = new CPPASTFunctionDefinition();
		
		getter.setDeclSpecifier(fieldDeclaration.getDeclSpecifier().copy());	
		IASTDeclarator getterDeclarator = getGetterDeclarator(varName, fieldDeclaration, name);
		// IASTFunctionDefinition. expects the outermost IASTFunctionDeclarator in declarator hierarchy
		while (!(getterDeclarator instanceof IASTFunctionDeclarator)) {
			getterDeclarator = getterDeclarator.getNestedDeclarator();
		}
		getter.setDeclarator((IASTFunctionDeclarator) getterDeclarator);
		
		getter.setBody(getGetterBody(varName));
		
		return getter;
	}

	private static CPPASTCompoundStatement getGetterBody(String varName) {
		CPPASTCompoundStatement compound = new CPPASTCompoundStatement();
		CPPASTReturnStatement returnStatement = new CPPASTReturnStatement();
		CPPASTIdExpression idExpr = new CPPASTIdExpression();
		CPPASTName returnVal = new CPPASTName();
		returnVal.setName(varName.toCharArray());
		idExpr.setName(returnVal);
		returnStatement.setReturnValue(idExpr);
		compound.addStatement(returnStatement);
		return compound;
	}

	private static IASTDeclarator getGetterDeclarator(String varName,
			IASTSimpleDeclaration fieldDeclaration, ICPPASTQualifiedName name) {
		
		CPPASTName getterName = new CPPASTName();
		String varPartOfGetterName = NameHelper.makeFirstCharUpper(NameHelper.trimFieldName(varName));
		getterName.setName("get".concat(varPartOfGetterName).toCharArray()); //$NON-NLS-1$
		
		// copy declarator hierarchy
		IASTDeclarator topDeclarator = fieldDeclaration.getDeclarators()[0].copy();
		
		// find the innermost declarator in hierarchy
		IASTDeclarator innermost = topDeclarator;
		while (innermost.getNestedDeclarator() != null) {
			innermost = innermost.getNestedDeclarator();
		}
		
		// create a new innermost function declarator basing on the field declarator 
		CPPASTFunctionDeclarator functionDeclarator = new CPPASTFunctionDeclarator();
		functionDeclarator.setConst(true);
		if(name != null) {
			name.addName(getterName);
			functionDeclarator.setName(name);
		}else {
			functionDeclarator.setName(getterName);
		}
		for(IASTPointerOperator pointer : innermost.getPointerOperators()){
			functionDeclarator.addPointerOperator(pointer.copy());
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
	
	public static IASTFunctionDefinition createSetterDefinition(String varName, IASTSimpleDeclaration fieldDeclaration, ICPPASTQualifiedName name) {
		
		IASTFunctionDefinition setter = new CPPASTFunctionDefinition();
		
		setter.setDeclSpecifier(getVoidDeclSpec());		
		setter.setDeclarator(getSetterDeclarator(varName, fieldDeclaration, name));
		setter.setBody(getSetterBody(fieldDeclaration));
		
		return setter;
	}

	private static CPPASTCompoundStatement getSetterBody(IASTSimpleDeclaration fieldDeclaration) {
		CPPASTCompoundStatement compound = new CPPASTCompoundStatement();
		CPPASTExpressionStatement exprStmt = new CPPASTExpressionStatement();
		CPPASTBinaryExpression binExpr = new CPPASTBinaryExpression();
		CPPASTFieldReference fieldRef = new CPPASTFieldReference();
		CPPASTLiteralExpression litExpr = new CPPASTLiteralExpression();
		litExpr.setValue(Keywords.cTHIS); 
		fieldRef.setFieldOwner(litExpr);
		fieldRef.setFieldName(fieldDeclaration.getDeclarators()[0].getName().copy());
		fieldRef.setIsPointerDereference(true);
		binExpr.setOperand1(fieldRef);
		binExpr.setOperator(IASTBinaryExpression.op_assign);
		CPPASTIdExpression idExpr = new CPPASTIdExpression();
		idExpr.setName(fieldDeclaration.getDeclarators()[0].getName().copy());
		binExpr.setOperand2(idExpr);
		exprStmt.setExpression(binExpr);
		compound.addStatement(exprStmt);
		return compound;
	}

	private static CPPASTFunctionDeclarator getSetterDeclarator(String varName,
			IASTSimpleDeclaration fieldDeclaration, ICPPASTQualifiedName name) {
		CPPASTName setterName = new CPPASTName();
		String varPartOfSetterName = NameHelper.makeFirstCharUpper(NameHelper.trimFieldName(varName));
		setterName.setName("set".concat(varPartOfSetterName).toCharArray()); //$NON-NLS-1$
		CPPASTFunctionDeclarator declarator = new CPPASTFunctionDeclarator();
		if(name != null) {
			name.addName(setterName);
			declarator.setName(name);
		}else {
			declarator.setName(setterName);
		}
		CPPASTParameterDeclaration parameterDeclaration = new CPPASTParameterDeclaration();
		parameterDeclaration.setDeclarator(fieldDeclaration.getDeclarators()[0].copy());
		parameterDeclaration.setDeclSpecifier(fieldDeclaration.getDeclSpecifier().copy());
		declarator.addParameterDeclaration(parameterDeclaration.copy());
		return declarator;
	}

	private static CPPASTSimpleDeclSpecifier getVoidDeclSpec() {
		CPPASTSimpleDeclSpecifier declSpecifier = new CPPASTSimpleDeclSpecifier();
		declSpecifier.setType(IASTSimpleDeclSpecifier.t_void);
		return declSpecifier;
	}

	public static IASTSimpleDeclaration createGetterDeclaration(String name,
			IASTSimpleDeclaration fieldDeclaration) {
		IASTSimpleDeclaration getter = new CPPASTSimpleDeclaration();
		getter.setDeclSpecifier(fieldDeclaration.getDeclSpecifier().copy());	
		getter.addDeclarator(getGetterDeclarator(name, fieldDeclaration, null));
		
		return getter;
	}

	public static IASTSimpleDeclaration createSetterDeclaration(String name,
			IASTSimpleDeclaration fieldDeclaration) {
		IASTSimpleDeclaration setter = new CPPASTSimpleDeclaration();
		setter.setDeclSpecifier(getVoidDeclSpec());		
		setter.addDeclarator(getSetterDeclarator(name, fieldDeclaration, null));
		return setter;
	}

}
