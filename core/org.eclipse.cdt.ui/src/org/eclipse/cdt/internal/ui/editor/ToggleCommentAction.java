/*******************************************************************************
 * Copyright (c) 2006, 2015 IBM Corporation and others.
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
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ResourceAction;
import org.eclipse.ui.texteditor.TextEditorAction;

/**
 * An action which toggles comment prefixes on the selected lines.
 *
 * @since 4.0.0
 */
public final class ToggleCommentAction extends TextEditorAction {
	/** The text operation target */
	private ITextOperationTarget fOperationTarget;
	/** The document partitioning */
	private String fDocumentPartitioning;
	/** The comment prefixes */
	private Map<String, String[]> fPrefixesMap;

	/**
	 * Creates and initializes the action for the given text editor. The action
	 * configures its visual representation from the given resource bundle.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param editor the text editor
	 * @see ResourceAction#ResourceAction(ResourceBundle, String, int)
	 */
	public ToggleCommentAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	/**
	 * Implementation of the <code>IAction</code> prototype. Checks if the selected
	 * lines are all commented or not and uncomments/comments them respectively.
	 */
	@Override
	public void run() {
		if (fOperationTarget == null || fDocumentPartitioning == null || fPrefixesMap == null)
			return;

		ITextEditor editor = getTextEditor();
		if (editor == null)
			return;

		if (!validateEditorInputState())
			return;

		final int operationCode;
		if (isSelectionCommented(editor.getSelectionProvider().getSelection()))
			operationCode = ITextOperationTarget.STRIP_PREFIX;
		else
			operationCode = ITextOperationTarget.PREFIX;

		Shell shell = editor.getSite().getShell();
		if (!fOperationTarget.canDoOperation(operationCode)) {
			if (shell != null) {
				MessageDialog.openError(shell, CEditorMessages.ToggleComment_error_title,
						CEditorMessages.ToggleComment_error_message);
			}
			return;
		}

		Display display = null;
		if (shell != null && !shell.isDisposed())
			display = shell.getDisplay();

		BusyIndicator.showWhile(display, () -> fOperationTarget.doOperation(operationCode));
	}

	/**
	 * Is the given selection single-line commented?
	 *
	 * @param selection Selection to check
	 * @return <code>true</code> iff all selected lines are commented
	 */
	private boolean isSelectionCommented(ISelection selection) {
		if (!(selection instanceof ITextSelection))
			return false;

		ITextSelection textSelection = (ITextSelection) selection;
		if (textSelection.getStartLine() < 0 || textSelection.getEndLine() < 0)
			return false;

		IDocument document = getTextEditor().getDocumentProvider().getDocument(getTextEditor().getEditorInput());

		try {
			IRegion block = getTextBlockFromSelection(textSelection, document);
			ITypedRegion[] regions = TextUtilities.computePartitioning(document, fDocumentPartitioning,
					block.getOffset(), block.getLength(), false);

			int[] lines = new int[regions.length * 2]; // [startline, endline, startline, endline, ...]

			// For each partition in the text selection, figure out the startline and endline.
			// Count the number of lines that are selected.
			for (int i = 0, j = 0; i < regions.length; i++, j += 2) {
				// Start line of region
				lines[j] = getFirstCompleteLineOfRegion(regions[i], document);
				// End line of region
				int length = regions[i].getLength();
				int offset = regions[i].getOffset() + length;
				if (length > 0)
					offset--;

				// If there is no startline for this region (startline = -1),
				// then there is no endline,
				// otherwise, get the line number of the endline and store it in the array.
				lines[j + 1] = (lines[j] == -1 ? -1 : document.getLineOfOffset(offset));

				// We could count the number of lines that are selected in this region
				// lineCount += lines[j + 1] - lines[j] + 1;

				assert i < regions.length;
				assert j < regions.length * 2;
			}

			// Perform the check
			boolean hasComment = false;
			for (int i = 0, j = 0; i < regions.length; i++, j += 2) {
				String[] prefixes = fPrefixesMap.get(regions[i].getType());
				if (prefixes != null && prefixes.length > 0 && lines[j] >= 0 && lines[j + 1] >= 0) {
					if (isBlockCommented(lines[j], lines[j + 1], prefixes, document)) {
						hasComment = true;
					} else if (!isBlockEmpty(lines[j], lines[j + 1], document)) {
						return false;
					}
				}
			}
			return hasComment;
		} catch (BadLocationException e) {
			CUIPlugin.log(e); // Should not happen
		}

		return false;
	}

	/**
	 * Creates a region describing the text block (something that starts at
	 * the beginning of a line) completely containing the current selection.
	 *
	 * Note, the implementation has to match {@link TextViewer}.getTextBlockFromSelection().
	 *
	 * @param selection The selection to use
	 * @param document The document
	 * @return the region describing the text block comprising the given selection
	 * @throws BadLocationException
	 */
	private IRegion getTextBlockFromSelection(ITextSelection selection, IDocument document)
			throws BadLocationException {
		int start = document.getLineOffset(selection.getStartLine());
		int end;
		int endLine = selection.getEndLine();
		if (document.getNumberOfLines() > endLine + 1) {
			end = document.getLineOffset(endLine + 1);
		} else {
			end = document.getLength();
		}
		return new Region(start, end - start);
	}

	/**
	 * Returns the index of the first line whose start offset is in the given text range.
	 *
	 * @param region the text range in characters where to find the line
	 * @param document The document
	 * @return the first line whose start index is in the given range, -1 if there is no such line
	 */
	private int getFirstCompleteLineOfRegion(IRegion region, IDocument document) {
		try {
			int startLine = document.getLineOfOffset(region.getOffset());

			int offset = document.getLineOffset(startLine);
			if (offset >= region.getOffset())
				return startLine;

			offset = document.getLineOffset(startLine + 1);
			return (offset > region.getOffset() + region.getLength() ? -1 : startLine + 1);
		} catch (BadLocationException e) {
			CUIPlugin.log(e); // Should not happen
		}

		return -1;
	}

	/**
	 * Determines whether each line is prefixed by one of the prefixes.
	 *
	 * @param startLine Start line in document
	 * @param endLine End line in document
	 * @param prefixes Possible comment prefixes
	 * @param document The document
	 * @return <code>true</code> iff each line from <code>startLine</code>
	 *             to and including <code>endLine</code> is prepended by one
	 *             of the <code>prefixes</code>, ignoring whitespace at the
	 *             begin of line
	 */
	private boolean isBlockCommented(int startLine, int endLine, String[] prefixes, IDocument document) {
		try {
			// Check for occurrences of prefixes in the given lines
			boolean hasComment = false;
			for (int i = startLine; i <= endLine; i++) {
				IRegion line = document.getLineInformation(i);
				String text = document.get(line.getOffset(), line.getLength());

				boolean isEmptyLine = text.trim().length() == 0;
				if (isEmptyLine) {
					continue;
				}

				int[] found = TextUtilities.indexOf(prefixes, text, 0);

				if (found[0] == -1) {
					// Found a line which is not commented
					return false;
				}
				String s = document.get(line.getOffset(), found[0]);
				s = s.trim();
				if (s.length() != 0) {
					// Found a line which is not commented
					return false;
				}
				hasComment = true;
			}
			return hasComment;
		} catch (BadLocationException e) {
			CUIPlugin.log(e); // Should not happen
		}

		return false;
	}

	/**
	 * Determines whether each line is empty
	 *
	 * @param startLine Start line in document
	 * @param endLine End line in document
	 * @param document The document
	 * @return <code>true</code> if each line from <code>startLine</code>
	 *             to and including <code>endLine</code> is empty
	 */
	private boolean isBlockEmpty(int startLine, int endLine, IDocument document) {
		try {
			for (int i = startLine; i <= endLine; i++) {
				IRegion line = document.getLineInformation(i);
				String text = document.get(line.getOffset(), line.getLength());

				boolean isEmptyLine = text.trim().length() == 0;
				if (!isEmptyLine) {
					return false;
				}
			}
			return true;
		} catch (BadLocationException e) {
			CUIPlugin.log(e); // Should not happen
		}

		return false;
	}

	/**
	 * Implementation of the <code>IUpdate</code> prototype method discovers
	 * the operation through the current editor's
	 * <code>ITextOperationTarget</code> adapter, and sets the enabled state
	 * accordingly.
	 */
	@Override
	public void update() {
		super.update();

		if (!canModifyEditor()) {
			setEnabled(false);
			return;
		}

		ITextEditor editor = getTextEditor();
		if (fOperationTarget == null && editor != null)
			fOperationTarget = editor.getAdapter(ITextOperationTarget.class);

		boolean isEnabled = (fOperationTarget != null && fOperationTarget.canDoOperation(ITextOperationTarget.PREFIX)
				&& fOperationTarget.canDoOperation(ITextOperationTarget.STRIP_PREFIX));
		setEnabled(isEnabled);
	}

	/*
	 * @see TextEditorAction#setEditor(ITextEditor)
	 */
	@Override
	public void setEditor(ITextEditor editor) {
		super.setEditor(editor);
		fOperationTarget = null;
	}

	/**
	 * For the different content types, get its default comment prefix and store the prefixes.
	 * @param sourceViewer
	 * @param configuration
	 */
	public void configure(ISourceViewer sourceViewer, SourceViewerConfiguration configuration) {
		fPrefixesMap = null;

		String[] types = configuration.getConfiguredContentTypes(sourceViewer);
		Map<String, String[]> prefixesMap = new HashMap<>(types.length);
		for (String type : types) {
			String[] prefixes = configuration.getDefaultPrefixes(sourceViewer, type);
			if (prefixes != null && prefixes.length > 0) {
				int emptyPrefixes = 0;
				for (String prefixe : prefixes) {
					if (prefixe.length() == 0)
						emptyPrefixes++;
				}

				if (emptyPrefixes > 0) {
					String[] nonemptyPrefixes = new String[prefixes.length - emptyPrefixes];
					for (int j = 0, k = 0; j < prefixes.length; j++) {
						String prefix = prefixes[j];
						if (prefix.length() != 0) {
							nonemptyPrefixes[k] = prefix;
							k++;
						}
					}
					prefixes = nonemptyPrefixes;
				}

				prefixesMap.put(type, prefixes);
			}
		}
		fDocumentPartitioning = configuration.getConfiguredDocumentPartitioning(sourceViewer);
		fPrefixesMap = prefixesMap;
	}
}
