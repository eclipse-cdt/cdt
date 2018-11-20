/*******************************************************************************
 * Copyright (c) 2005, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Wind River Systems - added support for IToggleBreakpointsTargetFactory
 *     Marc Khouzam - Create class for dynamic printf (Bug 400628)
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions.breakpoints;

import org.eclipse.cdt.debug.internal.ui.actions.ActionMessages;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.breakpoints.IToggleBreakpointsTargetCExtension;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetFactory;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Action to interactively create a dynamic printf from the vertical ruler of a
 * workbench part containing a document. The part must provide an
 * <code>IToggleBreakpointsTargetExtension2</code> adapter.
 * <p>
 * Clients may instantiate this class.
 * </p>
 * @since 7.4
 * @see org.eclipse.debug.ui.actions.RulerToggleBreakpointActionDelegate
 */
public class CAddDynamicPrintfInteractiveRulerAction extends Action implements IUpdate {

	private IWorkbenchPart fPart;
	private IDocument fDocument;
	private IVerticalRulerInfo fRulerInfo;

	private IToggleBreakpointsTargetCExtension fDynamicPrintfBreakpointsTarget;

	/**
	 * Constructs a new action to toggle a dynamic printf in the given
	 * part containing the given document and ruler.
	 *
	 * @param part the part in which to toggle the dynamic printf - provides
	 *  an <code>IToggleBreakpointsTarget</code> adapter
	 * @param document the document breakpoints are being set in or
	 * <code>null</code> when the document should be derived from the given part
	 * @param rulerInfo specifies location the user has double-clicked
	 */
	public CAddDynamicPrintfInteractiveRulerAction(IWorkbenchPart part, IDocument document,
			IVerticalRulerInfo rulerInfo) {
		super(ActionMessages.getString("CAddDynamicPrintfInteractiveRulerAction_label")); //$NON-NLS-1$
		fPart = part;
		fDocument = document;
		fRulerInfo = rulerInfo;
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.jface.action.IAction#run()
	 */
	@Override
	public void run() {
		IDocument document = getDocument();
		if (document == null) {
			return;
		}

		int line = fRulerInfo.getLineOfLastMouseButtonActivity();

		// Test if line is valid
		if (line == -1)
			return;

		try {
			if (fDynamicPrintfBreakpointsTarget != null) {
				ITextSelection selection = getTextSelection(document, line);
				if (fDynamicPrintfBreakpointsTarget.canCreateLineBreakpointsInteractive(fPart, selection)) {
					fDynamicPrintfBreakpointsTarget.createLineBreakpointsInteractive(fPart, selection);
				}
			}
		} catch (BadLocationException e) {
			reportException(e);
		} catch (CoreException e) {
			reportException(e);
		}
	}

	/**
	 * Report an error to the user.
	 *
	 * @param e underlying exception
	 */
	private void reportException(Exception e) {
		IStatus status = new Status(IStatus.ERROR, CDebugUIPlugin.PLUGIN_ID, "Error creating dynamic printf: ", e); //$NON-NLS-1$
		ErrorDialog.openError(fPart.getSite().getShell(),
				ActionMessages.getString("CAddBreakpointInteractiveRulerAction_error_title"), //$NON-NLS-1$
				ActionMessages.getString("CAddDynamicPrintfInteractiveRulerAction_error_message"), //$NON-NLS-1$
				status);
		CDebugUIPlugin.log(status);
	}

	/**
	 * Disposes this action. Clients must call this method when
	 * this action is no longer needed.
	 */
	public void dispose() {
		fDocument = null;
		fPart = null;
		fRulerInfo = null;
	}

	/**
	 * Returns the document on which this action operates.
	 *
	 * @return the document or <code>null</code> if none
	 */
	private IDocument getDocument() {
		if (fDocument != null)
			return fDocument;

		if (fPart instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) fPart;
			IDocumentProvider provider = editor.getDocumentProvider();
			if (provider != null)
				return provider.getDocument(editor.getEditorInput());
		}

		IDocument doc = fPart.getAdapter(IDocument.class);
		if (doc != null) {
			return doc;
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	@Override
	public void update() {
		IDocument document = getDocument();
		if (document != null) {
			int line = fRulerInfo.getLineOfLastMouseButtonActivity();
			if (line > -1) {
				try {
					ITextSelection selection = getTextSelection(document, line);

					if (fDynamicPrintfBreakpointsTarget == null) {
						fDynamicPrintfBreakpointsTarget = fetchDynamicPrintfBreakpointsTarget(selection);
					}

					if (fDynamicPrintfBreakpointsTarget == null) {
						setEnabled(false);
						return;
					}
					if (fDynamicPrintfBreakpointsTarget.canCreateLineBreakpointsInteractive(fPart, selection)) {
						setEnabled(true);
						return;
					}
				} catch (BadLocationException e) {
					reportException(e);
				}
			}
		}
		setEnabled(false);
	}

	/**
	 * Determines the text selection for the breakpoint action.  If clicking on the ruler inside
	 * the highlighted text, return the text selection for the highlighted text.  Otherwise,
	 * return a text selection representing the start of the line.
	 *
	 * @param document	The IDocument backing the Editor.
	 * @param line	The line clicked on in the ruler.
	 * @return	An ITextSelection as described.
	 * @throws BadLocationException	If underlying operations throw.
	 */
	private ITextSelection getTextSelection(IDocument document, int line) throws BadLocationException {
		IRegion region = document.getLineInformation(line);
		ITextSelection textSelection = new TextSelection(document, region.getOffset(), 0);
		ISelectionProvider provider = fPart.getSite().getSelectionProvider();
		if (provider != null) {
			ISelection selection = provider.getSelection();
			if (selection instanceof ITextSelection && ((ITextSelection) selection).getStartLine() <= line
					&& ((ITextSelection) selection).getEndLine() >= line) {
				textSelection = (ITextSelection) selection;
			}
		}
		return textSelection;
	}

	private IToggleBreakpointsTargetCExtension fetchDynamicPrintfBreakpointsTarget(ITextSelection selection) {
		if (fDynamicPrintfBreakpointsTarget == null) {
			IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(IDebugUIConstants.PLUGIN_ID,
					IDebugUIConstants.EXTENSION_POINT_TOGGLE_BREAKPOINTS_TARGET_FACTORIES);
			IConfigurationElement[] elements = ep.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				String id = elements[i].getAttribute("id"); //$NON-NLS-1$
				if (id != null && id.equals("org.eclipse.cdt.debug.ui.ToggleCDynamicPrintfTargetFactory")) { //$NON-NLS-1$
					try {
						Object obj = elements[i].createExecutableExtension("class"); //$NON-NLS-1$
						if (obj instanceof IToggleBreakpointsTargetFactory) {
							IToggleBreakpointsTarget target = ((IToggleBreakpointsTargetFactory) obj)
									.createToggleTarget(
											ToggleCDynamicPrintfTargetFactory.TOGGLE_C_DYNAMICPRINTF_TARGET_ID);
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
