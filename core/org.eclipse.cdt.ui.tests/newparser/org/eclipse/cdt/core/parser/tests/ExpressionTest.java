package org.eclipse.cdt.core.parser.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.cdt.internal.core.newparser.Parser;

public class ExpressionTest extends TestCase {

    public static Test suite() {
        return new TestSuite(ExpressionTest.class);
    }

	public ExpressionTest(String name) {
		super(name);
	}

	public void runTest(String code, TestCallback.StateData [] sd) throws Exception {
		TestCallback callback = new TestCallback(sd);
		Parser parser = new Parser(code, callback);
		parser.expression();
		callback.endTest();
	}
	
	public void testInteger() throws Exception {
		TestCallback.StateData [] sd = {
			new TestCallback.StateData(TestCallback.expressionTerminal, "5")
		};
		runTest("5", sd);
	}
	
	public void testBinaryExpression() throws Exception {
		TestCallback.StateData [] sd = {
			new TestCallback.StateData(TestCallback.expressionTerminal, "5"),
			new TestCallback.StateData(TestCallback.expressionTerminal, "6"),
			new TestCallback.StateData(TestCallback.expressionOperator, "<")
		};
		runTest("5 < 6", sd);
	}
	
	public void testBracketedExpression() throws Exception {
		TestCallback.StateData [] sd = {
			new TestCallback.StateData(TestCallback.expressionTerminal, "1"),
			new TestCallback.StateData(TestCallback.expressionTerminal, "2"),
			new TestCallback.StateData(TestCallback.expressionTerminal, "3"),
			new TestCallback.StateData(TestCallback.expressionOperator, "-"),
			new TestCallback.StateData(TestCallback.expressionOperator, "+")
		};
		runTest("1 + (2 - 3)", sd);
	}
}
