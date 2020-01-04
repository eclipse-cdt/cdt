/*******************************************************************************
 * Copyright (c) 2008, 2016 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 * Martin Stumpf - adapted orginal to cope with single line comments
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools.doxygen;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension4;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;

/**
 * {@link IAutoEditStrategy} for adding Doxygen tags for comments.
 *
 * @since 5.11
 * @noextend This class is not intended to be subclassed by clients.
 */
public class DoxygenSingleAutoEditStrategy extends DoxygenMultilineAutoEditStrategy {
	private static final String SLASH_COMMENT = "///"; //$NON-NLS-1$
	private static final String EXCL_COMMENT = "//!"; //$NON-NLS-1$
	private static String fgDefaultLineDelim = "\n"; //$NON-NLS-1$

	public DoxygenSingleAutoEditStrategy() {
	}

	/**
	 * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(org.eclipse.jface.text.IDocument, org.eclipse.jface.text.DocumentCommand)
	 */
	@Override
	public void customizeDocumentCommand(IDocument doc, DocumentCommand cmd) {
		fgDefaultLineDelim = TextUtilities.getDefaultLineDelimiter(doc);
		if (doc instanceof IDocumentExtension4) {
			boolean forNewLine = cmd.length == 0 && cmd.text != null && endsWithDelimiter(doc, cmd.text);

			if (forNewLine) {
				IDocumentExtension4 ext4 = (IDocumentExtension4) doc;
				DocumentRewriteSession drs = ext4.startRewriteSession(DocumentRewriteSessionType.UNRESTRICTED_SMALL);
				try {
					customizeDocumentAfterNewLine(doc, cmd);
				} finally {
					ext4.stopRewriteSession(drs);
				}
			}
		}
	}

	@Override
	public void customizeDocumentAfterNewLine(IDocument doc, final DocumentCommand c) {
		int offset = c.offset;
		if (offset == -1 || doc.getLength() == 0)
			return;

		final StringBuilder buf = new StringBuilder(c.text);
		try {
			IRegion line = doc.getLineInformationOfOffset(c.offset);
			String lineDelimiter = doc.getLineDelimiter(doc.getLineOfOffset(c.offset));
			int lineDelimiterLength = lineDelimiter.length();

			IRegion prefix = findPrefixRange(doc, line);
			String indentationWithPrefix = doc.get(prefix.getOffset(), prefix.getLength());
			String commentPrefix = getCommentPrefix(indentationWithPrefix);
			String commentContent = doc.get(prefix.getOffset() + prefix.getLength(),
					line.getLength() - prefix.getLength());
			//String commentContentBeforeCursor = doc.get(prefix.getOffset() + prefix.getLength(),
			//		c.offset - line.getOffset() - prefix.getLength());
			String commentContentBehindCursor = doc.get(c.offset, line.getLength() - (c.offset - line.getOffset()));

			buf.append(indentationWithPrefix);

			boolean commentAtStart = prefix.getOffset() + prefix.getLength() <= c.offset;
			boolean commentFollows = false;
			boolean commentAhead = false;
			boolean firstLineContainsText = commentContent.trim().length() > 0;

			if (commentAtStart) {
				if (line.getOffset() + line.getLength() + lineDelimiterLength < doc.getLength()) {
					IRegion nextLine = doc
							.getLineInformationOfOffset(line.getOffset() + line.getLength() + lineDelimiterLength);
					commentFollows = doc.get(nextLine.getOffset(), nextLine.getLength()).trim()
							.startsWith(commentPrefix);

					if (line.getOffset() >= 1) {
						IRegion previousLine = doc.getLineInformationOfOffset(line.getOffset() - 1);
						commentAhead = doc.get(previousLine.getOffset(), previousLine.getLength()).trim()
								.startsWith(commentPrefix);
					}
				}
				// comment started on this line
				buf.append(" "); //$NON-NLS-1$
			}

			c.shiftsCaret = false;
			c.caretOffset = c.offset + buf.length();

			if (commentAtStart && !commentFollows && !commentAhead) {
				try {
					StringBuilder content = getDeclarationLines(doc, offset);

					boolean contentAlreadyThere = (firstLineContainsText && content != null
							&& content.toString().contains(commentContentBehindCursor.trim()));
					if (content == null || content.toString().trim().length() == 0 || contentAlreadyThere) {
						buf.setLength(0);
						buf.append(fgDefaultLineDelim);
						buf.append(indentationWithPrefix).append(' ');
						c.shiftsCaret = false;
						c.caretOffset = c.offset + buf.length();
					} else {
						if (!firstLineContainsText) {
							c.shiftsCaret = false;
							c.caretOffset = c.offset + 1;
							buf.setLength(0);
							buf.append(' ').append(indent(content, indentationWithPrefix + " ", //$NON-NLS-1$
									fgDefaultLineDelim).substring((indentationWithPrefix + " ").length())); //$NON-NLS-1$
						} else {
							buf.append(fgDefaultLineDelim);
							buf.append(indent(content, indentationWithPrefix + " ", fgDefaultLineDelim)); //$NON-NLS-1$
						}

						buf.setLength(buf.length() - fgDefaultLineDelim.length());
					}
				} catch (BadLocationException ble) {
					ble.printStackTrace();
				}
			}

			c.text = buf.toString();

		} catch (BadLocationException excp) {
		}
	}

	private StringBuilder getDeclarationLines(IDocument doc, int offset) throws BadLocationException {
		IASTNode dec = null;
		IASTTranslationUnit ast = getAST();

		if (ast != null) {
			dec = findNextDocumentNode(ast, offset);
			if (dec == null) {
				IASTNodeSelector ans = ast.getNodeSelector(ast.getFilePath());
				IASTNode node = ans.findEnclosingNode(offset, 0);
				if (node instanceof IASTDeclaration) {
					dec = node;
				}
			}
		}

		if (dec != null) {
			ITypedRegion partition = TextUtilities.getPartition(doc, ICPartitions.C_PARTITIONING /* this! */, offset,
					false);
			return customizeForDeclaration(doc, dec, partition, null);
		}
		return null;
	}

	private String getCommentPrefix(String indent) throws BadLocationException {
		if (indent.endsWith(SLASH_COMMENT)) {
			return SLASH_COMMENT;
		} else {
			return EXCL_COMMENT;
		}
	}

	/**
	 * Returns the range of the comment prefix on the given line in
	 * <code>document</code>. The prefix greedily matches the following regex
	 * pattern: <code>\s*\/\/[\/!]</code>, that is, any number of whitespace
	 * characters, followed by an comment ('///' or '//!').
	 *
	 * @param document the document to which <code>line</code> refers
	 * @param line the line from which to extract the prefix range
	 * @return an <code>IRegion</code> describing the range of the prefix on
	 *         the given line
	 * @throws BadLocationException if accessing the document fails
	 */
	protected static IRegion findPrefixRange(IDocument document, IRegion line) throws BadLocationException {
		int lineOffset = line.getOffset();
		int lineEnd = lineOffset + line.getLength();
		int indentEnd = findEndOfWhiteSpaceAt(document, lineOffset, lineEnd);
		if (indentEnd < lineEnd - 2 && document.getChar(indentEnd) == '/' && document.getChar(indentEnd + 1) == '/'
				&& (document.getChar(indentEnd + 2) == '/' || document.getChar(indentEnd + 2) == '!')) {
			indentEnd += 3;
		}
		return new Region(lineOffset, indentEnd - lineOffset);
	}

}
