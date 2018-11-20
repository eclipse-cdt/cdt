/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Camelon (IBM Rational Software) - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTEqualsInitializer;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.c.CFunction;
import org.eclipse.cdt.internal.core.model.ASTStringUtil;
import org.eclipse.cdt.internal.core.parser.ParserException;

public class GCCCompleteParseExtensionsTest extends AST2TestBase {

	public GCCCompleteParseExtensionsTest() {
	}

	public GCCCompleteParseExtensionsTest(String name) {
		super(name);
	}

	private IASTTranslationUnit parseGCC(String code) throws ParserException {
		IASTTranslationUnit tu = parse(code, ParserLanguage.C, true, true);

		CNameResolver resolver = new CNameResolver();
		tu.accept(resolver);
		if (resolver.numProblemBindings > 0)
			throw new ParserException(" there are " + resolver.numProblemBindings + " ProblemBindings on the tu"); //$NON-NLS-2$
		if (resolver.numNullBindings > 0)
			throw new ParserException("Expected no null bindings, encountered " + resolver.numNullBindings);
		return tu;
	}

	private IASTTranslationUnit parseGPP(String code) throws ParserException {
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP, true, true);

		CPPNameResolver resolver = new CPPNameResolver();
		tu.accept(resolver);
		if (resolver.numProblemBindings > 0)
			throw new ParserException(" there are " + resolver.numProblemBindings + " ProblemBindings on the tu"); //$NON-NLS-2$
		if (resolver.numNullBindings > 0)
			throw new ParserException("Expected no null bindings, encountered " + resolver.numNullBindings);
		return tu;
	}

	public void testBug39695() throws Exception {
		parseGCC("int a = __alignof__ (int);").getDeclarations();
	}

	public void testBug39684() throws Exception {
		IASTDeclaration bar = parseGCC("typeof(foo(1)) bar () { return foo(1); }").getDeclarations()[0];
		assertTrue(bar instanceof CASTFunctionDefinition);
		CFunction barFunc = (CFunction) ((CASTFunctionDefinition) bar).getDeclarator().getName().resolveBinding();
		IFunctionType type = barFunc.getType();

		// TODO Devin typeof declSpec has 0 length, also doesn't seem to have a type for typeof... raise a bug
		//    	IASTSimpleTypeSpecifier simpleTypeSpec = ((IASTSimpleTypeSpecifier)bar.getReturnType().getTypeSpecifier());
		//		assertEquals(simpleTypeSpec.getType(), IASTGCCSimpleTypeSpecifier.Type.TYPEOF);
	}

	public void testBug39698A() throws Exception {
		IASTDeclaration[] decls = parseGPP("int a=0; \n int b=1; \n int c = a <? b;").getDeclarations();
		assertEquals(ASTStringUtil.getExpressionString(
				(IASTExpression) ((IASTEqualsInitializer) ((IASTSimpleDeclaration) decls[2]).getDeclarators()[0]
						.getInitializer()).getInitializerClause()),
				"a <? b");
	}

	public void testBug39698B() throws Exception {
		IASTDeclaration[] decls = parseGPP("int a=0; \n int b=1; \n int c = a >? b;").getDeclarations();
		assertEquals(ASTStringUtil.getExpressionString(
				(IASTExpression) ((IASTEqualsInitializer) ((IASTSimpleDeclaration) decls[2]).getDeclarators()[0]
						.getInitializer()).getInitializerClause()),
				"a >? b");
	}

	public void testPredefinedSymbol_bug69791() throws Exception {
		parseGPP("typedef __builtin_va_list __gnuc_va_list; \n").getDeclarations();
		parseGCC("typedef __builtin_va_list __gnuc_va_list; \n").getDeclarations();
	}

	public void testBug39697() throws Exception {
		Writer writer = new StringWriter();
		writer.write("__asm__( \"CODE\" );\n");
		writer.write("__inline__ int foo() { return 4; }\n");
		writer.write("__const__ int constInt;\n");
		writer.write("__volatile__ int volInt;\n");
		writer.write("__signed__ int signedInt;\n");
		IASTDeclaration[] decls = parseGCC(writer.toString()).getDeclarations();

		assertEquals(((IASTASMDeclaration) decls[0]).getAssembly(), "\"CODE\"");
		assertTrue(((IASTFunctionDefinition) decls[1]).getDeclSpecifier().isInline());
		assertTrue(((IASTSimpleDeclaration) decls[2]).getDeclSpecifier().isConst());
		assertTrue(((IASTSimpleDeclaration) decls[3]).getDeclSpecifier().isVolatile());
		assertTrue(((ICASTSimpleDeclSpecifier) ((IASTSimpleDeclaration) decls[4]).getDeclSpecifier()).isSigned());

		writer = new StringWriter();
		writer.write("int * __restrict__ resPointer1;\n");
		writer.write("int * __restrict resPointer2;\n");
		decls = parseGCC(writer.toString()).getDeclarations();
		assertTrue(((ICASTPointer) ((IASTSimpleDeclaration) decls[0]).getDeclarators()[0].getPointerOperators()[0])
				.isRestrict());
		assertTrue(((ICASTPointer) ((IASTSimpleDeclaration) decls[1]).getDeclarators()[0].getPointerOperators()[0])
				.isRestrict());

		writer = new StringWriter();
		writer.write("int * __restrict__ resPointer1;\n");
		writer.write("int * __restrict resPointer2;\n");
		decls = parseGPP(writer.toString()).getDeclarations();
		assertTrue(((IASTPointer) ((IASTSimpleDeclaration) decls[0]).getDeclarators()[0].getPointerOperators()[0])
				.isRestrict());
		assertTrue(((IASTPointer) ((IASTSimpleDeclaration) decls[1]).getDeclarators()[0].getPointerOperators()[0])
				.isRestrict());
	}

	public void testBug73954A() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("void f(){							\n");
		writer.write("	__builtin_expect( 23, 2); 		\n");
		writer.write("	__builtin_prefetch( (const void *)0, 1, 2);				\n");
		writer.write("	__builtin_huge_val();			\n");
		writer.write("	__builtin_huge_valf();			\n");
		writer.write("	__builtin_huge_vall();			\n");
		writer.write("	__builtin_inf();				\n");
		writer.write("	__builtin_inff();				\n");
		writer.write("	__builtin_infl();				\n");
		writer.write("	__builtin_nan(\"\");			\n");
		writer.write("	__builtin_nanf(\"\");			\n");
		writer.write("	__builtin_nanl(\"\");			\n");
		writer.write("	__builtin_nans(\"\");			\n");
		writer.write("	__builtin_nansf(\"\");			\n");
		writer.write("	__builtin_nansl(\"\");			\n");
		writer.write("	__builtin_ffs (0);				\n");
		writer.write("	__builtin_clz (0);				\n");
		writer.write("	__builtin_ctz (0);				\n");
		writer.write("	__builtin_popcount (0);			\n");
		writer.write("	__builtin_parity (0);			\n");
		writer.write("	__builtin_ffsl (0);				\n");
		writer.write("	__builtin_clzl (0);				\n");
		writer.write("	__builtin_ctzl (0);				\n");
		writer.write("	__builtin_popcountl (0);		\n");
		writer.write("	__builtin_parityl (0);			\n");
		writer.write("	__builtin_ffsll (0);			\n");
		writer.write("	__builtin_clzll (0);			\n");
		writer.write("	__builtin_ctzll (0);			\n");
		writer.write("	__builtin_popcountll (0);		\n");
		writer.write("	__builtin_parityll (0); 		\n");
		writer.write("	__builtin_powi (0, 0); 			\n");
		writer.write("	__builtin_powif (0, 0);	 		\n");
		writer.write("	__builtin_powil (0, 0);   		\n");
		writer.write("}                                 \n");

		parseGCC(writer.toString());
	}

	public void testBug39686() throws Exception {
		Writer code = new StringWriter();
		code.write("__complex__ double x; // complex double\n");
		code.write("__complex__ short int a; // complex short int\n");
		code.write("__complex__ float y = 2.5fi; // 2.5 imaginary float literal\n");
		code.write("__complex__ int z = 3i; // imaginary intege r literal\n");
		code.write("double v = __real__ x; // real part of expression\n");
		code.write("double w = __imag__ x; // imaginary part of expression\n");
		parseGCC(code.toString());
	}

	public void testBug39551B() throws Exception {
		//this used to be 99.99 * __I__, but I don't know where the __I__ came from, its not in C99, nor in GCC
		IASTDeclaration decl = parseGCC("_Imaginary double id = 99.99 * 1i;").getDeclarations()[0];
		// TODO Devin does ICPPASTSimpleDeclSpecifier need something for isImaginary ?
		//		assertEquals(variable.getName(), "id");
		//		assertTrue(((IASTSimpleTypeSpecifier)variable.getAbstractDeclaration().getTypeSpecifier()).isImaginary());
	}

	public void testBug39681() throws Exception {
		Writer code = new StringWriter();
		code.write("double\n");
		code.write("foo (double a, double b)\n");
		code.write("{\n");
		code.write("  double square (double z) { return z * z; }\n");
		code.write("  return square (a) + square (b);\n");
		code.write("}\n");
		parseGCC(code.toString());
	}

	public void testBug39677() throws Exception {
		parseGPP("class B { public: B(); int a;}; B::B() : a(({ 1; })) {}");
		Writer writer = new StringWriter();
		writer.write("int foo(); class B { public: B(); int a;};");
		writer.write("B::B() : a(( { int y = foo (); int z;\n");
		writer.write("if (y > 0) z = y;\n");
		writer.write("else z = - y;\n");
		writer.write("z; })) {}\n");
		parseGPP(writer.toString());

		writer = new StringWriter();
		writer.write("int x = ({ int foo(); int y = foo (); int z;\n");
		writer.write("if (y > 0) z = y;\n");
		writer.write("else z = - y;\n");
		writer.write("z; });\n");
		parseGPP(writer.toString());

		writer = new StringWriter();
		writer.write("int foo();                       \n");
		writer.write("typeof({ int y = foo ();         \n");
		writer.write("         int z;                  \n");
		writer.write("         if (y > 0) z = y;       \n");
		writer.write("         else z = - y;           \n");
		writer.write("         z;                      \n");
		writer.write("       }) zoot;                  \n");

		parseGPP(writer.toString()); // TODO Devin raised bug 93980
	}

	public void testBug75401() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define va_list  __builtin_va_list            \n");
		writer.write("#define va_arg(v,l) __builtin_va_arg(v,l)     \n");
		writer.write("#define va_start(v,l) __builtin_va_start(v,l) \n");
		writer.write("#define va_end(v) __builtin_va_end(v)         \n");
		writer.write("void variadic(int first, ...) {               \n");
		writer.write("   va_list v;                                 \n");
		writer.write("   va_start(v, first);                        \n");
		writer.write("   long l = va_arg(v, long);                  \n");
		writer.write("   va_end(v);                                 \n");
		writer.write("}                                             \n");

		parseGCC(writer.toString());
		parseGPP(writer.toString());
	}

	public void testBug73954B() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define foo(x)                                            \\\n");
		writer.write("  __builtin_choose_expr( 1, foo_d(x), (void)0 )             \n");
		writer.write("int foo_d( int x );                                         \n");
		writer.write("int main() {                                                \n");
		writer.write("   if( __builtin_constant_p(1) &&                           \n");
		writer.write("      __builtin_types_compatible_p( 1, 'c') )               \n");
		writer.write("          foo(1);                                           \n");
		writer.write("}                                                           \n");

		parseGCC(writer.toString());
	}

	public void testGNUExternalTemplate_bug71603() throws Exception {
		parseGPP("template <typename T> \n class A {}; \n extern template class A<int>; \n").getDeclarations();
	}

	public void testBug74190_g_assert_1() throws Exception {
		Writer writer = new StringWriter();
		writer.write("void log( int );               \n");
		writer.write("void f() {                     \n");
		writer.write("    int a = 1;                 \n");
		writer.write("    (void)({ if( a ){ }        \n");
		writer.write("             else{ log( a ); } \n");
		writer.write("           });                 \n");
		writer.write("}                              \n");

		parseGCC(writer.toString());
		parseGPP(writer.toString());
	}

	public void testBug74190_g_return_if_fail() throws Exception {
		Writer writer = new StringWriter();
		writer.write("void f() {                     \n");
		writer.write("    (void)({ if( ( ({ 0; }) ) ) \n");
		writer.write("            { }                \n");
		writer.write("           });                 \n");
		writer.write("}                              \n");

		parseGCC(writer.toString());
		parseGPP(writer.toString());
	}

	public void testBug95635() throws Exception {
		StringWriter writer = new StringWriter();
		writer.write("void f(){                         \n");
		writer.write("    char a[10];                   \n");
		writer.write("    __builtin_va_list b;          \n");
		writer.write("    __builtin_abort();            \n");
		writer.write("    __builtin_exit(1);            \n");
		writer.write("    __builtin__Exit(1);           \n");
		writer.write("    __builtin__exit(1);           \n");
		writer.write("    __builtin_conj(1);            \n");
		writer.write("    __builtin_conjf(1);           \n");
		writer.write("    __builtin_conjl(1);           \n");
		writer.write("    __builtin_creal(1);           \n");
		writer.write("    __builtin_crealf(1);          \n");
		writer.write("    __builtin_creall(1);          \n");
		writer.write("    __builtin_cimag(1);           \n");
		writer.write("    __builtin_cimagf(1);          \n");
		writer.write("    __builtin_cimagl(1);          \n");
		writer.write("    __builtin_imaxabs(1);         \n");
		writer.write("    __builtin_llabs(1);           \n");
		writer.write("    __builtin_vscanf(\"\",b);\n");
		writer.write("    __builtin_vsnprintf(a, 1, \"\", b); \n");
		writer.write("    __builtin_vsscanf(\"\", \"\", b);\n");
		writer.write("    __builtin_cosf(1);            \n");
		writer.write("    __builtin_cosl(1);            \n");
		writer.write("    __builtin_expf(1);            \n");
		writer.write("    __builtin_expl(1);            \n");
		writer.write("    __builtin_fabsf(1);           \n");
		writer.write("    __builtin_fabsl(1);           \n");
		writer.write("    __builtin_logf(1);            \n");
		writer.write("    __builtin_logl(1);            \n");
		writer.write("    __builtin_sinf(1);            \n");
		writer.write("    __builtin_sinl(1);            \n");
		writer.write("    __builtin_sqrtf(1);           \n");
		writer.write("    __builtin_sqrtl(1);           \n");
		writer.write("    __builtin_abs(1);             \n");
		writer.write("    __builtin_cos(1);             \n");
		writer.write("    __builtin_exp(1);             \n");
		writer.write("    __builtin_fabs(1);            \n");
		writer.write("    __builtin_fprintf((void*)0, \"\");\n");
		writer.write("    __builtin_fputs(\"\", (void*)0);\n");
		writer.write("    __builtin_labs(1);            \n");
		writer.write("    __builtin_log(1);             \n");
		writer.write("    __builtin_memcmp((void*)0, (void*)0, 1);\n");
		writer.write("    __builtin_memcpy((void*)0,(void*)0, 1);\n");
		writer.write("    __builtin_memset((void*)0, 1, 1);\n");
		writer.write("    __builtin_printf(\"\");       \n");
		writer.write("    __builtin_putchar(1);         \n");
		writer.write("    __builtin_puts(\"\");            \n");
		writer.write("    __builtin_scanf(\"\");        \n");
		writer.write("    __builtin_sin(1);             \n");
		writer.write("    __builtin_snprintf(a, 1, \"\");\n");
		writer.write("    __builtin_sprintf(a, \"\");\n");
		writer.write("    __builtin_sqrt(1);            \n");
		writer.write("    __builtin_sscanf(\"\", \"\"); \n");
		writer.write("    __builtin_strcat(a, \"\"); \n");
		writer.write("    __builtin_strchr(\"\", 1); \n");
		writer.write("    __builtin_strcmp(\"\", \"\"); \n");
		writer.write("    __builtin_strcpy(a, \"\"); \n");
		writer.write("    __builtin_strcspn(\"\", \"\");\n");
		writer.write("    __builtin_strlen(\"\");       \n");
		writer.write("    __builtin_strncat(a, \"\", 1);\n");
		writer.write("    __builtin_strncmp(\"\", \"\", 1);\n");
		writer.write("    __builtin_strncpy(a, \"\", 1);\n");
		writer.write("    __builtin_strpbrk(\"\", \"\");\n");
		writer.write("    __builtin_strrchr(\"\", 1);   \n");
		writer.write("    __builtin_strspn(\"\", \"\"); \n");
		writer.write("    __builtin_strstr(\"\", \"\"); \n");
		writer.write("    __builtin_strstr(\"\", \"\"); \n");
		writer.write("    __builtin_vprintf(a, b);\n");
		writer.write("    __builtin_vsprintf(a, \"\", b);    \n");
		writer.write("    __builtin_isgreater(1.0,1.0);      \n");
		writer.write("    __builtin_isgreaterequal(1.0,1.0);\n");
		writer.write("    __builtin_isless(1.0,1.0);        \n");
		writer.write("    __builtin_islessequal(1.0,1.0);   \n");
		writer.write("    __builtin_islessgreater(1.0,1.0); \n");
		writer.write("    __builtin_isunordered(1.0,1.0);   \n");
		writer.write("}                                 \n");

		final String code = writer.toString();
		parseGCC(code);
		parseGPP(code);
	}

	// typedef int size_t;  // will be defined in <stddef.h>
	// struct S {int m;};
	// void test() {
	//    int a= __builtin_offsetof(struct S, m);
	// };
	public void test__builtinOffsetof_Bug265001() throws Exception {
		// gcc with __GNUC__ >= 4 defines:
		// #define offsetof(type, field) __builtin_offsetof(type, field)

		String code = getAboveComment();
		parseGCC(code);
		parseGPP(code);
	}

	// typedef struct S {int m;} T;
	// void test() {
	//    int a= __offsetof__(1);
	// };
	public void test__offsetof__Bug265001() throws Exception {
		// gcc with __GNUC__ < 4 defines:
		//		#define offsetof(type, field)					\
		//		(__offsetof__ (reinterpret_cast <__size_t>		\
		//			 (& reinterpret_cast <const volatile char &>	\
		//			  (static_cast<type *> (0)->field))))
		parseGPP(getAboveComment());
	}

	//	void test(){
	//		bool b;
	//		b= __has_nothrow_assign (int);
	//		b= __has_nothrow_copy (int);
	//		b= __has_nothrow_constructor (int);
	//		b= __has_trivial_assign (int);
	//		b= __has_trivial_copy (int);
	//		b= __has_trivial_constructor (int);
	//		b= __has_trivial_destructor (int);
	//		b= __has_virtual_destructor (int);
	//		b= __is_abstract (int);
	//		b= __is_base_of (char, int);
	//		b= __is_class (int);
	//		b= __is_empty (int);
	//		b= __is_enum (int);
	//		b= __is_pod (int);
	//		b= __is_polymorphic (int);
	//		b= __is_union (int);
	//	}
	public void testTypeTraits_Bug342683() throws Exception {
		parseGPP(getAboveComment());
	}

	// __int128 a;
	// unsigned __int128 b;
	public void test__int128() throws Exception {
		String code = getAboveComment();
		parseGCC(code);
		parseGPP(code);
	}

	// __float128 f;
	public void test__float128() throws Exception {
		String code = getAboveComment();
		parseGCC(code);
		parseGPP(code);
	}

	// _Decimal32 x;
	public void test_Decimal32() throws Exception {
		String code = getAboveComment();
		parseGCC(code);
		parseGPP(code);
	}

	// _Decimal64 x;
	public void test_Decimal64() throws Exception {
		String code = getAboveComment();
		parseGCC(code);
		parseGPP(code);
	}

	// _Decimal128 x;
	public void test_Decimal128() throws Exception {
		String code = getAboveComment();
		parseGCC(code);
		parseGPP(code);
	}

	//	struct waldo {
	//	} __attribute__((__aligned__((1))));
	public void test__attribute__aligned_bug400204() throws Exception {
		String code = getAboveComment();
		parseGCC(code);
		parseGPP(code);
	}
}
