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

import org.eclipse.cdt.core.parser.tests.BaseDOMTest;

import org.eclipse.cdt.internal.core.dom.ClassSpecifier;
import org.eclipse.cdt.internal.core.dom.Declarator;
import org.eclipse.cdt.internal.core.dom.ParameterDeclaration;
import org.eclipse.cdt.internal.core.dom.SimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.TemplateDeclaration;
import org.eclipse.cdt.internal.core.dom.TranslationUnit;

/**
 * @author jcamelon
 */
public class DOMFailedTest extends BaseDOMTest  {

	public DOMFailedTest(String name) {
		super(name);
	}

	public void testBug36730()throws Exception {
		failTest("FUNCTION_MACRO( 1, a )\n	int i;");
	}
		
	public void testBug39504A() throws Exception	{
		TranslationUnit tu = parse("int y = sizeof(x[0]);");
		 
		assertEquals(tu.getDeclarations().size(), 1);
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertFalse("The expected error did not occur.", declaration.getDeclarators().size() == 1 );
	}
	
	public void testBug39504B() throws Exception	{
		failTest("int y = sizeof (int*);"); 
	}
	
	public void testBug39505A() throws Exception	{
		failTest("int AD::* gp_down = static_cast<int AD::*>(gp_stat);"); 
	}

	public void testBug39505B() throws Exception	{
		failTest("int* gp_down = static_cast<int*>(gp_stat);"); 
	}
	
	public void testBug39523()	{
		Writer code = new StringWriter();
		try	{ 
			code.write("#define e0	\"a\"\n");
			code.write("#define e1	e0 e0 e0 e0 e0 e0 e0 e0 e0 e0\n");
			code.write("#define e2	e1 e1 e1 e1 e1 e1 e1 e1 e1 e1\n");
			code.write("#define e3	e2 e2 e2 e2 e2 e2 e2 e2 e2 e2\n");
			code.write("#define e4	e3 e3 e3 e3 e3 e3 e3 e3 e3 e3\n");
			code.write("#define e5	e4 e4 e4 e4 e4 e4 e4 e4 e4 e4\n");
			code.write("void foo() { (void)(e5); }\n");
		} catch( IOException ioe ){}
		
		boolean testPassed = false;
		try {
			parse(code.toString());
			testPassed = true;
			fail( "We should not reach this point");
		} catch (Throwable e) {
			if (!(e instanceof StackOverflowError))
				fail("Unexpected Error: " + e.getMessage());
		}
		if (testPassed)
			fail("The expected error did not occur.");
	}
	
	public void testBug39525() throws Exception	{
		failTest("C &(C::*DD)(const C &x) = &C::operator=;"); 
	}
	
	public void testBug39526() throws Exception	{
		failTest("UnitList unit_list (String(\"keV\"));"); 
	}
	
	public void testBug39528() throws Exception	{
		Writer code = new StringWriter();
		try	{ 
			code.write("struct B: public A {\n");
			code.write("  A a;\n");
			code.write("  B() try : A(1), a(2)\n");
			code.write("	{ throw 1; }\n");
			code.write("  catch (...)\n");
			code.write("	{ if (c != 3) r |= 1; }\n");
			code.write("};\n");
		} catch( IOException ioe ){}
		
		failTest(code.toString()); 
	}
	
	public void testBug39531() throws Exception	{
		failTest("class AString { operator char const *() const; };"); 
	}
	
	public void testBug39532() throws Exception	{
		failTest("class N1::N2::B : public A {};"); 
	}
	
	public void testBug39535() throws Exception	{
		failTest("namespace bar = foo;"); 
	}
	
	public void testBug39536A() throws Exception	{
		TranslationUnit tu = parse("template<class E> class X { X<E>(); };");
		 
		assertEquals(tu.getDeclarations().size(), 1);
		TemplateDeclaration tDeclaration = (TemplateDeclaration)tu.getDeclarations().get(0);
		assertEquals(tDeclaration.getDeclarations().size(), 1);
		SimpleDeclaration declaration = (SimpleDeclaration)tDeclaration.getDeclarations().get(0);
		ClassSpecifier cs = (ClassSpecifier)declaration.getTypeSpecifier();
		assertEquals(cs.getDeclarations().size(), 1);
		SimpleDeclaration declaration2 = (SimpleDeclaration)cs.getDeclarations().get(0);
		assertEquals(declaration2.getDeclarators().size(), 1);
		Declarator declarator = (Declarator)declaration2.getDeclarators().get(0);
		assertFalse("The expected error did not occur.", declarator.getName() != null);
	}
	
	public void testBug39536B() throws Exception	{
		failTest("template<class E> class X { inline X<E>(int); };"); 
	}
	
	public void testBug39537() throws Exception	{
		failTest("typedef foo<(U::id > 0)> foobar;"); 
	}

	public void testBug39538() throws Exception	{
		failTest("template C::operator int<float> ();"); 
	}

	public void testBug39540() throws Exception	{
		failTest("class {} const null;"); 
	}

	public void testBug39542() throws Exception	{
		failTest("void f(int a, struct {int b[a];} c) {}"); 
	}

	public void testBug39546() throws Exception	{
		failTest("signed char c = (signed char) 0xffffffff;"); 
	}


// Here starts C99-specific section

	public void testBug39549() throws Exception	{
		failTest("struct X x = { .b = 40, .z = {} };"); 
	}

	public void testBug39550() throws Exception	{
		failTest("double x = 0x1.fp1;"); 
	}

	public void testBug39551A() throws Exception	{
		TranslationUnit tu = parse("extern float _Complex conjf (float _Complex);");
 
		assertEquals(tu.getDeclarations().size(), 1);
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals(declaration.getDeclarators().size(), 1);
		Declarator declarator = (Declarator)declaration.getDeclarators().get(0);
		assertEquals(declarator.getParms().getDeclarations().size(), 1);
		ParameterDeclaration declaration2 = (ParameterDeclaration)declarator.getParms().getDeclarations().get(0);
		assertEquals(declaration2.getDeclarators().size(), 1);
		Declarator declarator2 = (Declarator)declaration2.getDeclarators().get(0);
		assertFalse("The expected error did not occur.",  declarator2.getName() == null ); 
	}

	public void testBug39551B() throws Exception	{
		TranslationUnit tu = parse("_Imaginary double id = 99.99 * __I__;");
 
		assertEquals(tu.getDeclarations().size(), 1);
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		String s = declaration.getDeclSpecifier().getTypeName();
		assertFalse("The expected error did not occur.", !declaration.getDeclSpecifier().getTypeName().equals("double") );  
	}

	public void testBug39552A() throws Exception	{
		Writer code = new StringWriter();
		try	{ 
			code.write("%:define glue(x, y) x %:%: y	/* #define glue(x, y) x ## y. */\n");
			code.write("#ifndef glue\n");
			code.write("#error glue not defined!\n");
			code.write("#endif\n");
			code.write("%:define str(x) %:x		/* #define str(x) #x */\n");

			code.write("int main (int argc, char *argv<::>) /* argv[] */\n");
			code.write("glue (<, %) /* { */\n");
			code.write("			 /* di_str[] = */\n");
			code.write("  const char di_str glue(<, :)glue(:, >) = str(%:%:<::><%%>%:);\n");

			code.write("  /* Check the glue macro actually pastes, and that the spelling of\n");
			code.write("	 all digraphs is preserved.  */\n");
			code.write("  if (glue(str, cmp) (di_str, \"%:%:<::><%%>%:\"))\n");
			code.write("	err (\"Digraph spelling not preserved!\");\n");

			code.write("  return 0;\n");
			code.write("glue (%, >) /* } */\n");
		} catch( IOException ioe ){}
		
		failTest(code.toString()); 
	}

	public void testBug39552B() throws Exception	{
		Writer code = new StringWriter();
		try	{ 
			code.write("??=include <stdio.h>\n");

			code.write("??=define TWELVE 1??/\n");
			code.write("2\n");

			code.write("static const char str??(??) = \"0123456789??/n\";\n");

			code.write("int\n");
			code.write("main(void)\n");
			code.write("??<\n");
			code.write("  unsigned char x = 5;\n");

			code.write("  if (sizeof str != TWELVE)\n");
			code.write("	abort ();\n");

			code.write("  /* Test ^=, the only multi-character token to come from trigraphs.  */\n");
			code.write("  x ??'= 3;\n");
			code.write("  if (x != 6)\n");
			code.write("	abort ();\n");

			code.write("  if ((5 ??! 3) != 7)\n");
			code.write("	abort ();\n");

			code.write("  return 0;\n");
			code.write("??>\n");
		} catch( IOException ioe ){}
		
		failTest(code.toString()); 
	}
	
	public void testBug39553() throws Exception	{
		TranslationUnit tu = parse(
				  "#define COMP_INC \"foobar.h\"  \n" +
				  "#include COMP_INC"); 
		assertFalse("The expected error did not occur.",  tu.getInclusions().size() == 1 );
	}

	public void testBug39554() throws Exception	{
		failTest("_Pragma(\"foobar\")"); 
	}

	public void testBug39556() throws Exception	{
		TranslationUnit tu = parse("int *restrict ip_fn (void);");
 
		assertEquals(tu.getDeclarations().size(), 1);
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals(declaration.getDeclarators().size(), 1);
		Declarator declarator = (Declarator)declaration.getDeclarators().get(0);
		assertFalse("The expected error did not occur.", declarator.getPointerOperators().size() == 1 ); 
	}

//	Here C99-specific section ends

//	Here GCC-specific section starts

	public void testBug39676() throws Exception	{
		failTest("struct { int e1, e2; } v = { e2: 0 }"); 
	}
	
	public void testBug39677() throws Exception	{
		failTest("B::B() : a(({ 1; })) {}"); 
	}

	public void testBug39678() throws Exception	{
		failTest("char *s = L\"a\" \"b\";"); 
	}
	
	public void testBug39679() throws Exception	{
		failTest("Foo blat() return f(4) {}"); 
	}

	public void testBug39681() throws Exception	{
		Writer code = new StringWriter();
		try	{ 
			code.write("double\n");
			code.write("foo (double a, double b)\n");
			code.write("{\n");
			code.write("  double square (double z) { return z * z; }\n");

			code.write("  return square (a) + square (b);\n");
			code.write("}\n");
		} catch( IOException ioe ){}
		
		TranslationUnit tu = parse(code.toString()); 
		// Internal structure for functions is not supported, so actual test
		// needs to be added later 
	}
	
	public void testBug39682() throws Exception	{
		TranslationUnit tu = parse("typedef name = (a+1);");
		 
		assertEquals(tu.getDeclarations().size(), 1);
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals(declaration.getDeclarators().size(), 1);
		Declarator declarator = (Declarator)declaration.getDeclarators().get(0);
		assertFalse("The expected error did not occur.", declarator.getName() != null ); 
	}

	public void testBug39684() throws Exception	{
		failTest("typeof(foo(1)) bar () { return foo(1); }"); 
	}

	public void testBug39686() throws Exception	{
		Writer code = new StringWriter();
		try	{ 
			code.write("__complex__ double x; // complex double\n");
			code.write("__complex__ short int a; // complex short int\n");
			code.write("x = 2.5fi; // 2.5 imaginary float literal\n");
			code.write("a = 3i; // imaginary integer literal\n");
			code.write("double v = __real__ x; // real part of expression\n");
			code.write("double w = __imag__ x; // imaginary part of expression\n");
		} catch( IOException ioe ){}
		
		failTest(code.toString()); 
	}
	
	public void testBug39687() throws Exception	{
		failTest("struct entry tester (int len; char data[len][len], int len) {}"); 
	}

	public void testBug39688() throws Exception	{
		Writer code = new StringWriter();
		try	{ 
			code.write("#define decl(type, vars...)  \\\n");
			code.write(" type vars ;\n");
 
			code.write("decl(int, x, y)\n");
		} catch( IOException ioe ){}

		TranslationUnit tu = parse(code.toString());	
		assertFalse("The expected error did not occur.", tu.getDeclarations().size() == 1 );
	}

	public void testBug39694() throws Exception	{
		TranslationUnit tu = parse("int ab$cd = 1;");

		assertEquals(tu.getDeclarations().size(), 1);
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals(declaration.getDeclarators().size(), 1);
		Declarator declarator = (Declarator)declaration.getDeclarators().get(0);
		assertFalse("The expected error did not occur.", declarator.getName().equals("ab$cd") ); 
	}

	public void testBug39695() throws Exception	{
		failTest("int a = __alignof__ (int);"); 
	}

	public void testBug39695A() throws Exception	{
		failTest("int foo asm (\"myfoo\") = 2;"); 
	}

	public void testBug39695B() throws Exception	{
		failTest("extern func () asm (\"FUNC\");"); 
	}

	public void testBug39695C() throws Exception	{
		failTest("register int *foo asm (\"a5\");"); 
	}

	public void testBug39698A() throws Exception	{
		TranslationUnit tu = parse("int c = a <? b;");
 
		assertFalse("The expected error did not occur.", tu.getDeclarations().size() == 1 );
	}

	public void testBug39698B() throws Exception	{
		failTest("int c = a >? b;");
 	}

	public void testBug39701A() throws Exception	{
		failTest("extern template int max (int, int);"); 
	}

	public void testBug39701B() throws Exception	{
		failTest("inline template class Foo<int>;"); 
	}

	public void testBug39701C() throws Exception	{
		failTest("static template class Foo<int>;"); 
	}

	public void testBug39702() throws Exception	{
		Writer code = new StringWriter();
		try	{ 
			code.write("signature T	{\n");
			code.write("  int f (int);\n");
			code.write("  int f0 () { return f (0); };\n");
			code.write("};\n");
		} catch( IOException ioe ){}
		
		TranslationUnit tu = parse(code.toString()); 
		assertFalse("The expected error did not occur.", tu.getDeclarations().size() == 1 );
	}

	public void testBug39703() throws Exception	{
		Writer code = new StringWriter();
		try	{ 
			code.write("/* __extension__ enables GNU C mode for the duration of the declaration.  */\n");
			code.write("__extension__ struct G {\n");
			code.write("  struct { char z; };\n");
			code.write("  char g;\n");
			code.write("};\n");
		} catch( IOException ioe ){}
		
		TranslationUnit tu = parse(code.toString());
		assertEquals(tu.getDeclarations().size(), 1);
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertFalse("The expected error did not occur.", declaration.getDeclSpecifier().getName() == null ); 
	}

	public void testBug39704A() throws Exception	{
		failTest("__declspec (dllimport) int foo;"); 
	}

	public void testBug39704B() throws Exception	{
		TranslationUnit tu = parse("extern int (* import) (void) __attribute__((dllimport));");
		
		assertEquals(tu.getDeclarations().size(), 1);
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals(declaration.getDeclarators().size(), 1);
		Declarator declarator = (Declarator)declaration.getDeclarators().get(0);
		assertFalse("The expected error did not occur.", !declarator.getName().toString().equals("__attribute__")); 
	}

	public void testBug39704C() throws Exception	{
		TranslationUnit tu = parse("int func2 (void) __attribute__((dllexport));");
		
		assertEquals(tu.getDeclarations().size(), 1);
		SimpleDeclaration declaration = (SimpleDeclaration)tu.getDeclarations().get(0);
		assertEquals(declaration.getDeclarators().size(), 1);
		Declarator declarator = (Declarator)declaration.getDeclarators().get(0);
		assertFalse("The expected error did not occur.", !declarator.getName().toString().equals("__attribute__")); 
	}

	public void testBug39704D() throws Exception	{
		failTest("__declspec(dllexport) int func1 (int a) {}"); 
	}

	public void testBug39705() throws Exception	{
		failTest("#ident \"@(#)filename.c   1.3 90/02/12\""); 
	}
		
//	Here GCC-specific section ends

	public void testBug40007() throws Exception	{
		parse("int y = #;"); 
	}
}
