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

import org.eclipse.cdt.core.parser.ast2.c.CASTFactory;
import org.eclipse.cdt.core.parser.ast2.c.ICASTBinaryOperation;
import org.eclipse.cdt.core.parser.ast2.c.ICASTCompoundStatement;
import org.eclipse.cdt.core.parser.ast2.c.ICASTDeclarationStatement;
import org.eclipse.cdt.core.parser.ast2.c.ICASTDeclarator;
import org.eclipse.cdt.core.parser.ast2.c.ICASTFunctionDeclarator;
import org.eclipse.cdt.core.parser.ast2.c.ICASTFunctionDefinition;
import org.eclipse.cdt.core.parser.ast2.c.ICASTIdExpression;
import org.eclipse.cdt.core.parser.ast2.c.ICASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast2.c.ICASTSimpleDeclaration;
import org.eclipse.cdt.core.parser.ast2.c.ICASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast2.c.ICASTTranslationUnit;

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
		
		ICASTTranslationUnit tu = CASTFactory.parseString(buff);
		List declarations = tu.getDeclarations();
		// int x;
		ICASTSimpleDeclaration decl_x = (ICASTSimpleDeclaration)declarations.get(0);
		ICASTSimpleTypeSpecifier type_x
			= (ICASTSimpleTypeSpecifier)decl_x.getDeclSpecifiers().getTypeSpecifiers();
		assertEquals(ICASTSimpleTypeSpecifier.t_int, type_x.getType());
		ICASTDeclarator dclr_x = (ICASTDeclarator)decl_x.getDeclarators().get(0);
		assertEquals("x", dclr_x.getName().toString());
		// void f(...)
		ICASTFunctionDefinition def_f = (ICASTFunctionDefinition)declarations.get(1);
		ICASTSimpleTypeSpecifier ret_f
			= (ICASTSimpleTypeSpecifier)def_f.getReturnDeclSpecifiers().getTypeSpecifiers();
		assertEquals(ICASTSimpleTypeSpecifier.t_void, ret_f.getType());
		ICASTFunctionDeclarator dclr_f = (ICASTFunctionDeclarator)def_f.getDeclarator();
		assertEquals("f", dclr_f.getName().toString());
		// parameter - int y
		ICASTParameterDeclaration decl_y = (ICASTParameterDeclaration)dclr_f.getParameters().get(0);
		ICASTSimpleTypeSpecifier type_y
			= (ICASTSimpleTypeSpecifier)decl_y.getDeclSpecifiers().getTypeSpecifiers().get(0);
		assertEquals(ICASTSimpleTypeSpecifier.t_int, type_y.getType());
		ICASTDeclarator dclr_y = decl_y.getDeclarator();
		assertEquals("y", dclr_y.getName().toString());
		// int z
		ICASTCompoundStatement body_f = (ICASTCompoundStatement)def_f.getBody();
		ICASTDeclarationStatement dstmt_z = (ICASTDeclarationStatement)body_f.getStatements().get(0);
		ICASTSimpleDeclaration decl_z = (ICASTSimpleDeclaration)dstmt_z.getDeclaration();
		ICASTSimpleTypeSpecifier type_z
			= (ICASTSimpleTypeSpecifier)decl_z.getDeclSpecifiers().getTypeSpecifiers().get(0);
		assertEquals(ICASTSimpleTypeSpecifier.t_int, type_z.getType());
		ICASTDeclarator dclr_z = (ICASTDeclarator)decl_z.getDeclarators().get(0);
		assertEquals("z", dclr_z.getName().toString());
		// = x + y
		ICASTBinaryOperation z_plus = (ICASTBinaryOperation)dclr_z.getInitializer();
		ICASTIdExpression z_ref_x = (ICASTIdExpression)z_plus.getOperand1();
		assertEquals("x", z_ref_x.getName().toString());
		assertEquals(dclr_x, z_ref_x.getDeclarator());
		ICASTIdExpression z_ref_y = (ICASTIdExpression)z_plus.getOperand2();
		assertEquals("y", z_ref_y.getName().toString());
		assertEquals(dclr_y, z_ref_y.getDeclarator());
	}
}
