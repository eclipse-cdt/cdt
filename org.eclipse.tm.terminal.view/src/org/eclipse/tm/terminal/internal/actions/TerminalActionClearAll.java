
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

public class TerminalActionClearAll extends TerminalAction
{
    public TerminalActionClearAll(ITerminalView target)
    {
        super(target,
              TerminalActionClearAll.class.getName());

        setupAction(ActionMessages.CLEARALL,
                    ActionMessages.CLEARALL,
                    null,
                    null,
                    null,
                    false);
    }

	public void run() {
		fTarget.onEditClearAll();
	}
}


