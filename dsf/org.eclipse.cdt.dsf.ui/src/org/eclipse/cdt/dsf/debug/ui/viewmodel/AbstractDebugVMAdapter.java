/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.SteppingController.ISteppingControlParticipant;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMAdapter;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * Base class for VM adapters used for implementing a debugger integration.
 *
 * @since 1.1
 */
public class AbstractDebugVMAdapter extends AbstractDMVMAdapter implements ISteppingControlParticipant {

	public AbstractDebugVMAdapter(DsfSession session, final SteppingController controller) {
		super(session);
		fController = controller;
		try {
			fController.getExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					fController.addSteppingControlParticipant(AbstractDebugVMAdapter.this);
				}
			});
		} catch (RejectedExecutionException e) {
		} // Do nothing if session is shut down.
	}

	private final SteppingController fController;

	@Override
	protected IVMProvider createViewModelProvider(IPresentationContext context) {
		return null;
	}

	@Override
	public void doneHandleEvent(Object event) {
		if (event instanceof IRunControl.ISuspendedDMEvent) {
			final ISuspendedDMEvent suspendedEvent = (IRunControl.ISuspendedDMEvent) event;
			fController.getExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					fController.doneStepping(suspendedEvent.getDMContext(), AbstractDebugVMAdapter.this);
				}
			});
		}
	}

	@Override
	public void dispose() {
		try {
			fController.getExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					fController.removeSteppingControlParticipant(AbstractDebugVMAdapter.this);
				}
			});
		} catch (RejectedExecutionException e) {
		} // Do nothing if session is shut down.
		super.dispose();
	}
}
