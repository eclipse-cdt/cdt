/**********************************************************************
 * Copyright (c) 2005 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTArraySubscriptExpression;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.c.ICASTTypedefNameSpecifier;
import org.eclipse.cdt.core.dom.ast.c.ICBasicType;
import org.eclipse.cdt.core.dom.ast.c.ICScope;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.c.CParameter;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;
import org.eclipse.cdt.internal.core.dom.parser.c.ICBinding;

/**
 * @author dsteffle
 */
public class AST2KnRTests extends AST2BaseTest {
    public void testSimpleKRCTest1() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int f(char x);\n" ); //$NON-NLS-1$
    	buffer.append( "int f(x) char x;\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true );
    	
    	IASTSimpleDeclaration f1 = (IASTSimpleDeclaration)tu.getDeclarations()[0];
    	IASTFunctionDefinition f2 = (IASTFunctionDefinition)tu.getDeclarations()[1];
    	
    	assertTrue( f1.getDeclarators()[0] instanceof IASTStandardFunctionDeclarator );
    	
    	IParameter x4 = (IParameter)((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f2.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName().resolveBinding();
    	IParameter x3 = (IParameter)((IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)f2.getDeclarator()).getParameterDeclarations()[0]).getDeclarators()[0].getName().resolveBinding();
    	IParameter x2 = (IParameter)((ICASTKnRFunctionDeclarator)f2.getDeclarator()).getParameterNames()[0].resolveBinding();
    	IParameter x1 = (IParameter)((IASTStandardFunctionDeclarator)f1.getDeclarators()[0]).getParameters()[0].getDeclarator().getName().resolveBinding();
	    	
    	assertNotNull( x1 );
    	assertNotNull( x2 );
    	assertNotNull( x3 );
    	assertNotNull( x4 );
    	assertEquals( x1, x2 );
    	assertEquals( x2, x3 );
    	assertEquals( x3, x4 );
    	
		// test tu.getDeclarations(IBinding)
		IASTName[] decls = tu.getDeclarations(x1);
		assertEquals( decls.length, 2 );
		assertEquals( decls[0], ((IASTStandardFunctionDeclarator)f1.getDeclarators()[0]).getParameters()[0].getDeclarator().getName() );
		assertEquals( decls[1], ((IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)f2.getDeclarator()).getParameterDeclarations()[0]).getDeclarators()[0].getName() );

		assertNotNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("f").toCharArray()) ); //$NON-NLS-1$
		assertNotNull( ((ICScope)((IASTCompoundStatement)f2.getBody()).getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("x").toCharArray()) ); //$NON-NLS-1$
		CVisitor.clearBindings(tu);
		assertNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("f").toCharArray()) ); //$NON-NLS-1$
		assertNull( ((ICScope)((IASTCompoundStatement)f2.getBody()).getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("x").toCharArray()) ); //$NON-NLS-1$
    	
    }

    public void testSimpleKRCTest2() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int f();\n" ); //$NON-NLS-1$
    	buffer.append( "int f(x) char x;\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true );
    	
    	IASTSimpleDeclaration f1 = (IASTSimpleDeclaration)tu.getDeclarations()[0];
    	IASTFunctionDefinition f2 = (IASTFunctionDefinition)tu.getDeclarations()[1];
    	
    	assertTrue( f1.getDeclarators()[0] instanceof IASTStandardFunctionDeclarator );
    	
    	IParameter x4 = (IParameter)((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f2.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName().resolveBinding();
    	IParameter x3 = (IParameter)((IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)f2.getDeclarator()).getParameterDeclarations()[0]).getDeclarators()[0].getName().resolveBinding();
    	IParameter x2 = (IParameter)((ICASTKnRFunctionDeclarator)f2.getDeclarator()).getParameterNames()[0].resolveBinding();
	    	
    	assertNotNull( x2 );
    	assertNotNull( x3 );
    	assertNotNull( x4 );
    	assertEquals( x2, x3 );
    	assertEquals( x3, x4 );
    	
		// test tu.getDeclarations(IBinding)
		IASTName[] decls = tu.getDeclarations(x2); 
		assertEquals( decls.length, 1 );
		assertEquals( decls[0], ((IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)f2.getDeclarator()).getParameterDeclarations()[0]).getDeclarators()[0].getName() );

		assertNotNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("f").toCharArray()) ); //$NON-NLS-1$
		assertNotNull( ((ICScope)((IASTCompoundStatement)f2.getBody()).getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("x").toCharArray()) ); //$NON-NLS-1$
		CVisitor.clearBindings(tu);
		assertNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("f").toCharArray()) ); //$NON-NLS-1$
		assertNull( ((ICScope)((IASTCompoundStatement)f2.getBody()).getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("x").toCharArray()) ); //$NON-NLS-1$

    }

    public void testSimpleKRCTest3() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int const *f();\n" ); //$NON-NLS-1$
    	buffer.append( "int const *f(x) char x;\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true );
    	
    	IASTSimpleDeclaration f1 = (IASTSimpleDeclaration)tu.getDeclarations()[0];
    	IASTFunctionDefinition f2 = (IASTFunctionDefinition)tu.getDeclarations()[1];
    	
    	assertTrue( f1.getDeclarators()[0] instanceof IASTStandardFunctionDeclarator );
    	
    	IParameter x4 = (IParameter)((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f2.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName().resolveBinding();
    	IParameter x3 = (IParameter)((IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)f2.getDeclarator()).getParameterDeclarations()[0]).getDeclarators()[0].getName().resolveBinding();
    	IParameter x2 = (IParameter)((ICASTKnRFunctionDeclarator)f2.getDeclarator()).getParameterNames()[0].resolveBinding();
	    	
    	assertNotNull( x2 );
    	assertNotNull( x3 );
    	assertNotNull( x4 );
    	assertEquals( x2, x3 );
    	assertEquals( x3, x4 );
    }
    
    public void testKRC_1() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int isroot (x, y) /* comment */ \n" ); //$NON-NLS-1$
    	buffer.append( "int x;\n" ); //$NON-NLS-1$
    	buffer.append( "int y;\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true );
    	
    	IASTFunctionDefinition isroot_def = (IASTFunctionDefinition)tu.getDeclarations()[0];
    	
    	IASTName ret_x = ((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)isroot_def.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName();
    	IASTDeclarator isroot_decltor = isroot_def.getDeclarator();
    	
    	assertTrue( isroot_decltor instanceof ICASTKnRFunctionDeclarator );
    	IASTDeclarator x1 = ((IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)isroot_decltor).getParameterDeclarations()[0]).getDeclarators()[0];
    	IASTDeclarator y1 = ((IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)isroot_decltor).getParameterDeclarations()[1]).getDeclarators()[0];
    	
    	IParameter x_parm = (IParameter)x1.getName().resolveBinding();
    	IParameter y_parm = (IParameter)y1.getName().resolveBinding();
    	assertNotNull( x_parm );
    	assertNotNull( y_parm );

    	IASTDeclarator x2 = ((IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)isroot_decltor).getParameterDeclarations()[0]).getDeclarators()[0];
    	IASTDeclarator y2 = ((IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)isroot_decltor).getParameterDeclarations()[1]).getDeclarators()[0];
    	
    	IParameter x_parm2 = (IParameter)x2.getName().resolveBinding();
    	IParameter y_parm2 = (IParameter)y2.getName().resolveBinding();
    	
    	assertNotNull( x_parm2 );
    	assertNotNull( y_parm2 );
    	assertNotNull( ret_x.resolveBinding() );    	
    	
    	assertEquals( x_parm, x_parm2 );
    	assertEquals( y_parm, y_parm2 );
    	assertEquals( ret_x.resolveBinding(), x_parm );

		// test tu.getDeclarations(IBinding)
		IASTName[] decls = tu.getDeclarations(ret_x.resolveBinding()); 
		assertEquals( decls.length, 1 );
		assertEquals( decls[0], x1.getName() );

		assertNotNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("isroot").toCharArray()) ); //$NON-NLS-1$
		assertNotNull( ((ICScope)((IASTCompoundStatement)isroot_def.getBody()).getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("x").toCharArray()) ); //$NON-NLS-1$
		assertNotNull( ((ICScope)((IASTCompoundStatement)isroot_def.getBody()).getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("y").toCharArray()) ); //$NON-NLS-1$
		CVisitor.clearBindings(tu);
		assertNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("isroot").toCharArray()) ); //$NON-NLS-1$
		assertNull( ((ICScope)((IASTCompoundStatement)isroot_def.getBody()).getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("x").toCharArray()) ); //$NON-NLS-1$
		assertNull( ((ICScope)((IASTCompoundStatement)isroot_def.getBody()).getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("y").toCharArray()) ); //$NON-NLS-1$

    }

    public void testKRCWithTypes() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "typedef char c;\n" ); //$NON-NLS-1$
    	buffer.append( "int isroot (c);\n" ); //$NON-NLS-1$
    	buffer.append( "int isroot (x) \n" ); //$NON-NLS-1$
    	buffer.append( "c x;\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true );
    	
    	IASTSimpleDeclaration c_decl = (IASTSimpleDeclaration)tu.getDeclarations()[0];
    	IASTSimpleDeclaration isroot_decl = (IASTSimpleDeclaration)tu.getDeclarations()[1];
    	IASTFunctionDefinition isroot_def = (IASTFunctionDefinition)tu.getDeclarations()[2];

    	IASTName x0 = ((IASTStandardFunctionDeclarator)isroot_decl.getDeclarators()[0]).getParameters()[0].getDeclarator().getName();
    	IASTName x1 = ((ICASTKnRFunctionDeclarator)isroot_def.getDeclarator()).getParameterNames()[0];
    	IASTName x2 = ((IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)isroot_def.getDeclarator()).getParameterDeclarations()[0]).getDeclarators()[0].getName();
    	IASTName x3 = ((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)isroot_def.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName();
    	
    	IParameter x1_var = (IParameter)x1.resolveBinding();
    	IParameter x2_var = (IParameter)x2.resolveBinding();
    	IParameter x3_var = (IParameter)x3.resolveBinding();
    	
    	assertNotNull(x1_var);
    	assertNotNull(x2_var);
    	assertNotNull(x3_var);
    	assertEquals(x1_var, x2_var);
    	assertEquals(x2_var, x3_var);
    	
    	IASTName c1 = c_decl.getDeclarators()[0].getName();
    	IASTName c2 = ((IASTNamedTypeSpecifier)((IASTStandardFunctionDeclarator)isroot_decl.getDeclarators()[0]).getParameters()[0].getDeclSpecifier()).getName();
    	IASTName c3 = ((IASTNamedTypeSpecifier)((IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)isroot_def.getDeclarator()).getParameterDeclarations()[0]).getDeclSpecifier()).getName();
    	
    	ITypedef c1_t = (ITypedef)c1.resolveBinding();
    	ITypedef c2_t = (ITypedef)c2.resolveBinding();
    	ITypedef c3_t = (ITypedef)c3.resolveBinding();
    	
    	assertNotNull(c1_t);
    	assertNotNull(c2_t);
    	assertNotNull(c3_t);
    	assertEquals(c1_t, c2_t);
    	assertEquals(c2_t, c3_t);
    	assertTrue(c1_t.getType() instanceof IBasicType);
    	assertEquals(((IBasicType)c1_t.getType()).getType(), IBasicType.t_char);
    	
		// test tu.getDeclarations(IBinding)
		IASTName[] decls = tu.getDeclarations(x3.resolveBinding()); 
		assertEquals( decls.length, 2 );
		assertEquals( decls[0], x0 );
		assertEquals( decls[1], x2 );
		
		assertNotNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("c").toCharArray()) ); //$NON-NLS-1$
		assertNotNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("isroot").toCharArray()) ); //$NON-NLS-1$
		assertNotNull( ((ICScope)((IASTCompoundStatement)isroot_def.getBody()).getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("x").toCharArray()) ); //$NON-NLS-1$
		CVisitor.clearBindings(tu);
		assertNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("c").toCharArray()) ); //$NON-NLS-1$
		assertNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("isroot").toCharArray()) ); //$NON-NLS-1$
		assertNull( ((ICScope)((IASTCompoundStatement)isroot_def.getBody()).getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("x").toCharArray()) ); //$NON-NLS-1$
    }

    public void testKRCProblem1() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int f(x) char\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true, false );
    	
    	IASTFunctionDefinition f = (IASTFunctionDefinition)tu.getDeclarations()[0];
    	assertTrue(f.getDeclarator() instanceof ICASTKnRFunctionDeclarator);
    	ICASTKnRFunctionDeclarator f_kr = (ICASTKnRFunctionDeclarator)f.getDeclarator();
    	assertEquals(f_kr.getName().toString(), "f"); //$NON-NLS-1$
    	assertEquals(f_kr.getParameterNames()[0].toString(), "x"); //$NON-NLS-1$
    	assertTrue(f_kr.getParameterDeclarations()[0] instanceof IASTProblemDeclaration);
    	assertTrue(f.getBody() instanceof IASTCompoundStatement);
    	assertTrue(((IASTCompoundStatement)f.getBody()).getStatements()[0] instanceof IASTReturnStatement);
    	assertTrue(((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue() instanceof IASTBinaryExpression);
    	assertTrue(((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1() instanceof IASTIdExpression);
    	assertEquals(((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName().toString(), "x"); //$NON-NLS-1$
    	assertTrue(((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand2() instanceof IASTLiteralExpression);
    	assertEquals(((IASTLiteralExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand2()).toString(), "0"); //$NON-NLS-1$
    	
    	// TODO problem bindings, right now both bindings for x are null, similarly for the below KRCProblem tests
//    	f_kr.getParameterNames()[0].resolveBinding();
//    	((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName().resolveBinding();
    	
		// test tu.getDeclarations(IBinding)
		IASTName[] decls = tu.getDeclarations(((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName().resolveBinding()); 
		assertEquals( decls.length, 0 );
    	
    }
    
    public void testKRCProblem2() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int i=0;\n" ); //$NON-NLS-1$
    	buffer.append( "int f(x) i++;\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true, false );
    	
    	IASTFunctionDefinition f = (IASTFunctionDefinition)tu.getDeclarations()[1];
    	assertTrue(f.getDeclarator() instanceof ICASTKnRFunctionDeclarator);
    	ICASTKnRFunctionDeclarator f_kr = (ICASTKnRFunctionDeclarator)f.getDeclarator();
    	assertEquals(f_kr.getName().toString(), "f"); //$NON-NLS-1$
    	assertEquals(f_kr.getParameterNames()[0].toString(), "x"); //$NON-NLS-1$
    	assertTrue(f_kr.getParameterDeclarations()[0] instanceof IASTProblemDeclaration);
    	assertTrue(f.getBody() instanceof IASTCompoundStatement);
    	assertTrue(((IASTCompoundStatement)f.getBody()).getStatements()[0] instanceof IASTReturnStatement);
    	assertTrue(((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue() instanceof IASTBinaryExpression);
    	assertTrue(((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1() instanceof IASTIdExpression);
    	assertEquals(((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName().toString(), "x"); //$NON-NLS-1$
    	assertTrue(((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand2() instanceof IASTLiteralExpression);
    	assertEquals(((IASTLiteralExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand2()).toString(), "0"); //$NON-NLS-1$

		// test tu.getDeclarations(IBinding)
		IASTName[] decls = tu.getDeclarations(((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName().resolveBinding()); 
		assertEquals( decls.length, 0 );
		
		assertNotNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("i").toCharArray()) ); //$NON-NLS-1$
		assertNotNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("f").toCharArray()) ); //$NON-NLS-1$
		CVisitor.clearBindings(tu);
		assertNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("i").toCharArray()) ); //$NON-NLS-1$
		assertNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("f").toCharArray()) ); //$NON-NLS-1$
	}

    public void testKRCProblem3() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int f(x) char y;\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true, false );

    	IASTFunctionDefinition f = (IASTFunctionDefinition)tu.getDeclarations()[0];
    	assertTrue(f.getDeclarator() instanceof ICASTKnRFunctionDeclarator);
    	ICASTKnRFunctionDeclarator f_kr = (ICASTKnRFunctionDeclarator)f.getDeclarator();
    	assertEquals(f_kr.getName().toString(), "f"); //$NON-NLS-1$
    	assertEquals(f_kr.getParameterNames()[0].toString(), "x"); //$NON-NLS-1$
    	assertTrue(f_kr.getParameterDeclarations()[0] instanceof IASTProblemDeclaration);
    	assertTrue(f.getBody() instanceof IASTCompoundStatement);
    	assertTrue(((IASTCompoundStatement)f.getBody()).getStatements()[0] instanceof IASTReturnStatement);
    	assertTrue(((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue() instanceof IASTBinaryExpression);
    	assertTrue(((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1() instanceof IASTIdExpression);
    	assertEquals(((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName().toString(), "x"); //$NON-NLS-1$
    	assertTrue(((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand2() instanceof IASTLiteralExpression);
    	assertEquals(((IASTLiteralExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand2()).toString(), "0"); //$NON-NLS-1$

		// test tu.getDeclarations(IBinding)
		IASTName[] decls = tu.getDeclarations(((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName().resolveBinding()); 
		assertEquals( decls.length, 0 );
    }

    public void testKRCProblem4() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int f(x,y,z) char x,y,z; int a;\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true, false );

    	IASTFunctionDefinition f = (IASTFunctionDefinition)tu.getDeclarations()[0];
    	assertTrue(f.getDeclarator() instanceof ICASTKnRFunctionDeclarator);
    	ICASTKnRFunctionDeclarator f_kr = (ICASTKnRFunctionDeclarator)f.getDeclarator();
    	assertEquals(f_kr.getName().toString(), "f"); //$NON-NLS-1$
    	assertEquals(f_kr.getParameterNames()[0].toString(), "x"); //$NON-NLS-1$
    	assertTrue(f_kr.getParameterDeclarations()[0] instanceof IASTSimpleDeclaration);
    	assertTrue(f_kr.getParameterDeclarations()[1] instanceof IASTProblemDeclaration);
    	assertTrue(f.getBody() instanceof IASTCompoundStatement);
    	assertTrue(((IASTCompoundStatement)f.getBody()).getStatements()[0] instanceof IASTReturnStatement);
    	assertTrue(((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue() instanceof IASTBinaryExpression);
    	assertTrue(((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1() instanceof IASTIdExpression);
    	assertEquals(((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName().toString(), "x"); //$NON-NLS-1$
    	assertTrue(((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand2() instanceof IASTLiteralExpression);
    	assertEquals(((IASTLiteralExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand2()).toString(), "0"); //$NON-NLS-1$

    	// bindings should still be ok
    	IASTName x1 = f_kr.getParameterNames()[0];
    	IASTName y1 = f_kr.getParameterNames()[1];
    	IASTName z1 = f_kr.getParameterNames()[2];
    	IASTName x2 = ((IASTSimpleDeclaration)f_kr.getParameterDeclarations()[0]).getDeclarators()[0].getName();
    	IASTName y2 = ((IASTSimpleDeclaration)f_kr.getParameterDeclarations()[0]).getDeclarators()[1].getName();
    	IASTName z2 = ((IASTSimpleDeclaration)f_kr.getParameterDeclarations()[0]).getDeclarators()[2].getName();
    	IASTName x3 = ((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName();
    	
    	IParameter x1_parm = (IParameter)x1.resolveBinding();
    	IParameter x2_parm = (IParameter)x2.resolveBinding();
    	IParameter x3_parm = (IParameter)x3.resolveBinding();
    	IParameter y1_parm = (IParameter)y1.resolveBinding();
    	IParameter y2_parm = (IParameter)y2.resolveBinding();
    	IParameter z1_parm = (IParameter)z1.resolveBinding();
    	IParameter z2_parm = (IParameter)z2.resolveBinding();
    	
    	assertEquals(x1_parm, x2_parm);
    	assertEquals(x2_parm, x3_parm);
    	assertEquals(y1_parm, y2_parm);
    	assertEquals(z1_parm, z2_parm);
    	
		// test tu.getDeclarations(IBinding)
		IASTName[] decls = tu.getDeclarations(((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName().resolveBinding()); 
		assertEquals( decls.length, 1 );
		assertEquals( decls[0], x2 );
		
    }

    public void testKRCProblem5() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int f(x) char x,a;\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true, false );

    	IASTFunctionDefinition f = (IASTFunctionDefinition)tu.getDeclarations()[0];
    	assertTrue(f.getDeclarator() instanceof ICASTKnRFunctionDeclarator);
    	ICASTKnRFunctionDeclarator f_kr = (ICASTKnRFunctionDeclarator)f.getDeclarator();
    	assertEquals(f_kr.getName().toString(), "f"); //$NON-NLS-1$
    	assertEquals(f_kr.getParameterNames()[0].toString(), "x"); //$NON-NLS-1$
    	assertTrue(f_kr.getParameterDeclarations()[0] instanceof IASTProblemDeclaration);
    	assertTrue(f.getBody() instanceof IASTCompoundStatement);
    	assertTrue(((IASTCompoundStatement)f.getBody()).getStatements()[0] instanceof IASTReturnStatement);
    	assertTrue(((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue() instanceof IASTBinaryExpression);
    	assertTrue(((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1() instanceof IASTIdExpression);
    	assertEquals(((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName().toString(), "x"); //$NON-NLS-1$
    	assertTrue(((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand2() instanceof IASTLiteralExpression);
    	assertEquals(((IASTLiteralExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand2()).toString(), "0"); //$NON-NLS-1$
    	
		// test tu.getDeclarations(IBinding)
		IASTName[] decls = tu.getDeclarations(((IASTIdExpression)((IASTBinaryExpression)((IASTReturnStatement)((IASTCompoundStatement)f.getBody()).getStatements()[0]).getReturnValue()).getOperand1()).getName().resolveBinding()); 
		assertEquals( decls.length, 0 );    	
    }
    
    
    public void testKRC_monop_cards1() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "#ifdef __STDC__\n" ); //$NON-NLS-1$
    	buffer.append( "#define __P(x) x\n" ); //$NON-NLS-1$
    	buffer.append( "#else\n" ); //$NON-NLS-1$
    	buffer.append( "#define __P(x) ()\n" ); //$NON-NLS-1$
    	buffer.append( "#endif\n" ); //$NON-NLS-1$
    	buffer.append( "struct A_struct {\n" ); //$NON-NLS-1$
    	buffer.append( "int a;\n" ); //$NON-NLS-1$
    	buffer.append( "long *c;\n" ); //$NON-NLS-1$
    	buffer.append( "};\n" ); //$NON-NLS-1$
    	buffer.append( "typedef struct A_struct A;\n" ); //$NON-NLS-1$
    	buffer.append( "static void f __P((A *));\n" ); //$NON-NLS-1$
    	buffer.append( "static void\n" ); //$NON-NLS-1$
    	buffer.append( "f(x)\n" ); //$NON-NLS-1$
    	buffer.append( "A *x; {\n" ); //$NON-NLS-1$
    	buffer.append( "x->a = 0;\n" ); //$NON-NLS-1$
    	buffer.append( "x->c[1]=x->c[2];\n" ); //$NON-NLS-1$
    	buffer.append( "}\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true );
    	
    	IASTSimpleDeclaration A_struct = (IASTSimpleDeclaration)tu.getDeclarations()[0];
    	IASTSimpleDeclaration A = (IASTSimpleDeclaration)tu.getDeclarations()[1];
    	IASTSimpleDeclaration f_decl = (IASTSimpleDeclaration)tu.getDeclarations()[2];
    	IASTFunctionDefinition f_def = (IASTFunctionDefinition)tu.getDeclarations()[3];
    	
    	// check A_struct
    	assertTrue( A_struct.getDeclSpecifier() instanceof IASTCompositeTypeSpecifier );
    	assertEquals( A_struct.getDeclarators().length, 0 );
    	IASTName A_struct_name1 = ((IASTCompositeTypeSpecifier)A_struct.getDeclSpecifier()).getName();
    	assertEquals( A_struct_name1.toString(), "A_struct" ); //$NON-NLS-1$
    	ICompositeType A_struct_type1 = (ICompositeType)A_struct_name1.resolveBinding();
    	assertEquals( ((ICBinding)A_struct_type1).getPhysicalNode(), A_struct.getDeclSpecifier() );
    	IField[] fields = A_struct_type1.getFields();
    	IField a1 = fields[0];
    	IField c1 = fields[1];
    	assertEquals( a1.getName().toString(), "a" ); //$NON-NLS-1$
    	assertEquals( c1.getName().toString(), "c" ); //$NON-NLS-1$
    	IBasicType a1_t = (IBasicType)a1.getType();
    	IPointerType c1_t = (IPointerType)c1.getType();
    	assertEquals( a1_t.getType(), IBasicType.t_int );
    	assertTrue( c1_t.getType() instanceof IBasicType );
    	assertTrue( ((IBasicType)c1_t.getType()).isLong() );
    	
    	// check A
    	IASTName A_name1 = A.getDeclarators()[0].getName();
    	assertEquals( A_name1.toString(), "A" ); //$NON-NLS-1$
    	ITypedef A_var1 = (ITypedef)A_name1.resolveBinding();
    	assertTrue( A.getDeclSpecifier() instanceof IASTElaboratedTypeSpecifier );
    	IASTName A_struct_name_2 = ((IASTElaboratedTypeSpecifier)A.getDeclSpecifier()).getName();
    	assertEquals( A_struct_name_2.toString(), "A_struct" ); //$NON-NLS-1$
    	assertEquals( ((IASTElaboratedTypeSpecifier)A.getDeclSpecifier()).getStorageClass(), IASTDeclSpecifier.sc_typedef );
    	ICompositeType A_struct_type2 = (ICompositeType)A_struct_name_2.resolveBinding();
    	assertEquals( A_struct_type2, A_struct_type1 );
    	
    	// check f_decl
    	assertTrue( f_decl.getDeclarators()[0] instanceof IASTStandardFunctionDeclarator );
    	IASTStandardFunctionDeclarator f_decltor1 = ((IASTStandardFunctionDeclarator)f_decl.getDeclarators()[0]); 
    	IASTName f_name1 = f_decltor1.getName();
    	IFunction f_fun1 = (IFunction)f_name1.resolveBinding();
    	assertEquals( f_name1.toString(), "f" ); //$NON-NLS-1$
    	assertEquals( f_decltor1.getParameters().length, 1 );
    	IASTName x0 = f_decltor1.getParameters()[0].getDeclarator().getName();
    	IASTName A_name2 = ((ICASTTypedefNameSpecifier)f_decltor1.getParameters()[0].getDeclSpecifier()).getName();
    	assertEquals( A_name2.toString(), "A" ); //$NON-NLS-1$
    	ITypedef A_var2 = (ITypedef)A_name2.resolveBinding();
    	assertEquals( A_var1, A_var2 );
    	
    	// check f_def
    	assertTrue( f_def.getDeclarator() instanceof ICASTKnRFunctionDeclarator );
    	
    	ICASTKnRFunctionDeclarator f_decltor2 = (ICASTKnRFunctionDeclarator)f_def.getDeclarator();
    	assertEquals( f_decltor2.getName().toString(), "f" ); //$NON-NLS-1$
    	IFunction f_fun2 = (IFunction)f_decltor2.getName().resolveBinding();
    	assertEquals( f_fun1, f_fun2 );
    	ICBasicType f_ret_t = (ICBasicType)f_fun2.getType().getReturnType();
    	assertEquals( f_ret_t.getType(), IBasicType.t_void );
    	IASTName x1 = f_decltor2.getParameterNames()[0];
    	assertEquals( x1.toString(), "x" ); //$NON-NLS-1$
    	IASTSimpleDeclaration x_parm = (IASTSimpleDeclaration)f_decltor2.getParameterDeclarations()[0];
    	IASTName x2 = x_parm.getDeclarators()[0].getName();
    	assertEquals( x2.toString(), "x" ); //$NON-NLS-1$
    	assertEquals( x_parm.getDeclarators()[0].getPointerOperators().length, 1 );
    	IASTName A3 = ((IASTNamedTypeSpecifier)x_parm.getDeclSpecifier()).getName();
    	ITypedef A_var3 = (ITypedef)A3.resolveBinding();
    	assertEquals( A_var2, A_var3 );
    	assertEquals( A3.toString(), "A" ); //$NON-NLS-1$;
    	assertEquals( x1.resolveBinding(), x2.resolveBinding() );
    	
    	// check f_def body
    	assertTrue( f_def.getBody() instanceof IASTCompoundStatement );
    	IASTCompoundStatement f_def_body = (IASTCompoundStatement)f_def.getBody();
    	IASTExpressionStatement stmt1 = (IASTExpressionStatement)f_def_body.getStatements()[0];
    	IASTExpressionStatement stmt2 = (IASTExpressionStatement)f_def_body.getStatements()[1];
    	IASTName a2 = ((IASTFieldReference)((IASTBinaryExpression)stmt1.getExpression()).getOperand1()).getFieldName();
    	assertEquals( ((IASTName)((ICBinding)a1).getPhysicalNode()).resolveBinding(), a2.resolveBinding() );
    	IASTName x3 = ((IASTIdExpression)((IASTFieldReference)((IASTBinaryExpression)stmt1.getExpression()).getOperand1()).getFieldOwner()).getName();
    	assertEquals( x2.resolveBinding(), x3.resolveBinding() );
    	assertEquals( ((IASTBinaryExpression)stmt1.getExpression()).getOperand2().toString(), "0" ); //$NON-NLS-1$
    	assertTrue( ((IASTBinaryExpression)stmt2.getExpression()).getOperand1() instanceof  IASTArraySubscriptExpression );
    	assertTrue( ((IASTBinaryExpression)stmt2.getExpression()).getOperand2() instanceof  IASTArraySubscriptExpression );
    	IASTName c2 = ((IASTFieldReference)((IASTArraySubscriptExpression)((IASTBinaryExpression)stmt2.getExpression()).getOperand1()).getArrayExpression()).getFieldName();
    	IASTName x4 = ((IASTIdExpression)((IASTFieldReference)((IASTArraySubscriptExpression)((IASTBinaryExpression)stmt2.getExpression()).getOperand1()).getArrayExpression()).getFieldOwner()).getName();
    	IASTName c3 = ((IASTFieldReference)((IASTArraySubscriptExpression)((IASTBinaryExpression)stmt2.getExpression()).getOperand2()).getArrayExpression()).getFieldName();
    	IASTName x5 = ((IASTIdExpression)((IASTFieldReference)((IASTArraySubscriptExpression)((IASTBinaryExpression)stmt2.getExpression()).getOperand1()).getArrayExpression()).getFieldOwner()).getName();
    	assertEquals( ((IASTName)((ICBinding)c1).getPhysicalNode()).resolveBinding(), c2.resolveBinding() );
    	assertEquals( ((IASTName)((ICBinding)c1).getPhysicalNode()).resolveBinding(), c3.resolveBinding() );
    	assertEquals( x3.resolveBinding(), x4.resolveBinding() );
    	assertEquals( x4.resolveBinding(), x5.resolveBinding() );
    	
    	// test CFunction.getParameters size
    	IParameter[] f1_parms = f_fun1.getParameters();
    	assertEquals( f1_parms.length, 1 );

		// test tu.getDeclarations(IBinding)
		IASTName[] decls = tu.getDeclarations(x2.resolveBinding()); 
		assertEquals( decls.length, 2 );
		assertEquals( decls[0], x0 );
		assertEquals( decls[1], x2 );
		
		assertNotNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_TAG, new String("A_struct").toCharArray()) ); //$NON-NLS-1$
		assertNotNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("A").toCharArray()) ); //$NON-NLS-1$
		assertNotNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("f").toCharArray()) ); //$NON-NLS-1$
		assertNotNull( ((ICScope)((IASTCompoundStatement)f_def.getBody()).getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("x").toCharArray()) ); //$NON-NLS-1$
		CVisitor.clearBindings(tu);
		assertNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_TAG, new String("A_struct").toCharArray()) ); //$NON-NLS-1$
		assertNull( ((ICScope)tu.getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("f").toCharArray()) ); //$NON-NLS-1$
		assertNull( ((ICScope)((IASTCompoundStatement)f_def.getBody()).getScope()).getBinding(ICScope.NAMESPACE_TYPE_OTHER, new String("x").toCharArray()) ); //$NON-NLS-1$
		
    }
    
    public void testKRC_monop_cards2() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int\n" ); //$NON-NLS-1$
    	buffer.append( "getinp(prompt, list)\n" ); //$NON-NLS-1$
    	buffer.append( "        const char *prompt, *const list[];\n" ); //$NON-NLS-1$
    	buffer.append( "{\n	*list[1] = 'a';\n}\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true );
    	
    	IASTFunctionDefinition getinp = (IASTFunctionDefinition)tu.getDeclarations()[0];
    	
    	IASTName prompt1 = ((ICASTKnRFunctionDeclarator)getinp.getDeclarator()).getParameterNames()[0];
    	IASTName list1 = ((ICASTKnRFunctionDeclarator)getinp.getDeclarator()).getParameterNames()[1];
    	IASTName prompt2 = ((IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)getinp.getDeclarator()).getParameterDeclarations()[0]).getDeclarators()[0].getName();
    	IASTName list2 = ((IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)getinp.getDeclarator()).getParameterDeclarations()[0]).getDeclarators()[1].getName();
    	IASTName list3 = ((IASTIdExpression)((IASTArraySubscriptExpression)((IASTUnaryExpression)((IASTBinaryExpression)((IASTExpressionStatement)((IASTCompoundStatement)getinp.getBody()).getStatements()[0]).getExpression()).getOperand1()).getOperand()).getArrayExpression()).getName();
    	
    	assertEquals( prompt1.resolveBinding(), prompt2.resolveBinding() );
    	assertEquals( list1.resolveBinding(), list2.resolveBinding() );
    	assertEquals( list2.resolveBinding(), list3.resolveBinding() );
    	
    	IASTSimpleDeclaration parm_decl = (IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)getinp.getDeclarator()).getParameterDeclarations()[0];
    	assertTrue( ((IASTSimpleDeclSpecifier)parm_decl.getDeclSpecifier()).isConst() );
    	assertEquals( ((IASTSimpleDeclSpecifier)parm_decl.getDeclSpecifier()).getType(), IASTSimpleDeclSpecifier.t_char );
    	IASTDeclarator prompt = parm_decl.getDeclarators()[0];
    	IASTArrayDeclarator list = (IASTArrayDeclarator)parm_decl.getDeclarators()[1];
    	assertEquals( prompt.getName().toString(), "prompt" ); //$NON-NLS-1$
    	assertEquals( prompt.getPointerOperators().length, 1 );
    	assertEquals( list.getName().toString(), "list" ); //$NON-NLS-1$
    	assertEquals( list.getArrayModifiers().length, 1 );
    	assertNull( list.getArrayModifiers()[0].getConstantExpression() );
    	assertEquals( list.getPointerOperators().length, 1 );
    	
		// test tu.getDeclarations(IBinding)
		IASTName[] decls = tu.getDeclarations(list3.resolveBinding()); 
		assertEquals( decls.length, 1 );
		assertEquals( decls[0], list2 );
    	
		decls = tu.getDeclarations(prompt1.resolveBinding()); 
		assertEquals( decls.length, 1 );
		assertEquals( decls[0], prompt2 );
    }
    
    public void testKRC_getParametersOrder() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int f(a, b) int b,a;{}\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true );
    	
    	IASTFunctionDefinition f = (IASTFunctionDefinition)tu.getDeclarations()[0];
    	ICASTKnRFunctionDeclarator f_decltor = (ICASTKnRFunctionDeclarator)f.getDeclarator();
    	IFunction f_fun = (IFunction)f_decltor.getName().resolveBinding();
    	IParameter [] f_parms = f_fun.getParameters();
    	assertEquals( f_parms.length, 2 );
    	assertEquals( ((CParameter)f_parms[0]).getName(), "a" ); //$NON-NLS-1$
    	assertEquals( ((CParameter)f_parms[1]).getName(), "b" ); //$NON-NLS-1$
    	
    }

    public void testKRC_Ethereal_1() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "struct symbol {\n" ); //$NON-NLS-1$
    	buffer.append( "int lambda;\n};\n" ); //$NON-NLS-1$
    	buffer.append( "struct lemon {\n" ); //$NON-NLS-1$
    	buffer.append( "struct symbol **symbols;\n" ); //$NON-NLS-1$
    	buffer.append( "int errorcnt;\n};\n" ); //$NON-NLS-1$
    	buffer.append( "void f(lemp)\n" ); //$NON-NLS-1$
    	buffer.append( "struct lemon *lemp;\n{\n" ); //$NON-NLS-1$
    	buffer.append( "lemp->symbols[1]->lambda = 1;\n" ); //$NON-NLS-1$
    	buffer.append( "lemp->errorcnt++;}\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true );
    	
    	IASTSimpleDeclaration symbol_decl = (IASTSimpleDeclaration)tu.getDeclarations()[0];
    	IASTSimpleDeclaration lemon_decl = (IASTSimpleDeclaration)tu.getDeclarations()[1];
    	IASTFunctionDefinition f_def = (IASTFunctionDefinition)tu.getDeclarations()[2];
    	
    	IASTName symbol_name1 = ((IASTCompositeTypeSpecifier)symbol_decl.getDeclSpecifier()).getName();
    	IASTName lambda_name1 = ((IASTSimpleDeclaration)((IASTCompositeTypeSpecifier)symbol_decl.getDeclSpecifier()).getMembers()[0]).getDeclarators()[0].getName();
    	IASTName lemon_name1 = ((IASTCompositeTypeSpecifier)lemon_decl.getDeclSpecifier()).getName();
    	IASTName symbol_name2 =  ((IASTElaboratedTypeSpecifier)((IASTSimpleDeclaration)((IASTCompositeTypeSpecifier)lemon_decl.getDeclSpecifier()).getMembers()[0]).getDeclSpecifier()).getName();
    	IASTName symbols_name1 = ((IASTSimpleDeclaration)((IASTCompositeTypeSpecifier)lemon_decl.getDeclSpecifier()).getMembers()[0]).getDeclarators()[0].getName();
    	IASTName errorcnt_name1 = ((IASTSimpleDeclaration)((IASTCompositeTypeSpecifier)lemon_decl.getDeclSpecifier()).getMembers()[1]).getDeclarators()[0].getName();
    	IASTName lemp_name1 = ((ICASTKnRFunctionDeclarator)f_def.getDeclarator()).getParameterNames()[0];
    	IASTName lemon_name2 = ((IASTElaboratedTypeSpecifier)((IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)f_def.getDeclarator()).getParameterDeclarations()[0]).getDeclSpecifier()).getName();
    	IASTName lemp_name2 = ((IASTSimpleDeclaration)((ICASTKnRFunctionDeclarator)f_def.getDeclarator()).getParameterDeclarations()[0]).getDeclarators()[0].getName();
    	IASTName lemp_name3 = ((IASTIdExpression)((IASTFieldReference)((IASTArraySubscriptExpression)((IASTFieldReference)((IASTBinaryExpression)((IASTExpressionStatement)((IASTCompoundStatement)f_def.getBody()).getStatements()[0]).getExpression()).getOperand1()).getFieldOwner()).getArrayExpression()).getFieldOwner()).getName();
    	IASTName symbols_name2 = ((IASTFieldReference)((IASTArraySubscriptExpression)((IASTFieldReference)((IASTBinaryExpression)((IASTExpressionStatement)((IASTCompoundStatement)f_def.getBody()).getStatements()[0]).getExpression()).getOperand1()).getFieldOwner()).getArrayExpression()).getFieldName();
    	IASTName lambda_name2 = ((IASTFieldReference)((IASTBinaryExpression)((IASTExpressionStatement)((IASTCompoundStatement)f_def.getBody()).getStatements()[0]).getExpression()).getOperand1()).getFieldName();
    	
    	IASTName lemp_name4 = ((IASTIdExpression)((IASTFieldReference)((IASTUnaryExpression)((IASTExpressionStatement)((IASTCompoundStatement)f_def.getBody()).getStatements()[1]).getExpression()).getOperand()).getFieldOwner()).getName();
    	IASTName errorcnt_name2 = ((IASTFieldReference)((IASTUnaryExpression)((IASTExpressionStatement)((IASTCompoundStatement)f_def.getBody()).getStatements()[1]).getExpression()).getOperand()).getFieldName();
    	
    	assertEquals( symbol_name1.resolveBinding(), symbol_name2.resolveBinding() );
    	assertEquals( lambda_name1.resolveBinding(), lambda_name2.resolveBinding() );
    	assertEquals( lemon_name1.resolveBinding(), lemon_name2.resolveBinding() );
    	assertEquals( symbols_name1.resolveBinding(), symbols_name2.resolveBinding() );
    	assertEquals( errorcnt_name1.resolveBinding(), errorcnt_name2.resolveBinding() );
    	assertEquals( lemp_name1.resolveBinding(), lemp_name2.resolveBinding() );
    	assertEquals( lemp_name2.resolveBinding(), lemp_name3.resolveBinding() );
    	assertEquals( lemp_name3.resolveBinding(), lemp_name4.resolveBinding() );
    }
    
}
