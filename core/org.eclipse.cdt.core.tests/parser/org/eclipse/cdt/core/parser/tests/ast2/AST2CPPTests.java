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
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;

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
	
	public void testNamespaces() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "namespace A{            \n"); //$NON-NLS-1$
		buffer.append( "   int a;               \n"); //$NON-NLS-1$
		buffer.append( "}                       \n"); //$NON-NLS-1$
		buffer.append( "namespace B{            \n"); //$NON-NLS-1$
		buffer.append( "   using namespace A;   \n"); //$NON-NLS-1$
		buffer.append( "}                       \n"); //$NON-NLS-1$
		buffer.append( "namespace C{            \n"); //$NON-NLS-1$
		buffer.append( "   using namespace A;   \n"); //$NON-NLS-1$
		buffer.append( "}                       \n"); //$NON-NLS-1$
		buffer.append( "                        \n"); //$NON-NLS-1$
		buffer.append( "namespace BC{           \n"); //$NON-NLS-1$
		buffer.append( "   using namespace B;   \n"); //$NON-NLS-1$
		buffer.append( "   using namespace C;   \n"); //$NON-NLS-1$
		buffer.append( "}                       \n"); //$NON-NLS-1$
		buffer.append( "                        \n"); //$NON-NLS-1$
		buffer.append( "void f(){               \n"); //$NON-NLS-1$
		buffer.append( "   BC::a++; //ok        \n"); //$NON-NLS-1$
		buffer.append( "}                       \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		
		CPPNameCollector collector = new CPPNameCollector();
		CPPVisitor.visitTranslationUnit( tu, collector );
		
		assertEquals( collector.size(), 13 );
        ICPPNamespace A = (ICPPNamespace) collector.getName( 0 ).resolveBinding();
        IVariable a = (IVariable) collector.getName( 1 ).resolveBinding();
        ICPPNamespace B = (ICPPNamespace) collector.getName( 2 ).resolveBinding();
        ICPPNamespace C = (ICPPNamespace) collector.getName( 4 ).resolveBinding();
        ICPPNamespace BC = (ICPPNamespace) collector.getName( 6 ).resolveBinding();
        IFunction f = (IFunction) collector.getName( 9 ).resolveBinding();
        assertInstances( collector, A, 3 );
        assertInstances( collector, a, 3 );
        assertInstances( collector, B, 2 );
        assertInstances( collector, C, 2 );
        assertInstances( collector, BC, 2 );
        assertInstances( collector, f ,1 );
	}
	
	public void testNameHiding() throws Exception {
	    StringBuffer buffer = new StringBuffer();
	    buffer.append( "int A;                       \n"); //$NON-NLS-1$
	    buffer.append( "class A {};                  \n"); //$NON-NLS-1$
	    buffer.append( "void f() {                   \n"); //$NON-NLS-1$
	    buffer.append( "   A++;                      \n"); //$NON-NLS-1$
	    buffer.append( "   class A a;                \n"); //$NON-NLS-1$
	    buffer.append( "}                            \n"); //$NON-NLS-1$
	    
	    IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
	    
	    CPPNameCollector collector = new CPPNameCollector();
	    CPPVisitor.visitTranslationUnit( tu, collector );
	    
	    assertEquals( collector.size(), 6 );
	    IVariable vA = (IVariable) collector.getName( 0 ).resolveBinding();
	    ICompositeType cA = (ICompositeType) collector.getName( 1 ).resolveBinding();
	    IVariable a = (IVariable) collector.getName( 5 ).resolveBinding();
	    
	    assertSame( a.getType(), cA );
	    assertInstances( collector, vA, 2 );
	    assertInstances( collector, cA, 2 );
	}
	
	public void testBlockTraversal() throws Exception {
	    StringBuffer buffer = new StringBuffer();
	    buffer.append( "class A { void f(); };            \n" ); //$NON-NLS-1$
	    buffer.append( "class B;                          \n" ); //$NON-NLS-1$
	    buffer.append( "void A::f() {                     \n" ); //$NON-NLS-1$
	    buffer.append( "   B b;                           \n" ); //$NON-NLS-1$
	    buffer.append( "}                                 \n" ); //$NON-NLS-1$
	    buffer.append( "int B;                            \n" ); //$NON-NLS-1$
	    
	    IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
	    CPPNameCollector collector = new CPPNameCollector();
	    CPPVisitor.visitTranslationUnit( tu, collector );
	    
	    assertEquals( collector.size(), 9 );
	    ICompositeType A = (ICompositeType) collector.getName( 0 ).resolveBinding();
	    ICPPMethod f = (ICPPMethod) collector.getName( 1 ).resolveBinding();
	    ICompositeType B = (ICompositeType) collector.getName( 2 ).resolveBinding();
	    
	    IVariable b = (IVariable) collector.getName( 7 ).resolveBinding();
	    IVariable B2 = (IVariable) collector.getName( 8 ).resolveBinding();
	    assertSame( b.getType(), B );
	    assertInstances( collector, A, 2 );
	    assertInstances( collector, f, 3 );
	    assertInstances( collector, B, 2 );
	    assertInstances( collector, b, 1 );
	    assertInstances( collector, B2, 1 );
	}
	
	public void testFunctionResolution() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "void f( int i );                      \n"); //$NON-NLS-1$
		buffer.append( "void f( char c );                     \n"); //$NON-NLS-1$
		buffer.append( "void main() {                         \n"); //$NON-NLS-1$
		buffer.append( "   f( 1 );		//calls f( int );     \n"); //$NON-NLS-1$
		buffer.append( "   f( 'b' );                          \n"); //$NON-NLS-1$
		buffer.append( "}                                     \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector collector = new CPPNameCollector();
		CPPVisitor.visitTranslationUnit( tu, collector );
		IFunction f1 = (IFunction) collector.getName( 0 ).resolveBinding();
		IFunction f2 = (IFunction) collector.getName( 2 ).resolveBinding();
		
		assertInstances( collector, f1, 2 );
		assertInstances( collector, f2, 2 );
	}
	
	public void testSimpleStruct() throws Exception {
		StringBuffer buff = new StringBuffer();
		buff.append("typedef struct {  \n"); //$NON-NLS-1$
		buff.append("    int x;        \n"); //$NON-NLS-1$
		buff.append("} S;              \n"); //$NON-NLS-1$
		buff.append("void f() {        \n"); //$NON-NLS-1$
		buff.append("    S myS;        \n"); //$NON-NLS-1$
		buff.append("    myS.x = 5;    \n"); //$NON-NLS-1$
		buff.append("}                 \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buff.toString(), ParserLanguage.CPP );
		CPPNameCollector collector = new CPPNameCollector();
		CPPVisitor.visitTranslationUnit( tu, collector );
		ICPPClassType anonStruct = (ICPPClassType) collector.getName( 0 ).resolveBinding();
		ICPPField x = (ICPPField) collector.getName(1).resolveBinding();
		ITypedef S = (ITypedef) collector.getName(2).resolveBinding();
		IFunction f = (IFunction) collector.getName(3).resolveBinding();
		IVariable myS = (IVariable) collector.getName(5).resolveBinding();

		assertInstances( collector, anonStruct, 1 );
		assertInstances( collector, x, 2 );
		assertInstances( collector, S, 2 );
		assertInstances( collector, f, 1 );
		assertInstances( collector, myS, 2 );
	}
	public void testStructureTags_1() throws Exception {
		StringBuffer buffer = new StringBuffer();
    	buffer.append( "struct A;             \n" ); //$NON-NLS-1$
    	buffer.append( "void f(){             \n" ); //$NON-NLS-1$
    	buffer.append( "   struct A;          \n" ); //$NON-NLS-1$
    	buffer.append( "   struct A * a;      \n" ); //$NON-NLS-1$
    	buffer.append( "}                     \n" ); //$NON-NLS-1$
    	
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
    	CPPNameCollector collector = new CPPNameCollector();
		CPPVisitor.visitTranslationUnit( tu, collector );
		
		ICPPClassType A1 = (ICPPClassType) collector.getName( 0 ).resolveBinding();
		ICPPClassType A2 = (ICPPClassType) collector.getName( 2 ).resolveBinding();
		IVariable a = (IVariable) collector.getName( 4 ).resolveBinding();

		assertNotNull( a );
		assertNotNull( A1 );
		assertNotNull( A2 );
		assertNotSame( A1, A2 );
		assertInstances( collector, A1, 1 );
		assertInstances( collector, A2, 2 );
	}
	
	public void testStructureTags_2() throws Exception{
		StringBuffer buffer = new StringBuffer();
		buffer.append( "struct A;             \n" ); //$NON-NLS-1$
		buffer.append( "void f(){             \n" ); //$NON-NLS-1$
		buffer.append( "   struct A * a;      \n" ); //$NON-NLS-1$
		buffer.append( "}                     \r\n" ); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
    	CPPNameCollector collector = new CPPNameCollector();
		CPPVisitor.visitTranslationUnit( tu, collector );
		
		ICPPClassType A1 = (ICPPClassType) collector.getName( 0 ).resolveBinding();
		ICPPClassType A2 = (ICPPClassType) collector.getName( 2 ).resolveBinding();
		IVariable a = (IVariable) collector.getName( 3 ).resolveBinding();

		assertNotNull( a );
		assertNotNull( A1 );
		assertNotNull( A2 );
		assertSame( A1, A2 );
		assertInstances( collector, A1, 2 );
	}
	
	public void testStructureDef() throws Exception{
    	StringBuffer buffer = new StringBuffer();
    	buffer.append( "struct A;                \r\n"); //$NON-NLS-1$
    	buffer.append( "struct A * a;            \n"); //$NON-NLS-1$
    	buffer.append( "struct A { int i; };     \n"); //$NON-NLS-1$
    	buffer.append( "void f() {               \n"); //$NON-NLS-1$
    	buffer.append( "   a->i;                 \n"); //$NON-NLS-1$
    	buffer.append( "}                        \n"); //$NON-NLS-1$
    	
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
    	CPPNameCollector collector = new CPPNameCollector();
		CPPVisitor.visitTranslationUnit( tu, collector );
		
		ICPPClassType A1 = (ICPPClassType) collector.getName( 0 ).resolveBinding();
		IVariable a = (IVariable) collector.getName( 2 ).resolveBinding();
		ICPPField i = (ICPPField) collector.getName( 4 ).resolveBinding();
		
		assertInstances( collector, A1, 3 );
		assertInstances( collector, a, 2 );
		assertInstances( collector, i, 2 );
	}
	
	public void testStructureNamespace() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "struct x {};        \n" ); //$NON-NLS-1$
		buffer.append( "void f( int x ) {   \n" ); //$NON-NLS-1$
		buffer.append( "   struct x i;      \n" ); //$NON-NLS-1$
		buffer.append( "}                   \n" ); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector collector = new CPPNameCollector();
		CPPVisitor.visitTranslationUnit( tu, collector );
		
		ICPPClassType x = (ICPPClassType) collector.getName(0).resolveBinding();
		
		assertInstances( collector, x, 2 );
	}
	
	public void testFunctionDef() throws Exception {
		StringBuffer buffer  = new StringBuffer();
		buffer.append( "void f( int a );        \n"); //$NON-NLS-1$
		buffer.append( "void f( int b ){        \n"); //$NON-NLS-1$
		buffer.append( "   b;                   \n"); //$NON-NLS-1$
		buffer.append( "}                       \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector collector = new CPPNameCollector();
		CPPVisitor.visitTranslationUnit( tu, collector );
		
		IFunction f = (IFunction) collector.getName(0).resolveBinding();
		IParameter a = (IParameter) collector.getName( 1 ).resolveBinding();
		
		assertInstances( collector, f, 2 );
		assertInstances( collector, a, 3 );
	}
	public void testSimpleFunctionCall() throws Exception {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append( "void f();              \n" ); //$NON-NLS-1$
    	buffer.append( "void g() {             \n" ); //$NON-NLS-1$
    	buffer.append( "   f();                \n" ); //$NON-NLS-1$
    	buffer.append( "}                      \n" ); //$NON-NLS-1$
    	buffer.append( "void f(){ }            \n" ); //$NON-NLS-1$
    	
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector collector = new CPPNameCollector();
		CPPVisitor.visitTranslationUnit( tu, collector );
		
		IFunction f = (IFunction) collector.getName(0).resolveBinding();
		IFunction g = (IFunction) collector.getName( 1 ).resolveBinding();
		
		assertInstances( collector, f, 3 );
		assertInstances( collector, g, 1 );
	}
	
	public void testForLoop() throws Exception {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append( "void f() {                         \n"); //$NON-NLS-1$
    	buffer.append( "   for( int i = 0; i < 5; i++ ) {  \n"); //$NON-NLS-1$         
    	buffer.append( "      i;                           \n"); //$NON-NLS-1$
    	buffer.append( "   }                               \n"); //$NON-NLS-1$
    	buffer.append( "}                                  \n"); //$NON-NLS-1$
    	
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector collector = new CPPNameCollector();
		CPPVisitor.visitTranslationUnit( tu, collector );
		
		IVariable i = (IVariable) collector.getName(1).resolveBinding();
		
		assertInstances( collector, i, 4 );
	}
	
    public void testExpressionFieldReference() throws Exception{
    	StringBuffer buffer = new StringBuffer();
    	buffer.append( "struct A { int x; };    \n"); //$NON-NLS-1$
    	buffer.append( "void f(){               \n"); //$NON-NLS-1$
    	buffer.append( "   ((struct A *) 1)->x; \n"); //$NON-NLS-1$
    	buffer.append( "}                       \n"); //$NON-NLS-1$
    	
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector collector = new CPPNameCollector();
		CPPVisitor.visitTranslationUnit( tu, collector );
		
		ICPPClassType A = (ICPPClassType) collector.getName(0).resolveBinding();
		IField x = (IField) collector.getName(1).resolveBinding();
		
		assertInstances( collector, A, 2 );
		assertInstances( collector, x, 2 );
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
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector collector = new CPPNameCollector();
		CPPVisitor.visitTranslationUnit( tu, collector );
        
		IEnumeration hue = (IEnumeration) collector.getName(0).resolveBinding();
		IEnumerator red = (IEnumerator) collector.getName(1).resolveBinding();
		IEnumerator blue = (IEnumerator) collector.getName(2).resolveBinding();
		IEnumerator green = (IEnumerator) collector.getName(3).resolveBinding();
		IVariable col = (IVariable) collector.getName(5).resolveBinding();
		IVariable cp = (IVariable) collector.getName(6).resolveBinding();
		
		assertInstances( collector, hue, 2 );
		assertInstances( collector, red, 2 );
		assertInstances( collector, blue, 2 );
		assertInstances( collector, green, 1 );
		assertInstances( collector, col, 3 );
		assertInstances( collector, cp, 3 );
		
		assertTrue( cp.getType() instanceof IPointerType );
		IPointerType pt = (IPointerType) cp.getType();
		assertSame( pt.getType(), hue );
    }
    
    public void testPointerToFunction() throws Exception{
    	IASTTranslationUnit tu = parse( "int (*pfi)();", ParserLanguage.CPP ); //$NON-NLS-1$
    	CPPNameCollector collector = new CPPNameCollector();
		CPPVisitor.visitTranslationUnit( tu, collector );
		IVariable pf = (IVariable) collector.getName(0).resolveBinding();
		IPointerType pt = (IPointerType) pf.getType();
		assertTrue( pt.getType() instanceof IFunctionType );
		
		tu = parse( "struct A; int (*pfi)( int, struct A * );", ParserLanguage.CPP ); //$NON-NLS-1$
    	collector = new CPPNameCollector();
		CPPVisitor.visitTranslationUnit( tu, collector );
		ICPPClassType A = (ICPPClassType) collector.getName(0).resolveBinding();
		pf = (IVariable) collector.getName(1).resolveBinding();
		pt = (IPointerType) pf.getType();
		assertTrue( pt.getType() instanceof IFunctionType );
		IFunctionType ft = (IFunctionType) pt.getType();
		IType [] params = ft.getParameterTypes();
		assertTrue( params[0] instanceof IBasicType );
		assertTrue( params[1] instanceof IPointerType );
		pt = (IPointerType) params[1];
		assertSame( pt.getType(), A );
    }
    
    public void testFunctionTypes() throws Exception{
        StringBuffer buffer = new StringBuffer();
        buffer.append( "struct A;                           \n"); //$NON-NLS-1$
        buffer.append( "int * f( int i, char c );           \n"); //$NON-NLS-1$
        buffer.append( "void ( *g ) ( A * );         \n"); //$NON-NLS-1$
        buffer.append( "void (* (*h)(A**) ) ( int ); \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
        
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
        IType t_h_func_return = t_h_func.getReturnType();
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
    
    public void testFnReturningPtrToFn() throws Exception {
    	IASTTranslationUnit tu = parse( "void ( * f( int ) )(){}", ParserLanguage.CPP ); //$NON-NLS-1$
    	
        IASTFunctionDefinition def = (IASTFunctionDefinition) tu.getDeclarations()[0];
        IFunction f = (IFunction) def.getDeclarator().getNestedDeclarator().getName().resolveBinding();
        
        IFunctionType ft = f.getType();
        assertTrue( ft.getReturnType() instanceof IPointerType );
        assertTrue( ((IPointerType) ft.getReturnType()).getType() instanceof IFunctionType );
        assertEquals( ft.getParameterTypes().length, 1 );
    }
      
    public void testFunctionDeclarations() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("typedef int Int;      \n"); //$NON-NLS-1$
        buffer.append("void f( int i );      \n"); //$NON-NLS-1$
        buffer.append("void f( const int );  \n"); //$NON-NLS-1$
        buffer.append("void f( Int i );      \n"); //$NON-NLS-1$
        buffer.append("void g( char * );     \n"); //$NON-NLS-1$
        buffer.append("void g( char [] );    \n"); //$NON-NLS-1$
        buffer.append("void h( int()() );    \n"); //$NON-NLS-1$
        buffer.append("void h( int (*) () ); \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
		CPPNameCollector collector = new CPPNameCollector();
		CPPVisitor.visitTranslationUnit( tu, collector );
		
		IFunction f = (IFunction) collector.getName( 1 ).resolveBinding();
		IFunction g = (IFunction) collector.getName( 8 ).resolveBinding();
		IFunction h = (IFunction) collector.getName( 12 ).resolveBinding();
		
		assertInstances( collector, f, 3 );
		assertInstances( collector, g, 2 );
		assertInstances( collector, h, 2 );
    }
}

