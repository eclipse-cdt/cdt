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

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser2.c.CVisitor;

/**
 * Test the new AST.
 * 
 * @author Doug Schaefer
 */
public class AST2Tests extends AST2BaseTest {

	public void testBasicFunction() throws ParserException {
		StringBuffer buff = new StringBuffer();
		buff.append("int x;\n"); //$NON-NLS-1$
		buff.append("void f(int y) {\n"); //$NON-NLS-1$
		buff.append("   int z = x + y;\n"); //$NON-NLS-1$
		buff.append("}\n"); //$NON-NLS-1$

		IASTTranslationUnit tu = parse(buff.toString(), ParserLanguage.C );
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
		assertEquals("x", name_x.toString()); //$NON-NLS-1$

		// function - void f()
		IASTFunctionDefinition funcdef_f = (IASTFunctionDefinition) declarations
				.get(1);
		IASTSimpleDeclSpecifier declspec_f = (IASTSimpleDeclSpecifier) funcdef_f
				.getDeclSpecifier();
		assertEquals(IASTSimpleDeclSpecifier.t_void, declspec_f.getType());
		IASTFunctionDeclarator declor_f = funcdef_f
				.getDeclarator();
		IASTName name_f = declor_f.getName();
		assertEquals("f", name_f.toString()); //$NON-NLS-1$

		// parameter - int y
		IASTParameterDeclaration decl_y = (IASTParameterDeclaration) declor_f
				.getParameters().get(0);
		IASTSimpleDeclSpecifier declspec_y = (IASTSimpleDeclSpecifier) decl_y
				.getDeclSpecifier();
		assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_y.getType());
		IASTDeclarator declor_y = decl_y.getDeclarator();
		IASTName name_y = declor_y.getName();
		assertEquals("y", name_y.toString()); //$NON-NLS-1$

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
		assertEquals("z", name_z.toString()); //$NON-NLS-1$

		// = x + y
		IASTInitializerExpression initializer = (IASTInitializerExpression) declor_z.getInitializer();
		IASTBinaryExpression init_z = (IASTBinaryExpression) initializer.getExpression();
		assertEquals(IASTBinaryExpression.op_plus, init_z.getOperator());
		IASTIdExpression ref_x = (IASTIdExpression) init_z.getOperand1();
		IASTName name_ref_x = ref_x.getName();
		assertEquals("x", name_ref_x.toString()); //$NON-NLS-1$

		IASTIdExpression ref_y = (IASTIdExpression) init_z.getOperand2();
		IASTName name_ref_y = ref_y.getName();
		assertEquals("y", name_ref_y.toString()); //$NON-NLS-1$
		
		//BINDINGS
		// resolve the binding to get the variable object
		IVariable var_x = (IVariable) name_x.resolveBinding();
		assertEquals(globalScope, var_x.getScope());
		IFunction func_f = (IFunction) name_f.resolveBinding();
		assertEquals(globalScope, func_f.getScope());
		IParameter var_y = (IParameter) name_y.resolveBinding();
		assertEquals(func_f.getFunctionScope(), var_y.getScope());

		IVariable var_z = (IVariable) name_z.resolveBinding();
		assertEquals(func_f.getFunctionScope(), var_z.getScope());

		// make sure the variable referenced is the same one we declared above
		assertEquals(var_x, name_ref_x.resolveBinding());
		assertEquals(var_y, name_ref_y.resolveBinding());

	}

    public void testSimpleStruct() throws ParserException {
		StringBuffer buff = new StringBuffer();
		buff.append("typedef struct {\n"); //$NON-NLS-1$
		buff.append("    int x;\n"); //$NON-NLS-1$
		buff.append("} S;\n"); //$NON-NLS-1$

		buff.append("void f() {\n"); //$NON-NLS-1$
		buff.append("    S myS;\n"); //$NON-NLS-1$
		buff.append("    myS.x = 5;"); //$NON-NLS-1$
		buff.append("}"); //$NON-NLS-1$

		IASTTranslationUnit tu = parse(buff.toString(), ParserLanguage.C );
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration)tu.getDeclarations().get(0);
		IASTCompositeTypeSpecifier type = (IASTCompositeTypeSpecifier)decl.getDeclSpecifier();
		
		// it's a typedef
		assertEquals(IASTDeclSpecifier.sc_typedef, type.getStorageClass());
		// this an anonymous struct
		IASTName name_struct = type.getName();
		assertNull("", name_struct.toString()); //$NON-NLS-1$
		// member - x
		IASTSimpleDeclaration decl_x = (IASTSimpleDeclaration) type
				.getMembers().get(0);
		IASTSimpleDeclSpecifier spec_x = (IASTSimpleDeclSpecifier) decl_x
				.getDeclSpecifier();
		// it's an int
		assertEquals(IASTSimpleDeclSpecifier.t_int, spec_x.getType());
		IASTDeclarator tor_x = (IASTDeclarator) decl_x
				.getDeclarators().get(0);
		IASTName name_x = tor_x.getName();
		assertEquals("x", name_x.toString()); //$NON-NLS-1$

		// declarator S
		IASTDeclarator tor_S = (IASTDeclarator) decl.getDeclarators().get(0);
		IASTName name_S = tor_S.getName();
		assertEquals("S", name_S.toString()); //$NON-NLS-1$

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
		IASTNamedTypeSpecifier type_spec_myS = (IASTNamedTypeSpecifier)decl_myS.getDeclSpecifier();
		// the type name for myS
		IASTName name_type_myS = type_spec_myS.getName();
		// the declarator for myS
		IASTDeclarator tor_myS = (IASTDeclarator)decl_myS.getDeclarators().get(0);
		// the name for myS
		IASTName name_myS = tor_myS.getName();
		// the assignment expression statement
		IASTExpressionStatement exprstmt = (IASTExpressionStatement)body_f.getStatements().get(1);
		// the assignment expression
		IASTBinaryExpression assexpr = (IASTBinaryExpression)exprstmt.getExpression();
		// the field reference to myS.x
		IASTFieldReference fieldref = (IASTFieldReference)assexpr.getOperand1();
		// the reference to myS
		IASTIdExpression ref_myS = (IASTIdExpression)fieldref.getFieldOwner();
		IASTLiteralExpression lit_5 = (IASTLiteralExpression)assexpr.getOperand2();
		assertEquals("5", lit_5.toString()); //$NON-NLS-1$

		//Logical Bindings In Test
		ICompositeType type_struct = (ICompositeType) name_struct.resolveBinding();
		ITypedef typedef_S = (ITypedef) name_S.resolveBinding();
		// make sure the typedef is hooked up correctly
		assertEquals(type_struct, typedef_S.getType());
		// the typedef S for myS
		ITypedef typedef_myS = (ITypedef)name_type_myS.resolveBinding();
		assertEquals(typedef_S, typedef_myS);
		// get the real type for S which is our anonymous struct
		ICompositeType type_myS = (ICompositeType)typedef_myS.getType();
		assertEquals( type_myS, type_struct );
		// the variable myS
		IVariable var_myS = (IVariable)name_myS.resolveBinding();
		assertEquals(typedef_S, var_myS.getType());
		assertEquals(var_myS, ref_myS.getName().resolveBinding());
		IField field_x = (IField)name_x.resolveBinding();
		assertEquals(field_x, fieldref.getFieldName().resolveBinding());
	}
    
    public void testCExpressions() throws ParserException
    {
        validateSimpleUnaryExpressionC( "++x", IASTUnaryExpression.op_prefixIncr ); //$NON-NLS-1$
        validateSimpleUnaryExpressionC( "--x", IASTUnaryExpression.op_prefixDecr ); //$NON-NLS-1$
        validateSimpleUnaryExpressionC( "+x", IASTUnaryExpression.op_plus ); //$NON-NLS-1$
        validateSimpleUnaryExpressionC( "-x", IASTUnaryExpression.op_minus ); //$NON-NLS-1$
        validateSimpleUnaryExpressionC( "!x", IASTUnaryExpression.op_not ); //$NON-NLS-1$
        validateSimpleUnaryExpressionC( "~x", IASTUnaryExpression.op_tilde ); //$NON-NLS-1$
        validateSimpleUnaryExpressionC( "*x", IASTUnaryExpression.op_star ); //$NON-NLS-1$
        validateSimpleUnaryExpressionC( "&x", IASTUnaryExpression.op_amper ); //$NON-NLS-1$
        validateSimpleUnaryExpressionC( "sizeof x", IASTUnaryExpression.op_sizeof ); //$NON-NLS-1$
        validateSimpleTypeIdExpressionC( "sizeof( int )", IASTTypeIdExpression.op_sizeof ); //$NON-NLS-1$
        validateSimpleUnaryTypeIdExpression( "(int)x", IASTCastExpression.op_cast ); //$NON-NLS-1$
        validateSimplePostfixInitializerExpressionC( "(int) { 5 }"); //$NON-NLS-1$
        validateSimplePostfixInitializerExpressionC( "(int) { 5, }"); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x=y", IASTBinaryExpression.op_assign ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x*=y", IASTBinaryExpression.op_multiplyAssign ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x/=y", IASTBinaryExpression.op_divideAssign ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x%=y", IASTBinaryExpression.op_moduloAssign ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x+=y", IASTBinaryExpression.op_plusAssign); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x-=y", IASTBinaryExpression.op_minusAssign ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x<<=y", IASTBinaryExpression.op_shiftLeftAssign); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x>>=y", IASTBinaryExpression.op_shiftRightAssign ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x&=y", IASTBinaryExpression.op_binaryAndAssign ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x^=y", IASTBinaryExpression.op_binaryXorAssign ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x|=y", IASTBinaryExpression.op_binaryOrAssign ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x-y", IASTBinaryExpression.op_minus ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x+y", IASTBinaryExpression.op_plus ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x/y", IASTBinaryExpression.op_divide ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x*y", IASTBinaryExpression.op_multiply); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x%y", IASTBinaryExpression.op_modulo ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x<<y", IASTBinaryExpression.op_shiftLeft ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x>>y", IASTBinaryExpression.op_shiftRight ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x<y", IASTBinaryExpression.op_lessThan ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x>y", IASTBinaryExpression.op_greaterThan); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x<=y", IASTBinaryExpression.op_lessEqual ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x>=y", IASTBinaryExpression.op_greaterEqual ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x==y", IASTBinaryExpression.op_equals ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x!=y", IASTBinaryExpression.op_notequals ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x&y", IASTBinaryExpression.op_binaryAnd ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x^y", IASTBinaryExpression.op_binaryXor ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x|y", IASTBinaryExpression.op_binaryOr ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x&&y", IASTBinaryExpression.op_logicalAnd ); //$NON-NLS-1$
        validateSimpleBinaryExpressionC("x||y", IASTBinaryExpression.op_logicalOr ); //$NON-NLS-1$
        validateConditionalExpressionC( "x ? y : x" ); //$NON-NLS-1$
    }
    
    public void testMultipleDeclarators() throws Exception {
		IASTTranslationUnit tu = parse( "int r, s;" , ParserLanguage.C ); //$NON-NLS-1$
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration)tu.getDeclarations().get(0);
		List declarators = decl.getDeclarators();
		assertEquals( 2, declarators.size() );
		
		IASTDeclarator dtor1 = (IASTDeclarator) declarators.get(0);
		IASTDeclarator dtor2 = (IASTDeclarator) declarators.get(1);
		
		IASTName name1 = dtor1.getName();
		IASTName name2 = dtor2.getName();
		
		assertEquals( name1.resolveBinding().getName(), "r" ); //$NON-NLS-1$
		assertEquals( name2.resolveBinding().getName(), "s" ); //$NON-NLS-1$
    }
    
    public void testStructureTagScoping_1() throws Exception{
    	StringBuffer buffer = new StringBuffer();
    	buffer.append( "struct A;             \n" ); //$NON-NLS-1$
    	buffer.append( "void f(){             \n" ); //$NON-NLS-1$
    	buffer.append( "   struct A;          \n" ); //$NON-NLS-1$
    	buffer.append( "   struct A * a;      \n" ); //$NON-NLS-1$
    	buffer.append( "}                     \n" ); //$NON-NLS-1$
    	
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
    	
    	//struct A;
    	IASTSimpleDeclaration decl1 = (IASTSimpleDeclaration) tu.getDeclarations().get(0);
    	IASTElaboratedTypeSpecifier compTypeSpec = (IASTElaboratedTypeSpecifier) decl1.getDeclSpecifier();
    	assertEquals( 0, decl1.getDeclarators().size() );
    	IASTName nameA1 = compTypeSpec.getName();
    	
    	//void f() {
    	IASTFunctionDefinition fndef = (IASTFunctionDefinition) tu.getDeclarations().get(1);
    	IASTCompoundStatement compoundStatement = (IASTCompoundStatement) fndef.getBody();
    	assertEquals( 2, compoundStatement.getStatements().size() );
    	
    	//   struct A;
    	IASTDeclarationStatement declStatement = (IASTDeclarationStatement) compoundStatement.getStatements().get( 0 );
    	IASTSimpleDeclaration decl2 = (IASTSimpleDeclaration) declStatement.getDeclaration();
    	compTypeSpec = (IASTElaboratedTypeSpecifier) decl2.getDeclSpecifier();
    	assertEquals( 0, decl2.getDeclarators().size() );
    	IASTName nameA2 = compTypeSpec.getName();
    	
    	//   struct A * a;
    	declStatement = (IASTDeclarationStatement) compoundStatement.getStatements().get(1);
    	IASTSimpleDeclaration decl3 = (IASTSimpleDeclaration) declStatement.getDeclaration();
    	compTypeSpec = (IASTElaboratedTypeSpecifier) decl3.getDeclSpecifier();
    	IASTName nameA3 = compTypeSpec.getName();
    	IASTDeclarator dtor = (IASTDeclarator) decl3.getDeclarators().get(0);
    	IASTName namea = dtor.getName();
    	assertEquals( 1, dtor.getPointerOperators().size() );
    	assertTrue( dtor.getPointerOperators().get(0) instanceof ICASTPointer );

    	//bindings
    	ICompositeType str1 = (ICompositeType) nameA1.resolveBinding();
    	ICompositeType str2 = (ICompositeType) nameA2.resolveBinding();
    	IVariable var = (IVariable) namea.resolveBinding();
    	ICompositeType str3 = (ICompositeType) var.getType();
    	ICompositeType str4 = (ICompositeType) nameA3.resolveBinding();
    	assertNotNull( str1 );
    	assertNotNull( str2 );
    	assertNotSame( str1, str2 );
    	assertSame( str2, str3 );
    	assertSame( str3, str4 );
    }
    
    public void testStructureTagScoping_2() throws Exception{
    	StringBuffer buffer = new StringBuffer();
    	buffer.append( "struct A;             \n" ); //$NON-NLS-1$
    	buffer.append( "void f(){             \n" ); //$NON-NLS-1$
    	buffer.append( "   struct A * a;      \n" ); //$NON-NLS-1$
    	buffer.append( "}                     \r\n" ); //$NON-NLS-1$

    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
    	
    	//struct A;
    	IASTSimpleDeclaration decl1 = (IASTSimpleDeclaration) tu.getDeclarations().get(0);
    	IASTElaboratedTypeSpecifier compTypeSpec = (IASTElaboratedTypeSpecifier) decl1.getDeclSpecifier();
    	assertEquals( 0, decl1.getDeclarators().size() );
    	IASTName nameA1 = compTypeSpec.getName();
    	
    	//void f() {
    	IASTFunctionDefinition fndef = (IASTFunctionDefinition) tu.getDeclarations().get(1);
    	IASTCompoundStatement compoundStatement = (IASTCompoundStatement) fndef.getBody();
    	assertEquals( 1, compoundStatement.getStatements().size() );
    	  	
    	//   struct A * a;
    	IASTDeclarationStatement declStatement = (IASTDeclarationStatement) compoundStatement.getStatements().get(0);
    	IASTSimpleDeclaration decl2 = (IASTSimpleDeclaration) declStatement.getDeclaration();
    	compTypeSpec = (IASTElaboratedTypeSpecifier) decl2.getDeclSpecifier();
    	IASTName nameA2 = compTypeSpec.getName();  	
    	IASTDeclarator dtor = (IASTDeclarator) decl2.getDeclarators().get(0);
    	IASTName namea = dtor.getName();
    	assertEquals( 1, dtor.getPointerOperators().size() );
    	assertTrue( dtor.getPointerOperators().get(0) instanceof ICASTPointer );

    	//bindings
    	ICompositeType str1 = (ICompositeType) nameA1.resolveBinding();
    	ICompositeType str2 = (ICompositeType) nameA2.resolveBinding();
    	IVariable var = (IVariable) namea.resolveBinding();
    	ICompositeType str3 = (ICompositeType) var.getType();
    	assertNotNull( str1 );
    	assertSame( str1, str2 );
    	assertSame( str2, str3 );
    }
    
    public void testStructureDef() throws Exception{
    	StringBuffer buffer = new StringBuffer();
    	buffer.append( "struct A;                \r\n"); //$NON-NLS-1$
    	buffer.append( "struct A * a;            \n"); //$NON-NLS-1$
    	buffer.append( "struct A { int i; };     \n"); //$NON-NLS-1$
    	buffer.append( "void f() {               \n"); //$NON-NLS-1$
    	buffer.append( "   a->i;                 \n"); //$NON-NLS-1$
    	buffer.append( "}                        \n"); //$NON-NLS-1$
    	
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
    	
    	//struct A;
    	IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations().get(0);
    	IASTElaboratedTypeSpecifier elabTypeSpec = (IASTElaboratedTypeSpecifier) decl.getDeclSpecifier();
    	assertEquals( 0, decl.getDeclarators().size() );
    	IASTName name_A1 = elabTypeSpec.getName();
    	
    	//struct A * a;
    	decl = (IASTSimpleDeclaration) tu.getDeclarations().get(1);
    	elabTypeSpec = (IASTElaboratedTypeSpecifier) decl.getDeclSpecifier();
    	IASTName name_A2 = elabTypeSpec.getName();  	
    	IASTDeclarator dtor = (IASTDeclarator) decl.getDeclarators().get(0);
    	IASTName name_a = dtor.getName();
    	assertEquals( 1, dtor.getPointerOperators().size() );
    	assertTrue( dtor.getPointerOperators().get(0) instanceof ICASTPointer );
    	
    	//struct A {
    	decl = (IASTSimpleDeclaration) tu.getDeclarations().get(2);
    	ICASTCompositeTypeSpecifier compTypeSpec = (ICASTCompositeTypeSpecifier) decl.getDeclSpecifier();
    	IASTName name_Adef = compTypeSpec.getName();
    	
    	//   int i;
    	decl = (IASTSimpleDeclaration) compTypeSpec.getMembers().get(0);
    	dtor = (IASTDeclarator) decl.getDeclarators().get(0);
    	IASTName name_i = dtor.getName();
    
    	//void f() {
    	IASTFunctionDefinition fndef = (IASTFunctionDefinition) tu.getDeclarations().get(3);
    	IASTCompoundStatement compoundStatement = (IASTCompoundStatement) fndef.getBody();
    	assertEquals( 1, compoundStatement.getStatements().size() );
    	
    	//   a->i;
		IASTExpressionStatement exprstmt = (IASTExpressionStatement)compoundStatement.getStatements().get(0);
		IASTFieldReference fieldref = (IASTFieldReference)exprstmt.getExpression();
		IASTIdExpression id_a = (IASTIdExpression) fieldref.getFieldOwner();
		IASTName name_aref = id_a.getName();
		IASTName name_iref = fieldref.getFieldName();

		//bindings
		IVariable var_a1 = (IVariable) name_aref.resolveBinding();
		IVariable var_i1 = (IVariable) name_iref.resolveBinding();
		ICompositeType structA_1 = (ICompositeType) var_a1.getType();
		ICompositeType structA_2 = (ICompositeType) name_A1.resolveBinding();
		ICompositeType structA_3 = (ICompositeType) name_A2.resolveBinding();
		ICompositeType structA_4 = (ICompositeType) name_Adef.resolveBinding();
		
		IVariable var_a2 = (IVariable) name_a.resolveBinding();
		IVariable var_i2 = (IVariable) name_i.resolveBinding();
		
		assertSame( var_a1, var_a2 );
		assertSame( var_i1, var_i2 );
		assertSame( structA_1, structA_2 );
		assertSame( structA_2, structA_3 );
		assertSame( structA_3, structA_4 );
    }
    
    public void testStructureNamespace() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "struct x {};        \n" ); //$NON-NLS-1$
        buffer.append( "void f( int x ) {   \n" ); //$NON-NLS-1$
        buffer.append( "   struct x i;      \n" ); //$NON-NLS-1$
        buffer.append( "}                   \n" ); //$NON-NLS-1$

        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        
        IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) tu.getDeclarations().get(0);
        IASTCompositeTypeSpecifier typeSpec = (IASTCompositeTypeSpecifier) declaration.getDeclSpecifier();
        IASTName x_1 = typeSpec.getName();
        
        IASTFunctionDefinition fdef = (IASTFunctionDefinition) tu.getDeclarations().get(1);
        IASTParameterDeclaration param = (IASTParameterDeclaration) fdef.getDeclarator().getParameters().get(0);
        IASTName x_2 = param.getDeclarator().getName();
        
        IASTCompoundStatement compound = (IASTCompoundStatement) fdef.getBody();
        IASTDeclarationStatement declStatement = (IASTDeclarationStatement) compound.getStatements().get(0);
        declaration = (IASTSimpleDeclaration) declStatement.getDeclaration();
        IASTElaboratedTypeSpecifier elab = (IASTElaboratedTypeSpecifier) declaration.getDeclSpecifier();
        IASTName x_3 = elab.getName();
        
        ICompositeType x1 = (ICompositeType) x_1.resolveBinding();
        IVariable x2 = (IVariable) x_2.resolveBinding();
        ICompositeType x3 = (ICompositeType) x_3.resolveBinding();
        
        assertNotNull( x1 );
        assertNotNull( x2 );
        assertSame( x1, x3 );
        assertNotSame( x2, x3 );
        
    }
    public void testFunctionParameters() throws Exception {
    	StringBuffer buffer  = new StringBuffer();
    	buffer.append( "void f( int a );        \n"); //$NON-NLS-1$
    	buffer.append( "void f( int b ){        \n"); //$NON-NLS-1$
    	buffer.append( "   b;                   \n"); //$NON-NLS-1$
    	buffer.append( "}                       \n"); //$NON-NLS-1$
    	
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
    	
    	//void f(
    	IASTSimpleDeclaration f_decl = (IASTSimpleDeclaration) tu.getDeclarations().get(0);
    	IASTFunctionDeclarator dtor = (IASTFunctionDeclarator) f_decl.getDeclarators().get(0);
    	IASTName f_name1 = dtor.getName();
    	//        int a );
    	IASTParameterDeclaration param = (IASTParameterDeclaration) dtor.getParameters().get(0);
    	IASTDeclarator paramDtor = param.getDeclarator();
    	IASTName name_param1 = paramDtor.getName();
    	
    	//void f( 
    	IASTFunctionDefinition f_defn = (IASTFunctionDefinition) tu.getDeclarations().get(1);
    	dtor = f_defn.getDeclarator();
    	IASTName f_name2 = dtor.getName();
    	//        int b );
    	param = (IASTParameterDeclaration) dtor.getParameters().get(0);
    	paramDtor = param.getDeclarator();
    	IASTName name_param2 = paramDtor.getName();
    	
    	//   b;
    	IASTCompoundStatement compound = (IASTCompoundStatement) f_defn.getBody();
    	IASTExpressionStatement expStatement = (IASTExpressionStatement) compound.getStatements().get(0);
    	IASTIdExpression idexp = (IASTIdExpression) expStatement.getExpression();
    	IASTName name_param3 = idexp.getName();
    	
    	//bindings
    	IParameter param_1 = (IParameter) name_param3.resolveBinding();
    	IParameter param_2 = (IParameter) name_param2.resolveBinding();
    	IParameter param_3 = (IParameter) name_param1.resolveBinding();
    	IFunction  f_1 = (IFunction) f_name1.resolveBinding();
    	IFunction  f_2 = (IFunction) f_name2.resolveBinding();
    	
    	assertNotNull( param_1 );
    	assertNotNull( f_1 );
    	assertSame( param_1, param_2 );
    	assertSame( param_2, param_3 );
    	assertSame( f_1, f_2 );
    	
    	CVisitor.clearBindings( tu );
    	param_1 = (IParameter) name_param1.resolveBinding();
    	param_2 = (IParameter) name_param3.resolveBinding();
    	param_3 = (IParameter) name_param2.resolveBinding();
    	f_1 = (IFunction) f_name2.resolveBinding();
    	f_2 = (IFunction) f_name1.resolveBinding();
    	assertNotNull( param_1 );
    	assertNotNull( f_1 );
    	assertSame( param_1, param_2 );
    	assertSame( param_2, param_3 );
    	assertSame( f_1, f_2 );
    }
    
    public void testSimpleFunction() throws Exception {
    	StringBuffer buffer = new StringBuffer( "void f( int a, int b ) { }  \n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
    	
    	IASTFunctionDefinition fDef = (IASTFunctionDefinition) tu.getDeclarations().get(0);
    	IASTFunctionDeclarator fDtor = fDef.getDeclarator();
    	IASTName fName = fDtor.getName();
    	
    	IASTParameterDeclaration a = (IASTParameterDeclaration) fDtor.getParameters().get( 0 );
    	IASTName name_a = a.getDeclarator().getName();
    	
    	IASTParameterDeclaration b = (IASTParameterDeclaration) fDtor.getParameters().get( 1 );
    	IASTName name_b = b.getDeclarator().getName();
    	
    	IFunction function = (IFunction) fName.resolveBinding();
    	IParameter param_a = (IParameter) name_a.resolveBinding();
    	IParameter param_b = (IParameter) name_b.resolveBinding();
    	
    	assertEquals( "f", function.getName() ); //$NON-NLS-1$
    	assertEquals( "a", param_a.getName() ); //$NON-NLS-1$
    	assertEquals( "b", param_b.getName() ); //$NON-NLS-1$
    	
    	List params = function.getParameters();
    	assertEquals( 2, params.size() );
    	assertSame( params.get(0), param_a );
    	assertSame( params.get(1), param_b );
    }
    
    public void testSimpleFunctionCall() throws Exception {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append( "void f();              \n" ); //$NON-NLS-1$
    	buffer.append( "void g() {             \n" ); //$NON-NLS-1$
    	buffer.append( "   f();                \n" ); //$NON-NLS-1$
    	buffer.append( "}                      \n" ); //$NON-NLS-1$
    	buffer.append( "void f(){ }            \n" ); //$NON-NLS-1$
    	
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
    	
    	//void f();
    	IASTSimpleDeclaration fdecl = (IASTSimpleDeclaration) tu.getDeclarations().get(0);
    	IASTFunctionDeclarator fdtor = (IASTFunctionDeclarator) fdecl.getDeclarators().get(0);
    	IASTName name_f = fdtor.getName();
    	
    	//void g() {
    	IASTFunctionDefinition gdef = (IASTFunctionDefinition) tu.getDeclarations().get(1);
    	
    	//   f();
    	IASTCompoundStatement compound = (IASTCompoundStatement) gdef.getBody();
    	IASTExpressionStatement expStatement = (IASTExpressionStatement) compound.getStatements().get(0);
    	IASTFunctionCallExpression fcall = (IASTFunctionCallExpression) expStatement.getExpression();
    	IASTIdExpression fcall_id = (IASTIdExpression) fcall.getFunctionNameExpression();
    	IASTName name_fcall = fcall_id.getName();
    	assertNull( fcall.getParameterExpression() );
    	
    	//void f() {}
    	IASTFunctionDefinition fdef = (IASTFunctionDefinition) tu.getDeclarations().get(2);
    	fdtor = fdef.getDeclarator();
    	IASTName name_fdef = fdtor.getName();
    	
    	//bindings
    	IFunction function_1 = (IFunction) name_fcall.resolveBinding();
    	IFunction function_2 = (IFunction) name_f.resolveBinding();
    	IFunction function_3 =  (IFunction) name_fdef.resolveBinding();
    	
    	assertNotNull( function_1 );
    	assertSame( function_1, function_2 );
    	assertSame( function_2, function_3 );
    }
    
    public void testForLoop() throws Exception {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append( "void f() {                         \n"); //$NON-NLS-1$
    	buffer.append( "   for( int i = 0; i < 5; i++ ) {  \n"); //$NON-NLS-1$         
    	buffer.append( "      i;                           \n"); //$NON-NLS-1$
    	buffer.append( "   }                               \n"); //$NON-NLS-1$
    	buffer.append( "}                                  \n"); //$NON-NLS-1$
    	
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
    	
    	//void f() {
    	IASTFunctionDefinition fdef = (IASTFunctionDefinition) tu.getDeclarations().get(0);
    	IASTCompoundStatement compound = (IASTCompoundStatement) fdef.getBody();
    	
    	//   for( 
    	IASTForStatement for_stmt = (IASTForStatement) compound.getStatements().get(0);
    	//        int i = 0;
    	assertNull( for_stmt.getInitExpression() );
    	IASTSimpleDeclaration initDecl = (IASTSimpleDeclaration) for_stmt.getInitDeclaration();
    	IASTDeclarator dtor = (IASTDeclarator) initDecl.getDeclarators().get(0);
    	IASTName name_i = dtor.getName();
    	//                   i < 5;
    	IASTBinaryExpression exp = (IASTBinaryExpression) for_stmt.getCondition();
    	IASTIdExpression id_i = (IASTIdExpression) exp.getOperand1();
    	IASTName name_i2 = id_i.getName();
    	IASTLiteralExpression lit_5 = (IASTLiteralExpression) exp.getOperand2();
    	assertEquals( IASTLiteralExpression.lk_integer_constant, lit_5.getKind() );
    	//                           i++ ) {
    	IASTUnaryExpression un = (IASTUnaryExpression) for_stmt.getIterationExpression();
    	IASTIdExpression id_i2 = (IASTIdExpression) un.getOperand();
    	IASTName name_i3 = id_i2.getName();
    	assertEquals( IASTUnaryExpression.op_postFixIncr, un.getOperator() );
    	
    	//      i;
    	compound = (IASTCompoundStatement) for_stmt.getBody();
    	IASTExpressionStatement exprSt = (IASTExpressionStatement) compound.getStatements().get(0);
    	IASTIdExpression id_i3 = (IASTIdExpression) exprSt.getExpression();
    	IASTName name_i4 = id_i3.getName();
    	
    	//bindings
    	IVariable var_1 = (IVariable) name_i4.resolveBinding();
    	IVariable var_2 = (IVariable) name_i.resolveBinding();
    	IVariable var_3 = (IVariable) name_i2.resolveBinding();
    	IVariable var_4 = (IVariable) name_i3.resolveBinding();
    	
    	assertSame( var_1, var_2 );
    	assertSame( var_2, var_3 );
    	assertSame( var_3, var_4 );
    }
    
    public void testExpressionFieldReference() throws Exception{
    	StringBuffer buffer = new StringBuffer();
    	buffer.append( "struct A { int x; };    \n"); //$NON-NLS-1$
    	buffer.append( "void f(){               \n"); //$NON-NLS-1$
    	buffer.append( "   ((struct A *) 1)->x; \n"); //$NON-NLS-1$
    	buffer.append( "}                       \n"); //$NON-NLS-1$
    	
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
    	
    	IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) tu.getDeclarations().get(0);
    	IASTCompositeTypeSpecifier compType = (IASTCompositeTypeSpecifier) simpleDecl.getDeclSpecifier();
    	IASTSimpleDeclaration decl_x = (IASTSimpleDeclaration) compType.getMembers().get(0);
    	IASTName name_x1 = ((IASTDeclarator) decl_x.getDeclarators().get(0)).getName();
    	IASTFunctionDefinition fdef = (IASTFunctionDefinition) tu.getDeclarations().get(1);
    	IASTCompoundStatement body = (IASTCompoundStatement) fdef.getBody();
    	IASTExpressionStatement expStatement = (IASTExpressionStatement) body.getStatements().get(0);
    	IASTFieldReference fieldRef = (IASTFieldReference) expStatement.getExpression();
    	IASTName name_x2 = fieldRef.getFieldName();
    	
    	IField x1 = (IField) name_x1.resolveBinding();
    	IField x2 = (IField) name_x2.resolveBinding();
    	
    	assertNotNull( x1 );
    	assertSame( x1, x2 );
    }
    
    public void testLabels() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f() {          \n"); //$NON-NLS-1$
        buffer.append("   while( 1 ) {     \n"); //$NON-NLS-1$
        buffer.append("      if( 1 )       \n"); //$NON-NLS-1$
        buffer.append("         goto end;  \n"); //$NON-NLS-1$
        buffer.append("   }                \n"); //$NON-NLS-1$
        buffer.append("   end: ;           \n"); //$NON-NLS-1$
        buffer.append("}                   \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        
        NameCollector collector = new NameCollector();
        CVisitor.visitTranslationUnit( tu, collector );
        
        assertEquals( collector.size(), 3 );
        IFunction function = (IFunction) collector.getName( 0 ).resolveBinding();
        ILabel label_1 = (ILabel) collector.getName( 1 ).resolveBinding();
        ILabel label_2 = (ILabel) collector.getName( 2 ).resolveBinding();
        assertNotNull( function );
        assertNotNull( label_1 );
        assertEquals( label_1, label_2 );
    }
    
    public void testAnonStruct() throws Exception
    {
        StringBuffer buffer = new StringBuffer( "typedef struct { } X;\n"); //$NON-NLS-1$
        buffer.append( "int f( X x );"); //$NON-NLS-1$
        parse( buffer.toString(), ParserLanguage.C );
    }
    
    public void testLongLong() throws ParserException
    {
        parse( "long long x;\n", ParserLanguage.C ); //$NON-NLS-1$
    }
}

