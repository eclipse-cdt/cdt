/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Andrew Ferguson (Symbian)
 *     Andrew Gvozdev
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.DocumentRewriteSession;
import org.eclipse.jface.text.DocumentRewriteSessionType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditorExtension3;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;

import org.eclipse.cdt.internal.ui.editor.IndentUtil;

/**
 * Auto indent strategy sensitive to brackets.
 */
public class CAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {
	/** The line comment introducer. Value is "{@value}" */
	private static final String LINE_COMMENT= "//"; //$NON-NLS-1$
//	private static final GCCScannerExtensionConfiguration C_GNU_SCANNER_EXTENSION = new GCCScannerExtensionConfiguration();

//	private static class CompilationUnitInfo {
//		char[] buffer;
//		int delta;
//
//		CompilationUnitInfo(char[] buffer, int delta) {
//			this.buffer = buffer;
//			this.delta = delta;
//		}
//	}

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

	private String getIndentOfLine(IDocument d, int line) throws BadLocationException {
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

	private void smartIndentAfterClosingBracket(IDocument d, DocumentCommand c) {
		if (c.offset == -1 || d.getLength() == 0)
			return;

		try {
			int p = (c.offset == d.getLength() ? c.offset - 1 : c.offset);
			int line = d.getLineOfOffset(p);
			int start = d.getLineOffset(line);
			int whiteend = findEndOfWhiteSpace(d, start, c.offset);

			CHeuristicScanner scanner= new CHeuristicScanner(d);
			ITypedRegion partition= TextUtilities.getPartition(d, fPartitioning, p, false);
			if (ICPartitions.C_PREPROCESSOR.equals(partition.getType())) {
				scanner = new CHeuristicScanner(d, fPartitioning, ICPartitions.C_PREPROCESSOR);
			}
			CIndenter indenter = new CIndenter(d, scanner, fProject);

			// shift only when line does not contain any text up to the closing bracket
			if (whiteend == c.offset) {
				// evaluate the line with the opening bracket that matches out closing bracket
				int reference = indenter.findReferencePosition(c.offset, false, true, false, false, false);
				int indLine = d.getLineOfOffset(reference);
				if (indLine != -1 && indLine != line) {
					// take the indent of the found line
					StringBuilder replaceText = new StringBuilder(getIndentOfLine(d, indLine));
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
			CUIPlugin.log(e);
		}
	}

	private void smartIndentAfterOpeningBracket(IDocument d, DocumentCommand c) {
		if (c.offset < 1 || d.getLength() == 0)
			return;

		int p = (c.offset == d.getLength() ? c.offset - 1 : c.offset);

		try {
			CHeuristicScanner scanner= new CHeuristicScanner(d);
			ITypedRegion partition= TextUtilities.getPartition(d, fPartitioning, p, false);
			if (ICPartitions.C_PREPROCESSOR.equals(partition.getType())) {
				scanner = new CHeuristicScanner(d, fPartitioning, ICPartitions.C_PREPROCESSOR);
			}
			// current line
			int line = d.getLineOfOffset(c.offset);
			int lineOffset = d.getLineOffset(line);

			// make sure we don't have any leading comments etc.
			if (d.get(lineOffset, c.offset - lineOffset).trim().length() != 0)
				return;

			// Line of last C code
			int pos = scanner.findNonWhitespaceBackward(p, CHeuristicScanner.UNBOUND);
			if (pos == -1)
				return;
			int lastLine = d.getLineOfOffset(pos);

			// Only shift if the last C line is further up and is a braceless block candidate
			if (lastLine < line) {
				CIndenter indenter = new CIndenter(d, scanner, fProject);
				StringBuilder indent = indenter.computeIndentation(p, true);
				String toDelete = d.get(lineOffset, c.offset - lineOffset);
				if (indent != null && !indent.toString().equals(toDelete)) {
					c.text = indent.append(c.text).toString();
					c.length += c.offset - lineOffset;
					c.offset = lineOffset;
				}
			}
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
		}
	}

	private void smartIndentAfterNewLine(IDocument d, DocumentCommand c) {
		int docLength = d.getLength();
		if (c.offset == -1 || docLength == 0)
			return;

		int addIndent= 0;
		CHeuristicScanner scanner= new CHeuristicScanner(d);
		try {
			ITypedRegion partition= TextUtilities.getPartition(d, fPartitioning, c.offset, false);
			if (ICPartitions.C_PREPROCESSOR.equals(partition.getType()) && c.offset > 0 && d.getChar(c.offset-1) == '\\') {
				scanner = new CHeuristicScanner(d, fPartitioning, ICPartitions.C_PREPROCESSOR);
				addIndent= 1;
			}

			int line = d.getLineOfOffset(c.offset);
			IRegion reg = d.getLineInformation(line);
			int start = reg.getOffset();
			int lineEnd = start + reg.getLength();

			StringBuilder indent= null;
			CIndenter indenter= new CIndenter(d, scanner, fProject);
			if (getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_AUTO_INDENT)) {
				indent= indenter.computeIndentation(c.offset);
			} else {
				// reuse existing indent
				int wsEnd= findEndOfWhiteSpace(d, start, c.offset);
				if (wsEnd > start) {
					indent= new StringBuilder(d.get(start, wsEnd - start));
					addIndent= 0;
				}
			}
			if (indent == null) {
				indent= new StringBuilder();
			}
			if (addIndent > 0 && indent.length() == 0) {
				indent= indenter.createReusingIndent(indent, addIndent, 0);
			}

			StringBuilder buf = new StringBuilder(c.text + indent);
			int contentStart = findEndOfWhiteSpace(d, c.offset, lineEnd);
			c.length =  Math.max(contentStart - c.offset, 0);

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
				StringBuilder reference = null;
				int nonWS = findEndOfWhiteSpace(d, start, lineEnd);
				if (nonWS < c.offset && d.getChar(nonWS) == '{')
					reference = new StringBuilder(d.get(start, nonWS - start));
				else
					reference = indenter.getReferenceIndentation(c.offset);
				if (reference != null)
					buf.append(reference);
				buf.append('}');
				int bound= c.offset > 200 ? c.offset - 200 : CHeuristicScanner.UNBOUND;
				int bracePos = scanner.findOpeningPeer(c.offset - 1, bound, '{', '}');
				if (bracePos != CHeuristicScanner.NOT_FOUND) {
					if (scanner.looksLikeCompositeTypeDefinitionBackward(bracePos, bound) ||
							scanner.previousToken(bracePos - 1, bound) == Symbols.TokenEQUAL) {
						buf.append(';');
					}
				}
			}
			// insert extra line upon new line between two braces
			else if (c.offset > start && contentStart < lineEnd && d.getChar(contentStart) == '}') {
				int firstCharPos = scanner.findNonWhitespaceBackward(c.offset - 1, start);
				if (firstCharPos != CHeuristicScanner.NOT_FOUND && d.getChar(firstCharPos) == '{') {
					c.caretOffset = c.offset + buf.length();
					c.shiftsCaret = false;

					StringBuilder reference = null;
					int nonWS = findEndOfWhiteSpace(d, start, lineEnd);
					if (nonWS < c.offset && d.getChar(nonWS) == '{')
						reference = new StringBuilder(d.get(start, nonWS - start));
					else
						reference = indenter.getReferenceIndentation(c.offset);

					buf.append(TextUtilities.getDefaultLineDelimiter(d));

					if (reference != null)
						buf.append(reference);
				}
			}
			c.text = buf.toString();

		} catch (BadLocationException e) {
			CUIPlugin.log(e);
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
	 * @param scanner the C heuristic scanner set up on the document
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
		return getBlockBalance(document, offset, fPartitioning) <= 0;
		//TODO: Use smarter algorithm based on
//		CompilationUnitInfo info = getCompilationUnitForMethod(document, offset, fPartitioning);
//		if (info == null)
//			return false;
//
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

	/**
	 * Installs a C partitioner with <code>document</code>.
	 *
	 * @param document the document
	 */
	private static void installCPartitioner(Document document) {
		String[] types= new String[] {
		    ICPartitions.C_MULTI_LINE_COMMENT,
			ICPartitions.C_SINGLE_LINE_COMMENT,
			ICPartitions.C_STRING,
			ICPartitions.C_CHARACTER,
			ICPartitions.C_PREPROCESSOR,
			IDocument.DEFAULT_CONTENT_TYPE
		};
		FastPartitioner partitioner= new FastPartitioner(new FastCPartitionScanner(), types);
		partitioner.connect(document);
		document.setDocumentPartitioner(ICPartitions.C_PARTITIONING, partitioner);
	}

	/**
	 * Installs a C partitioner with <code>document</code>.
	 *
	 * @param document the document
	 */
	private static void removeCPartitioner(Document document) {
		document.setDocumentPartitioner(ICPartitions.C_PARTITIONING, null);
	}
	
	private void smartPaste(IDocument document, DocumentCommand command) {
		int newOffset= command.offset;
		int newLength= command.length;
		String newText= command.text;

		try {
			CHeuristicScanner scanner= new CHeuristicScanner(document);
			CIndenter indenter= new CIndenter(document, scanner, fProject);
			int offset= newOffset;

			// reference position to get the indent from
			int refOffset= indenter.findReferencePosition(offset);
			if (refOffset == CHeuristicScanner.NOT_FOUND)
				return;
			int peerOffset= getPeerPosition(document, command);
			peerOffset= indenter.findReferencePosition(peerOffset);
			if (peerOffset == CHeuristicScanner.NOT_FOUND)
				return;
			refOffset= Math.min(refOffset, peerOffset);

			// eat any WS before the insertion to the beginning of the line
			int firstLine= 1; // don't format the first line per default, as it has other content before it
			IRegion line= document.getLineInformationOfOffset(offset);
			String notSelected= document.get(line.getOffset(), offset - line.getOffset());
			if (notSelected.trim().length() == 0) {
				newLength += notSelected.length();
				newOffset= line.getOffset();
				firstLine= 0;
			}

			// Prefix: the part we need for formatting but won't paste.
			// Take up to 100 previous lines to preserve enough context.
			int firstPrefixLine= Math.max(document.getLineOfOffset(refOffset) - 100, 0);
			int prefixOffset= document.getLineInformation(firstPrefixLine).getOffset();
			String prefix= document.get(prefixOffset, newOffset - prefixOffset);

			// Handle the indentation computation inside a temporary document
			Document temp= new Document(prefix + newText);
			DocumentRewriteSession session= temp.startRewriteSession(DocumentRewriteSessionType.STRICTLY_SEQUENTIAL);
			scanner= new CHeuristicScanner(temp);
			indenter= new CIndenter(temp, scanner, fProject);
			installCPartitioner(temp);

			// Indent the first and second line
			// compute the relative indentation difference from the second line
			// (as the first might be partially selected) and use the value to
			// indent all other lines.
			boolean isIndentDetected= false;
			StringBuilder addition= new StringBuilder();
			int insertLength= 0;
			int first= document.computeNumberOfLines(prefix) + firstLine; // don't format first line
			int lines= temp.getNumberOfLines();
			boolean changed= false;
			boolean indentInsideLineComments= IndentUtil.indentInsideLineComments(fProject);

			for (int l= first; l < lines; l++) { // we don't change the number of lines while adding indents
				IRegion r= temp.getLineInformation(l);
				int lineOffset= r.getOffset();
				int lineLength= r.getLength();

				if (lineLength == 0) // don't modify empty lines
					continue;

				if (!isIndentDetected) {
					// indent the first pasted line
					String current= IndentUtil.getCurrentIndent(temp, l, indentInsideLineComments);
					StringBuilder correct= new StringBuilder(IndentUtil.computeIndent(temp, l, indenter, scanner));

					insertLength= subtractIndent(correct, current, addition);
					// workaround for bug 181139
					if (/*l != first && */temp.get(lineOffset, lineLength).trim().length() != 0) {
						isIndentDetected= true;
						if (insertLength == 0) {
							 // no adjustment needed, bail out
							if (firstLine == 0) {
								// but we still need to adjust the first line
								command.offset= newOffset;
								command.length= newLength;
								if (changed)
									break; // still need to get the leading indent of the first line
							}
							return;
						}
						removeCPartitioner(temp);
					} else {
						changed= insertLength != 0;
					}
				}

				// relatively indent all pasted lines
				if (insertLength > 0)
					addIndent(temp, l, addition, indentInsideLineComments);
				else if (insertLength < 0)
					cutIndent(temp, l, -insertLength, indentInsideLineComments);
			}

			temp.stopRewriteSession(session);
			newText= temp.get(prefix.length(), temp.getLength() - prefix.length());

			command.offset= newOffset;
			command.length= newLength;
			command.text= newText;
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
		}
	}

	/**
	 * Computes the difference of two indentations and returns the difference in
	 * length of current and correct. If the return value is positive, <code>addition</code>
	 * is initialized with a substring of that length of <code>correct</code>.
	 *
	 * @param correct the correct indentation
	 * @param current the current indentation (migth contain non-whitespace)
	 * @param difference a string buffer - if the return value is positive, it will be cleared and set to the substring of <code>current</code> of that length
	 * @return the difference in lenght of <code>correct</code> and <code>current</code>
	 */
	private int subtractIndent(CharSequence correct, CharSequence current, StringBuilder difference) {
		int c1= computeVisualLength(correct);
		int c2= computeVisualLength(current);
		int diff= c1 - c2;
		if (diff <= 0)
			return diff;

		difference.setLength(0);
		int len= 0, i= 0;
		while (len < diff) {
			char c= correct.charAt(i++);
			difference.append(c);
			len += computeVisualLength(c);
		}

		return diff;
	}

	/**
	 * Indents line <code>line</code> in <code>document</code> with <code>indent</code>.
	 * Leaves leading comment signs alone.
	 *
	 * @param document the document
	 * @param line the line
	 * @param indent the indentation to insert
	 * @param indentInsideLineComments option whether to indent inside line comments starting at column 0
	 * @throws BadLocationException on concurrent document modification
	 */
	private static void addIndent(Document document, int line, CharSequence indent, boolean indentInsideLineComments) throws BadLocationException {
		IRegion region= document.getLineInformation(line);
		int insert= region.getOffset();
		int endOffset= region.getOffset() + region.getLength();

		if (indentInsideLineComments) {
			// go behind line comments
			while (insert < endOffset - 2 && document.get(insert, 2).equals(LINE_COMMENT))
				insert += 2;
		}

		// insert indent
		document.replace(insert, 0, indent.toString());
	}

	/**
	 * Cuts the visual equivalent of <code>toDelete</code> characters out of the
	 * indentation of line <code>line</code> in <code>document</code>. Leaves
	 * leading comment signs alone.
	 *
	 * @param document the document
	 * @param line the line
	 * @param toDelete the number of space equivalents to delete.
	 * @param indentInsideLineComments option whether to indent inside line comments starting at column 0
	 * @throws BadLocationException on concurrent document modification
	 */
	private void cutIndent(Document document, int line, int toDelete, boolean indentInsideLineComments) throws BadLocationException {
		IRegion region= document.getLineInformation(line);
		int from= region.getOffset();
		int endOffset= region.getOffset() + region.getLength();

		if (indentInsideLineComments) {
			// go behind line comments
			while (from < endOffset - 2 && document.get(from, 2).equals(LINE_COMMENT))
				from += 2;
		}

		int to= from;
		while (toDelete > 0 && to < endOffset) {
			char ch= document.getChar(to);
			if (!Character.isWhitespace(ch))
				break;
			toDelete -= computeVisualLength(ch);
			if (toDelete >= 0)
				to++;
			else
				break;
		}

		document.replace(from, to - from, null);
	}

	/**
	 * Returns the visual length of a given <code>CharSequence</code> taking into
	 * account the visual tabulator length.
	 *
	 * @param seq the string to measure
	 * @return the visual length of <code>seq</code>
	 */
	private int computeVisualLength(CharSequence seq) {
		int size= 0;
		int tablen= getVisualTabLengthPreference();

		for (int i= 0; i < seq.length(); i++) {
			char ch= seq.charAt(i);
			if (ch == '\t') {
				if (tablen != 0)
					size += tablen - size % tablen;
				// else: size stays the same
			} else {
				size++;
			}
		}
		return size;
	}

	/**
	 * Returns the visual length of a given character taking into
	 * account the visual tabulator length.
	 *
	 * @param ch the character to measure
	 * @return the visual length of <code>ch</code>
	 */
	private int computeVisualLength(char ch) {
		if (ch == '\t')
			return getVisualTabLengthPreference();
		return 1;
	}

	/**
	 * The preference setting for the visual tabulator display.
	 *
	 * @return the number of spaces displayed for a tabulator in the editor
	 */
	private int getVisualTabLengthPreference() {
		return CodeFormatterUtil.getTabWidth(fProject);
	}

	private int getPeerPosition(IDocument document, DocumentCommand command) {
		if (document.getLength() == 0)
			return 0;
    	/*
    	 * Search for scope closers in the pasted text and find their opening peers
    	 * in the document.
    	 */
    	Document pasted= new Document(command.text);
    	installCPartitioner(pasted);
    	int firstPeer= command.offset;

    	CHeuristicScanner pScanner= new CHeuristicScanner(pasted);
    	CHeuristicScanner dScanner= new CHeuristicScanner(document);

    	// add scope relevant after context to peer search
    	int afterToken= dScanner.nextToken(command.offset + command.length, CHeuristicScanner.UNBOUND);
    	try {
			switch (afterToken) {
			case Symbols.TokenRBRACE:
				pasted.replace(pasted.getLength(), 0, "}"); //$NON-NLS-1$
				break;
			case Symbols.TokenRPAREN:
				pasted.replace(pasted.getLength(), 0, ")"); //$NON-NLS-1$
				break;
			case Symbols.TokenRBRACKET:
				pasted.replace(pasted.getLength(), 0, "]"); //$NON-NLS-1$
				break;
			}
		} catch (BadLocationException e) {
			// cannot happen
			Assert.isTrue(false);
		}

    	int pPos= 0; // paste text position (increasing from 0)
    	int dPos= Math.max(0, command.offset - 1); // document position (decreasing from paste offset)
    	while (true) {
    		int token= pScanner.nextToken(pPos, CHeuristicScanner.UNBOUND);
   			pPos= pScanner.getPosition();
    		switch (token) {
    			case Symbols.TokenLBRACE:
    			case Symbols.TokenLBRACKET:
    			case Symbols.TokenLPAREN:
    				pPos= skipScope(pScanner, pPos, token);
    				if (pPos == CHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				break; // closed scope -> keep searching
    			case Symbols.TokenRBRACE:
    				int peer= dScanner.findOpeningPeer(dPos, '{', '}');
    				dPos= peer - 1;
    				if (peer == CHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				firstPeer= peer;
    				break; // keep searching
    			case Symbols.TokenRBRACKET:
    				peer= dScanner.findOpeningPeer(dPos, '[', ']');
    				dPos= peer - 1;
    				if (peer == CHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				firstPeer= peer;
    				break; // keep searching
    			case Symbols.TokenRPAREN:
    				peer= dScanner.findOpeningPeer(dPos, '(', ')');
    				dPos= peer - 1;
    				if (peer == CHeuristicScanner.NOT_FOUND)
    					return firstPeer;
    				firstPeer= peer;
    				break; // keep searching
    				
    			case Symbols.TokenCASE:
    			case Symbols.TokenDEFAULT:
    			    {
    					CIndenter indenter= new CIndenter(document, dScanner, fProject);
    					peer= indenter.findReferencePosition(dPos, false, false, false, true, false);
    					if (peer == CHeuristicScanner.NOT_FOUND)
    						return firstPeer;
    					firstPeer= peer;
    				}
    				break; // keep searching

    			case Symbols.TokenPUBLIC:
    			case Symbols.TokenPROTECTED:
    			case Symbols.TokenPRIVATE:
				    {
						CIndenter indenter= new CIndenter(document, dScanner, fProject);
						peer= indenter.findReferencePosition(dPos, false, false, false, false, true);
						if (peer == CHeuristicScanner.NOT_FOUND)
							return firstPeer;
						firstPeer= peer;
					}
    				break; // keep searching
    				
    			case Symbols.TokenEOF:
    				return firstPeer;
    			default:
    				// keep searching
    		}
    	}
    }

    /**
     * Skips the scope opened by <code>token</code> in <code>document</code>,
     * returns either the position of the
     * @param pos
     * @param token
     * @return the position after the scope
     */
    private static int skipScope(CHeuristicScanner scanner, int pos, int token) {
    	int openToken= token;
    	int closeToken;
    	switch (token) {
    		case Symbols.TokenLPAREN:
    			closeToken= Symbols.TokenRPAREN;
    			break;
    		case Symbols.TokenLBRACKET:
    			closeToken= Symbols.TokenRBRACKET;
    			break;
    		case Symbols.TokenLBRACE:
    			closeToken= Symbols.TokenRBRACE;
    			break;
    		default:
    			Assert.isTrue(false);
    			return -1; // dummy
    	}

    	int depth= 1;
    	int p= pos;

    	while (true) {
    		int tok= scanner.nextToken(p, CHeuristicScanner.UNBOUND);
    		p= scanner.getPosition();

    		if (tok == openToken) {
    			depth++;
    		} else if (tok == closeToken) {
    			depth--;
    			if (depth == 0)
    				return p + 1;
    		} else if (tok == Symbols.TokenEOF) {
    			return CHeuristicScanner.NOT_FOUND;
    		}
    	}
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
			case ':':
				smartIndentAfterColumn(document, command);
				break;
			case '#':
				smartIndentAfterHash(document, command);
				break;
		}
	}

	private void smartIndentUponE(IDocument doc, DocumentCommand c) {
		if (c.offset < 4 || doc.getLength() == 0)
			return;

		try {
			String content = doc.get(c.offset - 3, 3);
			if (content.equals("els")) { //$NON-NLS-1$
				CHeuristicScanner scanner = new CHeuristicScanner(doc);
				int p = c.offset - 3;

				// current line
				int line = doc.getLineOfOffset(p);
				int lineOffset = doc.getLineOffset(line);

				// make sure we don't have any leading comments etc.
				if (doc.get(lineOffset, p - lineOffset).trim().length() != 0)
					return;

				// Line of last C code
				int pos = scanner.findNonWhitespaceBackward(p - 1, CHeuristicScanner.UNBOUND);
				if (pos == -1)
					return;
				int lastLine = doc.getLineOfOffset(pos);

				// Only shift if the last C line is further up and is a braceless block candidate
				if (lastLine < line) {
					CIndenter indenter = new CIndenter(doc, scanner, fProject);
					int ref = indenter.findReferencePosition(p, true, false, false, false, false);
					if (ref == CHeuristicScanner.NOT_FOUND)
						return;
					int refLine = doc.getLineOfOffset(ref);
					String indent = getIndentOfLine(doc, refLine);

					if (indent != null) {
						c.text = indent.toString() + "else"; //$NON-NLS-1$
						c.length += c.offset - lineOffset;
						c.offset = lineOffset;
					}
				}

				return;
			}

			if (content.equals("cas")) { //$NON-NLS-1$
				CHeuristicScanner scanner = new CHeuristicScanner(doc);
				int p = c.offset - 3;

				// current line
				int line = doc.getLineOfOffset(p);
				int lineOffset = doc.getLineOffset(line);

				// make sure we don't have any leading comments etc.
				if (doc.get(lineOffset, p - lineOffset).trim().length() != 0)
					return;

				// Line of last C code
				int pos = scanner.findNonWhitespaceBackward(p - 1, CHeuristicScanner.UNBOUND);
				if (pos == -1)
					return;
				int lastLine = doc.getLineOfOffset(pos);

				// Only shift if the last C line is further up and is a braceless block candidate
				if (lastLine < line) {
					CIndenter indenter = new CIndenter(doc, scanner, fProject);
					int ref = indenter.findReferencePosition(p, false, false, false, true, false);
					if (ref == CHeuristicScanner.NOT_FOUND)
						return;
					int refLine = doc.getLineOfOffset(ref);
					int nextToken = scanner.nextToken(ref, CHeuristicScanner.UNBOUND);
					String indent;
					if (nextToken == Symbols.TokenCASE || nextToken == Symbols.TokenDEFAULT)
						indent = getIndentOfLine(doc, refLine);
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
			CUIPlugin.log(e);
		}
	}

	private void smartIndentAfterColumn(IDocument doc, DocumentCommand c) {
		try {
			int offset = c.offset;
			// Current line
			int line = doc.getLineOfOffset(offset);
			IRegion startLine = doc.getLineInformationOfOffset(offset);
			int lineOffset = startLine.getOffset();

			CHeuristicScanner scanner = new CHeuristicScanner(doc);
			int prevToken = scanner.previousToken(offset - 1, lineOffset);
			switch (prevToken) {
				case Symbols.TokenDEFAULT:
				case Symbols.TokenPUBLIC:
				case Symbols.TokenPROTECTED:
				case Symbols.TokenPRIVATE:
					break;
					
				default:
					return;
			}
			
			int p = scanner.getPosition() + 1;

			// Make sure we don't have any leading comments etc.
			if (doc.get(lineOffset, p - lineOffset).trim().length() != 0)
				return;
			
			// Line of last C code
			int pos = scanner.findNonWhitespaceBackward(p - 1, CHeuristicScanner.UNBOUND);
			if (pos == -1)
				return;
			int lastLine = doc.getLineOfOffset(pos);

			// Only shift if the last C line is further up and is a braceless block candidate
			if (lastLine < line) {
				CIndenter indenter = new CIndenter(doc, scanner, fProject);
				int ref;
				if (prevToken == Symbols.TokenDEFAULT)
					ref = indenter.findReferencePosition(p, false, false, false, true, false);
				else
					ref = indenter.findReferencePosition(p, false, false, false, false, true);
				if (ref == CHeuristicScanner.NOT_FOUND)
					return;
				int refLine = doc.getLineOfOffset(ref);
				int nextToken = scanner.nextToken(ref, CHeuristicScanner.UNBOUND);
				String indent;
				if (nextToken == Symbols.TokenCASE || nextToken == Symbols.TokenDEFAULT ||
						nextToken == Symbols.TokenPUBLIC || nextToken == Symbols.TokenPROTECTED ||
						nextToken == Symbols.TokenPRIVATE) {
					indent = getIndentOfLine(doc, refLine);
				} else { // at the brace of the switch or the class
					indent = indenter.computeIndentation(p).toString();
				}

				if (indent != null) {
					c.text = indent.toString() + doc.get(p, offset - p) + c.text;
					c.length += c.offset - lineOffset;
					c.offset = lineOffset;
				}
			}

			return;
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
		}
	}

	private void smartIndentAfterHash(IDocument doc, DocumentCommand c) {
		try {
			ITypedRegion partition= TextUtilities.getPartition(doc, fPartitioning, c.offset, false);
			if (IDocument.DEFAULT_CONTENT_TYPE.equals(partition.getType())) {
				IRegion startLine= doc.getLineInformationOfOffset(c.offset);
				String indent= doc.get(startLine.getOffset(), c.offset - startLine.getOffset());
				if (indent.trim().length() == 0) {
					c.offset -= indent.length();
					c.length += indent.length();
				}
			}
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
		}
	}

	/*
	 * @see org.eclipse.jface.text.IAutoEditStrategy#customizeDocumentCommand(IDocument, DocumentCommand)
	 */
	@Override
	public void customizeDocumentCommand(IDocument d, DocumentCommand c) {
		if (!c.doit)
			return;

		clearCachedValues();
		if (!fIsSmartMode) {
			super.customizeDocumentCommand(d, c);
			return;
		}
		
		boolean isNewLine= c.length == 0 && c.text != null && isLineDelimiter(d, c.text);
		if (isNewLine) {
			smartIndentAfterNewLine(d, c);
		} else if (c.text.length() == 1 && getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_AUTO_INDENT)) {
			smartIndentOnKeypress(d, c);
		} else if (c.text.length() > 1
				&& getPreferenceStore().getBoolean(PreferenceConstants.EDITOR_SMART_PASTE)
				&& c.text.trim().length() != 0) {
			smartPaste(d, c); // no smart backspace for paste
		}
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
			if (part instanceof MultiPageEditorPart) {
				part= (IEditorPart)part.getAdapter(ITextEditorExtension3.class);
			}
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

//	private static CompilationUnitInfo getCompilationUnitForMethod(IDocument document, int offset, String partitioning) {
//		try {
//			CHeuristicScanner scanner = new CHeuristicScanner(document);
//
//			IRegion sourceRange = scanner.findSurroundingBlock(offset);
//			if (sourceRange == null)
//				return null;
//			String source = document.get(sourceRange.getOffset(), sourceRange.getLength());
//
//			StringBuilder contents = new StringBuilder();
//			contents.append("class ____C{void ____m()"); //$NON-NLS-1$
//			final int methodOffset = contents.length();
//			contents.append(source);
//			contents.append("};"); //$NON-NLS-1$
//
//			char[] buffer = contents.toString().toCharArray();
//			return new CompilationUnitInfo(buffer, sourceRange.getOffset() - methodOffset);
//		} catch (BadLocationException e) {
//			CUIPlugin.log(e);
//		}
//
//		return null;
//	}

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
