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

import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTConditionalExpression;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.tests.parser2.QuickParser2Tests.ProblemCollector;
import org.eclipse.cdt.internal.core.parser.ParserException;
import org.eclipse.cdt.internal.core.parser2.ISourceCodeParser;
import org.eclipse.cdt.internal.core.parser2.c.ANSICParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser2.c.GNUCSourceParser;
import org.eclipse.cdt.internal.core.parser2.c.ICParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser2.cpp.ANSICPPParserExtensionConfiguration;
import org.eclipse.cdt.internal.core.parser2.cpp.GNUCPPSourceParser;
import org.eclipse.cdt.internal.core.parser2.cpp.ICPPParserExtensionConfiguration;

/**
 * Test the new AST.
 * 
 * @author Doug Schaefer
 */
public class AST2Tests extends TestCase {

	private static final ISourceElementRequestor NULL_REQUESTOR = new NullSourceElementRequestor();
    private static final IParserLogService NULL_LOG = new NullLogService();
    
	/**
     * @param string
     * @param c
     * @return
	 * @throws ParserException
     */
    protected IASTTranslationUnit parse(String code, ParserLanguage lang) throws ParserException {
        ProblemCollector collector = new ProblemCollector();
        IScanner scanner = ParserFactory.createScanner(new CodeReader(code
                .toCharArray()), new ScannerInfo(), ParserMode.COMPLETE_PARSE,
                lang, NULL_REQUESTOR,
                NULL_LOG, Collections.EMPTY_LIST);
        ISourceCodeParser parser2 = null;
        if( lang == ParserLanguage.CPP )
        {
            ICPPParserExtensionConfiguration config = null;
            config = new ANSICPPParserExtensionConfiguration();
            parser2 = new GNUCPPSourceParser(scanner, ParserMode.COMPLETE_PARSE, collector,
                NULL_LOG,
                config );
        }
        else
        {
            ICParserExtensionConfiguration config = null;
             config = new ANSICParserExtensionConfiguration();
            
            parser2 = new GNUCSourceParser( scanner, ParserMode.COMPLETE_PARSE, collector, 
                NULL_LOG, config );
        }
        IASTTranslationUnit tu = parser2.parse();
        if( parser2.encounteredError() )
            throw new ParserException( "FAILURE"); //$NON-NLS-1$
        
        assertTrue( collector.hasNoProblems() );

        return tu;
    }


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
		assertEquals(func_f, var_y.getScope());

		IVariable var_z = (IVariable) name_z.resolveBinding();
		assertEquals(func_f, var_z.getScope());

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
		IASTTypedefNameSpecifier type_spec_myS = (IASTTypedefNameSpecifier)decl_myS.getDeclSpecifier();
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
		// the variable myS
		IVariable var_myS = (IVariable)name_myS.resolveBinding();
		assertEquals(type_myS, var_myS.getType());
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
        validateSimpleUnaryTypeIdExpression( "(int)x", IASTUnaryTypeIdExpression.op_cast ); //$NON-NLS-1$
        
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
    
    /**
     * @param string
     * @throws ParserException
     */
    protected void validateSimpleUnaryTypeIdExpression(String code, int op ) throws ParserException {
        IASTUnaryTypeIdExpression e = (IASTUnaryTypeIdExpression) getExpressionFromStatementInCode( code, ParserLanguage.C );
        assertNotNull( e );
        assertEquals( e.getOperator(), op );
        assertNotNull( e.getTypeId() );
        IASTIdExpression x = (IASTIdExpression) e.getOperand();
        assertEquals( x.getName().toString(), "x"); //$NON-NLS-1$
    }


    /**
     * @param code
     * @param op
     * @throws ParserException
     */
    protected void validateSimpleTypeIdExpressionC(String code, int op ) throws ParserException {
        IASTTypeIdExpression e = (IASTTypeIdExpression) getExpressionFromStatementInCode( code, ParserLanguage.C );
        assertNotNull( e );
        assertEquals( e.getOperator(), op );
        assertNotNull( e.getTypeId() );
    }


    /**
     * @param string
     * @param op_prefixIncr
     * @throws ParserException
     */
    protected void validateSimpleUnaryExpressionC(String code , int operator ) throws ParserException {
        IASTUnaryExpression e = (IASTUnaryExpression) getExpressionFromStatementInCode( code, ParserLanguage.C );
        assertNotNull( e );
        assertEquals( e.getOperator(), operator );
        IASTIdExpression x = (IASTIdExpression) e.getOperand();
        assertEquals( x.getName().toString(), "x"); //$NON-NLS-1$
    }


    /**
     * @param code 
     * @throws ParserException
     */
    protected void validateConditionalExpressionC(String code ) throws ParserException {
        IASTConditionalExpression e = (IASTConditionalExpression) getExpressionFromStatementInCode( code , ParserLanguage.C );
        assertNotNull( e );
        IASTIdExpression x = (IASTIdExpression) e.getLogicalConditionExpression();
        assertEquals( x.getName().toString(), "x" ); //$NON-NLS-1$
        IASTIdExpression y = (IASTIdExpression) e.getPositiveResultExpression();
        assertEquals( y.getName().toString(), "y"); //$NON-NLS-1$
        IASTIdExpression x2 = (IASTIdExpression) e.getNegativeResultExpression();
        assertEquals( x.getName().toString(), x2.getName().toString() );
    }


    /**
     * @param operand
     * @throws ParserException
     */
    protected void validateSimpleBinaryExpressionC( String code, int operand) throws ParserException {
        IASTBinaryExpression e = (IASTBinaryExpression) getExpressionFromStatementInCode( code, ParserLanguage.C ); //$NON-NLS-1$
        assertNotNull( e );
        assertEquals( e.getOperator(), operand );
        IASTIdExpression x = (IASTIdExpression) e.getOperand1();
        assertEquals( x.getName().toString(), "x"); //$NON-NLS-1$
        IASTIdExpression y = (IASTIdExpression) e.getOperand2();
        assertEquals( y.getName().toString(), "y"); //$NON-NLS-1$
    }


    protected IASTExpression getExpressionFromStatementInCode( String code, ParserLanguage language  ) throws ParserException
    {
        StringBuffer buffer = new StringBuffer( "void f() { "); //$NON-NLS-1$
        buffer.append( "int x, y;\n"); //$NON-NLS-1$
        buffer.append( code );
        buffer.append( ";\n}"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse( buffer.toString(), language );
        IASTFunctionDefinition f = (IASTFunctionDefinition) tu.getDeclarations().get(0);
        IASTCompoundStatement cs = (IASTCompoundStatement) f.getBody();
        IASTExpressionStatement s = (IASTExpressionStatement) cs.getStatements().get( 1 );
        return s.getExpression();
    }
}
