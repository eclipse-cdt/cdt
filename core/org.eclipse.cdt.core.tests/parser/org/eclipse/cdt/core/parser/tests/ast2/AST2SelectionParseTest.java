/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.parser.tests.ast2;

import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.dom.ast.IASTArrayModifier;
import org.eclipse.cdt.core.dom.ast.IASTDeclarator;
import org.eclipse.cdt.core.dom.ast.IASTIdExpression;
import org.eclipse.cdt.core.dom.ast.IASTInitializerExpression;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNamedTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPointer;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTTypeId;
import org.eclipse.cdt.core.dom.ast.ICompositeType;
import org.eclipse.cdt.core.dom.ast.IEnumeration;
import org.eclipse.cdt.core.dom.ast.IField;
import org.eclipse.cdt.core.dom.ast.IFunction;
import org.eclipse.cdt.core.dom.ast.IParameter;
import org.eclipse.cdt.core.dom.ast.ITypedef;
import org.eclipse.cdt.core.dom.ast.IVariable;
import org.eclipse.cdt.core.dom.ast.c.ICASTPointer;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPConstructor;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPField;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPNamespace;
import org.eclipse.cdt.core.parser.ParserLanguage;

/**
 * @author dsteffle
 */
public class AST2SelectionParseTest extends AST2SelectionParseBaseTest {
	public void testBaseCase_VariableReference() throws Exception
	{
		String code = "void f() { int x; x=3; }"; //$NON-NLS-1$
		int offset1 = code.indexOf( "x=" ); //$NON-NLS-1$
		int length = "x".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, offset1, length );
		assertNotNull(node);
		assertTrue( node instanceof IASTIdExpression );
		assertEquals(((IASTIdExpression)node).getName().toString(), "x"); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, offset1, length );
		assertNotNull(node);
		assertTrue( node instanceof IASTIdExpression );
		assertEquals(((IASTIdExpression)node).getName().toString(), "x"); //$NON-NLS-1$
		IASTName name = ((IASTIdExpression)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IVariable);
		assertEquals(((IVariable)name.resolveBinding()).getName(), "x"); //$NON-NLS-1$
	}

	public void testBaseCase_FunctionReference() throws Exception
	{
		String code = "int x(){x( );}"; //$NON-NLS-1$
		int offset1 = code.indexOf( "x( " ); //$NON-NLS-1$
		int length = "x".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, offset1, length );
		assertNotNull(node);
		assertTrue( node instanceof IASTIdExpression );
		assertEquals(((IASTIdExpression)node).getName().toString(), "x"); //$NON-NLS-1$
		IASTName name = ((IASTIdExpression)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IFunction);
		assertEquals(((IFunction)name.resolveBinding()).getName(), "x"); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, offset1, length );
		assertNotNull(node);
		assertTrue( node instanceof IASTIdExpression );
		assertEquals(((IASTIdExpression)node).getName().toString(), "x"); //$NON-NLS-1$
		name = ((IASTIdExpression)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IFunction);
		assertEquals(((IFunction)name.resolveBinding()).getName(), "x"); //$NON-NLS-1$
	}
	
	public void testBaseCase_Error() throws Exception
	{
		String code = "int x() { y( ) }"; //$NON-NLS-1$
		int offset1 = code.indexOf( "y( " ); //$NON-NLS-1$
		int length = "y".length(); //$NON-NLS-1$
		assertNull( parse( code, ParserLanguage.C, offset1, length, false ));
		assertNull( parse( code, ParserLanguage.CPP, offset1, length, false ));
	}
	
	public void testBaseCase_FunctionDeclaration() throws Exception
	{
		String code = "int x(); x( );"; //$NON-NLS-1$
		int offset1 = code.indexOf( "x()" ); //$NON-NLS-1$
		int length = "x".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, offset1, length );
		assertNotNull(node);
		assertTrue( node instanceof IASTName );
		assertEquals(node.toString(), "x"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IFunction);
		assertEquals(((IFunction)name.resolveBinding()).getName(), "x"); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, offset1, length );
		assertNotNull(node);
		assertTrue( node instanceof IASTName );
		assertEquals(node.toString(), "x"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IFunction);
		assertEquals(((IFunction)name.resolveBinding()).getName(), "x"); //$NON-NLS-1$
	}
	
	public void testBaseCase_FunctionDeclaration2() throws Exception
	{
		String code = "int printf( const char *, ... ); "; //$NON-NLS-1$
		int offset1 = code.indexOf( "printf" ); //$NON-NLS-1$
		int length = "printf".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, offset1, length );
		assertNotNull(node);
		assertTrue( node instanceof IASTName );
		assertEquals(node.toString(), "printf"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IFunction);
		assertEquals(((IFunction)name.resolveBinding()).getName(), "printf"); //$NON-NLS-1$		
		node = parse( code, ParserLanguage.CPP, offset1, length );
		assertNotNull(node);
		assertTrue( node instanceof IASTName );
		assertEquals(node.toString(), "printf"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IFunction);
		assertEquals(((IFunction)name.resolveBinding()).getName(), "printf"); //$NON-NLS-1$
	}

	public void testBaseCase_VariableDeclaration() throws Exception
	{
		String code = "int x = 3;"; //$NON-NLS-1$
		int offset1 = code.indexOf( "x" ); //$NON-NLS-1$
		int length = "x".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, offset1, length );
		assertNotNull(node);
		assertTrue( node instanceof IASTName );
		assertEquals(node.toString(), "x"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IVariable);
		assertEquals(((IVariable)name.resolveBinding()).getName(), "x"); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, offset1, length );
		assertNotNull(node);
		assertTrue( node instanceof IASTName );
		assertEquals(node.toString(), "x"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IVariable);
		assertEquals(((IVariable)name.resolveBinding()).getName(), "x"); //$NON-NLS-1$
	}
	
	public void testBaseCase_Parameter() throws Exception
	{
		String code = "int main( int argc ) { int x = argc; }"; //$NON-NLS-1$
		int offset1 = code.indexOf( "argc;" ); //$NON-NLS-1$
		int length = "argc".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, offset1, length );
		assertNotNull(node);
		assertTrue( node instanceof IASTInitializerExpression );
		assertEquals( ((IASTIdExpression)((IASTInitializerExpression)node).getExpression()).getName().toString(), "argc" ); //$NON-NLS-1$
		IASTName name = ((IASTIdExpression)((IASTInitializerExpression)node).getExpression()).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IParameter);
		assertEquals(((IParameter)name.resolveBinding()).getName(), "argc"); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, offset1, length );
		assertNotNull(node);
		assertTrue( node instanceof IASTInitializerExpression );
		assertEquals( ((IASTIdExpression)((IASTInitializerExpression)node).getExpression()).getName().toString(), "argc" ); //$NON-NLS-1$
		name = ((IASTIdExpression)((IASTInitializerExpression)node).getExpression()).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IParameter);
		assertEquals(((IParameter)name.resolveBinding()).getName(), "argc"); //$NON-NLS-1$
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
			stop = 8;
			IASTNode node = parse( code, ParserLanguage.CPP, start, stop );
			assertNotNull(node);
			assertTrue( node instanceof IASTName );
			assertEquals( node.toString(), "playHorn" ); //$NON-NLS-1$
			IASTName name = (IASTName)node;
			assertNotNull(name.resolveBinding());
			assertTrue(name.resolveBinding() instanceof ICPPMethod);
			assertEquals(((ICPPMethod)name.resolveBinding()).getName(), "playHorn"); //$NON-NLS-1$
		}
	}
	
	public void testConstructorDestructorDeclaration() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "class Gonzo { Gonzo(); ~Gonzo(); };"); //$NON-NLS-1$
		String code = writer.toString();
		int offset = code.indexOf( " Gonzo()") + 1; //$NON-NLS-1$
		int length = "Gonzo".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, offset, length );
		assertNotNull(node);
		assertTrue( node instanceof IASTName );
		assertEquals(node.toString(), "Gonzo"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPConstructor);
		assertEquals(((ICPPConstructor)name.resolveBinding()).getName(), "Gonzo"); //$NON-NLS-1$
		offset = code.indexOf( " ~Gonzo") + 1; //$NON-NLS-1$
		length = "~Gonzo".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, offset, length );
		assertNotNull(node);
		assertTrue( node instanceof IASTName );
		assertEquals(node.toString(), "~Gonzo"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPMethod);
		assertEquals(((ICPPMethod)name.resolveBinding()).getName(), "~Gonzo"); //$NON-NLS-1$

	}	
	
	public void testBug60264() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write( "namespace Muppets { int i;	}\n" ); //$NON-NLS-1$
		writer.write( "int	main(int argc, char **argv) {	Muppets::i = 1; }\n" ); //$NON-NLS-1$
		String code = writer.toString();
		int index = code.indexOf( "Muppets::"); //$NON-NLS-1$
		int length = "Muppets".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "Muppets"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPNamespace);
		assertEquals(((ICPPNamespace)name.resolveBinding()).getName(), "Muppets"); //$NON-NLS-1$
		index = code.indexOf( "e Muppets") + 2; //$NON-NLS-1$
		length = "Muppets".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "Muppets"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPNamespace);
		assertEquals(((ICPPNamespace)name.resolveBinding()).getName(), "Muppets"); //$NON-NLS-1$

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
		int length = "Foo".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "Foo"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPClassType);
		assertEquals(((ICPPClassType)name.resolveBinding()).getName(), "Foo"); //$NON-NLS-1$
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
			int startOffset = 0, length = 0;
			switch( i )
			{
				case 0:
					startOffset = code.indexOf( "new Gonzo()") + 4; //$NON-NLS-1$
					length = 5;
					break;
				case 1:
					startOffset = code.indexOf( "new Gonzo( ") + 4; //$NON-NLS-1$
					length = 5;
					break;
				default:
					startOffset = code.indexOf( "->~") + 2; //$NON-NLS-1$
					length = 6;
			}
			IASTNode node = parse( code, ParserLanguage.CPP, startOffset, length );
			assertNotNull(node);
			IASTName name = null;
			switch( i )
			{
				case 0:
				case 1: 
					assertTrue(node instanceof IASTTypeId);
					assertEquals(((IASTNamedTypeSpecifier)((IASTTypeId)node).getDeclSpecifier()).getName().toString(), "Gonzo"); //$NON-NLS-1$
					name = ((IASTNamedTypeSpecifier)((IASTTypeId)node).getDeclSpecifier()).getName();
					assertNotNull(name.resolveBinding());
					assertTrue(name.resolveBinding() instanceof ICPPConstructor);
					assertEquals(((ICPPConstructor)name.resolveBinding()).getName(), "Gonzo"); //$NON-NLS-1$
					break;
				default: 
					assertTrue(node instanceof IASTName);
					assertEquals(node.toString(), "~Gonzo"); //$NON-NLS-1$
					name = (IASTName)node;
					assertNotNull(name.resolveBinding());
					assertTrue(name.resolveBinding() instanceof ICPPMethod);
					assertEquals(((ICPPMethod)name.resolveBinding()).getName(), "~Gonzo"); //$NON-NLS-1$
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
		int length = "getAnswer".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "getAnswer"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPMethod);
		assertEquals(((ICPPMethod)name.resolveBinding()).getName(), "getAnswer"); //$NON-NLS-1$
	}
	
	public void testConstructorDefinition() throws Exception
	{
		String code = "class ABC { public: ABC(); }; ABC::ABC(){}"; //$NON-NLS-1$
		int startIndex = code.indexOf( "::ABC") + 2; //$NON-NLS-1$
		int length = "ABC".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertEquals(node.toString(), "ABC"); //$NON-NLS-1$
		assertTrue(name.resolveBinding() instanceof ICPPConstructor);
		assertEquals(((ICPPConstructor)name.resolveBinding()).getName(), "ABC"); //$NON-NLS-1$
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
		int length = "foo".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTIdExpression);
		assertEquals(((IASTIdExpression)node).getName().toString(), "foo"); //$NON-NLS-1$
		IASTName name = ((IASTIdExpression)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IFunction);
		assertEquals(((IFunction)name.resolveBinding()).getName(), "foo"); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTIdExpression);
		assertEquals(((IASTIdExpression)node).getName().toString(), "foo"); //$NON-NLS-1$
		name = ((IASTIdExpression)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IFunction);
		assertEquals(((IFunction)name.resolveBinding()).getName(), "foo"); //$NON-NLS-1$
	}
	
	public void testBug66744() throws Exception
	{
		Writer writerCPP = new StringWriter();
		writerCPP.write( "enum EColours { RED, GREEN, BLUE };      \n" ); //$NON-NLS-1$
		writerCPP.write( "void foo() {  EColours color = GREEN; }  \n" ); //$NON-NLS-1$
		
		Writer writerC = new StringWriter();
		writerC.write( "enum EColours { RED, GREEN, BLUE };      \n" ); //$NON-NLS-1$
		writerC.write( "void foo() { enum EColours color = GREEN; }  \n" ); //$NON-NLS-1$
		
		String codeCPP = writerCPP.toString();
		String codeC = writerC.toString();
		int startIndex = codeC.indexOf( "EColours color"); //$NON-NLS-1$
		int length = "EColours".length(); //$NON-NLS-1$
		IASTNode node = parse( codeC, ParserLanguage.C, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "EColours"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IEnumeration);
		assertEquals(((IEnumeration)name.resolveBinding()).getName(), "EColours"); //$NON-NLS-1$
		startIndex = codeCPP.indexOf( "EColours color"); //$NON-NLS-1$
		node = parse( codeCPP, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTNamedTypeSpecifier);
		assertEquals(((IASTNamedTypeSpecifier)node).getName().toString(), "EColours"); //$NON-NLS-1$
		name = ((IASTNamedTypeSpecifier)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IEnumeration);
		assertEquals(((IEnumeration)name.resolveBinding()).getName(), "EColours"); //$NON-NLS-1$
	}
	

	
	public void testBug68527() throws Exception
	{
		Writer writer = new StringWriter();
		writer.write("struct X;\n"); //$NON-NLS-1$
		writer.write("struct X anA;"); //$NON-NLS-1$
		String code = writer.toString();
		int startIndex = code.indexOf( "X anA"); //$NON-NLS-1$
		int length = "X".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "X"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICompositeType);
		assertEquals(((ICompositeType)name.resolveBinding()).getName(), "X"); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "X"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICompositeType);
		assertEquals(((ICompositeType)name.resolveBinding()).getName(), "X"); //$NON-NLS-1$
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
		int length = "static_function".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTIdExpression);
		assertEquals(((IASTIdExpression)node).getName().toString(), "static_function"); //$NON-NLS-1$
		IASTName name = ((IASTIdExpression)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IFunction);
		assertEquals(((IFunction)name.resolveBinding()).getName(), "static_function"); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTIdExpression);
		assertEquals(((IASTIdExpression)node).getName().toString(), "static_function"); //$NON-NLS-1$
		name = ((IASTIdExpression)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IFunction);
		assertEquals(((IFunction)name.resolveBinding()).getName(), "static_function"); //$NON-NLS-1$
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
		int length = "stInt".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "stInt"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPField);
		assertEquals(((ICPPField)name.resolveBinding()).getName(), "stInt"); //$NON-NLS-1$
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
		int length = "fprintf".length(); //$NON-NLS-1$
		
		IASTNode node = parse( code, ParserLanguage.C, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTIdExpression);
		assertEquals(((IASTIdExpression)node).getName().toString(), "fprintf"); //$NON-NLS-1$
		IASTName name = ((IASTIdExpression)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IFunction);
		assertEquals(((IFunction)name.resolveBinding()).getName(), "fprintf"); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTIdExpression);
		assertEquals(((IASTIdExpression)node).getName().toString(), "fprintf"); //$NON-NLS-1$
		name = ((IASTIdExpression)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IFunction);
		assertEquals(((IFunction)name.resolveBinding()).getName(), "fprintf"); //$NON-NLS-1$
	}
	
	public void testBug72818() throws Exception
	{
		Writer writerCPP = new StringWriter();
		writerCPP.write( "union Squaw	{	int x;	double u; };\n" ); //$NON-NLS-1$
		writerCPP.write( "int	main(int argc, char **argv) {\n" ); //$NON-NLS-1$
		writerCPP.write( "return sizeof( Squaw );\n" ); //$NON-NLS-1$
		writerCPP.write( "}\n" ); //$NON-NLS-1$
		
		Writer writerC = new StringWriter();
		writerC.write( "union Squaw	{	int x;	double u; };\n" ); //$NON-NLS-1$
		writerC.write( "int	main(int argc, char **argv) {\n" ); //$NON-NLS-1$
		writerC.write( "return sizeof( union Squaw );\n" ); //$NON-NLS-1$
		writerC.write( "}\n" ); //$NON-NLS-1$
		
		String codeC = writerC.toString();
		String codeCPP = writerCPP.toString();
		int startIndex = codeC.indexOf( "sizeof( union ") + "sizeof( union ".length();  //$NON-NLS-1$ //$NON-NLS-2$
		int length = "Squaw".length(); //$NON-NLS-1$
		IASTNode node = parse( codeC, ParserLanguage.C, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "Squaw"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICompositeType);
		assertEquals(((ICompositeType)name.resolveBinding()).getName(), "Squaw"); //$NON-NLS-1$
		startIndex = codeCPP.indexOf( "sizeof( ") + "sizeof( ".length();  //$NON-NLS-1$ //$NON-NLS-2$
		node = parse( codeCPP, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTTypeId);
		assertEquals(((IASTNamedTypeSpecifier)((IASTTypeId)node).getDeclSpecifier()).getName().toString(), "Squaw"); //$NON-NLS-1$
		name = ((IASTNamedTypeSpecifier)((IASTTypeId)node).getDeclSpecifier()).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPClassType);
		assertEquals(((ICPPClassType)name.resolveBinding()).getName(), "Squaw"); //$NON-NLS-1$
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
		int length = "FOUND_ME".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTIdExpression);
		assertEquals(((IASTIdExpression)node).getName().toString(), "FOUND_ME"); //$NON-NLS-1$
		IASTName name = ((IASTIdExpression)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IVariable);
		assertEquals(((IVariable)name.resolveBinding()).getName(), "FOUND_ME"); //$NON-NLS-1$
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
		int length = "ABC".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "ABC"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPConstructor);
		assertEquals(((ICPPConstructor)name.resolveBinding()).getName(), "ABC"); //$NON-NLS-1$
	}
	
	public void testBug72372() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write("namespace B {                                   \n"); //$NON-NLS-1$
	    writer.write("   class SD_02 { void f_SD(); };                \n"); //$NON-NLS-1$
	    writer.write("}                                               \n"); //$NON-NLS-1$
	    writer.write("using namespace B;                              \n"); //$NON-NLS-1$
	    writer.write("void SD_02::f_SD(){}                            \n"); //$NON-NLS-1$
	    
	    String code = writer.toString();
	    int startIndex = code.indexOf( ":f_SD" ) + 1; //$NON-NLS-1$
		int length = "f_SD".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "f_SD"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPMethod);
		assertEquals(((ICPPMethod)name.resolveBinding()).getName(), "f_SD"); //$NON-NLS-1$
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
	    int startIndex = code.indexOf( ":f_SD" ) + 1; //$NON-NLS-1$
		int length = "f_SD".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "f_SD"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IFunction);
		assertEquals(((IFunction)name.resolveBinding()).getName(), "f_SD"); //$NON-NLS-1$
	}
	
	public void testBug72713() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "class Deck{ void initialize(); };   \n"); //$NON-NLS-1$
	    writer.write( "void Deck::initialize(){}           \n"); //$NON-NLS-1$
	    
	    String code = writer.toString();
	    int startIndex = code.indexOf( ":initialize" ) + 1; //$NON-NLS-1$
	    int length = "initialize".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "initialize"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPMethod);
		assertEquals(((ICPPMethod)name.resolveBinding()).getName(), "initialize"); //$NON-NLS-1$
	}
	
	public void testBug72712() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "class B{ public: B(); }; void f(){ B* b; b = new B(); }" ); //$NON-NLS-1$
	    
	    String code = writer.toString();
	    int startIndex = code.indexOf( "new B" ) + 4; //$NON-NLS-1$
	    int length = "B".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTTypeId);
		assertEquals(((IASTNamedTypeSpecifier)((IASTTypeId)node).getDeclSpecifier()).getName().toString(), "B"); //$NON-NLS-1$
		IASTName name = ((IASTNamedTypeSpecifier)((IASTTypeId)node).getDeclSpecifier()).getName();
		assertNotNull(name.resolveBinding());
	    assertTrue(name.resolveBinding() instanceof ICPPConstructor);
		assertEquals(((ICPPConstructor)name.resolveBinding()).getName(), "B"); //$NON-NLS-1$
	}
	
	public void testBug72712_2() throws Exception{
	    Writer writer = new StringWriter();
	    writer.write( "class A {};                                        \n"); //$NON-NLS-1$
	    writer.write( "class B{ public: B( A* ); };                       \n"); //$NON-NLS-1$
	    writer.write( "void f(){ B* b; b = new B( (A*)0 ); }              \n"); //$NON-NLS-1$
	    
	    String code = writer.toString();
	    int startIndex = code.indexOf( "(A*)" ) + 1; //$NON-NLS-1$
	    int length = "A".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, startIndex, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTNamedTypeSpecifier);
		assertEquals(((IASTNamedTypeSpecifier)node).getName().toString(), "A"); //$NON-NLS-1$
		IASTName name = ((IASTNamedTypeSpecifier)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPClassType);
		assertEquals(((ICPPClassType)name.resolveBinding()).getName(), "A"); //$NON-NLS-1$
	}
	
	// TODO no template support yet for new AST?
//	public void testBug72814() throws Exception{
//	    Writer writer = new StringWriter();
//	    writer.write( "namespace N{                               \n"); //$NON-NLS-1$
//	    writer.write( "   template < class T > class AAA { T _t };\n"); //$NON-NLS-1$
//	    writer.write( "}                                          \n"); //$NON-NLS-1$
//	    writer.write( "N::AAA<int> a;                             \n"); //$NON-NLS-1$
//	    
//	    String code = writer.toString();
//	    int startIndex = code.indexOf( "AAA<int>" ); //$NON-NLS-1$
//		int length = "AAA".length(); //$NON-NLS-1$
//		IASTNode node = parse( code, ParserLanguage.CPP, startIndex, length );
//		assertNotNull(node);
//	    
////	    assertTrue( node instanceof IASTClassSpecifier );
////	    assertEquals( ((IASTClassSpecifier)node).getName(), "AAA" ); //$NON-NLS-1$
//	    
//		length = "AAA<int>".length(); //$NON-NLS-1$ 
//		node = parse( code, ParserLanguage.CPP, startIndex, length );
//		assertNotNull(node);
//	    
////	    assertTrue( node instanceof IASTClassSpecifier );
////	    assertEquals( ((IASTClassSpecifier)node).getName(), "AAA" ); //$NON-NLS-1$
//	}
	
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
		int length = "rank".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "rank"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPField);
		assertEquals(((ICPPField)name.resolveBinding()).getName(), "rank"); //$NON-NLS-1$
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
		int length = "rank".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "rank"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IFunction);
		assertEquals(((IFunction)name.resolveBinding()).getName(), "rank"); //$NON-NLS-1$
		
		index = code.indexOf( "class Card{") + 6; //$NON-NLS-1$
		length = "Card".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "Card"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPClassType);
		assertEquals(((ICPPClassType)name.resolveBinding()).getName(), "Card"); //$NON-NLS-1$
		
		index = code.indexOf( "Card( int rank );"); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "Card"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPConstructor);
		assertEquals(((ICPPConstructor)name.resolveBinding()).getName(), "Card"); //$NON-NLS-1$
		
		index = code.indexOf( "Card( int rank );") + 10; //$NON-NLS-1$
		length = "rank".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTDeclarator);
		assertEquals(((IASTDeclarator)node).getName().toString(), "rank"); //$NON-NLS-1$
		name = ((IASTDeclarator)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IVariable);
		assertEquals(((IVariable)name.resolveBinding()).getName(), "rank"); //$NON-NLS-1$

		index = code.indexOf( "int rank;") + 4; //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTDeclarator);
		assertEquals(((IASTDeclarator)node).getName().toString(), "rank"); //$NON-NLS-1$
		name = ((IASTDeclarator)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPField);
		assertEquals(((ICPPField)name.resolveBinding()).getName(), "rank"); //$NON-NLS-1$
		
		index = code.indexOf( "int getRank();") + 4; //$NON-NLS-1$
		length = "getRank".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "getRank"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPMethod);
		assertEquals(((ICPPMethod)name.resolveBinding()).getName(), "getRank"); //$NON-NLS-1$
		
		index = code.indexOf( "Card::Card( int rank )"); //$NON-NLS-1$
		length = "Card".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "Card"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPClassType);
		assertEquals(((ICPPClassType)name.resolveBinding()).getName(), "Card"); //$NON-NLS-1$
		
		index = code.indexOf( "Card::Card( int rank )") + 6; //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "Card"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPConstructor);
		assertEquals(((ICPPConstructor)name.resolveBinding()).getName(), "Card"); //$NON-NLS-1$
		
		index = code.indexOf( "Card::Card( int rank )") + 16; //$NON-NLS-1$
		length = "rank".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTDeclarator);
		assertEquals(((IASTDeclarator)node).getName().toString(), "rank"); //$NON-NLS-1$
		name = ((IASTDeclarator)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IVariable);
		assertEquals(((IVariable)name.resolveBinding()).getName(), "rank"); //$NON-NLS-1$
		
		index = code.indexOf( "this->rank = ::rank();") + 6; //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "rank"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPField);
		assertEquals(((ICPPField)name.resolveBinding()).getName(), "rank"); //$NON-NLS-1$
		
		index = code.indexOf( "this->rank = ::rank();") + 15; //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "rank"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IFunction);
		assertEquals(((IFunction)name.resolveBinding()).getName(), "rank"); //$NON-NLS-1$
		
		index = code.indexOf( "this->rank = this->rank;") + 6; //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "rank"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPField);
		assertEquals(((ICPPField)name.resolveBinding()).getName(), "rank"); //$NON-NLS-1$
		
		index = code.indexOf( "this->rank = this->rank;") + 19; //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "rank"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPField);
		assertEquals(((ICPPField)name.resolveBinding()).getName(), "rank"); //$NON-NLS-1$
		
		index = code.indexOf( "this->rank = rank;") + 6; //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "rank"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPField);
		assertEquals(((ICPPField)name.resolveBinding()).getName(), "rank"); //$NON-NLS-1$
		
		index = code.indexOf( "this->rank = rank;") + 13; //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTIdExpression);
		assertEquals(((IASTIdExpression)node).getName().toString(), "rank"); //$NON-NLS-1$
		name = ((IASTIdExpression)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IVariable);
		assertEquals(((IVariable)name.resolveBinding()).getName(), "rank"); //$NON-NLS-1$
		
		index = code.indexOf( "this->rank = Card::rank;") + 6; //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "rank"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPField);
		assertEquals(((ICPPField)name.resolveBinding()).getName(), "rank"); //$NON-NLS-1$
		
		index = code.indexOf( "this->rank = Card::rank;") + 19; //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "rank"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPField);
		assertEquals(((ICPPField)name.resolveBinding()).getName(), "rank"); //$NON-NLS-1$
		
		index = code.indexOf( "this->rank = getRank();") + 6; //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "rank"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPField);
		assertEquals(((ICPPField)name.resolveBinding()).getName(), "rank"); //$NON-NLS-1$
		
		index = code.indexOf( "this->rank = getRank();") + 13; //$NON-NLS-1$
		length = "getRank".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTIdExpression);
		assertEquals(((IASTIdExpression)node).getName().toString(), "getRank"); //$NON-NLS-1$
		name = ((IASTIdExpression)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPMethod);
		assertEquals(((ICPPMethod)name.resolveBinding()).getName(), "getRank"); //$NON-NLS-1$
	}

	public void testBug77989() throws Exception {
		Writer writer = new StringWriter();
		writer.write("namespace N {        /* A */\n"); //$NON-NLS-1$
		writer.write("class C{};\n}\n"); //$NON-NLS-1$
		writer.write("using namespace N;   /* B */\n"); //$NON-NLS-1$
		writer.write("N::C c;              /* C */\n"); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "using namespace N;") + 16; //$NON-NLS-1$
		int length = "N".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "N"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICPPNamespace);
		assertEquals(((ICPPNamespace)name.resolveBinding()).getName(), "N"); //$NON-NLS-1$
	}

	public void testBug78435() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int itself;          //A\n"); //$NON-NLS-1$ 
		writer.write("void f(int itself){} //B\n"); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf( "void f(int itself){}") + 11; //$NON-NLS-1$
		int length = "itself".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTDeclarator);
		assertEquals(((IASTDeclarator)node).getName().toString(), "itself"); //$NON-NLS-1$
		IASTName name = ((IASTDeclarator)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IVariable);
		assertEquals(((IVariable)name.resolveBinding()).getName(), "itself"); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTDeclarator);
		assertEquals(((IASTDeclarator)node).getName().toString(), "itself"); //$NON-NLS-1$
		name = ((IASTDeclarator)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IVariable);
		assertEquals(((IVariable)name.resolveBinding()).getName(), "itself"); //$NON-NLS-1$
	}

	public void testBug78231A() throws Exception {
		Writer writer = new StringWriter();
		writer.write("struct Base {\n"); //$NON-NLS-1$
		writer.write("int Data; // 1\n"); //$NON-NLS-1$
		writer.write("struct Data; // 2\n};\n"); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf("struct Data;") + 7; //$NON-NLS-1$
		int length = "Data".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "Data"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICompositeType);
		assertEquals(((ICompositeType)name.resolveBinding()).getName(), "Data"); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "Data"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICompositeType);
		assertEquals(((ICompositeType)name.resolveBinding()).getName(), "Data"); //$NON-NLS-1$
	}
	
	public void testBug78231B() throws Exception {
		Writer writer = new StringWriter();
		writer.write("int Data;\n"); //$NON-NLS-1$
		writer.write("struct Base {\n"); //$NON-NLS-1$
		writer.write("int Data; // 1\n"); //$NON-NLS-1$
		writer.write("struct Data; // 2\n};\n"); //$NON-NLS-1$
		
		String code = writer.toString();
		int index = code.indexOf("struct Data;") + 7; //$NON-NLS-1$
		int length = "Data".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "Data"); //$NON-NLS-1$
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICompositeType);
		assertEquals(((ICompositeType)name.resolveBinding()).getName(), "Data"); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		assertEquals(node.toString(), "Data"); //$NON-NLS-1$
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ICompositeType);
		assertEquals(((ICompositeType)name.resolveBinding()).getName(), "Data"); //$NON-NLS-1$
	}
	
	public void testSimpleKRCTest1() throws Exception {
		StringBuffer buffer = new StringBuffer();
    	buffer.append( "int f(char x);\n" ); //$NON-NLS-1$
    	buffer.append( "int f(x) char x;\n" ); //$NON-NLS-1$
    	buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
		
		String code = buffer.toString();
		int index = code.indexOf("x;"); //$NON-NLS-1$
		int length = "x".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTDeclarator);
		assertEquals(((IASTDeclarator)node).getName().toString(), "x"); //$NON-NLS-1$
		IASTName name = ((IASTDeclarator)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IParameter);
		assertEquals(((IParameter)name.resolveBinding()).getName(), "x"); //$NON-NLS-1$
	}
	
	public void testSimpleKRCTest2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "int f();\n" ); //$NON-NLS-1$
		buffer.append( "int f(x) char x;\n" ); //$NON-NLS-1$
		buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$
		
		String code = buffer.toString();
		int index = code.indexOf("x;"); //$NON-NLS-1$
		int length = "x".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTDeclarator);
		assertEquals(((IASTDeclarator)node).getName().toString(), "x"); //$NON-NLS-1$
		IASTName name = ((IASTDeclarator)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IParameter);
		assertEquals(((IParameter)name.resolveBinding()).getName(), "x"); //$NON-NLS-1$
	}
	
	public void testSimpleKRCTest3() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "int const *f();\n" ); //$NON-NLS-1$
		buffer.append( "int const *f(x) char x;\n" ); //$NON-NLS-1$
		buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$

		String code = buffer.toString();
		int index = code.indexOf("char x;"); //$NON-NLS-1$
		int length = "char x;".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTSimpleDeclaration);
		assertEquals( ((IASTSimpleDeclaration)node).getDeclarators()[0].getName().toString(), "x" ); //$NON-NLS-1$
		IASTName name = ((IASTSimpleDeclaration)node).getDeclarators()[0].getName();
		assertNotNull(name.resolveBinding());
		assertTrue( name.resolveBinding() instanceof IParameter );
		assertEquals( ((IParameter)name.resolveBinding()).getName(), "x" ); //$NON-NLS-1$
	}
	
	public void testKRC_1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "int isroot (x, y) /* comment */ \n" ); //$NON-NLS-1$
		buffer.append( "int x;\n" ); //$NON-NLS-1$
		buffer.append( "int y;\n" ); //$NON-NLS-1$
		buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$

		String code = buffer.toString();
		int index = code.indexOf("y;"); //$NON-NLS-1$
		int length = "y".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTDeclarator);
		assertEquals(((IASTDeclarator)node).getName().toString(), "y"); //$NON-NLS-1$
		IASTName name = ((IASTDeclarator)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IParameter);
		assertEquals(((IParameter)name.resolveBinding()).getName(), "y"); //$NON-NLS-1$
	}
	
	public void testKRCWithTypes() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "typedef char c;\n" ); //$NON-NLS-1$
		buffer.append( "int isroot (c);\n" ); //$NON-NLS-1$
		buffer.append( "int isroot (x) \n" ); //$NON-NLS-1$
		buffer.append( "c x;\n" ); //$NON-NLS-1$
		buffer.append( "{ return x == 0; }\n" ); //$NON-NLS-1$

		String code = buffer.toString();
		int index = code.indexOf("c x;"); //$NON-NLS-1$
		int length = "c".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTNamedTypeSpecifier);
		assertEquals(((IASTNamedTypeSpecifier)node).getName().toString(), "c"); //$NON-NLS-1$
		IASTName name = ((IASTNamedTypeSpecifier)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof ITypedef);
		assertEquals(((ITypedef)name.resolveBinding()).getName(), "c"); //$NON-NLS-1$
		
		index = code.indexOf("x;"); //$NON-NLS-1$
		length = "x".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTDeclarator);
		assertEquals(((IASTDeclarator)node).getName().toString(), "x"); //$NON-NLS-1$
		name = ((IASTDeclarator)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IParameter);
		assertEquals(((IParameter)name.resolveBinding()).getName(), "x"); //$NON-NLS-1$
	}
	
	public void testKRC_monop_cards1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "#ifdef __STDC__\n" ); //$NON-NLS-1$
		buffer.append( "#define __P(x) x\n" ); //$NON-NLS-1$
		buffer.append( "#else\n" ); //$NON-NLS-1$
		buffer.append( "#define __P(x) ()\n" ); //$NON-NLS-1$
		buffer.append( "#endif\n" ); //$NON-NLS-1$
		buffer.append( "struct A_struct {\n" ); //$NON-NLS-1$
		buffer.append( "int a;\n" ); //$NON-NLS-1$
		buffer.append( "long *c;\n" ); //$NON-NLS-1$
		buffer.append( "};\n" ); //$NON-NLS-1$
		buffer.append( "typedef struct A_struct A;\n" ); //$NON-NLS-1$
		buffer.append( "static void f __P((A *));\n" ); //$NON-NLS-1$
		buffer.append( "static void\n" ); //$NON-NLS-1$
		buffer.append( "f(x)\n" ); //$NON-NLS-1$
		buffer.append( "A *x; {\n" ); //$NON-NLS-1$
		buffer.append( "x->a = 0;\n" ); //$NON-NLS-1$
		buffer.append( "x->c[1]=x->c[2];\n" ); //$NON-NLS-1$
		buffer.append( "}\n" ); //$NON-NLS-1$

		String code = buffer.toString();
		int index = code.indexOf("*c;"); //$NON-NLS-1$
		int length = "*".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof ICASTPointer);
		
		
		index = code.indexOf("*c;") + 1; //$NON-NLS-1$
		length = "c".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IField);
		assertEquals(((IField)name.resolveBinding()).getName(), "c"); //$NON-NLS-1$
		
		index = code.indexOf("c[2]"); //$NON-NLS-1$
		length = "c".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IField);
		assertEquals(((IField)name.resolveBinding()).getName(), "c"); //$NON-NLS-1$
	}
	
	public void testKRC_monop_cards2() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "int\n" ); //$NON-NLS-1$
		buffer.append( "getinp(prompt, list)\n" ); //$NON-NLS-1$
		buffer.append( "        const char *prompt, *const list[];\n" ); //$NON-NLS-1$
		buffer.append( "{\n	*list[1] = 'a';\n}\n" ); //$NON-NLS-1$

		String code = buffer.toString();
		int index = code.indexOf("list[]"); //$NON-NLS-1$
		int length = "list".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		IASTName name = (IASTName)node;
		assertEquals(name.toString(), "list"); //$NON-NLS-1$
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IParameter);
		assertEquals(((IParameter)name.resolveBinding()).getName(), "list"); //$NON-NLS-1$
		
		index = code.indexOf("[]"); //$NON-NLS-1$
		length = "[]".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTArrayModifier);
		
		index = code.indexOf("*const list[]"); //$NON-NLS-1$
		length = "*const".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTPointer);
	}
	
	
	public void testKRC_getParametersOrder() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "int f(a, b) int b,a;{}\n" ); //$NON-NLS-1$

		String code = buffer.toString();
		int index = code.indexOf("b,a"); //$NON-NLS-1$
		int length = "b".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTDeclarator);
		IASTName name = ((IASTDeclarator)node).getName();
		assertEquals(name.toString(), "b"); //$NON-NLS-1$
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IParameter);
		assertEquals(((IParameter)name.resolveBinding()).getName(), "b"); //$NON-NLS-1$
	}
	
	public void testKRC_Ethereal_1() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "struct symbol {\n" ); //$NON-NLS-1$
		buffer.append( "int lambda;\n};\n" ); //$NON-NLS-1$
		buffer.append( "struct lemon {\n" ); //$NON-NLS-1$
		buffer.append( "struct symbol **symbols;\n" ); //$NON-NLS-1$
		buffer.append( "int errorcnt;\n};\n" ); //$NON-NLS-1$
		buffer.append( "void f(lemp)\n" ); //$NON-NLS-1$
		buffer.append( "struct lemon *lemp;\n{\n" ); //$NON-NLS-1$
		buffer.append( "lemp->symbols[1]->lambda = 1;\n" ); //$NON-NLS-1$
		buffer.append( "lemp->errorcnt++;}\n" ); //$NON-NLS-1$

		String code = buffer.toString();
		int index = code.indexOf("**symbols"); //$NON-NLS-1$
		int length = "*".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTPointer);
		
		index = code.indexOf("**symbols") + 1; //$NON-NLS-1$
		length = "*".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTPointer);

		index = code.indexOf("**symbols") + 2; //$NON-NLS-1$
		length = "symbols".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IField);
		assertEquals(((IField)name.resolveBinding()).getName(), "symbols"); //$NON-NLS-1$
		
		index = code.indexOf("lemp->symbols") + 6; //$NON-NLS-1$
		length = "symbols".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.C, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IField);
		assertEquals(((IField)name.resolveBinding()).getName(), "symbols"); //$NON-NLS-1$
	}

	public void testBug86698() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append( "struct C;\n"); //$NON-NLS-1$
		buffer.append( "void no_opt(C*);\n"); //$NON-NLS-1$
		buffer.append( "struct C {\n"); //$NON-NLS-1$
		buffer.append( "int c;\n"); //$NON-NLS-1$
		buffer.append( "C() : c(0) { no_opt(this); }\n"); //$NON-NLS-1$
		buffer.append( "};\n"); //$NON-NLS-1$

		String code = buffer.toString();
		int index = code.indexOf("c(0)"); //$NON-NLS-1$
		int length = "c".length(); //$NON-NLS-1$
		IASTNode node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue(node instanceof IASTName);
		IASTName name = (IASTName)node;
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IField);
		assertEquals(((IField)name.resolveBinding()).getName(), "c"); //$NON-NLS-1$
		
	}
	
	public void testLittleThings() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("int a[3];\r\n"); //$NON-NLS-1$
		buffer.append("int *b;\r\n"); //$NON-NLS-1$
		buffer.append("int &c;\r\n"); //$NON-NLS-1$
		buffer.append("char d='e';\r\n"); //$NON-NLS-1$
		
		String code = buffer.toString();
		int index = 0; 
		int length = 0;
		
		IASTNode node = null;
		ParserLanguage lang = null;
		for(int i=0; i<2; i++) {
			lang = i==0 ? ParserLanguage.C : ParserLanguage.CPP;
			
			index = code.indexOf("[3]"); //$NON-NLS-1$
			length = "[3]".length(); //$NON-NLS-1$
			node = parse( code, lang, index, length );
			assertNotNull(node);
			
			index = code.indexOf("3"); //$NON-NLS-1$
			length = "3".length(); //$NON-NLS-1$
			node = parse( code, lang, index, length );
			assertNotNull(node);
			
			index = code.indexOf("*"); //$NON-NLS-1$
			length = "*".length(); //$NON-NLS-1$
			node = parse( code, lang, index, length );
			assertNotNull(node);
			
			if (lang != ParserLanguage.C) {
				index = code.indexOf("&"); //$NON-NLS-1$
				length = "&".length(); //$NON-NLS-1$
				node = parse( code, lang, index, length );
				assertNotNull(node);
				
				index = code.indexOf("&c"); //$NON-NLS-1$
				length = "&c".length(); //$NON-NLS-1$
				node = parse( code, lang, index, length );
				assertNotNull(node);
			}
			
			index = code.indexOf("int a"); //$NON-NLS-1$
			length = "int".length(); //$NON-NLS-1$
			node = parse( code, lang, index, length );
			assertNotNull(node);
			
			index = code.indexOf("int a[3];"); //$NON-NLS-1$
			length = "int a[3];".length(); //$NON-NLS-1$
			node = parse( code, lang, index, length );
			assertNotNull(node);
			
			index = code.indexOf("a[3]"); //$NON-NLS-1$
			length = "a[3]".length(); //$NON-NLS-1$
			node = parse( code, lang, index, length );
			assertNotNull(node);
			
			index = code.indexOf("*b"); //$NON-NLS-1$
			length = "*b".length(); //$NON-NLS-1$
			node = parse( code, lang, index, length );
			assertNotNull(node);
			
			index = code.indexOf("'e'"); //$NON-NLS-1$
			length = "'e'".length(); //$NON-NLS-1$
			node = parse( code, lang, index, length );
			assertNotNull(node);
		}
	}
	
	public void testSimpleWindowsPreprocessorSelections() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define ONE 1\r\n"); //$NON-NLS-1$
		buffer.append("#ifdef ONE\r\n"); //$NON-NLS-1$
		buffer.append("int x=0;\r\n"); //$NON-NLS-1$
		buffer.append("#else\r\n"); //$NON-NLS-1$
		buffer.append("char c='c';\r\n"); //$NON-NLS-1$
		buffer.append("#endif\r\n"); //$NON-NLS-1$
		
		String code = buffer.toString();
		int index = 0; 
		int length = 0;
		
		IASTNode node = null;
		ParserLanguage lang = null;
		for(int i=0; i<2; i++) {
			lang = i==0 ? ParserLanguage.C : ParserLanguage.CPP;
			
			index = code.indexOf("#define ONE 1"); //$NON-NLS-1$
			length = "#define ONE 1".length(); //$NON-NLS-1$
			node = parse( code, lang, index, length );
			assertNotNull(node);

			// TODO bug 87179
//			index = code.indexOf("#ifdef ONE"); //$NON-NLS-1$
//			length = "#ifdef ONE".length(); //$NON-NLS-1$
//			node = parse( code, lang, index, length );
//			assertNotNull(node);
			
			index = code.indexOf("int x=0;"); //$NON-NLS-1$
			length = "int x=0;".length(); //$NON-NLS-1$
			node = parse( code, lang, index, length );
			assertNotNull(node);
			
			index = code.indexOf("#else"); //$NON-NLS-1$
			length = "#else".length(); //$NON-NLS-1$
			node = parse( code, lang, index, length );
			assertNotNull(node);
			
			index = code.indexOf("#endif"); //$NON-NLS-1$
			length = "#endif".length(); //$NON-NLS-1$
			node = parse( code, lang, index, length );
			assertNotNull(node);
		}
	}
	
	public void testBug86993() throws Exception {
		StringBuffer buffer = new StringBuffer();
		buffer.append("#define _BEGIN_STD_C extern \"C\" {\r\n"); //$NON-NLS-1$
		buffer.append("#define _END_STD_C  }\r\n"); //$NON-NLS-1$
		buffer.append("_BEGIN_STD_C\r\n"); //$NON-NLS-1$
		buffer.append("char c;\r\n"); //$NON-NLS-1$
		buffer.append("_END_STD_C\r\n"); //$NON-NLS-1$
		
		String code = buffer.toString();
		int index = 0; 
		int length = 0;
		
		IASTNode node = null;
		
		index = code.indexOf("c;"); //$NON-NLS-1$
		length = "c".length(); //$NON-NLS-1$
		node = parse( code, ParserLanguage.CPP, index, length );
		assertNotNull(node);
		assertTrue( node instanceof IASTDeclarator);
		assertEquals(((IASTDeclarator)node).getName().toString(), "c"); //$NON-NLS-1$
		IASTName name = ((IASTDeclarator)node).getName();
		assertNotNull(name.resolveBinding());
		assertTrue(name.resolveBinding() instanceof IVariable);
	}
}
