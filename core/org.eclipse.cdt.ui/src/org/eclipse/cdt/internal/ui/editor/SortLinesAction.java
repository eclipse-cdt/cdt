/*******************************************************************************
 * Copyright (c) 2010 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.editor;

import java.util.Arrays;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.undo.DocumentUndoManagerRegistry;
import org.eclipse.text.undo.IDocumentUndoManager;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

import com.ibm.icu.text.Collator;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.cdt.internal.corext.util.Strings;

import org.eclipse.cdt.internal.ui.util.EditorUtility;

/**
 * Sorts selected lines in alphabetical order. If both, comment and non-comment lines
 * are selected, the non-comment lines are sorted, and the comments are moved together
 * with the non-comment lines they precede.
 *
 * @since 5.2
 */
public final class SortLinesAction extends TextEditorAction {

	public SortLinesAction(ITextEditor editor) {
		super(CEditorMessages.getBundleForConstructedKeys(), "SortLines.", editor); //$NON-NLS-1$
	}

	/**
	 * Sorts selected lines.
	 */
	@Override
	public void run() {
		ITextEditor editor= getTextEditor();
		if (editor == null)
			return;

		ISelection selection = editor.getSelectionProvider().getSelection();
		if (!(selection instanceof ITextSelection))
			return;

		ITextSelection textSelection= (ITextSelection) selection;
		if (textSelection.getStartLine() < 0 || textSelection.getEndLine() < 0)
			return;

		IEditorInput editorInput = editor.getEditorInput();
		ICProject cProject = EditorUtility.getCProject(editorInput);
		IDocument document= editor.getDocumentProvider().getDocument(editorInput);
		try {
			IRegion block= getTextBlockFromSelection(textSelection, document);
			SortElement[] elements = createSortElements(block, document,
					CodeFormatterUtil.getTabWidth(cProject));
			if (elements.length <= 1)
				return;

			Arrays.sort(elements);
			StringBuilder buf = new StringBuilder();
			for (SortElement element : elements) {
				buf.append(document.get(element.getOffset(), element.getLength()));
				if (!isLastLineTerminated(element, document)) {
					buf.append(TextUtilities.getDefaultLineDelimiter(document));
				}
			}
			String replacement = buf.toString();
			if (replacement.equals(document.get(block.getOffset(), block.getLength())))
				return;
			if (!validateEditorInputState())
				return;

			ReplaceEdit edit = new ReplaceEdit(block.getOffset(), block.getLength(), replacement);
			IDocumentUndoManager manager= DocumentUndoManagerRegistry.getDocumentUndoManager(document);
			manager.beginCompoundChange();
			edit.apply(document);
			editor.getSelectionProvider().setSelection(new TextSelection(block.getOffset(), buf.length()));
			manager.endCompoundChange();
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
		}
	}

	/**
	 * Creates a region describing the text block (something that consists of full lines)
	 * completely containing the current selection.
	 *
	 * @param selection The selection to use
	 * @param document The document
	 * @return the region describing the text block comprising the given selection
	 */
	private IRegion getTextBlockFromSelection(ITextSelection selection, IDocument document) {
		try {
			IRegion firstLine= document.getLineInformationOfOffset(selection.getOffset());
			int selectionEnd = selection.getOffset() + selection.getLength();
			IRegion lastLine= document.getLineInformationOfOffset(selectionEnd);
			int length = lastLine.getOffset() - firstLine.getOffset();
			if (selectionEnd > lastLine.getOffset()) {
				 // Last line is included with the line delimiter.
				length += document.getLineLength(document.getLineOfOffset(selectionEnd));
			}
			return new Region(firstLine.getOffset(), length);
		} catch (BadLocationException e) {
			CUIPlugin.log(e);  // Should not happen
		}
		return null;
	}

	private SortElement[] createSortElements(IRegion block, IDocument document, int tabWidth)
			throws BadLocationException {
		ITypedRegion[] regions= TextUtilities.computePartitioning(document, ICPartitions.C_PARTITIONING,
				block.getOffset(), block.getLength(), false);

		int numLines = document.getNumberOfLines(block.getOffset(), block.getLength());
		if (endOf(block) <= document.getLineInformationOfOffset(endOf(block)).getOffset()) {
			numLines--;  // Last line is excluded
		}
		LineInfo[] lineDescriptors = new LineInfo[numLines];
		int numNonCommentLines = 0;
		int i = 0;
		int k = 0;
		int line = document.getLineOfOffset(block.getOffset());
		int endLine = line + numLines;
		for (; line < endLine; line++) {
			LineInfo lineInfo = new LineInfo(document, line);
			lineDescriptors[k++] = lineInfo;
			while (i < regions.length && endOf(regions[i]) <= lineInfo.getTrimmedOffset())
				i++;
			for (; i < regions.length && regions[i].getOffset() < lineInfo.getTrimmedEndOffset(); i++) {
				ITypedRegion region = regions[i];
				if (region.getType() != ICPartitions.C_MULTI_LINE_COMMENT &&
						region.getType() != ICPartitions.C_MULTI_LINE_DOC_COMMENT &&
						region.getType() != ICPartitions.C_SINGLE_LINE_COMMENT &&
						region.getType() != ICPartitions.C_SINGLE_LINE_DOC_COMMENT) {
					lineInfo.nonComment = true;
					break;
				}
			}
			if (lineInfo.nonComment) {
				numNonCommentLines++;
			}
		}
		SortElement[] elements;
		if (numNonCommentLines > 1) {
			elements = new SortElement[numNonCommentLines];
			k = 0;
			int offset = block.getOffset();
			for (int j = 0; j < lineDescriptors.length; j++) {
				LineInfo lineInfo = lineDescriptors[j];
				if (lineInfo.nonComment) {
					int endOffset = k < numNonCommentLines - 1 ?
							lineInfo.getEndOffset() : block.getOffset() + block.getLength();
					elements[k++] = new SortElement(new Region(offset, endOffset - offset), lineInfo,
							document, tabWidth);
					offset = lineInfo.getEndOffset();
				}
			}
		} else {
			elements = new SortElement[numLines];
			for (int j = 0; j < lineDescriptors.length; j++) {
				LineInfo lineInfo = lineDescriptors[j];
				elements[j] = new SortElement(lineInfo, lineInfo, document, tabWidth);
			}
		}
		return elements;
	}

	/**
	 * Returns end offset of a region.
	 */
	private int endOf(IRegion region) {
		return region.getOffset() + region.getLength();
	}

	/**
	 * Returns <code>true</code> if the given region is terminated by a line delimiter.
	 */
	private static boolean isLastLineTerminated(IRegion region, IDocument document) throws BadLocationException {
		int offset = region.getOffset() + region.getLength();
		IRegion nextLine = document.getLineInformationOfOffset(offset);
		return nextLine.getOffset() == offset;
	}

	@Override
	public void update() {
		if (!canModifyEditor()) {
			setEnabled(false);
			return;
		}

		// Enable if two or more lines are selected.
		boolean enabled = false;
		ITextEditor editor = getTextEditor();
		if (editor != null) {
			ISelection selection = editor.getSelectionProvider().getSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection= (ITextSelection) selection;
				int startLine = textSelection.getStartLine();
				int endLine = textSelection.getEndLine();
				if (startLine >= 0 && endLine > startLine) {
					if (endLine == startLine + 1) {
						IDocument document= editor.getDocumentProvider().getDocument(editor.getEditorInput());
						try {
							if (textSelection.getOffset() + textSelection.getLength() > document.getLineOffset(endLine)) {
								enabled = true;
							}
						} catch (BadLocationException e) {
							CUIPlugin.log(e);
						}
					} else {
						enabled = true;
					}
				}
			}
		}
		setEnabled(enabled);
	}

	/*
	 * @see TextEditorAction#setEditor(ITextEditor)
	 */
	@Override
	public void setEditor(ITextEditor editor) {
		super.setEditor(editor);
	}

	private static class SortElement implements Comparable<SortElement>, IRegion {
		private static final Collator collator = Collator.getInstance();
		private final IRegion region;
		private final String collationKey;

		public SortElement(IRegion region, IRegion collationLine, IDocument document, int tabWidth)
				throws BadLocationException {
			super();
			this.region = region;
			this.collationKey = Strings.convertTabsToSpaces(Strings.trimTrailingTabsAndSpaces(
					document.get(collationLine.getOffset(), collationLine.getLength())), tabWidth);
		}

		@Override
		public int compareTo(SortElement other) {
			return collator.compare(collationKey, other.collationKey);
		}

		@Override
		public int getOffset() {
			return region.getOffset();
		}

		@Override
		public int getLength() {
			return region.getLength();
		}
	}

	private static class LineInfo implements IRegion {
		final int offset;
		final int length;
		final int trimmedOffset;
		final int trimmedEndOffset;
		boolean nonComment;

		LineInfo(IDocument document, int line) throws BadLocationException {
			offset = document.getLineOffset(line);
			length = document.getLineLength(line);
			int begin = offset;
			int end = offset + length;
			while (--end >= begin && Character.isWhitespace(document.getChar(end))) {
			}
			end++;
			while (begin < end && Character.isWhitespace(document.getChar(begin))) {
				begin++;
			}
			trimmedOffset = begin;
			trimmedEndOffset = end;
		}

		/**
		 * Offset of the line in the document.
		 */
		@Override
		public int getOffset() {
			return offset;
		}

		/**
		 * Length of the line including line delimiter.
		 */
		@Override
		public int getLength() {
			return length;
		}

		/**
		 * End offset of the line including line delimiter.
		 */
		public int getEndOffset() {
			return offset + length;
		}

		/**
		 * Document offset of the first non-whitespace character of the line.
		 */
		public int getTrimmedOffset() {
			return trimmedOffset;
		}

		/**
		 * Document offset after the last non-whitespace character of the line.
		 */
		public int getTrimmedEndOffset() {
			return trimmedEndOffset;
		}
	}
}
