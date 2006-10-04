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

public class TerminalActionPaste extends TerminalAction
{
    protected TerminalActionPaste(TerminalTarget target)
    {
        super(target,
              ON_EDIT_PASTE,
              TerminalActionPaste.class.getName());

        ImageRegistry imageRegistry;
        
        imageRegistry = WorkbenchImages.getImageRegistry();
        setupAction(TERMINAL_TEXT_PASTE,
                    TERMINAL_TEXT_PASTE,
                    ISharedImages.IMG_TOOL_PASTE,
                    ISharedImages.IMG_TOOL_PASTE_DISABLED,
                    ISharedImages.IMG_TOOL_PASTE,
                    false,
                    imageRegistry);
    }
}
