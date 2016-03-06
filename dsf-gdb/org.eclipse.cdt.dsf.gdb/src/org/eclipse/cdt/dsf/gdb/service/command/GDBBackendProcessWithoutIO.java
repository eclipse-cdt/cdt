/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.service.command;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControlService;
import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.mi.service.IMIBackend;
import org.eclipse.cdt.dsf.mi.service.IMIBackend.BackendStateChangedEvent;
import org.eclipse.cdt.dsf.service.DsfServiceEventHandler;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * Note that starting with GDB 7.12, as long as a PTY is available, this process
 * is used instead of GDBBackendProcess. This is because the GDB CLI is handled
 * directly by GDB and the current class only needs to handle the life-cycle of
 * the GDB process.
 *
 * This class is therefore a representation of the GDB process that will be
 * added to the launch. This class is not the real GDB process but simply an
 * entry for the launch to handle user actions but no IO.
 * 
 * @since 5.1
 */
public class GDBBackendProcessWithoutIO extends Process implements IGDBBackendProcessWithoutIO {
	private ICommandControlService fControl;
	private DsfSession fSession;
	private boolean fDisposed;
	private BackedExitedEventListener fExitedEventListener;
	private IMIBackend fMIBackend;

	private AtomicInteger fExitCode = new AtomicInteger(-1);

	/**
	 * Listen for backend events.
	 * This class must be public to received the backend events.
	 */
	public class BackedExitedEventListener {
		private final List<RequestMonitor> fWaitForRMs = new ArrayList<RequestMonitor>();

		@DsfServiceEventHandler
		public void eventDispatched(BackendStateChangedEvent event) {
			if (event.getState() == IMIBackend.State.TERMINATED && event.getBackendId().equals(fMIBackend.getId())) {
				fExitCode.set(fMIBackend.getExitCode());
				for (RequestMonitor rm : fWaitForRMs) {
					rm.done();
				}
				fWaitForRMs.clear();
			}
		}

		void dispose() {
			for (RequestMonitor rm : fWaitForRMs) {
				rm.done(new Status(IStatus.ERROR, GdbPlugin.PLUGIN_ID, IDsfStatusConstants.REQUEST_FAILED,
						"Backend terminate event never received", null)); //$NON-NLS-1$
			}
			fWaitForRMs.clear();
		}
	}

	public GDBBackendProcessWithoutIO(ICommandControlService commandControl, IMIBackend backend) throws IOException {
		fControl = commandControl;
		fSession = fControl.getSession();
		fMIBackend = backend;
		if (fMIBackend.getState() == IMIBackend.State.TERMINATED) {
			fExitCode.set(fMIBackend.getExitCode());
		}
		fExitedEventListener = new BackedExitedEventListener();
		getSession().addServiceEventListener(fExitedEventListener, null);
	}

	public void dispose() {
		fDisposed = true;
		fExitedEventListener.dispose();
		getSession().removeServiceEventListener(fExitedEventListener);
	}

	protected DsfSession getSession() {
		return fSession;
	}

	protected boolean isDisposed() {
		return fDisposed;
	}

	@Override
	public void destroy() {
		try {
			// This is called when the user terminates the "gdb" process
			// node in the Debug view
			getSession().getExecutor().execute(new DsfRunnable() {
				@Override
				public void run() {
					if (!DsfSession.isSessionActive(getSession().getId()))
						return;
					if (isDisposed())
						return;

					if (fControl instanceof IGDBControl) {
						((IGDBControl) fControl).terminate(new RequestMonitor(getSession().getExecutor(), null));
					}
				}
			});
		} catch (RejectedExecutionException e) {
			// Session disposed.
		}
	}

	@Override
	public int waitFor() throws InterruptedException {
		if (!DsfSession.isSessionActive(getSession().getId())) {
			return fExitCode.get();
		}

		try {
			Query<Void> query = new Query<Void>() {
				@Override
				protected void execute(final DataRequestMonitor<Void> rm) {
					if (!DsfSession.isSessionActive(getSession().getId()) || isDisposed()
							|| fMIBackend.getState() == IMIBackend.State.TERMINATED) {
						rm.done();
					} else {
						fExitedEventListener.fWaitForRMs.add(rm);
					}
				}
			};
			getSession().getExecutor().execute(query);
			query.get();
		} catch (RejectedExecutionException e) {
		} catch (ExecutionException e) {
		}
		return fExitCode.get();
	}

	@Override
	public int exitValue() {
		if (!DsfSession.isSessionActive(getSession().getId())) {
			return fExitCode.get();
		}
		try {
			getSession().getExecutor().submit(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					if (fMIBackend.getState() != IMIBackend.State.TERMINATED) {
						throw new IllegalThreadStateException("Backend Process has not exited"); //$NON-NLS-1$
					}
					return null;
				}
			}).get();
		} catch (RejectedExecutionException e) {
		} catch (InterruptedException e) {
		} catch (ExecutionException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			}
		}
		return fExitCode.get();
	}

	@Override
	public InputStream getErrorStream() {
		// Streams are handled directly by the real process.
		// This class is just representation for the launch, without IO.
		return null;
	}

	@Override
	public InputStream getInputStream() {
		// Streams are handled directly by the real process.
		// This class is just representation for the launch, without IO.
		return null;
	}

	@Override
	public OutputStream getOutputStream() {
		// Streams are handled directly by the real process.
		// This class is just representation for the launch, without IO.
		return null;
	}
}
