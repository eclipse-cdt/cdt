package org.eclipse.cdt.qt.qml.core.tests;

import static org.junit.Assert.fail;

import java.util.BitSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.dfa.DFA;
import org.eclipse.cdt.qt.qml.core.parser.QMLLexer;
import org.eclipse.cdt.qt.qml.core.parser.QMLListener;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser;
import org.eclipse.cdt.qt.qml.core.parser.QMLParser.QmlProgramContext;
import org.junit.Test;

public class QMLParserTest extends AbstractParserTest {

	public void runParser(String code, QMLListener listener) throws Exception {
		ANTLRInputStream input = new ANTLRInputStream(code);
		QMLLexer lexer = new QMLLexer(input);
		lexer.addErrorListener(new ANTLRErrorListener() {

			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				fail(msg);
			}

			@Override
			public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
					int prediction, ATNConfigSet configs) {
				// TODO Auto-generated method stub

			}

			@Override
			public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
					BitSet conflictingAlts, ATNConfigSet configs) {
				// TODO Auto-generated method stub

			}

			@Override
			public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
					BitSet ambigAlts, ATNConfigSet configs) {
				// TODO Auto-generated method stub

			}
		});
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		QMLParser parser = new QMLParser(tokens);
		parser.addParseListener(listener);
		parser.addErrorListener(new ANTLRErrorListener() {
			@Override
			public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
					int charPositionInLine, String msg, RecognitionException e) {
				fail(msg);
			}

			@Override
			public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
					int prediction, ATNConfigSet configs) {
			}

			@Override
			public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
					BitSet conflictingAlts, ATNConfigSet configs) {
			}

			@Override
			public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
					BitSet ambigAlts, ATNConfigSet configs) {
			}
		});
		parser.qmlProgram();
	}

	// testCode
	@Test
	public void testCodeExtract() throws Exception {
		runParser(extract(), new AbstractQMLListener() {
			@Override
			public void exitQmlProgram(QmlProgramContext ctx) {

			}
		});
	}

}
