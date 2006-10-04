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

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.internal.WorkbenchImages;

public class TerminalActionCut extends TerminalAction
{
    protected TerminalActionCut(TerminalTarget target)
    {
        super(target,
              ON_EDIT_CUT,
              TerminalActionCut.class.getName());

        ImageRegistry imageRegistry;
        
        imageRegistry = WorkbenchImages.getImageRegistry();
        setupAction(TERMINAL_TEXT_CUT,
                    TERMINAL_TEXT_CUT,
                    ISharedImages.IMG_TOOL_CUT,
                    ISharedImages.IMG_TOOL_CUT,
                    ISharedImages.IMG_TOOL_CUT_DISABLED,
                    true,
                    imageRegistry);
    }
}
