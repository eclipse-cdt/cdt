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

public class TerminalActionDisconnect extends TerminalAction
{
    /**
     * 
     */ 
    protected TerminalActionDisconnect(TerminalTarget target)
    {
        super(target,
              ON_TERMINAL_DISCONNECT,
              TerminalActionDisconnect.class.getName());

        setupAction(TERMINAL_TEXT_DISCONNECT,
                    TERMINAL_TEXT_DISCONNECT,
                    TERMINAL_IMAGE_CLCL_DISCONNECT,
                    TERMINAL_IMAGE_ELCL_DISCONNECT,
                    TERMINAL_IMAGE_DLCL_DISCONNECT,
                    false);
    }
}
