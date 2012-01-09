/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedModeUI.ExitFlags;
import org.eclipse.jface.text.link.LinkedModeUI.IExitPolicy;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.link.EditorLinkedModeUI;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICCompletionProposal;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.EditorHighlightingSynchronizer;
import org.eclipse.cdt.internal.ui.search.LinkedNamesFinder;
import org.eclipse.cdt.internal.ui.text.correction.CorrectionCommandHandler;
import org.eclipse.cdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.cdt.internal.ui.text.correction.ICommandAccess;
import org.eclipse.cdt.internal.ui.viewsupport.ColoringLabelProvider;

/**
 * A proposal.allowing user to edit in place all occurrences of a name.
 */
public class LinkedNamesAssistProposal implements ICCompletionProposal, ICompletionProposalExtension2,
		ICompletionProposalExtension6, ICommandAccess {

	/**
	 * An exit policy that skips Backspace and Delete at the beginning and at the end
	 * of a linked position, respectively.
	 * 
	 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=183925 .
	 */
	public static class DeleteBlockingExitPolicy implements IExitPolicy {
		private IDocument fDocument;

		public DeleteBlockingExitPolicy(IDocument document) {
			fDocument= document;
		}

		@Override
		public ExitFlags doExit(LinkedModeModel model, VerifyEvent event, int offset, int length) {
			if (length == 0 && (event.character == SWT.BS || event.character == SWT.DEL)) {
				LinkedPosition position= model.findPosition(new LinkedPosition(fDocument, offset, 0, LinkedPositionGroup.NO_STOP));
				if (position != null) {
					if (event.character == SWT.BS) {
						if (offset - 1 < position.getOffset()) {
							//skip backspace at beginning of linked position
							event.doit= false;
						}
					} else /* event.character == SWT.DEL */ {
						if (offset + 1 > position.getOffset() + position.getLength()) {
							//skip delete at end of linked position
							event.doit= false;
						}
					}
				}
			}

			return null; // don't change behavior
		}
	}


	public static final String ASSIST_ID= "org.eclipse.cdt.ui.correction.renameInFile.assist"; //$NON-NLS-1$

	private ITranslationUnit fTranslationUnit;
	private String fLabel;
	private String fValueSuggestion;
	private int fRelevance;
	private IRegion[] fLocations;

	public LinkedNamesAssistProposal(ITranslationUnit tu) {
		this(CorrectionMessages.LinkedNamesAssistProposal_description, tu, null);
		fTranslationUnit= tu;
		fRelevance= 8;
	}

	public LinkedNamesAssistProposal(String label, ITranslationUnit tu, String valueSuggestion) {
		fLabel= label;
		fTranslationUnit= tu;
		fValueSuggestion= valueSuggestion;
		fRelevance= 8;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#apply(org.eclipse.jface.text.ITextViewer, char, int, int)
	 */
	@Override
	public void apply(final ITextViewer viewer, char trigger, int stateMask, final int offset) {
		try {
			fLocations = null;
			Point selection= viewer.getSelectedRange();
			final int secectionOffset = selection.x;
			final int selectionLength = selection.y;

			ASTProvider.getASTProvider().runOnAST(fTranslationUnit, ASTProvider.WAIT_ACTIVE_ONLY,
					new NullProgressMonitor(), new ASTRunnable() {

				@Override
				public IStatus runOnAST(ILanguage lang, IASTTranslationUnit astRoot) throws CoreException {
					if (astRoot == null)
						return Status.CANCEL_STATUS;
					
					IASTNodeSelector selector= astRoot.getNodeSelector(null);
					IASTName name= selector.findEnclosingName(secectionOffset, selectionLength);
					if (name != null) {
						fLocations = LinkedNamesFinder.findByName(astRoot, name);
					}
					return Status.OK_STATUS;
				}
			});

			if (fLocations == null || fLocations.length == 0) {
				return;
			}

			// Sort the locations starting with the one @ offset.
			Arrays.sort(fLocations, new Comparator<IRegion>() {

				@Override
				public int compare(IRegion n1, IRegion n2) {
					return rank(n1) - rank(n2);
				}

				/**
				 * Returns the absolute rank of a location. Location preceding <code>offset</code>
				 * are ranked last.
				 *
				 * @param location the location to compute the rank for
				 * @return the rank of the location with respect to the invocation offset
				 */
				private int rank(IRegion location) {
					int relativeRank= location.getOffset() + location.getLength() - offset;
					if (relativeRank < 0)
						return Integer.MAX_VALUE + relativeRank;
					else
						return relativeRank;
				}
			});
			
			IDocument document= viewer.getDocument();
			LinkedPositionGroup group= new LinkedPositionGroup();
			for (int i= 0; i < fLocations.length; i++) {
				IRegion item= fLocations[i];
				group.addPosition(new LinkedPosition(document, item.getOffset(), item.getLength(), i));
			}

			LinkedModeModel model= new LinkedModeModel();
			model.addGroup(group);
			model.forceInstall();
			CEditor editor= getCEditor();
			if (editor != null) {
				model.addLinkingListener(new EditorHighlightingSynchronizer(editor));
			}

			LinkedModeUI ui= new EditorLinkedModeUI(model, viewer);
			ui.setExitPolicy(new DeleteBlockingExitPolicy(document));
			ui.setExitPosition(viewer, offset, 0, LinkedPositionGroup.NO_STOP);
			ui.enter();

			if (fValueSuggestion != null) {
				document.replace(fLocations[0].getOffset(), fLocations[0].getLength(), fValueSuggestion);
				IRegion selectedRegion= ui.getSelectedRegion();
				selection= new Point(selectedRegion.getOffset(), fValueSuggestion.length());
			}

			viewer.setSelectedRange(selection.x, selection.y); // By default full word is selected, restore original selection
		} catch (BadLocationException e) {
			CUIPlugin.log(e);
		}
	}

	/**
	 * Returns the currently active C editor, or <code>null</code> if it
	 * cannot be determined.
	 *
	 * @return  the currently active C editor, or <code>null</code>
	 */
	private CEditor getCEditor() {
		IEditorPart part= CUIPlugin.getActivePage().getActiveEditor();
		if (part instanceof CEditor)
			return (CEditor) part;
		else
			return null;
	}

	/*
	 * @see ICompletionProposal#apply(IDocument)
	 */
	@Override
	public void apply(IDocument document) {
		// can't do anything
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
		return CorrectionMessages.LinkedNamesAssistProposal_proposalinfo;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	@Override
	public String getDisplayString() {
		String shortCutString= CorrectionCommandHandler.getShortCutString(getCommandId());
		if (shortCutString != null) {
			return NLS.bind(CorrectionMessages.ChangeCorrectionProposal_name_with_shortcut,
					fLabel, shortCutString);
		}
		return fLabel;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension6#getStyledDisplayString()
	 */
	@Override
	public StyledString getStyledDisplayString() {
		StyledString str= new StyledString(fLabel);
		
		String shortCutString= CorrectionCommandHandler.getShortCutString(getCommandId());
		if (shortCutString != null) {
			String decorated= NLS.bind(CorrectionMessages.ChangeCorrectionProposal_name_with_shortcut,
					fLabel, shortCutString);
			return ColoringLabelProvider.decorateStyledString(str, decorated, StyledString.QUALIFIER_STYLER); 
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#selected(org.eclipse.jface.text.ITextViewer, boolean)
	 */
	@Override
	public void selected(ITextViewer textViewer, boolean smartToggle) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#unselected(org.eclipse.jface.text.ITextViewer)
	 */
	@Override
	public void unselected(ITextViewer textViewer) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension2#validate(org.eclipse.jface.text.IDocument, int, org.eclipse.jface.text.DocumentEvent)
	 */
	@Override
	public boolean validate(IDocument document, int offset, DocumentEvent event) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.text.correction.ICommandAccess#getCommandId()
	 */
	@Override
	public String getCommandId() {
		return ASSIST_ID;
	}

	public void setRelevance(int relevance) {
		fRelevance= relevance;
	}

	@Override
	public String getIdString() {
		return ASSIST_ID;
	}
}
