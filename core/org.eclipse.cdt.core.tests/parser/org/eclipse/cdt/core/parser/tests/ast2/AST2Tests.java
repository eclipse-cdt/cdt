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

import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ast.ASTFactory;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaratorId;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;

/**
 * Test the new AST.
 * 
 * @author Doug Schaefer
 */
public class AST2Tests extends TestCase {

	public void testBasicFunction() {
		StringBuffer buff = new StringBuffer();
		buff.append("int x;\n");
		buff.append("void f(int y) {\n");
		buff.append("   int z = x + y;\n");
		buff.append("}\n");
		
		IASTTranslationUnit tu = ASTFactory.parseString(buff);
		List declarations = tu.getDeclarations();
		// int x
		IASTSimpleDeclaration decl_x = (IASTSimpleDeclaration)declarations.get(0);
		IASTSimpleDeclSpecifier declspec_x = (IASTSimpleDeclSpecifier)decl_x.getDeclSpecifier();
		assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_x.getType());
		IASTDeclarator declor_x = (IASTDeclarator)decl_x.getDeclarators().get(0);
		IASTSimpleDeclaratorId declid_x = (IASTSimpleDeclaratorId)declor_x.getDeclaratorId();
		assertEquals("x", declid_x.getName());
		// function - void f()
		IASTFunctionDefinition funcdef_f = (IASTFunctionDefinition)declarations.get(0);
		IASTSimpleDeclSpecifier declspec_f = (IASTSimpleDeclSpecifier)funcdef_f.getDeclSpecifier();
		assertEquals(IASTSimpleDeclSpecifier.t_void, declspec_f.getType());
		IASTFunctionDeclarator declor_f = (IASTFunctionDeclarator)funcdef_f.getDeclarator();
		IASTSimpleDeclaratorId declid_f = (IASTSimpleDeclaratorId)declor_f.getDeclaratorId();
		assertEquals("f", declid_f.getName());
		// parameter - int y
		IASTParameterDeclaration decl_y = (IASTParameterDeclaration)declor_f.getParameters().get(0);
		IASTSimpleDeclSpecifier declspec_y = (IASTSimpleDeclSpecifier)decl_y.getDeclSpecifier();
		assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_y.getType());
		IASTDeclarator declor_y = decl_y.getDeclarator();
		IASTSimpleDeclaratorId declid_y = (IASTSimpleDeclaratorId)declor_y.getDeclaratorId();
		assertEquals("y", declid_y.getName());
		// int z
		IASTCompoundStatement body_f = (IASTCompoundStatement)funcdef_f.getBody();
		IASTDeclarationStatement declstmt_z = (IASTDeclarationStatement)body_f.getStatements().get(0);
		IASTSimpleDeclaration decl_z = (IASTSimpleDeclaration)declstmt_z.getDeclaration();
		IASTSimpleDeclSpecifier declspec_z = (IASTSimpleDeclSpecifier)decl_z.getDeclSpecifier();
		assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_z.getType());
		IASTDeclarator declor_z = (IASTDeclarator)decl_z.getDeclarators().get(0);
		IASTSimpleDeclaratorId declid_z = (IASTSimpleDeclaratorId)declor_z.getDeclaratorId();
		assertEquals("z", declid_x.getName());
		// = x + y
		IASTBinaryExpression init_z = (IASTBinaryExpression)declor_z.getInitializer();
		assertEquals(IASTBinaryExpression.op_plus, init_z.getOperator());
		IASTIdExpression ref_x = (IASTIdExpression)init_z.getOperand1();
		assertEquals("x", ref_x.getName());
		IASTIdExpression ref_y = (IASTIdExpression)init_z.getOperand2();
		assertEquals("y", ref_y.getName());
	}
}
