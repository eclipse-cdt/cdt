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
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTNamespaceDefinition;
import org.eclipse.cdt.internal.core.parser.ast.complete.ASTParameterDeclaration;

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
	    assertTrue( node instanceof IASTMethod );
	    assertEquals( ((IASTMethod)node).getName(), "f_SD" ); //$NON-NLS-1$
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
	    assertTrue( node instanceof IASTFunction );
	    assertEquals( ((IASTFunction)node).getName(), "f_SD" ); //$NON-NLS-1$
	}
	
	public void testBug72713() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "class Deck{ void initialize(); };   \n"); //$NON-NLS-1$
	    writer.write( "void Deck::initialize(){}           \n"); //$NON-NLS-1$
	    
	    String code = writer.toString();
	    int startIndex = code.indexOf( ":initialize" ); //$NON-NLS-1$
	    IASTNode node = parse( code, startIndex + 1, startIndex + 11 );
	    assertTrue( node instanceof IASTMethod );
	    assertFalse( ((IASTMethod)node).previouslyDeclared() );
	    assertEquals( ((IASTMethod) node).getNameOffset(), code.indexOf( " initialize();" ) + 1 ); //$NON-NLS-1$
	}
	
	public void testBug72712() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "class B{ public: B(); }; void f(){ B* b; b = new B(); }" ); //$NON-NLS-1$
	    
	    String code = writer.toString();
	    int startIndex = code.indexOf( "new B" ) + 4; //$NON-NLS-1$
	    
	    IASTNode node = parse( code, startIndex, startIndex + 1 );
	    assertTrue( node instanceof IASTMethod );
	    assertTrue( ((IASTMethod) node).isConstructor() );
	}
	
	public void testBug72712_2() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "class A {};                                        \n"); //$NON-NLS-1$
	    writer.write( "class B{ public: B( A* ); };                       \n"); //$NON-NLS-1$
	    writer.write( "void f(){ B* b; b = new B( (A*)0 ); }              \n"); //$NON-NLS-1$
	    
	    String code = writer.toString();
	    int startIndex = code.indexOf( "(A*)" ) + 1; //$NON-NLS-1$
	    
	    IASTNode node = parse( code, startIndex, startIndex + 1 );
	    assertTrue( node instanceof IASTClassSpecifier );
	    assertEquals( ((IASTClassSpecifier)node).getName(), "A" ); //$NON-NLS-1$
	}
	
	public void testBug72814() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "namespace N{                               \n"); //$NON-NLS-1$
	    writer.write( "   template < class T > class AAA { T _t };\n"); //$NON-NLS-1$
	    writer.write( "}                                          \n"); //$NON-NLS-1$
	    writer.write( "N::AAA<int> a;                             \n"); //$NON-NLS-1$
	    
	    String code = writer.toString();
	    int startIndex = code.indexOf( "AAA<int>" ); //$NON-NLS-1$
	    IASTNode node = parse( code, startIndex, startIndex + 3 );
	    
	    assertTrue( node instanceof IASTClassSpecifier );
	    assertEquals( ((IASTClassSpecifier)node).getName(), "AAA" ); //$NON-NLS-1$
	    
	    node = parse( code, startIndex, startIndex + 8 );
	    
	    assertTrue( node instanceof IASTClassSpecifier );
	    assertEquals( ((IASTClassSpecifier)node).getName(), "AAA" ); //$NON-NLS-1$
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
		assertTrue( node instanceof IASTField );
		IASTField rank = (IASTField) node;
		assertEquals( rank.getName(), "rank"); //$NON-NLS-1$
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
		assertTrue( node instanceof IASTFunction );
		IASTFunction rank1 = (IASTFunction) node;
		assertEquals( rank1.getName(), "rank"); //$NON-NLS-1$
		assertEquals( rank1.getNameOffset(), index );
		
		index = code.indexOf( "class Card{") + 6; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTClassSpecifier );
		IASTClassSpecifier card1 = (IASTClassSpecifier) node;
		assertEquals( card1.getName(), "Card"); //$NON-NLS-1$
		assertEquals( card1.getNameOffset(), index );
		
		index = code.indexOf( "Card( int rank );"); //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTMethod );
		IASTMethod card2 = (IASTMethod) node;
		assertEquals( card2.getName(), "Card"); //$NON-NLS-1$
		assertEquals( card2.getNameOffset(), index );
		
		index = code.indexOf( "Card( int rank );") + 10; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTParameterDeclaration );
		IASTParameterDeclaration rank2 = (IASTParameterDeclaration) node;
		assertEquals( rank2.getName(), "rank"); //$NON-NLS-1$
		assertEquals( rank2.getNameOffset(), index );

		index = code.indexOf( "int rank;") + 4; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTField );
		IASTField rank3 = (IASTField) node;
		assertEquals( rank3.getName(), "rank"); //$NON-NLS-1$
		assertEquals( rank3.getNameOffset(), index );
		
		index = code.indexOf( "int getRank();") + 4; //$NON-NLS-1$
		node = parse( code, index, index + 7 );
		assertTrue( node instanceof IASTMethod );
		IASTMethod getRank1 = (IASTMethod) node;
		assertEquals( getRank1.getName(), "getRank"); //$NON-NLS-1$
		assertEquals( getRank1.getNameOffset(), index );
		
		index = code.indexOf( "Card::Card( int rank )"); //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTClassSpecifier );
		IASTClassSpecifier card3 = (IASTClassSpecifier) node;
		assertEquals( card3.getName(), "Card"); //$NON-NLS-1$
		assertEquals( card3.getNameOffset(), card1.getNameOffset() );
		
		index = code.indexOf( "Card::Card( int rank )") + 6; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTMethod );
		IASTMethod card4 = (IASTMethod) node;
		assertEquals( card4.getName(), "Card"); //$NON-NLS-1$
		assertEquals( card4.getNameOffset(), card2.getNameOffset() );
		
		index = code.indexOf( "Card::Card( int rank )") + 16; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTParameterDeclaration );
		IASTParameterDeclaration rank4 = (IASTParameterDeclaration) node;
		assertEquals( rank4.getName(), "rank"); //$NON-NLS-1$
		assertEquals( rank4.getNameOffset(), index );
		
		index = code.indexOf( "this->rank = ::rank();") + 6; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTField );
		IASTField rank5 = (IASTField) node;
		assertEquals( rank5.getName(), "rank"); //$NON-NLS-1$
		assertEquals( rank5.getNameOffset(), rank3.getNameOffset() );
		
		index = code.indexOf( "this->rank = ::rank();") + 15; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTFunction );
		IASTFunction rank6 = (IASTFunction) node;
		assertEquals( rank6.getName(), "rank"); //$NON-NLS-1$
		assertEquals( rank6.getNameOffset(), rank1.getNameOffset() );
		
		index = code.indexOf( "this->rank = this->rank;") + 6; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTField );
		IASTField rank7 = (IASTField) node;
		assertEquals( rank7.getName(), "rank"); //$NON-NLS-1$
		assertEquals( rank7.getNameOffset(), rank3.getNameOffset() );
		
		index = code.indexOf( "this->rank = this->rank;") + 19; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTField );
		IASTField rank8 = (IASTField) node;
		assertEquals( rank8.getName(), "rank"); //$NON-NLS-1$
		assertEquals( rank8.getNameOffset(), rank3.getNameOffset() );
		
		index = code.indexOf( "this->rank = rank;") + 6; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTField );
		IASTField rank9 = (IASTField) node;
		assertEquals( rank9.getName(), "rank"); //$NON-NLS-1$
		assertEquals( rank9.getNameOffset(), rank3.getNameOffset() );
		
		index = code.indexOf( "this->rank = rank;") + 13; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTParameterDeclaration );
		IASTParameterDeclaration rank10 = (IASTParameterDeclaration) node;
		assertEquals( rank10.getName(), "rank"); //$NON-NLS-1$
		assertEquals( rank10.getNameOffset(), rank4.getNameOffset() );
		
		index = code.indexOf( "this->rank = Card::rank;") + 6; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTField );
		IASTField rank11 = (IASTField) node;
		assertEquals( rank11.getName(), "rank"); //$NON-NLS-1$
		assertEquals( rank11.getNameOffset(), rank3.getNameOffset() );
		
		index = code.indexOf( "this->rank = Card::rank;") + 19; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTField );
		IASTField rank12 = (IASTField) node;
		assertEquals( rank12.getName(), "rank"); //$NON-NLS-1$
		assertEquals( rank12.getNameOffset(), rank3.getNameOffset() );
		
		index = code.indexOf( "this->rank = getRank();") + 6; //$NON-NLS-1$
		node = parse( code, index, index + 4 );
		assertTrue( node instanceof IASTField );
		IASTField rank13 = (IASTField) node;
		assertEquals( rank13.getName(), "rank"); //$NON-NLS-1$
		assertEquals( rank13.getNameOffset(), rank3.getNameOffset() );
		
		index = code.indexOf( "this->rank = getRank();") + 13; //$NON-NLS-1$
		node = parse( code, index, index + 7 );
		assertTrue( node instanceof IASTMethod );
		IASTMethod getRank2 = (IASTMethod) node;
		assertEquals( getRank2.getName(), "getRank"); //$NON-NLS-1$
		assertEquals( getRank2.getNameOffset(), getRank1.getNameOffset() );
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
		assertTrue( node instanceof ASTNamespaceDefinition );
		ASTNamespaceDefinition n = (ASTNamespaceDefinition) node;
		assertEquals( n.getName(), "N"); //$NON-NLS-1$
		assertEquals( n.getNameOffset(), 10 );
		assertEquals( n.getStartingLine(), 1 );
	}

	public void testBug78435() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int itself;          //A\n"); //$NON-NLS-1$ 
		writer.write("void f(int itself){} //B\n"); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "void f(int itself){}") + 11; //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 6 );
		assertTrue( node instanceof ASTParameterDeclaration );
		ASTParameterDeclaration n = (ASTParameterDeclaration) node;
		assertEquals( n.getName(), "itself"); //$NON-NLS-1$
		assertEquals( n.getNameOffset(), 36 );
		assertEquals( n.getStartingLine(), 2 );
	}

	public void testBug78231A() throws Exception {
		Writer writer = new StringWriter();
		writer.write("struct Base {\n"); //$NON-NLS-1$
		writer.write("int Data; // 1\n"); //$NON-NLS-1$
		writer.write("struct Data; // 2\n};\n"); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf("struct Data;") + 7; //$NON-NLS-1$
		IASTNode node = parse( code, index, index + 4 );
		assertTrue(node instanceof IASTOffsetableNamedElement);
		IASTOffsetableNamedElement n = (IASTOffsetableNamedElement)node;
		assertEquals(n.getName(), "Data"); //$NON-NLS-1$
		assertEquals(n.getNameOffset(), 36);
		assertEquals(n.getStartingLine(), 3);
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
		assertTrue(node instanceof IASTOffsetableNamedElement);
		IASTOffsetableNamedElement n = (IASTOffsetableNamedElement)node;
		assertEquals(n.getName(), "Data"); //$NON-NLS-1$
		assertEquals(n.getNameOffset(), 46);
		assertEquals(n.getStartingLine(), 4);
	}
}

