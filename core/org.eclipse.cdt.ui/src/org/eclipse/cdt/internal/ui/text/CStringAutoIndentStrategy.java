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
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditorExtension3;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

/**
 * Auto indent strategy for C strings
 */
public class CStringAutoIndentStrategy extends DefaultIndentLineAutoEditStrategy {

	private String fPartitioning;
	private final ICProject fProject;

	/**
	 * The input string doesn't contain any line delimiter.
	 *
	 * @param inputString the given input string
	 * @return the displayable string.
	 */
	private String displayString(String inputString, CharSequence indentation, String delimiter) {
		int length = inputString.length();
		StringBuilder buffer = new StringBuilder(length);
		java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(inputString, "\n\r", true); //$NON-NLS-1$
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equals("\r")) { //$NON-NLS-1$
				buffer.append("\\r"); //$NON-NLS-1$
				if (tokenizer.hasMoreTokens()) {
					token = tokenizer.nextToken();
					if (token.equals("\n")) { //$NON-NLS-1$
						buffer.append("\\n"); //$NON-NLS-1$
						buffer.append("\"" + delimiter); //$NON-NLS-1$
						buffer.append(indentation);
						buffer.append("\""); //$NON-NLS-1$
						continue;
					}
					buffer.append("\"" + delimiter); //$NON-NLS-1$
					buffer.append(indentation);
					buffer.append("\""); //$NON-NLS-1$
				} else {
					continue;
				}
			} else if (token.equals("\n")) { //$NON-NLS-1$
				buffer.append("\\n"); //$NON-NLS-1$
				buffer.append("\"" + delimiter); //$NON-NLS-1$
				buffer.append(indentation);
				buffer.append("\""); //$NON-NLS-1$
				continue;
			}

			StringBuilder tokenBuffer = new StringBuilder();
			for (int i = 0; i < token.length(); i++){
				char c = token.charAt(i);
				switch (c) {
					case '\r' :
						tokenBuffer.append("\\r"); //$NON-NLS-1$
						break;
					case '\n' :
						tokenBuffer.append("\\n"); //$NON-NLS-1$
						break;
					case '\b' :
						tokenBuffer.append("\\b"); //$NON-NLS-1$
						break;
					case '\t' :
						// keep tabs verbatim
						tokenBuffer.append("\t"); //$NON-NLS-1$
						break;
					case '\f' :
						tokenBuffer.append("\\f"); //$NON-NLS-1$
						break;
					case '\"' :
						tokenBuffer.append("\\\""); //$NON-NLS-1$
						break;
					case '\'' :
						tokenBuffer.append("\\'"); //$NON-NLS-1$
						break;
					case '\\' :
						tokenBuffer.append("\\\\"); //$NON-NLS-1$
						break;
					default :
						tokenBuffer.append(c);
				}
			}
			buffer.append(tokenBuffer);
		}
		return buffer.toString();
	}

	/**
	 * Creates a new C string auto indent strategy for the given document partitioning.
	 *
	 * @param partitioning the document partitioning
	 */
	public CStringAutoIndentStrategy(String partitioning, ICProject project) {
		super();
		fPartitioning = partitioning;
		fProject = project;
	}

	private boolean isLineDelimiter(IDocument document, String text) {
		String[] delimiters= document.getLegalLineDelimiters();
		if (delimiters != null)
			return TextUtilities.equals(delimiters, text) > -1;
		return false;
	}

	private String getModifiedText(String string, CharSequence indentation, String delimiter) {
		return displayString(string, indentation, delimiter);
	}

	private void indentStringAfterNewLine(IDocument document, DocumentCommand command) throws BadLocationException {
		ITypedRegion partition= TextUtilities.getPartition(document, fPartitioning, command.offset, true);
		int offset= partition.getOffset();
		int length= partition.getLength();

		if (command.offset == offset + length && document.getChar(offset + length - 1) == '\"')
			return;

		if (offset > 0 && document.getChar(offset - 1) == 'R')  // raw string
			return;
		
		CHeuristicScanner scanner = new CHeuristicScanner(document);
		CIndenter indenter = new CIndenter(document, scanner, fProject);
		StringBuilder indentation = indenter.computeContinuationLineIndentation(offset);
		if (indentation == null)
			indentation = new StringBuilder();

		String delimiter= TextUtilities.getDefaultLineDelimiter(document);
		IPreferenceStore preferenceStore= CUIPlugin.getDefault().getPreferenceStore();
		if (isLineDelimiter(document, command.text))
			command.text= "\"" + command.text + indentation + "\"";  //$NON-NLS-1$//$NON-NLS-2$
		else if (command.text.length() > 1 && preferenceStore.getBoolean(PreferenceConstants.EDITOR_ESCAPE_STRINGS))
			command.text= getModifiedText(command.text, indentation, delimiter);
	}

	private boolean isSmartMode() {
		IWorkbenchPage page= CUIPlugin.getActivePage();
		if (page != null)  {
			IEditorPart part= page.getActiveEditor();
			if (part instanceof MultiPageEditorPart) {
				part= (IEditorPart)part.getAdapter(ITextEditorExtension3.class);
			}
			if (part instanceof ITextEditorExtension3) {
				ITextEditorExtension3 extension= (ITextEditorExtension3) part;
				return extension.getInsertMode() == ITextEditorExtension3.SMART_INSERT;
			}
		}
		return false;
	}

	/*
	 * @see org.eclipse.jface.text.IAutoIndentStrategy#customizeDocumentCommand(IDocument, DocumentCommand)
	 */
	@Override
	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		try {
			if (command.length != 0 || command.text == null)
				return;

			IPreferenceStore preferenceStore= CUIPlugin.getDefault().getPreferenceStore();

			if (preferenceStore.getBoolean(PreferenceConstants.EDITOR_WRAP_STRINGS) && isSmartMode()) {
				indentStringAfterNewLine(document, command);
			}
		} catch (BadLocationException e) {
		}
	}
}
