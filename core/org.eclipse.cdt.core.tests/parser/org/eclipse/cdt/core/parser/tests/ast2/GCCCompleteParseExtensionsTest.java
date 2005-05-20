/**********************************************************************
 * Copyright (c) 2004 IBM Canada Ltd. and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.dom.ast.ASTSignatureUtil;
import org.eclipse.cdt.core.dom.ast.IASTASMDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFunctionDefinition;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IFunctionType;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.c.ICASTSimpleDeclSpecifier;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.IGPPASTPointer;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.internal.core.dom.parser.c.CASTFunctionDefinition;
import org.eclipse.cdt.internal.core.dom.parser.c.CFunction;
import org.eclipse.cdt.internal.core.parser.ParserException;

/**
 * @author jcamelon
 *
 */
public class GCCCompleteParseExtensionsTest extends AST2BaseTest {

	private IASTTranslationUnit parseGCC(String code) throws ParserException {
		IASTTranslationUnit tu = parse(code, ParserLanguage.C, true, true); 
		
		CNameResolver resolver = new CNameResolver();
		tu.accept(resolver);
		if (resolver.numProblemBindings > 0)
			throw new ParserException(" there are " + resolver.numProblemBindings + " ProblemBindings on the tu"); //$NON-NLS-1$ //$NON-NLS-2$
		if (resolver.numNullBindings > 0)
			throw new ParserException("Expected no null bindings, encountered " + resolver.numNullBindings); //$NON-NLS-1$
		
		return tu;
	}
	
	private IASTTranslationUnit parseGPP(String code) throws ParserException {
		IASTTranslationUnit tu = parse(code, ParserLanguage.CPP, true, true); 

		CPPNameResolver resolver = new CPPNameResolver();
		tu.accept(resolver);
		if (resolver.numProblemBindings > 0)
			throw new ParserException(" there are " + resolver.numProblemBindings + " ProblemBindings on the tu"); //$NON-NLS-1$ //$NON-NLS-2$
		if (resolver.numNullBindings > 0)
			throw new ParserException("Expected no null bindings, encountered " + resolver.numNullBindings); //$NON-NLS-1$
		
		return tu;
	}
	
    public void testBug39695() throws Exception
    {
        parseGCC("int a = __alignof__ (int);").getDeclarations(); //$NON-NLS-1$
    }
    
    public void testBug39684() throws Exception
    {
    	IASTDeclaration bar = parseGCC("typeof(foo(1)) bar () { return foo(1); }").getDeclarations()[0]; //$NON-NLS-1$
		assertTrue(bar instanceof CASTFunctionDefinition);
		CFunction barFunc = (CFunction)((CASTFunctionDefinition)bar).getDeclarator().getName().resolveBinding();
		IFunctionType type = barFunc.getType();
		
    	// TODO Devin typeof declSpec has 0 length, also doesn't seem to have a type for typeof... raise a bug
//    	IASTSimpleTypeSpecifier simpleTypeSpec = ((IASTSimpleTypeSpecifier)bar.getReturnType().getTypeSpecifier());
//		assertEquals( simpleTypeSpec.getType(), IASTGCCSimpleTypeSpecifier.Type.TYPEOF );
    }

    public void testBug39698A() throws Exception
    {
        IASTDeclaration[] decls = parseGPP("int a=0; \n int b=1; \n int c = a <? b;").getDeclarations(); //$NON-NLS-1$
        assertEquals( ASTSignatureUtil.getExpressionString( ((IASTInitializerExpression)((IASTSimpleDeclaration)decls[2]).getDeclarators()[0].getInitializer()).getExpression() ), "a <? b" ); //$NON-NLS-1$
    }
    public void testBug39698B() throws Exception
    {
    	IASTDeclaration[] decls = parseGPP("int a=0; \n int b=1; \n int c = a >? b;").getDeclarations(); //$NON-NLS-1$
        assertEquals( ASTSignatureUtil.getExpressionString( ((IASTInitializerExpression)((IASTSimpleDeclaration)decls[2]).getDeclarators()[0].getInitializer()).getExpression() ), "a >? b" ); //$NON-NLS-1$
    }

	public void testPredefinedSymbol_bug69791() throws Exception {
		parseGPP("typedef __builtin_va_list __gnuc_va_list; \n").getDeclarations();//$NON-NLS-1$
		parseGCC("typedef __builtin_va_list __gnuc_va_list; \n").getDeclarations();//$NON-NLS-1$
	}

	public void testBug39697() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "__asm__( \"CODE\" );\n" ); //$NON-NLS-1$
		writer.write( "__inline__ int foo() { return 4; }\n"); //$NON-NLS-1$
		writer.write( "__const__ int constInt;\n"); //$NON-NLS-1$
		writer.write( "__volatile__ int volInt;\n"); //$NON-NLS-1$
		writer.write( "__signed__ int signedInt;\n"); //$NON-NLS-1$
		IASTDeclaration[] decls = parseGCC( writer.toString() ).getDeclarations();
		
        assertEquals(((IASTASMDeclaration)decls[0]).getAssembly(), "CODE"); //$NON-NLS-1$
        assertTrue( ((IASTFunctionDefinition)decls[1]).getDeclSpecifier().isInline() );
        assertTrue( ((IASTSimpleDeclaration)decls[2]).getDeclSpecifier().isConst() );
        assertTrue( ((IASTSimpleDeclaration)decls[3]).getDeclSpecifier().isVolatile() );
        assertTrue( ((ICASTSimpleDeclSpecifier)((IASTSimpleDeclaration)decls[4]).getDeclSpecifier()).isSigned() );

        writer = new StringWriter();
        writer.write( "int * __restrict__ resPointer1;\n"); //$NON-NLS-1$
        writer.write( "int * __restrict resPointer2;\n"); //$NON-NLS-1$
        decls = parseGCC( writer.toString() ).getDeclarations();
        assertTrue( ((ICASTPointer)((IASTSimpleDeclaration)decls[0]).getDeclarators()[0].getPointerOperators()[0]).isRestrict() );
        assertTrue( ((ICASTPointer)((IASTSimpleDeclaration)decls[1]).getDeclarators()[0].getPointerOperators()[0]).isRestrict() );

        writer = new StringWriter();
        writer.write( "int * __restrict__ resPointer1;\n"); //$NON-NLS-1$
        writer.write( "int * __restrict resPointer2;\n"); //$NON-NLS-1$
        decls = parseGPP( writer.toString() ).getDeclarations();
        assertTrue( ((IGPPASTPointer)((IASTSimpleDeclaration)decls[0]).getDeclarators()[0].getPointerOperators()[0]).isRestrict() );
        assertTrue( ((IGPPASTPointer)((IASTSimpleDeclaration)decls[1]).getDeclarators()[0].getPointerOperators()[0]).isRestrict() );

	}

	public void testBug73954A() throws Exception{
	    StringWriter writer = new StringWriter();
		writer.write("void f(){							\n");//$NON-NLS-1$
		writer.write("	__builtin_expect( 23, 2); 		\n");//$NON-NLS-1$
		writer.write("	__builtin_prefetch( (const void *)0, 1, 2);				\n");//$NON-NLS-1$
		writer.write("	__builtin_huge_val();			\n");//$NON-NLS-1$
		writer.write("	__builtin_huge_valf();			\n");//$NON-NLS-1$
		writer.write("	__builtin_huge_vall();			\n");//$NON-NLS-1$
		writer.write("	__builtin_inf();				\n");//$NON-NLS-1$
		writer.write("	__builtin_inff();				\n");//$NON-NLS-1$
		writer.write("	__builtin_infl();				\n");//$NON-NLS-1$
		writer.write("	__builtin_nan(\"\");			\n");//$NON-NLS-1$
		writer.write("	__builtin_nanf(\"\");			\n");//$NON-NLS-1$
		writer.write("	__builtin_nanl(\"\");			\n");//$NON-NLS-1$
		writer.write("	__builtin_nans(\"\");			\n");//$NON-NLS-1$
		writer.write("	__builtin_nansf(\"\");			\n");//$NON-NLS-1$
		writer.write("	__builtin_nansl(\"\");			\n");//$NON-NLS-1$
		writer.write("	__builtin_ffs (0);				\n");//$NON-NLS-1$
		writer.write("	__builtin_clz (0);				\n");//$NON-NLS-1$
		writer.write("	__builtin_ctz (0);				\n");//$NON-NLS-1$
		writer.write("	__builtin_popcount (0);			\n");//$NON-NLS-1$
		writer.write("	__builtin_parity (0);			\n");//$NON-NLS-1$
		writer.write("	__builtin_ffsl (0);				\n");//$NON-NLS-1$
		writer.write("	__builtin_clzl (0);				\n");//$NON-NLS-1$
		writer.write("	__builtin_ctzl (0);				\n");//$NON-NLS-1$
		writer.write("	__builtin_popcountl (0);		\n");//$NON-NLS-1$
		writer.write("	__builtin_parityl (0);			\n");//$NON-NLS-1$
		writer.write("	__builtin_ffsll (0);			\n");//$NON-NLS-1$
		writer.write("	__builtin_clzll (0);			\n");//$NON-NLS-1$
		writer.write("	__builtin_ctzll (0);			\n");//$NON-NLS-1$
		writer.write("	__builtin_popcountll (0);		\n");//$NON-NLS-1$
		writer.write("	__builtin_parityll (0); 		\n");//$NON-NLS-1$
		writer.write("	__builtin_powi (0, 0); 			\n");//$NON-NLS-1$
		writer.write("	__builtin_powif (0, 0);	 		\n");//$NON-NLS-1$
		writer.write("	__builtin_powil (0, 0);   		\n");//$NON-NLS-1$
		writer.write("}                                 \n"); //$NON-NLS-1$
		
	    parseGCC( writer.toString() );
	}
	
    public void testBug39686() throws Exception
    {
        Writer code = new StringWriter();
        code.write("__complex__ double x; // complex double\n"); //$NON-NLS-1$
        code.write("__complex__ short int a; // complex short int\n"); //$NON-NLS-1$
        code.write("__complex__ float y = 2.5fi; // 2.5 imaginary float literal\n"); //$NON-NLS-1$
        code.write("__complex__ int z = 3i; // imaginary intege r literal\n"); //$NON-NLS-1$
        code.write("double v = __real__ x; // real part of expression\n"); //$NON-NLS-1$
        code.write("double w = __imag__ x; // imaginary part of expression\n"); //$NON-NLS-1$
        parseGCC(code.toString());
    }
    
	public void testBug39551B() throws Exception
	{
	    //this used to be 99.99 * __I__, but I don't know where the __I__ came from, its not in C99, nor in GCC
		IASTDeclaration decl = parseGCC("_Imaginary double id = 99.99 * 1i;").getDeclarations()[0]; //$NON-NLS-1$
		// TODO Devin does ICPPASTSimpleDeclSpecifier need something for isImaginary ?
		//		assertEquals( variable.getName(), "id"); //$NON-NLS-1$
//		assertTrue( ((IASTSimpleTypeSpecifier)variable.getAbstractDeclaration().getTypeSpecifier()).isImaginary() );
	}
    
    public void testBug39681() throws Exception
    {
        Writer code = new StringWriter();
        code.write("double\n"); //$NON-NLS-1$
        code.write("foo (double a, double b)\n"); //$NON-NLS-1$
        code.write("{\n"); //$NON-NLS-1$
        code.write("  double square (double z) { return z * z; }\n"); //$NON-NLS-1$
        code.write("  return square (a) + square (b);\n"); //$NON-NLS-1$
        code.write("}\n"); //$NON-NLS-1$
        parseGCC(code.toString());
    }
    
    public void testBug39677() throws Exception
    {
		parseGPP("class B { public: B(); int a;}; B::B() : a(({ 1; })) {}"); //$NON-NLS-1$
        Writer writer = new StringWriter();
        writer.write( "B::B() : a(( { int y = foo (); int z;\n" ); //$NON-NLS-1$
        writer.write( "if (y > 0) z = y;\n" ); //$NON-NLS-1$
        writer.write( "else z = - y;\n" );//$NON-NLS-1$
        writer.write( "z; }))\n" );//$NON-NLS-1$
		parseGPP( writer.toString() );
        
        writer = new StringWriter();
        writer.write( "int x = ({ int foo() { return 1; } int y = foo (); int z;\n" ); //$NON-NLS-1$
        writer.write( "if (y > 0) z = y;\n" ); //$NON-NLS-1$
        writer.write( "else z = - y;\n" );//$NON-NLS-1$
        writer.write( "z; });\n" );//$NON-NLS-1$
		parseGPP( writer.toString() );
        
        writer = new StringWriter();
        writer.write( "int foo();                       \n" ); //$NON-NLS-1$
        writer.write( "typeof({ int y = foo ();         \n" ); //$NON-NLS-1$
        writer.write( "         int z;                  \n" ); //$NON-NLS-1$
        writer.write( "         if (y > 0) z = y;       \n" ); //$NON-NLS-1$
        writer.write( "         else z = - y;           \n" ); //$NON-NLS-1$
        writer.write( "         z;                      \n" ); //$NON-NLS-1$
        writer.write( "       }) zoot;                  \n" ); //$NON-NLS-1$
        
        parseGPP( writer.toString() ); // TODO Devin raised bug 93980
    }
    
    public void testBug75401() throws Exception
    {
        Writer writer = new StringWriter();
        writer.write( "#define va_arg __builtin_va_arg      \n"); //$NON-NLS-1$
        writer.write( "#define va_list __builtin_va_list    \n"); //$NON-NLS-1$
        writer.write( "void main( int argc, char** argv ) { \n"); //$NON-NLS-1$
        writer.write( "   va_list v;                        \n"); //$NON-NLS-1$
        writer.write( "   long l = va_arg( v, long );       \n"); //$NON-NLS-1$
        writer.write( "}                                    \n"); //$NON-NLS-1$
        
        parseGCC( writer.toString() );
    }
    
    public void testBug73954B() throws Exception{
        Writer writer = new StringWriter();
        writer.write( "#define foo(x)                                            \\\n"); //$NON-NLS-1$
        writer.write( "  __builtin_choose_expr( 1, foo_d(x), (void)0 )             \n"); //$NON-NLS-1$
        writer.write( "int foo_d( int x );                                         \n"); //$NON-NLS-1$
        writer.write( "int main() {                                                \n"); //$NON-NLS-1$
        writer.write( "   if( __builtin_constant_p(1) &&                           \n"); //$NON-NLS-1$
        writer.write( "      __builtin_types_compatible_p( 1, 'c') )               \n"); //$NON-NLS-1$
        writer.write( "          foo(1);                                           \n"); //$NON-NLS-1$
        writer.write( "}                                                           \n"); //$NON-NLS-1$
        
        parseGCC( writer.toString());
    }
    
	public void testGNUExternalTemplate_bug71603() throws Exception {
		parseGPP("template <typename T> \n class A {}; \n extern template class A<int>; \n").getDeclarations(); //$NON-NLS-1$
	}

	public void testBug74190_g_assert_1() throws Exception {
	    Writer writer = new StringWriter();
	    writer.write( "void log( int );               \n"); //$NON-NLS-1$
	    writer.write( "void f() {                     \n"); //$NON-NLS-1$
	    writer.write( "    int a = 1;                 \n"); //$NON-NLS-1$
	    writer.write( "    (void)({ if( a ){ }        \n"); //$NON-NLS-1$
	    writer.write( "             else{ log( a ); } \n"); //$NON-NLS-1$
	    writer.write( "           });                 \n"); //$NON-NLS-1$
	    writer.write( "}                              \n"); //$NON-NLS-1$
	    
        try {
            parseGCC( writer.toString() ); // TODO Devin raised bug 93980
            assertFalse(true);
        } catch (Exception e) {}
	}
	
	public void testBug74190_g_return_if_fail() throws Exception {
	    Writer writer = new StringWriter();
	    writer.write( "void f() {                     \n"); //$NON-NLS-1$
	    writer.write( "    (void)({ if( ( ({ 0; }) ) ) \n"); //$NON-NLS-1$
	    writer.write( "            { }                \n"); //$NON-NLS-1$
	    writer.write( "           });                 \n"); //$NON-NLS-1$
	    writer.write( "}                              \n"); //$NON-NLS-1$
	    
        try {
            parseGCC( writer.toString() ); // TODO Devin raised bug 93982
            assertFalse(true);
        } catch (Exception e) {}
	}
    
	public void testBug95635() throws Exception{
        StringWriter writer = new StringWriter();
        writer.write("void f(){                         \n");//$NON-NLS-1$
        writer.write("    char a[10];                   \n"); //$NON-NLS-1$
        writer.write("    __builtin_va_list b;          \n"); //$NON-NLS-1$
        writer.write("    __builtin_abort();            \n");//$NON-NLS-1$
        writer.write("    __builtin_exit(1);            \n");//$NON-NLS-1$
        writer.write("    __builtin__Exit(1);           \n");//$NON-NLS-1$
        writer.write("    __builtin__exit(1);           \n");//$NON-NLS-1$
        writer.write("    __builtin_conj(1);            \n");//$NON-NLS-1$
        writer.write("    __builtin_conjf(1);           \n");//$NON-NLS-1$
        writer.write("    __builtin_conjl(1);           \n");//$NON-NLS-1$
        writer.write("    __builtin_creal(1);           \n");//$NON-NLS-1$
        writer.write("    __builtin_crealf(1);          \n");//$NON-NLS-1$
        writer.write("    __builtin_creall(1);          \n");//$NON-NLS-1$
        writer.write("    __builtin_cimag(1);           \n");//$NON-NLS-1$
        writer.write("    __builtin_cimagf(1);          \n");//$NON-NLS-1$
        writer.write("    __builtin_cimagl(1);          \n");//$NON-NLS-1$
        writer.write("    __builtin_imaxabs(1);         \n");//$NON-NLS-1$
        writer.write("    __builtin_llabs(1);           \n");//$NON-NLS-1$
        writer.write("    __builtin_vscanf(\"\",b);\n");//$NON-NLS-1$
        writer.write("    __builtin_vsnprintf(a, 1, \"\", b); \n");//$NON-NLS-1$
        writer.write("    __builtin_vsscanf(\"\", \"\", b);\n");//$NON-NLS-1$
        writer.write("    __builtin_cosf(1);            \n");//$NON-NLS-1$
        writer.write("    __builtin_cosl(1);            \n");//$NON-NLS-1$
        writer.write("    __builtin_expf(1);            \n");//$NON-NLS-1$
        writer.write("    __builtin_expl(1);            \n");//$NON-NLS-1$
        writer.write("    __builtin_fabsf(1);           \n");//$NON-NLS-1$
        writer.write("    __builtin_fabsl(1);           \n");//$NON-NLS-1$
        writer.write("    __builtin_logf(1);            \n");//$NON-NLS-1$
        writer.write("    __builtin_logl(1);            \n");//$NON-NLS-1$
        writer.write("    __builtin_sinf(1);            \n");//$NON-NLS-1$
        writer.write("    __builtin_sinl(1);            \n");//$NON-NLS-1$
        writer.write("    __builtin_sqrtf(1);           \n");//$NON-NLS-1$
        writer.write("    __builtin_sqrtl(1);           \n");//$NON-NLS-1$
        writer.write("    __builtin_abs(1);             \n");//$NON-NLS-1$
        writer.write("    __builtin_cos(1);             \n");//$NON-NLS-1$
        writer.write("    __builtin_exp(1);             \n");//$NON-NLS-1$
        writer.write("    __builtin_fabs(1);            \n");//$NON-NLS-1$
        writer.write("    __builtin_fprintf((void*)0, \"\");\n");//$NON-NLS-1$
        writer.write("    __builtin_fputs(\"\", (void*)0);\n");//$NON-NLS-1$
        writer.write("    __builtin_labs(1);            \n");//$NON-NLS-1$
        writer.write("    __builtin_log(1);             \n");//$NON-NLS-1$
        writer.write("    __builtin_memcmp((void*)0, (void*)0, 1);\n");//$NON-NLS-1$
        writer.write("    __builtin_memcpy((void*)0,(void*)0, 1);\n");//$NON-NLS-1$
        writer.write("    __builtin_memset((void*)0, 1, 1);\n");//$NON-NLS-1$
        writer.write("    __builtin_printf(\"\");       \n");//$NON-NLS-1$
        writer.write("    __builtin_putchar(1);         \n");//$NON-NLS-1$
        writer.write("    __builtin_puts(\"\");            \n");//$NON-NLS-1$
        writer.write("    __builtin_scanf(\"\");        \n");//$NON-NLS-1$
        writer.write("    __builtin_sin(1);             \n");//$NON-NLS-1$
        writer.write("    __builtin_snprintf(a, 1, \"\");\n");//$NON-NLS-1$
        writer.write("    __builtin_sprintf(a, \"\");\n");//$NON-NLS-1$
        writer.write("    __builtin_sqrt(1);            \n");//$NON-NLS-1$
        writer.write("    __builtin_sscanf(\"\", \"\"); \n");//$NON-NLS-1$1
        writer.write("    __builtin_strcat(a, \"\"); \n");//$NON-NLS-1$
        writer.write("    __builtin_strchr(\"\", 1); \n");//$NON-NLS-1$
        writer.write("    __builtin_strcmp(\"\", \"\"); \n");//$NON-NLS-1$
        writer.write("    __builtin_strcpy(a, \"\"); \n");//$NON-NLS-1$
        writer.write("    __builtin_strcspn(\"\", \"\");\n");//$NON-NLS-1$
        writer.write("    __builtin_strlen(\"\");       \n");//$NON-NLS-1$
        writer.write("    __builtin_strncat(a, \"\", 1);\n");//$NON-NLS-1$
        writer.write("    __builtin_strncmp(\"\", \"\", 1);\n");//$NON-NLS-1$
        writer.write("    __builtin_strncpy(a, \"\", 1);\n");//$NON-NLS-1$
        writer.write("    __builtin_strpbrk(\"\", \"\");\n");//$NON-NLS-1$
        writer.write("    __builtin_strrchr(\"\", 1);   \n");//$NON-NLS-1$
        writer.write("    __builtin_strspn(\"\", \"\"); \n");//$NON-NLS-1$
        writer.write("    __builtin_strstr(\"\", \"\"); \n");//$NON-NLS-1$
        writer.write("    __builtin_strstr(\"\", \"\"); \n");//$NON-NLS-1$
        writer.write("    __builtin_vprintf(a, b);\n");//$NON-NLS-1$
        writer.write("    __builtin_vsprintf(a, 1, \"\", b);    \n");//$NON-NLS-1$
        writer.write("    __builtin_isgreater(1,1);      \n");//$NON-NLS-1$
        writer.write("    __builtin_isgreaterequal(1,1);\n");//$NON-NLS-1$
        writer.write("    __builtin_isless(1,1);        \n");//$NON-NLS-1$
        writer.write("    __builtin_islessequal(1,1);   \n");//$NON-NLS-1$
        writer.write("    __builtin_islessgreater(1,1); \n");//$NON-NLS-1$
        writer.write("    __builtin_isunordered(1,1);   \n");//$NON-NLS-1$
        writer.write("}                                 \n"); //$NON-NLS-1$
        
        parseGCC( writer.toString() );
        parseGPP( writer.toString() );
    }
	
}
