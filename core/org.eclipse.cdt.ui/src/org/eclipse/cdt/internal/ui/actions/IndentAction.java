/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin, Google
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.ResourceBundle;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.ITextEditorExtension3;
import org.eclipse.ui.texteditor.TextEditorAction;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.IndentUtil;
import org.eclipse.cdt.internal.ui.text.CHeuristicScanner;
import org.eclipse.cdt.internal.ui.text.CIndenter;
import org.eclipse.cdt.internal.ui.util.EditorUtility;


/**
 * Indents a line or range of lines in a C document to its correct position. No complete
 * AST must be present, the indentation is computed using heuristics. The algorithm used is fast for
 * single lines, but does not store any information and therefore not so efficient for large line
 * ranges.
 * 
 * @see org.eclipse.cdt.internal.ui.text.CHeuristicScanner
 * @see org.eclipse.cdt.internal.ui.text.CIndenter
 */
public class IndentAction extends TextEditorAction {
	
	/** The caret offset after an indent operation. */
	private int fCaretOffset;
	
	/**
	 * Whether this is the action invoked by TAB. When <code>true</code>, indentation behaves
	 * differently to accommodate normal TAB operation.
	 */
	private final boolean fIsTabAction;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param bundle the resource bundle
	 * @param prefix the prefix to use for keys in <code>bundle</code>
	 * @param editor the text editor
	 * @param isTabAction whether the action should insert tabs if over the indentation
	 */
	public IndentAction(ResourceBundle bundle, String prefix, ITextEditor editor, boolean isTabAction) {
		super(bundle, prefix, editor);
		fIsTabAction= isTabAction;
	}
	
	/*
	 * @see org.eclipse.jface.action.Action#run()
	 */
	@Override
	public void run() {
		// update has been called by the framework
		if (!isEnabled() || !validateEditorInputState())
			return;
		
		ITextSelection selection= getSelection();
		final IDocument document= getDocument();
		
		if (document != null) {
			final int offset= selection.getOffset();
			final int length= selection.getLength();
			final Position end= new Position(offset + length);
			final int firstLine, nLines;
			fCaretOffset= -1;
			
			try {
				firstLine= document.getLineOfOffset(offset);
				// check for marginal (zero-length) lines
				int minusOne= length == 0 ? 0 : 1;
				nLines= document.getLineOfOffset(offset + length - minusOne) - firstLine + 1;
				document.addPosition(end);
			} catch (BadLocationException e) {
				// will only happen on concurrent modification
				CUIPlugin.log(new Status(IStatus.ERROR, CUIPlugin.getPluginId(), IStatus.OK, "", e)); //$NON-NLS-1$
				return;
			}
			
			Runnable runnable= new Runnable() {
				public void run() {
					IRewriteTarget target= (IRewriteTarget)getTextEditor().getAdapter(IRewriteTarget.class);
					if (target != null)
						target.beginCompoundChange();
					
					try {
						CHeuristicScanner scanner= new CHeuristicScanner(document);
						CIndenter indenter= new CIndenter(document, scanner, getCProject());
						final boolean multiLine= nLines > 1;
						boolean hasChanged= false;
						for (int i= 0; i < nLines; i++) {
							hasChanged |= indentLine(document, firstLine + i, offset, indenter, scanner, multiLine);
						}
						
						// update caret position: move to new position when indenting just one line
						// keep selection when indenting multiple
						int newOffset, newLength;
						if (!fIsTabAction && multiLine) {
							newOffset= offset;
							newLength= end.getOffset() - offset;
						} else {
							newOffset= fCaretOffset;
							newLength= 0;
						}
						
						// always reset the selection if anything was replaced
						// but not when we had a single line non-tab invocation
						if (newOffset != -1 && (hasChanged || newOffset != offset || newLength != length))
							selectAndReveal(newOffset, newLength);
						
					} catch (BadLocationException e) {
						// will only happen on concurrent modification
						CUIPlugin.log(new Status(IStatus.ERROR, CUIPlugin.getPluginId(), IStatus.OK, "ConcurrentModification in IndentAction", e)); //$NON-NLS-1$
					} finally {
						document.removePosition(end);
						if (target != null)
							target.endCompoundChange();
					}
				}
			};
			
			if (nLines > 50) {
				Display display= getTextEditor().getEditorSite().getWorkbenchWindow().getShell().getDisplay();
				BusyIndicator.showWhile(display, runnable);
			} else {
				runnable.run();
			}
		}
	}
	
	/**
	 * Selects the given range on the editor.
	 * 
	 * @param newOffset the selection offset
	 * @param newLength the selection range
	 */
	private void selectAndReveal(int newOffset, int newLength) {
		Assert.isTrue(newOffset >= 0);
		Assert.isTrue(newLength >= 0);
		ITextEditor editor= getTextEditor();
		if (editor instanceof CEditor) {
			ISourceViewer viewer= ((CEditor)editor).getViewer();
			if (viewer != null)
				viewer.setSelectedRange(newOffset, newLength);
		} else {
			// this is too intrusive, but will never get called anyway
			getTextEditor().selectAndReveal(newOffset, newLength);
		}
	}

	/**
	 * Indents a single line using the heuristic scanner. Multiline comments are
	 * indented as specified by the <code>CCommentAutoIndentStrategy</code>.
	 * 
	 * @param document the document
	 * @param line the line to be indented
	 * @param caret the caret position
	 * @param indenter the indenter
	 * @param scanner the heuristic scanner
	 * @param multiLine <code>true</code> if more than one line is being indented
	 * @return <code>true</code> if <code>document</code> was modified, <code>false</code> otherwise
	 * @throws BadLocationException if the document got changed concurrently
	 */
	private boolean indentLine(IDocument document, int line, int caret, CIndenter indenter, CHeuristicScanner scanner, boolean multiLine) throws BadLocationException {
		IRegion currentLine= document.getLineInformation(line);
		int offset= currentLine.getOffset();
		int wsStart= offset; // where we start searching for non-WS; after the "//" in single line comments
		
		String indent= null;
		if (offset < document.getLength()) {
			ITypedRegion partition= TextUtilities.getPartition(document, ICPartitions.C_PARTITIONING, offset, true);
			ITypedRegion startingPartition= TextUtilities.getPartition(document, ICPartitions.C_PARTITIONING, offset, false);
			String type= partition.getType();
			if (type.equals(ICPartitions.C_MULTI_LINE_COMMENT) || type.equals(ICPartitions.C_MULTI_LINE_DOC_COMMENT)) {
				indent= computeCommentIndent(document, line, scanner, startingPartition);
			} else if (startingPartition.getType().equals(ICPartitions.C_PREPROCESSOR)) {
				indent= computePreprocessorIndent(document, line, startingPartition);
			} else if (startingPartition.getType().equals(ICPartitions.C_STRING) && offset > startingPartition.getOffset()) {
				// don't indent inside (raw-)string
				return false;
			} else if (!fIsTabAction && startingPartition.getOffset() == offset && startingPartition.getType().equals(ICPartitions.C_SINGLE_LINE_COMMENT)) {
				// line comment starting at position 0 -> indent inside
				if (indentInsideLineComments()) {
					int max= document.getLength() - offset;
					int slashes= 2;
					while (slashes < max - 1 && document.get(offset + slashes, 2).equals("//")) //$NON-NLS-1$
						slashes+= 2;
					
					wsStart= offset + slashes;
					
					StringBuilder computed= indenter.computeIndentation(offset);
					if (computed == null)
						computed= new StringBuilder(0);
					int tabSize= getTabSize();
					while (slashes > 0 && computed.length() > 0) {
						char c= computed.charAt(0);
						if (c == '\t') {
							if (slashes > tabSize)
								slashes-= tabSize;
							else
								break;
						} else if (c == ' ') {
							slashes--;
						} else {
							break;
						}
						
						computed.deleteCharAt(0);
					}
					
					indent= document.get(offset, wsStart - offset) + computed;
				}
			}
		}
		
		// standard C code indentation
		if (indent == null) {
			StringBuilder computed= indenter.computeIndentation(offset);
			if (computed != null)
				indent= computed.toString();
			else
				indent= ""; //$NON-NLS-1$
		}
		
		// change document:
		// get current white space
		int lineLength= currentLine.getLength();
		int end= scanner.findNonWhitespaceForwardInAnyPartition(wsStart, offset + lineLength);
		if (end == CHeuristicScanner.NOT_FOUND) {
			// an empty line
			end= offset + lineLength;
			if (multiLine && !indentEmptyLines())
				indent= ""; //$NON-NLS-1$
		}
		int length= end - offset;
		String currentIndent= document.get(offset, length);
		
		// if we are right before the text start / line end, and already after the insertion point
		// then just shift to the right
		if (fIsTabAction && caret == end && whiteSpaceLength(currentIndent) >= whiteSpaceLength(indent)) {
			int indentWidth= whiteSpaceLength(currentIndent) + getIndentSize();
			if (useTabsAndSpaces()) {
				currentIndent = trimSpacesRight(currentIndent);
			}
			String replacement= IndentUtil.changePrefix(currentIndent, indentWidth, getTabSize(), useSpaces());
			document.replace(offset, length, replacement);
			fCaretOffset= offset + replacement.length();
			return true;
		}
		
		// set the caret offset so it can be used when setting the selection
		if (caret >= offset && caret <= end)
			fCaretOffset= offset + indent.length();
		else
			fCaretOffset= -1;
		
		// only change the document if it is a real change
		if (!indent.equals(currentIndent)) {
			document.replace(offset, length, indent);
			return true;
		}
		return false;
	}

	/**
	 * Strip trailing space characters.
	 * 
	 * @param indent
	 * @return string with trailing spaces removed
	 */
	private String trimSpacesRight(String indent) {
		int i = indent.length() - 1;
		while (i >= 0 && indent.charAt(i) == ' ') {
			--i;
		}
		return indent.substring(0, i+1);
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
	private String computeCommentIndent(IDocument document, int line, CHeuristicScanner scanner, ITypedRegion partition) throws BadLocationException {
		return IndentUtil.computeCommentIndent(document, line, scanner, partition);
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
	private String computePreprocessorIndent(IDocument document, int line, ITypedRegion partition) throws BadLocationException {
		return IndentUtil.computePreprocessorIndent(document, line, partition);
	}
	
	/**
	 * Returns the size in characters of a string. All characters count one, tabs count the editor's
	 * preference for the tab display
	 * 
	 * @param indent the string to be measured.
	 * @return the size in characters of a string
	 */
	private int whiteSpaceLength(String indent) {
		if (indent == null)
			return 0;
		return IndentUtil.computeVisualLength(indent, getTabSize());
	}

	/**
	 * Returns whether spaces should be used exclusively for indentation, depending on the editor and
	 * formatter preferences.
	 * 
	 * @return <code>true</code> if only spaces should be used
	 */
	private boolean useSpaces() {
		return CCorePlugin.SPACE.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR));
	}
	
	/**
	 * Returns whether mixed tabs/spaces should be used for indentation, depending on the editor and
	 * formatter preferences.
	 * 
	 * @return <code>true</code> if tabs and spaces should be used
	 */
	private boolean useTabsAndSpaces() {
		return DefaultCodeFormatterConstants.MIXED.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR));
	}
	
	/**
	 * Returns the tab size used by the editor, which is deduced from the
	 * formatter preferences.
	 * 
	 * @return the tab size as defined in the current formatter preferences
	 */
	private int getTabSize() {
		return getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE, 4);
	}

	/**
	 * Returns the indent size used by the editor, which is deduced from the
	 * formatter preferences.
	 * 
	 * @return the indent size as defined in the current formatter preferences
	 */
	private int getIndentSize() {
		return CodeFormatterUtil.getIndentWidth(getCProject());
	}

	/**
	 * Returns <code>true</code> if empty lines should be indented, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if empty lines should be indented, <code>false</code> otherwise
	 */
	private boolean indentEmptyLines() {
		return DefaultCodeFormatterConstants.TRUE.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_EMPTY_LINES));
	}
	
	/**
	 * Returns <code>true</code> if line comments at column 0 should be indented inside, <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if line comments at column 0 should be indented inside, <code>false</code> otherwise.
	 */
	private boolean indentInsideLineComments() {
		return DefaultCodeFormatterConstants.TRUE.equals(getCoreFormatterOption(DefaultCodeFormatterConstants.FORMATTER_INDENT_INSIDE_LINE_COMMENTS));
	}
	
	/**
	 * Returns the possibly project-specific core preference defined under <code>key</code>.
	 * 
	 * @param key the key of the preference
	 * @return the value of the preference
	 */
	private String getCoreFormatterOption(String key) {
		ICProject project= getCProject();
		if (project == null)
			return CCorePlugin.getOption(key);
		return project.getOption(key, true);
	}

	/**
	 * Returns the possibly project-specific core preference defined under <code>key</code>, or
	 * <code>def</code> if the value is not a integer.
	 * 
	 * @param key the key of the preference
	 * @param def the default value
	 * @return the value of the preference
	 */
	private int getCoreFormatterOption(String key, int def) {
		try {
			return Integer.parseInt(getCoreFormatterOption(key));
		} catch (NumberFormatException e) {
			return def;
		}
	}

	/**
	 * Returns the <code>ICProject</code> of the current editor input, or
	 * <code>null</code> if it cannot be found.
	 * 
	 * @return the <code>ICProject</code> of the current editor input, or
	 *         <code>null</code> if it cannot be found
	 */
	private ICProject getCProject() {
		ITextEditor editor= getTextEditor();
		if (editor == null)
			return null;
		
		return EditorUtility.getCProject(editor.getEditorInput());
	}

	/**
	 * Returns the editor's selection provider.
	 * 
	 * @return the editor's selection provider or <code>null</code>
	 */
	private ISelectionProvider getSelectionProvider() {
		ITextEditor editor= getTextEditor();
		if (editor != null) {
			return editor.getSelectionProvider();
		}
		return null;
	}
	
	/*
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	@Override
	public void update() {
		super.update();
		
		if (isEnabled()) {
			if (fIsTabAction)
				setEnabled(canModifyEditor() && isSmartMode() && isValidSelection());
			else
				setEnabled(canModifyEditor() && !getSelection().isEmpty());
		}
	}
	
	/**
	 * Returns if the current selection is valid, i.e. whether it is empty and the caret in the
	 * whitespace at the start of a line, or covers multiple lines.
	 * 
	 * @return <code>true</code> if the selection is valid for an indent operation
	 */
	private boolean isValidSelection() {
		ITextSelection selection= getSelection();
		if (selection.isEmpty())
			return false;
		
		int offset= selection.getOffset();
		int length= selection.getLength();
		
		IDocument document= getDocument();
		if (document == null)
			return false;
		
		try {
			IRegion firstLine= document.getLineInformationOfOffset(offset);
			int lineOffset= firstLine.getOffset();
			
			// either the selection has to be empty and the caret in the WS at the line start
			// or the selection has to extend over multiple lines
			if (length == 0) {
				return document.get(lineOffset, offset - lineOffset).trim().length() == 0;
			}
//			return lineOffset + firstLine.getLength() < offset + length;
			return false; // only enable for empty selections for now
		} catch (BadLocationException e) {
		}
		
		return false;
	}
	
	/**
	 * Returns the smart preference state.
	 * 
	 * @return <code>true</code> if smart mode is on, <code>false</code> otherwise
	 */
	private boolean isSmartMode() {
		ITextEditor editor= getTextEditor();
		
		if (editor instanceof ITextEditorExtension3)
			return ((ITextEditorExtension3) editor).getInsertMode() == ITextEditorExtension3.SMART_INSERT;
		
		return false;
	}
	
	/**
	 * Returns the document currently displayed in the editor, or <code>null</code> if none can be
	 * obtained.
	 * 
	 * @return the current document or <code>null</code>
	 */
	private IDocument getDocument() {
		
		ITextEditor editor= getTextEditor();
		if (editor != null) {
			
			IDocumentProvider provider= editor.getDocumentProvider();
			IEditorInput input= editor.getEditorInput();
			if (provider != null && input != null)
				return provider.getDocument(input);
			
		}
		return null;
	}
	
	/**
	 * Returns the selection on the editor or an invalid selection if none can be obtained. Returns
	 * never <code>null</code>.
	 * 
	 * @return the current selection, never <code>null</code>
	 */
	private ITextSelection getSelection() {
		ISelectionProvider provider= getSelectionProvider();
		if (provider != null) {
			
			ISelection selection= provider.getSelection();
			if (selection instanceof ITextSelection)
				return (ITextSelection) selection;
		}
		
		// null object
		return TextSelection.emptySelection();
	}
}
