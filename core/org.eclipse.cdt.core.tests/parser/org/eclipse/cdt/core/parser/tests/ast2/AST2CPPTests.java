/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
/*
 * Created on Nov 29, 2004
 */
package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * @author aniefer
 */

public class AST2CPPTests extends AST2BaseTest {

	public void testSimpleClass() throws Exception {
		StringBuffer buffer = new StringBuffer( "class A { } a;" ); //$NON-NLS-1$

		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		IASTCompositeTypeSpecifier compTypeSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_A = compTypeSpec.getName();
		
		IASTDeclarator dtor = decl.getDeclarators()[0];
		IASTName name_a = dtor.getName();
		
		ICompositeType A = (ICompositeType) name_A.resolveBinding();
		IVariable a = (IVariable) name_a.resolveBinding();
		ICompositeType A_2 = (ICompositeType) a.getType();
		assertNotNull( A );
		assertNotNull( a );
		assertSame( A, A_2 );
	}
	
	public void testClassForwardDecl() throws Exception {
		StringBuffer buffer = new StringBuffer( "class A; class A {};" ); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals( decl.getDeclarators().length, 0 );
		IASTElaboratedTypeSpecifier elabSpec = (IASTElaboratedTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_elab = elabSpec.getName();
		
		decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
		assertEquals( decl.getDeclarators().length, 0 );
		IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_comp = compSpec.getName();
		
		ICompositeType A = (ICompositeType) name_elab.resolveBinding();
		ICompositeType A_2 = (ICompositeType) name_comp.resolveBinding();
		
		assertNotNull( A );
		assertSame( A, A_2 );
	}
	
	public void testVariable() throws Exception {
		StringBuffer buffer = new StringBuffer( "class A {};  A a;" ); //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals( decl.getDeclarators().length, 0 );
		IASTCompositeTypeSpecifier compType = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_A = compType.getName();
		
		decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
		IASTDeclarator dtor = decl.getDeclarators()[0];
		IASTName name_a = dtor.getName();
		IASTNamedTypeSpecifier namedSpec = (IASTNamedTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_A2 = namedSpec.getName();
		
		IVariable a = (IVariable) name_a.resolveBinding();
		ICompositeType A1 = (ICompositeType) a.getType();
		ICompositeType A2 = (ICompositeType) name_A2.resolveBinding();
		ICompositeType A = (ICompositeType) name_A.resolveBinding();
	
		assertNotNull( a );
		assertNotNull( A );
		assertSame( A, A1 );
		assertSame( A1, A2 );
	}
	
	public void testField() throws Exception {
		StringBuffer buffer = new StringBuffer( "class A {  int f; };" ); //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals( decl.getDeclarators().length, 0 );
		ICPPASTCompositeTypeSpecifier comp = (ICPPASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_A = comp.getName();
		
		decl = (IASTSimpleDeclaration) comp.getMembers()[0];
		IASTDeclarator dtor = decl.getDeclarators()[0];
		IASTName name_f = dtor.getName();
		
		ICPPClassType A = (ICPPClassType) name_A.resolveBinding();
		IField f = (IField) name_f.resolveBinding();
		
		assertNotNull( A );
		assertNotNull( f );
		assertSame( f.getScope(), A.getCompositeScope() );
	}
	
	public void testMethodDeclaration() throws Exception {
		StringBuffer buffer = new StringBuffer( "class A { int f(); };" ); //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals( decl.getDeclarators().length, 0 );
		IASTCompositeTypeSpecifier comp = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_A = comp.getName();
		
		decl = (IASTSimpleDeclaration) comp.getMembers()[0];
		IASTDeclarator dtor = decl.getDeclarators()[0];
		IASTName name_f = dtor.getName();
		
		ICPPClassType A = (ICPPClassType) name_A.resolveBinding();
		ICPPMethod f = (ICPPMethod) name_f.resolveBinding();
		
		assertNotNull( A );
		assertNotNull( f );
		assertSame( f.getScope(), A.getCompositeScope() );
	}
	
	public void testMethodDefinition() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( " class A { void f();  };      \n" ); //$NON-NLS-1$
		buffer.append( " void A::f() { }              \n" ); //$NON-NLS-1$
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals( decl.getDeclarators().length, 0 );
		IASTCompositeTypeSpecifier comp = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_A = comp.getName();
		
		decl = (IASTSimpleDeclaration) comp.getMembers()[0];
		IASTDeclarator dtor = decl.getDeclarators()[0];
		IASTName name_f1 = dtor.getName();
		
		IASTFunctionDefinition def = (IASTFunctionDefinition) tu.getDeclarations()[1];
		IASTFunctionDeclarator fdtor = def.getDeclarator();
		ICPPASTQualifiedName name_f2 = (ICPPASTQualifiedName) fdtor.getName();
		
		ICPPClassType A = (ICPPClassType) name_A.resolveBinding();
		ICPPMethod f1 = (ICPPMethod) name_f1.resolveBinding();
		ICPPMethod f2 = (ICPPMethod) name_f2.resolveBinding();
		
		IASTName[] names = name_f2.getNames();
		assertEquals( names.length, 2 );
		IASTName qn1 = names[0];
		IASTName qn2 = names[1];
		
		ICPPClassType A2 = (ICPPClassType) qn1.resolveBinding();
		ICPPMethod f3 = (ICPPMethod) qn2.resolveBinding();
		
		assertNotNull( A );
		assertNotNull( f1 );
		assertSame( f1, f2 );
		assertSame( f2, f3 );
		assertSame( A, A2 );
	}
	
	public void testMemberReference() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "class A { void f(); int i;    };   \n" ); //$NON-NLS-1$
		buffer.append( "void A::f() { i; }                 \n" ); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals( decl.getDeclarators().length, 0 );
		IASTCompositeTypeSpecifier comp = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_A = comp.getName();
		
		decl = (IASTSimpleDeclaration) comp.getMembers()[0];
		IASTDeclarator dtor = decl.getDeclarators()[0];
		IASTName name_f1 = dtor.getName();
		
		decl = (IASTSimpleDeclaration) comp.getMembers()[1];
		dtor = decl.getDeclarators()[0];
		IASTName name_i = dtor.getName();
		
		IASTFunctionDefinition def = (IASTFunctionDefinition) tu.getDeclarations()[1];
		IASTFunctionDeclarator fdtor = def.getDeclarator();
		ICPPASTQualifiedName name_f2 = (ICPPASTQualifiedName) fdtor.getName();
		
		IASTCompoundStatement compound = (IASTCompoundStatement) def.getBody();
		IASTExpressionStatement statement = (IASTExpressionStatement) compound.getStatements()[0];
		IASTIdExpression idExp = (IASTIdExpression) statement.getExpression();
		IASTName name_i2 = idExp.getName();
		
		ICPPClassType A = (ICPPClassType) name_A.resolveBinding();
		ICPPMethod f1 = (ICPPMethod) name_f1.resolveBinding();
		ICPPMethod f2 = (ICPPMethod) name_f2.resolveBinding();
		ICPPField i1 = (ICPPField) name_i.resolveBinding();
		ICPPField i2 = (ICPPField) name_i2.resolveBinding();
		
		IASTName[] names = name_f2.getNames();
		assertEquals( names.length, 2 );
		IASTName qn1 = names[0];
		IASTName qn2 = names[1];
		
		ICPPClassType A2 = (ICPPClassType) qn1.resolveBinding();
		ICPPMethod f3 = (ICPPMethod) qn2.resolveBinding();
		
		assertNotNull( A );
		assertNotNull( f1 );
		assertNotNull( i1 );
		assertSame( f1, f2 );
		assertSame( f2, f3 );
		assertSame( A, A2 );
		assertSame( i1, i2 );
	}
	
	public void testBasicInheritance() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "class A { int i; };               \n" ); //$NON-NLS-1$
		buffer.append( "class B : public A { void f(); }; \n" ); //$NON-NLS-1$
		buffer.append( "void B::f() { i; }                \n" ); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		ICPPASTCompositeTypeSpecifier comp = (ICPPASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_A1 = comp.getName();
		
		decl = (IASTSimpleDeclaration) comp.getMembers()[0];
		IASTName name_i1 = decl.getDeclarators()[0].getName();
		
		decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
		comp = (ICPPASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_B1 = comp.getName();
		
		ICPPASTBaseSpecifier base = comp.getBaseSpecifiers()[0];
		IASTName name_A2 = base.getName();
		
		decl = (IASTSimpleDeclaration) comp.getMembers()[0];
		IASTName name_f1 = decl.getDeclarators()[0].getName();
		
		IASTFunctionDefinition def = (IASTFunctionDefinition) tu.getDeclarations()[2];
		ICPPASTQualifiedName name_f2 = (ICPPASTQualifiedName) def.getDeclarator().getName();
		IASTName name_B2 = name_f2.getNames()[0];
		IASTName name_f3 = name_f2.getNames()[1];

		IASTCompoundStatement compound = (IASTCompoundStatement) def.getBody();
		IASTExpressionStatement statement = (IASTExpressionStatement) compound.getStatements()[0];
		IASTIdExpression idExp = (IASTIdExpression) statement.getExpression();
		IASTName name_i2 = idExp.getName();
		
		ICPPField i2 = (ICPPField) name_i2.resolveBinding();
		ICPPField i1 = (ICPPField) name_i1.resolveBinding();
		
		ICPPClassType A2 = (ICPPClassType) name_A2.resolveBinding();
		ICPPClassType A1 = (ICPPClassType) name_A1.resolveBinding();
		ICPPClassType B2 = (ICPPClassType) name_B2.resolveBinding();
		ICPPClassType B1 = (ICPPClassType) name_B1.resolveBinding();
		
		ICPPMethod f3 = (ICPPMethod) name_f3.resolveBinding();
		ICPPMethod f2 = (ICPPMethod) name_f2.resolveBinding();
		ICPPMethod f1 = (ICPPMethod) name_f1.resolveBinding();
		assertNotNull( A1 );
		assertNotNull( B1 );
		assertNotNull( i1 );
		assertNotNull( f1 );
		assertSame( A1, A2 );
		assertSame( B1, B2 );
		assertSame( i1, i2 );
		assertSame( f1, f2 );
		assertSame( f2, f3 );
	}
	
//	public void testNamespaces() throws Exception {
//		StringBuffer buffer = new StringBuffer();
//		buffer.append( "namespace A{            \n"); //$NON-NLS-1$
//		buffer.append( "   int a;               \n"); //$NON-NLS-1$
//		buffer.append( "}                       \n"); //$NON-NLS-1$
//		buffer.append( "namespace B{            \n"); //$NON-NLS-1$
//		buffer.append( "   using namespace A;   \n"); //$NON-NLS-1$
//		buffer.append( "}                       \n"); //$NON-NLS-1$
//		buffer.append( "namespace C{            \n"); //$NON-NLS-1$
//		buffer.append( "   using namespace A;   \n"); //$NON-NLS-1$
//		buffer.append( "}                       \n"); //$NON-NLS-1$
//		buffer.append( "                        \n"); //$NON-NLS-1$
//		buffer.append( "namespace BC{           \n"); //$NON-NLS-1$
//		buffer.append( "   using namespace B;   \n"); //$NON-NLS-1$
//		buffer.append( "   using namespace C;   \n"); //$NON-NLS-1$
//		buffer.append( "}                       \n"); //$NON-NLS-1$
//		buffer.append( "                        \n"); //$NON-NLS-1$
//		buffer.append( "void f(){               \n"); //$NON-NLS-1$
//		buffer.append( "   BC::a++; //ok        \n"); //$NON-NLS-1$
//		buffer.append( "}                       \n"); //$NON-NLS-1$
//		
//		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
//		
//		//CPPVisitor.visitTranslationUnit( tu )
//	}
}
