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

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.List;

import junit.framework.TestCase;

import org.eclipse.cdt.internal.core.dom.ClassSpecifier;
import org.eclipse.cdt.internal.core.dom.DOMBuilder;
import org.eclipse.cdt.internal.core.dom.EnumerationSpecifier;
import org.eclipse.cdt.internal.core.dom.IOffsetable;
import org.eclipse.cdt.internal.core.dom.NamespaceDefinition;
import org.eclipse.cdt.internal.core.dom.SimpleDeclaration;
import org.eclipse.cdt.internal.core.dom.TemplateDeclaration;
import org.eclipse.cdt.internal.core.parser.IParser;
import org.eclipse.cdt.internal.core.parser.Parser;
import org.eclipse.cdt.internal.core.parser.Scanner;
import org.eclipse.cdt.internal.core.parser.Token;
import org.eclipse.core.runtime.Path;

/**
 * @author jcamelon
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class LineNumberTest extends TestCase {
	
	public LineNumberTest( String arg )
	{
		super( arg );
	}
	private InputStream fileIn;
	 
	protected void setUp() throws Exception {
		String fileName =org.eclipse.core.runtime.Platform.getPlugin("org.eclipse.cdt.ui.tests").find(new Path("/")).getFile() + "parser/org/eclipse/cdt/core/parser/resources/OffsetTest.h";
		fileIn = new FileInputStream(fileName);
	}
	
	public void testLineNos() throws Exception
	{
		Scanner scanner = new Scanner(); 
		Reader reader = new StringReader( "int x = 3;\n foo\nfire\nfoe ");
		scanner.initialize( reader, "string");
		Token t = scanner.nextToken(); 
		assertEquals( t.getType(), Token.t_int );
		assertEquals( scanner.getLineNumberForOffset(t.getOffset()), 1 );
		t = scanner.nextToken(); 
		assertEquals( t.getImage(), "x");
		assertEquals( scanner.getLineNumberForOffset(t.getOffset()), 1 );
		t = scanner.nextToken(); 
		assertEquals( t.getType(), Token.tASSIGN );
		assertEquals( scanner.getLineNumberForOffset(t.getOffset()), 1 );
		t = scanner.nextToken(); 
		assertEquals( t.getImage(), "3" );
		assertEquals( scanner.getLineNumberForOffset(t.getOffset()), 1 );
		t = scanner.nextToken(); 
		assertEquals( t.getType(), Token.tSEMI);
		assertEquals( scanner.getLineNumberForOffset(t.getOffset()), 1 );
		for( int i = 2; i < 5; ++i )
		{ 
			t = scanner.nextToken(); 
			assertEquals( t.getType(), Token.tIDENTIFIER);
			assertEquals( scanner.getLineNumberForOffset(t.getOffset()), i );
		}

		try {
			t = scanner.nextToken();
			fail( "EOF");
		} 
		catch (Parser.EndOfFile e) {
			assertEquals( scanner.getLineNumberForOffset(29), 4 ); 
		}

	}
	
	public void testDOMLineNos() throws Exception
	{
		DOMBuilder domBuilder = new DOMBuilder();
		IParser parser = new Parser( fileIn, domBuilder, true );  
		if( ! parser.parse() ) fail( "Parse of file failed");
		
		List macros = domBuilder.getTranslationUnit().getMacros();
		List inclusions = domBuilder.getTranslationUnit().getInclusions();
		List declarations = domBuilder.getTranslationUnit().getDeclarations();
		
		assertEquals( 3, macros.size() );
		assertEquals( 1, inclusions.size() );
		assertEquals( declarations.size(), 4 );
		validateLineNumbers( (IOffsetable)inclusions.get(0), 2, 2 );
		validateLineNumbers( (IOffsetable)macros.get(0), 5, 5 );
		validateLineNumbers( (IOffsetable)macros.get(1), 6, 6 );
		validateLineNumbers( (IOffsetable)macros.get(2), 30, 31 );
		
		NamespaceDefinition namespaceDecl = (NamespaceDefinition)declarations.get(0);
		validateLineNumbers( namespaceDecl, 8, 22 ); 
		List namespaceMembers = namespaceDecl.getDeclarations();
		assertEquals( namespaceMembers.size(), 1 );
		ClassSpecifier Hello = (ClassSpecifier)((SimpleDeclaration)namespaceMembers.get(0)).getTypeSpecifier();
		validateLineNumbers( Hello, 10, 21);
		List classMembers = Hello.getDeclarations();
		assertEquals( classMembers.size(), 3 );
		for( int i = 0; i < 3; ++i )
		{
			SimpleDeclaration memberDeclaration = (SimpleDeclaration)Hello.getDeclarations().get(i);
			switch( i )
			{
				case 0:
					validateLineNumbers(memberDeclaration, 13, 13 );
					break;
				case 1:
					validateLineNumbers(memberDeclaration, 15, 15 );
					break;
				case 2:
					validateLineNumbers(memberDeclaration, 18, 20 );
					break; 
				default:
					break;
			}
		}
	
		validateLineNumbers( (SimpleDeclaration)declarations.get(1), 25, 27);
		validateLineNumbers( (TemplateDeclaration)declarations.get(2), 34, 35);
		SimpleDeclaration d = (SimpleDeclaration)declarations.get(3);
		validateLineNumbers( d, 38, 43);
		validateLineNumbers( ((EnumerationSpecifier)d.getTypeSpecifier()), 38, 43);
		
	}
	
	protected void tearDown() throws Exception {
		if( fileIn != null )	fileIn.close(); 
	}	

	protected void validateLineNumbers( IOffsetable offsetable, int top, int bottom )
	{
		assertNotNull( offsetable );
		assertEquals( offsetable.getTopLine(), top );
		assertEquals( offsetable.getBottomLine(), bottom );
	}
}
