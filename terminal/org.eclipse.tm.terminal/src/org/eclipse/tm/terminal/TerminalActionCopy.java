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

public class TerminalActionCopy extends TerminalAction
{
    protected TerminalActionCopy(TerminalTarget target)
    {
        super(target,
              ON_EDIT_COPY,
              TerminalActionCopy.class.getName());

        ImageRegistry imageRegistry;
        
        imageRegistry = WorkbenchImages.getImageRegistry();
        setupAction(TERMINAL_TEXT_COPY,
                    TERMINAL_TEXT_COPY,
                    ISharedImages.IMG_TOOL_COPY,
                    ISharedImages.IMG_TOOL_COPY,
                    ISharedImages.IMG_TOOL_COPY_DISABLED,
                    true,
                    imageRegistry);
    }
}
