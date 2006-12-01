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

public class TerminalActionDisconnect extends TerminalAction
{
    /**
     * 
     */ 
    public TerminalActionDisconnect(ITerminalView target)
    {
        super(target,
              TerminalActionDisconnect.class.getName());

        setupAction(ActionMessages.DISCONNECT,
                    ActionMessages.DISCONNECT,
                    ImageConsts.IMAGE_CLCL_DISCONNECT,
                    ImageConsts.IMAGE_ELCL_DISCONNECT,
                    ImageConsts.IMAGE_DLCL_DISCONNECT,
                    false);
    }
	public void run() {
		fTarget.onTerminalDisconnect();
	}
}
