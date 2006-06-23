/*******************************************************************************
 * Copyright (c) 2000, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

public class CCProjectNature extends CProjectNature {

	public static final String CC_NATURE_ID= CCorePlugin.PLUGIN_ID + ".ccnature"; //$NON-NLS-1$

	public static void addCCNature(IProject project, IProgressMonitor mon) throws CoreException {
		addNature(project, CC_NATURE_ID, mon);
	}
	
	public static void removeCCNature(IProject project, IProgressMonitor mon) throws CoreException {
		removeNature(project, CC_NATURE_ID, mon);
	}
	
	/**
	 * Checks to ensure that a cnature already exists,
	 * if not throw a CoreException. Does NOT add a default builder
     * @see IProjectNature#configure
     */
    public void configure() throws CoreException {
    	if (!getProject().hasNature(CProjectNature.C_NATURE_ID)){
    		IStatus status = new Status(IStatus.ERROR, 
    									CCorePlugin.PLUGIN_ID, 
    									CCorePlugin.CDT_PROJECT_NATURE_ID_MISMATCH, 
    									CCorePlugin.getResourceString("CCProjectNature.exception.noNature"), null); // $NON_NLS //$NON-NLS-1$
    		throw new CoreException(status);
    	}
    }

}
