/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.browser.util;

import java.util.ArrayList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;

/**
 * A wrapper around multiple progress monitors which forwards
 * <code>IProgressMonitor</code> and <code>IProgressMonitorWithBlocking</code>
 * methods to the wrapped progress monitors.
 */
public class ProgressMonitorMultiWrapper implements IProgressMonitor, IProgressMonitorWithBlocking {

	private double internalWork;
	private int totalWork;
	private int work;
	private String taskName;
	private String subtaskName;
	private boolean isCanceled= false;
	private boolean blocked= false;

	/** The wrapped progress monitors. */
	private final ArrayList fProgressMonitors= new ArrayList(2);

	/**
	 * Creates a new monitor wrapper.
	 */
	public ProgressMonitorMultiWrapper() {
	}
	
	/**
	 * Creates a new monitor wrapper around the given monitor.
	 */
	public ProgressMonitorMultiWrapper(IProgressMonitor monitor) {
		addProgressMonitor(monitor);
	}

	/* 
	 * @see IProgressMonitor#beginTask
	 */
	public void beginTask(String name, int totalWork) {
		taskName= name;
		this.totalWork= totalWork;

		// Clone the monitors since they could remove themselves when called 
		ArrayList monitors= (ArrayList) fProgressMonitors.clone();
		for (int i= 0; i < monitors.size(); i++) {
			IProgressMonitor monitor= (IProgressMonitor) monitors.get(i);
			monitor.beginTask(name, totalWork);
		}
	}
	
	/*
	 * @see IProgressMonitor#setTaskName
	 */
	public void setTaskName(String name) {
		taskName= name;

		// Clone the monitors since they could remove themselves when called 
		ArrayList monitors= (ArrayList) fProgressMonitors.clone();
		for (int i= 0; i < monitors.size(); i++) {
			IProgressMonitor monitor= (IProgressMonitor) monitors.get(i);
			monitor.setTaskName(name);
		}
	}
	
	/*
	 * @see IProgressMonitor#subTask
	 */
	public void subTask(String name) {
		subtaskName= name;

		// Clone the monitors since they could remove themselves when called 
		ArrayList monitors= (ArrayList) fProgressMonitors.clone();
		for (int i= 0; i < monitors.size(); i++) {
			IProgressMonitor monitor= (IProgressMonitor) monitors.get(i);
			monitor.subTask(name);
		}
	}
	
	/*
	 * @see IProgressMonitor#worked
	 */
	public void worked(int work) {
		this.work= work;

		// Clone the monitors since they could remove themselves when called 
		ArrayList monitors= (ArrayList) fProgressMonitors.clone();
		for (int i= 0; i < monitors.size(); i++) {
			IProgressMonitor monitor= (IProgressMonitor) monitors.get(i);
			monitor.worked(work);
		}
	}
	
	/*
	 * @see IProgressMonitor#internalWorked
	 */
	public void internalWorked(double work) {
		internalWork= work;

		// Clone the monitors since they could remove themselves when called 
		ArrayList monitors= (ArrayList) fProgressMonitors.clone();
		for (int i= 0; i < monitors.size(); i++) {
			IProgressMonitor monitor= (IProgressMonitor) monitors.get(i);
			monitor.internalWorked(work);
		}
	}
	
	/*
	 * @see IProgressMonitor#done
	 */
	public void done() {
		// Clone the monitors since they could remove themselves when called 
		ArrayList monitors= (ArrayList) fProgressMonitors.clone();
		for (int i= 0; i < monitors.size(); i++) {
			IProgressMonitor monitor= (IProgressMonitor) monitors.get(i);
			monitor.done();
		}
	}
	
	/*
	 * @see IProgressMonitor#setCanceled
	 */
	public void setCanceled(boolean canceled) {
		isCanceled= canceled;

		// Clone the monitors since they could remove themselves when called 
		ArrayList monitors= (ArrayList) fProgressMonitors.clone();
		for (int i= 0; i < monitors.size(); i++) {
			IProgressMonitor monitor= (IProgressMonitor) monitors.get(i);
			monitor.setCanceled(canceled);
		}
	}

	/*
	 * @see IProgressMonitor#isCanceled
	 */
	public boolean isCanceled() {
		// Clone the monitors since they could remove themselves when called 
		ArrayList monitors= (ArrayList) fProgressMonitors.clone();
		for (int i= 0; i < monitors.size(); i++) {
			IProgressMonitor monitor= (IProgressMonitor) monitors.get(i);
			isCanceled |= monitor.isCanceled();
		}
		return isCanceled;
	}
	
	/*
	 * @see IProgressMonitor#setBlocked
	 */
	public void setBlocked(IStatus reason) {
		blocked= true;

		// Clone the monitors since they could remove themselves when called 
		ArrayList monitors= (ArrayList) fProgressMonitors.clone();
		for (int i= 0; i < monitors.size(); i++) {
			IProgressMonitor monitor= (IProgressMonitor) monitors.get(i);
			if (monitor instanceof IProgressMonitorWithBlocking)
				 ((IProgressMonitorWithBlocking) monitor).setBlocked(reason);
		}
	}
	
	/*
	 * @see IProgressMonitor#clearBlocked
	 */
	public void clearBlocked() {
		blocked= false;

		// Clone the monitors since they could remove themselves when called 
		ArrayList monitors= (ArrayList) fProgressMonitors.clone();
		for (int i= 0; i < monitors.size(); i++) {
			IProgressMonitor monitor= (IProgressMonitor) monitors.get(i);
			if (monitor instanceof IProgressMonitorWithBlocking)
				 ((IProgressMonitorWithBlocking) monitor).clearBlocked();
		}
	}
	
	/*
	 * brings monitor up to date
	 */
	private void syncUpMonitor(IProgressMonitor monitor) {
		if (totalWork > 0) {
			monitor.beginTask(taskName, totalWork);
			monitor.worked(work);
			monitor.internalWorked(internalWork);
			if (subtaskName != null && subtaskName.length() > 0)
				monitor.subTask(subtaskName);
			if (blocked && monitor instanceof IProgressMonitorWithBlocking)
				 ((IProgressMonitorWithBlocking) monitor).setBlocked(null);
		}
	}
	
	/**
	 * Adds a monitor to the list of wrapped monitors.
	 */
	public synchronized void addProgressMonitor(IProgressMonitor monitor) {
		if (fProgressMonitors.indexOf(monitor) == -1) {
			syncUpMonitor(monitor);
			fProgressMonitors.add(monitor);
		}
	}
	
	/**
	 * Removes a monitor from the list of wrapped monitors.
	 */
	public synchronized void removeProgressMonitor(IProgressMonitor monitor) {
		int index = fProgressMonitors.indexOf(monitor);
		if (index != -1) {
			fProgressMonitors.remove(index);
		}
	}

	/**
	 * Returns the wrapped progress monitors.
	 *
	 * @return the wrapped progress monitors
	 */
	public IProgressMonitor[] getWrappedProgressMonitors() {
		return (IProgressMonitor[]) fProgressMonitors.toArray();
	}
}
