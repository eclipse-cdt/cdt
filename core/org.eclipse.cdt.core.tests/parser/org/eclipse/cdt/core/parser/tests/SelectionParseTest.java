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

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
import org.eclipse.cdt.core.parser.ast.IASTMethod;
import org.eclipse.cdt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.cdt.core.parser.ast.IASTVariable;

/**
 * @author jcamelon
 */
public class SelectionParseTest extends CompleteParseBaseTest {

	protected IASTNode parse(String code, int offset1, int offset2 )
	throws Exception {
		callback = new FullParseCallback();
		IParser parser = null;

		parser =
			ParserFactory.createParser(
					ParserFactory.createScanner(
							new StringReader(code),
							"completion-test", //$NON-NLS-1$
							new ScannerInfo(),
							ParserMode.SELECTION_PARSE,
							ParserLanguage.CPP,
							callback,
							new NullLogService(), null),
							callback,
							ParserMode.SELECTION_PARSE,
							ParserLanguage.CPP,
							ParserUtil.getParserLogService());
		
		IParser.ISelectionParseResult result =parser.parse( offset1, offset2 );
		if( result == null ) return null;
		return (IASTNode) result.getOffsetableNamedElement();

	}
	
	
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
		assertNull( parse( code, offset1, offset2 ));
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

	}
}
