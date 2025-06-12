/*******************************************************************************
 * Copyright (c) 2025 John Dallaway and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     John Dallaway - Initial implementation (#1191)
 *******************************************************************************/

package org.eclipse.cdt.llvm.dsf.lldb.core.internal.service;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.gdb.service.GDBMemory;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.CLIAddressSizeInfo;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * LLDB memory service implementation
 */
public class LLDBMemory extends GDBMemory {

	private IGDBControl fCommandControl;

	public LLDBMemory(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor requestMonitor) {
		super.initialize(new ImmediateRequestMonitor(requestMonitor) {
			@Override
			protected void handleSuccess() {
				doInitialize(requestMonitor);
			}
		});
	}

	private void doInitialize(final RequestMonitor requestMonitor) {
		fCommandControl = getServicesTracker().getService(IGDBControl.class);
		requestMonitor.done();
	}

	@Override
	protected void readAddressSize(final IMemoryDMContext memContext, final DataRequestMonitor<Integer> drm) {
		// use a CLI command - LLDB-MI does not support expression evaluation until a process is started
		CommandFactory commandFactory = fCommandControl.getCommandFactory();
		fCommandControl.queueCommand(commandFactory.createCLIAddressSize(memContext),
				new DataRequestMonitor<CLIAddressSizeInfo>(ImmediateExecutor.getInstance(), drm) {
					@Override
					protected void handleSuccess() {
						Integer ptrBytes = getData().getAddressSize();
						drm.setData(ptrBytes * getAddressableSize(memContext));
						drm.done();
					}
				});
	}

	@Override
	protected void readEndianness(IMemoryDMContext memContext, final DataRequestMonitor<Boolean> drm) {
		// assume little-endian - LLDB-MI does not support the "show endian" CLI command
		drm.setData(Boolean.FALSE);
		drm.done();
	}

}
