/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.internal.qt.ui.text;

import java.util.Collection;

import javax.script.ScriptException;

import org.eclipse.cdt.internal.qt.ui.Activator;
import org.eclipse.cdt.internal.qt.ui.editor.QMLEditor;
import org.eclipse.cdt.qt.core.QMLAnalyzer;
import org.eclipse.cdt.qt.core.QMLTernCompletion;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.IFileEditorInput;

public class QMLContentAssistProcessor implements IContentAssistProcessor {
	private static final IContextInformation[] NO_CONTEXTS = {};
	private static final ICompletionProposal[] NO_COMPLETIONS = {};

	private final QMLAnalyzer analyzer = Activator.getService(QMLAnalyzer.class);
	private final QMLEditor editor;

	public QMLContentAssistProcessor(QMLEditor editor) {
		this.editor = editor;
	}

	@Override
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int offset) {
		IDocument document = viewer.getDocument();
		String prefix = lastWord(document, offset);
		// Save the file
		IFileEditorInput fileInput = (IFileEditorInput) editor.getEditorInput();
		String fileName = fileInput.getFile().getName();// getLocation().toOSString().substring(1);

		try {
			String contents = document.get();
			analyzer.addFile(fileName, contents);
			Collection<QMLTernCompletion> completions = analyzer.getCompletions(fileName, offset);
			if (!completions.isEmpty()) {
				ICompletionProposal[] proposals = new ICompletionProposal[completions.size()];
				int i = 0;
				for (QMLTernCompletion completion : completions) {
					String name = completion.getName();
					String type = completion.getType();
					String displayString = name;
					if (type != null) {
						displayString += " : " + completion.getType(); //$NON-NLS-1$
					}
					proposals[i++] = new CompletionProposal(name, offset - prefix.length(), prefix.length(),
							name.length(), null, displayString, null, completion.getOrigin());
				}
				return proposals;
			}
		} catch (NoSuchMethodException | ScriptException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return NO_COMPLETIONS;
	}

	private String lastWord(IDocument document, int offset) {
		try {
			for (int n = offset - 1; n >= 0; n--) {
				char c = document.getChar(n);
				if (!Character.isJavaIdentifierPart(c)) {
					return document.get(n + 1, offset - n - 1);
				}
			}
			return document.get(0, offset);
		} catch (BadLocationException e) {
			Activator.log(e);
		}
		return ""; //$NON-NLS-1$
	}

	@Override
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int offset) {
		return NO_CONTEXTS;
	}

	@Override
	public char[] getCompletionProposalAutoActivationCharacters() {
		return new char[] { '.' };
	}

	@Override
	public char[] getContextInformationAutoActivationCharacters() {
		// TODO not sure
		return null;
	}

	@Override
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		// TODO Auto-generated method stub
		return null;
	}

}
