/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.quickassist.IQuickAssistInvocationContext;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.texteditor.spelling.SpellingProblem;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.IInvocationContext;

import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellCheckEngine;
import org.eclipse.cdt.internal.ui.text.spelling.engine.ISpellChecker;

/**
 * Proposal to add the unknown word to the dictionaries.
 */
public class AddWordProposal implements ICCompletionProposal {
	private static final String PREF_KEY_DO_NOT_ASK= "do_not_ask_to_install_user_dictionary"; //$NON-NLS-1$
	
	/** The invocation context */
	private final IInvocationContext fContext;

	/** The word to add */
	private final String fWord;

	/**
	 * Creates a new add word proposal
	 *
	 * @param word
	 *                   The word to add
	 * @param context
	 *                   The invocation context
	 */
	public AddWordProposal(final String word, final IInvocationContext context) {
		fContext= context;
		fWord= word;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
	 */
	@Override
	public final void apply(final IDocument document) {
		final ISpellCheckEngine engine= SpellCheckEngine.getInstance();
		final ISpellChecker checker= engine.getSpellChecker();

		if (checker == null)
			return;
		
		IQuickAssistInvocationContext quickAssistContext= null;
		if (fContext instanceof IQuickAssistInvocationContext)
			quickAssistContext= (IQuickAssistInvocationContext)fContext;
		
		if (!checker.acceptsWords()) {
			final Shell shell;
			if (quickAssistContext != null && quickAssistContext.getSourceViewer() != null)
				shell= quickAssistContext.getSourceViewer().getTextWidget().getShell();
			else
				shell= CUIPlugin.getActiveWorkbenchShell();
			
			if (!canAskToConfigure() || !askUserToConfigureUserDictionary(shell))
				return;
			
			String[] preferencePageIds= new String[] { "org.eclipse.ui.editors.preferencePages.Spelling" }; //$NON-NLS-1$
			PreferencesUtil.createPreferenceDialogOn(shell, preferencePageIds[0], preferencePageIds, null).open();
		}
		
		if (checker.acceptsWords()) {
			checker.addWord(fWord);
			if (quickAssistContext != null && quickAssistContext.getSourceViewer() != null) {
				SpellingProblem.removeAll(quickAssistContext.getSourceViewer(), fWord);
			}
		}
	}

	/**
	 * Asks the user whether he wants to configure
	 * a user dictionary.
	 * 
	 * @param shell
	 * @return <code>true</code> if the user wants to configure the user dictionary
	 */
	private boolean askUserToConfigureUserDictionary(Shell shell) {
		MessageDialogWithToggle toggleDialog= MessageDialogWithToggle.openYesNoQuestion(
				shell,
				Messages.Spelling_add_askToConfigure_title,
				Messages.Spelling_add_askToConfigure_question,
				Messages.Spelling_add_askToConfigure_ignoreMessage,
				false,
				null,
				null);
		
		PreferenceConstants.getPreferenceStore().setValue(PREF_KEY_DO_NOT_ASK, toggleDialog.getToggleState());
		
		return toggleDialog.getReturnCode() == IDialogConstants.YES_ID;
	}

	/**
	 * Tells whether this proposal can ask to
	 * configure a user dictionary.
	 * 
	 * @return <code>true</code> if it can ask the user
	 */
	static boolean canAskToConfigure() {
		return !PreferenceConstants.getPreferenceStore().getBoolean(PREF_KEY_DO_NOT_ASK);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
	 */
	@Override
	public String getAdditionalProposalInfo() {
		return NLS.bind(Messages.Spelling_add_info, WordCorrectionProposal.getHtmlRepresentation(fWord));
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
	 */
	@Override
	public final IContextInformation getContextInformation() {
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
	 */
	@Override
	public String getDisplayString() {
		return NLS.bind(Messages.Spelling_add_label, fWord);
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
	 */
	@Override
	public Image getImage() {
		return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CORRECTION_ADD);
	}

	/*
	 * @see org.eclipse.cdt.ui.text.java.IJavaCompletionProposal#getRelevance()
	 */
	@Override
	public int getRelevance() {
		return Integer.MIN_VALUE;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
	 */
	@Override
	public final Point getSelection(final IDocument document) {
		return new Point(fContext.getSelectionOffset(), fContext.getSelectionLength());
	}

	@Override
	public String getIdString() {
		return fWord;
	}
}
