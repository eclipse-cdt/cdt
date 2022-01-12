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
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.control.actions;

import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class TerminalActionCopy extends AbstractTerminalAction {
	public TerminalActionCopy() {
		super(TerminalActionCopy.class.getName());
		setActionDefinitionId("org.eclipse.tm.terminal.copy"); //$NON-NLS-1$
		ISharedImages si = PlatformUI.getWorkbench().getSharedImages();
		setupAction(ActionMessages.COPY, ActionMessages.COPY, si.getImageDescriptor(ISharedImages.IMG_TOOL_COPY),
				si.getImageDescriptor(ISharedImages.IMG_TOOL_COPY),
				si.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED), true);
	}

	public TerminalActionCopy(ITerminalViewControl target) {
		super(target, TerminalActionCopy.class.getName());
		setActionDefinitionId("org.eclipse.tm.terminal.copy"); //$NON-NLS-1$
		ISharedImages si = PlatformUI.getWorkbench().getSharedImages();
		setupAction(ActionMessages.COPY, ActionMessages.COPY, si.getImageDescriptor(ISharedImages.IMG_TOOL_COPY),
				si.getImageDescriptor(ISharedImages.IMG_TOOL_COPY),
				si.getImageDescriptor(ISharedImages.IMG_TOOL_COPY_DISABLED), true);
	}

	@Override
	public void run() {
		ITerminalViewControl target = getTarget();
		if (target != null) {
			String selection = target.getSelection();

			if (!selection.equals("")) {//$NON-NLS-1$
				target.copy();
			} else {
				target.sendKey('\u0003');
			}
		}
	}

	@Override
	public void updateAction(boolean aboutToShow) {
		ITerminalViewControl target = getTarget();
		if (aboutToShow && target != null) {
			setEnabled(!target.getSelection().isEmpty());
		} else {
			setEnabled(false);
		}
	}
}
