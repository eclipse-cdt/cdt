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

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTFunction;
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
							"completion-test",
							new ScannerInfo(),
							ParserMode.SELECTION_PARSE,
							ParserLanguage.CPP,
							callback,
							new NullLogService()),
							callback,
							ParserMode.SELECTION_PARSE,
							ParserLanguage.CPP,
							ParserUtil.getParserLogService());
		
		return parser.parse( offset1, offset2 );

	}
	
	
	public void testBaseCase_VariableReference() throws Exception
	{
		String code = "void f() { int x; x=3; }";
		int offset1 = code.indexOf( "x=" );
		int offset2 = code.indexOf( '=');
		IASTNode node = parse( code, offset1, offset2 );
		assertTrue( node instanceof IASTVariable );
		assertEquals( ((IASTVariable)node).getName(), "x" );
	}

	public void testBaseCase_FunctionReference() throws Exception
	{
		String code = "int x(){x( );}";
		int offset1 = code.indexOf( "x( " );
		int offset2 = code.indexOf( "( )");
		IASTNode node = parse( code, offset1, offset2 );
		assertTrue( node instanceof IASTFunction );
		assertEquals( ((IASTFunction)node).getName(), "x" );
	}
	
	public void testBaseCase_Error() throws Exception
	{
		String code = "int x() { y( ) }";
		int offset1 = code.indexOf( "y( " );
		int offset2 = code.indexOf( "( )");
		assertNull( parse( code, offset1, offset2 ));
	}
	
	public void testBaseCase_FunctionDeclaration() throws Exception
	{
		String code = "int x(); x( );";
		int offset1 = code.indexOf( "x()" );
		int offset2 = code.indexOf( "()");
		IASTNode node = parse( code, offset1, offset2 );
		assertTrue( node instanceof IASTFunction );
		assertEquals( ((IASTFunction)node).getName(), "x" );
	}
	
	public void testBaseCase_FunctionDeclaration2() throws Exception
	{
		String code = "int printf( const char *, ... ); ";
		int offset1 = code.indexOf( "printf" );
		int offset2 = code.indexOf( "( const");
		IASTNode node = parse( code, offset1, offset2 );
		assertTrue( node instanceof IASTFunction );
		assertEquals( ((IASTFunction)node).getName(), "printf" );		
	}

	public void testBaseCase_VariableDeclaration() throws Exception
	{
		String code = "int x = 3;";
		int offset1 = code.indexOf( "x" );
		int offset2 = code.indexOf( " =");
		IASTNode node = parse( code, offset1, offset2 );
		assertNotNull( node );
		assertTrue( node instanceof IASTVariable );
		assertEquals( ((IASTVariable)node).getName(), "x" );
	}
	
	public void testBaseCase_Parameter() throws Exception
	{
		String code = "int main( int argc ) { int x = argc; }";
		int offset1 = code.indexOf( "argc;" );
		int offset2 = code.indexOf( ";" );
		IASTNode node = parse( code, offset1, offset2 );
		assertNotNull( node );
		assertTrue( node instanceof IASTParameterDeclaration );
		assertEquals( ((IASTParameterDeclaration)node).getName(), "argc" );		
	}
	
	
}
