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
package org.eclipse.cdt.core.parser.tests;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.cdt.core.parser.ast.ASTUtil;
import org.eclipse.cdt.core.parser.ast.IASTASMDefinition;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTSimpleTypeSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTTemplateInstantiation;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCExpression;
import org.eclipse.cdt.core.parser.ast.gcc.IASTGCCSimpleTypeSpecifier;

/**
 * @author jcamelon
 *
 */
public class GCCCompleteParseExtensionsTest extends CompleteParseBaseTest {

	/**
	 * 
	 */
	public GCCCompleteParseExtensionsTest() {
		super();
	}

	/**
	 * @param name
	 */
	public GCCCompleteParseExtensionsTest(String name) {
		super(name);
	}

    public void testBug39695() throws Exception
    {
        Iterator i = parse("int a = __alignof__ (int);").getDeclarations(); //$NON-NLS-1$
        IASTVariable a = (IASTVariable) i.next();
        assertFalse( i.hasNext() );
        IASTExpression exp = a.getInitializerClause().getAssigmentExpression();
        assertEquals( exp.getExpressionKind(), IASTGCCExpression.Kind.UNARY_ALIGNOF_TYPEID );
        assertEquals( exp.toString(), "__alignof__(int)"); //$NON-NLS-1$
    }
    
    public void testBug39684() throws Exception
    {
    	IASTFunction bar = (IASTFunction) parse("typeof(foo(1)) bar () { return foo(1); }").getDeclarations().next(); //$NON-NLS-1$
    	
    	IASTSimpleTypeSpecifier simpleTypeSpec = ((IASTSimpleTypeSpecifier)bar.getReturnType().getTypeSpecifier());
		assertEquals( simpleTypeSpec.getType(), IASTGCCSimpleTypeSpecifier.Type.TYPEOF );
    }

    public void testBug39698A() throws Exception
    {
        Iterator i = parse("int c = a <? b;").getDeclarations(); //$NON-NLS-1$
        IASTVariable c = (IASTVariable) i.next();
        IASTExpression exp = c.getInitializerClause().getAssigmentExpression();
        assertEquals( ASTUtil.getExpressionString( exp ), "a <? b" ); //$NON-NLS-1$
    }
    public void testBug39698B() throws Exception
    {
    	Iterator i = parse("int c = a >? b;").getDeclarations(); //$NON-NLS-1$
    	IASTVariable c = (IASTVariable) i.next();
        IASTExpression exp = c.getInitializerClause().getAssigmentExpression();
        assertEquals( ASTUtil.getExpressionString( exp ), "a >? b" ); //$NON-NLS-1$
    }

	public void testPredefinedSymbol_bug69791() throws Exception {
		Iterator i = 			parse("typedef __builtin_va_list __gnuc_va_list; \n").getDeclarations();//$NON-NLS-1$
		assertTrue( i.next() instanceof IASTTypedefDeclaration );
		assertFalse(i.hasNext());
	}

	public void testBug39697() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "__asm__( \"CODE\" );\n" ); //$NON-NLS-1$
		writer.write( "__inline__ int foo() { return 4; }\n"); //$NON-NLS-1$
		writer.write( "__const__ int constInt;\n"); //$NON-NLS-1$
		writer.write( "__volatile__ int volInt;\n"); //$NON-NLS-1$
		writer.write( "__signed__ int signedInt;\n"); //$NON-NLS-1$
		Iterator i = parse( writer.toString() ).getDeclarations();
		IASTASMDefinition asmDefinition = (IASTASMDefinition) i.next();
		assertEquals( asmDefinition.getBody(), "CODE"); //$NON-NLS-1$
		IASTFunction foo = (IASTFunction) i.next();
		assertTrue( foo.isInline() );
		IASTVariable constInt = (IASTVariable) i.next();
		assertTrue( constInt.getAbstractDeclaration().isConst());
		IASTVariable volInt = (IASTVariable) i.next();
		assertTrue( volInt.getAbstractDeclaration().isVolatile() );
		IASTVariable signedInt = (IASTVariable) i.next();
		assertTrue( ((IASTSimpleTypeSpecifier) signedInt.getAbstractDeclaration().getTypeSpecifier()).isSigned() );
		assertFalse( i.hasNext() );
		for( int j = 0; j < 2; ++j )
		{
			writer = new StringWriter();
			writer.write( "int * __restrict__ resPointer1;\n"); //$NON-NLS-1$
			writer.write( "int * __restrict resPointer2;\n"); //$NON-NLS-1$
			i = parse( writer.toString(), true, ((j == 0 )? ParserLanguage.C : ParserLanguage.CPP) ).getDeclarations();
			int count = 0;
			while( i.hasNext() )
			{
				++count;
				IASTVariable resPointer = (IASTVariable) i.next();
				Iterator pOps = resPointer.getAbstractDeclaration().getPointerOperators();
				assertTrue( pOps.hasNext() );
				ASTPointerOperator op = (ASTPointerOperator) pOps.next();
				assertFalse( pOps.hasNext() );
				assertEquals( op, ASTPointerOperator.RESTRICT_POINTER );
			}
	
			assertEquals( count, 2 );
		}
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
		writer.write("}                                 \n"); //$NON-NLS-1$
		
	    parse( writer.toString() );
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
        parse(code.toString());
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
        parse(code.toString());
    }
    
    public void testBug39677() throws Exception
    {
        parse("class B { public: B(); int a;}; B::B() : a(({ 1; })) {}"); //$NON-NLS-1$
        Writer writer = new StringWriter();
        writer.write( "B::B() : a(( { int y = foo (); int z;\n" ); //$NON-NLS-1$
        writer.write( "if (y > 0) z = y;\n" ); //$NON-NLS-1$
        writer.write( "else z = - y;\n" );//$NON-NLS-1$
        writer.write( "z; }))\n" );//$NON-NLS-1$
        parse( writer.toString() );
        
        writer = new StringWriter();
        writer.write( "int x = ({ int y = foo (); int z;\n" ); //$NON-NLS-1$
        writer.write( "if (y > 0) z = y;\n" ); //$NON-NLS-1$
        writer.write( "else z = - y;\n" );//$NON-NLS-1$
        writer.write( "z; });\n" );//$NON-NLS-1$
        parse( writer.toString() );
        
        writer = new StringWriter();
        writer.write( "int foo();                       \n" ); //$NON-NLS-1$
        writer.write( "typeof({ int y = foo ();         \n" ); //$NON-NLS-1$
        writer.write( "         int z;                  \n" ); //$NON-NLS-1$
        writer.write( "         if (y > 0) z = y;       \n" ); //$NON-NLS-1$
        writer.write( "         else z = - y;           \n" ); //$NON-NLS-1$
        writer.write( "         z;                      \n" ); //$NON-NLS-1$
        writer.write( "       }) zoot;                  \n" ); //$NON-NLS-1$
        parse( writer.toString() );
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
        
        parse( writer.toString() );
    }
    
    public void testBug73954B() throws Exception{
        Writer writer = new StringWriter();
        writer.write( "#define foo(x)                                            \\\n"); //$NON-NLS-1$
        writer.write( "  __builtin_choose_expr( 1, foob_d(x), (void)0 )             \n"); //$NON-NLS-1$
        writer.write( "int foo_d( int x );                                         \n"); //$NON-NLS-1$
        writer.write( "int main() {                                                \n"); //$NON-NLS-1$
        writer.write( "   if( __builtin_constant_p(1) &&                           \n"); //$NON-NLS-1$
        writer.write( "      __builtin_types_compatible_p( 1, 'c') )               \n"); //$NON-NLS-1$
        writer.write( "          foo(1);                                           \n"); //$NON-NLS-1$
        writer.write( "}                                                           \n"); //$NON-NLS-1$
        
        parse( writer.toString(), true, ParserLanguage.C );
    }
    
	public void testGNUExternalTemplate_bug71603() throws Exception {
		Iterator i = parse("template <typename T> \n class A {}; \n extern template class A<int>; \n").getDeclarations();
		IASTTemplateDeclaration td = (IASTTemplateDeclaration) i.next();
		IASTClassSpecifier cs = (IASTClassSpecifier) td.getOwnedDeclaration();
		IASTTemplateInstantiation ti = (IASTTemplateInstantiation) i.next();
		assertFalse(i.hasNext());
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
	    
	    parse( writer.toString() );
	}
	
	public void testBug74190_g_return_if_fail() throws Exception {
	    Writer writer = new StringWriter();
	    writer.write( "void f() {                     \n"); //$NON-NLS-1$
	    writer.write( "    (void)({ if( ( ({ 0; }) ) ) \n"); //$NON-NLS-1$
	    writer.write( "            { }                \n"); //$NON-NLS-1$
	    writer.write( "           });                 \n"); //$NON-NLS-1$
	    writer.write( "}                              \n"); //$NON-NLS-1$
	    
	    parse( writer.toString() );
	}
}
