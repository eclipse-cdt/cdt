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

import org.eclipse.cdt.core.parser.ast.IASTTypedefDeclaration;
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


    public void testBug39679() throws Exception
    {
        assertCodeFailsParse("Foo blat() return f(4) {}");
    }

    
    public void testBug39682() throws Exception
    {
        IASTTypedefDeclaration typedef = (IASTTypedefDeclaration)parse("typedef name = (a+1);").getDeclarations().next();
        assertFalse(
            "The expected error did not occur.",
            typedef.getName().equals( "name" ) );
    }
    
    public void testBug39687() throws Exception
    {
        assertCodeFailsParse("struct entry tester (int len; char data[len][len], int len) {}");
    }
//    public void testBug39688() throws Exception
//    {
//        Writer code = new StringWriter();
//        try
//        {
//            code.write("#define decl(type, vars...)  \\\n");
//            code.write(" type vars ;\n");
//            code.write("decl(int, x, y)\n");
//        }
//        catch (IOException ioe)
//        {
//        }
//        assertCodeFailsParse(code.toString());
//    }


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

    
	public void testBug40422() throws Exception {
		// Parse and get the translaton unit
		parse("int A::* x = 0;").getDeclarations().next();
	}
    
}
