/**********************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.internal.ui.CHelpProviderManager;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.IFunctionSummary;
import org.eclipse.cdt.ui.text.ICHelpInvocationContext;
import org.eclipse.cdt.ui.text.contentassist.ICompletionContributor;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ContextInformation;
import org.eclipse.swt.graphics.Image;

public class HelpCompletionContributor implements ICompletionContributor {

	public void contributeCompletionProposals(ITextViewer viewer, int offset,
			IWorkingCopy workingCopy, ASTCompletionNode completionNode,
			List proposals)
	{
		final IWorkingCopy fWorkingCopy = workingCopy;
		if (completionNode != null) {
			// Find matching functions
			ICHelpInvocationContext context = new ICHelpInvocationContext() {

				public IProject getProject() {
					return fWorkingCopy.getCProject().getProject();
				}

				public ITranslationUnit getTranslationUnit() {
					return fWorkingCopy.getTranslationUnit();
				}	
			};
			
			IASTName[] names = completionNode.getNames();
			for (int i = 0; i < names.length; ++i) {
				IASTName name = names[i];
				
				if (name.getTranslationUnit() == null)
					// Not connected
					continue;
			
				// ignore if this is a member access
				if (name.getParent() instanceof IASTFieldReference)
					continue;

				String prefix = new String(name.toCharArray());
				
				IFunctionSummary[] summaries = CHelpProviderManager.getDefault().getMatchingFunctions(context, prefix);
				if (summaries == null )
					continue;
				
				int repOffset = offset - prefix.length();
				int repLength = prefix.length();
				Image image = CUIPlugin.getImageDescriptorRegistry().get(CElementImageProvider.getFunctionImageDescriptor());

				for (int j = 0; j < summaries.length; j++) {
					IFunctionSummary summary = summaries[j];
					String fname = summary.getName() + "()"; //$NON-NLS-1$
					String fdesc = summary.getDescription();
					IFunctionSummary.IFunctionPrototypeSummary fproto = summary.getPrototype();
					String fargs = fproto.getArguments();
					
					CCompletionProposal proposal;
					proposal = new CCompletionProposal(fname, 
													   repOffset, 
													   repLength,
													   image, 
													   fproto.getPrototypeString(true),
													   2,
													   viewer);

					if (fdesc != null) {
						proposal.setAdditionalProposalInfo(fdesc);
					}
					
					if (fargs != null && fargs.length() > 0) {
						proposal.setContextInformation(new ContextInformation(fname, fargs));
						// set the cursor before the closing bracket
						proposal.setCursorPosition(fname.length() - 1);
					}

					proposals.add(proposal);
				}

			}
		}
	}

}
