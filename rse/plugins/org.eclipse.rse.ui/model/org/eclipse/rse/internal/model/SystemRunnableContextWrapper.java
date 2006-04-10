/********************************************************************************
 * Copyright (c) 2002, 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.internal.model;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.operation.ModalContext;
import org.eclipse.swt.widgets.Display;

/**
 * Runnable context to reuse an existing progress monitor.  This allows us to easily 
 * resuse a progress monitor when we know that a larger task may invoke other code
 * that also uses progress montiors but doesn't know that it is actually a subtask.  
 * <p>
 * <b>Note:</b> Any task that reuses the monitor must be a proper subtask of the parent
 * task.  i.e The subtask must start after the parent task and stop before the parent 
 * task.
 */
public class SystemRunnableContextWrapper implements IRunnableContext {

	// Inner class for running the first runable, used for grabbing the progress monitor during 
	// execution of the first runnable context.
	private class SystemRunnableWithProgress implements IRunnableWithProgress
	{
		private IRunnableWithProgress _runnable;
		private IProgressMonitor monitor;
		
		/**
		 * Constructor
		 */
		private SystemRunnableWithProgress(IRunnableWithProgress runnable)
		{
			_runnable = runnable;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException 
		{
			// invoked through an IRunnableContext so we need to grab the progress monitor and forward the request
			// store the monitor
			this.monitor = monitor;
			
			// start executing the real runnable
			_runnable.run(monitor); 
		}
		
	} // end of class SystemRunnableWithProgress

	// Inner class for running second instance
	private class InternalRunnable implements Runnable
	{
		private Exception _e;
		private boolean _fork;
		private IRunnableWithProgress _runnable;
		private IProgressMonitor _monitor;
		
		private InternalRunnable(IRunnableWithProgress runnable, boolean fork, IProgressMonitor monitor)
		{
			_runnable = runnable;
			_fork = fork;
			_monitor = monitor;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		public void run() 
		{
			Display display = Display.getCurrent();
			try
			{
				ModalContext.run(_runnable, _fork, _monitor, display);
			}
			catch (InterruptedException e)
			{
				_e = e;
			}
			catch (InvocationTargetException e)
			{
				_e = e;
			}
		}
		
		private Exception getException()
		{
			return _e;
		}
	}

	// Instance variable
	private IRunnableContext _runnableContext;
	private SystemRunnableWithProgress _runnable;
	
	/**
	 * Constructor
	 */
	public SystemRunnableContextWrapper(IRunnableContext runnableContext)
	{
		_runnableContext = runnableContext;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableContext#run(boolean, boolean, org.eclipse.jface.operation.IRunnableWithProgress)
	 */
	public void run(boolean fork, boolean cancelable, IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException 
	{
		boolean first = false;
		
		synchronized(this)
		{
			if (_runnable == null)
			{
				// first code to use this runable context so we need to use the SystemRunnableWithProgress to capture the monitor
				_runnable = new SystemRunnableWithProgress(runnable);
				first = true;			
			}
		}

		if (first)
		{			
			_runnableContext.run(fork, cancelable, _runnable);
		}
		else
		{		
			// wait for monitor to be initialized by the first task before proceeding
			while (_runnable.monitor == null)
			{
				Thread.sleep(100);
			}

			// we want to reuse the montior from the previous runnable
			SubProgressMonitor submonitor = new SubProgressMonitor(_runnable.monitor, 0);
			
			Display display = Display.getCurrent();
			if (display != null)
			{
				ModalContext.run(runnable, fork, submonitor, display);
			}
			else
			{
				InternalRunnable internalRunnable = new InternalRunnable(runnable, fork, submonitor);
				Display.getDefault().syncExec(internalRunnable);
				Exception e = internalRunnable.getException();
				if (e != null)
				{
					if (e instanceof InterruptedException)
						throw (InterruptedException) e;
					else if (e instanceof InvocationTargetException)
						throw (InvocationTargetException) e;
				}
			}
		}		
	}

}