/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems, Inc. - initial implementation
 *     
 *******************************************************************************/


package org.eclipse.tm.terminal.internal.actions;

import org.eclipse.tm.terminal.internal.view.ITerminalView;
import org.eclipse.tm.terminal.internal.view.ImageConsts;

public class TerminalActionConnect extends TerminalAction
{
    public TerminalActionConnect(ITerminalView target)
    {
        super(target,
              TerminalActionConnect.class.getName());

        setupAction(ActionMessages.CONNECT,
                    ActionMessages.CONNECT,
                    ImageConsts.IMAGE_CLCL_CONNECT,
                    ImageConsts.IMAGE_ELCL_CONNECT,
                    ImageConsts.IMAGE_DLCL_CONNECT,
                    true);
    }
	public void run() {
		fTarget.onTerminalConnect();
	}
}
