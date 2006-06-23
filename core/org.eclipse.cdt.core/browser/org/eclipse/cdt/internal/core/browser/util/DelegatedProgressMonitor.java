/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.util;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IProgressMonitorWithBlocking;
import org.eclipse.core.runtime.IStatus;

/**
 * A wrapper around one or more progress monitors.  Forwards
 * <code>IProgressMonitor</code> and <code>IProgressMonitorWithBlocking</code>
 * methods to the delegate monitors.
 */
public class DelegatedProgressMonitor implements IProgressMonitor, IProgressMonitorWithBlocking {

	private static int INITIAL_DELEGATE_COUNT = 2;
	private final ArrayList fDelegateList = new ArrayList(INITIAL_DELEGATE_COUNT);
	String fTaskName;
	String fSubTask;
	int fTotalWork;
	private double fWorked;
	private boolean fIsBlocked;
	boolean fIsCanceled;
	
	/**
	 * Creates a new delegated monitor.
	 */
	public DelegatedProgressMonitor() {
		init();
	}
	
	/**
	 * Creates a new delegated monitor, and adds a delegate.
	 */
	public DelegatedProgressMonitor(IProgressMonitor delegate) {
		init();
		addDelegate(delegate);
	}
	
	/**
	 * Resets delegated monitor to initial state.
	 */
	public synchronized void init() {
		fTaskName= null;
		fSubTask= null;
		fTotalWork= IProgressMonitor.UNKNOWN;
		fWorked= 0.0f;
		fIsBlocked= false;
		fIsCanceled= false;
	}
	
	/*
	 * @see IProgressMonitor#beginTask
	 */
	public synchronized void beginTask(String name, int totalWork) {
		fTaskName = name;
		fTotalWork = totalWork;
		visitDelegates(new IDelegateVisitor() {
			public void visit(IProgressMonitor delegate) {
				delegate.beginTask(fTaskName, fTotalWork);
			}
		});
	}
	
	/*
	 * @see IProgressMonitor#done
	 */
	public synchronized void done() {
		visitDelegates(new IDelegateVisitor() {
			public void visit(IProgressMonitor delegate) {
				delegate.done();
			}
		});
	}
	
	/*
	 * @see IProgressMonitor#setTaskName
	 */
	public synchronized void setTaskName(String name) {
		fTaskName = name;
		visitDelegates(new IDelegateVisitor() {
			public void visit(IProgressMonitor delegate) {
				delegate.setTaskName(fTaskName);
			}
		});
	}

	/*
	 * @see IProgressMonitor#subTask
	 */
	public synchronized void subTask(String name) {
		fSubTask = name;
		visitDelegates(new IDelegateVisitor() {
			public void visit(IProgressMonitor delegate) {
				delegate.subTask(fSubTask);
			}
		});
	}
	
	/*
	 * @see IProgressMonitor#worked
	 */
	public void worked(int work) {
		internalWorked(work);
	}
	
	/*
	 * @see IProgressMonitor#internalWorked
	 */
	public synchronized void internalWorked(double internalWork) {
		fWorked += internalWork;
		final double fInternalWork = internalWork;
		visitDelegates(new IDelegateVisitor() {
			public void visit(IProgressMonitor delegate) {
				delegate.internalWorked(fInternalWork);
			}
		});
	}

	/*
	 * @see IProgressMonitor#isCanceled
	 */
	public synchronized boolean isCanceled() {
		visitDelegates(new IDelegateVisitor() {
			public void visit(IProgressMonitor delegate) {
				fIsCanceled |= delegate.isCanceled();
			}
		});
		return fIsCanceled;
	}

	/*
	 * @see IProgressMonitor#setCanceled
	 */
	public synchronized void setCanceled(boolean canceled) {
		fIsCanceled = canceled;
		visitDelegates(new IDelegateVisitor() {
			public void visit(IProgressMonitor delegate) {
				delegate.setCanceled(fIsCanceled);
			}
		});
	}

	/*
	 * @see IProgressMonitor#setBlocked
	 */
	public synchronized void setBlocked(IStatus reason) {
		fIsBlocked = true;
		final IStatus fReason = reason;
		visitDelegates(new IDelegateVisitor() {
			public void visit(IProgressMonitor delegate) {
				if (delegate instanceof IProgressMonitorWithBlocking)
					((IProgressMonitorWithBlocking) delegate).setBlocked(fReason);
			}
		});
	}
	
	/*
	 * @see IProgressMonitor#clearBlocked
	 */
	public synchronized void clearBlocked() {
		fIsBlocked = false;
		visitDelegates(new IDelegateVisitor() {
			public void visit(IProgressMonitor delegate) {
				if (delegate instanceof IProgressMonitorWithBlocking)
					((IProgressMonitorWithBlocking) delegate).clearBlocked();
			}
		});
	}
	
	/**
	 * Adds a delegate.
	 */
	public synchronized void addDelegate(IProgressMonitor delegate) {
		if (fDelegateList.indexOf(delegate) == -1) {
			if (fTaskName != null)
				syncUp(delegate);
			fDelegateList.add(delegate);
		}
	}
	
	/**
	 * Brings delegate in sync with current progress.
	 */
	private void syncUp(IProgressMonitor delegate) {
		delegate.beginTask(fTaskName, fTotalWork);
		delegate.internalWorked(fWorked);
		if (fSubTask != null && fSubTask.length() > 0)
			delegate.subTask(fSubTask);
		if (fIsBlocked && delegate instanceof IProgressMonitorWithBlocking)
			 ((IProgressMonitorWithBlocking) delegate).setBlocked(null);
	}

	/**
	 * Removes a delegate.
	 */
	public synchronized void removeDelegate(IProgressMonitor delegate) {
		int index = fDelegateList.indexOf(delegate);
		if (index != -1) {
			fDelegateList.remove(index);
		}
	}

	/**
	 * Removes all delegates.
	 */
	public synchronized void removeAllDelegates() {
		fDelegateList.clear();
	}

	/**
	 * Returns the delegate list.
	 *
	 * @return An array of progress monitors added using <code>addDelegate()</code>.
	 */
	public synchronized IProgressMonitor[] getDelegates() {
		return (IProgressMonitor[]) fDelegateList.toArray();
	}

	/**
	 * Defines a delegate visitor.
	 */
	private static interface IDelegateVisitor {
		public void visit(IProgressMonitor delegate);
	}

	/**
	 * Visits each delegate in the delegates list.
	 */
	private void visitDelegates(IDelegateVisitor visitor) {
		// Clone the delegates since they could remove themselves when called 
		ArrayList delegatesList = (ArrayList) fDelegateList.clone();
		for (Iterator i = delegatesList.iterator(); i.hasNext(); ) {
			IProgressMonitor delegate = (IProgressMonitor) i.next();
			visitor.visit(delegate);
		}
	}
}
