/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.failedTests;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTPointerToFunction;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.tests.BaseASTTest;
/**
 * @author jcamelon
 *
 */
public class ASTFailedTests extends BaseASTTest
{
    private static final boolean debugging = false;
    public ASTFailedTests(String name)
    {
        super(name);
    }
    public void testBug36730() throws Exception
    {
        assertCodeFailsParse("FUNCTION_MACRO( 1, a )\n	int i;");
    }
    public void testBug39504A() throws Exception
    {
    	try
    	{
        	IASTVariable variable = (IASTVariable)parse("int y = sizeof(x[0]);").getDeclarations().next();
    	}
    	catch( ClassCastException cce )
    	{
    		assertFalse( "We should not get a cast error here", false );
    	}
    }
    public void testBug39504B() throws Exception
    {
        assertCodeFailsParse("int y = sizeof (int*);");
    }
    public void testBug39505A() throws Exception
    {
        assertCodeFailsParse("int AD::* gp_down = static_cast<int AD::*>(gp_stat);");
    }
    public void testBug39505B() throws Exception
    {
        assertCodeFailsParse("int* gp_down = static_cast<int*>(gp_stat);");
    }
   
    public void testBug39525() throws Exception
    {
        assertCodeFailsParse("C &(C::*DD)(const C &x) = &C::operator=;");
    }
    public void testBug39526() throws Exception
    {
        assertCodeFailsParse("UnitList unit_list (String(\"keV\"));");
    }
    public void testBug39528() throws Exception
    {
        Writer code = new StringWriter();
        try
        {
            code.write("struct B: public A {\n");
            code.write("  A a;\n");
            code.write("  B() try : A(1), a(2)\n");
            code.write("	{ throw 1; }\n");
            code.write("  catch (...)\n");
            code.write("	{ if (c != 3) r |= 1; }\n");
            code.write("};\n");
        }
        catch (IOException ioe)
        {
        }
        assertCodeFailsParse(code.toString());
    }
    public void testBug39531() throws Exception
    {
        assertCodeFailsParse("class AString { operator char const *() const; };");
    }
    public void testBug39532() throws Exception
    {
        assertCodeFailsParse("class N1::N2::B : public A {};");
    }
    public void testBug39535() throws Exception
    {
        assertCodeFailsParse("namespace bar = foo;");
    }
    public void testBug39536A() throws Exception
    {
        assertCodeFailsParse("template<class E> class X { X<E>(); };");
    }
    public void testBug39536B() throws Exception
    {
        assertCodeFailsParse("template<class E> class X { inline X<E>(int); };");
    }
    public void testBug39538() throws Exception
    {
        assertCodeFailsParse("template C::operator int<float> ();");
    }
    public void testBug39540() throws Exception
    {
        assertCodeFailsParse("class {} const null;");
    }
    public void testBug39542() throws Exception
    {
        assertCodeFailsParse("void f(int a, struct {int b[a];} c) {}");
    }

    //Here starts C99-specific section
    public void testBug39549() throws Exception
    {
        assertCodeFailsParse("struct X x = { .b = 40, .z = {} };");
    }

    public void testBug39551A() throws Exception
    {
        IASTFunction function = (IASTFunction)parse("extern float _Complex conjf (float _Complex);").getDeclarations().next();
        assertEquals( function.getName(), "conjf");
    }

    public void testBug39551B() throws Exception
    {
        IASTVariable variable = (IASTVariable)parse("_Imaginary double id = 99.99 * __I__;").getDeclarations().next();
        assertEquals( variable.getName(), "id");
    }
    
    public void testBug39554() throws Exception
    {
        assertCodeFailsParse("_Pragma(\"foobar\")");
    }
    public void testBug39556() throws Exception
    {
        IASTFunction function = (IASTFunction)parse("int *restrict ip_fn (void);").getDeclarations().next();
        assertFalse(
            "The expected error did not occur.",
            function.getReturnType().getPointerOperators().hasNext() );
    }
    
    //Here C99-specific section ends
    //Here GCC-specific section starts
    public void testBug39676() throws Exception
    {
        assertCodeFailsParse("struct { int e1, e2; } v = { e2: 0 }");
    }
    public void testBug39677() throws Exception
    {
        assertCodeFailsParse("B::B() : a(({ 1; })) {}");
    }
    public void testBug39678() throws Exception
    {
        assertCodeFailsParse("char *s = L\"a\" \"b\";");
    }
    public void testBug39679() throws Exception
    {
        assertCodeFailsParse("Foo blat() return f(4) {}");
    }
    public void testBug39681() throws Exception
    {
        Writer code = new StringWriter();
        try
        {
            code.write("double\n");
            code.write("foo (double a, double b)\n");
            code.write("{\n");
            code.write("  double square (double z) { return z * z; }\n");
            code.write("  return square (a) + square (b);\n");
            code.write("}\n");
        }
        catch (IOException ioe)
        {
        }
        parse(code.toString());
    }
    
    public void testBug39682() throws Exception
    {
        IASTTypedefDeclaration typedef = (IASTTypedefDeclaration)parse("typedef name = (a+1);").getDeclarations().next();
        assertFalse(
            "The expected error did not occur.",
            typedef.getName().equals( "name" ) );
    }
    public void testBug39684() throws Exception
    {
        assertCodeFailsParse("typeof(foo(1)) bar () { return foo(1); }");
    }
    public void testBug39686() throws Exception
    {
        Writer code = new StringWriter();
        try
        {
            code.write("__complex__ double x; // complex double\n");
            code.write("__complex__ short int a; // complex short int\n");
            code.write("x = 2.5fi; // 2.5 imaginary float literal\n");
            code.write("a = 3i; // imaginary integer literal\n");
            code.write("double v = __real__ x; // real part of expression\n");
            code.write(
                "double w = __imag__ x; // imaginary part of expression\n");
        }
        catch (IOException ioe)
        {
        }
        assertCodeFailsParse(code.toString());
    }
    public void testBug39687() throws Exception
    {
        assertCodeFailsParse("struct entry tester (int len; char data[len][len], int len) {}");
    }
    public void testBug39688() throws Exception
    {
        Writer code = new StringWriter();
        try
        {
            code.write("#define decl(type, vars...)  \\\n");
            code.write(" type vars ;\n");
            code.write("decl(int, x, y)\n");
        }
        catch (IOException ioe)
        {
        }
        Iterator declarations = parse(code.toString()).getDeclarations();
        assertFalse( "Should be 2 declarations, not 0", declarations.hasNext() );
    }
    public void testBug39694() throws Exception
    {
        IASTVariable variable = (IASTVariable)parse("int ab$cd = 1;").getDeclarations().next();
        assertFalse(
            "The expected error did not occur.",
            variable.equals("ab$cd"));
    }
    public void testBug39695() throws Exception
    {
        assertCodeFailsParse("int a = __alignof__ (int);");
    }
    public void testBug39695A() throws Exception
    {
        assertCodeFailsParse("int foo asm (\"myfoo\") = 2;");
    }
    public void testBug39695B() throws Exception
    {
        assertCodeFailsParse("extern func () asm (\"FUNC\");");
    }
    public void testBug39695C() throws Exception
    {
        assertCodeFailsParse("register int *foo asm (\"a5\");");
    }
    public void testBug39698A() throws Exception
    {
        Iterator declarations = parse("int c = a <? b;").getDeclarations();
        assertFalse( "Should be 1 declaration, not 0", declarations.hasNext() );
    }
    public void testBug39698B() throws Exception
    {
        assertCodeFailsParse("int c = a >? b;");
    }
    public void testBug39701A() throws Exception
    {
        assertCodeFailsParse("extern template int max (int, int);");
    }
    public void testBug39701B() throws Exception
    {
        assertCodeFailsParse("inline template class Foo<int>;");
    }
    public void testBug39701C() throws Exception
    {
        assertCodeFailsParse("static template class Foo<int>;");
    }
    public void testBug39702() throws Exception
    {
        Writer code = new StringWriter();
        try
        {
            code.write("signature T	{\n");
            code.write("  int f (int);\n");
            code.write("  int f0 () { return f (0); };\n");
            code.write("};\n");
        }
        catch (IOException ioe)
        {
        }
        Iterator declarations =  parse(code.toString()).getDeclarations();
        IASTDeclaration d = (IASTDeclaration)declarations.next(); 
		assertFalse( "Should be 1 declaration, not 2", !declarations.hasNext() );
    }
    public void testBug39703() throws Exception
    {
        Writer code = new StringWriter();
        try
        {
            code.write(
                "/* __extension__ enables GNU C mode for the duration of the declaration.  */\n");
            code.write("__extension__ struct G {\n");
            code.write("  struct { char z; };\n");
            code.write("  char g;\n");
            code.write("};\n");
        }
        catch (IOException ioe)
        {
        }
       	IASTAbstractTypeSpecifierDeclaration abs = (IASTAbstractTypeSpecifierDeclaration)assertSoleDeclaration(code.toString());
       	assertEquals( ((IASTClassSpecifier)abs.getTypeSpecifier()).getName(), "G" ); 
    }
    public void testBug39704A() throws Exception
    {
        assertCodeFailsParse("__declspec (dllimport) int foo;");
    }
    
    
    public void testBug39704B() throws Exception
    {
    	try
    	{
			IASTPointerToFunction p2f = (IASTPointerToFunction)assertSoleDeclaration("extern int (* import) (void) __attribute__((dllimport));");
			fail( "We should not reach this point");	
    	}
    	catch( ClassCastException cce )
    	{
			failedAsExpected();
    	}
    }
    public void testBug39704C() throws Exception
    {
    	try
    	{
			IASTFunction f = (IASTFunction)assertSoleDeclaration("int func2 (void) __attribute__((dllexport));");
			assertNotReached();
    	} catch( ClassCastException cce )
    	{
    	}
    }
    public void testBug39704D() throws Exception
    {
        assertCodeFailsParse("__declspec(dllexport) int func1 (int a) {}");
    }
    public void testBug39705() throws Exception
    {
        assertCodeFailsParse("#ident \"@(#)filename.c   1.3 90/02/12\"");
    }
    //Here GCC-specific section ends
    public void testBug40007() throws Exception
    {
        parse("int y = #;");
    }
    
	public void testBug40422() throws Exception {
		// Parse and get the translaton unit
		parse("int A::* x = 0;").getDeclarations().next();
	}
    
}
