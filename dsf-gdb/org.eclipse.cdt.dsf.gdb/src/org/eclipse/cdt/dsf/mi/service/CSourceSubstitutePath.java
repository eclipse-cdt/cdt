/*******************************************************************************
 * Copyright (c) 2015 Kichwa Coders and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation to Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service;

import java.util.Hashtable;

import org.eclipse.cdt.debug.core.sourcelookup.ISourceSubstitutePathContainer;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.ISourceSubstitutePath;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.osgi.framework.BundleContext;

/**
 * @since 4.8
 */
public class CSourceSubstitutePath extends AbstractDsfService implements ISourceSubstitutePath {

	private ICommandControl fCommand;
	private CommandFactory fCommandFactory;

	public CSourceSubstitutePath(DsfSession session) {
		super(session);
	}

	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new ImmediateRequestMonitor(rm) {
			@Override
			protected void handleSuccess() {
				doInitialize(rm);
			}
		});
	}

	private void doInitialize(final RequestMonitor rm) {
		fCommand = getServicesTracker().getService(ICommandControl.class);
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		register(new String[] { CSourceSubstitutePath.class.getName(), ISourceSubstitutePath.class.getName() },
				new Hashtable<String, String>());
		rm.done();
	}

	@Override
	public void setSourceSubstitutePath(ISourceSubstituteDMContext context, ISourceContainer[] containers,
			RequestMonitor rm) {

		if (containers.length == 0) {
			rm.done();
			return;
		}

		CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm);

		for (ISourceContainer container : containers) {

			if (container instanceof ISourceSubstitutePathContainer) {
				ISourceSubstitutePathContainer sourceSubContainer = (ISourceSubstitutePathContainer) container;

				IPath fromPath = sourceSubContainer.getBackendPath();
				IPath toPath = sourceSubContainer.getLocalPath();

				// On Windows we must use forward slashes to make GDB happy, so
				// don't use toOSString.
				String from = fromPath.toString();
				String to = toPath.toString();

				fCommand.queueCommand(fCommandFactory.createMISetSubstitutePath(context, from, to),
						new DataRequestMonitor<MIInfo>(getExecutor(), countingRm));
			} else {
				countingRm.done();
			}

			if (container.isComposite()) {
				ISourceContainer[] childContainers;
				try {
					childContainers = container.getSourceContainers();
				} catch (CoreException e) {
					// Consistent with other uses of getSourceContainers, we
					// silently ignore these children.
					childContainers = new ISourceContainer[0];
				}
				setSourceSubstitutePath(context, childContainers, countingRm);
			} else {
				countingRm.done();
			}
		}

		countingRm.setDoneCount(containers.length * 2);
	}
}
