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

public class TerminalActionSelectAll extends TerminalAction
{
    protected TerminalActionSelectAll(TerminalTarget target)
    {
        super(target,
              ON_EDIT_SELECTALL,
              TerminalActionSelectAll.class.getName());

        setupAction(TERMINAL_TEXT_SELECTALL,
                    TERMINAL_TEXT_SELECTALL,
                    null,
                    null,
                    null,
                    false);
    }
}
