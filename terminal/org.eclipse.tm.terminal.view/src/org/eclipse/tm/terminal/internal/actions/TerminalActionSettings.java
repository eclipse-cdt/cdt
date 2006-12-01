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

public class TerminalActionSettings extends TerminalAction
{
    public TerminalActionSettings(ITerminalView target)
    {
        super(target,
              TerminalActionSettings.class.getName());

        setupAction(ActionMessages.SETTINGS_ELLIPSE,
                    ActionMessages.SETTINGS,
                    ImageConsts.IMAGE_CLCL_SETTINGS,
                    ImageConsts.IMAGE_ELCL_SETTINGS,
                    ImageConsts.IMAGE_DLCL_SETTINGS,
                    true);
    }
	public void run() {
		fTarget.onTerminalSettings();
	}
}
