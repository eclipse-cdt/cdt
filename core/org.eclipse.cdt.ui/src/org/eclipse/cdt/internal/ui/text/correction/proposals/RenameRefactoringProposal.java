/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.correction.proposals;

import org.eclipse.cdt.internal.corext.util.Messages;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.ICEditorActionDefinitionIds;
import org.eclipse.cdt.internal.ui.text.correction.CorrectionCommandHandler;
import org.eclipse.cdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.cdt.internal.ui.text.correction.ICommandAccess;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.refactoring.actions.CRenameAction;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.viewers.StyledCellLabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * A quick assist proposal that starts the Rename refactoring.
 */
public class RenameRefactoringProposal implements ICCompletionProposal, ICompletionProposalExtension6, ICommandAccess {
	private final CEditor fEditor;
	private final String fLabel;
	private int fRelevance;

	public RenameRefactoringProposal(CEditor editor) {
		fEditor = editor;
		fLabel = CorrectionMessages.RenameRefactoringProposal_name;
		fRelevance = 8;
	}

	@Override
	public void apply(IDocument document) {
		CRenameAction action = new CRenameAction();
		action.setEditor(fEditor);
		action.run();
	}

	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	@Override
	public String getAdditionalProposalInfo() {
		return CorrectionMessages.RenameRefactoringProposal_additionalInfo;
	}

	@Override
	public String getDisplayString() {
		String shortCutString = CorrectionCommandHandler.getShortCutString(getCommandId());
		if (shortCutString != null) {
			return Messages.format(CorrectionMessages.ChangeCorrectionProposal_name_with_shortcut,
					new String[] { fLabel, shortCutString });
		}
		return fLabel;
	}

	@Override
	public StyledString getStyledDisplayString() {
		StyledString str = new StyledString(fLabel);

		String shortCutString = CorrectionCommandHandler.getShortCutString(getCommandId());
		if (shortCutString != null) {
			String decorated = Messages.format(CorrectionMessages.ChangeCorrectionProposal_name_with_shortcut,
					new String[] { fLabel, shortCutString });
			return StyledCellLabelProvider.styleDecoratedString(decorated, StyledString.QUALIFIER_STYLER, str);
		}
		return str;
	}

	@Override
	public Image getImage() {
		return CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_CORRECTION_LINKED_RENAME);
	}

	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	@Override
	public int getRelevance() {
		return fRelevance;
	}

	@Override
	public String getIdString() {
		return getCommandId();
	}

	@Override
	public String getCommandId() {
		return ICEditorActionDefinitionIds.RENAME_ELEMENT;
	}

	public void setRelevance(int relevance) {
		fRelevance = relevance;
	}
}
