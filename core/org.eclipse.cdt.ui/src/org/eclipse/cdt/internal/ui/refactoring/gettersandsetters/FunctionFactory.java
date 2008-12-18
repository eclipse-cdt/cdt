/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik  
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
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPointerOperator;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
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

import org.eclipse.cdt.internal.ui.refactoring.utils.NameHelper;

public class FunctionFactory {

	public static IASTFunctionDefinition createGetter(String varName, IASTSimpleDeclaration fieldDeclaration) {
		
		IASTFunctionDefinition getter = new CPPASTFunctionDefinition();
		
		getter.setDeclSpecifier(fieldDeclaration.getDeclSpecifier().copy());
				
		CPPASTName getterName = new CPPASTName();
		String varPartOfGetterName = NameHelper.makeFirstCharUpper(NameHelper.trimFieldName(varName));
		getterName.setName("get".concat(varPartOfGetterName).toCharArray()); //$NON-NLS-1$
		CPPASTFunctionDeclarator declarator = new CPPASTFunctionDeclarator();
		declarator.setConst(true);
		declarator.setName(getterName);
		for(IASTPointerOperator pointer : fieldDeclaration.getDeclarators()[0].getPointerOperators()){
			declarator.addPointerOperator(pointer);
		}
		getter.setDeclarator(declarator);
		
		CPPASTCompoundStatement compound = new CPPASTCompoundStatement();
		CPPASTReturnStatement returnStatement = new CPPASTReturnStatement();
		CPPASTIdExpression idExpr = new CPPASTIdExpression();
		CPPASTName returnVal = new CPPASTName();
		returnVal.setName(varName.toCharArray());
		idExpr.setName(returnVal);
		returnStatement.setReturnValue(idExpr);
		compound.addStatement(returnStatement);
		
		getter.setBody(compound);
		
		return getter;
	}
	
	public static IASTFunctionDefinition createSetter(String varName, IASTSimpleDeclaration fieldDeclaration) {
		
		IASTFunctionDefinition setter = new CPPASTFunctionDefinition();
		
		CPPASTSimpleDeclSpecifier declSpecifier = new CPPASTSimpleDeclSpecifier();
		declSpecifier.setType(IASTSimpleDeclSpecifier.t_void);
		setter.setDeclSpecifier(declSpecifier);
				
		CPPASTName setterName = new CPPASTName();
		String varPartOfSetterName = NameHelper.makeFirstCharUpper(NameHelper.trimFieldName(varName));
		setterName.setName("set".concat(varPartOfSetterName).toCharArray()); //$NON-NLS-1$
		CPPASTFunctionDeclarator declarator = new CPPASTFunctionDeclarator();
		declarator.setName(setterName);
		setter.setDeclarator(declarator);
		CPPASTParameterDeclaration parameterDeclaration = new CPPASTParameterDeclaration();
		parameterDeclaration.setDeclarator(fieldDeclaration.getDeclarators()[0].copy());
		parameterDeclaration.setDeclSpecifier(fieldDeclaration.getDeclSpecifier().copy());
		declarator.addParameterDeclaration(parameterDeclaration.copy());
		
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
		
		setter.setBody(compound);
		
		return setter;
	}

}
