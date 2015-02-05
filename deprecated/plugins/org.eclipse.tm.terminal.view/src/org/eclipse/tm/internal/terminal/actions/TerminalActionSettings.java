/*******************************************************************************
 * Copyright (c) 2004, 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following Wind River employees contributed to the Terminal component
 * that contains this file: Chris Thew, Fran Litterio, Stephen Lamb,
 * Helmut Haigermoser and Ted Williams.
 *
 * Contributors:
 * Michael Scharf (Wind River) - split into core, view and connector plugins 
 * Martin Oberhuber (Wind River) - fixed copyright headers and beautified
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.actions;

import org.eclipse.tm.internal.terminal.view.ITerminalView;
import org.eclipse.tm.internal.terminal.view.ImageConsts;

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
