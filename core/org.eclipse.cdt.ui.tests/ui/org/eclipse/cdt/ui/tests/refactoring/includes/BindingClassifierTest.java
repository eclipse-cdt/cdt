/*******************************************************************************
 * Copyright (c) 2012, 2015 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.includes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.DOMException;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBinding;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.index.IIndexManager;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.parser.util.StringUtil;
import org.eclipse.cdt.core.testplugin.util.OneSourceMultipleHeadersTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.internal.ui.refactoring.includes.BindingClassifier;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeCreationContext;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.jface.preference.IPreferenceStore;

import com.ibm.icu.text.MessageFormat;

import junit.framework.TestSuite;

/**
 * Tests for {@link BindingClassifier}.
 */
public class BindingClassifierTest extends OneSourceMultipleHeadersTestCase {
	private IIndex fIndex;
	private BindingClassifier fBindingClassifier;

	public BindingClassifierTest() {
		super(new TestSourceReader(CTestPlugin.getDefault().getBundle(), "ui", BindingClassifierTest.class), true);
	}

	public static TestSuite suite() {
		return suite(BindingClassifierTest.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp(true);
		IASTTranslationUnit ast = getAst();
		fIndex = CCorePlugin.getIndexManager().getIndex(getCProject(),
				IIndexManager.ADD_DEPENDENCIES | IIndexManager.ADD_EXTENSION_FRAGMENTS_ADD_IMPORT);
		fIndex.acquireReadLock();
		IPreferenceStore preferenceStore = getPreferenceStore();
		preferenceStore.setToDefault(PreferenceConstants.FORWARD_DECLARE_FUNCTIONS);
	}

	private void classifyBindings() {
		if (fBindingClassifier == null) {
			IASTTranslationUnit ast = getAst();
			ITranslationUnit tu = ast.getOriginatingTranslationUnit();
			IncludeCreationContext context = new IncludeCreationContext(tu, fIndex);
			fBindingClassifier = new BindingClassifier(context);
			fBindingClassifier.classifyNodeContents(ast);
		}
	}

	@Override
	protected void tearDown() throws Exception {
		fIndex.releaseReadLock();
		fBindingClassifier = null;
		super.tearDown();
	}

	private IPreferenceStore getPreferenceStore() {
		return CUIPlugin.getDefault().getPreferenceStore();
	}

	private void assertDefined(String... names) throws Exception {
		classifyBindings();
		assertExpectedBindings(names, fBindingClassifier.getBindingsToDefine(), "defined");
	}

	private void assertDeclared(String... names) throws Exception {
		classifyBindings();
		assertExpectedBindings(names, fBindingClassifier.getBindingsToForwardDeclare(), "declared");
	}

	private void assertExpectedBindings(String[] expectedNames, Set<IBinding> bindings, String verb) throws Exception {
		Set<String> expected = new TreeSet<>(Arrays.asList(expectedNames));
		Set<String> extra = new TreeSet<>();
		for (IBinding binding : bindings) {
			extra.add(getQualifiedName(binding));
		}
		Set<String> missing = new TreeSet<>(expected);
		missing.removeAll(extra);
		extra.removeAll(expected);
		if (extra.isEmpty() && missing.isEmpty())
			return;
		List<String> errors = new ArrayList<>(2);
		if (!missing.isEmpty()) {
			errors.add(MessageFormat.format("{0,choice,1#Binding|1<Bindings} \"{1}\" {0,choice,1#is|1<are} not {2}.",
					missing.size(), StringUtil.join(missing, "\", \""), verb));
		}
		if (!extra.isEmpty()) {
			errors.add(MessageFormat.format("{0,choice,1#Binding|1<Bindings} \"{1}\" should not be {2}.", extra.size(),
					StringUtil.join(extra, "\", \""), verb));
		}
		fail(StringUtil.join(errors, " "));
	}

	protected String getQualifiedName(IBinding binding) throws DOMException {
		if (binding instanceof ICPPBinding) {
			return StringUtil.join(((ICPPBinding) binding).getQualifiedName(), "::");
		} else {
			return binding.getName();
		}
	}

	//	class A;
	//	typedef A* td1;
	//	typedef td1* td2;
	//	td2 f();

	//	A* a = *f();
	public void testTypedef_1() throws Exception {
		assertDefined("f");
		assertDeclared("A");
	}

	//	class A;
	//	typedef A* td1;
	//	typedef td1* td2;
	//	td2 f();

	//	td1 a = *f();
	public void testTypedef_2() throws Exception {
		assertDefined("f", "td1");
		assertDeclared();
	}

	//	template<typename T> struct allocator {};
	//	template<typename T, typename U = allocator<T>> class basic_string {};
	//	typedef basic_string<char> string;
	//	template<typename T, typename A>
	//	basic_string<T, A> f(const T* a, const basic_string<T, A>& b);

	//	void test() {
	//	  string a;
	//	  f("*", a);
	//	}
	public void testTypedef_3() throws Exception {
		assertDefined("f", "string"); // "basic_string" and "allocator" should not be defined.
		assertDeclared();
	}

	//	struct A { int x; };
	//	typedef A* td;
	//	td f();

	//	int a = f()->x;
	public void testClassMember() throws Exception {
		assertDefined("A", "f");
		assertDeclared();
	}

	//	struct A { void m(); };
	//	class B : public A {};
	//	B b;
	//	class C : public A {};
	//	C* c;

	//	void test() {
	//	  b.m();
	//	  c->m();
	//	}
	public void testClassHierarchy() throws Exception {
		assertDefined("B", "b", "C", "c", "A::m");
		assertDeclared();
	}

	//	class A { void m(); };

	//	void test(A* a) {
	//	  a->m();
	//	}
	public void testMethodCall() throws Exception {
		assertDefined("A", "A::m");
		assertDeclared();
	}

	//	struct A {
	//	  void a() const;
	//	};
	//	struct B : public A {
	//	}
	//	struct C {
	//	  const B& c() const;
	//	};

	//	void test(const C& x) {
	//	  x.c().a();
	//	}
	public void testMethodCall_488349() throws Exception {
		assertDefined("A::a", "B", "C", "C::c");
		assertDeclared();
	}

	//	class Base {
	//	public:
	//	  void m();
	//	};
	//
	//	class Derived : public Base	{
	//	};

	//	class Derived;
	//	void test(Derived& d) {
	//	  d.m();
	//	}
	public void testSuperClassMethodCall_436656() throws Exception {
		assertDefined("Derived", "Base::m");
		assertDeclared();
	}

	//	class A {};
	//	void f(const A* p);
	//	A* g();

	//	void test() {
	//	  f(g());
	//	  f(0);
	//	  f(nullptr);
	//	}
	public void testFunctionCallWithPointerParameter_1() throws Exception {
		getPreferenceStore().setValue(PreferenceConstants.FORWARD_DECLARE_FUNCTIONS, true);
		assertDefined();
		assertDeclared("A", "f", "g");
	}

	//	typedef int A;
	//	void f(const A* p) {}

	//	void test() {
	//	  f(nullptr);
	//	}
	public void testFunctionCallWithPointerParameter_2() throws Exception {
		getPreferenceStore().setValue(PreferenceConstants.FORWARD_DECLARE_FUNCTIONS, true);
		assertDefined("f"); // Inline definition has to be included.
		assertDeclared();
	}

	//	class A {};
	//	void f(const A& p);
	//	A& g();

	//	void test() {
	//	  f(g());
	//	}
	public void testFunctionCallWithReferenceParameter() throws Exception {
		getPreferenceStore().setValue(PreferenceConstants.FORWARD_DECLARE_FUNCTIONS, true);
		assertDefined();
		assertDeclared("A", "f", "g");
	}

	//	struct A {
	//	  A(const char* s);
	//	};
	//	void f(A p);

	//	void test() {
	//	  f("");
	//	}
	public void testFunctionCallWithTypeConversion_1() throws Exception {
		// A header declaring the function is responsible for defining the parameter type that
		// provides constructor that can be used for implicit conversion.
		assertDefined("f");
		assertDeclared();
	}

	//	struct A {};
	//	struct B { operator A(); };
	//	void f(A p);

	//	void test(B b) {
	//	  f(b);
	//	}
	public void testFunctionCallWithTypeConversion_2() throws Exception {
		// A header declaring the function is not responsible for defining the parameter type since
		// the implicit conversion from B to A is provided externally to parameter type.
		assertDefined("A", "B", "f");
		assertDeclared();
	}

	//	typedef int int32;
	//	void f(int32* p);

	//	void test(int i) {
	//	  f(&i);
	//	}
	public void testFunctionCallWithTypedef() throws Exception {
		getPreferenceStore().setValue(PreferenceConstants.FORWARD_DECLARE_FUNCTIONS, false);
		assertDefined("f");
		assertDeclared();
	}

	//	struct A {
	//	  A(void* p);
	//	};

	//	void test() {
	//	  A(nullptr);
	//	}
	public void testConstructorCall() throws Exception {
		getPreferenceStore().setValue(PreferenceConstants.FORWARD_DECLARE_FUNCTIONS, true);
		assertDefined("A", "A::A");
		assertDeclared();
	}

	//	struct A {
	//	  A(void* p);
	//	};
	//	typedef A B;

	//	void test() {
	//	  B(nullptr);
	//	}
	public void testConstructorCallWithTypedef() throws Exception {
		getPreferenceStore().setValue(PreferenceConstants.FORWARD_DECLARE_FUNCTIONS, true);
		assertDefined("B", "A::A");
		assertDeclared();
	}

	//	struct A {
	//	  A(const B& b);
	//	};
	//	struct B {};

	//	A f(B* b) {
	//	  return *b;
	//	}
	public void testFunctionReturnType_1() throws Exception {
		assertDefined("A", "B");
		assertDeclared();
	}

	//	class A;

	//	A* f() {
	//	  return nullptr;
	//	}
	public void testFunctionReturnType_2() throws Exception {
		assertDefined();
		assertDeclared("A");
	}

	//	class A;

	//	const A* f(A* a) {
	//	  return a;
	//	}
	public void testFunctionReturnType_3() throws Exception {
		assertDefined();
		assertDeclared("A");
	}

	//	class A {
	//	  void m();
	//	};

	//	void A::m() {
	//	}
	public void testMethodDefinition() throws Exception {
		assertDefined("A");
		assertDeclared();
	}

	//	class A {};
	//	class B {};
	//	class C {};
	//	class D {};

	//	void foo(A* a, B& b, C& c) {
	//	  A& aa(*a);
	//	  B* bb(&b);
	//	  C cc(c);
	//	  D d;
	//	}
	public void testVariableDeclaration() throws Exception {
		assertDefined("C", "D");
		assertDeclared("A", "B");
	}

	//	class A {};
	//	class B {};
	//	class C {};

	//	class D {
	//	  A* aa;
	//	  B& bb;
	//	  C cc;
	//	  D(A* a, B& b, C& c)
	//        : aa(a), bb(b), cc(c) {}
	//	};
	public void testConstructorChainInitializer() throws Exception {
		assertDefined("C");
		assertDeclared("A", "B");
	}

	//	class A {};
	//	class B : public A {};
	//	extern A* a;
	//	extern B* b;

	//	void test() {
	//	  a = { b };
	//	}
	public void testInitializerList_506529() throws Exception {
		assertDefined("B", "a", "b");
	}

	//	namespace ns1 {
	//	namespace ns2 {
	//	class A {};
	//	}
	//	}
	//	namespace ns = ns1::ns2;

	//	ns::A a;
	public void testNamespaceAlias() throws Exception {
		assertDefined("ns1::ns2::A", "ns");
		assertDeclared();
	}

	//	namespace ns {
	//	class A {};
	//	}

	//	using ns::A;
	public void testUsingDeclaration() throws Exception {
		assertDefined();
		assertDeclared("ns::A");
	}

	//	struct A {
	//	  A(const char* s);
	//	};
	//	struct B {
	//	  explicit B(const char* s);
	//	};

	//	void f(A a, B b);
	public void testFunctionDeclarationWithTypeConversion() throws Exception {
		// A file declaring the function is responsible for defining the parameter type that
		// provides constructor that can be used for implicit conversion.
		assertDefined("A");
		assertDeclared("B");
	}

	//	struct A {};
	//	struct B {};

	//	struct C {
	//	  A a;
	//	  static B b;
	//	};
	public void testFieldReference() throws Exception {
		assertDefined("A");
		assertDeclared("B");
	}

	//	namespace std {
	//  template<typename T> class shared_ptr {};
	//	}
	//
	//	struct A {
	//	  int x;
	//	};
	//	struct B {
	//	  const std::shared_ptr<A> y;
	//	};

	//	int test(B* b) {
	//	  return b->y->x;
	//	};
	public void testFieldReference_487971() throws Exception {
		assertDefined("A", "B");
		assertDeclared();
	}

	//	typedef unsigned int size_t;
	//	size_t a;

	//	void test() {
	//	  void* x = &a;
	//	}
	public void testVariableReference() throws Exception {
		assertDefined("a"); // Forward declaration of variables is not allowed by default.
		assertDeclared();
	}

	//	struct A {
	//	  void operator()(int p);
	//	};
	//	A a;

	//	void test() {
	//	  a(1);
	//	}
	public void testCallOperator() throws Exception {
		assertDefined("A", "a", "A::operator ()");
		assertDeclared();
	}

	//	struct A {
	//	  int x;
	//	};
	//	inline bool operator==(const A& a1, const A& a2) {
	//	  return a1.x == a2.x;
	//	}

	//	bool test(const A& a, const A& b) {
	//	  return a == b;
	//	}
	public void testOverloadedOperator() throws Exception {
		assertDefined("operator ==");
		assertDeclared("A");
	}

	//	class A {};
	//	class B : public A {};

	//	void test(B* b) {
	//	  const A* a = b;
	//	}
	public void testBaseClass() throws Exception {
		assertDefined("B");
		assertDeclared();
	}

	//	class Base {};

	//	class Derived : public Base {
	//	public:
	//	  Derived();
	//	};
	public void testBaseClause_421398() throws Exception {
		assertDefined("Base");
		assertDeclared();
	}

	//	struct A {};
	//	template<typename T> struct B {};
	//	template<typename T, typename U = B<T>> struct C {};

	//	struct D : public C<A> {};
	public void testTemplate_1() throws Exception {
		assertDefined("A", "C");
		assertDeclared();
	}

	//	struct A {};
	//	template<typename T> struct B {};
	//	template<typename T, typename U = B<T>> struct C {};
	//	struct D : public C<A> {};

	//	void test() {
	//	  D d;
	//	}
	public void testTemplate_2() throws Exception {
		assertDefined("D");
		assertDeclared();
	}

	//	class A;
	//
	//	A& f();
	//
	//	struct A {
	//	  void m();
	//	};

	//	template <typename T>
	//	void g(T& p) {
	//	  p.m();
	//	}
	//
	//	void test() {
	//	  g(f());
	//	}
	public void testTemplateParameter_514197() throws Exception {
		getPreferenceStore().setValue(PreferenceConstants.FORWARD_DECLARE_FUNCTIONS, true);
		assertDefined("A");
		assertDeclared("f");
	}

	//	namespace std {
	//  template<typename T> class shared_ptr {};
	//  template<typename T> class unique_ptr {};
	//	}
	//	class A {};
	//	class B {};
	//	class C {};

	//	using std::unique_ptr;
	//	using std::shared_ptr;
	//
	//	struct P {
	//	  ~P();
	//	  shared_ptr<A> x;
	//	  unique_ptr<A> y;
	//	};
	//
	//	struct Q {
	//	  ~Q() {}
	//	  shared_ptr<B> x;
	//	  unique_ptr<B> y;
	//	};
	//
	//	void test() {
	//	  shared_ptr<C> x;
	//	  unique_ptr<C> y;
	//	}
	public void testTemplatesAllowingIncompleteParameterType_1() throws Exception {
		assertDefined("B", "C", "std::shared_ptr", "std::unique_ptr");
		assertDeclared("A");
	}

	//	namespace std {
	//	template<typename T>
	//	struct unique_ptr {
	//	  T* operator->();
	//	};
	//	}
	//	struct A {
	//	  void m();
	//	};
	//	class B : public A {
	//	};
	//	std::unique_ptr<B> b;

	//	void test() {
	//	  b->m();
	//	}
	public void testTemplatesAllowingIncompleteParameterType_2() throws Exception {
		assertDefined("B", "b", "A::m");
		assertDeclared();
	}

	//	namespace std {
	//	template<typename T>
	//	struct unique_ptr {
	//	  T* operator->();
	//	};
	//	}
	//	struct A {
	//	  void m();
	//	};
	//	class B : public A {
	//	};
	//	struct C {
	//	  std::unique_ptr<B> x;
	//	};

	//	void test(C* c) {
	//	  c->x->m();
	//	}
	public void testTemplatesAllowingIncompleteParameterType_3() throws Exception {
		assertDefined("B", "C", "A::m");
		assertDeclared();
	}

	//	namespace std {
	//	template<typename T>
	//	struct shared_ptr {
	//	  T* operator->();
	//	};
	//	}
	//	struct A {
	//	  void m();
	//	};
	//	class B : public A {
	//	};
	//	std::shared_ptr<B> f();

	//	void test() {
	//	  f()->m();
	//	}
	public void testTemplatesAllowingIncompleteParameterType_4() throws Exception {
		assertDefined("B", "f", "A::m");
		assertDeclared();
	}

	//	namespace std {
	//	template<typename T>
	//	struct unique_ptr {
	//	  T* operator->();
	//	};
	//	}
	//	struct A {
	//	  void m();
	//	};
	//	class B : public A {
	//	};
	//	struct C {
	//	  std::unique_ptr<B> f();
	//	};

	//	void test(C* c) {
	//	  c->f()->m();
	//	}
	public void testTemplatesAllowingIncompleteParameterType_5() throws Exception {
		assertDefined("B", "C", "C::f", "A::m");
		assertDeclared();
	}

	//	struct A {};

	//	auto lambda = [](A* a) { return *a; };
	public void testLambdaExpression() throws Exception {
		assertDefined("A");
		assertDeclared();
	}

	//	struct A {
	//	  void operator()();
	//	};
	//	struct B : public A {
	//	};
	//	struct C {
	//	  B b;
	//	};

	//	void test(C* c) {
	//	  c->b();
	//	}
	public void testFieldAccess_442841_1() throws Exception {
		assertDefined("C", "A::operator ()");
		assertDeclared();
	}

	//	struct A {
	//	  void operator()();
	//	};
	//	struct B : public A {
	//	};
	//	struct C {
	//	  B& b;
	//	};

	//	void test(C* c) {
	//	  c->b();
	//	}
	public void testFieldAccess_442841_2() throws Exception {
		assertDefined("B", "C", "A::operator ()");
		assertDeclared();
	}

	//	struct A {};
	//	struct B {};
	//	struct C {};
	//	struct prefixD {};
	//	#define MACRO(t1, v1, t2, v3, t4, v4) t1 v1; t2 b; C v3; prefix##t4 v4

	//	MACRO(A, a, B, c, D, d);
	public void testMacro_1() throws Exception {
		assertDefined("A", "B", "MACRO");
		assertDeclared();
	}

	//	typedef int INT;
	//	#define MACRO(x) extern INT x

	//	MACRO(a);
	//	INT b;
	public void testMacro_2() throws Exception {
		assertDefined("MACRO", "INT"); // INT has to be defined because it is used outside of MACRO.
		assertDeclared();
	}

	//	class A {};
	//	A f();
	//	#define MACRO(x) A x = f()

	//	MACRO(a);
	public void testMacro_3() throws Exception {
		assertDefined("MACRO");
		assertDeclared();
	}

	//	void f(int);
	//	#define MACRO(name, arg) void name() { f(arg); }

	//  int bar;
	//	MACRO(foo, bar);
	public void testMacro_4() throws Exception {
		assertDefined("MACRO");
		assertDeclared();
	}

	//	template <typename T>
	//	void m();
	//
	//	#define MACRO(a) m<a>()

	//	typedef int INT;
	//	void test() {
	//	  MACRO(INT);
	//	}
	public void testMacro_5() throws Exception {
		assertDefined("MACRO");
		assertDeclared();
	}

	//	struct A {
	//	  A(int);
	//	};
	//
	//	#define MACRO(a, b) A a(b)

	//	void test(int x) {
	//	  MACRO(a, x);
	//	}
	public void testMacro_6() throws Exception {
		assertDefined("MACRO");
		assertDeclared();
	}

	//	#define bool bool
	//	#define false false

	//  bool b = false;
	public void testIdentityMacro_487972() throws Exception {
		assertDefined();
		assertDeclared();
	}
}
