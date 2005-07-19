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
import org.eclipse.cdt.core.dom.ast.IASTFieldReference;
import org.eclipse.cdt.core.dom.ast.IASTName;
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

        // No prefix, no completions
        if (prefix.length() == 0)
            return;

        if (!validContext(completionNode))
            return;
        
        String[] keywords = cppkeywords; // default to C++
        if (workingCopy != null && workingCopy.isCLanguage())
            keywords = ckeywords;
        
        if (prefix.length() > 0)
            for (int i = 0; i < keywords.length; ++i)
                if (keywords[i].startsWith(prefix)) {
                    ImageDescriptor imagedesc = CElementImageProvider.getKeywordImageDescriptor();
                    Image image = imagedesc != null ? CUIPlugin.getImageDescriptorRegistry().get(imagedesc) : null;
                    int repLength = prefix.length();
                    int repOffset = offset - repLength;
                    proposals.add(new CCompletionProposal(keywords[i], repOffset, repLength, image, keywords[i], 1, viewer));
                }
    }

    // TODO This is copied from the search completion contributor
    // We should make this common
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
