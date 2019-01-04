/*******************************************************************************
 * Copyright (c) 2006, 2018 Wind River Systems and others.
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
 *     Marc Khouzam (Ericsson) - Fix NPE for partial launches (Bug 368597)
 *     Marc Khouzam (Ericsson) - Create the gdb process through the process factory (Bug 210366)
 *     Alvaro Sanchez-Leon (Ericsson AB) - Each memory context needs a different MemoryRetrieval (Bug 250323)
 *     John Dallaway - Resolve variables using launch context (Bug 399460)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.launching;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.core.cdtvariables.ICdtVariableManager;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.internal.core.CRequest;
import org.eclipse.cdt.debug.internal.core.DebugStringVariableSubstitutor;
import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DefaultDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateExecutor;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.concurrent.Sequence.Step;
import org.eclipse.cdt.dsf.concurrent.ThreadSafe;
import org.eclipse.cdt.dsf.concurrent.ThreadSafeAndProhibitedFromDsfExecutor;
import org.eclipse.cdt.dsf.debug.internal.provisional.model.IMemoryBlockRetrievalManager;
import org.eclipse.cdt.dsf.debug.model.DsfLaunch;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService.ICommandControlShutdownDMEvent;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugConstants;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.memory.GdbMemoryBlockRetrievalManager;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IDisconnectHandler;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.debug.core.model.IDisconnect;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.ITerminate;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.launch.ITargetedLaunch;

/**
 * The only object in the model that implements the traditional interfaces.
 */
@ThreadSafe
public class GdbLaunch extends DsfLaunch implements ITerminate, IDisconnect, ITracedLaunch, ITargetedLaunch {
	private DefaultDsfExecutor fExecutor;
	private DsfSession fSession;
	private DsfServicesTracker fTracker;
	private boolean fInitialized = false;
	private boolean fShutDown = false;
	private IMemoryBlockRetrievalManager fMemRetrievalManager;
	private IDsfDebugServicesFactory fServiceFactory;
	private ILaunchTarget fLaunchTarget;
	private Properties fInitialEnv;

	private String fGdbVersion;

	public GdbLaunch(ILaunchConfiguration launchConfiguration, String mode, ISourceLocator locator) {
		super(launchConfiguration, mode, locator);

		// Create the dispatch queue to be used by debugger control and services
		// that belong to this launch
		final DefaultDsfExecutor dsfExecutor = new DefaultDsfExecutor(GdbLaunchDelegate.GDB_DEBUG_MODEL_ID);
		dsfExecutor.prestartCoreThread();
		fExecutor = dsfExecutor;
		fSession = DsfSession.startSession(fExecutor, GdbLaunchDelegate.GDB_DEBUG_MODEL_ID);
	}

	public DsfExecutor getDsfExecutor() {
		return fExecutor;
	}

	public IDsfDebugServicesFactory getServiceFactory() {
		return fServiceFactory;
	}

	public void initialize() throws DebugException {
		/*
		 * Registering the launch as an adapter. This ensures that this launch
		 * will be associated with all DMContexts from this session. We do this
		 * here because we want to have access to the launch even if we run
		 * headless, but when we run headless, GdbAdapterFactory is not
		 * initialized.
		 */
		fSession.registerModelAdapter(ILaunch.class, this);

		Runnable initRunnable = new DsfRunnable() {
			@Override
			public void run() {
				fTracker = new DsfServicesTracker(GdbPlugin.getBundleContext(), fSession.getId());
				fSession.addServiceEventListener(GdbLaunch.this, null);

				fInitialized = true;
				fireChanged();
			}
		};

		// Invoke the execution code and block waiting for the result.
		try {
			fExecutor.submit(initRunnable).get();
		} catch (InterruptedException e) {
			throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Error initializing launch", e)); //$NON-NLS-1$
		} catch (ExecutionException e) {
			throw new DebugException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
					"Error initializing launch", e)); //$NON-NLS-1$
		}
	}

	public void initializeControl() throws CoreException {
		// Create a memory retrieval manager and register it with the session
		// To maintain a mapping of memory contexts to the corresponding memory
		// retrieval in this session
		try {
			fExecutor.submit(new Callable<Object>() {
				@Override
				public Object call() throws CoreException {
					fMemRetrievalManager = new GdbMemoryBlockRetrievalManager(GdbLaunchDelegate.GDB_DEBUG_MODEL_ID,
							getLaunchConfiguration(), fSession);
					fSession.registerModelAdapter(IMemoryBlockRetrievalManager.class, fMemRetrievalManager);
					fSession.addServiceEventListener(fMemRetrievalManager, null);
					return null;
				}
			}).get();
		} catch (InterruptedException e) {
			throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, 0,
					"Interrupted while waiting for get process callable.", e)); //$NON-NLS-1$
		} catch (ExecutionException e) {
			throw (CoreException) e.getCause();
		} catch (RejectedExecutionException e) {
			throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, 0,
					"Debugger shut down before launch was completed.", e)); //$NON-NLS-1$
		}
	}

	public DsfSession getSession() {
		return fSession;
	}

	@ThreadSafeAndProhibitedFromDsfExecutor("getDsfExecutor()")
	public void addCLIProcess(String label) throws CoreException {
		try {
			// Add the GDB process object to the launch.
			Process gdbProc = getDsfExecutor().submit(new Callable<Process>() {
				@Override
				public Process call() throws CoreException {
					IGDBControl gdb = fTracker.getService(IGDBControl.class);
					if (gdb != null) {
						return gdb.getGDBBackendProcess();
					}
					return null;
				}
			}).get();

			// Need to go through DebugPlugin.newProcess so that we can use
			// the overrideable process factory to allow others to override.
			// First set attribute to specify we want to create the gdb process.
			// Bug 210366
			Map<String, String> attributes = new HashMap<>();
			attributes.put(IGdbDebugConstants.PROCESS_TYPE_CREATION_ATTR,
					IGdbDebugConstants.GDB_PROCESS_CREATION_VALUE);
			DebugPlugin.newProcess(this, gdbProc, label, attributes);
		} catch (InterruptedException e) {
			throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, 0,
					"Interrupted while waiting for get process callable.", e)); //$NON-NLS-1$
		} catch (ExecutionException e) {
			throw (CoreException) e.getCause();
		} catch (RejectedExecutionException e) {
			throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, 0,
					"Debugger shut down before launch was completed.", e)); //$NON-NLS-1$
		}
	}

	public void setServiceFactory(IDsfDebugServicesFactory factory) {
		fServiceFactory = factory;
	}

	///////////////////////////////////////////////////////////////////////////
	// IServiceEventListener
	@DsfServiceEventHandler
	public void eventDispatched(ICommandControlShutdownDMEvent event) {
		shutdownSession(new ImmediateRequestMonitor());
	}

	///////////////////////////////////////////////////////////////////////////
	// ITerminate

	static class LaunchCommandRequest extends CRequest implements IDebugCommandRequest {
		private Object[] elements;

		public LaunchCommandRequest(Object[] objects) {
			elements = objects;
		}

		@Override
		public Object[] getElements() {
			return elements;
		}

		@Override
		public void done() {
			IStatus status = getStatus();
			if (status != null && !status.isOK()) {
				IStatusHandler statusHandler = DebugPlugin.getDefault().getStatusHandler(status);
				if (statusHandler != null) {
					try {
						statusHandler.handleStatus(status, null);
					} catch (CoreException ex) {
						GdbPlugin.getDefault().getLog().log(ex.getStatus());
					}
				} else {
					GdbPlugin.getDefault().getLog().log(status);
				}
			}
		}
	}

	@Override
	public boolean canTerminate() {
		return fInitialized && super.canTerminate();
	}

	@Override
	public void terminate() throws DebugException {
		// Execute asynchronously to avoid potential deadlocks
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=434645

		ITerminateHandler handler = getAdapter(ITerminateHandler.class);
		if (handler == null) {
			super.terminate();
			return;
		}

		LaunchCommandRequest req = new LaunchCommandRequest(new Object[] { this });
		handler.execute(req);
	}

	// ITerminate
	///////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////
	// IDisconnect
	@Override
	public boolean canDisconnect() {
		return canTerminate();
	}

	@Override
	public boolean isDisconnected() {
		return isTerminated();
	}

	@Override
	public void disconnect() throws DebugException {
		IDisconnectHandler handler = getAdapter(IDisconnectHandler.class);
		if (handler == null) {
			super.disconnect();
			return;
		}

		LaunchCommandRequest req = new LaunchCommandRequest(new Object[] { this });
		handler.execute(req);
	}

	// IDisconnect
	///////////////////////////////////////////////////////////////////////////

	/**
	 * Terminates the gdb session, shuts down the services, the session and the
	 * executor associated with this launch.
	 * <p>
	 * Note: The argument request monitor to this method should NOT use the
	 * executor that belongs to this launch. By the time the shutdown is
	 * complete, this executor will not be dispatching anymore and the request
	 * monitor will never be invoked. Instead callers should use the
	 * {@link ImmediateExecutor}.
	 * </p>
	 *
	 * @param rm
	 *            The request monitor invoked when the shutdown is complete.
	 */
	@ConfinedToDsfExecutor("getSession().getExecutor()")
	public void shutdownSession(final RequestMonitor rm) {
		if (fShutDown) {
			rm.done();
			return;
		}
		fShutDown = true;

		final Sequence shutdownSeq = new ShutdownSequence(getDsfExecutor(), fSession.getId(),
				new RequestMonitor(fSession.getExecutor(), rm) {
					@Override
					public void handleCompleted() {
						if (fMemRetrievalManager != null) {
							fSession.removeServiceEventListener(fMemRetrievalManager);
							fMemRetrievalManager.dispose();
						}

						fSession.removeServiceEventListener(GdbLaunch.this);
						if (!isSuccess()) {
							GdbPlugin.getDefault().getLog().log(new MultiStatus(GdbPlugin.PLUGIN_ID, -1,
									new IStatus[] { getStatus() }, "Session shutdown failed", null)); //$NON-NLS-1$
						}
						// Last order of business, shutdown the dispatch queue.
						if (fTracker != null) {
							fTracker.dispose();
							fTracker = null;
						}

						DsfSession.endSession(fSession);

						// 'fireTerminate()' removes this launch from the list
						// of 'DebugEvent'
						// listeners. The launch may not be terminated at this
						// point: the inferior
						// and gdb processes are monitored in separate threads.
						// This will prevent
						// updating of some of the Debug view actions.
						// 'DebugEvent.TERMINATE' will be fired when each of the
						// corresponding processes
						// exits and handled by 'handleDebugEvents()' method.
						if (isTerminated()) {
							fireTerminate();
						}

						rm.setStatus(getStatus());
						rm.done();
					}
				});

		final Step[] steps = new Step[] { new Step() {
			@Override
			public void execute(RequestMonitor rm) {
				if (fTracker != null) {
					IGDBControl control = fTracker.getService(IGDBControl.class);
					if (control != null) {
						control.terminate(rm);
						return;
					}
				}

				rm.done();
				return;
			}
		},

				new Step() {
					@Override
					public void execute(RequestMonitor rm) {
						fExecutor.execute(shutdownSeq);
					}
				} };

		fExecutor.execute(new Sequence(fExecutor) {

			@Override
			public Step[] getSteps() {
				return steps;
			}
		});
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (!adapter.equals(ITerminateHandler.class)) {
			// Must force adapters to be loaded.
			// Except in the case of terminate. Terminate can be used
			// when running headless (no UI) and therefore we should not
			// force the loading of UI plugins in this case.
			// This can happen when running JUnit tests for example.
			Platform.getAdapterManager().loadAdapter(this, adapter.getName());
		}
		return super.getAdapter(adapter);
	}

	@Override
	public void launchRemoved(ILaunch launch) {
		if (this.equals(launch)) {
			// When the launch fails early, we may not have cleaned up
			// properly.  Let's do it here if needed.
			if (DsfSession.isSessionActive(fSession.getId())) {
				DsfSession.endSession(fSession);
			}
			fExecutor.shutdown();
			fExecutor = null;
		}
		super.launchRemoved(launch);
	}

	/**
	 * Get the default GDB path if not specified in the launch or launch config.
	 *
	 * @since 5.0
	 */
	protected String getDefaultGDBPath() {
		return Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_COMMAND,
				IGDBLaunchConfigurationConstants.DEBUGGER_DEBUG_NAME_DEFAULT, null);
	}

	/**
	 * Returns the path to gdb.
	 *
	 * @since 5.0
	 */
	public IPath getGDBPath() {
		try {
			String gdb = getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME);
			if (gdb == null) {
				gdb = getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME,
						getDefaultGDBPath());
			}
			if (gdb != null) {
				IProject project = getProject();
				gdb = new DebugStringVariableSubstitutor(project).performStringSubstitution(gdb, false);
				return new Path(gdb);
			} else {
				return null;
			}
		} catch (CoreException e) {
			GdbPlugin.log(e);
			return null;
		}
	}

	/**
	 * Set the path to gdb
	 *
	 * @param path
	 *            the path to gdb
	 * @since 5.0
	 */
	public void setGDBPath(String path) {
		setAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUG_NAME, path);
	}

	/**
	 * This method actually launches 'gdb --version' to determine the version of
	 * the GDB that is being used. The result is then cached for any future requests.
	 *
	 * A timeout is scheduled which will kill the process if it takes too long.
	 *
	 * @since 5.0
	 */
	public String getGDBVersion() throws CoreException {
		if (fGdbVersion != null) {
			return fGdbVersion;
		}

		String gdbPath = getGDBPath().toOSString();
		String[] launchEnvironment = getLaunchEnvironment();

		String gdbVersion = LaunchUtils.getGDBVersion(gdbPath, launchEnvironment);
		fGdbVersion = gdbVersion;
		return fGdbVersion;

	}

	/**
	 * Read from the specified stream and return what was read.
	 *
	 * @param stream
	 *            The input stream to be used to read the data. This method will
	 *            close the stream.
	 * @return The data read from the stream
	 * @throws IOException
	 *             If an IOException happens when reading the stream
	 */
	private static String readStream(InputStream stream) throws IOException {
		StringBuilder cmdOutput = new StringBuilder(200);
		try {
			Reader r = new InputStreamReader(stream);
			BufferedReader reader = new BufferedReader(r);

			String line;
			while ((line = reader.readLine()) != null) {
				cmdOutput.append(line);
				cmdOutput.append('\n');
			}
			return cmdOutput.toString();
		} finally {
			// Cleanup to avoid leaking pipes
			// Bug 345164
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
	}

	/**
	 * Gets the CDT environment from the CDT project's configuration referenced
	 * by the launch
	 *
	 * @since 5.0
	 */
	public String[] getLaunchEnvironment() throws CoreException {
		IProject project = getProject();

		HashMap<String, String> envMap = new HashMap<>();
		ICProjectDescription projDesc = CoreModel.getDefault().getProjectDescription(project, false);
		if (projDesc != null) {
			String buildConfigID = getLaunchConfiguration()
					.getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID, ""); //$NON-NLS-1$
			ICConfigurationDescription cfg = null;
			if (buildConfigID.length() != 0) {
				cfg = projDesc.getConfigurationById(buildConfigID);
			}

			// if configuration is null fall-back to active
			if (cfg == null) {
				cfg = projDesc.getActiveConfiguration();
			}

			// Environment variables and inherited vars
			IEnvironmentVariable[] vars = CCorePlugin.getDefault().getBuildEnvironmentManager().getVariables(cfg, true);
			for (IEnvironmentVariable var : vars) {
				envMap.put(var.getName(), var.getValue());
			}

			// Add variables from build info
			ICdtVariableManager manager = CCorePlugin.getDefault().getCdtVariableManager();
			ICdtVariable[] buildVars = manager.getVariables(cfg);
			for (ICdtVariable var : buildVars) {
				try {
					// The project_classpath variable contributed by JDT is
					// useless for running C/C++ binaries, but it can be lethal
					// if it has a very large value that exceeds shell limit. See
					// http://bugs.eclipse.org/bugs/show_bug.cgi?id=408522
					if (!"project_classpath".equals(var.getName())) {//$NON-NLS-1$
						String value = manager.resolveValue(var.getStringValue(), "", File.pathSeparator, cfg); //$NON-NLS-1$
						envMap.put(var.getName(), value);
					}
				} catch (CdtVariableException e) {
					// Some Eclipse dynamic variables can't be resolved
					// dynamically... we don't care.
				}
			}
		}

		// Turn it into an envp format
		List<String> strings = new ArrayList<>(envMap.size());
		for (Entry<String, String> entry : envMap.entrySet()) {
			StringBuilder buffer = new StringBuilder(entry.getKey());
			buffer.append('=').append(entry.getValue());
			strings.add(buffer.toString());
		}

		return strings.toArray(new String[strings.size()]);
	}

	private IProject getProject() throws CoreException {
		ILaunchConfiguration configuration = getLaunchConfiguration();
		return org.eclipse.cdt.launch.LaunchUtils.getProject(configuration);
	}

	/**
	 * Get the location of the gdbinit file.
	 *
	 * @return gdbinit file location
	 * @throws CoreException
	 * @since 5.0
	 */
	public String getGDBInitFile() throws CoreException {
		String defaultGdbInit = Platform.getPreferencesService().getString(GdbPlugin.PLUGIN_ID,
				IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_INIT,
				IGDBLaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT, null);

		return getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_GDB_INIT, defaultGdbInit);
	}

	/**
	 * Get the working directory.
	 *
	 * @return the working directory
	 * @throws CoreException
	 * @since 5.0
	 */
	public IPath getGDBWorkingDirectory() throws CoreException {
		// First try to use the user-specified working directory for the
		// debugged program.
		// This is fine only with local debug.
		// For remote debug, the working dir of the debugged program will be
		// on remote device
		// and hence not applicable. In such case we may just use debugged
		// program path on host
		// as the working dir for GDB.
		// However, we cannot find a standard/common way to distinguish
		// remote debug from local
		// debug. For instance, a local debug may also use gdbserver+gdb. So
		// it's up to each
		// debugger implementation to make the distinction.
		//
		IPath path = null;
		String location = getLaunchConfiguration().getAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
				(String) null);

		if (location != null) {
			String expandedLocation = VariablesPlugin.getDefault().getStringVariableManager()
					.performStringSubstitution(location);
			if (!expandedLocation.isEmpty()) {
				path = new Path(expandedLocation);
			}
		}

		if (path != null) {
			// Some validity check. Should have been done by UI code.
			if (path.isAbsolute()) {
				File dir = new File(path.toPortableString());
				if (!dir.isDirectory())
					path = null;
			} else {
				IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(path);
				if (res instanceof IContainer && res.exists()) {
					path = res.getLocation();
				} else
					// Relative but not found in workspace.
					path = null;
			}
		}

		if (path == null) {
			// default working dir is the project if this config has a
			// project
			ICProject cp = LaunchUtils.getCProject(getLaunchConfiguration());
			if (cp != null) {
				IProject p = cp.getProject();
				path = p.getLocation();
			} else {
				// no meaningful value found. Just return null.
			}
		}

		return path;
	}

	/**
	 * Get the program arguments
	 *
	 * @return program arguments
	 * @throws CoreException
	 * @since 5.0
	 */
	public String getProgramArguments() throws CoreException {
		return org.eclipse.cdt.launch.LaunchUtils.getProgramArguments(getLaunchConfiguration());
	}

	/**
	 * Return the program path
	 *
	 * @return the program path
	 * @since 5.0
	 */
	public String getProgramPath() throws CoreException {
		ILaunchConfiguration configuration = getLaunchConfiguration();
		String programName = getAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME);
		return org.eclipse.cdt.launch.LaunchUtils.resolveProgramPath(configuration, programName);
	}

	/**
	 * Sets the program path
	 *
	 * @param programPath
	 *            the program path
	 * @throws CoreException
	 * @since 5.0
	 */
	public void setProgramPath(String programPath) throws CoreException {
		setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, programPath);
	}

	/**
	 * Return shared library paths
	 *
	 * @return shared library paths
	 * @throws CoreException
	 * @since 5.0
	 */
	public List<String> getSharedLibraryPaths() throws CoreException {
		return getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_SOLIB_PATH,
				new ArrayList<String>(0));
	}

	/**
	 * return the environment
	 *
	 * @return the environment
	 * @throws CoreException
	 * @since 5.0
	 */
	public Properties getEnvironmentVariables() throws CoreException {
		Properties envVariables = new Properties();
		if (fInitialEnv != null) {
			envVariables.putAll(fInitialEnv);
		}

		// if the attribute ATTR_APPEND_ENVIRONMENT_VARIABLES is set,
		// the LaunchManager will return both the new variables and the
		// existing ones.
		// That would force us to delete all the variables in GDB, and then
		// re-create then all
		// that is not very efficient. So, let's fool the LaunchManager into
		// returning just the
		// list of new variables.

		boolean append = getLaunchConfiguration().getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);

		String[] properties;
		if (append) {
			ILaunchConfigurationWorkingCopy wc = getLaunchConfiguration().copy(""); //$NON-NLS-1$
			// Don't save this change, it is just temporary, and in just a
			// copy of our launchConfig.
			wc.setAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, false);
			properties = DebugPlugin.getDefault().getLaunchManager().getEnvironment(wc);
		} else {
			// We're getting rid of the environment anyway, so this call
			// will only yield the new variables.
			properties = DebugPlugin.getDefault().getLaunchManager().getEnvironment(getLaunchConfiguration());
		}

		if (properties == null) {
			properties = new String[0];
		}

		for (String property : properties) {
			int idx = property.indexOf('=');
			if (idx != -1) {
				String key = property.substring(0, idx);
				String value = property.substring(idx + 1);
				envVariables.setProperty(key, value);
			} else {
				envVariables.setProperty(property, ""); //$NON-NLS-1$
			}
		}
		return envVariables;
	}

	/**
	 * Get whether to clear the environment before applying the variables
	 *
	 * @return clear
	 * @throws CoreException
	 * @since 5.0
	 */
	public boolean getClearEnvironment() throws CoreException {
		return !getLaunchConfiguration().getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true);
	}

	/**
	 * Get whether to update thread list on suspend
	 *
	 * @return whether
	 * @throws CoreException
	 * @since 5.0
	 */
	public boolean getUpdateThreadListOnSuspend() throws CoreException {
		return getLaunchConfiguration().getAttribute(
				IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND,
				IGDBLaunchConfigurationConstants.DEBUGGER_UPDATE_THREADLIST_ON_SUSPEND_DEFAULT);
	}

	/**
	 * Set the launch target
	 *
	 * @param launchTarget
	 *            the launch target
	 * @since 5.0
	 */
	public void setLaunchTarget(ILaunchTarget launchTarget) {
		this.fLaunchTarget = launchTarget;
	}

	/**
	 * Return the launch target
	 *
	 * @since 5.0
	 */
	@Override
	public ILaunchTarget getLaunchTarget() {
		return fLaunchTarget;
	}

	/**
	 * Set the initial environment variables. These can then be overriden
	 * by launch configuration attributes.
	 *
	 * @since 5.2
	 */
	public void setInitialEnvironment(Properties initialEnv) {
		this.fInitialEnv = initialEnv;
	}
}
