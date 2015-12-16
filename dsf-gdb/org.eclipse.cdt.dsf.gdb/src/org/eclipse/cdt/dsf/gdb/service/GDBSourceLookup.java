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
package org.eclipse.cdt.dsf.gdb.service;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbSourceLookupDirector;
import org.eclipse.cdt.dsf.mi.service.CSourceLookup;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;

/**
 * Default implementation of {@link IGDBSourceLookup}
 * 
 * @since 5.0
 */
public class GDBSourceLookup extends CSourceLookup implements IGDBSourceLookup {

	private ICommandControl fCommand;
	private CommandFactory fCommandFactory;
	private Map<ISourceLookupDMContext, CSourceLookupDirector> fDirectors = new HashMap<>();

	public GDBSourceLookup(DsfSession session) {
		super(session);
	}

	@Override
	public void initialize(final RequestMonitor rm) {
		super.initialize(new RequestMonitor(getExecutor(), rm) {
			@Override
			protected void handleSuccess() {
				doInitialize(rm);
			}
		});
	}

	private void doInitialize(RequestMonitor rm) {
		fCommand = getServicesTracker().getService(ICommandControl.class);
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		register(new String[] { IGDBSourceLookup.class.getName(), GDBSourceLookup.class.getName() },
				new Hashtable<String, String>());

		rm.done();
	}

	@Override
	public void shutdown(final RequestMonitor rm) {
		unregister();
		super.shutdown(rm);
	}

	@Override
	public void setSourceLookupDirector(ISourceLookupDMContext ctx, CSourceLookupDirector director) {
		fDirectors.put(ctx, director);
		super.setSourceLookupDirector(ctx, director);
	}

	@Override
	public void initializeSourceSubstitutions(final ISourceLookupDMContext sourceLookupCtx, final RequestMonitor rm) {
		if (!fDirectors.containsKey(sourceLookupCtx)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE,
					"No source director configured for given context", null)); //$NON-NLS-1$ );
			rm.done();
			return;
		}

		CSourceLookupDirector director = fDirectors.get(sourceLookupCtx);
		if (director instanceof GdbSourceLookupDirector) {
			final Map<String, String> entries = ((GdbSourceLookupDirector) director).getSubstitutionsPaths();
			setSubstitutePaths(sourceLookupCtx, entries, rm);
		}

	}

	@Override
	public void sourceContainersChanged(ISourceLookupDirector director, RequestMonitor rm) {
		if (!fDirectors.containsValue(director)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE,
					"No source director configured for given context", null)); //$NON-NLS-1$ );
			rm.done();
			return;
		}

		for (Entry<ISourceLookupDMContext, CSourceLookupDirector> entry : fDirectors.entrySet()) {
			if (entry.getValue().equals(director)) {
				sourceContainersChanged(entry.getKey(), rm);
			}
		}

	}

	@Override
	public void sourceContainersChanged(final ISourceLookupDMContext sourceLookupCtx, final RequestMonitor rm) {
		fCommand.queueCommand(fCommandFactory.createCLIUnsetSubstitutePath(sourceLookupCtx),
				new DataRequestMonitor<MIInfo>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						initializeSourceSubstitutions(sourceLookupCtx, rm);
					}
				});
	}

	protected void setSubstitutePaths(ISourceLookupDMContext sourceLookupCtx, Map<String, String> entries,
			RequestMonitor rm) {
		CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm);
		countingRm.setDoneCount(entries.size());
		for (Map.Entry<String, String> entry : entries.entrySet()) {
			fCommand.queueCommand(
					fCommandFactory.createMISetSubstitutePath(sourceLookupCtx, entry.getKey(), entry.getValue()),
					new DataRequestMonitor<MIInfo>(getExecutor(), countingRm));
		}
	}

}
