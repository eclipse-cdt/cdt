/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 **********************************************************************/
package org.eclipse.cdt.internal.ui.text.contentassist;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.ASTCompletionNode;
import org.eclipse.cdt.core.model.IWorkingCopy;
import org.eclipse.cdt.core.parser.Keywords;
import org.eclipse.cdt.internal.ui.viewsupport.CElementImageProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.contentassist.ICompletionContributor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.graphics.Image;

public class KeywordCompletionContributor implements ICompletionContributor {

    public void contributeCompletionProposals(ITextViewer viewer, int offset,
            IWorkingCopy workingCopy, ASTCompletionNode completionNode,
            String prefix, List proposals) {

        String[] keywords = cppkeywords; // default to C++
        if (workingCopy != null && workingCopy.isCLanguage())
            keywords = ckeywords;
        
        if (prefix.length() > 0)
            for (int i = 0; i < keywords.length; ++i)
                if (keywords[i].startsWith(prefix))
                    handleKeyword(keywords[i], completionNode, offset, viewer, proposals);
    }
    
    private void handleKeyword(String keyword, ASTCompletionNode completionNode, int offset, ITextViewer viewer, List proposals) {
        Image image = getImage(CElementImageProvider.getKeywordImageDescriptor());
        proposals.add(createProposal(keyword, keyword, image, completionNode, offset, viewer));
    }
    
    private CCompletionProposal createProposal(String repString, String dispString, Image image, ASTCompletionNode completionNode, int offset, ITextViewer viewer) {
        int repLength = completionNode.getLength();
        int repOffset = offset - repLength;
        return new CCompletionProposal(repString, repOffset, repLength, image, dispString, 1, viewer);
    }

    private Image getImage(ImageDescriptor desc) {
        return desc != null ? CUIPlugin.getImageDescriptorRegistry().get(desc) : null;
    }
    
    // These are the keywords we complete
    // We only do the ones that are >= 5 characters long
    private static String [] ckeywords = {
        Keywords.BREAK,
        Keywords.CONST,
        Keywords.CONTINUE,
        Keywords.DEFAULT,
        Keywords.DOUBLE,
        Keywords.EXTERN,
        Keywords.FLOAT,
        Keywords.INLINE,
        Keywords.REGISTER,
        Keywords.RESTRICT,
        Keywords.RETURN,
        Keywords.SHORT,
        Keywords.SIGNED,
        Keywords.SIZEOF,
        Keywords.STATIC,
        Keywords.STRUCT,
        Keywords.SWITCH,
        Keywords.TYPEDEF,
        Keywords.UNION,
        Keywords.UNSIGNED,
        Keywords.VOLATILE,
        Keywords.WHILE,
        Keywords._BOOL,
        Keywords._COMPLEX,
        Keywords._IMAGINARY
    };

    private static String [] cppkeywords = {
        Keywords.BREAK,
        Keywords.CATCH,
        Keywords.CLASS,
        Keywords.CONST,
        Keywords.CONST_CAST,
        Keywords.CONTINUE,
        Keywords.DEFAULT,
        Keywords.DELETE,
        Keywords.DOUBLE,
        Keywords.DYNAMIC_CAST,
        Keywords.EXPLICIT,
        Keywords.EXPORT,
        Keywords.EXTERN,
        Keywords.FALSE,
        Keywords.FLOAT,
        Keywords.FRIEND,
        Keywords.INLINE,
        Keywords.MUTABLE,
        Keywords.NAMESPACE,
        Keywords.OPERATOR,
        Keywords.PRIVATE,
        Keywords.PROTECTED,
        Keywords.PUBLIC,
        Keywords.REGISTER,
        Keywords.REINTERPRET_CAST,
        Keywords.RETURN,
        Keywords.SHORT,
        Keywords.SIGNED,
        Keywords.SIZEOF,
        Keywords.STATIC,
        Keywords.STATIC_CAST,
        Keywords.STRUCT,
        Keywords.SWITCH,
        Keywords.TEMPLATE,
        Keywords.THROW,
        Keywords.TYPEDEF,
        Keywords.TYPEID,
        Keywords.TYPENAME,
        Keywords.UNION,
        Keywords.UNSIGNED,
        Keywords.USING,
        Keywords.VIRTUAL,
        Keywords.VOLATILE,
        Keywords.WCHAR_T,
        Keywords.WHILE
    };

}
