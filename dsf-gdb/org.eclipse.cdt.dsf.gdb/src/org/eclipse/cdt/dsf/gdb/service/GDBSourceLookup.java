/*******************************************************************************
 * Copyright (c) 2015, 2018 Kichwa Coders and others.
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

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
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
import org.eclipse.cdt.dsf.datamodel.AbstractDMEvent;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.ICachingService;
import org.eclipse.cdt.dsf.debug.service.command.CommandCache;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbSourceLookupDirector;
import org.eclipse.cdt.dsf.mi.service.CSourceLookup;
import org.eclipse.cdt.dsf.mi.service.IMICommandControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MiSourceFilesInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MiSourceFilesInfo.SourceFileInfo;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Default implementation of {@link IGDBSourceLookup}
 *
 * @since 5.0
 */
public class GDBSourceLookup extends CSourceLookup implements IGDBSourceLookup, IDebugSourceFiles, ICachingService {

	private static class DebugSourceFilesChangedEvent extends AbstractDMEvent<IDMContext>
			implements IDebugSourceFilesChangedEvent {
		public DebugSourceFilesChangedEvent(IDMContext context) {
			super(context);
		}
	}

	private ICommandControlService fCommand;
	private CommandFactory fCommandFactory;
	private Map<ISourceLookupDMContext, CSourceLookupDirector> fDirectors = new HashMap<>();
	/**
	 * The current set of path substitutions that have been set on GDB.
	 */
	private Map<String, String> fCachedEntries = Collections.emptyMap();

	private CommandCache fDebugSourceFilesCache;

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
		fCommand = getServicesTracker().getService(ICommandControlService.class);
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();

		fDebugSourceFilesCache = new CommandCache(getSession(), fCommand);
		fDebugSourceFilesCache.setContextAvailable(fCommand.getContext(), true);

		register(new String[] { IGDBSourceLookup.class.getName(), GDBSourceLookup.class.getName(),
				IDebugSourceFiles.class.getName() }, new Hashtable<String, String>());
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
			 * Issue the clear and set commands back to back so that the executor thread
			 * atomically changes the source lookup settings. Any commands to GDB issued
			 * after this call will get the new source substitute settings.
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
			protected void handleCompleted() {
				// Reset the list of source files when source path substitutions change
				fDebugSourceFilesCache.reset();
				getSession().dispatchEvent(new DebugSourceFilesChangedEvent(sourceLookupCtx), getProperties());
				if (!isSuccess()) {
					/*
					 * We failed to apply the changes. Clear the cache as it does not represent the
					 * state of the backend. However we don't have a good recovery here, so on
					 * future sourceContainersChanged() calls we will simply reissue the
					 * substitutions.
					 */
					fCachedEntries = null;
				}
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

	private static final class DebugSourceFileInfo implements IDebugSourceFileInfo {
		private final SourceFileInfo miInfo;

		private DebugSourceFileInfo(SourceFileInfo miInfo) {
			if (miInfo == null)
				throw new IllegalArgumentException("The SourceFileInfo provided is null"); //$NON-NLS-1$
			this.miInfo = miInfo;
		}

		@Override
		public String getName() {
			// we get the file name without the path
			String name = miInfo != null ? miInfo.getFile() : null;
			if (name == null)
				return name;
			try {
				Path p = Paths.get(name);
				name = p.getFileName() != null ? p.getFileName().toString() : ""; //$NON-NLS-1$
			} catch (InvalidPathException e) {
				// do nothing
			}
			return name;
		}

		@Override
		public String getPath() {
			// we get the file name without the path
			String path = miInfo != null ? miInfo.getFullName() : null;
			if (path == null)
				return path;
			try {
				Path p = Paths.get(path);
				path = p.toString();
			} catch (InvalidPathException e) {
				// do nothing
			}
			return path;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((miInfo == null) ? 0 : miInfo.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			DebugSourceFileInfo other = (DebugSourceFileInfo) obj;
			if (miInfo == null) {
				if (other.miInfo != null)
					return false;
			} else if (!miInfo.equals(other.miInfo))
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "DebugSourceFileInfo [miInfo=" + miInfo + "]"; //$NON-NLS-1$ //$NON-NLS-2$
		}

	}

	@Override
	public void getSources(final IDMContext dmc, final DataRequestMonitor<IDebugSourceFileInfo[]> rm) {
		fDebugSourceFilesCache.execute(fCommandFactory.createMiFileListExecSourceFiles(dmc),
				new DataRequestMonitor<MiSourceFilesInfo>(getExecutor(), rm) {
					@Override
					protected void handleSuccess() {
						IDebugSourceFileInfo[] result = null;
						MiSourceFilesInfo sourceFiles = getData();
						SourceFileInfo[] info = sourceFiles.getSourceFiles();
						result = Arrays.asList(info).stream().map(DebugSourceFileInfo::new)
								.toArray(IDebugSourceFileInfo[]::new);
						rm.setData(result);
						rm.done();
					}
				});
	}

	@Override
	public void flushCache(IDMContext context) {
		fDebugSourceFilesCache.reset();
		getSession().dispatchEvent(new DebugSourceFilesChangedEvent(fCommand.getContext()), getProperties());
	}
}
