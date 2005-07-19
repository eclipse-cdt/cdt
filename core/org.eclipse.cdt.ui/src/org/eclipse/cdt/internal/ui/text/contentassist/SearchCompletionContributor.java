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
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.core.search.OrPattern;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.core.search.ICSearchConstants.SearchFor;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.contentassist.ICompletionContributor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Image;

public class SearchCompletionContributor implements ICompletionContributor {

    // The completion search for list
    // Kind of like the All Elements but excluding METHODS and FIELDS
    private static SearchFor[] completionSearchFor = {
        ICSearchConstants.CLASS_STRUCT,
        ICSearchConstants.FUNCTION,
        ICSearchConstants.VAR,
        ICSearchConstants.UNION,
        ICSearchConstants.ENUM,
        ICSearchConstants.ENUMTOR,
        ICSearchConstants.NAMESPACE,
        ICSearchConstants.TYPEDEF,
        ICSearchConstants.MACRO
    };

    public void contributeCompletionProposals(ITextViewer viewer, int offset,
			IWorkingCopy workingCopy, ASTCompletionNode completionNode, String prefix,
			List proposals)
	{
	    if (!validContext(completionNode))
            return;
        
        if (prefix == null || prefix.length() == 0)
            return;
        
		// Create search engine
		SearchEngine searchEngine = new SearchEngine();
		searchEngine.setWaitingPolicy( ICSearchConstants.FORCE_IMMEDIATE_SEARCH );
				
		// Create search scope
        ICSearchScope scope;
        if (workingCopy != null) {
            ICElement[] projects = new ICElement[] { workingCopy.getCProject() };
            scope = SearchEngine.createCSearchScope(projects, true);
        } else
            scope = SearchEngine.createWorkspaceScope();
			
		// Create the pattern
        String patternString = prefix + "*"; //$NON-NLS-1$
        OrPattern pattern = new OrPattern();
        for (int i = 0; i < completionSearchFor.length; i++)
            pattern.addPattern( SearchEngine.createSearchPattern( patternString,
                    completionSearchFor[i], ICSearchConstants.ALL_OCCURRENCES, true));

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
			IMatch match = (IMatch)iResults.next();
            switch (match.getElementType()) {
            case ICElement.C_FUNCTION:
            case ICElement.C_FUNCTION_DECLARATION:
            case ICElement.C_METHOD:
            case ICElement.C_METHOD_DECLARATION:
                handleFunction(match, viewer, prefix, offset, proposals);
                break;
            default:
                handleMatch(match, viewer, prefix, offset, proposals);
            }
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

    private void handleMatch(IMatch match, ITextViewer viewer, String prefix, int offset, List proposals) {
        int repLength = prefix.length();
        int repOffset = offset - repLength;
        Image image = CUIPlugin.getImageDescriptorRegistry().get(CElementImageProvider.getImageDescriptor(match.getElementType()));
        String repString = match.getName();
        CCompletionProposal proposal = new CCompletionProposal(repString, repOffset, repLength, image, repString, 1, viewer);
        proposals.add(proposal);
    }
    
	private void handleFunction(IMatch match, ITextViewer viewer, String prefix, int offset, List proposals) {
		Image image = CUIPlugin.getImageDescriptorRegistry().get(CElementImageProvider.getImageDescriptor(match.getElementType()));
        
        StringBuffer repStringBuff = new StringBuffer();
        repStringBuff.append(match.getName());
        repStringBuff.append('(');
        
        StringBuffer args = new StringBuffer();
        String[] params = match.getParameters();
        if (params != null)
            for (int i = 0; i < params.length; ++i) {
                if (i > 0)
                    args.append(',');
                args.append(params[i]);
            }
            
        String returnType = match.getReturnType();
        String argString = args.toString();
        
        StringBuffer dispStringBuff = new StringBuffer(repStringBuff.toString());
        dispStringBuff.append(argString);
        dispStringBuff.append(')');

        repStringBuff.append(')');
        String repString = repStringBuff.toString();

        String idString = null;
        if (returnType != null) {
            idString = dispStringBuff.toString();
            dispStringBuff.append(' ');
            dispStringBuff.append(returnType);
        }
        String dispString = dispStringBuff.toString();

        int repLength = prefix.length();
        int repOffset = offset - repLength;
        CCompletionProposal proposal = new CCompletionProposal(repString, repOffset, repLength, image, dispString, idString, 1, viewer);
        proposal.setCursorPosition(repString.length() - 1);
        
        if (argString.length() > 0) {
            CProposalContextInformation info = new CProposalContextInformation(repString, argString);
            info.setContextInformationPosition(offset);
            proposal.setContextInformation(info);
        }
        
        proposals.add(proposal);
    }
}
