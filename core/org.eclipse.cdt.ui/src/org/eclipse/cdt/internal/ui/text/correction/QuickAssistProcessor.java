/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sebastian Davids <sdavids@gmx.de> - Bug 37432 getInvertEqualsProposal
 *     Benjamin Muskalla <b.muskalla@gmx.net> - Bug 36350 convertToStringBufferPropsal
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IEditorPart;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeSelector;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.cdt.ui.text.IInvocationContext;
import org.eclipse.cdt.ui.text.IProblemLocation;
import org.eclipse.cdt.ui.text.IQuickAssistProcessor;

import org.eclipse.cdt.internal.core.model.ASTCache.ASTRunnable;

import org.eclipse.cdt.internal.ui.editor.ASTProvider;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.text.correction.proposals.LinkedNamesAssistProposal;
import org.eclipse.cdt.internal.ui.text.correction.proposals.RenameRefactoringProposal;

/**
 * see org.eclipse.cdt.ui.text.IQuickAssistProcessor
 */
public class QuickAssistProcessor implements IQuickAssistProcessor {

	public QuickAssistProcessor() {
		super();
	}

	@Override
	public boolean hasAssists(final IInvocationContext context) throws CoreException {
		IStatus status = ASTProvider.getASTProvider().runOnAST(context.getTranslationUnit(),
				ASTProvider.WAIT_ACTIVE_ONLY, new NullProgressMonitor(), new ASTRunnable() {

			@Override
			public IStatus runOnAST(ILanguage lang, IASTTranslationUnit astRoot) throws CoreException {
				IASTNodeSelector selector= astRoot.getNodeSelector(null);
				IASTName name= selector.findEnclosingName(context.getSelectionOffset(), context.getSelectionLength());

				// Activate the proposal only if a simple name is selected.
				if (name != null && name == name.getLastName()) {
					IBinding binding= name.resolveBinding();
					if (binding != null) {
						return Status.OK_STATUS;
					}
				}
				return Status.CANCEL_STATUS;
			}
		});
		return status.isOK();
	}

	@Override
	public ICCompletionProposal[] getAssists(final IInvocationContext context,
			final IProblemLocation[] problemLocations) throws CoreException {
		final ArrayList<ICCompletionProposal> proposals= new ArrayList<ICCompletionProposal>();

		ASTProvider.getASTProvider().runOnAST(context.getTranslationUnit(), ASTProvider.WAIT_ACTIVE_ONLY,
				new NullProgressMonitor(),
				new ASTRunnable() {

			@Override
			public IStatus runOnAST(ILanguage lang, IASTTranslationUnit astRoot) throws CoreException {
				IASTNodeSelector selector= astRoot.getNodeSelector(null);
				IASTName name= selector.findEnclosingName(context.getSelectionOffset(), context.getSelectionLength());

				// Activate the proposal only if a simple name is selected.
				if (name != null && name == name.getLastName()) {
					IBinding binding= name.resolveBinding();
					if (binding != null) {
						boolean noErrorsAtLocation= noErrorsAtLocation(problemLocations);
						
						// Quick assists that show up also if there is an error/warning
						getRenameLocalProposals(context, problemLocations, noErrorsAtLocation, proposals);
						getRenameRefactoringProposal(context, problemLocations, noErrorsAtLocation, proposals);
					}
				}
				return Status.OK_STATUS;
			}
		});
		
		return proposals.isEmpty() ? null : proposals.toArray(new ICCompletionProposal[proposals.size()]);
	}

	private boolean noErrorsAtLocation(IProblemLocation[] locations) {
		if (locations != null) {
			for (int i= 0; i < locations.length; i++) {
				if (locations[i].isError()) {
					return false;
				}
			}
		}
		return true;
	}
	
	private static void getRenameLocalProposals(IInvocationContext context, IProblemLocation[] locations,
			boolean noErrorsAtLocation, Collection<ICCompletionProposal> proposals) {
		LinkedNamesAssistProposal proposal= new LinkedNamesAssistProposal(context.getTranslationUnit());
		if (!noErrorsAtLocation) {
			proposal.setRelevance(1);
		}
		
		proposals.add(proposal);
	}

	private static boolean getRenameRefactoringProposal(IInvocationContext context, IProblemLocation[] locations,
			boolean noErrorsAtLocation, Collection<ICCompletionProposal> proposals)
			throws CoreException {
		IEditorPart editor= CUIPlugin.getActivePage().getActiveEditor();
		if (!(editor instanceof CEditor))
			return false;

		if (proposals == null) {
			return true;
		}
		RenameRefactoringProposal proposal= new RenameRefactoringProposal((CEditor) editor);
		if (!noErrorsAtLocation) {
			proposal.setRelevance(1);
		}

		proposals.add(proposal);
		return true;
	}
}
