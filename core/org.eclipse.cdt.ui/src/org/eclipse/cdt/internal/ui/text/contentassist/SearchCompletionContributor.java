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
			IWorkingCopy workingCopy, ASTCompletionNode completionNode, String prefix,
			List proposals)
	{
	    if (!validContext(completionNode))
            return;
            
		// Create search engine
		SearchEngine searchEngine = new SearchEngine();
		searchEngine.setWaitingPolicy( ICSearchConstants.FORCE_IMMEDIATE_SEARCH );
				
		// Create search scope
		ICElement[] projects = new ICElement[] { workingCopy.getCProject() };
		//ICSearchScope scope = SearchEngine.createCSearchScope(projects, true);
        ICSearchScope scope = SearchEngine.createWorkspaceScope();
			
		// Create the pattern
		ICSearchPattern pattern = SearchEngine.createSearchPattern(prefix + "*", ICSearchConstants.FUNCTION, ICSearchConstants.DECLARATIONS, false); //$NON-NLS-1$
				
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
			handleFunction(match.getName(), viewer, prefix, offset, proposals);
		}
	}

    private boolean validContext(ASTCompletionNode completionNode) {
        if (completionNode == null)
            // No completion node, assume true
            return true;

        boolean valid = true;
        IASTName[] names = completionNode.getNames();
        for (int i = 0; i < names.length; i++) {
            IASTName name = names[i];
            
            // not hooked up, not a valid name, ignore
            if (name.getTranslationUnit() == null)
                continue;
            
            // member access currently isn't valid
            if (name.getParent() instanceof IASTFieldReference) {
                valid = false;
                continue;
            }
            
            // found one that was valid
            return true;
        }

        // Couldn't find a valid context
        return valid;
    }

	private void handleFunction(String name, ITextViewer viewer, String prefix, int offset, List proposals) {
		int repLength = prefix.length();
		int repOffset = offset - repLength;
		Image image = CUIPlugin.getImageDescriptorRegistry().get(CElementImageProvider.getFunctionImageDescriptor());
		String repString = name + "()"; //$NON-NLS-1$
		CCompletionProposal proposal = new CCompletionProposal(repString, repOffset, repLength, image, repString, 1, viewer);
		proposal.setCursorPosition(repString.length() - 1);
		proposals.add(proposal);
	}

}
