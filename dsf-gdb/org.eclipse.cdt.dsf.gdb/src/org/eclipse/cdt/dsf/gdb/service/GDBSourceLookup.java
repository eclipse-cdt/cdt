/*******************************************************************************
 * Copyright (c) 2015, 2016 Kichwa Coders and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jonah Graham (Kichwa Coders) - initial API and implementation to Add support for gdb's "set substitute-path" (Bug 472765)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
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

/**
 * Default implementation of {@link IGDBSourceLookup}
 *
 * @since 5.0
 */
public class GDBSourceLookup extends CSourceLookup implements IGDBSourceLookup {

	private ICommandControl fCommand;
	private CommandFactory fCommandFactory;
	private Map<ISourceLookupDMContext, CSourceLookupDirector> fDirectors = new HashMap<>();
	/**
	 * The current set of path substitutions that have been set on GDB.
	 */
	private Map<String, String> fCachedEntries = Collections.emptyMap();

	public GDBSourceLookup(DsfSession session) {
		super(session);
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
		setSubstitutePaths(sourceLookupCtx, getSubstitutionsPaths(sourceLookupCtx), rm);
	}

	private Map<String, String> getSubstitutionsPaths(ISourceLookupDMContext sourceLookupCtx) {
		CSourceLookupDirector director = fDirectors.get(sourceLookupCtx);
		if (director instanceof GdbSourceLookupDirector) {
			return ((GdbSourceLookupDirector) director).getSubstitutionsPaths();
		}
		return Collections.emptyMap();
	}

	@Override
	public void sourceContainersChanged(final ISourceLookupDMContext sourceLookupCtx,
			final DataRequestMonitor<Boolean> rm) {
		if (!fDirectors.containsKey(sourceLookupCtx)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE,
					"No source director configured for given context", null)); //$NON-NLS-1$ );
			rm.done();
			return;
		}

		Map<String, String> entries = getSubstitutionsPaths(sourceLookupCtx);
		if (entries.equals(fCachedEntries)) {
			rm.done(false);
		} else {
			/*
			 * Issue the clear and set commands back to back so that the
			 * executor thread atomically changes the source lookup settings.
			 * Any commands to GDB issued after this call will get the new
			 * source substitute settings.
			 */
			CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm) {
				@Override
				protected void handleSuccess() {
					rm.done(true);
				}
			};
			fCommand.queueCommand(fCommandFactory.createCLIUnsetSubstitutePath(sourceLookupCtx),
					new DataRequestMonitor<MIInfo>(getExecutor(), countingRm));
			initializeSourceSubstitutions(sourceLookupCtx, new RequestMonitor(getExecutor(), countingRm));
			countingRm.setDoneCount(2);
		}
	}

	protected void setSubstitutePaths(ISourceLookupDMContext sourceLookupCtx, Map<String, String> entries,
			RequestMonitor rm) {
		fCachedEntries = entries;
		CountingRequestMonitor countingRm = new CountingRequestMonitor(getExecutor(), rm) {
			@Override
			protected void handleFailure() {
				/*
				 * We failed to apply the changes. Clear the cache as it does
				 * not represent the state of the backend. However we don't have
				 * a good recovery here, so on future sourceContainersChanged()
				 * calls we will simply reissue the substitutions.
				 */
				fCachedEntries = null;
				rm.done();
			}
		};
		countingRm.setDoneCount(entries.size());
		for (Map.Entry<String, String> entry : entries.entrySet()) {
			fCommand.queueCommand(
					fCommandFactory.createMISetSubstitutePath(sourceLookupCtx, entry.getKey(), entry.getValue()),
					new DataRequestMonitor<MIInfo>(getExecutor(), countingRm));
		}
	}

}
