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

import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IEnumerator;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IPointerType;
import org.eclipse.cdt.core.dom.ast.IProblemBinding;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVisitor;

/**
 * @author aniefer
 */

public class AST2CPPTests extends AST2BaseTest {

    public void testSimpleClass() throws Exception {
        StringBuffer buffer = new StringBuffer("class A { } a;"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTCompositeTypeSpecifier compTypeSpec = (IASTCompositeTypeSpecifier) decl
                .getDeclSpecifier();
        IASTName name_A = compTypeSpec.getName();

        IASTDeclarator dtor = decl.getDeclarators()[0];
        IASTName name_a = dtor.getName();

        ICompositeType A = (ICompositeType) name_A.resolveBinding();
        IVariable a = (IVariable) name_a.resolveBinding();
        ICompositeType A_2 = (ICompositeType) a.getType();
        assertNotNull(A);
        assertNotNull(a);
        assertSame(A, A_2);
    }

    public void testClassForwardDecl() throws Exception {
        StringBuffer buffer = new StringBuffer("class A; class A {};"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        assertEquals(decl.getDeclarators().length, 0);
        IASTElaboratedTypeSpecifier elabSpec = (IASTElaboratedTypeSpecifier) decl
                .getDeclSpecifier();
        IASTName name_elab = elabSpec.getName();

        decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
        assertEquals(decl.getDeclarators().length, 0);
        IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl
                .getDeclSpecifier();
        IASTName name_comp = compSpec.getName();

        ICompositeType A = (ICompositeType) name_elab.resolveBinding();
        ICompositeType A_2 = (ICompositeType) name_comp.resolveBinding();

        assertNotNull(A);
        assertSame(A, A_2);
    }

    public void testVariable() throws Exception {
        StringBuffer buffer = new StringBuffer("class A {};  A a;"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        assertEquals(decl.getDeclarators().length, 0);
        IASTCompositeTypeSpecifier compType = (IASTCompositeTypeSpecifier) decl
                .getDeclSpecifier();
        IASTName name_A = compType.getName();

        decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
        IASTDeclarator dtor = decl.getDeclarators()[0];
        IASTName name_a = dtor.getName();
        IASTNamedTypeSpecifier namedSpec = (IASTNamedTypeSpecifier) decl
                .getDeclSpecifier();
        IASTName name_A2 = namedSpec.getName();

        IVariable a = (IVariable) name_a.resolveBinding();
        ICompositeType A1 = (ICompositeType) a.getType();
        ICompositeType A2 = (ICompositeType) name_A2.resolveBinding();
        ICompositeType A = (ICompositeType) name_A.resolveBinding();

        assertNotNull(a);
        assertNotNull(A);
        assertSame(A, A1);
        assertSame(A1, A2);
    }

    public void testField() throws Exception {
        StringBuffer buffer = new StringBuffer("class A {  int f; };"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        assertEquals(decl.getDeclarators().length, 0);
        ICPPASTCompositeTypeSpecifier comp = (ICPPASTCompositeTypeSpecifier) decl
                .getDeclSpecifier();
        IASTName name_A = comp.getName();

        decl = (IASTSimpleDeclaration) comp.getMembers()[0];
        IASTDeclarator dtor = decl.getDeclarators()[0];
        IASTName name_f = dtor.getName();

        ICPPClassType A = (ICPPClassType) name_A.resolveBinding();
        IField f = (IField) name_f.resolveBinding();

        assertNotNull(A);
        assertNotNull(f);
        assertSame(f.getScope(), A.getCompositeScope());
    }

    public void testMethodDeclaration() throws Exception {
        StringBuffer buffer = new StringBuffer("class A { int f(); };"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        assertEquals(decl.getDeclarators().length, 0);
        IASTCompositeTypeSpecifier comp = (IASTCompositeTypeSpecifier) decl
                .getDeclSpecifier();
        IASTName name_A = comp.getName();

        decl = (IASTSimpleDeclaration) comp.getMembers()[0];
        IASTDeclarator dtor = decl.getDeclarators()[0];
        IASTName name_f = dtor.getName();

        ICPPClassType A = (ICPPClassType) name_A.resolveBinding();
        ICPPMethod f = (ICPPMethod) name_f.resolveBinding();

        assertNotNull(A);
        assertNotNull(f);
        assertSame(f.getScope(), A.getCompositeScope());
    }

    public void testMethodDefinition() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append(" class A { void f();  };      \n"); //$NON-NLS-1$
        buffer.append(" void A::f() { }              \n"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        assertEquals(decl.getDeclarators().length, 0);
        IASTCompositeTypeSpecifier comp = (IASTCompositeTypeSpecifier) decl
                .getDeclSpecifier();
        IASTName name_A = comp.getName();

        decl = (IASTSimpleDeclaration) comp.getMembers()[0];
        IASTDeclarator dtor = decl.getDeclarators()[0];
        IASTName name_f1 = dtor.getName();

        IASTFunctionDefinition def = (IASTFunctionDefinition) tu
                .getDeclarations()[1];
        IASTFunctionDeclarator fdtor = def.getDeclarator();
        ICPPASTQualifiedName name_f2 = (ICPPASTQualifiedName) fdtor.getName();

        ICPPClassType A = (ICPPClassType) name_A.resolveBinding();
        ICPPMethod f1 = (ICPPMethod) name_f1.resolveBinding();
        ICPPMethod f2 = (ICPPMethod) name_f2.resolveBinding();

        IASTName[] names = name_f2.getNames();
        assertEquals(names.length, 2);
        IASTName qn1 = names[0];
        IASTName qn2 = names[1];

        ICPPClassType A2 = (ICPPClassType) qn1.resolveBinding();
        ICPPMethod f3 = (ICPPMethod) qn2.resolveBinding();

        assertNotNull(A);
        assertNotNull(f1);
        assertSame(f1, f2);
        assertSame(f2, f3);
        assertSame(A, A2);
    }

    public void testMemberReference() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class A { void f(); int i;    };   \n"); //$NON-NLS-1$
        buffer.append("void A::f() { i; }                 \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        assertEquals(decl.getDeclarators().length, 0);
        IASTCompositeTypeSpecifier comp = (IASTCompositeTypeSpecifier) decl
                .getDeclSpecifier();
        IASTName name_A = comp.getName();

        decl = (IASTSimpleDeclaration) comp.getMembers()[0];
        IASTDeclarator dtor = decl.getDeclarators()[0];
        IASTName name_f1 = dtor.getName();

        decl = (IASTSimpleDeclaration) comp.getMembers()[1];
        dtor = decl.getDeclarators()[0];
        IASTName name_i = dtor.getName();

        IASTFunctionDefinition def = (IASTFunctionDefinition) tu
                .getDeclarations()[1];
        IASTFunctionDeclarator fdtor = def.getDeclarator();
        ICPPASTQualifiedName name_f2 = (ICPPASTQualifiedName) fdtor.getName();

        IASTCompoundStatement compound = (IASTCompoundStatement) def.getBody();
        IASTExpressionStatement statement = (IASTExpressionStatement) compound
                .getStatements()[0];
        IASTIdExpression idExp = (IASTIdExpression) statement.getExpression();
        IASTName name_i2 = idExp.getName();

        ICPPClassType A = (ICPPClassType) name_A.resolveBinding();
        ICPPMethod f1 = (ICPPMethod) name_f1.resolveBinding();
        ICPPMethod f2 = (ICPPMethod) name_f2.resolveBinding();
        ICPPField i1 = (ICPPField) name_i.resolveBinding();
        ICPPField i2 = (ICPPField) name_i2.resolveBinding();

        IASTName[] names = name_f2.getNames();
        assertEquals(names.length, 2);
        IASTName qn1 = names[0];
        IASTName qn2 = names[1];

        ICPPClassType A2 = (ICPPClassType) qn1.resolveBinding();
        ICPPMethod f3 = (ICPPMethod) qn2.resolveBinding();

        assertNotNull(A);
        assertNotNull(f1);
        assertNotNull(i1);
        assertSame(f1, f2);
        assertSame(f2, f3);
        assertSame(A, A2);
        assertSame(i1, i2);
    }

    public void testBasicInheritance() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class A { int i; };               \n"); //$NON-NLS-1$
        buffer.append("class B : public A { void f(); }; \n"); //$NON-NLS-1$
        buffer.append("void B::f() { i; }                \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        ICPPASTCompositeTypeSpecifier comp = (ICPPASTCompositeTypeSpecifier) decl
                .getDeclSpecifier();
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

        IASTFunctionDefinition def = (IASTFunctionDefinition) tu
                .getDeclarations()[2];
        ICPPASTQualifiedName name_f2 = (ICPPASTQualifiedName) def
                .getDeclarator().getName();
        IASTName name_B2 = name_f2.getNames()[0];
        IASTName name_f3 = name_f2.getNames()[1];

        IASTCompoundStatement compound = (IASTCompoundStatement) def.getBody();
        IASTExpressionStatement statement = (IASTExpressionStatement) compound
                .getStatements()[0];
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
        assertNotNull(A1);
        assertNotNull(B1);
        assertNotNull(i1);
        assertNotNull(f1);
        assertSame(A1, A2);
        assertSame(B1, B2);
        assertSame(i1, i2);
        assertSame(f1, f2);
        assertSame(f2, f3);
    }

    public void testNamespaces() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace A{            \n"); //$NON-NLS-1$
        buffer.append("   int a;               \n"); //$NON-NLS-1$
        buffer.append("}                       \n"); //$NON-NLS-1$
        buffer.append("namespace B{            \n"); //$NON-NLS-1$
        buffer.append("   using namespace A;   \n"); //$NON-NLS-1$
        buffer.append("}                       \n"); //$NON-NLS-1$
        buffer.append("namespace C{            \n"); //$NON-NLS-1$
        buffer.append("   using namespace A;   \n"); //$NON-NLS-1$
        buffer.append("}                       \n"); //$NON-NLS-1$
        buffer.append("                        \n"); //$NON-NLS-1$
        buffer.append("namespace BC{           \n"); //$NON-NLS-1$
        buffer.append("   using namespace B;   \n"); //$NON-NLS-1$
        buffer.append("   using namespace C;   \n"); //$NON-NLS-1$
        buffer.append("}                       \n"); //$NON-NLS-1$
        buffer.append("                        \n"); //$NON-NLS-1$
        buffer.append("void f(){               \n"); //$NON-NLS-1$
        buffer.append("   BC::a++; //ok        \n"); //$NON-NLS-1$
        buffer.append("}                       \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);

        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        assertEquals(collector.size(), 13);
        ICPPNamespace A = (ICPPNamespace) collector.getName(0).resolveBinding();
        IVariable a = (IVariable) collector.getName(1).resolveBinding();
        ICPPNamespace B = (ICPPNamespace) collector.getName(2).resolveBinding();
        ICPPNamespace C = (ICPPNamespace) collector.getName(4).resolveBinding();
        ICPPNamespace BC = (ICPPNamespace) collector.getName(6)
                .resolveBinding();
        IFunction f = (IFunction) collector.getName(9).resolveBinding();
        assertInstances(collector, A, 3);
        assertInstances(collector, a, 3);
        assertInstances(collector, B, 2);
        assertInstances(collector, C, 2);
        assertInstances(collector, BC, 2);
        assertInstances(collector, f, 1);
    }

    public void testNameHiding() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("int A;                       \n"); //$NON-NLS-1$
        buffer.append("class A {};                  \n"); //$NON-NLS-1$
        buffer.append("void f() {                   \n"); //$NON-NLS-1$
        buffer.append("   A++;                      \n"); //$NON-NLS-1$
        buffer.append("   class A a;                \n"); //$NON-NLS-1$
        buffer.append("}                            \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);

        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        assertEquals(collector.size(), 6);
        IVariable vA = (IVariable) collector.getName(0).resolveBinding();
        ICompositeType cA = (ICompositeType) collector.getName(1)
                .resolveBinding();
        IVariable a = (IVariable) collector.getName(5).resolveBinding();

        assertSame(a.getType(), cA);
        assertInstances(collector, vA, 2);
        assertInstances(collector, cA, 2);
    }

    public void testBlockTraversal() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class A { void f(); };            \n"); //$NON-NLS-1$
        buffer.append("class B;                          \n"); //$NON-NLS-1$
        buffer.append("void A::f() {                     \n"); //$NON-NLS-1$
        buffer.append("   B b;                           \n"); //$NON-NLS-1$
        buffer.append("}                                 \n"); //$NON-NLS-1$
        buffer.append("int B;                            \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        assertEquals(collector.size(), 9);
        ICompositeType A = (ICompositeType) collector.getName(0)
                .resolveBinding();
        ICPPMethod f = (ICPPMethod) collector.getName(1).resolveBinding();
        ICompositeType B = (ICompositeType) collector.getName(2)
                .resolveBinding();

        IVariable b = (IVariable) collector.getName(7).resolveBinding();
        IVariable B2 = (IVariable) collector.getName(8).resolveBinding();
        assertSame(b.getType(), B);
        assertInstances(collector, A, 2);
        assertInstances(collector, f, 3);
        assertInstances(collector, B, 2);
        assertInstances(collector, b, 1);
        assertInstances(collector, B2, 1);
    }

    public void testFunctionResolution() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f( int i );                      \n"); //$NON-NLS-1$
        buffer.append("void f( char c );                     \n"); //$NON-NLS-1$
        buffer.append("void main() {                         \n"); //$NON-NLS-1$
        buffer.append("   f( 1 );		//calls f( int );     \n"); //$NON-NLS-1$
        buffer.append("   f( 'b' );                          \n"); //$NON-NLS-1$
        buffer.append("}                                     \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);
        IFunction f1 = (IFunction) collector.getName(0).resolveBinding();
        IFunction f2 = (IFunction) collector.getName(2).resolveBinding();

        assertInstances(collector, f1, 2);
        assertInstances(collector, f2, 2);
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

        IASTTranslationUnit tu = parse(buff.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);
        ICPPClassType anonStruct = (ICPPClassType) collector.getName(0)
                .resolveBinding();
        ICPPField x = (ICPPField) collector.getName(1).resolveBinding();
        ITypedef S = (ITypedef) collector.getName(2).resolveBinding();
        IFunction f = (IFunction) collector.getName(3).resolveBinding();
        IVariable myS = (IVariable) collector.getName(5).resolveBinding();

        assertInstances(collector, anonStruct, 1);
        assertInstances(collector, x, 2);
        assertInstances(collector, S, 2);
        assertInstances(collector, f, 1);
        assertInstances(collector, myS, 2);
    }

    public void testStructureTags_1() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A;             \n"); //$NON-NLS-1$
        buffer.append("void f(){             \n"); //$NON-NLS-1$
        buffer.append("   struct A;          \n"); //$NON-NLS-1$
        buffer.append("   struct A * a;      \n"); //$NON-NLS-1$
        buffer.append("}                     \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        ICPPClassType A1 = (ICPPClassType) collector.getName(0)
                .resolveBinding();
        ICPPClassType A2 = (ICPPClassType) collector.getName(2)
                .resolveBinding();
        IVariable a = (IVariable) collector.getName(4).resolveBinding();

        assertNotNull(a);
        assertNotNull(A1);
        assertNotNull(A2);
        assertNotSame(A1, A2);
        assertInstances(collector, A1, 1);
        assertInstances(collector, A2, 2);
    }

    public void testStructureTags_2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A;             \n"); //$NON-NLS-1$
        buffer.append("void f(){             \n"); //$NON-NLS-1$
        buffer.append("   struct A * a;      \n"); //$NON-NLS-1$
        buffer.append("}                     \r\n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        ICPPClassType A1 = (ICPPClassType) collector.getName(0)
                .resolveBinding();
        ICPPClassType A2 = (ICPPClassType) collector.getName(2)
                .resolveBinding();
        IVariable a = (IVariable) collector.getName(3).resolveBinding();

        assertNotNull(a);
        assertNotNull(A1);
        assertNotNull(A2);
        assertSame(A1, A2);
        assertInstances(collector, A1, 2);
    }

    public void testStructureDef() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A;                \r\n"); //$NON-NLS-1$
        buffer.append("struct A * a;            \n"); //$NON-NLS-1$
        buffer.append("struct A { int i; };     \n"); //$NON-NLS-1$
        buffer.append("void f() {               \n"); //$NON-NLS-1$
        buffer.append("   a->i;                 \n"); //$NON-NLS-1$
        buffer.append("}                        \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        ICPPClassType A1 = (ICPPClassType) collector.getName(0)
                .resolveBinding();
        IVariable a = (IVariable) collector.getName(2).resolveBinding();
        ICPPField i = (ICPPField) collector.getName(4).resolveBinding();

        assertInstances(collector, A1, 3);
        assertInstances(collector, a, 2);
        assertInstances(collector, i, 2);
    }

    public void testStructureNamespace() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct x {};        \n"); //$NON-NLS-1$
        buffer.append("void f( int x ) {   \n"); //$NON-NLS-1$
        buffer.append("   struct x i;      \n"); //$NON-NLS-1$
        buffer.append("}                   \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        ICPPClassType x = (ICPPClassType) collector.getName(0).resolveBinding();

        assertInstances(collector, x, 2);
    }

    public void testFunctionDef() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f( int a );        \n"); //$NON-NLS-1$
        buffer.append("void f( int b ){        \n"); //$NON-NLS-1$
        buffer.append("   b;                   \n"); //$NON-NLS-1$
        buffer.append("}                       \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        IFunction f = (IFunction) collector.getName(0).resolveBinding();
        IParameter a = (IParameter) collector.getName(1).resolveBinding();

        assertInstances(collector, f, 2);
        assertInstances(collector, a, 3);
    }

    public void testSimpleFunctionCall() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f();              \n"); //$NON-NLS-1$
        buffer.append("void g() {             \n"); //$NON-NLS-1$
        buffer.append("   f();                \n"); //$NON-NLS-1$
        buffer.append("}                      \n"); //$NON-NLS-1$
        buffer.append("void f(){ }            \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        IFunction f = (IFunction) collector.getName(0).resolveBinding();
        IFunction g = (IFunction) collector.getName(1).resolveBinding();

        assertInstances(collector, f, 3);
        assertInstances(collector, g, 1);
    }

    public void testForLoop() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f() {                         \n"); //$NON-NLS-1$
        buffer.append("   for( int i = 0; i < 5; i++ ) {  \n"); //$NON-NLS-1$         
        buffer.append("      i;                           \n"); //$NON-NLS-1$
        buffer.append("   }                               \n"); //$NON-NLS-1$
        buffer.append("}                                  \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        IVariable i = (IVariable) collector.getName(1).resolveBinding();

        assertInstances(collector, i, 4);
    }

    public void testExpressionFieldReference() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A { int x; };    \n"); //$NON-NLS-1$
        buffer.append("void f(){               \n"); //$NON-NLS-1$
        buffer.append("   ((struct A *) 1)->x; \n"); //$NON-NLS-1$
        buffer.append("}                       \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        ICPPClassType A = (ICPPClassType) collector.getName(0).resolveBinding();
        IField x = (IField) collector.getName(1).resolveBinding();

        assertInstances(collector, A, 2);
        assertInstances(collector, x, 2);
    }

    public void testEnumerations() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("enum hue { red, blue, green };     \n"); //$NON-NLS-1$
        buffer.append("enum hue col, *cp;                 \n"); //$NON-NLS-1$
        buffer.append("void f() {                         \n"); //$NON-NLS-1$
        buffer.append("   col = blue;                     \n"); //$NON-NLS-1$
        buffer.append("   cp = &col;                      \n"); //$NON-NLS-1$
        buffer.append("   if( *cp != red )                \n"); //$NON-NLS-1$
        buffer.append("      return;                      \n"); //$NON-NLS-1$
        buffer.append("}                                  \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        IEnumeration hue = (IEnumeration) collector.getName(0).resolveBinding();
        IEnumerator red = (IEnumerator) collector.getName(1).resolveBinding();
        IEnumerator blue = (IEnumerator) collector.getName(2).resolveBinding();
        IEnumerator green = (IEnumerator) collector.getName(3).resolveBinding();
        IVariable col = (IVariable) collector.getName(5).resolveBinding();
        IVariable cp = (IVariable) collector.getName(6).resolveBinding();

        assertInstances(collector, hue, 2);
        assertInstances(collector, red, 2);
        assertInstances(collector, blue, 2);
        assertInstances(collector, green, 1);
        assertInstances(collector, col, 3);
        assertInstances(collector, cp, 3);

        assertTrue(cp.getType() instanceof IPointerType);
        IPointerType pt = (IPointerType) cp.getType();
        assertSame(pt.getType(), hue);
    }

    public void testPointerToFunction() throws Exception {
        IASTTranslationUnit tu = parse("int (*pfi)();", ParserLanguage.CPP); //$NON-NLS-1$
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);
        IVariable pf = (IVariable) collector.getName(0).resolveBinding();
        IPointerType pt = (IPointerType) pf.getType();
        assertTrue(pt.getType() instanceof IFunctionType);

        tu = parse(
                "struct A; int (*pfi)( int, struct A * );", ParserLanguage.CPP); //$NON-NLS-1$
        collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);
        ICPPClassType A = (ICPPClassType) collector.getName(0).resolveBinding();
        pf = (IVariable) collector.getName(1).resolveBinding();
        pt = (IPointerType) pf.getType();
        assertTrue(pt.getType() instanceof IFunctionType);
        IFunctionType ft = (IFunctionType) pt.getType();
        IType[] params = ft.getParameterTypes();
        assertTrue(params[0] instanceof IBasicType);
        assertTrue(params[1] instanceof IPointerType);
        pt = (IPointerType) params[1];
        assertSame(pt.getType(), A);
    }

    public void testFunctionTypes() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct A;                           \n"); //$NON-NLS-1$
        buffer.append("int * f( int i, char c );           \n"); //$NON-NLS-1$
        buffer.append("void ( *g ) ( A * );         \n"); //$NON-NLS-1$
        buffer.append("void (* (*h)(A**) ) ( int ); \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTElaboratedTypeSpecifier elabSpec = (IASTElaboratedTypeSpecifier) decl
                .getDeclSpecifier();
        ICompositeType A = (ICompositeType) elabSpec.getName().resolveBinding();

        decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
        IFunction f = (IFunction) decl.getDeclarators()[0].getName()
                .resolveBinding();

        decl = (IASTSimpleDeclaration) tu.getDeclarations()[2];
        IVariable g = (IVariable) decl.getDeclarators()[0]
                .getNestedDeclarator().getName().resolveBinding();

        decl = (IASTSimpleDeclaration) tu.getDeclarations()[3];
        IVariable h = (IVariable) decl.getDeclarators()[0]
                .getNestedDeclarator().getNestedDeclarator().getName()
                .resolveBinding();

        IFunctionType t_f = f.getType();
        IType t_f_return = t_f.getReturnType();
        assertTrue(t_f_return instanceof IPointerType);
        assertTrue(((IPointerType) t_f_return).getType() instanceof IBasicType);
        IType[] t_f_params = t_f.getParameterTypes();
        assertEquals(t_f_params.length, 2);
        assertTrue(t_f_params[0] instanceof IBasicType);
        assertTrue(t_f_params[1] instanceof IBasicType);

        //g is a pointer to a function that returns void and has 1 parameter
        // struct A *
        IType t_g = g.getType();
        assertTrue(t_g instanceof IPointerType);
        assertTrue(((IPointerType) t_g).getType() instanceof IFunctionType);
        IFunctionType t_g_func = (IFunctionType) ((IPointerType) t_g).getType();
        IType t_g_func_return = t_g_func.getReturnType();
        assertTrue(t_g_func_return instanceof IBasicType);
        IType[] t_g_func_params = t_g_func.getParameterTypes();
        assertEquals(t_g_func_params.length, 1);
        IType t_g_func_p1 = t_g_func_params[0];
        assertTrue(t_g_func_p1 instanceof IPointerType);
        assertSame(((IPointerType) t_g_func_p1).getType(), A);

        //h is a pointer to a function that returns a pointer to a function
        //the returned pointer to function returns void and takes 1 parameter
        // int
        // the *h function takes 1 parameter struct A**
        IType t_h = h.getType();
        assertTrue(t_h instanceof IPointerType);
        assertTrue(((IPointerType) t_h).getType() instanceof IFunctionType);
        IFunctionType t_h_func = (IFunctionType) ((IPointerType) t_h).getType();
        IType t_h_func_return = t_h_func.getReturnType();
        IType[] t_h_func_params = t_h_func.getParameterTypes();
        assertEquals(t_h_func_params.length, 1);
        IType t_h_func_p1 = t_h_func_params[0];
        assertTrue(t_h_func_p1 instanceof IPointerType);
        assertTrue(((IPointerType) t_h_func_p1).getType() instanceof IPointerType);
        assertSame(((IPointerType) ((IPointerType) t_h_func_p1).getType())
                .getType(), A);

        assertTrue(t_h_func_return instanceof IPointerType);
        IFunctionType h_return = (IFunctionType) ((IPointerType) t_h_func_return)
                .getType();
        IType h_r = h_return.getReturnType();
        IType[] h_ps = h_return.getParameterTypes();
        assertTrue(h_r instanceof IBasicType);
        assertEquals(h_ps.length, 1);
        assertTrue(h_ps[0] instanceof IBasicType);
    }

    public void testFnReturningPtrToFn() throws Exception {
        IASTTranslationUnit tu = parse(
                "void ( * f( int ) )(){}", ParserLanguage.CPP); //$NON-NLS-1$

        IASTFunctionDefinition def = (IASTFunctionDefinition) tu
                .getDeclarations()[0];
        IFunction f = (IFunction) def.getDeclarator().getNestedDeclarator()
                .getName().resolveBinding();

        IFunctionType ft = f.getType();
        assertTrue(ft.getReturnType() instanceof IPointerType);
        assertTrue(((IPointerType) ft.getReturnType()).getType() instanceof IFunctionType);
        assertEquals(ft.getParameterTypes().length, 1);
    }

    public void testUsingDeclaration_1() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f();                  \n"); //$NON-NLS-1$
        buffer.append("namespace A {              \n"); //$NON-NLS-1$
        buffer.append("   void g();               \n"); //$NON-NLS-1$
        buffer.append("}                          \n"); //$NON-NLS-1$
        buffer.append("namespace X {              \n"); //$NON-NLS-1$
        buffer.append("   using ::f;              \n"); //$NON-NLS-1$
        buffer.append("   using A::g;             \n"); //$NON-NLS-1$
        buffer.append("}                          \n"); //$NON-NLS-1$
        buffer.append("void h() {                 \n"); //$NON-NLS-1$
        buffer.append("   X::f();                 \n"); //$NON-NLS-1$
        buffer.append("   X::g();                 \n"); //$NON-NLS-1$
        buffer.append("}                          \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        IFunction f = (IFunction) collector.getName(0).resolveBinding();
        ICPPNamespace A = (ICPPNamespace) collector.getName(1).resolveBinding();
        IFunction g = (IFunction) collector.getName(2).resolveBinding();
        ICPPNamespace X = (ICPPNamespace) collector.getName(3).resolveBinding();

        assertInstances(collector, f, 5);
        assertInstances(collector, A, 2);
        assertInstances(collector, X, 3);
        assertInstances(collector, g, 5);
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

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        IFunction f = (IFunction) collector.getName(1).resolveBinding();
        IFunction g = (IFunction) collector.getName(8).resolveBinding();
        IFunction h = (IFunction) collector.getName(12).resolveBinding();

        assertInstances(collector, f, 3);
        assertInstances(collector, g, 2);
        assertInstances(collector, h, 2);
    }

    public void testProblem_AmbiguousInParent() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class P1 { public: int x; };        \n"); //$NON-NLS-1$
        buffer.append("class P2 { public: int x; };        \n"); //$NON-NLS-1$
        buffer.append("class B : public P1, public P2 {};  \n"); //$NON-NLS-1$
        buffer.append("void main() {                       \n"); //$NON-NLS-1$
        buffer.append("   B * b = new B();                 \n"); //$NON-NLS-1$
        buffer.append("   b->x;                            \n"); //$NON-NLS-1$
        buffer.append("}                                   \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        IProblemBinding x = (IProblemBinding) collector.getName(12)
                .resolveBinding();
        assertEquals(x.getID(), IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP);
    }

    public void testVirtualParentLookup() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class D { public: int x; };       \n"); //$NON-NLS-1$
        buffer.append("class C : public virtual D {};    \n"); //$NON-NLS-1$
        buffer.append("class B : public virtual D {};    \n"); //$NON-NLS-1$
        buffer.append("class A : public B, public C {};  \n"); //$NON-NLS-1$
        buffer.append("void main() {                     \n"); //$NON-NLS-1$
        buffer.append("   A * a = new A();               \n"); //$NON-NLS-1$
        buffer.append("   a->x;                          \n"); //$NON-NLS-1$
        buffer.append("}                                 \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        assertEquals(collector.size(), 15);

        ICPPClassType D = (ICPPClassType) collector.getName(0).resolveBinding();
        ICPPField x = (ICPPField) collector.getName(1).resolveBinding();
        ICPPClassType C = (ICPPClassType) collector.getName(2).resolveBinding();
        ICPPClassType B = (ICPPClassType) collector.getName(4).resolveBinding();
        ICPPClassType A = (ICPPClassType) collector.getName(6).resolveBinding();
        ICPPConstructor ctor = A.getConstructors()[0];
        assertInstances(collector, D, 3);
        assertInstances(collector, C, 2);
        assertInstances(collector, B, 2);
        assertInstances(collector, A, 2);
        assertInstances(collector, ctor, 1);
        assertInstances(collector, x, 2);
    }

    public void testAmbiguousVirtualParentLookup() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class D { public: int x; };        \n"); //$NON-NLS-1$
        buffer.append("class C : public D {};             \n"); //$NON-NLS-1$
        buffer.append("class B : public D {};             \n"); //$NON-NLS-1$
        buffer.append("class A : public B, public C {};   \n"); //$NON-NLS-1$
        buffer.append("void main() {                      \n"); //$NON-NLS-1$
        buffer.append("   A * a = new A();                \n"); //$NON-NLS-1$
        buffer.append("   a->x;                           \n"); //$NON-NLS-1$
        buffer.append("}                                  \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        assertEquals(collector.size(), 15);

        ICPPClassType D = (ICPPClassType) collector.getName(0).resolveBinding();
        ICPPField x1 = (ICPPField) collector.getName(1).resolveBinding();
        ICPPClassType C = (ICPPClassType) collector.getName(2).resolveBinding();
        ICPPClassType B = (ICPPClassType) collector.getName(4).resolveBinding();
        ICPPClassType A = (ICPPClassType) collector.getName(6).resolveBinding();
        ICPPConstructor ctor = A.getConstructors()[0];
        IProblemBinding x2 = (IProblemBinding) collector.getName(14)
                .resolveBinding();
        assertEquals(x2.getID(), IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP);

        assertInstances(collector, D, 3);
        assertInstances(collector, C, 2);
        assertInstances(collector, B, 2);
        assertInstances(collector, A, 2);
        assertInstances(collector, ctor, 1);
        assertInstances(collector, x1, 1);
    }

    public void testExtendedNamespaces() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace A {                      \n"); //$NON-NLS-1$
        buffer.append("   int x;                          \n"); //$NON-NLS-1$
        buffer.append("}                                  \n"); //$NON-NLS-1$
        buffer.append("int x;                             \n"); //$NON-NLS-1$
        buffer.append("namespace A {                      \n"); //$NON-NLS-1$
        buffer.append("   void f() { x; }                 \n"); //$NON-NLS-1$
        buffer.append("}                                  \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector collector = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, collector);

        assertEquals(collector.size(), 6);

        ICPPNamespace A = (ICPPNamespace) collector.getName(0).resolveBinding();
        IVariable x1 = (IVariable) collector.getName(1).resolveBinding();
        IVariable x2 = (IVariable) collector.getName(2).resolveBinding();

        assertInstances(collector, A, 2);
        assertInstances(collector, x1, 2);
        assertInstances(collector, x2, 1);
    }

    public void testImplicitConstructors() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class A { }; "); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl
                .getDeclSpecifier();
        ICPPClassType A = (ICPPClassType) compSpec.getName().resolveBinding();
        ICPPConstructor[] ctors = A.getConstructors();

        assertNotNull(ctors);
        assertEquals(ctors.length, 2);

        assertEquals(ctors[0].getParameters().length, 0);
        assertEquals(ctors[1].getParameters().length, 1);

        IType t = ctors[1].getParameters()[0].getType();
        assertTrue(t instanceof ICPPReferenceType);
        assertTrue(((ICPPReferenceType) t).getType() instanceof IQualifierType);
        IQualifierType qt = (IQualifierType) ((ICPPReferenceType) t).getType();
        assertTrue(qt.isConst());
        assertSame(qt.getType(), A);
    }

    public void testConstructors() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class A { A();  A( const A & ); }; "); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl
                .getDeclSpecifier();
        ICPPClassType A = (ICPPClassType) compSpec.getName().resolveBinding();
        ICPPConstructor[] ctors = A.getConstructors();

        assertNotNull(ctors);
        assertEquals(ctors.length, 2);

        assertEquals(ctors[0].getParameters().length, 0);
        assertEquals(ctors[1].getParameters().length, 1);

        IType t = ctors[1].getParameters()[0].getType();
        assertTrue(t instanceof ICPPReferenceType);
        assertTrue(((ICPPReferenceType) t).getType() instanceof IQualifierType);
        IQualifierType qt = (IQualifierType) ((ICPPReferenceType) t).getType();
        assertTrue(qt.isConst());
        assertSame(qt.getType(), A);
    }

    public void testNamespaceAlias() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace A { int x; }   \n"); //$NON-NLS-1$
        buffer.append("namespace B = A;         \n"); //$NON-NLS-1$
        buffer.append("int f(){ B::x;  }        \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        assertEquals(col.size(), 8);
        ICPPNamespace A = (ICPPNamespace) col.getName(0).resolveBinding();
        IVariable x = (IVariable) col.getName(1).resolveBinding();

        assertInstances(col, A, 4);
        assertInstances(col, x, 3);
    }

    //   public void testBug84250() throws Exception {
    //      assertTrue(((IASTDeclarationStatement) ((IASTCompoundStatement)
    // ((IASTFunctionDefinition) parse(
    //            "void f() { int (*p) [2]; }",
    // ParserLanguage.CPP).getDeclarations()[0]).getBody()).getStatements()[0]).getDeclaration()
    // instanceof IASTSimpleDeclaration); //$NON-NLS-1$
    //   }

    public void testBug84250() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f() {                 \n"); //$NON-NLS-1$
        buffer.append("   int ( *p ) [2];         \n"); //$NON-NLS-1$
        buffer.append("   (&p)[0] = 1;            \n"); //$NON-NLS-1$
        buffer.append("}                          \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        assertEquals(col.size(), 3);
        IVariable p = (IVariable) col.getName(1).resolveBinding();
        assertTrue(p.getType() instanceof IPointerType);
        assertTrue(((IPointerType) p.getType()).getType() instanceof IArrayType);
        IArrayType at = (IArrayType) ((IPointerType) p.getType()).getType();
        assertTrue(at.getType() instanceof IBasicType);

        assertInstances(col, p, 2);
    }

    public void testBug84250_2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f() {                 \n"); //$NON-NLS-1$
        buffer.append("   int ( *p ) [2];         \n"); //$NON-NLS-1$
        buffer.append("   (&p)[0] = 1;            \n"); //$NON-NLS-1$
        buffer.append("}                          \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        assertEquals(col.size(), 3);

        IVariable p_ref = (IVariable) col.getName(2).resolveBinding();
        IVariable p_decl = (IVariable) col.getName(1).resolveBinding();

        assertSame(p_ref, p_decl);
    }

    public void testBug84266() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct s { double i; } f(void);  \n"); //$NON-NLS-1$
        buffer.append("struct s f(void){}               \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        assertEquals(col.size(), 7);

        ICompositeType s_ref = (ICompositeType) col.getName(4).resolveBinding();
        ICompositeType s_decl = (ICompositeType) col.getName(0)
                .resolveBinding();

        assertSame(s_ref, s_decl);
    }

    public void testBug84266_2() throws Exception {
        IASTTranslationUnit tu = parse("struct s f(void);", ParserLanguage.CPP); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        assertEquals(col.size(), 3);

        ICompositeType s = (ICompositeType) col.getName(0).resolveBinding();
        assertNotNull(s);

        tu = parse("struct s f(void){}", ParserLanguage.CPP); //$NON-NLS-1$
        col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        assertEquals(col.size(), 3);

        s = (ICompositeType) col.getName(0).resolveBinding();
        assertNotNull(s);
    }

    public void testBug84228() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f( int m, int c[m][m] );        \n"); //$NON-NLS-1$
        buffer.append("void f( int m, int c[m][m] ){        \n"); //$NON-NLS-1$
        buffer.append("   int x;                            \n"); //$NON-NLS-1$
        buffer.append("   { int x = x; }                    \n"); //$NON-NLS-1$
        buffer.append("}                                    \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        assertEquals(col.size(), 13);

        IParameter m = (IParameter) col.getName(3).resolveBinding();
        IVariable x3 = (IVariable) col.getName(12).resolveBinding();
        IVariable x2 = (IVariable) col.getName(11).resolveBinding();
        IVariable x1 = (IVariable) col.getName(10).resolveBinding();

        assertSame(x2, x3);
        assertNotSame(x1, x2);

        assertInstances(col, m, 6);
        assertInstances(col, x1, 1);
        assertInstances(col, x2, 2);
    }

    public void testBug84615() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class A { public : static int n; };  \n"); //$NON-NLS-1$
        buffer.append("int main() {                         \n"); //$NON-NLS-1$
        buffer.append("   int A;                            \n"); //$NON-NLS-1$
        buffer.append("   A::n = 42;                        \n"); //$NON-NLS-1$
        buffer.append("   A b;                              \n"); //$NON-NLS-1$
        buffer.append("}                                    \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        assertEquals(col.size(), 9);

        ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
        ICPPField n = (ICPPField) col.getName(1).resolveBinding();
        IBinding Aref = col.getName(5).resolveBinding();
        IBinding nref = col.getName(6).resolveBinding();
        IProblemBinding prob = (IProblemBinding) col.getName(7)
                .resolveBinding();

        assertSame(A, Aref);
        assertSame(n, nref);
        assertNotNull(prob);
    }

    public void testBug84371() throws Exception {
        String code = "int x = ::ABC::DEF::ghi;"; //$NON-NLS-1$
        IASTTranslationUnit tu = parse(code, ParserLanguage.CPP);
        IASTSimpleDeclaration x = (IASTSimpleDeclaration) tu.getDeclarations()[0];
        IASTInitializerExpression e = (IASTInitializerExpression) x
                .getDeclarators()[0].getInitializer();
        IASTIdExpression id = (IASTIdExpression) e.getExpression();
        ICPPASTQualifiedName name = (ICPPASTQualifiedName) id.getName();
        assertTrue(name.isFullyQualified());
        assertEquals(name.getNames().length, 3);
        assertEquals(name.getNames()[0].toString(), "ABC"); //$NON-NLS-1$
        assertEquals(name.getNames()[1].toString(), "DEF"); //$NON-NLS-1$
        assertEquals(name.getNames()[2].toString(), "ghi"); //$NON-NLS-1$
    }

    public void testBug84679() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer
                .append("namespace Y { void f(float); }                         \n"); //$NON-NLS-1$
        buffer
                .append("namespace A { using namespace Y; f(int); }             \n"); //$NON-NLS-1$
        buffer
                .append("namespace B { void f(char);  }                         \n"); //$NON-NLS-1$
        buffer
                .append("namespace AB { using namespace A; using namespace B; } \n"); //$NON-NLS-1$
        buffer.append("void h(){         \n"); //$NON-NLS-1$
        buffer.append("   AB::f(1);      \n"); //$NON-NLS-1$
        buffer.append("   AB::f(’c’);    \n"); //$NON-NLS-1$
        buffer.append("}                 \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        ICPPNamespace Y = (ICPPNamespace) col.getName(0).resolveBinding();
        ICPPNamespace A = (ICPPNamespace) col.getName(3).resolveBinding();
        ICPPNamespace B = (ICPPNamespace) col.getName(7).resolveBinding();
        ICPPNamespace AB = (ICPPNamespace) col.getName(10).resolveBinding();

        IFunction f = (IFunction) col.getName(16).resolveBinding();
        IFunction fdef = (IFunction) col.getName(5).resolveBinding();
        IProblemBinding f2 = (IProblemBinding) col.getName(19).resolveBinding();
        assertSame(f, fdef);
        assertEquals(IProblemBinding.SEMANTIC_NAME_NOT_FOUND, f2.getID());
        assertInstances(col, Y, 2);
        assertInstances(col, A, 2);
        assertInstances(col, B, 2);
        assertInstances(col, AB, 3);
    }

    public void testBug84692() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct Node {          \n"); //$NON-NLS-1$
        buffer.append("   struct Node* Next;  \n"); //$NON-NLS-1$
        buffer.append("   struct Data* Data;  \n"); //$NON-NLS-1$
        buffer.append("};                     \n"); //$NON-NLS-1$
        buffer.append("struct Data {          \n"); //$NON-NLS-1$
        buffer.append("   struct Node * node; \n"); //$NON-NLS-1$
        buffer.append("   friend struct Glob; \n"); //$NON-NLS-1$
        buffer.append("};                     \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        assertEquals(col.size(), 9);

        ICPPClassType Node = (ICPPClassType) col.getName(1).resolveBinding();
        ICPPClassType Data = (ICPPClassType) col.getName(3).resolveBinding();
        assertSame( Data.getScope(),  tu.getScope() );
        
        assertInstances(col, Node, 3);
        assertInstances(col, Data, 2);
    }

    public void testBug84686() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace B { int b; }                        \n"); //$NON-NLS-1$
        buffer.append("namespace A { using namespace B;  int a;  }   \n"); //$NON-NLS-1$
        buffer.append("namespace B { using namespace A; }            \n"); //$NON-NLS-1$
        buffer.append("void f() { B::a++;  }                         \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        assertEquals(col.size(), 11);

        IVariable a1 = (IVariable) col.getName(4).resolveBinding();
        IVariable a2 = (IVariable) col.getName(10).resolveBinding();
        assertSame(a1, a2);
    }

    public void testBug84705() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct C {                               \n"); //$NON-NLS-1$
        buffer.append("   void f();                             \n"); //$NON-NLS-1$
        buffer.append("   const C& operator=( const C& );       \n"); //$NON-NLS-1$
        buffer.append("};                                       \n"); //$NON-NLS-1$
        buffer.append("const C& C::operator=( const C& other) { \n"); //$NON-NLS-1$
        buffer.append("   if( this != &other ) {                \n"); //$NON-NLS-1$
        buffer.append("      this->~C();                        \n"); //$NON-NLS-1$
        buffer.append("      new (this) C(other );              \n"); //$NON-NLS-1$
        buffer.append("      f();                               \n"); //$NON-NLS-1$
        buffer.append("   }                                     \n"); //$NON-NLS-1$
        buffer.append("   return *this;                         \n"); //$NON-NLS-1$
        buffer.append("}                                        \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        assertEquals(col.size(), 17);

        ICPPMethod f = (ICPPMethod) col.getName(1).resolveBinding();
        IASTName[] refs = tu.getReferences(f);
        assertEquals(1, refs.length);
        assertSame(f, refs[0].resolveBinding());

        ICPPClassType C = (ICPPClassType) col.getName(0).resolveBinding();
        ICPPMethod op = (ICPPMethod) col.getName(3).resolveBinding();
        IParameter other = (IParameter) col.getName(11).resolveBinding();
        ICPPMethod dtor = (ICPPMethod) col.getName(13).resolveBinding();
        assertNotNull(dtor);
        assertEquals(dtor.getName(), "~C"); //$NON-NLS-1$
        assertInstances(col, C, 6);

        assertInstances(col, op, 3);
        assertInstances(col, other, 4);
    }

    public void testThis() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class A { void f(); void g() const; };   \n"); //$NON-NLS-1$
        buffer.append("void A::f(){ this; }                     \n"); //$NON-NLS-1$
        buffer.append("void A::g() const { *this; }             \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);

        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu
                .getDeclarations()[0];
        ICPPClassType A = (ICPPClassType) ((IASTCompositeTypeSpecifier) decl
                .getDeclSpecifier()).getName().resolveBinding();

        IASTFunctionDefinition def = (IASTFunctionDefinition) tu
                .getDeclarations()[1];
        IASTExpressionStatement expStatement = (IASTExpressionStatement) ((IASTCompoundStatement) def
                .getBody()).getStatements()[0];
        assertTrue(expStatement.getExpression() instanceof IASTLiteralExpression);
        IType type = CPPVisitor.getExpressionType(expStatement.getExpression());

        assertTrue(type instanceof IPointerType);
        assertSame(((IPointerType) type).getType(), A);

        def = (IASTFunctionDefinition) tu.getDeclarations()[2];
        expStatement = (IASTExpressionStatement) ((IASTCompoundStatement) def
                .getBody()).getStatements()[0];
        IASTUnaryExpression ue = (IASTUnaryExpression) expStatement
                .getExpression();
        type = CPPVisitor.getExpressionType(ue);

   		assertTrue( type instanceof IQualifierType );
   		assertSame( ((IQualifierType) type).getType(), A );
   		assertTrue( ((IQualifierType) type).isConst() );
    }

    public void testBug84710() throws Exception {
        IASTTranslationUnit tu = parse("class T { T(); };", ParserLanguage.CPP); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);
        ICPPConstructor T = (ICPPConstructor) col.getName(1).resolveBinding();
        assertTrue(CharArrayUtils.equals(T.getNameCharArray(),
                "T".toCharArray())); //$NON-NLS-1$
        assertEquals(T.getName(), "T"); //$NON-NLS-1$
    }

    public void testArgumentDependantLookup() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace NS {                \n"); //$NON-NLS-1$
        buffer.append("   class T {};                \n"); //$NON-NLS-1$
        buffer.append("   void f( T );               \n"); //$NON-NLS-1$
        buffer.append("}                             \n"); //$NON-NLS-1$
        buffer.append("NS::T parm;                   \n"); //$NON-NLS-1$
        buffer.append("int main() {                  \n"); //$NON-NLS-1$
        buffer.append("   f( parm );                 \n"); //$NON-NLS-1$
        buffer.append("}                             \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        ICPPNamespace NS = (ICPPNamespace) col.getName(0).resolveBinding();
        ICPPClassType T = (ICPPClassType) col.getName(1).resolveBinding();
        IFunction f = (IFunction) col.getName(2).resolveBinding();
        IVariable parm = (IVariable) col.getName(8).resolveBinding();

        assertInstances(col, NS, 2);
        assertInstances(col, T, 4);
        assertInstances(col, f, 2);
        assertInstances(col, parm, 2);
    }

    public void testArgumentDependantLookup_2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace NS1{                       \n"); //$NON-NLS-1$
        buffer.append("   void f( void * );                 \n"); //$NON-NLS-1$
        buffer.append("}                                    \n"); //$NON-NLS-1$
        buffer.append("namespace NS2{                       \n"); //$NON-NLS-1$
        buffer.append("   using namespace NS1;              \n"); //$NON-NLS-1$
        buffer.append("   class B {};                       \n"); //$NON-NLS-1$
        buffer.append("   void f( void * );                 \n"); //$NON-NLS-1$
        buffer.append("}                                    \n"); //$NON-NLS-1$
        buffer.append("class A : public NS2::B {} *a;       \n"); //$NON-NLS-1$
        buffer.append("int main() {                         \n"); //$NON-NLS-1$
        buffer.append("   f( a );                           \n"); //$NON-NLS-1$
        buffer.append("}                                    \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        IFunction fref = (IFunction) col.getName(14).resolveBinding();
        IFunction f1 = (IFunction) col.getName(1).resolveBinding();
        IFunction f2 = (IFunction) col.getName(6).resolveBinding();

        assertSame(f2, fref);
        assertNotNull(f1);
        assertNotNull(f2);
    }

    public void testBug84610() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace { int i; } //1\n"); //$NON-NLS-1$
        buffer.append("void f(){ i; }          \n"); //$NON-NLS-1$
        buffer.append("namespace A {           \n"); //$NON-NLS-1$
        buffer.append("   namespace {          \n"); //$NON-NLS-1$
        buffer.append("      int i;    //2     \n"); //$NON-NLS-1$
        buffer.append("      int j;            \n"); //$NON-NLS-1$
        buffer.append("   }                    \n"); //$NON-NLS-1$
        buffer.append("   void g(){ i; }       \n"); //$NON-NLS-1$
        buffer.append("}                       \n"); //$NON-NLS-1$
        buffer.append("using namespace A;      \n"); //$NON-NLS-1$
        buffer.append("void h() {              \n"); //$NON-NLS-1$
        buffer.append("   i;    //ambiguous    \n"); //$NON-NLS-1$
        buffer.append("   A::i; //i2           \n"); //$NON-NLS-1$
        buffer.append("   j;                   \n"); //$NON-NLS-1$
        buffer.append("}                       \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        assertEquals(17, col.size());

        IVariable i1 = (IVariable) col.getName(1).resolveBinding();
        IVariable i2 = (IVariable) col.getName(6).resolveBinding();
        IVariable j = (IVariable) col.getName(7).resolveBinding();

        assertInstances(col, i1, 2);
        assertInstances(col, i2, 4);
        assertInstances(col, j, 2);

        IProblemBinding problem = (IProblemBinding) col.getName(12)
                .resolveBinding();
        assertEquals(IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, problem.getID());
    }

    public void testBug84703() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct B {          \n"); //$NON-NLS-1$
        buffer.append("   void mutate();   \n"); //$NON-NLS-1$
        buffer.append("};                  \n"); //$NON-NLS-1$
        buffer.append("void g() {          \n"); //$NON-NLS-1$
        buffer.append("   B* pb = new B(); \n"); //$NON-NLS-1$
        buffer.append("   pb->mutate();    \n"); //$NON-NLS-1$
        buffer.append("}                   \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        assertEquals(8, col.size());

        ICPPMethod mutate = (ICPPMethod) col.getName(1).resolveBinding();
        ICPPClassType B = (ICPPClassType) col.getName(0).resolveBinding();
        IVariable pb = (IVariable) col.getName(4).resolveBinding();

        assertInstances(col, pb, 2);
        assertInstances(col, mutate, 2);
        assertInstances(col, B, 2);
    }

    public void testBug84469() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct S { int i; };        \n"); //$NON-NLS-1$
        buffer.append("void f() {         ;        \n"); //$NON-NLS-1$
        buffer.append("   int S::* pm = &S::i;      \n"); //$NON-NLS-1$
        buffer.append("}                           \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        assertEquals(9, col.size());
    }

    public void testPointerToMemberType() throws Exception {
        IASTTranslationUnit tu = parse("struct S; int S::* pm;", //$NON-NLS-1$
                ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        assertEquals(4, col.size());

        IVariable pm = (IVariable) col.getName(3).resolveBinding();
        ICPPClassType S = (ICPPClassType) col.getName(0).resolveBinding();

        IType t = pm.getType();
        assertNotNull(t);
        assertTrue(t instanceof ICPPPointerToMemberType);
        ICPPClassType cls = (ICPPClassType) ((ICPPPointerToMemberType) t).getMemberOfClass();
        assertSame(S, cls);
        assertTrue(((ICPPPointerToMemberType) t).getType() instanceof IBasicType);
    }

    public void testBug_PM_() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct S { int i ; } *s; \n"); //$NON-NLS-1$
        buffer.append("int S::* pm = &S::i;     \n"); //$NON-NLS-1$
        buffer.append("void f() {               \n"); //$NON-NLS-1$
        buffer.append("   s->*pm = 1;           \n"); //$NON-NLS-1$
        buffer.append("}                        \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        IBinding ref = col.getName(11).resolveBinding();
        IVariable pm = (IVariable) col.getName(5).resolveBinding();

        assertSame(pm, ref);
    }

    public void testBug_PM_2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct S {               \n"); //$NON-NLS-1$
        buffer.append("   int i;                \n"); //$NON-NLS-1$
        buffer.append("   S* f();               \n"); //$NON-NLS-1$
        buffer.append("} *s;                    \n"); //$NON-NLS-1$
        buffer.append("S* (S::* pm) () = &S::f; \n"); //$NON-NLS-1$
        buffer.append("void foo() {             \n"); //$NON-NLS-1$
        buffer.append("   (s->*pm)()->i;        \n"); //$NON-NLS-1$
        buffer.append("}                        \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);

        ICPPClassType S = (ICPPClassType) col.getName(0).resolveBinding();
        IVariable pm = (IVariable) col.getName(8).resolveBinding();
        IField i = (IField) col.getName(1).resolveBinding();
        ICPPMethod f = (ICPPMethod) col.getName(3).resolveBinding();

        IType t = pm.getType();
        assertTrue(t instanceof ICPPPointerToMemberType);
        IFunctionType ft = (IFunctionType) ((ICPPPointerToMemberType) t)
                .getType();
        ICPPClassType ST = (ICPPClassType) ((ICPPPointerToMemberType) t)
                .getMemberOfClass();

        assertTrue(ft.getReturnType() instanceof IPointerType);
        assertSame(ST, ((IPointerType) ft.getReturnType()).getType());
        assertSame(S, ST);

        assertInstances(col, S, 5);
        assertInstances(col, pm, 2);
        assertInstances(col, i, 2);
        assertInstances(col, f, 3);
    }

    public void _testBug84469() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct S { int i; };        \n"); //$NON-NLS-1$
        buffer.append("void f() {         ;        \n"); //$NON-NLS-1$
        buffer.append("   int S::* pm = &S::i;      \n"); //$NON-NLS-1$
        buffer.append("}                           \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);
    }

    public void testFindTypeBinding_1() throws Exception {
        IASTTranslationUnit tu = parse(
                "int x = 5; int y(x);", ParserLanguage.CPP); //$NON-NLS-1$

        IASTStandardFunctionDeclarator fdtor = (IASTStandardFunctionDeclarator) ((IASTSimpleDeclaration) tu
                .getDeclarations()[1]).getDeclarators()[0];
        IASTName name = fdtor.getParameters()[0].getDeclarator().getName();
        IBinding binding = CPPSemantics.findTypeBinding(tu, name);
        assertNull(binding);

        tu = parse("struct x; int y(x);", ParserLanguage.CPP); //$NON-NLS-1$

        fdtor = (IASTStandardFunctionDeclarator) ((IASTSimpleDeclaration) tu
                .getDeclarations()[1]).getDeclarators()[0];
        name = ((ICPPASTNamedTypeSpecifier) fdtor.getParameters()[0]
                .getDeclSpecifier()).getName();
        binding = CPPSemantics.findTypeBinding(tu, name);
        assertNotNull(binding);
        assertTrue(binding instanceof ICPPClassType);
    }

    public void testFindTypeBinding_2() throws Exception {
        IASTTranslationUnit tu = parse(
                "struct B; void f() { B * bp; }", ParserLanguage.CPP); //$NON-NLS-1$
        IASTCompoundStatement compound = (IASTCompoundStatement) ((IASTFunctionDefinition) tu
                .getDeclarations()[1]).getBody();
        IASTBinaryExpression b = (IASTBinaryExpression) ((IASTExpressionStatement)compound.getStatements()[0]).getExpression();
        IBinding binding = ((IASTIdExpression)b.getOperand1()).getName().resolveBinding();
//        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) ((IASTDeclarationStatement) compound
//                .getStatements()[0]).getDeclaration();
//        IBinding binding = CPPSemantics.findTypeBinding(compound,
//                ((ICPPASTNamedTypeSpecifier)decl.getDeclSpecifier()).getName());
        assertNotNull(binding);
        assertTrue(binding instanceof ICPPClassType);
    }

//    public void testBug85049() throws Exception {
//        StringBuffer buffer = new StringBuffer( "struct B { };\n" ); //$NON-NLS-1$
//        buffer.append( "void g() {\n" ); //$NON-NLS-1$
//        buffer.append( "B * bp;  //1\n" ); //$NON-NLS-1$
//        buffer.append( "}\n" ); //$NON-NLS-1$
//        IASTTranslationUnit t = parse( buffer.toString(), ParserLanguage.CPP );
//        IASTFunctionDefinition g = (IASTFunctionDefinition) t.getDeclarations()[1];
//        IASTCompoundStatement body = (IASTCompoundStatement) g.getBody();
//        assertTrue( body.getStatements()[0] instanceof IASTDeclarationStatement );
//    }
    

    public void testPMConversions() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class A { public: int i; };             \n"); //$NON-NLS-1$
        buffer.append("class B : public A {};                  \n"); //$NON-NLS-1$
        buffer.append("void f( int B::* );                     \n"); //$NON-NLS-1$
        buffer.append("void g() {                              \n"); //$NON-NLS-1$
        buffer.append("   int A::* pm = &A::i;                 \n"); //$NON-NLS-1$
        buffer.append("   f( pm );                             \n"); //$NON-NLS-1$
        buffer.append("}                                       \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);
        
        IFunction f = (IFunction) col.getName(15).resolveBinding();
        ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
        ICPPField i = (ICPPField) col.getName(1).resolveBinding();
        ICPPClassType B = (ICPPClassType) col.getName(2).resolveBinding();
        IVariable pm = (IVariable) col.getName(11).resolveBinding();
        
        assertInstances( col, f, 2 );
        assertInstances( col, A, 4 );
        assertInstances( col, i, 3 );
        assertInstances( col, B, 2 );
        assertInstances( col, pm, 2 );
    }
    
    public void testPMKoenig() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "namespace N {                            \n" ); //$NON-NLS-1$
        buffer.append( "   class A { public: int i; };           \n" ); //$NON-NLS-1$
        buffer.append( "   void f( int A::* );                   \n" ); //$NON-NLS-1$
        buffer.append( "}                                        \n" ); //$NON-NLS-1$
        buffer.append( "int N::A::* pm = &N::A::i;               \n" ); //$NON-NLS-1$
        buffer.append( "void g() {                               \n" ); //$NON-NLS-1$
        buffer.append( "   f( pm );                              \n" ); //$NON-NLS-1$
        buffer.append( "}                                        \n" ); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);
        
        IFunction f = (IFunction) col.getName(16).resolveBinding();
        ICPPNamespace N = (ICPPNamespace) col.getName(0).resolveBinding();
        ICPPClassType A = (ICPPClassType) col.getName(1).resolveBinding();
        
        assertInstances( col, f, 2 );
        assertInstances( col, N, 3 );
        assertInstances( col, A, 4 );
    }
    
    public void testPMKoenig_2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "namespace M {                            \n" ); //$NON-NLS-1$
        buffer.append( "   class B { };                          \n" ); //$NON-NLS-1$
        buffer.append( "   void f( B* );                         \n" ); //$NON-NLS-1$
        buffer.append( "}                                        \n" ); //$NON-NLS-1$
        buffer.append( "namespace N {                            \n" ); //$NON-NLS-1$
        buffer.append( "   class A { public: M::B * b; };        \n" ); //$NON-NLS-1$
        buffer.append( "}                                        \n" ); //$NON-NLS-1$
        buffer.append( "M::B* N::A::* pm = &N::A::b;             \n" ); //$NON-NLS-1$
        buffer.append( "void g() {                               \n" ); //$NON-NLS-1$
        buffer.append( "   N::A * a;                             \n" ); //$NON-NLS-1$
        buffer.append( "   f( a->*pm );                          \n" ); //$NON-NLS-1$
        buffer.append( "}                                        \n" ); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);
        
        IFunction f = (IFunction) col.getName(27).resolveBinding();
        ICPPNamespace M = (ICPPNamespace) col.getName(0).resolveBinding();
        ICPPClassType B = (ICPPClassType) col.getName(1).resolveBinding();
        ICPPNamespace N = (ICPPNamespace) col.getName(5).resolveBinding();
        ICPPClassType A = (ICPPClassType) col.getName(6).resolveBinding();
        IVariable pm = (IVariable) col.getName(17).resolveBinding();
        
        assertInstances( col, f, 2 );
        assertInstances( col, M, 3 );
        assertInstances( col, B, 6 );
        assertInstances( col, N, 4 );
        assertInstances( col, A, 5 );
        assertInstances( col, pm, 2 );
    }
    
    public void testFriend_1() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class A {                       \n"); //$NON-NLS-1$
        buffer.append("   friend void set();           \n"); //$NON-NLS-1$
        buffer.append("   friend class B;              \n"); //$NON-NLS-1$
        buffer.append("};                              \n"); //$NON-NLS-1$
        buffer.append("void set();                     \n"); //$NON-NLS-1$
        buffer.append("class B{};                      \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);
        
        ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
        IFunction set = (IFunction) col.getName(1).resolveBinding();
        ICPPClassType B = (ICPPClassType) col.getName(2).resolveBinding();
        
        assertInstances( col, set, 2 );
        assertInstances( col, B, 2 );
        
        IBinding [] friends = A.getFriends();
        assertEquals( 2, friends.length );
        assertSame( friends[0], set );
        assertSame( friends[1], B );
    }
    
    public void testBug59149() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class A { friend class B; friend class B; }; \n"); //$NON-NLS-1$
        buffer.append("class B{};                                   \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);
        
        ICPPClassType B = (ICPPClassType) col.getName(2).resolveBinding();
        ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
        
        assertInstances( col, B, 3 );
        
        IBinding [] friends = A.getFriends();
        assertEquals( friends.length, 1 );
        assertSame( friends[0], B );
    }
    public void testBug59302() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "class A {                      \n"); //$NON-NLS-1$
        buffer.append( "   public: class N {};         \n"); //$NON-NLS-1$
        buffer.append( "};                             \n"); //$NON-NLS-1$
        buffer.append( "class B {                      \n"); //$NON-NLS-1$
        buffer.append( "   friend class A::N;          \n"); //$NON-NLS-1$
        buffer.append( "};                             \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);
        
        ICPPClassType N = (ICPPClassType) col.getName(5).resolveBinding();
        ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
        ICPPClassType B = (ICPPClassType) col.getName(2).resolveBinding();
        assertInstances( col, N, 3 );
        
        IBinding [] friends = B.getFriends();
        assertEquals( friends.length, 1 );
        assertSame( friends[0], N );
        
        assertEquals( A.getFriends().length, 0 );
        assertEquals( N.getFriends().length, 0 );
    }
    
    public void testBug75482() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "class A {                      \n"); //$NON-NLS-1$
        buffer.append( "   friend class B *helper();   \n");  //$NON-NLS-1$
        buffer.append( "};                             \n"); //$NON-NLS-1$
                
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        CPPVisitor.visitTranslationUnit(tu, col);
        
        IFunction helper = (IFunction) col.getName(2).resolveBinding();
        assertSame( helper.getScope(), tu.getScope() );
        
        ICPPClassType B = (ICPPClassType) col.getName(1).resolveBinding();
        ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
        assertSame( B.getScope(), A.getScope() );
        
        IBinding [] friends = A.getFriends();
        assertEquals( friends.length, 1 );
        assertSame( friends[0], helper );
    }
}

