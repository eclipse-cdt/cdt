/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTAbstractTypeSpecifierDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTVariable;

/**
 * @author jcamelon
 *
 */
public class GCCQuickParseExtensionsTest extends BaseASTTest {

	/**
	 * @param a
	 */
	public GCCQuickParseExtensionsTest(String a) {
		super(a);
		// TODO Auto-generated constructor stub
	}

    public void testBug39694() throws Exception
    {
        IASTVariable variable = (IASTVariable)parse("int ab$cd = 1;").getDeclarations().next(); //$NON-NLS-1$
        assertEquals( variable.getName(), "ab$cd"); //$NON-NLS-1$
    }
    
    public void testBug39704A() throws Exception
    {
        IASTVariable foo = (IASTVariable) assertSoleDeclaration("__declspec (dllimport) int foo;"); //$NON-NLS-1$
        assertEquals( foo.getName(), "foo"); //$NON-NLS-1$
    } 
    public void testBug39704D() throws Exception
    {
        IASTFunction func1 = (IASTFunction) assertSoleDeclaration("__declspec(dllexport) int func1 (int a) {}"); //$NON-NLS-1$
        assertEquals( func1.getName(), "func1"); //$NON-NLS-1$
    }
    
    public void testBug39695() throws Exception
    {
        parse("int a = __alignof__ (int);"); //$NON-NLS-1$
    }
    
    public void testBug39684() throws Exception
    {
        parse("typeof(foo(1)) bar () { return foo(1); }"); //$NON-NLS-1$
    }
    
    public void testBug39703() throws Exception
    {
        Writer code = new StringWriter();
        code.write("/* __extension__ enables GNU C mode for the duration of the declaration.  */\n"); //$NON-NLS-1$
        code.write("__extension__ struct G {\n"); //$NON-NLS-1$
        code.write("  struct { char z; };\n"); //$NON-NLS-1$
        code.write("  char g;\n"); //$NON-NLS-1$
        code.write("};\n"); //$NON-NLS-1$
       	IASTAbstractTypeSpecifierDeclaration abs = (IASTAbstractTypeSpecifierDeclaration)assertSoleDeclaration(code.toString());
       	IASTClassSpecifier G = ((IASTClassSpecifier)abs.getTypeSpecifier());
       	assertEquals( G.getName(), "G" ); //$NON-NLS-1$
       	assertEquals( G.getClassKind(), ASTClassKind.STRUCT );
       	Iterator i = G.getDeclarations();
       	assertEquals( ((IASTClassSpecifier)((IASTAbstractTypeSpecifierDeclaration)i.next()).getTypeSpecifier()).getName(), "" ); //$NON-NLS-1$
       	assertEquals( ((IASTField)i.next()).getName(), "g" ); //$NON-NLS-1$
       	assertFalse( i.hasNext() );
    }

    public void testBug39698A() throws Exception
    {
        parse("int c = a <? b;"); //$NON-NLS-1$
    }
    public void testBug39698B() throws Exception
    {
    	parse("int c = a >? b;"); //$NON-NLS-1$
    }

	public void testBug39554() throws Exception
	{
		 parse("_Pragma(\"foobar\")", true, true, ParserLanguage.C ); //$NON-NLS-1$
	}

    public void testBug39704B() throws Exception
    {
		IASTVariable d = (IASTVariable)assertSoleDeclaration("extern int (* import) (void) __attribute__((dllimport));"); //$NON-NLS-1$
		assertEquals( d.getName(), "import"); // false assertion  //$NON-NLS-1$
    }
    public void testBug39704C() throws Exception
    {
 		IASTFunction f = (IASTFunction)assertSoleDeclaration("int func2 (void) __attribute__((dllexport));"); //$NON-NLS-1$
		assertEquals( f.getName(), "func2"); //$NON-NLS-1$
    }
    
    public void testBug39686() throws Exception
    {
        Writer code = new StringWriter();
        code.write("__complex__ double x; // complex double\n"); //$NON-NLS-1$
        code.write("__complex__ short int a; // complex short int\n"); //$NON-NLS-1$
        code.write("__complex__ float y = 2.5fi; // 2.5 imaginary float literal\n"); //$NON-NLS-1$
        code.write("__complex__ int a = 3i; // imaginary intege r literal\n"); //$NON-NLS-1$
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
        parse("B::B() : a(({ 1; })) {}"); //$NON-NLS-1$
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
        writer = new StringWriter();
        writer.write( "typeof({ int y = foo (); int z;\n" ); //$NON-NLS-1$
        writer.write( "if (y > 0) z = y;\n" ); //$NON-NLS-1$
        writer.write( "else z = - y;\n" );//$NON-NLS-1$
        writer.write( "z; }) zoot;\n" );//$NON-NLS-1$
    }
    
    public void testBug39701A() throws Exception
    {
    	parse("extern template int max (int, int);"); //$NON-NLS-1$
    }
    public void testBug39701B() throws Exception
    {
    	parse("inline template class Foo<int>;"); //$NON-NLS-1$
    }
    public void testBug39701C() throws Exception
    {
        parse("static template class Foo<int>;"); //$NON-NLS-1$
    }
}
