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

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.contentassist.ICompletionContributor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Image;

public class SearchCompletionContributor implements ICompletionContributor {

	public void contributeCompletionProposals(ITextViewer viewer, int offset,
			IWorkingCopy workingCopy, ASTCompletionNode completionNode,
			List proposals)
	{
		// and only for C source files
        if (workingCopy.isHeaderUnit() || !workingCopy.isCLanguage()) {
        	return;
        }
		if (completionNode != null) {
			IASTName[] names = completionNode.getNames();
			for (int i = 0; i < names.length; i++) {
				IASTName name = names[i];
				
				// not hooked up, ignore
				if (name.getTranslationUnit() == null)
					continue;
				
				// ignore if this is a member access
				if (name.getParent() instanceof IASTFieldReference)
					continue;
				
				// Create search engine
				SearchEngine searchEngine = new SearchEngine();
				searchEngine.setWaitingPolicy( ICSearchConstants.FORCE_IMMEDIATE_SEARCH );
				
				// Create search scope
				ICElement[] projects = new ICElement[] { workingCopy.getCProject() };
				ICSearchScope scope = SearchEngine.createCSearchScope(projects, true);
				
				// Create the pattern
				String prefix = new String(name.toCharArray()) + "*"; //$NON-NLS-1$
				ICSearchPattern pattern = SearchEngine.createSearchPattern(prefix, ICSearchConstants.FUNCTION, ICSearchConstants.DEFINITIONS, false);
				
				// Run the search
				BasicSearchResultCollector collector = new BasicSearchResultCollector();
				try {
					searchEngine.search(CUIPlugin.getWorkspace(), pattern, scope, collector, false);
				} catch (InterruptedException e) {
					return;
				}
				
				Set results = collector.getSearchResults();
				Iterator iResults = results.iterator();
				while (iResults.hasNext()) {
					BasicSearchMatch match = (BasicSearchMatch)iResults.next();
					handleFunction(match.getName(), viewer, completionNode, offset, proposals);
				}
			}
		}
		// TODO else search the prefix text
	}

	private void handleFunction(String name, ITextViewer viewer, ASTCompletionNode completionNode, int offset, List proposals) {
		int repLength = completionNode.getLength();
		int repOffset = offset - repLength;
		Image image = CUIPlugin.getImageDescriptorRegistry().get(CElementImageProvider.getFunctionImageDescriptor());
		String repString = name + "()"; //$NON-NLS-1$
		CCompletionProposal proposal = new CCompletionProposal(repString, repOffset, repLength, image, repString, 1, viewer);
		proposal.setCursorPosition(repString.length() - 1);
		proposals.add(proposal);
	}

}
