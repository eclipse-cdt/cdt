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
 * Uwe Stieber (Wind River) - [260372] [terminal] Certain terminal actions are enabled if no target terminal control is available
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.control.actions;

import org.eclipse.tm.internal.terminal.control.ITerminalViewControl;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class TerminalActionCut extends AbstractTerminalAction {
	public TerminalActionCut() {
		super(TerminalActionCut.class.getName());
		ISharedImages si = PlatformUI.getWorkbench().getSharedImages();
		setupAction(ActionMessages.CUT, ActionMessages.CUT, si.getImageDescriptor(ISharedImages.IMG_TOOL_CUT),
				si.getImageDescriptor(ISharedImages.IMG_TOOL_CUT),
				si.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED), true);
	}

	public TerminalActionCut(ITerminalViewControl target) {
		super(target, TerminalActionCut.class.getName());
		ISharedImages si = PlatformUI.getWorkbench().getSharedImages();
		setupAction(ActionMessages.CUT, ActionMessages.CUT, si.getImageDescriptor(ISharedImages.IMG_TOOL_CUT),
				si.getImageDescriptor(ISharedImages.IMG_TOOL_CUT),
				si.getImageDescriptor(ISharedImages.IMG_TOOL_CUT_DISABLED), true);
	}

	@Override
	public void run() {
		ITerminalViewControl target = getTarget();
		if (target != null) {
			target.sendKey('\u0018');
		}
	}

	@Override
	public void updateAction(boolean aboutToShow) {
		// Cut is always disabled
		setEnabled(false);
	}
}
