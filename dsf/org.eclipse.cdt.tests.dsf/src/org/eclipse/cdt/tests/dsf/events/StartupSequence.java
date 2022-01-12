/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
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
package org.eclipse.cdt.tests.dsf.events;

import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.service.DsfSession;

class StartupSequence extends Sequence {
	DsfSession fSession;

	StartupSequence(DsfSession session) {
		super(session.getExecutor());
		fSession = session;
	}

	@Override
	public Step[] getSteps() {
		return fSteps;
	}

	final Step[] fSteps = new Step[] { new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			new Service1(fSession).initialize(requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			new Service2(fSession).initialize(requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			new Service3(fSession).initialize(requestMonitor);
		}
	}, new Step() {
		@Override
		public void execute(RequestMonitor requestMonitor) {
			new Service4(fSession).initialize(requestMonitor);
		}
	} };
}
