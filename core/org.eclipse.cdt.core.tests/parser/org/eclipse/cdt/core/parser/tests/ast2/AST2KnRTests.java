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

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.gnu.c.ICASTKnRFunctionDeclarator;
import org.eclipse.cdt.core.parser.ParserLanguage;

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
    	
    }

    public void testKRCProblem1() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int f(x) char\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true );
    	
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
    	
    }
    
    public void testKRCProblem2() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int i=0;\n" ); //$NON-NLS-1$
    	buffer.append( "int f(x) i++;\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true );
    	
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
    	
    }

    public void testKRCProblem3() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int f(x) char y;\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true );

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

    }

    public void testKRCProblem4() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int f(x,y,z) char x,y,z; int a;\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true );

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
    }

    public void testKRCProblem5() throws Exception {
    	StringBuffer buffer = new StringBuffer(); //$NON-NLS-1$
    	buffer.append( "int f(x) char x,a;\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.C, true );

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
    }

}
