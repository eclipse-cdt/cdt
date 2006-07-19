/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems) - Fixed bug 48339
 *     Sergey Prigogin, Google
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.texteditor.ITextEditorExtension3;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICPartitions;

/**
 * Auto indent strategy sensitive to brackets.
 */
public class CAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {
	private static final String MULTILINE_COMMENT_CLOSE = "*/"; //$NON-NLS-1$
//	private static final GCCScannerExtensionConfiguration C_GNU_SCANNER_EXTENSION = new GCCScannerExtensionConfiguration();

	private static class CompilationUnitInfo {
		char[] buffer;
		int delta;

		CompilationUnitInfo(char[] buffer, int delta) {
			this.buffer = buffer;
			this.delta = delta;
		}
	}

	private boolean fCloseBrace;
	private boolean fIsSmartMode;

	private String fPartitioning;
	private final ICProject fProject;

	/**
	 * Creates a new C auto indent strategy for the given document partitioning.
	 *
	 * @param partitioning the document partitioning
	 * @param project the project to get formatting preferences from, or null to use default preferences
	 */
	public CAutoIndentStrategy(String partitioning, ICProject project) {
		fPartitioning = partitioning;
		fProject = project;
 	}

	// evaluate the line with the opening bracket that matches the closing bracket on the given line
	protected int findMatchingOpenBracket(IDocument d, int line, int end, int closingBracketIncrease) throws BadLocationException {


		int start = d.getLineOffset(line);
		int brackcount = getBracketCount(d, start, end, false) - closingBracketIncrease;


		// sum up the brackets counts of each line (closing brackets count negative, 
		// opening positive) until we find a line the brings the count to zero
		while (brackcount < 0) {
			line--;
			if (line < 0) {
				return -1;
			}
			start = d.getLineOffset(line);
			end = start + d.getLineLength(line) - 1;
			brackcount += getBracketCount(d, start, end, false);
		}
		return line;
	}


	private int getBracketCount(IDocument d, int start, int end, boolean ignoreCloseBrackets) throws BadLocationException {
		int bracketcount = 0;
		while (start < end) {
			char curr = d.getChar(start);
			start++;
			switch (curr) {
				case '/' :
					if (start < end) {
						char next = d.getChar(start);
						if (next == '*') {
							// a comment starts, advance to the comment end
							start = getCommentEnd(d, start + 1, end);
						} else if (next == '/') {
							// '//'-comment: nothing to do anymore on this line 
							start = end;
						}
					}
					break;
				case '*' :
					if (start < end) {
						char next = d.getChar(start);
						if (next == '/') {
							// we have been in a comment: forget what we read before
							bracketcount = 0;
							start++;
						}
					}
					break;
				case '{' :
					bracketcount++;
					ignoreCloseBrackets = false;
					break;
				case '}' :
					if (!ignoreCloseBrackets) {
						bracketcount--;
					}
					break;
				case '"' :
				case '\'' :
					start = getStringEnd(d, start, end, curr);
					break;
				default :
			}
		}
		return bracketcount;
	}

	// ----------- bracket counting ------------------------------------------------------


	private int getCommentEnd(IDocument d, int pos, int end) throws BadLocationException {
		while (pos < end) {
			char curr = d.getChar(pos);
			pos++;
			if (curr == '*') {
				if (pos < end && d.getChar(pos) == '/') {
					return pos + 1;
				}
			}
		}
		return end;
	}

	protected String getIndentOfLine(IDocument d, int line) throws BadLocationException {
		if (line > -1) {
			int start = d.getLineOffset(line);
			int end = start + d.getLineLength(line) - 1;
			int whiteend = findEndOfWhiteSpace(d, start, end);
			return d.get(start, whiteend - start);
		}
		return ""; //$NON-NLS-1$
	}

	private int getStringEnd(IDocument d, int pos, int end, char ch) throws BadLocationException {
		while (pos < end) {
			char curr = d.getChar(pos);
			pos++;
			if (curr == '\\') {
				// ignore escaped characters
				pos++;
			} else if (curr == ch) {
				return pos;
			}
		}
		return end;
	}

	protected void smartInsertAfterBracket(IDocument d, DocumentCommand c) {
		if (c.offset == -1 || d.getLength() == 0)
			return;

		try {
			int p = (c.offset == d.getLength() ? c.offset - 1 : c.offset);
			int line = d.getLineOfOffset(p);
			int start = d.getLineOffset(line);
			int whiteend = findEndOfWhiteSpace(d, start, c.offset);


			// shift only when line does not contain any text up to the closing bracket
			if (whiteend == c.offset) {
				// evaluate the line with the opening bracket that matches out closing bracket
				int indLine = findMatchingOpenBracket(d, line, c.offset, 1);
				if (indLine != -1 && indLine != line) {
					// take the indent of the found line
					StringBuffer replaceText = new StringBuffer(getIndentOfLine(d, indLine));
					// add the rest of the current line including the just added close bracket
					replaceText.append(d.get(whiteend, c.offset - whiteend));
					replaceText.append(c.text);
					// modify document command
					c.length = c.offset - start;
					c.offset = start;
					c.text = replaceText.toString();
				}
			}
		} catch (BadLocationException excp) {
			CUIPlugin.getDefault().log(excp);
		}
	}

	private void smartIndentAfterClosingBracket(IDocument d, DocumentCommand c) {
		if (c.offset == -1 || d.getLength() == 0)
			return;

		try {
			int p = (c.offset == d.getLength() ? c.offset - 1 : c.offset);
			int line = d.getLineOfOffset(p);
			int start = d.getLineOffset(line);
			int whiteend = findEndOfWhiteSpace(d, start, c.offset);

			CHeuristicScanner scanner = new CHeuristicScanner(d);
			CIndenter indenter = new CIndenter(d, scanner, fProject);

			// shift only when line does not contain any text up to the closing bracket
			if (whiteend == c.offset) {
				// evaluate the line with the opening bracket that matches out closing bracket
				int reference = indenter.findReferencePosition(c.offset, false, true, false, false);
				int indLine = d.getLineOfOffset(reference);
				if (indLine != -1 && indLine != line) {
					// take the indent of the found line
					StringBuffer replaceText = new StringBuffer(getIndentOfLine(d, indLine));
					// add the rest of the current line including the just added close bracket
					replaceText.append(d.get(whiteend, c.offset - whiteend));
					replaceText.append(c.text);
					// modify document command
					c.length += c.offset - start;
					c.offset = start;
					c.text = replaceText.toString();
				}
			}
		} catch (BadLocationException e) {
			CUIPlugin.getDefault().log(e);
		}
	}

	private void smartIndentAfterOpeningBracket(IDocument d, DocumentCommand c) {
		if (c.offset < 1 || d.getLength() == 0)
			return;

		CHeuristicScanner scanner = new CHeuristicScanner(d);

		int p = (c.offset == d.getLength() ? c.offset - 1 : c.offset);

		try {
			// current line
			int line = d.getLineOfOffset(p);
			int lineOffset = d.getLineOffset(line);

			// make sure we don't have any leading comments etc.
			if (d.get(lineOffset, p - lineOffset).trim().length() != 0)
				return;

			// line of last javacode
			int pos = scanner.findNonWhitespaceBackward(p, CHeuristicScanner.UNBOUND);
			if (pos == -1)
				return;
			int lastLine = d.getLineOfOffset(pos);

			// only shift if the last java line is further up and is a braceless block candidate
			if (lastLine < line) {
				CIndenter indenter = new CIndenter(d, scanner, fProject);
				StringBuffer indent = indenter.computeIndentation(p, true);
				String toDelete = d.get(lineOffset, c.offset - lineOffset);
				if (indent != null && !indent.toString().equals(toDelete)) {
					c.text = indent.append(c.text).toString();
					c.length += c.offset - lineOffset;
					c.offset = lineOffset;
				}
			}

		} catch (BadLocationException e) {
			CUIPlugin.getDefault().log(e);
		}
	}

	private void smartIndentAfterNewLine(IDocument d, DocumentCommand c) {
		CHeuristicScanner scanner = new CHeuristicScanner(d);
		CIndenter indenter = new CIndenter(d, scanner, fProject);
		StringBuffer indent = indenter.computeIndentation(c.offset);
		if (indent == null)
			indent = new StringBuffer(); 

		int docLength = d.getLength();
		if (c.offset == -1 || docLength == 0)
			return;

		try {
			int p = (c.offset == docLength ? c.offset - 1 : c.offset);
			int line = d.getLineOfOffset(p);

			StringBuffer buf = new StringBuffer(c.text + indent);

			IRegion reg = d.getLineInformation(line);
			int lineEnd = reg.getOffset() + reg.getLength();

			int contentStart = findEndOfWhiteSpace(d, c.offset, lineEnd);
			c.length =  Math.max(contentStart - c.offset, 0);

			int start = reg.getOffset();

			// insert closing brace on new line after an unclosed opening brace
			if (getBracketCount(d, start, c.offset, true) > 0 && fCloseBrace && !isClosedBrace(d, c.offset, c.length)) {
				c.caretOffset = c.offset + buf.length();
				c.shiftsCaret = false;

				// copy old content of line behind insertion point to new line
				// unless we think we are inserting an anonymous type definition
				if (c.offset == 0 || !(computeAnonymousPosition(d, c.offset - 1, fPartitioning, lineEnd) != -1)) {
					if (lineEnd - contentStart > 0) {
						c.length =  lineEnd - c.offset;
						buf.append(d.get(contentStart, lineEnd - contentStart).toCharArray());
					}
				}

				buf.append(TextUtilities.getDefaultLineDelimiter(d));
				StringBuffer reference = null;
				int nonWS = findEndOfWhiteSpace(d, start, lineEnd);
				if (nonWS < c.offset && d.getChar(nonWS) == '{')
					reference = new StringBuffer(d.get(start, nonWS - start));
				else
					reference = indenter.getReferenceIndentation(c.offset);
				if (reference != null)
					buf.append(reference);
				buf.append('}');
			}
			// insert extra line upon new line between two braces
			else if (c.offset > start && contentStart < lineEnd && d.getChar(contentStart) == '}') {
				int firstCharPos = scanner.findNonWhitespaceBackward(c.offset - 1, start);
				if (firstCharPos != CHeuristicScanner.NOT_FOUND && d.getChar(firstCharPos) == '{') {
					c.caretOffset = c.offset + buf.length();
					c.shiftsCaret = false;

					StringBuffer reference = null;
					int nonWS = findEndOfWhiteSpace(d, start, lineEnd);
					if (nonWS < c.offset && d.getChar(nonWS) == '{')
						reference = new StringBuffer(d.get(start, nonWS - start));
					else
						reference = indenter.getReferenceIndentation(c.offset);

					buf.append(TextUtilities.getDefaultLineDelimiter(d));

					if (reference != null)
						buf.append(reference);
				}
			}
			c.text = buf.toString();

		} catch (BadLocationException e) {
			CUIPlugin.getDefault().log(e);
		}
	}
	
	/**
	 * Computes an insert position for an opening brace if <code>offset</code> maps to a position in
	 * <code>document</code> with a expression in parenthesis that will take a block after the closing parenthesis.
	 *
	 * @param document the document being modified
	 * @param offset the offset of the caret position, relative to the line start.
	 * @param partitioning the document partitioning
	 * @param max the max position
	 * @return an insert position relative to the line start if <code>line</code> contains a parenthesized expression that can be followed by a block, -1 otherwise
	 */
	private static int computeAnonymousPosition(IDocument document, int offset, String partitioning,  int max) {
		// find the opening parenthesis for every closing parenthesis on the current line after offset
		// return the position behind the closing parenthesis if it looks like a method declaration
		// or an expression for an if, while, for, catch statement

		CHeuristicScanner scanner = new CHeuristicScanner(document);
		int pos = offset;
		int length = max;
		int scanTo = scanner.scanForward(pos, length, '}');
		if (scanTo == -1)
			scanTo = length;

		int closingParen = findClosingParenToLeft(scanner, pos) - 1;

		while (true) {
			int startScan = closingParen + 1;
			closingParen = scanner.scanForward(startScan, scanTo, ')');
			if (closingParen == -1)
				break;

			int openingParen = scanner.findOpeningPeer(closingParen - 1, '(', ')');

			// no way an expression at the beginning of the document can mean anything
			if (openingParen < 1)
				break;

			// only select insert positions for parenthesis currently embracing the caret
			if (openingParen > pos)
				continue;
		}

		return -1;
	}

	/**
	 * Finds a closing parenthesis to the left of <code>position</code> in document, where that parenthesis is only
	 * separated by whitespace from <code>position</code>. If no such parenthesis can be found, <code>position</code> is returned.
	 *
	 * @param scanner the java heuristic scanner set up on the document
	 * @param position the first character position in <code>document</code> to be considered
	 * @return the position of a closing parenthesis left to <code>position</code> separated only by whitespace, or <code>position</code> if no parenthesis can be found
	 */
	private static int findClosingParenToLeft(CHeuristicScanner scanner, int position) {
		if (position < 1)
			return position;

		if (scanner.previousToken(position - 1, CHeuristicScanner.UNBOUND) == Symbols.TokenRPAREN)
			return scanner.getPosition() + 1;
		return position;
	}

	private boolean isClosedBrace(IDocument document, int offset, int length) {
		CompilationUnitInfo info = getCompilationUnitForMethod(document, offset, fPartitioning);
		if (info == null)
			return false;

		return getBlockBalance(document, offset, fPartitioning) <= 0;
		//TODO: Use smarter algorithm based on 
//		CodeReader reader = new CodeReader(info.buffer);
//		ICodeReaderFactory fileCreator = CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_WORKING_COPY_WHENEVER_POSSIBLE);
//		
//		IScanner domScanner = new DOMScanner(reader, new ScannerInfo(), ParserMode.COMPLETE_PARSE,
//				ParserLanguage.C, ParserFactory.createDefaultLogService(),
//				C_GNU_SCANNER_EXTENSION, fileCreator);
//		
//		ISourceCodeParser parser = new GNUCPPSourceParser(
//				domScanner,
//				ParserMode.COMPLETE_PARSE,
//				ParserUtil.getParserLogService(),
//				new GPPParserExtensionConfiguration());
//	
//		IASTTranslationUnit translationUnit = parser.parse();
//		final int relativeOffset = offset - info.delta;
//	    IASTNode node = translationUnit.selectNodeForLocation(reader.getPath(), relativeOffset, length);
//		
//		if (node == null)
//			return false;
//
//		if (node instanceof IASTCompoundStatement) {
//			return getBlockBalance(document, offset, fPartitioning) <= 0;
//		} else if (node instanceof IASTIfStatement) { 
//			IASTIfStatement ifStatement = (IASTIfStatement) node;
//			IASTExpression expression = ifStatement.getConditionExpression();
//			IRegion expressionRegion = createRegion(expression, info.delta);
//			IASTStatement thenStatement = ifStatement.getThenClause();
//			IRegion thenRegion = createRegion(thenStatement, info.delta);
//
//			// Between expression and then statement
//			if (expressionRegion.getOffset() + expressionRegion.getLength() <= offset && offset + length <= thenRegion.getOffset())
//				return thenStatement != null;
//
//			IASTStatement elseStatement = ifStatement.getElseClause();
//			IRegion elseRegion = createRegion(elseStatement, info.delta);
//
//			if (elseStatement != null) {
//				int sourceOffset = thenRegion.getOffset() + thenRegion.getLength();
//				int sourceLength = elseRegion.getOffset() - sourceOffset;
//				CHeuristicScanner scanner = new CHeuristicScanner(new SimpleDocument(info.buffer));
//				int pos = sourceOffset;
//				int id;
//				while ((id = scanner.nextToken(pos, sourceOffset + sourceLength - pos)) != CHeuristicScanner.TokenEOF) {
//					if (id == CHeuristicScanner.TokenELSE) {
//						pos = scanner.getPosition();
//						// Between 'else' token and else statement.
//						return pos <= offset && offset + length < elseRegion.getOffset();
//					}
//				}
//				
//				return true;
//			}
//		} else if (node instanceof IASTForStatement) {
//			IASTExpression expression = ((IASTForStatement) node).getConditionExpression();
//			IRegion expressionRegion = createRegion(expression, info.delta);
//			IASTStatement body = ((IASTForStatement) node).getBody();
//			IRegion bodyRegion = createRegion(body, info.delta);
//
//			// Between expression and body statement
//			if (expressionRegion.getOffset() + expressionRegion.getLength() <= offset && offset + length <= bodyRegion.getOffset()) {
//				return body != null;
//			}
//		} else if (node instanceof IASTWhileStatement) {
//			IASTExpression expression = ((IASTWhileStatement) node).getCondition();
//			IRegion expressionRegion = createRegion(expression, info.delta);
//			IASTStatement body = ((IASTWhileStatement) node).getBody();
//			IRegion bodyRegion = createRegion(body, info.delta);
//
//			// Between expression and body statement
//			if (expressionRegion.getOffset() + expressionRegion.getLength() <= offset && offset + length <= bodyRegion.getOffset()) {
//				return body != null;
//			}
//		} else if (node instanceof IASTDoStatement) {
//			IASTDoStatement doStatement = (IASTDoStatement) node;
//			IRegion doRegion = createRegion(doStatement, info.delta);
//			IASTStatement body = doStatement.getBody();
//			IRegion bodyRegion = createRegion(body, info.delta);
//
//			// Between 'do' and body statement.
//			if (doRegion.getOffset() + doRegion.getLength() <= offset && offset + length <= bodyRegion.getOffset()) {
//				return body != null;
//			}
//		}
//
//		return true;
	}

    private boolean isLineDelimiter(IDocument document, String text) {
		String[] delimiters = document.getLegalLineDelimiters();
		if (delimiters != null)
			return TextUtilities.equals(delimiters, text) > -1;
		return false;
	}

	private void smartIndentOnKeypress(IDocument document, DocumentCommand command) {
		switch (command.text.charAt(0)) {
			case '}':
				smartIndentAfterClosingBracket(document, command);
				break;
			case '{':
				smartIndentAfterOpeningBracket(document, command);
				break;
			case 'e':
				smartIndentUponE(document, command);
				break;
		}
	}

	private void smartIndentUponE(IDocument d, DocumentCommand c) {
		if (c.offset < 4 || d.getLength() == 0)
			return;

		try {
			String content = d.get(c.offset - 3, 3);
			if (content.equals("els")) { //$NON-NLS-1$
				CHeuristicScanner scanner = new CHeuristicScanner(d);
				int p = c.offset - 3;

				// current line
				int line = d.getLineOfOffset(p);
				int lineOffset = d.getLineOffset(line);

				// make sure we don't have any leading comments etc.
				if (d.get(lineOffset, p - lineOffset).trim().length() != 0)
					return;

				// line of last javacode
				int pos = scanner.findNonWhitespaceBackward(p - 1, CHeuristicScanner.UNBOUND);
				if (pos == -1)
					return;
				int lastLine = d.getLineOfOffset(pos);

				// only shift if the last java line is further up and is a braceless block candidate
				if (lastLine < line) {

					CIndenter indenter = new CIndenter(d, scanner, fProject);
					int ref = indenter.findReferencePosition(p, true, false, false, false);
					if (ref == CHeuristicScanner.NOT_FOUND)
						return;
					int refLine = d.getLineOfOffset(ref);
					String indent = getIndentOfLine(d, refLine);

					if (indent != null) {
						c.text = indent.toString() + "else"; //$NON-NLS-1$
						c.length += c.offset - lineOffset;
						c.offset = lineOffset;
					}
				}

				return;
			}

			if (content.equals("cas")) { //$NON-NLS-1$
				CHeuristicScanner scanner = new CHeuristicScanner(d);
				int p = c.offset - 3;

				// current line
				int line = d.getLineOfOffset(p);
				int lineOffset = d.getLineOffset(line);

				// make sure we don't have any leading comments etc.
				if (d.get(lineOffset, p - lineOffset).trim().length() != 0)
					return;

				// line of last javacode
				int pos = scanner.findNonWhitespaceBackward(p - 1, CHeuristicScanner.UNBOUND);
				if (pos == -1)
					return;
				int lastLine = d.getLineOfOffset(pos);

				// only shift if the last java line is further up and is a braceless block candidate
				if (lastLine < line) {

					CIndenter indenter = new CIndenter(d, scanner, fProject);
					int ref = indenter.findReferencePosition(p, false, false, false, true);
					if (ref == CHeuristicScanner.NOT_FOUND)
						return;
					int refLine = d.getLineOfOffset(ref);
					int nextToken = scanner.nextToken(ref, CHeuristicScanner.UNBOUND);
					String indent;
					if (nextToken == Symbols.TokenCASE || nextToken == Symbols.TokenDEFAULT)
						indent = getIndentOfLine(d, refLine);
					else // at the brace of the switch
						indent = indenter.computeIndentation(p).toString();

					if (indent != null) {
						c.text = indent.toString() + "case"; //$NON-NLS-1$
						c.length += c.offset - lineOffset;
						c.offset = lineOffset;
					}
				}

				return;
			}
		} catch (BadLocationException e) {
			CUIPlugin.getDefault().log(e);
		}
	}

	/*
	 * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(IDocument, DocumentCommand)
	 */
	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		if (!c.doit)
			return;

		clearCachedValues();
		if (!fIsSmartMode) {
			super.customizeDocumentCommand(d, c);
			return;
		}

		if (c.length == 0 && c.text != null && isLineDelimiter(d, c.text)) {
			if (isAppendToOpenMultilineComment(d, c)) {
				// special case: multi-line comment at end of document (bug 48339)
				CCommentAutoIndentStrategy.commentIndentAfterNewLine(d, c);
			} else {
				smartIndentAfterNewLine(d, c);
			}
		} else if ("/".equals(c.text) && isAppendToOpenMultilineComment(d, c)) { //$NON-NLS-1$
			// special case: multi-line comment at end of document (bug 48339)
			CCommentAutoIndentStrategy.commentIndentForCommentEnd(d, c);
		} else if (c.text.length() == 1) {
			smartIndentOnKeypress(d, c);
// TODO Support smart paste.
//		} else if (c.text.length() > 1 && getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SMART_PASTE)) {
//			smartPaste(d, c); // no smart backspace for paste
		}
	}
	
	/**
	 * Check, if the command appends to an open multi-line comment.
	 * @param d  the document
	 * @param c  the document command
	 * @return true, if the command appends to an open multi-line comment.
	 */
	private boolean isAppendToOpenMultilineComment(IDocument d, DocumentCommand c) {
		if (d.getLength() >= 2 && c.offset == d.getLength()) {
			try {
				String contentType = org.eclipse.jface.text.TextUtilities.getContentType(d, fPartitioning, c.offset - 1, false);
				if (ICPartitions.C_MULTI_LINE_COMMENT.equals(contentType)) {
					return !d.get(c.offset - 2, 2).equals(MULTILINE_COMMENT_CLOSE);
				}
			} catch (BadLocationException exc) {
				// see below
			}
		}
		return false;
	}

	private static IPreferenceStore getPreferenceStore() {
		return CUIPlugin.getDefault().getCombinedPreferenceStore();
	}

	private void clearCachedValues() {
        IPreferenceStore preferenceStore = getPreferenceStore();
		fCloseBrace = preferenceStore.getBoolean(PreferenceConstants.EDITOR_CLOSE_BRACES);
		fIsSmartMode = computeSmartMode();
	}

	private boolean computeSmartMode() {
		IWorkbenchPage page = CUIPlugin.getActivePage();
		if (page != null)  {
			IEditorPart part = page.getActiveEditor();
			if (part instanceof ITextEditorExtension3) {
				ITextEditorExtension3 extension = (ITextEditorExtension3) part;
				return extension.getInsertMode() == ITextEditorExtension3.SMART_INSERT;
			}
			if (part == null) {
				// TODO: Remove this if statement once CAutoIndentTest is fixed so that getActiveEditor does not return null.
				return true;
			}
		}
		return false;
	}

	private static CompilationUnitInfo getCompilationUnitForMethod(IDocument document, int offset, String partitioning) {
		try {
			CHeuristicScanner scanner = new CHeuristicScanner(document);

			IRegion sourceRange = scanner.findSurroundingBlock(offset);
			if (sourceRange == null)
				return null;
			String source = document.get(sourceRange.getOffset(), sourceRange.getLength());

			StringBuffer contents = new StringBuffer();
			contents.append("class ____C{void ____m()"); //$NON-NLS-1$
			final int methodOffset = contents.length();
			contents.append(source);
			contents.append("};"); //$NON-NLS-1$

			char[] buffer = contents.toString().toCharArray();
			return new CompilationUnitInfo(buffer, sourceRange.getOffset() - methodOffset);
		} catch (BadLocationException e) {
			CUIPlugin.getDefault().log(e);
		}

		return null;
	}

	/**
	 * Returns the block balance, i.e. zero if the blocks are balanced at
	 * <code>offset</code>, a negative number if there are more closing than opening
	 * braces, and a positive number if there are more opening than closing braces.
	 *
	 * @param document
	 * @param offset
	 * @param partitioning
	 * @return the block balance
	 */
	private static int getBlockBalance(IDocument document, int offset, String partitioning) {
		if (offset < 1)
			return -1;
		if (offset >= document.getLength())
			return 1;

		int begin = offset;
		int end = offset - 1;

		CHeuristicScanner scanner = new CHeuristicScanner(document);

		while (true) {
			begin = scanner.findOpeningPeer(begin - 1, '{', '}');
			end = scanner.findClosingPeer(end + 1, '{', '}');
			if (begin == -1 && end == -1)
				return 0;
			if (begin == -1)
				return -1;
			if (end == -1)
				return 1;
		}
	}

//	private static IRegion createRegion(IASTNode node, int delta) {
//		IASTNodeLocation nodeLocation = node.getNodeLocations()[0];
//		return node == null ? null : new Region(nodeLocation.getNodeOffset() + delta, nodeLocation.getNodeLength());
//	}
}
