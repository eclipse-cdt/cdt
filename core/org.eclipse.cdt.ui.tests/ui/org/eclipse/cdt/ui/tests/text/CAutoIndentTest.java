/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;

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
			for (int i=0; i<n; ++i) {
				backspace();
			}
		}
		public void backspace() throws BadLocationException {
			TestDocumentCommand command = new TestDocumentCommand(fCaretOffset-1, 1, ""); //$NON-NLS-1$
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
			IRegion region = fDoc.getLineInformation(getCaretLine()+i);
			return fDoc.get(region.getOffset(), region.getLength());
		}

		public String getContentType() throws BadLocationException {
			return getContentType(0);
		}

		public String getContentType(int i) throws BadLocationException {
			return TextUtilities.getContentType(fDoc, fPartitioning, fCaretOffset+i, false);
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
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();  
		shell.forceActive();
		shell.forceFocus();
	}
	
	private AutoEditTester createAutoEditTester() {
		CTextTools textTools = CUIPlugin.getDefault().getTextTools();
		IDocument doc = new Document();
		textTools.setupCDocument(doc);
		AutoEditTester tester = new AutoEditTester(doc, textTools.getDocumentPartitioning());
		tester.setAutoEditStrategy(IDocument.DEFAULT_CONTENT_TYPE,
				                   new CAutoIndentStrategy(textTools.getDocumentPartitioning(), null));
		tester.setAutoEditStrategy(ICPartitions.C_MULTI_LINE_COMMENT, new CCommentAutoIndentStrategy());
		return tester;
	}

	public void testCAutoIndent() throws IOException, CoreException, BadLocationException {
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

	public void testDefaultAutoIndent() throws IOException, CoreException, BadLocationException {
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

	public void testCCommentAutoIndent() throws IOException, CoreException, BadLocationException {
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
		// TODO: indent is one space - should be no indent
//		assertEquals("", tester.getLine()); //$NON-NLS-1$
//		assertEquals(0, tester.getCaretColumn());
	}

}
