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

public class TerminalActionConnect extends TerminalAction
{
    protected TerminalActionConnect(TerminalTarget target)
    {
        super(target,
              ON_TERMINAL_CONNECT,
              TerminalActionConnect.class.getName());

        setupAction(TERMINAL_TEXT_CONNECT,
                    TERMINAL_TEXT_CONNECT,
                    TERMINAL_IMAGE_CLCL_CONNECT,
                    TERMINAL_IMAGE_ELCL_CONNECT,
                    TERMINAL_IMAGE_DLCL_CONNECT,
                    true);
    }
}
