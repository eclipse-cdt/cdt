/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
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
package org.eclipse.cdt.examples.dsf.gdb.launch;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.ServicesLaunchSequence;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.examples.dsf.gdb.service.IGDBExtendedFunctions;
import org.eclipse.core.runtime.IProgressMonitor;

public class GdbExtendedServicesLaunchSequence extends ServicesLaunchSequence {

	private GdbLaunch fLaunch;

	public GdbExtendedServicesLaunchSequence(DsfSession session, GdbLaunch launch, IProgressMonitor pm) {
		super(session, launch, pm);
		fLaunch = launch;
	}

	@Override
	public Step[] getSteps() {
		// Add an extra step at the end to create the new service
		Step[] steps = super.getSteps();
		Step[] moreSteps = new Step[steps.length + 1];
		System.arraycopy(steps, 0, moreSteps, 0, steps.length);
		moreSteps[steps.length] = new Step() {
			@Override
			public void execute(RequestMonitor requestMonitor) {
				fLaunch.getServiceFactory().createService(IGDBExtendedFunctions.class, fLaunch.getSession())
						.initialize(requestMonitor);
			}
		};
		return moreSteps;
	}
}
