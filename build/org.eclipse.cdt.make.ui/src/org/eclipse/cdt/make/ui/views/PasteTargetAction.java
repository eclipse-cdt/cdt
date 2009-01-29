/*******************************************************************************
 * Copyright (c) 2008, 2009 Andrew Gvozdev.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev (Quoin Inc.)- Initial implementation
 *******************************************************************************/

package org.eclipse.cdt.make.ui.views;

import java.util.List;

import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.dnd.FileTransferDropTargetListener;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetTransfer;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetTransferData;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetTransferDropTargetListener;
import org.eclipse.cdt.make.internal.ui.dnd.TextTransferDropTargetListener;
import org.eclipse.core.resources.IContainer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.SelectionListenerAction;

/**
 * Action to paste make targets from clipboard to Make Target View.
 * {@link PasteTargetAction} can accept several kinds of clipboard transfers
 * including make targets, plain text or files .
 * <p>
 * {@link CopyTargetAction} and {@link PasteTargetAction} are able to transfer
 * targets inside Make Target View or between eclipse sessions.
 *
 */
public class PasteTargetAction extends SelectionListenerAction {

	private final Shell shell;
	private final Clipboard clipboard;

	/**
	 * Constructor setting internal private fields and initializing the action.
	 *
	 * @param shell - the shell in which to show any dialogs.
	 * @param clipboard - system clipboard.
	 */
	protected PasteTargetAction(Shell shell, Clipboard clipboard) {
		super(MakeUIPlugin.getResourceString("PasteTargetAction.label")); //$NON-NLS-1$
		this.shell = shell;
		this.clipboard = clipboard;

		ISharedImages images = PlatformUI.getWorkbench().getSharedImages();
		setDisabledImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED));
		setImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));
		setHoverImageDescriptor(images.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE));

		setToolTipText(MakeUIPlugin.getResourceString("PasteTargetAction.tooltip")); //$NON-NLS-1$
	}

	/**
	 * Updates enablement of this action in response to the given selection.
	 */
	@Override
	protected boolean updateSelection(IStructuredSelection selection) {
		if (!super.updateSelection(selection)) {
			return false;
		}

		if (selection.size() == 1 && (selection.getFirstElement() instanceof IContainer)) {
			return true;
		}

		return false;
	}

	/**
	 * Perform actual action of pasting of make targets from clipboard. An appropriate
	 * transfer agent is used depending on the clipboard content data type.
	 */
	@Override
	public void run() {
		List<?> resources = getSelectedResources();
		if (resources.size() != 1 || !(resources.get(0) instanceof IContainer)) {
			return;
		}
		IContainer dropContainer = (IContainer) resources.get(0);

		Object clipboardContent;

		clipboardContent = clipboard.getContents(MakeTargetTransfer.getInstance());
		if (clipboardContent instanceof MakeTargetTransferData) {
			MakeTargetTransferDropTargetListener.createTransferTargetsUI(
				(MakeTargetTransferData) clipboardContent, dropContainer, DND.DROP_COPY, shell);
			return;
		}

		clipboardContent = clipboard.getContents(FileTransfer.getInstance());
		if (clipboardContent instanceof String[]) {
			FileTransferDropTargetListener.createFileTargetsUI((String[]) clipboardContent,
				dropContainer, DND.DROP_COPY, shell);
			return;
		}

		clipboardContent = clipboard.getContents(TextTransfer.getInstance());
		if (clipboardContent instanceof String) {
			TextTransferDropTargetListener.createMultilineTargetsUI((String) clipboardContent,
				dropContainer, DND.DROP_COPY, shell);
			return;
		}

	}

}
