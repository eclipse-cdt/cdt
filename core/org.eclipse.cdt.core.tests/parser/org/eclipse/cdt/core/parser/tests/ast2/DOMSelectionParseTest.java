/**********************************************************************
 * Copyright (c) 2002,2005 Rational Software Corporation and others.
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
import org.eclipse.cdt.core.dom.ast.cpp.ICPPTemplateInstance;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPVariable;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.cdt.internal.core.model.CProject;
import org.eclipse.core.resources.IFile;

/**
 * @author dsteffle
 */
public class DOMSelectionParseTest extends DOMSelectionParseBaseTest {

	public DOMSelectionParseTest(String name) {
		super(name, DOMSelectionParseTest.class);
	}

	public void testBaseCase_VariableReference() throws Exception
	{
		String code = "void f() { int x; x=3; }"; //$NON-NLS-1$
		int offset1 = code.indexOf( "x=" ); //$NON-NLS-1$
		int offset2 = code.indexOf( '=');
		IASTNode node = parse( code, offset1, offset2 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IVariable );
		assertEquals( ((IASTName)node).toString(), "x" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "x" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 15);
		assertEquals( ((ASTNode)decls[0]).getLength(), 1);
	}

	public void testBaseCase_FunctionReference() throws Exception
	{
		String code = "int x(){x( );}"; //$NON-NLS-1$
		int offset1 = code.indexOf( "x( " ); //$NON-NLS-1$
		int offset2 = code.indexOf( "( )"); //$NON-NLS-1$
		IASTNode node = parse( code, offset1, offset2 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IFunction );
		assertEquals( ((IASTName)node).toString(), "x" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "x" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 4);
		assertEquals( ((ASTNode)decls[0]).getLength(), 1);
	}
	
	public void testBaseCase_Error() throws Exception
	{
		String code = "int x() { y( ) }"; //$NON-NLS-1$
		int offset1 = code.indexOf( "y( " ); //$NON-NLS-1$
		int offset2 = code.indexOf( "( )"); //$NON-NLS-1$
		assertNull( parse( code, offset1, offset2, false ));
	}
	
	public void testBaseCase_FunctionDeclaration() throws Exception
	{
		String code = "int x(); x( );"; //$NON-NLS-1$
		int offset1 = code.indexOf( "x()" ); //$NON-NLS-1$
		int offset2 = code.indexOf( "()"); //$NON-NLS-1$
		IASTNode node = parse( code, offset1, offset2 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IFunction );
		assertEquals( ((IASTName)node).toString(), "x" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "x" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 4);
		assertEquals( ((ASTNode)decls[0]).getLength(), 1);
	}
	
	public void testBaseCase_FunctionDeclaration2() throws Exception
	{
		String code = "int printf( const char *, ... ); "; //$NON-NLS-1$
		int offset1 = code.indexOf( "printf" ); //$NON-NLS-1$
		int offset2 = code.indexOf( "( const"); //$NON-NLS-1$
		IASTNode node = parse( code, offset1, offset2 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IFunction );
		assertEquals( ((IASTName)node).toString(), "printf" ); //$NON-NLS-1$
	}

	public void testBaseCase_VariableDeclaration() throws Exception
	{
		String code = "int x = 3;"; //$NON-NLS-1$
		int offset1 = code.indexOf( "x" ); //$NON-NLS-1$
		int offset2 = code.indexOf( " ="); //$NON-NLS-1$
		IASTNode node = parse( code, offset1, offset2 );
		assertNotNull( node );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IVariable );
		assertEquals( ((IASTName)node).toString(), "x" ); //$NON-NLS-1$
	}
	
	public void testBaseCase_Parameter() throws Exception
	{
		String code = "int main( int argc ) { int x = argc; }"; //$NON-NLS-1$
		int offset1 = code.indexOf( "argc;" ); //$NON-NLS-1$
		int offset2 = code.indexOf( ";" ); //$NON-NLS-1$
		IASTNode node = parse( code, offset1, offset2 );
		assertNotNull( node );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IParameter );
		assertEquals( ((IASTName)node).toString(), "argc" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "argc" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 14);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
	}
	
	public void testBug57898() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class Gonzo {  public: void playHorn(); };\n" ); //$NON-NLS-1$
		writer.write( "void Gonzo::playHorn() { return; }\n" ); //$NON-NLS-1$
		writer.write( "int	main(int argc, char **argv) { Gonzo gonzo; gonzo.playHorn(); }\n" ); //$NON-NLS-1$
		String code = writer.toString();
		for( int i = 0; i < 3; ++i )
		{
			int start = -1, stop = -1;
			switch( i )
			{
				case 0:
					start = code.indexOf( "void playHorn") + 5; //$NON-NLS-1$
					break;
				case 1:
					start = code.indexOf( "::playHorn") + 2; //$NON-NLS-1$
					break;
				case 2:
					start = code.indexOf( ".playHorn") + 1; //$NON-NLS-1$
					break;
			}
			stop = start + 8;
			IASTNode node = parse( code, start, stop );
			assertNotNull( node );
			assertTrue( node instanceof IASTName );
			assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPMethod );
			assertEquals( ((IASTName)node).toString(), "playHorn" ); //$NON-NLS-1$
			IASTName[] decls = getDeclarationOffTU((IASTName)node);
			assertEquals(decls.length, 2);
			assertEquals( decls[0].toString(), "playHorn" ); //$NON-NLS-1$
			assertEquals( ((ASTNode)decls[0]).getOffset(), 28);
			assertEquals( ((ASTNode)decls[0]).getLength(), 8);
		}
	}
	
	public void testConstructorDestructorDeclaration() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class Gonzo { Gonzo(); ~Gonzo(); };"); //$NON-NLS-1$
		String code = writer.toString();
		int offset = code.indexOf( " Gonzo()") + 1; //$NON-NLS-1$
		IASTNode node = parse( code, offset, offset + 5 );
		assertNotNull( node );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPConstructor );
		assertEquals( ((IASTName)node).toString(), "Gonzo" ); //$NON-NLS-1$
		
		offset = code.indexOf( "~Gonzo"); //$NON-NLS-1$
		node = parse( code, offset, offset + 6 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPMethod );
		assertEquals( ((IASTName)node).toString(), "~Gonzo" ); //$NON-NLS-1$
	}	
	
	public void testBug60264() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "namespace Muppets { int i;	}\n" ); //$NON-NLS-1$
		writer.write( "int	main(int argc, char **argv) {	Muppets::i = 1; }\n" ); //$NON-NLS-1$
		String code = writer.toString();
		int index = code.indexOf( "Muppets::"); //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 7 );
		assertNotNull( node );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPNamespace );
		assertEquals( ((IASTName)node).toString(), "Muppets" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "Muppets" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 10);
		assertEquals( ((ASTNode)decls[0]).getLength(), 7);

		index = code.indexOf( "e Muppets") + 2; //$NON-NLS-1$
		node = parse( code, index, index + 7 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPNamespace );
		assertEquals( ((IASTName)node).toString(), "Muppets" ); //$NON-NLS-1$
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "Muppets" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 10);
		assertEquals( ((ASTNode)decls[0]).getLength(), 7);
	}
	
	public void testBug61613() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class Foo {  // ** (A) **\n" ); //$NON-NLS-1$
		writer.write( "	public:\n" ); //$NON-NLS-1$
		writer.write( "Foo() {};\n" ); //$NON-NLS-1$
		writer.write( "};\n" ); //$NON-NLS-1$
		writer.write( "int \n" ); //$NON-NLS-1$
		writer.write( "main(int argc, char **argv) {\n" ); //$NON-NLS-1$
		writer.write( "Foo foo;  // ** (B) **\n" ); //$NON-NLS-1$
		writer.write( "}\n" ); //$NON-NLS-1$
		String code = writer.toString();
		int index = code.indexOf( "class Foo") + 6; //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 3 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPClassType );
		assertEquals( ((IASTName)node).toString(), "Foo" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "Foo" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 6);
		assertEquals( ((ASTNode)decls[0]).getLength(), 3);
	}
	
	public void testBug60038() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class Gonzo {\n");		 //$NON-NLS-1$
		writer.write( "public:\n"); //$NON-NLS-1$
		writer.write( "Gonzo( const Gonzo & other ){}\n"); //$NON-NLS-1$
		writer.write( "Gonzo()	{}\n"); //$NON-NLS-1$
		writer.write( "~Gonzo(){}\n"); //$NON-NLS-1$
		writer.write( "};\n"); //$NON-NLS-1$
		writer.write( "int main(int argc, char **argv) {\n"); //$NON-NLS-1$
		writer.write( " Gonzo * g = new Gonzo();\n"); //$NON-NLS-1$
		writer.write( " Gonzo * g2 = new Gonzo( *g );\n"); //$NON-NLS-1$
		writer.write( " g->~Gonzo();\n"); //$NON-NLS-1$
		writer.write( " return (int) g2;\n"); //$NON-NLS-1$
		writer.write( "}\n"); //$NON-NLS-1$
		String code = writer.toString();
		for( int i = 0; i < 3; ++i )
		{
			int startOffset = 0, endOffset = 0;
			switch( i )
			{
				case 0:
					startOffset = code.indexOf( "new Gonzo()") + 4; //$NON-NLS-1$
					endOffset = startOffset + 5;
					break;
				case 1:
					startOffset = code.indexOf( "new Gonzo( ") + 4; //$NON-NLS-1$
					endOffset = startOffset + 5;
					break;
				default:
					startOffset = code.indexOf( "->~") + 2; //$NON-NLS-1$
					endOffset = startOffset + 6;
			}
			IASTNode node = parse( code, startOffset, endOffset );
			assertTrue( node instanceof IASTName );
			assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPMethod );
			IBinding binding = ((IASTName)node).resolveBinding();
			IASTName[] decls = null;
			switch( i )
			{
				case 0:
					assertTrue( binding instanceof ICPPConstructor );
					decls = getDeclarationOffTU((IASTName)node);
					assertEquals(decls.length, 1);
					assertEquals( decls[0].toString(), "Gonzo" ); //$NON-NLS-1$
					assertEquals( ((ASTNode)decls[0]).getOffset(), 53);
					assertEquals( ((ASTNode)decls[0]).getLength(), 5);
					break;
				case 1: 
					assertTrue( binding instanceof ICPPConstructor );
					decls = getDeclarationOffTU((IASTName)node);
					assertEquals(decls.length, 1);
					assertEquals( decls[0].toString(), "Gonzo" ); //$NON-NLS-1$
					assertEquals( ((ASTNode)decls[0]).getOffset(), 22);
					assertEquals( ((ASTNode)decls[0]).getLength(), 5);
					break;
				default: 
					assertFalse( binding instanceof ICPPConstructor );
					String name = ((IASTName)node).toString();
					assertEquals( name.indexOf("~"), 0); //$NON-NLS-1$
					assertEquals( name.indexOf("Gonzo"), 1); //$NON-NLS-1$
					decls = getDeclarationOffTU((IASTName)node);
					assertEquals(decls.length, 1);
					assertEquals( decls[0].toString(), "~Gonzo" ); //$NON-NLS-1$
					assertEquals( ((ASTNode)decls[0]).getOffset(), 64);
					assertEquals( ((ASTNode)decls[0]).getLength(), 6);
					break;
					
			}
		}
	}
	
	public void testMethodReference() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class Sample { public:\n"); //$NON-NLS-1$
		writer.write( "  int getAnswer() const;\n"); //$NON-NLS-1$
		writer.write( "};\n"); //$NON-NLS-1$
		writer.write( "int main(int argc, char **argv) {\n" ); //$NON-NLS-1$
		writer.write( " Sample * s = new Sample();\n" ); //$NON-NLS-1$
		writer.write( " return s->getAnswer();\n" ); //$NON-NLS-1$
		writer.write( "}\n" ); //$NON-NLS-1$
		String code = writer.toString();
		int startIndex = code.indexOf( "->getAnswer") + 2; //$NON-NLS-1$
		IASTNode node = parse( code, startIndex, startIndex+9);
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPMethod );
		assertEquals( ((IASTName)node).toString(), "getAnswer" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "getAnswer" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 29);
		assertEquals( ((ASTNode)decls[0]).getLength(), 9);
	}
	
	public void testConstructorDefinition() throws Exception
	{
		String code = "class ABC { public: ABC(); }; ABC::ABC(){}"; //$NON-NLS-1$
		int startIndex = code.indexOf( "::ABC") + 2; //$NON-NLS-1$
		IASTNode node = parse( code, startIndex, startIndex + 3 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPConstructor );
		assertEquals( ((IASTName)node).toString(), "ABC" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 2);
		assertEquals( decls[0].toString(), "ABC" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 20);
		assertEquals( ((ASTNode)decls[0]).getLength(), 3);
	}
	
	public void testBug63966() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "void foo(int a) {}\n" ); //$NON-NLS-1$
		writer.write( "void foo(long a) {}\n" ); //$NON-NLS-1$
		writer.write( "int main(int argc, char **argv) {\n" ); //$NON-NLS-1$
		writer.write( "foo(1); \n }" ); //$NON-NLS-1$
		String code = writer.toString();
		int startIndex = code.indexOf( "foo(1)"); //$NON-NLS-1$
		parse( code, startIndex, startIndex + 3 );
	}
	
	public void testBug66744() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "enum EColours { RED, GREEN, BLUE };      \n" ); //$NON-NLS-1$
		writer.write( "void foo() {  EColours color = GREEN; }  \n" ); //$NON-NLS-1$
		
		String code = writer.toString();
		int startIndex = code.indexOf( "EColours color"); //$NON-NLS-1$
		parse( code, startIndex, startIndex + 8 );
	}
	

	
	public void testBug68527() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("struct X;\n"); //$NON-NLS-1$
		writer.write("struct X anA;"); //$NON-NLS-1$
		String code = writer.toString();
		int startIndex = code.indexOf( "X anA"); //$NON-NLS-1$
		parse( code, startIndex, startIndex + 1 );
	}

	public void testBug60407() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "struct ZZZ { int x, y, z; };\n" ); //$NON-NLS-1$
		writer.write( "typedef struct ZZZ _FILE;\n" ); //$NON-NLS-1$
		writer.write( "typedef _FILE FILE;\n" ); //$NON-NLS-1$
		writer.write( "static void static_function(FILE * lcd){}\n" ); //$NON-NLS-1$
		writer.write( "int	main(int argc, char **argv) {\n" ); //$NON-NLS-1$
		writer.write( "FILE * file = 0;\n" ); //$NON-NLS-1$
		writer.write( "static_function( file );\n" ); //$NON-NLS-1$
		writer.write( "return 0;\n" );	 //$NON-NLS-1$
		writer.write( "}\n" ); //$NON-NLS-1$
		String code = writer.toString();
		int startIndex = code.indexOf( "static_function( file )"); //$NON-NLS-1$
		parse( code, startIndex, startIndex + "static_function".length() ); //$NON-NLS-1$
	}
	
	public void testBug61800() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class B {};\n"); //$NON-NLS-1$
		writer.write( "class ABCDEF {\n"); //$NON-NLS-1$
		writer.write( " static B stInt; };\n"); //$NON-NLS-1$
		writer.write( "B ABCDEF::stInt = 5;\n"); //$NON-NLS-1$
		String code = writer.toString();
		int startIndex = code.indexOf( "::stInt") + 2; //$NON-NLS-1$

		IASTNode node = parse( code, startIndex, startIndex+ 5 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPField );
		assertEquals( ((IASTName)node).toString(), "stInt" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 2);
		assertEquals( decls[0].toString(), "stInt" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 37);
		assertEquals( ((ASTNode)decls[0]).getLength(), 5);
	}
	
	public void testBug68739() throws Exception
	{
	    Writer writer = new StringWriter();
	    writer.write( "int fprintf( int *, const char *, ... );               \n" ); //$NON-NLS-1$
	    writer.write( "void boo( int * lcd ) {                                \n" ); //$NON-NLS-1$
	    writer.write( "  /**/fprintf( lcd, \"%c%s 0x%x\", ' ', \"bbb\", 2 );  \n" ); //$NON-NLS-1$
	    writer.write( "}                                                      \n" ); //$NON-NLS-1$
	    
	    String code = writer.toString();
		int startIndex = code.indexOf( "/**/fprintf") + 4; //$NON-NLS-1$

		IASTNode node = parse( code, startIndex, startIndex+ 7 );
		
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IFunction );
		assertEquals( ((IASTName)node).toString(), "fprintf" ); //$NON-NLS-1$
	}
	
	public void testBug72818() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "union Squaw	{	int x;	double u; };\n" ); //$NON-NLS-1$
		writer.write( "int	main(int argc, char **argv) {\n" ); //$NON-NLS-1$
		writer.write( "return sizeof( Squaw );\n" ); //$NON-NLS-1$
		writer.write( "}\n" ); //$NON-NLS-1$
		String code = writer.toString();
		int startIndex = code.indexOf( "sizeof( ") + "sizeof( ".length();  //$NON-NLS-1$ //$NON-NLS-2$
		IASTNode node = parse( code, startIndex, startIndex + 5 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPClassType );
		assertEquals( ((IASTName)node).toString(), "Squaw" ); //$NON-NLS-1$
		assertEquals( ((ICPPClassType)((IASTName)node).resolveBinding()).getKey(), ICPPClassType.k_union );
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "Squaw" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 6);
		assertEquals( ((ASTNode)decls[0]).getLength(), 5);
	}
	
	public void test72220() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "const int FOUND_ME = 1;\n" ); //$NON-NLS-1$
		writer.write( "class Test{\n" ); //$NON-NLS-1$
		writer.write( "public:\n" ); //$NON-NLS-1$
		writer.write( "const int findCode() const;\n" ); //$NON-NLS-1$
		writer.write( "};\n" ); //$NON-NLS-1$
		writer.write( "const int Test::findCode() const {\n" ); //$NON-NLS-1$
		writer.write( "return FOUND_ME;\n" ); //$NON-NLS-1$
		writer.write( "}\n" ); //$NON-NLS-1$
		String code = writer.toString();
		int startIndex = code.indexOf( "return ") + "return ".length();  //$NON-NLS-1$ //$NON-NLS-2$
		IASTNode node = parse( code, startIndex, startIndex + 8 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IVariable );
		assertEquals( ((IASTName)node).toString(), "FOUND_ME" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "FOUND_ME" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 10);
		assertEquals( ((ASTNode)decls[0]).getLength(), 8);
	}
	
	public void testBug72721() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write(" class ABC { public: ABC(int); };   \n"); //$NON-NLS-1$
	    writer.write("void f() {                          \n"); //$NON-NLS-1$
	    writer.write("   int j = 1;                       \n"); //$NON-NLS-1$
	    writer.write("   new ABC( j + 1 );                \n"); //$NON-NLS-1$
	    writer.write("}                                   \n"); //$NON-NLS-1$
	    
	    String code = writer.toString();
	    int startIndex = code.indexOf( "ABC(" ); //$NON-NLS-1$
	    IASTNode node = parse( code, startIndex, startIndex + 3 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPConstructor );
		assertEquals( ((IASTName)node).toString(), "ABC" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "ABC" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 21);
		assertEquals( ((ASTNode)decls[0]).getLength(), 3);
	}
	
	public void testBug72372() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("namespace B {                                   \n"); //$NON-NLS-1$
	    writer.write("   class SD_02 { void f_SD(); };                \n"); //$NON-NLS-1$
	    writer.write("}                                               \n"); //$NON-NLS-1$
	    writer.write("using namespace B;                              \n"); //$NON-NLS-1$
	    writer.write("void SD_02::f_SD(){}                            \n"); //$NON-NLS-1$
	    
	    String code = writer.toString();
	    int startIndex = code.indexOf( ":f_SD" ); //$NON-NLS-1$
	    IASTNode node = parse( code, startIndex + 1, startIndex + 5 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPMethod );
		assertEquals( ((IASTName)node).toString(), "f_SD" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 2);
		assertEquals( decls[0].toString(), "f_SD" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 71);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
	}
	public void testBug72372_2() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("namespace A {                                   \n"); //$NON-NLS-1$
	    writer.write("   namespace B {                                \n"); //$NON-NLS-1$
	    writer.write("      void f_SD();                              \n"); //$NON-NLS-1$
	    writer.write("   }                                            \n"); //$NON-NLS-1$
	    writer.write("}                                               \n"); //$NON-NLS-1$
	    writer.write("namespace C {                                   \n"); //$NON-NLS-1$
	    writer.write("   using namespace A;                           \n"); //$NON-NLS-1$
	    writer.write("}                                               \n"); //$NON-NLS-1$
	    writer.write("void C::B::f_SD(){}                             \n"); //$NON-NLS-1$
	    
	    String code = writer.toString();
	    int startIndex = code.indexOf( ":f_SD" ); //$NON-NLS-1$
	    IASTNode node = parse( code, startIndex + 1, startIndex + 5 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IFunction );
		assertEquals( ((IASTName)node).toString(), "f_SD" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 2);
		assertEquals( decls[0].toString(), "f_SD" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 109);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
	}
	
	public void testBug72713() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "class Deck{ void initialize(); };   \n"); //$NON-NLS-1$
	    writer.write( "void Deck::initialize(){}           \n"); //$NON-NLS-1$
	    
	    String code = writer.toString();
	    int startIndex = code.indexOf( ":initialize" ); //$NON-NLS-1$
	    IASTNode node = parse( code, startIndex + 1, startIndex + 11 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPMethod );
		assertEquals( ((IASTName)node).toString(), "initialize" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 2);
		assertEquals( decls[0].toString(), "initialize" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 17);
		assertEquals( ((ASTNode)decls[0]).getLength(), 10);
	}
	
	public void testBug72712() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "class B{ public: B(); }; void f(){ B* b; b = new B(); }" ); //$NON-NLS-1$
	    
	    String code = writer.toString();
	    int startIndex = code.indexOf( "new B" ) + 4; //$NON-NLS-1$
	    
	    IASTNode node = parse( code, startIndex, startIndex + 1 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPConstructor );
		assertEquals( ((IASTName)node).toString(), "B" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "B" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 17);
		assertEquals( ((ASTNode)decls[0]).getLength(), 1);
	}
	
	public void testBug72712_2() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "class A {};                                        \n"); //$NON-NLS-1$
	    writer.write( "class B{ public: B( A* ); };                       \n"); //$NON-NLS-1$
	    writer.write( "void f(){ B* b; b = new B( (A*)0 ); }              \n"); //$NON-NLS-1$
	    
	    String code = writer.toString();
	    int startIndex = code.indexOf( "(A*)" ) + 1; //$NON-NLS-1$
	    
	    IASTNode node = parse( code, startIndex, startIndex + 1 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPClassType );
		assertEquals( ((IASTName)node).toString(), "A" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "A" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 6);
		assertEquals( ((ASTNode)decls[0]).getLength(), 1);
	}
	
	public void testBug72814() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "namespace N{                                \n"); //$NON-NLS-1$
	    writer.write( "   template < class T > class AAA { T _t; };\n"); //$NON-NLS-1$
	    writer.write( "}                                           \n"); //$NON-NLS-1$
	    writer.write( "N::AAA<int> a;                              \n"); //$NON-NLS-1$
	    
	    String code = writer.toString();
	    int startIndex = code.indexOf( "AAA<int>" ); //$NON-NLS-1$
	    IASTNode node = parse( code, startIndex, startIndex + 3 );
	    
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPClassType );
		assertEquals( ((IASTName)node).toString(), "AAA" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "AAA" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 75);
		assertEquals( ((ASTNode)decls[0]).getLength(), 3);
		
	    node = parse( code, startIndex, startIndex + 8 );
	    
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPClassType );
		assertEquals( ((IASTName)node).toString(), "AAA" ); //$NON-NLS-1$
		decls = getDeclarationOffTU((IASTName)node);
		// TODO raised bug 92632 for below
//		assertEquals(decls.length, 1);
//		assertEquals( decls[0].toString(), "AAA" ); //$NON-NLS-1$
//		assertEquals( ((ASTNode)decls[0]).getOffset(), 15);
//		assertEquals( ((ASTNode)decls[0]).getLength(), 3);
	}
	
	public void testBug72710() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class Card{\n" ); //$NON-NLS-1$
		writer.write( "	Card( int rank );\n" ); //$NON-NLS-1$
		writer.write( " int rank;\n" ); //$NON-NLS-1$
		writer.write( "};\n" ); //$NON-NLS-1$
		writer.write( "Card::Card( int rank ) {\n" ); //$NON-NLS-1$
		writer.write( "this->rank = rank;\n" ); //$NON-NLS-1$
		writer.write( "}\n" ); //$NON-NLS-1$
		String code = writer.toString();
		int index = code.indexOf( "this->rank") + 6; //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPField );
		assertEquals( ((IASTName)node).toString(), "rank" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 36);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
	}
	
	
	public void testBug75731() throws Exception 
	{
		Writer writer = new StringWriter();
		writer.write("int rank() {\n"); //$NON-NLS-1$
		writer.write("return 5;\n}\n"); //$NON-NLS-1$
		writer.write("class Card{\n"); //$NON-NLS-1$
		writer.write("private:\n"); //$NON-NLS-1$
		writer.write("Card( int rank );\n"); //$NON-NLS-1$
		writer.write("int rank;\n"); //$NON-NLS-1$
		writer.write("public:\n");  //$NON-NLS-1$
		writer.write("int getRank();\n};\n"); //$NON-NLS-1$
		writer.write("Card::Card( int rank )\n{\n");  //$NON-NLS-1$
		writer.write("this->rank = ::rank();\n"); //$NON-NLS-1$
		writer.write("this->rank = this->rank;\n"); //$NON-NLS-1$
		writer.write("this->rank = rank;\n"); //$NON-NLS-1$
		writer.write("this->rank = Card::rank;\n"); //$NON-NLS-1$
		writer.write("this->rank = getRank();\n}\n"); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "int rank() {") + 4; //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IFunction );
		assertEquals( ((IASTName)node).toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 4);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "class Card{") + 6; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPClassType );
		assertEquals( ((IASTName)node).toString(), "Card" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "Card" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 31);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "Card( int rank );"); //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPConstructor );
		assertEquals( ((IASTName)node).toString(), "Card" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 2);
		assertEquals( decls[0].toString(), "Card" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 46);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "Card( int rank );") + 10; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IParameter );
		assertEquals( ((IASTName)node).toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 2);
		assertEquals( decls[0].toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 56);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);

		index = code.indexOf( "int rank;") + 4; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPField );
		assertEquals( ((IASTName)node).toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 68);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "int getRank();") + 4; //$NON-NLS-1$
		node = parse( code, index, index + 7 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPMethod );
		assertEquals( ((IASTName)node).toString(), "getRank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "getRank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 86);
		assertEquals( ((ASTNode)decls[0]).getLength(), 7);
		
		index = code.indexOf( "Card::Card( int rank )"); //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPClassType );
		assertEquals( ((IASTName)node).toString(), "Card" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "Card" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 31);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "Card::Card( int rank )") + 6; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPConstructor );
		assertEquals( ((IASTName)node).toString(), "Card" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 2);
		assertEquals( decls[0].toString(), "Card" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 46);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "Card::Card( int rank )") + 16; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IParameter );
		assertEquals( ((IASTName)node).toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 2);
		assertEquals( decls[0].toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 56);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "this->rank = ::rank();") + 6; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPField );
		assertEquals( ((IASTName)node).toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 68);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "this->rank = ::rank();") + 15; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IFunction );
		assertEquals( ((IASTName)node).toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 4);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "this->rank = this->rank;") + 6; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPField );
		assertEquals( ((IASTName)node).toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 68);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "this->rank = this->rank;") + 19; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPField );
		assertEquals( ((IASTName)node).toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 68);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "this->rank = rank;") + 6; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPField );
		assertEquals( ((IASTName)node).toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 68);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "this->rank = rank;") + 13; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IParameter );
		assertEquals( ((IASTName)node).toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 2);
		assertEquals( decls[0].toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 56);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "this->rank = Card::rank;") + 6; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPField );
		assertEquals( ((IASTName)node).toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 68);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "this->rank = Card::rank;") + 19; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPField );
		assertEquals( ((IASTName)node).toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 68);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "this->rank = getRank();") + 6; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPField );
		assertEquals( ((IASTName)node).toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "rank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 68);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
		
		index = code.indexOf( "this->rank = getRank();") + 13; //$NON-NLS-1$
		node = parse( code, index, index + 7 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPMethod );
		assertEquals( ((IASTName)node).toString(), "getRank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "getRank" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 86);
		assertEquals( ((ASTNode)decls[0]).getLength(), 7);
	}

	public void testBug77989() throws Exception {
		Writer writer = new StringWriter();
		writer.write("namespace N {        /* A */\n"); //$NON-NLS-1$
		writer.write("class C{};\n}\n"); //$NON-NLS-1$
		writer.write("using namespace N;   /* B */\n"); //$NON-NLS-1$
		writer.write("N::C c;              /* C */\n"); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "using namespace N;") + 16; //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 1 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPNamespace );
		assertEquals( ((IASTName)node).toString(), "N" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "N" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 10);
		assertEquals( ((ASTNode)decls[0]).getLength(), 1);
	}

	public void testBug78435() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int itself;          //A\n"); //$NON-NLS-1$ 
		writer.write("void f(int itself){} //B\n"); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "void f(int itself){}") + 11; //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 6 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IParameter );
		assertEquals( ((IASTName)node).toString(), "itself" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "itself" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 36);
		assertEquals( ((ASTNode)decls[0]).getLength(), 6);
	}

	public void testBug78231A() throws Exception {
		Writer writer = new StringWriter();
		writer.write("struct Base {\n"); //$NON-NLS-1$
		writer.write("int Data; // 1\n"); //$NON-NLS-1$
		writer.write("struct Data; // 2\n};\n"); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf("struct Data;") + 7; //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICompositeType );
		assertEquals( ((IASTName)node).toString(), "Data" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "Data" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 36);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
	}
	
	public void testBug78231B() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int Data;\n"); //$NON-NLS-1$
		writer.write("struct Base {\n"); //$NON-NLS-1$
		writer.write("int Data; // 1\n"); //$NON-NLS-1$
		writer.write("struct Data; // 2\n};\n"); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf("struct Data;") + 7; //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICompositeType );
		assertEquals( ((IASTName)node).toString(), "Data" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)node).getOffset(), index);
	}
	
	public void testBug64326() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class foo {\n"); //$NON-NLS-1$
		writer.write("public:\n"); //$NON-NLS-1$
		writer.write("foo() {}\n"); //$NON-NLS-1$
		writer.write("int bar;\n"); //$NON-NLS-1$
		writer.write("};\n"); //$NON-NLS-1$
		writer.write("int\n"); //$NON-NLS-1$
		writer.write("main(int argc, char **argv) {\n"); //$NON-NLS-1$
		writer.write("foo* f;\n"); //$NON-NLS-1$
		writer.write("f->bar = 1; // ** (A) **\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
				
		String code = writer.toString();
		int index = code.indexOf("f->bar") + 3; //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 3 );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPField );
		assertEquals( ((IASTName)node).toString(), "bar" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "bar" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 33);
		assertEquals( ((ASTNode)decls[0]).getLength(), 3);
	}
	
	public void testBug92605() throws Exception {
		Writer writer = new StringWriter();
		writer.write("#define UINT32 unsigned int\n"); //$NON-NLS-1$
		writer.write("#define HANDLE unsigned int**\n"); //$NON-NLS-1$
		writer.write("// ...\n"); //$NON-NLS-1$
		writer.write("void foo()\n"); //$NON-NLS-1$
		writer.write("{\n"); //$NON-NLS-1$
		writer.write("UINT32 u;  // line A\n"); //$NON-NLS-1$
		writer.write("HANDLE h;  // line B\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		String code = writer.toString();

		int index = code.indexOf("UINT32 u;"); //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 6, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IMacroBinding );
		assertEquals( ((IASTName)node).toString(), "UINT32" ); //$NON-NLS-1$
		
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "UINT32" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 8);
		assertEquals( ((ASTNode)decls[0]).getLength(), 6);
	}
	
	public void testBug79877() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int Func2() {\n"); //$NON-NLS-1$
		writer.write("int i;\n"); //$NON-NLS-1$
		writer.write("i = Func1();\n"); //$NON-NLS-1$
		writer.write("return(0);\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		String code = writer.toString();
		IFile test1 = importFile("test1.c", code); //$NON-NLS-1$
		
		writer.write("int Func1(void) {\n"); //$NON-NLS-1$
		writer.write("return(10);\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		importFile("test2.c", writer.toString()); //$NON-NLS-1$
		
		int index = code.indexOf("Func1"); //$NON-NLS-1$
		IASTNode node = parse( test1, index, index + 5, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICExternalBinding );
		assertEquals( ((IASTName)node).toString(), "Func1" ); //$NON-NLS-1$
		
		ICElement[] scope = new ICElement[1];
		scope[0] = new CProject(null, project);

//		// TODO need to register to an index and wait for it to finish before this test will work
//		
//		Set matches = SearchEngine.getMatchesFromSearchEngine(SearchEngine.createCSearchScope(scope), (IASTName)node, CSearchPattern.DECLARATIONS);
//		assertEquals(matches.size(), 1);
	}
	
	public void testBug78114() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class Point{			//line C\n"); //$NON-NLS-1$
		writer.write("public:\n"); //$NON-NLS-1$
		writer.write("Point(): xCoord(0){}\n"); //$NON-NLS-1$
		writer.write("Point(int x){}		//line B	\n"); //$NON-NLS-1$
		writer.write("private:	\n"); //$NON-NLS-1$
		writer.write("int xCoord;	\n"); //$NON-NLS-1$
		writer.write("};\n"); //$NON-NLS-1$
		writer.write("int main(int argc, char **argv) {\n"); //$NON-NLS-1$
		writer.write("Point &p2 = *(new Point(10));	// line A\n"); //$NON-NLS-1$
		writer.write("return (0);\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		
		String code = writer.toString();
					 							
		int index = code.indexOf("Point(10)"); //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 5, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPConstructor );
		assertEquals( ((IASTName)node).toString(), "Point" ); //$NON-NLS-1$
		
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "Point" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 53);
		assertEquals( ((ASTNode)decls[0]).getLength(), 5);
	}
	
	public void testBug73398() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int joo=4;\n"); //$NON-NLS-1$
		writer.write("#define koo 4\n"); //$NON-NLS-1$
		writer.write("int main(int argc, char **argv) {\n"); //$NON-NLS-1$
		writer.write("return (koo);\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		
		String code = writer.toString();
			
		int index = code.indexOf("koo);"); //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 3, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IMacroBinding );
		assertEquals( ((IASTName)node).toString(), "koo" ); //$NON-NLS-1$
		
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "koo" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 19);
		assertEquals( ((ASTNode)decls[0]).getLength(), 3);
	}
	
	public void testBug() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class Point{	\n"); //$NON-NLS-1$
		writer.write("public:\n"); //$NON-NLS-1$
		writer.write("Point(): xCoord(0){}\n"); //$NON-NLS-1$
		writer.write(" Point& operator=(const Point &rhs){return *this;}	// line A\n"); //$NON-NLS-1$
		writer.write("private:		\n"); //$NON-NLS-1$
		writer.write("int xCoord;		\n"); //$NON-NLS-1$
		writer.write("};\n"); //$NON-NLS-1$
		writer.write("static const Point zero;\n"); //$NON-NLS-1$
		writer.write("int main(int argc, char **argv) {	\n"); //$NON-NLS-1$
		writer.write(" Point *p2 = new Point();\n"); //$NON-NLS-1$
		writer.write("p2->operator=(zero);           // line B\n"); //$NON-NLS-1$
		writer.write("return (0);	\n"); //$NON-NLS-1$
		writer.write(" }\n"); //$NON-NLS-1$
		
	    String code = writer.toString();
			
		int index = code.indexOf("operator=(zero)"); //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 9, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPMethod );
		assertEquals( ((IASTName)node).toString(), "operator =" ); //$NON-NLS-1$
		
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "operator =" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 51);
		assertEquals( ((ASTNode)decls[0]).getLength(), 9);
	}
	
	public void testBug80826() throws Exception {
		Writer writer = new StringWriter();
		writer.write("void swapImpl(int& a, int& b) {/*...*/} // line C\n"); //$NON-NLS-1$
		writer.write("#define swap(a,b) (swapImpl(a,b))   // line B\n"); //$NON-NLS-1$
		writer.write("//		...\n"); //$NON-NLS-1$
		writer.write("void foo(int x, int y)\n"); //$NON-NLS-1$
		writer.write("{\n"); //$NON-NLS-1$
		writer.write("swap(x,y); // line A\n"); //$NON-NLS-1$
		writer.write("		  //...\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		
		String code = writer.toString();
			
		int index = code.indexOf("swap(x,y);"); //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 4, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IMacroBinding );
		assertEquals( ((IASTName)node).toString(), "swap" ); //$NON-NLS-1$
		
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "swap" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 58);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
	}
	
	public void testBug78389() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class A{\n"); //$NON-NLS-1$
		writer.write("void method1(){}  //line A\n"); //$NON-NLS-1$
		writer.write("void method1(int i){} //line B\n"); //$NON-NLS-1$
		writer.write("};\n"); //$NON-NLS-1$
		writer.write("void f(){\n"); //$NON-NLS-1$
		writer.write("A a; \n"); //$NON-NLS-1$
		writer.write("a.method1(3); // F3 on method1 in this line should highlight method1 on line B\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
			
		String code = writer.toString();
			
		int index = code.indexOf("method1(3)"); //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 7, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPMethod );
		assertEquals( ((IASTName)node).toString(), "method1" ); //$NON-NLS-1$
		
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "method1" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 41);
		assertEquals( ((ASTNode)decls[0]).getLength(), 7);
	}
	
	public void testBug78625() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class A{            \n"); //$NON-NLS-1$
		writer.write("public: A(int i){} \n"); //$NON-NLS-1$
		writer.write("};\n"); //$NON-NLS-1$
		writer.write("class B: A{\n"); //$NON-NLS-1$
		writer.write("B():A(2) {}    //line 5\n"); //$NON-NLS-1$
		writer.write("};\n"); //$NON-NLS-1$
			
		String code = writer.toString();
			
		int index = code.indexOf("A(2)"); //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 1, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPConstructor );
		assertEquals( ((IASTName)node).toString(), "A" ); //$NON-NLS-1$
		
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "A" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 29);
		assertEquals( ((ASTNode)decls[0]).getLength(), 1);
	}
	
	public void testBug78656() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class A{\n"); //$NON-NLS-1$
		writer.write("public: int method1(){} //line 2\n"); //$NON-NLS-1$
		writer.write("};\n"); //$NON-NLS-1$
		writer.write("void f() {\n"); //$NON-NLS-1$
		writer.write("A a;\n"); //$NON-NLS-1$
		writer.write("int i=a.method1();  //line 6\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		
		String code = writer.toString();
			
		int index = code.indexOf("method1();"); //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 7, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPMethod );
		assertEquals( ((IASTName)node).toString(), "method1" ); //$NON-NLS-1$
		
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "method1" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 21);
		assertEquals( ((ASTNode)decls[0]).getLength(), 7);
	}
	
	public void testBug79965() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int i = 2, half_i = i / 2;\n"); //$NON-NLS-1$
		
		String code = writer.toString();
			
		int index = code.indexOf("i / 2"); //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 1, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IVariable );
		assertEquals( ((IASTName)node).toString(), "i" ); //$NON-NLS-1$
		
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "i" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 4);
		assertEquals( ((ASTNode)decls[0]).getLength(), 1);
	}
	
	public void testBug64326A() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class foo {\n"); //$NON-NLS-1$
		writer.write("public:\n"); //$NON-NLS-1$
		writer.write("foo() {}\n"); //$NON-NLS-1$
		writer.write("void bar() {}\n"); //$NON-NLS-1$
		writer.write("};\n"); //$NON-NLS-1$
		writer.write("int\n"); //$NON-NLS-1$
		writer.write("main(int argc, char **argv) {\n"); //$NON-NLS-1$
		writer.write("foo* f;\n"); //$NON-NLS-1$
		writer.write("f->bar(); // ** (A) **\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		
		String code = writer.toString();
			
		int index = code.indexOf("bar();"); //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 3, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPMethod );
		assertEquals( ((IASTName)node).toString(), "bar" ); //$NON-NLS-1$
		
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "bar" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 34);
		assertEquals( ((ASTNode)decls[0]).getLength(), 3);
	}
	
	public void testBug64326B() throws Exception {
		Writer writer = new StringWriter();
		writer.write("class foo {\n"); //$NON-NLS-1$
		writer.write("public:\n"); //$NON-NLS-1$
		writer.write("foo() {}\n"); //$NON-NLS-1$
		writer.write("int bar;\n"); //$NON-NLS-1$
		writer.write("};\n"); //$NON-NLS-1$
		writer.write("int \n"); //$NON-NLS-1$
		writer.write("main(int argc, char **argv) {\n"); //$NON-NLS-1$
		writer.write("foo* f;\n"); //$NON-NLS-1$
		writer.write("f->bar = 1; // ** (A) **\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		
		String code = writer.toString();
			
		int index = code.indexOf("bar = "); //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 3, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPField );
		assertEquals( ((IASTName)node).toString(), "bar" ); //$NON-NLS-1$
		
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "bar" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 33);
		assertEquals( ((ASTNode)decls[0]).getLength(), 3);
	}
	
	public void testBug43128A() throws Exception {
		Writer writer = new StringWriter();
		writer.write("void foo()\n"); //$NON-NLS-1$
		writer.write("{\n"); //$NON-NLS-1$
		writer.write("int x=3;\n"); //$NON-NLS-1$
		writer.write("		  // ...\n"); //$NON-NLS-1$
		writer.write("x++;\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		
		String code = writer.toString();
			
		int index = code.indexOf("x++"); //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 1, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IVariable );
		assertEquals( ((IASTName)node).toString(), "x" ); //$NON-NLS-1$
		
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "x" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 17);
		assertEquals( ((ASTNode)decls[0]).getLength(), 1);
	}
	
	public void testBug43128B() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int\n"); //$NON-NLS-1$
		writer.write("main(int argc, char **argv) {\n"); //$NON-NLS-1$
		writer.write("int x = argc;\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		
		String code = writer.toString();
			
		int index = code.indexOf("argc;"); //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 4, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IParameter );
		assertEquals( ((IASTName)node).toString(), "argc" ); //$NON-NLS-1$
		
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "argc" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 13);
		assertEquals( ((ASTNode)decls[0]).getLength(), 4);
	}
	
	public void testBug43128C() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int j;\n"); //$NON-NLS-1$
		writer.write("int foo(int x) \n"); //$NON-NLS-1$
		writer.write("{	\n"); //$NON-NLS-1$
		writer.write("int y;\n"); //$NON-NLS-1$
		writer.write("x = 4;\n"); //$NON-NLS-1$
		writer.write("j = 5;\n"); //$NON-NLS-1$
		writer.write("y = 6;\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$

		String code = writer.toString();
			
		int index = code.indexOf("x ="); //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 1, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IVariable );
		assertEquals( ((IASTName)node).toString(), "x" ); //$NON-NLS-1$
		
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "x" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 19);
		assertEquals( ((ASTNode)decls[0]).getLength(), 1);
		
		index = code.indexOf("j ="); //$NON-NLS-1$
		node = parse( code, index, index + 1, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IVariable );
		assertEquals( ((IASTName)node).toString(), "j" ); //$NON-NLS-1$
		
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "j" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 4);
		assertEquals( ((ASTNode)decls[0]).getLength(), 1);
		
		index = code.indexOf("y ="); //$NON-NLS-1$
		node = parse( code, index, index + 1, true );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IVariable );
		assertEquals( ((IASTName)node).toString(), "y" ); //$NON-NLS-1$
		
		decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "y" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 30);
		assertEquals( ((ASTNode)decls[0]).getLength(), 1);
	}	
	
    public void testBug86504() throws Exception {
        Writer writer = new StringWriter();
        writer.write("class C { };\n"); //$NON-NLS-1$
        writer.write("void f(int(C)) { } // void f(int (*fp)(C c)) { }\n"); //$NON-NLS-1$
        writer.write("// not: void f(int C);\n"); //$NON-NLS-1$
        writer.write("int g(C);\n"); //$NON-NLS-1$
        writer.write("void foo() {\n"); //$NON-NLS-1$
        writer.write("f(g); // openDeclarations on g causes StackOverflowError\n"); //$NON-NLS-1$
        writer.write("}\n"); //$NON-NLS-1$
        
        String code = writer.toString();
            
        int index = code.indexOf("g); "); //$NON-NLS-1$
        IASTNode node = parse( code, index, index + 1, true );
        assertTrue( node instanceof IASTName );
        assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPFunction );
        assertEquals( ((IASTName)node).toString(), "g" ); //$NON-NLS-1$
        
        IASTName[] decls = getDeclarationOffTU((IASTName)node);
        assertEquals(decls.length, 1);
        assertEquals( decls[0].toString(), "g" ); //$NON-NLS-1$
        assertEquals( ((ASTNode)decls[0]).getOffset(), 89);
        assertEquals( ((ASTNode)decls[0]).getLength(), 1);
    }   

    public void testBug79811() throws Exception {
        Writer writer = new StringWriter();
        writer.write("enum E{E0};\n"); //$NON-NLS-1$
        writer.write("void f() {\n"); //$NON-NLS-1$
        writer.write("enum E{E1};\n"); //$NON-NLS-1$
        writer.write("E e;   //this one is incorrectly found\n"); //$NON-NLS-1$
        writer.write("}\n"); //$NON-NLS-1$
        writer.write("E f;   //ok\n"); //$NON-NLS-1$
        
        String code = writer.toString();
            
        int index = code.indexOf("E{E0}"); //$NON-NLS-1$
        IASTNode node = parse( code, index, index + 1, true );
        assertTrue( node instanceof IASTName );
        assertTrue( ((IASTName)node).resolveBinding() instanceof IEnumeration );
        assertEquals( ((IASTName)node).toString(), "E" ); //$NON-NLS-1$
        
        IASTName[] decls = getReferencesOffTU((IASTName)node);
        assertEquals(decls.length, 1);
        assertEquals( decls[0].toString(), "E" ); //$NON-NLS-1$
        assertEquals( ((ASTNode)decls[0]).getOffset(), 76);
        assertEquals( ((ASTNode)decls[0]).getLength(), 1);
    }   

    public void testBugLabelWithMacro() throws Exception {
        Writer writer = new StringWriter();
        writer.write("#define UINT32 unsigned int\n"); //$NON-NLS-1$
        writer.write("#define HANDLE unsigned int**\n"); //$NON-NLS-1$
        writer.write("void foo()\n"); //$NON-NLS-1$
        writer.write("{\n"); //$NON-NLS-1$
        writer.write("UINT32 u;\n"); //$NON-NLS-1$
        writer.write("HANDLE h;\n"); //$NON-NLS-1$
        writer.write("}\n"); //$NON-NLS-1$
        writer.write("int foo2() {\n"); //$NON-NLS-1$
        writer.write("test:\n"); //$NON-NLS-1$
        writer.write("goto test;\n"); //$NON-NLS-1$
        writer.write("return foo();\n"); //$NON-NLS-1$
        writer.write("}\n"); //$NON-NLS-1$
        
        String code = writer.toString();
            
        int index = code.indexOf("HANDLE h"); //$NON-NLS-1$
        IASTNode node = parse( code, index, index + 6, true );
        assertTrue( node instanceof IASTName );
        assertTrue( ((IASTName)node).resolveBinding() instanceof IMacroBinding );
        assertEquals( ((IASTName)node).toString(), "HANDLE" ); //$NON-NLS-1$
        
        IASTName[] decls = getDeclarationOffTU((IASTName)node);
        assertEquals(decls.length, 1);
        assertEquals( decls[0].toString(), "HANDLE" ); //$NON-NLS-1$
        assertEquals( ((ASTNode)decls[0]).getOffset(), 36);
        assertEquals( ((ASTNode)decls[0]).getLength(), 6);
        
        index = code.indexOf("test;"); //$NON-NLS-1$
        node = parse( code, index, index + 4, true );
        assertTrue( node instanceof IASTName );
        assertTrue( ((IASTName)node).resolveBinding() instanceof ILabel );
        assertEquals( ((IASTName)node).toString(), "test" ); //$NON-NLS-1$
        
        decls = getDeclarationOffTU((IASTName)node);
        assertEquals(decls.length, 1);
        assertEquals( decls[0].toString(), "test" ); //$NON-NLS-1$
        assertEquals( ((ASTNode)decls[0]).getOffset(), 132);
        assertEquals( ((ASTNode)decls[0]).getLength(), 4);
    }   

    public void testBugMethodDef() throws Exception {
        Writer writer = new StringWriter();
        writer.write("class tetrahedron {\n"); //$NON-NLS-1$
        writer.write("private:\n"); //$NON-NLS-1$
        writer.write("int color;\n"); //$NON-NLS-1$
        writer.write("public:\n"); //$NON-NLS-1$
        writer.write("/* Methods */\n"); //$NON-NLS-1$
        writer.write("void setColor(int c) \n"); //$NON-NLS-1$
        writer.write("{color = c < 0 ? 0 : c;};\n"); //$NON-NLS-1$
        writer.write("void set();\n"); //$NON-NLS-1$
        writer.write("};\n"); //$NON-NLS-1$
        writer.write("void tetrahedron::set() {\n"); //$NON-NLS-1$
        writer.write("int color;\n"); //$NON-NLS-1$
        writer.write("setColor(color);\n"); //$NON-NLS-1$
        writer.write("}\n"); //$NON-NLS-1$        
        
        String code = writer.toString();
            
        int index = code.indexOf("setColor(color)"); //$NON-NLS-1$
        IASTNode node = parse( code, index, index + 8, true );
        assertTrue( node instanceof IASTName );
        assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPMethod );
        assertEquals( ((IASTName)node).toString(), "setColor" ); //$NON-NLS-1$
        
        IASTName[] decls = getDeclarationOffTU((IASTName)node);
        assertEquals(decls.length, 1);
        assertEquals( decls[0].toString(), "setColor" ); //$NON-NLS-1$
        assertEquals( ((ASTNode)decls[0]).getOffset(), 67);
        assertEquals( ((ASTNode)decls[0]).getLength(), 8);
        
        IASTName[] refs = getReferencesOffTU((IASTName)node);
        assertEquals(refs.length, 1);
        assertEquals( refs[0].toString(), "setColor" ); //$NON-NLS-1$
        assertEquals( ((ASTNode)refs[0]).getOffset(), 162);
        assertEquals( ((ASTNode)refs[0]).getLength(), 8);
    }   

    public void testBug86698A() throws Exception {
        Writer writer = new StringWriter();
        writer.write("struct C;\n"); //$NON-NLS-1$
        writer.write("void no_opt(C*);\n"); //$NON-NLS-1$
        writer.write("struct C {\n"); //$NON-NLS-1$
        writer.write("int c;\n"); //$NON-NLS-1$
        writer.write("C() : c(0) { no_opt(this); }\n"); //$NON-NLS-1$
        writer.write("};\n"); //$NON-NLS-1$
        
        String code = writer.toString();
            
        int index = code.indexOf("c(0)"); //$NON-NLS-1$
        IASTNode node = parse( code, index, index + 1, true );
        assertTrue( node instanceof IASTName );
        assertTrue( ((IASTName)node).resolveBinding() instanceof IVariable );
        assertEquals( ((IASTName)node).toString(), "c" ); //$NON-NLS-1$
        
        IASTName[] decls = getDeclarationOffTU((IASTName)node);
        assertEquals(decls.length, 1);
        assertEquals( decls[0].toString(), "c" ); //$NON-NLS-1$
        assertEquals( ((ASTNode)decls[0]).getOffset(), 42);
        assertEquals( ((ASTNode)decls[0]).getLength(), 1);
    }   
	
    public void testBug86698B() throws Exception {
        Writer writer = new StringWriter();
        writer.write("void foo() {\n"); //$NON-NLS-1$
		writer.write("int f(int);\n"); //$NON-NLS-1$
		writer.write("class C {\n"); //$NON-NLS-1$
		writer.write("int i;\n"); //$NON-NLS-1$
		writer.write("double d;\n"); //$NON-NLS-1$
		writer.write("public:\n"); //$NON-NLS-1$
		writer.write("C(int, double);\n"); //$NON-NLS-1$
		writer.write("};\n"); //$NON-NLS-1$
		writer.write("C::C(int ii, double id)\n"); //$NON-NLS-1$
		writer.write("try\n"); //$NON-NLS-1$
		writer.write(": i(f(ii)), d(id)\n"); //$NON-NLS-1$
		writer.write("{\n"); //$NON-NLS-1$
		writer.write("//		 constructor function body\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		writer.write("catch (...)\n"); //$NON-NLS-1$
		writer.write("{\n"); //$NON-NLS-1$
		writer.write("//		 handles exceptions thrown from the ctorinitializer\n"); //$NON-NLS-1$
		writer.write("//		 and from the constructor function body\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		writer.write("}\n"); //$NON-NLS-1$
		
		String code = writer.toString();
            
        int index = code.indexOf("i(f(ii)), d(id)"); //$NON-NLS-1$
        IASTNode node = parse( code, index, index + 1, true );
        assertTrue( node instanceof IASTName );
        assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPField );
        assertEquals( ((IASTName)node).toString(), "i" ); //$NON-NLS-1$
        
        IASTName[] decls = getDeclarationOffTU((IASTName)node);
        assertEquals(decls.length, 1);
        assertEquals( decls[0].toString(), "i" ); //$NON-NLS-1$
        assertEquals( ((ASTNode)decls[0]).getOffset(), 39);
        assertEquals( ((ASTNode)decls[0]).getLength(), 1);
    }   

	public void testBug64181() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("namespace Foo { // ** (A) **\n"); //$NON-NLS-1$
		buffer.append("int bar;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("namespace Foo { // ** (B) **\n"); //$NON-NLS-1$
		buffer.append("long fooboo;\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		buffer.append("int \n"); //$NON-NLS-1$
		buffer.append("main(int argc, char **argv) {\n"); //$NON-NLS-1$
		buffer.append("Foo::bar; // ** (C) **\n"); //$NON-NLS-1$
		buffer.append("}\n"); //$NON-NLS-1$
		
		String code = buffer.toString();
		int index = code.indexOf("Foo::bar;"); //$NON-NLS-1$
        IASTNode node = parse( code, index, index + 3, true );
		assertNotNull( node );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPNamespace );
		assertEquals( ((IASTName)node).toString(), "Foo" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 2);
		assertEquals( decls[0].toString(), "Foo" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 10);
		assertEquals( ((ASTNode)decls[0]).getLength(), 3);
		assertEquals( ((ASTNode)decls[1]).getOffset(), 50);
		assertEquals( ((ASTNode)decls[1]).getLength(), 3);
	}
	
	public void testBug80823() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("class MyEggImpl {}; // line A\n"); //$NON-NLS-1$
		buffer.append("#define MyChicken MyEggImpl\n"); //$NON-NLS-1$
		buffer.append("MyChicken c; // line C\n"); //$NON-NLS-1$

		String code = buffer.toString();
		int index = code.indexOf("MyChicken c;"); //$NON-NLS-1$
        IASTNode node = parse( code, index, index + 9, true );
		assertNotNull( node );
		assertTrue( node instanceof IASTName );
		assertTrue( ((IASTName)node).resolveBinding() instanceof IMacroBinding );
		assertEquals( ((IASTName)node).toString(), "MyChicken" ); //$NON-NLS-1$
		IASTName[] decls = getDeclarationOffTU((IASTName)node);
		assertEquals(decls.length, 1);
		assertEquals( decls[0].toString(), "MyChicken" ); //$NON-NLS-1$
		assertEquals( ((ASTNode)decls[0]).getOffset(), 38);
		assertEquals( ((ASTNode)decls[0]).getLength(), 9);
	}

    public void testBug86993() throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("#define _BEGIN_STD_C extern \"C\" {\n"); //$NON-NLS-1$
        buffer.append("#define _END_STD_C  }\n"); //$NON-NLS-1$
        buffer.append("_BEGIN_STD_C\n"); //$NON-NLS-1$
        buffer.append("char c; // selection on this fails because offset for \n"); //$NON-NLS-1$
        buffer.append("_END_STD_C\n"); //$NON-NLS-1$
        buffer.append("char foo() {\n"); //$NON-NLS-1$
        buffer.append("return c;   \n"); //$NON-NLS-1$
        buffer.append("}\n"); //$NON-NLS-1$            
        
        String code = buffer.toString();
        int index = code.indexOf("return c;"); //$NON-NLS-1$
        IASTNode node = parse( code, index + 7, index + 8, true );
        assertNotNull( node );
        assertTrue( node instanceof IASTName );
        assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPVariable );
        assertEquals( ((IASTName)node).toString(), "c" ); //$NON-NLS-1$
        IASTName[] decls = getDeclarationOffTU((IASTName)node);
        assertEquals(decls.length, 1);
        assertEquals( decls[0].toString(), "c" ); //$NON-NLS-1$
        assertEquals( ((ASTNode)decls[0]).getOffset(), 86);
        assertEquals( ((ASTNode)decls[0]).getLength(), 1);
        
        index = code.indexOf("char c"); //$NON-NLS-1$
        node = parse( code, index + 5, index + 6, true );
        assertNotNull( node );
        assertTrue( node instanceof IASTName );
        assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPVariable );
        IASTName[] refs = getReferencesOffTU((IASTName)node);
        assertEquals(refs.length, 1);
        assertEquals( refs[0].toString(), "c" ); //$NON-NLS-1$
        assertEquals( ((ASTNode)refs[0]).getOffset(), 168);
        assertEquals( ((ASTNode)decls[0]).getLength(), 1);
    }
    
    public void testBug92632() throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("namespace N{ \n"); //$NON-NLS-1$
        buffer.append("            template < class T > class AAA { T _t; };\n"); //$NON-NLS-1$
        buffer.append("}       \n"); //$NON-NLS-1$
        buffer.append("N::AAA<int> a;  \n"); //$NON-NLS-1$
        
        String code = buffer.toString();
        int index = code.indexOf("AAA<int>"); //$NON-NLS-1$
        IASTNode node = parse( code, index, index + 8, true );
        assertNotNull( node );
        assertTrue( node instanceof IASTName );
        assertTrue( ((IASTName)node).resolveBinding() instanceof ICPPTemplateInstance );
        assertEquals( ((IASTName)node).toString(), "AAA" ); //$NON-NLS-1$
        IASTName[] decls = getDeclarationOffTU((IASTName)node);
        assertEquals(decls.length, 1);
        assertEquals( decls[0].toString(), "AAA" ); //$NON-NLS-1$
        assertEquals( ((ASTNode)decls[0]).getOffset(), 53);
        assertEquals( ((ASTNode)decls[0]).getLength(), 3);
    }
    
}

