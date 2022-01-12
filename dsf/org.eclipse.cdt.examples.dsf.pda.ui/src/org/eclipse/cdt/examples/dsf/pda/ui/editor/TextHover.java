/*******************************************************************************
 *  Copyright (c) 2005, 2009 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     Wind River Systems - adopted to use with DSF
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda.ui.editor;

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
	@Override
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
	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		return WordFinder.findWord(textViewer.getDocument(), offset);
	}

}
