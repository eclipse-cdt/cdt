/*******************************************************************************
 * Copyright (c) 2004, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 * Anna Dushistova (MontaVista) - [227537] moved actions from terminal.view to terminal plugin
 * Uwe Stieber (Wind River) - [260372] [terminal] Certain terminal actions are enabled if no target terminal control is available
 * Uwe Stieber (Wind River) - [294719] [terminal] SWT Widget disposed in TerminalActionPaste
 * Martin Oberhuber (Wind River) - [296212] Cannot paste text into terminal on some Linux hosts
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.control.actions;

import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.tm.internal.terminal.provisional.api.TerminalState;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class TerminalActionPaste extends AbstractTerminalAction {
	public TerminalActionPaste() {
		super(TerminalActionPaste.class.getName());
		setActionDefinitionId("org.eclipse.tm.terminal.paste"); //$NON-NLS-1$
		ISharedImages si = PlatformUI.getWorkbench().getSharedImages();
		setupAction(ActionMessages.PASTE, ActionMessages.PASTE, si.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE),
				si.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE),
				si.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED), false);
	}

	public TerminalActionPaste(ITerminalViewControl target) {
		super(target, TerminalActionPaste.class.getName());
		setActionDefinitionId("org.eclipse.tm.terminal.paste"); //$NON-NLS-1$
		ISharedImages si = PlatformUI.getWorkbench().getSharedImages();
		setupAction(ActionMessages.PASTE, ActionMessages.PASTE, si.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE),
				si.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE),
				si.getImageDescriptor(ISharedImages.IMG_TOOL_PASTE_DISABLED), false);
	}

	@Override
	public void run() {
		ITerminalViewControl target = getTarget();
		if (target != null) {
			target.paste();
		}
	}

	@Override
	public void updateAction(boolean aboutToShow) {
		ITerminalViewControl target = getTarget();
		boolean bEnabled = false;
		if (target != null) {
			if (target.getState() == TerminalState.CONNECTED) {
				if (target.getClipboard() != null && !target.getClipboard().isDisposed()) {
					String strText = (String) target.getClipboard().getContents(TextTransfer.getInstance());
					if (strText != null && !strText.isEmpty()) {
						bEnabled = true;
					}
				}
			}
		}
		setEnabled(bEnabled);
	}
}
