/**********************************************************************
 * Copyright (c) 2002-2004 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.core.parser.failedTests;

import java.io.StringWriter;
import java.util.Iterator;

import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTField;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTNode.ILookupResult;
import org.eclipse.cdt.core.parser.tests.CompletionParseBaseTest;

/**
 * @author johnc
 */
public class CompletionParseFailedTest extends CompletionParseBaseTest {
	/**
	 * 
	 */
	public CompletionParseFailedTest() {
		super();
		// TODO Auto-generated constructor stub
	}
	/**
	 * @param name
	 */
	public CompletionParseFailedTest(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}
	
	public void testCompletionInTypeDef() throws Exception{
		StringWriter writer = new StringWriter();
		writer.write( "struct A {  int name;  };  \n" );
		writer.write( "typedef struct A * PA;     \n" );
		writer.write( "int main() {               \n" );
		writer.write( "   PA a;                   \n" );
		writer.write( "   a->SP                   \n" );
		writer.write( "}                          \n" );
		
		String code = writer.toString();
		int index = code.indexOf( "SP" );
		
		IASTCompletionNode node = parse( code, index );
		ILookupResult result = node.getCompletionScope().lookup( node.getCompletionPrefix(), 
                                                                 new IASTNode.LookupKind[]{ IASTNode.LookupKind.ALL },
				                                                 node.getCompletionContext() );

		//this is where the failure happens ... when the bug is fixed this line can be removed and the rest uncommented
		assertEquals( result.getResultsSize(), 4 );
//		assertEquals( result.getResultsSize(), 1 );
//		
//		Iterator iter = result.getNodes();
//		IASTField name = (IASTField) iter.next();
//		
//		assertEquals( name.getName(), "name" );
//		assertFalse( iter.hasNext() );
	}
}
