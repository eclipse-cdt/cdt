/*******************************************************************************
 * Copyright (c) 2013 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Doug Schaefer (QNX) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.core.templateengine.process.processes;

import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;

/**
 * @author dschaefer
 * @since 5.5
 *
 */
public class AddNature extends ProcessRunner {

	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor)
			throws ProcessFailureException {
		IProject project = null;
		String natureId = null;

		for (ProcessArgument arg : args) {
			String argName = arg.getName();
			if (argName.equals("projectName")) //$NON-NLS-1$
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(arg.getSimpleValue());
			else if (argName.equals("natureId")) //$NON-NLS-1$
				natureId = arg.getSimpleValue();
		}

		if (project == null)
			throw new ProcessFailureException(
					getProcessMessage(processId, IStatus.ERROR, Messages.getString("AddNature.noProject"))); //$NON-NLS-1$

		if (natureId == null)
			throw new ProcessFailureException(
					getProcessMessage(processId, IStatus.ERROR, Messages.getString("AddNature.noNature"))); //$NON-NLS-1$

		try {
			CProjectNature.addNature(project, natureId, monitor);
		} catch (CoreException e) {
			throw new ProcessFailureException(e);
		}
	}

}
