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
package org.eclipse.cdt.core.parser.tests;

import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.parser.ast.ASTClassKind;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;

/**
 * @author jcamelon
 */
public class SelectionParseTest extends SelectionParseBaseTest {

	public void testBaseCase_VariableReference() throws Exception
	{
		String code = "void f() { int x; x=3; }"; //$NON-NLS-1$
		int offset1 = code.indexOf( "x=" ); //$NON-NLS-1$
		int offset2 = code.indexOf( '=');
		IASTNode node = parse( code, offset1, offset2 );
		assertTrue( node instanceof IASTVariable );
		assertEquals( ((IASTVariable)node).getName(), "x" ); //$NON-NLS-1$
	}

	public void testBaseCase_FunctionReference() throws Exception
	{
		String code = "int x(){x( );}"; //$NON-NLS-1$
		int offset1 = code.indexOf( "x( " ); //$NON-NLS-1$
		int offset2 = code.indexOf( "( )"); //$NON-NLS-1$
		IASTNode node = parse( code, offset1, offset2 );
		assertTrue( node instanceof IASTFunction );
		assertEquals( ((IASTFunction)node).getName(), "x" ); //$NON-NLS-1$
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
		assertTrue( node instanceof IASTFunction );
		assertEquals( ((IASTFunction)node).getName(), "x" ); //$NON-NLS-1$
	}
	
	public void testBaseCase_FunctionDeclaration2() throws Exception
	{
		String code = "int printf( const char *, ... ); "; //$NON-NLS-1$
		int offset1 = code.indexOf( "printf" ); //$NON-NLS-1$
		int offset2 = code.indexOf( "( const"); //$NON-NLS-1$
		IASTNode node = parse( code, offset1, offset2 );
		assertTrue( node instanceof IASTFunction );
		assertEquals( ((IASTFunction)node).getName(), "printf" );		 //$NON-NLS-1$
	}

	public void testBaseCase_VariableDeclaration() throws Exception
	{
		String code = "int x = 3;"; //$NON-NLS-1$
		int offset1 = code.indexOf( "x" ); //$NON-NLS-1$
		int offset2 = code.indexOf( " ="); //$NON-NLS-1$
		IASTNode node = parse( code, offset1, offset2 );
		assertNotNull( node );
		assertTrue( node instanceof IASTVariable );
		assertEquals( ((IASTVariable)node).getName(), "x" ); //$NON-NLS-1$
	}
	
	public void testBaseCase_Parameter() throws Exception
	{
		String code = "int main( int argc ) { int x = argc; }"; //$NON-NLS-1$
		int offset1 = code.indexOf( "argc;" ); //$NON-NLS-1$
		int offset2 = code.indexOf( ";" ); //$NON-NLS-1$
		IASTNode node = parse( code, offset1, offset2 );
		assertNotNull( node );
		assertTrue( node instanceof IASTParameterDeclaration );
		assertEquals( ((IASTParameterDeclaration)node).getName(), "argc" );		 //$NON-NLS-1$
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
			assertTrue( node instanceof IASTMethod );
			IASTMethod method = (IASTMethod) node;
			assertEquals( method.getName(), "playHorn"); //$NON-NLS-1$
			IASTClassSpecifier gonzo = method.getOwnerClassSpecifier();
			assertEquals( gonzo.getName(), "Gonzo"); //$NON-NLS-1$
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
		assertTrue( node instanceof IASTMethod );
		IASTMethod constructor = ((IASTMethod)node);
		assertEquals( constructor.getName(), "Gonzo" ); //$NON-NLS-1$
		assertTrue( constructor.isConstructor() );
		offset = code.indexOf( "~Gonzo"); //$NON-NLS-1$
		node = parse( code, offset, offset + 6 );
		assertNotNull( node );
		assertTrue( node instanceof IASTMethod );
		IASTMethod destructor = ((IASTMethod)node);
		assertEquals( destructor.getName(), "~Gonzo" ); //$NON-NLS-1$
		assertTrue( destructor.isDestructor() );
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
		assertTrue( node instanceof IASTNamespaceDefinition );
		IASTNamespaceDefinition namespace = (IASTNamespaceDefinition) node;
		assertEquals( namespace.getName(), "Muppets"); //$NON-NLS-1$
		assertEquals( namespace.getStartingLine(), 1 );
		
		index = code.indexOf( "e Muppets") + 2; //$NON-NLS-1$
		node = parse( code, index, index + 7 );
		assertTrue( node instanceof IASTNamespaceDefinition );
		namespace = (IASTNamespaceDefinition) node;
		assertEquals( namespace.getName(), "Muppets"); //$NON-NLS-1$
		assertEquals( namespace.getStartingLine(), 1 );

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
		assertTrue( node instanceof IASTClassSpecifier );
		IASTClassSpecifier foo = (IASTClassSpecifier) node;
		assertEquals( foo.getName(), "Foo"); //$NON-NLS-1$
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
			assertTrue( node instanceof IASTMethod );
			IASTMethod method = (IASTMethod) node;
			switch( i )
			{
				case 0:
				case 1: 
					assertTrue( method.isConstructor() );
					assertFalse( method.isDestructor() );
					break;
				default: 
					assertFalse( method.isConstructor() );
					assertTrue( method.isDestructor() );
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
		assertTrue( node instanceof IASTMethod );
		assertEquals( ((IASTMethod)node).getName(), "getAnswer" ); //$NON-NLS-1$
	}
	
	public void testConstructorDefinition() throws Exception
	{
		String code = "class ABC { public: ABC(); }; ABC::ABC(){}"; //$NON-NLS-1$
		int startIndex = code.indexOf( "::ABC") + 2; //$NON-NLS-1$
		IASTNode node = parse( code, startIndex, startIndex + 3 );
		assertTrue( node instanceof IASTMethod );
		IASTMethod constructor = (IASTMethod) node;
		assertTrue( constructor.isConstructor() );
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
		assertTrue( node instanceof IASTField );
		assertEquals( ((IASTField)node).getName(), "stInt" ); //$NON-NLS-1$
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
		
		assertTrue( node instanceof IASTFunction );
		assertEquals( ((IASTFunction)node).getName(), "fprintf" ); //$NON-NLS-1$	    
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
		assertTrue( node instanceof IASTClassSpecifier );
		IASTClassSpecifier classSpecifier = (IASTClassSpecifier) node;
		assertEquals( classSpecifier.getClassKind(), ASTClassKind.UNION );
		assertEquals( classSpecifier.getName(), "Squaw"); //$NON-NLS-1$
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
		assertTrue( node instanceof IASTVariable );
		assertEquals( ((IASTVariable)node).getName(), "FOUND_ME" ); //$NON-NLS-1$
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
	    assertTrue( node instanceof IASTMethod );
	    assertEquals( ((IASTMethod)node).getName(), "ABC" ); //$NON-NLS-1$
	    assertTrue( ((IASTMethod)node).isConstructor() );
	}
}
