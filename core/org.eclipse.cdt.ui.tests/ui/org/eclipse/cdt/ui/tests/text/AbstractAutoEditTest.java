/*******************************************************************************
 * Copyright (c) 2006, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Andrew Ferguson (Symbian)
 *     Andrew Gvozdev
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;

import org.eclipse.cdt.core.testplugin.util.BaseTestCase;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;

/**
 * IAutoEditStrategy related tests
 */
public class AbstractAutoEditTest extends BaseTestCase {
	
	protected AbstractAutoEditTest(String name) {
		super(name);
	}
	
	/**
	 * Helper class to test the auto-edit strategies on a document.
	 * Split out from CAutoIndentTest.
	 */
	protected static class AutoEditTester {
		private IAutoEditStrategy tabsToSpacesConverter;
		private Map<String, IAutoEditStrategy> fStrategyMap = new HashMap<String, IAutoEditStrategy>();
		IDocument fDoc;
		private String fPartitioning;
		private int fCaretOffset;

		public AutoEditTester(IDocument doc, String partitioning) {
			super();
			fDoc = doc;
			fPartitioning = partitioning;
		}

		public void setTabsToSpacesConverter(IAutoEditStrategy converter) {
			tabsToSpacesConverter = converter;
		}

		public void setAutoEditStrategy(String contentType, IAutoEditStrategy aes) {
			fStrategyMap.put(contentType, aes);
		}

		public IAutoEditStrategy getAutoEditStrategy(String contentType) {
			return fStrategyMap.get(contentType);
		}

		/**
		 * Empties the document, and returns the caret to the origin (0,0)
		 * @return <code>this</code> for method chaining
		 */
		public AutoEditTester reset() {
			try {
				goTo(0,0);
				fDoc.set("");
			} catch(BadLocationException ble) {
				fail(ble.getMessage());
			}
			return this;
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
			if (tabsToSpacesConverter != null) {
				tabsToSpacesConverter.customizeDocumentCommand(fDoc, command);
			}
			IAutoEditStrategy aes = getAutoEditStrategy(getContentType(command.offset));
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

		public String getContentType(int offset) throws BadLocationException {
			return TextUtilities.getContentType(fDoc, fPartitioning, offset, true);
		}
	}
	
	/**
	 * A DocumentCommand with public constructor and exec method.
	 */
	protected static class TestDocumentCommand extends DocumentCommand {

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
		 * @param doc
		 * @return the new caret position.
		 * @throws BadLocationException
		 */
		public int exec(IDocument doc) throws BadLocationException {
			doc.replace(offset, length, text);
			return caretOffset != -1 ?
						caretOffset :
						offset + (text == null ? 0 : text.length());
		}
	}
	
	protected CharSequence[] getTestContents() {
		try {
			return TestSourceReader.getContentsForTest(CTestPlugin.getDefault().getBundle(), "ui", this.getClass(), getName(), 2);
		} catch (IOException e) {
			fail(e.getMessage());
		}
		return null;
	}
	
	protected CharSequence[] getTestContents1() {
		try {
			return TestSourceReader.getContentsForTest(CTestPlugin.getDefault().getBundle(), "ui", this.getClass(), getName(), 1);
		} catch (IOException e) {
			fail(e.getMessage());
		}
		return null;
	}
}
