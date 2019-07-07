/*******************************************************************************
 * Copyright (c) 2004, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Niefer (IBM Corporation) - initial API and implementation
 *     Ed Swartz (Nokia)
 *     Markus Schorn (Wind River Systems)
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *     Thomas Corbat (IFS)
 *     Nathan Ridge
 *     Marc-Andre Laperle
 *     Richard Eames
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.LVALUE;
import static org.eclipse.cdt.core.dom.ast.IASTExpression.ValueCategory.XVALUE;
import static org.eclipse.cdt.core.parser.ParserLanguage.CPP;
import static org.eclipse.cdt.core.parser.tests.VisibilityAsserts.assertVisibility;
import static org.junit.Assert.assertNotEquals;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.ASTTypeUtil;
import org.eclipse.cdt.core.dom.ast.EScopeKind;
import org.eclipse.cdt.core.dom.ast.IASTArrayDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTAttribute;
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTCastExpression;
import org.eclipse.cdt.core.dom.ast.IASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTCompoundStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclarationStatement;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTElaboratedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpressionList;
import org.eclipse.cdt.core.dom.ast.IASTExpressionStatement;
import org.eclipse.cdt.core.dom.ast.IASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTImplicitName;
import org.eclipse.cdt.core.dom.ast.IASTImplicitNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTInitializer;
import org.eclipse.cdt.core.dom.ast.IASTLabelStatement;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNameOwner;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTProblem;
import org.eclipse.cdt.core.dom.ast.IASTProblemDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTReturnStatement;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTStandardFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTToken;
import org.eclipse.cdt.core.dom.ast.IASTTokenList;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.IASTTypeIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.IArrayType;
import org.eclipse.cdt.core.dom.ast.IBasicType;
import org.eclipse.cdt.core.dom.ast.IBasicType.Kind;
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
import org.eclipse.cdt.core.dom.ast.IProblemType;
import org.eclipse.cdt.core.dom.ast.IQualifierType;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.core.dom.ast.ISemanticProblem;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCastExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier.ICPPASTBaseSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTConversionName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTDeleteExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionCallExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDeclarator;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLinkageSpecification;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNameSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNamespaceDefinition;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTNewExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTOperatorName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTPointerToMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTQualifiedName;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTSimpleTypeConstructorExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTTemplateDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUnaryExpression;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTWhileStatement;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBasicType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBlockScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassScope;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPEnumeration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionTemplate;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunctionType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMember;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespaceAlias;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPParameter;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPPointerToMemberType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPReferenceType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPUsingDeclaration;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.dom.ast.cpp.SemanticQueries;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.util.CharArrayUtils;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPBasicType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPClassType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPConstructor;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPFunctionType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPMethod;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPPointerType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPQualifierType;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ClassTypeHelper;
import org.eclipse.cdt.internal.core.dom.parser.cpp.ICPPInternalBinding;
import org.eclipse.cdt.internal.core.dom.parser.cpp.OverloadableOperator;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPSemantics;
import org.eclipse.cdt.internal.core.dom.parser.cpp.semantics.CPPVisitor;
import org.eclipse.cdt.internal.core.index.IndexCPPSignatureUtil;
import org.eclipse.cdt.internal.core.parser.ParserException;

import junit.framework.TestSuite;

public class AST2CPPTests extends AST2CPPTestBase {

	public AST2CPPTests() {
	}

	public AST2CPPTests(String name) {
		super(name);
	}

	public static TestSuite suite() {
		return suite(AST2CPPTests.class);
	}

	private void assertProblemBinding(int id, IBinding b) {
		assertTrue(b instanceof IProblemBinding);
		assertEquals(id, ((IProblemBinding) b).getID());
	}

	private void assertDefinition(ICPPBinding b) {
		assertTrue(((IASTName) ((ICPPInternalBinding) b).getDefinition()).isDefinition());
	}

	private void assertDeclaration(ICPPBinding b) {
		assertTrue(((IASTName) ((ICPPInternalBinding) b).getDeclarations()[0]).isDeclaration());
	}

	private ICPPMethod extractSingleMethod(IBinding[] bindings) {
		assertEquals(1, bindings.length);
		assertTrue(bindings[0] instanceof ICPPMethod);
		return (ICPPMethod) bindings[0];
	}

	private ICPPClassType extractSingleClass(IBinding[] bindings) {
		assertEquals(1, bindings.length);
		assertTrue(bindings[0] instanceof ICPPClassType);
		return (ICPPClassType) bindings[0];
	}

	private void checkNewExpression(IASTFunctionDefinition fdef, int i_expr, Class<?> placement, String type,
			Class<?> init) {
		IASTExpression expr;
		ICPPASTNewExpression newExpr;
		expr = getExpressionOfStatement(fdef, i_expr);
		assertInstance(expr, ICPPASTNewExpression.class);
		newExpr = (ICPPASTNewExpression) expr;
		if (placement == null) {
			assertNull(newExpr.getNewPlacement());
		} else {
			assertInstance(newExpr.getNewPlacement(), placement);
		}
		if (init == null) {
			assertNull(newExpr.getNewInitializer());
		} else {
			assertInstance(newExpr.getNewInitializer(), init);
		}
		isTypeEqual(CPPVisitor.createType(newExpr.getTypeId()), type);
	}

	private void checkDeclDef(String[] declNames, String[] defNames, IASTDeclaration[] decls) {
		int i = 0, j = 0;
		for (IASTDeclaration decl : decls) {
			if (decl instanceof ICPPASTLinkageSpecification) {
				decl = ((ICPPASTLinkageSpecification) decl).getDeclarations()[0];
			}
			final IASTDeclarator[] dtors = ((IASTSimpleDeclaration) decl).getDeclarators();
			for (IASTDeclarator dtor : dtors) {
				final String name = dtor.getName().toString();
				switch (dtor.getRoleForName(dtor.getName())) {
				case IASTNameOwner.r_declaration:
					assertTrue("Unexpected decl " + name, i < declNames.length);
					assertEquals(declNames[i++], name);
					break;
				case IASTNameOwner.r_definition:
					assertTrue("Unexpected decl " + name, j < defNames.length);
					assertEquals(defNames[j++], name);
					break;
				default:
					assertTrue(name, false);
				}
			}
		}
		assertEquals(declNames.length, i);
		assertEquals(defNames.length, j);
	}

	protected static void assertSameType(IType first, IType second) {
		assertNotNull(first);
		assertNotNull(second);
		assertTrue("Expected types to be the same, but first was: '" + first.toString() + "' and second was: '" + second
				+ "'", first.isSameType(second));
	}

	private void checkUserDefinedLiteralIsType(String code, String type_name) throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings(code, CPP);
		IASTDeclaration[] declarations = tu.getDeclarations();
		IASTDeclaration declaration = declarations[declarations.length - 1];

		IASTInitializer init = ((IASTSimpleDeclaration) declaration).getDeclarators()[0].getInitializer();
		IType type = ((IASTExpression) ((IASTEqualsInitializer) init).getInitializerClause()).getExpressionType();

		assertEquals(type_name, type.toString());
	}

	private void checkUserDefinedLiteralIsRet(String code) throws Exception {
		checkUserDefinedLiteralIsType(code, "Ret");
	}

	// int *zzz1 (char);
	// int (*zzz2) (char);
	// int ((*zzz3)) (char);
	// int (*(zzz4)) (char);
	public void testBug40768() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertNoProblemBindings(col);
	}

	public void testBug40422() throws Exception {
		IASTTranslationUnit tu = parse("class A { int y; }; int A::* x = 0;", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertNoProblemBindings(col);
	}

	public void testBug43241() throws Exception {
		parseAndCheckBindings("int m(int); int (*pm)(int) = &m; int f(){} int f(int); int x = f((*pm)(5));");
	}

	// class A { int m(int); };
	// A a;
	// int (A::*pm)(int) = &A::m;
	// int f(){}
	// int f(int);
	// int x = f((a.*pm)(5));
	public void testBug43242() throws Exception {
		parseAndCheckBindings(getAboveComment());
	}

	//	class A {
	//		int m;
	//	};
	//	A* a;
	//	int A::*pm;
	//	int f(){}
	//	int f(int);
	//	int x = f(a->*pm);
	public void testBug43579() throws Exception {
		parseAndCheckBindings();
	}

	public void testBug75189() throws Exception {
		parseAndCheckBindings("struct A{};typedef int (*F) (A*);");
	}

	public void testBug75340() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings("void f(int i = 0, int * p = 0);");
		IASTSimpleDeclaration sd = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		isParameterSignatureEqual(sd.getDeclarators()[0], "(int=0, int*=0)");
	}

	//	bool f() {
	//	  int first, last;
	//	  if (first < 1 || last > 99)
	//	    return false;
	//	}
	public void testBug75858() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertNoProblemBindings(col);
	}

	// enum type {A};
	// enum type a, b;
	// type c;
	// enum type2 {A, B};
	// enum type2 d, e;
	public void testBug77967() throws Exception {
		parseAndCheckBindings();
	}

	//	int *p1; int *p2;
	//	union {
	//	  struct {int a; int b;} A;
	//	  struct {int a; int b;};
	//	} MyStruct;
	//	void test (void) {
	//	  p1 = &MyStruct.A.a;
	//	  p2 = &MyStruct.b;
	//	  MyStruct.b = 1;
	//	}
	public void testBug78103() throws Exception {
		parseAndCheckBindings(getAboveComment());
	}

	//	class B {
	//	public:
	//	  B(int t);
	//	};
	//	class A : public B {
	//	public:
	//	  A(int t);
	//	};
	//	A::A(int t) : B(t - 1) {}
	public void testBug78883() throws Exception {
		parseAndCheckBindings();
	}

	//	#define REF_WRAP(e) class A { public: A (){ } A& foo2(e& v) { return *this; } }
	//	class B {
	//	  REF_WRAP(B);
	//	  B();
	//	  void foo();
	//	};
	public void testBug79540() throws Exception {
		parseAndCheckBindings(getAboveComment());
	}

	public void testBug86282() throws Exception {
		IASTTranslationUnit tu = parse("void foo() { int (* f[])() = new (int (*[10])());  }", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertNoProblemBindings(col);
	}

	// class A {
	//   public:
	//   int x;
	//   A* next;
	// };
	// A* start;
	// void test() {
	//   for(A *y = start; y->x != 0;  y = y->next) {
	//     42;
	//   }
	//   for(int x = 0 ; x < 10; x++) {
	//   }
	// }
	public void testBug95411() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector nameCol = new NameCollector();
		tu.accept(nameCol);
		assertNoProblemBindings(nameCol);
	}

	public void testBug95424() throws Exception {
		IASTTranslationUnit tu = parse("void f(){ traits_type::copy(__r->_M_refdata(), __buf, __i); }", CPP, true,
				true);
		tu = parse("void f(){ traits_type::copy(__r->_M_refdata(), __buf, __i); }", CPP, false, true);
		IASTFunctionDefinition f = (IASTFunctionDefinition) tu.getDeclarations()[0];
		IASTCompoundStatement cs = (IASTCompoundStatement) f.getBody();
		IASTExpressionStatement es = (IASTExpressionStatement) cs.getStatements()[0];
		assertTrue(es.getExpression() instanceof IASTFunctionCallExpression);
	}

	//	#define CURLOPTTYPE_OBJECTPOINT   10000
	//	#define CINIT(name,type,number) CURLOPT_ ## name = CURLOPTTYPE_ ## type + number
	//	typedef enum {
	//	  CINIT(FILE, OBJECTPOINT, 1),
	//	  CINIT(URL,  OBJECTPOINT, 2)
	//	} CURLoption;
	public void testBug102825() throws Exception {
		parseAndCheckBindings(getAboveComment());
	}

	//	template <class T, int someConst=0>
	//	class WithTemplate {};
	//	int main () {
	//	  WithTemplate <int, 10> normalInstance;
	//	  const int localConst=10;
	//	  WithTemplate <int, localConst> brokenInstance;
	//	  return 0;
	//	}
	public void testBug103578() throws Exception {
		parseAndCheckBindings(getAboveComment());
	}

	//	template <typename T>
	//	struct A {
	//	  template <typename U> class B;
	//	  typedef int type;
	//	};
	//
	//	template <typename T>
	//	template <typename U>
	//	struct A<T>::B {
	//	  typedef typename A::type type;
	//	};
	public void testBug433556() throws Exception {
		parseAndCheckBindings();
	}

	// class A { } a;
	public void testSimpleClass() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		IASTCompositeTypeSpecifier compTypeSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
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

	// class A; class A {};
	public void testClassForwardDecl() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);

		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals(decl.getDeclarators().length, 0);
		IASTElaboratedTypeSpecifier elabSpec = (IASTElaboratedTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_elab = elabSpec.getName();

		decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
		assertEquals(decl.getDeclarators().length, 0);
		IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_comp = compSpec.getName();

		ICompositeType A = (ICompositeType) name_elab.resolveBinding();
		ICompositeType A_2 = (ICompositeType) name_comp.resolveBinding();

		assertNotNull(A);
		assertSame(A, A_2);
	}

	// class A {};  A a;
	public void testVariable() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);

		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals(decl.getDeclarators().length, 0);
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

		assertNotNull(a);
		assertNotNull(A);
		assertSame(A, A1);
		assertSame(A1, A2);
	}

	// class A {  int f; };
	public void testField() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);

		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals(decl.getDeclarators().length, 0);
		ICPPASTCompositeTypeSpecifier comp = (ICPPASTCompositeTypeSpecifier) decl.getDeclSpecifier();
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

	// class A { int f(); };
	public void testMethodDeclaration() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);

		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals(decl.getDeclarators().length, 0);
		IASTCompositeTypeSpecifier comp = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
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

	//  class A { void f();  };
	//  void A::f() { }
	public void testMethodDefinition() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);

		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals(decl.getDeclarators().length, 0);
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

		ICPPASTNameSpecifier[] qualifier = name_f2.getQualifier();
		assertEquals(qualifier.length, 1);
		IASTName qn1 = (IASTName) qualifier[0];
		IASTName qn2 = name_f2.getLastName();

		ICPPClassType A2 = (ICPPClassType) qn1.resolveBinding();
		ICPPMethod f3 = (ICPPMethod) qn2.resolveBinding();

		assertNotNull(A);
		assertNotNull(f1);
		assertSame(f1, f2);
		assertSame(f2, f3);
		assertSame(A, A2);
	}

	// class A { void f(); int i;    };
	// void A::f() { i; }
	public void testMemberReference() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);

		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals(decl.getDeclarators().length, 0);
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

		ICPPASTNameSpecifier[] qualifier = name_f2.getQualifier();
		assertEquals(qualifier.length, 1);
		IASTName qn1 = (IASTName) qualifier[0];
		IASTName qn2 = name_f2.getLastName();

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

	// class A { int i; };
	// class B : public A { void f(); };
	// void B::f() { i; }
	public void testBasicInheritance() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);

		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		ICPPASTCompositeTypeSpecifier comp = (ICPPASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_A1 = comp.getName();

		decl = (IASTSimpleDeclaration) comp.getMembers()[0];
		IASTName name_i1 = decl.getDeclarators()[0].getName();

		decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
		comp = (ICPPASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		IASTName name_B1 = comp.getName();

		ICPPASTBaseSpecifier base = comp.getBaseSpecifiers()[0];
		IASTName name_A2 = (IASTName) base.getNameSpecifier();

		decl = (IASTSimpleDeclaration) comp.getMembers()[0];
		IASTName name_f1 = decl.getDeclarators()[0].getName();

		IASTFunctionDefinition def = (IASTFunctionDefinition) tu.getDeclarations()[2];
		ICPPASTQualifiedName name_f2 = (ICPPASTQualifiedName) def.getDeclarator().getName();
		IASTName name_B2 = (IASTName) name_f2.getQualifier()[0];
		IASTName name_f3 = name_f2.getLastName();

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

	// namespace A{
	//    int a;
	// }
	// namespace B{
	//    using namespace A;
	// }
	// namespace C{
	//    using namespace A;
	// }
	//
	// namespace BC{
	//    using namespace B;
	//    using namespace C;
	// }
	//
	// void f(){
	//    BC::a++; //ok
	// }
	public void testNamespaces() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);

		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 13);
		ICPPNamespace A = (ICPPNamespace) collector.getName(0).resolveBinding();
		IVariable a = (IVariable) collector.getName(1).resolveBinding();
		ICPPNamespace B = (ICPPNamespace) collector.getName(2).resolveBinding();
		ICPPNamespace C = (ICPPNamespace) collector.getName(4).resolveBinding();
		ICPPNamespace BC = (ICPPNamespace) collector.getName(6).resolveBinding();
		IFunction f = (IFunction) collector.getName(9).resolveBinding();
		assertInstances(collector, A, 3);
		assertInstances(collector, a, 3);
		assertInstances(collector, B, 2);
		assertInstances(collector, C, 2);
		assertInstances(collector, BC, 2);
		assertInstances(collector, f, 1);
	}

	// int A;
	// class A {};
	// void f() {
	//    A++;
	//    class A a;
	// }
	public void testNameHiding() throws Exception {
		String content = getAboveComment();
		IASTTranslationUnit tu = parse(content, CPP);

		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 6);
		IVariable vA = (IVariable) collector.getName(0).resolveBinding();
		ICompositeType cA = (ICompositeType) collector.getName(1).resolveBinding();
		IVariable a = (IVariable) collector.getName(5).resolveBinding();

		assertSame(a.getType(), cA);
		assertInstances(collector, vA, 2);
		assertInstances(collector, cA, 2);

		tu = parse(content, CPP);
		collector = new NameCollector();
		tu.accept(collector);

		cA = (ICompositeType) collector.getName(1).resolveBinding();
		IBinding A = collector.getName(3).resolveBinding();
		vA = (IVariable) collector.getName(0).resolveBinding();
		assertSame(vA, A);
	}

	// class A { void f(); };
	// class B;
	// void A::f() {
	//    B b;
	// }
	// int B;
	public void testBlockTraversal() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 9);
		ICompositeType A = (ICompositeType) collector.getName(0).resolveBinding();
		ICPPMethod f = (ICPPMethod) collector.getName(1).resolveBinding();
		ICompositeType B = (ICompositeType) collector.getName(2).resolveBinding();

		IVariable b = (IVariable) collector.getName(7).resolveBinding();
		IVariable B2 = (IVariable) collector.getName(8).resolveBinding();
		assertSame(b.getType(), B);
		assertInstances(collector, A, 2);
		assertInstances(collector, f, 3);
		assertInstances(collector, B, 2);
		assertInstances(collector, b, 1);
		assertInstances(collector, B2, 1);
	}

	// void f(int i);
	// void f(char c);
	// void main() {
	//    f(1);		//calls f(int);
	//    f('b');
	// }
	public void testFunctionResolution() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);
		IFunction f1 = (IFunction) collector.getName(0).resolveBinding();
		IFunction f2 = (IFunction) collector.getName(2).resolveBinding();

		assertInstances(collector, f1, 2);
		assertInstances(collector, f2, 2);
	}

	// typedef struct {
	//     int x;
	// } S;
	// void f() {
	//     S myS;
	//     myS.x = 5;
	// }
	public void testSimpleStruct() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);
		ICPPClassType anonStruct = (ICPPClassType) collector.getName(0).resolveBinding();
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

	// struct A;
	// void f(){
	//    struct A;
	//    struct A * a;
	// }
	public void testStructureTags_a() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		ICPPClassType A1 = (ICPPClassType) collector.getName(0).resolveBinding();
		ICPPClassType A2 = (ICPPClassType) collector.getName(2).resolveBinding();
		IVariable a = (IVariable) collector.getName(4).resolveBinding();

		assertNotNull(a);
		assertNotNull(A1);
		assertNotNull(A2);
		assertNotSame(A1, A2);
		assertInstances(collector, A1, 1);
		assertInstances(collector, A2, 2);
	}

	// struct A;
	// void f(){
	//    struct A * a;
	// }
	public void testStructureTags_b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		ICPPClassType A1 = (ICPPClassType) collector.getName(0).resolveBinding();
		ICPPClassType A2 = (ICPPClassType) collector.getName(2).resolveBinding();
		IVariable a = (IVariable) collector.getName(3).resolveBinding();

		assertNotNull(a);
		assertNotNull(A1);
		assertNotNull(A2);
		assertSame(A1, A2);
		assertInstances(collector, A1, 2);
	}

	// struct A;
	// struct A * a;
	// struct A { int i; };
	// void f() {
	//    a->i;
	// }
	public void testStructureDef() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		ICPPClassType A1 = (ICPPClassType) collector.getName(0).resolveBinding();
		IVariable a = (IVariable) collector.getName(2).resolveBinding();
		ICPPField i = (ICPPField) collector.getName(4).resolveBinding();

		assertInstances(collector, A1, 3);
		assertInstances(collector, a, 2);
		assertInstances(collector, i, 2);
	}

	// struct x {};
	// void f(int x) {
	//    struct x i;
	// }
	public void testStructureNamespace() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		ICPPClassType x = (ICPPClassType) collector.getName(0).resolveBinding();

		assertInstances(collector, x, 2);
	}

	// void f(int a);
	// void f(int b){
	//    b;
	// }
	public void testFunctionDef() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		IFunction f = (IFunction) collector.getName(0).resolveBinding();
		IParameter a = (IParameter) collector.getName(1).resolveBinding();

		assertInstances(collector, f, 2);
		assertInstances(collector, a, 3);

		IScope scope = a.getScope();
		assertNotNull(scope);
		assertSame(scope.getParent(), f.getScope());
	}

	// void f();
	// void g() {
	//    f();
	// }
	// void f(){ }
	public void testSimpleFunctionCall() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		IFunction f = (IFunction) collector.getName(0).resolveBinding();
		IFunction g = (IFunction) collector.getName(1).resolveBinding();

		assertInstances(collector, f, 3);
		assertInstances(collector, g, 1);
	}

	// void f() {
	//    for(int i = 0; i < 5; i++) {
	//       i;
	//    }
	// }
	public void testForLoop() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		IVariable i = (IVariable) collector.getName(1).resolveBinding();

		assertInstances(collector, i, 4);
	}

	// struct A { int x; };
	// void f(){
	//    ((struct A *) 1)->x;
	// }
	public void testExpressionFieldReference() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		ICPPClassType A = (ICPPClassType) collector.getName(0).resolveBinding();
		IField x = (IField) collector.getName(1).resolveBinding();

		assertInstances(collector, A, 2);
		assertInstances(collector, x, 2);
	}

	// enum hue { red, blue, green };
	// enum hue col, *cp;
	// void f() {
	//    col = blue;
	//    cp = &col;
	//    if (*cp != red)
	//       return;
	// }
	public void testEnumerations() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

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
		IASTTranslationUnit tu = parse("int (*pfi)();", CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);
		IVariable pf = (IVariable) collector.getName(0).resolveBinding();
		IPointerType pt = (IPointerType) pf.getType();
		assertTrue(pt.getType() instanceof IFunctionType);

		tu = parse("struct A; int (*pfi)(int, struct A *);", CPP);
		collector = new NameCollector();
		tu.accept(collector);
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

	// struct A;
	// int * f(int i, char c);
	// void (*g) (A *);
	// void (* (*h)(A**)) (int);
	public void testFunctionTypes() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);

		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		IASTElaboratedTypeSpecifier elabSpec = (IASTElaboratedTypeSpecifier) decl.getDeclSpecifier();
		ICompositeType A = (ICompositeType) elabSpec.getName().resolveBinding();

		decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
		IFunction f = (IFunction) decl.getDeclarators()[0].getName().resolveBinding();

		decl = (IASTSimpleDeclaration) tu.getDeclarations()[2];
		IVariable g = (IVariable) decl.getDeclarators()[0].getNestedDeclarator().getName().resolveBinding();

		decl = (IASTSimpleDeclaration) tu.getDeclarations()[3];
		IVariable h = (IVariable) decl.getDeclarators()[0].getNestedDeclarator().getNestedDeclarator().getName()
				.resolveBinding();

		IFunctionType t_f = f.getType();
		IType t_f_return = t_f.getReturnType();
		assertTrue(t_f_return instanceof IPointerType);
		assertTrue(((IPointerType) t_f_return).getType() instanceof IBasicType);
		IType[] t_f_params = t_f.getParameterTypes();
		assertEquals(t_f_params.length, 2);
		assertTrue(t_f_params[0] instanceof IBasicType);
		assertTrue(t_f_params[1] instanceof IBasicType);

		// g is a pointer to a function that returns void and has 1 parameter
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

		// h is a pointer to a function that returns a pointer to a function
		// the returned pointer to function returns void and takes 1 parameter
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
		assertSame(((IPointerType) ((IPointerType) t_h_func_p1).getType()).getType(), A);

		assertTrue(t_h_func_return instanceof IPointerType);
		IFunctionType h_return = (IFunctionType) ((IPointerType) t_h_func_return).getType();
		IType h_r = h_return.getReturnType();
		IType[] h_ps = h_return.getParameterTypes();
		assertTrue(h_r instanceof IBasicType);
		assertEquals(h_ps.length, 1);
		assertTrue(h_ps[0] instanceof IBasicType);
	}

	public void testFnReturningPtrToFn() throws Exception {
		IASTTranslationUnit tu = parse("void (* f(int))(){}", CPP);

		IASTFunctionDefinition def = (IASTFunctionDefinition) tu.getDeclarations()[0];
		IFunction f = (IFunction) def.getDeclarator().getName().resolveBinding();

		IFunctionType ft = f.getType();
		assertTrue(ft.getReturnType() instanceof IPointerType);
		assertTrue(((IPointerType) ft.getReturnType()).getType() instanceof IFunctionType);
		assertEquals(ft.getParameterTypes().length, 1);
	}

	// void f();
	// namespace A {
	//    void g();
	// }
	// namespace X {
	//    using ::f;
	//    using A::g;
	// }
	// void h() {
	//    X::f();
	//    X::g();
	// }
	public void testUsingDeclaration() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		IFunction f = (IFunction) collector.getName(0).resolveBinding();
		ICPPNamespace A = (ICPPNamespace) collector.getName(1).resolveBinding();
		IFunction g = (IFunction) collector.getName(2).resolveBinding();
		ICPPNamespace X = (ICPPNamespace) collector.getName(3).resolveBinding();

		ICPPUsingDeclaration using = (ICPPUsingDeclaration) collector.getName(5).resolveBinding();
		IBinding[] delegates = using.getDelegates();
		assertEquals(delegates.length, 1);
		assertSame(delegates[0], f);
		assertInstances(collector, delegates[0], 3); // decl + using-decl + ref
		assertInstances(collector, A, 2);
		assertInstances(collector, X, 3);

		ICPPUsingDeclaration using_g = (ICPPUsingDeclaration) collector.getName(8).resolveBinding();
		assertSame(using_g.getDelegates()[0], g);
		assertInstances(collector, using_g.getDelegates()[0], 3); // decl + using-decl + ref
	}

	//	namespace A {
	//	  void f(int);
	//	}
	//	using A::f;
	//	namespace A {
	//	  void f(char);
	//	}
	//	void foo() {
	//	  f('i');
	//	}
	//	void bar() {
	//	  using A::f;
	//	  f('c');
	//	}
	public void testUsingDeclaration_86368() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		IFunction f1 = bh.assertNonProblem("f(int)", 1);
		IFunction f2 = bh.assertNonProblem("f('i')", 1);
		assertSame(f1, f2);
		IFunction g1 = bh.assertNonProblem("f(char)", 1);
		IFunction g2 = bh.assertNonProblem("f('c')", 1);
		assertSame(g1, g2);

		// Alternative binding resolution order.
		bh = getAssertionHelper();
		f2 = bh.assertNonProblem("f('i')", 1);
		f1 = bh.assertNonProblem("f(int)", 1);
		assertSame(f1, f2);
		g2 = bh.assertNonProblem("f('c')", 1);
		g1 = bh.assertNonProblem("f(char)", 1);
		assertSame(g1, g2);
	}

	// typedef int Int;
	// void f(int i);
	// void f(const int);
	// void f(Int i);
	// void g(char *);
	// void g(char []);
	// void h(int(a)());
	// void h(int (*) ());
	public void testFunctionDeclarations() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		IFunction f = (IFunction) collector.getName(1).resolveBinding();
		isTypeEqual(f.getType(), "void (int)");
		IFunction g = (IFunction) collector.getName(8).resolveBinding();
		isTypeEqual(g.getType(), "void (char *)");
		IFunction h = (IFunction) collector.getName(12).resolveBinding();
		isTypeEqual(h.getType(), "void (int (*)())");

		assertInstances(collector, f, 3);
		assertInstances(collector, g, 2);
		assertInstances(collector, h, 2);
	}

	// class P1 { public: int x; };
	// class P2 { public: int x; };
	// class B : public P1, public P2 {};
	// void main() {
	//    B * b = new B();
	//    b->x;
	// }
	public void testProblem_AmbiguousInParent() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		IProblemBinding x = (IProblemBinding) collector.getName(12).resolveBinding();
		assertEquals(x.getID(), IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP);
	}

	// class D { public: int x; };
	// class C : public virtual D {};
	// class B : public virtual D {};
	// class A : public B, public C {};
	// void main() {
	//    A * a = new A();
	//    a->x;
	// }
	public void testVirtualParentLookup() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector(true);
		tu.accept(collector);

		assertEquals(collector.size(), 16);

		ICPPClassType D = (ICPPClassType) collector.getName(0).resolveBinding();
		ICPPField x = (ICPPField) collector.getName(1).resolveBinding();
		ICPPClassType C = (ICPPClassType) collector.getName(2).resolveBinding();
		ICPPClassType B = (ICPPClassType) collector.getName(4).resolveBinding();
		ICPPClassType A = (ICPPClassType) collector.getName(6).resolveBinding();
		ICPPConstructor ctor = A.getConstructors()[0];
		assertInstances(collector, D, 3);
		assertInstances(collector, C, 2);
		assertInstances(collector, B, 2);
		assertInstances(collector, A, 3);
		assertInstances(collector, ctor, 1);
		assertInstances(collector, x, 2);
	}

	// class D { public: int x; };
	// class C : public D {};
	// class B : public D {};
	// class A : public B, public C {};
	// void main() {
	//    A * a = new A();
	//    a->x;
	// }
	public void testAmbiguousVirtualParentLookup() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector(true);
		tu.accept(collector);

		assertEquals(collector.size(), 16);

		ICPPClassType D = (ICPPClassType) collector.getName(0).resolveBinding();
		ICPPField x1 = (ICPPField) collector.getName(1).resolveBinding();
		ICPPClassType C = (ICPPClassType) collector.getName(2).resolveBinding();
		ICPPClassType B = (ICPPClassType) collector.getName(4).resolveBinding();
		ICPPClassType A = (ICPPClassType) collector.getName(6).resolveBinding();
		ICPPConstructor ctor = A.getConstructors()[0];
		IProblemBinding x2 = (IProblemBinding) collector.getName(15).resolveBinding();
		assertEquals(x2.getID(), IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP);

		assertInstances(collector, D, 3);
		assertInstances(collector, C, 2);
		assertInstances(collector, B, 2);
		assertInstances(collector, A, 3);
		assertInstances(collector, ctor, 1);
		assertInstances(collector, x1, 1);
	}

	// namespace A {
	//    int x;
	// }
	// int x;
	// namespace A {
	//    void f() { x; }
	// }
	public void testExtendedNamespaces() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector collector = new NameCollector();
		tu.accept(collector);

		assertEquals(collector.size(), 6);

		ICPPNamespace A = (ICPPNamespace) collector.getName(0).resolveBinding();
		IVariable x1 = (IVariable) collector.getName(1).resolveBinding();
		IVariable x2 = (IVariable) collector.getName(2).resolveBinding();

		assertInstances(collector, A, 2);
		assertInstances(collector, x1, 2);
		assertInstances(collector, x2, 1);
	}

	// class A { A(void);  A(const A &); };
	public void testConstructors() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);

		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		ICPPClassType A = (ICPPClassType) compSpec.getName().resolveBinding();
		ICPPConstructor[] ctors = A.getConstructors();

		assertNotNull(ctors);
		assertEquals(ctors.length, 2);

		assertEquals(0, ctors[0].getParameters().length);
		assertEquals(1, ctors[1].getParameters().length);

		IType t = ctors[1].getParameters()[0].getType();
		assertTrue(t instanceof ICPPReferenceType);
		assertTrue(((ICPPReferenceType) t).getType() instanceof IQualifierType);
		IQualifierType qt = (IQualifierType) ((ICPPReferenceType) t).getType();
		assertTrue(qt.isConst());
		assertSame(qt.getType(), A);
	}

	// class A {};
	public void testImplicitConstructors() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);

		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
		ICPPClassType A = (ICPPClassType) compSpec.getName().resolveBinding();
		ICPPConstructor[] ctors = A.getConstructors();

		assertNotNull(ctors);
		assertEquals(2, ctors.length);

		assertEquals(0, ctors[0].getParameters().length);

		assertEquals(1, ctors[1].getParameters().length);

		IType t = ctors[1].getParameters()[0].getType();
		assertTrue(t instanceof ICPPReferenceType);
		assertTrue(((ICPPReferenceType) t).getType() instanceof IQualifierType);
		IQualifierType qt = (IQualifierType) ((ICPPReferenceType) t).getType();
		assertTrue(qt.isConst());
		assertSame(qt.getType(), A);
	}

	//	template<typename T> struct C {
	//		C(const C<T>& c) {}
	//	};
	//	struct D {
	//      typedef const D& TD;
	//		D(TD c) {}
	//	};
	//  struct E {
	//     E();
	//  };
	//  typedef E F;
	//  F::E() {}
	public void testImplicitConstructors_360223() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPClassType c = bh.assertNonProblem("C", 0);
		ICPPConstructor[] ctors = c.getConstructors();
		assertEquals(1, ctors.length);
		assertFalse(ctors[0].isImplicit());

		c = bh.assertNonProblem("D", 0);
		ctors = c.getConstructors();
		assertEquals(1, ctors.length);
		assertFalse(ctors[0].isImplicit());

		IBinding ctor = bh.assertNonProblem("E() {}", 1);
		assertTrue(ctor instanceof ICPPConstructor);
	}

	//	struct A {
	//	  A(int);
	//	};
	//
	//	struct B : public A {
	//	  using A::A;
	//	};
	//
	//	void foo(B);
	//
	//	int test() {
	//	  foo(1);
	//	}
	public void testInheritedConstructor() throws Exception {
		parseAndCheckBindings();
	}

	//	template <class T>
	//	struct A {
	//	  A(T);
	//	};
	//
	//	struct B : public A<int> {
	//	  using A::A;
	//	};
	//
	//	void foo(B);
	//
	//	int test() {
	//	  foo(1);
	//	}
	public void testInheritedConstructorFromTemplateInstance() throws Exception {
		parseAndCheckBindings();
	}

	//	struct A {
	//	  A(int);
	//	};
	//
	//	template <class T>
	//	struct B : public T {
	//	  using T::T;
	//	};
	//
	//	void foo(B<A>);
	//
	//	int test() {
	//	  foo(1);
	//	}
	public void testInheritedConstructorFromUnknownClass() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct A {};
	//
	//	struct B {
	//	  template <typename T>
	//	  B(const A<T>&, int i = 3);
	//	};
	//
	//	struct C : public B {
	//	  using B::B;
	//	};
	//
	//	void foo(C);
	//
	//	void test(A<int> a) {
	//	  foo(a);
	//	}
	public void testInheritedTemplateConstructor() throws Exception {
		parseAndCheckBindings();
	}

	//	struct base {};
	//
	//	struct derived : public base {
	//		using base::base;
	//		base waldo() const;
	//	};
	public void testInheritedConstructorShadowingBaseClassName_485383() throws Exception {
		parseAndCheckBindings();
	}

	// class A { ~A(); };
	// class B { ~B(void); };
	public void testExplicitDestructor_183160() throws Exception {
		// class F { (~)F(); };
		// class G { (~)G(void); };

		BufferedReader br = new BufferedReader(new StringReader(getAboveComment()));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			IASTTranslationUnit tu = parse(line, CPP);
			IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
			ICPPClassType A = (ICPPClassType) compSpec.getName().resolveBinding();

			ICPPMethod[] methods = ((ICPPClassScope) A.getCompositeScope()).getImplicitMethods();
			assertNotNull(methods);
			int count = 0;
			for (ICPPMethod method : methods)
				count += method.getName().startsWith("~") ? 1 : 0;
			assertEquals(line, 0, count);

			methods = A.getDeclaredMethods();
			assertNotNull(methods);
			count = 0;
			for (ICPPMethod method : methods)
				count += method.getName().startsWith("~") ? 1 : 0;
			assertEquals(line, 1, count);
		}
	}

	// class C {};
	// class D {D();};
	// class E {E(void);};
	public void testImplicitDestructor_183160() throws Exception {
		BufferedReader br = new BufferedReader(new StringReader(getAboveComment()));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			IASTTranslationUnit tu = parse(line, CPP);
			IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
			ICPPClassType A = (ICPPClassType) compSpec.getName().resolveBinding();

			ICPPMethod[] methods = ((ICPPClassScope) A.getCompositeScope()).getImplicitMethods();
			assertNotNull(methods);
			int count = 0;
			for (ICPPMethod method : methods)
				count += method.getName().startsWith("~") ? 1 : 0;
			assertEquals(line, 1, count);

			methods = A.getDeclaredMethods();
			assertNotNull(methods);
			count = 0;
			for (ICPPMethod method : methods)
				count += method.getName().startsWith("~") ? 1 : 0;
			assertEquals(line, 0, count);
		}
	}

	//  class A {public: A();};
	//	class B {public: B() {}};
	//	class C {public: C() {}};
	//	class D {public: D(void) {}};
	//	class E {protected: E();};
	//	class F {protected: F() {}};
	//	class G {protected: G() {}};
	//	class H {protected: H(void) {}};
	//	class I {private: I();};
	//	class J {private: J() {}};
	//	class K {private: K() {}};
	//	class L {private: L(void) {}};
	//	class M {M();};
	//	class N {N() {}};
	//	class O {O() {}};
	//	class P {P(void) {}};
	//  class Q {public: Q(int k=5);};
	//  class R {public: R(int k=5, long k=4);};
	//  class S {public: S(int k=5, int* ip= 0);};
	//  class T {public: T(int k=5, int* ip= 0, T* t= 0);};
	public void testExplicitDefaultConstructor_183160() throws Exception {
		BufferedReader br = new BufferedReader(new StringReader(getAboveComment()));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			IASTTranslationUnit tu = parse(line, CPP);
			IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
			ICPPClassType A = (ICPPClassType) compSpec.getName().resolveBinding();
			ICPPConstructor[] ctors = A.getConstructors();

			assertNotNull(ctors);
			assertEquals(2, ctors.length); // one user-declared default constructor, one implicit copy constructor
		}
	}

	//  class Q {public: Q(int k);};
	//  class R {public: R(int k=5, long k);};
	//  class S {public: S(int k=5, int* ip);};
	//  class T {public: T(int k, int* ip= 0, T* t= 0);};
	public void testExplicitNonDefaultConstructor_183160() throws Exception {
		BufferedReader br = new BufferedReader(new StringReader(getAboveComment()));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			IASTTranslationUnit tu = parse(line, CPP);
			IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
			ICPPClassType A = (ICPPClassType) compSpec.getName().resolveBinding();
			ICPPConstructor[] ctors = A.getConstructors();

			assertNotNull(ctors);
			assertEquals(2, ctors.length); // one user-declared non-default constructor, one implicit copy constructor
		}
	}

	//  class A {public: A(A &);};
	//	class B {private: B(const B &);};
	//	class C {protected: C(volatile C &);};
	//	class D {D(const volatile D &) {}};
	//	class E {public: E(E &, int k=5);};
	//	class F {private: F(const F &, int j=2, int k=3);};
	//	class G {protected: G(volatile G &, int i=4, int l=2);};
	//	class H {H(const volatile H &, int i=1, long k=2) {}};
	public void testExplicitCopyConstructor_183160() throws Exception {
		BufferedReader br = new BufferedReader(new StringReader(getAboveComment()));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			IASTTranslationUnit tu = parse(line, CPP);
			IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
			ICPPClassType A = (ICPPClassType) compSpec.getName().resolveBinding();
			ICPPConstructor[] ctors = A.getConstructors();

			assertNotNull(ctors);
			// one copy constructor, no implicit default constructor
			assertEquals(1, ctors.length);
		}
	}

	//	class I {public: I(I *, int k=5);}; // I * rather than I &
	//	class J {private: J(const J *, int j, int k=3);}; // J * rather than J &
	//	class K {protected: K(volatile K *, int i=4, int l=2);}; // K * rather than K  &
	//	class L {L(const volatile L &, int i=1, long k=2, int* x) {}}; // param int* x has no initializer
	public void testNotExplicitCopyConstructor_183160() throws Exception {
		BufferedReader br = new BufferedReader(new StringReader(getAboveComment()));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			IASTTranslationUnit tu = parse(line, CPP);
			IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
			ICPPClassType A = (ICPPClassType) compSpec.getName().resolveBinding();
			ICPPConstructor[] ctors = A.getConstructors();

			assertNotNull(ctors);
			// one copy constructor, one user declared constructor (not a copy constructor)
			assertEquals(2, ctors.length);
		}
	}

	//	class A {public: void operator=(int); };  // legitimate, but not a copy assignment operator
	//	class B {public: void operator=(B &, int); };  // compile error
	//	class C {public: void operator=(C &c, int k=5) {} };  // compile error
	//	class D {public: void operator=(const D &, const D &); };  // compile error
	public void testNotExplicitCopyAssignmentOperator_183160() throws Exception {
		BufferedReader br = new BufferedReader(new StringReader(getAboveComment()));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			IASTTranslationUnit tu = parse(line, CPP);
			IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
			ICPPClassType A = (ICPPClassType) compSpec.getName().resolveBinding();
			ICPPMethod[] methods = ((ICPPClassScope) A.getCompositeScope()).getImplicitMethods();
			assertNotNull(methods);
			int count = 0;
			for (ICPPMethod method : methods) {
				boolean eq = Arrays.equals(method.getName().toCharArray(), OverloadableOperator.ASSIGN.toCharArray());
				count += eq ? 1 : 0;
			}

			assertEquals(1, count); // check for one implicit operator= method

			methods = A.getDeclaredMethods();
			assertNotNull(methods);
			count = 0;
			for (ICPPMethod method : methods) {
				boolean eq = Arrays.equals(method.getName().toCharArray(), OverloadableOperator.ASSIGN.toCharArray());
				count += eq ? 1 : 0;
			}

			assertEquals(1, count); // check for the user declared
		}
	}

	//	class A {public: void operator=(A &); };
	//	class B {protected: void operator=(const B &); };
	//	class C {private: void operator=(volatile C &) {} };
	//	class D {D& operator=(volatile const D &); };
	public void testExplicitCopyAssignmentOperator_183160() throws Exception {
		BufferedReader br = new BufferedReader(new StringReader(getAboveComment()));
		for (String line = br.readLine(); line != null; line = br.readLine()) {
			IASTTranslationUnit tu = parse(line, CPP);
			IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
			IASTCompositeTypeSpecifier compSpec = (IASTCompositeTypeSpecifier) decl.getDeclSpecifier();
			ICPPClassType A = (ICPPClassType) compSpec.getName().resolveBinding();
			ICPPMethod[] methods = ((ICPPClassScope) A.getCompositeScope()).getImplicitMethods();
			assertNotNull(methods);
			int count = 0;
			for (ICPPMethod method : methods) {
				boolean eq = Arrays.equals(method.getName().toCharArray(), OverloadableOperator.ASSIGN.toCharArray());
				count += eq ? 1 : 0;
			}

			assertEquals(0, count); // check for no implicit operator= methods

			methods = A.getDeclaredMethods();
			assertNotNull(methods);
			count = 0;
			for (ICPPMethod method : methods) {
				boolean eq = Arrays.equals(method.getName().toCharArray(), OverloadableOperator.ASSIGN.toCharArray());
				count += eq ? 1 : 0;
			}

			assertEquals(1, count); // only should get the user declared
		}
	}

	//	struct A {
	//	  A(int x);
	//	  A(char x);
	//	};
	//
	//	A a = A(1);
	public void testConstructorCall() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPConstructor ctor = bh.assertNonProblem("A(int x)", "A", ICPPConstructor.class);
		ICPPClassType classType = bh.assertNonProblem("A(1)", "A", ICPPClassType.class);
		assertSame(ctor.getOwner(), classType);
		IASTName name = bh.findName("A(1)", "A");
		IASTImplicitName[] implicitNames = ((IASTImplicitNameOwner) name.getParent().getParent()).getImplicitNames();
		assertEquals(1, implicitNames.length);
		IBinding ctor2 = implicitNames[0].getBinding();
		assertSame(ctor, ctor2);
	}

	//	struct A {
	//	  A(int x, int y);
	//	};
	//
	//	void test() {
	//	  A a("hi", 5, 10);
	//	}
	public void testInvalidImplicitConstructorCall() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertImplicitName("a", 1, IProblemBinding.class);
	}

	//	struct A {
	//	  A(int x);
	//	};
	//	struct B {
	//	  operator A();
	//	};
	//
	//	void test() {
	//	  B b;
	//	  A a = A(b);
	//	}
	public void _testConversionOperator() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPMethod oper = bh.assertNonProblem("operator A", "operator A", ICPPMethod.class);
		ICPPMethod conv = bh.assertNonProblem("A(b)", "A", ICPPMethod.class);
		// This assertion fails because conv is the copy ctor A(const A&), not the conversion operator B::operator A()
		assertSame(oper, conv);
	}

	//	struct S {
	//	  operator int() &;
	//	  operator int() &&;
	//	};
	//
	//	void waldo(int);
	//
	//	int main() {
	//	  S s;
	//	  waldo(s);  // ERROR
	//	}
	public void testOverloadedRefQualifiedConversionOperators_506728() throws Exception {
		parseAndCheckBindings();
	}

	// namespace A { int x; }
	// namespace B = A;
	// int f(){ B::x;  }
	public void testNamespaceAlias() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(col.size(), 8);
		ICPPNamespace A = (ICPPNamespace) col.getName(0).resolveBinding();
		IVariable x = (IVariable) col.getName(1).resolveBinding();
		ICPPNamespace B = (ICPPNamespace) col.getName(2).resolveBinding();
		assertTrue(B instanceof ICPPNamespaceAlias);
		assertSame(((ICPPNamespaceAlias) B).getBinding(), A);

		assertInstances(col, A, 2);
		assertInstances(col, B, 2);
		assertInstances(col, x, 3);
	}

	// public void testBug84250() throws Exception {
	// assertTrue(((IASTDeclarationStatement) ((IASTCompoundStatement)
	// ((IASTFunctionDefinition) parse(
	// "void f() { int (*p) [2]; }",
	// CPP).getDeclarations()[0]).getBody()).getStatements()[0]).getDeclaration()
	// instanceof IASTSimpleDeclaration);
	// }

	// void f() {
	//    int (*p) [2];
	//    (&p)[0] = 1;
	// }
	public void testBug84250() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(col.size(), 3);
		IVariable p = (IVariable) col.getName(1).resolveBinding();
		assertTrue(p.getType() instanceof IPointerType);
		assertTrue(((IPointerType) p.getType()).getType() instanceof IArrayType);
		IArrayType at = (IArrayType) ((IPointerType) p.getType()).getType();
		assertTrue(at.getType() instanceof IBasicType);

		assertInstances(col, p, 2);
	}

	// void f() {
	//    int (*p) [2];
	//    (&p)[0] = 1;
	// }
	public void testBug84250b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(col.size(), 3);

		IVariable p_ref = (IVariable) col.getName(2).resolveBinding();
		IVariable p_decl = (IVariable) col.getName(1).resolveBinding();

		assertSame(p_ref, p_decl);
	}

	// struct s { double i; } f(void);
	// struct s f(void){}
	public void testBug84266() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(col.size(), 7);

		ICompositeType s_ref = (ICompositeType) col.getName(4).resolveBinding();
		ICompositeType s_decl = (ICompositeType) col.getName(0).resolveBinding();

		assertSame(s_ref, s_decl);
	}

	public void testBug84266b() throws Exception {
		IASTTranslationUnit tu = parse("struct s f(void);", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(col.size(), 3);

		ICompositeType s = (ICompositeType) col.getName(0).resolveBinding();
		assertNotNull(s);

		tu = parse("struct s f(void){}", CPP);
		col = new NameCollector();
		tu.accept(col);

		assertEquals(col.size(), 3);

		s = (ICompositeType) col.getName(0).resolveBinding();
		assertNotNull(s);
	}

	// void f(int m, int c[m][m]);
	// void f(int m, int c[m][m]){
	//    int x;
	//    { int x = x; }
	// }
	public void testBug84228() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

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

	// class A { public : static int n; };
	// int main() {
	//    int A;
	//    A::n = 42;
	//    A b;
	// }
	public void testBug84615() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(col.size(), 9);

		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPField n = (ICPPField) col.getName(1).resolveBinding();
		IBinding Aref = col.getName(5).resolveBinding();
		IBinding nref = col.getName(6).resolveBinding();
		IProblemBinding prob = (IProblemBinding) col.getName(7).resolveBinding();

		assertSame(A, Aref);
		assertSame(n, nref);
		assertNotNull(prob);
	}

	public void testBug84371() throws Exception {
		String code = "int x = ::ABC::DEF::ghi;";
		IASTTranslationUnit tu = parse(code, CPP);
		IASTSimpleDeclaration x = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		IASTEqualsInitializer e = (IASTEqualsInitializer) x.getDeclarators()[0].getInitializer();
		IASTIdExpression id = (IASTIdExpression) e.getInitializerClause();
		ICPPASTQualifiedName name = (ICPPASTQualifiedName) id.getName();
		assertTrue(name.isFullyQualified());
		assertEquals(name.getQualifier().length, 2);
		assertEquals(name.getQualifier()[0].toString(), "ABC");
		assertEquals(name.getQualifier()[1].toString(), "DEF");
		assertEquals(name.getLastName().toString(), "ghi");
	}

	// namespace Y { void f(float); }
	// namespace A { using namespace Y; void f(int); }
	// namespace B { void f(char);  }
	// namespace AB { using namespace A; using namespace B; }
	// void h(){
	//    AB::f(1);
	//    AB::f(`c`);
	// }
	public void testBug84679() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, false, false);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPNamespace Y = (ICPPNamespace) col.getName(0).resolveBinding();
		ICPPNamespace A = (ICPPNamespace) col.getName(3).resolveBinding();
		ICPPNamespace B = (ICPPNamespace) col.getName(7).resolveBinding();
		ICPPNamespace AB = (ICPPNamespace) col.getName(10).resolveBinding();

		IFunction f = (IFunction) col.getName(16).resolveBinding();
		IFunction fdef = (IFunction) col.getName(5).resolveBinding();
		assertTrue(col.getName(19).resolveBinding() instanceof IProblemBinding);
		assertSame(f, fdef);
		// assertEquals(IProblemBinding.SEMANTIC_NAME_NOT_FOUND, f2.getID());
		assertInstances(col, Y, 2);
		assertInstances(col, A, 2);
		assertInstances(col, B, 2);
		assertInstances(col, AB, 3);
	}

	// struct Node {
	//    struct Node* Next;
	//    struct Data* Data;
	// };
	// struct Data {
	//    struct Node * node;
	//    friend struct Glob;
	// };
	public void testBug84692() throws Exception {
		// also tests bug 234042.
		CPPASTNameBase.sAllowRecursionBindings = false;

		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(col.size(), 9);

		ICPPClassType Node = (ICPPClassType) col.getName(1).resolveBinding();
		ICPPClassType Data = (ICPPClassType) col.getName(3).resolveBinding();
		assertSame(Data.getScope(), tu.getScope());

		assertInstances(col, Node, 3);
		assertInstances(col, Data, 2);
	}

	// namespace B { int b; }
	// namespace A { using namespace B;  int a;  }
	// namespace B { using namespace A; }
	// void f() { B::a++;  }
	public void testBug84686() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(col.size(), 11);

		ICPPVariable a1 = (ICPPVariable) col.getName(4).resolveBinding();
		IVariable a2 = (IVariable) col.getName(10).resolveBinding();
		assertSame(a1, a2);
	}

	// struct C {
	//    void f();
	//    const C& operator=(const C&);
	// };
	// const C& C::operator=(const C& other) {
	//    if (this != &other) {
	//       this->~C();
	//       new (this) C(other);
	//       f();
	//    }
	//    return *this;
	// }
	public void testBug84705() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

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
		assertEquals(dtor.getName(), "~C");
		assertInstances(col, C, 7);

		assertInstances(col, op, 3);
		assertInstances(col, other, 4);
	}

	// class A { void f(); void g() const; };
	// void A::f(){ this; }
	// void A::g() const { *this; }
	public void testThis() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);

		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		ICPPClassType A = (ICPPClassType) ((IASTCompositeTypeSpecifier) decl.getDeclSpecifier()).getName()
				.resolveBinding();

		IASTFunctionDefinition def = (IASTFunctionDefinition) tu.getDeclarations()[1];
		IASTExpressionStatement expStatement = (IASTExpressionStatement) ((IASTCompoundStatement) def.getBody())
				.getStatements()[0];
		assertTrue(expStatement.getExpression() instanceof IASTLiteralExpression);
		IType type = expStatement.getExpression().getExpressionType();

		assertTrue(type instanceof IPointerType);
		assertSame(((IPointerType) type).getType(), A);

		def = (IASTFunctionDefinition) tu.getDeclarations()[2];
		expStatement = (IASTExpressionStatement) ((IASTCompoundStatement) def.getBody()).getStatements()[0];
		IASTUnaryExpression ue = (IASTUnaryExpression) expStatement.getExpression();
		type = ue.getExpressionType();

		assertTrue(type instanceof IQualifierType);
		assertSame(((IQualifierType) type).getType(), A);
		assertTrue(((IQualifierType) type).isConst());
	}

	public void testBug84710() throws Exception {
		IASTTranslationUnit tu = parse("class T { T(); };", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);
		ICPPConstructor T = (ICPPConstructor) col.getName(1).resolveBinding();
		assertTrue(CharArrayUtils.equals(T.getNameCharArray(), "T".toCharArray()));
		assertEquals(T.getName(), "T");
	}

	// namespace NS {
	//    class T {};
	//    void f(T);
	// }
	// NS::T parm;
	// int main() {
	//    f(parm);
	// }
	public void testArgumentDependantLookup() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPNamespace NS = (ICPPNamespace) col.getName(0).resolveBinding();
		ICPPClassType T = (ICPPClassType) col.getName(1).resolveBinding();
		IFunction f = (IFunction) col.getName(2).resolveBinding();
		IVariable parm = (IVariable) col.getName(8).resolveBinding();

		assertInstances(col, NS, 2);
		assertInstances(col, T, 4);
		assertInstances(col, f, 2);
		assertInstances(col, parm, 2);
	}

	// namespace NS1{
	//    void f(void *);
	// }
	// namespace NS2{
	//    using namespace NS1;
	//    class B {};
	//    void f(void *);
	// }
	// class A : public NS2::B {} *a;
	// int main() {
	//    f(a);
	// }
	public void testArgumentDependantLookup_b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction fref = (IFunction) col.getName(14).resolveBinding();
		IFunction f1 = (IFunction) col.getName(1).resolveBinding();
		IFunction f2 = (IFunction) col.getName(6).resolveBinding();

		assertSame(f2, fref);
		assertNotNull(f1);
		assertNotNull(f2);
	}

	// namespace { int i; } //1
	// void f(){ i; }
	// namespace A {
	//    namespace {
	//       int i;    //2
	//       int j;
	//    }
	//    void g(){ i; }
	// }
	// using namespace A;
	// void h() {
	//    i;    //ambiguous
	//    A::i; //i2
	//    j;
	// }
	public void testBug84610() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(17, col.size());

		IVariable i1 = (IVariable) col.getName(1).resolveBinding();
		IVariable i2 = (IVariable) col.getName(6).resolveBinding();
		IVariable j = (IVariable) col.getName(7).resolveBinding();

		assertInstances(col, i1, 2);
		assertInstances(col, i2, 4);
		assertInstances(col, j, 2);

		IProblemBinding problem = (IProblemBinding) col.getName(12).resolveBinding();
		assertEquals(IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, problem.getID());
	}

	// struct B {
	//    void mutate();
	// };
	// void g() {
	//    B* pb = new B();
	//    pb->mutate();
	// }
	public void testBug84703() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(8, col.size());

		ICPPMethod mutate = (ICPPMethod) col.getName(1).resolveBinding();
		ICPPClassType B = (ICPPClassType) col.getName(0).resolveBinding();
		IVariable pb = (IVariable) col.getName(4).resolveBinding();

		assertInstances(col, pb, 2);
		assertInstances(col, mutate, 2);
		assertInstances(col, B, 3);
	}

	// struct S { int i; };
	// void f() {         ;
	//    int S::* pm = &S::i;
	// }
	public void testBug84469() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(9, col.size());
	}

	public void testPointerToMemberType() throws Exception {
		IASTTranslationUnit tu = parse("struct S; int S::* pm;", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(4, col.size());

		IVariable pm = (IVariable) col.getName(3).resolveBinding();
		ICPPClassType S = (ICPPClassType) col.getName(0).resolveBinding();

		IType t = pm.getType();
		assertNotNull(t);
		assertTrue(t instanceof ICPPPointerToMemberType);
		IType cls = ((ICPPPointerToMemberType) t).getMemberOfClass();
		assertSame(S, cls);
		assertTrue(((ICPPPointerToMemberType) t).getType() instanceof IBasicType);
	}

	// struct S { int i ; } *s;
	// int S::* pm = &S::i;
	// void f() {
	//    s->*pm = 1;
	// }
	public void testBug_PM_() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IBinding ref = col.getName(11).resolveBinding();
		IVariable pm = (IVariable) col.getName(5).resolveBinding();

		assertSame(pm, ref);
	}

	// struct S {
	//    int i;
	//    S* f();
	// } *s;
	// S* (S::* pm) () = &S::f;
	// void foo() {
	//    (s->*pm)()->i;
	// }
	public void testBug_PM_b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType S = (ICPPClassType) col.getName(0).resolveBinding();
		IVariable pm = (IVariable) col.getName(8).resolveBinding();
		IField i = (IField) col.getName(1).resolveBinding();
		ICPPMethod f = (ICPPMethod) col.getName(3).resolveBinding();

		IType t = pm.getType();
		assertTrue(t instanceof ICPPPointerToMemberType);
		IFunctionType ft = (IFunctionType) ((ICPPPointerToMemberType) t).getType();
		IType ST = ((ICPPPointerToMemberType) t).getMemberOfClass();

		assertTrue(ft.getReturnType() instanceof IPointerType);
		assertSame(ST, ((IPointerType) ft.getReturnType()).getType());
		assertSame(S, ST);

		assertInstances(col, S, 5);
		assertInstances(col, pm, 2);
		assertInstances(col, i, 2);
		assertInstances(col, f, 3);
	}

	// public void testFindTypeBinding_a() throws Exception {
	// IASTTranslationUnit tu = parse(
	// "int x = 5; int y(x);", CPP);
	//
	// IASTStandardFunctionDeclarator fdtor = (IASTStandardFunctionDeclarator)
	// ((IASTSimpleDeclaration) tu
	// .getDeclarations()[1]).getDeclarators()[0];
	// IASTName name = fdtor.getParameters()[0].getDeclarator().getName();
	// IBinding binding = CPPSemantics.findTypeBinding(tu, name);
	// assertNull(binding);
	//
	// tu = parse("struct x; int y(x);", CPP);
	//
	// fdtor = (IASTStandardFunctionDeclarator) ((IASTSimpleDeclaration) tu
	// .getDeclarations()[1]).getDeclarators()[0];
	// name = ((ICPPASTNamedTypeSpecifier) fdtor.getParameters()[0]
	// .getDeclSpecifier()).getName();
	// binding = CPPSemantics.findTypeBinding(tu, name);
	// assertNotNull(binding);
	// assertTrue(binding instanceof ICPPClassType);
	// }
	//
	// public void testFindTypeBinding_b() throws Exception {
	// IASTTranslationUnit tu = parse(
	// "struct B; void f() { B * bp; }", CPP);
	// IASTCompoundStatement compound = (IASTCompoundStatement)
	// ((IASTFunctionDefinition) tu
	// .getDeclarations()[1]).getBody();
	// IASTBinaryExpression b = (IASTBinaryExpression)
	// ((IASTExpressionStatement)compound.getStatements()[0]).getExpression();
	// IBinding binding =
	// ((IASTIdExpression)b.getOperand1()).getName().resolveBinding();
	// // IASTSimpleDeclaration decl = (IASTSimpleDeclaration)
	// ((IASTDeclarationStatement) compound
	// // .getStatements()[0]).getDeclaration();
	// // IBinding binding = CPPSemantics.findTypeBinding(compound,
	// // ((ICPPASTNamedTypeSpecifier)decl.getDeclSpecifier()).getName());
	// assertNotNull(binding);
	// assertTrue(binding instanceof ICPPClassType);
	// }

	// struct B { };
	// void g() {
	// B * bp;  //1
	// }
	public void testBug85049() throws Exception {
		IASTTranslationUnit t = parse(getAboveComment(), CPP);
		IASTFunctionDefinition g = (IASTFunctionDefinition) t.getDeclarations()[1];
		IASTCompoundStatement body = (IASTCompoundStatement) g.getBody();
		assertTrue(body.getStatements()[0] instanceof IASTDeclarationStatement);
	}

	// class A { public: int i; };
	// class B : public A {};
	// void f(int B::*);
	// void g() {
	//    int A::* pm = &A::i;
	//    f(pm);
	// }
	public void testPMConversions() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f = (IFunction) col.getName(15).resolveBinding();
		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPField i = (ICPPField) col.getName(1).resolveBinding();
		ICPPClassType B = (ICPPClassType) col.getName(2).resolveBinding();
		IVariable pm = (IVariable) col.getName(11).resolveBinding();

		assertInstances(col, f, 2);
		assertInstances(col, A, 4);
		assertInstances(col, i, 3);
		assertInstances(col, B, 2);
		assertInstances(col, pm, 2);
	}

	// namespace N {
	//    class A { public: int i; };
	//    void f(int A::*);
	// }
	// int N::A::* pm = &N::A::i;
	// void g() {
	//    f(pm);
	// }
	public void testPMKoenig_a() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f = (IFunction) col.getName(16).resolveBinding();
		ICPPNamespace N = (ICPPNamespace) col.getName(0).resolveBinding();
		ICPPClassType A = (ICPPClassType) col.getName(1).resolveBinding();

		assertInstances(col, f, 2);
		assertInstances(col, N, 3);
		assertInstances(col, A, 4);
	}

	// namespace M {
	//    class B { };
	//    void f(B*);
	// }
	// namespace N {
	//    class A { public: M::B * b; };
	// }
	// M::B* N::A::* pm = &N::A::b;
	// void g() {
	//    N::A * a;
	//    f(a->*pm);
	// }
	public void testPMKoenig_b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f = (IFunction) col.getName(27).resolveBinding();
		ICPPNamespace M = (ICPPNamespace) col.getName(0).resolveBinding();
		ICPPClassType B = (ICPPClassType) col.getName(1).resolveBinding();
		ICPPNamespace N = (ICPPNamespace) col.getName(5).resolveBinding();
		ICPPClassType A = (ICPPClassType) col.getName(6).resolveBinding();
		IVariable pm = (IVariable) col.getName(17).resolveBinding();

		assertInstances(col, f, 2);
		assertInstances(col, M, 3);
		assertInstances(col, B, 6);
		assertInstances(col, N, 4);
		assertInstances(col, A, 5);
		assertInstances(col, pm, 2);
	}

	// class A {
	//    friend void set();
	//    friend class B;
	// };
	// void set();
	// class B{};
	public void testFriend() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		IFunction set = (IFunction) col.getName(1).resolveBinding();
		ICPPClassType B = (ICPPClassType) col.getName(2).resolveBinding();

		assertInstances(col, set, 2);
		assertInstances(col, B, 2);

		IBinding[] friends = A.getFriends();
		assertEquals(2, friends.length);
		assertSame(friends[0], set);
		assertSame(friends[1], B);
	}

	// class B;
	// class A {
	//    friend B;
	// };
	// class B{};
	public void testForwardDeclaredFriend_525645() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		ICPPClassType A = bh.assertNonProblem("A", 1);
		ICPPClassType B = bh.assertNonProblem("B", 1);

		IBinding[] friends = A.getFriends();
		assertEquals(1, friends.length);
		assertSame(friends[0], B);
	}

	// class Other {
	//    void m(); };
	// class A {
	//    friend void set();
	//    friend void Other::m();
	// };
	public void testFriend_275358() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		ICPPClassType A = bh.assertNonProblem("A", 1);
		IFunction set = bh.assertNonProblem("set()", 3);
		IFunction m = bh.assertNonProblem("Other::m()", 8);

		IBinding[] friends = A.getFriends();
		assertEquals(2, friends.length);
		assertSame(friends[0], set);
		assertSame(friends[1], m);

		IBinding[] declaredMethods = A.getAllDeclaredMethods();
		assertEquals(0, declaredMethods.length);
		declaredMethods = A.getDeclaredMethods();
		assertEquals(0, declaredMethods.length);
	}

	// class A { friend class B; friend class B; };
	// class B{};
	public void testBug59149() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType B = (ICPPClassType) col.getName(2).resolveBinding();
		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();

		assertInstances(col, B, 3);

		IBinding[] friends = A.getFriends();
		assertEquals(friends.length, 1);
		assertSame(friends[0], B);
	}

	// class A {
	//    public: class N {};
	// };
	// class B {
	//    friend class A::N;
	// };
	public void testBug59302() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType N = (ICPPClassType) col.getName(5).resolveBinding();
		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPClassType B = (ICPPClassType) col.getName(2).resolveBinding();
		assertInstances(col, N, 3);

		IBinding[] friends = B.getFriends();
		assertEquals(friends.length, 1);
		assertSame(friends[0], N);

		assertEquals(A.getFriends().length, 0);
		assertEquals(N.getFriends().length, 0);
	}

	// class A {
	//    friend class B *helper();
	// };
	public void testBug75482() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction helper = (IFunction) col.getName(2).resolveBinding();
		assertSame(helper.getScope(), tu.getScope());

		ICPPClassType B = (ICPPClassType) col.getName(1).resolveBinding();
		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		assertSame(B.getScope(), A.getScope());

		IBinding[] friends = A.getFriends();
		assertEquals(friends.length, 1);
		assertSame(friends[0], helper);
	}

	// void f(int);
	// void f(char);
	// void (*pf) (int) = &f;
	// void foo() {
	//    pf = &f;
	// }
	public void testBug45763a() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f1 = (IFunction) col.getName(0).resolveBinding();
		IFunction f2 = (IFunction) col.getName(2).resolveBinding();
		IVariable pf = (IVariable) col.getName(4).resolveBinding();

		assertInstances(col, pf, 2);
		assertInstances(col, f1, 3);
		assertInstances(col, f2, 1);
	}

	// void f(char);
	// void f(int);
	// void g(void (*)(int)) {}
	// void (*pg)(void(*)(int));
	// void foo() {
	//    g(&f);
	//    (*pg)(&f);
	// }
	public void testBug45763b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f1 = (IFunction) col.getName(0).resolveBinding();
		IFunction f2 = (IFunction) col.getName(2).resolveBinding();
		IFunction g = (IFunction) col.getName(4).resolveBinding();
		IVariable pg = (IVariable) col.getName(7).resolveBinding();

		assertInstances(col, f1, 1);
		assertInstances(col, f2, 3);
		assertInstances(col, g, 2);
		assertInstances(col, pg, 2);
	}

	// void f(int);
	// void f(char);
	// void (* bar ()) (int) {
	//    return &f;
	// }
	public void testBug45763c() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f1 = (IFunction) col.getName(0).resolveBinding();
		IFunction f2 = (IFunction) col.getName(2).resolveBinding();
		IFunction bar = (IFunction) col.getName(4).resolveBinding();
		assertNotNull(bar);

		assertInstances(col, f1, 2);
		assertInstances(col, f2, 1);
	}

	// void f(int);
	// void f(char);
	// void foo () {
	//    (void (*)(int)) &f;
	// }
	public void testBug45763d() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f1 = (IFunction) col.getName(0).resolveBinding();
		IFunction f2 = (IFunction) col.getName(2).resolveBinding();
		IFunction bar = (IFunction) col.getName(4).resolveBinding();
		assertNotNull(bar);

		assertInstances(col, f1, 2);
		assertInstances(col, f2, 1);
	}

	// extern int g;
	// int g;
	// void f() {  g = 1; }
	public void testBug85824() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IVariable g = (IVariable) col.getName(3).resolveBinding();
		assertInstances(col, g, 3);
	}

	// struct A {
	//     int a2;
	// };
	// struct B : public A {
	//     int a1;
	//     void f();
	// };
	// int a3;
	// void B::f(){
	//    int a4;
	//    a;
	// }
	public void testPrefixLookup() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IASTName name = col.getName(11);
		assertEquals("a", name.toString());
		IBinding[] bs = CPPSemantics.findBindingsForContentAssist(name, true, null);

		// check the result
		HashSet result = new HashSet();
		for (IBinding binding : bs) {
			result.add(binding.getName());
		}
		assertTrue(result.contains("a1"));
		assertTrue(result.contains("a2"));
		assertTrue(result.contains("a3"));
		assertTrue(result.contains("a4"));
		assertTrue(result.contains("A"));
		assertEquals(7, bs.length); // the bindings above + 2 constructors
	}

	// namespace ns {
	//   int v_outer;
	//   namespace inner {
	//     int v_inner;
	//   }
	// }
	// void test(){
	//   v_;
	// }
	public void testAdditionalNamespaceLookup() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IASTName name = col.getName(5);
		assertEquals("v_", name.toString());
		IBinding[] bs = CPPSemantics.findBindingsForContentAssist(name, true, new String[] { "ns::inner" });

		// check the result
		HashSet result = new HashSet();
		for (IBinding binding : bs) {
			result.add(binding.getName());
		}
		assertTrue(result.contains("v_inner"));
		assertEquals(1, bs.length); // the bindings above
	}

	// static void f();
	// void f() {}
	public void testIsStatic() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f = (IFunction) col.getName(1).resolveBinding();
		assertTrue(f.isStatic());
		assertInstances(col, f, 2);
	}

	//	// void f() {
	//	// if (__io.flags() & ios_base::showbase
	//	// || __i < 2 || __sign.size() > 1
	//	// || ((static_cast<part>(__p.field[3]) !=
	//	// money_base::none)
	//	// && __i == 2))
	//	// return;
	//	// }
	//	public void testBug85310() throws Exception {
	//		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
	//		IASTFunctionDefinition f = (IASTFunctionDefinition)
	//		tu.getDeclarations()[0];
	//		IASTCompoundStatement body = (IASTCompoundStatement) f.getBody();
	//		IASTCPPIfStatement if_stmt = (IASTCPPIfStatement) body.getStatements()[0];
	//		assertNotNull(if_stmt.getCondition());
	//	}

	// struct B { void mutate(); };
	// struct D1 : B {};
	// struct D2 : B {};
	// void B::mutate() {
	//    new (this) D2;
	// }
	// void g() {
	//    B* pb = new (p) D1;
	// }
	public void testBug86267() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector(true);
		tu.accept(col);

		ICPPClassType D1 = (ICPPClassType) col.getName(2).resolveBinding();
		ICPPClassType D2 = (ICPPClassType) col.getName(4).resolveBinding();

		ICPPConstructor[] ctors = D1.getConstructors();
		ICPPConstructor d1_ctor = ctors[0];

		ctors = D2.getConstructors();
		ICPPConstructor d2_ctor = ctors[0];

		assertInstances(col, d1_ctor, 1);
		assertInstances(col, d2_ctor, 1);
	}

	// struct C {
	//    void f();
	//    const C& operator =(const C&);
	// };
	// const C& C::operator = (const C& other) {
	//    if (this != &other) {
	//       this->~C();
	//       new (this) C(other);
	//       f();
	//    }
	//    return *this;
	// }
	public void testBug86269() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType C = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPMethod f = (ICPPMethod) col.getName(1).resolveBinding();
		ICPPMethod op = (ICPPMethod) col.getName(3).resolveBinding();
		IParameter other = (IParameter) col.getName(5).resolveBinding();

		assertInstances(col, C, 7);
		assertInstances(col, f, 2);
		assertInstances(col, op, 3);
		assertInstances(col, other, 4);

		assertEquals(other.getName(), "other");
	}

	// extern "C" {
	//    void printf(const char *);
	//    void sprintf(const char *);
	// }
	// void foo(){
	//    char *p;
	//    printf(p);
	//    printf("abc");
	// }
	public void testBug86279() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction r1 = (IFunction) col.getName(6).resolveBinding();
		IFunction r2 = (IFunction) col.getName(8).resolveBinding();
		IFunction printf = (IFunction) col.getName(0).resolveBinding();

		assertSame(printf, r1);
		assertSame(printf, r2);
	}

	// struct S;
	// extern S a;
	// void g(S);
	// void h() {
	//    g(a);
	// }
	public void testBug86346() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType S = (ICPPClassType) col.getName(0).resolveBinding();
		IFunction g = (IFunction) col.getName(3).resolveBinding();

		assertInstances(col, S, 3);
		assertInstances(col, g, 2);
	}

	public void testBug86288() throws Exception {
		String code = "int *foo(int *b) { return (int *)(b); }";
		IASTTranslationUnit tu = parse(code, CPP);
		IASTFunctionDefinition function = (IASTFunctionDefinition) tu.getDeclarations()[0];
		IASTReturnStatement r = (IASTReturnStatement) ((IASTCompoundStatement) function.getBody()).getStatements()[0];
		assertTrue(r.getReturnValue() instanceof IASTCastExpression);
	}

	// void foo() {
	// struct B {
	// int f();
	// };
	// int (B::*pb)() = &B::f;
	// }
	public void testBug84476() throws Exception {
		String code = getAboveComment();
		IASTFunctionDefinition foo = (IASTFunctionDefinition) parse(code, CPP).getDeclarations()[0];
		IASTDeclarationStatement decl = (IASTDeclarationStatement) ((IASTCompoundStatement) foo.getBody())
				.getStatements()[1];
		IASTSimpleDeclaration pb = (IASTSimpleDeclaration) decl.getDeclaration();
		IASTDeclarator d = pb.getDeclarators()[0];
		assertEquals(d.getNestedDeclarator().getPointerOperators().length, 1);
		assertEquals(d.getNestedDeclarator().getName().toString(), "pb");
		assertTrue(d.getNestedDeclarator().getPointerOperators()[0] instanceof ICPPASTPointerToMember);
	}

	// struct A {
	//    A operator()(int x) {
	//       return A(x);
	//    }
	//    A(int) {}
	// };
	public void testBug86336() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPClassType clazz = bh.assertNonProblem("A {", "A", ICPPClassType.class);
		ICPPConstructor ctor = bh.assertNonProblem("A(int)", "A", ICPPConstructor.class);
		ICPPClassType clazz2 = bh.assertNonProblem("A(x)", "A", ICPPClassType.class);
		assertSame(clazz, clazz2);
		clazz2 = bh.assertNonProblem("A operator", "A", ICPPClassType.class);
		assertSame(clazz, clazz2);
		IASTName name = bh.findName("A(x)", "A");
		IASTImplicitName[] implicitNames = ((IASTImplicitNameOwner) name.getParent().getParent()).getImplicitNames();
		assertEquals(1, implicitNames.length);
		IBinding ctor2 = implicitNames[0].getBinding();
		assertSame(ctor, ctor2);
	}

	// struct S { int i; };
	// void foo() {
	//    int S::* pm = &S::i;
	// }
	public void testBug86306() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType S = (ICPPClassType) col.getName(0).resolveBinding();

		assertInstances(col, S, 3);

		IASTName[] refs = tu.getReferences(S);
		assertEquals(refs.length, 2);
		assertSame(refs[0], col.getName(4));
		assertSame(refs[1], col.getName(7));
	}

	// class A {
	// 	public:
	//    template <class T> void f(T);
	//    template <class T> struct X { };
	// };
	// class B : public A {
	//  public:
	//    using A::f<double>; // illformed
	//    using A::X<int>; // illformed
	// };
	public void testBug86372() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);
	}

	// void foo() {
	//    int i = 42;
	//    int a[10];
	//    for(int i = 0; i < 10; i++)
	//       a[i] = 1;
	//    int j = i;
	// }
	public void testBug86319() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IVariable i1 = (IVariable) col.getName(1).resolveBinding();
		IVariable i2 = (IVariable) col.getName(3).resolveBinding();

		assertNotSame(i1, i2);
		assertInstances(col, i1, 2);
		assertInstances(col, i2, 4);
	}

	// class X { int i, j; };
	// class Y { X x; };
	// void foo() {
	//    const Y y;
	//    y.x.i++;
	//    y.x.j++;
	//    Y* p = const_cast<Y*>(&y);
	//    p->x.i;
	//    p->x.j;
	// }
	public void testBug86350() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPField i = (ICPPField) col.getName(1).resolveBinding();
		ICPPField j = (ICPPField) col.getName(2).resolveBinding();
		ICPPField x = (ICPPField) col.getName(5).resolveBinding();

		assertInstances(col, i, 3);
		assertInstances(col, j, 3);
		assertInstances(col, x, 5);
	}

	// void foo() {
	//   struct A {
	//     int val;
	//     A(int i) : val(i) { }
	//     ~A() { }
	//     operator bool() { return val != 0; }
	//   };
	//   int i = 1;
	//   while (A a = i) {
	//     i = 0;
	//   }
	// }
	public void testBug84478() throws Exception {
		IASTFunctionDefinition foo = (IASTFunctionDefinition) parse(getAboveComment(), CPP).getDeclarations()[0];
		ICPPASTWhileStatement whileStatement = (ICPPASTWhileStatement) ((IASTCompoundStatement) foo.getBody())
				.getStatements()[2];
		assertNull(whileStatement.getCondition());
		assertNotNull(whileStatement.getConditionDeclaration());
	}

	//	void foo() {
	//	  const int x = 12;
	//	  {   enum { x = x };  }
	//	}
	//	enum { RED };
	public void testBug86353() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IEnumerator enum_x = (IEnumerator) col.getName(3).resolveBinding();
		IBinding x_ref = col.getName(4).resolveBinding();
		IEnumerator RED = (IEnumerator) col.getName(6).resolveBinding();

		String[] s = ((ICPPBinding) RED).getQualifiedName();
		assertEquals(s[0], "RED");
		assertTrue(((ICPPBinding) RED).isGloballyQualified());

		IASTName[] decls = tu.getDeclarationsInAST(enum_x);
		assertEquals(decls.length, 1);
		assertSame(decls[0], col.getName(3));

		decls = tu.getDeclarationsInAST(x_ref);
		assertEquals(decls.length, 1);
		assertSame(decls[0], col.getName(1));

		decls = tu.getDeclarationsInAST(RED);
		assertEquals(decls.length, 1);
		assertSame(decls[0], col.getName(6));
	}

	// class D {};
	// D d1;
	// const D d2;
	// void foo() {
	//    typeid(d1) == typeid(d2);
	//    typeid(D) == typeid(d2);
	// }
	public void testBug86274() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertEquals(col.size(), 10);

		IVariable d1 = (IVariable) col.getName(6).resolveBinding();
		IVariable d2 = (IVariable) col.getName(7).resolveBinding();
		ICPPClassType D = (ICPPClassType) col.getName(8).resolveBinding();

		assertInstances(col, D, 4);
		assertInstances(col, d1, 2);
		assertInstances(col, d2, 3);
	}

	// void point (int = 3, int = 4);
	// void foo() {
	//    point(1, 2);
	//    point(1);
	//    point();
	// }
	public void testBug86546() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction point = (IFunction) col.getName(0).resolveBinding();

		assertInstances(col, point, 4);
	}

	// namespace Outer{
	//    int i;
	//    namespace Inner {
	//       void f() {  i++;  }
	//       int i;
	//       void g() {  i++;  }
	//    }
	// }
	public void testBug86358a() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IVariable i = (IVariable) col.getName(4).resolveBinding();
		IVariable i2 = (IVariable) col.getName(7).resolveBinding();

		assertInstances(col, i, 2);
		assertInstances(col, i2, 2);
	}

	// namespace Q {
	//    namespace V {
	//       void f();
	//    }
	//    void V::f() {}
	//    namespace V {
	//    }
	// }
	public void testBug86358b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f1 = (IFunction) col.getName(2).resolveBinding();
		ICPPFunction f2 = (ICPPFunction) col.getName(5).resolveBinding();
		assertSame(f1, f2);

		IASTName[] decls = tu.getDeclarationsInAST(f2);
		assertEquals(decls.length, 2);
		assertSame(decls[0], col.getName(2));
		assertSame(decls[1], col.getName(5));

		String[] s = f2.getQualifiedName();
		assertEquals(s[0], "Q");
		assertEquals(s[1], "V");
		assertEquals(s[2], "f");
		assertTrue(f2.isGloballyQualified());
	}

	// struct B {
	//    void f (char);
	//    void g (char);
	// };
	// struct D : B {
	//    using B::f;
	//    void f(int) { f('c'); }
	//    void g(int) { g('c'); }
	// };
	public void test86371() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunction f_ref = (ICPPFunction) col.getName(12).resolveBinding();
		ICPPFunction g_ref = (ICPPFunction) col.getName(15).resolveBinding();

		ICPPFunction f = (ICPPFunction) col.getName(1).resolveBinding();
		assertSame(f_ref, f);

		IFunction g = (IFunction) col.getName(13).resolveBinding();
		assertSame(g, g_ref);

		assertInstances(col, f_ref, 2);
		assertInstances(col, g_ref, 2);

		String[] s = f_ref.getQualifiedName();
		assertEquals(s[0], "B");
		assertEquals(s[1], "f");
		assertTrue(f_ref.isGloballyQualified());

		s = g_ref.getQualifiedName();
		assertEquals(s[0], "D");
		assertEquals(s[1], "g");
		assertTrue(f.isGloballyQualified());
	}

	// namespace Company_with_veryblahblah {}
	// namespace CWVLN = Company_with_veryblahblah;
	// namespace CWVLN = Company_with_veryblahblah;
	// namespace CWVLN = CWVLN;
	public void testBug86369() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPNamespace ns = (ICPPNamespace) col.getName(0).resolveBinding();
		ICPPNamespace alias = (ICPPNamespace) col.getName(1).resolveBinding();

		String[] s = ns.getQualifiedName();
		assertEquals(s[0], "Company_with_veryblahblah");
		s = alias.getQualifiedName();
		assertEquals(s[0], "CWVLN");

		assertTrue(alias instanceof ICPPNamespaceAlias);
		assertSame(((ICPPNamespaceAlias) alias).getBinding(), ns);

		IASTName[] refs = tu.getReferences(ns);
		assertEquals(refs.length, 2);
		assertSame(refs[0], col.getName(2));
		assertSame(refs[1], col.getName(4));

		IASTName[] decls = tu.getDeclarationsInAST(ns);
		assertEquals(decls.length, 1);
		assertSame(decls[0], col.getName(0));

		refs = tu.getReferences(alias);
		assertEquals(refs.length, 1);
		assertSame(refs[0], col.getName(6));

		decls = tu.getDeclarationsInAST(alias);
		assertEquals(decls.length, 3);
		assertSame(decls[0], col.getName(1));
		assertSame(decls[1], col.getName(3));
		assertSame(decls[2], col.getName(5));
	}

	// namespace A {
	//    void f(char);
	//    void f(int);
	// }
	// using A::f;
	public void testBug86470a() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPUsingDeclaration u = (ICPPUsingDeclaration) col.getName(7).resolveBinding();

		IASTName[] decls = tu.getDeclarationsInAST(u);
		assertEquals(3, decls.length); // 2 function-decls + using-decl
		assertSame(decls[0], col.getName(1));
		assertSame(decls[1], col.getName(3));

		IBinding[] delegates = u.getDelegates();
		assertEquals(delegates.length, 2);

		decls = tu.getDeclarationsInAST(delegates[0]);
		assertEquals(2, decls.length); // function-decl + using-decl
		assertSame(decls[0], col.getName(1));
		assertSame(decls[1], col.getName(7));

		decls = tu.getDeclarationsInAST(delegates[0]);
		assertEquals(2, decls.length); // function-decl + using-decl
		assertSame(decls[0], col.getName(1));
	}

	// namespace A {
	//    void f(int);
	//    void f(double);
	// }
	// namespace B {
	//    void f(int);
	//    void f(double);
	//    void f(char);
	// }
	// void g() {
	//    using A::f;
	//    using B::f;
	//    f('c');
	// }
	public void testBug86470b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f_decl = (IFunction) col.getName(10).resolveBinding();
		IFunction f_ref = (IFunction) col.getName(19).resolveBinding();
		assertSame(f_decl, f_ref);
	}

	// namespace A {
	//    struct g {};
	//    void g (char);
	// }
	// void f() {
	//    using A::g;
	//    g('a');
	//    struct g gg;
	// }
	public void testBug86470c() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IBinding ref1 = col.getName(8).resolveBinding();
		IBinding ref2 = col.getName(9).resolveBinding();

		ICPPClassType g_struct = (ICPPClassType) col.getName(1).resolveBinding();
		IFunction g_func = (IFunction) col.getName(2).resolveBinding();

		assertSame(g_struct, ref2);
		assertSame(g_func, ref1);

		ICPPUsingDeclaration comp = (ICPPUsingDeclaration) col.getName(7).resolveBinding();
		IASTName[] decls = tu.getDeclarationsInAST(comp);
		assertEquals(3, decls.length); // struct, func and using-decl
		assertSame(decls[0], col.getName(1));
		assertSame(decls[1], col.getName(2));
		assertSame(decls[2], col.getName(7));
	}

	// namespace A {
	//    int x;
	// }
	// namespace B {
	//    struct x {};
	// }
	// void f() {
	//    using A::x;
	//    using B::x;
	//    x = 1;
	//    struct x xx;
	// }
	public void testBug86470d() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPBinding ref1 = (ICPPBinding) col.getName(11).resolveBinding();
		ICPPBinding ref2 = (ICPPBinding) col.getName(12).resolveBinding();

		ICPPClassType x_struct = (ICPPClassType) col.getName(3).resolveBinding();
		IVariable x_var = (IVariable) col.getName(1).resolveBinding();

		assertSame(x_struct, ref2);
		assertSame(x_var, ref1);

		IASTName[] refs = tu.getReferences(x_struct);
		assertEquals(2, refs.length); // 1 ref + using-decl
		assertSame(refs[0], col.getName(10));
		assertSame(refs[1], col.getName(12));

		String[] s = ref2.getQualifiedName();
		assertEquals(s[0], "B");
		assertEquals(s[1], "x");
		assertTrue(ref2.isGloballyQualified());

		s = x_struct.getQualifiedName();
		assertEquals(s[0], "B");
		assertEquals(s[1], "x");
		assertTrue(x_struct.isGloballyQualified());
	}

	// namespace A {
	//    void f(int);
	//    void f(double);
	// }
	// void g() {
	//    void f(char);
	//    using A::f;
	//    f(3.5);
	// }
	public void testBug86470e() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunction f = (ICPPFunction) col.getName(3).resolveBinding();

		ICPPFunction f_ref = (ICPPFunction) col.getName(11).resolveBinding();
		assertSame(f_ref, f);

		String[] s = f_ref.getQualifiedName();
		assertEquals(s[0], "A");
		assertEquals(s[1], "f");
		assertTrue(f_ref.isGloballyQualified());

		s = f.getQualifiedName();
		assertEquals(s[0], "A");
		assertEquals(s[1], "f");
		assertTrue(f.isGloballyQualified());
	}

	// class B;
	// class A {
	//    int i;
	//    friend void f(B *);
	// };
	// class B : public A {};
	// void f(B* p) {
	//    p->i = 1;
	// }
	public void testBug86678() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType B = (ICPPClassType) col.getName(6).resolveBinding();
		ICPPField i = (ICPPField) col.getName(12).resolveBinding();
		IParameter p = (IParameter) col.getName(10).resolveBinding();

		assertInstances(col, B, 4);
		assertInstances(col, i, 2);
		assertInstances(col, p, 3);
	}

	// int printf(const char *, ...);
	// void foo(){
	//    int a, b;
	//    printf("hello");
	//    printf("a=%d b=%d", a, b);
	// }
	public void testBug86543() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction printf = (IFunction) col.getName(6).resolveBinding();
		assertInstances(col, printf, 3);
	}

	// int max(int a, int b, int c) {
	//    int m = (a > b) ? a : b;
	//    return (m > c) ? m : c;
	// }
	public void testBug86554() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IVariable m = (IVariable) col.getName(11).resolveBinding();
		IParameter a = (IParameter) col.getName(1).resolveBinding();
		IParameter b = (IParameter) col.getName(2).resolveBinding();
		IParameter c = (IParameter) col.getName(3).resolveBinding();

		String[] s = ((ICPPBinding) a).getQualifiedName();
		assertEquals(s[0], "a");
		assertFalse(((ICPPBinding) a).isGloballyQualified());

		assertInstances(col, m, 3);
		assertInstances(col, a, 3);
		assertInstances(col, b, 3);
		assertInstances(col, c, 3);
	}

	// int g();
	// struct X { static int g(); };
	// struct Y : X { static int i ; };
	// int Y::i = g();
	public void testBug86621() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunction g1 = (ICPPFunction) col.getName(0).resolveBinding();
		ICPPMethod g2 = (ICPPMethod) col.getName(9).resolveBinding();

		String[] s = g1.getQualifiedName();
		assertEquals(s[0], "g");
		assertTrue(g1.isGloballyQualified());

		s = g2.getQualifiedName();
		assertEquals(s[0], "X");
		assertEquals(s[1], "g");
		assertTrue(g2.isGloballyQualified());

		assertInstances(col, g1, 1);
		assertInstances(col, g2, 2);
	}

	// class V { int f(); int x; };
	// class W { int g(); int y; };
	// class B : public virtual V, public W {
	//    int f();  int x;
	//    int g();  int y;
	// };
	// class C : public virtual V, public W {};
	// class D : public B, public C {
	//    void foo();
	// };
	// void D::foo(){
	//    x++;
	//    f();
	//    y++;
	//    g();
	// }
	public void testBug86649() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPField x = (ICPPField) col.getName(23).resolveBinding();
		ICPPMethod f = (ICPPMethod) col.getName(24).resolveBinding();

		String[] s = f.getQualifiedName();
		assertEquals(s[0], "B");
		assertEquals(s[1], "f");
		assertTrue(f.isGloballyQualified());

		s = x.getQualifiedName();
		assertEquals(s[0], "B");
		assertEquals(s[1], "x");
		assertTrue(x.isGloballyQualified());

		IProblemBinding y = (IProblemBinding) col.getName(25).resolveBinding();
		IProblemBinding g = (IProblemBinding) col.getName(26).resolveBinding();

		assertEquals(y.getID(), IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP);
		assertEquals(g.getID(), IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP);

		assertInstances(col, x, 2);
		assertInstances(col, f, 2);
	}

	// struct C {
	//    int c;
	//    C() : c(0) { }
	// };
	public void testBug86827() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPVariable c = (ICPPVariable) col.getName(1).resolveBinding();

		String[] s = c.getQualifiedName();
		assertEquals(s.length, 2);
		assertEquals(s[0], "C");
		assertEquals(s[1], "c");
		assertTrue(c.isGloballyQualified());

		IASTName[] refs = tu.getReferences(c);
		assertEquals(refs.length, 1);
		assertSame(refs[0], col.getName(3));
	}

	// void f(int par) {
	//    int v1;
	//    {
	//       int v2;
	//    }
	// }
	public void testFind_a() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPVariable v1 = (ICPPVariable) col.getName(2).resolveBinding();
		ICPPVariable v2 = (ICPPVariable) col.getName(3).resolveBinding();

		String[] s = v1.getQualifiedName();
		assertEquals("v1", s[0]);
		assertFalse(v1.isGloballyQualified());

		s = v2.getQualifiedName();
		assertEquals("v2", s[0]);
		assertFalse(v2.isGloballyQualified());

		ICPPBlockScope scope = (ICPPBlockScope) v2.getScope();
		IBinding[] bs = scope.find("v1", tu);
		assertEquals(1, bs.length);
		assertSame(bs[0], v1);
	}

	// class A { int a; };
	// class B : public A {
	//    void f();
	// };
	// void B::f() {
	// }
	public void testFind_b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPField a = (ICPPField) col.getName(1).resolveBinding();
		ICPPMethod f = (ICPPMethod) col.getName(7).resolveBinding();

		IScope scope = f.getFunctionScope();
		IBinding[] bs = scope.find("a", tu);
		assertEquals(1, bs.length);
		assertSame(bs[0], a);

		bs = scope.find("~B", tu);
		assertEquals(1, bs.length);
		assertTrue(bs[0] instanceof ICPPMethod);
		assertTrue(bs[0].getName().equals("~B"));

		bs = scope.find("A", tu);
		assertEquals(1, bs.length);
		assertSame(bs[0], A);
	}

	// namespace A {
	//    void f(int);
	//    void f(double);
	// }
	// void g() {
	//    void f(char);
	//    using A::f;
	// }
	public void testFind_c() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f1 = (IFunction) col.getName(1).resolveBinding();
		IFunction f2 = (IFunction) col.getName(3).resolveBinding();
		IFunction f3 = (IFunction) col.getName(6).resolveBinding();

		IASTFunctionDefinition def = (IASTFunctionDefinition) col.getName(5).getParent().getParent();
		IScope scope = ((IASTCompoundStatement) def.getBody()).getScope();
		IBinding[] bs = scope.find("f", tu);
		assertEquals(3, bs.length);
		assertSame(bs[0], f3);
		assertSame(bs[1], f1);
		assertSame(bs[2], f2);

		String[] s = ((ICPPBinding) bs[1]).getQualifiedName();
		assertEquals(2, s.length);
		assertEquals("A", s[0]);
		assertEquals("f", s[1]);
		assertTrue(((ICPPBinding) bs[1]).isGloballyQualified());
	}

	// namespace A {
	//    struct f;
	//    void f();
	// }
	// namespace B {
	//    void f(int);
	// }
	// namespace C {
	//    using namespace B;
	// }
	// void g(){
	//    using namespace A;
	//    using namespace C;
	// }
	public void testFind_d() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType f = (ICPPClassType) col.getName(1).resolveBinding();
		IFunction f1 = (IFunction) col.getName(2).resolveBinding();
		IFunction f2 = (IFunction) col.getName(4).resolveBinding();

		IASTFunctionDefinition def = (IASTFunctionDefinition) col.getName(8).getParent().getParent();
		IScope scope = ((IASTCompoundStatement) def.getBody()).getScope();
		IBinding[] bs = scope.find("f", tu);
		assertEquals(3, bs.length);
		assertSame(bs[0], f);
		assertSame(bs[1], f1);
		assertSame(bs[2], f2);
	}

	// class A {
	// public:
	//    A();
	//    void f();
	// };
	// class B : public A {
	// public:
	//    B();
	//    void bf();
	// };
	public void testFind_185408() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f1 = (IFunction) col.getName(6).resolveBinding();
		IScope classScope = f1.getScope();
		assertTrue(classScope instanceof ICPPClassScope);
		IBinding[] bindings = classScope.find("bf", tu);
		ICPPMethod method = extractSingleMethod(bindings);
		assertEquals("B", method.getQualifiedName()[0]);

		bindings = classScope.find("f", tu);
		method = extractSingleMethod(bindings);
		assertEquals("A", method.getQualifiedName()[0]);

		bindings = classScope.find("B", tu);
		ICPPClassType classType = extractSingleClass(bindings);
		assertEquals("B", classType.getQualifiedName()[0]);

		bindings = classScope.find("A", tu);
		classType = extractSingleClass(bindings);
		assertEquals("A", classType.getQualifiedName()[0]);
	}

	// class A {
	//    int a;
	//    void fa();
	// };
	// class B : public A {
	//    int b;
	//    void fb();
	// };
	public void testImplicitMethods() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPClassType B = (ICPPClassType) col.getName(3).resolveBinding();
		ICPPField a = (ICPPField) col.getName(1).resolveBinding();
		ICPPMethod fa = (ICPPMethod) col.getName(2).resolveBinding();
		ICPPField b = (ICPPField) col.getName(5).resolveBinding();
		ICPPMethod fb = (ICPPMethod) col.getName(6).resolveBinding();

		Object[] result = B.getDeclaredFields();
		assertEquals(1, result.length);
		assertSame(result[0], b);

		result = B.getFields();
		assertEquals(2, result.length);
		assertSame(result[0], b);
		assertSame(result[1], a);

		result = B.getDeclaredMethods();
		assertEquals(1, result.length);
		assertSame(result[0], fb);

		result = B.getAllDeclaredMethods();
		assertEquals(2, result.length);
		assertSame(result[0], fb);
		assertSame(result[1], fa);

		ICPPMethod[] B_implicit = ((ICPPClassScope) B.getCompositeScope()).getImplicitMethods();
		assertEquals(4, B_implicit.length);
		assertTrue(B_implicit[0].getName().equals("B"));
		assertTrue(B_implicit[1].getName().equals("B"));
		assertTrue(B_implicit[2].getName().equals("operator ="));
		assertTrue(B_implicit[3].getName().equals("~B"));

		ICPPMethod[] A_implicit = ((ICPPClassScope) A.getCompositeScope()).getImplicitMethods();
		assertEquals(4, A_implicit.length);
		assertTrue(A_implicit[0].getName().equals("A"));
		assertTrue(A_implicit[1].getName().equals("A"));
		assertTrue(A_implicit[2].getName().equals("operator ="));
		assertTrue(A_implicit[3].getName().equals("~A"));

		result = B.getMethods();
		assertEquals(10, result.length);
		assertSame(result[0], fb);
		assertSame(result[1], B_implicit[0]);
		assertSame(result[2], B_implicit[1]);
		assertSame(result[3], B_implicit[2]);
		assertSame(result[4], B_implicit[3]);
		assertSame(result[5], fa);
		assertSame(result[6], A_implicit[0]);
		assertSame(result[7], A_implicit[1]);
		assertSame(result[8], A_implicit[2]);
		assertSame(result[9], A_implicit[3]);
	}

	public void testBug87424() throws Exception {
		IASTTranslationUnit tu = parse("int * __restrict x;", CPP, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IVariable x = (IVariable) col.getName(0).resolveBinding();
		IType t = x.getType();
		assertTrue(t instanceof IPointerType);
		assertTrue(((IPointerType) t).isRestrict());

		tu = parse("class A {}; int A::* __restrict x;", CPP, true);
		col = new NameCollector();
		tu.accept(col);

		x = (IVariable) col.getName(3).resolveBinding();
		t = x.getType();
		assertTrue(t instanceof ICPPPointerToMemberType);
		assertTrue(((ICPPPointerToMemberType) t).isRestrict());
	}

	public void testBug87705() throws Exception {
		IASTTranslationUnit tu = parse("class A { friend class B::C; };", CPP, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IProblemBinding B = (IProblemBinding) col.getName(2).resolveBinding();
		assertEquals(B.getID(), IProblemBinding.SEMANTIC_NAME_NOT_FOUND);
		IProblemBinding C = (IProblemBinding) col.getName(3).resolveBinding();
		assertEquals(C.getID(), IProblemBinding.SEMANTIC_NAME_NOT_FOUND);
	}

	public void testBug88459() throws Exception {
		IASTTranslationUnit tu = parse("int f(); ", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f = (IFunction) col.getName(0).resolveBinding();
		assertFalse(f.isStatic());
	}

	public void testBug88501a() throws Exception {
		IASTTranslationUnit tu = parse("void f(); void f(int); struct f;", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertTrue(col.getName(0).resolveBinding() instanceof IFunction);
		assertTrue(col.getName(1).resolveBinding() instanceof IFunction);
		assertTrue(col.getName(3).resolveBinding() instanceof ICPPClassType);
	}

	// public void testBug8342a() throws Exception {
	// IASTTranslationUnit tu = parse("int a; int a;", CPP);
	//
	// NameCollector col = new NameCollector();
	// tu.accept(col);
	//
	// assertTrue(col.getName(0).resolveBinding() instanceof IVariable);
	// IProblemBinding p = (IProblemBinding) col.getName(1).resolveBinding();
	// assertEquals(p.getID(), IProblemBinding.SEMANTIC_INVALID_REDEFINITION);
	// }
	public void testBug8342b() throws Exception {
		IASTTranslationUnit tu = parse("extern int a; extern char a;", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertTrue(col.getName(0).resolveBinding() instanceof IVariable);
		IProblemBinding p = (IProblemBinding) col.getName(1).resolveBinding();
		assertEquals(p.getID(), IProblemBinding.SEMANTIC_INVALID_REDECLARATION);
	}

	// namespace A { int i; }
	// namespace B = A;
	// void f() {
	//    B::i;
	// }
	public void testNamespaceAlias_b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPNamespace A = (ICPPNamespace) col.getName(0).resolveBinding();
		ICPPNamespaceAlias alias = (ICPPNamespaceAlias) col.getName(6).resolveBinding();
		ICPPVariable i = (ICPPVariable) col.getName(7).resolveBinding();

		assertInstances(col, A, 2);
		assertInstances(col, alias, 2);
		assertInstances(col, i, 3);

		String[] s = i.getQualifiedName();
		assertEquals(s[0], "A");
		assertEquals(s[1], "i");
		assertTrue(i.isGloballyQualified());

		s = alias.getQualifiedName();
		assertEquals(s[0], "B");
		assertTrue(alias.isGloballyQualified());
	}

	//	namespace ns { struct A {}; }
	//
	//	#define MACRO []() { namespace waldo = ::ns; waldo::A x; return x; }()
	//
	//	void test() {
	//	  MACRO;
	//	}
	public void testNamespaceAliasInMacroExpansion_506668() throws Exception {
		parseAndCheckBindings();
	}

	// class A{};
	// class B : public A {
	//    B () : A() {}
	// };
	public void testBug89539() throws Exception {
		String content = getAboveComment();
		IASTTranslationUnit tu = parse(content, CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType A1 = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPClassType A2 = (ICPPClassType) col.getName(2).resolveBinding();
		assertSame(A1, A2);

		ICPPConstructor A3 = (ICPPConstructor) col.getName(4).resolveBinding();
		assertSame(A3.getScope(), A1.getCompositeScope());

		tu = parse(content, CPP);
		col = new NameCollector();
		tu.accept(col);

		assertTrue(col.getName(4).resolveBinding() instanceof ICPPConstructor);
	}

	// class B * b;
	// class A {
	//    A * a;
	// };
	// class A;
	public void testBug89851() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertTrue(col.getName(0).resolveBinding() instanceof ICPPClassType);
		assertTrue(col.getName(1).resolveBinding() instanceof ICPPVariable);
		assertTrue(col.getName(2).resolveBinding() instanceof ICPPClassType);
		assertTrue(col.getName(3).resolveBinding() instanceof ICPPClassType);
	}

	public void testBug89828() throws Exception {
		IASTTranslationUnit tu = parse("class B * b; void f();  void f(int);", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertTrue(col.getName(0).resolveBinding() instanceof ICPPClassType);
		assertTrue(col.getName(1).resolveBinding() instanceof ICPPVariable);
		IFunction f1 = (IFunction) col.getName(2).resolveBinding();
		IFunction f2 = (IFunction) col.getName(3).resolveBinding();

		IScope scope = tu.getScope();
		IBinding[] bs = scope.find("f", tu);
		assertEquals(bs.length, 2);
		assertSame(bs[0], f1);
		assertSame(bs[1], f2);
	}

	// class A {
	//    enum type { t1, t2 };
	//    void f(type t);
	// };
	// class B : public A {
	//    void g() {
	//       f(A::t1);
	//    }
	// };
	public void testBug90039() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f = (IFunction) col.getName(10).resolveBinding();
		IEnumerator t1 = (IEnumerator) col.getName(13).resolveBinding();

		assertInstances(col, f, 2);
		assertInstances(col, t1, 3);
	}

	// void f(void) {
	//    enum { one };
	// }
	public void testBug90039b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertTrue(col.getName(0).resolveBinding() instanceof IFunction);
		assertTrue(col.getName(1).resolveBinding() instanceof IParameter);
		IEnumeration e = (IEnumeration) col.getName(2).resolveBinding();
		IEnumerator one = (IEnumerator) col.getName(3).resolveBinding();
		assertSame(one.getType(), e);
	}

	// class Foo {
	// public:
	// operator int();
	// char& operator[](unsigned int);
	// };
	public void testOperatorConversionNames() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IASTName name1 = col.getName(1);
		IASTName name2 = col.getName(2);

		assertNotNull(name1);
		assertNotNull(name2);

		assertTrue(name1 instanceof ICPPASTConversionName);
		assertTrue(name2 instanceof ICPPASTOperatorName);

		IASTTypeId typeId = ((ICPPASTConversionName) name1).getTypeId();
		assertNotNull(typeId);
		assertEquals(((IASTSimpleDeclSpecifier) typeId.getDeclSpecifier()).getType(), IASTSimpleDeclSpecifier.t_int);

	}

	// class X { operator int(); };
	// X::operator int() { }
	// template <class A,B> class X<A,C> { operator int(); };
	// template <class A,B> X<A,C>::operator int() { }
	public void testBug36769B() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		// 1,4,12,21 - conversion
		// 2, 16 .isConversion

		final IASTName int1 = col.getName(1);
		assertNotNull(int1);
		assertTrue(int1 instanceof ICPPASTConversionName);
		assertNotNull(((ICPPASTConversionName) int1).getTypeId());

		IASTFunctionDefinition fdef = getDeclaration(tu, 1);
		final IASTName x_int = fdef.getDeclarator().getName();
		assertNotNull(x_int);
		assertTrue(x_int instanceof ICPPASTQualifiedName);
		assertTrue(((ICPPASTQualifiedName) x_int).isConversionOrOperator());

		final IASTName int2 = ((ICPPASTQualifiedName) x_int).getLastName();
		assertNotNull(int2);
		assertTrue(int2 instanceof ICPPASTConversionName);
		assertNotNull(((ICPPASTConversionName) int2).getTypeId());

		final IASTName int3 = col.getName(12);
		assertNotNull(int3);
		assertTrue(int3 instanceof ICPPASTConversionName);
		assertNotNull(((ICPPASTConversionName) int3).getTypeId());

		ICPPASTTemplateDeclaration tdef = getDeclaration(tu, 3);
		fdef = (IASTFunctionDefinition) tdef.getDeclaration();
		final IASTName x_ac_int = fdef.getDeclarator().getName();
		assertNotNull(x_ac_int);
		assertTrue(x_ac_int instanceof ICPPASTQualifiedName);
		assertTrue(((ICPPASTQualifiedName) x_ac_int).isConversionOrOperator());

		final IASTName int4 = ((ICPPASTQualifiedName) x_ac_int).getLastName();
		assertNotNull(int4);
		assertTrue(int4 instanceof ICPPASTConversionName);
		assertNotNull(((ICPPASTConversionName) int4).getTypeId());
	}

	public void testBug88662() throws Exception {
		IASTTranslationUnit tu = parse("int foo() {  return int();}", CPP);
		IASTReturnStatement returnStatement = (IASTReturnStatement) ((IASTCompoundStatement) ((IASTFunctionDefinition) tu
				.getDeclarations()[0]).getBody()).getStatements()[0];
		ICPPASTSimpleTypeConstructorExpression expression = (ICPPASTSimpleTypeConstructorExpression) returnStatement
				.getReturnValue();
		assertEquals(expression.getInitialValue(), null);
		assertEquals(expression.getSimpleType(), ICPPASTSimpleTypeConstructorExpression.t_int);
	}

	public void testBug90498a() throws Exception {
		IASTTranslationUnit tu = parse("typedef int INT;\ntypedef INT (FOO) (INT);", CPP);

		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[1];
		IASTDeclSpecifier declSpec = decl.getDeclSpecifier();
		assertTrue(declSpec instanceof ICPPASTNamedTypeSpecifier);
		assertEquals(((ICPPASTNamedTypeSpecifier) declSpec).getName().toString(), "INT");

		IASTDeclarator dtor = decl.getDeclarators()[0];
		assertTrue(dtor instanceof IASTFunctionDeclarator);
		assertNotNull(dtor.getNestedDeclarator());
		IASTDeclarator nested = dtor.getNestedDeclarator();
		assertEquals(nested.getName().toString(), "FOO");
	}

	public void testBug90498b() throws Exception {
		IASTTranslationUnit tu = parse("int (* foo) (int) (0);", CPP);

		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		IASTDeclSpecifier declSpec = decl.getDeclSpecifier();
		assertTrue(declSpec instanceof IASTSimpleDeclSpecifier);

		IASTDeclarator dtor = decl.getDeclarators()[0];
		assertTrue(dtor instanceof IASTFunctionDeclarator);
		assertNotNull(dtor.getNestedDeclarator());
		IASTDeclarator nested = dtor.getNestedDeclarator();
		assertEquals(nested.getName().toString(), "foo");

		assertNotNull(dtor.getInitializer());
	}

	// class D { /* ... */ };
	// D d1;
	// const D d2;
	// void foo() {
	//     typeid(d1) == typeid(d2);
	//     typeid(D) == typeid(d2);
	// }
	public void testBug866274() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		IASTFunctionDefinition foo = (IASTFunctionDefinition) tu.getDeclarations()[3];
		IASTCompoundStatement cs = (IASTCompoundStatement) foo.getBody();
		IASTStatement[] subs = cs.getStatements();
		for (int i = 0; i < subs.length; ++i) {
			IASTBinaryExpression be = (IASTBinaryExpression) ((IASTExpressionStatement) subs[i]).getExpression();
			if (i == 1) {
				IASTTypeIdExpression expression = (IASTTypeIdExpression) be.getOperand1();
				IASTTypeId typeId = expression.getTypeId();
				IASTName name = ((IASTNamedTypeSpecifier) typeId.getDeclSpecifier()).getName();
				assertTrue(name.resolveBinding() instanceof IType);
			} else {
				IASTUnaryExpression expression = (IASTUnaryExpression) be.getOperand1();
				IASTIdExpression idExpression = (IASTIdExpression) expression.getOperand();
				assertTrue(idExpression.getName().resolveBinding() instanceof IVariable);
			}
			IASTUnaryExpression expression = (IASTUnaryExpression) be.getOperand2();
			IASTIdExpression idExpression = (IASTIdExpression) expression.getOperand();
			assertTrue(idExpression.getName().resolveBinding() instanceof IVariable);
		}
	}

	public void testTypedefFunction() throws Exception {
		IASTTranslationUnit tu = parse("typedef int foo (int);", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IBinding binding = col.getName(0).resolveBinding();
		assertTrue(binding instanceof ITypedef);
		assertTrue(((ITypedef) binding).getType() instanceof IFunctionType);
	}

	//	struct A {
	//	  A(int x);
	//	};
	//	typedef A B;
	//
	//	B a = B(1);
	public void testTypedefConstructorCall() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPConstructor ctor = bh.assertNonProblem("A(int x)", "A", ICPPConstructor.class);
		ITypedef typedef = bh.assertNonProblem("B(1)", "B", ITypedef.class);
		assertSame(ctor.getOwner(), typedef.getType());
		IASTName name = bh.findName("B(1)", "B");
		IASTImplicitName[] implicitNames = ((IASTImplicitNameOwner) name.getParent().getParent()).getImplicitNames();
		assertEquals(1, implicitNames.length);
		IBinding ctor2 = implicitNames[0].getBinding();
		assertSame(ctor, ctor2);
	}

	// void f(int);
	// void foo(){
	//    f((1, 2));
	// }
	public void testBug90616() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f1 = (IFunction) col.getName(0).resolveBinding();
		IFunction f2 = (IFunction) col.getName(3).resolveBinding();
		assertSame(f1, f2);
	}

	public void testBug90603() throws Exception {
		IASTTranslationUnit tu = parse("class X { void f(){} };", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType X = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPMethod f1 = (ICPPMethod) col.getName(1).resolveBinding();

		assertFalse(f1.isStatic());

		String[] qns = f1.getQualifiedName();
		assertEquals(qns.length, 2);
		assertEquals(qns[0], "X");
		assertEquals(qns[1], "f");
		assertTrue(f1.isGloballyQualified());
		assertEquals(f1.getVisibility(), ICPPMember.v_private);

		assertSame(f1.getScope(), X.getCompositeScope());
	}

	// class X {   };
	// X x;
	// class X {   };
	public void testBug90662() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType X = (ICPPClassType) col.getName(0).resolveBinding();
		IVariable x = (IVariable) col.getName(2).resolveBinding();
		IProblemBinding problem = (IProblemBinding) col.getName(3).resolveBinding();
		assertSame(x.getType(), X);
		assertEquals(problem.getID(), IProblemBinding.SEMANTIC_INVALID_REDEFINITION);
	}

	// struct C {
	//    void* operator new [ ] (unsigned int);
	//    void* operator new (unsigned int);
	//    void operator delete [ ] (void *);
	//    void operator delete (void *);
	//    const C& operator+=(const C&);
	//    const C& operator -= (const C&);
	//    const C& operator *=   (const C&);
	//    const C& operator /= (const C&);
	//    const C& operator %= (const C&);
	//    const C& operator^=(const C&);
	//    const C& operator&= (const C&);
	//    const C& operator |= (const C&);
	//    const C& operator >>=(const C&);
	//    const C& operator<<= (const C&);
	//    const C& operator<<(const C&);
	//    const C& operator>>(const C&);
	//    const C& operator /**/   == /**/  (const C&);
	//    const C& operator != /**/ (const C&);
	//    const C& operator <= (const C&);
	//    const C& operator /**/ >=(const C&);
	//    const C& operator =(const C&);
	//    const C& operator&& (const C&);
	//    const C& operator ||(const C&);
	//    const C& operator ++(const C&);
	//    const C& operator-- (const C&);
	//    const C& operator/**/,/**/(const C&);
	//    const C& operator->*
	// (const C&);
	//    const C& operator -> (const C&);
	//    const C& operator /**/ (/**/) /**/ (const C&);
	//    const C& operator /**/ [ /**/ ] /**/ (const C&);
	//    const C& operator + (const C&);
	//    const C& operator- (const C&);
	//    const C& operator *(const C&);
	//    const C& operator /(const C&);
	//    const C& operator % /**/(const C&);
	//    const C& operator ^(const C&);
	//    const C& operator &(const C&);
	//    const C& operator |(const C&);
	//    const C& operator   ~ (const C&);
	//    const C& operator
	//  ! /**/ (const C&);
	//    const C& operator <(const C&);
	//    const C& operator>(const C&);
	// };
	public void testOperatorNames() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(col.size(), 161);
		assertEquals(col.getName(1).toString(), "operator new[]");
		assertEquals(col.getName(3).toString(), "operator new");
		assertEquals(col.getName(5).toString(), "operator delete[]");
		assertEquals(col.getName(7).toString(), "operator delete");
		assertEquals(col.getName(10).toString(), "operator +=");
		assertEquals(col.getName(14).toString(), "operator -=");
		assertEquals(col.getName(18).toString(), "operator *=");
		assertEquals(col.getName(22).toString(), "operator /=");
		assertEquals(col.getName(26).toString(), "operator %=");
		assertEquals(col.getName(30).toString(), "operator ^=");
		assertEquals(col.getName(34).toString(), "operator &=");
		assertEquals(col.getName(38).toString(), "operator |=");
		assertEquals(col.getName(42).toString(), "operator >>=");
		assertEquals(col.getName(46).toString(), "operator <<=");
		assertEquals(col.getName(50).toString(), "operator <<");
		assertEquals(col.getName(54).toString(), "operator >>");
		assertEquals(col.getName(58).toString(), "operator ==");
		assertEquals(col.getName(62).toString(), "operator !=");
		assertEquals(col.getName(66).toString(), "operator <=");
		assertEquals(col.getName(70).toString(), "operator >=");
		assertEquals(col.getName(74).toString(), "operator =");
		assertEquals(col.getName(78).toString(), "operator &&");
		assertEquals(col.getName(82).toString(), "operator ||");
		assertEquals(col.getName(86).toString(), "operator ++");
		assertEquals(col.getName(90).toString(), "operator --");
		assertEquals(col.getName(94).toString(), "operator ,");
		assertEquals(col.getName(98).toString(), "operator ->*");
		assertEquals(col.getName(102).toString(), "operator ->");
		assertEquals(col.getName(106).toString(), "operator ()");
		assertEquals(col.getName(110).toString(), "operator []");
		assertEquals(col.getName(114).toString(), "operator +");
		assertEquals(col.getName(118).toString(), "operator -");
		assertEquals(col.getName(122).toString(), "operator *");
		assertEquals(col.getName(126).toString(), "operator /");
		assertEquals(col.getName(130).toString(), "operator %");
		assertEquals(col.getName(134).toString(), "operator ^");
		assertEquals(col.getName(138).toString(), "operator &");
		assertEquals(col.getName(142).toString(), "operator |");
		assertEquals(col.getName(146).toString(), "operator ~");
		assertEquals(col.getName(150).toString(), "operator !");
		assertEquals(col.getName(154).toString(), "operator <");
		assertEquals(col.getName(158).toString(), "operator >");
	}

	// typedef int I;
	// typedef int I;
	// typedef I I;
	// class A {
	//    typedef char I;
	//    typedef char I;
	//    typedef I I;
	// };
	public void testBug90623() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ITypedef I1 = (ITypedef) col.getName(0).resolveBinding();
		ITypedef I2 = (ITypedef) col.getName(1).resolveBinding();
		ITypedef I3 = (ITypedef) col.getName(2).resolveBinding();
		ITypedef I4 = (ITypedef) col.getName(3).resolveBinding();
		ITypedef I8 = (ITypedef) col.getName(5).resolveBinding();
		ITypedef I5 = (ITypedef) col.getName(8).resolveBinding();
		ITypedef I6 = (ITypedef) col.getName(7).resolveBinding();
		ITypedef I7 = (ITypedef) col.getName(6).resolveBinding();
		// ITypedef I8 = (ITypedef) col.getName(5).resolveBinding();

		assertSame(I1, I2);
		assertSame(I2, I3);
		assertSame(I3, I4);
		assertNotSame(I4, I5);
		assertSame(I5, I6);
		assertSame(I6, I7);
		assertSame(I7, I8);

		assertTrue(I1.getType() instanceof IBasicType);
		assertEquals(((IBasicType) I1.getType()).getType(), IBasicType.t_int);

		assertTrue(I8.getType() instanceof IBasicType);
		assertEquals(((IBasicType) I8.getType()).getType(), IBasicType.t_char);
	}

	// typedef int I;
	// void f11(I i);
	// void main(){ f a; }
	public void testBug90623b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IASTName f = col.getName(5);
		f.getCompletionContext().findBindings(f, true);
	}

	// class X {
	// public:
	//   X(const X &);
	// };
	// class Y {
	// public:
	//    operator X();
	// };
	// Y y;
	// X* x = new X(y);
	public void testBug90654a() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector(true);
		tu.accept(col);

		ICPPConstructor ctor1 = (ICPPConstructor) col.getName(1).resolveBinding();
		ICPPConstructor ctor = (ICPPConstructor) col.getName(12).resolveBinding();
		assertSame(ctor, ctor1);
	}

	// struct A {
	//    operator short();
	// } a;
	// int f(int);
	// int f(float);
	// int x = f(a);
	public void testBug90654b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f1 = (IFunction) col.getName(3).resolveBinding();
		IFunction f2 = (IFunction) col.getName(8).resolveBinding();
		assertSame(f1, f2);
	}

	// struct A {};
	// struct B : public A {
	//    B& operator = (const B &);
	// };
	// B& B::operator = (const B & s){
	//    this->A::operator=(s);
	//    return *this;
	// }
	public void testBug90653() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPMethod implicit = A.getMethods()[2];

		ICPPMethod op1 = (ICPPMethod) col.getName(4).resolveBinding();
		ICPPMethod op2 = (ICPPMethod) col.getName(10).resolveBinding();

		ICPPMethod op = (ICPPMethod) col.getName(15).resolveBinding();

		assertSame(op1, op2);
		assertSame(op, implicit);
	}

	// void f(char *);
	// void foo() {
	//    f("test");
	// }
	public void testBug86618() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f = (IFunction) col.getName(0).resolveBinding();
		assertInstances(col, f, 2);
	}

	// void f(int (*pf) (char));
	// int g(char);
	// void foo () {
	//    f(g) ;
	// }
	public void testBug45129() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunction f1 = (ICPPFunction) col.getName(0).resolveBinding();
		ICPPFunction g1 = (ICPPFunction) col.getName(3).resolveBinding();

		IBinding f2 = col.getName(6).resolveBinding();
		IBinding g2 = col.getName(7).resolveBinding();

		assertSame(f1, f2);
		assertSame(g1, g2);
	}

	// class ABC {
	//    class DEF { };
	//    static int GHI;
	// };
	// int ABC::GHI = 77; // ray bourque
	// int f() {
	//   int value;
	//   ABC::DEF * var;
	//   ABC::GHI * value;
	// }
	public void testAmbiguousStatements() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		IASTDeclaration[] declarations = tu.getDeclarations();
		assertEquals(3, declarations.length);
		IASTCompoundStatement cs = (IASTCompoundStatement) ((IASTFunctionDefinition) declarations[2]).getBody();
		assertTrue(cs.getStatements()[1] instanceof IASTDeclarationStatement);
		assertTrue(cs.getStatements()[2] instanceof IASTExpressionStatement);

	}

	// void f(){
	//    union { int a; char* p; };
	//    a = 1;
	// }
	public void testBug86639() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPField a = (ICPPField) col.getName(2).resolveBinding();
		ICPPField a2 = (ICPPField) col.getName(4).resolveBinding();
		assertSame(a, a2);
	}

	// void f () {
	//    int aa1, aa2;
	//    a;
	// }
	public void testBug80940() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IVariable a1 = (IVariable) col.getName(1).resolveBinding();
		IVariable a2 = (IVariable) col.getName(2).resolveBinding();

		IBinding[] bs = col.getName(3).getCompletionContext().findBindings(col.getName(3), true);
		assertEquals(bs.length, 2);
		assertSame(bs[0], a1);
		assertSame(bs[1], a2);
	}

	// struct Ex {
	//    int d();
	//    int d() const;
	// };
	// int Ex::d() {}
	// int Ex::d() const {}
	// void f() {
	//    const Ex * e;
	//    e->d();
	// }
	public void testBug77024() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunction d1 = (ICPPFunction) col.getName(1).resolveBinding();
		ICPPFunction d2 = (ICPPFunction) col.getName(2).resolveBinding();

		assertNotSame(d1, d2);

		assertFalse(d1.getType().isConst());
		assertTrue((d2.getType()).isConst());

		ICPPFunction dr1 = (ICPPFunction) col.getName(5).resolveBinding();
		ICPPFunction dr2 = (ICPPFunction) col.getName(8).resolveBinding();

		assertSame(d1, dr1);
		assertSame(d2, dr2);

		IBinding r = col.getName(13).resolveBinding();
		assertSame(d2, r);
	}

	// class Point {
	//    Point() : xCoord(0) {}
	//    int xCoord;
	// };
	public void testBug91773() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPField x = (ICPPField) col.getName(2).resolveBinding();
		ICPPField x2 = (ICPPField) col.getName(3).resolveBinding();
		assertSame(x, x2);
	}

	public void testBug90648() throws ParserException {
		IASTTranslationUnit tu = parse("int f() { int (&ra)[3] = a; }", CPP);
		IASTFunctionDefinition f = (IASTFunctionDefinition) tu.getDeclarations()[0];
		IASTCompoundStatement body = (IASTCompoundStatement) f.getBody();
		final IASTDeclarationStatement statement = (IASTDeclarationStatement) body.getStatements()[0];
		IASTSimpleDeclaration d = (IASTSimpleDeclaration) statement.getDeclaration();
		IASTSimpleDeclSpecifier declSpec = (IASTSimpleDeclSpecifier) d.getDeclSpecifier();
		assertEquals(IASTSimpleDeclSpecifier.t_int, declSpec.getType());
		final IASTDeclarator[] declarators = d.getDeclarators();
		assertEquals(declarators.length, 1);
		assertTrue(declarators[0] instanceof IASTArrayDeclarator);
	}

	public void testBug92980() throws Exception {
		String code = "struct A { A(); A(const A&) throw(1); ~A() throw(X); };";
		parse(code, CPP, true, false);
	}

	// class Dummy { int v(); int d; };
	// void Dummy::v(int){ d++; }
	public void testBug92882() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertTrue(col.getName(5).resolveBinding() instanceof IProblemBinding);
		ICPPField d1 = (ICPPField) col.getName(2).resolveBinding();
		ICPPField d2 = (ICPPField) col.getName(7).resolveBinding();
		assertSame(d1, d2);
	}

	// void f(int, int);
	// void f(int, int = 3);
	// void f(int = 2, int);
	// void g() {
	//    f(3);
	//    f();
	// }
	public void testBug86547() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f1 = (IFunction) col.getName(0).resolveBinding();
		assertInstances(col, f1, 5);
	}

	public void testBug90647() throws Exception {
		parse("char msg[] = \"Syntax error on line %s\\n\";", CPP);
	}

	// int main(int argc, char **argv)
	// {
	// int sum=0;
	// int i;
	// for (i = 0; i < 10; ++i)
	// for (int j = 0; j < 3; ++j)
	// sum += j;
	// for (i = 0; i < 10; ++i)
	// for (int j = 0; j < 3; ++j) // line X
	// sum += j;  // line Y
	// int k;
	// k = sum;
	// }
	public void testBug82766() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertNoProblemBindings(col);
	}

	// int main(int argc, char *argv[])
	// {
	//     unsigned long l = 0;
	//     char *c;
	//     l |= ((unsigned long)(*((c)++)))<<24;
	// }
	public void testBug77385() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertNoProblemBindings(col);
	}

	public void testBug83997() throws Exception {
		IASTTranslationUnit tu = parse("namespace { int x; }", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertNoProblemBindings(col);
	}

	public void testBug85786() throws Exception {
		IASTTranslationUnit tu = parse("void f(int); void foo () { void * p = &f; ((void (*) (int)) p) (1); }",
				ParserLanguage.C);
		NameCollector nameResolver = new NameCollector();
		tu.accept(nameResolver);
		assertNoProblemBindings(nameResolver);
	}

	// class C {
	//    static const int n = 1;
	//    static int arr[ n ];
	// };
	// int C::arr[n];
	public void testBug90610() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPField n = (ICPPField) col.getName(1).resolveBinding();
		assertTrue(n.isStatic());

		assertInstances(col, n, 3);
	}

	// int a;
	// extern int b;
	// extern int c = 1;
	// int f();
	// int f(int p){}
	// struct S;
	// struct S { int d; };
	// struct X {
	//    static int y;
	// };
	// namespace N {}
	// int X::y = 1;
	// int (*g(int))(int);
	// int (*pf)(int);
	public void testDeclDefn() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertTrue(col.getName(0).isDefinition()); // a
		assertFalse(col.getName(1).isDefinition()); // b
		assertTrue(col.getName(2).isDefinition()); // c
		assertFalse(col.getName(3).isDefinition()); // f ()
		assertTrue(col.getName(4).isDefinition()); // f () {}
		assertTrue(col.getName(5).isDefinition()); // p
		assertFalse(col.getName(6).isDefinition()); // struct S;
		assertTrue(col.getName(7).isDefinition()); // struct S {}
		assertTrue(col.getName(8).isDefinition()); // d
		assertTrue(col.getName(9).isDefinition()); // X
		assertFalse(col.getName(10).isDefinition()); // y
		assertTrue(col.getName(11).isDefinition()); // N
		assertTrue(col.getName(12).isDefinition()); // X::y
		assertFalse(col.getName(15).isDefinition()); // g
		assertTrue(col.getName(18).isDefinition()); // pf
	}

	// int f(double);
	// int f(int);
	// int (&rfi)(int) = f;
	// int (&rfd)(double) = f;
	public void testBug95200() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunction f1 = (ICPPFunction) col.getName(0).resolveBinding();
		ICPPFunction f2 = (ICPPFunction) col.getName(2).resolveBinding();

		assertSame(col.getName(6).resolveBinding(), f2);
		assertSame(col.getName(9).resolveBinding(), f1);
	}

	public void testBug95425() throws Exception {
		IASTTranslationUnit tu = parse("class A { A(); };", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPConstructor ctor = (ICPPConstructor) col.getName(1).resolveBinding();

		ICPPConstructor[] ctors = A.getConstructors();
		assertEquals(2, ctors.length); // one user declared constructor, one copy constructor
		assertSame(ctor, ctors[1]);

		tu = parse("class A { A(void); };", CPP);
		col = new NameCollector();
		tu.accept(col);

		A = (ICPPClassType) col.getName(0).resolveBinding();
		ctor = (ICPPConstructor) col.getName(1).resolveBinding();

		ctors = A.getConstructors();
		assertEquals(2, ctors.length); // one user declared constructor, one copy constructor
		assertSame(ctor, ctors[1]);
	}

	// void f(char *);
	// void g(){
	//    char x[100];
	//    f(x);
	// }
	public void testBug95461() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunction f1 = (ICPPFunction) col.getName(0).resolveBinding();
		assertSame(f1, col.getName(4).resolveBinding());
	}

	// class A { };
	// int f() {
	//   A * b = 0;
	//   A & c = 0;
	// }
	public void testAmbiguity() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		IASTSimpleDeclaration A = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		IASTFunctionDefinition f = (IASTFunctionDefinition) tu.getDeclarations()[1];
		IASTCompoundStatement body = (IASTCompoundStatement) f.getBody();
		for (int i = 0; i < 2; ++i) {
			IASTDeclarationStatement ds = (IASTDeclarationStatement) body.getStatements()[i];
			String s1 = ((IASTNamedTypeSpecifier) ((IASTSimpleDeclaration) ds.getDeclaration()).getDeclSpecifier())
					.getName().toString();
			String s2 = ((IASTCompositeTypeSpecifier) A.getDeclSpecifier()).getName().toString();
			assertEquals(s1, s2);
		}
	}

	// struct A {
	//  int a;
	// };
	// struct B: virtual A { };
	// struct C: B { };
	// struct D: B { };
	// struct E: public C, public D { };
	// struct F: public A { };
	// void f() {
	// E e;
	// e.B::a = 0;
	// F f;
	// f.A::a = 1;
	// }
	public void testBug84696() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertEquals(col.size(), 26);

		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPClassType B = (ICPPClassType) col.getName(2).resolveBinding();

		assertNotNull(A);
		assertNotNull(B);

		assertInstances(col, A, 4);
		assertInstances(col, B, 4);
	}

	//	class X {
	//	public:
	//	  void f(int);
	//	  int a;
	//	};
	//	int X:: * pmi = &X::a;
	public void testBasicPointerToMember() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		assertEquals(tu.getDeclarations().length, 2);
		IASTSimpleDeclaration p2m = (IASTSimpleDeclaration) tu.getDeclarations()[1];
		IASTDeclarator d = p2m.getDeclarators()[0];
		ICPPASTPointerToMember po = (ICPPASTPointerToMember) d.getPointerOperators()[0];
		assertEquals("X::", po.getName().toString());
	}

	//	struct cat {
	//	    void meow();
	//	};
	//	struct waldo {
	//	    cat operator->*(bool);
	//	};
	//	void foo() {
	//	    (waldo()->*true).meow();  // Method 'meow' could not be resolved
	//	}
	public void testOverloadedPointerToMemberOperator_488611() throws Exception {
		parseAndCheckBindings();
	}

	//	struct B {};
	//	struct D : B {};
	//	void foo(D* dp) {
	//	  B* bp = dynamic_cast<B*>(dp);
	//	}
	public void testBug84466() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		ICPPASTCastExpression dynamic_cast = (ICPPASTCastExpression) ((IASTEqualsInitializer) ((IASTSimpleDeclaration) ((IASTDeclarationStatement) ((IASTCompoundStatement) ((IASTFunctionDefinition) tu
				.getDeclarations()[2]).getBody()).getStatements()[0]).getDeclaration()).getDeclarators()[0]
						.getInitializer()).getInitializerClause();

		assertEquals(dynamic_cast.getOperator(), ICPPASTCastExpression.op_dynamic_cast);
	}

	public void testBug88338_CPP() throws Exception {
		IASTTranslationUnit tu = parse("struct A; struct A* a;", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertTrue(col.getName(0).isDeclaration());
		assertFalse(col.getName(0).isReference());
		assertTrue(col.getName(1).isReference());
		assertFalse(col.getName(1).isDeclaration());

		tu = parse("struct A* a;", CPP);
		col = new NameCollector();
		tu.accept(col);

		assertTrue(col.getName(0).isDeclaration());
		assertFalse(col.getName(0).isReference());
	}

	public void testPointerToFunction_CPP() throws Exception {
		IASTTranslationUnit tu = parse("int (*pfi)();", CPP);
		assertEquals(tu.getDeclarations().length, 1);
		IASTSimpleDeclaration d = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		assertEquals(d.getDeclarators().length, 1);
		IASTStandardFunctionDeclarator f = (IASTStandardFunctionDeclarator) d.getDeclarators()[0];
		assertEquals(f.getName().toString(), "");
		assertNotNull(f.getNestedDeclarator());
		assertEquals(f.getNestedDeclarator().getName().toString(), "pfi");
	}

	//	class X { public: int bar; };
	//	void f(){
	//	  X a[10];
	//	  a[0].bar;
	//	}
	public void testBug95484() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPField bar = (ICPPField) col.getName(1).resolveBinding();
		assertSame(bar, col.getName(6).resolveBinding());
	}

	//	int strcmp(const char *);
	//	void f(const char * const * argv){
	//	  strcmp(*argv);
	//	}
	public void testBug95419() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunction strcmp = (ICPPFunction) col.getName(0).resolveBinding();
		assertSame(strcmp, col.getName(4).resolveBinding());
	}

	//	class Other;
	//	class Base {
	//	  public: Base(Other *);
	//	};
	//	class Sub : public Base {
	//	  public: Sub(Other *);
	//	};
	//	Sub::Sub(Other * b) : Base(b) {}
	public void testBug95673() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();

		ICPPConstructor ctor = ba.assertNonProblem("Base(Other", 4, ICPPConstructor.class);
		ICPPConstructor ctor2 = ba.assertNonProblem("Base(b)", 4, ICPPConstructor.class);
		assertSame(ctor, ctor2);
	}

	//	void mem(void *, const void *);
	//	void f() {
	//	  char *x;  int offset;
	//	  mem(x, "FUNC");
	//	  mem(x + offset, "FUNC2");
	//	}
	public void testBug95768() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunction mem = (ICPPFunction) col.getName(0).resolveBinding();
		assertSame(mem, col.getName(6).resolveBinding());
		assertSame(mem, col.getName(8).resolveBinding());
	}

	// void trace(const void *);
	// class Foo {
	//    public: int import();
	// };
	// int Foo::import(){
	//    trace(this);
	// }
	public void testBug95741() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunction trace = (ICPPFunction) col.getName(0).resolveBinding();
		assertSame(trace, col.getName(7).resolveBinding());
	}

	// class RTCharacter {
	//    char value;
	//    public: operator char (void) const;
	// };
	// RTCharacter::operator char(void)const {
	//    return value;
	// }
	public void testBug95692() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPMethod op = (ICPPMethod) col.getName(2).resolveBinding();
		assertSame(op, col.getName(6).resolveBinding());
	}

	// int str(const char *);
	// void f(){
	//    str(0);
	//    str(00);  str(0x0);
	// }
	public void testBug95734() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunction str = (ICPPFunction) col.getName(0).resolveBinding();
		assertSame(str, col.getName(3).resolveBinding());
		assertSame(str, col.getName(4).resolveBinding());
		assertSame(str, col.getName(5).resolveBinding());
	}

	// int str(bool);
	// enum { ONE };
	// void f(char * p){
	//    str(1.2);
	//    str(ONE);  str(p);
	// }
	public void testBug95734b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunction str = (ICPPFunction) col.getName(0).resolveBinding();
		assertSame(str, col.getName(6).resolveBinding());
		assertSame(str, col.getName(7).resolveBinding());
		assertSame(str, col.getName(9).resolveBinding());
	}

	// void f() {
	// char * value;
	// ::operator delete(value);
	// }
	public void testBug95786() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);

		NameCollector col = new NameCollector();
		tu.accept(col);

		assertFalse(col.getName(2).resolveBinding() instanceof IProblemBinding);
	}

	// int foo()
	// try
	// {
	// // function body
	// }
	// catch (...)
	// {
	// }
	public void testBug86868() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		assertEquals(tu.getDeclarations().length, 1);
	}

	// void f(int t){
	// int s (t);
	// }
	public void testBug94779() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		IASTDeclarationStatement ds = (IASTDeclarationStatement) ((IASTCompoundStatement) ((IASTFunctionDefinition) tu
				.getDeclarations()[0]).getBody()).getStatements()[0];
		IASTDeclarator d = ((IASTSimpleDeclaration) ds.getDeclaration()).getDeclarators()[0];
		assertTrue(d.getName().resolveBinding() instanceof IVariable);
	}

	// int t= 0;
	// int s (t);
	public void testBug211756() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		IASTSimpleDeclaration sd = (IASTSimpleDeclaration) (tu.getDeclarations()[1]);
		IASTDeclarator d = sd.getDeclarators()[0];
		assertTrue(d.getName().resolveBinding() instanceof IVariable);
	}

	// typedef struct xs {
	//    int state;
	// } xs;
	// void f(xs *ci) {
	//    ci->state;
	//    (ci - 1)->state;
	// }
	public void testBug95714() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPField state = (ICPPField) col.getName(1).resolveBinding();
		assertSame(state, col.getName(7).resolveBinding());
		assertSame(state, col.getName(9).resolveBinding());
	}

	// float _Complex x;
	// double _Complex y;
	public void testBug95757() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		IASTDeclaration[] decls = tu.getDeclarations();

		assertTrue(((IASTSimpleDeclSpecifier) ((IASTSimpleDeclaration) decls[0]).getDeclSpecifier()).isComplex());
		assertEquals(((IASTSimpleDeclSpecifier) ((IASTSimpleDeclaration) decls[0]).getDeclSpecifier()).getType(),
				IASTSimpleDeclSpecifier.t_float);
		assertTrue(((IASTSimpleDeclSpecifier) ((IASTSimpleDeclaration) decls[1]).getDeclSpecifier()).isComplex());
		assertEquals(((IASTSimpleDeclSpecifier) ((IASTSimpleDeclaration) decls[1]).getDeclSpecifier()).getType(),
				IASTSimpleDeclSpecifier.t_double);
	}

	// class _A {
	//    static int i;
	// };
	// typedef _A A;
	// void f(){
	//    A::i++;
	// }
	public void testTypedefQualified() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPField i = (ICPPField) col.getName(1).resolveBinding();
		assertSame(i, col.getName(7).resolveBinding());
	}

	//	int f() {
	//	  return 5;
	//	}
	//	int main() {
	//	  int a(5);
	//	  int b(f());
	//	  return a+b;
	//  }
	public void testBug86849() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertNoProblemBindings(col);
	}

	// void copy(void *);
	// typedef struct {} A;
	// void f(A * a) {
	//    copy(a);
	// }
	public void testBug96655() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunction copy = (ICPPFunction) col.getName(0).resolveBinding();
		assertSame(copy, col.getName(7).resolveBinding());
	}

	public void testBug96678() throws Exception {
		parse("int x; // comment \r\n", CPP, false, true);
	}

	// struct A {};
	// void copy(A *);
	// void f() {
	//    copy(new A());
	// }
	public void testNewExpressionType() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPFunction copy = (ICPPFunction) col.getName(1).resolveBinding();
		assertSame(copy, col.getName(5).resolveBinding());
	}

	// class A {
	//    A(int i = 0);
	// };
	public void testDefaultConstructor() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPConstructor ctor = (ICPPConstructor) col.getName(1).resolveBinding();

		ICPPConstructor[] cs = A.getConstructors();
		assertTrue(cs.length == 2);
		assertSame(cs[1], ctor);
	}

	// class C { public: int foo; };
	// class B {
	//    C* operator ->();
	//    C& operator [] (int);
	// };
	// void f(){
	//    B b;
	//    b->foo;
	//    b[0].foo;
	// }
	public void testBug91707() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPField foo = (ICPPField) col.getName(1).resolveBinding();

		assertSame(foo, col.getName(12).resolveBinding());
		assertSame(foo, col.getName(14).resolveBinding());
	}

	// class A;
	// class A {
	//    class B;
	//    class C {};
	// };
	// class A::B{};
	public void testBug92425() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType A = (ICPPClassType) col.getName(0).resolveBinding();
		ICPPClassType B = (ICPPClassType) col.getName(2).resolveBinding();
		ICPPClassType C = (ICPPClassType) col.getName(3).resolveBinding();

		ICPPClassType[] classes = A.getNestedClasses();
		assertEquals(classes.length, 2);
		assertSame(classes[0], B);
		assertSame(classes[1], C);
	}

	// namespace A {
	//    struct F {} f;
	//    void f(int a) {}
	// }
	public void testBug92425b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPNamespace A = (ICPPNamespace) col.getName(0).resolveBinding();
		IBinding[] bindings = A.getMemberBindings();
		assertEquals(bindings.length, 3);
		assertSame(bindings[0], col.getName(1).resolveBinding());
		assertSame(bindings[1], col.getName(2).resolveBinding());
		assertSame(bindings[2], col.getName(3).resolveBinding());
	}

	// A< B< C< D< E< F< G< H<int> > > > > > > > a;
	// int A::B<int>::* b;
	public void testBug98704() throws Exception {
		parse(getAboveComment(), CPP);
	}

	// void f();
	// void f(void) {}
	public void testBug_AIOOBE() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction f = (IFunction) col.getName(0).resolveBinding();
		assertSame(f, col.getName(1).resolveBinding());
		IParameter p = (IParameter) col.getName(2).resolveBinding();
		assertNotNull(p);
	}

	// void f(const int);
	// void f(int);
	// void g() { f(1); }
	public void testRankingQualificationConversions_a() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertSame(col.getName(2).resolveBinding(), col.getName(5).resolveBinding());
	}

	// void f(const volatile int);
	// void f(const int);
	// void g() { f(1); }
	public void testRankingQualificationConversions_b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertSame(col.getName(2).resolveBinding(), col.getName(5).resolveBinding());
	}

	//	void f(const int&);
	//	void f(int&);
	//	void g(const int&);
	//	void g(int);
	//	void h(const int * const&);
	//	void h(int *);
	//	void i(int * const &);
	//	void i(const int *);
	//	void test() {
	//		const int ca= 1; int a;
	//		f(ca); // f(const int&)
	//		f(a);  // f(int &)
	//		g(ca); // ambiguous
	//		g(a);  // ambiguous
	//		h(&ca); // h(const int * const&)
	//		h(&a);  // void h(int *)
	//		i(&ca); // i(const int *)
	//		i(&a);  // i(int * const &)
	//	}
	public void testRankingQualificationConversions_c() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		ICPPFunction f1 = bh.assertNonProblem("f(const int&)", 1);
		ICPPFunction f2 = bh.assertNonProblem("f(int&)", 1);
		ICPPFunction g1 = bh.assertNonProblem("g(const int&)", 1);
		ICPPFunction g2 = bh.assertNonProblem("g(int)", 1);
		ICPPFunction h1 = bh.assertNonProblem("h(const int * const&)", 1);
		ICPPFunction h2 = bh.assertNonProblem("h(int *)", 1);
		ICPPFunction i1 = bh.assertNonProblem("i(int * const &)", 1);
		ICPPFunction i2 = bh.assertNonProblem("i(const int *)", 1);

		ICPPFunction ref;
		ref = bh.assertNonProblem("f(ca)", 1);
		assertSame(f1, ref);
		ref = bh.assertNonProblem("f(a)", 1);
		assertSame(f2, ref);
		bh.assertProblem("g(ca)", 1);
		bh.assertProblem("g(a)", 1);
		ref = bh.assertNonProblem("h(&ca)", 1);
		assertSame(h1, ref);
		ref = bh.assertNonProblem("h(&a)", 1);
		assertSame(h2, ref);
		ref = bh.assertNonProblem("i(&ca)", 1);
		assertSame(i2, ref);
		ref = bh.assertNonProblem("i(&a)", 1);
		assertSame(i1, ref);
	}

	// namespace n {
	//    namespace m {
	//       class A;
	//    }
	// }
	// namespace n {
	//    namespace m {
	//       class A { void f(); };
	//    }
	// }
	// namespace n {
	//    namespace m {
	//       void A::f(){}
	//    }
	// }
	public void testBug98818() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPNamespace n = (ICPPNamespace) col.getName(0).resolveBinding();
		ICPPNamespace m = (ICPPNamespace) col.getName(1).resolveBinding();
		assertSame(n, col.getName(3).resolveBinding());
		assertSame(n, col.getName(7).resolveBinding());
		assertSame(m, col.getName(4).resolveBinding());
		assertSame(m, col.getName(8).resolveBinding());

		ICPPClassType A = (ICPPClassType) col.getName(2).resolveBinding();
		assertSame(A, col.getName(5).resolveBinding());

		ICPPMethod f = (ICPPMethod) col.getName(9).resolveBinding();
		assertSame(f, col.getName(11).resolveBinding());
	}

	// struct A {
	//    struct { int i; } B;
	//    struct { int j; } C;
	// };
	// void f(){
	//    A a;
	//    a.B.i; a.C.j;
	// }
	public void testAnonymousStructures() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPField i = (ICPPField) col.getName(12).resolveBinding();
		ICPPField j = (ICPPField) col.getName(15).resolveBinding();
		assertSame(i, col.getName(2).resolveBinding());
		assertSame(j, col.getName(5).resolveBinding());
	}

	public void testBug99262() throws Exception {
		parse("void foo() {void *f; f=__null;}", CPP, true, true);
	}

	// void f1(int*) {
	// }
	// void f2() {
	//   f1(__null);
	// }
	public void testBug240567() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertNonProblem("f1(__null", 2, ICPPFunction.class);
	}

	public void testBug100408() throws Exception {
		IASTTranslationUnit tu = parse("int foo() { int x=1; (x)*3; }", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertNoProblemBindings(col);
	}

	public void testBug84478c() throws Exception {
		IASTTranslationUnit tu = parse("void foo() { switch(int x = 4) { case 4: x++; break; default: break;} }", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertNoProblemBindings(col);
		assertSame(col.getName(1).resolveBinding(), col.getName(2).resolveBinding());
	}

	public void testBug84478d() throws Exception {
		IASTTranslationUnit tu = parse("void foo() { for(int i = 0; int j = 0; ++i) {} }", CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);
		assertNoProblemBindings(col);
	}

	// void f(){
	//    if (int x = 1)  x++;
	//    else             x--;
	//    while(int y = 2)
	//       y++;
	//    for(int a = 1; int b = 2; b++){
	//       a++; b++;
	//    }
	// }
	public void testBug84478b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertNoProblemBindings(col);
		IVariable x = (IVariable) col.getName(1).resolveBinding();
		assertSame(x, col.getName(2).resolveBinding());
		assertSame(x, col.getName(3).resolveBinding());

		IVariable y = (IVariable) col.getName(4).resolveBinding();
		assertSame(y, col.getName(5).resolveBinding());

		IVariable a = (IVariable) col.getName(6).resolveBinding();
		IVariable b = (IVariable) col.getName(7).resolveBinding();
		assertSame(b, col.getName(8).resolveBinding());
		assertSame(a, col.getName(9).resolveBinding());
		assertSame(b, col.getName(10).resolveBinding());
	}

	// void free(void*);
	// void f(char** p) {
	//    free(p);
	// }
	public void testBug100415() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IFunction free = (IFunction) col.getName(0).resolveBinding();
		assertSame(free, col.getName(4).resolveBinding());
	}

	// class X;
	// void f() {
	//    class A {
	//       friend class X;
	//    };
	// }
	// namespace B {
	//    class A {
	//       friend class X;
	//    };
	// }
	public void testBug86688() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPClassType X = (ICPPClassType) col.getName(0).resolveBinding();
		assertNotSame(X, col.getName(3).resolveBinding());
		assertTrue(col.getName(3).resolveBinding() instanceof ICPPClassType);
		assertSame(X, col.getName(6).resolveBinding());
	}

	// class m {
	//    int m::f();
	// };
	// int m::f(){}
	public void testBug100403() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPMethod f = (ICPPMethod) col.getName(3).resolveBinding();
		assertSame(f, col.getName(6).resolveBinding());
	}

	// struct A {
	//    typedef int AT;
	//    void f1(AT);
	//    void f2(float);
	// };
	// struct B {
	//    typedef float BT;
	//    friend void A::f1(AT);
	//    friend void A::f2(BT);
	// };
	public void testBug90609() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ITypedef AT = (ITypedef) col.getName(1).resolveBinding();
		ICPPMethod f1 = (ICPPMethod) col.getName(2).resolveBinding();
		ICPPMethod f2 = (ICPPMethod) col.getName(5).resolveBinding();
		ITypedef BT = (ITypedef) col.getName(8).resolveBinding();

		assertSame(f1, col.getName(11).resolveBinding());
		assertSame(AT, col.getName(12).resolveBinding());
		assertSame(f2, col.getName(16).resolveBinding());
		assertSame(BT, col.getName(17).resolveBinding());
	}

	// struct Except { int blah; };
	// void f() {
	//    try { }
	//    catch (Except * e) {
	//       e->blah;
	//    }
	// }
	public void testBug103281() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IField blah = (IField) col.getName(1).resolveBinding();
		IVariable e = (IVariable) col.getName(4).resolveBinding();

		assertSame(e, col.getName(5).resolveBinding());
		assertSame(blah, col.getName(6).resolveBinding());
	}

	public void testBug78800() throws Exception {
		parseAndCheckBindings(
				"class Matrix {  public: Matrix & operator *(Matrix &); }; Matrix rotate, translate; Matrix transform = rotate * translate;");
	}

	// struct U { static int i; };
	// struct V : U { };
	// struct W : U { using U::i; };
	// struct X : V, W { void foo(); };
	// void X::foo() {
	//    i;
	// }
	public void test10_2s3b() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		NameCollector col = new NameCollector();
		tu.accept(col);

		ICPPField i = (ICPPField) col.getName(1).resolveBinding();
		ICPPUsingDeclaration using = (ICPPUsingDeclaration) col.getName(6).resolveBinding();
		IBinding[] delegates = using.getDelegates();
		assertEquals(delegates.length, 1);
		assertSame(i, delegates[0]);

		assertSame(i, col.getName(16).resolveBinding());
	}

	// int f() {
	// int x = 4;  while(x < 10) blah: ++x;
	// }
	public void testBug1043290() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment());
		IASTFunctionDefinition fd = (IASTFunctionDefinition) tu.getDeclarations()[0];
		IASTStatement[] statements = ((IASTCompoundStatement) fd.getBody()).getStatements();
		IASTWhileStatement whileStmt = (IASTWhileStatement) statements[1];
		IASTLabelStatement labelStmt = (IASTLabelStatement) whileStmt.getBody();
		assertTrue(labelStmt.getNestedStatement() instanceof IASTExpressionStatement);
		IASTExpressionStatement es = (IASTExpressionStatement) labelStmt.getNestedStatement();
		assertTrue(es.getExpression() instanceof IASTUnaryExpression);
	}

	// int f() {
	// int i;
	// do { ++i; } while(i < 10);
	// return 0;
	// }
	public void testBug104800() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment());
		IASTFunctionDefinition f = (IASTFunctionDefinition) tu.getDeclarations()[0];
		IASTCompoundStatement body = (IASTCompoundStatement) f.getBody();
		assertEquals(body.getStatements().length, 3);
	}

	public void testBug107150() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("#define FUNC_PROTOTYPE_PARAMS(list)    list\r\n");
		buffer.append("int func1 FUNC_PROTOTYPE_PARAMS((int arg1)){\r\n");
		buffer.append("return 0;\r\n");
		buffer.append("}\r\n");
		buffer.append("int func2 FUNC_PROTOTYPE_PARAMS\r\n");
		buffer.append("((int arg1)){\r\n");
		buffer.append("return 0;\r\n");
		buffer.append("}\r\n");
		IASTTranslationUnit tu = parse(buffer.toString(), CPP);
		assertFalse(tu.getDeclarations()[1] instanceof IASTProblemDeclaration);

		buffer = new StringBuilder();
		buffer.append("#define FUNC_PROTOTYPE_PARAMS(list)    list\n");
		buffer.append("int func1 FUNC_PROTOTYPE_PARAMS((int arg1)){\n");
		buffer.append("return 0;\n");
		buffer.append("}\n");
		buffer.append("int func2 FUNC_PROTOTYPE_PARAMS\n");
		buffer.append("((int arg1)){\n");
		buffer.append("return 0;\n");
		buffer.append("}\n");
		tu = parse(buffer.toString(), CPP);
		assertFalse(tu.getDeclarations()[1] instanceof IASTProblemDeclaration);
	}

	// class __attribute__((visibility("default"))) FooClass
	// {
	// int foo();
	// };
	// int FooClass::foo() {
	// return 0;
	// }
	public void testBug108202() throws Exception {
		parse(getAboveComment(), CPP, true, true);
	}

	// // Test redundant class specifiers
	// class MyClass {
	// 	int MyClass::field;
	// 	static int MyClass::static_field;
	// };
	// int MyClass::static_field;
	public void testBug174791() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);

		// check class
		IASTSimpleDeclaration sd = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		ICPPASTCompositeTypeSpecifier cts = (ICPPASTCompositeTypeSpecifier) sd.getDeclSpecifier();

		IASTSimpleDeclaration md = (IASTSimpleDeclaration) cts.getMembers()[0];
		IASTName name = md.getDeclarators()[0].getName();
		IField field = (IField) name.resolveBinding();
		// would crash and/or return wrong result
		assertFalse(field.isStatic());
		assertFalse(field.isExtern());
		assertFalse(field.isAuto());
		assertFalse(field.isRegister());

		md = (IASTSimpleDeclaration) cts.getMembers()[1];
		name = md.getDeclarators()[0].getName();
		field = (IField) name.resolveBinding();
		// would crash
		assertTrue(field.isStatic());
		assertFalse(field.isExtern());
		assertFalse(field.isAuto());
		assertFalse(field.isRegister());

		// check real static defn
		sd = (IASTSimpleDeclaration) tu.getDeclarations()[1];
		name = sd.getDeclarators()[0].getName();
		field = (IField) name.resolveBinding();
		assertTrue(field.isStatic());
	}

	// namespace nsSplit {}
	// namespace nsSplit {
	//    void a();
	// }
	// void nsSplit::a() {
	// }
	public void testBug180979() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);

		// check class
		IASTFunctionDefinition fd = (IASTFunctionDefinition) tu.getDeclarations()[2];
		IASTDeclarator fdecl = fd.getDeclarator();
		IASTName name = fdecl.getName();
		IBinding binding = name.resolveBinding();
		assertTrue(binding instanceof IFunction);
		assertFalse(binding instanceof IProblemBinding);
	}

	// #define _GLIBCXX_VISIBILITY(V) __attribute__ ((__visibility__ (#V)))
	// #define _GLIBCXX_BEGIN_NAMESPACE(X) namespace X _GLIBCXX_VISIBILITY(default) {
	// _GLIBCXX_BEGIN_NAMESPACE(std)
	// } // end the namespace
	public void testBug195701() throws Exception {
		parse(getAboveComment(), CPP, true, true);
	}

	// class A {
	//    double operator*(const A&);
	// };
	// class B : public A {
	//    double operator*(double);
	//    using A::operator*;
	// };
	public void testBug178059() throws Exception {
		parse(getAboveComment(), CPP, true, true);
	}

	// void foo (void *p) throw () ;
	// void bar (void *p) __attribute__ ((__nonnull__(1)));
	// void zot (void *p) throw () __attribute__ ((__nonnull__(1)));
	public void testBug179712() throws Exception {
		parse(getAboveComment(), CPP, true, true);
	}

	//	class C {
	//	public:
	//		int i;
	//	};
	//
	//	void problem1(int x) {}
	//	void problem2(const int *x) {}
	//	void problem3(int* x) {}
	//
	//	void nonproblem1(bool x) {}
	//	void problem4(bool* x) {}
	//	void problem5(const bool* x) {}
	//
	//	void f() {
	//		int C::* ptm;
	//
	//		problem1("p");
	//		problem2("p");
	//		problem3("p");
	//		nonproblem1("p");
	//		problem4("p");
	//		problem5("p");
	//
	//		problem1(ptm);
	//		problem2(ptm);
	//		problem3(ptm);
	//		nonproblem1(ptm);
	//		problem4(ptm);
	//		problem5(ptm);
	//	}
	public void testBug214335() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();

		IBinding b00 = bh.assertProblem("problem1(\"", 8);
		IBinding b01 = bh.assertProblem("problem2(\"", 8);
		IBinding b02 = bh.assertProblem("problem3(\"", 8);
		IBinding b03 = bh.assertNonProblem("nonproblem1(\"", 11);
		IBinding b04 = bh.assertProblem("problem4(\"", 8);
		IBinding b05 = bh.assertProblem("problem5(\"", 8);

		IBinding b06 = bh.assertProblem("problem1(ptm", 8);
		IBinding b07 = bh.assertProblem("problem2(ptm", 8);
		IBinding b08 = bh.assertProblem("problem3(ptm", 8);
		IBinding b09 = bh.assertNonProblem("nonproblem1(ptm", 11);
		IBinding b10 = bh.assertProblem("problem4(ptm", 8);
		IBinding b11 = bh.assertProblem("problem5(ptm", 8);

		assertInstance(b03, ICPPFunction.class);
		assertInstance(b09, ICPPFunction.class);
	}

	//	namespace outer {
	//		namespace inner {
	//			class foo{};
	//		}
	//		using namespace inner __attribute__((__strong__));
	//	}
	//	outer::foo x;
	//	outer::inner::foo y;
	public void testAttributeInUsingDirective_351228() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//	namespace source {
	//		class cls {
	//		};
	//		void fs();
	//		void fs(int a);
	//
	//	}
	//  void test1() {
	//		source::fs();
	//		source::fs(1);
	//		source::cls c2;
	//  }
	//
	//	using source::cls;
	//	using source::fs;
	//
	//	void test() {
	//		cls c2;
	//		fs();
	//		fs(1);
	//	}
	public void testReferencesOfUsingDecls() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);

		IASTDeclaration[] decls = tu.getDeclarations();
		ICPPASTNamespaceDefinition nsdef = (ICPPASTNamespaceDefinition) decls[0];
		ICPPASTUsingDeclaration udcl = (ICPPASTUsingDeclaration) decls[2];
		ICPPASTUsingDeclaration udf = (ICPPASTUsingDeclaration) decls[3];
		IASTFunctionDefinition fdef = (IASTFunctionDefinition) decls[4];

		IASTDeclaration[] nsdecls = nsdef.getDeclarations();
		ICPPASTCompositeTypeSpecifier cldef = (ICPPASTCompositeTypeSpecifier) ((IASTSimpleDeclaration) nsdecls[0])
				.getDeclSpecifier();
		ICPPASTFunctionDeclarator fdecl1 = (ICPPASTFunctionDeclarator) ((IASTSimpleDeclaration) nsdecls[1])
				.getDeclarators()[0];
		ICPPASTFunctionDeclarator fdecl2 = (ICPPASTFunctionDeclarator) ((IASTSimpleDeclaration) nsdecls[2])
				.getDeclarators()[0];

		IASTStatement[] stmts = ((IASTCompoundStatement) fdef.getBody()).getStatements();
		IASTName clname = ((IASTNamedTypeSpecifier) ((IASTSimpleDeclaration) ((IASTDeclarationStatement) stmts[0])
				.getDeclaration()).getDeclSpecifier()).getName();
		IASTName fnname1 = ((IASTIdExpression) ((IASTFunctionCallExpression) ((IASTExpressionStatement) stmts[1])
				.getExpression()).getFunctionNameExpression()).getName();
		IASTName fnname2 = ((IASTIdExpression) ((IASTFunctionCallExpression) ((IASTExpressionStatement) stmts[2])
				.getExpression()).getFunctionNameExpression()).getName();

		// check class
		IBinding b = cldef.getName().resolveBinding();
		assertEquals(3, tu.getReferences(b).length); // 2 refs + using-decl
		assertEquals(1, tu.getDefinitionsInAST(b).length); // class-def
		assertEquals(2, tu.getDeclarationsInAST(b).length); // class-def + using-decl

		// check functions
		b = fdecl1.getName().resolveBinding();
		assertEquals(3, tu.getReferences(b).length); // 2 refs + using-decl
		assertEquals(0, tu.getDefinitionsInAST(b).length); // function is not defined
		assertEquals(2, tu.getDeclarationsInAST(b).length); // func-decl + using-decl
		b = fdecl2.getName().resolveBinding();
		assertEquals(3, tu.getReferences(b).length); // 2 refs + using-decl
		assertEquals(0, tu.getDefinitionsInAST(b).length); // function is not defined
		assertEquals(2, tu.getDeclarationsInAST(b).length); // func-decl + using-decl

		// check using declaration class
		b = udcl.getName().resolveBinding();
		assertEquals(3, tu.getReferences(b).length); // 2 refs + using-decl
		assertEquals(1, tu.getDefinitionsInAST(b).length); // class-def
		assertEquals(2, tu.getDeclarationsInAST(b).length); // class-def + using-decl

		// check using declaration function
		b = udf.getName().resolveBinding();
		assertEquals(5, tu.getReferences(b).length); // 4 refs + using-decl
		assertEquals(0, tu.getDefinitionsInAST(b).length); // function is not defined
		assertEquals(3, tu.getDeclarationsInAST(b).length); // using-decl + 2 func-decls

		// check class reference
		b = clname.resolveBinding();
		assertEquals(3, tu.getReferences(b).length); // 2 refs + using-decl
		assertEquals(1, tu.getDefinitionsInAST(b).length); // class-def
		assertEquals(2, tu.getDeclarationsInAST(b).length); // class-def + using-decl

		// check function references
		b = fnname1.resolveBinding();
		assertEquals(3, tu.getReferences(b).length); // 2 refs + using-decl
		assertEquals(0, tu.getDefinitionsInAST(b).length); // function is not defined
		assertEquals(2, tu.getDeclarationsInAST(b).length); // using-decl + func-decl
		b = fnname2.resolveBinding();
		assertEquals(3, tu.getReferences(b).length); // 2 refs + using-decl
		assertEquals(0, tu.getDefinitionsInAST(b).length); // function is not defined
		assertEquals(2, tu.getDeclarationsInAST(b).length); // using-decl + func-decl
	}

	// namespace x {
	//    int a;
	// }
	// using namespace x;
	// class O {
	//    class I {
	//       void f();
	//    };
	// };
	// void O::I::f() {
	//    a=0;
	// }
	public void testUsingDirectiveWithNestedClass_209582() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();

		IBinding b = bh.assertNonProblem("a=", 1);
		assertEquals("x", b.getScope().getScopeName().toString());
	}

	// class Test {
	// public:
	//    Test(int, int *) {}
	//    Test(int *) {}
	// };
	// void test () {
	//    int bar= 0;
	//    int * pBar = &bar;
	//    Test foo1 (bar, &bar);
	//    Test foo2 (bar, pBar);
	//    Test foo3 (&bar);
	// }
	public void testCastAmbiguity_211756() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();

		bh.assertNonProblem("foo1", 4);
		bh.assertNonProblem("foo2", 4);
		bh.assertNonProblem("foo3", 4);
	}

	// int foo2() {
	//   int relayIndex = -1;
	//   int numRelays = 0;
	//	 if (relayIndex < 0 || relayIndex > numRelays)
	//	    return 0;
	// }
	public void testTemplateIDAmbiguity_104706() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();

		bh.assertNonProblem("relayIndex <", 10);
		bh.assertNonProblem("relayIndex >", 10);
		bh.assertNonProblem("numRelays)", 9);
	}

	//	template<typename A, typename B>
	//	struct and_ : public integral_constant<bool, (A::value && B::value)> {
	//	};
	//	template<typename A, typename B>
	//	struct or_ : public integral_constant<bool, (A::value || B::value)> {
	//	};
	//	template <class T> struct is_pod
	//	 : integral_constant<bool, (is_integral<T>::value ||
	//	                            is_floating_point<T>::value ||
	//	                            is_pointer<T>::value)> { };
	//
	//	typedef base::integral_constant<bool,
	//	    (base::has_trivial_copy<value_type>::value &&
	//	    base::has_trivial_destructor<value_type>::value)>
	//	    realloc_ok;
	public void testTemplateIDAmbiguity_228118() throws Exception {
		parse(getAboveComment(), CPP);
	}

	// namespace ns {
	//    class cl {};
	//    void func(cl c);
	// }
	// using ns::cl;
	// void test() {
	//    ns::cl qualified;
	//    cl unqualified;
	//    func(qualified);
	//    func(unqualified);
	// }
	public void testScopeOfUsingDelegates_219424() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();

		bh.assertNonProblem("cl c", 2);
		bh.assertNonProblem("func(qualified)", 4);
		bh.assertNonProblem("func(unqualified)", 4);
	}

	// class Test {
	//    void Test::member1();
	//    void Test::member2() {};
	// };
	// void Test::member1(){
	//    member2();
	// }
	public void testQualifiedMemberDeclaration_222026() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		IBinding b = bh.assertNonProblem("member1", 7);
		IBinding b2 = bh.assertNonProblem("member1(){", 7);
		assertTrue(b instanceof ICPPMethod);
		ICPPMethod m1 = (ICPPMethod) b;
		assertEquals("member1", m1.getName());
		assertEquals("Test", m1.getScope().getScopeName().toString());
		assertSame(b, b2);

		bh = new AST2AssertionHelper(code, true);
		b = bh.assertNonProblem("member2", 7);
		b2 = bh.assertNonProblem("member2();", 7);
		assertTrue(b instanceof ICPPMethod);
		m1 = (ICPPMethod) b;
		assertEquals("member2", m1.getName());
		assertEquals("Test", m1.getScope().getScopeName().toString());
		assertSame(b, b2);

		// different resolution order
		bh = new AST2AssertionHelper(code, true);
		b2 = bh.assertNonProblem("member1(){", 7);
		b = bh.assertNonProblem("member1", 7);
		assertTrue(b instanceof ICPPMethod);
		m1 = (ICPPMethod) b;
		assertEquals("member1", m1.getName());
		assertEquals("Test", m1.getScope().getScopeName().toString());
		assertSame(b, b2);

		bh = new AST2AssertionHelper(code, true);
		b2 = bh.assertNonProblem("member2();", 7);
		b = bh.assertNonProblem("member2", 7);
		assertTrue(b instanceof ICPPMethod);
		m1 = (ICPPMethod) b;
		assertEquals("member2", m1.getName());
		assertEquals("Test", m1.getScope().getScopeName().toString());
		assertSame(b, b2);
	}

	// namespace Test {
	//    void Test::member1();
	//    void Test::member2() {};
	// }
	// void Test::member1(){
	//    member2();
	// }
	public void testQualifiedMemberDeclarationInNamespace_222026() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		IBinding b = bh.assertNonProblem("member1", 7);
		IBinding b2 = bh.assertNonProblem("member1(){", 7);
		assertTrue(b instanceof ICPPFunction);
		ICPPFunction m1 = (ICPPFunction) b;
		assertEquals("member1", m1.getName());
		assertEquals("Test", m1.getScope().getScopeName().toString());
		assertSame(b, b2);

		bh = new AST2AssertionHelper(code, true);
		b = bh.assertNonProblem("member2", 7);
		b2 = bh.assertNonProblem("member2();", 7);
		assertTrue(b instanceof ICPPFunction);
		m1 = (ICPPFunction) b;
		assertEquals("member2", m1.getName());
		assertEquals("Test", m1.getScope().getScopeName().toString());
		assertSame(b, b2);

		// different resolution order
		bh = new AST2AssertionHelper(code, true);
		b2 = bh.assertNonProblem("member1(){", 7);
		b = bh.assertNonProblem("member1", 7);
		assertTrue(b instanceof ICPPFunction);
		m1 = (ICPPFunction) b;
		assertEquals("member1", m1.getName());
		assertEquals("Test", m1.getScope().getScopeName().toString());
		assertSame(b, b2);

		bh = new AST2AssertionHelper(code, true);
		b2 = bh.assertNonProblem("member2();", 7);
		b = bh.assertNonProblem("member2", 7);
		assertTrue(b instanceof ICPPFunction);
		m1 = (ICPPFunction) b;
		assertEquals("member2", m1.getName());
		assertEquals("Test", m1.getScope().getScopeName().toString());
		assertSame(b, b2);
	}

	// namespace ns { typedef int ns::TINT; } // illegal, still no CCE is expected.
	public void testQualifiedTypedefs_222093() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		IBinding td = bh.assertProblem("TINT", 4);
		bh.assertProblem("ns::", 2);
	}

	// void func() {
	// int a, b;
	// a < b || (a==b && a < b);
	//   if (a > b) {
	//   }
	// }
	public void testResettingTemplateIdScopesStack_223777() throws Exception {
		parseAndCheckBindings(getAboveComment());
	}

	// long x= 10L;
	public void testLongLiteral_225534() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		IASTDeclarator decltor = ((IASTSimpleDeclaration) tu.getDeclarations()[0]).getDeclarators()[0];
		IASTEqualsInitializer init = (IASTEqualsInitializer) decltor.getInitializer();
		ICPPASTLiteralExpression exp = (ICPPASTLiteralExpression) init.getInitializerClause();
		ICPPBasicType type = (ICPPBasicType) exp.getExpressionType();
		assertTrue(type.isLong());
	}

	// signed int si(0);
	// unsigned   u(10U);
	// signed     s(-1);
	// short      sh(0);
	// long       l(0L);
	// long long  ll(0LL);
	public void testInitializeUnsigned_245070() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//	void foo/*_a*/(int x) {}
	//	void foo/*_b*/(unsigned int) {}
	//	void foo/*_c*/(short x) {}
	//	void foo/*_d*/(unsigned short x) {}
	//	void foo/*_e*/(long x) {}
	//	void foo/*_f*/(unsigned long x) {}
	//	void foo/*_g*/(long long g) {}
	//	void foo/*_h*/(unsigned long long x) {}
	//	void foo/*_i*/(float x) {}
	//	void foo/*_j*/(double x) {}
	//	void foo/*_k*/(long double x) {}
	//
	//	int main() {
	//		foo/*a1*/(1);
	//		foo/*a2*/(010);
	//		foo/*a3*/(0x010);
	//
	//		foo/*b1*/(1U);
	//		foo/*b2*/(010U);
	//		foo/*b3*/(0x010U);
	//
	//      /*c*/ /*d*/
	//
	//		foo/*e1*/(1L);
	//		foo/*e2*/(010L);
	//		foo/*e3*/(0x010L);
	//
	//		foo/*f1*/(1UL);
	//		foo/*f2*/(010UL);
	//		foo/*f3*/(0x010UL);
	//
	//		foo/*g1*/(100000000000000000LL);
	//		foo/*g2*/(0100000000000LL);
	//		foo/*g3*/(0x01000000000LL);
	//
	//		foo/*h1*/(100000000000000000ULL);
	//		foo/*h2*/(0100000000000ULL);
	//		foo/*h3*/(0x01000000000ULL);
	//
	//		foo/*i1*/(11.1F);
	//		foo/*i2*/(11E1F);
	//
	//		foo/*j1*/(11.1);
	//		foo/*j2*/(11.1E1);
	//
	//		foo/*k1*/(11.1L);
	//		foo/*k2*/(11.1E1L);
	//	}
	public void testLiteralsViaOverloads_225534() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		char[] cs = { 'a', 'b', 'e', 'f', 'g', 'h', 'i', 'j', 'k' };
		for (char c : cs) {
			for (int i = 1; i < (c < 'i' ? 4 : 3); i++) {
				ICPPFunction def = ba.assertNonProblem("foo/*_" + c + "*/", 3, ICPPFunction.class);
				ICPPFunction ref = ba.assertNonProblem("foo/*" + c + "" + i + "*/", 3, ICPPFunction.class);
				assertSame("function ref: " + c + "" + i, def, ref);
			}
		}
	}

	//	class A {
	//		public:
	//			void doA() {}
	//	};
	//
	//	class FirstLevelProxy {
	//		public:
	//			A* operator->() {A *a = new A(); return a;} // leaky
	//			void doFLP() {}
	//	};
	//
	//	class SecondLevelProxy {
	//		public:
	//			FirstLevelProxy operator->() {FirstLevelProxy p; return p;}
	//			void doSLP() {}
	//	};
	//
	//	int main(int argc, char **argv) {
	//		SecondLevelProxy p2;
	//		p2->doA();
	//	}
	public void testRecursiveUserDefinedFieldAccess_205964() throws Exception {
		parseAndCheckBindings(getAboveComment());
	}

	// namespace ns {
	// class A {};
	// }
	//
	// using ns::A;
	//
	// class B: public A {};
	public void testBug235196() throws Exception {
		parseAndCheckBindings(getAboveComment());
	}

	// typedef int tint;
	// class X {
	//   typedef int xtint;
	//	 X();
	//	 ~X();
	//	 operator int ();
	//	 tint(a); // 1
	// };
	// X::X() {}
	// X::~X() {}
	// X::operator int() {}
	// X::xtint(a); // 2
	public void testEmptyDeclSpecifier() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertNonProblem("X {", 1, ICPPClassType.class);
		ba.assertNonProblem("X()", 1, ICPPConstructor.class);
		ba.assertNonProblem("~X", 2, ICPPMethod.class);
		ba.assertNonProblem("operator int", 12, ICPPMethod.class);
		ba.assertNonProblem("a); // 1", 1, ICPPField.class);

		ba.assertNonProblem("X() {}", 1, ICPPConstructor.class);
		ba.assertNonProblem("~X() {}", 2, ICPPMethod.class);
		ba.assertNonProblem("operator int() {}", 12, ICPPMethod.class);
		ba.assertNonProblem("a); // 2", 1, ICPPVariable.class);
	}

	// void* operator new (unsigned int, int[100]);
	// typedef int T;
	// int p[100];
	// void test(int f) {
	//	  new T;
	//    new T();
	//    new T(f);
	//    new (p) T;
	//    new (p) T();
	//    new (p) T(f);
	//	  new (T);
	//    new (T)();
	//    new (T)(f);
	//    new (p) (T);
	//    new (p) (T)();
	//    new (p) (T)(f);
	//	  new T[f][f];
	//    new (p) T[f][f];
	//	  new (T[f][f]);
	//    new (p) (T[f][f]);
	// };
	public void testNewPlacement() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment());
		IASTFunctionDefinition fdef = getDeclaration(tu, 3);

		checkNewExpression(fdef, 0, null, "int", null);
		checkNewExpression(fdef, 1, null, "int", IASTExpressionList.class);
		checkNewExpression(fdef, 2, null, "int", IASTIdExpression.class);
		checkNewExpression(fdef, 3, IASTIdExpression.class, "int", null);
		checkNewExpression(fdef, 4, IASTIdExpression.class, "int", IASTExpressionList.class);
		checkNewExpression(fdef, 5, IASTIdExpression.class, "int", IASTIdExpression.class);
		checkNewExpression(fdef, 6, null, "int", null);
		checkNewExpression(fdef, 7, null, "int", IASTExpressionList.class);
		checkNewExpression(fdef, 8, null, "int", IASTIdExpression.class);
		checkNewExpression(fdef, 9, IASTIdExpression.class, "int", null);
		checkNewExpression(fdef, 10, IASTIdExpression.class, "int", IASTExpressionList.class);
		checkNewExpression(fdef, 11, IASTIdExpression.class, "int", IASTIdExpression.class);

		checkNewExpression(fdef, 12, null, "int [][]", null);
		checkNewExpression(fdef, 13, IASTIdExpression.class, "int [][]", null);
		checkNewExpression(fdef, 14, null, "int [][]", null);
		checkNewExpression(fdef, 15, IASTIdExpression.class, "int [][]", null);
	}

	// namespace ns {
	//   void test() {}
	//   +
	// }
	public void testTrailingSyntaxErrorInNamespace() throws Exception {
		final String comment = getAboveComment();
		IASTTranslationUnit tu = parse(comment, CPP, false, false);
		ICPPASTNamespaceDefinition ns = getDeclaration(tu, 0);
		IASTDeclaration decl = getDeclaration(ns, 0);
		IASTProblemDeclaration pdecl = getDeclaration(ns, 1);
		assertEquals("+", pdecl.getRawSignature());
	}

	// extern "C" {
	//   void test() {}
	//   +
	// }
	public void testTrailingSyntaxErrorInLinkageSpec() throws Exception {
		final String comment = getAboveComment();
		IASTTranslationUnit tu = parse(comment, CPP, false, false);
		ICPPASTLinkageSpecification ls = getDeclaration(tu, 0);
		IASTDeclaration decl = getDeclaration(ls, 0);
		IASTProblemDeclaration pdecl = getDeclaration(ls, 1);
		assertEquals("+", pdecl.getRawSignature());
	}

	// class C;
	// void func(void (C::*m)(int) const);
	public void test233889_a() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPFunction func = bh.assertNonProblem("func(", 4, ICPPFunction.class);
		assertEquals(1, func.getParameters().length);
		IType type = func.getParameters()[0].getType();
		ICPPPointerToMemberType ptm = assertInstance(type, ICPPPointerToMemberType.class);
		ICPPFunctionType t = ((ICPPFunctionType) ptm.getType());
		assertTrue(t.isConst());
	}

	//	 struct C {
	//		 int m1(int a);
	//		 int m2(int a) const;
	//	 };
	//
	//	 C* func(int (C::*m)(int) const);
	//	 C* func(int (C::*m)(int));
	//
	//	 void ref() {
	//		 func(&C::m1);
	//		 func(&C::m2);
	//	 }
	public void testBug233889_b() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPFunction fn1 = bh.assertNonProblem("func(&C::m1", 4, ICPPFunction.class);
		ICPPFunction fn2 = bh.assertNonProblem("func(&C::m2", 4, ICPPFunction.class);
		assertNotSame(fn1, fn2);
	}

	//	class A;
	//
	//	void foo(A* a) {}
	//	void foo(const A* a) {}
	//	void foo(volatile A* a) {}
	//	void foo(const volatile A* a) {}
	//
	//	class A {
	//	public:
	//		void member1() { foo(this);/*1*/ }
	//		void member2() const { foo(this);/*2*/ }
	//		void member3() volatile { foo(this);/*3*/ }
	//		void member4() const volatile { foo(this);/*4*/ }
	//	};
	public void testThisType() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ICPPFunction pt1 = ba.assertNonProblem("foo(this);/*1*/", 3, ICPPFunction.class);
		ICPPFunction pt2 = ba.assertNonProblem("foo(this);/*2*/", 3, ICPPFunction.class);
		ICPPFunction pt3 = ba.assertNonProblem("foo(this);/*3*/", 3, ICPPFunction.class);
		ICPPFunction pt4 = ba.assertNonProblem("foo(this);/*4*/", 3, ICPPFunction.class);

		IType t1 = ((IPointerType) pt1.getType().getParameterTypes()[0]).getType();
		IQualifierType t2 = (IQualifierType) ((IPointerType) pt2.getType().getParameterTypes()[0]).getType();
		IQualifierType t3 = (IQualifierType) ((IPointerType) pt3.getType().getParameterTypes()[0]).getType();
		IQualifierType t4 = (IQualifierType) ((IPointerType) pt4.getType().getParameterTypes()[0]).getType();

		assertTrue(!(t1 instanceof IQualifierType));
		assertTrue(t2.isConst());
		assertTrue(!t2.isVolatile());
		assertTrue(!t3.isConst());
		assertTrue(t3.isVolatile());
		assertTrue(t4.isConst());
		assertTrue(t4.isVolatile());
	}

	//	class A {
	//		public:
	//			void foo() {}
	//	};
	//
	//	void ref() {
	//		A a1= *new A();
	//		a1->foo();/*1*/
	//      A* a2= new A();
	//      a2->foo();/*2*/
	//	}
	public void testMemberAccessOperator_a() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertProblem("foo();/*1*/", 3);
		ba.assertNonProblem("foo();/*2*/", 3);
	}

	//	class A {
	//		public:
	//			void foo() {};
	//	};
	//
	//	class B {
	//		public:
	//			void foo() {};
	//			A* operator->() {return new A();}
	//	};
	//
	//	void ref() {
	//		B b= *new B();
	//		b->foo();/*1*/
	//		B* b2= new B();
	//		b2->foo();/*2*/
	//	}
	public void testMemberAccessOperator_b() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ICPPMethod m1 = ba.assertNonProblem("foo();/*1*/", 3, ICPPMethod.class);
		ICPPMethod m2 = ba.assertNonProblem("foo();/*2*/", 3, ICPPMethod.class);
		assertEquals(m1.getClassOwner().getName(), "A");
		assertEquals(m2.getClassOwner().getName(), "B");
	}

	//	class A { public: void foo(); };
	//	class B { public: A* operator->() {return  new A();} };
	//	class C { public: B  operator->() {return *new B();} };
	//
	//	void ref() {
	//	   C c;
	//	   c->foo();/**/ // refers to A::foo
	//	}
	public void testMemberAccessOperator_c() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertNonProblem("foo();/**/", 3);
	}

	//	class A { public: void foo(); };
	//	class B { public: A* operator->() {return  new A();} };
	//	class C { public: B* operator->() {return new B();} };
	//
	//	void ref() {
	//	   C c;
	//	   c->foo();/**/ // expect problem - foo is not in B
	//	}
	public void testMemberAccessOperator_d() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertProblem("foo();/**/", 3);
	}

	//	class A { public: void foo(); };
	//  typedef A A2;
	//	class B { public: A2* operator->() {return  new A();} };
	//  typedef B B2;
	//	class C { public: B2 operator->() {return *new B();} };
	//
	//  typedef C C2;
	//	void ref() {
	//	   C2 c;
	//	   c->foo();/**/ // refers to A::foo
	//	}
	public void testMemberAccessOperator_e() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertNonProblem("foo();/**/", 3);
	}

	//  namespace std {class type_info{public: const char* name() const;};}
	//	int main() {
	//		int s;
	//		typeid(int).name();
	//		typeid(s).name();
	//	}
	public void testTypeid_209578() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP);
	}

	//	typedef int int5[5];
	//
	//	void f1(int* x);
	//	void f2(int** x);
	//	void f3(int5 x);
	//	void f4(int5* x);
	//
	//	void test(int5 p, int5* q, int* r, int** s) {
	//	  f1(p);
	//	  f2(q);
	//	  f3(r);
	//	  f4(s);
	//	}
	public void testArrayToPointerConversion() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertNonProblem("f1(p)", 2, ICPPFunction.class);
		ba.assertProblem("f2(q)", 2);
		ba.assertNonProblem("f3(r)", 2, ICPPFunction.class);
		ba.assertProblem("f4(s)", 2);
	}

	//	typedef char* t1;
	//	typedef char t2[8];
	//  typedef char* charp;
	//	void test(charp x) {}
	//	int main(void) {
	//		char x[12];
	//		t1 y;
	//		t2 z;
	//		test(x);
	//		test(y);
	//		test(z);
	//	}
	public void testArrayToPtrConversionForTypedefs_239931() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP);
	}

	//	typedef char t[12];
	//	void test1(char *);
	//	void test2(char []);
	//	void test3(t);
	//	void xx() {
	//	  char* x= 0;
	//	  test1(x);
	//	  test2(x);
	//	  test3(x);
	//	}
	public void testAdjustmentOfParameterTypes_239975() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP);
	}

	//	class A {
	//	public:
	//	  void m(int c);
	//	};
	//
	//	void test(char c) {
	//	  void (A::* ptr2mem)(char);
	//	  ptr2mem= reinterpret_cast<void (A::*)(char)>(&A::m);
	//	  ptr2mem= (void (A::*)(int))(0);
	//	}
	public void testTypeIdForPtrToMember_242197() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP);
	}

	// void restrict();
	public void testRestrictIsNoCPPKeyword_228826() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code, CPP, false);
		parseAndCheckBindings(getAboveComment(), CPP, true); // even with gnu extensions
	}

	// void test1();
	// void test2() throw ();
	// void test3() throw (int);
	public void testEmptyExceptionSpecification_86943() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment(), CPP);

		IASTSimpleDeclaration d = getDeclaration(tu, 0);
		ICPPASTFunctionDeclarator fdtor = (ICPPASTFunctionDeclarator) d.getDeclarators()[0];
		IASTTypeId[] specs = fdtor.getExceptionSpecification();
		assertEquals(0, specs.length);
		assertSame(ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION, specs);

		d = getDeclaration(tu, 1);
		fdtor = (ICPPASTFunctionDeclarator) d.getDeclarators()[0];
		specs = fdtor.getExceptionSpecification();
		assertEquals(0, specs.length);
		assertNotSame(ICPPASTFunctionDeclarator.NO_EXCEPTION_SPECIFICATION, specs);

		d = getDeclaration(tu, 2);
		fdtor = (ICPPASTFunctionDeclarator) d.getDeclarators()[0];
		specs = fdtor.getExceptionSpecification();
		assertEquals(1, specs.length);
	}

	//	int test() {
	//		try {
	//		} catch (const int &ex) {
	//		} catch (const char &ex) {
	//		}
	//	}
	public void testScopeOfCatchHandler_209579() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP);
	}

	//	void func(const char& c);
	//	void func(char& c);
	//
	//	void test(const char& x, char& y) {
	//	  func(x);
	//	  func(y);
	//	}
	public void testOverloadedFunction_248774() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPFunction func1 = helper.assertNonProblem("func(x)", 4, ICPPFunction.class);
		ICPPFunction func2 = helper.assertNonProblem("func(y)", 4, ICPPFunction.class);
		assertNotSame(func1, func2);
	}

	//	struct A {
	//	  const char& operator[](int pos) const;
	//	  char& operator[](int pos);
	//	};
	//
	//	void func(const char& c);
	//	void func(char& c);
	//
	//	void test(const A& x, A& y) {
	//	  func(x[0]);
	//	  func(y[0]);
	//	}
	public void testOverloadedOperator_248803() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPFunction func1 = helper.assertNonProblem("func(x[0])", 4, ICPPFunction.class);
		ICPPFunction func2 = helper.assertNonProblem("func(y[0])", 4, ICPPFunction.class);
		assertNotSame(func1, func2);
	}

	// class NonVirt {
	//    void m();//0
	// };
	// class C1 : NonVirt {
	//    virtual void m();//1
	// };
	// class C2 : C1 {
	//    void m();//2
	// };
	// class C3 : C2 {
	//    void m(int);
	// };
	// class C4 : C3 {
	//    void m();//4
	// };
	// class C5 : C1 {
	//    void m();//5
	// };
	public void testOverridden_248846() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPMethod m0 = helper.assertNonProblem("m();//0", 1, ICPPMethod.class);
		ICPPMethod m1 = helper.assertNonProblem("m();//1", 1, ICPPMethod.class);
		ICPPMethod m2 = helper.assertNonProblem("m();//2", 1, ICPPMethod.class);
		ICPPMethod m3 = helper.assertNonProblem("m(int);", 1, ICPPMethod.class);
		ICPPMethod m4 = helper.assertNonProblem("m();//4", 1, ICPPMethod.class);
		ICPPMethod m5 = helper.assertNonProblem("m();//5", 1, ICPPMethod.class);

		assertFalse(ClassTypeHelper.isVirtual(m0));
		assertFalse(ClassTypeHelper.isVirtual(m3));
		assertTrue(ClassTypeHelper.isVirtual(m1));
		assertTrue(ClassTypeHelper.isVirtual(m2));
		assertTrue(ClassTypeHelper.isVirtual(m4));
		assertTrue(ClassTypeHelper.isVirtual(m5));

		assertFalse(ClassTypeHelper.isOverrider(m0, m0));
		assertFalse(ClassTypeHelper.isOverrider(m1, m0));
		assertFalse(ClassTypeHelper.isOverrider(m2, m0));
		assertFalse(ClassTypeHelper.isOverrider(m3, m0));
		assertFalse(ClassTypeHelper.isOverrider(m4, m0));
		assertFalse(ClassTypeHelper.isOverrider(m5, m0));

		assertFalse(ClassTypeHelper.isOverrider(m0, m1));
		assertFalse(ClassTypeHelper.isOverrider(m1, m1));
		assertFalse(ClassTypeHelper.isOverrider(m3, m1));
		assertTrue(ClassTypeHelper.isOverrider(m2, m1));
		assertTrue(ClassTypeHelper.isOverrider(m4, m1));
		assertTrue(ClassTypeHelper.isOverrider(m5, m1));

		assertFalse(ClassTypeHelper.isOverrider(m0, m2));
		assertFalse(ClassTypeHelper.isOverrider(m1, m2));
		assertFalse(ClassTypeHelper.isOverrider(m2, m2));
		assertFalse(ClassTypeHelper.isOverrider(m3, m2));
		assertFalse(ClassTypeHelper.isOverrider(m5, m2));
		assertTrue(ClassTypeHelper.isOverrider(m4, m2));

		ICPPMethod[] ors = ClassTypeHelper.findOverridden(m0);
		assertEquals(0, ors.length);
		ors = ClassTypeHelper.findOverridden(m1);
		assertEquals(0, ors.length);
		ors = ClassTypeHelper.findOverridden(m2);
		assertEquals(1, ors.length);
		assertEquals(ors[0], m1);
		ors = ClassTypeHelper.findOverridden(m3);
		assertEquals(0, ors.length);
		ors = ClassTypeHelper.findOverridden(m4);
		assertEquals(2, ors.length);
		assertEquals(ors[0], m2);
		assertEquals(ors[1], m1);
		ors = ClassTypeHelper.findOverridden(m5);
		assertEquals(1, ors.length);
		assertEquals(ors[0], m1);
	}

	//  struct A {
	//    int a;
	//  };
	//
	//  void test(A* p) {
	//    p.a; // should not resolve
	//  }
	public void testPointerMemberAccess_245068() throws Exception {
		final String comment = getAboveComment();
		final boolean[] isCpps = { false, true };
		for (boolean isCpp : isCpps) {
			BindingAssertionHelper ba = new AST2AssertionHelper(comment, isCpp);
			ba.assertProblem("a; // should not resolve", 1);
		}
	}

	//	namespace ns {
	//	  template<typename T> class CT {};
	//  }
	//  using ns::CT<int>;
	public void testTemplateIdInUsingDecl_251199() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP);
	}

	//	namespace ns {
	//	struct A {};
	//	}
	//
	//	struct B {
	//	  operator ns::A(); // problems on operator ns and on A
	//	};
	public void testNamespaceQualifiedOperator_256840() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		bh.assertNonProblem("operator ns::A", 14);
		parseAndCheckBindings(code, CPP);
	}

	//	void f();
	//
	//	void test(int p) {
	//	  f(p);
	//	}
	public void testFunctionExtraArgument() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertProblem("f(p)", 1);
	}

	//	void f(...);
	//
	//	void test(int* p) {
	//	  f(p);
	//	}
	public void testVariadicFunction_2500582() throws Exception {
		final String comment = getAboveComment();
		final boolean[] isCpps = { false, true };
		for (boolean isCpp : isCpps) {
			BindingAssertionHelper ba = new AST2AssertionHelper(comment, isCpp);
			ba.assertNonProblem("f(p)", 1, IFunction.class);
		}
	}

	//  struct Incomplete;
	//
	//  void f(Incomplete* p);
	//  void f(...);
	//
	//  void test() {
	//	  // Should resolve to f(Incomplete*) since 0 can be converted to Incomplete*
	//    f(0);
	//	}
	public void testVariadicFunction_2500583() throws Exception {
		final String comment = getAboveComment();
		final boolean[] isCpps = { false, true };
		for (boolean isCpp : isCpps) {
			BindingAssertionHelper ba = new AST2AssertionHelper(comment, isCpp);
			IFunction decl = ba.assertNonProblem("f(Incomplete* p)", 1, IFunction.class);
			IFunction func = ba.assertNonProblem("f(0)", 1, IFunction.class);
			assertSame(decl, func);
		}
	}

	//    class MyClass{
	//    public:
	//       int v;
	//       int& operator () (int i){ return v; }
	//       int& operator () (int i, int j){ return v; }
	//    };
	//
	//    int main(MyClass c, int i){
	//      c(i,i)= 0;
	//    	c(i) = 0;
	//    }
	public void testFunctionCallOnLHS_252695() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parseAndCheckBindings(code, CPP, true);
		IASTFunctionDefinition fdef = getDeclaration(tu, 1);
		IASTExpressionStatement exstmt = getStatement(fdef, 0);
		assertInstance(exstmt.getExpression(), IASTBinaryExpression.class);
		exstmt = getStatement(fdef, 1);
		assertInstance(exstmt.getExpression(), IASTBinaryExpression.class);
	}

	//    template <int E> class A;
	//    class A {};
	//    class A;
	//    class B;
	//    template <int E> class B;
	//    template <int E> class B {};
	public void testInvalidClassRedeclaration_254961() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parse(code, CPP, true, false);
		NameCollector nc = new NameCollector();
		tu.accept(nc);
		assertProblemBindings(nc, 4);
		assertProblemBinding(IProblemBinding.SEMANTIC_INVALID_REDEFINITION, nc.getName(2).resolveBinding());
		assertProblemBinding(IProblemBinding.SEMANTIC_INVALID_REDECLARATION, nc.getName(3).resolveBinding());
		assertProblemBinding(IProblemBinding.SEMANTIC_INVALID_REDECLARATION, nc.getName(6).resolveBinding());
		assertProblemBinding(IProblemBinding.SEMANTIC_INVALID_REDEFINITION, nc.getName(8).resolveBinding());
	}

	//    template <typename T> class A;
	//    template <template<typename> class T> class A {};
	//    template <template<typename> class T> class A;
	//    template <template<typename> class T> class B {};
	//    template <typename T> class B;
	//    template <typename T> class B {};
	public void testInvalidClassRedeclaration_364226() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parse(code, CPP, true, false);
		NameCollector nc = new NameCollector();
		tu.accept(nc);
		assertProblemBindings(nc, 4);
		assertProblemBinding(IProblemBinding.SEMANTIC_INVALID_REDEFINITION, nc.getName(4).resolveBinding());
		assertProblemBinding(IProblemBinding.SEMANTIC_INVALID_REDECLARATION, nc.getName(7).resolveBinding());
		assertProblemBinding(IProblemBinding.SEMANTIC_INVALID_REDECLARATION, nc.getName(12).resolveBinding());
		assertProblemBinding(IProblemBinding.SEMANTIC_INVALID_REDEFINITION, nc.getName(14).resolveBinding());
	}

	//    struct Foo {
	//        void foo();
	//    };
	//    void Foo::foo(void) {
	//    }
	public void testVoidParamInDefinition_257376() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP);
	}

	//    namespace ns {
	//    	struct C {
	//    		C(C* c){}
	//    	};
	//    	int a;
	//    }
	//    struct C {
	//    	C(ns::C*){}
	//    };
	//    void testbla() {
	//    	ns::C* cptr= 0;
	//    	C c= C(cptr);
	//    }
	public void testNoKoenigForConstructors() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP);
	}

	//    typedef void VOID;
	//    void donothing();
	//    void donothing(VOID){}
	//    void donothing(VOID);
	//    void test() {
	//      donothing();
	//    }
	public void testVoidViaTypedef_258694() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP);
	}

	//	struct A {
	//	  B method(B p, int& a = y) { // B is not defined
	//	    B b;
	//      int x = y + 1;
	//      return b;
	//	  }
	//	  struct B {};
	//    static int& x = y; // y is not defined
	//    int y;
	//	};
	public void testScopeOfClassMember_259460() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertNonProblem("B b", 1, ICPPClassType.class);
		ba.assertProblem("B p", 1);
		ba.assertProblem("B method", 1);
		ba.assertNonProblem("y + 1;", 1, ICPPField.class);
		ba.assertNonProblem("y) {", 1, ICPPField.class);
		ba.assertProblem("y; // y is not defined", 1);
	}

	//  class A {
	//    int method(int a = GREEN) {
	//      return RED;
	//    }
	//    static int x = GREEN; // GREEN is not defined
	//    enum Color {
	//      RED, GREEN
	//    };
	//  };
	public void testScopeOfClassMember_259648() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertNonProblem("GREEN)", 5, IEnumerator.class);
		ba.assertNonProblem("RED;", 3, IEnumerator.class);
		ba.assertProblem("GREEN;", 5);
	}

	//  struct A {
	//    int& operator*();
	//    const int& operator*() const;
	//  };
	//  void func(int& p) {}
	//  void func(const int& p) {}
	//
	// 	void test(A& a, const A& b) {
	// 	  func(*a);
	// 	  func(*b);
	// 	}
	public void testSmartPointerReference_259680() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ICPPFunction f1 = ba.assertNonProblem("func(*a)", 4, ICPPFunction.class);
		ICPPFunction f2 = ba.assertNonProblem("func(*b)", 4, ICPPFunction.class);
		assertNotSame(f1, f2);
	}

	//	struct C {int a;};
	//	void myfunc(C c) {
	//		return c < c.a ||
	//	        (c == c.a && (c<c.a || (c == c.a && (c<c.a || (c == c.a && (c<c.a ||
	//	        (c == c.a && (c<c.a || (c == c.a && (c<c.a || (c == c.a && (c<c.a ||
	//	        (c == c.a && (c<c.a || (c == c.a && (c<c.a || (c == c.a && (c<c.a ||
	//	        (c == c.a && (c<c.a || (c == c.a && (c<c.a || (c == c.a && (c<c.a ||
	//	        (c == c.a && (c<c.a || (c == c.a && (c<c.a || (c == c.a && (c<c.a
	//	        ))))))))))))))))))))))))))))));
	//	}
	public void testNestedTemplateIDAmbiguity_259501() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
	}

	//  struct A {
	//    int a;
	//  };
	//
	//  struct B {
	//    A operator-(B b);
	//    A operator+(B& b);
	//    A operator*(const B& b);
	//    A operator/(B b) const;
	//    A operator%(const B& b) const;
	//  };
	//
	//  void test(B p1, B p2) {
	//    (p1 - p2).a; //1
	//    (p1 + p2).a; //2
	//    (p1 * p2).a; //3
	//    (p1 / p2).a; //4
	//    (p1 % p2).a; //5
	//  }
	public void testOverloadedBinaryOperator_259927a() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertNonProblem("a; //1", 1, ICPPField.class);
		ba.assertNonProblem("a; //2", 1, ICPPField.class);
		ba.assertNonProblem("a; //3", 1, ICPPField.class);
		ba.assertNonProblem("a; //4", 1, ICPPField.class);
		ba.assertNonProblem("a; //5", 1, ICPPField.class);
	}

	//  struct A {
	//    int a;
	//  };
	//  struct B {};
	//  enum E { zero };
	//
	//  A operator-(B p1, int p2);
	//  A operator+(int p1, const B& p2);
	//  A operator*(E p1, int p2);
	//  A operator/(int p1, const E& p2);
	//  A operator%(const B& p1, const B& p2);
	//
	//  void test(B b, E e, int i) {
	//    (b - i).a; //1
	//    (i + b).a; //2
	//    (e * i).a; //3
	//    (i / e).a; //4
	//    (b % b).a; //5
	//    (b + i).a; //6
	//  }
	public void testOverloadedBinaryOperator_259927b() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertNonProblem("a; //1", 1, ICPPField.class);
		ba.assertNonProblem("a; //2", 1, ICPPField.class);
		ba.assertNonProblem("a; //3", 1, ICPPField.class);
		ba.assertNonProblem("a; //4", 1, ICPPField.class);
		ba.assertNonProblem("a; //5", 1, ICPPField.class);
		ba.assertProblem("a; //6", 1);
	}

	// struct A {
	//   int x;
	// };
	//
	// struct B {
	//    A& operator++(); // prefix
	//    A operator++(int); // postfix
	// };
	//
	// void test(B p1, B p2) {
	//    (p1++).x; //1
	//    (++p1).x; //2
	// }
	public void testOverloadedUnaryOperator_259927c() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertNonProblem("x; //1", 1, ICPPField.class);
		ba.assertNonProblem("x; //2", 1, ICPPField.class);
	}

	// struct A {
	//   int x;
	// };
	// struct B { };
	// A& operator++(B); // prefix
	// A operator++(B, int); // postfix
	//
	// void test(B p1, B p2) {
	//    (p1++).x; //1
	//    (++p1).x; //2
	// }
	public void testOverloadedUnaryOperator_259927d() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertNonProblem("x; //1", 1, ICPPField.class);
		ba.assertNonProblem("x; //2", 1, ICPPField.class);
	}

	// struct A {
	//	int xx;
	// };
	//
	//
	// struct B {
	//	A operator*();
	//	A operator&();
	//	A operator+();
	//	A operator-();
	//	A operator!();
	//	A operator~();
	// };
	//
	// int main() {
	//	B b;
	//
	//	(*b).xx; // 1
	//	(&b).xx; // 2
	//	(+b).xx; // 3
	//	(-b).xx; // 4
	//	(!b).xx; // 5
	//	(~b).xx; // 6
	// }
	public void testOverloadedUnaryOperator_259927e() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		for (int i = 1; i <= 6; i++)
			ba.assertNonProblem("xx; // " + i, 2, ICPPField.class);
	}

	// struct A {
	//	int xx;
	// };
	//
	//
	// struct B {
	// };
	//
	// A operator*(B);
	// A operator&(B);
	// A operator+(B);
	// A operator-(B);
	// A operator!(B);
	// A operator~(B);
	//
	// int main() {
	//	B b;
	//
	//	(*b).xx; // 1
	//	(&b).xx; // 2
	//	(+b).xx; // 3
	//	(-b).xx; // 4
	//	(!b).xx; // 5
	//	(~b).xx; // 6
	//}
	public void testOverloadedUnaryOperator_259927f() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		for (int i = 1; i <= 6; i++)
			ba.assertNonProblem("xx; // " + i, 2, ICPPField.class);
	}

	// int a,b,c,d ;
	// class X {
	//	 void m() {
	//      T* a;
	//		I* b;
	//		S1* c;
	//		S2* d;
	//	 }
	//	 typedef int T;
	//	 int I;
	//	 typedef int S1 (int(T));  // resolve this ambiguity first
	//	 typedef int S2 (int(t));  // resolve this ambiguity first
	// };
	public void testOrderOfAmbiguityResolution_259373() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ICPPVariable a = ba.assertNonProblem("a;", 1);
		assertInstance(a.getType(), IPointerType.class);
		ICPPVariable b = ba.assertNonProblem("b;", 1);
		assertInstance(b.getType(), IBasicType.class);
		ICPPVariable c = ba.assertNonProblem("c;", 1);
		assertInstance(c.getType(), IPointerType.class);
		ITypedef s1 = (ITypedef) ((IPointerType) c.getType()).getType();
		assertInstance(((IFunctionType) s1.getType()).getParameterTypes()[0], IPointerType.class);
		ICPPVariable d = ba.assertNonProblem("d;", 1);
		assertInstance(d.getType(), IPointerType.class);
		ITypedef s2 = (ITypedef) ((IPointerType) d.getType()).getType();
		assertInstance(((IFunctionType) s2.getType()).getParameterTypes()[0], IBasicType.class);
	}

	//	namespace A {
	//	  class X {
	//	    friend void f(int);
	//	    class Y {
	//		  friend void g(int);
	//	  	};
	//	  };
	//	  void test() {
	//	    f(1);
	//	    g(1);
	//	  }
	//	}
	public void testFriendFunctionResolution_86368() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		IFunction f1 = bh.assertNonProblem("f(int)", 1);
		IFunction f2 = bh.assertNonProblem("f(1)", 1);
		assertSame(f1, f2);
		IFunction g1 = bh.assertNonProblem("g(int)", 1);
		IFunction g2 = bh.assertNonProblem("g(1)", 1);
		assertSame(g1, g2);

		// Alternative binding resolution order.
		bh = getAssertionHelper();
		f2 = bh.assertNonProblem("f(1)", 1);
		f1 = bh.assertNonProblem("f(int)", 1);
		assertSame(f1, f2);
		g2 = bh.assertNonProblem("g(1)", 1);
		g1 = bh.assertNonProblem("g(int)", 1);
		assertSame(g1, g2);
	}

	//	template <typename T>
	//	class A {
	//	  template <typename U>
	//	  friend int func(int i);
	//	};
	//
	//	template <typename U>
	//	int func(int i = 0);
	//
	//	template <typename U>
	//	int func(int i) { return i; }
	public void testFriendFunction_438114() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPFunction f1 = bh.assertNonProblemOnFirstIdentifier("func(int i);");
		ICPPFunction f2 = bh.assertNonProblemOnFirstIdentifier("func(int i = 0);");
		ICPPFunction f3 = bh.assertNonProblemOnFirstIdentifier("func(int i) {");
		assertSame(f1, f2);
		assertSame(f2, f3);
		assertEquals(0, f1.getRequiredArgumentCount());
	}

	// class A {
	// public:
	//    void foo() const volatile;
	//    void foo() volatile;
	//    void foo() const;
	//    void foo();
	//    void bar() const volatile;
	//    void bar() volatile;
	//    void bar() const;
	//    void bar();
	// };
	// void A::foo() const volatile { bar();/*1*/ }
	// void A::foo() volatile       { bar();/*2*/ }
	// void A::foo() const          { bar();/*3*/ }
	// void A::foo()                { bar();/*4*/ }
	// void test() {
	//   A a;
	//   const A ca;
	//   volatile A va;
	//   const volatile A cva;
	//   cva.bar();/*5*/
	//   va.bar();/*6*/
	//   ca.bar();/*7*/
	//   a.bar();/*8*/
	// }
	public void testMemberFunctionDisambiguationByCVness_238409() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		ICPPMethod bar_cv = bh.assertNonProblem("bar();/*1*/", 3, ICPPMethod.class);
		ICPPMethod bar_v = bh.assertNonProblem("bar();/*2*/", 3, ICPPMethod.class);
		ICPPMethod bar_c = bh.assertNonProblem("bar();/*3*/", 3, ICPPMethod.class);
		ICPPMethod bar = bh.assertNonProblem("bar();/*4*/", 3, ICPPMethod.class);
		ICPPFunctionType bar_cv_ft = bar_cv.getType();
		ICPPFunctionType bar_v_ft = bar_v.getType();
		ICPPFunctionType bar_c_ft = bar_c.getType();
		ICPPFunctionType bar_ft = bar.getType();

		assertTrue(bar_cv_ft.isConst());
		assertTrue(bar_cv_ft.isVolatile());
		assertTrue(!bar_v_ft.isConst());
		assertTrue(bar_v_ft.isVolatile());
		assertTrue(bar_c_ft.isConst());
		assertTrue(!bar_c_ft.isVolatile());
		assertTrue(!bar_ft.isConst());
		assertTrue(!bar_ft.isVolatile());

		bar_cv = bh.assertNonProblem("bar();/*5*/", 3, ICPPMethod.class);
		bar_v = bh.assertNonProblem("bar();/*6*/", 3, ICPPMethod.class);
		bar_c = bh.assertNonProblem("bar();/*7*/", 3, ICPPMethod.class);
		bar = bh.assertNonProblem("bar();/*8*/", 3, ICPPMethod.class);
		bar_cv_ft = bar_cv.getType();
		bar_v_ft = bar_v.getType();
		bar_c_ft = bar_c.getType();
		bar_ft = bar.getType();

		assertTrue(bar_cv_ft.isConst());
		assertTrue(bar_cv_ft.isVolatile());
		assertTrue(!bar_v_ft.isConst());
		assertTrue(bar_v_ft.isVolatile());
		assertTrue(bar_c_ft.isConst());
		assertTrue(!bar_c_ft.isVolatile());
		assertTrue(!bar_ft.isConst());
		assertTrue(!bar_ft.isVolatile());
	}

	//    void test1(float f);
	//    void test1(void);
	//    void blabla() {
	//    	test1(1);
	//    }
	//    enum E {e1};
	//    class C {};
	//    void test2(float f);
	//    void test2(C c);
	//    void blabla2() {
	//    	test2(e1);
	//    }
	public void testOverloadResolution_262191() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
	}

	//	enum E {e1};
	//	typedef int TInt;
	//	void select(int);
	//	void test() {
	//    int a= TInt(1);
	//    E e= E(0);
	//    void* h;
	//    select (int (h) + 1);
	//  }
	public void testSimpleTypeConstructorExpressions() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code);
	}

	//	struct A {};
	//	A a();
	//
	//	void f(const int& x);
	//
	//	void test() {
	//	  f(a());
	//	}
	public void testBug263152a() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertProblem("f(a())", 1);
	}

	//	struct A {};
	//	A a();
	//
	//	struct B {
	//	  void m(const A& x);
	//	  void m(const B& x);
	//	};
	//
	//	void test(B p) {
	//	  p.m(a());
	//	}
	public void testBug263152b() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertNonProblem("m(a())", 1, ICPPMethod.class);
	}

	//	struct A {
	//	  int a;
	//	  static void m() {
	//	    a = 0;
	//	  }
	//	};
	public void _testInstanceMemberInStaticMethod_263154() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertProblem("a =", 1);
	}

	//	struct A {};
	//	void test() {
	//		int B, b;
	//		while (A* a = 0);
	//		while (A* a = 0) {a= 0;}
	//		while (B* b) {b= 0;}
	//		if (A* a = 0) {a= 0;}
	//		if (B* b) {b= 0;}
	//		switch (A* a = 0) {case 1: a=0;}
	//		switch (B* b) {case1: b= 0;}
	//		for (;A* a = 0;) {a= 0;}
	//		for (;B* b;) {b= 0;}
	//	}
	public void testAmbiguityResolutionInCondition_263158() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper ba = new AST2AssertionHelper(code, true);
		ba.assertNonProblem("A*", 1, ICPPClassType.class);
		ba.assertNonProblem("a", 1, ICPPVariable.class);
		ba.assertNonProblem("B*", 1, ICPPVariable.class);

		parseAndCheckBindings(code, CPP);
	}

	//	typedef struct xx{} type;
	//	void test(void* ptr) {
	//		delete (type)(ptr);
	//	}
	public void testAmbiguityResolutionInDeleteExpression_428922() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper ba = new AST2AssertionHelper(code, true);
		ba.assertNonProblem("type)", 4, ITypedef.class);
		ba.assertNonProblem("ptr);", 3, ICPPVariable.class);

		IASTTranslationUnit tu = parseAndCheckBindings(code, CPP);
		ICPPASTFunctionDefinition test = getDeclaration(tu, 1);
		IASTExpressionStatement stmt = getStatement(test, 0);
		ICPPASTDeleteExpression dexpr = (ICPPASTDeleteExpression) stmt.getExpression();
		assertTrue(dexpr.getOperand() instanceof ICPPASTCastExpression);
	}

	//	void f(int x);
	//
	//	void test(int* p, const int* q, int r[], const int s[]) {
	//	  f(p);
	//	  f(q);
	//	  f(r);
	//	  f(s);
	//	}
	public void testPointerToNonPointerConversion_263159() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertProblem("f(p)", 1);
		ba.assertProblem("f(q)", 1);
		ba.assertProblem("f(r)", 1);
		ba.assertProblem("f(s)", 1);
	}

	//	void fip(int* x);
	//	void fia(int x[]);
	//
	//	void test() {
	//	  fip(1);
	//	  fia(1);
	//	  fip(0);
	//	  fia(0);
	//	}
	public void testNonPointerToPointerConversion_263707() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertProblem("fip(1)", 3);
		ba.assertProblem("fia(1)", 3);
		ba.assertNonProblem("fip(0)", 3, ICPPFunction.class);
		ba.assertNonProblem("fia(0)", 3, ICPPFunction.class);
	}

	//	class cl {};
	//	typedef cl tl;
	//	void reset(cl* ptr){};
	//	void blatest() {
	//	   reset(new tl());
	//	   reset(new cl());
	//     reset(new cl[1]);
	//	}
	public void testTypeOfNewExpression_264163() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code, CPP);
	}

	//	template<typename IteratorT> class range {
	//	public:
	//		template<class Range> range(const Range& r) {}
	//	};
	//	void onRange(const range<const char*>& r) {}
	//	void test() {
	//		range<char*> ir(0);
	//		onRange(ir);
	//	}
	public void testConstructorTemplateInImplicitConversion_264314() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper ba = new AST2AssertionHelper(code, true);
		ba.assertNonProblem("onRange(ir)", 7);
		parseAndCheckBindings(code, CPP);
	}

	//	typedef int * pi;
	//	typedef int *const* pcpi;
	//	typedef const pi* pcpi2;
	//	void check(pcpi) {};
	//	void testxxx() {
	//	        pcpi p1;
	//	        pcpi2 p2;
	//	        check(p1);
	//	        check(p2);
	//	}
	//  template<typename T> class CT {};
	//  CT<pcpi> ct1;
	//  CT<pcpi2> ct2;
	public void testConstTypedef_264474() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper ba = new AST2AssertionHelper(code, true);
		ba.assertNonProblem("check(p2)", 5);
		IBinding ct1 = ba.assertNonProblem("CT<pcpi>", 8);
		IBinding ct2 = ba.assertNonProblem("CT<pcpi2>", 9);
		assertSame(ct1, ct2);

		parseAndCheckBindings(code, CPP);
	}

	//	class X {
	//		public:
	//			int f;
	//	void m(int) {};
	//	void cm(int) const {};
	//	static int sf;
	//	static void sm(int) {};
	//	};
	//	int X::sf;
	//
	//	void mpr(int X::* p){}
	//	void mpr(void (X::* q)(int)){}
	//	void mprc(void (X::* q)(int) const){}
	//	void mprcp(void (X::** q)(int) const){}
	//	void pr(int * p){}
	//	void pr(void (*q)(int)){}
	//
	//	void testxxx() {
	//		void (X::* ptr)(int) const= &X::cm;
	//		mpr(&X::f);
	//		mpr(&X::m);
	//		mprc(&X::cm);
	//		mprcp(&ptr);
	//	    pr(&X::sf);
	//	    pr(&(X::sf));
	//	    pr(&X::sm);
	//	    pr(&(X::sm));
	//
	//		// invalid constructs:
	//		mpr(&(X::f));  // cannot use parenthesis
	//		mpr(&(X::m));   // cannot use parenthesis
	//		mpr(&X::sf);    // sf is static
	//		mpr(&X::sm);    // sm is static
	//		mpr(&X::cm);    // cm is const
	//		mprc(&X::m);    // m is not const
	//	}
	public void testMemberPtrs_264479() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper ba = new AST2AssertionHelper(code, true);
		ba.assertNonProblem("mpr(&X::f)", 3);
		ba.assertNonProblem("mpr(&X::m)", 3);
		ba.assertNonProblem("mprc(&X::cm)", 4);
		ba.assertNonProblem("mprcp(&ptr)", 5);
		ba.assertNonProblem("pr(&X::sf)", 2);
		ba.assertNonProblem("pr(&(X::sf))", 2);
		ba.assertNonProblem("pr(&X::sm)", 2);
		ba.assertNonProblem("pr(&(X::sm))", 2);

		ba.assertProblem("mpr(&(X::f))", 3);
		ba.assertProblem("mpr(&(X::m))", 3);
		ba.assertProblem("mpr(&X::sf)", 3);
		ba.assertProblem("mpr(&X::sm)", 3);
		ba.assertProblem("mpr(&X::cm)", 3);
		ba.assertProblem("mprc(&X::m)", 4);
	}

	//	void f(int x);
	//
	//	void test(int* p) {
	//	  f(!p);
	//	}
	public void testTypeOfNotExpression_265779() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertNonProblem("f(!p)", 1);
	}

	//	template<typename T>
	//	struct A {
	//	  enum {
	//	    e1 = 0,
	//	    e2 = int(e1)
	//	  };
	//	};
	//
	//	template<typename T, int v = A<T>::e2>
	//	struct B;
	//
	//	template<typename T>
	//	struct B<T, 0> {
	//	  void waldo();
	//	};
	//
	//	void test(B<int>& p) {
	//	  p.waldo();
	//	}
	public void testDependentEnumeration_446711a() throws Exception {
		parseAndCheckBindings();
	}

	//	constexpr int f(int p) { return p; }
	//	constexpr long f(long p) { return p + 0x100000000L; }
	//
	//	template<typename T>
	//	struct A {
	//	  enum {
	//	    e1 = 0,
	//	    e2 = f(e1)
	//	  };
	//	};
	//
	//	template<typename T, long v = A<T>::e2>
	//	struct B;
	//
	//	template<typename T>
	//	struct B<T, 0> {
	//	  void waldo();
	//	};
	//
	//	void test(B<int>& p) {
	//	  p.waldo();
	//	}
	public void testDependentEnumeration_446711b() throws Exception {
		parseAndCheckBindings();
	}

	//	constexpr int f(long p) { return p; }
	//	constexpr long f(int p) { return p + 0x100000000L; }
	//
	//	template<typename T>
	//	struct A {
	//	  enum {
	//	    e1 = 0L,
	//	    e2 = f(e1)
	//	  };
	//	};
	//
	//	template<typename T, long v = A<T>::e2>
	//	struct B;
	//
	//	template<typename T>
	//	struct B<T, 0> {
	//	  void waldo();
	//	};
	//
	//	void test(B<int>& p) {
	//	  p.waldo();
	//	}
	public void testDependentEnumeration_446711c() throws Exception {
		parseAndCheckBindings();
	}

	//	constexpr int f(int p) { return p; }
	//	constexpr long f(long p) { return p + 0x100000000L; }
	//
	//	template<typename T, T v>
	//	struct A {
	//	  enum {
	//	    e1 = v,
	//	    e2 = f(e1)
	//	  };
	//	};
	//
	//	template<typename T, T u, long v = A<T, u>::e2>
	//	struct B;
	//
	//	template<typename T, T u>
	//	struct B<T, u, 0> {
	//	  void waldo();
	//	};
	//
	//	void test(B<int, 0>& p) {
	//	  p.waldo();
	//	}
	public void testDependentEnumeration_446711d() throws Exception {
		parseAndCheckBindings();
	}

	//	constexpr int f(int p) { return p; }
	//	constexpr long f(long p) { return p + 0x100000000L; }
	//
	//	template<typename T, T v>
	//	struct A {
	//	  enum {
	//	    e1 = v,
	//	    e2 = f(e1)
	//	  };
	//	};
	//
	//	template<typename T, T u, long v = A<T, u>::e2>
	//	struct B {};
	//
	//	template<typename T, T u>
	//	struct B<T, u, 0> {
	//	  void waldo();
	//	};
	//
	//	void test(B<long, 0>& p) {
	//	  p.waldo();
	//	}
	public void testDependentEnumeration_446711e() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertProblemOnFirstIdentifier(".waldo()");
	}

	//	class S {
	//	  S(int);
	//	};
	//	void test() {
	//	  S **temp = new S*[1];
	//	  temp = new S*;
	//	  temp = new (S*);
	//	  temp = new ::S*[1];
	//	  temp = new ::S*;
	//	  temp = new (::S*);
	//	}
	public void testNewPointerOfClass_267168() throws Exception {
		parseAndCheckBindings();
	}

	//	void f(char *(names[2])= 0);
	//  void f2(const char *(n[])) {
	//    if (n && 1){}
	//  }
	public void testPointerToArrayWithDefaultVal_267184() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper ba = new AST2AssertionHelper(code, true);
		ICPPParameter p = ba.assertNonProblem("names", 5);
		assertTrue(p.hasDefaultValue());
		IType t = p.getType();
		assertInstance(t, IPointerType.class); // parameter of type array is converted to pointer
		t = ((IPointerType) t).getType();
		assertInstance(t, IPointerType.class);
		t = ((IPointerType) t).getType();
		assertInstance(t, IBasicType.class);

		parseAndCheckBindings(code, CPP);
	}

	//	class X {
	//	  virtual void pv() = 0;
	//	  void (*ptrToFunc) ()= 0;
	//	};
	public void testPureVirtualVsInitDeclarator_267184() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parseAndCheckBindings(code, CPP);
		ICPPASTCompositeTypeSpecifier ct = getCompositeType(tu, 0);
		IASTSimpleDeclaration sdecl = getDeclaration(ct, 0);
		ICPPASTFunctionDeclarator dtor = (ICPPASTFunctionDeclarator) sdecl.getDeclarators()[0];
		assertTrue(dtor.isPureVirtual());
		assertNull(dtor.getInitializer());

		sdecl = getDeclaration(ct, 1);
		dtor = (ICPPASTFunctionDeclarator) sdecl.getDeclarators()[0];
		assertFalse(dtor.isPureVirtual());
		assertNotNull(dtor.getInitializer());

		parseAndCheckBindings(code, CPP);
	}

	//	namespace ns {
	//		struct S {
	//			int a;
	//		};
	//	}
	//	class A {
	//		public:
	//			operator ns::S*(){return 0;};
	//	};
	//	namespace ns {
	//		void bla() {
	//			A a;
	//			a.operator S *();
	//		}
	//	}
	public void testLookupScopeForConversionNames_267221() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code, CPP);
	}

	//	struct A {int a;};
	//
	//	int foo();
	//	int bar(A);
	//
	//	int func() {
	//		bar((A){foo()});
	//	}
	public void testBug268714() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code, CPP);
	}

	//	void f(int volatile * const *) {}
	//	void f(int const volatile * const *) {}
	//	void test() {
	//	   int ** x;
	//	   f(x);
	//	}
	public void testRankingOfQualificationConversion_269321() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code, CPP);
	}

	//	struct A { int a; };
	//	struct B { int b; };
	//
	//	struct X {
	//	    A operator+(int);
	//	    X(int);
	//	};
	//
	//	A operator+(X,X);
	//	B operator+(X,double);
	//
	//	void test(X x) {
	//	    (x + 1).a; //1
	//	    (1 + x).a; //2
	//	    (x + 1.0).b; //3
	//	}
	public void testOverloadResolutionForOperators_266211() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertNonProblem("a; //1", 1, ICPPField.class);
		ba.assertNonProblem("a; //2", 1, ICPPField.class);
		ba.assertNonProblem("b; //3", 1, ICPPField.class);
	}

	//	struct A { int a; };
	//	struct X {
	//	    A operator+(X);
	//	    void m();
	//	};
	//
	//	A operator+(X,double);
	//
	//	void X::m() {
	//	    X x;
	//	    (x + x).a; //1
	//	    (x + 1.0).a; //2
	//	}
	public void testOverloadResolutionForOperators_268534() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertNonProblem("a; //1", 1, ICPPField.class);
		ba.assertNonProblem("a; //2", 1, ICPPField.class);
	}

	//	class C {
	//		operator int(){return 0;}
	//	};
	//	void test(int) {}
	//	void ref() {
	//		C* c= 0;
	//		test(c);
	//	}
	public void testInvalidUserDefinedConversion_269729() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertProblem("test(c)", 4);
	}

	//	int foo(char * x);
	//	int foo(wchar_t * x);
	//  int foo(char x);
	//	int foo(wchar_t x);
	//
	//	int main() {
	//		foo("asdf");
	//		foo(L"asdf");
	//		foo('a');
	//		foo(L'a');
	//	}
	public void testWideCharacterLiteralTypes_270892() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		assertSame(col.getName(0).resolveBinding(), col.getName(9).resolveBinding());
		assertSame(col.getName(2).resolveBinding(), col.getName(10).resolveBinding());
		assertSame(col.getName(4).resolveBinding(), col.getName(11).resolveBinding());
		assertSame(col.getName(6).resolveBinding(), col.getName(12).resolveBinding());
	}

	// auto L = L"";
	// auto u8 = u8"";
	// auto u = u"";
	// auto U = U"";
	public void testStringLiteralPrefixTypes_526724() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);
		NameCollector col = new NameCollector();
		tu.accept(col);

		IType actual_L = ((IVariable) col.getName(0).resolveBinding()).getType();
		IType actual_u8 = ((IVariable) col.getName(1).resolveBinding()).getType();
		IType actual_u = ((IVariable) col.getName(2).resolveBinding()).getType();
		IType actual_U = ((IVariable) col.getName(3).resolveBinding()).getType();

		IType eWChar = createStringType(Kind.eWChar);
		IType eChar = createStringType(Kind.eChar);
		IType eChar16 = createStringType(Kind.eChar16);
		IType eChar32 = createStringType(Kind.eChar32);

		assertSameType(actual_L, eWChar);
		assertSameType(actual_u8, eChar);
		assertSameType(actual_u, eChar16);
		assertSameType(actual_U, eChar32);
	}

	protected IType createStringType(Kind kind) {
		IType type = new CPPBasicType(kind, 0);
		type = new CPPQualifierType(type, true, false);
		return new CPPPointerType(type);
	}

	//	namespace ns {
	//	  struct A {};
	//	}
	//	using ns::A;
	//	struct A;
	//	A a;
	public void testForwardDeclarationAfterUsing_271236() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertNonProblem("A a;", 1, ICPPClassType.class);
	}

	//	template <class T> class Moo;
	//	bool getFile(Moo <class Foo> & res);
	public void testScopeOfClassFwdDecl_270831() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ICPPClassType t = ba.assertNonProblem("Foo", 3, ICPPClassType.class);
		IScope scope = t.getScope();
		assertEquals(EScopeKind.eGlobal, scope.getKind());
	}

	//	class C {};
	//	class D : public C {};
	//	class E {
	//	   public: E(D) {}
	//	};
	//	void test(C c) {}
	//	void test(E e) {}
	//
	//	void xx() {
	//	   D d1;
	//	   const D d2= D();
	//	   test(d1); // test(C c) has to be selected
	//	   test(d2); // test(C c) has to be selected
	//	}
	public void testDerivedToBaseConversion_269318() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper ba = new AST2AssertionHelper(code, true);
		ICPPFunction t = ba.assertNonProblem("test(d1);", 4, ICPPFunction.class);
		ICPPClassType ct = (ICPPClassType) t.getParameters()[0].getType();
		assertEquals("C", ct.getName());

		t = ba.assertNonProblem("test(d2);", 4, ICPPFunction.class);
		ct = (ICPPClassType) t.getParameters()[0].getType();
		assertEquals("C", ct.getName());

		parseAndCheckBindings(code, CPP);
	}

	//	int foo(int x);
	//	int bar(int x);
	//
	//	typedef int (*fp)(int);
	//	struct A {
	//	        operator fp() { return foo; }
	//	} a;
	//
	//	int i = bar(a(1));
	public void testCallToObjectOfClassType_267389() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code, CPP);
	}

	// typedef int T;
	// class C {
	//    C(T);  // ctor
	//    C(s);  // instance s;
	// };
	public void testDeclarationAmbiguity_269953() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parseAndCheckBindings(code, CPP);
		ICPPASTCompositeTypeSpecifier ct = getCompositeType(tu, 1);
		ICPPClassType c = (ICPPClassType) ct.getName().resolveBinding();

		ICPPMethod[] methods = c.getDeclaredMethods();
		assertEquals(1, methods.length);
		assertEquals("C", methods[0].getName());

		ICPPField[] fields = c.getDeclaredFields();
		assertEquals(1, fields.length);
		assertEquals("s", fields[0].getName());
	}

	//	class C3 {
	//		C3(int);
	//	};
	//	typedef C3 T3;
	//	T3::C3(int) {
	//	}
	public void testCtorWithTypedef_269953() throws Exception {
		parseAndCheckBindings();
	}

	//	template<class T> class Compare {
	//	    Compare();
	//	    ~Compare();
	//	    bool check;
	//	};
	//	typedef Compare<int> MY_COMPARE;
	//	template<> MY_COMPARE::Compare() {}
	public void testTemplateCTorWithTypedef_269953() throws Exception {
		parseAndCheckBindings();
	}

	//	class IBase {
	//		public:
	//			virtual void base() = 0;
	//	};
	//
	//	class IDerived : virtual public IBase {
	//		public:
	//			virtual void derived() = 0;
	//	};
	//
	//	class BaseImplHelper : virtual public IBase {
	//		public:
	//			virtual void base() {}
	//	};
	//
	//	class Derived : virtual public IDerived, public BaseImplHelper {
	//		public:
	//			virtual void derived() {}
	//	};
	//
	//	int main() {
	//		Derived d;
	//		d.base(); // Parser log reports ambiguity on 'base'
	//		return 0;
	//	}
	public void testHiddenVirtualBase_282993() throws Exception {
		parseAndCheckBindings();
	}

	//	class C {
	//	  C& operator()() { return *this; }
	//	};
	//	void test() {
	//	  C c;
	//	  c()()()()()()()()()()()()()();
	//	}
	public void testNestedOverloadedFunctionCalls_283324() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parseAndCheckBindings(code, CPP);
		IASTFunctionDefinition test = getDeclaration(tu, 1);
		IASTExpressionStatement stmt = getStatement(test, 1);
		long now = System.currentTimeMillis();
		IType t = stmt.getExpression().getExpressionType();
		assertInstance(t, ICPPClassType.class);
		assertTrue(stmt.getExpression().isLValue());
		final long time = System.currentTimeMillis() - now;
		assertTrue("Lasted " + time + "ms", time < 5000);
	}

	//	class A {
	//	  friend inline void m(A p) {}
	//	};
	//
	//	void test(A a) {
	//	  m(a);
	//	}
	public void testInlineFriendFunction_284690() throws Exception {
		parseAndCheckBindings();
	}

	//	void f(int t);
	//	void f(unsigned int t);
	//	void f(long t);
	//
	//	enum IntEnum { i1 };
	//	enum UnsignedEnum { u1 = 0x7FFFFFFF, u2 };
	//	enum LongEnum { l1 = -1, l2 = 0x7FFFFFFF, l3 };
	//
	//	void test() {
	//	  f(i1);
	//	  f(u1);
	//	  f(l1);
	//	}
	public void testEnumToIntConversion_285368() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ICPPFunction f1 = ba.assertNonProblem("f(i1)", 1, ICPPFunction.class);
		IType t1 = f1.getType().getParameterTypes()[0];
		assertTrue(t1 instanceof ICPPBasicType);
		assertEquals(IBasicType.Kind.eInt, ((ICPPBasicType) t1).getKind());
		assertEquals(0, ((ICPPBasicType) t1).getModifiers());
		ICPPFunction f2 = ba.assertNonProblem("f(u1)", 1, ICPPFunction.class);
		IType t2 = f2.getType().getParameterTypes()[0];
		assertTrue(t2 instanceof ICPPBasicType);
		assertEquals(IBasicType.Kind.eInt, ((ICPPBasicType) t2).getKind());
		assertEquals(IBasicType.IS_UNSIGNED, ((ICPPBasicType) t2).getModifiers());
		ICPPFunction f3 = ba.assertNonProblem("f(l1)", 1, ICPPFunction.class);
		IType t3 = f3.getType().getParameterTypes()[0];
		assertTrue(t3 instanceof ICPPBasicType);
		assertEquals(IBasicType.Kind.eInt, ((ICPPBasicType) t3).getKind());
		assertEquals(IBasicType.IS_LONG, ((ICPPBasicType) t3).getModifiers());
	}

	//	void f(int t);
	//	void f(long t);
	//
	//	enum e {
	//	  i1 = 0L,
	//	  i2 = (long) i1 + 1,
	//	  i3 = long(i2 + 1)
	//	};
	//
	//	void test() {
	//	  f(i3);
	//	}
	public void testCastInEnumeratorValue_446380() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		IEnumerator i2 = ba.assertNonProblem("i2", IEnumerator.class);
		Number v2 = i2.getValue().numberValue();
		assertNotNull(v2);
		assertEquals(1, v2.intValue());
		IEnumerator i3 = ba.assertNonProblem("i3", IEnumerator.class);
		Number v3 = i3.getValue().numberValue();
		assertNotNull(v3);
		assertEquals(2, v3.intValue());
		ICPPFunction f = ba.assertNonProblemOnFirstIdentifier("f(i3)", ICPPFunction.class);
		IType t = f.getType().getParameterTypes()[0];
		// The declared types of the enum values don't affect the underlying type of the enum,
		// only the values themselves do.
		assertEquals("int", ASTTypeUtil.getType(t));
	}

	//	typedef enum enum_name enum_name;
	public void testTypedefRecursion_285457() throws Exception {
		BindingAssertionHelper ba = getAssertionHelper();
		ba.assertProblem("enum_name", 9);
	}

	//	struct MyStruct {
	//	  enum MyEnum {};
	//	  MyStruct(MyEnum value) {}
	//	};
	public void testEnumRedefinitionInStruct_385144() throws Exception {
		parseAndCheckBindings();
	}

	//	class CL {
	//	  typedef int x;
	//	  friend void test() {
	//	    x a;
	//	  }
	//	};
	public void testLookupFromInlineFriend_284690() throws Exception {
		parseAndCheckBindings();
	}

	//	class XInterface {};
	//	class OWeakObject : public XInterface{};
	//	class XTypeProvider : public XInterface{};
	//	class WeakImplHelper1 : public OWeakObject, public XTypeProvider {};
	//	class PropertyValueList : public WeakImplHelper1 {
	//	public:
	//	  void hello();
	//	};
	//
	//	XInterface temp; // No problem
	//	void  PropertyValueList::hello() {
	//	  XInterface temp;
	//	}
	public void testTypeLookupWithMultipleInheritance_286213() throws Exception {
		parseAndCheckBindings();
	}

	//	template<class>
	//	struct ContainerOf {
	//		int numParts;
	//	};
	//
	//	struct HumanPart;
	//	struct RobotPart;
	//
	//	struct BionicMan : ContainerOf<HumanPart>, ContainerOf<RobotPart> {
	//		int numRoboParts() {
	//			return ContainerOf<RobotPart>::numParts;
	//		}
	//	};
	public void testInheritanceFromMultipleInstancesOfSameTemplate_383502() throws Exception {
		parseAndCheckBindings();
	}

	//	int v1;
	//	static int v2;
	//	extern int v3;
	//	int v4= 12;
	//	static int v5= 1;
	//	class X {
	//		int v6;
	//		static const int v7;
	//		static const int v8= 1;
	//	};
	//	const int X::v7= 1;
	public void testVariableDefVsDecl_286259() throws Exception {
		String[] declNames = { "v3" };
		String[] defNames = { "v1", "v2", "v4", "v5", "X::v7" };
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment(), CPP);
		checkDeclDef(declNames, defNames, tu.getDeclarations());

		declNames = new String[] { "v7", "v8" };
		defNames = new String[] { "v6" };
		IASTCompositeTypeSpecifier cls = getCompositeType(tu, 5);
		checkDeclDef(declNames, defNames, cls.getMembers());
	}

	//	extern "C" int v1;
	//	class X {
	//		static const int v2= 1;
	//	};
	//	const int X::v2;
	public void testVariableDefVsDecl_292635() throws Exception {
		String[] declNames = { "v1" };
		String[] defNames = { "X::v2" };
		IASTTranslationUnit tu = parseAndCheckBindings(getAboveComment(), CPP);
		checkDeclDef(declNames, defNames, tu.getDeclarations());

		declNames = new String[] { "v2" };
		defNames = new String[] {};
		IASTCompositeTypeSpecifier cls = getCompositeType(tu, 1);
		checkDeclDef(declNames, defNames, cls.getMembers());
	}

	//	class X {
	//	    struct S* m1;
	//	    struct T;
	//	    struct T* m2;
	//	};
	public void testStructOwner_290693() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code, CPP);

		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		ICPPClassType S = bh.assertNonProblem("S*", 1);
		assertNull(S.getOwner());

		ICPPClassType X = bh.assertNonProblem("X {", 1);
		ICPPClassType T = bh.assertNonProblem("T;", 1);
		assertSame(X, T.getOwner());

		T = bh.assertNonProblem("T* m2", 1);
		assertSame(X, T.getOwner());
	}

	//	class ULONGLONG {
	//	public :
	//	   ULONGLONG (unsigned long long val) {}
	//	   friend ULONGLONG operator ~ (const ULONGLONG &) { return 0; }
	//	};
	//
	//	int main() {
	//	    return ~0;
	//	}
	public void testNonUserdefinedOperator_291409b() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parseAndCheckBindings(code, CPP);
		IASTFunctionDefinition def = getDeclaration(tu, 1);
		IASTReturnStatement rstmt = getStatement(def, 0);
		IASTImplicitNameOwner expr = (IASTImplicitNameOwner) rstmt.getReturnValue();
		assertEquals(0, expr.getImplicitNames().length);
	}

	//	class Test {
	//	   template <int T> void t() {}
	//	   inline void t();
	//	};
	//
	//	inline void Test::t() {
	//	   t<1>();
	//	}
	public void testMethodTemplateWithSameName_292051() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code, CPP);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		ICPPMethod m = bh.assertNonProblem("t<1>", 1);
		assertTrue(m.isInline());
	}

	//	class B {};
	//	class A {
	//	public:
	//		A() {};
	//		A(B c) {};
	//	};
	//
	//	class C : B {
	//	public:
	//		operator A() {}
	//	};
	//	void foo(A a) {}
	//	void test() {
	//		C c= *new C();
	//		foo(c);
	//	}
	public void testUserDefinedConversion_222444a() throws Exception {
		parseAndCheckBindings();
	}

	//	class From {};
	//	class To1 {
	//	public:
	//		To1(...) {}
	//	};
	//	class To2 {
	//	public:
	//		To2(From f) {}
	//	};
	//	class To3 {
	//	public:
	//		To3(From f, int a=0) {}
	//	};
	//
	//	void x1(To1 t) {}
	//	void x2(To2 t) {}
	//	void x3(To3 t) {}
	//	void test() {
	//		From f;
	//		x1(f);
	//		x2(f);
	//		x3(f);
	//	}
	public void testUserDefinedConversion_222444b() throws Exception {
		parseAndCheckBindings();
	}

	//	class A {};
	//	class B : public A {};
	//
	//	class C {
	//	public:
	//		operator const B() { return *new B();}
	//	};
	//
	//	void foo(A a) {}
	//
	//	void refs() {
	//		C c= *new C();
	//		const C cc= *new C();
	//		foo(c);
	//      foo(cc);
	//	}
	public void testUserDefinedConversion_222444c() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		bh.assertNonProblem("foo(c);", 3);
		bh.assertProblem("foo(cc);", 3);
	}

	//	class ULONGLONG {
	//  public :
	//	   ULONGLONG (unsigned long long val) {}
	//	   friend bool operator == (const ULONGLONG & , const int) { return true; }
	//	};
	//	enum E {A, B, C};
	//	int main() {
	//		return B == 23;
	//	}
	public void testInvalidOverload_291409() throws Exception {
		final String code = getAboveComment();
		IASTTranslationUnit tu = parseAndCheckBindings(code, CPP);
		IASTFunctionDefinition fdef = getDeclaration(tu, 2);
		IASTReturnStatement stmt = getStatement(fdef, 0);
		IASTImplicitNameOwner no = (IASTImplicitNameOwner) stmt.getReturnValue();
		assertEquals(0, no.getImplicitNames().length);
	}

	//	typedef int Fixed_Array[20];
	//	void f(const ::Fixed_Array message) {}
	//
	//	void test() {
	//		int* ptr;
	//		f(ptr);
	//	}
	public void testParameterAdjustment_293538() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code, CPP);
	}

	//	template <typename T> struct CT {
	//		CT(int) {}
	//	};
	//	namespace ns {
	//		typedef int B;
	//		struct A : CT<B>{
	//			A();
	//		};
	//	}
	//
	//	ns::A::A() : CT<B>(1) {
	//	}
	public void testLookupInConstructorChainInitializer_293566() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		bh.assertNonProblem("B>(1)", 1);
		parseAndCheckBindings(code, CPP);
	}

	//	namespace ns {
	//		enum E { e1, e2 };
	//		E operator|(E __a, E __b) { return e1; }
	//	}
	//
	//	void f(ns::E e) {}
	//	void test() {
	//		f(ns::e1);
	//		f(ns::e1 | ns::e2);  // the operator| needs to be looked up in the
	//	                             // associated namespace.
	//	}
	public void testAssociatedScopesForOverloadedOperators_293589() throws Exception {
		parseAndCheckBindings();
	}

	// typedef int F(int);
	// template<typename T> F functionTemplate;
	// class C {
	//   F method;
	//   friend F friendFunction;
	//   template<typename T> F methodTemplate;
	// };
	public void testFunctionDeclViaTypedef_86495() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code, CPP);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		ICPPFunctionTemplate template = bh.assertNonProblem("functionTemplate", 16);
		assertNotNull(template.getType());
		assertEquals(1, template.getParameters().length);

		ICPPMethod method = bh.assertNonProblem("method", 6);
		assertNotNull(method.getType());
		assertEquals(1, method.getParameters().length);

		ICPPFunction friendFunction = bh.assertNonProblem("friendFunction", 14);
		assertNotNull(friendFunction.getType());
		assertEquals(1, friendFunction.getParameters().length);

		ICPPMethod methodTemplate = bh.assertNonProblem("methodTemplate", 14);
		assertTrue(methodTemplate instanceof ICPPFunctionTemplate);
		assertNotNull(methodTemplate.getType());
		assertEquals(1, methodTemplate.getParameters().length);
	}

	//	void x(const char (&)[1]) {};
	//
	//	typedef char Array[12];
	//	typedef const char CArray[12];
	//	void xx(CArray* p);
	//
	//  template <typename T> void f(T& p);
	//	void test() {
	//	  x("");
	//	  Array a;
	//    xx(&a);
	//	  f("");
	//	}
	public void testCVQualifiersWithArrays_293982() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T, int N> char (&func(T (&array)[N]))[N];
	//	void f(const int (&a)[1]) {}
	//	typedef int TA[];
	//	TA a = { 0 };
	//	int b[1];
	//
	//	void test() {
	//	  f(a); func(a);
	//	  f(b); func(b);
	//	}
	public void testArrayTypeSizeFromInitializer_294144() throws Exception {
		parseAndCheckBindings();
	}

	//	struct A {
	//		template<typename T> void m() {
	//			C<T> c;
	//		}
	//		template<typename T> struct C {};
	//	};
	public void testLookupInClassScopeForTemplateIDs_294904() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T, int N> char (&func(T (&array)[N]))[N];
	//	struct A {
	//	  static int array[];
	//	};
	//	int A::array[] = { 0 };
	//
	//	void test() {
	//	  func(A::array);
	//	}
	public void testCompleteArrayTypeWithIncompleteDeclaration_294144() throws Exception {
		parseAndCheckBindings();
	}

	//	void test() {
	//		int x,y;
	//		return y < x ? -1 : y > x ? 1 : 0;
	//	}
	public void testSyntax1_295064() throws Exception {
		parseAndCheckBindings();
	}

	//	typedef int TInt;
	//	template <typename T> class CT {
	//		CT(TInt);
	//	};
	//	template <typename T> inline CT<T>::CT(TInt) {}
	public void testSyntax2_295064() throws Exception {
		parseAndCheckBindings();
	}

	//	const unsigned int EIGHT= 8;
	//	static_assert(sizeof(long) >= EIGHT, "no 64-bit support");
	//	namespace ns {
	//	   static_assert(sizeof(long) >= 4, "no 32-bit support");
	//	}
	//	template <typename T> class basic_string {
	//		static_assert(T::value, "bla");
	//	};
	//	void do_something() {
	//		struct VMPage {
	//		};
	//		static_assert(sizeof(VMPage) == 1, "bla");
	//	}
	public void testStaticAssertions_294730() throws Exception {
		parseAndCheckBindings();
	}

	//	struct A {};
	//
	//	void foo(const A&);  // #1
	//	void foo(A&&);       // #2
	//
	//	A   source_rvalue();
	//	A&  source_ref();
	//	A&& source_rvalue_ref();
	//
	//	const A   source_const_rvalue();
	//	const A&  source_const_ref();
	//	const A&& source_const_rvalue_ref();
	//
	//	int main() {
	//	    A a;
	//	    A&  ra = a;
	//	    A&& rra = a;
	//	    const A ca;
	//	    const A& rca = ca;
	//	    const A&& rrca = ca;
	//
	//	    foo(a);      // #1
	//	    foo(ra);     // #1
	//	    foo(rra);    // #1
	//	    foo(ca);     // #1
	//	    foo(rca);    // #1
	//	    foo(rrca);   // #1
	//	    foo(source_rvalue());     // #2
	//	    foo(source_ref());        // #1
	//	    foo(source_rvalue_ref()); // #2
	//	    foo(source_const_rvalue());     // #1
	//	    foo(source_const_ref());        // #1
	//	    foo(source_const_rvalue_ref()); // #1
	//	}
	public void testRValueReference_294730() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IBinding foo1 = bh.assertNonProblem("foo(const A&)", 3);
		IBinding foo2 = bh.assertNonProblem("foo(A&&)", 3);
		IBinding b;
		b = bh.assertNonProblem("foo(a)", 3);
		assertSame(b, foo1);
		b = bh.assertNonProblem("foo(ra)", 3);
		assertSame(b, foo1);
		b = bh.assertNonProblem("foo(rra)", 3);
		assertSame(b, foo1);
		b = bh.assertNonProblem("foo(ca)", 3);
		assertSame(b, foo1);
		b = bh.assertNonProblem("foo(rca)", 3);
		assertSame(b, foo1);
		b = bh.assertNonProblem("foo(rrca)", 3);
		assertSame(b, foo1);
		b = bh.assertNonProblem("foo(source_rvalue())", 3);
		assertSame(b, foo2);
		b = bh.assertNonProblem("foo(source_ref())", 3);
		assertSame(b, foo1);
		b = bh.assertNonProblem("foo(source_rvalue_ref())", 3);
		assertSame(b, foo2);
		b = bh.assertNonProblem("foo(source_const_rvalue())", 3);
		assertSame(b, foo1);
		b = bh.assertNonProblem("foo(source_const_ref())", 3);
		assertSame(b, foo1);
		b = bh.assertNonProblem("foo(source_const_rvalue_ref())", 3);
		assertSame(b, foo1);
	}

	//	int i;
	//	typedef int&  LRI;
	//	typedef int&& RRI;
	//	LRI& r1 = i;            // r1 has the type int&
	//	const LRI& r2 = i;      // r2 has the type int&
	//	const LRI&& r3 = i;     // r3 has the type int&
	//	RRI& r4 = i;            // r4 has the type int&
	//	RRI&& r5 = i;           // r5 has the type int&&
	public void testRValueReferenceTypedefs_294730() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IVariable v;
		v = bh.assertNonProblem("r1", 2);
		assertEquals("int &", ASTTypeUtil.getType(v.getType()));
		v = bh.assertNonProblem("r2", 2);
		assertEquals("int &", ASTTypeUtil.getType(v.getType()));
		v = bh.assertNonProblem("r3", 2);
		assertEquals("int &", ASTTypeUtil.getType(v.getType()));
		v = bh.assertNonProblem("r4", 2);
		assertEquals("int &", ASTTypeUtil.getType(v.getType()));
		v = bh.assertNonProblem("r5", 2);
		assertEquals("int &&", ASTTypeUtil.getType(v.getType()));
	}

	//  void dref(double&);
	//  void drref(double&&);
	//  void cdref(const double&);
	//	struct A { };
	//	struct B : A { };
	// 	extern B f();
	//	struct X {
	//		operator B();
	//	} x;
	//  void aref(A&);
	//  void caref(const A&);
	//  void carref(const A&&);
	//  void test() {
	//    B b;
	//	  double d = 2.0;
	//	  const volatile double cvd = 1;
	//	  int i = 2;
	//	  dref(d);
	//	  cdref(d);
	//    aref(b);
	//    caref(b);
	//	  dref(2.0); // error: not an lvalue and reference not const
	//	  dref(i);   // error: type mismatch and reference not const
	//	  drref(i);  // bound to temporary double object
	//    caref(f());  // bound to the A subobject of the B rvalue.
	//    carref(f()); // same as above
	//    caref(x);    // bound to the A subobject of the result of the conversion
	//	  cdref(2); // rcd2 refers to temporary with value 2.0
	//	  drref(2);  // rcd3 refers to temporary with value 2.0
	//	  cdref(cvd); // error: type qualifiers dropped
	// }
	public void testDirectBinding_294730() throws Exception {
		final String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		bh.assertNonProblem("dref(d)", 4);
		bh.assertNonProblem("cdref(d)", 5);
		bh.assertNonProblem("aref(b)", 4);
		bh.assertNonProblem("caref(b)", 5);
		bh.assertProblem("dref(2.0)", 4);
		bh.assertProblem("dref(i)", 4);
		bh.assertNonProblem("drref(i)", 5);
		bh.assertNonProblem("caref(f())", 5);
		bh.assertNonProblem("carref(f())", 6);
		bh.assertNonProblem("caref(x)", 5);
		bh.assertNonProblem("cdref(2)", 5);
		bh.assertNonProblem("drref(2)", 5);
		bh.assertProblem("cdref(cvd)", 5);
	}

	//	struct S {
	//		operator int() {return 0;}
	//	};
	//	S s(){return *new S();}
	//	void test(int) {
	//		test(s());
	//	}
	public void testSpecialRuleForImplicitObjectType_294730() throws Exception {
		final String code = getAboveComment();
		parseAndCheckBindings(code, CPP);
	}

	//	template <class T, class X> struct enable_if_same {};
	//	template <class X> struct enable_if_same<X, X> {
	//	    typedef char type;
	//	};
	//	struct X {
	//	    X();
	//	    X(X& rhs);
	//	    template <class T> X(T& rhs, typename enable_if_same<X const,T>::type = 0);
	//	    X& operator=(X rhs);
	//
	//	    struct ref { ref(X*p) : p(p) {} X* p; };
	//	    operator ref();
	//	    X(ref rhs);
	//	};
	//
	//	X source();
	//	X const csource();
	//	void sink(X);
	//	void sink2(X&);
	//	void sink2(X const&);
	//	void sink3(X&);
	//	template <class T> void tsink(T);
	//
	//	void test() {
	//	    X z2((X()));
	//	    X z4 = X();
	//	    X z5 = z4;
	//	    X z6(z4);
	//	    X const z7;
	//	    X z8 = z7;
	//	    X z9(z7);
	//	    sink(source());
	//	    sink(csource());
	//	    sink2(source());
	//	    sink2(csource());
	//	    sink(z5);
	//	    sink(z7);
	//	    sink2(z4);
	//	    sink2(z7);
	//	    tsink(source());
	//	    typedef X const XC;
	//	    sink2(XC(X()));
	//	    z4 = z5;
	//	    z4 = source();

	//	    // Expected failures
	//	    sink3(source());
	//	    sink3(csource());
	//	    sink3(z7);

	// }
	public void testInitOfClassObjectsByRValues_294730() throws Exception {
		final CharSequence[] contents = getContents(3);
		final String code = contents[0].toString();
		final String end = contents[2].toString();
		parseAndCheckBindings(code + end, CPP);
		BindingAssertionHelper bh = new AST2AssertionHelper(code + contents[1] + end, true);
		bh.assertProblem("sink3(source())", 5);
		bh.assertProblem("sink3(csource())", 5);
		bh.assertProblem("sink3(z7)", 5);
	}

	//	struct A {};
	//	namespace ns {
	//	    struct C {
	//	    	int operator+(int);
	//	    };
	//	    int operator+(const C&, const A&);
	//	}
	//
	//	void check(int);
	//	int main(int argc, char** argv) {
	//		A a;
	//		ns::C c;
	//		check(c+1);
	//		check(c+a);
	//	}
	public void testADLForOperators_296906() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code, CPP);
	}

	//	namespace ns {
	//		struct A{};
	//		void f(ns::A, char) {}
	//	}
	//	void f(ns::A, int) {}
	//
	//	ns::A a;
	//	void test() {
	//		f(a, '1');  // calls ns::f(ns::A, char)
	//		f(a, 1);  // calls ns::f(ns::A, char)
	//	}
	public void testADL_299101() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code, CPP);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IFunction inns = bh.assertNonProblem("f(ns::A, char)", 1);
		IFunction glob = bh.assertNonProblem("f(ns::A, int)", 1);

		IBinding b = bh.assertNonProblem("f(a, '1')", 1);
		assertSame(b, inns);

		b = bh.assertNonProblem("f(a, 1)", 1);
		assertSame(b, glob);
	}

	//	const int&& foo();
	//	int i;
	//	struct A { double x; };
	//	const A* a = new A();
	//	decltype(foo()) t1(); // type is const int&&
	//	decltype(i) t2(); // type is int
	//	decltype(a->x) t3(); // type is double
	//	decltype((a->x)) t4(); // type is const double&
	//	__typeof foo() t5();   // type is const int
	//	__typeof(i) t6();      // type is int
	//	__typeof(a->x) t7();   // type is const double
	//	__typeof((a->x)) t8(); // type is const double

	public void testDecltype_294730() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code, CPP);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IFunction f = bh.assertNonProblem("t1", 2);
		assertEquals("const int &&", ASTTypeUtil.getType(f.getType().getReturnType()));
		f = bh.assertNonProblem("t2", 2);
		assertEquals("int", ASTTypeUtil.getType(f.getType().getReturnType()));
		f = bh.assertNonProblem("t3", 2);
		assertEquals("double", ASTTypeUtil.getType(f.getType().getReturnType()));
		f = bh.assertNonProblem("t4", 2);
		assertEquals("const double &", ASTTypeUtil.getType(f.getType().getReturnType()));

		f = bh.assertNonProblem("t5", 2);
		assertEquals("const int", ASTTypeUtil.getType(f.getType().getReturnType()));
		f = bh.assertNonProblem("t6", 2);
		assertEquals("int", ASTTypeUtil.getType(f.getType().getReturnType()));
		f = bh.assertNonProblem("t7", 2);
		assertEquals("const double", ASTTypeUtil.getType(f.getType().getReturnType()));
		f = bh.assertNonProblem("t8", 2);
		assertEquals("const double", ASTTypeUtil.getType(f.getType().getReturnType()));
	}

	//	class Waldo {
	//		typedef int type;
	//		static int value;
	//	};
	//
	//	int main() {
	//		Waldo w;
	//		decltype(w)::type i;
	//		int x = decltype(w)::value;
	//	}
	public void testDecltypeInNameQualifier_380751() throws Exception {
		parseAndCheckBindings();
	}

	//	struct Base {};
	//	struct Derived : decltype(Base()) {};
	public void testDecltypeInBaseSpecifier_438348() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPClassType base = helper.assertNonProblem("struct Base", "Base");
		ICPPClassType derived = helper.assertNonProblem("Derived");
		ICPPBase[] bases = derived.getBases();
		assertEquals(1, bases.length);
		assertEquals(base, bases[0].getBaseClass());
	}

	//	template <typename T>
	//	T bar();
	//	struct S {
	//	    void waldo();
	//	};
	//	int main() {
	//	    auto L = [](S s) { return s; };
	//	    typedef decltype(L) lambda_type;
	//	    decltype(bar<const lambda_type>()(S())) v;
	//	    v.waldo();
	//	}
	public void testDecltypeWithConstantLambda_397494() throws Exception {
		parseAndCheckBindings();
	}

	//	template<typename T>
	//	void f(T p);
	//
	//	class A {
	//	  void m() {
	//	    f([this] (int x) {});
	//	  }
	//	};
	public void testLambdaWithCapture() throws Exception {
		parseAndCheckBindings();
	}

	//	int foo = 0;
	//	auto bar = [foo] { return foo; };
	public void testLambdaWithCapture_446225() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPVariable foo1 = bh.assertNonProblemOnFirstIdentifier("foo =", ICPPVariable.class);
		ICPPVariable foo2 = bh.assertNonProblemOnFirstIdentifier("[foo]", ICPPVariable.class);
		assertTrue(foo1 == foo2);
		assertEquals(2, bh.getTranslationUnit().getReferences(foo1).length);
	}

	//	template<typename T>
	//	class B;
	//
	//	template<typename T, typename... U>
	//	struct B<T(U...)> {
	//	  template<typename V>
	//	  B(V);
	//	};
	//
	//	struct A {
	//	  B<bool(int arg)> f;
	//	};
	//
	//	void waldo(A a);
	//
	//	void test() {
	//	  int p;
	//	  waldo({[p](int arg) { return true; }});
	//	}
	public void testLambdaWithCaptureInInitializerList_488265() throws Exception {
		parseAndCheckBindings();
	}

	//	typedef int TInt;
	//	void test() {
	//		int a1= {}, a2{};		// Initializer for declarator
	//		int b1= {1}, b2{1};
	//
	//		TInt c[12];
	//		c[{1,2}];				// Array subscript
	//
	//		a1= int{};				// Functional casts
	//		a2= TInt{};
	//		b1= int{1};
	//		b2= TInt{};
	//
	//		new({1}) int {};		// New expression initializer
	//		new({1}) int {1};
	//
	//		a1= b1= {1};			// Assinment expression
	//		if (int a={2}) {}		// Condition
	//		return {0,1,2};			// Return statement
	//	}
	//
	//	struct S {
	//		int f;
	//		S(int);
	//	};
	//
	//	S::S(int a) : f{a} {}		// Member initializer
	public void testInitSyntax_302412() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace std {
	//		template<typename T> class initializer_list;
	//	}
	//	template<typename T> struct complex {
	//		complex(const T& r, const T& i);
	//	};
	//	template<typename T> struct vector {
	//		vector(std::initializer_list<T>){};
	//	};
	//	struct str {
	//		str(const char*);
	//	};
	//	template<typename T> void f(std::initializer_list<T>);
	//	std::initializer_list<const char*> test() {
	//		int a = {1};
	//		complex<double> z{1,2};
	//		new vector<str>{"a", "b", "c", "d"}; // 4 string elements
	//		f({"a","b"}); // pass list of two elements
	//		int* e {}; // initialization to zero / null pointer
	//		a = double{1}; // explicitly construct a double
	//		return { "a" }; // return list of one element
	//	}
	//
	//	struct S {
	//		S(std::initializer_list<double>) {}
	//		S(std::initializer_list<int>) {}
	//      S() {}
	//	};
	//	void fs(S){}
	//	void tests() {
	//		fs({1,2,3});
	//		fs({1.0,2.0,3.0});
	//      fs({});
	//	}
	public void testListInitialization_302412a() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace std {
	//		template<typename T> class initializer_list;
	//	}
	//	struct str {
	//		str(const char*);
	//	};
	//	struct A {
	//		A(std::initializer_list<double>); // #1
	//		A(std::initializer_list<str>); // #3
	//	};
	//	void f(std::initializer_list<int>);
	//	void g(A);
	//	void test() {
	//		f({1,2,3}); // OK: f(initializer_list<int>) identity conversion
	//		f({'a','b'}); // OK: f(initializer_list<int>) integral promotion
	//		f({1.0}); // error: narrowing
	//		A a{ 1.0,2.0 }; // OK, uses #1
	//		g({ "foo", "bar" }); // OK, uses #3
	//	}
	public void testListInitialization_302412b() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		bh.assertNonProblem("f({1,2,3})", 1);
		bh.assertNonProblem("f({'a','b'})", 1);
		bh.assertProblem("f({1.0})", 1);
		bh.assertNonProblem("g({ \"foo\", \"bar\" })", 1);
	}

	//	namespace std {
	//		template<typename T> class initializer_list;
	//	}
	//	struct str {
	//		str(const char*) {
	//		}
	//	};
	//	struct A {
	//		A(std::initializer_list<int>);
	//	};
	//	struct B {
	//		B(int, double);
	//	};
	//	struct C {
	//		C(str);
	//	};
	//	struct D {
	//		D(A, C);
	//	};
	//  struct X {
	//      X();
	//      X(X&);
	//  };
	//	void e(A);
	//	void f(A);
	//	void f(B);
	//	void g(B);
	//	void h(C);
	//	void i(D);
	//	void x(const X);
	//
	//	void test() {
	//		e({ 'a', 'b' }); // OK: f(A(std::initializer_list<int>)) user-defined conversion
	//		g({ 'a', 'b' }); // OK: g(B(int,double)) user-defined conversion
	//		g({ 1.0, 1.0 }); // error: narrowing
	//		f({ 'a', 'b' }); // error: ambiguous f(A) or f(B)
	//		h({ "foo" }); // OK: h(C(std::string("foo")))
	//		i({ { 1, 2 }, { "bar" } }); // OK: i(D(A(std::initializer_list<int>{1,2}),C(std::string("bar"))))
	//      X x1;
	//      x({x1});  // no matching constructor
	//	}
	public void testListInitialization_302412c() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		bh.assertNonProblem("e({ 'a', 'b' })", 1);
		bh.assertNonProblem("g({ 'a', 'b' })", 1);
		bh.assertProblem("g({ 1.0, 1.0 })", 1);
		bh.assertProblem("f({ 'a', 'b' })", 1);
		bh.assertNonProblem("h({", 1);
		bh.assertNonProblem("i({ { 1, 2 }, {", 1);
		bh.assertProblem("x({x1})", 1);
	}

	//	namespace std {
	//		template<typename T> class initializer_list;
	//	}
	//	struct A {
	//		int m1;
	//		double m2;
	//	};
	//	void f(A);
	//	void test() {
	//		f({'a', 'b'}); // OK: f(A(int,double)) user-defined conversion
	//		f({1.0});      // narrowing not detected by cdt.
	//	}
	public void testListInitialization_302412d() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		bh.assertNonProblem("f({'a', 'b'})", 1);
		// not detected by CDT
		// bh.assertProblem("f({1.0})", 1);
	}

	//	namespace std {
	//		template<typename T> class initializer_list;
	//	}
	//	void g(const double &);
	//	void h(int);
	//	void test() {
	//		g({1}); // same conversion as int to double
	//
	//		h({'a'}); // OK: same conversion as char to int
	//		h({1.0}); // error: narrowing
	//		h({ }); // OK: identity conversion
	//	}
	public void testListInitialization_302412e() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		// not detected by CDT
		// bh.assertProblem("g({1})", 1);
		bh.assertNonProblem("h({'a'})", 1);
		bh.assertProblem("h({1.0})", 1);
		bh.assertNonProblem("h({ })", 1);
	}

	//	namespace std {
	//		template<typename T> class initializer_list;
	//	}
	//	void f(int, int){}
	//	struct F { F(int,int){}};
	//	void  fF(F) {}
	//
	//	struct G { G(int){} G(){}};
	//	void  fG(G) {}
	//
	//	struct H { H(G) {}};
	//	void  fH(H) {}
	//
	//	void test() {
	//		f({1,1});	// list is not expanded
	//		new F({1,1});  // F(F(1,1))
	//		fF({1,1});     // F(1,1)
	//
	//		fG(1);		// G(1)
	//		fG({1});    // G(1)
	//
	//		new H(1);   // H(G(1))
	//		new H({1}); // H(G(1)) or H(H(G(1)))
	//		fH(1);      // no conversion from int to H
	//		fH({1});    // H(G(1))
	//	}
	public void testListInitialization_302412f() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertProblem("f({1,1})", 1);
		bh.assertImplicitName("F({1,1})", 1, ICPPConstructor.class);
		bh.assertNonProblem("fF({1,1})", 2);

		bh.assertNonProblem("fG(1)", 2);
		bh.assertNonProblem("fG({1})", 2);

		bh.assertImplicitName("H(1)", 1, ICPPConstructor.class);
		IASTImplicitName n = bh.assertImplicitName("H({1})", 1, IProblemBinding.class);
		IProblemBinding problem = (IProblemBinding) n.resolveBinding();
		assertEquals(IProblemBinding.SEMANTIC_AMBIGUOUS_LOOKUP, problem.getID());
		bh.assertProblem("fH(1)", 2);
		bh.assertNonProblem("fH({1})", 2);
	}

	//	namespace std {
	//		template <typename T> class initializer_list;
	//	}
	//	void waldo(int);
	//	void waldo(std::initializer_list<int>);
	//	void foo() {
	//		waldo({});
	//		waldo({1});
	//		waldo({short(1)});
	//		waldo({1, 2});
	//	}
	public void testListInitialization_439477a() throws Exception {
		parseAndCheckBindings();
	}

	//	void waldo(int const (&)[2]);
	//	void waldo(int const (&)[3]);
	//	void foo1() {
	//		waldo({1, 2});        // should resolve to waldo(int const (&)[2])
	//		waldo({1, 2, 3});     // should resolve to waldo(int const (&)[3])
	//		waldo({1, 2, 3, 4});  // should not resolve
	//	}
	public void testListInitialization_439477b() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		ICPPFunction def1 = helper.assertNonProblem("waldo(int const (&)[2])", "waldo");
		ICPPFunction def2 = helper.assertNonProblem("waldo(int const (&)[3])", "waldo");

		ICPPFunction call1 = helper.assertNonProblem("waldo({1, 2})", "waldo");
		ICPPFunction call2 = helper.assertNonProblem("waldo({1, 2, 3})", "waldo");

		assertEquals(call1, def1);
		assertEquals(call2, def2);

		helper.assertProblem("waldo({1, 2, 3, 4})", "waldo", IProblemBinding.SEMANTIC_NAME_NOT_FOUND);
	}

	//	namespace std {
	//		template<class E>
	//		class initializer_list {
	//		  const E* _array;
	//		  long unsigned int _len;
	//
	//		  initializer_list(const E* array, int len) {}
	//		};
	//	}
	//
	//	template<typename T>
	//	struct A {
	//	  A(std::initializer_list<T> list) {}
	//	};
	//
	//	struct B {
	//	  int b;
	//	};
	//
	//	A<B> waldo({{0}, {0}});
	public void testListInitialization_458679() throws Exception {
		parseAndCheckImplicitNameBindings();
	}

	//	namespace std {	template<typename T> class initializer_list; }
	//
	//	struct A {
	//	  A(const char* s);
	//	};
	//
	//	void waldo(A p);
	//	void waldo(std::initializer_list<A> p);
	//
	//	void test() {
	//	  waldo({""});
	//	}
	public void testListInitialization_491842() throws Exception {
		parseAndCheckBindings();
	}

	//	struct A {
	//	  A();
	//	  A(int);
	//	  A(int, int);
	//	};
	//
	//	void test() {
	//	  A{};
	//	  A{1};
	//	  A{1, 2};
	//	}
	public void testListInitializer_495227() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper ah = getAssertionHelper();
		IASTImplicitName name = ah.findImplicitName("A{}", 1);
		IBinding binding = name.resolveBinding();
		assertTrue(binding instanceof ICPPConstructor);
		assertEquals(0, ((ICPPConstructor) binding).getType().getParameterTypes().length);
		name = ah.findImplicitName("A{1}", 1);
		binding = name.resolveBinding();
		assertTrue(binding instanceof ICPPConstructor);
		assertEquals(1, ((ICPPConstructor) binding).getType().getParameterTypes().length);
		name = ah.findImplicitName("A{1, 2}", 1);
		binding = name.resolveBinding();
		assertTrue(binding instanceof ICPPConstructor);
		assertEquals(2, ((ICPPConstructor) binding).getType().getParameterTypes().length);
	}

	//	struct S {
	//	    int a;
	//	    float b;
	//	    char c;
	//	};
	//
	//	struct T {
	//	    char x;
	//	    int y;
	//	};
	//
	//	void foo(S);
	//	void foo(T);
	//
	//	int main() {
	//	    foo({3, 4.0, 'c'});  // 'foo' is ambiguous
	//	}
	public void testAggregateInitialization_487555() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace std {
	//		template<typename T> class initializer_list;
	//	}
	//	struct A {};
	//	A a;
	//	auto c(a);
	//	auto b = a;
	//	const auto *p = &b, q = "";
	//	static auto d = 0.0;
	//	auto r = { 'a', 'b' };
	//	auto *s = new auto(1L);
	//	auto t = new auto({1.0});
	//	auto x;  // Error - missing initializer.
	//	auto y = { 1.0, 5 };  // Error - inconsistent types in the array initializer.
	public void testAutoType_289542() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		ICPPVariable b = bh.assertNonProblem("b =", 1);
		assertEquals("A", ASTTypeUtil.getType(b.getType()));
		ICPPVariable c = bh.assertNonProblem("c(a)", "c");
		assertEquals("A", ASTTypeUtil.getType(c.getType()));
		ICPPVariable p = bh.assertNonProblem("p =", 1);
		assertEquals("const A *", ASTTypeUtil.getType(p.getType()));
		ICPPVariable q = bh.assertNonProblem("q =", 1);
		assertEquals("const char * const", ASTTypeUtil.getType(q.getType()));
		ICPPVariable d = bh.assertNonProblem("d =", 1);
		assertEquals("double", ASTTypeUtil.getType(d.getType()));
		ICPPVariable r = bh.assertNonProblem("r =", 1);
		assertEquals("std::initializer_list<char>", ASTTypeUtil.getType(r.getType()));
		ICPPVariable s = bh.assertNonProblem("s =", 1);
		assertEquals("long int *", ASTTypeUtil.getType(s.getType()));
		ICPPVariable t = bh.assertNonProblem("t =", 1);
		assertEquals("std::initializer_list<double> *", ASTTypeUtil.getType(t.getType()));
		ICPPVariable x = bh.assertNonProblem("x;", 1);
		IProblemType pt = (IProblemType) x.getType();
		assertEquals(ISemanticProblem.TYPE_CANNOT_DEDUCE_AUTO_TYPE, pt.getID());
		ICPPVariable y = bh.assertNonProblem("y =", 1);
		pt = (IProblemType) y.getType();
		assertEquals(ISemanticProblem.TYPE_CANNOT_DEDUCE_AUTO_TYPE, pt.getID());
	}

	//	auto x = y + z;
	//	auto y = x;
	//	auto z = x;
	//	void test() {
	//	  for (auto a : a) {}
	//	}
	public void testAutoType_305970() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPVariable x = bh.assertNonProblem("x =", 1, ICPPVariable.class);
		IProblemType xt = (IProblemType) x.getType();
		assertEquals(ISemanticProblem.TYPE_CANNOT_DEDUCE_AUTO_TYPE, xt.getID());
		ICPPVariable a = bh.assertNonProblem("a :", "a", ICPPVariable.class);
		IProblemType at = (IProblemType) a.getType();
		assertEquals(ISemanticProblem.TYPE_CANNOT_DEDUCE_AUTO_TYPE, at.getID());
	}

	// struct A { auto a = 1; };         // Auto-typed non-static fields are not allowed.
	// struct B { static auto b = 1; };  // Auto-typed static fields are ok.
	public void testAutoType_305987() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		ICPPVariable a = bh.assertNonProblem("a =", 1);
		IProblemType pt = (IProblemType) a.getType();
		assertEquals(ISemanticProblem.TYPE_AUTO_FOR_NON_STATIC_FIELD, pt.getID());
		ICPPVariable b = bh.assertNonProblem("b =", 1);
	}

	// auto fpif1(int)->int(*)(int)
	// auto fpif2(int)->int(*)(int) {}
	public void testNewFunctionDeclaratorSyntax_305972() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		ICPPFunction f = bh.assertNonProblem("fpif1", 0);
		assertEquals("int (* (int))(int)", ASTTypeUtil.getType(f.getType()));

		f = bh.assertNonProblem("fpif2", 0);
		assertEquals("int (* (int))(int)", ASTTypeUtil.getType(f.getType()));
	}

	// enum class EScoped1 {a1};
	// enum class EScoped2 : short {a2};
	// enum class EScoped3;
	// enum EUnscoped1 {b1};
	// enum EUnscoped2 : long {b2};
	// enum EUnscoped3 : int;
	public void testScopedEnums_305975a() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		ICPPEnumeration e;
		ICPPBinding ei;
		e = bh.assertNonProblem("EScoped1", 0);
		assertTrue(e.isScoped());
		assertEquals("int", ASTTypeUtil.getType(e.getFixedType()));
		assertDefinition(e);
		ei = bh.assertNonProblem("a1", 0);
		assertSame(e, ei.getOwner());
		assertEquals(2, ei.getQualifiedName().length);

		e = bh.assertNonProblem("EScoped2", 0);
		assertTrue(e.isScoped());
		assertEquals("short int", ASTTypeUtil.getType(e.getFixedType()));
		assertDefinition(e);
		ei = bh.assertNonProblem("a2", 0);
		assertSame(e, ei.getOwner());
		assertEquals(2, ei.getQualifiedName().length);

		e = bh.assertNonProblem("EScoped3", 0);
		assertTrue(e.isScoped());
		assertEquals("int", ASTTypeUtil.getType(e.getFixedType()));
		assertDeclaration(e);

		e = bh.assertNonProblem("EUnscoped1", 0);
		assertFalse(e.isScoped());
		assertNull(e.getFixedType());
		assertDefinition(e);
		ei = bh.assertNonProblem("b1", 0);
		assertSame(e, ei.getOwner());
		assertEquals(1, ei.getQualifiedName().length);

		e = bh.assertNonProblem("EUnscoped2", 0);
		assertFalse(e.isScoped());
		assertEquals("long int", ASTTypeUtil.getType(e.getFixedType()));
		assertDefinition(e);
		ei = bh.assertNonProblem("b2", 0);
		assertSame(e, ei.getOwner());
		assertEquals(1, ei.getQualifiedName().length);

		e = bh.assertNonProblem("EUnscoped3", 0);
		assertFalse(e.isScoped());
		assertEquals("int", ASTTypeUtil.getType(e.getFixedType()));
		assertDeclaration(e);
	}

	//	enum class E { a, b };
	//	enum E x1 = E::a; // OK
	//	enum F { a, b };
	//	enum F y1 = a; // OK
	//	enum E1 : int; // OK: E1 is un-scoped, underlying type is int
	//	enum class F1; // OK: F1 is scoped, underlying type is int
	public void testScopedEnums_305975b() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);
	}

	//	enum class E x2 = E::a; //  illegal (elaborated type specifier)
	//	enum class F y2 = a; // illegal
	//	enum E; // illegal
	public void testScopedEnums_305975c() throws Exception {
		String code = getAboveComment();
		IASTTranslationUnit tu = parse(code, CPP, true, false);
		IASTDeclaration[] decls = tu.getDeclarations();
		assertEquals(3, decls.length);
		assertInstance(decls[0], IASTProblemDeclaration.class);
		assertInstance(decls[1], IASTProblemDeclaration.class);
		assertInstance(decls[2], IASTProblemDeclaration.class);
	}

	//  enum class Col { red, yellow, green };
	//	void fint(int);
	//	void fbool(bool);
	//	void fCol(Col);
	//
	//	void test() {
	//		fCol(Col::red);
	//		fint(Col::red); // error: no conversion to int
	//		fbool(Col::red); // error: no Col to bool conversion
	//	}
	public void testScopedEnums_305975d() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		bh.assertNonProblem("fCol(Col::red)", 4);
		bh.assertProblem("fint(Col::red)", 4);
		bh.assertProblem("fbool(Col::red)", 5);
	}

	//	enum direction { left='l', right='r'};
	//	void g() {
	//		direction d; // OK
	//		d = left; // OK
	//		d = direction::right; // OK
	//	}
	//
	//	enum class altitude { high=1, low=2 };
	//	void h() {
	//		altitude a; // OK
	//		a = altitude::low; // OK
	//	}
	//
	//	struct X {
	//		enum xdir { xl=1, xr=2 };
	//		int f(int i) { return i==xl ? 0 : i==xr ? 1 : 2; }
	//	};
	//	void g(X* p) {
	//		int i;
	//		i = p->f(X::xr); // OK
	//		i = p->f(p->xl); // OK
	//	}
	public void testScopedEnums_305975e() throws Exception {
		parseAndCheckBindings();
	}

	//	enum class altitude { high=1, low=2 };
	//	struct X {
	//		enum xdir { xl=1, xr=2 };
	//		int f(int i) { return i==xl ? 0 : i==xr ? 1 : 2; }
	//	};
	//	void g(X* p) {
	//		altitude a= high; // error: high not in scope
	//		xdir d; // error: xdir not in scope
	//		xl;     // error: not in scope
	//	}
	public void testScopedEnums_305975f() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		bh.assertProblem("high;", -1);
		bh.assertProblem("xdir d", -2);
		bh.assertProblem("xl;", -1);
	}

	//	constexpr int f(int);
	//	enum class X {e1, e2= e1+2, e3};
	//	enum class Y {e1, e2= f(e1)+2, e3};
	//	enum A {e1, e2= e1+2, e3};
	//	enum B {e1, e2= f(e1)+2, e3};
	public void testScopedEnums_305975g() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);
	}

	//	typedef int Int;
	//	struct S {
	//	    enum waldo1 : int;
	//	    enum waldo2 : Int;
	//	};
	//	enum S::waldo1 : int {
	//		someConstant = 1508
	//	};
	public void testForwardDeclaringEnumAtClassScope_475894() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S {
	//	  int m;
	//	};
	//	void f(unsigned int x);
	//	void test() {
	//	  f(sizeof(S::m));
	//	//  f(sizeof(S::m + 1)); // Error not detected by CDT: reference to non-static member in subexpression
	//	}
	public void testSizeofOfNonstaticMember_305979() throws Exception {
		parseAndCheckBindings();
	}

	//	template <bool> struct B {};
	//	template <>
	//	struct B<true> {
	//	  void waldo();
	//	};
	//	typedef char& one;
	//	void test() {
	//	  B<sizeof(one) == 1> b;
	//	  b.waldo();
	//	}
	public void testSizeofReference_397342() throws Exception {
		parseAndCheckBindings();
	}

	//	constexpr int waldo = sizeof("cat\b\\\n");
	public void testSizeofStringLiteralWithEscapeCharacters_459279() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPVariable waldo = helper.assertNonProblem("waldo");
		assertConstantValue(7, waldo); // "cat" + backspace + slash + newline + null terminator
	}

	//	struct A {
	//	  char a[100];
	//	};
	//	struct B {
	//	  A& b;
	//	};
	//	A* p;
	public void testSizeofStructWithReferenceField_397342() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		IASTName nameB = bh.findName("B");
		IASTName namep = bh.findName("p");
		ICPPClassType B = (ICPPClassType) nameB.resolveBinding();
		IPointerType ptrToA = (IPointerType) ((ICPPVariable) namep.resolveBinding()).getType();
		long pointerSize = getSizeAndAlignment(ptrToA, namep).size;
		long BSize = getSizeAndAlignment(B, nameB).size;
		assertEquals(pointerSize, BSize);
	}

	//	struct waldo {};
	public void testSizeofEmptyStruct_457770() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		IASTName nameWaldo = bh.findName("waldo");
		ICPPClassType waldo = (ICPPClassType) nameWaldo.resolveBinding();
		long waldoSize = getSizeAndAlignment(waldo, nameWaldo).size;
		assertNotEquals(0, waldoSize);
	}

	//	template <bool> struct B {};
	//	template <>
	//	struct B<true> {
	//	  void waldo();
	//	};
	//	typedef char& one;
	//	void test() {
	//	  B<alignof(one) == 1> b;
	//	  b.waldo();
	//	}
	public void testAlignof_451082() throws Exception {
		parseAndCheckBindings();
	}

	//	void f(int);
	//	void f(unsigned int);
	//	void test() {
	//		char16_t c16= '1';
	//		char32_t c32= '1';
	//		f(c16);
	//		f(c32);
	//	}
	public void testNewCharacterTypes_305976() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IFunction f1 = bh.assertNonProblem("f(int)", 1);
		IFunction f2 = bh.assertNonProblem("f(unsigned int)", 1);
		IBinding b = bh.assertNonProblem("f(c16)", 1);
		assertSame(f1, b);
		b = bh.assertNonProblem("f(c32)", 1);
		assertSame(f2, b);
	}

	//	int test() {
	//	    void (11);
	//	    return 42;
	//	}
	public void testCastToVoid_309155() throws Exception {
		parseAndCheckBindings();
	}

	//	void test() {
	//	    void *libHandle (0);
	//	}
	public void testCtorInitializerForVoidPtr_314113() throws Exception {
		parseAndCheckBindings();
	}

	//	struct D {};
	//	struct C {
	//	  operator D();
	//	  operator char();
	//	};
	//	long operator+(D d, char a);
	//	void f(long);
	//	void f(int);
	//	void xx() {
	//	    C c;
	//	    f(c+1);   // converts c to a char and calls operator+(int, int)
	//	}
	public void testBuiltinOperators_294543a() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IFunction fint = bh.assertNonProblem("f(int)", 1);
		IFunction f = bh.assertNonProblem("f(c+1)", 1);
		assertSame(fint, f);
	}

	//	struct A {
	//		operator long& ();
	//	};
	//	void f(long);
	//	void f(A);
	//	void test() {
	//		A a;
	//		f(a= 1);   // A cannot be converted to long& here
	//	}
	public void testBuiltinOperators_294543b() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IFunction fA = bh.assertNonProblem("f(A)", 1);
		IFunction f = bh.assertNonProblem("f(a= 1)", 1);
		assertSame(fA, f);
	}

	//	struct X {};
	//	struct Y : X {};
	//	struct A {
	//      virtual X* m();//0
	//      virtual X* m(X*);//1
	//	};
	//	struct B : A {
	//      Y* m();//2
	//      Y* m(Y*);//3
	//	};
	public void testOverrideSimpleCovariance_321617() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPMethod m0 = helper.assertNonProblem("m();//0", 1, ICPPMethod.class);
		ICPPMethod m1 = helper.assertNonProblem("m(X*);//1", 1, ICPPMethod.class);
		ICPPMethod m2 = helper.assertNonProblem("m();//2", 1, ICPPMethod.class);
		ICPPMethod m3 = helper.assertNonProblem("m(Y*);//3", 1, ICPPMethod.class);

		assertTrue(ClassTypeHelper.isVirtual(m0));
		assertTrue(ClassTypeHelper.isVirtual(m1));
		assertTrue(ClassTypeHelper.isVirtual(m2));
		assertFalse(ClassTypeHelper.isVirtual(m3));

		assertFalse(ClassTypeHelper.isOverrider(m0, m0));
		assertFalse(ClassTypeHelper.isOverrider(m0, m1));

		assertFalse(ClassTypeHelper.isOverrider(m1, m0));
		assertFalse(ClassTypeHelper.isOverrider(m1, m1));

		assertTrue(ClassTypeHelper.isOverrider(m2, m0));
		assertFalse(ClassTypeHelper.isOverrider(m2, m1));

		assertFalse(ClassTypeHelper.isOverrider(m3, m0));
		assertFalse(ClassTypeHelper.isOverrider(m3, m1));

		ICPPMethod[] ors = ClassTypeHelper.findOverridden(m0);
		assertEquals(0, ors.length);
		ors = ClassTypeHelper.findOverridden(m1);
		assertEquals(0, ors.length);
		ors = ClassTypeHelper.findOverridden(m2);
		assertEquals(1, ors.length);
		assertSame(ors[0], m0);
		ors = ClassTypeHelper.findOverridden(m3);
		assertEquals(0, ors.length);
	}

	//	struct A {
	//		virtual void f();  // A
	//	};
	//
	//	struct B : virtual A {
	//		virtual void f();  // B
	//	};
	//
	//	struct C : B , virtual A {
	//		using A::f;
	//	};
	//
	//	void foo() {
	//		C c;
	//		c.f();
	//		c.C::f();
	//	}
	public void testResolutionToFinalOverrider_86654() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		ICPPMethod actual = helper.assertNonProblem("c.f()", "f");
		ICPPMethod expected = helper.assertNonProblem("virtual void f();  // B", "f");
		assertEquals(expected, actual);

		actual = helper.assertNonProblem("c.C::f()", "f");
		expected = helper.assertNonProblem("virtual void f();  // A", "f");
		assertEquals(expected, actual);
	}

	//	struct X {
	//		X();
	//	};
	//	X::X() = default;
	//	int f(int) = delete;
	//	auto g() -> int = delete;
	public void testDefaultedAndDeletedFunctions_305978() throws Exception {
		String code = getAboveComment();
		IASTTranslationUnit tu = parseAndCheckBindings(code);

		ICPPASTFunctionDefinition f = getDeclaration(tu, 1);
		assertTrue(f.isDefaulted());
		assertFalse(f.isDeleted());

		f = getDeclaration(tu, 2);
		assertFalse(f.isDefaulted());
		assertTrue(f.isDeleted());

		f = getDeclaration(tu, 3);
		assertFalse(f.isDefaulted());
		assertTrue(f.isDeleted());

		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		ICPPFunction fb = bh.assertNonProblem("X() =", 1);
		assertFalse(fb.isDeleted());

		fb = bh.assertNonProblem("f(int)", 1);
		assertTrue(fb.isDeleted());

		fb = bh.assertNonProblem("g()", 1);
		assertTrue(fb.isDeleted());
	}

	//	const int b=12;
	//	void f(int a= b) = delete ;
	public void testDefaultedAndDeletedFunctions_305978b() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);
	}

	//	namespace ns {
	//		struct S {};
	//	}
	//
	//	namespace m {
	//		void h(ns::S);
	//	}
	//
	//	namespace ns {
	//		inline namespace a {
	//			using namespace m;
	//			struct A {};
	//			void fa(S s);
	//		}
	//		inline namespace b {
	//			struct B {};
	//			void fb(S s);
	//			void gb(a::A);
	//		}
	//		void f(S s);
	//		void g(a::A);
	//		void g(b::B);
	//	}
	//
	//	ns::S s;
	//	ns::A a0;
	//	ns::B b0;
	//	ns::a::A a;
	//	ns::b::B b;
	//
	//	void ok() {
	//		fa(s); fb(s); f(s);
	//		ns::h(s);
	//		g(a);
	//		gb(a);
	//	}
	public void testInlineNamespace_305980a() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace ns {
	//		inline namespace m {
	//			int a;
	//		}
	//	}
	//	void test() {
	//		ns::m::a;
	//		ns::a;
	//	}
	public void testInlineNamespace_305980b() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace out {
	//		void f(int);
	//	}
	//  namespace out2 {
	//      void g(int);
	//  }
	//	using namespace out;
	//	inline namespace in {
	//      inline namespace in2 {
	//		   void f(char);
	//         using namespace out2;
	//      }
	//	}
	//	void test() {
	//		::f(1);
	//      ::g(1);
	//	}
	public void testInlineNamespace_305980c() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);

		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IFunction fo = bh.assertNonProblem("f(int)", 1);
		IFunction g = bh.assertNonProblem("g(int)", 1);
		IFunction fi = bh.assertNonProblem("f(char)", 1);

		IFunction ref = bh.assertNonProblem("f(1)", 1);
		assertSame(fi, ref);
		ref = bh.assertNonProblem("g(1)", 1);
		assertSame(g, ref);
	}

	// namespace ns {
	//    inline namespace m {
	//       void f();
	//    }
	// }
	// void ns::f() {}
	public void testInlineNamespace_305980d() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);

		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IFunction f1 = bh.assertNonProblem("f();", 1);
		IFunction f2 = bh.assertNonProblem("f() {", 1);
		assertSame(f1, f2);
	}

	//	struct C {
	//	    void test() {
	//	      int a;
	//	      []{};
	//	      [=]{};
	//	      [&]{};
	//	      [this]{};
	//	      [=, this]{};
	//	      [&, this]{};
	//	      [a]{};
	//	      [=, a]{};
	//	      [&, a]{};
	//	      [a ...]{};
	//        [](int p){};
	//        [](int p) mutable {};
	//        [](int p) mutable throw(int) {};
	//        [](int p) mutable throw(int) -> char {};
	//        [](int p) throw(int) {};
	//        [](int p) -> char {};
	//	    }
	//	};
	public void testLambdaExpression_316307a() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);
	}

	//	template <typename T, typename C> void sort(T from, T to, C compare) {}
	//	float abs(float f);
	//
	//	void abssort(float *x, unsigned N) {
	//	  sort(x, x + N, [](float a, float b) {
	//	        return abs(a) < abs(b);
	//	  });
	//	}
	public void testLambdaExpression_316307b() throws Exception {
		parseAndCheckBindings();
	}

	//	struct function {
	//		template <typename T>
	//		function(T);
	//	};
	//	struct S {
	//		void waldo();
	//		function f = [this](){ waldo(); };
	//	};
	public void testLambdaInDefaultMemberInitializer_494182() throws Exception {
		parseAndCheckBindings();
	}

	//	typedef int MyType;
	//
	//	void f(const MyType& val);
	//	void g(MyType& val);
	public void testTypeString_323596() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IFunction f = bh.assertNonProblem("f(", 1);
		assertEquals("const MyType &", ASTTypeUtil.getType(f.getType().getParameterTypes()[0], false));
		assertEquals("const int &", ASTTypeUtil.getType(f.getType().getParameterTypes()[0], true));

		IFunction g = bh.assertNonProblem("g(", 1);
		assertEquals("MyType &", ASTTypeUtil.getType(g.getType().getParameterTypes()[0], false));
		assertEquals("int &", ASTTypeUtil.getType(g.getType().getParameterTypes()[0], true));
	}

	//	class container {
	//	public:
	//		void constBegin() const;
	//	    void begin();
	//	};
	//	struct test {
	//		void test_func() const {
	//			cnt.constBegin(); //ref
	//			cnt.begin(); //ref
	//		}
	//		container cnt;
	//	};
	public void testConstMember_323599() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IFunction f = bh.assertNonProblem("constBegin(); //ref", 10);
		bh.assertProblem("begin(); //ref", 5);
	}

	//	template <class T> class PE {
	//	    explicit operator bool() const;
	//	};
	//	template <class T> class P {
	//	    operator bool() const;
	//	};
	//	void fint(int);
	//	void test() {
	//	  PE<int> pe;
	//	  P<int> p;
	//	  fint(pe + pe); // problem
	//	  fint(p + p);   // converted to boolean and then int.
	//	};
	public void testExplicitConversionOperators() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		bh.assertProblem("fint(pe + pe);", 4);
		bh.assertNonProblem("fint(p + p);", 4);
	}

	//	struct C {
	//		C(const C& c) {}
	//	};
	//	struct D {
	//		explicit operator C();
	//	};
	//	void f() {
	//		D d;
	//		C c (d);
	//	}
	public void testExplicitOperatorInDirectInit() throws Exception {
		String code = getAboveComment();
		IASTTranslationUnit tu = parseAndCheckBindings(code);
		ICPPASTFunctionDefinition fdef = getDeclaration(tu, 2);
		IASTDeclarationStatement declstmt = getStatement(fdef, 1);
		IASTSimpleDeclaration decl = (IASTSimpleDeclaration) declstmt.getDeclaration();
		IASTImplicitName[] names = ((IASTImplicitNameOwner) decl.getDeclarators()[0]).getImplicitNames();
		assertEquals(1, names.length);
		assertTrue(names[0].resolveBinding() instanceof ICPPConstructor);
	}

	//	void g(char *);
	//	void f(char *);
	//	void f(const char *);
	//	void testa() {
	//		f("abc");
	//		g("abc");
	//	}
	public void testRankingOfDeprecatedConversionOnStringLiteral() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IFunction g = bh.assertNonProblem("g(char *)", 1);
		IFunction fconst = bh.assertNonProblem("f(const char *)", 1);

		IBinding ref = bh.assertNonProblem("f(\"abc\")", 1);
		assertSame(fconst, ref);
		ref = bh.assertNonProblem("g(\"abc\")", 1);
		assertSame(g, ref);
	}

	//	struct A {
	//		int m;
	//	};
	//	A&& operator+(A, A);
	//	A&& f();
	//	A a;
	//	A&& ar = static_cast<A&&>(a);
	//  void test() {
	//     f();   				// xvalue
	//     f().m; 				// xvalue
	//     static_cast<A&&>(a);	// xvalue
	//     a+a;					// xvalue
	//     ar;					// rvalue
	//  }
	public void testXValueCategories() throws Exception {
		String code = getAboveComment();
		IASTTranslationUnit tu = parseAndCheckBindings(code);
		ICPPASTFunctionDefinition fdef = getDeclaration(tu, 5);
		IASTExpression expr;

		expr = getExpressionOfStatement(fdef, 0);
		assertEquals(XVALUE, expr.getValueCategory());
		assertEquals("A", ASTTypeUtil.getType(expr.getExpressionType()));

		expr = getExpressionOfStatement(fdef, 1);
		assertEquals(XVALUE, expr.getValueCategory());
		assertEquals("int", ASTTypeUtil.getType(expr.getExpressionType()));

		expr = getExpressionOfStatement(fdef, 2);
		assertEquals(XVALUE, expr.getValueCategory());
		assertEquals("A", ASTTypeUtil.getType(expr.getExpressionType()));

		expr = getExpressionOfStatement(fdef, 3);
		assertEquals(XVALUE, expr.getValueCategory());
		assertEquals("A", ASTTypeUtil.getType(expr.getExpressionType()));

		// ar;
		expr = getExpressionOfStatement(fdef, 4);
		assertEquals(LVALUE, expr.getValueCategory());
		assertEquals("A", ASTTypeUtil.getType(expr.getExpressionType()));
	}

	//	void test() {
	//	  int i;
	//	  int f1();
	//	  int&& f2();
	//	  int g(const int&);
	//	  int g(const int&&);
	//	  int j = g(i); // calls g(const int&)
	//	  int k = g(f1()); // calls g(const int&&)
	//	  int l = g(f2()); // calls g(const int&&)
	//	}
	public void testRankingOfReferenceBindings_a() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		IFunction g1 = bh.assertNonProblemOnFirstIdentifier("g(const int&)");
		IFunction g2 = bh.assertNonProblemOnFirstIdentifier("g(const int&&)");

		IFunction ref;
		ref = bh.assertNonProblemOnFirstIdentifier("g(i);");
		assertSame(g1, ref);
		ref = bh.assertNonProblemOnFirstIdentifier("g(f1());");
		assertSame(g2, ref);
		ref = bh.assertNonProblemOnFirstIdentifier("g(f2());");
		assertSame(g2, ref);
	}

	//	struct A {
	//	  A& operator<<(int);
	//	  void p() &;
	//	  void p() &&;
	//	};
	//	A& operator<<(A&&, char);
	//
	//	void test() {
	//	  A a;
	//	  A() << 1;//1     // calls A::operator<<(int)
	//	  A() << 'c';//2   // calls operator<<(A&&, char)
	//	  a << 1;//3       // calls A::operator<<(int)
	//	  a << 'c';//4     // calls A::operator<<(int)
	//	  A().p();//5      // calls A::p()&&
	//	  a.p();//6        // calls A::p()&
	//  }
	public void testRankingOfReferenceBindings_b() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPMethod s1 = bh.assertNonProblem("operator<<(int)", 10);
		ICPPFunction s2 = bh.assertNonProblem("operator<<(A&&, char)", 10);
		ICPPMethod p1 = bh.assertNonProblemOnFirstIdentifier("p() &;");
		ICPPMethod p2 = bh.assertNonProblemOnFirstIdentifier("p() &&;");

		IASTImplicitName name;
		name = bh.assertImplicitName("<< 1;//1", 2, ICPPMethod.class);
		assertSame(s1, name.getBinding());
		name = bh.assertImplicitName("<< 'c';//2", 2, ICPPFunction.class);
		assertSame(s2, name.getBinding());
		name = bh.assertImplicitName("<< 1;//3", 2, ICPPMethod.class);
		assertSame(s1, name.getBinding());
		name = bh.assertImplicitName("<< 'c';//4", 2, ICPPMethod.class);
		assertSame(s1, name.getBinding());
		ICPPMethod ref;
		ref = bh.assertNonProblemOnFirstIdentifier("p();//5");
		assertSame(p2, ref);
		ref = bh.assertNonProblemOnFirstIdentifier("p();//6");
		assertSame(p1, ref);
	}

	//	namespace std {
	//		template<typename T> class initializer_list;
	//	}
	//	struct S {
	//		S(std::initializer_list<double>); // #1
	//		S(std::initializer_list<int>);    // #2
	//		S();                              // #3
	//	};
	//	S s1 = { 1.0, 2.0, 3.0 };            // invoke #1
	//	S s2 = { 1, 2, 3 };                  // invoke #2
	//	S s3 = { };                          // invoke #3
	public void testEmptyInitializerList_324096() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IFunction ctor1 = bh.assertNonProblem("S(std::initializer_list<double>);", 1);
		IFunction ctor2 = bh.assertNonProblem("S(std::initializer_list<int>);", 1);
		IFunction ctor3 = bh.assertNonProblem("S();", 1);

		IASTName name;
		IASTImplicitNameOwner dtor;
		name = bh.findName("s1", 2);
		dtor = (IASTImplicitNameOwner) name.getParent();
		assertSame(ctor1, dtor.getImplicitNames()[0].resolveBinding());
		name = bh.findName("s2", 2);
		dtor = (IASTImplicitNameOwner) name.getParent();
		assertSame(ctor2, dtor.getImplicitNames()[0].resolveBinding());
		name = bh.findName("s3", 2);
		dtor = (IASTImplicitNameOwner) name.getParent();
		assertSame(ctor3, dtor.getImplicitNames()[0].resolveBinding());
	}

	//	struct S {
	//	    bool a;
	//	    int b;
	//	};
	//
	//	void waldo(S);
	//
	//	int main() {
	//	    waldo({1, 2});
	//	}
	public void testIntToBoolConversionInInitList_521543() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace A {
	//        inline namespace B {
	//            namespace C {
	//                int i;
	//            }
	//            using namespace C;
	//        }
	//        int i;
	//    }
	//    int j = A::i;
	public void testInlineNamespaceLookup_324096() throws Exception {
		parseAndCheckBindings();
	}

	//	struct C {
	//		operator int();
	//	};
	//	void f(C);
	//	void f(int);
	//	void test() {
	//		C c;
	//		f(true ? c : 1); // calls f(int), not f(C);
	//	}
	public void testConditionalOperator_324853a() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		IBinding f = bh.assertNonProblem("f(int);", 1);
		IBinding ref = bh.assertNonProblem("f(true ? c : 1)", 1);
		assertSame(f, ref);
	}

	//	void f(char*);
	//	void f(const char*);
	//  void g(char*);
	//	void test(char* p, const char* cp) {
	//	  f(0 ? cp : p);
	//	  f(0 ? p : ""); // uses f(const char*);
	//    g(0 ? p : ""); // converts "" to char*
	//	}
	public void testConditionalOperator_324853b() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		IBinding fc = bh.assertNonProblem("f(const char*);", 1);
		IBinding ref;
		ref = bh.assertNonProblem("f(0 ? cp : p)", 1);
		assertSame(fc, ref);
		ref = bh.assertNonProblem("f(0 ? p : \"\")", 1); // "" converted to char*
		assertSame(fc, ref);
		bh.assertNonProblem("g(0 ? p : \"\")", 1); //
	}

	//	struct C {
	//	  C();
	//	  C(int a, int b);
	//	};
	//
	//	C c1;                 // C()
	//	C c2(1,2);            // C(int, int)
	//	C c3 ({1,2});         // C(C(int, int)) // copy ctor is elided
	//	C c4 ={1,2};          // C(C(int, int)) // copy ctor is elided
	//	C c5 {1,2};           // C(int, int)
	public void testCtorForAutomaticVariables_156668() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IFunction ctor1 = bh.assertNonProblem("C();", 1);
		IFunction ctor2 = bh.assertNonProblem("C(int a, int b);", 1);

		IASTName name;
		IASTImplicitNameOwner dtor;
		name = bh.findName("c1", 2);
		dtor = (IASTImplicitNameOwner) name.getParent();
		assertSame(ctor1, dtor.getImplicitNames()[0].resolveBinding());
		name = bh.findName("c2", 2);
		dtor = (IASTImplicitNameOwner) name.getParent();
		assertSame(ctor2, dtor.getImplicitNames()[0].resolveBinding());
		name = bh.findName("c3", 2);
		dtor = (IASTImplicitNameOwner) name.getParent();
		assertSame(ctor2, dtor.getImplicitNames()[0].resolveBinding());
		name = bh.findName("c4", 2);
		dtor = (IASTImplicitNameOwner) name.getParent();
		assertSame(ctor2, dtor.getImplicitNames()[0].resolveBinding());
		name = bh.findName("c5", 2);
		dtor = (IASTImplicitNameOwner) name.getParent();
		assertSame(ctor2, dtor.getImplicitNames()[0].resolveBinding());
	}

	//	void g(int * __restrict a);
	//	void g(int * a) {}
	//
	//	void test1() {
	//	  int number = 5;
	//	  g(&number);
	//	}
	public void testTopLevelRestrictQualifier_327328() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IFunction g = bh.assertNonProblem("g(int * __restrict a)", 1);
		IFunction ref;
		ref = bh.assertNonProblem("g(int * a)", 1);
		assertSame(g, ref);
		ref = bh.assertNonProblem("g(&number)", 1);
		assertSame(g, ref);
	}

	//	void f(int * __restrict* a) {}
	//	void f(int ** a) {}              // different function
	//
	//	void test2() {
	//	  int* __restrict rnumber= 0;
	//	  int* number = 0;
	//	  f(&rnumber); // calls f(int* __restrict* a)
	//	  f(&number); // calls f(int** a)
	//	}
	public void testOverloadingWithRestrictQualifier_327328() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		IFunction f1 = bh.assertNonProblem("f(int * __restrict* a)", 1);
		IFunction f2 = bh.assertNonProblem("f(int ** a)", 1);
		IFunction ref;
		ref = bh.assertNonProblem("f(&rnumber)", 1);
		assertSame(f1, ref);
		ref = bh.assertNonProblem("f(&number)", 1);
		assertSame(f2, ref);
	}

	//	struct S {
	//		class C {};
	//	};
	//	typedef const S T;
	//	T::C c;
	public void testCVQualifiedClassName_328063() throws Exception {
		parseAndCheckBindings();
	}

	//	void test() {
	//		int array[5] = { 1, 2, 3, 4, 5 };
	//		for (int& x : array)
	//			x *= 2;
	//	}
	public void testRangeBasedForLoop_327223() throws Exception {
		parseAndCheckBindings();
	}

	//	struct A{};
	//	struct B : A {};
	//	struct C {
	//		 A& get();
	//	};
	//	struct D : C {
	//		  using C::get;
	//		  B& get();
	//	};
	//
	//	void func(A&) {}
	//	void test(D& c) {
	//		  func(c.get()); // error.
	//	}
	public void testOverrideUsingDeclaredMethod_328802() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace ns {
	//	class A {
	//	  void m() {
	//	    int a;
	//	    extern int b;
	//	    void f();
	//	  }
	//	  typedef int x;
	//	  int y;
	//	};
	//	}
	public void testOwner() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPNamespace ns = bh.assertNonProblemOnFirstIdentifier("ns");
		ICPPClassType A = bh.assertNonProblemOnFirstIdentifier("A");
		assertEquals(ns, A.getOwner());
		ICPPMethod m = bh.assertNonProblemOnFirstIdentifier("m()");
		assertEquals(A, m.getOwner());
		assertEquals(A, bh.assertNonProblemOnFirstIdentifier("x;").getOwner());
		assertEquals(A, bh.assertNonProblemOnFirstIdentifier("y;").getOwner());
		assertEquals(m, bh.assertNonProblemOnFirstIdentifier("a;").getOwner());
		assertNull(bh.assertNonProblemOnFirstIdentifier("b;").getOwner());
		assertNull(bh.assertNonProblemOnFirstIdentifier("f()").getOwner());
	}

	//	class A {
	//		A(int a = f());
	//		static int f();
	//	};
	public void testFwdLookupForDefaultArgument() throws Exception {
		parseAndCheckBindings();
	}

	//  auto f2 ();		// missing late return type.
	public void testBug332114a() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		IBinding b = bh.assertNonProblem("f2", 0);
		// Must not throw a NPE
		IndexCPPSignatureUtil.getSignature(b);
	}

	//	enum E: short;
	//	enum E: short;
	//	enum E: short {e1, e2};
	public void testBug332114b() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S {
	//	    void f();
	//	};
	//	void g() {
	//	    S array[5];
	//	    for (auto& s : array)
	//	        s.f();  // ERROR HERE
	//	}
	public void testAutoTypeInRangeBasedFor_332883a() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S {
	//	    void f();
	//	};
	//	struct Range {};
	//	namespace std {
	//	  S* begin(Range);
	//	}
	//
	//	void g() {
	//	    Range range;
	//	    for (auto s : range)
	//	        s.f();  // ERROR HERE
	//	}
	public void testAutoTypeInRangeBasedFor_332883b() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace std {
	//	    template <class T1, class T2> struct pair {
	//	        T1 first;
	//	        T2 second;
	//	    };
	//
	//	    template <typename T, typename U> T begin(const pair<T, U>& p) {
	//	        return p.first;
	//	    }
	//	    template <typename T, typename U> U end(const pair<T, U>& p) {
	//	        return p.second;
	//	    }
	//	}
	//	struct S {
	//	    int x;
	//	};
	//
	//	int main() {
	//	    S arr[5];
	//	    std::pair<S*, S*> p{arr, arr + 5};
	//	    for (const auto& r : p)
	//	        r.x;
	//	}
	public void testAutoTypeInRangeBasedFor_332883c() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S {
	//	    void f();
	//	};
	//	struct Vector {
	//		S* begin();
	//	};
	//	void test() {
	//		Vector v;
	//	    for (auto e : v) {
	//			e.f();
	//	    }
	//	}
	public void testAutoTypeInRangeBasedFor_359653() throws Exception {
		parseAndCheckBindings();
	}

	//	struct Iter {
	//	    Iter& operator++();
	//	    int& operator*();
	//	    friend bool operator!=(Iter, Iter);
	//	};
	//
	//	class C {};
	//	Iter begin(C&);
	//	Iter end(C&);
	//
	//	int main() {
	//	    for (auto x : C());  // ERROR: Symbol 'begin' could not be resolved
	//	}
	public void testRangedBasedFor_538517() throws Exception {
		parseAndCheckImplicitNameBindings();
	}

	//	typedef int T;
	//	struct B {
	//	    int a, b;
	//	    B(T) : a(0), b(0) {}
	//	};
	public void testMemberInitializer_333200() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T, typename U> auto add(T t, U u) -> decltype(t + u);
	//	template <typename T, typename U> auto add(T t, U u) -> decltype(t + u) {
	//	    return t + u;
	//	}
	public void testResolutionInTrailingReturnType_333256() throws Exception {
		parseAndCheckBindings();
	}

	//	struct Base {
	//		virtual bool waldo();
	//	};
	//	struct Derived : Base {
	//		virtual auto waldo() -> bool override;
	//	};
	public void testOverrideSpecifierAfterTrailingReturnType_489876() throws Exception {
		parseAndCheckBindings();
	}

	//	auto (*x1)() -> int;
	//	int (*x2)();
	//
	//	auto (*y1)() -> int(*)();
	//	int (*(*y2)())();
	//
	//	auto (*z1)() -> auto(*)() -> int(*)();
	//	int (*(*(*z2)())())();
	public void testTrailingReturnTypeInFunctionPointer() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();

		ICPPVariable x1 = bh.assertNonProblem("x1");
		ICPPVariable x2 = bh.assertNonProblem("x2");
		assertSameType(x1.getType(), x2.getType());

		ICPPVariable y1 = bh.assertNonProblem("y1");
		ICPPVariable y2 = bh.assertNonProblem("y2");
		assertSameType(y1.getType(), y2.getType());

		ICPPVariable z1 = bh.assertNonProblem("z1");
		ICPPVariable z2 = bh.assertNonProblem("z2");
		assertSameType(z1.getType(), z2.getType());
	}

	//	struct CHAINER {
	//	    CHAINER const & operator,(int x) const;
	//	};
	//	void test(const CHAINER& x) {
	//	    CHAINER c;
	//	    test((c,0,1));
	//	}
	public void testOverloadedCommaOpWithConstClassRef_334955() throws Exception {
		parseAndCheckBindings();
	}

	//	struct OK {int ok;};
	//	struct NOK {};
	//
	//	template<typename T> struct bos {
	//	  NOK operator<<(const void*);
	//	};
	//	template <typename T> struct os : bos<T> {};
	//	OK operator<<(bos<char>&, const char*);
	//
	//	void test() {
	//	  os<char> ss;
	//	  (ss << "").ok;
	//	}
	public void testOverloadedOperatorWithInheritanceDistance_335387() throws Exception {
		parseAndCheckBindings();
	}

	//	struct Object {
	//	    bool IsOk() const;
	//	};
	//
	//	struct Bitmap;
	//
	//	struct Region : Object {
	//	    Region(const Bitmap& bmp) {
	//	        Union(bmp);
	//	    }
	//
	//	    void Union(const Region&);
	//	    void Union(const Bitmap&);
	//	};
	//
	//	struct Bitmap : Object {};
	//
	//	void waldo(Bitmap& icon) {
	//	    icon.IsOk();  // ERROR: "Method 'IsOk' could not be resolved"
	//	}
	public void testBaseComputationWhileTypeIsIncomplete_498393() throws Exception {
		parseAndCheckBindings();
	}

	// namespace ns {int a;}
	// using ns::a;
	public void testPropertyOfUsingDeclaration() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		ICPPASTUsingDeclaration udecl = getDeclaration(tu, 1);
		ICPPASTQualifiedName qn = (ICPPASTQualifiedName) udecl.getName();
		assertFalse(qn.isDefinition());
		assertFalse(qn.getLastName().isDefinition());
		assertTrue(qn.isDeclaration());
		assertTrue(qn.getLastName().isDeclaration());
	}

	//	struct S{
	//		void foo(){}
	//	};
	//	void test(){
	//		S s[1];
	//		s->foo();
	//	}
	public void testMemberAccessForArray_347298() throws Exception {
		parseAndCheckBindings();
	}

	//	struct X {};
	//	struct Y : X {
	//		Y(){}
	//		Y(Y const & y){}
	//	};
	//	void test() {
	//		Y y;
	//		Y y2 = y;
	//      X x = y2;
	//	}
	public void testReferenceToCopyConstructor() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		ICPPASTFunctionDefinition fdef = getDeclaration(tu, 2);

		IASTDeclarationStatement dst = getStatement(fdef, 0);
		IASTDeclarator dtor = ((IASTSimpleDeclaration) dst.getDeclaration()).getDeclarators()[0];
		IBinding ctor = ((IASTImplicitNameOwner) dtor).getImplicitNames()[0].resolveBinding();
		assertTrue(ctor instanceof ICPPConstructor);
		assertEquals(0, ((ICPPConstructor) ctor).getType().getParameterTypes().length);

		dst = getStatement(fdef, 1);
		dtor = ((IASTSimpleDeclaration) dst.getDeclaration()).getDeclarators()[0];
		ctor = ((IASTImplicitNameOwner) dtor).getImplicitNames()[0].resolveBinding();
		assertTrue(ctor instanceof ICPPConstructor);
		assertEquals(1, ((ICPPConstructor) ctor).getType().getParameterTypes().length);

		dst = getStatement(fdef, 2);
		dtor = ((IASTSimpleDeclaration) dst.getDeclaration()).getDeclarators()[0];
		ctor = ((IASTImplicitNameOwner) dtor).getImplicitNames()[0].resolveBinding();
		assertTrue(ctor instanceof ICPPConstructor);
		assertEquals(1, ((ICPPConstructor) ctor).getType().getParameterTypes().length);
	}

	//	struct Foo {
	//	    void Method(int) {}
	//	    void Method() const {}
	//	};
	//	template<typename Arg> struct Callback {
	//	    Callback(void (Foo::*function)(Arg arg)) {
	//	    }
	//	};
	//	typedef Callback<int> MyCallback;
	//	void xx() {
	//		MyCallback x= MyCallback(&Foo::Method); // Invalid overload of 'Foo::Method'
	//	}
	public void testTypedefAsClassNameWithFunctionPtrArgument_350345() throws Exception {
		parseAndCheckBindings();
	}

	//	int func1(int input) {
	//	    return input;
	//	}
	//	void dut() {
	//	    int const_zero;
	//	    int lll = (func1(func1(const_zero))) + func1(const_zero);
	//	    int kkk = (func1(func1(const_zero))) + func1(const_zero);
	//	}
	//
	//	void f3(int (x), int y=x);
	//	void f2(int (x), int y=x) {
	//		x= 1;
	//	}
	public void testAmbiguityResolution_354599() throws Exception {
		parseAndCheckBindings();
	}

	//	struct A {
	//	  void method(A);
	//	};
	//
	//	struct B : A {
	//	  void method(B);
	//	  using A::method;
	//	};
	//
	//	struct C : B {
	//	};
	//
	//	void test() {
	//	  B b;
	//	  C c;
	//	  c.method(b);
	//	}
	public void testAmbiguityResolution_356268() throws Exception {
		parseAndCheckBindings();
	}

	//	void (g)(char);
	//	void (g)(int); //1
	//	void (g)(int); //2
	public void testFunctionRedeclarations() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		IFunction g1 = bh.assertNonProblem("g)(char)", 1);
		IFunction g2 = bh.assertNonProblem("g)(int); //1", 1);
		IFunction g3 = bh.assertNonProblem("g)(int); //2", 1);
		assertNotSame(g1, g2);
		assertSame(g2, g3);
	}

	//	int test() {
	//		extern int *e();
	//		if (auto r = e()) { return *r; }
	//		int r = 12;
	//		return r;
	//	}
	public void testVariableDeclarationInIfStatement() throws Exception {
		parseAndCheckBindings();
	}

	//	class A : A {
	//	};
	public void testRecursiveClassInheritance_357256() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPClassType c = bh.assertNonProblem("A", 1);
		assertEquals(0, SemanticQueries.getPureVirtualMethods(c, null).length);
	}

	//	template <typename T> struct CT1 {};
	//	template <typename T> struct CT2 {};
	//	typedef char Tdef;
	//	template<> struct CT1< CT2<int> > {
	//		CT1<Tdef> x;			   // Ambiguity causes lookup in CT1< CT2<int> >
	//	};
	//	template<> struct CT2<Tdef> {  // Accessed before ambiguity is resolved
	//	};
	public void testAmbiguityResolution_359364() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename T>
	//	struct A {
	//	  typedef char c;
	//	  static constexpr int method() {
	//	    return sizeof(c);
	//	  }
	//
	//	  static const int x = method();
	//	};
	//
	//	template<typename T, int x = A<int>::x>
	//	struct B;
	//
	//	template<>
	//	struct B<int> {
	//	};
	public void testAmbiguityResolution_427854() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S;
	//	struct S {
	//		S();
	//		~S();
	//		T();
	//		~T();
	//	};
	public void testErrorForDestructorWithWrongName_367590() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, false, false);
		IASTCompositeTypeSpecifier S;
		IASTProblemDeclaration p;
		IASTSimpleDeclaration s;

		S = getCompositeType(tu, 1);
		s = getDeclaration(S, 0);
		s = getDeclaration(S, 1);
		p = getDeclaration(S, 2);
		p = getDeclaration(S, 3);
	}

	//	typedef int int8_t __attribute__ ((__mode__ (__QI__)));
	//	typedef int int16_t __attribute__ ((__mode__ (__HI__)));
	//	typedef int int32_t __attribute__ ((__mode__ (__SI__)));
	//	typedef int int64_t __attribute__ ((__mode__ (__DI__)));
	//	typedef int word_t __attribute__ ((__mode__ (__word__)));
	//	void f(int8_t*) {}
	//	void f(int16_t*) {}
	//	void f(int32_t*) {}
	//	void f(int64_t*) {}
	//	void test(signed char* i8, short* i16, int* i32, long* i64, word_t* word) {
	//		f(i8);
	//		f(i16);
	//		f(i32);
	//		f(i64);
	//		f(word);
	//	}
	public void testModeAttribute_330635() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		String[] calls = { "f(i8)", "f(i16)", "f(i32)", "f(i64)", "f(word)" };
		ICPPFunction[] functions = new ICPPFunction[calls.length];
		for (int i = 0; i < calls.length; i++) {
			functions[i] = bh.assertNonProblem(calls[i], 1, ICPPFunction.class);
		}
		for (int i = 0; i < functions.length - 1; i++) {
			for (int j = 0; j < i; j++) {
				assertNotSame(calls[i] + " and " + calls[j] + " resolve to the same function", functions[i],
						functions[j]);
			}
		}
		assertSame(calls[calls.length - 1] + " and " + calls[calls.length - 2] + " resolve to different functions",
				functions[calls.length - 1], functions[calls.length - 2]);
	}

	//	void f(int x) try {
	//	} catch(...) {
	//		(void)x;
	//	}
	public void testParentScopeOfCatchHandler_376246() throws Exception {
		parseAndCheckBindings();
	}

	//	struct MyClass {
	//	    struct MyException {};
	//	    void doSomething() throw(MyException);
	//	};
	//	void MyClass::doSomething() throw (MyException) {}
	public void testScopeOfExceptionSpecification_377457() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace std {
	//		typedef decltype(nullptr) nullptr_t;
	//	}
	//	template <bool b> struct eval {
	//		int FALSE;
	//	};
	//	template <> struct eval<true> {
	//		int TRUE;
	//	};
	//	void checkptr(int*);
	//	void checkbool(bool b);
	//	void checkNullPtr(std::nullptr_t np);
	//	void test() {
	//		int* ip;
	//		checkptr(nullptr);
	//		checkNullPtr(0);
	//		checkNullPtr(nullptr);
	//		checkbool(nullptr < 0);
	//		checkbool(nullptr);
	//		checkbool(ip > nullptr);
	//		sizeof(nullptr);
	//		typeid(nullptr);
	//		if (false)
	//			throw nullptr;
	//		eval<nullptr == 0> e1; e1.TRUE;
	//		checkptr(true ? nullptr : nullptr);
	//	}
	//	template<typename T> void g( T* t );
	//	template<typename T> void h( T t );
	//	void testtemplates() {
	//		g( (float*) nullptr ); // deduces T = float
	//		h( 0 ); // deduces T = int
	//		h( nullptr ); // deduces T = nullptr_t
	//		h( (float*) nullptr ); // deduces T = float*
	//	}
	public void testNullptr_327298a() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace std {
	//		typedef decltype(nullptr) nullptr_t;
	//	}
	//	void checklvalue(int*&);
	//	void checkNullPtr(std::nullptr_t np);
	//	void test() {
	//		checkNullPtr(1);
	//		checklvalue(nullptr);
	//  }
	//	template<typename T> void g( T* t );
	//	template<typename T> void h( T t );
	//	void testtemplates() {
	//		g( nullptr ); // error
	//	}
	public void testNullptr_327298b() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		bh.assertProblem("checkNullPtr(1)", 12);
		bh.assertProblem("checklvalue(nullptr)", 11);
		bh.assertProblem("g( nullptr )", 1);
	}

	//	void f( char* );
	//	void f( int );
	//	void test2() {
	//		f( nullptr ); // calls f( char* )
	//		f( 0 ); // calls f( int )
	//	}
	public void testNullptr_327298c() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		IFunction f = bh.assertNonProblem("f( nullptr )", 1);
		assertEquals("void (char *)", ASTTypeUtil.getType(f.getType()));
		f = bh.assertNonProblem("f( 0 )", 1);
		assertEquals("void (int)", ASTTypeUtil.getType(f.getType()));
	}

	// void foo(struct S s);
	public void testParameterForwardDeclaration_379511() throws Exception {
		BindingAssertionHelper bh = new AST2AssertionHelper(getAboveComment(), CPP);
		ICPPClassType struct = bh.assertNonProblem("S", 1, ICPPClassType.class);
		IName[] declarations = bh.getTranslationUnit().getDeclarations(struct);
		assertEquals(1, declarations.length);
		assertEquals(bh.findName("S", 1), declarations[0]);
	}

	// struct F {};
	// struct S {
	//     friend F;
	// };
	public void testFriendClass() throws Exception {
		parseAndCheckBindings();
	}

	// struct F {};
	// typedef F T;
	// struct S {
	//     friend T;
	// };
	public void testFriendTypedef() throws Exception {
		parseAndCheckBindings();
	}

	// template<typename P>
	// struct T {
	//     friend P;
	// };
	public void testFriendTemplateParameter() throws Exception {
		parseAndCheckBindings();
	}

	//	struct foo {
	//	    foo();
	//	    ~foo();
	//	};
	//
	//	class bar {
	//	    friend foo::foo();
	//	    friend foo::~foo();
	//	};
	public void testFriendConstructorDestructor_400940() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace Hugo {
	//	    class C {
	//	        friend class Waldo;
	//	    };
	//	}
	//	using namespace Hugo;
	//	class Waldo {};
	//	void foo() {
	//	    Waldo c;  // error here
	//	}
	public void testFriendClassLookup_512932() throws Exception {
		parseAndCheckBindings();
	}

	// struct S {
	//     virtual void mFuncDecl() final;
	//     virtual void mFuncDef() final {}
	// };
	public void testFinalFunction() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);

		BindingAssertionHelper bindingHelper = new AST2AssertionHelper(code, true);

		CPPMethod functionDeclarationBinding = bindingHelper.assertNonProblem("mFuncDecl()", 9);
		assertFalse(functionDeclarationBinding.isOverride());
		assertTrue(functionDeclarationBinding.isFinal());
		IASTNode[] functionDeclarators = functionDeclarationBinding.getDeclarations();
		assertEquals(1, functionDeclarators.length);
		assertInstance(functionDeclarators[0], ICPPASTFunctionDeclarator.class);
		assertVirtualSpecifiers((ICPPASTFunctionDeclarator) functionDeclarators[0], false, true);

		CPPMethod functionDefinitionBinding = bindingHelper.assertNonProblem("mFuncDef()", 8);
		assertFalse(functionDefinitionBinding.isOverride());
		assertTrue(functionDefinitionBinding.isFinal());
		IASTFunctionDeclarator declarator = functionDefinitionBinding.getDefinition();
		assertInstance(declarator, ICPPASTFunctionDeclarator.class);
		assertVirtualSpecifiers((ICPPASTFunctionDeclarator) declarator, false, true);
	}

	// struct Base {
	//     virtual void mFuncDecl();
	//     virtual void mFuncDef(){}
	// };
	// struct S : public Base {
	//     void mFuncDecl() override;
	//     void mFuncDef() override {}
	// };
	public void testOverrideFunction() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);

		BindingAssertionHelper bindingHelper = new AST2AssertionHelper(code, true);

		CPPMethod functionDeclarationBinding = bindingHelper.assertNonProblem("mFuncDecl() override", 9);
		assertTrue(functionDeclarationBinding.isOverride());
		assertFalse(functionDeclarationBinding.isFinal());
		IASTDeclarator[] functionDeclarators = functionDeclarationBinding.getDeclarations();
		assertEquals(1, functionDeclarators.length);
		assertInstance(functionDeclarators[0], ICPPASTFunctionDeclarator.class);
		assertVirtualSpecifiers((ICPPASTFunctionDeclarator) functionDeclarators[0], true, false);

		CPPMethod functionDefinitionBinding = bindingHelper.assertNonProblem("mFuncDef() override", 8);
		assertTrue(functionDefinitionBinding.isOverride());
		assertFalse(functionDefinitionBinding.isFinal());
		IASTFunctionDeclarator declarator = functionDefinitionBinding.getDefinition();
		assertInstance(declarator, ICPPASTFunctionDeclarator.class);
		assertVirtualSpecifiers((ICPPASTFunctionDeclarator) declarator, true, false);
	}

	// struct Base {
	//     virtual void mFuncDecl();
	//     virtual void mFuncDef(){}
	// };
	// struct S : public Base {
	//     void mFuncDecl() final override;
	//     void mFuncDef() final override {}
	// };
	public void testOverrideFinalFunction() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);

		BindingAssertionHelper bindingHelper = new AST2AssertionHelper(code, true);

		CPPMethod functionDeclarationBinding = bindingHelper.assertNonProblem("mFuncDecl() final", 9);
		assertTrue(functionDeclarationBinding.isOverride());
		assertTrue(functionDeclarationBinding.isFinal());
		IASTDeclarator[] functionDeclarators = functionDeclarationBinding.getDeclarations();
		assertEquals(1, functionDeclarators.length);
		assertInstance(functionDeclarators[0], ICPPASTFunctionDeclarator.class);
		assertVirtualSpecifiers((ICPPASTFunctionDeclarator) functionDeclarators[0], true, true);

		CPPMethod functionDefinitionBinding = bindingHelper.assertNonProblem("mFuncDef() final", 8);
		assertTrue(functionDefinitionBinding.isOverride());
		assertTrue(functionDefinitionBinding.isFinal());
		IASTFunctionDeclarator declarator = functionDefinitionBinding.getDefinition();
		assertInstance(declarator, ICPPASTFunctionDeclarator.class);
		assertVirtualSpecifiers((ICPPASTFunctionDeclarator) declarator, true, true);
	}

	private void assertVirtualSpecifiers(ICPPASTFunctionDeclarator declarator, boolean expectOverride,
			boolean expectFinal) {
		assertEquals(expectOverride, declarator.isOverride());
		assertEquals(expectFinal, declarator.isFinal());
	}

	// struct Base {
	// };
	// struct S final : public Base {
	// };
	public void testFinalClass() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);

		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		CPPClassType structBase = bh.assertNonProblem("Base {", 4);
		assertFalse(structBase.isFinal());
		IASTNode baseDefinitionName = structBase.getDefinition();
		IASTNode baseDefinition = baseDefinitionName.getParent();
		assertInstance(baseDefinition, ICPPASTCompositeTypeSpecifier.class);
		assertFalse(((ICPPASTCompositeTypeSpecifier) baseDefinition).isFinal());

		CPPClassType structS = bh.assertNonProblem("S", 1);
		assertTrue(structS.isFinal());
		IASTNode sDefinitionName = structS.getDefinition();
		IASTNode sDefinition = sDefinitionName.getParent();
		assertInstance(sDefinition, ICPPASTCompositeTypeSpecifier.class);
		assertTrue(((ICPPASTCompositeTypeSpecifier) sDefinition).isFinal());
	}

	// struct S {
	//     template<typename T>
	// 	   void foo(T t) final {
	// 	   }
	// };
	//
	// int main() {
	// 	   S s;
	// 	   s.foo(1);
	// }
	public void testFinalTemplateMethod() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);

		BindingAssertionHelper bindingHelper = new AST2AssertionHelper(code, true);

		ICPPMethod fooTemplate = bindingHelper.assertNonProblem("foo(T", 3);
		assertFalse(fooTemplate.isOverride());
		assertTrue(fooTemplate.isFinal());
	}

	// void foo() {
	//     int final, override;
	//     final = 4;
	//     override = 2;
	// }
	public void testFinalAndOverrideVariables() throws Exception {
		parseAndCheckBindings();
	}

	// struct S {
	//     int i;
	// };
	// void foo(struct S final) {
	//     final.i = 23;
	// }
	public void testFinalParameter() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S __final {};
	//	struct T { void foo() __final; };
	public void testFinalGccExtension_442457() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPClassType s = bh.assertNonProblem("S");
		assertTrue(s.isFinal());
		ICPPMethod foo = bh.assertNonProblem("foo");
		assertTrue(foo.isFinal());
	}

	//	struct S {};
	//	bool b1 = __is_trivially_copyable(S);
	//	bool b2 = __is_trivially_assignable(S, S&);
	//	bool b3 = __is_trivially_constructible(S, int, char*);
	//	// Test that __is_trivially_constructible can take parameter packs
	//	template <typename... Args>
	//	struct U {
	//		static const bool value = __is_trivially_constructible(S, Args...);
	//	};
	public void testParsingOfGcc5TypeTraitIntrinsics_485713() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true /* use GNU extensions */);
	}

	//	struct S1 {};
	//	S1 s1;
	//	const int i= 1;
	//	template<int I> struct CT {};
	//	typedef int TD;
	//	bool operator==(S1 a, int r );
	//	static const int x = sizeof(CT<i>((TD * (CT<sizeof(s1 == 1)>::*)) 0));
	//	template<int I> bool operator==(S1 a, const CT<I>& r);
	public void testOrderInAmbiguityResolution_390759() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace N {
	//	    enum E { A, B };
	//	    void bar(E);
	//	}
	//	struct S {
	//	    void operator()(N::E);
	//	};
	//	S bar;
	//	int main() {
	//	    bar(N::A);
	//	}
	public void testADLForFunctionObject_388287() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace A {
	//		template <typename T>
	//		void foo(T);
	//	}
	//
	//	namespace B {
	//		template <typename T>
	//		void foo(T);
	//
	//		struct S {};
	//	}
	//
	//	struct outer : B::S {
	//		struct waldo {};
	//		enum E { };
	//	};
	//
	//	using A::foo;
	//
	//	int main() {
	//		foo(outer::waldo{});
	//		foo(outer::E{});
	//	}
	public void testADL_485710() throws Exception {
		parseAndCheckBindings();
	}

	//	template <bool> struct A {};
	//	template <>
	//	struct A<false> {
	//	  typedef int type;
	//	};
	//	struct S {};
	//	const bool b = __is_base_of(S, int);
	//	typedef A<b>::type T;
	public void testIsBaseOf_395019() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//	template<typename T, T v>
	//	struct integral_constant {
	//	  static constexpr T value = v;
	//	  typedef integral_constant<T, v> type;
	//	};
	//
	//	typedef integral_constant<bool, true> true_type;
	//
	//	typedef integral_constant<bool, false> false_type;
	//
	//	template<typename Base, typename Derived>
	//	struct is_base_of : public integral_constant<bool, __is_base_of(Base, Derived)> {};
	//
	//	template<bool, typename T = void>
	//	struct enable_if {};
	//
	//	template<typename T>
	//	struct enable_if<true, T> {
	//	  typedef T type;
	//	};
	//
	//	template <class BaseType, class SubType>
	//	using EnableIfIsBaseOf =
	//	    typename enable_if<is_base_of<BaseType, SubType>::value, int>::type;
	//
	//	class A {};
	//
	//	template <typename T>
	//	class B {};
	//
	//	template <typename T, EnableIfIsBaseOf<A, T> = 0>
	//	using Waldo = B<T>;
	//
	//	Waldo<A> c;
	public void testIsBaseOf_446094() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//  struct Bool { Bool(bool); };
	//  struct Char { Char(char); };
	//  struct Short { Short(short); };
	//  struct Int { Int(int); };
	//  struct UInt { UInt(unsigned int); };
	//  struct Long { Long(long); };
	//  struct ULong { ULong(unsigned long); };
	//  struct Float { Float(float); };
	//  struct Double { Double(double); };
	//  struct LongDouble { LongDouble(long double); };
	//  void fbool(Bool);
	//  void fchar(Char);
	//  void fshort(Short);
	//  void fint(Int);
	//  void flong(Long);
	//  void fuint(UInt);
	//  void fulong(ULong);
	//  void ffloat(Float);
	//  void fdouble(Double);
	//  void flongdouble(LongDouble);
	//  enum UnscopedEnum : int { x, y, z };
	//
	//  int main() {
	//      bool vbool;
	//      char vchar;
	//      short vshort;
	//      unsigned short vushort;
	//      int vint;
	//      unsigned int vuint;
	//      long vlong;
	//      float vfloat;
	//      double vdouble;
	//      long double vlongdouble;
	//      UnscopedEnum vue;
	//
	//      // Narrowing conversions
	//      fint({vdouble});
	//      ffloat({vlongdouble});
	//      ffloat({vdouble});
	//      fdouble({vlongdouble});
	//      fdouble({vint});
	//      fdouble({vue});
	//      fshort({vint});
	//      fuint({vint});
	//      fint({vuint});
	//      fulong({vshort});
	//      fbool({vint});
	//      fchar({vint});
	//
	//      // Non-narrowing conversions
	//      fint({vshort});
	//      flong({vint});
	//      fuint({vushort});
	//      flong({vshort});
	//      fulong({vuint});
	//      fulong({vushort});
	//      fdouble({vfloat});
	//      flongdouble({vfloat});
	//      flongdouble({vdouble});
	//      fint({vbool});
	//      fint({vchar});
	//  }
	public void testNarrowingConversionsInListInitialization_389782() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		// Narrowing conversions
		helper.assertProblemOnFirstIdentifier("fint({vdouble");
		helper.assertProblemOnFirstIdentifier("ffloat({vlongdouble");
		helper.assertProblemOnFirstIdentifier("ffloat({vdouble");
		helper.assertProblemOnFirstIdentifier("fdouble({vlongdouble");
		helper.assertProblemOnFirstIdentifier("fdouble({vint");
		helper.assertProblemOnFirstIdentifier("fdouble({vue");
		helper.assertProblemOnFirstIdentifier("fshort({vint");
		helper.assertProblemOnFirstIdentifier("fuint({vint");
		helper.assertProblemOnFirstIdentifier("fint({vuint");
		helper.assertProblemOnFirstIdentifier("fulong({vshort");
		helper.assertProblemOnFirstIdentifier("fbool({vint");
		helper.assertProblemOnFirstIdentifier("fchar({vint");

		// Non-narrowing conversions
		helper.assertNonProblemOnFirstIdentifier("fint({vshort");
		helper.assertNonProblemOnFirstIdentifier("flong({vint");
		helper.assertNonProblemOnFirstIdentifier("fuint({vushort");
		helper.assertNonProblemOnFirstIdentifier("flong({vshort");
		helper.assertNonProblemOnFirstIdentifier("fulong({vuint");
		helper.assertNonProblemOnFirstIdentifier("fulong({vushort");
		helper.assertNonProblemOnFirstIdentifier("fdouble({vfloat");
		helper.assertNonProblemOnFirstIdentifier("flongdouble({vfloat");
		helper.assertNonProblemOnFirstIdentifier("flongdouble({vdouble");
		helper.assertNonProblemOnFirstIdentifier("fint({vbool");
		helper.assertNonProblemOnFirstIdentifier("fint({vchar");
	}

	//	void waldo(char x);
	//
	//	void test() {
	//	  waldo({1});
	//	}
	public void testNarrowingConversionInListInitialization_491748() throws Exception {
		parseAndCheckBindings();
	}

	//	namespace std {
	//		struct string {};
	//	 	struct exception {};
	//	}
	//	void f(){}
	//
	//	int problemA(int i) {
	//		return i ? throw 7 : i;
	//	}
	//	int problemB(int i) {
	//		return i ? throw std::string{} : i;
	//	}
	//	void ploblemC(int i) {
	//		return i ? throw std::exception() : throw 3;
	//	}
	//	void ploblemD(int i) {
	//		return i ? throw std::exception() : f();
	//	}
	//	std::string ploblemE(int i) {
	//		return i ? throw 3 : "a";
	//	}
	//	std::string ploblemF(int i) {
	//		return (i<2 ? throw 2 : "x") ? (i>2 ? throw 3 : "d") : (i>22 ? throw 4 : "e");
	//	}
	//	auto ploblemG(int i) ->decltype(i ? throw 3 : "d"){
	//		return i ? throw 3 : "d" ;
	//	}
	//	void fine1(int i) {
	//		return i ? f() : f();
	//	}
	//	std::string fine2(int i) {
	//		return i ? "a" : "b";
	//	}
	public void testThrowExpressionInConditional_396663() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//	struct A {};
	//
	//	struct B : A {};
	//
	//	void foo(A*);
	//
	//	int main() {
	//	    foo(true ? new A() : new B());
	//	}
	public void testBasePointerConverstionInConditional_462705() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S {};
	//	typedef S* S_ptr;
	//	void waldo(S*);
	//	S_ptr s;
	//	int main() {
	//	    waldo(false ? s : 0);
	//	}
	public void testTypedefOfPointerInConditional_481078() throws Exception {
		parseAndCheckBindings();
	}

	//	template <bool>
	//	struct enable_if {
	//	};
	//	template <>
	//	struct enable_if<true> {
	//	    typedef void type;
	//	};
	//	struct base {};
	//	struct derived : base {};
	//	typedef enable_if<__is_base_of(base, derived)>::type T;
	public void testIsBaseOf_399353() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//	struct base {};
	//	struct derived : base {};
	//	typedef derived derived2;
	//	const bool value = __is_base_of(base, derived2);
	public void testIsBaseOf_409100() throws Exception {
		BindingAssertionHelper b = getAssertionHelper();
		IVariable var = b.assertNonProblem("value");
		assertConstantValue(1 /*true */, var);
	}

	//  namespace NS {
	//  class Enclosing {
	//    class Inner {};
	//  };
	//  }
	public void testNestedClassScopeInlineDefinition_401661() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);

		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		ICPPClassType Enclosing = bh.assertNonProblem("Enclosing", 9);
		ICPPClassType Inner = bh.assertNonProblem("Inner", 5);

		assertEquals(Enclosing.getCompositeScope(), Inner.getScope());
	}

	//  namespace NS {
	//  class Enclosing {
	//    class Inner;
	//  };
	//  }
	//  class NS::Enclosing::Inner{};
	public void testNestedClassScopeSeparateDefinition_401661() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);

		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		ICPPClassType Enclosing = bh.assertNonProblem("Enclosing", 9);
		ICPPClassType Inner = bh.assertNonProblem("Inner;", 5);

		assertEquals(Enclosing.getCompositeScope(), Inner.getScope());
	}

	//  namespace NS {
	//  class Inner {};
	//  }
	public void testClassScopeInlineDefinition_401661() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);

		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		ICPPNamespace NamespaceNS = bh.assertNonProblem("NS {", 2);
		ICPPClassType Inner = bh.assertNonProblem("Inner", 5);

		assertEquals(NamespaceNS.getNamespaceScope(), Inner.getScope());
	}

	//  namespace NS {
	//  class Inner;
	//  }
	//  class NS::Inner{};
	public void testClassScopeSeparateDefinition_401661() throws Exception {
		String code = getAboveComment();
		parseAndCheckBindings(code);

		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		ICPPNamespace NamespaceNS = bh.assertNonProblem("NS {", 2);
		ICPPClassType Inner = bh.assertNonProblem("Inner;", 5);

		assertEquals(NamespaceNS.getNamespaceScope(), Inner.getScope());
	}

	//  class AClass {
	//    int defaultMemberVariable;
	//    void defaultMemberFunction();
	//    class defaultNestedClass {};
	//  public:
	//    int publicMemberVariable;
	//    void publicMemberFunction();
	//    class publicNestedClass {};
	//  protected:
	//    int protectedMemberVariable;
	//    void protectedMemberFunction();
	//    class protectedNestedClass {};
	//  private:
	//    int privateMemberVariable;
	//    void privateMemberFunction();
	//    class privateNestedClass {};
	//  };
	public void testMemberAccessibilities() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();

		ICPPClassType aClass = bh.assertNonProblem("AClass");

		ICPPField defaultMemberVariable = bh.assertNonProblem("defaultMemberVariable");
		assertVisibility(ICPPClassType.v_private, aClass.getVisibility(defaultMemberVariable));
		ICPPMethod defaultMemberFunction = bh.assertNonProblem("defaultMemberFunction");
		assertVisibility(ICPPClassType.v_private, aClass.getVisibility(defaultMemberFunction));
		ICPPClassType defaultNestedClass = bh.assertNonProblem("defaultNestedClass");
		assertVisibility(ICPPClassType.v_private, aClass.getVisibility(defaultNestedClass));

		ICPPField publicMemberVariable = bh.assertNonProblem("publicMemberVariable");
		assertVisibility(ICPPClassType.v_public, aClass.getVisibility(publicMemberVariable));
		ICPPMethod publicMemberFunction = bh.assertNonProblem("publicMemberFunction");
		assertVisibility(ICPPClassType.v_public, aClass.getVisibility(publicMemberFunction));
		ICPPClassType publicNestedClass = bh.assertNonProblem("publicNestedClass");
		assertVisibility(ICPPClassType.v_public, aClass.getVisibility(publicNestedClass));

		ICPPField protectedMemberVariable = bh.assertNonProblem("protectedMemberVariable");
		assertVisibility(ICPPClassType.v_protected, aClass.getVisibility(protectedMemberVariable));
		ICPPMethod protectedMemberFunction = bh.assertNonProblem("protectedMemberFunction");
		assertVisibility(ICPPClassType.v_protected, aClass.getVisibility(protectedMemberFunction));
		ICPPClassType protectedNestedClass = bh.assertNonProblem("protectedNestedClass");
		assertVisibility(ICPPClassType.v_protected, aClass.getVisibility(protectedNestedClass));

		ICPPField privateMemberVariable = bh.assertNonProblem("privateMemberVariable");
		assertVisibility(ICPPClassType.v_private, aClass.getVisibility(privateMemberVariable));
		ICPPMethod privateMemberFunction = bh.assertNonProblem("privateMemberFunction");
		assertVisibility(ICPPClassType.v_private, aClass.getVisibility(privateMemberFunction));
		ICPPClassType privateNestedClass = bh.assertNonProblem("privateNestedClass");
		assertVisibility(ICPPClassType.v_private, aClass.getVisibility(privateNestedClass));
	}

	//	int main() {
	//		int i = 0;
	//		__sync_bool_compare_and_swap(& i, 0, 1);
	//		__sync_val_compare_and_swap(&i, 1, 2);
	//		__sync_synchronize();
	//	}
	public void testGNUSyncBuiltins_389578() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//	int main() {
	//		void* p;
	//		__sync_bool_compare_and_swap(&p, p, p);
	//		__sync_fetch_and_add(&p, p, 2);
	//	}
	public void testGNUSyncBuiltinsOnVoidPtr_533822() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//	// __int128_t and __uint128_t are available but are not keywords.
	//	void a() {
	//	  __int128_t s;
	//	  __uint128_t t;
	//	}
	//	void b() {
	//	  int __int128_t;
	//	  int __uint128_t;
	//	}
	public void testGCCIntBuiltins_444577() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//	template <typename T>
	//	struct underlying_type {
	//	    typedef __underlying_type(T) type;
	//	};
	//
	//	enum class e_fixed_short1 : short;
	//	enum class e_fixed_short2 : short { a = 1, b = 2 };
	//
	//	enum class e_scoped { a = 1, b = 2 };
	//
	//	enum e_unsigned { a1 = 1, b1 = 2 };
	//	enum e_int { a2 = -1, b2 = 1 };
	//	enum e_ulong { a3 = 5000000000, b3 };
	//	enum e_long { a4 = -5000000000, b4 = 5000000000 };
	//
	//	typedef underlying_type<e_fixed_short1>::type short1_type;
	//	typedef underlying_type<e_fixed_short2>::type short2_type;
	//
	//	typedef underlying_type<e_scoped>::type scoped_type;
	//
	//	typedef underlying_type<e_unsigned>::type unsigned_type;
	//	typedef underlying_type<e_int>::type int_type;
	//	typedef underlying_type<e_ulong>::type ulong_type;
	//	typedef underlying_type<e_long>::type loong_type;
	public void testUnderlyingTypeBuiltin_411196() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		assertSameType((ITypedef) helper.assertNonProblem("short1_type"), CPPBasicType.SHORT);
		assertSameType((ITypedef) helper.assertNonProblem("short2_type"), CPPBasicType.SHORT);

		assertSameType((ITypedef) helper.assertNonProblem("scoped_type"), CPPBasicType.INT);

		assertSameType((ITypedef) helper.assertNonProblem("unsigned_type"), CPPBasicType.UNSIGNED_INT);
		assertSameType((ITypedef) helper.assertNonProblem("int_type"), CPPBasicType.INT);
		assertSameType((ITypedef) helper.assertNonProblem("ulong_type"), CPPBasicType.UNSIGNED_LONG);
		assertSameType((ITypedef) helper.assertNonProblem("loong_type"), CPPBasicType.LONG);
	}

	//	template <typename T>
	//	struct underlying_type {
	//		using type = __underlying_type(T);
	//	};
	//	enum class E : short {};
	//	using target = underlying_type<E>::type;
	public void testUnderlyingType_548954() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		assertSameType((ITypedef) helper.assertNonProblem("target"), CPPBasicType.SHORT);
	}

	//	template <typename T>
	//	struct underlying_type {
	//	    typedef __underlying_type(T) type;
	//	};
	//
	//	enum class FooBar { Foo, Bar };
	//	using Alias = FooBar;
	//
	//	void bar(underlying_type<Alias>::type);
	//
	//	void test() {
	//	  bar(2);
	//	}
	public void testUnderlyingType_540909() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true /* use GNU extensions */);
	}

	//	void ptrFunc(void*);
	//	void intFunc(int);
	//	void foo(int* pi, unsigned i) {
	//		ptrFunc(__builtin_assume_aligned(pi, 4));
	//		intFunc(__builtin_constant_p(i));
	//		intFunc(__builtin_expect(i, 0));
	//		ptrFunc(__builtin_return_address(4));
	//	}
	public void testGCCBuiltins_512932a() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true);
	}

	//	void ptrFunc(void*);
	//	void intFunc(int);
	//	void foo(int* pi, unsigned i) {
	//		ptrFunc(__builtin_assume_aligned(i, 4));
	//		ptrFunc(__builtin_constant_p(i));
	//		ptrFunc(__builtin_constant_p(pI));
	//		intFunc(__builtin_expect(pI, (int*)0));
	//		ptrFunc(__builtin_expect(i, 0));
	//	}
	public void testGCCBuiltins_512932b() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertProblem("__builtin_assume_aligned", "__builtin_assume_aligned");
		helper.assertProblem("ptrFunc(__builtin_constant_p(i", "ptrFunc");
		helper.assertProblem("ptrFunc(__builtin_constant_p(pI", "ptrFunc");
		helper.assertProblem("__builtin_expect(pI", "__builtin_expect");
		helper.assertProblem("ptrFunc(__builtin_expect", "ptrFunc");
	}

	// namespace A {
	//   int a;
	//   namespace B {
	//     int b;
	//     namespace C {
	//       int c;
	//     }
	//     namespace A {
	//       int a;
	//     }
	//   }
	// }
	public void testQualifiedNameLookup() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP);

		IScope scope = tu.getScope();
		assertNotNull(scope);

		IBinding[] bindings = CPPSemantics.findBindingsForQualifiedName(scope, "  A::a");
		assertNotNull(bindings);
		assertEquals(1, bindings.length);
		IBinding a = bindings[0];
		assertEquals("a", a.getName());

		bindings = CPPSemantics.findBindingsForQualifiedName(scope, "A::B::b	");
		assertNotNull(bindings);
		assertEquals(1, bindings.length);
		IBinding b = bindings[0];
		assertEquals("b", b.getName());

		bindings = CPPSemantics.findBindingsForQualifiedName(scope, "A::	B  ::C::c");
		assertNotNull(bindings);
		assertEquals(1, bindings.length);
		IBinding c = bindings[0];
		assertEquals("c", c.getName());

		// From the level of c, there should be two A::a (::A::a and ::A::B::A::a).
		IScope scopeC = c.getScope();
		assertNotNull(scopeC);
		bindings = CPPSemantics.findBindingsForQualifiedName(scopeC, "A::a");
		assertNotNull(bindings);
		assertEquals(2, bindings.length);

		// From the level of c, there should be only one ::A::a.
		assertNotNull(scopeC);
		bindings = CPPSemantics.findBindingsForQualifiedName(scopeC, "::A::a");
		assertNotNull(bindings);
		assertEquals(1, bindings.length);
	}

	//	struct Test {
	//		operator char* &();
	//
	//		void test(char) {
	//			Test a;
	//			test(*a);
	//		}
	//	};
	public void testBuiltInOperator_423396() throws Exception {
		parseAndCheckBindings();
	}

	//	struct Test {
	//		typedef void (*TypeFunc)(char);
	//		operator TypeFunc* &();
	//
	//		void test(TypeFunc) {
	//			Test a;
	//			test(*a);
	//		}
	//	};
	public void testBuiltInOperatorFunctionType_423396() throws Exception {
		parseAndCheckBindings();
	}

	//	template <unsigned N>
	//	void waldo(const char (&)[N]);
	//	void foo() {
	//	    waldo(__FUNCTION__);
	//	}
	public void testTypeOfBuiltinSymbol_512932() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S {
	//	  int S;
	//	};
	//	void tint(int);
	//	void test() {
	//	  S s;
	//	  tint(s.S);
	//	}
	public void testFieldWithSameNameAsClass_326750() throws Exception {
		parseAndCheckBindings();
	}

	//	void waldo(void(*)());
	//
	//	int main() {
	//	    waldo([](){});
	//	}
	public void testConversionFromLambdaToFunctionPointer_424765() throws Exception {
		parseAndCheckBindings();
	}

	//	void bar(void *a1, void *a2) __attribute__((nonnull(1, 2)));
	public void testGCCAttributeSequence_416430() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, true);
		IASTDeclaration[] declarations = tu.getDeclarations();
		assertEquals(1, declarations.length);
		IASTDeclaration declaration = declarations[0];
		IASTSimpleDeclaration functionDefinition = assertInstance(declaration, IASTSimpleDeclaration.class);
		IASTDeclarator[] declarators = functionDefinition.getDeclarators();
		assertEquals(1, declarators.length);
		IASTDeclarator declarator = declarators[0];
		IASTFunctionDeclarator functionDeclarator = assertInstance(declarator, IASTFunctionDeclarator.class);
		IASTAttribute[] attributes = declarator.getAttributes();
		assertEquals(1, attributes.length);
		IASTAttribute nonnullAttribute = attributes[0];
		assertEquals("nonnull(1, 2)", String.valueOf(nonnullAttribute.getRawSignature()));
		IASTToken argumentClause = nonnullAttribute.getArgumentClause();
		IASTTokenList argumentTokenList = assertInstance(argumentClause, IASTTokenList.class);
		IASTToken[] argumentTokens = argumentTokenList.getTokens();
		assertEquals(3, argumentTokens.length);
		assertEquals('1', argumentTokens[0].getTokenCharImage()[0]);
		assertEquals(',', argumentTokens[1].getTokenCharImage()[0]);
		assertEquals('2', argumentTokens[2].getTokenCharImage()[0]);
	}

	//	struct MyStruct {
	//	    struct Inner {
	//	        int waldo;
	//	    } Inner;
	//	};
	//
	//	void foo() {
	//	    struct MyStruct::Inner in;
	//	    in.waldo;
	//	}
	public void testFieldAndNestedTypeWithSameName_425033() throws Exception {
		parseAndCheckBindings();
	}

	//	void f(double (&(x)));
	public void testParenthesizedReferenceArgument_424898() throws Exception {
		parseAndCheckBindings();
	}

	//	constexpr int naive_fibonacci(int x) {
	//		return x == 0 ? 0
	//			 : x == 1 ? 1
	//			 : naive_fibonacci(x - 2) + naive_fibonacci(x - 1);
	//	}
	//
	//	constexpr int waldo = naive_fibonacci(5);
	public void testConditionalExpressionFolding_429891() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		IVariable waldo = helper.assertNonProblem("waldo");
		assertConstantValue(5, waldo);
	}

	//	constexpr int naive_fibonacci(int x) {
	//		return x == 0 ? 0
	//			 : x == 1 ? 1
	//			 : naive_fibonacci(x - 2) + naive_fibonacci(x - 1);
	//	}
	//
	//	constexpr int waldo = naive_fibonacci(50);
	public void testConstexprEvaluationLimit_429891() throws Exception {
		// Here we're just checking that the computation of the initial
		// value finishes (with a null result) in a reasonable time.
		// If we tried to run the computation of naive_fibonacci(50)
		// to its end, the IDE would appear to hang.
		BindingAssertionHelper helper = getAssertionHelper();
		IVariable waldo = helper.assertNonProblem("waldo");
		assertNull(waldo.getInitialValue().numberValue());
	}

	//	constexpr int foo(int a = 42) {
	//		return a;
	//	}
	//	constexpr int waldo = foo();
	public void testNameLookupInDefaultArgument_432701() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		IVariable waldo = helper.assertNonProblem("waldo");
		assertConstantValue(42, waldo);
	}

	//	struct S1 { S1(int); };
	//	struct S2 { void operator()(int); };
	//	S2 s2;
	//	int main() {
	//		S1(42);
	//		s2(43);
	//	}
	public void testICPPASTFunctionCallExpression_getOverload_441701() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		ICPPASTFunctionCallExpression call1 = helper.assertNode("S1(42)");
		ICPPFunction constructor = helper.assertNonProblem("S1(int)", "S1");
		assertEquals(constructor, call1.getOverload());

		ICPPASTFunctionCallExpression call2 = helper.assertNode("s2(43)");
		ICPPFunction operator = helper.assertNonProblem("operator()");
		assertEquals(operator, call2.getOverload());
	}

	//  void f(int &&a);
	public void testRValueReferenceSignature_427856() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		IASTSimpleDeclaration sd = (IASTSimpleDeclaration) tu.getDeclarations()[0];
		isParameterSignatureEqual(sd.getDeclarators()[0], "(int&&)");
	}

	//	struct S { S(int); };
	//	void find(S&&);
	//	int main() {
	//		int waldo = 42;
	//		find(waldo);
	//	}
	public void testRValueReferenceBindingToTemporary_470943() throws Exception {
		parseAndCheckBindings();
	}

	//	constexpr int waldo1 = 42;
	//	constexpr auto waldo2 = 43;
	public void testConstexprVariableIsConst_451091() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPVariable waldo1 = helper.assertNonProblem("waldo1");
		ICPPVariable waldo2 = helper.assertNonProblem("waldo2");
		// constexpr on a variable *should* make it const
		assertSameType(waldo1.getType(), CommonCPPTypes.constInt);
		assertSameType(waldo2.getType(), CommonCPPTypes.constInt);
	}

	//	constexpr int* waldo;
	public void testConstexprPointerVariable_541670() throws Exception {
		getAssertionHelper().assertVariableType("waldo", CommonCPPTypes.constPointerToInt);
	}

	//	constexpr int waldo1();
	//	constexpr int (*waldo2())(int);
	//	struct S { constexpr int waldo3(); };
	public void testTypeOfConstexprFunction_451090() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPFunction waldo1 = helper.assertNonProblem("waldo1");
		ICPPFunction waldo2 = helper.assertNonProblem("waldo2");
		ICPPFunction waldo3 = helper.assertNonProblem("waldo3");
		// constexpr on a function *should not* make its return type const
		assertSameType(waldo1.getType().getReturnType(), CommonCPPTypes.int_);
		assertSameType(waldo2.getType().getReturnType(), new CPPPointerType(
				new CPPFunctionType(CommonCPPTypes.int_, new IType[] { CommonCPPTypes.int_ }, null)));
		// constexpr on a method *should not* make the method const
		assertSameType(waldo3.getType(), new CPPFunctionType(CommonCPPTypes.int_, new IType[] {}, null));
	}

	//	void waldo() noexcept;
	public void testASTCopyForNoexceptDefault_bug456207() throws Exception {
		parseAndCheckBindings();
	}

	//	template <typename> struct waldo { waldo(int); };
	//	auto x = static_cast<waldo<int>>(0);
	public void testTemplateIdInsideCastOperator_460080() throws Exception {
		parseAndCheckBindings();
	}

	//	alignas(8) int x;
	//	alignas(int) char y;
	//	alignas(16) struct { int x; int y; };
	//	alignas(8) enum { A, B, C };
	//	struct S {
	//		alignas(long long) int x;
	//		alignas(decltype(x)) int y;
	//	};
	//	alignas(32) S s;
	//	template <typename... T>
	//	struct U {
	//		alignas(S) int x;
	//		alignas(T...) char y;
	//	};
	//	template <int... N>
	//	struct Y {
	//		alignas(N...) char x;
	//	};
	public void testAlignas_451082() throws Exception {
		parseAndCheckBindings();
	}

	//	struct alignas(16) Node {};
	//	enum alignas(8) E { E1, E2 };
	public void testAlignas_475739() throws Exception {
		parseAndCheckBindings();
	}

	//	__attribute__((section(".example"))) alignas(4) static int waldo;
	public void testAlignasAfterAttribute_538615() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true /* use GNU extensions */);
	}

	// int operator "" _A(unsigned long long i) { return 1; }
	// int operator "" _B(long double d) { return 1; }
	// int operator "" _C(const char* s, unsigned int sz) { return sz; }
	// int operator "" _D(const wchar_t* s, unsigned int sz) { return sz; }
	// int operator "" _E(const char16_t* s, unsigned int sz) { return sz; }
	// int operator "" _F(const char32_t* s, unsigned int sz) { return sz; }
	// int operator "" _G(char c) { return (int)c; }
	// constexpr double operator "" _km_to_miles(long double km) { return km * 0.6213; }
	public void testSimpleUserDefinedLiteralOperators() throws Exception {
		parseAndCheckBindings();
	}

	//	int integers[] = {
	//			1,
	//			1U,
	//			1L,
	//			1LL,
	//			1ULL,
	//			1suff,
	//			1_suff,
	//			0x3003,
	//			0x3003U,
	//			0x3003L,
	//			0x3003LL,
	//			0x3003ULL,
	//			0x3003suff,
	//			0x3003_suff,
	//			0xabcdef,
	//			0xABCDEF,
	//			0Xabcdef,
	//			0xABCDEF,
	//			0xABCDEFU,
	//			0xABCDEFL,
	//			0xABCDEFULL,
	//			0xABCDEFsuff,
	//			0xABCDEF_suff,
	//			01,
	//			01U,
	//			01L,
	//			07LL,
	//			04ULL,
	//			01_suff,
	//			1ULL << 34,
	//	};
	public void testIntegerUserDefinedLiterals() throws Exception {
		parseAndCheckBindings();
	}

	//	double numbers[] = {
	//		1f,
	//		1.f,
	//		1.X,
	//		1.0x,
	//		0x01p3,
	//		0x01p3XX,
	//		1._X
	//	};
	public void testDoublesUserDefinedLiterals() throws Exception {
		parseAndCheckBindings();
	}

	// char c1 = '0'_suff;
	// char c2 = '0'suff;
	// char* c3 = "Hello"_suff;
	// char* c4 = "Hello"suff;
	public void testCharStringUserDefinedLiterals() throws Exception {
		parseAndCheckBindings();
	}

	// class Ret {};
	// Ret operator "" _X(unsigned long long i) { return Ret(); }
	// auto test = 123_X;
	public void testUserDefinedLiteralOperatorTypes1() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(long double i) { return Ret(); }
	// auto test = 12.3_X;
	public void testUserDefinedLiteralOperatorTypes2() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char* s) { return Ret(); }
	// auto test = 123_X;
	public void testUserDefinedLiteralOperatorTypes1a() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char* s) { return Ret(); }
	// auto test = 12.3_X;
	public void testUserDefinedLiteralOperatorTypes2a() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(unsigned long long d) { return Ret(); }
	// bool operator "" _X(const char* s) { return false; }
	// auto test = 123_X;
	public void testUserDefinedLiteralOperatorTypes1b() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(long double d) { return Ret(); }
	// bool operator "" _X(const char* s) { return false; }
	// auto test = 12.3_X;
	public void testUserDefinedLiteralOperatorTypes2b() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char* s, unsigned sz) { return Ret(); }
	// auto test = "123"_X;
	public void testUserDefinedLiteralOperatorTypes3() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const wchar_t* s, unsigned sz) { return Ret(); }
	// auto test = L"123"_X;
	public void testUserDefinedLiteralOperatorTypes3a() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char16_t* s, unsigned sz) { return Ret(); }
	// auto test = u"123"_X;
	public void testUserDefinedLiteralOperatorTypes3b() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char32_t* s, unsigned sz) { return Ret(); }
	// auto test = U"123"_X;
	public void testUserDefinedLiteralOperatorTypes3c() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// template<char... Chars> Ret operator "" _X() { return Ret(); }
	// auto test = 123_X;
	public void testUserDefinedLiteralOperatorTypes4a() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// template<char... Chars> Ret operator "" _X() { return Ret(); }
	// auto test = 123.123_X;
	public void testUserDefinedLiteralOperatorTypes4b() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char* s, unsigned sz) { return Ret(); }
	// auto test = "123" "123"_X;
	public void testUserDefinedLiteralConcatenation1a() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char* s, unsigned sz) { return Ret(); }
	// auto test = "123"_X "123";
	public void testUserDefinedLiteralConcatenation1b() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char* s, unsigned sz) { return Ret(); }
	// auto test = u8"123" "123"_X;
	public void testUserDefinedLiteralConcatenation2a() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char* s, unsigned sz) { return Ret(); }
	// auto test = u8"123"_X "123";
	public void testUserDefinedLiteralConcatenation2b() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char* s, unsigned sz) { return Ret(); }
	// auto test = "123" u8"123"_X;
	public void testUserDefinedLiteralConcatenation2c() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char* s, unsigned sz) { return Ret(); }
	// auto test = "123"_X u8"123";
	public void testUserDefinedLiteralConcatenation2d() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const wchar_t* s, unsigned sz) { return Ret(); }
	// auto test = L"123" "123"_X;
	public void testUserDefinedLiteralConcatenation3a() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const wchar_t* s, unsigned sz) { return Ret(); }
	// auto test = L"123"_X "123";
	public void testUserDefinedLiteralConcatenation3b() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const wchar_t* s, unsigned sz) { return Ret(); }
	// auto test = "123" L"123"_X;
	public void testUserDefinedLiteralConcatenation3c() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const wchar_t* s, unsigned sz) { return Ret(); }
	// auto test = "123"_X L"123";
	public void testUserDefinedLiteralConcatenation3d() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char16_t* s, unsigned sz) { return Ret(); }
	// auto test = u"123" "123"_X;
	public void testUserDefinedLiteralConcatenation4a() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char16_t* s, unsigned sz) { return Ret(); }
	// auto test = u"123"_X "123";
	public void testUserDefinedLiteralConcatenation4b() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char16_t* s, unsigned sz) { return Ret(); }
	// auto test = "123" u"123"_X;
	public void testUserDefinedLiteralConcatenation4c() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char16_t* s, unsigned sz) { return Ret(); }
	// auto test = "123"_X u"123";
	public void testUserDefinedLiteralConcatenation4d() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char32_t* s, unsigned sz) { return Ret(); }
	// auto test = U"123" "123"_X;
	public void testUserDefinedLiteralConcatenation5a() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char32_t* s, unsigned sz) { return Ret(); }
	// auto test = U"123"_X "123";
	public void testUserDefinedLiteralConcatenation5b() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char32_t* s, unsigned sz) { return Ret(); }
	// auto test = "123" U"123"_X;
	public void testUserDefinedLiteralConcatenation5c() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char32_t* s, unsigned sz) { return Ret(); }
	// auto test = "123"_X U"123";
	public void testUserDefinedLiteralConcatenation5d() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char32_t* s, unsigned sz) { return Ret(); }
	// auto test = "123"_X U"123"_X;
	public void testUserDefinedLiteralConcatenation6() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(char const * const s, unsigned long sz) { return Ret(); }
	// auto test = "123"_X;
	public void testUserDefinedLiteralConcatenation_528196_1a() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(char const * volatile s, unsigned long sz) { return Ret(); }
	// auto test = "123"_X;
	public void testUserDefinedLiteralConcatenation_528196_1b() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(char const * const volatile s, unsigned long sz) { return Ret(); }
	// auto test = "123"_X;
	public void testUserDefinedLiteralConcatenation_528196_1c() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(char const * s, unsigned long const sz) { return Ret(); }
	// auto test = "123"_X;
	public void testUserDefinedLiteralConcatenation_528196_2a() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(char const * s, unsigned long volatile sz) { return Ret(); }
	// auto test = "123"_X;
	public void testUserDefinedLiteralConcatenation_528196_2b() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(char const * s, unsigned long const volatile sz) { return Ret(); }
	// auto test = "123"_X;
	public void testUserDefinedLiteralConcatenation_528196_2c() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(unsigned long long const) { return Ret(); }
	// auto test = 10_X;
	public void testUserDefinedLiteralConcatenation_528196_3a() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(unsigned long long volatile) { return Ret(); }
	// auto test = 10_X;
	public void testUserDefinedLiteralConcatenation_528196_3b() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(unsigned long long const volatile) { return Ret(); }
	// auto test = 10_X;
	public void testUserDefinedLiteralConcatenation_528196_3c() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(char const) { return Ret(); }
	// auto test = 'a'_X;
	public void testUserDefinedLiteralConcatenation_528196_4a() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(char volatile) { return Ret(); }
	// auto test = 'a'_X;
	public void testUserDefinedLiteralConcatenation_528196_4b() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(char const volatile) { return Ret(); }
	// auto test = 'a'_X;
	public void testUserDefinedLiteralConcatenation_528196_4c() throws Exception {
		checkUserDefinedLiteralIsRet(getAboveComment());
	}

	// class Ret {};
	// Ret operator "" _X(const char* s, unsigned sz) { return Ret(); }
	// Ret operator "" _Y(const char* s, unsigned sz) { return Ret(); }
	// auto test = "123"_X "123"_Y;
	public void testUserDefinedLiteralBadConcatenation1() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, false);

		IASTProblem[] problems = tu.getPreprocessorProblems();
		assertEquals(1, problems.length);

		assertEquals(IProblem.PREPROCESSOR_MULTIPLE_USER_DEFINED_SUFFIXES_IN_CONCATENATION, problems[0].getID());
	}

	// struct basic_string {
	//   basic_string(char const * str, int len);
	// };
	// basic_string operator""s(char const * str, int len) {
	//   return basic_string { str, len };
	// }
	// auto waldo = "Waldo"s;
	public void testStringLiterals() throws Exception {
		checkUserDefinedLiteralIsType(getAboveComment(), "basic_string");
	}

	// auto waldo = 1i + 1;
	public void testComplexNumbersCompilerSupport1() throws Exception {
		checkUserDefinedLiteralIsType(getAboveComment(), "_Complex int");
	}

	// auto waldo = 1j + 1;
	public void testComplexNumbersCompilerSupport2() throws Exception {
		checkUserDefinedLiteralIsType(getAboveComment(), "_Complex int");
	}

	// struct complex {
	//   complex(unsigned long long real, unsigned long long imag);
	//   complex operator+(unsigned long long);
	// };
	// complex operator""i(unsigned long long imag) {
	//   return complex { 0, imag };
	// }
	// auto waldo = 1i + 1;
	public void testComplexNumbersOverriddenCompilerSupport() throws Exception {
		checkUserDefinedLiteralIsType(getAboveComment(), "complex");
	}

	// struct complex {
	//   complex(long double real, long double imag);
	//   complex operator+(long double long);
	// };
	// complex operator""if(long double imag) {
	//   return complex { 0, imag };
	// }
	// auto waldo = 1.0if + 1;
	public void testComplexFloatNumbersOverriddenCompilerSupport() throws Exception {
		checkUserDefinedLiteralIsType(getAboveComment(), "complex");
	}

	// // Test name lacking a space
	// int operator ""X(const char* s) { return 0; }
	// int operator ""_X(const char* s) { return 0; }
	public void testUserDefinedLiteralNoWhiteSpace1() throws Exception {
		parseAndCheckBindings();
	}

	// // Test literals with spaces before the suffix
	// int operator "" _X(const char* s) { return 0; }
	// auto a = 1 _X;
	// auto b = 1.0 _X;
	// auto c1 = '1' _X;
	// auto c2 = L'1' _X;
	// auto c3 = u8'1' _X;
	// auto c4 = u'1' _X;
	// auto c5 = U'1' _X;
	// auto d1 = "1" _X;
	// auto d2 = L"1" _X;
	// auto d3 = u8"1" _X;
	// auto d4 = u"1" _X;
	// auto d5 = U"1" _X;
	// auto e1 = "1" _X "2";
	// auto e2 = L"1" _X "2";
	// auto e3 = u8"1" _X "2";
	// auto e4 = u"1" _X "2";
	// auto e5 = U"1" _X "2";
	// auto d5 = U"1" _X;
	public void testUserDefinedLiteralNoWhiteSpace2() throws Exception {
		IASTTranslationUnit tu = parse(getAboveComment(), CPP, true, false);
		IASTDeclaration[] decls = tu.getDeclarations();

		for (int i = 1; i < decls.length; i++) {
			IASTDeclaration decl = decls[i];
			assertTrue(decl instanceof IASTProblemDeclaration);
			assertEquals(IProblem.SYNTAX_ERROR, ((IASTProblemDeclaration) decl).getProblem().getID());
		}
	}

	// class RetA {};
	// class RetB {};
	// template<char... Chars> RetA operator "" _X() { return RetA(); }
	// RetB operator "" _X(unsigned long long i) { return RetB(); }
	// auto a = 123_X;
	public void testUserDefinedLiteralResolution1() throws Exception {
		checkUserDefinedLiteralIsType(getAboveComment(), "RetB");
	}

	// class RetA {};
	// class RetB {};
	// template<char... Chars> RetA operator "" _X() { return RetA(); }
	// RetB operator "" _X(long double i) { return RetB(); }
	// auto a = 123.123_X;
	public void testUserDefinedLiteralResolution2() throws Exception {
		checkUserDefinedLiteralIsType(getAboveComment(), "RetB");
	}

	// class RetA {};
	// class RetB {};
	// template<char... Chars> RetA operator "" _X() { return RetA(); }
	// RetB operator "" _X(const char * c) { return RetB(); }
	// auto test = 123_X;
	public void testUserDefinedLiteralResolution3() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		ICPPVariable test = bh.assertNonProblemOnFirstIdentifier("test");
		assertTrue(test.getType() instanceof IProblemType); // resolution is ambiguous
	}

	//	namespace N {
	//	    class Color {};
	//	    Color operator"" _color(const char*, unsigned long);
	//	}
	//	void setColor(N::Color);
	//	int main() {
	//	    using namespace N;
	//	    setColor("#ffffff"_color);  // ERROR
	//	}
	public void testUserDefinedLiteralInNamespace_510665() throws Exception {
		parseAndCheckBindings();
	}

	//	double waldo1 = 02.968;
	//	double waldo2 = 09.268;
	//	double waldo3 = 02e2;
	//	double waldo4 = 09e2;
	public void testFloatLiteralWithLeadingZero_498434() throws Exception {
		parseAndCheckImplicitNameBindings();
	}

	//	char foo() {
	//		return '*';
	//	}
	public void testRegression_484618() throws Exception {
		parseAndCheckImplicitNameBindings();
	}

	//	constexpr int lambdas_supported =
	//	#if __has_feature(cxx_lambdas)
	//		1;
	//	#else
	//		0;
	//	#endif
	//
	//	constexpr int generic_lambdas_supported =
	//	#if __has_feature(cxx_generic_lambdas)
	//		1;
	//	#else
	//		0;
	//	#endif
	public void testHasFeature_442325() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("lambdas_supported", 1);
		// Note: when support for generic lambdas is implemented,
		// this test will need to be updated.
		helper.assertVariableValue("generic_lambdas_supported", 0);
	}

	//	struct S {
	//	  int a;
	//	  int b;
	//	  int c[10];
	//	  int d;
	//	};
	//	void test() {
	//	  S a = { .a = 10, .b = 11 };
	//	  S b = { .c[4 ... 6] = 3 };
	//	  S{ .d = 5 };
	//	  int c[6] = { [4] = 29, [2] = 15 };
	//	  int d[6] = { [2 ... 4] = 29 };
	//	}
	public void testDesignatedInitializers() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		//		ICPPASTDesignatedInitializer d1 = bh.assertNode(".a = 10");
		//		assertEquals(1, d1.getDesignators().length);
		//		assertTrue(d1.getDesignators()[0] instanceof ICPPASTFieldDesignator);
		//		ICPPASTDesignatedInitializer d2 = bh.assertNode(".b = 11");
		//		assertEquals(1, d2.getDesignators().length);
		//		assertTrue(d2.getDesignators()[0] instanceof ICPPASTFieldDesignator);
		//		ICPPASTDesignatedInitializer d3 = bh.assertNode(".c[4 ... 6] = 3");
		//		assertEquals(2, d3.getDesignators().length);
		//		assertTrue(d3.getDesignators()[0] instanceof ICPPASTFieldDesignator);
		//		assertTrue(d3.getDesignators()[1] instanceof IGPPASTArrayRangeDesignator);
		//		ICPPASTDesignatedInitializer d4 = bh.assertNode(".d = 5");
		//		assertEquals(1, d4.getDesignators().length);
		//		assertTrue(d4.getDesignators()[0] instanceof ICPPASTFieldDesignator);
		//		ICPPASTDesignatedInitializer d5 = bh.assertNode("[4] = 29");
		//		assertEquals(1, d5.getDesignators().length);
		//		assertTrue(d5.getDesignators()[0] instanceof ICPPASTArrayDesignator);
		//		ICPPASTDesignatedInitializer d6 = bh.assertNode("[2] = 15");
		//		assertEquals(1, d6.getDesignators().length);
		//		assertTrue(d6.getDesignators()[0] instanceof ICPPASTArrayDesignator);
		//		ICPPASTDesignatedInitializer d7 = bh.assertNode("[2 ... 4] = 29");
		//		assertEquals(1, d7.getDesignators().length);
		//		assertTrue(d7.getDesignators()[0] instanceof IGPPASTArrayRangeDesignator);
		//		ICPPField a = bh.assertNonProblemOnFirstIdentifier(".a");
		//		ICPPField b = bh.assertNonProblemOnFirstIdentifier(".b");
		//		ICPPField c = bh.assertNonProblemOnFirstIdentifier(".c[4 ... 6]");
		ICPPField d = bh.assertNonProblemOnFirstIdentifier(".d");
	}

	//	struct A {
	//		A() {}
	//	};
	//
	//	struct B {
	//		B() {}
	//	};
	//
	//	struct C : A {
	//		C() {}
	//	};
	//
	//	struct D : virtual A, virtual B {
	//		D() {}
	//	};
	//
	//	struct E {
	//		E() {}
	//	};
	//
	//	struct F : D, virtual E {
	//		F() {}
	//	};
	public void testImplicitlyCalledBaseConstructor_393717() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();

		ICPPConstructor aCtor = helper.assertNonProblem("A()", "A");
		ICPPConstructor bCtor = helper.assertNonProblem("B()", "B");
		ICPPConstructor dCtor = helper.assertNonProblem("D()", "D");
		ICPPConstructor eCtor = helper.assertNonProblem("E()", "E");

		ICPPASTFunctionDefinition ctorDef = helper.assertNode("C() {}");
		IASTImplicitName[] implicitNames = ((IASTImplicitNameOwner) ctorDef).getImplicitNames();
		assertEquals(1, implicitNames.length);
		assertEquals(aCtor, implicitNames[0].resolveBinding());

		ctorDef = helper.assertNode("D() {}");
		implicitNames = ((IASTImplicitNameOwner) ctorDef).getImplicitNames();
		sortNames(implicitNames);
		assertEquals(2, implicitNames.length);
		assertEquals(aCtor, implicitNames[0].resolveBinding());
		assertEquals(bCtor, implicitNames[1].resolveBinding());

		ctorDef = helper.assertNode("F() {}");
		implicitNames = ((IASTImplicitNameOwner) ctorDef).getImplicitNames();
		sortNames(implicitNames);
		assertEquals(4, implicitNames.length);
		assertEquals(aCtor, implicitNames[0].resolveBinding());
		assertEquals(bCtor, implicitNames[1].resolveBinding());
		assertEquals(dCtor, implicitNames[2].resolveBinding());
		assertEquals(eCtor, implicitNames[3].resolveBinding());
	}

	//	struct A {
	//		A(int, int);
	//	};
	//	void a(A);
	//	int main() {
	//		a(A{3, 4});
	//	}
	public void testImplicitConstructorNameInTypeConstructorExpression_447431() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPConstructor ctor = helper.assertNonProblem("A(int, int)", "A");
		ICPPASTSimpleTypeConstructorExpression typeConstructorExpr = helper.assertNode("A{3, 4}");
		IASTImplicitName[] implicitNames = ((IASTImplicitNameOwner) typeConstructorExpr).getImplicitNames();
		assertEquals(1, implicitNames.length);
		assertEquals(ctor, implicitNames[0].resolveBinding());
	}

	//	template <typename T>
	//	struct Waldo {
	//	    T x;
	//	};
	//
	//	void test() {
	//	    using Waldo = Waldo<int>;
	//	    auto size = sizeof(Waldo::x);
	//	}
	public void testShadowingAliasDeclaration_484200() throws Exception {
		parseAndCheckBindings();
	}

	//	struct foo {
	//	  using E = enum { Zero, One, Two };
	//
	//	  bool f() {
	//	    return Zero == 0; // "Symbol 'Zero' could not be resolved"
	//	  }
	//	};
	public void testAnonymousEnumInAliasDeclaration_502016() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S {
	//	    void foo() {
	//	        bar(E::A);       // ERROR: Symbol 'A' could not be resolved
	//	    }
	//	    enum class E { A };
	//	    void bar(E);
	//	};
	public void testEnumDeclaredLaterInClass_491747() throws Exception {
		parseAndCheckBindings();
	}

	//	enum E { A = 2 };
	//	constexpr int operator+(E, E) {
	//	    return 5;
	//	}
	//	constexpr int waldo = A + A;
	public void testOverloadedOperatorWithEnumArgument_506672() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo", 5);
	}

	//	class S {
	//	    static S waldo;
	//	};
	//	void foo(const S& = S());
	public void testValueRepresentationOfClassWithStaticMemberOfOwnType_490475() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPFunction foo = helper.assertNonProblem("foo");
		// Trigger computation of value representation of S().
		foo.getParameters()[0].getDefaultValue();
	}

	//	class S {
	//		S waldo;  // invalid
	//	};
	//	void foo(const S& = S());
	public void testClassDirectlyAggregatingItself_490475() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPFunction foo = helper.assertNonProblem("foo");
		// Trigger computation of value representation of S().
		foo.getParameters()[0].getDefaultValue();
	}

	//	class T;
	//	class S {
	//		T waldo;  // invalid
	//	};
	//	class T {
	//		S waldo;
	//	};
	//	void foo(const T& = T());
	public void testClassIndirectlyAggregatingItself_490475() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		ICPPFunction foo = helper.assertNonProblem("foo");
		// Trigger computation of value representation of S().
		foo.getParameters()[0].getDefaultValue();
	}

	//	namespace std {
	//		template<typename T> class initializer_list;
	//	}
	//	struct A {};
	//	A&& foo();
	//	A a;
	//	decltype(auto) b = a; // decltype(b) is A
	//	decltype(auto) c(a); // decltype(c) is A
	//	decltype(auto) d = (a); // decltype(d) is A &
	//	decltype(auto) e = foo(); // decltype(e) is A &&
	//	static decltype(auto) f = 0.0; // decltype(f) is double
	//	decltype(auto) g = &a, h = &b; // decltype(g) and decltype(h) are A *
	//	decltype(auto) i = { 'a', 'b' }; // Error - cannot deduce decltype(auto) from initializer list.
	//	decltype(auto) j{42}; // Valid since C++17: decltype(j) is int
	//	decltype(auto) k = new decltype(auto)(1L); // decltype(j) is long int *
	//	decltype(auto) l; // Error - missing initializer.
	//	decltype(auto) const m = 42; // Error - decltype(auto) does not allow type specifiers. Bug 527553
	//	decltype(auto) volatile n = 42; // Error - decltype(auto) does not allow type specifiers. Bug 527553
	//	decltype(auto) const volatile o = 42; // Error - decltype(auto) does not allow type specifiers. Bug 527553
	public void testDecltypeAutoVariableTypes_482225() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);

		ICPPVariable b = bh.assertNonProblem("b =", 1);
		assertEquals("A", ASTTypeUtil.getType(b.getType()));

		ICPPVariable c = bh.assertNonProblem("c(a)", 1);
		assertEquals("A", ASTTypeUtil.getType(c.getType()));

		ICPPVariable d = bh.assertNonProblem("d =", 1);
		assertEquals("A &", ASTTypeUtil.getType(d.getType()));

		ICPPVariable e = bh.assertNonProblem("e =", 1);
		assertEquals("A &&", ASTTypeUtil.getType(e.getType()));

		ICPPVariable f = bh.assertNonProblem("f =", 1);
		assertEquals("double", ASTTypeUtil.getType(f.getType()));

		ICPPVariable g = bh.assertNonProblem("g =", 1);
		assertEquals("A *", ASTTypeUtil.getType(g.getType()));

		ICPPVariable h = bh.assertNonProblem("h =", 1);
		assertEquals("A *", ASTTypeUtil.getType(h.getType()));

		ICPPVariable i = bh.assertNonProblem("i =", 1);
		IProblemType iType = (IProblemType) i.getType();
		assertEquals(ISemanticProblem.TYPE_CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE, iType.getID());

		ICPPVariable j = bh.assertNonProblem("j{42}", 1);
		assertEquals("int", ASTTypeUtil.getType(j.getType()));

		ICPPVariable k = bh.assertNonProblem("k =", 1);
		assertEquals("long int *", ASTTypeUtil.getType(k.getType()));

		ICPPVariable l = bh.assertNonProblem("l;", 1);
		IProblemType lType = (IProblemType) l.getType();
		assertEquals(ISemanticProblem.TYPE_CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE, lType.getID());

		ICPPVariable m = bh.assertNonProblem("m =", 1);
		IProblemType mType = (IProblemType) m.getType();
		assertEquals(ISemanticProblem.TYPE_CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE, mType.getID());

		ICPPVariable n = bh.assertNonProblem("n =", 1);
		IProblemType nType = (IProblemType) n.getType();
		assertEquals(ISemanticProblem.TYPE_CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE, nType.getID());

		ICPPVariable o = bh.assertNonProblem("o =", 1);
		IProblemType oType = (IProblemType) o.getType();
		assertEquals(ISemanticProblem.TYPE_CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE, oType.getID());
	}

	//	auto foo() -> decltype(auto) const {
	//		return 23.0;
	//	}
	//	auto a = foo();
	public void testDecltypeAutoTrailingReturnTypeConst_527553() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		ICPPVariable a = bh.assertNonProblem("a =", 1);
		IProblemType aType = (IProblemType) a.getType();
		assertEquals(ISemanticProblem.TYPE_CANNOT_DEDUCE_AUTO_TYPE, aType.getID());
	}

	//	auto foo() -> decltype(auto) volatile {
	//		return 23.0;
	//	}
	//  auto a = foo();
	public void testDecltypeAutoTrailingReturnTypeVolatile_527553() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		ICPPVariable a = bh.assertNonProblem("a =", 1);
		IProblemType aType = (IProblemType) a.getType();
		assertEquals(ISemanticProblem.TYPE_CANNOT_DEDUCE_AUTO_TYPE, aType.getID());
	}

	//	auto foo() -> decltype(auto) const {
	//		return 23.0;
	//	}
	//	decltype(auto) a = foo();
	public void testDecltypeAutoTrailingReturnTypeConstDecltype_527553() throws Exception {
		String code = getAboveComment();
		BindingAssertionHelper bh = new AST2AssertionHelper(code, true);
		ICPPVariable a = bh.assertNonProblem("a =", 1);
		IProblemType aType = (IProblemType) a.getType();
		assertEquals(ISemanticProblem.TYPE_CANNOT_DEDUCE_DECLTYPE_AUTO_TYPE, aType.getID());
	}

	//	auto foo() -> decltype(auto) {
	//		return 23.0;
	//	}
	public void testDecltypeAutoTrailingReturnType_482225() throws Exception {
		parseAndCheckBindings();
	}

	//	decltype(auto) foo() {
	//		return 23.0;
	//	}
	public void testDecltypeAutoReturnType_482225() throws Exception {
		parseAndCheckBindings();
	}

	//	struct Waldo {
	//	    int foo();
	//	};
	//
	//	int main() {
	//	    Waldo w;
	//	    auto i1 = w, i2 = i1.foo();  // Error on 'foo'
	//	}
	public void testAutoWithTwoDeclarators_522066() throws Exception {
		parseAndCheckBindings();
	}

	//	constexpr int waldo = (sizeof(double) % 16);
	public void testSizeofDouble_506170() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo", 8);
	}

	//	struct BooleanConvertible {
	//		constexpr explicit operator bool() const { return true; }
	//	};
	//	void explicitBooleanContexts() {
	//		BooleanConvertible bc{};
	//		bc && bc;
	//		bc || bc;
	//		!bc;
	//	}
	public void testContextualBooleanConversion_506972() throws Exception {
		IASTTranslationUnit tu = parseAndCheckBindings();
		IASTDeclaration explicitBooleanContextsFunction = tu.getDeclarations()[1];
		IASTNode functionBody = explicitBooleanContextsFunction.getChildren()[2];

		IASTNode logicalAndExpressionStatement = functionBody.getChildren()[1];
		ICPPASTBinaryExpression logicalAndExpression = (ICPPASTBinaryExpression) logicalAndExpressionStatement
				.getChildren()[0];
		ICPPFunction logicalAndOverload = logicalAndExpression.getOverload();
		assertNotNull(logicalAndOverload);
		ICPPFunctionType logicalAndType = logicalAndOverload.getType();
		isTypeEqual(logicalAndType, "bool (bool, bool)");

		IASTNode logicalOrExpressionStatement = functionBody.getChildren()[2];
		ICPPASTBinaryExpression logicalOrExpression = (ICPPASTBinaryExpression) logicalOrExpressionStatement
				.getChildren()[0];
		ICPPFunction logicalOrOverload = logicalOrExpression.getOverload();
		assertNotNull(logicalOrOverload);
		ICPPFunctionType logicalOrType = logicalOrOverload.getType();
		isTypeEqual(logicalOrType, "bool (bool, bool)");

		IASTNode logicalNotExpressionStatement = functionBody.getChildren()[3];
		ICPPASTUnaryExpression logicalNotExpression = (ICPPASTUnaryExpression) logicalNotExpressionStatement
				.getChildren()[0];
		ICPPFunction logicalNotOverload = logicalNotExpression.getOverload();
		assertNotNull(logicalNotOverload);
		ICPPFunctionType logicalNotType = logicalNotOverload.getType();
		isTypeEqual(logicalNotType, "bool (bool)");
	}

	//	enum enumDecl {
	//	    el0 __attribute__((deprecated)),
	//	    el1,
	//	};
	public void testEnumeratorAttribute_514821() throws Exception {
		parseAndCheckBindings(getAboveComment(), CPP, true /* use GNU extensions */);
	}

	//	void foo([[maybe_unused]] int a);
	public void testCxx11AttributeBeforeParameterDeclaration_530729() throws Exception {
		parseAndCheckBindings();
	}

	//	struct CType {
	//	    char m_Array[4];
	//	};
	//
	//	void foo(char(&)[4]);
	//
	//	int main() {
	//	    char name[sizeof(CType().m_Array)];
	//	    foo(name);
	//	}
	public void testSizeofArrayField_512932() throws Exception {
		parseAndCheckBindings();
	}

	//void ns2 ();
	//
	//namespace ns1 {
	//namespace ns2 {
	//class TestNameSpace {
	//public:
	//	TestNameSpace();
	//};
	//}
	//}
	//
	//using namespace ns1;
	//using namespace ns2; //ns2 shouldn't be ambiguous because only namespace names should be considered.
	//
	//TestNameSpace::TestNameSpace() {
	//}
	public void testUsingDirectiveNamespaceWithPreviousFunctionName_517402() throws Exception {
		parseAndCheckBindings();
	}

	//void ns2 ();
	//namespace ns1 {
	//namespace ns2 {
	//class TestNameSpace {
	//public:
	//	TestNameSpace();
	//};
	//}
	//}
	//
	//using namespace ns1;
	//namespace ns12 = ns2;
	//ns12::TestNameSpace::TestNameSpace() {
	//}
	public void testNamespaceAliasNamespaceWithPreviousFunctionName_517402() throws Exception {
		parseAndCheckBindings();
	}

	//	class C {};
	//	typedef C D;
	//	constexpr bool waldo = __is_class(D);
	public void testIsClassBuiltinOnTypedef_522509() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo", 1);
	}

	//	struct A {};
	//	struct A* b = (1 == 1) ? new struct A : new struct A;
	public void test_ElabTypeSpecInNewExprInConditional_526134() throws Exception {
		parseAndCheckBindings();
	}

	//	constexpr bool waldo1 = __is_trivially_constructible(int, short);
	//	struct S { int fMem; S(int mem) : fMem(mem) {} };
	//	constexpr bool waldo2 = __is_trivially_constructible(S);
	//	constexpr bool waldo3 = __is_trivially_constructible(S, const S&);
	//	constexpr bool waldo4 = __is_trivially_constructible(S, int);
	//	constexpr bool waldo5 = __is_trivially_constructible(S, const S&, float);
	public void testIsTriviallyConstructible_528072() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo1", 1);
		helper.assertVariableValue("waldo2", 0);
		helper.assertVariableValue("waldo3", 1);
		helper.assertVariableValue("waldo4", 0);
		helper.assertVariableValue("waldo5", 0);
	}

	//	template <typename T, typename U>
	//	struct pair {
	//	    pair();
	//	    pair(T, U);
	//	};
	//
	//	constexpr bool waldo = __is_constructible(pair<const int>, pair<int, int>&&);
	public void testIsConstructible_539052() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo", 1);
	}

	//	constexpr bool waldo = unsigned(-1) < unsigned(0);
	public void testNegativeCastToUnsigned_544509() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("waldo", 0);
	}

	//  namespace x {
	//  	void foo();
	//	}
	//	namespace x::y {
	//  	void bar() {
	//      	foo();
	//  	}
	//	}
	//	int main() {
	//  	x::y::bar();
	//	}
	public void testNestedNamespaceDefinition_490359() throws Exception {
		parseAndCheckBindings();
	}

	//	constexpr auto l_a = 0b01'100'100'100;
	//	constexpr auto l_b = 1'123'456;
	//	constexpr auto l_c = 0x1000'1000;
	//	constexpr auto l_d = 0111'1000;
	//	auto v_a = 1'123'456ul;
	//	auto v_b = 1'123'456ull;
	//	auto v_c = 0xAABB'CCDDll;
	public void testLiteralDecimalSeparators_519062() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("l_a", 804);
		helper.assertVariableValue("l_b", 1123456);
		helper.assertVariableValue("l_c", 268439552);
		helper.assertVariableValue("l_d", 299520);

		helper.assertVariableType("v_a", CPPBasicType.UNSIGNED_LONG);
		helper.assertVariableType("v_b", CPPBasicType.UNSIGNED_LONG_LONG);
		helper.assertVariableType("v_c", CPPBasicType.LONG_LONG);
	}

	//	extern void *List[];
	//	extern void *List[];
	//	void *List[] = { 0 };
	//	unsigned int ListSize = sizeof(List)/sizeof(List[0]);
	public void testMultipleExternDecls_534098() throws Exception {
		BindingAssertionHelper helper = getAssertionHelper();
		IVariable var = helper.assertNonProblem("ListSize");
		// Trigger initial value computation, test that it
		// does not throw an exception.
		var.getInitialValue();
	}

	//static_assert(true);
	public void testStaticAssertWithoutMessage_534808() throws Exception {
		parseAndCheckBindings();
	}

	//	struct MyStruct {
	//	    unsigned i;
	//	};
	//
	//	auto myFunA() -> struct MyStruct {
	//	    return {5};
	//	};
	public void testElabSpecInTrailingReturn_535777() throws Exception {
		parseAndCheckBindings();
	}

	//	struct type {
	//		type(int,int){};
	//	};
	//
	//	int main() {
	//		type a0(1,2);
	//		type a1{1,2};
	//		type a2 = {1,2};
	//		type a3(1,2,3);
	//		type a4{1,2,3};
	//		type a5 = {1,2,3};
	//	}
	public void testInitListConstructor_542448() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertImplicitName("a0", 2, CPPConstructor.class);
		bh.assertImplicitName("a1", 2, CPPConstructor.class);
		bh.assertImplicitName("a2", 2, CPPConstructor.class);

		bh.assertImplicitName("a3", 2, IProblemBinding.class);
		bh.assertImplicitName("a4", 2, IProblemBinding.class);
		bh.assertImplicitName("a5", 2, IProblemBinding.class);
	}

	//	struct type {
	//		explicit type(int,int){};
	//	};
	//
	//	int main() {
	//		type a0(1,2); // ok
	//		type a1{1,2}; // ok
	//		type a2 = {1,2}; // error: using explict ctor
	//	}
	public void testInitListConstructorWithExplicit_542448() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertImplicitName("a0", 2, CPPConstructor.class);
		bh.assertImplicitName("a1", 2, CPPConstructor.class);
		bh.assertImplicitName("a2", 2, IProblemBinding.class);
	}

	// struct type {
	//     explicit type(int,int);
	//     type(float,float);
	// };
	// int main() {
	//     type a0 = {1,2};
	// }
	public void testInitListConstructorWithExplicit2_542448() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		// ill-formed, because overload resolution
		// prefers the (int,int) constructor
		// which is explicit
		bh.assertImplicitName("a0", 2, IProblemBinding.class);
	}

	//	namespace std {
	//	  template<class>
	//	  class initializer_list {};
	//	}
	//
	//	struct A {
	//	    A(const std::initializer_list<int>&);
	//	};
	//
	//	A a{1, 3, 5, 6};
	public void testInitListConstRef_549035() throws Exception {
		parseAndCheckImplicitNameBindings();
	}

	//	struct type {
	//	  type(int a) {}
	//	};
	//
	//	struct other_type {};
	//
	//	int main() {
	//	  type(1, 2);
	//	  type{1, 2};
	//	  type(other_type());
	//	}
	public void testCtorWithWrongArguments_543913() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertImplicitName("type(1, 2)", 4, IProblemBinding.class);
		bh.assertImplicitName("type{1, 2}", 4, IProblemBinding.class);
		bh.assertImplicitName("type(other_type())", 4, IProblemBinding.class);
	}

	//	struct array{
	//		int data[1];
	//	};
	//
	//	void foo(array) {}
	//
	//	int main() {
	//		array{{1}};
	//		array{1};
	//      array a = {1};
	//	    foo({1});
	//	    foo({{1}});
	//	}
	public void testBraceElisionForAggregateInit0_SimpleValid_543038() throws Exception {
		parseAndCheckBindings();
	}

	//	struct array{
	//		int data[1];
	//	};
	//
	//	void foo(array) {}
	//
	//	int main() {
	//		array{{1,2}};
	//		array{1,2};
	//      array a0 = {1,2};
	//	    foo({1,2});
	//	    foo({{1,2}});
	//	}
	public void testBraceElisionForAggregateInit1_SimpleTooManyInitializers_543038() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertImplicitName("array{{1,2}}", 5, IProblemBinding.class);
		bh.assertImplicitName("array{1,2}", 5, IProblemBinding.class);
		bh.assertImplicitName("a0", 2, IProblemBinding.class);
		bh.assertProblem("foo({1,2})", 3);
		bh.assertProblem("foo({{1,2}})", 3);
	}

	//	struct level0{
	//		int a;
	//		int b;
	//	};
	//
	//	struct level1{
	//		public:
	//			level1(level0 a): a(a){}
	//	    private:
	//			level0 a;
	//	};
	//
	//	struct level2{
	//		level1 data;
	//	};
	//
	//	void foo(level2) {}
	//
	//	int main() {
	//		level1{{1,2}}; // ok
	//		foo({level1{{1,2}}}); // ok
	//		level1{1,2}; // ERROR: calling level1 constructor, not aggregate init of level0
	//		foo({{{1,2,3}}}); // ERROR: not aggregate init
	//	}
	public void testBraceElisionForAggregateInit2_WithNonAggregate_543038() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertNonProblem("foo({level1{{1,2}}})", 3);
		bh.assertImplicitName("level1{{1,2}};", 6, ICPPConstructor.class);
		bh.assertImplicitName("level1{1,2};", 6, IProblemBinding.class);
		bh.assertProblem("foo({{{1,2,3}}}", 3);
	}

	//	struct array2D{
	//		int data[2][3];
	//	};
	//
	//	void foo(array2D) {}
	//
	//	int main() {
	//	    foo({{{1,2,3},{1,2,3}}}); // no elision
	//	    foo({{1,2,3,1,2,3}}); // eliding one level
	//	    foo({{1,2}}); // eliding one level, but with only 2 elements which seems to initialize the outer level
	//	    foo({1,2,3,1,2,3}); // eliding all levels
	//	    foo({{1,2,3},{1,2,3}}); // ERROR eliding outer-most is not allowed
	//	}
	public void testBraceElisionForAggregateInit3_543038() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertNonProblem("foo({{{1,2,3},{1,2,3}}});", 3);
		bh.assertNonProblem("foo({{1,2,3,1,2,3}});", 3);
		bh.assertNonProblem("foo({{1,2}});", 3);
		bh.assertNonProblem("foo({1,2,3,1,2,3});", 3);
		bh.assertProblem("foo({{1,2,3},{1,2,3}});", 3);
	}

	//	struct type{
	//		type(int){};
	//	};
	//
	//	struct array{
	//		type data[2];
	//	};
	//
	//	void foo(array){}
	//
	//	int main() {
	//		foo({type{1},type{2}});
	//		foo({type{1}}); // ERROR: type is not default constructible
	//	}
	public void testBraceElisionForAggregateInit4_nonDefaultConstructible_543038() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertNonProblem("foo({type{1},type{2}});", 3);
		bh.assertProblem("foo({type{1}});", 3);
	}

	//	struct array2D{
	//		int data[2][3];
	//	};
	//
	//	void foo(array2D) {}
	//
	//	int main() {
	//	      foo({{{1,2,3},1,2,3}}); // ok: data[0] is initialized without elision, data[1] with elision
	//	      foo({{1,2,3,{1,2,3}}}); // ok: data[1] is initialized without elision, data[0] with elision
	//	      foo({{1,2,{1,2,3}}}); // ERROR: trying to initialize data[0][2] with {1,2,3}
	//	}
	public void testBraceElisionForAggregateInit5_partlyEliding_543038() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertNonProblem("foo({{{1,2,3},1,2,3}});", 3);
		bh.assertNonProblem("foo({{1,2,3,{1,2,3}}});", 3);
		bh.assertProblem("foo({{1,2,{1,2,3}}});", 3);
	}

	//	template<typename T, int D>
	//	struct trait {
	//		using type = T[D];
	//	};
	//
	//	template<typename T, int D>
	//	struct array {
	//		typename trait<T, D>::type data;
	//	};
	//
	// void foo(array<int, 1>) {
	// }
	//
	//	int main() {
	//		foo({1});
	//	}
	public void testBraceElisionForAggregateInit6_typedef_543038() throws Exception {
		parseAndCheckBindings();
	}

	//	struct S1 {
	//	};
	//	struct S2 {
	//		S1 s1;
	//	};
	//	auto f() {
	//		auto s1 = S1 { };
	//		return S2 { s1 };
	//	}
	public void testBraceElisionForAggregateInit7_545957() throws Exception {
		parseAndCheckImplicitNameBindings();
	}

	//  struct type{
	//      int a;
	//  };
	//  type b{sizeof(type)};
	public void testAggregateInitNoNarrowingConversionInConstContext_545756() throws Exception {
		parseAndCheckImplicitNameBindings();
	}

	//	struct A {
	//	    int x;
	//	};
	//
	//	template <typename T>
	//	struct B {
	//	    A a{sizeof(T)};
	//	};
	public void testAggregateInitNoNarrowingConversionInDependentConstContext_545756() throws Exception {
		parseAndCheckImplicitNameBindings();
	}

	//  struct type{
	//      int a;
	//  };
	//  const unsigned long v = 1;
	//  type b{v};
	public void testAggregateInitNoNarrowingConversionInConstContext2_545756() throws Exception {
		parseAndCheckImplicitNameBindings();
	}

	//  struct type{
	//      int a;
	//  };
	//  unsigned long v = 1;
	//  type b{v};
	public void testAggregateInitNarrowingConversion_545756() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertImplicitName("b{v};", 1, IProblemBinding.class);
	}

	//  using my_char = char;
	//	struct type {
	//	    my_char data[2];
	//	};
	//
	//	type foo{"s"};
	public void testCharArrayInitFromStringLiteral_545756() throws Exception {
		parseAndCheckImplicitNameBindings();
	}

	//	struct type {
	//	    char data[3];
	//	};
	//
	//	type foo{"big"};
	public void testCharArrayInitFromTooLargeStringLiteral_545756() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertImplicitName("foo", 3, IProblemBinding.class);
	}

	//	struct type {
	//	    char data[2];
	//	};
	//
	//	type foo{L"s"};
	public void testCharArrayInitFromWrongTypeStringLiteral_545756() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertImplicitName("foo", 3, IProblemBinding.class);
	}

	//	struct type {
	//		const char data[];
	//	};
	//
	//	type foo{"s"};
	public void testUnknownSizeCharArrayInitFromStringLiteral_545756() throws Exception {
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertImplicitName("foo", 3, IProblemBinding.class);
	}

	//	class Time {
	//	};
	//
	//	template<class T>
	//	class Test {
	//	private:
	//	    T t;
	//	public:
	//	   void test() {
	//	        Time time = t.get();
	//	   }
	//	};
	public void testCopyInitializationFromDependentType_546843() throws Exception {
		parseAndCheckImplicitNameBindings();
	}

	//	class Time {
	//		int a;
	//	 };
	//
	//	template<class T>
	//	class Test {
	//	private:
	//	    T t;
	//	public:
	//	   void test() {
	//	        Time time{t.get()};
	//	   }
	//	};
	public void testListInitializationFromDependentType_546843() throws Exception {
		parseAndCheckImplicitNameBindings();
	}

	//	int fun();
	//	int fun_noexcept() noexcept;
	//	int fun2(int);
	//	int fun2_noexcept(int) noexcept;
	//	constexpr int fun_constexpr() {return 1;}
	//	int (*fptr)();
	//	int (*fptr_noexcept)() noexcept;
	//  bool condition();
	//
	//	constexpr bool fun_is_not_noexcept = noexcept(fun());
	//	constexpr bool unevaluated_fun_is_noexcept = noexcept(fun);
	//	constexpr bool fun_noexcept_is_noexcept = noexcept(fun_noexcept());
	//	constexpr bool fun2_arg_is_not_noexcept = noexcept(fun2(1));
	//	constexpr bool fun2_noexcept_arg_is_noexcept = noexcept(fun2_noexcept(1));
	//	constexpr bool fun2_noexcept_arg_not_noexcept_is_not_noexcept = noexcept(fun2_noexcept(fun()));
	//	constexpr bool fun_constexpr_is_noexcept = noexcept(fun_constexpr());
	//	constexpr bool fptr_is_not_noexcept = noexcept(fptr());
	//	constexpr bool fptr_noexcept_is_noexcept = noexcept(fptr_noexcept());
	//  constexpr bool throw_is_not_noexcept = noexcept(throw fun_noexcept());
	public void testNoexceptOperatorFunctions_545021() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("fun_is_not_noexcept", 0);
		helper.assertVariableValue("unevaluated_fun_is_noexcept", 1);
		helper.assertVariableValue("fun_noexcept_is_noexcept", 1);
		helper.assertVariableValue("fun2_arg_is_not_noexcept", 0);
		helper.assertVariableValue("fun2_noexcept_arg_is_noexcept", 1);
		helper.assertVariableValue("fun2_noexcept_arg_not_noexcept_is_not_noexcept", 0);
		helper.assertVariableValue("fun_constexpr_is_noexcept", 1);
		helper.assertVariableValue("fptr_is_not_noexcept", 0);
		helper.assertVariableValue("fptr_noexcept_is_noexcept", 1);
		helper.assertVariableValue("throw_is_not_noexcept", 0);
	}

	//	int fun();
	//	int fun_noexcept() noexcept;
	//  bool condition();
	//  bool noexcept_condition() noexcept;
	//	constexpr bool comma_is_noexcept = noexcept(fun_noexcept(), fun_noexcept());
	//	constexpr bool comma_is_not_noexcept = noexcept(fun(), fun_noexcept());
	//  constexpr bool not_noexcept_conditional = noexcept(noexcept_condition() ? fun() : fun_noexcept());
	//  constexpr bool is_noexcept_conditional = noexcept(noexcept_condition() ? fun_noexcept() : fun_noexcept());
	//  constexpr bool condition_not_noexcept = noexcept(condition() ? fun_noexcept() : fun_noexcept());
	public void testNoexceptOperatorOperators_545021() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("comma_is_noexcept", 1);
		helper.assertVariableValue("comma_is_not_noexcept", 0);
		helper.assertVariableValue("not_noexcept_conditional", 0);
		helper.assertVariableValue("is_noexcept_conditional", 1);
		helper.assertVariableValue("condition_not_noexcept", 0);
	}

	//  struct aggregate{
	//      int a;
	//  };
	//  aggregate agg;
	//
	//  constexpr bool aggregate_init_is_noexcept = noexcept(aggregate{1});
	//  constexpr bool aggregate_access_is_noexcept = noexcept(agg.a);
	public void testNoexceptOperatorAggregate_545021() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("aggregate_init_is_noexcept", 1);
		helper.assertVariableValue("aggregate_access_is_noexcept", 1);
	}

	//	struct myclass{
	//		myclass() noexcept{}
	//		myclass(int){}
	//		constexpr myclass(int, int){}
	//	};
	//  constexpr bool ctor_is_noexcept = noexcept(myclass{});
	//  constexpr bool ctor_is_not_noexcept = noexcept(myclass{1});
	//  constexpr bool constexpr_ctor_is_noexcept = noexcept(myclass{1, 1});
	public void testNoexceptOperatorConstructors_545021() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("ctor_is_noexcept", 1);
		helper.assertVariableValue("ctor_is_not_noexcept", 0);
		helper.assertVariableValue("constexpr_ctor_is_noexcept", 1);
	}

	//	struct type {
	//		int mem();
	//		int mem_noexcept() noexcept;
	//		constexpr int constexpr_mem() {return 1;}
	//		operator int() noexcept;
	//		operator int*();
	//	};
	//  type t;
	//	int (type::*memptr)();
	//	int (type::*memptr_noexcept)() noexcept;
	//	constexpr bool mem_is_not_noexcept = noexcept(type{}.mem());
	//	constexpr bool unevaluated_mem_is_noexcept = noexcept(type{}.mem);
	//	constexpr bool mem_noexcept_is_noexcept = noexcept(type{}.mem_noexcept());
	//	constexpr bool constexpr_mem_is_noexcept = noexcept(type{}.constexpr_mem());
	//	constexpr bool memptr_is_not_noexcept = noexcept((type{}.*(memptr))());
	//	constexpr bool unevaluated_memptr_is_noexcept = noexcept((type{}.*(memptr)));
	//	constexpr bool memptr_noexcept_is_noexcept = noexcept((type{}.*(memptr_noexcept))());
	//	constexpr bool noexcept_conversion = noexcept(static_cast<int>(t));
	//	constexpr bool not_noexcept_conversion = noexcept(static_cast<int*>(t));
	//	constexpr bool conversion_from_constructor = noexcept(static_cast<int*>(type{}));
	public void testNoexceptOperatorType_545021() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("mem_is_not_noexcept", 0);
		helper.assertVariableValue("unevaluated_mem_is_noexcept", 1);
		helper.assertVariableValue("mem_noexcept_is_noexcept", 1);
		helper.assertVariableValue("constexpr_mem_is_noexcept", 1);
		helper.assertVariableValue("memptr_is_not_noexcept", 0);
		//		TODO(havogt): needs implementation of [except.spec] p.14 (c++11) noexcept for implicitly declared special member functions
		//		helper.assertVariableValue("unevaluated_memptr_is_noexcept", 1);
		helper.assertVariableValue("memptr_noexcept_is_noexcept", 1);
		helper.assertVariableValue("noexcept_conversion", 1);
		helper.assertVariableValue("not_noexcept_conversion", 0);
		helper.assertVariableValue("conversion_from_constructor", 0);
	}

	//	template<typename T>
	//	int funt(T);
	//	template<typename T>
	//	int funt_noexcept(T) noexcept;
	//
	//	constexpr bool funt_is_not_noexcept = noexcept(funt(1));
	//	constexpr bool funt_noexcept_is_noexcept = noexcept(funt_noexcept(1));
	public void testNoexceptOperatorFunctionTemplates_545021() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("funt_is_not_noexcept", 0);
		helper.assertVariableValue("funt_noexcept_is_noexcept", 1);
	}

	//  struct type1{
	//      void operator=(int);
	//      bool operator!();
	//  };
	//  type1 t1;
	//  struct type2{
	//      void operator=(int) noexcept;
	//      bool operator!() noexcept;
	//  };
	//  type2 t2;
	//  constexpr bool binaryop_is_not_noexcept = noexcept(t1 = 1);
	//  constexpr bool unaryop_is_not_noexcept = noexcept(!t1);
	//  constexpr bool noexcept_binaryop_is_noexcept = noexcept(t2 = 1);
	//  constexpr bool noexcept_unaryop_is_noexcept = noexcept(!t2);
	public void testNoexceptOperatorOverloadedOperators_545021() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("binaryop_is_not_noexcept", 0);
		helper.assertVariableValue("unaryop_is_not_noexcept", 0);
		helper.assertVariableValue("noexcept_binaryop_is_noexcept", 1);
		helper.assertVariableValue("noexcept_unaryop_is_noexcept", 1);
	}

	//	void fun();
	//	void fun_taking_funptr(void(*ptr)()) noexcept;
	//
	//	constexpr bool is_noexcept = noexcept(fun_taking_funptr(fun));
	public void testNoexceptOperatorNoncalledFunctionPtr_545021() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("is_noexcept", 1);
	}

	//	void fun() throw();
	//	constexpr bool is_noexcept = noexcept(fun());
	public void testNoexceptOperatorEmptyThrow_545021() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("is_noexcept", 1);
	}

	//  auto closure_noexcept = [](int i) noexcept {return i;};
	//  constexpr bool is_noexcept = noexcept(closure_noexcept());
	//  auto closure = [](int i) {return i;};
	//  constexpr bool is_not_noexcept = noexcept(closure());
	//  constexpr bool conversion_is_noexcept = noexcept(static_cast<int (*)(int)>(closure));
	public void testNoexceptOperatorLambda_545021() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("is_noexcept", 1);
		helper.assertVariableValue("is_not_noexcept", 0);
		helper.assertVariableValue("conversion_is_noexcept", 1);
	}

	//	  template <bool B>
	//	  void foo() noexcept(B);
	//
	//	  constexpr bool is_noexcept = noexcept(foo<true>());
	//	  constexpr bool is_not_noexcept = noexcept(foo<false>());
	public void testNoexceptOperatorDependentNoexcept_545021() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("is_noexcept", 1);
		helper.assertVariableValue("is_not_noexcept", 0);
	}

	//	  struct S { int mem; };
	//	  S foo();  // could throw
	//	  constexpr bool is_not_noexcept = noexcept(foo().mem);  // should be false
	public void testNoexceptOperatorOwnerEval_545021() throws Exception {
		parseAndCheckBindings();
		BindingAssertionHelper helper = getAssertionHelper();
		helper.assertVariableValue("is_not_noexcept", 0);
	}

	//	struct B{
	//	    B a;
	//	};
	//	B t{1};
	public void testSelfAggregation_546805() throws Exception {
		// Note that ideally we would report an error already on the declaration of B as
		// the class is aggregating itself. Today, we only report an error because of a
		// wrong constructor argument.
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertImplicitName("t{1};", 1, IProblemBinding.class);
	}

	//	struct B;
	//	struct A{
	//	    B b;
	//	};
	//	struct B{
	//		A a;
	//	};
	//	B t{1};
	public void testIndirectSelfAggregation_546805() throws Exception {
		// See comment in previous test
		BindingAssertionHelper bh = getAssertionHelper();
		bh.assertImplicitName("t{1};", 1, IProblemBinding.class);
	}
}
