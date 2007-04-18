/*******************************************************************************
 * Copyright (c) 2006, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Sergey Prigogin, Google
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.text;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.formatter.DefaultCodeFormatterOptions;

import org.eclipse.cdt.internal.ui.text.CAutoIndentStrategy;
import org.eclipse.cdt.internal.ui.text.CCommentAutoIndentStrategy;
import org.eclipse.cdt.internal.ui.text.CTextTools;

/**
 * Testing the auto indent strategies.
 */
public class CAutoIndentTest extends TestCase {

	/**
	 * Helper class to test the auto-edit strategies on a document.
	 */
	static class AutoEditTester {

		private Map fStrategyMap = new HashMap();
		private IDocument fDoc;
		private String fPartitioning;
		private int fCaretOffset;

		public AutoEditTester(IDocument doc, String partitioning) {
			super();
			fDoc = doc;
			fPartitioning = partitioning;
		}

		public void setAutoEditStrategy(String contentType, IAutoEditStrategy aes) {
			fStrategyMap.put(contentType, aes);
		}

		public IAutoEditStrategy getAutoEditStrategy(String contentType) {
			return (IAutoEditStrategy)fStrategyMap.get(contentType);
		}

		/**
		 * Empties the document, and returns the caret to the origin (0,0)
		 */
		public void reset() {
			try {
				goTo(0,0);
				fDoc.set("");
			} catch(BadLocationException ble) {
				fail(ble.getMessage());
			}
		}
		
		public void type(String text) throws BadLocationException {
			for (int i = 0; i < text.length(); ++i) {
				type(text.charAt(i));
			}
		}

		public void type(char c) throws BadLocationException {
			TestDocumentCommand command = new TestDocumentCommand(fCaretOffset, 0, new String(new char[] { c }));
			customizeDocumentCommand(command);
			fCaretOffset = command.exec(fDoc);
		}

		private void customizeDocumentCommand(TestDocumentCommand command) throws BadLocationException {
			IAutoEditStrategy aes = getAutoEditStrategy(getContentType());
			if (aes != null) {
				aes.customizeDocumentCommand(fDoc, command);
			}
		}

		public void type(int offset, String text) throws BadLocationException {
			fCaretOffset = offset;
			type(text);
		}

		public void type(int offset, char c) throws BadLocationException {
			fCaretOffset = offset;
			type(c);
		}

		public void paste(String text) throws BadLocationException {
			TestDocumentCommand command = new TestDocumentCommand(fCaretOffset, 0, text);
			customizeDocumentCommand(command);
			fCaretOffset = command.exec(fDoc);
		}

		public void paste(int offset, String text) throws BadLocationException {
			fCaretOffset = offset;
			paste(text);
		}

		public void backspace(int n) throws BadLocationException {
			for (int i = 0; i < n; ++i) {
				backspace();
			}
		}
		
		public void backspace() throws BadLocationException {
			TestDocumentCommand command = new TestDocumentCommand(fCaretOffset - 1, 1, ""); //$NON-NLS-1$
			customizeDocumentCommand(command);
			fCaretOffset = command.exec(fDoc);
		}

		public int getCaretOffset() {
			return fCaretOffset;
		}

		public int setCaretOffset(int offset) {
			fCaretOffset = offset;
			if (fCaretOffset < 0)
				fCaretOffset = 0;
			else if (fCaretOffset > fDoc.getLength())
				fCaretOffset = fDoc.getLength();
			return fCaretOffset;
		}
		
		/**
		 * Moves caret right or left by the given number of characters.
		 * 
		 * @param shift Move distance.
		 * @return New caret offset.
		 */
		public int moveCaret(int shift) {
			return setCaretOffset(fCaretOffset + shift);
		}
		
		public int goTo(int line) throws BadLocationException {
			fCaretOffset = fDoc.getLineOffset(line);
			return fCaretOffset;
		}

		public int goTo(int line, int column) throws BadLocationException {
			if (column < 0 || column > fDoc.getLineLength(line)) {
				throw new BadLocationException("No column " + column + " in line " + line); //$NON-NLS-1$ $NON-NLS-2$
			}
			fCaretOffset = fDoc.getLineOffset(line) + column;
			return fCaretOffset;
		}

		public int getCaretLine() throws BadLocationException {
			return fDoc.getLineOfOffset(fCaretOffset);
		}

		public int getCaretColumn() throws BadLocationException {
			IRegion region = fDoc.getLineInformationOfOffset(fCaretOffset);
			return fCaretOffset - region.getOffset();
		}

		public char getChar() throws BadLocationException {
			return getChar(0);
		}
		
		public char getChar(int i) throws BadLocationException {
			return fDoc.getChar(fCaretOffset+i);
		}
		
		public String getLine() throws BadLocationException {
			return getLine(0);
		}

		public String getLine(int i) throws BadLocationException {
			IRegion region = fDoc.getLineInformation(getCaretLine() + i);
			return fDoc.get(region.getOffset(), region.getLength());
		}

		public String getContentType() throws BadLocationException {
			return getContentType(0);
		}

		public String getContentType(int i) throws BadLocationException {
			return TextUtilities.getContentType(fDoc, fPartitioning, fCaretOffset + i, false);
		}
	}

	/**
	 * A DocumentCommand with public constructor and exec method.
	 */
	static class TestDocumentCommand extends DocumentCommand {

		public TestDocumentCommand(int offset, int length, String text) {
			super();
			doit = true;
			this.text = text;

			this.offset = offset;
			this.length = length;

			owner = null;
			caretOffset = -1;
		}

		/**
		 * Returns new caret position.
		 */
		public int exec(IDocument doc) throws BadLocationException {
			doc.replace(offset, length, text);
			return caretOffset != -1 ?
						caretOffset :
						offset + (text == null ? 0 : text.length());
		}
	}

	private HashMap fOptions;

	
	/**
	 * @param name
	 */
	public CAutoIndentTest(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(CAutoIndentTest.class);
	}

	protected void setUp() throws Exception {
		super.setUp();
//		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();  
//		shell.forceActive();
//		shell.forceFocus();
		fOptions= CCorePlugin.getOptions();
	}
	
	/*
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		CCorePlugin.setOptions(fOptions);
		super.tearDown();
	}

	private AutoEditTester createAutoEditTester() {
		CTextTools textTools = CUIPlugin.getDefault().getTextTools();
		IDocument doc = new Document();
		textTools.setupCDocument(doc);
		AutoEditTester tester = new AutoEditTester(doc, ICPartitions.C_PARTITIONING);
		tester.setAutoEditStrategy(IDocument.DEFAULT_CONTENT_TYPE, new CAutoIndentStrategy(ICPartitions.C_PARTITIONING, null));
		tester.setAutoEditStrategy(ICPartitions.C_MULTI_LINE_COMMENT, new CCommentAutoIndentStrategy());
		tester.setAutoEditStrategy(ICPartitions.C_PREPROCESSOR, new CAutoIndentStrategy(ICPartitions.C_PARTITIONING, null));
		return tester;
	}

	public void testCAutoIndent() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		tester.type("void main() {\n"); //$NON-NLS-1$
		assertEquals(1, tester.getCaretLine());
		// Nested statement is indented by one.
		assertEquals(1, tester.getCaretColumn());
		// The brace was closed automatically.
		assertEquals("}", tester.getLine(1)); //$NON-NLS-1$
		tester.type("if (expression1 &&\n"); //$NON-NLS-1$
		assertEquals(2, tester.getCaretLine());
		// Continuation line is indented by two relative to the statement.
		assertEquals(3, tester.getCaretColumn());
		tester.type("expression2 &&\n"); //$NON-NLS-1$
		assertEquals(3, tester.getCaretLine());
		// Second continuation line is also indented by two relative to the statement.
		assertEquals(3, tester.getCaretColumn());
		tester.type("expression3) {"); //$NON-NLS-1$
		// Remember caret position.
		int offset = tester.getCaretOffset();
		// Press Enter
        tester.type("\n");  //$NON-NLS-1$
		assertEquals(4, tester.getCaretLine());
		// Nested statement is indented by one relative to the containing statement.
		assertEquals(2, tester.getCaretColumn());
		// The brace was closed automatically.
		assertEquals("\t}", tester.getLine(1)); //$NON-NLS-1$
		tester.type("int x = 5;"); //$NON-NLS-1$
		// Move caret back after the opening brace.
		tester.setCaretOffset(offset);
		// Press Enter
        tester.type("\n"); //$NON-NLS-1$
		assertEquals(4, tester.getCaretLine());
		// Nested statement is indented by one relative to the containing statement.
		assertEquals(2, tester.getCaretColumn());
        // No auto closing brace since the braces are already balanced.
		assertEquals("\t\tint x = 5;", tester.getLine(1)); //$NON-NLS-1$
	}

	public void testPasteAutoIndent() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		tester.type("class A {\n"); //$NON-NLS-1$
		tester.goTo(1, 0);
		tester.paste("class B {\n" +
				     "protected:\n" +
				     "\tB();\n" +
				     "public:\n" +
				     "\tint getX() const {\n" +
				     "\t\treturn x_;\n" +
				     "\t}\n" +
				     "private:\n" +
				     "\tint x_;\n" +
				     "};\n"); //$NON-NLS-1$
		tester.goTo(1, 0);
		assertEquals("\tclass B {", tester.getLine(0)); //$NON-NLS-1$
		assertEquals("\tprotected:", tester.getLine(1)); //$NON-NLS-1$
		assertEquals("\t\tB();", tester.getLine(2)); //$NON-NLS-1$
		assertEquals("\tpublic:", tester.getLine(3)); //$NON-NLS-1$
		assertEquals("\t\tint getX() const {", tester.getLine(4)); //$NON-NLS-1$
		assertEquals("\t\t\treturn x_;", tester.getLine(5)); //$NON-NLS-1$
		assertEquals("\t\t}", tester.getLine(6)); //$NON-NLS-1$
		assertEquals("\tprivate:", tester.getLine(7)); //$NON-NLS-1$
		assertEquals("\t\tint x_;", tester.getLine(8)); //$NON-NLS-1$
		assertEquals("\t};", tester.getLine(9)); //$NON-NLS-1$
	}

	public void testDefaultAutoIndent() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		tester.type("   initial indent=3\n"); //$NON-NLS-1$
		assertEquals(1, tester.getCaretLine());
		assertEquals(3, tester.getCaretColumn());
		tester.type("indent=3\n"); //$NON-NLS-1$
		assertEquals(2, tester.getCaretLine());
		assertEquals(3, tester.getCaretColumn());
		tester.backspace();
		tester.type("indent=2\n"); //$NON-NLS-1$
		assertEquals(3, tester.getCaretLine());
		assertEquals(2, tester.getCaretColumn());
		tester.backspace();
		tester.backspace();
		tester.type("indent=0\n"); //$NON-NLS-1$
		assertEquals(4, tester.getCaretLine());
		assertEquals(0, tester.getCaretColumn());
		tester.type("\n"); //$NON-NLS-1$
		assertEquals(5, tester.getCaretLine());
		assertEquals(0, tester.getCaretColumn());
	}

	public void testCCommentAutoIndent() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		tester.type("/*\n"); //$NON-NLS-1$
		assertEquals(ICPartitions.C_MULTI_LINE_COMMENT, tester.getContentType(-1));
		assertEquals(1, tester.getCaretLine());
		assertEquals(3, tester.getCaretColumn());
		assertEquals(" * ", tester.getLine()); //$NON-NLS-1$
		tester.type('\n');
		assertEquals(" * ", tester.getLine()); //$NON-NLS-1$
		tester.type('/');
		assertEquals(" */", tester.getLine()); //$NON-NLS-1$
		tester.type('\n');
		assertEquals(3, tester.getCaretLine());
		assertEquals("", tester.getLine()); //$NON-NLS-1$
		assertEquals(0, tester.getCaretColumn());
	}

	public void testPreprocessorAutoIndent() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		tester.type("void main() {\n"); //$NON-NLS-1$
		assertEquals(1, tester.getCaretLine());
		// Nested statement is indented by one.
		assertEquals(1, tester.getCaretColumn());
		// The brace was closed automatically.
		assertEquals("}", tester.getLine(1)); //$NON-NLS-1$
		tester.type("#define"); //$NON-NLS-1$
		assertEquals("#define", tester.getLine()); //$NON-NLS-1$
		tester.type(" FOREVER \\\n");
		assertEquals(1, tester.getCaretColumn());
		tester.type("for(;;) \\\n");
		assertEquals(1, tester.getCaretColumn());
		tester.type("\t{");
		assertEquals(2, tester.getCaretColumn());
		assertEquals("\t{", tester.getLine());
		tester.type("\\\n");
		assertEquals(2, tester.getCaretColumn());
		assertEquals("\t}", tester.getLine(1));
	}

	public void testPasteBlockCommentAutoIndent() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		tester.type("class A {\n};"); //$NON-NLS-1$
		tester.goTo(1, 0);
		tester.paste("/*\n" +
				     " * block comment\n" +
				     " */\n");
		tester.goTo(1, 0);
		assertEquals("\t/*", tester.getLine(0)); //$NON-NLS-1$
		assertEquals("\t * block comment", tester.getLine(1)); //$NON-NLS-1$
		assertEquals("\t */", tester.getLine(2)); //$NON-NLS-1$
	}

	public void testPasteLineCommentAutoIndent() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		tester.type("class A {\n};"); //$NON-NLS-1$
		tester.goTo(1, 0);
		tester.paste("// int f;\n");
		tester.goTo(1, 0);
		assertEquals("\t// int f;", tester.getLine(0)); //$NON-NLS-1$
	}

	/**
	 * Tests brackets with semi-colons are inserted in the appropriate
	 * contexts
	 * @throws BadLocationException
	 */
	public void testBracketWithSemiColonInsertion() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester(); 
		String[] kw= new String[] {"class", "union", "struct", "enum"};
		String[] kw_anon= new String[] {"union", "struct", "enum"};

		for(int i=0; i<kw.length; i++) {
			tester.reset();

			tester.type("\n\n\n "+kw[i]+" A {\n"); //$NON-NLS-1$
			assertEquals("\n\n\n "+kw[i]+" A {\n\t \n };", tester.fDoc.get()); //$NON-NLS-1$
		}
		
		for(int i=0; i<kw.length; i++) {
			tester.reset();

			tester.type(kw[i]+" A {\n"); //$NON-NLS-1$
			assertEquals(kw[i]+" A {\n\t\r\n};", tester.fDoc.get()); //$NON-NLS-1$
		}
		
		for(int i=0; i<kw.length; i++) {		
			tester.reset();

			tester.type("\n\n\n "+kw[i]+" A {\n"); //$NON-NLS-1$
			assertEquals("\n\n\n "+kw[i]+" A {\n\t \n };", tester.fDoc.get()); //$NON-NLS-1$
		}
		
		for(int i=0; i<kw.length; i++) {		
			tester.reset();

			tester.type("\n// foo\n\n\n//bar\n\n"); //$NON-NLS-1$
			tester.goTo(2,0);
			tester.type(kw[i]+" A {\n"); //$NON-NLS-1$
			assertEquals("\n// foo\n"+kw[i]+" A {\n\t\n};\n\n//bar\n\n", tester.fDoc.get()); //$NON-NLS-1$
		}

		// this tests for a sensible behaviour for enums, although the
		// code generated is invalid, its the user entered part that is
		// the problem
		for(int i=0; i<kw.length; i++) {		
			tester.reset();

			tester.type("\n\n\n"+kw[i]+" A\n:\npublic B\n,\npublic C\n{\n"); //$NON-NLS-1$
			assertEquals("\n\n\n"+kw[i]+" A\n:\n\tpublic B\n\t,\n\tpublic C\n\t{\n\t\n\t};", tester.fDoc.get()); //$NON-NLS-1$
		}
		
		for(int i=0; i<kw.length; i++) {		
			tester.reset();

			tester.type("\n// foo\n\n\n//bar\n\n"); //$NON-NLS-1$
			tester.goTo(2,0);
			tester.type(kw[i]+" /* for(int i=0; i<100; i++) {} */\nA \n{\n"); //$NON-NLS-1$
			assertEquals("\n// foo\n"+kw[i]+" /* for(int i=0; i<100; i++) {} */\nA \n{\n\t\n};\n\n//bar\n\n", tester.fDoc.get()); //$NON-NLS-1$
		}		

		for(int i=0; i<kw_anon.length; i++) {		
			tester.reset();

			tester.type("\n\n\n"+kw_anon[i]+" {\n"); //$NON-NLS-1$
			assertEquals("\n\n\n"+kw_anon[i]+" {\n\t\n};", tester.fDoc.get()); //$NON-NLS-1$
		}
}
	
	/**
	 * Tests that brackets are inserted (without semi-colons) in appropriate
	 * contexts
	 * @throws BadLocationException
	 */
	public void testBracketInsertion() throws BadLocationException {
		AutoEditTester tester = createAutoEditTester();
		
		tester.type("for (;;) {\n");
		assertEquals("for (;;) {\n\t\r\n}", tester.fDoc.get()); //$NON-NLS-1$
		
		tester.reset();
		tester.type("for /*class*/ (;;) {\n"); //$NON-NLS-1$
		assertEquals("for /*class*/ (;;) {\n\t\r\n}", tester.fDoc.get()); //$NON-NLS-1$	
		
		tester.reset();
		tester.type("for (;;) /*class*/ {\n"); //$NON-NLS-1$
		assertEquals("for (;;) /*class*/ {\n\t\r\n}", tester.fDoc.get()); //$NON-NLS-1$

		tester.reset();
		tester.type("int i[5]={\n"); //$NON-NLS-1$
		assertEquals("int i[5]={\n\t\t\r\n};", tester.fDoc.get()); //$NON-NLS-1$
	}
	
	public void testSmartPasteWhitesmiths_Bug180531() throws Exception {
		DefaultCodeFormatterOptions whitesmiths= DefaultCodeFormatterOptions.getWhitesmithsSettings();
		CCorePlugin.setOptions(new HashMap(whitesmiths.getMap()));
		AutoEditTester tester = createAutoEditTester(); //$NON-NLS-1$
		
		tester.type("A::~A()\n{");
		assertEquals("A::~A()\n    {", tester.fDoc.get());
		tester.type("\ndelete x;");
		assertEquals("A::~A()\n    {\n    delete x;\n    }", tester.fDoc.get());
		
		tester.setCaretOffset(tester.fDoc.getLength());
		tester.type('\n');
		String copy= tester.fDoc.get();
		tester.paste(copy);
		assertEquals(copy+copy, tester.fDoc.get());
	}
}

