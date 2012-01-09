/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.doctools;

import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IWorkingCopyManager;
import org.eclipse.cdt.ui.text.contentassist.ContentAssistInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration;


abstract class AbstractDocCommentProposalComputer implements ICompletionProposalComputer {
	
	protected abstract IDocCommentViewerConfiguration getConfiguration(IDocCommentOwner owner);
	
	protected final IDocCommentViewerConfiguration getConfiguration() {
		IResource resource= getResource();
		IDocCommentOwner owner= DocCommentOwnerManager.getInstance().getCommentOwner(resource);
		return getConfiguration(owner);
	}
	
	@Override
	public List<ICompletionProposal> computeCompletionProposals(ContentAssistInvocationContext context, IProgressMonitor monitor) {
		return getConfiguration().createProposalComputer().computeCompletionProposals(context, monitor);
	}

	@Override
	public List<IContextInformation> computeContextInformation(
			ContentAssistInvocationContext context, IProgressMonitor monitor) {
		return getConfiguration().createProposalComputer().computeContextInformation(context, monitor);
	}

	@Override
	public String getErrorMessage() {
		return getConfiguration().createProposalComputer().getErrorMessage();
	}

	@Override
	public void sessionEnded() {
		// XXX
	}

	@Override
	public void sessionStarted() {
		// XXX
	}
	
	
	private static IResource getResource() {
		ITranslationUnit tu= getTranslationUnit();
		return tu.getResource();
	}
	
	private static ITranslationUnit getTranslationUnit() {
		IWorkbenchWindow window= PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null)
			return null;

		IWorkbenchPage page= window.getActivePage();
		if (page == null)
			return null;

		IEditorPart editor= page.getActiveEditor();
		if (editor == null)
			return null;

		IWorkingCopyManager manager= CUIPlugin.getDefault().getWorkingCopyManager();
		ITranslationUnit unit= manager.getWorkingCopy(editor.getEditorInput());
		if (unit == null)
			return null;

		return unit;
	}
}
