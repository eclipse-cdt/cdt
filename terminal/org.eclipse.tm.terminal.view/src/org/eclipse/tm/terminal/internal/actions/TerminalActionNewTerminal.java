/*******************************************************************************
 * Copyright (c) 2005, 2006 Wind River Systems, Inc. and others.
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
package org.eclipse.tm.terminal.internal.actions;

import org.eclipse.tm.terminal.internal.view.ITerminalView;
import org.eclipse.tm.terminal.internal.view.ImageConsts;

/**
 * UNDER CONSTRUCTION 
 *
 * @author Fran Litterio <francis.litterio@windriver.com>
 */
public class TerminalActionNewTerminal extends TerminalAction
{
    /**
     * UNDER CONSTRUCTION 
     */ 
    public TerminalActionNewTerminal(ITerminalView target)
    {
        super(target, TerminalActionNewTerminal.class.getName());

        setupAction(ActionMessages.NEW_TERMINAL,
                    ActionMessages.NEW_TERMINAL,
                    ImageConsts.IMAGE_NEW_TERMINAL,
                    ImageConsts.IMAGE_NEW_TERMINAL,
                    ImageConsts.IMAGE_NEW_TERMINAL,
                    true);
    }
	public void run() {
		fTarget.onTerminalNewTerminal();
	}
}
