/*******************************************************************************
 * Copyright (c) 2009, 2011 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.mi.service.breakpoint.actions;

import org.eclipse.cdt.debug.core.breakpointactions.IResumeActionEnabler;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;

/**
 * @since 3.0
 */
public class MIResumeActionEnabler implements IResumeActionEnabler {

	private final DsfExecutor fExecutor;
	private final DsfServicesTracker fServiceTracker;
	private final IExecutionDMContext fContext;

	public MIResumeActionEnabler(DsfExecutor executor, DsfServicesTracker serviceTracker, IDMContext context) {
		fExecutor = executor;
		fServiceTracker = serviceTracker;
		fContext = (IExecutionDMContext) context;
	}

	@Override
	public void resume() throws Exception {
		fExecutor.execute(new DsfRunnable() {
			@Override
			public void run() {
				final IRunControl runControlService = fServiceTracker.getService(IRunControl.class);
				if (runControlService != null) {
					runControlService.resume(fContext, new RequestMonitor(fExecutor, null));
				}
			}
		});
	}
}
