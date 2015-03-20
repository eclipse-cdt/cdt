/*******************************************************************************
 * Copyright (c) 2015 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbSessionAdapters;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMAdapter;
import org.eclipse.cdt.examples.dsf.gdb.viewmodel.GdbExtendedViewModelAdapter;
import org.eclipse.debug.core.ILaunch;

@SuppressWarnings("restriction")
public class GdbExtendedSessionAdapters extends GdbSessionAdapters {
    
    public GdbExtendedSessionAdapters(ILaunch launch, DsfSession session) {
	super(launch, session);
    }
    
    @Override
    protected IVMAdapter createViewModelAdapter(DsfSession session, SteppingController controller) {
        return new GdbExtendedViewModelAdapter(session, controller);
    }
}
