package org.eclipse.cdt.qt.qml.core.tests;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.cdt.qt.qml.core.parser.QMLLexer;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser;
import org.junit.Test;

@SuppressWarnings("nls")
public class QMLParserTest {

	@Test
	public void test() throws Exception {
		String path = "/Users/dschaefer/Qt/5.5/clang_64/qml/QtTest/TestCase.qml";
		ANTLRFileStream input = new ANTLRFileStream(path);
		QMLLexer lexer = new QMLLexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		QMLParser parser = new QMLParser(tokens);

		long start = System.currentTimeMillis();
		parser.qmlProgram();
		System.out.println("time: " + (System.currentTimeMillis() - start) + "ms.");
	}

}
