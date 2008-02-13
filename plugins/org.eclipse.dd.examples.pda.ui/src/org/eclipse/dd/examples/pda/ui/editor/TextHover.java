/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.ui.editor;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;


/**
 * Produces debug hover for the PDA debugger.
 */
public class TextHover implements ITextHover {

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextHover#getHoverInfo(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
     */
    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
        /*String varName = null;
        try {
            varName = textViewer.getDocument().get(hoverRegion.getOffset(), hoverRegion.getLength());
        } catch (BadLocationException e) {
           return null;
        }
        if (varName.startsWith("$") && varName.length() > 1) {
            varName = varName.substring(1);
        }
   
        PDAStackFrame frame = null;
        IAdaptable debugContext = DebugUITools.getDebugContext();
        if (debugContext instanceof PDAStackFrame) {
           frame = (PDAStackFrame) debugContext;
        } else if (debugContext instanceof PDAThread) {
            PDAThread thread = (PDAThread) debugContext;
            try {
                frame = (PDAStackFrame) thread.getTopStackFrame();
            } catch (DebugException e) {
                return null;
            }
        } else if (debugContext instanceof PDADebugTarget) {
            PDADebugTarget target = (PDADebugTarget) debugContext;
            try {
                IThread[] threads = target.getThreads();
                if (threads.length > 0) {
                    frame = (PDAStackFrame) threads[0].getTopStackFrame();
                }
            } catch (DebugException e) {
                return null;
            }
        }
        if (frame != null) {
            try {
                IVariable[] variables = frame.getVariables();
                for (int i = 0; i < variables.length; i++) {
                    IVariable variable = variables[i];
                    if (variable.getName().equals(varName)) {
                        return varName + " = " + variable.getValue().getValueString(); 
                    }
                }
            } catch (DebugException e) {
            }
        }*/
        return null;
    }
    	
    /* (non-Javadoc)
     * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer, int)
     */
    public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
        return WordFinder.findWord(textViewer.getDocument(), offset);
    }

}
