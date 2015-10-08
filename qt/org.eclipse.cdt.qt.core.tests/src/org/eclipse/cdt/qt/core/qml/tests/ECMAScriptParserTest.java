/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.qt.core.qml.tests;

import static org.junit.Assert.*;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.eclipse.cdt.internal.qt.core.qml.parser.ECMAScriptLexer;
import org.eclipse.cdt.internal.qt.core.qml.parser.ECMAScriptListener;
import org.eclipse.cdt.internal.qt.core.qml.parser.ECMAScriptParser;
import org.eclipse.cdt.internal.qt.core.qml.parser.ECMAScriptParser.EcmaScriptProgramContext;
import org.junit.Ignore;
import org.junit.Test;

public class ECMAScriptParserTest extends AbstractParserTest {

	public void runParser(CharSequence code, ECMAScriptListener listener) throws Exception {
		ANTLRInputStream input = new ANTLRInputStream(code.toString());

		// Create the lexer
		ECMAScriptLexer lexer = new ECMAScriptLexer(input);
		lexer.addErrorListener(createANTLRErrorListener());
		CommonTokenStream tokens = new CommonTokenStream(lexer);

		// Create and run the parser
		ECMAScriptParser parser = new ECMAScriptParser(tokens);
		parser.addParseListener(listener);
		parser.addErrorListener(createANTLRErrorListener());
		parser.ecmaScriptProgram();
	}

	// identifier;
	@Test
	public void test_Identifier() throws Exception {
		runParser(getComment(), new AbstractECMAScriptListener() {
			@Override
			public void exitEcmaScriptProgram(EcmaScriptProgramContext ctx) {
				String identifier = ctx.sourceElements().sourceElement(0).singleExpression().Identifier().getText();
				assertEquals("identifier", identifier); //$NON-NLS-1$
			}
		});
	}

	// identifier
	@Test
	public void test_ASI() throws Exception {
		runParser(getComment(), new AbstractECMAScriptListener() {
			@Override
			public void exitEcmaScriptProgram(EcmaScriptProgramContext ctx) {
				String identifier = ctx.sourceElements().sourceElement(0).singleExpression().Identifier().getText();
				assertEquals("identifier", identifier); //$NON-NLS-1$
			}
		});
	}

	// identifier /*
	// */
	@Test
	public void test_ASI_MultilineComment() throws Exception {
		runParser(getComment(), new AbstractECMAScriptListener() {
			@Override
			public void exitEcmaScriptProgram(EcmaScriptProgramContext ctx) {
				String identifier = ctx.sourceElements().sourceElement(0).singleExpression().Identifier().getText();
				assertEquals("identifier", identifier); //$NON-NLS-1$
			}
		});
	}

	// return
	// a + 3;
	@Test
	@Ignore // TODO run once proper expressions are in place
	public void test_ASI_ReturnStatement() throws Exception {
		runParser(getComment(), new AbstractECMAScriptListener() {
			@Override
			public void exitEcmaScriptProgram(EcmaScriptProgramContext ctx) {
				// TODO make sure the return statement parsed correctly
			}
		});
	}
}
