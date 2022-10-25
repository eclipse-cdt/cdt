/*******************************************************************************
 * Copyright (c) 2006, 2022 Wind River Systems, Nokia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Nokia              - initial API and implementation with some code moved from GDBControl.
 *     Wind River System
 *     Ericsson
 *     Marc Khouzam (Ericsson) - Use the new IMIBackend2 interface (Bug 350837)
 *     Mark Bozeman (Mentor Graphics) - Report GDB start failures (Bug 376203)
 *     Iulia Vasii (Freescale Semiconductor) - Separate GDB command from its arguments (Bug 445360)
 *     John Dallaway - Implement getDebuggerCommandLineArray() method (Bug 572944)
 *     John Dallaway - Eliminate deprecated methods (#112)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.cdt.core.parser.util.StringUtil;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.ImmediateRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.gdb.service.command.GDBControl.InitializationShutdownStep;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.IMIBackend2;
import org.eclipse.cdt.dsf.mi.service.command.events.MIStoppedEvent;
import org.eclipse.cdt.dsf.service.AbstractDsfService;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.utils.CommandLineUtil;
import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.cdt.utils.spawner.Spawner;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.osgi.framework.BundleContext;

/**
 * Implementation of {@link IGDBBackend} for the common case where GDB is
 * launched in local file system on host PC where Eclipse runs. This also
 * manages some GDB parameters from a given launch configuration.<br>
 * <br>
 * You can subclass for you special needs.
 *
 * @since 1.1
 */
public class GDBBackend extends AbstractDsfService implements IGDBBackend, IMIBackend2 {

	private final ILaunchConfiguration fLaunchConfiguration;

	/*
	 * Parameters for launching GDB.
	 */
	private SessionType fSessionType;
	private Boolean fAttach;
	private State fBackendState = State.NOT_INITIALIZED;

	/*
	 * Unique ID of this service instance.
	 */
	private final String fBackendId;
	private static int fgInstanceCounter = 0;

	/*
	 * Service state parameters.
	 */
	private MonitorJob fMonitorJob;
	private Process fProcess;
	private int fGDBExitValue;
	private int fGDBLaunchTimeout = 30;

	/**
	 * A Job that will set a failed status in the proper request monitor, if the
	 * interrupt did not succeed after a certain time.
	 */
	private MonitorInterruptJob fInterruptFailedJob;

	public GDBBackend(DsfSession session, ILaunchConfiguration lc) {
		super(session);
		this.fLaunchConfiguration = lc;
		fBackendId = "gdb[" + Integer.toString(fgInstanceCounter++) + "]"; //$NON-NLS-1$//$NON-NLS-2$
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
		getExecutor().execute(getStartupSequence(requestMonitor));
	}

	/** @since 5.0 */
	protected Sequence getStartupSequence(final RequestMonitor requestMonitor) {
		final Sequence.Step[] initializeSteps = new Sequence.Step[] {
				new GDBProcessStep(InitializationShutdownStep.Direction.INITIALIZING),
				new MonitorJobStep(InitializationShutdownStep.Direction.INITIALIZING),
				new RegisterStep(InitializationShutdownStep.Direction.INITIALIZING), };

		return new Sequence(getExecutor(), requestMonitor) {
			@Override
			public Step[] getSteps() {
				return initializeSteps;
			}
		};
	}

	@Override
	public void shutdown(final RequestMonitor requestMonitor) {
		getExecutor().execute(getShutdownSequence(new RequestMonitor(getExecutor(), requestMonitor) {
			@Override
			protected void handleCompleted() {
				GDBBackend.super.shutdown(requestMonitor);
			}
		}));
	}

	/** @since 5.0 */
	protected Sequence getShutdownSequence(RequestMonitor requestMonitor) {
		final Sequence.Step[] shutdownSteps = new Sequence.Step[] {
				new RegisterStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
				new MonitorJobStep(InitializationShutdownStep.Direction.SHUTTING_DOWN),
				new GDBProcessStep(InitializationShutdownStep.Direction.SHUTTING_DOWN), };

		return new Sequence(getExecutor(), requestMonitor) {
			@Override
			public Step[] getSteps() {
				return shutdownSteps;
			}
		};
	}

	/** @since 5.2 */
	protected GdbLaunch getGDBLaunch() {
		return (GdbLaunch) getSession().getModelAdapter(ILaunch.class);
	}

	/** @since 4.0 */
	protected IPath getGDBPath() {
		return getGDBLaunch().getGDBPath();
	}

	/**
	 * Options for GDB process. Returns the GDB command and its arguments as an
	 * array. Allow subclass to override.
	 */
	@Override
	public String[] getDebuggerCommandLineArray() {
		// The goal here is to keep options to an absolute minimum.
		// All configuration should be done in the final launch sequence
		// to allow for more flexibility.

		String cmd = getGDBPath().toOSString() + " --interpreter" + //$NON-NLS-1$
		// We currently work with MI version 2. Don't use just 'mi' because it
		// points to the latest MI version, while we want mi2 specifically.
				" mi2" + //$NON-NLS-1$
				// Don't read the gdbinit file here. It is read explicitly in
				// the LaunchSequence to make it easier to customize.
				" --nx"; //$NON-NLS-1$

		// Parse to properly handle spaces and such things (bug 458499)
		return CommandLineUtil.argumentsToArray(cmd);
	}

	@Override
	public String getGDBInitFile() throws CoreException {
		return getGDBLaunch().getGDBInitFile();
	}

	@Override
	public IPath getGDBWorkingDirectory() throws CoreException {
		return getGDBLaunch().getGDBWorkingDirectory();
	}

	@Override
	public String getProgramArguments() throws CoreException {
		return getGDBLaunch().getProgramArguments();
	}

	@Override
	public IPath getProgramPath() {
		try {
			return new Path(getGDBLaunch().getProgramPath());
		} catch (CoreException e) {
			return new Path(""); //$NON-NLS-1$
		}
	}

	@Override
	public List<String> getSharedLibraryPaths() throws CoreException {
		return getGDBLaunch().getSharedLibraryPaths();
	}

	/** @since 3.0 */
	@Override
	public Properties getEnvironmentVariables() throws CoreException {
		return getGDBLaunch().getEnvironmentVariables();
	}

	/** @since 3.0 */
	@Override
	public boolean getClearEnvironment() throws CoreException {
		return getGDBLaunch().getClearEnvironment();
	}

	/** @since 3.0 */
	@Override
	public boolean getUpdateThreadListOnSuspend() throws CoreException {
		return getGDBLaunch().getUpdateThreadListOnSuspend();
	}

	/**
	 * Launch GDB process. Allow subclass to override.
	 *
	 * @since 5.2
	 */
	// Again, we create a new method that we know has not been already
	// overridden.  That way, even if extenders have overridden the
	// original launchGDBProcess(String[]), we will instead use
	// the GDBBackend_7_12#launchGDBProcess() method when appropriate.
	// This is important because if we didn't, the new console would
	// not work properly.
	//
	// Of course, in that case, we won't call the extenders overridden
	// launchGDBProcess(String[]) and therefore will not get their
	// specialized code.  I feel this is still a lower risk than
	// not starting the full GDB console properly.
	protected Process launchGDBProcess() throws CoreException {
		// Call the old method in case it was overridden
		return launchGDBProcess(getDebuggerCommandLineArray());
	}

	/**
	 * Launch GDB process with command and arguments. Allow subclass to
	 * override.
	 *
	 * @since 4.6
	 * @deprecated Replace by launchGDBProcess()
	 */
	@Deprecated
	protected Process launchGDBProcess(String[] commandLine) throws CoreException {
		Process proc = null;
		try {
			proc = ProcessFactory.getFactory().exec(commandLine, getGDBLaunch().getLaunchEnvironment());
		} catch (IOException e) {
			String message = "Error while launching command: " + StringUtil.join(commandLine, " "); //$NON-NLS-1$ //$NON-NLS-2$
			throw new CoreException(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, message, e));
		}

		return proc;
	}

	@Override
	public Process getProcess() {
		return fProcess;
	}

	@Override
	public OutputStream getMIOutputStream() {
		return fProcess.getOutputStream();
	}

	@Override
	public InputStream getMIInputStream() {
		return fProcess.getInputStream();
	}

	/** @since 4.1 */
	@Override
	public InputStream getMIErrorStream() {
		return fProcess.getErrorStream();
	}

	@Override
	public String getId() {
		return fBackendId;
	}

	@Override
	public void interrupt() {
		if (fProcess instanceof Spawner) {
			Spawner gdbSpawner = (Spawner) fProcess;

			// Cygwin gdb 6.8 is capricious when it comes to interrupting the
			// target. The same logic here will work with MinGW, though. And on
			// linux it's irrelevant since interruptCTRLC()==interrupt(). So,
			// one odd size fits all.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=304096#c54
			if (getSessionType() == SessionType.REMOTE) {
				gdbSpawner.interrupt();
			} else {
				gdbSpawner.interruptCTRLC();
			}
		}
	}

	/**
	 * @since 3.0
	 */
	@Override
	public void interruptAndWait(int timeout, RequestMonitor rm) {
		if (fProcess instanceof Spawner) {
			Spawner gdbSpawner = (Spawner) fProcess;

			// Cygwin gdb 6.8 is capricious when it comes to interrupting the
			// target. The same logic here will work with MinGW, though. And on
			// linux it's irrelevant since interruptCTRLC()==interrupt(). So,
			// one odd size fits all.
			// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=304096#c54
			if (getSessionType() == SessionType.REMOTE) {
				gdbSpawner.interrupt();
			} else {
				gdbSpawner.interruptCTRLC();
			}
			fInterruptFailedJob = new MonitorInterruptJob(timeout, rm);
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED,
					"Cannot interrupt.", null)); //$NON-NLS-1$
			rm.done();
		}
	}

	/**
	 * @since 3.0
	 */
	@Override
	public void interruptInferiorAndWait(long pid, int timeout, RequestMonitor rm) {
		if (fProcess instanceof Spawner) {
			Spawner gdbSpawner = (Spawner) fProcess;
			gdbSpawner.raise((int) pid, gdbSpawner.INT);
			fInterruptFailedJob = new MonitorInterruptJob(timeout, rm);
		} else {
			rm.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.NOT_SUPPORTED,
					"Cannot interrupt.", null)); //$NON-NLS-1$
			rm.done();
		}
	}

	@Override
	public void destroy() {
		// Don't close the streams ourselves as it may be too early.
		// Wait for the actual user of the streams to close it.
		// Bug 339379

		// destroy() should be supported even if it's not spawner.
		if (getState() == State.STARTED) {
			fProcess.destroy();
		}
	}

	@Override
	public State getState() {
		return fBackendState;
	}

	@Override
	public int getExitCode() {
		return fGDBExitValue;
	}

	@Override
	public SessionType getSessionType() {
		if (fSessionType == null) {
			fSessionType = LaunchUtils.getSessionType(fLaunchConfiguration);
		}
		return fSessionType;
	}

	@Override
	public boolean getIsAttachSession() {
		if (fAttach == null) {
			fAttach = LaunchUtils.getIsAttach(fLaunchConfiguration);
		}
		return fAttach;
	}

	@Override
	protected BundleContext getBundleContext() {
		return GdbPlugin.getBundleContext();
	}

	protected class GDBProcessStep extends InitializationShutdownStep {
		GDBProcessStep(Direction direction) {
			super(direction);
		}

		@Override
		public void initialize(final RequestMonitor requestMonitor) {
			doGDBProcessStep(requestMonitor);
		}

		@Override
		protected void shutdown(final RequestMonitor requestMonitor) {
			undoGDBProcessStep(requestMonitor);
		}
	}

	protected class MonitorJobStep extends InitializationShutdownStep {
		MonitorJobStep(Direction direction) {
			super(direction);
		}

		@Override
		public void initialize(final RequestMonitor requestMonitor) {
			doMonitorJobStep(requestMonitor);
		}

		@Override
		protected void shutdown(RequestMonitor requestMonitor) {
			undoMonitorJobStep(requestMonitor);
		}
	}

	protected class RegisterStep extends InitializationShutdownStep {
		RegisterStep(Direction direction) {
			super(direction);
		}

		@Override
		public void initialize(RequestMonitor requestMonitor) {
			doRegisterStep(requestMonitor);
		}

		@Override
		protected void shutdown(RequestMonitor requestMonitor) {
			undoRegisterStep(requestMonitor);
		}
	}

	/** @since 5.0 */
	protected void doGDBProcessStep(final RequestMonitor requestMonitor) {
		class GDBLaunchMonitor {
			boolean fLaunched = false;
			boolean fTimedOut = false;
			public ScheduledFuture<?> fTimeoutFuture;
		}
		final GDBLaunchMonitor fGDBLaunchMonitor = new GDBLaunchMonitor();

		final RequestMonitor gdbLaunchRequestMonitor = new RequestMonitor(getExecutor(), requestMonitor) {
			@Override
			protected void handleCompleted() {
				if (!fGDBLaunchMonitor.fTimedOut) {
					fGDBLaunchMonitor.fLaunched = true;
					fGDBLaunchMonitor.fTimeoutFuture.cancel(false);
					if (!isSuccess()) {
						requestMonitor.setStatus(getStatus());
					}
					requestMonitor.done();
				}
			}
		};

		final Job startGdbJob = new Job("Start GDB Process Job") { //$NON-NLS-1$
			{
				setSystem(true);
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (gdbLaunchRequestMonitor.isCanceled()) {
					gdbLaunchRequestMonitor.setStatus(
							new Status(IStatus.CANCEL, GdbPlugin.PLUGIN_ID, -1, "Canceled starting GDB", null)); //$NON-NLS-1$
					gdbLaunchRequestMonitor.done();
					return Status.OK_STATUS;
				}

				try {
					fProcess = launchGDBProcess();

					// Need to do this on the executor for thread-safety
					getExecutor().submit(new DsfRunnable() {
						@Override
						public void run() {
							fBackendState = State.STARTED;
						}
					});
					// Don't send the backendStarted event yet. We wait
					// until we have registered this service
					// so that other services can have access to it.
				} catch (CoreException e) {
					gdbLaunchRequestMonitor
							.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, e.getMessage(), e));
					gdbLaunchRequestMonitor.done();
					return Status.OK_STATUS;
				}

				BufferedReader inputReader = null;
				BufferedReader errorReader = null;
				boolean success = false;
				try {
					// Must call getMIInputStream() because we always want to read from the MI stream,
					// which is not always the same as the input stream of fProcess.  They are
					// different when we use the full GDB console
					InputStream inputStream = getMIInputStream();
					// Read initial GDB prompt
					inputReader = new BufferedReader(new InputStreamReader(inputStream));
					String line;
					while ((line = inputReader.readLine()) != null) {
						line = line.trim();
						if (line.endsWith("(gdb)")) { //$NON-NLS-1$
							success = true;
							break;
						}
					}

					// Failed to read initial prompt, check for error
					if (!success) {
						// Don't call getMIErrorStream() because it can be overridden with a
						// dummy stream in the case of the full GDB console.
						// Instead, make sure we read the error from the process itself.
						InputStream errorStream = fProcess.getErrorStream();
						errorReader = new BufferedReader(new InputStreamReader(errorStream));
						String errorInfo = errorReader.readLine();
						if (errorInfo == null) {
							errorInfo = "GDB prompt not read"; //$NON-NLS-1$
						}
						gdbLaunchRequestMonitor
								.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, errorInfo, null));
					}
				} catch (IOException e) {
					success = false;
					gdbLaunchRequestMonitor.setStatus(
							new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, -1, "Error reading GDB output", e)); //$NON-NLS-1$
				}

				// In the case of failure, close the MI streams so
				// they are not leaked.
				if (!success) {
					if (inputReader != null) {
						try {
							inputReader.close();
						} catch (IOException e) {
						}
					}
					if (errorReader != null) {
						try {
							errorReader.close();
						} catch (IOException e) {
						}
					}
				}

				gdbLaunchRequestMonitor.done();
				return Status.OK_STATUS;
			}
		};
		startGdbJob.schedule();

		fGDBLaunchMonitor.fTimeoutFuture = getExecutor().schedule(() -> {
			// Only process the event if we have not finished yet (hit
			// the breakpoint).
			if (!fGDBLaunchMonitor.fLaunched) {
				fGDBLaunchMonitor.fTimedOut = true;
				Thread jobThread = startGdbJob.getThread();
				if (jobThread != null) {
					jobThread.interrupt();
				}

				destroy();

				requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
						DebugException.TARGET_REQUEST_FAILED, "Timed out trying to launch GDB.", null)); //$NON-NLS-1$
				requestMonitor.done();
			}
		}, fGDBLaunchTimeout, TimeUnit.SECONDS);
	}

	/** @since 5.0 */
	protected void undoGDBProcessStep(final RequestMonitor requestMonitor) {
		if (getState() != State.STARTED) {
			// gdb not started yet or already killed, don't bother starting
			// a job to kill it
			requestMonitor.done();
			return;
		}

		new Job("Terminating GDB process.") { //$NON-NLS-1$
			{
				setSystem(true);
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					// Need to do this on the executor for thread-safety
					// And we should wait for it to complete since we then
					// check if the killing of GDB worked.
					getExecutor().submit(new DsfRunnable() {
						@Override
						public void run() {
							destroy();

							if (fMonitorJob.fMonitorExited) {
								// Now that we have destroyed the process, and
								// that the monitoring thread was killed, we
								// need to set our state and send the event
								fBackendState = State.TERMINATED;
								getSession().dispatchEvent(
										new BackendStateChangedEvent(getSession().getId(), getId(), State.TERMINATED),
										getProperties());
							}
						}
					}).get();
				} catch (InterruptedException e1) {
				} catch (ExecutionException e1) {
				}

				int attempts = 0;
				while (attempts < 10) {
					try {
						// Don't know if we really need the exit value...
						// but what the heck.
						// throws exception if process not exited
						fGDBExitValue = fProcess.exitValue();

						requestMonitor.done();
						return Status.OK_STATUS;
					} catch (IllegalThreadStateException ie) {
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
					}
					attempts++;
				}
				requestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
						IDsfStatusConstants.REQUEST_FAILED, "GDB terminate failed", null)); //$NON-NLS-1$
				requestMonitor.done();
				return Status.OK_STATUS;
			}
		}.schedule();
	}

	/** @since 5.0 */
	protected void doMonitorJobStep(final RequestMonitor requestMonitor) {
		fMonitorJob = new MonitorJob(fProcess, new DsfRunnable() {
			@Override
			public void run() {
				requestMonitor.done();
			}
		});
		fMonitorJob.schedule();
	}

	/** @since 5.0 */
	protected void undoMonitorJobStep(RequestMonitor requestMonitor) {
		if (fMonitorJob != null) {
			fMonitorJob.kill();
		}
		requestMonitor.done();
	}

	/** @since 5.0 */
	protected void doRegisterStep(RequestMonitor requestMonitor) {
		register(new String[] { IMIBackend.class.getName(), IMIBackend2.class.getName(), IGDBBackend.class.getName() },
				new Hashtable<String, String>());

		getSession().addServiceEventListener(GDBBackend.this, null);

		/*
		 * This event is not consumed by any one at present, instead it's the
		 * GDBControlInitializedDMEvent that's used to indicate that GDB back
		 * end is ready for MI commands. But we still fire the event as it does
		 * no harm and may be needed sometime.... 09/29/2008
		 *
		 * We send the event in the register step because that is when other
		 * services have access to it.
		 */
		getSession().dispatchEvent(new BackendStateChangedEvent(getSession().getId(), getId(), State.STARTED),
				getProperties());

		requestMonitor.done();
	}

	/** @since 5.0 */
	protected void undoRegisterStep(RequestMonitor requestMonitor) {
		unregister();
		getSession().removeServiceEventListener(GDBBackend.this);
		requestMonitor.done();
	}

	/**
	 * Monitors a system process, waiting for it to terminate, and then notifies
	 * the associated runtime process.
	 */
	private class MonitorJob extends Job {
		boolean fMonitorExited = false;
		DsfRunnable fMonitorStarted;
		Process fMonProcess;

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			synchronized (fMonProcess) {
				getExecutor().submit(fMonitorStarted);
				try {
					fMonProcess.waitFor();
					fGDBExitValue = fMonProcess.exitValue();

					// Need to do this on the executor for thread-safety
					getExecutor().submit(new DsfRunnable() {
						@Override
						public void run() {
							destroy();
							fBackendState = State.TERMINATED;
							getSession().dispatchEvent(
									new BackendStateChangedEvent(getSession().getId(), getId(), State.TERMINATED),
									getProperties());
						}
					});
				} catch (InterruptedException ie) {
					// clear interrupted state
					Thread.interrupted();
				}

				fMonitorExited = true;
			}
			return Status.OK_STATUS;
		}

		MonitorJob(Process process, DsfRunnable monitorStarted) {
			super("GDB process monitor job."); //$NON-NLS-1$
			fMonProcess = process;
			fMonitorStarted = monitorStarted;
			setSystem(true);
		}

		void kill() {
			synchronized (fMonProcess) {
				if (!fMonitorExited) {
					getThread().interrupt();
				}
			}
		}
	}

	/**
	 * Stores the request monitor that must be dealt with for the result of the
	 * interrupt operation. If the interrupt successfully suspends the backend,
	 * the request monitor can be retrieved and completed successfully, and then
	 * this job should be canceled. If this job is not canceled before the time
	 * is up, it will imply the interrupt did not successfully suspend the
	 * backend, and the current job will indicate this in the request monitor.
	 *
	 * The specified timeout is used to indicate how many milliseconds this job
	 * should wait for. Default timeout is provided by preference
	 * {@code IGdbDebugPreferenceConstants.PREF_SUSPEND_TIMEOUT_VALUE}.
	 * The default is also used if the timeout value is 0 or
	 * negative.
	 *
	 * @since 3.0
	 */
	protected class MonitorInterruptJob extends Job {

		private final RequestMonitor fRequestMonitor;

		public MonitorInterruptJob(int timeout, RequestMonitor rm) {
			super("Interrupt monitor job."); //$NON-NLS-1$
			setSystem(true);
			fRequestMonitor = rm;

			if (timeout == INTERRUPT_TIMEOUT_DEFAULT || timeout <= 0) {
				timeout = 1000 * Platform.getPreferencesService().getInt(GdbPlugin.PLUGIN_ID,
						IGdbDebugPreferenceConstants.PREF_SUSPEND_TIMEOUT_VALUE,
						IGdbDebugPreferenceConstants.SUSPEND_TIMEOUT_VALUE_DEFAULT, null);
			}

			schedule(timeout);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			getExecutor().submit(new DsfRunnable() {
				@Override
				public void run() {
					fInterruptFailedJob = null;
					fRequestMonitor.setStatus(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID,
							IDsfStatusConstants.REQUEST_FAILED, "Interrupt failed.", null)); //$NON-NLS-1$
					fRequestMonitor.done();
				}
			});
			return Status.OK_STATUS;
		}

		public RequestMonitor getRequestMonitor() {
			return fRequestMonitor;
		}

	}

	/**
	 * We use this handler to determine if the SIGINT we sent to GDB has been
	 * effective. We must listen for an MI event and not a higher-level
	 * ISuspendedEvent. The reason is that some ISuspendedEvent are not sent
	 * when the target stops, in cases where we don't want to views to update.
	 * For example, if we want to interrupt the target to set a breakpoint, this
	 * interruption is done silently; we will receive the MI event though.
	 *
	 * <p>
	 * Though we send a SIGINT, we may not specifically get an MISignalEvent.
	 * Typically we will, but not always, so wait for an MIStoppedEvent. See
	 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=305178#c21
	 *
	 * @since 3.0
	 *
	 */
	@DsfServiceEventHandler
	public void eventDispatched(final MIStoppedEvent e) {
		if (fInterruptFailedJob != null) {
			if (fInterruptFailedJob.cancel()) {
				fInterruptFailedJob.getRequestMonitor().done();
			}
			fInterruptFailedJob = null;
		}
	}
}
