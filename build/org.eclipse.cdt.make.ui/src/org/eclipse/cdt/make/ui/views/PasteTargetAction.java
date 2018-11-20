/*******************************************************************************
 * Copyright (c) 2008, 2011 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Andrew Gvozdev (Quoin Inc.)- Initial implementation
 *******************************************************************************/

package org.eclipse.cdt.make.ui.views;

import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.dnd.FileTransferDropTargetListener;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetTransfer;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetTransferData;
import org.eclipse.cdt.make.internal.ui.dnd.MakeTargetTransferDropTargetListener;
import org.eclipse.cdt.make.internal.ui.dnd.TextTransferDropTargetListener;
import org.eclipse.cdt.make.ui.TargetSourceContainer;
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
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class PasteTargetAction extends SelectionListenerAction {

	private final Shell shell;
	private final Clipboard clipboard;

	/**
	 * Constructor setting internal private fields and initializing the action.
	 *
	 * @param shell
	 *            - the shell in which to show any dialogs.
	 * @param clipboard
	 *            - system clipboard.
	 * @since 7.3
	 */
	public PasteTargetAction(Shell shell, Clipboard clipboard) {
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

		return determineDropContainer() != null;
	}

	/**
	 * Perform actual action of pasting of make targets from clipboard. An appropriate
	 * transfer agent is used depending on the clipboard content data type.
	 */
	@Override
	public void run() {
		IContainer dropContainer = determineDropContainer();
		if (dropContainer == null) {
			return;
		}

		Object clipboardContent;

		clipboardContent = clipboard.getContents(MakeTargetTransfer.getInstance());
		if (clipboardContent instanceof MakeTargetTransferData) {
			MakeTargetTransferDropTargetListener.createTransferTargetsUI((MakeTargetTransferData) clipboardContent,
					dropContainer, DND.DROP_COPY, shell);
			return;
		}

		clipboardContent = clipboard.getContents(FileTransfer.getInstance());
		if (clipboardContent instanceof String[]) {
			FileTransferDropTargetListener.createFileTargetsUI((String[]) clipboardContent, dropContainer,
					DND.DROP_COPY, shell);
			return;
		}

		clipboardContent = clipboard.getContents(TextTransfer.getInstance());
		if (clipboardContent instanceof String) {
			TextTransferDropTargetListener.createMultilineTargetsUI((String) clipboardContent, dropContainer,
					DND.DROP_COPY, shell);
			return;
		}

	}

	/**
	 * Drop container is determined by first element. The rest of the logic is
	 * to figure out if drop is allowed. The drop is allowed if the selection is
	 * one {@code IContainer} or {@code IMakeTarget}s from the same folder.
	 *
	 * @return drop container or {@code null}.
	 */
	private IContainer determineDropContainer() {
		IStructuredSelection selection = getStructuredSelection();
		if (selection.size() == 0) {
			return null;
		}

		Object first = selection.getFirstElement();

		if (first instanceof IContainer) {
			if (selection.size() == 1) {
				return (IContainer) first;
			} else {
				return null;
			}
		}

		if (first instanceof IMakeTarget) {
			// it has to be selection of IMakeTargets only and from the same IContainer
			IContainer dropContainer = ((IMakeTarget) first).getContainer();
			for (Object item : selection.toList()) {
				if (!(item instanceof IMakeTarget) || ((IMakeTarget) item).getContainer() != dropContainer) {
					return null;
				}
			}
			return dropContainer;
		}

		if (first instanceof TargetSourceContainer) {
			return ((TargetSourceContainer) first).getContainer();
		}

		return null;
	}

}
