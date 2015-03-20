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

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbSessionAdapters;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.gdb.actions.DsfExtendedTerminateCommand;
import org.eclipse.cdt.examples.dsf.gdb.viewmodel.GdbExtendedViewModelAdapter;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputProvider;

@SuppressWarnings("restriction")
public class GdbExtendedSessionAdapters extends GdbSessionAdapters {
    
	public GdbExtendedSessionAdapters(ILaunch launch, DsfSession session) {
		super(launch, session);
	}
    
	@Override
	protected Object createModelAdapter(Class<?> adapterType, ILaunch launch, DsfSession session) {
		if (ITerminateHandler.class.equals(adapterType)) { 
			return new DsfExtendedTerminateCommand(session);
		}
		if (IViewerInputProvider.class.equals(adapterType)) {
			return new GdbExtendedViewModelAdapter(session, getSteppingController());
		}

		return super.createModelAdapter(adapterType, launch, session);
	}
}
