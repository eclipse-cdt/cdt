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
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
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
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
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
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.IASTEnumerationSpecifier.IASTEnumerator;
import org.eclipse.cdt.core.dom.ast.c.ICASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTEnumerationSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICFunctionScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.parser.ParserException;

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

		IASTDeclaration[] declarations = tu.getDeclarations();

		// int x
		IASTSimpleDeclaration decl_x = (IASTSimpleDeclaration) declarations[0];
		IASTSimpleDeclSpecifier declspec_x = (IASTSimpleDeclSpecifier) decl_x
				.getDeclSpecifier();
		assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_x.getType());
		IASTDeclarator declor_x = decl_x.getDeclarators()[0];
		IASTName name_x = declor_x.getName();
		assertEquals("x", name_x.toString()); //$NON-NLS-1$

		// function - void f()
		IASTFunctionDefinition funcdef_f = (IASTFunctionDefinition) declarations[1];
		IASTSimpleDeclSpecifier declspec_f = (IASTSimpleDeclSpecifier) funcdef_f
				.getDeclSpecifier();
		assertEquals(IASTSimpleDeclSpecifier.t_void, declspec_f.getType());
		IASTFunctionDeclarator declor_f = funcdef_f
				.getDeclarator();
		IASTName name_f = declor_f.getName();
		assertEquals("f", name_f.toString()); //$NON-NLS-1$

		// parameter - int y
		IASTParameterDeclaration decl_y = declor_f.getParameters()[0];
		IASTSimpleDeclSpecifier declspec_y = (IASTSimpleDeclSpecifier) decl_y
				.getDeclSpecifier();
		assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_y.getType());
		IASTDeclarator declor_y = decl_y.getDeclarator();
		IASTName name_y = declor_y.getName();
		assertEquals("y", name_y.toString()); //$NON-NLS-1$

		// int z
		IASTCompoundStatement body_f = (IASTCompoundStatement) funcdef_f
				.getBody();
		IASTDeclarationStatement declstmt_z = (IASTDeclarationStatement) body_f.getStatements()[0];
		IASTSimpleDeclaration decl_z = (IASTSimpleDeclaration) declstmt_z
				.getDeclaration();
		IASTSimpleDeclSpecifier declspec_z = (IASTSimpleDeclSpecifier) decl_z
				.getDeclSpecifier();
		assertEquals(IASTSimpleDeclSpecifier.t_int, declspec_z.getType());
		IASTDeclarator declor_z = decl_z.getDeclarators()[0];
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
		assertEquals(((ICFunctionScope)func_f.getFunctionScope()).getBodyScope(), var_z.getScope());

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
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration)tu.getDeclarations()[0];
		IASTCompositeTypeSpecifier type = (IASTCompositeTypeSpecifier)decl.getDeclSpecifier();
		
		// it's a typedef
		assertEquals(IASTDeclSpecifier.sc_typedef, type.getStorageClass());
		// this an anonymous struct
		IASTName name_struct = type.getName();
		assertNull("", name_struct.toString()); //$NON-NLS-1$
		// member - x
		IASTSimpleDeclaration decl_x = (IASTSimpleDeclaration) type.getMembers()[0];
		IASTSimpleDeclSpecifier spec_x = (IASTSimpleDeclSpecifier) decl_x
				.getDeclSpecifier();
		// it's an int
		assertEquals(IASTSimpleDeclSpecifier.t_int, spec_x.getType());
		IASTDeclarator tor_x = decl_x.getDeclarators()[0];
		IASTName name_x = tor_x.getName();
		assertEquals("x", name_x.toString()); //$NON-NLS-1$

		// declarator S
		IASTDeclarator tor_S = decl.getDeclarators()[0];
		IASTName name_S = tor_S.getName();
		assertEquals("S", name_S.toString()); //$NON-NLS-1$

		// function f
		IASTFunctionDefinition def_f = (IASTFunctionDefinition) tu.getDeclarations()[1];
		// f's body
		IASTCompoundStatement body_f = (IASTCompoundStatement) def_f.getBody();
		// the declaration statement for myS
		IASTDeclarationStatement declstmt_myS = (IASTDeclarationStatement)body_f.getStatements()[0];
		// the declaration for myS
		IASTSimpleDeclaration decl_myS = (IASTSimpleDeclaration)declstmt_myS.getDeclaration();
		// the type specifier for myS
		IASTNamedTypeSpecifier type_spec_myS = (IASTNamedTypeSpecifier)decl_myS.getDeclSpecifier();
		// the type name for myS
		IASTName name_type_myS = type_spec_myS.getName();
		// the declarator for myS
		IASTDeclarator tor_myS = decl_myS.getDeclarators()[0];
		// the name for myS
		IASTName name_myS = tor_myS.getName();
		// the assignment expression statement
		IASTExpressionStatement exprstmt = (IASTExpressionStatement)body_f.getStatements()[1];
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
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration)tu.getDeclarations()[0];
		IASTDeclarator[] declarators = decl.getDeclarators();
		assertEquals( 2, declarators.length );
		
		IASTDeclarator dtor1 = declarators[0];
		IASTDeclarator dtor2 = declarators[1];
		
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
    	IASTSimpleDeclaration decl1 = (IASTSimpleDeclaration) tu.getDeclarations()[0];
    	IASTElaboratedTypeSpecifier compTypeSpec = (IASTElaboratedTypeSpecifier) decl1.getDeclSpecifier();
    	assertEquals( 0, decl1.getDeclarators().length );
    	IASTName nameA1 = compTypeSpec.getName();
    	
    	//void f() {
    	IASTFunctionDefinition fndef = (IASTFunctionDefinition) tu.getDeclarations()[1];
    	IASTCompoundStatement compoundStatement = (IASTCompoundStatement) fndef.getBody();
    	assertEquals( 2, compoundStatement.getStatements().length );
    	
    	//   struct A;
    	IASTDeclarationStatement declStatement = (IASTDeclarationStatement) compoundStatement.getStatements()[0];
    	IASTSimpleDeclaration decl2 = (IASTSimpleDeclaration) declStatement.getDeclaration();
    	compTypeSpec = (IASTElaboratedTypeSpecifier) decl2.getDeclSpecifier();
    	assertEquals( 0, decl2.getDeclarators().length );
    	IASTName nameA2 = compTypeSpec.getName();
    	
    	//   struct A * a;
    	declStatement = (IASTDeclarationStatement) compoundStatement.getStatements()[1];
    	IASTSimpleDeclaration decl3 = (IASTSimpleDeclaration) declStatement.getDeclaration();
    	compTypeSpec = (IASTElaboratedTypeSpecifier) decl3.getDeclSpecifier();
    	IASTName nameA3 = compTypeSpec.getName();
    	IASTDeclarator dtor = decl3.getDeclarators()[0];
    	IASTName namea = dtor.getName();
    	assertEquals( 1, dtor.getPointerOperators().length );
    	assertTrue( dtor.getPointerOperators()[0] instanceof ICASTPointer );

    	//bindings
    	ICompositeType str1 = (ICompositeType) nameA1.resolveBinding();
    	ICompositeType str2 = (ICompositeType) nameA2.resolveBinding();
    	IVariable var = (IVariable) namea.resolveBinding();
    	IType str3pointer = var.getType();
    	assertTrue(str3pointer instanceof IPointerType);
    	ICompositeType str3 = (ICompositeType) ((IPointerType)str3pointer).getType();
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
    	IASTSimpleDeclaration decl1 = (IASTSimpleDeclaration) tu.getDeclarations()[0];
    	IASTElaboratedTypeSpecifier compTypeSpec = (IASTElaboratedTypeSpecifier) decl1.getDeclSpecifier();
    	assertEquals( 0, decl1.getDeclarators().length );
    	IASTName nameA1 = compTypeSpec.getName();
    	
    	//void f() {
    	IASTFunctionDefinition fndef = (IASTFunctionDefinition) tu.getDeclarations()[1];
    	IASTCompoundStatement compoundStatement = (IASTCompoundStatement) fndef.getBody();
    	assertEquals( 1, compoundStatement.getStatements().length );
    	  	
    	//   struct A * a;
    	IASTDeclarationStatement declStatement = (IASTDeclarationStatement) compoundStatement.getStatements()[0];
    	IASTSimpleDeclaration decl2 = (IASTSimpleDeclaration) declStatement.getDeclaration();
    	compTypeSpec = (IASTElaboratedTypeSpecifier) decl2.getDeclSpecifier();
    	IASTName nameA2 = compTypeSpec.getName();  	
    	IASTDeclarator dtor = decl2.getDeclarators()[0];
    	IASTName namea = dtor.getName();
    	assertEquals( 1, dtor.getPointerOperators().length );
    	assertTrue( dtor.getPointerOperators()[0] instanceof ICASTPointer );

    	//bindings
    	ICompositeType str1 = (ICompositeType) nameA1.resolveBinding();
    	ICompositeType str2 = (ICompositeType) nameA2.resolveBinding();
    	IVariable var = (IVariable) namea.resolveBinding();
    	IPointerType str3pointer = (IPointerType) var.getType();
    	ICompositeType str3 = (ICompositeType) str3pointer.getType();
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
    	IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
    	IASTElaboratedTypeSpecifier elabTypeSpec = (IASTElaboratedTypeSpecifier) decl.getDeclSpecifier();
    	assertEquals( 0, decl.getDeclarators().length );
    	IASTName name_A1 = elabTypeSpec.getName();
    	
    	//struct A * a;
    	decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
    	elabTypeSpec = (IASTElaboratedTypeSpecifier) decl.getDeclSpecifier();
    	IASTName name_A2 = elabTypeSpec.getName();  	
    	IASTDeclarator dtor = decl.getDeclarators()[0];
    	IASTName name_a = dtor.getName();
    	assertEquals( 1, dtor.getPointerOperators().length );
    	assertTrue( dtor.getPointerOperators()[0] instanceof ICASTPointer );
    	
    	//struct A {
    	decl = (IASTSimpleDeclaration) tu.getDeclarations()[2];
    	ICASTCompositeTypeSpecifier compTypeSpec = (ICASTCompositeTypeSpecifier) decl.getDeclSpecifier();
    	IASTName name_Adef = compTypeSpec.getName();
    	
    	//   int i;
    	decl = (IASTSimpleDeclaration) compTypeSpec.getMembers()[0];
    	dtor = decl.getDeclarators()[0];
    	IASTName name_i = dtor.getName();
    
    	//void f() {
    	IASTFunctionDefinition fndef = (IASTFunctionDefinition) tu.getDeclarations()[3];
    	IASTCompoundStatement compoundStatement = (IASTCompoundStatement) fndef.getBody();
    	assertEquals( 1, compoundStatement.getStatements().length );
    	
    	//   a->i;
		IASTExpressionStatement exprstmt = (IASTExpressionStatement)compoundStatement.getStatements()[0];
		IASTFieldReference fieldref = (IASTFieldReference)exprstmt.getExpression();
		IASTIdExpression id_a = (IASTIdExpression) fieldref.getFieldOwner();
		IASTName name_aref = id_a.getName();
		IASTName name_iref = fieldref.getFieldName();

		//bindings
		IVariable var_a1 = (IVariable) name_aref.resolveBinding();
		IVariable var_i1 = (IVariable) name_iref.resolveBinding();
		IPointerType structA_1pointer = (IPointerType) var_a1.getType();
		ICompositeType structA_1 = (ICompositeType) structA_1pointer.getType();
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
        
        IASTSimpleDeclaration declaration = (IASTSimpleDeclaration) tu.getDeclarations()[0];
        IASTCompositeTypeSpecifier typeSpec = (IASTCompositeTypeSpecifier) declaration.getDeclSpecifier();
        IASTName x_1 = typeSpec.getName();
        
        IASTFunctionDefinition fdef = (IASTFunctionDefinition) tu.getDeclarations()[1];
        IASTParameterDeclaration param = fdef.getDeclarator().getParameters()[0];
        IASTName x_2 = param.getDeclarator().getName();
        
        IASTCompoundStatement compound = (IASTCompoundStatement) fdef.getBody();
        IASTDeclarationStatement declStatement = (IASTDeclarationStatement) compound.getStatements()[0];
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
    	IASTSimpleDeclaration f_decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
    	IASTFunctionDeclarator dtor = (IASTFunctionDeclarator) f_decl.getDeclarators()[0];
    	IASTName f_name1 = dtor.getName();
    	//        int a );
    	IASTParameterDeclaration param = dtor.getParameters()[0];
    	IASTDeclarator paramDtor = param.getDeclarator();
    	IASTName name_param1 = paramDtor.getName();
    	
    	//void f( 
    	IASTFunctionDefinition f_defn = (IASTFunctionDefinition) tu.getDeclarations()[1];
    	dtor = f_defn.getDeclarator();
    	IASTName f_name2 = dtor.getName();
    	//        int b );
    	param = dtor.getParameters()[0];
    	paramDtor = param.getDeclarator();
    	IASTName name_param2 = paramDtor.getName();
    	
    	//   b;
    	IASTCompoundStatement compound = (IASTCompoundStatement) f_defn.getBody();
    	IASTExpressionStatement expStatement = (IASTExpressionStatement) compound.getStatements()[0];
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
    	
    	IASTFunctionDefinition fDef = (IASTFunctionDefinition) tu.getDeclarations()[0];
    	IASTFunctionDeclarator fDtor = fDef.getDeclarator();
    	IASTName fName = fDtor.getName();
    	
    	IASTParameterDeclaration a = fDtor.getParameters()[0];
    	IASTName name_a = a.getDeclarator().getName();
    	
    	IASTParameterDeclaration b = fDtor.getParameters()[1];
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
    	IASTSimpleDeclaration fdecl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
    	IASTFunctionDeclarator fdtor = (IASTFunctionDeclarator) fdecl.getDeclarators()[0];
    	IASTName name_f = fdtor.getName();
    	
    	//void g() {
    	IASTFunctionDefinition gdef = (IASTFunctionDefinition) tu.getDeclarations()[1];
    	
    	//   f();
    	IASTCompoundStatement compound = (IASTCompoundStatement) gdef.getBody();
    	IASTExpressionStatement expStatement = (IASTExpressionStatement) compound.getStatements()[0];
    	IASTFunctionCallExpression fcall = (IASTFunctionCallExpression) expStatement.getExpression();
    	IASTIdExpression fcall_id = (IASTIdExpression) fcall.getFunctionNameExpression();
    	IASTName name_fcall = fcall_id.getName();
    	assertNull( fcall.getParameterExpression() );
    	
    	//void f() {}
    	IASTFunctionDefinition fdef = (IASTFunctionDefinition) tu.getDeclarations()[2];
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
    	IASTFunctionDefinition fdef = (IASTFunctionDefinition) tu.getDeclarations()[0];
    	IASTCompoundStatement compound = (IASTCompoundStatement) fdef.getBody();
    	
    	//   for( 
    	IASTForStatement for_stmt = (IASTForStatement) compound.getStatements()[0];
    	//        int i = 0;
    	assertNull( for_stmt.getInitExpression() );
    	IASTSimpleDeclaration initDecl = (IASTSimpleDeclaration) for_stmt.getInitDeclaration();
    	IASTDeclarator dtor = initDecl.getDeclarators()[0];
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
    	IASTExpressionStatement exprSt = (IASTExpressionStatement) compound.getStatements()[0];
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
    	
    	IASTSimpleDeclaration simpleDecl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
    	IASTCompositeTypeSpecifier compType = (IASTCompositeTypeSpecifier) simpleDecl.getDeclSpecifier();
    	IASTSimpleDeclaration decl_x = (IASTSimpleDeclaration) compType.getMembers()[0];
    	IASTName name_x1 = decl_x.getDeclarators()[0].getName();
    	IASTFunctionDefinition fdef = (IASTFunctionDefinition) tu.getDeclarations()[1];
    	IASTCompoundStatement body = (IASTCompoundStatement) fdef.getBody();
    	IASTExpressionStatement expStatement = (IASTExpressionStatement) body.getStatements()[0];
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
        
        CNameCollector collector = new CNameCollector();
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
    
    public void testEnumerations() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "enum hue { red, blue, green };     \n" ); //$NON-NLS-1$
        buffer.append( "enum hue col, *cp;                 \n" ); //$NON-NLS-1$
        buffer.append( "void f() {                         \n" ); //$NON-NLS-1$
        buffer.append( "   col = blue;                     \n" ); //$NON-NLS-1$
        buffer.append( "   cp = &col;                      \n" ); //$NON-NLS-1$
        buffer.append( "   if( *cp != red )                \n" ); //$NON-NLS-1$
        buffer.append( "      return;                      \n" ); //$NON-NLS-1$
        buffer.append( "}                                  \n" ); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        
        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
        assertEquals( decl.getDeclarators().length, 0 );
        ICASTEnumerationSpecifier enumSpec = (ICASTEnumerationSpecifier) decl.getDeclSpecifier();
        IASTEnumerator e1 = enumSpec.getEnumerators()[0];
        IASTEnumerator e2 = enumSpec.getEnumerators()[1];
        IASTEnumerator e3 = enumSpec.getEnumerators()[2];
        IASTName name_hue = enumSpec.getName();
        
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
        IASTDeclarator dtor = decl.getDeclarators()[0];
        IASTName name_col = dtor.getName();
        dtor = decl.getDeclarators()[1];
        IASTName name_cp = dtor.getName();
        IASTElaboratedTypeSpecifier spec = (IASTElaboratedTypeSpecifier) decl.getDeclSpecifier();
        assertEquals( spec.getKind(), IASTElaboratedTypeSpecifier.k_enum );
        IASTName name_hue2 = spec.getName();
        
        IASTFunctionDefinition fn = (IASTFunctionDefinition) tu.getDeclarations()[2];
        IASTCompoundStatement compound = (IASTCompoundStatement) fn.getBody();
        IASTExpressionStatement expStatement = (IASTExpressionStatement) compound.getStatements()[0];
        IASTBinaryExpression exp = (IASTBinaryExpression) expStatement.getExpression();
        assertEquals( exp.getOperator(), IASTBinaryExpression.op_assign );
        IASTIdExpression id1 = (IASTIdExpression) exp.getOperand1();
        IASTIdExpression id2 = (IASTIdExpression) exp.getOperand2();
        IASTName r_col = id1.getName();
        IASTName r_blue = id2.getName();
        
        expStatement = (IASTExpressionStatement) compound.getStatements()[1];
        exp = (IASTBinaryExpression) expStatement.getExpression();
        assertEquals( exp.getOperator(), IASTBinaryExpression.op_assign );
        id1 = (IASTIdExpression) exp.getOperand1();
        IASTUnaryExpression ue = (IASTUnaryExpression) exp.getOperand2();
        id2 = (IASTIdExpression) ue.getOperand();
        IASTName r_cp = id1.getName();
        IASTName r_col2 = id2.getName();
        
        IASTIfStatement ifStatement = (IASTIfStatement) compound.getStatements()[2];
        exp = (IASTBinaryExpression) ifStatement.getCondition();
        ue = (IASTUnaryExpression) exp.getOperand1();
        id1 = (IASTIdExpression) ue.getOperand();
        id2 = (IASTIdExpression) exp.getOperand2();
        
        IASTName r_cp2 = id1.getName();
        IASTName r_red = id2.getName();
        
        IEnumeration hue = (IEnumeration) name_hue.resolveBinding();
        IEnumerator red = (IEnumerator) e1.getName().resolveBinding();
        IEnumerator blue = (IEnumerator) e2.getName().resolveBinding();
        IEnumerator green = (IEnumerator) e3.getName().resolveBinding();
        IVariable col = (IVariable) name_col.resolveBinding();
        IVariable cp = (IVariable) name_cp.resolveBinding();
        IEnumeration hue_2 = (IEnumeration) name_hue2.resolveBinding();
        IVariable col2 = (IVariable) r_col.resolveBinding();
        IEnumerator blue2 = (IEnumerator) r_blue.resolveBinding();
        IVariable cp2 = (IVariable) r_cp.resolveBinding();
        IVariable col3 = (IVariable) r_col2.resolveBinding();
        IVariable cp3 = (IVariable) r_cp2.resolveBinding();
        IEnumerator red2 = (IEnumerator) r_red.resolveBinding();
        
        assertNotNull( hue );
        assertSame( hue, hue_2 );
        assertNotNull( red );
        assertNotNull( green );
        assertNotNull( blue );
        assertNotNull( col );
        assertNotNull( cp );
        assertSame( col, col2 );
        assertSame( blue, blue2);
        assertSame( cp, cp2 );
        assertSame( col, col3 );
        assertSame( cp, cp3 );
        assertSame( red, red2 );
    }
    
    public void testPointerToFunction() throws Exception
    {
        IASTTranslationUnit tu = parse( "int (*pfi)();", ParserLanguage.C ); //$NON-NLS-1$
        assertEquals( tu.getDeclarations().length, 1 );
        IASTSimpleDeclaration d = (IASTSimpleDeclaration) tu.getDeclarations()[0];
        assertEquals( d.getDeclarators().length, 1 );
        IASTFunctionDeclarator f = (IASTFunctionDeclarator) d.getDeclarators()[0];
        assertNull( f.getName().toString() );
        assertNotNull( f.getNestedDeclarator() );
        assertEquals( f.getNestedDeclarator().getName().toString(), "pfi"); //$NON-NLS-1$
        assertTrue( f.getPointerOperators().length == 0 );
        assertFalse( f.getNestedDeclarator().getPointerOperators().length == 0 );
        tu = parse( "int (*pfi)();", ParserLanguage.CPP ); //$NON-NLS-1$
        assertEquals( tu.getDeclarations().length, 1 );
        d = (IASTSimpleDeclaration) tu.getDeclarations()[0];
        assertEquals( d.getDeclarators().length, 1 );
        f = (IASTFunctionDeclarator) d.getDeclarators()[0];
        assertNull( f.getName().toString() );
        assertNotNull( f.getNestedDeclarator() );
        assertEquals( f.getNestedDeclarator().getName().toString(), "pfi"); //$NON-NLS-1$
    }
    

    public void testBasicPointerToMember() throws Exception
    {
        StringBuffer buffer = new StringBuffer( "class X {\n"); //$NON-NLS-1$
        buffer.append( "  public:\n"); //$NON-NLS-1$
        buffer.append( "  void f(int);\n"); //$NON-NLS-1$
        buffer.append( "  int a;\n"); //$NON-NLS-1$
        buffer.append( "};\n"); //$NON-NLS-1$
        buffer.append( "int X:: * pmi = &X::a;\n"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
        assertEquals( tu.getDeclarations().length, 2 );
        IASTSimpleDeclaration p2m = (IASTSimpleDeclaration) tu.getDeclarations()[1];
        IASTDeclarator d = p2m.getDeclarators()[0];
        ICPPASTPointerToMember po = (ICPPASTPointerToMember) d.getPointerOperators()[0];
        assertEquals( po.getName().toString(), "X::"); //$NON-NLS-1$
    }
    
    public void testAmbiguity() throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "class A { };\n"); //$NON-NLS-1$
        buffer.append( "int f() { \n"); //$NON-NLS-1$
        buffer.append( "  A * b = 0;\n"); //$NON-NLS-1$
        buffer.append( "  A & c = 0;\n"); //$NON-NLS-1$
        buffer.append( "}"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
        IASTSimpleDeclaration A = (IASTSimpleDeclaration) tu.getDeclarations()[0];
        IASTFunctionDefinition f = (IASTFunctionDefinition) tu.getDeclarations()[1];
        IASTCompoundStatement body = (IASTCompoundStatement) f.getBody();
        for( int i = 0; i < 2; ++i )
        {
	        IASTDeclarationStatement ds = (IASTDeclarationStatement) body.getStatements()[i];
	        String s1 = ((IASTNamedTypeSpecifier)((IASTSimpleDeclaration)ds.getDeclaration()).getDeclSpecifier()).getName().toString();
	        String s2 = ((IASTCompositeTypeSpecifier)A.getDeclSpecifier()).getName().toString();
	        assertEquals( s1, s2);
        }
         
    }
    
    public void testBasicTypes() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "int a;       \n" ); //$NON-NLS-1$
        buffer.append( "char * b;    \n" ); //$NON-NLS-1$
        buffer.append( "const int c; \n" ); //$NON-NLS-1$
        buffer.append( "const char * const d; \n" ); //$NON-NLS-1$
        buffer.append( "const char ** e; \n" ); //$NON-NLS-1$
        buffer.append( "const char ***** f; \n" ); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        
        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
        IVariable a = (IVariable) decl.getDeclarators()[0].getName().resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
        IVariable b = (IVariable) decl.getDeclarators()[0].getName().resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[2];
        IVariable c = (IVariable) decl.getDeclarators()[0].getName().resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[3];
        IVariable d = (IVariable) decl.getDeclarators()[0].getName().resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[4];
        IVariable e = (IVariable) decl.getDeclarators()[0].getName().resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[5];
        IVariable f = (IVariable) decl.getDeclarators()[0].getName().resolveBinding();
        
        IType t_a_1 = a.getType();
        assertTrue( t_a_1 instanceof IBasicType );
        assertFalse( ((IBasicType)t_a_1).isLong() );
        assertFalse( ((IBasicType)t_a_1).isShort() );
        assertFalse( ((IBasicType)t_a_1).isSigned() );
        assertFalse( ((IBasicType)t_a_1).isUnsigned() );
        assertEquals( ((IBasicType)t_a_1).getType(), IBasicType.t_int );
        
        IType t_b_1 = b.getType();
        assertTrue( t_b_1 instanceof IPointerType );
        IType t_b_2 = ((IPointerType) t_b_1).getType();
        assertTrue( t_b_2 instanceof IBasicType );
        assertEquals( ((IBasicType)t_b_2).getType(), IBasicType.t_char );

        IType t_c_1 = c.getType();
        assertTrue( t_c_1 instanceof IQualifierType );
        assertTrue( ((IQualifierType)t_c_1).isConst() );
        IType t_c_2 = ((IQualifierType)t_c_1).getType();
        assertTrue( t_c_2 instanceof IBasicType );
        assertEquals( ((IBasicType)t_c_2).getType(), IBasicType.t_int );
        
        IType t_d_1 = d.getType();
        assertTrue( t_d_1 instanceof IPointerType );
        assertTrue( ((IPointerType)t_d_1).isConst() );
        IType t_d_2 = ((IPointerType)t_d_1).getType();
        assertTrue( t_d_2 instanceof IQualifierType );
        assertTrue( ((IQualifierType)t_d_2).isConst() );
        IType t_d_3 = ((IQualifierType)t_d_2).getType();
        assertTrue( t_d_3 instanceof IBasicType );
        assertEquals( ((IBasicType)t_d_3).getType(), IBasicType.t_char );
        
        IType t_e_1 = e.getType();
        assertTrue( t_e_1 instanceof IPointerType );
        IType t_e_2 = ((IPointerType)t_e_1).getType();
        assertTrue( t_e_2 instanceof IPointerType );
        IType t_e_3 = ((IPointerType)t_e_2).getType();
        assertTrue( t_e_3 instanceof IQualifierType );
        assertTrue( ((IQualifierType)t_e_3).isConst() );
        IType t_e_4 = ((IQualifierType)t_e_3).getType();
        assertTrue( t_e_4 instanceof IBasicType );
        assertEquals( ((IBasicType)t_e_4).getType(), IBasicType.t_char );
        
        IType t_f_1 = f.getType();
        assertTrue( t_f_1 instanceof IPointerType );
        IType t_f_2 = ((IPointerType)t_f_1).getType();
        assertTrue( t_f_2 instanceof IPointerType );
        IType t_f_3 = ((IPointerType)t_f_2).getType();
        assertTrue( t_f_2 instanceof IPointerType );
        IType t_f_4 = ((IPointerType)t_f_3).getType();
        assertTrue( t_f_2 instanceof IPointerType );
        IType t_f_5 = ((IPointerType)t_f_4).getType();
        assertTrue( t_f_2 instanceof IPointerType );
        IType t_f_6 = ((IPointerType)t_f_5).getType();
        assertTrue( t_f_6 instanceof IQualifierType );
        assertTrue( ((IQualifierType)t_f_6).isConst() );
        IType t_f_7 = ((IQualifierType)t_f_6).getType();
        assertTrue( t_f_7 instanceof IBasicType );
        assertEquals( ((IBasicType)t_f_7).getType(), IBasicType.t_char );
    }
    
    public void testCompositeTypes() throws Exception{
        StringBuffer buffer = new StringBuffer();
        buffer.append( "struct A {} a1;              \n"); //$NON-NLS-1$
        buffer.append( "typedef struct A * AP;       \n"); //$NON-NLS-1$
        buffer.append( "struct A * const a2;         \n"); //$NON-NLS-1$
        buffer.append( "AP a3;                       \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        
        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
        IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
        ICompositeType A = (ICompositeType) compSpec.getName().resolveBinding();
        IVariable a1 = (IVariable) decl.getDeclarators()[0].getName().resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
        ITypedef AP = (ITypedef) decl.getDeclarators()[0].getName().resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[2];
        IVariable a2 = (IVariable) decl.getDeclarators()[0].getName().resolveBinding();
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[3];
        IVariable a3 = (IVariable) decl.getDeclarators()[0].getName().resolveBinding();

        IType t_a1 = a1.getType();
        assertSame( t_a1, A );
        
        IType t_a2 = a2.getType();
        assertTrue( t_a2 instanceof IPointerType );
        assertTrue( ((IPointerType) t_a2).isConst() );
        assertSame( ((IPointerType) t_a2).getType(), A );
        
        IType t_a3 = a3.getType();
        assertSame( t_a3, AP );
        IType t_AP = AP.getType();
        assertTrue( t_AP instanceof IPointerType );
        assertSame( ((IPointerType) t_AP).getType(), A );
    }
    
    public void _testFunctionTypes() throws Exception{
        StringBuffer buffer = new StringBuffer();
        buffer.append( "struct A;                           \n"); //$NON-NLS-1$
        buffer.append( "int * f( int i, char c );           \n"); //$NON-NLS-1$
        buffer.append( "void ( *g ) ( struct A * );         \n"); //$NON-NLS-1$
        buffer.append( "void (* (*h)(struct A**) ) ( int ); \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C );
        
        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
        IASTElaboratedTypeSpecifier elabSpec = (IASTElaboratedTypeSpecifier) decl.getDeclSpecifier();
        ICompositeType A = (ICompositeType) elabSpec.getName().resolveBinding();
        
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
        IFunction f = (IFunction) decl.getDeclarators()[0].getName().resolveBinding();
        
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[2];
        IVariable g = (IVariable) decl.getDeclarators()[0].getNestedDeclarator().getName().resolveBinding();
        
        decl = (IASTSimpleDeclaration) tu.getDeclarations()[3];
        IVariable h = (IVariable) decl.getDeclarators()[0].getNestedDeclarator().getNestedDeclarator().getName().resolveBinding();
        
        IFunctionType t_f = f.getType();
        IType t_f_return = t_f.getReturnType();
        assertTrue( t_f_return instanceof IPointerType );
        assertTrue( ((IPointerType) t_f_return).getType() instanceof IBasicType );
        IType [] t_f_params = t_f.getParameterTypes();
        assertEquals( t_f_params.length, 2 );
        assertTrue( t_f_params[0] instanceof IBasicType );
        assertTrue( t_f_params[1] instanceof IBasicType );
        
        //g is a pointer to a function that returns void and has 1 parameter struct A *
        IType t_g = g.getType();
        assertTrue( t_g instanceof IPointerType );
        assertTrue( ((IPointerType) t_g).getType() instanceof IFunctionType );
        IFunctionType t_g_func = (IFunctionType) ((IPointerType) t_g).getType();
        IType t_g_func_return = t_g_func.getReturnType();
        assertTrue( t_g_func_return instanceof IBasicType );
        IType [] t_g_func_params = t_g_func.getParameterTypes();
        assertEquals( t_g_func_params.length, 1 );
        IType t_g_func_p1 = t_g_func_params[0];
        assertTrue( t_g_func_p1 instanceof IPointerType );
        assertSame( ((IPointerType)t_g_func_p1).getType(), A );
        
        //h is a pointer to a function that returns a pointer to a function
        //the returned pointer to function returns void and takes 1 parameter int
        // the *h function takes 1 parameter struct A**
        IType t_h = h.getType();
        assertTrue( t_h instanceof IPointerType );
        assertTrue( ((IPointerType) t_h).getType() instanceof IFunctionType );
        IFunctionType t_h_func = (IFunctionType) ((IPointerType) t_h).getType();
        IType t_h_func_return = t_g_func.getReturnType();
        IType [] t_h_func_params = t_h_func.getParameterTypes();
        assertEquals( t_h_func_params.length, 1 );
        IType t_h_func_p1 = t_h_func_params[0];
        assertTrue( t_h_func_p1 instanceof IPointerType );
        assertTrue( ((IPointerType)t_h_func_p1).getType() instanceof IPointerType );
        assertSame( ((IPointerType) ((IPointerType)t_h_func_p1).getType() ).getType(), A );
        
        assertTrue( t_h_func_return instanceof IPointerType );
        IFunctionType h_return = (IFunctionType) ((IPointerType) t_h_func_return).getType();
        IType h_r = h_return.getReturnType();
        IType [] h_ps = h_return.getParameterTypes();
        assertTrue( h_r instanceof IBasicType );
        assertEquals( h_ps.length, 1 );
        assertTrue( h_ps[0] instanceof IBasicType );
    }
}

