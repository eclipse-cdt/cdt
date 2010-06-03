/*******************************************************************************
 * Copyright (c) 2009, 2010 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Nokia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.executables;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * IProjectExecutablesProvider supplies a list of executables for a project
 * to the Executables Manager.
 * 
 * @author Warren Paul
 * @since 7.0
 * 
 */
public interface IProjectExecutablesProvider {

	/**
	 * Get the list of project natures that should be present in projects that
	 * this provider will get the list of executables for.  Since there could
	 * be any number of executable providers, the one that matches the given
	 * project natures the closest will be chosen.
	 * @return the list of project nature id's
	 */
	List<String> getProjectNatures();	
	
	/**
	 * Get the list of executables for the given project
	 * @param project the project to get the executables for
	 * @param monitor progress monitor
	 * @return the list of executables (which may be empty)
	 */
	List<Executable> getExecutables(IProject project, IProgressMonitor monitor);

	/**
	 * Remove the given executable.  Note that the project can be obtained from Executable.
	 * @param executable the executable to remove
	 * @param monitor progress monitor
	 * @return the status of the remove operation
	 */
	IStatus removeExecutable(Executable executable, IProgressMonitor monitor);

}
