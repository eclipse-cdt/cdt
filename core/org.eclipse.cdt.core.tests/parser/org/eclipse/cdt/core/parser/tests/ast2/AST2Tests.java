/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.ast2.ASTFactory;
import org.eclipse.cdt.core.parser.ast2.IASTCompoundStatement;
import org.eclipse.cdt.core.parser.ast2.IASTDeclarationStatement;
import org.eclipse.cdt.core.parser.ast2.IASTExpression;
import org.eclipse.cdt.core.parser.ast2.IASTFunction;
import org.eclipse.cdt.core.parser.ast2.IASTFunctionDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTParameter;
import org.eclipse.cdt.core.parser.ast2.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ast2.IASTType;
import org.eclipse.cdt.core.parser.ast2.IASTTypeDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTVariable;
import org.eclipse.cdt.core.parser.ast2.IASTVariableDeclaration;
import org.eclipse.cdt.core.parser.ast2.IASTVariableReference;

/**
 * Test the new AST.
 * 
 * @author Doug Schaefer
 */
public class AST2Tests extends TestCase {

	private IASTTranslationUnit parse(StringBuffer code) {
		return ASTFactory.parseString(code.toString(), ScannerConfigFactory.getScannerInfo());
	}
	
	public void testBasicFunction() {
		StringBuffer buff = new StringBuffer();
		buff.append("int x;");
		buff.append("void f(int y) {");
		buff.append("   int z = x + y;");
		buff.append("}");
		
		IASTTranslationUnit tu = parse(buff);
		// Built-in for int
		IASTTypeDeclaration typeDecl_int = (IASTTypeDeclaration)tu.getMemberDeclaration(0);
		assertTrue(typeDecl_int.getName().equals("int"));
		IASTType type_int = typeDecl_int.getType();
		assertEquals(typeDecl_int, type_int.getDeclaration(0));
		// Built-in for void
		IASTTypeDeclaration typeDecl_void = (IASTTypeDeclaration)tu.getMemberDeclaration(1);
		assertTrue(typeDecl_void.getName().equals("void"));
		IASTType type_void = typeDecl_void.getType();
		assertEquals(typeDecl_void, type_void.getDeclaration(0));
		// int x;
		IASTVariableDeclaration varDecl_x = (IASTVariableDeclaration)tu.getMemberDeclaration(2);
		assertTrue(varDecl_x.getName().equals("x"));
		assertEquals(type_int, varDecl_x.getType());
		IASTVariable var_x = varDecl_x.getVariable();
		assertEquals(varDecl_x, var_x.getDeclaration(0));
		// function void f();
		IASTFunctionDeclaration funcDecl_f = (IASTFunctionDeclaration)tu.getMemberDeclaration(3);
		assertTrue(varDecl_x.getName().equals("f"));
		assertEquals(type_void, funcDecl_f.getReturnType());
		IASTFunction func_f = funcDecl_f.getFunction();
		assertEquals(funcDecl_f, func_f.getDeclaration(0));
		// parameter int y;
		IASTParameterDeclaration paramDecl_y = (IASTParameterDeclaration)funcDecl_f.getParameters()[0];
		assertTrue(paramDecl_y.getName().equals("y"));
		assertEquals(type_int, paramDecl_y.getType());
		IASTParameter param_y = (IASTParameter)paramDecl_y.getVariable();
		assertEquals(paramDecl_y, param_y.getDeclaration(0));
		// function body
		IASTCompoundStatement body = (IASTCompoundStatement)func_f.getBody();
		IASTDeclarationStatement decl_stmt = (IASTDeclarationStatement)body.getStatement(0);
		IASTVariableDeclaration varDecl_z = (IASTVariableDeclaration)decl_stmt.getDeclaration();
		assertTrue(varDecl_z.getName().equals("z"));
		assertEquals(type_int, varDecl_z.getType());
		IASTVariable var_z = varDecl_z.getVariable();
		assertEquals(varDecl_z, var_z.getDeclaration(0));
		IASTExpression init_expr = var_z.getInitialization();
		assertTrue(init_expr.getOperator().equals("+"));
		IASTVariableReference varRef_x = (IASTVariableReference)init_expr.getOperands(0);
		assertEquals(var_x, varRef_x.getVariable());
		assertEquals(varRef_x, var_x.getReference(0));
		IASTVariableReference varRef_y = (IASTVariableReference)init_expr.getOperands(1);
		assertEquals(param_y, varRef_y.getVariable());
		assertEquals(varRef_y, param_y.getReference(0));
	}
}
