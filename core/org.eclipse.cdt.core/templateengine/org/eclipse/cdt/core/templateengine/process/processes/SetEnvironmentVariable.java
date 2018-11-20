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
package org.eclipse.cdt.core.templateengine.process.processes;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.envvar.IContributedEnvironment;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescriptionManager;
import org.eclipse.cdt.core.templateengine.TemplateCore;
import org.eclipse.cdt.core.templateengine.process.ProcessArgument;
import org.eclipse.cdt.core.templateengine.process.ProcessFailureException;
import org.eclipse.cdt.core.templateengine.process.ProcessRunner;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A template process for setting an environment variable in all of the new project's
 * build configurations.  E.g.,
 * <pre>
 *  &lt;process type="org.eclipse.cdt.core.SetEnvironmentVariable">
 *      &lt;simple name="projectName" value="$(projectName)"/>
 *      &lt;complex-array name="variables">
 *          &lt;element>
 *              &lt;simple name="name" value="QMAKE"/>
 *              &lt;simple name="value" value="$(qmake)"/>
 *          &lt;/element>
 *      &lt;/complex-array>
 *  &lt;/process>
 * </pre>
 * This will create an environment variable called "QMAKE" and will set the value to
 * be the value entered in a field (called qmake) in the New Project wizard.
 *
 * @since 5.6
 */
public class SetEnvironmentVariable extends ProcessRunner {

	private static final String PROJECTNAME_VARNAME = "projectName"; //$NON-NLS-1$
	private static final String VARIABLES_VARNAME = "variables"; //$NON-NLS-1$
	private static final String VARIABLES_NAME_VARNAME = "name"; //$NON-NLS-1$
	private static final String VARIABLES_VALUE_VARNAME = "value"; //$NON-NLS-1$

	@Override
	public void process(TemplateCore template, ProcessArgument[] args, String processId, IProgressMonitor monitor)
			throws ProcessFailureException {

		IProject project = null;
		Map<String, String> envVars = new LinkedHashMap<>();

		for (ProcessArgument arg : args) {
			String argName = arg.getName();
			if (PROJECTNAME_VARNAME.equals(argName))
				project = ResourcesPlugin.getWorkspace().getRoot().getProject(arg.getSimpleValue());
			else if (VARIABLES_VARNAME.equals(argName)) {

				for (ProcessArgument[] envVarArgs : arg.getComplexArrayValue()) {
					String name = null;
					String value = null;

					for (ProcessArgument varArg : envVarArgs) {
						String varArgName = varArg.getName();
						if (VARIABLES_NAME_VARNAME.equals(varArgName))
							name = varArg.getSimpleValue();
						else if (VARIABLES_VALUE_VARNAME.equals(varArgName))
							value = varArg.getSimpleValue();
					}

					if (name == null)
						throw missingArgException(processId, VARIABLES_NAME_VARNAME);
					if (value == null)
						throw missingArgException(processId, VARIABLES_VALUE_VARNAME);

					envVars.put(name, value);
				}
			}
		}

		if (project == null)
			throw missingArgException(processId, PROJECTNAME_VARNAME);

		ICProjectDescriptionManager mngr = CoreModel.getDefault().getProjectDescriptionManager();
		ICProjectDescription des = mngr.getProjectDescription(project, true);
		ICConfigurationDescription[] configDescs = des.getConfigurations();
		IContributedEnvironment ice = CCorePlugin.getDefault().getBuildEnvironmentManager().getContributedEnvironment();

		for (Map.Entry<String, String> envVar : envVars.entrySet()) {
			String name = envVar.getKey();
			String value = envVar.getValue();

			for (ICConfigurationDescription configDesc : configDescs)
				ice.addVariable(name, value, IEnvironmentVariable.ENVVAR_REPLACE, null, configDesc);
		}

		try {
			mngr.setProjectDescription(project, des);
		} catch (CoreException e) {
			CCorePlugin.log(e);
		}
	}
}
