/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 * 	   Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension;
import org.eclipse.jface.text.contentassist.ICompletionProposalExtension2;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.keys.IBindingService;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.IInvocationContext;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;

import org.eclipse.cdt.internal.ui.actions.ActionUtil;
import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.correction.proposals.LinkedNamesAssistProposal;

/**
 * Handler to be used to run a quick fix or assist by keyboard shortcut
 */
public class CorrectionCommandHandler extends AbstractHandler {
	private final CEditor fEditor;
	private final String fId;
	private final boolean fIsAssist;

	public CorrectionCommandHandler(CEditor editor, String id, boolean isAssist) {
		fEditor= editor;
		fId= id;
		fIsAssist= isAssist;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		doExecute();
		return null;
	}

	/**
	 * Try to execute the correction command.
	 * 
	 * @return <code>true</code> iff the correction could be started
	 * @since 5.3
	 */
	public boolean doExecute() {
		ISelection selection= fEditor.getSelectionProvider().getSelection();
		ITranslationUnit tu= CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fEditor.getEditorInput());
		IAnnotationModel model= CUIPlugin.getDefault().getDocumentProvider().getAnnotationModel(fEditor.getEditorInput());
		if (selection instanceof ITextSelection && tu != null && model != null) {
			if (!ActionUtil.isEditable(fEditor)) {
				return false;
			}
			ICompletionProposal proposal= findCorrection(fId, fIsAssist, (ITextSelection) selection, tu, model);
			if (proposal != null) {
				invokeProposal(proposal, ((ITextSelection) selection).getOffset());
			}
		}
		return false;
	}
	
	private ICompletionProposal findCorrection(String id, boolean isAssist, ITextSelection selection, ITranslationUnit tu, IAnnotationModel model) {
		CorrectionContext context= new CorrectionContext(tu, selection.getOffset(), selection.getLength());
		Collection<ICCompletionProposal> proposals= new ArrayList<ICCompletionProposal>(10);
		if (isAssist) {
			if (id.equals(LinkedNamesAssistProposal.ASSIST_ID)) {
				return getLocalRenameProposal(context); // shortcut for local rename
			}
			CCorrectionProcessor.collectAssists(context, new ProblemLocation[0], proposals);
		} else {
			try {
				boolean goToClosest= selection.getLength() == 0; 
				Annotation[] annotations= getAnnotations(selection.getOffset(), goToClosest);
				CCorrectionProcessor.collectProposals(context, model, annotations, true, false, proposals);
			} catch (BadLocationException e) {
				return null;
			}
		}
		for (Iterator<ICCompletionProposal> iter= proposals.iterator(); iter.hasNext();) {
			Object curr= iter.next();
			if (curr instanceof ICommandAccess) {
				if (id.equals(((ICommandAccess) curr).getCommandId())) {
					return (ICompletionProposal) curr;
				}
			}
		}
		return null;
	}

	private Annotation[] getAnnotations(int offset, boolean goToClosest) throws BadLocationException {
		ArrayList<Annotation> resultingAnnotations= new ArrayList<Annotation>();
		CCorrectionAssistant.collectQuickFixableAnnotations(fEditor, offset, goToClosest, resultingAnnotations);
		return resultingAnnotations.toArray(new Annotation[resultingAnnotations.size()]);
	}
	
	private ICompletionProposal getLocalRenameProposal(final IInvocationContext context) {
		final ICCompletionProposal[] proposals= new ICCompletionProposal[1];

		ASTProvider.getASTProvider().runOnAST(context.getTranslationUnit(), ASTProvider.WAIT_ACTIVE_ONLY,
				new NullProgressMonitor(), new ASTRunnable() {

			@Override
			public IStatus runOnAST(ILanguage lang, IASTTranslationUnit astRoot) throws CoreException {
				IASTNodeSelector selector= astRoot.getNodeSelector(null);
				IASTName name= selector.findEnclosingName(context.getSelectionOffset(), context.getSelectionLength());

				// Activate the proposal only if a simple name is selected.
				if (name != null && name == name.getLastName()) {
					IBinding binding= name.resolveBinding();
					if (binding != null) {
						proposals[0]= new LinkedNamesAssistProposal(context.getTranslationUnit());
					}
				}
				return Status.OK_STATUS;
			}
		});
		return proposals[0];
	}

	private IDocument getDocument() {
		return CUIPlugin.getDefault().getDocumentProvider().getDocument(fEditor.getEditorInput());
	}
	
	private void invokeProposal(ICompletionProposal proposal, int offset) {
		if (proposal instanceof ICompletionProposalExtension2) {
			ITextViewer viewer= fEditor.getViewer();
			if (viewer != null) {
				((ICompletionProposalExtension2) proposal).apply(viewer, (char) 0, 0, offset);
				return;
			}
		} else if (proposal instanceof ICompletionProposalExtension) {
			IDocument document= getDocument();
			if (document != null) {
				((ICompletionProposalExtension) proposal).apply(document, (char) 0, offset);
				return;
			}
		}
		IDocument document= getDocument();
		if (document != null) {
			proposal.apply(document);
		}
	}
	
	public static String getShortCutString(String proposalId) {
		if (proposalId != null) {
			IBindingService bindingService= (IBindingService) PlatformUI.getWorkbench().getAdapter(IBindingService.class);
			if (bindingService != null) {
				TriggerSequence[] activeBindingsFor= bindingService.getActiveBindingsFor(proposalId);
				if (activeBindingsFor.length > 0) {
					return activeBindingsFor[0].format();
				}
			}
		}
		return null;
	}
}
