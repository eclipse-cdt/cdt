/*******************************************************************************
 * Copyright (c) 2008, 2010 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Gvozdev (Quoin Inc.) - Initial implementation
 *******************************************************************************/

package org.eclipse.cdt.make.ui.views;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetDndUtil;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetTransfer;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetTransferData;
import org.eclipse.cdt.make.internal.ui.dnd.TextTransferDragSourceListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * Action to copy make targets from Make Target View to clipboard.
 * {@link CopyTargetAction} can transfer targets as {@link IMakeTarget}s or
 * plain text.
 * <p>
 * {@link CopyTargetAction} and {@link PasteTargetAction} are able to transfer
 * targets inside Make Target View or between eclipse sessions.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CopyTargetAction extends SelectionListenerAction {

	private final Shell shell;
	private final Clipboard clipboard;
	private final PasteTargetAction pasteAction;

	/**
	 * Constructor setting internal private fields and initializing the action.
	 *
	 * @param shell
	 *            - the shell in which to show any dialogs.
	 * @param clipboard
	 *            - system clipboard.
	 * @param pasteAction
	 *            -associated paste action. May be {@code null}.
	 * @since 7.3
	 */
	public CopyTargetAction(Shell shell, Clipboard clipboard, PasteTargetAction pasteAction) {
		super(MakeUIPlugin.getResourceString("CopyTargetAction.label")); //$NON-NLS-1$
		this.shell = shell;
		this.clipboard = clipboard;
		this.pasteAction = pasteAction;

		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
		setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED));
		setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));
		setHoverImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_COPY));

		setToolTipText(MakeUIPlugin.getResourceString("CopyTargetAction.tooltip")); //$NON-NLS-1$
	}

	/**
	 * Updates enablement of this action in response to the given selection.
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!super.updateSelection(selection)) {
			return false;
		}

		return MakeTargetDndUtil.isDragable(selection);
	}

	/**
	 * Perform actual action of copying of make targets to clipboard. Two
	 * transfer agents are initiated, {@link MakeTargetTransfer} and
	 * {@link TextTransfer}.
	 */
	@Override
	public void run() {
		IStructuredSelection selection = getStructuredSelection();

		MakeTargetTransferData makeTargetTransferData = new MakeTargetTransferData();
		for (Object selectionItem : selection.toList()) {
			if (selectionItem instanceof IMakeTarget) {
				makeTargetTransferData.addMakeTarget((IMakeTarget) selectionItem);
			}
		}

		try {
			clipboard.setContents(
					new Object[] { makeTargetTransferData,
							TextTransferDragSourceListener.convertSelectedMakeTargetsToText(selection) },
					new Transfer[] { MakeTargetTransfer.getInstance(), TextTransfer.getInstance() });

			// update the enablement of the paste action
			if (pasteAction != null && pasteAction.getStructuredSelection() != null) {
				pasteAction.selectionChanged(pasteAction.getStructuredSelection());
			}

		} catch (SWTError e) {
			MakeUIPlugin.errorDialog(shell, MakeUIPlugin.getResourceString("CopyTargetAction.title.clipboardProblem"), //$NON-NLS-1$
					MakeUIPlugin.getResourceString("CopyTargetAction.message.clipboardProblem"), //$NON-NLS-1$
					e);
		}

	}

}
