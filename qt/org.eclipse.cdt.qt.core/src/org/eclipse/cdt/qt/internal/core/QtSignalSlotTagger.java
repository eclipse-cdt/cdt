/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.qt.internal.core;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.tag.IBindingTagger;
import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.dom.ast.tag.ITagWriter;
import org.eclipse.cdt.core.dom.ast.tag.IWritableTag;
import org.eclipse.cdt.qt.core.QtKeywords;
import org.eclipse.cdt.qt.core.QtPlugin;

public class QtSignalSlotTagger implements IBindingTagger
{
    private static ICPPASTVisibilityLabel findVisibilityLabel( ICPPMethod method, IASTNode ast )
    {
        // the visibility cannot be found without an ast
        if( ast == null )
            return null;

        IASTNode methodDecl = ast;
        ICPPASTCompositeTypeSpecifier classType = null;
        while( methodDecl != null
            && classType == null )
        {
            IASTNode parent = methodDecl.getParent();
            if( parent instanceof ICPPASTCompositeTypeSpecifier )
                classType = (ICPPASTCompositeTypeSpecifier)parent;
            else
                methodDecl = parent;
        }

        if( methodDecl == null
         || classType == null )
            return null;

        ICPPASTVisibilityLabel lastLabel = null;
        for( IASTDeclaration decl : classType.getMembers() )
        {
            if( decl instanceof ICPPASTVisibilityLabel )
                lastLabel = (ICPPASTVisibilityLabel)decl;
            else if( decl == methodDecl )
                return lastLabel;
        }

        return null;
    }

    @Override
    public ITag process( ITagWriter tagWriter, IBinding binding, IASTName ast )
    {
        // only methods a be signals or slots
        if( ! ( binding instanceof ICPPMethod ) )
            return null;

        // a visibility label is required in order to decide whether the method is a signal/slot
        ICPPMethod method = (ICPPMethod)binding;
        ICPPASTVisibilityLabel v = findVisibilityLabel( method, ast );
        if( v == null )
            return null;

        byte bitset = 0;
        for( IASTNodeLocation loc : v.getNodeLocations() )
            if( loc instanceof IASTMacroExpansionLocation )
            {
                IASTMacroExpansionLocation macroExpansion = (IASTMacroExpansionLocation)loc;
                IASTPreprocessorMacroExpansion exp = macroExpansion.getExpansion();
                String macro = exp.getMacroReference().toString();

                if( QtKeywords.SIGNALS.equals( macro ) || QtKeywords.Q_SIGNALS.equals( macro ) )
                    bitset |= QtPlugin.SignalSlot_Mask_signal;
                else if( QtKeywords.SLOTS.equals( macro ) || QtKeywords.Q_SLOTS.equals( macro ) )
                    bitset |= QtPlugin.SignalSlot_Mask_slot;
            }

        if( bitset != 0 )
        {
            IWritableTag tag = tagWriter.createTag( QtPlugin.SIGNAL_SLOT_TAGGER_ID, 1 );
            if( tag != null
             && tag.putByte( 0, bitset ) )
                return tag;
        }

        return null;
    }
}
