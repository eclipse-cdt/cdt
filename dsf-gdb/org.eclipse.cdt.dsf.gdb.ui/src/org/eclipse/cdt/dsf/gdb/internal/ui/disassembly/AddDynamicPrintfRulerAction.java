/*******************************************************************************
 * Copyright (c) 2008, 2014 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Marc Khouzam (Ericsson) - Support for dynamic printf (Bug 400628)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.disassembly;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.breakpoints.IToggleBreakpointsTargetCExtension;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions.AbstractDisassemblyBreakpointRulerAction;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints.ToggleDynamicPrintfTargetFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetFactory;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Ruler action to add dynamic printf with a dialog properties.
 */
public class AddDynamicPrintfRulerAction extends AbstractDisassemblyBreakpointRulerAction {

	private IToggleBreakpointsTargetCExtension fDynamicPrintfBreakpointsTarget;

	protected AddDynamicPrintfRulerAction(IDisassemblyPart disassemblyPart, IVerticalRulerInfo rulerInfo) {
		super(disassemblyPart, rulerInfo);
		setText(DisassemblyMessages.Disassembly_action_AddDynamicPrintf_label);
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.actions.AbstractDisassemblyAction#run()
	 */
	@Override
	public void run() {
		if (fDynamicPrintfBreakpointsTarget != null) {
			IWorkbenchPart part = getDisassemblyPart();
			ISelection selection = getSelection();
			try {
				if (fDynamicPrintfBreakpointsTarget.canCreateLineBreakpointsInteractive(part, selection)) {
					fDynamicPrintfBreakpointsTarget.createLineBreakpointsInteractive(part, selection);
				}
			} catch (CoreException e) {
				reportException(e);
			}
		}
	}

	@Override
	public void update() {
		IDisassemblyPart part = getDisassemblyPart();
		if (part != null && part.isConnected()) {
			ISelection selection = getSelection();
			if (fDynamicPrintfBreakpointsTarget == null) {
				fDynamicPrintfBreakpointsTarget = fetchDynamicPrintfBreakpointsTarget(selection);
			}

			if (fDynamicPrintfBreakpointsTarget == null) {
				setEnabled(false);
				return;
			}
			if (fDynamicPrintfBreakpointsTarget.canCreateLineBreakpointsInteractive(part, selection)) {
				setEnabled(true);
				return;
			}
		}
		setEnabled(false);
	}

	/**
	 * Report an error to the user.
	 *
	 * @param e underlying exception
	 */
	private void reportException(Exception e) {
		IStatus status = new Status(IStatus.ERROR, CDebugUIPlugin.PLUGIN_ID, "Error creating dynamic printf: ", e); //$NON-NLS-1$
		ErrorDialog.openError(getDisassemblyPart().getSite().getShell(),
				DisassemblyMessages.Disassembly_action_AddDynamicPrintf_errorTitle,
				DisassemblyMessages.Disassembly_action_AddDynamicPrintf_errorMessage, status);
		CDebugUIPlugin.log(status);
	}

	/**
	 * Determines the text selection for the breakpoint action.  If clicking on the ruler inside
	 * the highlighted text, return the text selection for the highlighted text.  Otherwise,
	 * return a text selection representing the start of the line.
	 *
	 * @return  An ISelection as described.
	 * @throws BadLocationException If underlying operations throw.
	 */
	private ISelection getSelection() {
		IDocument document = getDocument();
		if (document != null) {
			int line = getRulerInfo().getLineOfLastMouseButtonActivity();

			try {
				IRegion region = getDocument().getLineInformation(line);
				ITextSelection textSelection = new TextSelection(document, region.getOffset(), 0);
				ISelectionProvider provider = getDisassemblyPart().getSite().getSelectionProvider();
				if (provider != null) {
					ISelection selection = provider.getSelection();
					if (selection instanceof ITextSelection && ((ITextSelection) selection).getStartLine() <= line
							&& ((ITextSelection) selection).getEndLine() >= line) {
						textSelection = (ITextSelection) selection;
					}
				}
				return textSelection;
			} catch (BadLocationException e) {
			}
		}
		return StructuredSelection.EMPTY;
	}

	private IToggleBreakpointsTargetCExtension fetchDynamicPrintfBreakpointsTarget(ISelection selection) {
		if (fDynamicPrintfBreakpointsTarget == null) {
			IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(IDebugUIConstants.PLUGIN_ID,
					IDebugUIConstants.EXTENSION_POINT_TOGGLE_BREAKPOINTS_TARGET_FACTORIES);
			IConfigurationElement[] elements = ep.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				String id = elements[i].getAttribute("id"); //$NON-NLS-1$
				if (id != null && id.equals("org.eclipse.cdt.dsf.gdb.ui.ToggleDynamicPrintfTargetFactory")) { //$NON-NLS-1$
					try {
						Object obj = elements[i].createExecutableExtension("class"); //$NON-NLS-1$
						if (obj instanceof IToggleBreakpointsTargetFactory) {
							IToggleBreakpointsTarget target = ((IToggleBreakpointsTargetFactory) obj)
									.createToggleTarget(
											ToggleDynamicPrintfTargetFactory.TOGGLE_C_DYNAMICPRINTF_TARGET_ID);
							if (target instanceof IToggleBreakpointsTargetCExtension) {
								fDynamicPrintfBreakpointsTarget = (IToggleBreakpointsTargetCExtension) target;
							}
						}
					} catch (CoreException e) {
					}
					break;
				}
			}
		}
		return fDynamicPrintfBreakpointsTarget;
	}

}
