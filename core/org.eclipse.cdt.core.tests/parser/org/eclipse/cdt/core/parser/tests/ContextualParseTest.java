/*
 * Created on Dec 8, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.core.parser.tests;

import java.io.StringReader;
import java.io.StringWriter;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTClassSpecifier;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.internal.core.parser.ParserLogService;

/**
 * @author jcamelon
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ContextualParseTest extends CompleteParseBaseTest {

	
	public ContextualParseTest(String name) {
		super(name);
	}

	protected IASTCompletionNode parse(String code, int offset)
		throws Exception {
		IParserLogService log = new ParserLogService();
		callback = new FullParseCallback();
		IParser parser = null;

		parser =
			ParserFactory.createParser(
				ParserFactory.createScanner(
					new StringReader(code),
					"completion-test",
					new ScannerInfo(),
					ParserMode.CONTEXTUAL_PARSE,
					ParserLanguage.CPP,
					callback,
					log),
				callback,
				ParserMode.CONTEXTUAL_PARSE,
				ParserLanguage.CPP,
				log);
		
		return parser.parse( offset );

	}

	public void testBaseCase() throws Exception
	{
		StringWriter writer = new StringWriter(); 
		writer.write( "class ABC " ); 
		writer.write( "{int x;}; " ); 
		writer.write( "AB\n\n" );

		IASTCompletionNode node = parse( writer.toString(), 21); 
		assertNotNull( node );
		assertNotNull( node.getCompletionPrefix() );
		assertEquals( node.getCompletionScope(), ((Scope)callback.getCompilationUnit()).getScope() );
		assertEquals( node.getCompletionPrefix(), "A");
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.VARIABLE_TYPE );

		node = parse( writer.toString(), 12); 
		assertNotNull( node );
		assertNotNull( node.getCompletionPrefix() );
		assertTrue( node.getCompletionScope() instanceof IASTClassSpecifier );
		assertEquals( node.getCompletionPrefix(), "i");
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.FIELD_TYPE );
		
		
		node = parse( writer.toString(), 22); 
		assertNotNull( node );
		assertNotNull( node.getCompletionPrefix() );
		assertEquals( node.getCompletionScope(), ((Scope)callback.getCompilationUnit()).getScope() );
		assertEquals( node.getCompletionPrefix(), "AB");
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.VARIABLE_TYPE );
	
		node = parse( writer.toString(), 6); 
		assertNotNull( node );
		assertNotNull( node.getCompletionPrefix() );
		assertEquals( node.getCompletionScope(), ((Scope)callback.getCompilationUnit()).getScope() );
		assertEquals( node.getCompletionPrefix(), "");
		assertEquals( node.getCompletionKind(), IASTCompletionNode.CompletionKind.USER_SPECIFIED_NAME );
		
	}
}
