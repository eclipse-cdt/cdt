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

package org.eclipse.tm.terminal;

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
    protected TerminalActionNewTerminal(TerminalTarget target)
    {
        super(target, ON_TERMINAL_NEW_TERMINAL, TerminalActionNewTerminal.class.getName());

        setupAction(TERMINAL_TEXT_NEW_TERMINAL,
                    TERMINAL_TEXT_NEW_TERMINAL,
                    TERMINAL_IMAGE_NEW_TERMINAL,
                    TERMINAL_IMAGE_NEW_TERMINAL,
                    TERMINAL_IMAGE_NEW_TERMINAL,
                    true);
    }
}
