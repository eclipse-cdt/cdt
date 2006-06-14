/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

/*
 * Created on Sept 30, 2004
 */
package org.eclipse.cdt.core.parser.tests;

import java.io.StringWriter;
import java.io.Writer;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.ISourceElementRequestor;
import org.eclipse.cdt.core.parser.NullLogService;
import org.eclipse.cdt.core.parser.NullSourceElementRequestor;
import org.eclipse.cdt.core.parser.ParseError;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.cdt.core.parser.ast.IASTMacro;
import org.eclipse.cdt.core.parser.ast.IASTVariable;

/**
 * @author dsteffle
 */
public class ScannerParserLoopTest extends FileBasePluginTest {
	private static final int NUMBER_ITERATIONS = 30000;

	public ScannerParserLoopTest(String name) {
		super(name, ScannerParserLoopTest.class);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(ScannerParserLoopTest.class);
		suite.addTest(new ScannerParserLoopTest("cleanupProject")); //$NON-NLS-1$
		return suite;
	}

	// test scanner cancel()
	public void testBug72611A() throws Exception {
		Writer writer = new StringWriter();

		for (int i = 0; i < NUMBER_ITERATIONS; i++) {
			writer.write("#define A");
			writer.write(String.valueOf(i));
			writer.write(" B");
			writer.write(String.valueOf(i));
			writer.write("\n");
			writer.write("#define B");
			writer.write(String.valueOf(i));
			writer.write(" C");
			writer.write(String.valueOf(i));
			writer.write("\n");
			writer.write("#define C");
			writer.write(String.valueOf(i));
			writer.write(" D");
			writer.write(String.valueOf(i));
			writer.write("\n");
		}

		runCancelTest(writer);
	}

	// test parser cancel()
	public void testBug72611B() throws Exception {
		Writer writer = new StringWriter();

		for (int i = 0; i < NUMBER_ITERATIONS; i++) {
			writer.write("int a");
			writer.write(String.valueOf(i));
			writer.write("; // comment\n");
		}

		runCancelTest(writer);
	}

	private void runCancelTest(Writer writer) throws Exception {
		IFile file = importFile("code.cpp", writer.toString()); //$NON-NLS-1$

		try {
			TimeoutCallback callback = new TimeoutCallback();
			IParser parser = ParserFactory.createParser(ParserFactory
					.createScanner(new CodeReader(file.getRawLocation()
							.toString()), new ScannerInfo(), //$NON-NLS-1$
							ParserMode.COMPLETE_PARSE, ParserLanguage.CPP,
							callback, new NullLogService(), null), callback,
					ParserMode.COMPLETE_PARSE, ParserLanguage.CPP, null);

			callback.setParser(parser);
			parser.parse();

			assertTrue(false); // fail if parse succeeds before being cancelled
		} catch (ParseError pe) { // expected
			assertEquals(pe.getErrorKind(),
					ParseError.ParseErrorKind.TIMEOUT_OR_CANCELLED);
		}
	}

	private static class TimeoutCallback extends NullSourceElementRequestor
			implements ISourceElementRequestor {
		private IParser parser;
		private boolean timerStarted = false;

		public void setParser(IParser parser) {
			this.parser = parser;
		}

		public void acceptMacro(IASTMacro macro) {
			parser.cancel();
		}
		
		public void acceptVariable(IASTVariable variable) {
			parser.cancel();
		}
	}
}
