/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui;

import org.eclipse.cdt.dsf.debug.ui.AbstractDsfDebugTextHover;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunchDelegate;

/**
 * Debug editor text hover for GDB.
 * 
 * @since 2.1
 */
public class GdbDebugTextHover extends AbstractDsfDebugTextHover {

    @Override
    protected String getModelId() {
        return GdbLaunchDelegate.GDB_DEBUG_MODEL_ID;
    }

}
