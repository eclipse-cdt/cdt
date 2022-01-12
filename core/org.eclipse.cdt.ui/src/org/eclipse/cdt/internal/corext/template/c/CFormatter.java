/*******************************************************************************
 * Copyright (c) 2005, 2008 IBM Corporation and others.
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
 *     Qnx Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin, Google
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.template.c;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.CodeFormatter;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.cdt.internal.ui.editor.IndentUtil;
import org.eclipse.cdt.internal.ui.text.FastCPartitionScanner;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * A template editor using the C/C++ formatter to format a template buffer.
 */
public class CFormatter {

	private static final String COMMENT_START = "/*-"; //$NON-NLS-1$
	private static final String COMMENT_END = "*/"; //$NON-NLS-1$

	/** The line delimiter to use if code formatter is not used. */
	private final String fLineDelimiter;
	/** The initial indent level */
	private final int fInitialIndentLevel;

	/** Flag indicating whether to use the code formatter or not. */
	private boolean fUseCodeFormatter;
	private final ICProject fProject;

	/**
	 * Creates a CFormatter with the target line delimiter.
	 *
	 * @param lineDelimiter the line delimiter to use
	 * @param initialIndentLevel the initial indentation level
	 * @param useCodeFormatter <code>true</code> if the core code formatter should be used
	 * @param project the C/C++ project from which to get the preferences, or <code>null</code> for workbench settings
	 */
	public CFormatter(String lineDelimiter, int initialIndentLevel, boolean useCodeFormatter, ICProject project) {
		fLineDelimiter = lineDelimiter;
		fUseCodeFormatter = useCodeFormatter;
		fInitialIndentLevel = initialIndentLevel;
		fProject = project;
	}

	/**
	 * Formats the template buffer.
	 * @param buffer
	 * @param context
	 * @throws BadLocationException
	 */
	public void format(TemplateBuffer buffer, TemplateContext context) throws BadLocationException {
		try {
			VariableTracker tracker = new VariableTracker(buffer);
			IDocument document = tracker.getDocument();

			internalFormat(document, context, buffer);
			convertLineDelimiters(document);
			if (isReplacedAreaEmpty(context))
				trimStart(document);

			tracker.updateBuffer();
		} catch (MalformedTreeException e) {
			throw new BadLocationException();
		}
	}

	/**
	 * @param document
	 * @param context
	 * @param buffer
	 * @throws BadLocationException
	 */
	private void internalFormat(IDocument document, TemplateContext context, TemplateBuffer buffer)
			throws BadLocationException {
		if (fUseCodeFormatter) {
			// try to format and fall back to indenting
			try {
				format(document);
				return;
			} catch (BadLocationException e) {
			} catch (MalformedTreeException e) {
			}
		}
		// fallback: indent
		indent(document, buffer);
	}

	private void convertLineDelimiters(IDocument document) throws BadLocationException {
		int lines = document.getNumberOfLines();
		for (int line = 0; line < lines; line++) {
			IRegion region = document.getLineInformation(line);
			String lineDelimiter = document.getLineDelimiter(line);
			if (lineDelimiter != null)
				document.replace(region.getOffset() + region.getLength(), lineDelimiter.length(), fLineDelimiter);
		}
	}

	private void trimStart(IDocument document) throws BadLocationException {
		trimAtOffset(0, document);
	}

	private String trimAtOffset(int offset, IDocument document) throws BadLocationException {
		int i = offset;
		while ((i < document.getLength()) && Character.isWhitespace(document.getChar(i)))
			i++;

		String trim = document.get(offset, i - offset);
		document.replace(offset, i - offset, ""); //$NON-NLS-1$
		return trim;
	}

	private boolean isReplacedAreaEmpty(TemplateContext context) {
		if (context instanceof DocumentTemplateContext) {
			DocumentTemplateContext dtc = (DocumentTemplateContext) context;
			if (dtc.getCompletionLength() == 0) {
				return true;
			}
			try {
				if (dtc.getDocument().get(dtc.getStart(), dtc.getEnd() - dtc.getStart()).trim().length() == 0)
					return true;
			} catch (BadLocationException x) {
				// ignore - this may happen when the document was modified after the initial invocation, and the
				// context does not track the changes properly - don't trim in that case
				return false;
			}
		}
		return false;
	}

	private void format(IDocument doc) throws BadLocationException {
		Map<String, String> options;
		if (fProject != null)
			options = fProject.getOptions(true);
		else
			options = CCorePlugin.getOptions();

		TextEdit edit = CodeFormatterUtil.format(CodeFormatter.K_UNKNOWN, doc.get(), fInitialIndentLevel,
				fLineDelimiter, options);
		if (edit == null)
			throw new BadLocationException(); // fall back to indenting

		edit.apply(doc, TextEdit.UPDATE_REGIONS);
	}

	private void indent(IDocument document, TemplateBuffer buffer) throws BadLocationException, MalformedTreeException {
		prefixSelectionLines(document, buffer);
		adjustIndentation(document);
	}

	/**
	 * Convert leading tabs to correct indentation and add initial indentation level.
	 *
	 * @param document
	 * @throws BadLocationException
	 */
	private void adjustIndentation(IDocument document) throws BadLocationException {
		int lines = document.getNumberOfLines();
		if (lines == 0)
			return;

		String option;
		if (fProject == null)
			option = CCorePlugin.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
		else
			option = fProject.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, true);

		boolean useSpaces = CCorePlugin.SPACE.equals(option);
		int indentWidth = CodeFormatterUtil.getIndentWidth(fProject);
		int tabWidth = CodeFormatterUtil.getTabWidth(fProject);

		for (int line = 0; line < lines; ++line) {
			IRegion lineRegion = document.getLineInformation(line);
			String indent = IndentUtil.getCurrentIndent(document, line, false);
			int tabs = 0;
			for (int i = 0; i < indent.length(); i++) {
				if (indent.charAt(i) == '\t') {
					tabs++;
				} else {
					break;
				}
			}
			int totalIndentWidth = (fInitialIndentLevel + tabs) * indentWidth
					+ IndentUtil.computeVisualLength(indent.substring(tabs), tabWidth);
			String newIndent = IndentUtil.changePrefix("", totalIndentWidth, tabWidth, useSpaces); //$NON-NLS-1$
			document.replace(lineRegion.getOffset(), indent.length(), newIndent);
		}
	}

	/**
	 * Prefix each line of <code>line_selection</code> substitutions with
	 * the prefix of the first line.
	 *
	 * @param document
	 * @param buffer
	 */
	private void prefixSelectionLines(IDocument document, TemplateBuffer buffer) {
		TemplateVariable[] variables = buffer.getVariables();
		for (int i = 0; i < variables.length; i++) {
			if (variables[i].getName().equals(GlobalTemplateVariables.LineSelection.NAME)) {
				if (variables[i].getLength() > 0) {
					final int tabWidth = CodeFormatterUtil.getTabWidth(fProject);
					int delta = 0;
					int[] offsets = variables[i].getOffsets();
					for (int j = 0; j < offsets.length; j++) {
						final int offset = offsets[j] + delta;
						try {
							int startLine = document.getLineOfOffset(offset);
							int startOffset = document.getLineOffset(startLine);
							int endLine = document.getLineOfOffset(offset + variables[i].getLength() - 1);
							String prefix = document.get(startOffset, offset - startOffset);
							String shift = trimAtOffset(offset, document);
							delta -= shift.length();
							if (startLine < endLine) {
								int shiftWidth = IndentUtil.computeVisualLength(shift, tabWidth);
								for (int line = startLine + 1; line <= endLine; ++line) {
									String currentIndent = IndentUtil.getCurrentIndent(document, line, false);
									int indentWidth = IndentUtil.computeVisualLength(currentIndent, tabWidth);
									int newIndentWidth = Math.max(0, indentWidth - shiftWidth);
									// replace current indent with prefix + spaces
									String newIndent = IndentUtil.changePrefix(prefix, prefix.length() + newIndentWidth,
											1, true);
									int lineOffset = document.getLineOffset(line);
									document.replace(lineOffset, currentIndent.length(), newIndent);
									delta -= currentIndent.length();
									delta += newIndent.length();
								}
							}
						} catch (BadLocationException exc) {
							break;
						}
					}
				}
				break;
			}
		}
	}

	/**
	 * Wraps a {@link TemplateBuffer} and tracks the variable offsets while changes to the buffer
	 * occur. Whitespace variables are also tracked.
	 */
	private static final class VariableTracker {
		private static final String CATEGORY = "__template_variables"; //$NON-NLS-1$
		private Document fDocument;
		private final TemplateBuffer fBuffer;
		private List<TypedPosition> fPositions;

		/**
		 * Creates a new tracker.
		 *
		 * @param buffer the buffer to track
		 * @throws MalformedTreeException
		 * @throws BadLocationException
		 */
		public VariableTracker(TemplateBuffer buffer) throws MalformedTreeException, BadLocationException {
			Assert.isLegal(buffer != null);
			fBuffer = buffer;
			fDocument = new Document(fBuffer.getString());
			installCStuff(fDocument);
			fDocument.addPositionCategory(CATEGORY);
			fDocument.addPositionUpdater(new ExclusivePositionUpdater(CATEGORY));
			fPositions = createRangeMarkers(fBuffer.getVariables(), fDocument);
		}

		/**
		 * Installs a C partitioner with <code>document</code>.
		 *
		 * @param document the document
		 */
		private static void installCStuff(Document document) {
			String[] types = new String[] { ICPartitions.C_MULTI_LINE_COMMENT, ICPartitions.C_SINGLE_LINE_COMMENT,
					ICPartitions.C_STRING, ICPartitions.C_CHARACTER, ICPartitions.C_PREPROCESSOR,
					IDocument.DEFAULT_CONTENT_TYPE };
			FastPartitioner partitioner = new FastPartitioner(new FastCPartitionScanner(), types);
			partitioner.connect(document);
			document.setDocumentPartitioner(ICPartitions.C_PARTITIONING, partitioner);
		}

		/**
		 * Returns the document with the buffer contents. Whitespace variables are decorated with
		 * comments.
		 *
		 * @return the buffer document
		 */
		public IDocument getDocument() {
			checkState();
			return fDocument;
		}

		private void checkState() {
			if (fDocument == null)
				throw new IllegalStateException();
		}

		/**
		 * Restores any decorated regions and updates the buffer's variable offsets.
		 *
		 * @return the buffer.
		 * @throws MalformedTreeException
		 * @throws BadLocationException
		 */
		public TemplateBuffer updateBuffer() throws MalformedTreeException, BadLocationException {
			checkState();
			TemplateVariable[] variables = fBuffer.getVariables();
			try {
				removeRangeMarkers(fPositions, fDocument, variables);
			} catch (BadPositionCategoryException x) {
				Assert.isTrue(false);
			}
			fBuffer.setContent(fDocument.get(), variables);
			fDocument = null;

			return fBuffer;
		}

		private List<TypedPosition> createRangeMarkers(TemplateVariable[] variables, IDocument document)
				throws MalformedTreeException, BadLocationException {
			Map<ReplaceEdit, String> markerToOriginal = new HashMap<>();

			MultiTextEdit root = new MultiTextEdit(0, document.getLength());
			List<TextEdit> edits = new ArrayList<>();
			boolean hasModifications = false;
			for (int i = 0; i != variables.length; i++) {
				final TemplateVariable variable = variables[i];
				int[] offsets = variable.getOffsets();

				String value = variable.getDefaultValue();
				if (isWhitespaceVariable(value)) {
					// replace whitespace positions with unformattable comments
					String placeholder = COMMENT_START + value + COMMENT_END;
					for (int j = 0; j != offsets.length; j++) {
						ReplaceEdit replace = new ReplaceEdit(offsets[j], value.length(), placeholder);
						root.addChild(replace);
						hasModifications = true;
						markerToOriginal.put(replace, value);
						edits.add(replace);
					}
				} else {
					for (int j = 0; j != offsets.length; j++) {
						RangeMarker marker = new RangeMarker(offsets[j], value.length());
						root.addChild(marker);
						edits.add(marker);
					}
				}
			}

			if (hasModifications) {
				// update the document and convert the replaces to markers
				root.apply(document, TextEdit.UPDATE_REGIONS);
			}

			List<TypedPosition> positions = new ArrayList<>();
			for (Iterator<TextEdit> it = edits.iterator(); it.hasNext();) {
				TextEdit edit = it.next();
				try {
					// abuse TypedPosition to piggy back the original contents of the position
					final TypedPosition pos = new TypedPosition(edit.getOffset(), edit.getLength(),
							markerToOriginal.get(edit));
					document.addPosition(CATEGORY, pos);
					positions.add(pos);
				} catch (BadPositionCategoryException x) {
					Assert.isTrue(false);
				}
			}

			return positions;
		}

		private boolean isWhitespaceVariable(String value) {
			int length = value.length();
			return length == 0 || value.trim().length() == 0;
		}

		private void removeRangeMarkers(List<TypedPosition> positions, IDocument document, TemplateVariable[] variables)
				throws MalformedTreeException, BadLocationException, BadPositionCategoryException {

			// revert previous changes
			for (Iterator<TypedPosition> it = positions.iterator(); it.hasNext();) {
				TypedPosition position = it.next();
				// remove and re-add in order to not confuse ExclusivePositionUpdater
				document.removePosition(CATEGORY, position);
				final String original = position.getType();
				if (original != null) {
					document.replace(position.getOffset(), position.getLength(), original);
					position.setLength(original.length());
				}
				document.addPosition(position);
			}

			Iterator<TypedPosition> it = positions.iterator();
			for (int i = 0; i != variables.length; i++) {
				TemplateVariable variable = variables[i];

				int[] offsets = new int[variable.getOffsets().length];
				for (int j = 0; j != offsets.length; j++)
					offsets[j] = it.next().getOffset();

				variable.setOffsets(offsets);
			}

		}
	}
}
