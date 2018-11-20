/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb;

import java.util.List;

import org.eclipse.cdt.dsf.gdb.internal.ui.GdbSessionAdapters;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.gdb.actions.DsfExtendedTerminateCommand;
import org.eclipse.cdt.examples.dsf.gdb.actions.GdbShowVersionHandler;
import org.eclipse.cdt.examples.dsf.gdb.commands.IShowVersionHandler;
import org.eclipse.cdt.examples.dsf.gdb.viewmodel.GdbExtendedViewModelAdapter;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerInputProvider;

@SuppressWarnings("restriction")
public class GdbExtendedSessionAdapters extends GdbSessionAdapters {

	public GdbExtendedSessionAdapters(ILaunch launch, DsfSession session, Class<?>[] launchAdapterTypes) {
		super(launch, session, launchAdapterTypes);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T> T createModelAdapter(Class<T> adapterType, ILaunch launch, DsfSession session) {
		if (ITerminateHandler.class.equals(adapterType)) {
			return (T) new DsfExtendedTerminateCommand(session);
		}
		if (IViewerInputProvider.class.equals(adapterType)) {
			return (T) new GdbExtendedViewModelAdapter(session, getSteppingController());
		}
		if (IShowVersionHandler.class.equals(adapterType)) {
			return (T) new GdbShowVersionHandler(session);
		}

		return super.createModelAdapter(adapterType, launch, session);
	}

	@Override
	protected List<Class<?>> getModelAdapters() {
		List<Class<?>> modelAdapters = super.getModelAdapters();
		modelAdapters.add(IShowVersionHandler.class);
		return modelAdapters;
	}
}
