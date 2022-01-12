/*******************************************************************************
 * Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin, Google
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;
import org.eclipse.cdt.internal.ui.text.CIndenter;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.LineRange;

/**
 * Utility that indents a number of lines in a document.
 */
public final class IndentUtil {

	private static final String SLASHES = "//"; //$NON-NLS-1$

	/**
	 * The result of an indentation operation. The result may be passed to
	 * subsequent calls to
	 * {@link IndentUtil#indentLines(IDocument, ILineRange, ICProject, IndentUtil.IndentResult) indentLines}
	 * to obtain consistent results with respect to the indentation of
	 * line-comments.
	 */
	public static final class IndentResult {
		private IndentResult(boolean[] commentLines) {
			commentLinesAtColumnZero = commentLines;
		}

		private boolean[] commentLinesAtColumnZero;
		private boolean hasChanged;
		private int leftmostLine = -1;

		/**
		 * Returns <code>true</code> if the indentation operation changed the
		 * document, <code>false</code> if not.
		 * @return <code>true</code> if the document was changed
		 */
		public boolean hasChanged() {
			return hasChanged;
		}
	}

	private IndentUtil() {
		// do not instantiate
	}

	/**
	 * Indents the line range specified by <code>lines</code> in
	 * <code>document</code>. The passed C project may be
	 * <code>null</code>, it is used solely to obtain formatter preferences.
	 *
	 * @param document the document to be changed
	 * @param lines the line range to be indented
	 * @param project the C project to get the formatter preferences from, or
	 *        <code>null</code> if global preferences should be used
	 * @param result the result from a previous call to <code>indentLines</code>,
	 *        in order to maintain comment line properties, or <code>null</code>.
	 *        Note that the passed result may be changed by the call.
	 * @return an indent result that may be queried for changes and can be
	 *         reused in subsequent indentation operations
	 * @throws BadLocationException if <code>lines</code> is not a valid line
	 *         range on <code>document</code>
	 */
	public static IndentResult indentLines(IDocument document, ILineRange lines, ICProject project, IndentResult result)
			throws BadLocationException {
		int numberOfLines = lines.getNumberOfLines();

		if (numberOfLines < 1)
			return new IndentResult(null);

		result = reuseOrCreateToken(result, numberOfLines);

		CHeuristicScanner scanner = new CHeuristicScanner(document);
		CIndenter indenter = new CIndenter(document, scanner, project);
		boolean changed = false;
		int tabSize = CodeFormatterUtil.getTabWidth(project);
		boolean indentInsideLineComments = indentInsideLineComments(project);
		for (int line = lines.getStartLine(), last = line + numberOfLines, i = 0; line < last; line++) {
			changed |= indentLine(document, line, indenter, scanner, result.commentLinesAtColumnZero, i++, tabSize,
					indentInsideLineComments);
		}
		result.hasChanged = changed;

		return result;
	}

	/**
	 * Inserts <code>indent</code> string at the beginning of each line in <code>lines</code>.
	 * @param document the document to be changed.
	 * @param lines the line range to be indented.
	 * @param indent the indent string to be inserted.
	 * @throws BadLocationException if <code>lines</code> is not a valid line
	 *         range on <code>document</code>
	 */
	public static void indentLines(IDocument document, LineRange lines, String indent) throws BadLocationException {
		int numberOfLines = lines.getNumberOfLines();
		for (int line = lines.getStartLine(), last = line + numberOfLines; line < last; line++) {
			int offset = document.getLineOffset(line);
			document.replace(offset, 0, indent);
		}
	}

	/**
	 * Returns <code>true</code> if line comments at column 0 should be indented inside, <code>false</code> otherwise.
	 *
	 * @param project  the project to get project specific options from
	 * @return <code>true</code> if line comments at column 0 should be indented inside, <code>false</code> otherwise.
	 */
	public static boolean indentInsideLineComments(ICProject project) {
		return DefaultCodeFormatterConstants.TRUE
				.equals(getCoreOption(project, DefaultCodeFormatterConstants.FORMATTER_INDENT_INSIDE_LINE_COMMENTS));
	}

	/**
	 * Returns the possibly <code>project</code>-specific core preference
	 * defined under <code>key</code>.
	 *
	 * @param project the project to get the preference from, or
	 *        <code>null</code> to get the global preference
	 * @param key the key of the preference
	 * @return the value of the preference
	 */
	private static String getCoreOption(ICProject project, String key) {
		if (project == null)
			return CCorePlugin.getOption(key);
		return project.getOption(key, true);
	}

	/**
	 * Shifts the line range specified by <code>lines</code> in
	 * <code>document</code>. The amount that the lines get shifted
	 * are determined by the first line in the range, all subsequent
	 * lines are adjusted accordingly. The passed C project may be
	 * <code>null</code>, it is used solely to obtain formatter
	 * preferences.
	 *
	 * @param document the document to be changed
	 * @param lines the line range to be shifted
	 * @param project the C project to get the formatter preferences
	 *        from, or <code>null</code> if global preferences should
	 *        be used
	 * @param result the result from a previous call to
	 *        <code>shiftLines</code>, in order to maintain comment
	 *        line properties, or <code>null</code>. Note that the
	 *        passed result may be changed by the call.
	 * @return an indent result that may be queried for changes and can
	 *         be reused in subsequent indentation operations
	 * @throws BadLocationException if <code>lines</code> is not a
	 *         valid line range on <code>document</code>
	 */
	public static IndentResult shiftLines(IDocument document, ILineRange lines, ICProject project, IndentResult result)
			throws BadLocationException {
		int numberOfLines = lines.getNumberOfLines();

		if (numberOfLines < 1)
			return new IndentResult(null);

		result = reuseOrCreateToken(result, numberOfLines);
		result.hasChanged = false;

		CHeuristicScanner scanner = new CHeuristicScanner(document);
		CIndenter indenter = new CIndenter(document, scanner, project);

		boolean indentInsideLineComments = indentInsideLineComments(project);
		String current = getCurrentIndent(document, lines.getStartLine(), indentInsideLineComments);
		StringBuilder correct = new StringBuilder(computeIndent(document, lines.getStartLine(), indenter, scanner));

		int tabSize = CodeFormatterUtil.getTabWidth(project);
		StringBuilder addition = new StringBuilder();
		int difference = subtractIndent(correct, current, addition, tabSize);

		if (difference == 0)
			return result;

		if (result.leftmostLine == -1)
			result.leftmostLine = getLeftMostLine(document, lines, tabSize, indentInsideLineComments);

		int maxReduction = computeVisualLength(
				getCurrentIndent(document, result.leftmostLine + lines.getStartLine(), indentInsideLineComments),
				tabSize);

		if (difference > 0) {
			for (int line = lines.getStartLine(), last = line + numberOfLines, i = 0; line < last; line++)
				addIndent(document, line, addition, result.commentLinesAtColumnZero, i++, indentInsideLineComments);
		} else {
			int reduction = Math.min(-difference, maxReduction);
			for (int line = lines.getStartLine(), last = line + numberOfLines, i = 0; line < last; line++)
				cutIndent(document, line, reduction, tabSize, result.commentLinesAtColumnZero, i++,
						indentInsideLineComments);
		}

		result.hasChanged = true;

		return result;

	}

	/**
	 * Indents line <code>line</code> in <code>document</code> with <code>indent</code>.
	 * Leaves leading comment signs alone.
	 *
	 * @param document the document
	 * @param line the line
	 * @param indent the indentation to insert
	 * @param commentlines
	 * @param relative
	 * @param indentInsideLineComments option whether to indent inside line comments starting at column 0
	 * @throws BadLocationException on concurrent document modification
	 */
	private static void addIndent(IDocument document, int line, CharSequence indent, boolean[] commentlines,
			int relative, boolean indentInsideLineComments) throws BadLocationException {
		IRegion region = document.getLineInformation(line);
		int insert = region.getOffset();
		int endOffset = region.getOffset() + region.getLength();

		if (indentInsideLineComments) {
			// go behind line comments
			if (!commentlines[relative]) {
				while (insert < endOffset - 2 && document.get(insert, 2).equals(SLASHES))
					insert += 2;
			}
		}

		// insert indent
		document.replace(insert, 0, indent.toString());
	}

	/**
	 * Cuts the visual equivalent of <code>toDelete</code> characters out of the
	 * indentation of line <code>line</code> in <code>document</code>.
	 *
	 * @param document
	 * @param line
	 * @param shiftWidth
	 * @param tabWidth
	 * @return number of characters deleted
	 * @throws BadLocationException
	 */
	public static int cutIndent(IDocument document, int line, int shiftWidth, int tabWidth)
			throws BadLocationException {
		return cutIndent(document, line, shiftWidth, tabWidth, new boolean[1], 0, false);
	}

	/**
	 * Cuts the visual equivalent of <code>toDelete</code> characters out of the
	 * indentation of line <code>line</code> in <code>document</code>. Leaves
	 * leading comment signs alone if desired.
	 *
	 * @param document the document
	 * @param line the line
	 * @param toDelete the number of space equivalents to delete.
	 * @param commentLines
	 * @param relative
	 * @param indentInsideLineComments option whether to indent inside line comments starting at column 0
	 * @return number of characters deleted
	 * @throws BadLocationException on concurrent document modification
	 */
	private static int cutIndent(IDocument document, int line, int toDelete, int tabSize, boolean[] commentLines,
			int relative, boolean indentInsideLineComments) throws BadLocationException {
		IRegion region = document.getLineInformation(line);
		int from = region.getOffset();
		int endOffset = region.getOffset() + region.getLength();

		if (indentInsideLineComments) {
			// go behind line comments
			while (from < endOffset - 2 && document.get(from, 2).equals(SLASHES))
				from += 2;
		}

		int to = from;
		while (toDelete > 0 && to < endOffset) {
			char ch = document.getChar(to);
			if (!Character.isWhitespace(ch))
				break;
			toDelete -= computeVisualLength(ch, tabSize);
			if (toDelete >= 0)
				to++;
			else
				break;
		}

		if (endOffset > to + 1 && document.get(to, 2).equals(SLASHES))
			commentLines[relative] = true;

		document.replace(from, to - from, ""); //$NON-NLS-1$
		return to - from;
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
	private static int subtractIndent(CharSequence correct, CharSequence current, StringBuilder difference,
			int tabSize) {
		int c1 = computeVisualLength(correct, tabSize);
		int c2 = computeVisualLength(current, tabSize);
		int diff = c1 - c2;
		if (diff <= 0)
			return diff;

		difference.setLength(0);
		int len = 0, i = 0;
		while (len < diff) {
			char c = correct.charAt(i++);
			difference.append(c);
			len += computeVisualLength(c, tabSize);
		}

		return diff;
	}

	private static int computeVisualLength(char ch, int tabSize) {
		if (ch == '\t')
			return tabSize;
		return 1;
	}

	/**
	 * Returns the visual length of a given <code>CharSequence</code> taking into
	 * account the visual tabulator length.
	 *
	 * @param seq the string to measure
	 * @return the visual length of <code>seq</code>
	 */
	public static int computeVisualLength(CharSequence seq, int tablen) {
		int size = 0;

		for (int i = 0; i < seq.length(); i++) {
			char ch = seq.charAt(i);
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
	 * Returns the indentation of the line <code>line</code> in <code>document</code>.
	 * The returned string may contain pairs of leading slashes that are considered
	 * part of the indentation.
	 *
	 * @param document the document
	 * @param line the line
	 * @param indentInsideLineComments  option whether to indent inside line comments starting at column 0
	 * @return the indentation of <code>line</code> in <code>document</code>
	 * @throws BadLocationException if the document is changed concurrently
	 */
	public static String getCurrentIndent(IDocument document, int line, boolean indentInsideLineComments)
			throws BadLocationException {
		IRegion region = document.getLineInformation(line);
		int from = region.getOffset();
		int endOffset = region.getOffset() + region.getLength();

		int to = from;
		if (indentInsideLineComments) {
			// go behind line comments
			while (to < endOffset - 2 && document.get(to, 2).equals(SLASHES))
				to += 2;
		}

		while (to < endOffset) {
			char ch = document.getChar(to);
			if (!Character.isWhitespace(ch))
				break;
			to++;
		}

		return document.get(from, to - from);
	}

	private static int getLeftMostLine(IDocument document, ILineRange lines, int tabSize,
			boolean indentInsideLineComments) throws BadLocationException {
		int numberOfLines = lines.getNumberOfLines();
		int first = lines.getStartLine();
		int minLine = -1;
		int minIndent = Integer.MAX_VALUE;
		for (int line = 0; line < numberOfLines; line++) {
			int length = computeVisualLength(getCurrentIndent(document, line + first, indentInsideLineComments),
					tabSize);
			if (length < minIndent && document.getLineLength(line + first) > 0) {
				minIndent = length;
				minLine = line;
			}
		}
		return minLine;
	}

	private static IndentResult reuseOrCreateToken(IndentResult token, int numberOfLines) {
		if (token == null)
			token = new IndentResult(new boolean[numberOfLines]);
		else if (token.commentLinesAtColumnZero == null)
			token.commentLinesAtColumnZero = new boolean[numberOfLines];
		else if (token.commentLinesAtColumnZero.length != numberOfLines) {
			boolean[] commentBooleans = new boolean[numberOfLines];
			System.arraycopy(token.commentLinesAtColumnZero, 0, commentBooleans, 0,
					Math.min(numberOfLines, token.commentLinesAtColumnZero.length));
			token.commentLinesAtColumnZero = commentBooleans;
		}
		return token;
	}

	/**
	 * Indents a single line using the heuristic scanner. Multiline comments are
	 * indented as specified by the <code>CCommentAutoIndentStrategy</code>.
	 *
	 * @param document the document
	 * @param line the line to be indented
	 * @param indenter the C indenter
	 * @param scanner the heuristic scanner
	 * @param commentLines the indent token comment booleans
	 * @param lineIndex the zero-based line index
	 * @param indentInsideLineComments option whether to indent inside line comments
	 *             starting at column 0
	 * @return <code>true</code> if the document was modified,
	 *         <code>false</code> if not
	 * @throws BadLocationException if the document got changed concurrently
	 */
	private static boolean indentLine(IDocument document, int line, CIndenter indenter, CHeuristicScanner scanner,
			boolean[] commentLines, int lineIndex, int tabSize, boolean indentInsideLineComments)
			throws BadLocationException {
		IRegion currentLine = document.getLineInformation(line);
		final int offset = currentLine.getOffset();
		int wsStart = offset; // where we start searching for non-WS; after the "//" in single line comments

		String indent = null;
		if (offset < document.getLength()) {
			ITypedRegion partition = TextUtilities.getPartition(document, ICPartitions.C_PARTITIONING, offset, true);
			ITypedRegion startingPartition = TextUtilities.getPartition(document, ICPartitions.C_PARTITIONING, offset,
					false);
			String type = partition.getType();
			if (type.equals(ICPartitions.C_MULTI_LINE_COMMENT) || type.equals(ICPartitions.C_MULTI_LINE_DOC_COMMENT)) {
				indent = computeCommentIndent(document, line, scanner, startingPartition);
			} else if (startingPartition.getType().equals(ICPartitions.C_PREPROCESSOR)) {
				indent = computePreprocessorIndent(document, line, startingPartition);
			} else if (!commentLines[lineIndex] && startingPartition.getOffset() == offset
					&& startingPartition.getType().equals(ICPartitions.C_SINGLE_LINE_COMMENT)) {
				return false;
			}
		}

		// standard C code indentation
		if (indent == null) {
			StringBuilder computed = indenter.computeIndentation(offset);
			if (computed != null)
				indent = computed.toString();
			else
				indent = ""; //$NON-NLS-1$
		}

		// change document:
		// get current white space
		int lineLength = currentLine.getLength();
		int end = scanner.findNonWhitespaceForwardInAnyPartition(wsStart, offset + lineLength);
		if (end == CHeuristicScanner.NOT_FOUND)
			end = offset + lineLength;
		int length = end - offset;
		String currentIndent = document.get(offset, length);

		// memorize the fact that a line is a single line comment (but not at column 0) and should be treated like code
		// as opposed to commented out code, which should keep its slashes at column 0
		// if 'indentInsideLineComments' is false, all comment lines are indented with the code
		if (length > 0 || !indentInsideLineComments) {
			ITypedRegion partition = TextUtilities.getPartition(document, ICPartitions.C_PARTITIONING, end, false);
			if (partition.getOffset() == end && ICPartitions.C_SINGLE_LINE_COMMENT.equals(partition.getType())) {
				commentLines[lineIndex] = true;
			}
		}

		// only change the document if it is a real change
		if (!indent.equals(currentIndent)) {
			document.replace(offset, length, indent);
			return true;
		}

		return false;
	}

	/**
	 * Computes and returns the indentation for a source line.
	 *
	 * @param document the document
	 * @param line the line in document
	 * @param indenter the C indenter
	 * @param scanner the scanner
	 * @return the indent, never <code>null</code>
	 * @throws BadLocationException
	 */
	public static String computeIndent(IDocument document, int line, CIndenter indenter, CHeuristicScanner scanner)
			throws BadLocationException {
		IRegion currentLine = document.getLineInformation(line);
		final int offset = currentLine.getOffset();

		String indent = null;
		if (offset < document.getLength()) {
			ITypedRegion partition = TextUtilities.getPartition(document, ICPartitions.C_PARTITIONING, offset, true);
			ITypedRegion startingPartition = TextUtilities.getPartition(document, ICPartitions.C_PARTITIONING, offset,
					false);
			String type = partition.getType();
			if (type.equals(ICPartitions.C_MULTI_LINE_COMMENT) || type.equals(ICPartitions.C_MULTI_LINE_DOC_COMMENT)) {
				indent = computeCommentIndent(document, line, scanner, startingPartition);
			} else if (startingPartition.getType().equals(ICPartitions.C_PREPROCESSOR)) {
				indent = computePreprocessorIndent(document, line, startingPartition);
			}
		}

		// standard C code indentation
		if (indent == null) {
			StringBuilder computed = indenter.computeIndentation(offset);
			if (computed != null)
				indent = computed.toString();
			else
				indent = ""; //$NON-NLS-1$
		}
		return indent;
	}

	/**
	 * Computes and returns the indentation for a block comment line.
	 *
	 * @param document the document
	 * @param line the line in document
	 * @param scanner the scanner
	 * @param partition the comment partition
	 * @return the indent, or <code>null</code> if not computable
	 * @throws BadLocationException
	 */
	public static String computeCommentIndent(IDocument document, int line, CHeuristicScanner scanner,
			ITypedRegion partition) throws BadLocationException {
		if (line == 0) // impossible - the first line is never inside a comment
			return null;

		// don't make any assumptions if the line does not start with \s*\* - it might be
		// commented out code, for which we don't want to change the indent
		final IRegion lineInfo = document.getLineInformation(line);
		final int lineStart = lineInfo.getOffset();
		final int lineLength = lineInfo.getLength();
		final int lineEnd = lineStart + lineLength;
		int nonWS = scanner.findNonWhitespaceForwardInAnyPartition(lineStart, lineEnd);
		if (nonWS == CHeuristicScanner.NOT_FOUND || document.getChar(nonWS) != '*') {
			if (nonWS == CHeuristicScanner.NOT_FOUND)
				return document.get(lineStart, lineLength);
			return document.get(lineStart, nonWS - lineStart);
		}

		// take the indent from the previous line and reuse
		IRegion previousLine = document.getLineInformation(line - 1);
		int previousLineStart = previousLine.getOffset();
		int previousLineLength = previousLine.getLength();
		int previousLineEnd = previousLineStart + previousLineLength;

		StringBuilder buf = new StringBuilder();
		int previousLineNonWS = scanner.findNonWhitespaceForwardInAnyPartition(previousLineStart, previousLineEnd);
		if (previousLineNonWS == CHeuristicScanner.NOT_FOUND || document.getChar(previousLineNonWS) != '*') {
			// align with the comment start if the previous line is not an asterix line
			previousLine = document.getLineInformationOfOffset(partition.getOffset());
			previousLineStart = previousLine.getOffset();
			previousLineLength = previousLine.getLength();
			previousLineEnd = previousLineStart + previousLineLength;
			previousLineNonWS = scanner.findNonWhitespaceForwardInAnyPartition(previousLineStart, previousLineEnd);
			if (previousLineNonWS == CHeuristicScanner.NOT_FOUND)
				previousLineNonWS = previousLineEnd;

			// add the initial space
			// TODO this may be controlled by a formatter preference in the future
			buf.append(' ');
		}

		String indentation = document.get(previousLineStart, previousLineNonWS - previousLineStart);
		buf.insert(0, indentation);
		return buf.toString();
	}

	/**
	 * Computes and returns the indentation for a preprocessor line.
	 *
	 * @param document the document
	 * @param line the line in document
	 * @param partition the comment partition
	 * @return the indent, or <code>null</code> if not computable
	 * @throws BadLocationException
	 */
	public static String computePreprocessorIndent(IDocument document, int line, ITypedRegion partition)
			throws BadLocationException {
		int ppFirstLine = document.getLineOfOffset(partition.getOffset());
		if (line == ppFirstLine) {
			return ""; //$NON-NLS-1$
		}
		CHeuristicScanner ppScanner = new CHeuristicScanner(document, ICPartitions.C_PARTITIONING, partition.getType());
		CIndenter ppIndenter = new CIndenter(document, ppScanner);
		if (line == ppFirstLine + 1) {
			return ppIndenter.createReusingIndent(new StringBuilder(), ppIndenter.getContinuationLineIndent(), 0)
					.toString();
		}
		StringBuilder computed = ppIndenter.computeIndentation(document.getLineOffset(line), false);
		if (computed != null) {
			return computed.toString();
		}
		// take the indent from the previous line and reuse
		IRegion previousLine = document.getLineInformation(line - 1);
		int previousLineStart = previousLine.getOffset();
		int previousLineLength = previousLine.getLength();
		int previousLineEnd = previousLineStart + previousLineLength;

		int previousLineNonWS = ppScanner.findNonWhitespaceForwardInAnyPartition(previousLineStart, previousLineEnd);
		String previousIndent = document.get(previousLineStart, previousLineNonWS - previousLineStart);
		computed = new StringBuilder(previousIndent);
		return computed.toString();
	}

	/**
	 * Extends the string with whitespace to match displayed width.
	 * @param prefix  add to this string
	 * @param displayedWidth  the desired display width
	 * @param tabWidth  the configured tab width
	 * @param useSpaces  whether to use spaces only
	 */
	public static String changePrefix(String prefix, int displayedWidth, int tabWidth, boolean useSpaces) {
		int column = computeVisualLength(prefix, tabWidth);
		if (column > displayedWidth) {
			return prefix;
		}
		final StringBuilder buffer = new StringBuilder(prefix);
		appendIndent(buffer, displayedWidth, tabWidth, useSpaces, column);
		return buffer.toString();
	}

	/**
	 * Appends whitespace to given buffer such that its visual length equals the given width.
	 * @param buffer  the buffer to add whitespace to
	 * @param width  the desired visual indent width
	 * @param tabWidth  the configured tab width
	 * @param useSpaces  whether tabs should be substituted by spaces
	 * @param startColumn  the column where to start measurement
	 * @return StringBuilder
	 */
	private static StringBuilder appendIndent(StringBuilder buffer, int width, int tabWidth, boolean useSpaces,
			int startColumn) {
		assert tabWidth > 0;
		int tabStop = startColumn - startColumn % tabWidth;
		int tabs = useSpaces ? 0 : (width - tabStop) / tabWidth;
		for (int i = 0; i < tabs; ++i) {
			buffer.append('\t');
			tabStop += tabWidth;
			startColumn = tabStop;
		}
		int spaces = width - startColumn;
		for (int i = 0; i < spaces; ++i) {
			buffer.append(' ');
		}
		return buffer;
	}

}
