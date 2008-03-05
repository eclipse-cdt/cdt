/********************************************************************************
 * Copyright (c) 2008 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - [197167] adding notification and waiting for RSE model
 ********************************************************************************/
package org.eclipse.rse.internal.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.osgi.util.NLS;
import org.eclipse.rse.core.IRSEInitListener;
import org.eclipse.rse.core.IRSEModelInitializer;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.logging.Logger;

/**
 * The InitRSEJob is a job named "Initialize RSE". It is instantiated and run during 
 * RSE startup. It must not be run at any other time. The job restores the 
 * persistent form of the RSE model. Use the extension point 
 * org.eclipse.rse.core.modelInitializers to supplement the model once it is 
 * restored.
 */
public final class InitRSEJob extends Job {
	
	/**
	 * The name of this job. This is API. Clients may use this name to find this job by name.
	 */
	public final static String NAME = "Initialize RSE"; //$NON-NLS-1$
	
	private static InitRSEJob singleton = null;
	
	private boolean isComplete = false;
	private boolean isModelComplete = false;
	private Set listeners = new HashSet(10);
	private Logger logger = null;
	
	/**
	 * Returns the singleton instance of this job.
	 * @return the InitRSEJob instance for this workbench.
	 */
	public synchronized static InitRSEJob getInstance() {
		if (singleton == null) {
			singleton = new InitRSEJob();
		}
		return singleton;
	}
	
	private InitRSEJob() {
		super(NAME);
		logger = RSECorePlugin.getDefault().getLogger();
	}
	
	/**
	 * Adds a new listener to the set of listeners to be notified when initialization phases complete.
	 * If the listener is added after the phase has completed it will not be invoked.
	 * If the listener is already in the set it will not be added again.
	 * Listeners may be notified in any order.
	 * @param listener the listener to be added
	 */
	public void addInitListener(IRSEInitListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	/**
	 * Removes a listener to the set of listeners to be notified when phases complete.
	 * If the listener is not in the set this does nothing.
	 * @param listener the listener to be removed
	 */
	public void removeInitListener(IRSEInitListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	/**
	 * Notify all registered listeners of a phase completion
	 * @param phase the phase just completed.
	 */
	private void notifyListeners(int phase) {
		List myListeners = new ArrayList(listeners.size());
		synchronized (listeners) {
			myListeners.addAll(listeners);
		}
		for (Iterator z = myListeners.iterator(); z.hasNext();) {
			IRSEInitListener listener = (IRSEInitListener) z.next();
			try {
				listener.phaseComplete(phase);
			} catch (RuntimeException e) {
				logger.logError(RSECoreMessages.InitRSEJob_listener_ended_in_error, e);
			}
		}
	}
	
	public IStatus run(IProgressMonitor monitor) {
		IStatus result = Status.OK_STATUS;
		Logger logger = RSECorePlugin.getDefault().getLogger();
		// get and initialize the profile manager
		RSECorePlugin.getTheSystemProfileManager();
		isModelComplete = true;
		notifyListeners(RSECorePlugin.INIT_MODEL);
		// instantiate initializers
		List initializers = new ArrayList(10);
		IConfigurationElement[] elements = Platform.getExtensionRegistry().getConfigurationElementsFor("org.eclipse.rse.core.modelInitializers"); //$NON-NLS-1$
		IStatus status = Status.OK_STATUS;
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement element = elements[i];
			String initializerName = element.getAttribute("class"); //$NON-NLS-1$
			try {
				IRSEModelInitializer initializer = (IRSEModelInitializer) element.createExecutableExtension("class"); //$NON-NLS-1$
				initializers.add(initializer);
			} catch (CoreException e) {
				String message = NLS.bind(RSECoreMessages.InitRSEJob_initializer_failed_to_load, initializerName); 
				logger.logError(message, e);
				status = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, message, e);
			}
			if (result.getSeverity() < status.getSeverity()) {
				result = status;
			}
		}
		// run initializers
		monitor.beginTask(RSECoreMessages.InitRSEJob_initializing_rse, elements.length); 
		for (Iterator z = initializers.iterator(); z.hasNext() && !monitor.isCanceled();) {
			IRSEModelInitializer initializer = (IRSEModelInitializer) z.next();
			IProgressMonitor submonitor = new SubProgressMonitor(monitor, 1);
			String initializerName = initializer.getClass().getName();
			try {
				status = initializer.run(submonitor);
				if (status.getSeverity() < IStatus.ERROR) {
					try {
						while (!initializer.isComplete()) {
							String message = NLS.bind(RSECoreMessages.InitRSEJob_waiting_for_initializer, initializerName); 
							logger.logInfo(message);
							Thread.sleep(1000l); // wait 1 second
						}
					} catch (InterruptedException e) {
						String message = NLS.bind(RSECoreMessages.InitRSEJob_initializer_interrupted, initializerName); 
						logger.logWarning(message, e);
						status = new Status(IStatus.WARNING, RSECorePlugin.PLUGIN_ID, message);
					}
				}
			} catch (RuntimeException e) {
				String message = NLS.bind(RSECoreMessages.InitRSEJob_initializer_ended_in_error, initializerName); 
				logger.logError(message, e);
				status = new Status(IStatus.ERROR, RSECorePlugin.PLUGIN_ID, message, e);
			}
			if (result.getSeverity() < status.getSeverity()) {
				result = status;
			}
			submonitor.done();
		}
		if (monitor.isCanceled()) {
			result = Status.CANCEL_STATUS;
		} else {
			monitor.done();
		}
		// finish up
		isComplete = true;
		notifyListeners(RSECorePlugin.INIT_ALL);
		return result;
	}

	/**
	 * Waits until the job is completed.
	 * @return the status of the job upon its completion.
	 * @throws InterruptedException if the job is interrupted while waiting.
	 */
	public IStatus waitForCompletion() throws InterruptedException {
		IStatus result = Status.OK_STATUS;
		while (!isComplete(RSECorePlugin.INIT_ALL)) {
			try {
				if (getState() != Job.RUNNING) {
					String message = NLS.bind(RSECoreMessages.InitRSEJob_waiting_for_job, NAME); 
					logger.logInfo(message);
					Thread.sleep(1000l);
				} else {
					String message = NLS.bind(RSECoreMessages.InitRSEJob_joining_job, NAME); 
					logger.logInfo(message);
					join();
				}
			} catch (InterruptedException e) {
				String message = NLS.bind(RSECoreMessages.InitRSEJob_job_interrupted, NAME); 
				logger.logError(message, e);
				throw e;
			}
		}
		result = getResult();
		return result;
	}

	/**
	 * @param phase the phase for which completion is requested. 
	 * Phases are defined in {@link RSECorePlugin}.
	 * @return true if this phase has completed.
	 * @throws IllegalArgumentException if the phase is undefined.
	 * @see RSECorePlugin#INIT_ALL
	 * @see RSECorePlugin#INIT_MODEL
	 */
	public boolean isComplete(int phase) {
		boolean result = false;
		switch (phase) {
		case RSECorePlugin.INIT_MODEL:
			result = isModelComplete;
			break;
		case RSECorePlugin.INIT_ALL:
			result = isComplete;
			break;
		default:
			throw new IllegalArgumentException("undefined phase"); //$NON-NLS-1$
		}
		return result;
	}
	
}