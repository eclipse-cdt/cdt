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
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;

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
		IScope globalScope = tu.getScope();

		List declarations = tu.getDeclarations();

		// int x
		IASTSimpleDeclaration decl_x = (IASTSimpleDeclaration) declarations
				.get(0);
		IASTSimpleDeclSpecifier declspec_x = (IASTSimpleDeclSpecifier) decl_x
				.getDeclSpecifier();
		assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_x.getType());
		IASTDeclarator declor_x = (IASTDeclarator) decl_x.getDeclarators().get(
				0);
		IASTName name_x = declor_x.getName();
		assertEquals("x", name_x.toString());
		// resolve the binding to get the variable object
		IVariable var_x = (IVariable) name_x.resolveBinding();
		assertEquals(globalScope, var_x.getScope());

		// function - void f()
		IASTFunctionDefinition funcdef_f = (IASTFunctionDefinition) declarations
				.get(1);
		IASTSimpleDeclSpecifier declspec_f = (IASTSimpleDeclSpecifier) funcdef_f
				.getDeclSpecifier();
		assertEquals(IASTSimpleDeclSpecifier.t_void, declspec_f.getType());
		IASTFunctionDeclarator declor_f = (IASTFunctionDeclarator) funcdef_f
				.getDeclarator();
		IASTName name_f = declor_f.getName();
		assertEquals("f", name_f.toString());
		IFunction func_f = (IFunction) name_f.resolveBinding();
		assertEquals(globalScope, func_f.getScope());

		// parameter - int y
		IASTParameterDeclaration decl_y = (IASTParameterDeclaration) declor_f
				.getParameters().get(0);
		IASTSimpleDeclSpecifier declspec_y = (IASTSimpleDeclSpecifier) decl_y
				.getDeclSpecifier();
		assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_y.getType());
		IASTDeclarator declor_y = decl_y.getDeclarator();
		IASTName name_y = declor_y.getName();
		assertEquals("y", name_y.toString());
		IParameter var_y = (IParameter) name_y.resolveBinding();
		assertEquals(func_f, var_y.getScope());

		// int z
		IASTCompoundStatement body_f = (IASTCompoundStatement) funcdef_f
				.getBody();
		IASTDeclarationStatement declstmt_z = (IASTDeclarationStatement) body_f
				.getStatements().get(0);
		IASTSimpleDeclaration decl_z = (IASTSimpleDeclaration) declstmt_z
				.getDeclaration();
		IASTSimpleDeclSpecifier declspec_z = (IASTSimpleDeclSpecifier) decl_z
				.getDeclSpecifier();
		assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_z.getType());
		IASTDeclarator declor_z = (IASTDeclarator) decl_z.getDeclarators().get(
				0);
		IASTName name_z = declor_z.getName();
		assertEquals("z", name_z.toString());
		IVariable var_z = (IVariable) name_z.resolveBinding();
		assertEquals(func_f, var_z.getScope());

		// = x + y
		IASTBinaryExpression init_z = (IASTBinaryExpression) declor_z
				.getInitializer();
		assertEquals(IASTBinaryExpression.op_plus, init_z.getOperator());
		IASTIdExpression ref_x = (IASTIdExpression) init_z.getOperand1();
		IASTName name_ref_x = ref_x.getName();
		assertEquals("x", name_ref_x.toString());
		// make sure the variable referenced is the same one we declared above
		assertEquals(var_x, (IVariable) name_ref_x.resolveBinding());

		IASTIdExpression ref_y = (IASTIdExpression) init_z.getOperand2();
		IASTName name_ref_y = ref_y.getName();
		assertEquals("y", name_ref_y.toString());
		assertEquals(var_y, (IVariable) name_ref_y.resolveBinding());
	}

	public void testSimpleStruct() {
		StringBuffer buff = new StringBuffer();
		buff.append("typedef struct {\n");
		buff.append("    int x;\n");
		buff.append("} S;\n");

		buff.append("void f() {\n");
		buff.append("    S myS;\n");
		buff.append("    myS.x = 5;");
		buff.append("}");

		IASTTranslationUnit tu = ASTFactory.parseString(buff);
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration)tu.getDeclarations().get(0);
		IASTCompositeTypeSpecifier type = (IASTCompositeTypeSpecifier)decl.getDeclSpecifier();
		
		// it's a typedef
		assertEquals(IASTSimpleDeclSpecifier.sc_typedef, type.getStorageClass());
		// this an anonymous struct
		IASTName name_struct = type.getName();
		assertNull("", name_struct.toString());
		ICompositeType type_struct = (ICompositeType) name_struct
				.resolveBinding();
		// member - x
		IASTSimpleDeclaration decl_x = (IASTSimpleDeclaration) type
				.getMembers().get(0);
		IASTSimpleDeclSpecifier spec_x = (IASTSimpleDeclSpecifier) decl_x
				.getDeclSpecifier();
		// it's an int
		assertEquals(IASTSimpleDeclSpecifier.t_int, spec_x.getType());
		IASTFieldDeclarator tor_x = (IASTFieldDeclarator) decl_x
				.getDeclarators().get(0);
		IASTName name_x = tor_x.getName();
		assertEquals("x", name_x.toString());
		IField field_x = (IField)name_x.resolveBinding();
		// declarator S
		IASTDeclarator tor_S = (IASTDeclarator) decl.getDeclarators().get(0);
		IASTName name_S = tor_S.getName();
		assertEquals("S", name_S.toString());
		ITypedef typedef_S = (ITypedef) name_S.resolveBinding();
		// make sure the typedef is hooked up correctly
		assertEquals(type_struct, typedef_S.getType());

		// function f
		IASTFunctionDefinition def_f = (IASTFunctionDefinition) tu
				.getDeclarations().get(1);
		// f's body
		IASTCompoundStatement body_f = (IASTCompoundStatement) def_f.getBody();
		// the declaration statement for myS
		IASTDeclarationStatement declstmt_myS = (IASTDeclarationStatement)body_f.getStatements().get(0);
		// the declaration for myS
		IASTSimpleDeclaration decl_myS = (IASTSimpleDeclaration)declstmt_myS.getDeclaration();
		// the type specifier for myS
		IASTCompositeTypeSpecifier type_spec_myS = (IASTCompositeTypeSpecifier)decl_myS.getDeclSpecifier();
		// the type name for myS
		IASTName name_type_myS = type_spec_myS.getName();
		// the type for myS
		ICompositeType type_myS = (ICompositeType)name_type_myS.resolveBinding();
		// this should be type typedef of S as seen above
		assertEquals(typedef_S, type_myS);
		// the declarator for myS
		IASTDeclarator tor_myS = (IASTDeclarator)decl_myS.getDeclarators().get(0);
		// the name for myS
		IASTName name_myS = tor_myS.getName();
		// the variable myS
		IVariable var_myS = (IVariable)name_myS.resolveBinding();
		// the assignment expression statement
		IASTExpressionStatement exprstmt = (IASTExpressionStatement)body_f.getStatements().get(1);
		// the assignment expression
		IASTBinaryExpression assexpr = (IASTBinaryExpression)exprstmt.getExpression();
		// the field reference to myS.x
		IASTFieldReference fieldref = (IASTFieldReference)assexpr.getOperand1();
		// the reference to myS
		IASTIdExpression ref_myS = (IASTIdExpression)fieldref.getFieldOwner();
		// make sure this is our variable
		assertEquals(var_myS, ref_myS.getName().resolveBinding());
		// make sure the field is correct
		assertEquals(field_x, fieldref.getFieldName().resolveBinding());
		// while we're at it make sure the literal is correct
		IASTLiteralExpression lit_5 = (IASTLiteralExpression)assexpr.getOperand2();
		assertEquals("5", lit_5.toString());
	}
}
