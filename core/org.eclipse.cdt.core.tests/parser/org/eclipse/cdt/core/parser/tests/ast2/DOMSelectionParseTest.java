/*******************************************************************************
 * Copyright (c) 2002, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *     Nathan Ridge
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.dom.IName;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.ILabel;
import org.eclipse.cdt.core.dom.ast.IMacroBinding;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICExternalBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPFunction;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTNameBase;
import org.eclipse.cdt.internal.core.model.CProject;
import org.eclipse.core.resources.IFile;

/**
 * @author dsteffle
 */
public class DOMSelectionParseTest extends DOMSelectionParseTestBase {

	public DOMSelectionParseTest() {
	}

	public DOMSelectionParseTest(String name, Class className) {
		super(name, className);
	}

	public DOMSelectionParseTest(String name) {
		super(name, DOMSelectionParseTest.class);
	}

	public void testBaseCase_VariableReference() throws Exception {
		String code = "void f() { int x; x=3; }";
		int offset1 = code.indexOf("x=");
		int offset2 = code.indexOf('=');
		IASTNode node = parse(code, offset1, offset2);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IVariable);
		assertEquals(((IASTName) node).toString(), "x");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "x");
		assertEquals(((ASTNode) decls[0]).getOffset(), 15);
		assertEquals(((ASTNode) decls[0]).getLength(), 1);
	}

	public void testBaseCase_FunctionReference() throws Exception {
		String code = "int x(){x( );}";
		int offset1 = code.indexOf("x( ");
		int offset2 = code.indexOf("( )");
		IASTNode node = parse(code, offset1, offset2);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IFunction);
		assertEquals(((IASTName) node).toString(), "x");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "x");
		assertEquals(((ASTNode) decls[0]).getOffset(), 4);
		assertEquals(((ASTNode) decls[0]).getLength(), 1);
	}

	public void testBaseCase_Error() throws Exception {
		String code = "int x() { y( ) }";
		int offset1 = code.indexOf("y( ");
		int offset2 = code.indexOf("( )");
		assertNull(parse(code, offset1, offset2, false));
	}

	public void testBaseCase_FunctionDeclaration() throws Exception {
		String code = "int x(); void test() {x( );}";
		int offset1 = code.indexOf("x( )");
		int offset2 = code.indexOf("( )");
		IASTNode node = parse(code, offset1, offset2);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IFunction);
		assertEquals(((IASTName) node).toString(), "x");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "x");
		assertEquals(((ASTNode) decls[0]).getOffset(), 4);
		assertEquals(((ASTNode) decls[0]).getLength(), 1);
	}

	public void testBaseCase_FunctionDeclaration2() throws Exception {
		String code = "int printf( const char *, ... ); ";
		int offset1 = code.indexOf("printf");
		int offset2 = code.indexOf("( const");
		IASTNode node = parse(code, offset1, offset2);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IFunction);
		assertEquals(((IASTName) node).toString(), "printf");
	}

	public void testBaseCase_VariableDeclaration() throws Exception {
		String code = "int x = 3;";
		int offset1 = code.indexOf("x");
		int offset2 = code.indexOf(" =");
		IASTNode node = parse(code, offset1, offset2);
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IVariable);
		assertEquals(((IASTName) node).toString(), "x");
	}

	public void testBaseCase_Parameter() throws Exception {
		String code = "int main( int argc ) { int x = argc; }";
		int offset1 = code.indexOf("argc;");
		int offset2 = code.indexOf(";");
		IASTNode node = parse(code, offset1, offset2);
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IParameter);
		assertEquals(((IASTName) node).toString(), "argc");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "argc");
		assertEquals(((ASTNode) decls[0]).getOffset(), 14);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);
	}

	public void testBug57898() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class Gonzo {  public: void playHorn(); };\n");
		writer.write("void Gonzo::playHorn() { return; }\n");
		writer.write("int	main(int argc, char **argv) { Gonzo gonzo; gonzo.playHorn(); }\n");
		String code = writer.toString();
		for (int i = 0; i < 3; ++i) {
			int start = -1, stop = -1;
			switch (i) {
			case 0:
				start = code.indexOf("void playHorn") + 5;
				break;
			case 1:
				start = code.indexOf("::playHorn") + 2;
				break;
			case 2:
				start = code.indexOf(".playHorn") + 1;
				break;
			}
			stop = start + 8;
			IASTNode node = parse(code, start, stop);
			assertNotNull(node);
			assertTrue(node instanceof IASTName);
			assertTrue(((IASTName) node).resolveBinding() instanceof ICPPMethod);
			assertEquals(((IASTName) node).toString(), "playHorn");
			IName[] decls = getDeclarationOffTU((IASTName) node);
			assertEquals(decls.length, 2);
			assertEquals(decls[0].toString(), "playHorn");
			assertEquals(((ASTNode) decls[0]).getOffset(), 28);
			assertEquals(((ASTNode) decls[0]).getLength(), 8);
		}
	}

	public void testConstructorDestructorDeclaration() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class Gonzo { Gonzo(); ~Gonzo(); };");
		String code = writer.toString();
		int offset = code.indexOf(" Gonzo()") + 1;
		IASTNode node = parse(code, offset, offset + 5);
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPConstructor);
		assertEquals(((IASTName) node).toString(), "Gonzo");

		offset = code.indexOf("~Gonzo");
		node = parse(code, offset, offset + 6);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPMethod);
		assertEquals(((IASTName) node).toString(), "~Gonzo");
	}

	public void testBug60264() throws Exception {
		Writer writer = new StringWriter();
		writer.write("namespace Muppets { int i;	}\n");
		writer.write("int	main(int argc, char **argv) {	Muppets::i = 1; }\n");
		String code = writer.toString();
		int index = code.indexOf("Muppets::");
		IASTNode node = parse(code, index, index + 7);
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPNamespace);
		assertEquals(((IASTName) node).toString(), "Muppets");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "Muppets");
		assertEquals(((ASTNode) decls[0]).getOffset(), 10);
		assertEquals(((ASTNode) decls[0]).getLength(), 7);

		index = code.indexOf("e Muppets") + 2;
		node = parse(code, index, index + 7);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPNamespace);
		assertEquals(((IASTName) node).toString(), "Muppets");
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "Muppets");
		assertEquals(((ASTNode) decls[0]).getOffset(), 10);
		assertEquals(((ASTNode) decls[0]).getLength(), 7);
	}

	public void testBug61613() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class Foo {  // ** (A) **\n");
		writer.write("	public:\n");
		writer.write("Foo() {};\n");
		writer.write("};\n");
		writer.write("int \n");
		writer.write("main(int argc, char **argv) {\n");
		writer.write("Foo foo;  // ** (B) **\n");
		writer.write("}\n");
		String code = writer.toString();
		int index = code.indexOf("class Foo") + 6;
		IASTNode node = parse(code, index, index + 3);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPClassType);
		assertEquals(((IASTName) node).toString(), "Foo");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "Foo");
		assertEquals(((ASTNode) decls[0]).getOffset(), 6);
		assertEquals(((ASTNode) decls[0]).getLength(), 3);
	}

	public void testBug60038() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class Gonzo {\n");
		writer.write("public:\n");
		writer.write("Gonzo( const Gonzo & other ){}\n");
		writer.write("Gonzo()	{}\n");
		writer.write("~Gonzo(){}\n");
		writer.write("};\n");
		writer.write("int main(int argc, char **argv) {\n");
		writer.write(" Gonzo * g = new Gonzo();\n");
		writer.write(" Gonzo * g2 = new Gonzo( *g );\n");
		writer.write(" g->~Gonzo();\n");
		writer.write(" return (int) g2;\n");
		writer.write("}\n");
		String code = writer.toString();
		for (int i = 0; i < 3; ++i) {
			int startOffset = 0, endOffset = 0;
			switch (i) {
			case 0:
				startOffset = code.indexOf("new Gonzo()") + 4;
				endOffset = startOffset + 5;
				break;
			case 1:
				startOffset = code.indexOf("new Gonzo( ") + 4;
				endOffset = startOffset + 5;
				break;
			default:
				startOffset = code.indexOf("->~") + 2;
				endOffset = startOffset + 6;
			}
			IASTNode node = parse(code, startOffset, endOffset);
			assertTrue(node instanceof IASTName);
			IBinding binding = ((IASTName) node).resolveBinding();
			if (binding instanceof ICPPClassType) {
				node = TestUtil.findImplicitName(node);
				binding = ((IASTName) node).resolveBinding();
			}
			assertTrue(binding instanceof ICPPMethod);
			IName[] decls = null;
			switch (i) {
			case 0:
				assertTrue(binding instanceof ICPPConstructor);
				decls = getDeclarationOffTU((IASTName) node);
				assertEquals(decls.length, 1);
				assertEquals(decls[0].toString(), "Gonzo");
				assertEquals(((ASTNode) decls[0]).getOffset(), 53);
				assertEquals(((ASTNode) decls[0]).getLength(), 5);
				break;
			case 1:
				assertTrue(binding instanceof ICPPConstructor);
				decls = getDeclarationOffTU((IASTName) node);
				assertEquals(decls.length, 1);
				assertEquals(decls[0].toString(), "Gonzo");
				assertEquals(((ASTNode) decls[0]).getOffset(), 22);
				assertEquals(((ASTNode) decls[0]).getLength(), 5);
				break;
			default:
				assertFalse(binding instanceof ICPPConstructor);
				String name = ((IASTName) node).toString();
				assertEquals(name.indexOf("~"), 0);
				assertEquals(name.indexOf("Gonzo"), 1);
				decls = getDeclarationOffTU((IASTName) node);
				assertEquals(decls.length, 1);
				assertEquals(decls[0].toString(), "~Gonzo");
				assertEquals(((ASTNode) decls[0]).getOffset(), 64);
				assertEquals(((ASTNode) decls[0]).getLength(), 6);
				break;

			}
		}
	}

	public void testMethodReference() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class Sample { public:\n");
		writer.write("  int getAnswer() const;\n");
		writer.write("};\n");
		writer.write("int main(int argc, char **argv) {\n");
		writer.write(" Sample * s = new Sample();\n");
		writer.write(" return s->getAnswer();\n");
		writer.write("}\n");
		String code = writer.toString();
		int startIndex = code.indexOf("->getAnswer") + 2;
		IASTNode node = parse(code, startIndex, startIndex + 9);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPMethod);
		assertEquals(((IASTName) node).toString(), "getAnswer");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "getAnswer");
		assertEquals(((ASTNode) decls[0]).getOffset(), 29);
		assertEquals(((ASTNode) decls[0]).getLength(), 9);
	}

	public void testConstructorDefinition() throws Exception {
		String code = "class ABC { public: ABC(); }; ABC::ABC(){}";
		int startIndex = code.indexOf("::ABC") + 2;
		IASTNode node = parse(code, startIndex, startIndex + 3);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPConstructor);
		assertEquals(((IASTName) node).toString(), "ABC");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 2);
		assertEquals(decls[0].toString(), "ABC");
		assertEquals(((ASTNode) decls[0]).getOffset(), 20);
		assertEquals(((ASTNode) decls[0]).getLength(), 3);
	}

	public void testBug63966() throws Exception {
		Writer writer = new StringWriter();
		writer.write("void foo(int a) {}\n");
		writer.write("void foo(long a) {}\n");
		writer.write("int main(int argc, char **argv) {\n");
		writer.write("foo(1); \n }");
		String code = writer.toString();
		int startIndex = code.indexOf("foo(1)");
		parse(code, startIndex, startIndex + 3);
	}

	public void testBug66744() throws Exception {
		Writer writer = new StringWriter();
		writer.write("enum EColours { RED, GREEN, BLUE };      \n");
		writer.write("void foo() {  EColours color = GREEN; }  \n");

		String code = writer.toString();
		int startIndex = code.indexOf("EColours color");
		parse(code, startIndex, startIndex + 8);
	}

	public void testBug68527() throws Exception {
		Writer writer = new StringWriter();
		writer.write("struct X;\n");
		writer.write("struct X anA;");
		String code = writer.toString();
		int startIndex = code.indexOf("X anA");
		parse(code, startIndex, startIndex + 1);
	}

	public void testBug60407() throws Exception {
		Writer writer = new StringWriter();
		writer.write("struct ZZZ { int x, y, z; };\n");
		writer.write("typedef struct ZZZ _FILE;\n");
		writer.write("typedef _FILE FILE;\n");
		writer.write("static void static_function(FILE * lcd){}\n");
		writer.write("int	main(int argc, char **argv) {\n");
		writer.write("FILE * file = 0;\n");
		writer.write("static_function( file );\n");
		writer.write("return 0;\n");
		writer.write("}\n");
		String code = writer.toString();
		int startIndex = code.indexOf("static_function( file )");
		parse(code, startIndex, startIndex + "static_function".length());
	}

	public void testBug61800() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class B {};\n");
		writer.write("class ABCDEF {\n");
		writer.write(" static B stInt; };\n");
		writer.write("B ABCDEF::stInt = 5;\n");
		String code = writer.toString();
		int startIndex = code.indexOf("::stInt") + 2;

		IASTNode node = parse(code, startIndex, startIndex + 5);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPField);
		assertEquals(((IASTName) node).toString(), "stInt");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 2);
		assertEquals(decls[0].toString(), "stInt");
		assertEquals(((ASTNode) decls[0]).getOffset(), 37);
		assertEquals(((ASTNode) decls[0]).getLength(), 5);
	}

	public void testBug68739() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int fprintf( int *, const char *, ... );               \n");
		writer.write("void boo( int * lcd ) {                                \n");
		writer.write("  /**/fprintf( lcd, \"%c%s 0x%x\", ' ', \"bbb\", 2 );  \n");
		writer.write("}                                                      \n");

		String code = writer.toString();
		int startIndex = code.indexOf("/**/fprintf") + 4;

		IASTNode node = parse(code, startIndex, startIndex + 7);

		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IFunction);
		assertEquals(((IASTName) node).toString(), "fprintf");
	}

	public void testBug72818() throws Exception {
		Writer writer = new StringWriter();
		writer.write("union Squaw	{	int x;	double u; };\n");
		writer.write("int	main(int argc, char **argv) {\n");
		writer.write("return sizeof( Squaw );\n");
		writer.write("}\n");
		String code = writer.toString();
		int startIndex = code.indexOf("sizeof( ") + "sizeof( ".length(); //$NON-NLS-2$
		IASTNode node = parse(code, startIndex, startIndex + 5);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPClassType);
		assertEquals(((IASTName) node).toString(), "Squaw");
		assertEquals(((ICPPClassType) ((IASTName) node).resolveBinding()).getKey(), ICompositeType.k_union);
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "Squaw");
		assertEquals(((ASTNode) decls[0]).getOffset(), 6);
		assertEquals(((ASTNode) decls[0]).getLength(), 5);
	}

	public void test72220() throws Exception {
		Writer writer = new StringWriter();
		writer.write("const int FOUND_ME = 1;\n");
		writer.write("class Test{\n");
		writer.write("public:\n");
		writer.write("const int findCode() const;\n");
		writer.write("};\n");
		writer.write("const int Test::findCode() const {\n");
		writer.write("return FOUND_ME;\n");
		writer.write("}\n");
		String code = writer.toString();
		int startIndex = code.indexOf("return ") + "return ".length(); //$NON-NLS-2$
		IASTNode node = parse(code, startIndex, startIndex + 8);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IVariable);
		assertEquals(((IASTName) node).toString(), "FOUND_ME");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "FOUND_ME");
		assertEquals(((ASTNode) decls[0]).getOffset(), 10);
		assertEquals(((ASTNode) decls[0]).getLength(), 8);
	}

	public void testBug72721() throws Exception {
		Writer writer = new StringWriter();
		writer.write(" class ABC { public: ABC(int); };   \n");
		writer.write("void f() {                          \n");
		writer.write("   int j = 1;                       \n");
		writer.write("   new ABC( j + 1 );                \n");
		writer.write("}                                   \n");

		String code = writer.toString();
		int startIndex = code.indexOf("ABC(");
		IASTNode node = parse(code, startIndex, startIndex + 3);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPConstructor);
		assertEquals(((IASTName) node).toString(), "ABC");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "ABC");
		assertEquals(((ASTNode) decls[0]).getOffset(), 21);
		assertEquals(((ASTNode) decls[0]).getLength(), 3);
	}

	public void testBug72372() throws Exception {
		Writer writer = new StringWriter();
		writer.write("namespace B {                                   \n");
		writer.write("   class SD_02 { void f_SD(); };                \n");
		writer.write("}                                               \n");
		writer.write("using namespace B;                              \n");
		writer.write("void SD_02::f_SD(){}                            \n");

		String code = writer.toString();
		int startIndex = code.indexOf(":f_SD");
		IASTNode node = parse(code, startIndex + 1, startIndex + 5);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPMethod);
		assertEquals(((IASTName) node).toString(), "f_SD");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 2);
		assertEquals(decls[0].toString(), "f_SD");
		assertEquals(((ASTNode) decls[0]).getOffset(), 71);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);
	}

	public void testBug72372_2() throws Exception {
		Writer writer = new StringWriter();
		writer.write("namespace A {                                   \n");
		writer.write("   namespace B {                                \n");
		writer.write("      void f_SD();                              \n");
		writer.write("   }                                            \n");
		writer.write("}                                               \n");
		writer.write("namespace C {                                   \n");
		writer.write("   using namespace A;                           \n");
		writer.write("}                                               \n");
		writer.write("void C::B::f_SD(){}                             \n");

		String code = writer.toString();
		int startIndex = code.indexOf(":f_SD");
		IASTNode node = parse(code, startIndex + 1, startIndex + 5);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IFunction);
		assertEquals(((IASTName) node).toString(), "f_SD");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 2);
		assertEquals(decls[0].toString(), "f_SD");
		assertEquals(((ASTNode) decls[0]).getOffset(), 109);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);
	}

	public void testBug72713() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class Deck{ void initialize(); };   \n");
		writer.write("void Deck::initialize(){}           \n");

		String code = writer.toString();
		int startIndex = code.indexOf(":initialize");
		IASTNode node = parse(code, startIndex + 1, startIndex + 11);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPMethod);
		assertEquals(((IASTName) node).toString(), "initialize");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 2);
		assertEquals(decls[0].toString(), "initialize");
		assertEquals(((ASTNode) decls[0]).getOffset(), 17);
		assertEquals(((ASTNode) decls[0]).getLength(), 10);
	}

	public void testBug72712() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class B{ public: B(); }; void f(){ B* b; b = new B(); }");

		String code = writer.toString();
		int startIndex = code.indexOf("new B") + 4;

		IASTNode node = parse(code, startIndex, startIndex + 1);
		node = TestUtil.findImplicitName(node);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPConstructor);
		assertEquals(((IASTName) node).toString(), "B");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "B");
		assertEquals(17, ((ASTNode) decls[0]).getOffset());
		assertEquals(1, ((ASTNode) decls[0]).getLength());
	}

	public void testBug72712_2() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class A {};                                        \n");
		writer.write("class B{ public: B( A* ); };                       \n");
		writer.write("void f(){ B* b; b = new B( (A*)0 ); }              \n");

		String code = writer.toString();
		int startIndex = code.indexOf("(A*)") + 1;

		IASTNode node = parse(code, startIndex, startIndex + 1);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPClassType);
		assertEquals(((IASTName) node).toString(), "A");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "A");
		assertEquals(((ASTNode) decls[0]).getOffset(), 6);
		assertEquals(((ASTNode) decls[0]).getLength(), 1);
	}

	public void testBug72814() throws Exception {
		CPPASTNameBase.sAllowNameComputation = true;

		Writer writer = new StringWriter();
		writer.write("namespace N{                                \n");
		writer.write("   template < class T > class AAA { T _t; };\n");
		writer.write("}                                           \n");
		writer.write("N::AAA<int> a;                              \n");

		String code = writer.toString();
		int startIndex = code.indexOf("AAA<int>");
		IASTNode node = parse(code, startIndex, startIndex + 3);

		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPClassType);
		assertEquals(((IASTName) node).toString(), "AAA");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "AAA");
		assertEquals(((ASTNode) decls[0]).getOffset(), 75);
		assertEquals(((ASTNode) decls[0]).getLength(), 3);

		node = parse(code, startIndex, startIndex + 8);

		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPClassType);
		assertEquals(((IASTName) node).toString(), "AAA<int>");
		decls = getDeclarationOffTU((IASTName) node);
		// TODO raised bug 92632 for below
		//		assertEquals(decls.length, 1);
		//		assertEquals(decls[0].toString(), "AAA");
		//		assertEquals(((ASTNode) decls[0]).getOffset(), 15);
		//		assertEquals(((ASTNode) decls[0]).getLength(), 3);
	}

	public void testBug72710() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class Card{\n");
		writer.write("	Card( int rank );\n");
		writer.write(" int rank;\n");
		writer.write("};\n");
		writer.write("Card::Card( int rank ) {\n");
		writer.write("this->rank = rank;\n");
		writer.write("}\n");
		String code = writer.toString();
		int index = code.indexOf("this->rank") + 6;
		IASTNode node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPField);
		assertEquals(((IASTName) node).toString(), "rank");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "rank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 36);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);
	}

	public void testBug75731() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int rank() {\n");
		writer.write("return 5;\n}\n");
		writer.write("class Card{\n");
		writer.write("private:\n");
		writer.write("Card( int rank );\n");
		writer.write("int rank;\n");
		writer.write("public:\n");
		writer.write("int getRank();\n};\n");
		writer.write("Card::Card( int rank )\n{\n");
		writer.write("this->rank = ::rank();\n");
		writer.write("this->rank = this->rank;\n");
		writer.write("this->rank = rank;\n");
		writer.write("this->rank = Card::rank;\n");
		writer.write("this->rank = getRank();\n}\n");

		String code = writer.toString();
		int index = code.indexOf("int rank() {") + 4;
		IASTNode node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IFunction);
		assertEquals(((IASTName) node).toString(), "rank");
		assertEquals(((ASTNode) node).getOffset(), index);
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "rank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 4);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("class Card{") + 6;
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPClassType);
		assertEquals(((IASTName) node).toString(), "Card");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "Card");
		assertEquals(((ASTNode) decls[0]).getOffset(), 31);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("Card( int rank );");
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPConstructor);
		assertEquals(((IASTName) node).toString(), "Card");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 2);
		assertEquals(decls[0].toString(), "Card");
		assertEquals(((ASTNode) decls[0]).getOffset(), 46);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("Card( int rank );") + 10;
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IParameter);
		assertEquals(((IASTName) node).toString(), "rank");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 2);
		assertEquals(decls[0].toString(), "rank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 56);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("int rank;") + 4;
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPField);
		assertEquals(((IASTName) node).toString(), "rank");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "rank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 68);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("int getRank();") + 4;
		node = parse(code, index, index + 7);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPMethod);
		assertEquals(((IASTName) node).toString(), "getRank");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "getRank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 86);
		assertEquals(((ASTNode) decls[0]).getLength(), 7);

		index = code.indexOf("Card::Card( int rank )");
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPClassType);
		assertEquals(((IASTName) node).toString(), "Card");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "Card");
		assertEquals(((ASTNode) decls[0]).getOffset(), 31);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("Card::Card( int rank )") + 6;
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPConstructor);
		assertEquals(((IASTName) node).toString(), "Card");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 2);
		assertEquals(decls[0].toString(), "Card");
		assertEquals(((ASTNode) decls[0]).getOffset(), 46);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("Card::Card( int rank )") + 16;
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IParameter);
		assertEquals(((IASTName) node).toString(), "rank");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 2);
		assertEquals(decls[0].toString(), "rank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 56);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("this->rank = ::rank();") + 6;
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPField);
		assertEquals(((IASTName) node).toString(), "rank");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "rank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 68);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("this->rank = ::rank();") + 15;
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IFunction);
		assertEquals(((IASTName) node).toString(), "rank");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "rank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 4);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("this->rank = this->rank;") + 6;
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPField);
		assertEquals(((IASTName) node).toString(), "rank");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "rank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 68);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("this->rank = this->rank;") + 19;
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPField);
		assertEquals(((IASTName) node).toString(), "rank");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "rank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 68);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("this->rank = rank;") + 6;
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPField);
		assertEquals(((IASTName) node).toString(), "rank");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "rank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 68);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("this->rank = rank;") + 13;
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IParameter);
		assertEquals(((IASTName) node).toString(), "rank");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 2);
		assertEquals(decls[0].toString(), "rank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 56);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("this->rank = Card::rank;") + 6;
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPField);
		assertEquals(((IASTName) node).toString(), "rank");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "rank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 68);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("this->rank = Card::rank;") + 19;
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPField);
		assertEquals(((IASTName) node).toString(), "rank");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "rank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 68);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("this->rank = getRank();") + 6;
		node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPField);
		assertEquals(((IASTName) node).toString(), "rank");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "rank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 68);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);

		index = code.indexOf("this->rank = getRank();") + 13;
		node = parse(code, index, index + 7);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPMethod);
		assertEquals(((IASTName) node).toString(), "getRank");
		assertEquals(((ASTNode) node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "getRank");
		assertEquals(((ASTNode) decls[0]).getOffset(), 86);
		assertEquals(((ASTNode) decls[0]).getLength(), 7);
	}

	public void testBug77989() throws Exception {
		Writer writer = new StringWriter();
		writer.write("namespace N {        /* A */\n");
		writer.write("class C{};\n}\n");
		writer.write("using namespace N;   /* B */\n");
		writer.write("N::C c;              /* C */\n");

		String code = writer.toString();
		int index = code.indexOf("using namespace N;") + 16;
		IASTNode node = parse(code, index, index + 1);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPNamespace);
		assertEquals(((IASTName) node).toString(), "N");
		assertEquals(((ASTNode) node).getOffset(), index);
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "N");
		assertEquals(((ASTNode) decls[0]).getOffset(), 10);
		assertEquals(((ASTNode) decls[0]).getLength(), 1);
	}

	public void testBug78435() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int itself;          //A\n");
		writer.write("void f(int itself){} //B\n");

		String code = writer.toString();
		int index = code.indexOf("void f(int itself){}") + 11;
		IASTNode node = parse(code, index, index + 6);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IParameter);
		assertEquals(((IASTName) node).toString(), "itself");
		assertEquals(((ASTNode) node).getOffset(), index);
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "itself");
		assertEquals(((ASTNode) decls[0]).getOffset(), 36);
		assertEquals(((ASTNode) decls[0]).getLength(), 6);
	}

	public void testBug78231A() throws Exception {
		Writer writer = new StringWriter();
		writer.write("struct Base {\n");
		writer.write("int Data; // 1\n");
		writer.write("struct Data; // 2\n};\n");

		String code = writer.toString();
		int index = code.indexOf("struct Data;") + 7;
		IASTNode node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICompositeType);
		assertEquals(((IASTName) node).toString(), "Data");
		assertEquals(((ASTNode) node).getOffset(), index);
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "Data");
		assertEquals(((ASTNode) decls[0]).getOffset(), 36);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);
	}

	public void testBug78231B() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int Data;\n");
		writer.write("struct Base {\n");
		writer.write("int Data; // 1\n");
		writer.write("struct Data; // 2\n};\n");

		String code = writer.toString();
		int index = code.indexOf("struct Data;") + 7;
		IASTNode node = parse(code, index, index + 4);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICompositeType);
		assertEquals(((IASTName) node).toString(), "Data");
		assertEquals(((ASTNode) node).getOffset(), index);
	}

	public void testBug64326() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class foo {\n");
		writer.write("public:\n");
		writer.write("foo() {}\n");
		writer.write("int bar;\n");
		writer.write("};\n");
		writer.write("int\n");
		writer.write("main(int argc, char **argv) {\n");
		writer.write("foo* f;\n");
		writer.write("f->bar = 1; // ** (A) **\n");
		writer.write("}\n");

		String code = writer.toString();
		int index = code.indexOf("f->bar") + 3;
		IASTNode node = parse(code, index, index + 3);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPField);
		assertEquals(((IASTName) node).toString(), "bar");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "bar");
		assertEquals(((ASTNode) decls[0]).getOffset(), 33);
		assertEquals(((ASTNode) decls[0]).getLength(), 3);
	}

	public void testBug92605() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define UINT32 unsigned int\n");
		writer.write("#define HANDLE unsigned int**\n");
		writer.write("// ...\n");
		writer.write("void foo()\n");
		writer.write("{\n");
		writer.write("UINT32 u;  // line A\n");
		writer.write("HANDLE h;  // line B\n");
		writer.write("}\n");
		String code = writer.toString();

		int index = code.indexOf("UINT32 u;");
		IASTNode node = parse(code, index, index + 6, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IMacroBinding);
		assertEquals(((IASTName) node).toString(), "UINT32");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "UINT32");
		assertEquals(((ASTNode) decls[0]).getOffset(), 8);
		assertEquals(((ASTNode) decls[0]).getLength(), 6);
	}

	public void testBug79877() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int Func2() {\n");
		writer.write("int i;\n");
		writer.write("i = Func1();\n");
		writer.write("return(0);\n");
		writer.write("}\n");
		String code = writer.toString();
		IFile test1 = importFile("test1.c", code);

		writer.write("int Func1(void) {\n");
		writer.write("return(10);\n");
		writer.write("}\n");
		importFile("test2.c", writer.toString());

		int index = code.indexOf("Func1");
		IASTNode node = parse(test1, index, index + 5, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICExternalBinding);
		assertEquals(((IASTName) node).toString(), "Func1");

		ICElement[] scope = new ICElement[1];
		scope[0] = new CProject(null, project);

		//		// TODO need to register to an index and wait for it to finish before this test will work
		//
		//		Set matches = SearchEngine.getMatchesFromSearchEngine(SearchEngine.createCSearchScope(scope), (IASTName) node, CSearchPattern.DECLARATIONS);
		//		assertEquals(matches.size(), 1);
	}

	public void testBug78114() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class Point{			//line C\n");
		writer.write("public:\n");
		writer.write("Point(): xCoord(0){}\n");
		writer.write("Point(int x){}		//line B	\n");
		writer.write("private:	\n");
		writer.write("int xCoord;	\n");
		writer.write("};\n");
		writer.write("int main(int argc, char **argv) {\n");
		writer.write("Point &p2 = *(new Point(10));	// line A\n");
		writer.write("return (0);\n");
		writer.write("}\n");

		String code = writer.toString();

		int index = code.indexOf("Point(10)");
		IASTNode node = parse(code, index, index + 5, true);
		node = TestUtil.findImplicitName(node);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPConstructor);
		assertEquals(((IASTName) node).toString(), "Point");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "Point");
		assertEquals(((ASTNode) decls[0]).getOffset(), 53);
		assertEquals(((ASTNode) decls[0]).getLength(), 5);
	}

	public void testBug73398() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int joo=4;\n");
		writer.write("#define koo 4\n");
		writer.write("int main(int argc, char **argv) {\n");
		writer.write("return (koo);\n");
		writer.write("}\n");

		String code = writer.toString();

		int index = code.indexOf("koo);");
		IASTNode node = parse(code, index, index + 3, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IMacroBinding);
		assertEquals(((IASTName) node).toString(), "koo");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "koo");
		assertEquals(((ASTNode) decls[0]).getOffset(), 19);
		assertEquals(((ASTNode) decls[0]).getLength(), 3);
	}

	public void testBug() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class Point{	\n");
		writer.write("public:\n");
		writer.write("Point(): xCoord(0){}\n");
		writer.write(" Point& operator=(const Point &rhs){return *this;}	// line A\n");
		writer.write("private:		\n");
		writer.write("int xCoord;		\n");
		writer.write("};\n");
		writer.write("static const Point zero;\n");
		writer.write("int main(int argc, char **argv) {	\n");
		writer.write(" Point *p2 = new Point();\n");
		writer.write("p2->operator=(zero);           // line B\n");
		writer.write("return (0);	\n");
		writer.write(" }\n");

		String code = writer.toString();

		int index = code.indexOf("operator=(zero)");
		IASTNode node = parse(code, index, index + 9, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPMethod);
		assertEquals(((IASTName) node).toString(), "operator =");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "operator =");
		assertEquals(((ASTNode) decls[0]).getOffset(), 51);
		assertEquals(((ASTNode) decls[0]).getLength(), 9);
	}

	public void testBug80826() throws Exception {
		Writer writer = new StringWriter();
		writer.write("void swapImpl(int& a, int& b) {/*...*/} // line C\n");
		writer.write("#define swap(a,b) (swapImpl(a,b))   // line B\n");
		writer.write("//		...\n");
		writer.write("void foo(int x, int y)\n");
		writer.write("{\n");
		writer.write("swap(x,y); // line A\n");
		writer.write("		  //...\n");
		writer.write("}\n");

		String code = writer.toString();

		int index = code.indexOf("swap(x,y);");
		IASTNode node = parse(code, index, index + 4, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IMacroBinding);
		assertEquals(((IASTName) node).toString(), "swap");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "swap");
		assertEquals(((ASTNode) decls[0]).getOffset(), 58);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);
	}

	public void testBug78389() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class A{\n");
		writer.write("void method1(){}  //line A\n");
		writer.write("void method1(int i){} //line B\n");
		writer.write("};\n");
		writer.write("void f(){\n");
		writer.write("A a; \n");
		writer.write("a.method1(3); // F3 on method1 in this line should highlight method1 on line B\n");
		writer.write("}\n");

		String code = writer.toString();

		int index = code.indexOf("method1(3)");
		IASTNode node = parse(code, index, index + 7, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPMethod);
		assertEquals(((IASTName) node).toString(), "method1");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "method1");
		assertEquals(((ASTNode) decls[0]).getOffset(), 41);
		assertEquals(((ASTNode) decls[0]).getLength(), 7);
	}

	public void testBug78625() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class A{            \n");
		writer.write("public: A(int i){} \n");
		writer.write("};\n");
		writer.write("class B: A{\n");
		writer.write("B():A(2) {}    //line 5\n");
		writer.write("};\n");

		String code = writer.toString();

		int index = code.indexOf("A(2)");
		IASTNode node = parse(code, index, index + 1, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPConstructor);
		assertEquals(((IASTName) node).toString(), "A");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "A");
		assertEquals(((ASTNode) decls[0]).getOffset(), 29);
		assertEquals(((ASTNode) decls[0]).getLength(), 1);
	}

	public void testBug78656() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class A{\n");
		writer.write("public: int method1(){} //line 2\n");
		writer.write("};\n");
		writer.write("void f() {\n");
		writer.write("A a;\n");
		writer.write("int i=a.method1();  //line 6\n");
		writer.write("}\n");

		String code = writer.toString();

		int index = code.indexOf("method1();");
		IASTNode node = parse(code, index, index + 7, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPMethod);
		assertEquals(((IASTName) node).toString(), "method1");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "method1");
		assertEquals(((ASTNode) decls[0]).getOffset(), 21);
		assertEquals(((ASTNode) decls[0]).getLength(), 7);
	}

	public void testBug79965() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int i = 2, half_i = i / 2;\n");

		String code = writer.toString();

		int index = code.indexOf("i / 2");
		IASTNode node = parse(code, index, index + 1, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IVariable);
		assertEquals(((IASTName) node).toString(), "i");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "i");
		assertEquals(((ASTNode) decls[0]).getOffset(), 4);
		assertEquals(((ASTNode) decls[0]).getLength(), 1);
	}

	public void testBug64326A() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class foo {\n");
		writer.write("public:\n");
		writer.write("foo() {}\n");
		writer.write("void bar() {}\n");
		writer.write("};\n");
		writer.write("int\n");
		writer.write("main(int argc, char **argv) {\n");
		writer.write("foo* f;\n");
		writer.write("f->bar(); // ** (A) **\n");
		writer.write("}\n");

		String code = writer.toString();

		int index = code.indexOf("bar();");
		IASTNode node = parse(code, index, index + 3, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPMethod);
		assertEquals(((IASTName) node).toString(), "bar");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "bar");
		assertEquals(((ASTNode) decls[0]).getOffset(), 34);
		assertEquals(((ASTNode) decls[0]).getLength(), 3);
	}

	public void testBug64326B() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class foo {\n");
		writer.write("public:\n");
		writer.write("foo() {}\n");
		writer.write("int bar;\n");
		writer.write("};\n");
		writer.write("int \n");
		writer.write("main(int argc, char **argv) {\n");
		writer.write("foo* f;\n");
		writer.write("f->bar = 1; // ** (A) **\n");
		writer.write("}\n");

		String code = writer.toString();

		int index = code.indexOf("bar = ");
		IASTNode node = parse(code, index, index + 3, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPField);
		assertEquals(((IASTName) node).toString(), "bar");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "bar");
		assertEquals(((ASTNode) decls[0]).getOffset(), 33);
		assertEquals(((ASTNode) decls[0]).getLength(), 3);
	}

	public void testBug43128A() throws Exception {
		Writer writer = new StringWriter();
		writer.write("void foo()\n");
		writer.write("{\n");
		writer.write("int x=3;\n");
		writer.write("		  // ...\n");
		writer.write("x++;\n");
		writer.write("}\n");

		String code = writer.toString();

		int index = code.indexOf("x++");
		IASTNode node = parse(code, index, index + 1, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IVariable);
		assertEquals(((IASTName) node).toString(), "x");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "x");
		assertEquals(((ASTNode) decls[0]).getOffset(), 17);
		assertEquals(((ASTNode) decls[0]).getLength(), 1);
	}

	public void testBug43128B() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int\n");
		writer.write("main(int argc, char **argv) {\n");
		writer.write("int x = argc;\n");
		writer.write("}\n");

		String code = writer.toString();

		int index = code.indexOf("argc;");
		IASTNode node = parse(code, index, index + 4, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IParameter);
		assertEquals(((IASTName) node).toString(), "argc");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "argc");
		assertEquals(((ASTNode) decls[0]).getOffset(), 13);
		assertEquals(((ASTNode) decls[0]).getLength(), 4);
	}

	public void testBug43128C() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int j;\n");
		writer.write("int foo(int x) \n");
		writer.write("{	\n");
		writer.write("int y;\n");
		writer.write("x = 4;\n");
		writer.write("j = 5;\n");
		writer.write("y = 6;\n");
		writer.write("}\n");

		String code = writer.toString();

		int index = code.indexOf("x =");
		IASTNode node = parse(code, index, index + 1, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IVariable);
		assertEquals(((IASTName) node).toString(), "x");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "x");
		assertEquals(((ASTNode) decls[0]).getOffset(), 19);
		assertEquals(((ASTNode) decls[0]).getLength(), 1);

		index = code.indexOf("j =");
		node = parse(code, index, index + 1, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IVariable);
		assertEquals(((IASTName) node).toString(), "j");

		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "j");
		assertEquals(((ASTNode) decls[0]).getOffset(), 4);
		assertEquals(((ASTNode) decls[0]).getLength(), 1);

		index = code.indexOf("y =");
		node = parse(code, index, index + 1, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IVariable);
		assertEquals(((IASTName) node).toString(), "y");

		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "y");
		assertEquals(((ASTNode) decls[0]).getOffset(), 30);
		assertEquals(((ASTNode) decls[0]).getLength(), 1);
	}

	public void testBug86504() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class C { };\n");
		writer.write("void f(int(C)) { } // void f(int (*fp)(C c)) { }\n");
		writer.write("// not: void f(int C);\n");
		writer.write("int g(C);\n");
		writer.write("void foo() {\n");
		writer.write("f(g); // openDeclarations on g causes StackOverflowError\n");
		writer.write("}\n");

		String code = writer.toString();

		int index = code.indexOf("g); ");
		IASTNode node = parse(code, index, index + 1, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPFunction);
		assertEquals(((IASTName) node).toString(), "g");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "g");
		assertEquals(((ASTNode) decls[0]).getOffset(), 89);
		assertEquals(((ASTNode) decls[0]).getLength(), 1);
	}

	public void testBug79811() throws Exception {
		Writer writer = new StringWriter();
		writer.write("enum E{E0};\n");
		writer.write("void f() {\n");
		writer.write("enum E{E1};\n");
		writer.write("E e;   //this one is incorrectly found\n");
		writer.write("}\n");
		writer.write("E f;   //ok\n");

		String code = writer.toString();

		int index = code.indexOf("E{E0}");
		IASTNode node = parse(code, index, index + 1, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IEnumeration);
		assertEquals(((IASTName) node).toString(), "E");

		IName[] decls = getReferencesOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "E");
		assertEquals(((ASTNode) decls[0]).getOffset(), 76);
		assertEquals(((ASTNode) decls[0]).getLength(), 1);
	}

	public void testBugLabelWithMacro() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define UINT32 unsigned int\n");
		writer.write("#define HANDLE unsigned int**\n");
		writer.write("void foo()\n");
		writer.write("{\n");
		writer.write("UINT32 u;\n");
		writer.write("HANDLE h;\n");
		writer.write("}\n");
		writer.write("int foo2() {\n");
		writer.write("test:\n");
		writer.write("goto test;\n");
		writer.write("return foo();\n");
		writer.write("}\n");

		String code = writer.toString();

		int index = code.indexOf("HANDLE h");
		IASTNode node = parse(code, index, index + 6, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IMacroBinding);
		assertEquals(((IASTName) node).toString(), "HANDLE");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "HANDLE");
		assertEquals(((ASTNode) decls[0]).getOffset(), 36);
		assertEquals(((ASTNode) decls[0]).getLength(), 6);

		index = code.indexOf("test;");
		node = parse(code, index, index + 4, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ILabel);
		assertEquals(((IASTName) node).toString(), "test");

		decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "test");
		assertLocation(code, "test:", 4, decls[0]);
	}

	public void testBugMethodDef() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class tetrahedron {\n");
		writer.write("private:\n");
		writer.write("int color;\n");
		writer.write("public:\n");
		writer.write("/* Methods */\n");
		writer.write("void setColor(int c) \n");
		writer.write("{color = c < 0 ? 0 : c;};\n");
		writer.write("void set();\n");
		writer.write("};\n");
		writer.write("void tetrahedron::set() {\n");
		writer.write("int color;\n");
		writer.write("setColor(color);\n");
		writer.write("}\n");

		String code = writer.toString();

		int index = code.indexOf("setColor(color)");
		IASTNode node = parse(code, index, index + 8, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPMethod);
		assertEquals(((IASTName) node).toString(), "setColor");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "setColor");
		assertEquals(((ASTNode) decls[0]).getOffset(), 67);
		assertEquals(((ASTNode) decls[0]).getLength(), 8);

		IName[] refs = getReferencesOffTU((IASTName) node);
		assertEquals(refs.length, 1);
		assertEquals(refs[0].toString(), "setColor");
		assertEquals(((ASTNode) refs[0]).getOffset(), 162);
		assertEquals(((ASTNode) refs[0]).getLength(), 8);
	}

	public void testBug86698A() throws Exception {
		Writer writer = new StringWriter();
		writer.write("struct C;\n");
		writer.write("void no_opt(C*);\n");
		writer.write("struct C {\n");
		writer.write("int c;\n");
		writer.write("C() : c(0) { no_opt(this); }\n");
		writer.write("};\n");

		String code = writer.toString();

		int index = code.indexOf("c(0)");
		IASTNode node = parse(code, index, index + 1, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IVariable);
		assertEquals(((IASTName) node).toString(), "c");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "c");
		assertEquals(((ASTNode) decls[0]).getOffset(), 42);
		assertEquals(((ASTNode) decls[0]).getLength(), 1);
	}

	public void testBug86698B() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int f(int);\n");
		writer.write("class C {\n");
		writer.write("int i;\n");
		writer.write("double d;\n");
		writer.write("public:\n");
		writer.write("C(int, double);\n");
		writer.write("};\n");
		writer.write("C::C(int ii, double id)\n");
		writer.write("try\n");
		writer.write(": i(f(ii)), d(id)\n");
		writer.write("{\n");
		writer.write("//		 constructor function body\n");
		writer.write("}\n");
		writer.write("catch (...)\n");
		writer.write("{\n");
		writer.write("//		 handles exceptions thrown from the ctorinitializer\n");
		writer.write("//		 and from the constructor function body\n");
		writer.write("}\n");

		String code = writer.toString();

		int index = code.indexOf("i(f(ii)), d(id)");
		IASTNode node = parse(code, index, index + 1, true);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPField);
		assertEquals(((IASTName) node).toString(), "i");

		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "i");
		assertEquals(code.indexOf("int i") + 4, ((ASTNode) decls[0]).getOffset());
		assertEquals(((ASTNode) decls[0]).getLength(), 1);
	}

	public void testBug64181() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("namespace Foo { // ** (A) **\n");
		buffer.append("int bar;\n");
		buffer.append("}\n");
		buffer.append("namespace Foo { // ** (B) **\n");
		buffer.append("long fooboo;\n");
		buffer.append("}\n");
		buffer.append("int \n");
		buffer.append("main(int argc, char **argv) {\n");
		buffer.append("Foo::bar; // ** (C) **\n");
		buffer.append("}\n");

		String code = buffer.toString();
		int index = code.indexOf("Foo::bar;");
		IASTNode node = parse(code, index, index + 3, true);
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPNamespace);
		assertEquals(((IASTName) node).toString(), "Foo");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 2);
		assertEquals(decls[0].toString(), "Foo");
		assertEquals(((ASTNode) decls[0]).getOffset(), 10);
		assertEquals(((ASTNode) decls[0]).getLength(), 3);
		assertEquals(((ASTNode) decls[1]).getOffset(), 50);
		assertEquals(((ASTNode) decls[1]).getLength(), 3);
	}

	public void testBug80823() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("class MyEggImpl {}; // line A\n");
		buffer.append("#define MyChicken MyEggImpl\n");
		buffer.append("MyChicken c; // line C\n");

		String code = buffer.toString();
		int index = code.indexOf("MyChicken c;");
		IASTNode node = parse(code, index, index + 9, true);
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof IMacroBinding);
		assertEquals(((IASTName) node).toString(), "MyChicken");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "MyChicken");
		assertEquals(((ASTNode) decls[0]).getOffset(), 38);
		assertEquals(((ASTNode) decls[0]).getLength(), 9);
	}

	public void testBug86993() throws Exception {
		StringBuilder buffer = new StringBuilder();
		buffer.append("#define _BEGIN_STD_C extern \"C\" {\n");
		buffer.append("#define _END_STD_C  }\n");
		buffer.append("_BEGIN_STD_C\n");
		buffer.append("char c; // selection on this fails because offset for \n");
		buffer.append("_END_STD_C\n");
		buffer.append("char foo() {\n");
		buffer.append("return c; // ref   \n");
		buffer.append("}\n");

		String code = buffer.toString();
		int index = code.indexOf("return c;");
		IASTNode node = parse(code, index + 7, index + 8, true);
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPVariable);
		assertEquals(((IASTName) node).toString(), "c");
		IName[] decls = getDeclarationOffTU((IASTName) node);
		assertEquals(decls.length, 1);
		assertEquals(decls[0].toString(), "c");
		assertLocation(code, "c;", 1, decls[0]);

		index = code.indexOf("char c");
		node = parse(code, index + 5, index + 6, true);
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertTrue(((IASTName) node).resolveBinding() instanceof ICPPVariable);
		IName[] refs = getReferencesOffTU((IASTName) node);
		assertEquals(refs.length, 1);
		assertEquals(refs[0].toString(), "c");
		assertLocation(code, "c; // ref", 1, refs[0]);
	}

	private void assertLocation(String code, String occur, int length, IName name) {
		int offset = code.indexOf(occur);
		final IASTFileLocation loc = name.getFileLocation();
		assertEquals(offset, loc.getNodeOffset());
		assertEquals(length, loc.getNodeLength());
	}
}
