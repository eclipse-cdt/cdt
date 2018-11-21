/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems and others.
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
package org.eclipse.cdt.dsf.mi.service;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.sourcelookup.CProjectSourceContainer;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.ISourceLookup;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.DirectorySourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.osgi.framework.BundleContext;

/**
 * ISourceLookup service implementation based on the CDT CSourceLookupDirector.
 */
public class CSourceLookup extends AbstractDsfService implements ISourceLookup {
	private Map<ISourceLookupDMContext, CSourceLookupDirector> fDirectors = new HashMap<>();

	ICommandControl fConnection;
	private CommandFactory fCommandFactory;

	public CSourceLookup(DsfSession session) {
		super(session);
	}

	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	public void setSourceLookupDirector(ISourceLookupDMContext ctx, CSourceLookupDirector director) {
		fDirectors.put(ctx, director);
	}

	public void setSourceLookupPath(ISourceLookupDMContext ctx, ISourceContainer[] containers, RequestMonitor rm) {
		List<String> pathList = getSourceLookupPath(containers);
		String[] paths = pathList.toArray(new String[pathList.size()]);

		fConnection.queueCommand(fCommandFactory.createMIEnvironmentDirectory(ctx, paths, false),
				new DataRequestMonitor<MIInfo>(getExecutor(), rm));
	}

	private List<String> getSourceLookupPath(ISourceContainer[] containers) {
		ArrayList<String> list = new ArrayList<>(containers.length);

		for (ISourceContainer container : containers) {
			if (container instanceof CProjectSourceContainer) {
				IProject project = ((CProjectSourceContainer) container).getProject();
				if (project != null && project.exists()) {
					IPath location = project.getLocation();
					if (location != null) {
						list.add(location.toPortableString());
					}
				}
			} else if (container instanceof ProjectSourceContainer) { // For backward compatibility
				IProject project = ((ProjectSourceContainer) container).getProject();
				if (project != null && project.exists()) {
					IPath location = project.getLocation();
					if (location != null) {
						list.add(location.toPortableString());
					}
				}
			} else if (container instanceof FolderSourceContainer) {
				IContainer folder = ((FolderSourceContainer) container).getContainer();
				if (folder != null && folder.exists()) {
					IPath location = folder.getLocation();
					if (location != null) {
						list.add(location.toPortableString());
					}
				}
			} else if (container instanceof DirectorySourceContainer) {
				File dir = ((DirectorySourceContainer) container).getDirectory();
				if (dir != null && dir.exists()) {
					IPath path = new Path(dir.getAbsolutePath());
					list.add(path.toPortableString());
				}
			}
			if (container.isComposite()) {
				try {
					list.addAll(getSourceLookupPath(container.getSourceContainers()));
				} catch (CoreException e) {
				}
			}
		}

		return list;
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
		fConnection = getServicesTracker().getService(ICommandControl.class);
		fCommandFactory = getServicesTracker().getService(IMICommandControl.class).getCommandFactory();
		// Register this service
		register(new String[] { CSourceLookup.class.getName(), ISourceLookup.class.getName() },
				new Hashtable<String, String>());
		requestMonitor.done();
	}

	@Override
	public void shutdown(final RequestMonitor requestMonitor) {
		unregister();
		super.shutdown(requestMonitor);
	}

	@Override
	public void getDebuggerPath(ISourceLookupDMContext sourceLookupCtx, Object source,
			final DataRequestMonitor<String> rm) {
		if (!(source instanceof String)) {
			// In future if needed other elements such as URIs could be supported.
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED,
					"Only string source element is supported", null)); //$NON-NLS-1$);
			rm.done();
			return;
		}
		final String sourceString = (String) source;

		if (!fDirectors.containsKey(sourceLookupCtx)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE,
					"No source director configured for given context", null)); //$NON-NLS-1$);
			rm.done();
			return;
		}
		final CSourceLookupDirector director = fDirectors.get(sourceLookupCtx);

		new Job("Lookup Debugger Path") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				IPath debuggerPath = director.getCompilationPath(sourceString);
				if (debuggerPath != null) {
					rm.setData(debuggerPath.toString());
				} else {
					rm.setData(sourceString);
				}
				rm.done();
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	@Override
	public void getSource(ISourceLookupDMContext sourceLookupCtx, final String debuggerPath,
			final DataRequestMonitor<Object> rm) {
		if (!fDirectors.containsKey(sourceLookupCtx)) {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INVALID_HANDLE,
					"No source director configured for given context", null)); //$NON-NLS-1$);
			rm.done();
			return;
		}
		final CSourceLookupDirector director = fDirectors.get(sourceLookupCtx);

		new Job("Lookup Source") { //$NON-NLS-1$
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Object[] sources;
				try {
					sources = director.findSourceElements(debuggerPath);
					if (sources == null || sources.length == 0) {
						rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED,
								"No sources found", null)); //$NON-NLS-1$);
					} else {
						rm.setData(sources[0]);
					}
				} catch (CoreException e) {
					rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED,
							"Source lookup failed", e)); //$NON-NLS-1$);
				} finally {
					rm.done();
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}
}
