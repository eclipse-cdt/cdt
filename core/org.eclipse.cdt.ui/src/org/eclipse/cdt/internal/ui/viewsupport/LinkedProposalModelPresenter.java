/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.viewsupport;

import java.util.Iterator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.ILinkedModeListener;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.text.link.ProposalPosition;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICCompletionProposal;

import org.eclipse.cdt.internal.corext.fix.LinkedProposalModel;
import org.eclipse.cdt.internal.corext.fix.LinkedProposalPositionGroup;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.EditorHighlightingSynchronizer;

/**
 * Does the setup of the linked mode from a {@link LinkedProposalModel}
 */
public class LinkedProposalModelPresenter {
	
	public LinkedProposalModelPresenter() {
	}
	
	public void enterLinkedMode(ITextViewer viewer, IEditorPart editor, LinkedProposalModel linkedProposalModel) throws BadLocationException {
		IDocument document= viewer.getDocument();

		LinkedModeModel model= new LinkedModeModel();
		boolean added= false;

		Iterator<LinkedProposalPositionGroup> iterator= linkedProposalModel.getPositionGroupIterator();
		while (iterator.hasNext()) {
			LinkedProposalPositionGroup curr= iterator.next();

			LinkedPositionGroup group= new LinkedPositionGroup();

			LinkedProposalPositionGroup.PositionInformation[] positions= curr.getPositions();
			if (positions.length > 0) {
				LinkedProposalPositionGroup.Proposal[] linkedModeProposals= curr.getProposals();
				if (linkedModeProposals.length <= 1) {
					for (int i= 0; i < positions.length; i++) {
						LinkedProposalPositionGroup.PositionInformation pos= positions[i];
						if (pos.getOffset() != -1) {
							group.addPosition(new LinkedPosition(document, pos.getOffset(), pos.getLength(), pos.getSequenceRank()));
						}
					}
				} else {
					LinkedPositionProposalImpl[] proposalImpls= new LinkedPositionProposalImpl[linkedModeProposals.length];
					for (int i= 0; i < linkedModeProposals.length; i++) {
						proposalImpls[i]= new LinkedPositionProposalImpl(linkedModeProposals[i], model);
					}

					for (int i= 0; i < positions.length; i++) {
						LinkedProposalPositionGroup.PositionInformation pos= positions[i];
						if (pos.getOffset() != -1) {
							group.addPosition(new ProposalPosition(document, pos.getOffset(), pos.getLength(), pos.getSequenceRank(), proposalImpls));
						}
					}
				}
				model.addGroup(group);
				added= true;
			}
		}

		model.forceInstall();

		if (editor instanceof CEditor) {
			model.addLinkingListener(new EditorHighlightingSynchronizer((CEditor) editor));
		}

		if (added) { // only set up UI if there are any positions set
			LinkedModeUI ui= new EditorLinkedModeUI(model, viewer);
			LinkedProposalPositionGroup.PositionInformation endPosition= linkedProposalModel.getEndPosition();
			if (endPosition != null && endPosition.getOffset() != -1) {
				ui.setExitPosition(viewer, endPosition.getOffset() + endPosition.getLength(), 0, Integer.MAX_VALUE);
			} else {
				int cursorPosition= viewer.getSelectedRange().x;
				if (cursorPosition != 0) {
					ui.setExitPosition(viewer, cursorPosition, 0, Integer.MAX_VALUE);
				}
			}
			ui.setExitPolicy(new LinkedModeExitPolicy());
			ui.enter();

			IRegion region= ui.getSelectedRegion();
			viewer.setSelectedRange(region.getOffset(), region.getLength());
			viewer.revealRange(region.getOffset(), region.getLength());
		}
	}

	private static class LinkedPositionProposalImpl implements ICompletionProposalExtension2, ICCompletionProposal {
		private final LinkedProposalPositionGroup.Proposal fProposal;
		private final LinkedModeModel fLinkedPositionModel;

		public LinkedPositionProposalImpl(LinkedProposalPositionGroup.Proposal proposal, LinkedModeModel model) {
			fProposal= proposal;
			fLinkedPositionModel= model;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#apply(org.eclipse.jface.text.ITextViewer, char, int, int)
		 */
		@Override
		public void apply(ITextViewer viewer, char trigger, int stateMask, int offset) {
			IDocument doc= viewer.getDocument();
			LinkedPosition position= fLinkedPositionModel.findPosition(new LinkedPosition(doc, offset, 0));
			if (position != null) {
				try {
					try {
						TextEdit edit= fProposal.computeEdits(offset, position, trigger, stateMask, fLinkedPositionModel);
						if (edit != null) {
							edit.apply(position.getDocument(), 0);
						}
					} catch (MalformedTreeException e) {
						throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, IStatus.ERROR, "Unexpected exception applying edit", e)); //$NON-NLS-1$
					} catch (BadLocationException e) {
						throw new CoreException(new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, IStatus.ERROR, "Unexpected exception applying edit", e)); //$NON-NLS-1$
					}
				} catch (CoreException e) {
					CUIPlugin.log(e);
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
		 */
		@Override
		public String getDisplayString() {
			return fProposal.getDisplayString();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
		 */
		@Override
		public Image getImage() {
			return fProposal.getImage();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.cdt.ui.text.ICCompletionProposal#getRelevance()
		 */
		@Override
		public int getRelevance() {
			return fProposal.getRelevance();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
		 */
		@Override
		public void apply(IDocument document) {
			// not called
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
		 */
		@Override
		public String getAdditionalProposalInfo() {
			return fProposal.getAdditionalProposalInfo();
		}

		@Override
		public Point getSelection(IDocument document) { return null; }
		@Override
		public IContextInformation getContextInformation() { return null; }
		@Override
		public void selected(ITextViewer viewer, boolean smartToggle) {}
		@Override
		public void unselected(ITextViewer viewer) {}

		/*
		 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#validate(org.eclipse.jface.text.IDocument, int, org.eclipse.jface.text.DocumentEvent)
		 */
		@Override
		public boolean validate(IDocument document, int offset, DocumentEvent event) {
			// ignore event
			String insert= getDisplayString();

			int off;
			LinkedPosition pos= fLinkedPositionModel.findPosition(new LinkedPosition(document, offset, 0));
			if (pos != null) {
				off= pos.getOffset();
			} else {
				off= Math.max(0, offset - insert.length());
			}
			int length= offset - off;

			if (offset <= document.getLength()) {
				try {
					String content= document.get(off, length);
					if (insert.startsWith(content))
						return true;
				} catch (BadLocationException e) {
					CUIPlugin.log(e);
					// and ignore and return false
				}
			}
			return false;
		}

		@Override
		public String getIdString() {
			return getDisplayString();
		}
	}
	
	private static class LinkedModeExitPolicy implements LinkedModeUI.IExitPolicy {
		@Override
		public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {
			if (event.character  == '=') {
				return new ExitFlags(ILinkedModeListener.EXIT_ALL, true);
			}
			return null;
		}
	}
}
