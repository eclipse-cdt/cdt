package org.eclipse.cdt.core.parser.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.core.parser.ast.IASTExpression;
import org.eclipse.cdt.internal.core.parser.IExpressionParser;
import org.eclipse.cdt.internal.core.parser.InternalParserUtil;

public class ExprEvalTest extends TestCase {

	public static Test suite() {
		return new TestSuite(ExprEvalTest.class);
	}
	
	public ExprEvalTest(String name) {
		super(name);
	}
	
	public void runTest(String code, int expectedValue) throws Exception {
		
		final NullSourceElementRequestor nullCallback = new NullSourceElementRequestor();
        IExpressionParser parser = InternalParserUtil.createExpressionParser(ParserFactory.createScanner( new CodeReader( code.toCharArray() ), new ScannerInfo(), null, ParserLanguage.CPP, nullCallback, new NullLogService(), null ), ParserLanguage.CPP, null );
		IASTExpression expression = parser.expression(null,null, null);
		assertEquals(expectedValue, expression.evaluateExpression());
	}
	
	public void testInteger() throws Exception {
		runTest("5;", 5); //$NON-NLS-1$
		runTest( "33;", 33 ); //$NON-NLS-1$
	}
	
	public void testNot() throws Exception
	{
		runTest( "!1;", 0 ); //$NON-NLS-1$
		runTest( "!0;", 1 ); //$NON-NLS-1$
		runTest( "!4;", 0 );  //$NON-NLS-1$
		runTest( "!!4;", 1 ); //$NON-NLS-1$
	}
	
	public void testMultiplicational() throws Exception
	{
		runTest( "3 * 4;", 12 );  //$NON-NLS-1$
		runTest( "55 * 2;", 110 ); //$NON-NLS-1$
		runTest( "4 / 3;", 1 ); //$NON-NLS-1$
		runTest( "100/4;", 25 ); //$NON-NLS-1$
		runTest( "8 % 2;", 0 ); //$NON-NLS-1$
		runTest( "8 % 3;", 2 ); //$NON-NLS-1$
	}
	
	public void testAdditive() throws Exception
	{
		runTest( "4 + 4;", 8 ); //$NON-NLS-1$
		runTest( "4 - 4;", 0 ); //$NON-NLS-1$
	}
	
	public void testLogicalAnd() throws Exception
	{
		runTest( "4 && 5;", 1 ); //$NON-NLS-1$
		runTest( "0 && 5;", 0 ); //$NON-NLS-1$
		runTest( "5 && 0;", 0 ); //$NON-NLS-1$
		runTest( "0 && 0;", 0 ); //$NON-NLS-1$
	}
	
	public void testLogicalOr() throws Exception
	{
		runTest( "4 || 5;", 1 ); //$NON-NLS-1$
		runTest( "0 || 5;", 1 ); //$NON-NLS-1$
		runTest( "5 || 0;", 1 ); //$NON-NLS-1$
		runTest( "0 || 0;", 0 ); //$NON-NLS-1$
	}
	
	public void testRelational() throws Exception {
		runTest("1 < 2;", 1); //$NON-NLS-1$
		runTest("2 < 1;", 0); //$NON-NLS-1$
		runTest("2 == 1 + 1;", 1); //$NON-NLS-1$
		runTest("2 != 1 + 1;", 0); //$NON-NLS-1$
	}
	
	public void testBracketed() throws Exception {
		runTest("2 * (3 + 4);", 14); //$NON-NLS-1$
	}
}
