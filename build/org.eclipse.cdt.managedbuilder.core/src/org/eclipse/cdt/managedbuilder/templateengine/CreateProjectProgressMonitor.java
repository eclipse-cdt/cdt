/*******************************************************************************
 * Copyright (c) 2007, 2010 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.templateengine;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CreateProjectProgressMonitor extends NullProgressMonitor {

	/**
	 * Constructs a new progress monitor.
	 */
	public CreateProjectProgressMonitor() {
		super();
	}
	
	public CreateProjectProgressMonitor(String taskName) {
		super();
		super.setTaskName(taskName);
	}
	
	/**
	 * Override this method to do something
	 * with the name of the task.
	 * 
	 * @see IProgressMonitor#setTaskName(String)
	 */
	@Override
	public void setTaskName(String name) {
		super.setTaskName(name);
	}

	/**
	 * This implementation sets the value of an internal state variable.
	 *
	 * @see IProgressMonitor#isCanceled()
	 * @see IProgressMonitor#setCanceled(boolean)
	 */
	@Override
	public void setCanceled(boolean cancelled) {
		super.setCanceled(cancelled);
	}

	/**
	 * This implementation returns the value of the internal 
	 * state variable set by <code>setCanceled</code>.
	 *
	 * @see IProgressMonitor#isCanceled()
	 * @see IProgressMonitor#setCanceled(boolean)
	 */
	@Override
	public boolean isCanceled() {
		return super.isCanceled();
	}



}
