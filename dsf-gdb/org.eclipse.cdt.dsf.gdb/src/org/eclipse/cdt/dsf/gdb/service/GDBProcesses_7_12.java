/*******************************************************************************
 * Copyright (c) 2016 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Map;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * Adding support for async-on with all-stop for GDB 7.12 or higher
 *
 * @since 5.2
 */
public class GDBProcesses_7_12 extends GDBProcesses_7_10 {

    public GDBProcesses_7_12(DsfSession session) {
        super(session);
    }
    
    @Override
    protected Sequence getStartOrRestartProcessSequence(DsfExecutor executor, IContainerDMContext containerDmc,
            Map<String, Object> attributes, boolean restart,
            DataRequestMonitor<IContainerDMContext> rm) {
        return new StartOrRestartProcessSequence_7_12(executor, containerDmc, attributes, restart, rm);
    }
}

