/**********************************************************************
 * Copyright (c) 2002,2003 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.ui.text.makefile;

import java.util.ArrayList;

import org.eclipse.cdt.make.core.makefile.IMacroDefinition;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.makefile.IRule;
import org.eclipse.cdt.make.core.makefile.IStatement;
import org.eclipse.cdt.make.internal.ui.MakeUIImages;
import org.eclipse.cdt.make.internal.ui.editor.MakefileEditor;
import org.eclipse.cdt.make.internal.ui.text.WordPartDetector;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationPresenter;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.swt.graphics.Image;

/**
 * MakefileCompletionProcessor
 */
public class MakefileCompletionProcessor implements IContentAssistProcessor {

	/**
	 * Simple content assist tip closer. The tip is valid in a range
	 * of 5 characters around its popup location.
	 */
	protected static class Validator implements IContextInformationValidator, IContextInformationPresenter {

		protected int fInstallOffset;

		/*
		 * @see IContextInformationValidator#isContextInformationValid(int)
		 */
		public boolean isContextInformationValid(int offset) {
			return Math.abs(fInstallOffset - offset) < 5;
		}

		/*
		 * @see IContextInformationValidator#install(IContextInformation, ITextViewer, int)
		 */
		public void install(IContextInformation info, ITextViewer viewer, int offset) {
			fInstallOffset = offset;
		}

		/*
		 * @see org.eclipse.jface.text.contentassist.IContextInformationPresenter#updatePresentation(int, TextPresentation)
		 */
		public boolean updatePresentation(int documentPosition, TextPresentation presentation) {
			return false;
		}
	}

	protected IContextInformationValidator fValidator = new Validator();
	protected Image imageMacro = MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_MACRO);
	protected Image imageTarget = MakeUIImages.getImage(MakeUIImages.IMG_OBJS_MAKEFILE_TARGET_RULE);
	protected MakefileEditor fEditor;

	public MakefileCompletionProcessor(MakefileEditor editor) {
		fEditor = editor;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeCompletionProposals(org.eclipse.jface.text.ITextViewer, int)
	 */
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
		boolean macro = inMacro(viewer, documentOffset);
		IMakefile makefile = fEditor.getMakefile(viewer.getDocument());
		IStatement[] statements = null;
		if (macro) {
			statements = makefile.getMacroDefinitions();
		} else {
			statements = makefile.getTargetRules();
		}

		ArrayList proposalList = new ArrayList(statements.length);
		WordPartDetector wordPart = new WordPartDetector(viewer, documentOffset);

		// iterate over all the different categories
		for (int i = 0; i < statements.length; i++) {
			String name = null;
			Image image = null;
			String infoString = "";//getContentInfoString(name);
			if (statements[i] instanceof IMacroDefinition) {
				name = ((IMacroDefinition) statements[i]).getName();
				image = imageMacro;
				infoString = ((IMacroDefinition)statements[i]).getValue();
			} else if (statements[i] instanceof IRule) {
				name = ((IRule) statements[i]).getTarget().toString();
				image = imageTarget;
				infoString = name;
			}
			if (name != null && name.startsWith(wordPart.getString())) {
				IContextInformation info = new ContextInformation(name, infoString);
				ICompletionProposal result =
					new CompletionProposal(
						name,
						wordPart.getOffset(),
						wordPart.getString().length(),
						name.length(),
						image,
						name,
						info,
						infoString);
				proposalList.add(result);
			}
		}
		return (ICompletionProposal[]) proposalList.toArray(new ICompletionProposal[0]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#computeContextInformation(org.eclipse.jface.text.ITextViewer, int)
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		WordPartDetector wordPart = new WordPartDetector(viewer, documentOffset);
		boolean macro = inMacro(viewer, documentOffset);
		IMakefile makefile = fEditor.getMakefile(viewer.getDocument());
		ArrayList contextList = new ArrayList();
		if (macro) {
			IStatement[] statements = makefile.getMacroDefinitions();
			for (int i = 0; i < statements.length; i++) {
				if (statements[i] instanceof IMacroDefinition) {
					String name = ((IMacroDefinition) statements[i]).getName();
					if (name != null && name.equals(wordPart.getString())) {
						String value = ((IMacroDefinition) statements[i]).getValue();
						if (value != null && value.length() > 0) {
							contextList.add(value);
						}
					}
				}
			}
		}

		IContextInformation[] result = new IContextInformation[contextList.size()];
		for (int i = 0; i < result.length; i++) {
			String context = (String)contextList.get(i);
			result[i] = new ContextInformation(imageMacro, wordPart.getString(), context);
		}
		return result;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getCompletionProposalAutoActivationCharacters()
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationAutoActivationCharacters()
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getErrorMessage()
	 */
	public String getErrorMessage() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.IContentAssistProcessor#getContextInformationValidator()
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return fValidator;
	}

	private boolean inMacro(ITextViewer viewer, int offset) {
		boolean isMacro = false;
		IDocument document = viewer.getDocument();
		// Try to figure out if we are in a Macro.
		try {
			for (int index = offset - 1; index >= 0; index--) {
				char c;
				c = document.getChar(index);
				if (c == '$') {
					isMacro = true;
					break;
				} else if (Character.isWhitespace(c)) {
					break;
				}
			}
		} catch (BadLocationException e) {
		}
		return isMacro;
	}

}
