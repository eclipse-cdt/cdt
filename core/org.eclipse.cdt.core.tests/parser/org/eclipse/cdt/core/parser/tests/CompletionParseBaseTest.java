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
package org.eclipse.cdt.core.parser.tests;

import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.core.parser.ast.IASTNode;
import org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode.CompletionKind;
import org.eclipse.cdt.core.parser.ast.IASTNode.ILookupResult;

/**
 * @author johnc
 */
public class CompletionParseBaseTest extends CompleteParseBaseTest {
	/**
	 * 
	 */
	public CompletionParseBaseTest() {
		super();
	}
	/**
	 * @param name
	 */
	public CompletionParseBaseTest(String name) {
		super(name);
	}
	
	protected IASTCompletionNode parse(String code, int offset) throws Exception {
		callback = new FullParseCallback();
		IParser parser = null;
	
		parser =
			ParserFactory.createParser(
				ParserFactory.createScanner(
					new CodeReader(code.toCharArray()),
					new ScannerInfo(),
					ParserMode.COMPLETION_PARSE,
					ParserLanguage.CPP,
					callback,
					new NullLogService(), null),
				callback,
				ParserMode.COMPLETION_PARSE,
				ParserLanguage.CPP,
				null);
		
		return parser.parse( offset );
	
	}
	protected IASTCompletionNode parse(String code, int offset, ParserLanguage lang) throws Exception {
		callback = new FullParseCallback();
		IParser parser = null;
	
		parser =
			ParserFactory.createParser(
				ParserFactory.createScanner(
					new CodeReader(code.toCharArray()),
					new ScannerInfo(),
					ParserMode.COMPLETION_PARSE,
					lang,
					callback,
					new NullLogService(), null),
				callback,
				ParserMode.COMPLETION_PARSE,
				lang,
				null);
		
		return parser.parse( offset );
	
	}
	/**
	 * @param result
	 */
	protected void validateLookupResult(ILookupResult result, Set matches) {
		
		assertNotNull( matches );
		assertEquals( result.getResultsSize(), matches.size() );
		
		Iterator iter = result.getNodes();
		while( iter.hasNext() )
		{
			IASTOffsetableNamedElement element = (IASTOffsetableNamedElement) iter.next();
			assertTrue( matches.contains( element.getName() ));
		}
	}
	/**
	 * @return
	 */
	protected IASTCompilationUnit getCompilationUnit() {
		IASTCompilationUnit compilationUnit = (IASTCompilationUnit) ((Scope) callback.getCompilationUnit()).getScope();
		return compilationUnit;
	}
	/**
	 * @param node
	 * @param hasKeywords
	 */
	protected void validateCompletionNode(IASTCompletionNode node, String prefix, CompletionKind kind, IASTNode context, boolean hasKeywords) {
		assertNotNull( node );
		assertEquals( node.getCompletionPrefix(), prefix);
		assertEquals( node.getCompletionKind(), kind );
		assertEquals( node.getCompletionContext(), context );
		if( hasKeywords )
			assertTrue( node.getKeywords().hasNext() );
		else
			assertFalse( node.getKeywords().hasNext()  );
	}	
	
}
