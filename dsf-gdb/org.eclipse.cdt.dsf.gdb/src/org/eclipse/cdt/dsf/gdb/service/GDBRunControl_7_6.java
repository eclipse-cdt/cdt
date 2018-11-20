/*******************************************************************************
 * Copyright (c) 2013 Ericsson and others.
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

package org.eclipse.cdt.dsf.gdb.service;

import java.util.Hashtable;

import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IRunControl2;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.debug.service.command.IEventListener;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIRunControl;
import org.eclipse.cdt.dsf.mi.service.MIRunControl;
import org.eclipse.cdt.dsf.mi.service.command.output.MINotifyAsyncOutput;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOOBRecord;
import org.eclipse.cdt.dsf.mi.service.command.output.MIOutput;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * @since 4.2
 */
public class GDBRunControl_7_6 extends GDBRunControl_7_0 implements IEventListener {

	private ICommandControl fCommandControl;

	public GDBRunControl_7_6(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			public void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	private void doInitialize(final RequestMonitor requestMonitor) {

		fCommandControl = getServicesTracker().getService(ICommandControl.class);

		if (fCommandControl == null) {
			requestMonitor.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, "Service is not available")); //$NON-NLS-1$
			return;
		}

		fCommandControl.addEventListener(this);

		register(
				new String[] { IRunControl.class.getName(), IRunControl2.class.getName(), IMIRunControl.class.getName(),
						MIRunControl.class.getName(), GDBRunControl_7_0.class.getName(),
						GDBRunControl_7_6.class.getName(), IReverseRunControl.class.getName() },
				new Hashtable<String, String>());
		requestMonitor.done();
	}

	@Override
	public void shutdown(final RequestMonitor requestMonitor) {
		if (fCommandControl != null) {
			fCommandControl.removeEventListener(this);
		}
		unregister();
		super.shutdown(requestMonitor);
	}

	@Override
	public void eventReceived(Object output) {
		if (output instanceof MIOutput) {
			MIOOBRecord[] records = ((MIOutput) output).getMIOOBRecords();
			for (MIOOBRecord r : records) {
				if (r instanceof MINotifyAsyncOutput) {
					MINotifyAsyncOutput notifyOutput = (MINotifyAsyncOutput) r;
					String asyncClass = notifyOutput.getAsyncClass();
					// These events have been added with GDB 7.6
					if ("record-started".equals(asyncClass) || //$NON-NLS-1$
							"record-stopped".equals(asyncClass)) { //$NON-NLS-1$

						boolean enable = "record-started".equals(asyncClass); //$NON-NLS-1$
						setReverseModeEnabled(enable);
					}
				}
			}
		}
	}
}
