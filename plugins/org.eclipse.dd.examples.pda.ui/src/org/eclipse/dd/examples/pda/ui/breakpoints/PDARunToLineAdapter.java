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
package org.eclipse.dd.examples.pda.ui.breakpoints;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dd.examples.pda.PDAPlugin;
import org.eclipse.dd.examples.pda.breakpoints.PDARunToLineBreakpoint;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.debug.ui.actions.RunToLineHandler;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;


/**
 * Run to line target for the Java debugger
 */
public class PDARunToLineAdapter implements IRunToLineTarget {
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IRunToLineTarget#runToLine(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.core.model.ISuspendResume)
	 */
	public void runToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) throws CoreException {
		IEditorPart editorPart = (IEditorPart)part;
		ITextEditor textEditor = (ITextEditor)editorPart;
		ITextSelection textSelection = (ITextSelection) selection;
		int lineNumber = textSelection.getStartLine() + 1;
		if (lineNumber > 0) {
			if (target instanceof IAdaptable) {
				IDebugTarget debugTarget = (IDebugTarget) ((IAdaptable)target).getAdapter(IDebugTarget.class);
				if (debugTarget != null) {
					//#ifdef ex7
//#					// TODO: Exercise 7 - perform the run-to-line with a run-to-line breakpoint and handler
					//#else
					IFile resource = (IFile) textEditor.getEditorInput().getAdapter(IResource.class);
					IBreakpoint breakpoint= new PDARunToLineBreakpoint(resource, lineNumber);
					RunToLineHandler handler = new RunToLineHandler(debugTarget, target, breakpoint);
					handler.run(new NullProgressMonitor());
					//#endif
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IRunToLineTarget#canRunToLine(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.core.model.ISuspendResume)
	 */
	public boolean canRunToLine(IWorkbenchPart part, ISelection selection, ISuspendResume target) {
		//#ifdef ex7
//#		// TODO: Exercise 7 - ensure the target is a PDA target
//#		return false;
		//#else
		return target instanceof IDebugElement &&
		 ((IDebugElement)target).getModelIdentifier().equals(PDAPlugin.ID_PDA_DEBUG_MODEL);
		//#endif
	}
}
