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
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.templateengine;

import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.cdt.make.core.IMakeCommonBuildInfo;
import org.eclipse.cdt.make.core.IMakeTarget;
import org.eclipse.cdt.make.core.IMakeTargetManager;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A step that can be used by the New Project template.xml file to add make targets to
 * newly created C/C++ projects.  E.g.,
 * <pre>
 *  &lt;process type="org.eclipse.cdt.make.core.AddMakeTarget">
 *      &lt;simple name="projectName" value="$(projectName)"/>
 *      &lt;simple name="targetName" value="build-debug"/>
 *      &lt;simple name="makeTarget" value="debug"/>
 *  &lt;/process>
 * </pre>
 * The rule's parameters are used to populate fields in the "Create|Modify Make Target"
 * dialog box (which is opened from the Make Target view).  The two mandatory parameters
 * are projectName and targetName.  There are also three optional parameters:
 * <p>
 * <u>makeTarget</u>: The name of the make target to run, defaults to targetName<br>
 * <u>buildCommand</u>: The build command to execute, e.g., "make"<br>
 * <u>buildArguments</u>: The arguments that should be passed to the build command, e.g., "-s"<br>
 */
public class AddMakeTarget extends ProcessRunner {

	private static final String BUILDER_ID = "org.eclipse.cdt.build.MakeTargetBuilder"; //$NON-NLS-1$

	private static final String PROJECTNAME_VARNAME = "projectName"; //$NON-NLS-1$
	private static final String TARGETNAME_VARNAME = "targetName"; //$NON-NLS-1$
	private static final String MAKETARGET_VARNAME = "makeTarget"; //$NON-NLS-1$
	private static final String BUILDCOMMAND_VARNAME = "buildCommand"; //$NON-NLS-1$
	private static final String BUILDARGUMENTS_COMMAND_VARNAME = "buildArguments"; //$NON-NLS-1$

	private static final String BUILDCOMMAND_DEFAULT = "make"; //$NON-NLS-1$
	private static final String BUILDARGUMENTS_DEFAULT = ""; //$NON-NLS-1$

	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor)
			throws ProcessFailureException {
		IProject project = null;
		String targetName = null;
		String makeTarget = null;
		String buildCommand = null;
		String buildArguments = null;

		for (ProcessArgument arg : args) {
			String argName = arg.getName();
			if (PROJECTNAME_VARNAME.equals(argName))
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(arg.getSimpleValue());
			else if (TARGETNAME_VARNAME.equals(argName))
				targetName = arg.getSimpleValue();
			else if (MAKETARGET_VARNAME.equals(argName))
				makeTarget = arg.getSimpleValue();
			else if (BUILDCOMMAND_VARNAME.equals(argName))
				buildCommand = arg.getSimpleValue();
			else if (BUILDARGUMENTS_COMMAND_VARNAME.equals(argName))
				buildArguments = arg.getSimpleValue();
		}

		if (project == null)
			throw missingArgException(processId, PROJECTNAME_VARNAME);
		if (targetName == null)
			throw missingArgException(processId, TARGETNAME_VARNAME);

		IMakeTargetManager makeTargetManager = MakeCorePlugin.getDefault().getTargetManager();
		try {
			IMakeTarget target = makeTargetManager.createTarget(project, targetName, BUILDER_ID);

			target.setBuildAttribute(IMakeTarget.BUILD_TARGET, makeTarget == null ? targetName : makeTarget);

			target.setUseDefaultBuildCmd(buildCommand == null);
			target.setBuildAttribute(IMakeCommonBuildInfo.BUILD_COMMAND,
					buildCommand == null ? BUILDCOMMAND_DEFAULT : buildCommand);
			target.setBuildAttribute(IMakeCommonBuildInfo.BUILD_ARGUMENTS,
					buildArguments == null ? BUILDARGUMENTS_DEFAULT : buildArguments);

			makeTargetManager.addTarget(target);
		} catch (CoreException e) {
			throw new ProcessFailureException(e);
		}
	}
}
