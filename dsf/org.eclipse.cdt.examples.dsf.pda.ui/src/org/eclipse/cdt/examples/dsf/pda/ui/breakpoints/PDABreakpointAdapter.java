/*******************************************************************************
 *  Copyright (c) 2005, 2015 IBM Corporation and others.
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
package org.eclipse.cdt.examples.dsf.pda.ui.breakpoints;

import org.eclipse.cdt.examples.dsf.pda.PDAPlugin;
import org.eclipse.cdt.examples.dsf.pda.breakpoints.PDALineBreakpoint;
import org.eclipse.cdt.examples.dsf.pda.breakpoints.PDAWatchpoint;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.ILineBreakpoint;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Adapter to create breakpoints in PDA files.
 * <p>
 * This class is identical to the corresponding in PDA debugger implemented in
 * org.eclipse.debug.examples.ui.
 * </p>
 */
public class PDABreakpointAdapter implements IToggleBreakpointsTargetExtension {
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		ITextEditor textEditor = getEditor(part);
		if (textEditor != null) {
			IResource resource = textEditor.getEditorInput().getAdapter(IResource.class);
			ITextSelection textSelection = (ITextSelection) selection;
			int lineNumber = textSelection.getStartLine();
			IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager()
					.getBreakpoints(PDAPlugin.ID_PDA_DEBUG_MODEL);
			for (int i = 0; i < breakpoints.length; i++) {
				IBreakpoint breakpoint = breakpoints[i];
				if (breakpoint instanceof ILineBreakpoint && resource.equals(breakpoint.getMarker().getResource())) {
					if (((ILineBreakpoint) breakpoint).getLineNumber() == (lineNumber + 1)) {
						// remove
						breakpoint.delete();
						return;
					}
				}
			}
			// create line breakpoint (doc line numbers start at 0)
			PDALineBreakpoint lineBreakpoint = new PDALineBreakpoint(resource, lineNumber + 1);
			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(lineBreakpoint);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		return getEditor(part) != null;
	}

	/**
	 * Returns the editor being used to edit a PDA file, associated with the
	 * given part, or <code>null</code> if none.
	 *
	 * @param part workbench part
	 * @return the editor being used to edit a PDA file, associated with the
	 * given part, or <code>null</code> if none
	 */
	private ITextEditor getEditor(IWorkbenchPart part) {
		if (part instanceof ITextEditor) {
			ITextEditor editorPart = (ITextEditor) part;
			IResource resource = editorPart.getEditorInput().getAdapter(IResource.class);
			if (resource != null) {
				String extension = resource.getFileExtension();
				if (extension != null && extension.equals("pda")) {
					return editorPart;
				}
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		String[] variableAndFunctionName = getVariableAndFunctionName(part, selection);
		if (variableAndFunctionName != null && part instanceof ITextEditor && selection instanceof ITextSelection) {
			ITextEditor editorPart = (ITextEditor) part;
			int lineNumber = ((ITextSelection) selection).getStartLine();
			IResource resource = editorPart.getEditorInput().getAdapter(IResource.class);
			String var = variableAndFunctionName[0];
			String fcn = variableAndFunctionName[1];
			// look for existing watchpoint to delete
			IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager()
					.getBreakpoints(PDAPlugin.ID_PDA_DEBUG_MODEL);
			for (int i = 0; i < breakpoints.length; i++) {
				IBreakpoint breakpoint = breakpoints[i];
				if (breakpoint instanceof PDAWatchpoint && resource.equals(breakpoint.getMarker().getResource())) {
					PDAWatchpoint watchpoint = (PDAWatchpoint) breakpoint;
					String otherVar = watchpoint.getVariableName();
					String otherFcn = watchpoint.getFunctionName();
					if (otherVar.equals(var) && otherFcn.equals(fcn)) {
						breakpoint.delete();
						return;
					}
				}
			}
			// create watchpoint
			PDAWatchpoint watchpoint = new PDAWatchpoint(resource, lineNumber + 1, fcn, var, true, true);
			DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(watchpoint);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		return getVariableAndFunctionName(part, selection) != null;
	}

	/**
	 * Returns the variable and function names at the current line, or <code>null</code> if none.
	 *
	 * @param part text editor
	 * @param selection text selection
	 * @return the variable and function names at the current line, or <code>null</code> if none.
	 *  The array has two elements, the first is the variable name, the second is the function name.
	 */
	private String[] getVariableAndFunctionName(IWorkbenchPart part, ISelection selection) {
		ITextEditor editor = getEditor(part);
		if (editor != null && selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection) selection;
			IDocumentProvider documentProvider = editor.getDocumentProvider();
			try {
				documentProvider.connect(this);
				IDocument document = documentProvider.getDocument(editor.getEditorInput());
				IRegion region = document.getLineInformationOfOffset(textSelection.getOffset());
				String string = document.get(region.getOffset(), region.getLength()).trim();
				if (string.startsWith("var ")) {
					String varName = string.substring(4).trim();
					String fcnName = getFunctionName(document, varName,
							document.getLineOfOffset(textSelection.getOffset()));
					return new String[] { varName, fcnName };
				}
			} catch (CoreException e) {
			} catch (BadLocationException e) {
			} finally {
				documentProvider.disconnect(this);
			}
		}
		return null;
	}

	/**
	 * Returns the name of the function containing the given variable defined at the given
	 * line number in the specified document.
	 *
	 * @param document PDA source file
	 * @param varName variable name
	 * @param line line numbner at which the variable is defined
	 * @return name of function defining the variable
	 */
	private String getFunctionName(IDocument document, String varName, int line) {
		// This is a simple guess at the function name - look for the labels preceeding
		// the variable definition, and then see if there are any 'calls' to that
		// label. If none, assumet the variable is in the "_main_" function
		String source = document.get();
		int lineIndex = line - 1;
		while (lineIndex >= 0) {
			try {
				IRegion information = document.getLineInformation(lineIndex);
				String lineText = document.get(information.getOffset(), information.getLength());
				if (lineText.startsWith(":")) {
					String label = lineText.substring(1);
					if (source.indexOf("call " + label) >= 0) {
						return label;
					}
				}
				lineIndex--;
			} catch (BadLocationException e) {
			}
		}
		return "_main_";
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#toggleBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		if (canToggleWatchpoints(part, selection)) {
			toggleWatchpoints(part, selection);
		} else {
			toggleLineBreakpoints(part, selection);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#canToggleBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
		return canToggleLineBreakpoints(part, selection) || canToggleWatchpoints(part, selection);
	}
}
