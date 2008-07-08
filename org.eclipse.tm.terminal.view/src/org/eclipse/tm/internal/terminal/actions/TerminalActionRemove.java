/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: 
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.actions;

import org.eclipse.tm.internal.terminal.view.ITerminalViewConnectionManager;
import org.eclipse.tm.internal.terminal.view.ImageConsts;
import org.eclipse.tm.internal.terminal.view.ITerminalViewConnectionManager.ITerminalViewConnectionListener;

public class TerminalActionRemove extends TerminalAction implements ITerminalViewConnectionListener 
{
	private final ITerminalViewConnectionManager fConnectionManager;
    public TerminalActionRemove(ITerminalViewConnectionManager target)
    {
        super(null,
              TerminalActionRemove.class.getName());
        fConnectionManager=target;
        setupAction(ActionMessages.REMOVE,
                    ActionMessages.REMOVE,
                    null,
                    ImageConsts.IMAGE_ELCL_REMOVE,
                    ImageConsts.IMAGE_DLCL_REMOVE,
                    true);
		fConnectionManager.addListener(this);
		connectionsChanged();
    }
	public void run() {
		fConnectionManager.removeActive();
	}
	public void connectionsChanged() {
		setEnabled(fConnectionManager.size()>1);
	}
}
