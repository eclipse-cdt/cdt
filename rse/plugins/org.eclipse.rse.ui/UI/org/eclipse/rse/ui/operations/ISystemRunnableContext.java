/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.operations;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.widgets.Shell;

/**
 * Interface for a system runnable context.
 */
public interface ISystemRunnableContext {
	
	/**
	 * Runs the given runnable in the context of the receiver. By default, the
	 * progress is provided by the active workbench window but subclasses may
	 * override this to provide progress in some other way (through Progress view using Eclipse Job support).
	 */
	public abstract void run(IRunnableWithProgress runnable) throws InvocationTargetException, InterruptedException;
		
	/**
	 * Returns a shell that can be used to prompt the user.
	 * @return a shell.
	 */
	public abstract Shell getShell();
}