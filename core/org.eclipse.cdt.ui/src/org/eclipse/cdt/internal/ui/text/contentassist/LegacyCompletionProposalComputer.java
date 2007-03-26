/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Anton Leherbauer (Wind River Systems) - initial API and implementation
 *     Bryan Wilkinson (QNX)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.ITextViewer;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTCompletionNode;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.ui.CUIPlugin;

/**
 * A proposal computer for handling the legacy extensions from the
 * <tt>org.eclipse.cdt.core.completionContributors</tt> extension point.
 * 
 * @since 4.0
 */
public class LegacyCompletionProposalComputer extends ParsingBasedProposalComputer {

	/**
	 * Default constructor is required (executable extension).
	 */
	public LegacyCompletionProposalComputer() {
	}

	/**
	 * @deprecated this is for backwards compatibility, only.
	 */
	protected List computeCompletionProposals(
			CContentAssistInvocationContext context,
			IASTCompletionNode completionNode, String prefix) throws CoreException {
		
		if (!(completionNode instanceof ASTCompletionNode)) {
			// unsupported IASTCompletionNode implementation
			return Collections.EMPTY_LIST;
		}
		if (context.isContextInformationStyle()) {
			// context information cannot be supported by completionContributors
			return Collections.EMPTY_LIST;
		}
		ITextViewer viewer = context.getViewer();
		int offset = context.getInvocationOffset();
		IWorkingCopy workingCopy = context.getTranslationUnit().getWorkingCopy();
		
		List proposals = new ArrayList();
		
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CUIPlugin.PLUGIN_ID, "completionContributors"); //$NON-NLS-1$
		if (point == null)
			return null;
		IExtension[] extensions = point.getExtensions();
		for (int i = 0; i < extensions.length; ++i) {
			IConfigurationElement[] elements = extensions[i].getConfigurationElements();
			for (int j = 0; j < elements.length; ++j) {
				IConfigurationElement element = elements[j];
				if (!"contributor".equals(element.getName())) //$NON-NLS-1$
					continue;
				Object contribObject = element.createExecutableExtension("class"); //$NON-NLS-1$
				if (!(contribObject instanceof org.eclipse.cdt.ui.text.contentassist.ICompletionContributor))
					continue;
				org.eclipse.cdt.ui.text.contentassist.ICompletionContributor contributor = (org.eclipse.cdt.ui.text.contentassist.ICompletionContributor)contribObject;
				contributor.contributeCompletionProposals(viewer, offset,
						workingCopy, (ASTCompletionNode) completionNode,
						prefix, proposals);
			}
		}
		
		return proposals;
	}
}
