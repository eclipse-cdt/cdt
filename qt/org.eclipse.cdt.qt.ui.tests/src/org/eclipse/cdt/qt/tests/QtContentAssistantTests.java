/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.qt.tests;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.qt.ui.assist.QPropertyExpansion;
import org.eclipse.cdt.ui.text.contentassist.ICEditorContentAssistInvocationContext;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.ui.IEditorPart;

import junit.framework.TestCase;

public class QtContentAssistantTests extends TestCase {

	public void testQPropertyProposals() throws Exception {

		String decl = "Q_PROPERTY( type name READ accessor WRITE modifier )";
		int atEnd = decl.length();
		int atRParen = decl.indexOf(')');
		int afterModifier = decl.indexOf("modifier") + "modifier".length();
		int afterWRITE = decl.indexOf("WRITE") + "WRITE".length();
		int inModifier = decl.indexOf("modifier") + 3;
		int inWRITE = decl.indexOf("WRITE") + 3;

		IDocument doc = new Document(decl);

		// The expansion is not applicable when invoked after the closing paren
		QPropertyExpansion exp = QPropertyExpansion.create(new Context(doc, atEnd));
		assertNull(exp);

		exp = QPropertyExpansion.create(new Context(doc, atRParen));
		assertNotNull(exp);
		assertNull(exp.getCurrIdentifier());
		assertEquals("modifier", exp.getPrevIdentifier());

		exp = QPropertyExpansion.create(new Context(doc, afterModifier));
		assertNotNull(exp);
		assertEquals("modifier", exp.getCurrIdentifier());
		assertEquals("WRITE", exp.getPrevIdentifier());

		exp = QPropertyExpansion.create(new Context(doc, afterWRITE));
		assertNotNull(exp);
		assertEquals("WRITE", exp.getCurrIdentifier());
		assertEquals("accessor", exp.getPrevIdentifier());

		exp = QPropertyExpansion.create(new Context(doc, inModifier));
		assertNotNull(exp);
		assertEquals("modifier", exp.getCurrIdentifier());
		assertEquals("WRITE", exp.getPrevIdentifier());

		exp = QPropertyExpansion.create(new Context(doc, inWRITE));
		assertNotNull(exp);
		assertEquals("WRITE", exp.getCurrIdentifier());
		assertEquals("accessor", exp.getPrevIdentifier());
	}

	public void testQPropertyWithoutLeadingWhitespace() throws Exception {

		String decl = "Q_PROPERTY(type name READ accessor )";
		int atRParen = decl.indexOf(')');
		IDocument doc = new Document(decl);

		// The expansion should be created even when there is no leading whitesapce in the
		// expansion parameter.
		QPropertyExpansion exp = QPropertyExpansion.create(new Context(doc, atRParen));
		assertNotNull(exp);
	}

	public void testQPropertyPrefixes() throws Exception {
		String decl = "Q_PROPERTY( type name READ accessor WRITE  )";
		int len = decl.length();
		IDocument doc = new Document(decl);

		// The expansion is not applicable when invoked after the closing paren
		QPropertyExpansion atEnd = QPropertyExpansion.create(new Context(doc, len));
		assertNull(atEnd);

		QPropertyExpansion inWS = QPropertyExpansion.create(new Context(doc, len - 2));
		assertNotNull(inWS);
		assertNull(inWS.getPrefix());
		assertNull(inWS.getCurrIdentifier());
		assertEquals("WRITE", inWS.getPrevIdentifier());

		QPropertyExpansion afterWRITE = QPropertyExpansion.create(new Context(doc, len - 3));
		assertNotNull(afterWRITE);
		assertEquals("WRITE", afterWRITE.getPrefix());
		assertEquals("WRITE", afterWRITE.getCurrIdentifier());
		assertEquals("accessor", afterWRITE.getPrevIdentifier());

		QPropertyExpansion inWRITE_e = QPropertyExpansion.create(new Context(doc, len - 4));
		assertNotNull(inWRITE_e);
		assertEquals("WRIT", inWRITE_e.getPrefix());
		assertEquals("WRITE", inWRITE_e.getCurrIdentifier());
		assertEquals("accessor", inWRITE_e.getPrevIdentifier());

		QPropertyExpansion inWRITE_b = QPropertyExpansion.create(new Context(doc, len - 6));
		assertNotNull(inWRITE_b);
		assertEquals("WR", inWRITE_b.getPrefix());
		assertEquals("WRITE", inWRITE_b.getCurrIdentifier());
		assertEquals("accessor", inWRITE_b.getPrevIdentifier());

		QPropertyExpansion startWRITE = QPropertyExpansion.create(new Context(doc, len - 8));
		assertNotNull(startWRITE);
		assertNull(startWRITE.getPrefix());
		assertNull(startWRITE.getCurrIdentifier());
		assertEquals("accessor", startWRITE.getPrevIdentifier());
	}

	// This implements only the parts that are known to be used in the QPropertyExpansion
	// implementation.
	private static class Context implements ICEditorContentAssistInvocationContext {

		private final IDocument doc;
		private final int contextOffset;
		private final int invokedOffset;

		public Context(IDocument doc, int invoked) {
			this.doc = doc;
			this.contextOffset = doc.get().indexOf('(') + 1;
			this.invokedOffset = invoked;
		}

		@Override
		public int getInvocationOffset() {
			return invokedOffset;
		}

		@Override
		public int getContextInformationOffset() {
			return contextOffset;
		}

		@Override
		public IDocument getDocument() {
			return doc;
		}

		@Override
		public boolean isContextInformationStyle() {
			return false;
		}

		@Override
		public ITextViewer getViewer() {
			return null;
		}

		@Override
		public ITranslationUnit getTranslationUnit() {
			return null;
		}

		@Override
		public ICProject getProject() {
			return null;
		}

		@Override
		public int getParseOffset() {
			return 0;
		}

		@Override
		public IEditorPart getEditor() {
			return null;
		}

		@Override
		public IASTCompletionNode getCompletionNode() {
			return null;
		}

		@Override
		public CharSequence computeIdentifierPrefix() throws BadLocationException {
			return null;
		}
	}
}
