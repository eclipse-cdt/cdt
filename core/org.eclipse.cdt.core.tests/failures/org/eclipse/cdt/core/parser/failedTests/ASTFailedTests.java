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

import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.core.parser.tests.BaseASTTest;
/**
 * @author jcamelon
 *
 */
public class ASTFailedTests extends BaseASTTest
{
 
    public ASTFailedTests(String name)
    {
        super(name);
    }
    public void testBug36730() throws Exception
    {
        assertCodeFailsParse("FUNCTION_MACRO( 1, a )\n	int i;");
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
        assertCodeFailsParse(code.toString());
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
        assertCodeFailsParse("int c = a <? b;");
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
        assertCodeFailsFullParse(code.toString());
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
			IASTVariable d = (IASTVariable)assertSoleDeclaration("extern int (* import) (void) __attribute__((dllimport));");
			assertEquals( d.getName(), "__attribute__"); // false assertion 
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
			assertEquals( f.getName(), "func2");
    	} catch( ClassCastException cce )
    	{
    	}
    }
    public void testBug39704D() throws Exception
    {
        assertCodeFailsParse("__declspec(dllexport) int func1 (int a) {}");
    }


    
	public void testBug40422() throws Exception {
		// Parse and get the translaton unit
		parse("int A::* x = 0;").getDeclarations().next();
	}
    
}
