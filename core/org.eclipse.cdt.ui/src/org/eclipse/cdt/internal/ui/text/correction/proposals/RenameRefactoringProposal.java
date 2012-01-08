/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.correction.proposals;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.refactoring.actions.CRenameAction;
import org.eclipse.cdt.ui.text.ICCompletionProposal;

import org.eclipse.cdt.internal.corext.util.Messages;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;
import org.eclipse.cdt.internal.ui.text.correction.CorrectionCommandHandler;
import org.eclipse.cdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.cdt.internal.ui.text.correction.ICommandAccess;

/**
 * A quick assist proposal that starts the Rename refactoring.
 */
public class RenameRefactoringProposal implements ICCompletionProposal, ICompletionProposalExtension6, ICommandAccess {
	private final CEditor fEditor;
	private final String fLabel;
	private int fRelevance;

	public RenameRefactoringProposal(CEditor editor) {
		fEditor = editor;
		fLabel= CorrectionMessages.RenameRefactoringProposal_name;
		fRelevance= 8;
	}

	/*
	 * @see ICompletionProposal#apply(IDocument)
	 */
	@Override
	public void apply(IDocument document) {
		CRenameAction action= new CRenameAction();
		action.setEditor(fEditor);
		action.run();
	}

	/*
	 * @see ICompletionProposal#getSelection(IDocument)
	 */
	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	/*
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	@Override
	public String getAdditionalProposalInfo() {
		return CorrectionMessages.RenameRefactoringProposal_additionalInfo;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	@Override
	public String getDisplayString() {
		String shortCutString= CorrectionCommandHandler.getShortCutString(getCommandId());
		if (shortCutString != null) {
			return Messages.format(CorrectionMessages.ChangeCorrectionProposal_name_with_shortcut,
					new String[] { fLabel, shortCutString });
		}
		return fLabel;
	}

	/*
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension6#getStyledDisplayString()
	 */
	@Override
	public StyledString getStyledDisplayString() {
		StyledString str= new StyledString(fLabel);

		String shortCutString= CorrectionCommandHandler.getShortCutString(getCommandId());
		if (shortCutString != null) {
			String decorated= Messages.format(CorrectionMessages.ChangeCorrectionProposal_name_with_shortcut,
					new String[] { fLabel, shortCutString });
			return StyledCellLabelProvider.styleDecoratedString(decorated, StyledString.QUALIFIER_STYLER, str);
		}
		return str;
	}

	/*
	 * @see ICompletionProposal#getImage()
	 */
	@Override
	public Image getImage() {
		return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CORRECTION_LINKED_RENAME);
	}

	/*
	 * @see ICompletionProposal#getContextInformation()
	 */
	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	/*
	 * @see ICCompletionProposal#getRelevance()
	 */
	@Override
	public int getRelevance() {
		return fRelevance;
	}

	/*
	 * @see ICCompletionProposal#getIdString()
	 */
	@Override
	public String getIdString() {
		return getCommandId();
	}

	/*
	 * @see ICommandAccess#getCommandId()
	 */
	@Override
	public String getCommandId() {
		return ICEditorActionDefinitionIds.RENAME_ELEMENT;
	}

	public void setRelevance(int relevance) {
		fRelevance= relevance;
	}
}
