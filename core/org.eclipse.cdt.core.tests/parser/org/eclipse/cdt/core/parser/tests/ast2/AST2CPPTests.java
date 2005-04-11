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
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
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
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
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
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPDelegate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPPointerType;
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
        tu.accept( collector);

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
        tu.accept( collector);

        assertEquals(collector.size(), 6);
        IVariable vA = (IVariable) collector.getName(0).resolveBinding();
        ICompositeType cA = (ICompositeType) collector.getName(1)
                .resolveBinding();
        IVariable a = (IVariable) collector.getName(5).resolveBinding();

        assertSame(a.getType(), cA);
        assertInstances(collector, vA, 2);
        assertInstances(collector, cA, 2);
        
        tu = parse(buffer.toString(), ParserLanguage.CPP);
        collector = new CPPNameCollector();
        tu.accept( collector);

        cA = (ICompositeType) collector.getName(1).resolveBinding();
        IBinding A = collector.getName(3).resolveBinding();
        vA = (IVariable) collector.getName(0).resolveBinding();
        assertSame(vA, A);
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
        tu.accept( collector);

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
        tu.accept( collector);
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
        tu.accept( collector);
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
        tu.accept( collector);

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
        tu.accept( collector);

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
        tu.accept( collector);

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
        tu.accept( collector);

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
        tu.accept( collector);

        IFunction f = (IFunction) collector.getName(0).resolveBinding();
        IParameter a = (IParameter) collector.getName(1).resolveBinding();

        assertInstances(collector, f, 2);
        assertInstances(collector, a, 3);
        
        IScope scope = a.getScope();
        assertNotNull( scope );
        assertSame( scope.getParent(), f.getScope() );
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
        tu.accept( collector);

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
        tu.accept( collector);

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
        tu.accept( collector);

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
        tu.accept( collector);

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
        tu.accept( collector);
        IVariable pf = (IVariable) collector.getName(0).resolveBinding();
        IPointerType pt = (IPointerType) pf.getType();
        assertTrue(pt.getType() instanceof IFunctionType);

        tu = parse(
                "struct A; int (*pfi)( int, struct A * );", ParserLanguage.CPP); //$NON-NLS-1$
        collector = new CPPNameCollector();
        tu.accept( collector);
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
        tu.accept( collector);

        IFunction f = (IFunction) collector.getName(0).resolveBinding();
        ICPPNamespace A = (ICPPNamespace) collector.getName(1).resolveBinding();
        IFunction g = (IFunction) collector.getName(2).resolveBinding();
        ICPPNamespace X = (ICPPNamespace) collector.getName(3).resolveBinding();

        ICPPUsingDeclaration using = (ICPPUsingDeclaration) collector.getName(5).resolveBinding();
        ICPPDelegate [] delegates = using.getDelegates();
        assertEquals( delegates.length, 1 );
        assertSame( delegates[0].getBinding(), f );
        assertInstances(collector, delegates[0], 2);
        assertInstances(collector, A, 2);
        assertInstances(collector, X, 3);
        
        ICPPUsingDeclaration using_g = (ICPPUsingDeclaration) collector.getName(8).resolveBinding();
        assertSame( using_g.getDelegates()[0].getBinding(), g );
        assertInstances(collector, using_g.getDelegates()[0], 2);
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
        tu.accept( collector);

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
        tu.accept( collector);

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
        tu.accept( collector);

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
        tu.accept( collector);

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
        tu.accept( collector);

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
        tu.accept( col);

        assertEquals(col.size(), 8);
        ICPPNamespace A = (ICPPNamespace) col.getName(0).resolveBinding();
        IVariable x = (IVariable) col.getName(1).resolveBinding();
        ICPPNamespace B = (ICPPNamespace) col.getName(2).resolveBinding();
        assertTrue( B instanceof ICPPDelegate );
        assertSame( ((ICPPDelegate)B).getBinding(), A );

        assertInstances(col, A, 2);
        assertInstances(col, B, 2);
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
        tu.accept( col);

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
        tu.accept( col);

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
        tu.accept( col);

        assertEquals(col.size(), 7);

        ICompositeType s_ref = (ICompositeType) col.getName(4).resolveBinding();
        ICompositeType s_decl = (ICompositeType) col.getName(0)
                .resolveBinding();

        assertSame(s_ref, s_decl);
    }

    public void testBug84266_2() throws Exception {
        IASTTranslationUnit tu = parse("struct s f(void);", ParserLanguage.CPP); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col);

        assertEquals(col.size(), 3);

        ICompositeType s = (ICompositeType) col.getName(0).resolveBinding();
        assertNotNull(s);

        tu = parse("struct s f(void){}", ParserLanguage.CPP); //$NON-NLS-1$
        col = new CPPNameCollector();
        tu.accept( col);

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
        tu.accept( col);

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
        tu.accept( col);

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
        tu.accept( col);

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
        tu.accept( col);

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
        tu.accept( col);

        assertEquals(col.size(), 11);

        ICPPVariable a1 = (ICPPVariable) col.getName(4).resolveBinding();
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
        tu.accept( col);

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
        tu.accept( col);
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
        tu.accept( col);

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
        tu.accept( col);

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
        tu.accept( col);

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
        tu.accept( col);

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
        tu.accept( col);

        assertEquals(9, col.size());
    }

    public void testPointerToMemberType() throws Exception {
        IASTTranslationUnit tu = parse("struct S; int S::* pm;", //$NON-NLS-1$
                ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col);

        assertEquals(4, col.size());

        IVariable pm = (IVariable) col.getName(3).resolveBinding();
        ICPPClassType S = (ICPPClassType) col.getName(0).resolveBinding();

        IType t = pm.getType();
        assertNotNull(t);
        assertTrue(t instanceof ICPPPointerToMemberType);
        ICPPClassType cls = ((ICPPPointerToMemberType) t).getMemberOfClass();
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
        tu.accept( col);

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
        tu.accept( col);

        ICPPClassType S = (ICPPClassType) col.getName(0).resolveBinding();
        IVariable pm = (IVariable) col.getName(8).resolveBinding();
        IField i = (IField) col.getName(1).resolveBinding();
        ICPPMethod f = (ICPPMethod) col.getName(3).resolveBinding();

        IType t = pm.getType();
        assertTrue(t instanceof ICPPPointerToMemberType);
        IFunctionType ft = (IFunctionType) ((ICPPPointerToMemberType) t)
                .getType();
        ICPPClassType ST = ((ICPPPointerToMemberType) t).getMemberOfClass();

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
        tu.accept( col);
    }

//    public void testFindTypeBinding_1() throws Exception {
//        IASTTranslationUnit tu = parse(
//                "int x = 5; int y(x);", ParserLanguage.CPP); //$NON-NLS-1$
//
//        IASTStandardFunctionDeclarator fdtor = (IASTStandardFunctionDeclarator) ((IASTSimpleDeclaration) tu
//                .getDeclarations()[1]).getDeclarators()[0];
//        IASTName name = fdtor.getParameters()[0].getDeclarator().getName();
//        IBinding binding = CPPSemantics.findTypeBinding(tu, name);
//        assertNull(binding);
//
//        tu = parse("struct x; int y(x);", ParserLanguage.CPP); //$NON-NLS-1$
//
//        fdtor = (IASTStandardFunctionDeclarator) ((IASTSimpleDeclaration) tu
//                .getDeclarations()[1]).getDeclarators()[0];
//        name = ((ICPPASTNamedTypeSpecifier) fdtor.getParameters()[0]
//                .getDeclSpecifier()).getName();
//        binding = CPPSemantics.findTypeBinding(tu, name);
//        assertNotNull(binding);
//        assertTrue(binding instanceof ICPPClassType);
//    }
//
//    public void testFindTypeBinding_2() throws Exception {
//        IASTTranslationUnit tu = parse(
//                "struct B; void f() { B * bp; }", ParserLanguage.CPP); //$NON-NLS-1$
//        IASTCompoundStatement compound = (IASTCompoundStatement) ((IASTFunctionDefinition) tu
//                .getDeclarations()[1]).getBody();
//        IASTBinaryExpression b = (IASTBinaryExpression) ((IASTExpressionStatement)compound.getStatements()[0]).getExpression();
//        IBinding binding = ((IASTIdExpression)b.getOperand1()).getName().resolveBinding();
////        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) ((IASTDeclarationStatement) compound
////                .getStatements()[0]).getDeclaration();
////        IBinding binding = CPPSemantics.findTypeBinding(compound,
////                ((ICPPASTNamedTypeSpecifier)decl.getDeclSpecifier()).getName());
//        assertNotNull(binding);
//        assertTrue(binding instanceof ICPPClassType);
//    }

    public void testBug85049() throws Exception {
        StringBuffer buffer = new StringBuffer( "struct B { };\n" ); //$NON-NLS-1$
        buffer.append( "void g() {\n" ); //$NON-NLS-1$
        buffer.append( "B * bp;  //1\n" ); //$NON-NLS-1$
        buffer.append( "}\n" ); //$NON-NLS-1$
        IASTTranslationUnit t = parse( buffer.toString(), ParserLanguage.CPP );
        IASTFunctionDefinition g = (IASTFunctionDefinition) t.getDeclarations()[1];
        IASTCompoundStatement body = (IASTCompoundStatement) g.getBody();
        assertTrue( body.getStatements()[0] instanceof IASTDeclarationStatement );
    }
    

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
        tu.accept( col);
        
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
        tu.accept( col);
        
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
        tu.accept( col);
        
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
        tu.accept( col);
        
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
        tu.accept( col);
        
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
        tu.accept( col);
        
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
        tu.accept( col);
        
        IFunction helper = (IFunction) col.getName(2).resolveBinding();
        assertSame( helper.getScope(), tu.getScope() );
        
        ICPPClassType B = (ICPPClassType) col.getName(1).resolveBinding();
        ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
        assertSame( B.getScope(), A.getScope() );
        
        IBinding [] friends = A.getFriends();
        assertEquals( friends.length, 1 );
        assertSame( friends[0], helper );
    }
    
    public void testBug45763_1() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "void f( int );                         \n"); //$NON-NLS-1$
        buffer.append( "void f( char );                        \n"); //$NON-NLS-1$
        buffer.append( "void (*pf) (int) = &f;                 \n"); //$NON-NLS-1$
        buffer.append( "void foo() {                           \n"); //$NON-NLS-1$
        buffer.append( "   pf = &f;                            \n"); //$NON-NLS-1$
        buffer.append( "}                                      \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col);
        
        IFunction f1 = (IFunction) col.getName(0).resolveBinding();
        IFunction f2 = (IFunction) col.getName(2).resolveBinding();
        IVariable pf = (IVariable) col.getName(4).resolveBinding();
        
        assertInstances( col, pf, 2 );
        assertInstances( col, f1, 3 );
        assertInstances( col, f2, 1 );
    }
    
    public void testBug45763_2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "void f( char );                        \n"); //$NON-NLS-1$
        buffer.append( "void f( int  );                        \n"); //$NON-NLS-1$
        buffer.append( "void g( void (*)( int ) ) {}           \n"); //$NON-NLS-1$
        buffer.append( "void (*pg)( void(*)(int) );            \n"); //$NON-NLS-1$
        buffer.append( "void foo() {                           \n"); //$NON-NLS-1$
        buffer.append( "   g( &f );                            \n"); //$NON-NLS-1$
        buffer.append( "   (*pg)( &f );                        \n"); //$NON-NLS-1$
        buffer.append( "}                                      \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col);
        
        IFunction f1 = (IFunction) col.getName(0).resolveBinding();
        IFunction f2 = (IFunction) col.getName(2).resolveBinding();
        IFunction g = (IFunction) col.getName(4).resolveBinding();
        IVariable pg = (IVariable) col.getName(7).resolveBinding();
        
        assertInstances( col, f1, 1 );
        assertInstances( col, f2, 3 );
        assertInstances( col, g, 2 );
        assertInstances( col, pg, 2 );
    }
    
    public void testBug45763_3() throws Exception {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("void f( int );                  \n"); //$NON-NLS-1$
    	buffer.append("void f( char );                 \n"); //$NON-NLS-1$
    	buffer.append("void (* bar () ) ( int ) {      \n"); //$NON-NLS-1$
    	buffer.append("   return &f;                   \n"); //$NON-NLS-1$
    	buffer.append("}                               \n"); //$NON-NLS-1$
    	
    	IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col);
        
        IFunction f1 = (IFunction) col.getName(0).resolveBinding();
        IFunction f2 = (IFunction) col.getName(2).resolveBinding();
        IFunction bar = (IFunction) col.getName(4).resolveBinding();
        assertNotNull( bar );
        
        assertInstances( col, f1, 2 );
        assertInstances( col, f2, 1 );
    }
    public void _testBug45763_4() throws Exception {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("void f( int );                  \n"); //$NON-NLS-1$
    	buffer.append("void f( char );                 \n"); //$NON-NLS-1$
    	buffer.append("void foo () {                   \n"); //$NON-NLS-1$
    	buffer.append("   ( void (*)(int) ) &f;        \n"); //$NON-NLS-1$
    	buffer.append("}                               \n"); //$NON-NLS-1$
    	
    	IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col);
        
        IFunction f1 = (IFunction) col.getName(0).resolveBinding();
        IFunction f2 = (IFunction) col.getName(2).resolveBinding();
        IFunction bar = (IFunction) col.getName(4).resolveBinding();
        assertNotNull( bar );
        
        assertInstances( col, f1, 2 );
        assertInstances( col, f2, 1 );
    }
    
    public void testBug85824() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "extern int g;       \n "); //$NON-NLS-1$
        buffer.append( "int g;              \n "); //$NON-NLS-1$
        buffer.append( "void f() {  g = 1; }\n "); //$NON-NLS-1$
        
    	IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col);
        
        IVariable g = (IVariable) col.getName(3).resolveBinding();
        assertInstances( col, g, 3 );
    }
    
    public void testPrefixLookup() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "struct A {            \n"); //$NON-NLS-1$
        buffer.append( "    int a2;           \n"); //$NON-NLS-1$
        buffer.append( "};                    \n"); //$NON-NLS-1$
        buffer.append( "struct B : public A { \n"); //$NON-NLS-1$
        buffer.append( "    int a1;           \n"); //$NON-NLS-1$
        buffer.append( "    void f();         \n"); //$NON-NLS-1$
        buffer.append( "}                     \n"); //$NON-NLS-1$
        buffer.append( "int a3;               \n"); //$NON-NLS-1$
        buffer.append( "void B::f(){          \n"); //$NON-NLS-1$
        buffer.append( "   int a4;            \n"); //$NON-NLS-1$
        buffer.append( "   a;                 \n"); //$NON-NLS-1$
        buffer.append( "}                     \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col);
        
        IASTName name = col.getName(11);
        IBinding [] bs = CPPSemantics.prefixLookup( name );
        assertEquals( 4, bs.length );
    }
    
    public void testIsStatic() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("static void f();    \n" ); //$NON-NLS-1$
        buffer.append("void f() {}         \n" ); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col);
        
        IFunction f = (IFunction) col.getName(1).resolveBinding();
        assertTrue( f.isStatic() );
        assertInstances( col, f, 2 );
    }
    
//    public void testBug85310() throws Exception
//    {
//    	  StringBuffer buffer = new StringBuffer( "void f() {" ); //$NON-NLS-1$
//    	  buffer.append( " if (__io.flags() & ios_base::showbase" );  //$NON-NLS-1$
//    	  buffer.append( "	      || __i < 2 || __sign.size() > 1" ); //$NON-NLS-1$
//    	  buffer.append( "	      || ((static_cast<part>(__p.field[3]) != money_base::none)" ); //$NON-NLS-1$
//    	  buffer.append( "		  && __i == 2)) " ); //$NON-NLS-1$
//    	  buffer.append( "	  return;" ); //$NON-NLS-1$
//    	  buffer.append( "}"); //$NON-NLS-1$
//    	  String code = buffer.toString();
//    	  IASTTranslationUnit tu = parse( code, ParserLanguage.CPP );
//    	  IASTFunctionDefinition f = (IASTFunctionDefinition) tu.getDeclarations()[0];
//    	  IASTCompoundStatement body = (IASTCompoundStatement) f.getBody();
//    	  IASTIfStatement if_stmt = (IASTIfStatement) body.getStatements()[0];
//    	  assertNotNull( if_stmt.getCondition() );
//    }
    
    public void testBug86267() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct B { void mutate(); };     \n"); //$NON-NLS-1$
        buffer.append("struct D1 : B {};                \n"); //$NON-NLS-1$
        buffer.append("struct D2 : B {};                \n"); //$NON-NLS-1$
        buffer.append("void B::mutate() {               \n"); //$NON-NLS-1$
        buffer.append("   new (this) D2;                \n"); //$NON-NLS-1$
        buffer.append("}                                \n"); //$NON-NLS-1$
        buffer.append("void g() {                       \n"); //$NON-NLS-1$
        buffer.append("   B* pb = new (p) D1;           \n"); //$NON-NLS-1$
        buffer.append("}                                \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col);
        
        ICPPClassType D1 = (ICPPClassType) col.getName(2).resolveBinding();
        ICPPClassType D2 = (ICPPClassType) col.getName(4).resolveBinding();
        
        ICPPConstructor [] ctors = D1.getConstructors();
        ICPPConstructor d1_ctor = ctors[0];
        
        ctors = D2.getConstructors();
        ICPPConstructor d2_ctor = ctors[0];
        
        assertInstances( col, d1_ctor, 1 );
        assertInstances( col, d2_ctor, 1 );
    }
    
    public void testBug86269() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct C {                                  \n"); //$NON-NLS-1$
        buffer.append("   void f();                                \n"); //$NON-NLS-1$
        buffer.append("   const C& operator =( const C& );         \n"); //$NON-NLS-1$
        buffer.append("};                                          \n"); //$NON-NLS-1$
        buffer.append("const C& C::operator = ( const C& other ) { \n"); //$NON-NLS-1$
        buffer.append("   if( this != &other ) {                   \n"); //$NON-NLS-1$
        buffer.append("      this->~C();                           \n"); //$NON-NLS-1$
        buffer.append("      new (this) C(other);                  \n"); //$NON-NLS-1$
        buffer.append("      f();                                  \n"); //$NON-NLS-1$
        buffer.append("   }                                        \n"); //$NON-NLS-1$
        buffer.append("   return *this;                            \n"); //$NON-NLS-1$
        buffer.append("}                                           \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col);
        
        ICPPClassType C = (ICPPClassType) col.getName(0).resolveBinding();
        ICPPMethod f = (ICPPMethod) col.getName(1).resolveBinding();
        ICPPMethod op = (ICPPMethod) col.getName(3).resolveBinding();
        IParameter other = (IParameter) col.getName(5).resolveBinding();
        
        assertInstances( col, C, 6 );
        assertInstances( col, f, 2 );
        assertInstances( col, op, 3 );
        assertInstances( col, other, 4 );
        
        assertEquals( other.getName(), "other" ); //$NON-NLS-1$
    }
    
    public void testBug86279() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("extern \"C\" {                               \n"); //$NON-NLS-1$
        buffer.append("   void printf( const char * );              \n"); //$NON-NLS-1$
        buffer.append("   void sprintf( const char * );             \n"); //$NON-NLS-1$
        buffer.append("}                                            \n"); //$NON-NLS-1$
        buffer.append("void foo(){                                  \n"); //$NON-NLS-1$
        buffer.append("   char *p;                                  \n"); //$NON-NLS-1$
        buffer.append("   printf( p );                              \n"); //$NON-NLS-1$
        buffer.append("   printf( \"abc\" );                        \n"); //$NON-NLS-1$
        buffer.append("}                                            \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col);
        
        IFunction r1 = (IFunction) col.getName(6).resolveBinding();
        IFunction r2 = (IFunction) col.getName(8).resolveBinding();
        IFunction printf = (IFunction) col.getName(0).resolveBinding();
        
        assertSame( printf, r1 );
        assertSame( printf, r2 );
    }
    
    public void testBug86346() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct S;            \n"); //$NON-NLS-1$
        buffer.append("extern S a;          \n"); //$NON-NLS-1$
        buffer.append("void g( S );         \n"); //$NON-NLS-1$
        buffer.append("void h() {           \n"); //$NON-NLS-1$
        buffer.append("   g( a );           \n"); //$NON-NLS-1$
        buffer.append("}                    \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPClassType S = (ICPPClassType) col.getName(0).resolveBinding();
        IFunction g = (IFunction) col.getName(3).resolveBinding();
        
        assertInstances( col, S, 3 );
        assertInstances( col, g, 2 );
    }
    
    public void testBug86288() throws Exception
    {
    	String code = "int *foo( int *b ) { return (int *)(b); }"; //$NON-NLS-1$
    	IASTTranslationUnit tu = parse( code, ParserLanguage.CPP );
    	IASTReturnStatement r = (IASTReturnStatement) ((IASTCompoundStatement)((IASTFunctionDefinition)tu.getDeclarations()[0]).getBody()).getStatements()[0];
    	assertTrue( r.getReturnValue() instanceof IASTCastExpression );
    }
 
     public void testBug84476() throws Exception
    {
    	StringBuffer buffer = new StringBuffer();
//    	buffer.append( "struct B {	int f();};\n"); //$NON-NLS-1$
//    	buffer.append( "int (B::*pb)() = &B::f; \n"); //$NON-NLS-1$
    	buffer.append( "void foo() {\n"); //$NON-NLS-1$
    	buffer.append( "struct B {\n"); //$NON-NLS-1$
    	buffer.append( "int f();\n"); //$NON-NLS-1$
    	buffer.append( "};    			\n"); //$NON-NLS-1$
    	buffer.append( "int (B::*pb)() = &B::f;\n");  //$NON-NLS-1$
    	buffer.append( "}\n" ); //$NON-NLS-1$
    	String code = buffer.toString();
    	IASTFunctionDefinition foo = (IASTFunctionDefinition) parse( code, ParserLanguage.CPP ).getDeclarations()[0];
    	IASTDeclarationStatement decl = (IASTDeclarationStatement) ((IASTCompoundStatement)foo.getBody()).getStatements()[1];
    	IASTSimpleDeclaration pb = (IASTSimpleDeclaration) decl.getDeclaration();
    	IASTDeclarator d = pb.getDeclarators()[0];
    	assertEquals( d.getNestedDeclarator().getPointerOperators().length,  1 );
    	assertEquals( d.getNestedDeclarator().getName().toString(), "pb" ); //$NON-NLS-1$
    	assertTrue( d.getNestedDeclarator().getPointerOperators()[0] instanceof ICPPASTPointerToMember );
    }
       
    public void testBug86336() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct T1 {                   \n"); //$NON-NLS-1$
        buffer.append("   T1 operator() ( int x ) {  \n"); //$NON-NLS-1$
        buffer.append("      return T1(x);           \n"); //$NON-NLS-1$
        buffer.append("   }                          \n"); //$NON-NLS-1$
        buffer.append("   T1( int ) {}               \n"); //$NON-NLS-1$
        buffer.append("};                            \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPConstructor T1_ctor = (ICPPConstructor) col.getName(6).resolveBinding();
        ICPPClassType T1 = (ICPPClassType) col.getName(0).resolveBinding();
        
        assertInstances( col, T1_ctor, 2 );
        assertInstances( col, T1, 2 );
    }
    
    public void testBug86306() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct S { int i; };    \n"); //$NON-NLS-1$
        buffer.append("void foo() {            \n"); //$NON-NLS-1$
        buffer.append("   int S::* pm = &S::i; \n"); //$NON-NLS-1$
        buffer.append("}                       \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPClassType S = (ICPPClassType) col.getName(0).resolveBinding();
        
        assertInstances( col, S, 3 );
        
        IASTName [] refs = tu.getReferences( S );
        assertEquals( refs.length, 2 );
        assertSame( refs[0], col.getName(4) );
        assertSame( refs[1], col.getName(7) );
    }
    
    public void testBug86372() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class A {                           \n"); //$NON-NLS-1$
		buffer.append("	public:                            \n"); //$NON-NLS-1$
		buffer.append("   template <class T> void f(T);    \n"); //$NON-NLS-1$
		buffer.append("   template <class T> struct X { }; \n"); //$NON-NLS-1$
		buffer.append("};                                  \n"); //$NON-NLS-1$
		buffer.append("class B : public A {                \n"); //$NON-NLS-1$
		buffer.append(" public:                            \n"); //$NON-NLS-1$
		buffer.append("   using A::f<double>; // illformed \n"); //$NON-NLS-1$
		buffer.append("   using A::X<int>; // illformed    \n"); //$NON-NLS-1$
		buffer.append("};                                  \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
    }
    
    public void testBug86319() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void foo() {                    \n"); //$NON-NLS-1$
        buffer.append("   int i = 42;                  \n"); //$NON-NLS-1$
        buffer.append("   int a[10];                   \n"); //$NON-NLS-1$
        buffer.append("   for( int i = 0; i < 10; i++ )\n"); //$NON-NLS-1$
        buffer.append("      a[i] = 1;                 \n"); //$NON-NLS-1$
        buffer.append("   int j = i;                   \n"); //$NON-NLS-1$
        buffer.append("}                               \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        IVariable i1 = (IVariable) col.getName(1).resolveBinding();
        IVariable i2 = (IVariable) col.getName(3).resolveBinding();
        
        assertNotSame( i1, i2 );
        assertInstances( col, i1, 2 );
        assertInstances( col, i2, 4 );
    }
    
    public void testBug86350() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class X { int i, j; };           \n"); //$NON-NLS-1$
        buffer.append("class Y { X x; };                \n"); //$NON-NLS-1$
        buffer.append("void foo() {                     \n"); //$NON-NLS-1$
        buffer.append("   const Y y;                    \n"); //$NON-NLS-1$
        buffer.append("   y.x.i++;                      \n"); //$NON-NLS-1$
        buffer.append("   y.x.j++;                      \n"); //$NON-NLS-1$
        buffer.append("   Y* p = const_cast<Y*>(&y);    \n"); //$NON-NLS-1$
        buffer.append("   p->x.i;                       \n"); //$NON-NLS-1$
        buffer.append("   p->x.j;                       \n"); //$NON-NLS-1$
        buffer.append("}                                \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPField i = (ICPPField) col.getName(1).resolveBinding();
        ICPPField j = (ICPPField) col.getName(2).resolveBinding();
        ICPPField x = (ICPPField) col.getName(5).resolveBinding();
        
        assertInstances( col, i, 3 );
        assertInstances( col, j, 3 );
        assertInstances( col, x, 5 );
    }
    public void testBug84478() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "void foo() {\n" ); //$NON-NLS-1$
		buffer.append( "  struct A {\n" ); //$NON-NLS-1$
		buffer.append( "    int val;\n" ); //$NON-NLS-1$
		buffer.append( "    A(int i) : val(i) { }\n" ); //$NON-NLS-1$
		buffer.append( "    ~A() { }\n" ); //$NON-NLS-1$
		buffer.append( "    operator bool() { return val != 0; }\n" ); //$NON-NLS-1$
		buffer.append( "  };\n" ); //$NON-NLS-1$
		buffer.append( "  int i = 1;\n" ); //$NON-NLS-1$
		buffer.append( "  while (A a = i) {\n" );  //$NON-NLS-1$
		buffer.append( "    i = 0;\n" ); //$NON-NLS-1$
		buffer.append( "  }\n" ); //$NON-NLS-1$
		buffer.append( "}\n" ); //$NON-NLS-1$
		IASTFunctionDefinition foo = (IASTFunctionDefinition) parse( buffer.toString(), ParserLanguage.CPP ).getDeclarations()[0];
		ICPPASTWhileStatement whileStatement = (ICPPASTWhileStatement) ((IASTCompoundStatement)foo.getBody()).getStatements()[2];
		assertNull( whileStatement.getCondition() );
		assertNotNull( whileStatement.getConditionDeclaration() );
	}
	    
    public void testBug86353() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void foo() {                      \n"); //$NON-NLS-1$
        buffer.append("   const int x = 12;              \n"); //$NON-NLS-1$
        buffer.append("   {   enum { x = x };  }         \n"); //$NON-NLS-1$
        buffer.append("}                                 \n"); //$NON-NLS-1$
        buffer.append("enum { RED };                     \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        IEnumerator enum_x = (IEnumerator) col.getName(3).resolveBinding();
        IBinding x_ref = col.getName(4).resolveBinding();
        IEnumerator RED = (IEnumerator) col.getName(6).resolveBinding();
        
        String [] s = ((ICPPBinding)RED).getQualifiedName();
        assertEquals( s[0], "RED" ); //$NON-NLS-1$
        assertTrue( ((ICPPBinding)RED).isGloballyQualified() );
        
        IASTName [] decls = tu.getDeclarations( enum_x );
        assertEquals( decls.length, 1 );
        assertSame( decls[0], col.getName(3) );
        
        decls = tu.getDeclarations( x_ref );
        assertEquals( decls.length, 1 );
        assertSame( decls[0], col.getName(1) );
        
        decls = tu.getDeclarations( RED );
        assertEquals( decls.length, 1 );
        assertSame( decls[0], col.getName(6) );
    }
    
    public void _testBug86274() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class D {};                   \n"); //$NON-NLS-1$
        buffer.append("D d1;                         \n"); //$NON-NLS-1$
        buffer.append("const D d2;                   \n"); //$NON-NLS-1$
        buffer.append("void foo() {                  \n"); //$NON-NLS-1$
        buffer.append("   typeid(d1) == typeid(d2);  \n"); //$NON-NLS-1$
        buffer.append("   typeid( D ) == typeid(d2); \n"); //$NON-NLS-1$
        buffer.append("}                             \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        assertEquals( col.size(), 10 );
        
        IVariable d1 = (IVariable) col.getName(6).resolveBinding();
        IVariable d2 = (IVariable) col.getName(7).resolveBinding();
        ICPPClassType D = (ICPPClassType) col.getName(8).resolveBinding();
        
        assertInstances( col, D, 4 );
        assertInstances( col, d1, 2 );
        assertInstances( col, d2, 3 );
    }
    
    public void testBug86546() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void point ( int = 3, int = 4 );        \n"); //$NON-NLS-1$
        buffer.append("void foo() {                            \n"); //$NON-NLS-1$
        buffer.append("   point( 1, 2 );                       \n"); //$NON-NLS-1$
        buffer.append("   point( 1 );                          \n"); //$NON-NLS-1$
        buffer.append("   point( );                            \n"); //$NON-NLS-1$
        buffer.append("}                                       \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        IFunction point = (IFunction) col.getName(0).resolveBinding();
        
        assertInstances( col, point, 4 );
    }
    
    public void testBug86358_1() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace Outer{                   \n"); //$NON-NLS-1$
        buffer.append("   int i;                          \n"); //$NON-NLS-1$
        buffer.append("   namespace Inner {               \n"); //$NON-NLS-1$
        buffer.append("      void f() {  i++;  }          \n"); //$NON-NLS-1$
        buffer.append("      int i;                       \n"); //$NON-NLS-1$
        buffer.append("      void g() {  i++;  }          \n"); //$NON-NLS-1$
        buffer.append("   }                               \n"); //$NON-NLS-1$
        buffer.append("}                                  \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        IVariable i = (IVariable) col.getName(4).resolveBinding();
        IVariable i2 = (IVariable) col.getName(7).resolveBinding();
        
        assertInstances( col, i, 2 );
        assertInstances( col, i2, 2 );
    }
    
    public void testBug86358_2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace Q {                    \n"); //$NON-NLS-1$
        buffer.append("   namespace V {                 \n"); //$NON-NLS-1$
        buffer.append("      void f();                  \n"); //$NON-NLS-1$
        buffer.append("   }                             \n"); //$NON-NLS-1$
        buffer.append("   void V::f() {}                \n"); //$NON-NLS-1$
        buffer.append("   namespace V {                 \n"); //$NON-NLS-1$
        buffer.append("   }                             \n"); //$NON-NLS-1$
        buffer.append("}                                \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        IFunction f1 = (IFunction) col.getName(2).resolveBinding();
        ICPPFunction f2 = (ICPPFunction) col.getName(5).resolveBinding();
        assertSame( f1, f2 );  

        IASTName [] decls = tu.getDeclarations( f2 );
        assertEquals( decls.length, 2 );
        assertSame( decls[0], col.getName(2) );
        assertSame( decls[1], col.getName(5) );
        
        String [] s = f2.getQualifiedName();
        assertEquals( s[0], "Q" ); //$NON-NLS-1$
        assertEquals( s[1], "V" ); //$NON-NLS-1$
        assertEquals( s[2], "f" ); //$NON-NLS-1$
        assertTrue( f2.isGloballyQualified() );
    }
    
    public void test86371() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct B {                          \n"); //$NON-NLS-1$
        buffer.append("   void f ( char );                 \n"); //$NON-NLS-1$
        buffer.append("   void g ( char );                 \n"); //$NON-NLS-1$
        buffer.append("};                                  \n"); //$NON-NLS-1$
        buffer.append("struct D : B {                      \n"); //$NON-NLS-1$
        buffer.append("   using B::f;                      \n"); //$NON-NLS-1$
        buffer.append("   void f( int ) { f('c'); }        \n"); //$NON-NLS-1$
        buffer.append("   void g( int ) { g('c'); }        \n"); //$NON-NLS-1$
        buffer.append("};                                  \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPFunction f_ref = (ICPPFunction) col.getName(12).resolveBinding();
        assertTrue( f_ref instanceof ICPPDelegate );
        IFunction g_ref = (IFunction) col.getName(15).resolveBinding();
        
        ICPPFunction f = (ICPPFunction) col.getName(1).resolveBinding();
        assertSame( ((ICPPDelegate)f_ref).getBinding(), f );
        
        IFunction g = (IFunction) col.getName(13).resolveBinding();
        assertSame( g, g_ref );
        
        assertInstances( col, f_ref, 1 );
        assertInstances( col, g_ref, 2 );
        
        String [] s = f_ref.getQualifiedName();
        assertEquals( s[0], "D" ); //$NON-NLS-1$
        assertEquals( s[1], "f" ); //$NON-NLS-1$
        assertTrue( f_ref.isGloballyQualified() );
        
        s = f.getQualifiedName();
        assertEquals( s[0], "B" ); //$NON-NLS-1$
        assertEquals( s[1], "f" ); //$NON-NLS-1$
        assertTrue( f.isGloballyQualified() );
    }
    
    public void testBug86369() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace Company_with_veryblahblah {}       \n"); //$NON-NLS-1$
        buffer.append("namespace CWVLN = Company_with_veryblahblah; \n"); //$NON-NLS-1$
        buffer.append("namespace CWVLN = Company_with_veryblahblah; \n"); //$NON-NLS-1$
        buffer.append("namespace CWVLN = CWVLN;                     \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPNamespace ns = (ICPPNamespace) col.getName(0).resolveBinding();
        ICPPNamespace alias = (ICPPNamespace) col.getName(1).resolveBinding();
        
        String [] s = ns.getQualifiedName();
        assertEquals( s[0], "Company_with_veryblahblah" ); //$NON-NLS-1$
        s = alias.getQualifiedName();
        assertEquals( s[0], "CWVLN" ); //$NON-NLS-1$
        
        assertTrue( alias instanceof ICPPDelegate );
        assertSame( ((ICPPDelegate)alias).getBinding(), ns );
        
        IASTName [] refs = tu.getReferences( ns );
        assertEquals( refs.length, 2 );
        assertSame( refs[0], col.getName(2) );
        assertSame( refs[1], col.getName(4) );

        IASTName [] decls = tu.getDeclarations( ns );
        assertEquals( decls.length, 1 );
        assertSame( decls[0], col.getName(0) );
        
        refs = tu.getReferences( alias );
        assertEquals( refs.length, 1 );
        assertSame( refs[0], col.getName(6) );
        
        decls = tu.getDeclarations( alias );
        assertEquals( decls.length, 3 );
        assertSame( decls[0], col.getName(1) );
        assertSame( decls[1], col.getName(3) );
        assertSame( decls[2], col.getName(5) );
    }
    
    public void testBug86470_1() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace A {                  \n"); //$NON-NLS-1$
        buffer.append("   void f( char );             \n"); //$NON-NLS-1$
        buffer.append("   void f( int );              \n"); //$NON-NLS-1$
        buffer.append("}                              \n"); //$NON-NLS-1$
        buffer.append("using A::f;                    \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPUsingDeclaration u = (ICPPUsingDeclaration) col.getName(7).resolveBinding();
        
        IASTName [] decls = tu.getDeclarations( u );
        assertEquals( decls.length, 2 );
        assertSame( decls[0], col.getName(1) );
        assertSame( decls[1], col.getName(3) );
        
        ICPPDelegate [] delegates = u.getDelegates();
        assertEquals( delegates.length, 2 );
        
        decls = tu.getDeclarations( delegates[0] );
        assertEquals( decls.length, 1 );
        assertSame( decls[0], col.getName(7) );
        
        decls = tu.getDeclarations( delegates[0].getBinding() );
        assertEquals( decls.length, 1 );
        assertSame( decls[0], col.getName(1) );
    }
    
    public void testBug86470_2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace A {                  \n"); //$NON-NLS-1$
        buffer.append("   void f( int );              \n"); //$NON-NLS-1$
        buffer.append("   void f( double );           \n"); //$NON-NLS-1$
        buffer.append("}                              \n"); //$NON-NLS-1$
        buffer.append("namespace B {                  \n"); //$NON-NLS-1$
        buffer.append("   void f( int );              \n"); //$NON-NLS-1$
        buffer.append("   void f( double );           \n"); //$NON-NLS-1$
        buffer.append("   void f( char );             \n"); //$NON-NLS-1$
        buffer.append("}                              \n"); //$NON-NLS-1$
        buffer.append("void g() {                     \n"); //$NON-NLS-1$
        buffer.append("   using A::f;                 \n"); //$NON-NLS-1$
        buffer.append("   using B::f;                 \n"); //$NON-NLS-1$
        buffer.append("   f( 'c' );                   \n"); //$NON-NLS-1$
        buffer.append("}                              \n"); //$NON-NLS-1$
                
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        IFunction f_decl = (IFunction) col.getName(10).resolveBinding();
        IFunction f_ref = (IFunction) col.getName(19).resolveBinding();
        assertTrue( f_ref instanceof ICPPDelegate );
        assertSame( f_decl, ((ICPPDelegate)f_ref).getBinding() );
    }
    
    public void testBug86470_3() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace A {                       \n"); //$NON-NLS-1$
        buffer.append("   struct g {};                     \n"); //$NON-NLS-1$
        buffer.append("   void g ( char );                 \n"); //$NON-NLS-1$
        buffer.append("}                                   \n"); //$NON-NLS-1$
        buffer.append("void f() {                          \n"); //$NON-NLS-1$
        buffer.append("   using A::g;                      \n"); //$NON-NLS-1$
        buffer.append("   g('a');                          \n"); //$NON-NLS-1$
        buffer.append("   struct g gg;                     \n"); //$NON-NLS-1$
        buffer.append("}                                   \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        IBinding ref1 = col.getName(8).resolveBinding();
        IBinding ref2 = col.getName(9).resolveBinding();
        
        assertTrue( ref1 instanceof ICPPDelegate );
        assertTrue( ref2 instanceof ICPPDelegate );
        
        ICPPClassType g_struct = (ICPPClassType) col.getName(1).resolveBinding();
        IFunction g_func = (IFunction) col.getName(2).resolveBinding();
        
        assertSame( g_struct, ((ICPPDelegate)ref2).getBinding() );
        assertSame( g_func, ((ICPPDelegate)ref1).getBinding() );
        
        ICPPUsingDeclaration comp = (ICPPUsingDeclaration) col.getName(7).resolveBinding();
        IASTName [] decls = tu.getDeclarations(comp);
        assertEquals( decls.length, 2 );
        assertSame( decls[0], col.getName(1) );
        assertSame( decls[1], col.getName(2) );
    }
    
    public void testBug86470_4() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace A {                       \n"); //$NON-NLS-1$
        buffer.append("   int x;                           \n"); //$NON-NLS-1$
        buffer.append("}                                   \n"); //$NON-NLS-1$
        buffer.append("namespace B {                       \n"); //$NON-NLS-1$
        buffer.append("   struct x {};                     \n"); //$NON-NLS-1$
        buffer.append("}                                   \n"); //$NON-NLS-1$
        buffer.append("void f() {                          \n"); //$NON-NLS-1$
        buffer.append("   using A::x;                      \n"); //$NON-NLS-1$
        buffer.append("   using B::x;                      \n"); //$NON-NLS-1$
        buffer.append("   x = 1;                           \n"); //$NON-NLS-1$
        buffer.append("   struct x xx;                     \n"); //$NON-NLS-1$
        buffer.append("}                                   \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPBinding ref1 = (ICPPBinding) col.getName(11).resolveBinding();
        ICPPBinding ref2 = (ICPPBinding) col.getName(12).resolveBinding();
        
        ICPPClassType x_struct = (ICPPClassType) col.getName(3).resolveBinding();
        IVariable x_var = (IVariable) col.getName(1).resolveBinding();
        
        assertTrue( ref1 instanceof ICPPDelegate );
        assertTrue( ref2 instanceof ICPPDelegate );
        assertSame( x_struct, ((ICPPDelegate)ref2).getBinding() );
        assertSame( x_var, ((ICPPDelegate)ref1).getBinding() );
        
        IASTName [] refs = tu.getReferences( x_struct );
        assertEquals( refs.length, 1 );
        assertSame( refs[0], col.getName(10) );
		
        String [] s = ref2.getQualifiedName();
        assertEquals( s[0], "x" ); //$NON-NLS-1$
        assertFalse( ref2.isGloballyQualified() );
        
        s = x_struct.getQualifiedName();
        assertEquals( s[0], "B" ); //$NON-NLS-1$
        assertEquals( s[1], "x" ); //$NON-NLS-1$
        assertTrue( x_struct.isGloballyQualified() );
    }
    
    public void testBug86470_5() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace A {                       \n"); //$NON-NLS-1$
        buffer.append("   void f( int );                   \n"); //$NON-NLS-1$
        buffer.append("   void f( double );                \n"); //$NON-NLS-1$
        buffer.append("}                                   \n"); //$NON-NLS-1$
        buffer.append("void g() {                          \n"); //$NON-NLS-1$
        buffer.append("   void f( char );                  \n"); //$NON-NLS-1$
        buffer.append("   using A::f;                      \n"); //$NON-NLS-1$
        buffer.append("   f( 3.5 );                        \n"); //$NON-NLS-1$
        buffer.append("}                                   \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPFunction f = (ICPPFunction) col.getName(3).resolveBinding();
        
        ICPPFunction f_ref = (ICPPFunction) col.getName(11).resolveBinding();
        assertTrue( f_ref instanceof ICPPDelegate );
        assertSame( ((ICPPDelegate)f_ref).getBinding(), f );
        
        String [] s = f_ref.getQualifiedName();
        assertEquals( s[0], "f" ); //$NON-NLS-1$
        assertFalse( f_ref.isGloballyQualified() );
        
        s = f.getQualifiedName();
        assertEquals( s[0], "A" ); //$NON-NLS-1$
        assertEquals( s[1], "f" ); //$NON-NLS-1$
        assertTrue( f.isGloballyQualified() );
    }
    
    public void testBug86678 () throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class B;                                 \n"); //$NON-NLS-1$
        buffer.append("class A {                                \n"); //$NON-NLS-1$
        buffer.append("   int i;                                \n"); //$NON-NLS-1$
        buffer.append("   friend void f( B * );                 \n"); //$NON-NLS-1$
        buffer.append("};                                       \n"); //$NON-NLS-1$
        buffer.append("class B : public A {};                   \n"); //$NON-NLS-1$
        buffer.append("void f( B* p ) {                         \n"); //$NON-NLS-1$
        buffer.append("   p->i = 1;                             \n"); //$NON-NLS-1$
        buffer.append("}                                        \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPClassType B = (ICPPClassType) col.getName(6).resolveBinding();
        ICPPField i = (ICPPField) col.getName(12).resolveBinding();
        IParameter p = (IParameter) col.getName(10).resolveBinding();
        
        assertInstances( col, B, 4 );
        assertInstances( col, i, 2 );
        assertInstances( col, p, 3 );
    }
    
    public void testBug86543() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("int printf( const char *, ... );             \n"); //$NON-NLS-1$
        buffer.append("void foo(){                                  \n"); //$NON-NLS-1$
        buffer.append("   int a, b;                                 \n"); //$NON-NLS-1$
        buffer.append("   printf( \"hello\" );                      \n"); //$NON-NLS-1$
        buffer.append("   printf(\"a=%d b=%d\", a, b );             \n"); //$NON-NLS-1$
        buffer.append("}                                            \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        IFunction printf = (IFunction) col.getName(6).resolveBinding();
        assertInstances( col, printf, 3 );
    }
    
    public void testBug86554() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("int max( int a, int b, int c ) {                  \n"); //$NON-NLS-1$
        buffer.append("   int m = ( a > b ) ? a : b;                     \n"); //$NON-NLS-1$
        buffer.append("   return ( m > c ) ? m : c;                      \n"); //$NON-NLS-1$
        buffer.append("}                                                 \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        IVariable m = (IVariable) col.getName(11).resolveBinding();
        IParameter a = (IParameter) col.getName(1).resolveBinding();
        IParameter b = (IParameter) col.getName(2).resolveBinding();
        IParameter c = (IParameter) col.getName(3).resolveBinding();
        
        String [] s = ((ICPPBinding)a).getQualifiedName();
        assertEquals( s[0], "a" ); //$NON-NLS-1$
        assertFalse( ((ICPPBinding)a).isGloballyQualified() );
        
        assertInstances( col, m, 3 );
        assertInstances( col, a, 3 );
        assertInstances( col, b, 3 );
        assertInstances( col, c, 3 );
    }
    
    public void testBug86621() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("int g();                               \n"); //$NON-NLS-1$
        buffer.append("struct X { static int g(); };          \n"); //$NON-NLS-1$
        buffer.append("struct Y : X { static int i ; };       \n"); //$NON-NLS-1$
        buffer.append("int Y::i = g();                        \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPFunction g1 = (ICPPFunction) col.getName(0).resolveBinding();
        ICPPMethod g2 = (ICPPMethod) col.getName(9).resolveBinding();
        
        String [] s = g1.getQualifiedName();
        assertEquals( s[0], "g" ); //$NON-NLS-1$
        assertTrue( g1.isGloballyQualified() );
        
        s = g2.getQualifiedName();
        assertEquals( s[0], "X" ); //$NON-NLS-1$
        assertEquals( s[1], "g" ); //$NON-NLS-1$
        assertTrue( g2.isGloballyQualified() );
        
        assertInstances( col, g1, 1 );
        assertInstances( col, g2, 2 );
    }
    
    public void testBug86649() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class V { int f(); int x; };                 \n"); //$NON-NLS-1$
        buffer.append("class W { int g(); int y; };                 \n"); //$NON-NLS-1$
        buffer.append("class B : public virtual V, public W {       \n"); //$NON-NLS-1$
        buffer.append("   int f();  int x;                          \n"); //$NON-NLS-1$
        buffer.append("   int g();  int y;                          \n"); //$NON-NLS-1$
        buffer.append("};                                           \n"); //$NON-NLS-1$
        buffer.append("class C : public virtual V, public W {};     \n"); //$NON-NLS-1$
        buffer.append("class D : public B, public C {               \n"); //$NON-NLS-1$
        buffer.append("   void foo();                               \n"); //$NON-NLS-1$
        buffer.append("};                                           \n"); //$NON-NLS-1$
        buffer.append("void D::foo(){                               \n"); //$NON-NLS-1$
        buffer.append("   x++;                                      \n"); //$NON-NLS-1$
        buffer.append("   f();                                      \n"); //$NON-NLS-1$
        buffer.append("   y++;                                      \n"); //$NON-NLS-1$
        buffer.append("   g();                                      \n"); //$NON-NLS-1$
        buffer.append("}                                            \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPField x = (ICPPField) col.getName(23).resolveBinding();
        ICPPMethod f = (ICPPMethod) col.getName(24).resolveBinding();
        
        String [] s= f.getQualifiedName();
        assertEquals( s[0], "B" ); //$NON-NLS-1$
        assertEquals( s[1], "f" ); //$NON-NLS-1$
        assertTrue( f.isGloballyQualified() );
        
        s = x.getQualifiedName();
        assertEquals( s[0], "B" ); //$NON-NLS-1$
        assertEquals( s[1], "x" ); //$NON-NLS-1$
        assertTrue( x.isGloballyQualified() );
        
        IProblemBinding y = (IProblemBinding) col.getName(25).resolveBinding();
        IProblemBinding g = (IProblemBinding) col.getName(26).resolveBinding();
        
        assertEquals( y.getID(), IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP );
        assertEquals( g.getID(), IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP );
        
        assertInstances( col, x, 2 );
        assertInstances( col, f, 2 );
    }
    
    public void testBug86827() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("struct C {                    \n"); //$NON-NLS-1$
        buffer.append("   int c;                     \n"); //$NON-NLS-1$
        buffer.append("   C() : c(0) { }             \n"); //$NON-NLS-1$
        buffer.append("};                            \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPVariable c = (ICPPVariable) col.getName(1).resolveBinding();
        
        String [] s = c.getQualifiedName();
        assertEquals( s.length, 2 );
        assertEquals( s[0], "C" ); //$NON-NLS-1$
        assertEquals( s[1], "c" ); //$NON-NLS-1$
        assertTrue( c.isGloballyQualified() );

        IASTName [] refs = tu.getReferences( c );
        assertEquals( refs.length, 1 );
        assertSame( refs[0], col.getName(3) );
    }
    
    public void testFind_1() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f( int par ) {            \n"); //$NON-NLS-1$
        buffer.append("   int v1;                     \n"); //$NON-NLS-1$
        buffer.append("   {                           \n"); //$NON-NLS-1$
        buffer.append("      int v2;                  \n"); //$NON-NLS-1$
        buffer.append("   }                           \n"); //$NON-NLS-1$
        buffer.append("}                              \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPVariable v1 = (ICPPVariable) col.getName(2).resolveBinding();
        ICPPVariable v2 = (ICPPVariable) col.getName(3).resolveBinding();
        
        String [] s = v1.getQualifiedName();
        assertEquals( s[0], "v1" ); //$NON-NLS-1$
        assertFalse( v1.isGloballyQualified() );
        
        s = v2.getQualifiedName();
        assertEquals( s[0], "v2" ); //$NON-NLS-1$
        assertFalse( v2.isGloballyQualified() );
        
        ICPPBlockScope scope = (ICPPBlockScope) v2.getScope();
        IBinding [] bs = scope.find( "v1" ); //$NON-NLS-1$
        assertEquals( bs.length, 1 );
        assertSame( bs[0], v1 );
    }
    public void testFind_2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class A { int a; };            \n"); //$NON-NLS-1$
        buffer.append("class B : public A {           \n"); //$NON-NLS-1$
        buffer.append("   void f();                   \n"); //$NON-NLS-1$
        buffer.append("};                             \n"); //$NON-NLS-1$
        buffer.append("void B::f() {                  \n"); //$NON-NLS-1$
        buffer.append("}                              \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
        ICPPField a = (ICPPField) col.getName(1).resolveBinding();
        ICPPMethod f = (ICPPMethod) col.getName(7).resolveBinding();
        
        IScope scope = f.getFunctionScope();
        IBinding [] bs = scope.find( "a" ); //$NON-NLS-1$
        assertEquals( bs.length, 1 );
        assertSame( bs[0], a );
        
        bs = scope.find( "~B" ); //$NON-NLS-1$
        assertEquals( bs.length, 1 );
        assertTrue( bs[0] instanceof ICPPMethod );
        assertTrue( bs[0].getName().equals( "~B" ) ); //$NON-NLS-1$
        
        bs = scope.find( "A" ); //$NON-NLS-1$
        assertEquals( bs.length, 1 );
        assertSame( bs[0], A );
    }
    public void testFind_3() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace A {                       \n"); //$NON-NLS-1$
        buffer.append("   void f( int );                   \n"); //$NON-NLS-1$
        buffer.append("   void f( double );                \n"); //$NON-NLS-1$
        buffer.append("}                                   \n"); //$NON-NLS-1$
        buffer.append("void g() {                          \n"); //$NON-NLS-1$
        buffer.append("   void f( char );                  \n"); //$NON-NLS-1$
        buffer.append("   using A::f;                      \n"); //$NON-NLS-1$
        buffer.append("}                                   \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);

        IFunction f1 = (IFunction) col.getName(1).resolveBinding();
        IFunction f2 = (IFunction) col.getName(3).resolveBinding();
        IFunction f3 = (IFunction) col.getName(6).resolveBinding();
        
        IASTFunctionDefinition def = (IASTFunctionDefinition) col.getName(5).getParent().getParent();
        IScope scope = ((IASTCompoundStatement)def.getBody()).getScope();
        IBinding [] bs = scope.find( "f" ); //$NON-NLS-1$
        assertEquals( bs.length, 3 );
        assertSame( bs[0], f3 );
        assertSame( ((ICPPDelegate)bs[1]).getBinding(), f1 );
        assertSame( ((ICPPDelegate)bs[2]).getBinding(), f2 );
        
        String [] s = ((ICPPDelegate)bs[1]).getQualifiedName();
        assertEquals( s.length, 1 );
        assertEquals( s[0], "f" ); //$NON-NLS-1$
        assertFalse( ((ICPPDelegate)bs[1]).isGloballyQualified() );
        
        s = ((ICPPBinding) ((ICPPDelegate)bs[1]).getBinding()).getQualifiedName();
        assertEquals( s.length, 2 );
        assertEquals( s[0], "A"); //$NON-NLS-1$
        assertEquals( s[1], "f" ); //$NON-NLS-1$
        assertTrue( ((ICPPBinding) ((ICPPDelegate)bs[1]).getBinding()).isGloballyQualified() );
        
    }
    
    public void testFind_4() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace A {                                  \n"); //$NON-NLS-1$
        buffer.append("   struct f;                                   \n"); //$NON-NLS-1$
        buffer.append("   void f();                                   \n"); //$NON-NLS-1$
        buffer.append("}                                              \n"); //$NON-NLS-1$
        buffer.append("namespace B {                                  \n"); //$NON-NLS-1$
        buffer.append("   void f( int );                              \n"); //$NON-NLS-1$
        buffer.append("}                                              \n"); //$NON-NLS-1$
        buffer.append("namespace C {                                  \n"); //$NON-NLS-1$
        buffer.append("   using namespace B;                          \n"); //$NON-NLS-1$
        buffer.append("}                                              \n"); //$NON-NLS-1$
        buffer.append("void g(){                                      \n"); //$NON-NLS-1$
        buffer.append("   using namespace A;                          \n"); //$NON-NLS-1$
        buffer.append("   using namespace C;                          \n"); //$NON-NLS-1$
        buffer.append("}                                              \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);

        ICPPClassType f = (ICPPClassType) col.getName(1).resolveBinding();
        IFunction f1 = (IFunction) col.getName(2).resolveBinding();
        IFunction f2 = (IFunction) col.getName(4).resolveBinding();
        
        IASTFunctionDefinition def = (IASTFunctionDefinition) col.getName(8).getParent().getParent();
        IScope scope = ((IASTCompoundStatement)def.getBody()).getScope();
        IBinding [] bs = scope.find( "f" ); //$NON-NLS-1$
        assertEquals( bs.length, 3 );
        assertSame( bs[0], f );
        assertSame( bs[1], f1 );
        assertSame( bs[2], f2 );
    }
    
    public void testGets() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class A {                                 \n"); //$NON-NLS-1$
        buffer.append("   int a;                                 \n"); //$NON-NLS-1$
        buffer.append("   void fa();                             \n"); //$NON-NLS-1$
        buffer.append("};                                        \n"); //$NON-NLS-1$
        buffer.append("class B : public A {                      \n"); //$NON-NLS-1$
        buffer.append("   int b;                                 \n"); //$NON-NLS-1$
        buffer.append("   void fb();                             \n"); //$NON-NLS-1$
        buffer.append("};                                        \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
        ICPPClassType B = (ICPPClassType) col.getName(3).resolveBinding();
        ICPPField a = (ICPPField) col.getName(1).resolveBinding();
        ICPPMethod fa = (ICPPMethod) col.getName(2).resolveBinding();
        ICPPField b = (ICPPField) col.getName(5).resolveBinding();
        ICPPMethod fb = (ICPPMethod) col.getName(6).resolveBinding();
        
        Object [] result = B.getDeclaredFields();
        assertEquals( result.length, 1 );
        assertSame( result[0], b );
        
        result = B.getFields();
        assertEquals( result.length, 2 );
        assertSame( result[0], b );
        assertSame( result[1], a );
        
        result = B.getDeclaredMethods();
        assertEquals( result.length, 1 );
        assertSame( result[0], fb );
        
        result = B.getAllDeclaredMethods();
        assertEquals( result.length, 2 );
        assertSame( result[0], fb );
        assertSame( result[1], fa );
        
        ICPPMethod [] B_implicit = ((ICPPClassScope)B.getCompositeScope()).getImplicitMethods();
        assertEquals( B_implicit.length, 4 );
        assertTrue( B_implicit[0].getName().equals( "B" ) ); //$NON-NLS-1$
        assertTrue( B_implicit[1].getName().equals( "B" ) ); //$NON-NLS-1$
        assertTrue( B_implicit[2].getName().equals( "operator =" ) ); //$NON-NLS-1$
        assertTrue( B_implicit[3].getName().equals( "~B" ) ); //$NON-NLS-1$
        
        ICPPMethod [] A_implicit = ((ICPPClassScope)A.getCompositeScope()).getImplicitMethods();
        assertEquals( A_implicit.length, 4 );
        assertTrue( A_implicit[0].getName().equals( "A" ) ); //$NON-NLS-1$
        assertTrue( A_implicit[1].getName().equals( "A" ) ); //$NON-NLS-1$
        assertTrue( A_implicit[2].getName().equals( "operator =" ) ); //$NON-NLS-1$
        assertTrue( A_implicit[3].getName().equals( "~A" ) ); //$NON-NLS-1$
        
        result = B.getMethods();
        assertEquals( result.length, 10 );
        assertSame( result[0], fb );
        assertSame( result[1], B_implicit[0] );
        assertSame( result[2], B_implicit[1] );
        assertSame( result[3], B_implicit[2] );
        assertSame( result[4], B_implicit[3] );
        assertSame( result[5], fa );
        assertSame( result[6], A_implicit[0] );
        assertSame( result[7], A_implicit[1] );
        assertSame( result[8], A_implicit[2] );
        assertSame( result[9], A_implicit[3] );
    }
    
    public void testBug87424() throws Exception{
        IASTTranslationUnit tu = parse( "int * restrict x;", ParserLanguage.CPP, true ); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        IVariable x = (IVariable) col.getName(0).resolveBinding();
        IType t = x.getType();
        assertTrue( t instanceof IGPPPointerType );
        assertTrue( ((IGPPPointerType) t).isRestrict() );
        
        tu = parse( "class A {}; int A::* restrict x;", ParserLanguage.CPP, true ); //$NON-NLS-1$
        col = new CPPNameCollector();
        tu.accept(col);
        
        x = (IVariable) col.getName(3).resolveBinding();
        t = x.getType();
        assertTrue( t instanceof IGPPPointerToMemberType );
        assertTrue( ((IGPPPointerToMemberType) t).isRestrict() );
    }
    
    public void testBug87705() throws Exception {
        IASTTranslationUnit tu = parse( "class A { friend class B::C; };", ParserLanguage.CPP, true ); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        IProblemBinding B = (IProblemBinding) col.getName(2).resolveBinding();
        assertEquals( B.getID(), IProblemBinding.SEMANTIC_NAME_NOT_FOUND );
        IProblemBinding C = (IProblemBinding) col.getName(3).resolveBinding();
        assertEquals( C.getID(), IProblemBinding.SEMANTIC_BAD_SCOPE );
    }
    
    public void testBug88459() throws Exception {
    	IASTTranslationUnit tu = parse( "int f(); ", ParserLanguage.CPP ); //$NON-NLS-1$
    	CPPNameCollector col = new CPPNameCollector();
    	tu.accept( col );
    	
    	IFunction f = (IFunction) col.getName(0).resolveBinding();
    	assertFalse( f.isStatic() );	
    }
    
    public void testBug88501_1() throws Exception {
    	IASTTranslationUnit tu = parse( "void f(); void f( int ); struct f;", ParserLanguage.CPP ); //$NON-NLS-1$
    	CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        assertTrue( col.getName(0).resolveBinding() instanceof IFunction );
        assertTrue( col.getName(1).resolveBinding() instanceof IFunction );
        assertTrue( col.getName(3).resolveBinding() instanceof ICPPClassType );
    }
        
//    public void testBug8342_1() throws Exception {
//    	IASTTranslationUnit tu = parse( "int a; int a;", ParserLanguage.CPP ); //$NON-NLS-1$
//    	CPPNameCollector col = new CPPNameCollector();
//        tu.accept(col);
//        
//        assertTrue( col.getName(0).resolveBinding() instanceof IVariable );
//        IProblemBinding p = (IProblemBinding) col.getName(1).resolveBinding();
//        assertEquals( p.getID(), IProblemBinding.SEMANTIC_INVALID_REDEFINITION );
//    }
    
    public void testBug8342_2() throws Exception {
    	IASTTranslationUnit tu = parse( "extern int a; extern char a;", ParserLanguage.CPP ); //$NON-NLS-1$
    	CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);
        
        assertTrue( col.getName(0).resolveBinding() instanceof IVariable );
        IProblemBinding p = (IProblemBinding) col.getName(1).resolveBinding();
        assertEquals( p.getID(), IProblemBinding.SEMANTIC_INVALID_REDECLARATION );
    }
    
    public void testNamespaceAlias_2() throws Exception {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("namespace A { int i; }      \n"); //$NON-NLS-1$
    	buffer.append("namespace B = A;            \n"); //$NON-NLS-1$
    	buffer.append("void f() {                  \n"); //$NON-NLS-1$
    	buffer.append("   B::i;                    \n"); //$NON-NLS-1$
    	buffer.append("}                           \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
        tu.accept(col);

        ICPPNamespace A = (ICPPNamespace) col.getName(0).resolveBinding();
        ICPPNamespaceAlias alias = (ICPPNamespaceAlias) col.getName(6).resolveBinding();
        ICPPVariable i = (ICPPVariable) col.getName(7).resolveBinding();
        
        assertInstances( col, A, 2 );
        assertInstances( col, alias, 2 );
        assertInstances( col, i, 3 );
        
        String [] s = i.getQualifiedName();
        assertEquals( s[0], "A" ); //$NON-NLS-1$
        assertEquals( s[1], "i" ); //$NON-NLS-1$
        assertTrue( i.isGloballyQualified() );
        
        s = alias.getQualifiedName();
        assertEquals( s[0], "B" ); //$NON-NLS-1$
        assertTrue( alias.isGloballyQualified() );
    }
    
    public void testBug89539() throws Exception {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("class A{};              \n"); //$NON-NLS-1$
    	buffer.append("class B : public A {    \n"); //$NON-NLS-1$
    	buffer.append("   B () : A() {}        \n"); //$NON-NLS-1$
    	buffer.append("};                      \n"); //$NON-NLS-1$
    	
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); //$NON-NLS-1$
    	CPPNameCollector col = new CPPNameCollector();
    	tu.accept( col );
    	
    	ICPPClassType A1 = (ICPPClassType) col.getName(0).resolveBinding();
    	ICPPClassType A2 = (ICPPClassType) col.getName(2).resolveBinding();
    	assertSame( A1, A2 );
    	
    	ICPPConstructor A3 = (ICPPConstructor) col.getName(4).resolveBinding();
    	assertSame( A3.getScope(), A1.getCompositeScope() );
    	
    	tu = parse( buffer.toString(), ParserLanguage.CPP ); //$NON-NLS-1$
    	col = new CPPNameCollector();
    	tu.accept( col );
    	
    	assertTrue( col.getName(4).resolveBinding() instanceof ICPPConstructor );
    }
    
    public void testBug89851() throws Exception {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append( "class B * b;            \n"); //$NON-NLS-1$
    	buffer.append( "class A {               \n"); //$NON-NLS-1$
    	buffer.append( "   A * a;               \n"); //$NON-NLS-1$
    	buffer.append( "};                      \n"); //$NON-NLS-1$
    	buffer.append( "class A;                \n"); //$NON-NLS-1$
    	
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); //$NON-NLS-1$
    	CPPNameCollector col = new CPPNameCollector();
    	tu.accept( col );
    	
    	assertTrue( col.getName(0).resolveBinding() instanceof ICPPClassType );
    	assertTrue( col.getName(1).resolveBinding() instanceof ICPPVariable );
    	assertTrue( col.getName(2).resolveBinding() instanceof ICPPClassType );
    	assertTrue( col.getName(3).resolveBinding() instanceof ICPPClassType );
    }
    
    public void testBug89828() throws Exception {
        IASTTranslationUnit tu = parse( "class B * b; void f();  void f( int );", ParserLanguage.CPP ); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col );
        
        assertTrue( col.getName(0).resolveBinding() instanceof ICPPClassType );
    	assertTrue( col.getName(1).resolveBinding() instanceof ICPPVariable );
        IFunction f1 = (IFunction) col.getName(2).resolveBinding();
        IFunction f2 = (IFunction) col.getName(3).resolveBinding();
        
        IScope scope = tu.getScope();
        IBinding [] bs = scope.find("f"); //$NON-NLS-1$
        assertEquals( bs.length, 2 );
        assertSame( bs[0], f1 );
        assertSame( bs[1], f2 );
    }
    
    public void testBug90039() throws Exception {
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("class A {                  \n"); //$NON-NLS-1$
    	buffer.append("   enum type { t1, t2 };   \n"); //$NON-NLS-1$
    	buffer.append("   void f( type t );       \n"); //$NON-NLS-1$
    	buffer.append("};                         \n"); //$NON-NLS-1$
    	buffer.append("class B : public A {       \n"); //$NON-NLS-1$
    	buffer.append("   void g() {              \n"); //$NON-NLS-1$
    	buffer.append("      f( A::t1 );          \n"); //$NON-NLS-1$
    	buffer.append("   }                       \n"); //$NON-NLS-1$
    	buffer.append("};                         \n"); //$NON-NLS-1$
    	
    	IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col );

        IFunction f = (IFunction) col.getName( 10 ).resolveBinding();
        IEnumerator t1 = (IEnumerator) col.getName( 13 ).resolveBinding();

        assertInstances( col, f, 2 );
        assertInstances( col, t1, 3 );
    }
    
    public void testBug90039_2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("void f( void ) {                 \n"); //$NON-NLS-1$
        buffer.append("   enum { one };                 \n"); //$NON-NLS-1$
        buffer.append("}                                \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col );
        assertTrue( col.getName(0).resolveBinding() instanceof IFunction );
        assertTrue( col.getName(1).resolveBinding() instanceof IParameter );
        IEnumeration e = (IEnumeration) col.getName(2).resolveBinding(); 
        IEnumerator one = (IEnumerator) col.getName(3).resolveBinding();
        assertSame( one.getType(), e );
    }
	
	
	public void testOperatorConversionNames() throws Exception {
		StringBuffer buffer = new StringBuffer();
    	buffer.append("class Foo {\n"); //$NON-NLS-1$
		buffer.append("public:\n"); //$NON-NLS-1$
		buffer.append("operator int();\n"); //$NON-NLS-1$
		buffer.append("char& operator[](unsigned int);\n"); //$NON-NLS-1$
		buffer.append("};\n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
    	tu.accept( col );
		
		IASTName name1 = col.getName(1);
		IASTName name2 = col.getName(2);
		
		assertNotNull(name1);
		assertNotNull(name2);
		
		assertTrue(name1 instanceof ICPPASTConversionName);
		assertTrue(name2 instanceof ICPPASTOperatorName);
		
		IASTTypeId typeId = ((ICPPASTConversionName)name1).getTypeId();
		assertNotNull(typeId);
		assertEquals( ((IASTSimpleDeclSpecifier)typeId.getDeclSpecifier()).getType(), IASTSimpleDeclSpecifier.t_int );
		
	}
	
    public void testBug36769B() throws Exception {
		StringBuffer buffer = new StringBuffer();
    	buffer.append("class X { operator int(); }; \n"); //$NON-NLS-1$
		buffer.append("X::operator int() { } \n"); //$NON-NLS-1$
		buffer.append("template <class A,B> class X<A,C> { operator int(); }; \n"); //$NON-NLS-1$
		buffer.append("template <class A,B> X<A,C>::operator int() { } \n"); //$NON-NLS-1$
		
		IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
    	tu.accept( col );
		
		// 1,4,12,21  - conversion
		// 2, 16 .isConversion
		
		assertEquals(col.size(), 22);
		
		assertNotNull(col.getName(1));
		assertNotNull(col.getName(4));
		assertNotNull(col.getName(12));
		assertNotNull(col.getName(21));
		assertNotNull(col.getName(2));
		assertNotNull(col.getName(16));
		
		// ensure the conversions are conversions
		assertTrue(col.getName(1) instanceof ICPPASTConversionName);
		assertTrue(col.getName(4) instanceof ICPPASTConversionName);
		assertTrue(col.getName(12) instanceof ICPPASTConversionName);
		assertTrue(col.getName(21) instanceof ICPPASTConversionName);
		assertNotNull(((ICPPASTConversionName)col.getName(1)).getTypeId());
		assertNotNull(((ICPPASTConversionName)col.getName(4)).getTypeId());
		assertNotNull(((ICPPASTConversionName)col.getName(12)).getTypeId());
		assertNotNull(((ICPPASTConversionName)col.getName(21)).getTypeId());
		
		// ensure qualified name isConversionOrOperator
		assertTrue(col.getName(2) instanceof ICPPASTQualifiedName);
		assertTrue(col.getName(16) instanceof ICPPASTQualifiedName);
		assertTrue(((ICPPASTQualifiedName)col.getName(2)).isConversionOrOperator());
		assertTrue(((ICPPASTQualifiedName)col.getName(16)).isConversionOrOperator());
    }

	public void testBug88662() throws Exception {
        IASTTranslationUnit tu = parse( "int foo() {  return int();}", ParserLanguage.CPP ); //$NON-NLS-1$
        IASTReturnStatement returnStatement = (IASTReturnStatement) ((IASTCompoundStatement)((IASTFunctionDefinition)tu.getDeclarations()[0]).getBody()).getStatements()[0];
        ICPPASTSimpleTypeConstructorExpression expression = (ICPPASTSimpleTypeConstructorExpression) returnStatement.getReturnValue();
        assertEquals( expression.getInitialValue(), null );
        assertEquals( expression.getSimpleType(), ICPPASTSimpleTypeConstructorExpression.t_int );
    }
    
  
	
    public void testBug90498_1() throws Exception {
        IASTTranslationUnit tu = parse( "typedef INT ( FOO ) (INT);", ParserLanguage.CPP ); //$NON-NLS-1$
        
        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
        IASTDeclSpecifier declSpec = decl.getDeclSpecifier();
        assertTrue( declSpec instanceof ICPPASTNamedTypeSpecifier );
        assertEquals( ((ICPPASTNamedTypeSpecifier)declSpec).getName().toString(), "INT" ); //$NON-NLS-1$
        
        IASTDeclarator dtor = decl.getDeclarators()[0];
        assertTrue( dtor instanceof IASTFunctionDeclarator );
        assertNotNull( dtor.getNestedDeclarator() );
        IASTDeclarator nested = dtor.getNestedDeclarator();
        assertEquals( nested.getName().toString(), "FOO" );  //$NON-NLS-1$
    }
    
    public void testBug90498_2() throws Exception {
        IASTTranslationUnit tu = parse( "int (* foo) (int) (0);", ParserLanguage.CPP ); //$NON-NLS-1$
        
        IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
        IASTDeclSpecifier declSpec = decl.getDeclSpecifier();
        assertTrue( declSpec instanceof IASTSimpleDeclSpecifier );
        
        IASTDeclarator dtor = decl.getDeclarators()[0];
        assertTrue( dtor instanceof IASTFunctionDeclarator );
        assertNotNull( dtor.getNestedDeclarator() );
        IASTDeclarator nested = dtor.getNestedDeclarator();
        assertEquals( nested.getName().toString(), "foo" );  //$NON-NLS-1$
        
        assertNotNull( dtor.getInitializer() );
    }
    
    
    public void testBug866274() throws Exception {
        StringBuffer buffer = new StringBuffer( "class D { /* ... */ };\n" ); //$NON-NLS-1$
        buffer.append( "D d1;\n"); //$NON-NLS-1$
        buffer.append( "const D d2;\n"); //$NON-NLS-1$
        buffer.append( "void foo() {\n"); //$NON-NLS-1$
        buffer.append( "    typeid(d1) == typeid(d2);\n"); //$NON-NLS-1$
        buffer.append( "    typeid(D) == typeid(d2);\n"); //$NON-NLS-1$
        buffer.append( "}\n"); //$NON-NLS-1$
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP );
        IASTFunctionDefinition foo = (IASTFunctionDefinition) tu.getDeclarations()[3];
        IASTCompoundStatement cs = (IASTCompoundStatement) foo.getBody();
        IASTStatement [] subs = cs.getStatements();
        for( int i = 0; i < subs.length; ++i )
        {
            IASTBinaryExpression be = (IASTBinaryExpression) ((IASTExpressionStatement)subs[i]).getExpression();
            if( i == 1 )
            {
                IASTTypeIdExpression expression = (IASTTypeIdExpression) be.getOperand1();
                IASTTypeId typeId = expression.getTypeId();
                assertTrue( ((IASTNamedTypeSpecifier)typeId.getDeclSpecifier()).getName().resolveBinding() instanceof IType );
            }
            else
            {
                IASTUnaryExpression expression = (IASTUnaryExpression) be.getOperand1();
                IASTIdExpression idExpression = (IASTIdExpression) expression.getOperand();
                assertTrue( idExpression.getName().resolveBinding() instanceof IVariable );
            }
            IASTUnaryExpression expression = (IASTUnaryExpression) be.getOperand2();
            IASTIdExpression idExpression = (IASTIdExpression) expression.getOperand();
            assertTrue( idExpression.getName().resolveBinding() instanceof IVariable );
            
        }
    }

    
    public void testTypedefFunction() throws Exception {
        IASTTranslationUnit tu = parse( "typedef int foo (int);", ParserLanguage.CPP ); //$NON-NLS-1$
        CPPNameCollector col = new CPPNameCollector();
        tu.accept( col );
        
        IBinding binding = col.getName(0).resolveBinding();
        assertTrue( binding instanceof ITypedef );
        assertTrue( ((ITypedef)binding).getType() instanceof IFunctionType );
    }
    
    public void testBug90616() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append( "void f( int );          \n"); //$NON-NLS-1$
        buffer.append( "void foo(){             \n"); //$NON-NLS-1$
        buffer.append( "   f( ( 1, 2 ) );       \n"); //$NON-NLS-1$
        buffer.append( "}                       \n"); //$NON-NLS-1$
        
        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
    	tu.accept( col );
    	
    	IFunction f1 = (IFunction) col.getName(0).resolveBinding();
    	IFunction f2 = (IFunction) col.getName(3).resolveBinding();
    	assertSame( f1, f2 );
    }
    
    public void testBug90603() throws Exception {
        IASTTranslationUnit tu = parse( "class X { void f(){} };", ParserLanguage.CPP ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
    	tu.accept( col );
    	
    	ICPPClassType X = (ICPPClassType) col.getName(0).resolveBinding();
    	ICPPMethod f1 = (ICPPMethod) col.getName(1).resolveBinding();
    	
    	assertFalse( f1.isStatic() );
    	
    	String[] qns = f1.getQualifiedName();
    	assertEquals( qns.length, 2 );
    	assertEquals( qns[0], "X" ); //$NON-NLS-1$
    	assertEquals( qns[1], "f" ); //$NON-NLS-1$
    	assertTrue( f1.isGloballyQualified() );
    	assertEquals( f1.getVisibility(), ICPPMember.v_private );
    	
    	assertSame( f1.getScope(), X.getCompositeScope() );
    }
    
    public void testBug90662() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("class X {   };           \n"); //$NON-NLS-1$
        buffer.append("X x;                     \n"); //$NON-NLS-1$
        buffer.append("class X {   };           \n"); //$NON-NLS-1$

        IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
    	tu.accept( col );
    	
    	ICPPClassType X = (ICPPClassType) col.getName(0).resolveBinding();
    	IVariable x = (IVariable) col.getName(2).resolveBinding();
    	IProblemBinding problem = (IProblemBinding) col.getName(3).resolveBinding();
    	assertSame( x.getType(), X );
    	assertEquals( problem.getID(), IProblemBinding.SEMANTIC_INVALID_REDEFINITION );
    }
	
	public void testOperatorNames() throws Exception {
	        StringBuffer buffer = new StringBuffer();
	        buffer.append("struct C {                               \n"); //$NON-NLS-1$
			buffer.append("   void* operator new [ ] (unsigned int);\n"); //$NON-NLS-1$
			buffer.append("   void* operator new (unsigned int);\n"); //$NON-NLS-1$
			buffer.append("   void operator delete [ ] ( void * );       \n"); //$NON-NLS-1$
			buffer.append("   void operator delete (void *);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator+=(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator -= (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator *=   (const C&);\n"); //$NON-NLS-1$
			buffer.append("   cosnt C& operator /= (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator %= (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator^=(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator&= (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator |= (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator >>=(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator<<= (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator<<(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator>>(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator /**/   == /**/  (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator != /**/ (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator <= (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator /**/ >=(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator =(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator&& (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator ||(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator ++(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator-- (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator/**/,/**/(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator->*\n(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator -> (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator /**/ ( /**/ ) /**/ (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator /**/ [ /**/ ] /**/ (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator + (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator- (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator *(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator /(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator % /**/(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator ^(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator &(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator |(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator   ~ (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator \t\r\n ! /**/ (const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator <(const C&);\n"); //$NON-NLS-1$
			buffer.append("   const C& operator>(const C&);\n"); //$NON-NLS-1$
			buffer.append("};                                       \n"); //$NON-NLS-1$

	        IASTTranslationUnit tu = parse(buffer.toString(), ParserLanguage.CPP);
	        CPPNameCollector col = new CPPNameCollector();
	        tu.accept( col );

	        assertEquals(col.size(), 161);
			assertEquals(col.getName(1).toString(), "operator new[]"); //$NON-NLS-1$
			assertEquals(col.getName(3).toString(), "operator new"); //$NON-NLS-1$
			assertEquals(col.getName(5).toString(), "operator delete[]"); //$NON-NLS-1$
			assertEquals(col.getName(7).toString(), "operator delete"); //$NON-NLS-1$
			assertEquals(col.getName(10).toString(), "operator +="); //$NON-NLS-1$
			assertEquals(col.getName(14).toString(), "operator -="); //$NON-NLS-1$
			assertEquals(col.getName(18).toString(), "operator *="); //$NON-NLS-1$
			assertEquals(col.getName(22).toString(), "operator /="); //$NON-NLS-1$
			assertEquals(col.getName(26).toString(), "operator %="); //$NON-NLS-1$
			assertEquals(col.getName(30).toString(), "operator ^="); //$NON-NLS-1$
			assertEquals(col.getName(34).toString(), "operator &="); //$NON-NLS-1$
			assertEquals(col.getName(38).toString(), "operator |="); //$NON-NLS-1$
			assertEquals(col.getName(42).toString(), "operator >>="); //$NON-NLS-1$
			assertEquals(col.getName(46).toString(), "operator <<="); //$NON-NLS-1$
			assertEquals(col.getName(50).toString(), "operator <<"); //$NON-NLS-1$
			assertEquals(col.getName(54).toString(), "operator >>"); //$NON-NLS-1$
			assertEquals(col.getName(58).toString(), "operator =="); //$NON-NLS-1$
			assertEquals(col.getName(62).toString(), "operator !="); //$NON-NLS-1$
			assertEquals(col.getName(66).toString(), "operator <="); //$NON-NLS-1$
			assertEquals(col.getName(70).toString(), "operator >="); //$NON-NLS-1$
			assertEquals(col.getName(74).toString(), "operator ="); //$NON-NLS-1$
			assertEquals(col.getName(78).toString(), "operator &&"); //$NON-NLS-1$
			assertEquals(col.getName(82).toString(), "operator ||"); //$NON-NLS-1$
			assertEquals(col.getName(86).toString(), "operator ++"); //$NON-NLS-1$
			assertEquals(col.getName(90).toString(), "operator --"); //$NON-NLS-1$
			assertEquals(col.getName(94).toString(), "operator ,"); //$NON-NLS-1$
			assertEquals(col.getName(98).toString(), "operator ->*"); //$NON-NLS-1$
			assertEquals(col.getName(102).toString(), "operator ->"); //$NON-NLS-1$
			assertEquals(col.getName(106).toString(), "operator ()"); //$NON-NLS-1$
			assertEquals(col.getName(110).toString(), "operator []"); //$NON-NLS-1$
			assertEquals(col.getName(114).toString(), "operator +"); //$NON-NLS-1$
			assertEquals(col.getName(118).toString(), "operator -"); //$NON-NLS-1$
			assertEquals(col.getName(122).toString(), "operator *"); //$NON-NLS-1$
			assertEquals(col.getName(126).toString(), "operator /"); //$NON-NLS-1$
			assertEquals(col.getName(130).toString(), "operator %"); //$NON-NLS-1$
			assertEquals(col.getName(134).toString(), "operator ^"); //$NON-NLS-1$
			assertEquals(col.getName(138).toString(), "operator &"); //$NON-NLS-1$
			assertEquals(col.getName(142).toString(), "operator |"); //$NON-NLS-1$
			assertEquals(col.getName(146).toString(), "operator ~"); //$NON-NLS-1$
			assertEquals(col.getName(150).toString(), "operator !"); //$NON-NLS-1$
			assertEquals(col.getName(154).toString(), "operator <"); //$NON-NLS-1$
			assertEquals(col.getName(158).toString(), "operator >"); //$NON-NLS-1$
	}
	
	public void testBug90623() throws Exception {
	    StringBuffer buffer = new StringBuffer();
	    buffer.append( "typedef int I;             \n"); //$NON-NLS-1$
	    buffer.append( "typedef int I;             \n"); //$NON-NLS-1$
	    buffer.append( "typedef I I;               \n"); //$NON-NLS-1$
	    buffer.append( "class A {                  \n"); //$NON-NLS-1$
	    buffer.append( "   typedef char I;         \n"); //$NON-NLS-1$
	    buffer.append( "   typedef char I;         \n"); //$NON-NLS-1$
	    buffer.append( "   typedef I I;            \n"); //$NON-NLS-1$
	    buffer.append( "};                         \n"); //$NON-NLS-1$
	    
	    IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
    	tu.accept( col );
    	
    	ITypedef I1 = (ITypedef) col.getName(0).resolveBinding();
    	ITypedef I2 = (ITypedef) col.getName(1).resolveBinding();
    	ITypedef I3 = (ITypedef) col.getName(2).resolveBinding();
    	ITypedef I4 = (ITypedef) col.getName(3).resolveBinding();
    	ITypedef I8 = (ITypedef) col.getName(5).resolveBinding();
    	ITypedef I5 = (ITypedef) col.getName(8).resolveBinding();
    	ITypedef I6 = (ITypedef) col.getName(7).resolveBinding();
    	ITypedef I7 = (ITypedef) col.getName(6).resolveBinding();
    	//ITypedef I8 = (ITypedef) col.getName(5).resolveBinding();
    	
    	assertSame( I1, I2 );
    	assertSame( I2, I3 );
    	assertSame( I3, I4 );
    	assertNotSame( I4, I5 );
    	assertSame( I5, I6 );
    	assertSame( I6, I7 );
    	assertSame( I7, I8 );
    	
    	assertTrue( I1.getType() instanceof IBasicType );
    	assertEquals( ((IBasicType)I1.getType()).getType(), IBasicType.t_int );
    	
    	assertTrue( I8.getType() instanceof IBasicType );
    	assertEquals( ((IBasicType)I8.getType()).getType(), IBasicType.t_char );    	
	}
	
	public void testBug90623_2() throws Exception {
	    StringBuffer buffer = new StringBuffer();
	    buffer.append( "typedef int I;             \n"); //$NON-NLS-1$
	    buffer.append( "void f11( I i );           \n"); //$NON-NLS-1$
	    buffer.append( "void main(){ f a; }          \n"); //$NON-NLS-1$
	    
	    IASTTranslationUnit tu = parse( buffer.toString(), ParserLanguage.CPP ); //$NON-NLS-1$
		CPPNameCollector col = new CPPNameCollector();
    	tu.accept( col );

    	IASTName f = col.getName( 5 );
    	f.resolvePrefix();
	}
}

