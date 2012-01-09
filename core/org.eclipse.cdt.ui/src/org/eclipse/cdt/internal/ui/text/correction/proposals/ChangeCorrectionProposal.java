/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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

import com.ibm.icu.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import org.eclipse.jface.viewers.StyledString;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRewriteTarget;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension6;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.link.LinkedModeModel;

import org.eclipse.ui.IEditorPart;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.IUndoManager;
import org.eclipse.ltk.core.refactoring.NullChange;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICCompletionProposal;

import org.eclipse.cdt.internal.ui.text.correction.CorrectionCommandHandler;
import org.eclipse.cdt.internal.ui.text.correction.CorrectionMessages;
import org.eclipse.cdt.internal.ui.text.correction.ICommandAccess;
import org.eclipse.cdt.internal.ui.util.ExceptionHandler;
import org.eclipse.cdt.internal.ui.viewsupport.ColoringLabelProvider;

/**
 * Implementation of a C completion proposal to be used for quick fix and quick assist
 * proposals that invoke a {@link Change}. The proposal offers a proposal information but no context
 * information.
 * 
 * @since 5.1
 */
public class ChangeCorrectionProposal implements ICCompletionProposal, ICommandAccess, ICompletionProposalExtension6 {
	private Change fChange;
	private String fName;
	private int fRelevance;
	private Image fImage;
	private String fCommandId;

	/**
	 * Constructs a change correction proposal.
	 * 
	 * @param name The name that is displayed in the proposal selection dialog.
	 * @param change The change that is executed when the proposal is applied or <code>null</code>
	 * if the change will be created by implementors of {@link #createChange()}.
	 * @param relevance The relevance of this proposal.
	 * @param image The image that is displayed for this proposal or <code>null</code> if no
	 * image is desired.
	 */
	public ChangeCorrectionProposal(String name, Change change, int relevance, Image image) {
		if (name == null) {
			throw new IllegalArgumentException("Name must not be null"); //$NON-NLS-1$
		}
		fName= name;
		fChange= change;
		fRelevance= relevance;
		fImage= image;
		fCommandId= null;
	}

	/*
	 * @see ICompletionProposal#apply(IDocument)
	 */
	@Override
	public void apply(IDocument document) {
		try {
			performChange(CUIPlugin.getActivePage().getActiveEditor(), document);
		} catch (CoreException e) {
			ExceptionHandler.handle(e, CorrectionMessages.ChangeCorrectionProposal_error_title, CorrectionMessages.ChangeCorrectionProposal_error_message);
		}
	}

	/**
	 * Performs the change associated with this proposal.
	 * 
	 * @param activeEditor The editor currently active or <code>null</code> if no
	 * editor is active.
	 * @param document The document of the editor currently active or <code>null</code> if
	 * no editor is visible.
	 * @throws CoreException Thrown when the invocation of the change failed.
	 */
	protected void performChange(IEditorPart activeEditor, IDocument document) throws CoreException {
		Change change= null;
		IRewriteTarget rewriteTarget= null;
		try {
			change= getChange();
			if (change != null) {
				if (document != null) {
					LinkedModeModel.closeAllModels(document);
				}
				if (activeEditor != null) {
					rewriteTarget= (IRewriteTarget) activeEditor.getAdapter(IRewriteTarget.class);
					if (rewriteTarget != null) {
						rewriteTarget.beginCompoundChange();
					}
				}

				change.initializeValidationData(new NullProgressMonitor());
				RefactoringStatus valid= change.isValid(new NullProgressMonitor());
				if (valid.hasFatalError()) {
					IStatus status= new Status(IStatus.ERROR, CUIPlugin.getPluginId(), IStatus.ERROR,
						valid.getMessageMatchingSeverity(RefactoringStatus.FATAL), null);
					throw new CoreException(status);
				} else {
					IUndoManager manager= RefactoringCore.getUndoManager();
					manager.aboutToPerformChange(change);
					Change undoChange= change.perform(new NullProgressMonitor());
					manager.changePerformed(change, true);
					if (undoChange != null) {
						undoChange.initializeValidationData(new NullProgressMonitor());
						manager.addUndo(getName(), undoChange);
					}
				}
			}
		} finally {
			if (rewriteTarget != null) {
				rewriteTarget.endCompoundChange();
			}

			if (change != null) {
				change.dispose();
			}
		}
	}

	/*
	 * @see ICompletionProposal#getAdditionalProposalInfo()
	 */
	@Override
	public String getAdditionalProposalInfo() {
		StringBuffer buf= new StringBuffer();
		buf.append("<p>"); //$NON-NLS-1$
		try {
			Change change= getChange();
			if (change != null) {
				String name= change.getName();
				if (name.length() == 0) {
					return null;
				}
				buf.append(name);
			} else {
				return null;
			}
		} catch (CoreException e) {
			buf.append("Unexpected error when accessing this proposal:<p><pre>"); //$NON-NLS-1$
			buf.append(e.getLocalizedMessage());
			buf.append("</pre>"); //$NON-NLS-1$
		}
		buf.append("</p>"); //$NON-NLS-1$
		return buf.toString();
	}

	/*
	 * @see ICompletionProposal#getContextInformation()
	 */
	@Override
	public IContextInformation getContextInformation() {
		return null;
	}

	/*
	 * @see ICompletionProposal#getDisplayString()
	 */
	@Override
	public String getDisplayString() {
		String shortCutString= CorrectionCommandHandler.getShortCutString(getCommandId());
		if (shortCutString != null) {
			return MessageFormat.format(CorrectionMessages.ChangeCorrectionProposal_name_with_shortcut, new Object[] {getName(), shortCutString});
		}
		return getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.text.contentassist.ICompletionProposalExtension6#getStyledDisplayString()
	 */
	@Override
	public StyledString getStyledDisplayString() {
		StyledString str= new StyledString(getName());
		
		String shortCutString= CorrectionCommandHandler.getShortCutString(getCommandId());
		if (shortCutString != null) {
			String decorated= MessageFormat.format(CorrectionMessages.ChangeCorrectionProposal_name_with_shortcut, new Object[] {getName(), shortCutString});
			return ColoringLabelProvider.decorateStyledString(str, decorated, StyledString.QUALIFIER_STYLER); 
		}
		return str;
	}
	
	/** 
	 * Returns the name of the proposal.
	 * 
	 * @return return the name of the proposal
	 */
	public String getName() {
		return fName;
	}
	
	/*
	 * @see ICompletionProposal#getImage()
	 */
	@Override
	public Image getImage() {
		return fImage;
	}

	/*
	 * @see ICompletionProposal#getSelection(IDocument)
	 */
	@Override
	public Point getSelection(IDocument document) {
		return null;
	}

	/**
	 * Sets the proposal's image or <code>null</code> if no image is desired.
	 * 
	 * @param image the desired image.
	 */
	public void setImage(Image image) {
		fImage= image;
	}

	/**
	 * Returns the change that will be executed when the proposal is applied.
	 * 
	 * @return returns the change for this proposal.
	 * @throws CoreException thrown when the change could not be created
	 */
	public final Change getChange() throws CoreException {
		if (fChange == null) {
			fChange= createChange();
		}
		return fChange;
	}

	/**
	 * Creates the text change for this proposal.
	 * This method is only called once and only when no text change has been passed in
 	 * {@link #ChangeCorrectionProposal(String, Change, int, Image)}.
 	 * 
	 * @return returns the created change.
	 * @throws CoreException thrown if the creation of the change failed.
	 */
	protected Change createChange() throws CoreException {
		return new NullChange();
	}
	
	/**
	 * Sets the display name.
	 * 
	 * @param name the name to set
	 */
	public void setDisplayName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Name must not be null"); //$NON-NLS-1$
		}
		fName= name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.text.c.ICCompletionProposal#getRelevance()
	 */
	@Override
	public int getRelevance() {
		return fRelevance;
	}

	/**
	 * Sets the relevance.
	 * @param relevance the relevance to set
	 */
	public void setRelevance(int relevance) {
		fRelevance= relevance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.text.correction.IShortcutProposal#getProposalId()
	 */
	@Override
	public String getCommandId() {
		return fCommandId;
	}
	
	/**
	 * Set the proposal id to allow assigning a shortcut to the correction proposal.
	 * 
	 * @param commandId The proposal id for this proposal or <code>null</code> if no command
	 * should be assigned to this proposal.
	 */
	public void setCommandId(String commandId) {
		fCommandId= commandId;
	}

	@Override
	public String getIdString() {
		return fCommandId;
	}
}
