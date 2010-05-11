/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.xlc.core.activator;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

/**
 * @author crecoskie
 *
 */
public class Activator extends Plugin {

	public static final String PLUGIN_ID = "org.eclipse.cdt.make.xlc.core"; //$NON-NLS-1$
	private static Activator fInstance;

	/**
	 * 
	 */
	public Activator() {
		super();
		if(fInstance == null) {
			fInstance = this;
		}
	}
	
	public static void log(String e) {
		log(createStatus(e));
	}
	
	public static void log(Throwable e) {
		log("Error", e); //$NON-NLS-1$
	}
	
	public static void log(String message, Throwable e) {
		Throwable nestedException;
		if (e instanceof CModelException 
				&& (nestedException = ((CModelException)e).getException()) != null) {
			e = nestedException;
		}
		log(createStatus(message, e));
	}

	public static IStatus createStatus(String msg) {
		return createStatus(msg, null);
	}

	public static IStatus createStatus(String msg, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, msg, e);
	}
	
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	private static Plugin getDefault() {
		return fInstance;
	}

}
