/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

/**
 * Synchronously executes a {@link Job} while allowing user to cancel it if it takes too long.
 */
public final class BusyCursorJobRunner {
	/**
	 * Adapts a {@link Job} to be an {@link IRunnableWithProgress}.
	 */
	private static class JobRunnableWithProgressAdapter implements IRunnableWithProgress {
		private final Job job;

		/**
		 * Creates the {@link IRunnableWithProgress} from the {@link Job}.
		 */
		public JobRunnableWithProgressAdapter(Job job) {
			this.job = job;
		}

		@Override
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			IStatus result;
			try {
				monitor.beginTask(job.getName(), IProgressMonitor.UNKNOWN);
				result = executeAndWait(job, monitor);
			} catch (RuntimeException e) {
				throw new InvocationTargetException(e);
			}

			switch (result.getSeverity()) {
			case IStatus.CANCEL:
				throw new InterruptedException();

			case IStatus.ERROR:
				if (result.getException() instanceof OperationCanceledException) {
					throw new InterruptedException();
				}
				throw new InvocationTargetException(new CoreException(result));
			}
		}
	}

	/**
	 * Runs the given job and waits for it to finish. If executing in the UI thread, sets the cursor
	 * to busy while the job is being executed.
	 *
	 * @param job the job to execute
	 * @return the status reflecting the result of the job execution
	 */
	public static IStatus execute(Job job) {
		boolean inUiThread = Thread.currentThread() == Display.getDefault().getThread();
		if (inUiThread) {
			return busyCursorWhile(job);
		}
		return executeAndWait(job, new NullProgressMonitor());
	}

	private static IStatus busyCursorWhile(Job job) {
		try {
			IProgressService progressService = PlatformUI.getWorkbench().getProgressService();
			progressService.busyCursorWhile(new JobRunnableWithProgressAdapter(job));
		} catch (InterruptedException e) {
			return Status.CANCEL_STATUS; // Operation was cancelled.
		} catch (InvocationTargetException e) {
			Throwable targetException = e.getTargetException();
			if (targetException instanceof CoreException) {
				return ((CoreException) targetException).getStatus();
			}
			return new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID, e.getMessage(), e);
		}
		return Status.OK_STATUS;
	}

	private static IStatus executeAndWait(final Job job, IProgressMonitor monitor) {
		final IStatus[] statusHolder = new IStatus[1];

		IJobManager jobManager = Job.getJobManager();
		JobChangeAdapter listener = new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				super.done(event);
				if (event.getJob() == job) {
					synchronized (statusHolder) {
						statusHolder[0] = event.getResult();
						statusHolder.notifyAll();
					}
				}
			}
		};
		jobManager.addJobChangeListener(listener);
		job.schedule();

		try {
			synchronized (statusHolder) {
				while (statusHolder[0] == null) {
					try {
						statusHolder.wait(100);
						if (monitor.isCanceled()) {
							job.cancel();
						}
					} catch (InterruptedException e) {
						job.cancel();
						return Status.CANCEL_STATUS;
					}
				}

				return statusHolder[0];
			}
		} finally {
			monitor.done();
			jobManager.removeJobChangeListener(listener);
		}
	}

	private BusyCursorJobRunner() {
	}
}
