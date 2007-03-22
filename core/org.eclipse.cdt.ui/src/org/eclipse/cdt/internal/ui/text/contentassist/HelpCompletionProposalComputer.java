/*******************************************************************************
 * Copyright (c) 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bryan Wilkinson (QNX) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.graphics.Image;

import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;

import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;

public class HelpCompletionProposalComputer extends ParsingBasedProposalComputer {

	protected List computeCompletionProposals(
			CContentAssistInvocationContext cContext,
			IASTCompletionNode completionNode, String prefix)
			throws CoreException {
		
		boolean handleHelp = false;
		if (completionNode != null) {
			IASTName[] names = completionNode.getNames();
			for (int i = 0; i < names.length; ++i) {
				IASTName name = names[i];
				
				// ignore if not connected
				if (name.getTranslationUnit() == null)
					continue;
			
				// ignore if this is a member access
				if (name.getParent() instanceof IASTFieldReference)
					continue;
				
				handleHelp = true;
				break;
			}
		}
		
		if (!handleHelp) {
			return Collections.EMPTY_LIST;
		}
		
		final ITranslationUnit tu = cContext.getTranslationUnit();
		// Find matching functions
		ICHelpInvocationContext helpContext = new ICHelpInvocationContext() {

			public IProject getProject() {
				return tu.getCProject().getProject();
			}

			public ITranslationUnit getTranslationUnit() {
				return tu;
			}
		};

		IFunctionSummary[] summaries = CHelpProviderManager.getDefault()
				.getMatchingFunctions(helpContext, prefix);
		if (summaries == null)
			return Collections.EMPTY_LIST;

		int repOffset = cContext.getInvocationOffset() - prefix.length();
		int repLength = prefix.length();
		Image image = CUIPlugin.getImageDescriptorRegistry().get(
				CElementImageProvider.getFunctionImageDescriptor());

		List proposals = new ArrayList();

		for (int j = 0; j < summaries.length; j++) {
			IFunctionSummary summary = summaries[j];
			String fname = summary.getName() + "()"; //$NON-NLS-1$
			String fdesc = summary.getDescription();
			IFunctionSummary.IFunctionPrototypeSummary fproto = summary
					.getPrototype();
			String fargs = fproto.getArguments();

			CCompletionProposal proposal;
			proposal = new CCompletionProposal(
					fname,
					repOffset,
					repLength,
					image,
					fproto.getPrototypeString(true),
					2,
					cContext.getViewer());

			if (fdesc != null) {
				proposal.setAdditionalProposalInfo(fdesc);
			}

			if (!cContext.isContextInformationStyle()) {
				// set the cursor before the closing bracket
				proposal.setCursorPosition(fname.length() - 1);
			}
			
			if (fargs != null && fargs.length() > 0) {
				CProposalContextInformation info = new CProposalContextInformation(image, fname, fargs);
				info.setContextInformationPosition(cContext.getContextInformationOffset());
				proposal.setContextInformation(info);

			}

			proposals.add(proposal);
		}

		return proposals;
	}
}
