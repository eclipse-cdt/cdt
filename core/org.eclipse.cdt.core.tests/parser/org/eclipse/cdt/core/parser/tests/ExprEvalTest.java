package org.eclipse.cdt.core.parser.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.internal.core.parser.ExpressionEvaluator;
import org.eclipse.cdt.internal.core.parser.Parser;

public class ExprEvalTest extends TestCase {

	public static Test suite() {
		return new TestSuite(ExprEvalTest.class);
	}
	
	public ExprEvalTest(String name) {
		super(name);
	}
	
	public void runTest(String code, int expectedValue) throws Exception {
		ExpressionEvaluator evaluator = new ExpressionEvaluator();
		IParser parser = new Parser(code, evaluator);
		parser.expression(null);
		assertEquals(expectedValue, ((Integer)evaluator.getResult()).intValue());
	}
	
	public void testInteger() throws Exception {
		runTest("5;", 5);
	}
	
	public void testRelational() throws Exception {
		runTest("1 < 2;", 1);
		runTest("2 < 1;", 0);
		runTest("2 == 1 + 1;", 1);
		runTest("2 != 1 + 1;", 0);
	}
	
	public void testBracketed() throws Exception {
		runTest("2 * (3 + 4);", 14);
	}
}
