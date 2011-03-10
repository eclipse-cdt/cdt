/*******************************************************************************
 * Copyright (c) 2011 Texas Instruments, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Dobrin Alexiev (Texas Instruments) - initial API and implementation (bug 336876)
********************************************************************************/
package org.eclipse.cdt.debug.internal.ui.commands;

import org.eclipse.cdt.debug.core.model.IUngroupDebugContextsHandler;
import org.eclipse.debug.ui.actions.DebugCommandHandler;

/**
 * Command handler to trigger ungrouping of debug contexts operation.
 * 
 * @since 7.1 
 */
public class UngroupDebugContextsCommandHandler extends DebugCommandHandler {
	
    @Override
    protected Class<?> getCommandType() {
        return IUngroupDebugContextsHandler.class;
    }
}
