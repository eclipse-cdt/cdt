/*
 * Created on Dec 8, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.eclipse.cdt.core.parser.tests;

import java.io.StringReader;

import junit.framework.TestCase;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTCompletionNode;
import org.eclipse.cdt.internal.core.parser.ParserLogService;

/**
 * @author jcamelon
 * 
 * To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Generation - Code and Comments
 */
public class ContextualParseTest extends TestCase {

	public ContextualParseTest(String name) {
		super(name);
	}

	protected IASTCompletionNode parse(String code, int offset)
		throws Exception {
		ISourceElementRequestor requestor = new NullSourceElementRequestor();
		IParserLogService log = new ParserLogService();

		IParser parser = null;

		parser =
			ParserFactory.createParser(
				ParserFactory.createScanner(
					new StringReader(code),
					"completion-test",
					new ScannerInfo(),
					ParserMode.CONTEXTUAL_PARSE,
					ParserLanguage.CPP,
					requestor,
					log),
				requestor,
				ParserMode.CONTEXTUAL_PARSE,
				ParserLanguage.CPP,
				log);
		
		return parser.parse( offset );

	}

	public void testBaseCase() throws Exception
	{
		IASTCompletionNode node = parse( "class ABC { }; AB\n\n", 17);
		assertNotNull( node );
		assertNotNull( node.getCompletionPrefix() );
		assertEquals( node.getCompletionPrefix(), "AB");
	}
}
