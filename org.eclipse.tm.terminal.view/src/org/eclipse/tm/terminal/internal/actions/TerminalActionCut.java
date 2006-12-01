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

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.tm.terminal.internal.view.ITerminalView;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchImages;

public class TerminalActionCut extends TerminalAction
{
    public TerminalActionCut(ITerminalView target)
    {
        super(target,
              TerminalActionCut.class.getName());

        ImageRegistry imageRegistry;
        
        imageRegistry = WorkbenchImages.getImageRegistry();
        setupAction(ActionMessages.CUT,
                    ActionMessages.CUT,
                    ISharedImages.IMG_TOOL_CUT,
                    ISharedImages.IMG_TOOL_CUT,
                    ISharedImages.IMG_TOOL_CUT_DISABLED,
                    true,
                    imageRegistry);
    }
	public void run() {
		fTarget.onEditCut();
	}
}
