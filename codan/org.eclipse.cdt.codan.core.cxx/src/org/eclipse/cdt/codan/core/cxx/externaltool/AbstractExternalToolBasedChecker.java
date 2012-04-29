/*******************************************************************************
 * Copyright (c) 2012 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alex Ruiz (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.cxx.externaltool;

import java.net.URI;

import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.cxx.Activator;
import org.eclipse.cdt.codan.core.cxx.internal.externaltool.ExternalToolInvoker;
import org.eclipse.cdt.codan.core.model.AbstractCheckerWithProblemPreferences;
import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemLocation;
import org.eclipse.cdt.codan.core.model.IProblemLocationFactory;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.param.MapProblemPreference;
import org.eclipse.cdt.codan.core.param.RootProblemPreference;
import org.eclipse.cdt.codan.core.param.SharedRootProblemPreference;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.IMarkerGenerator;
import org.eclipse.cdt.core.ProblemMarkerInfo;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

/**
 * Base class for checkers that invoke external command-line tools to perform code checking.
 * <p>
 * A file, to be processed by this type of checker, must:
 * <ol>
 * <li>be in the current active editor</li>
 * <li>not have any unsaved changes</li>
 * </ol>
 * </p>
 * By default, implementations of this checker are not allowed to run while the user types, since
 * external tools cannot see unsaved changes.
 *
 * @since 2.1
 */
public abstract class AbstractExternalToolBasedChecker extends AbstractCheckerWithProblemPreferences
		implements IMarkerGenerator {
	private final IInvocationParametersProvider parametersProvider;
	private final ArgsSeparator argsSeparator;
	private final ConfigurationSettings settings;
	private final ExternalToolInvoker externalToolInvoker;
	private final RootProblemPreference preferences;

	/**
	 * Constructor.
	 * @param settings user-configurable external tool configuration settings.
	 */
	public AbstractExternalToolBasedChecker(ConfigurationSettings settings) {
		this(new InvocationParametersProvider(), new ArgsSeparator(), settings);
	}

	/**
	 * Constructor.
	 * @param parametersProvider provides the parameters to pass when invoking the external tool.
	 * @param argsSeparator separates the arguments to pass to the external tool executable. These
	 *        arguments are stored in a single {@code String}.
	 * @param settings user-configurable external tool configuration settings.
	 */
	public AbstractExternalToolBasedChecker(IInvocationParametersProvider parametersProvider,
			ArgsSeparator argsSeparator, ConfigurationSettings settings) {
		this.parametersProvider = parametersProvider;
		this.argsSeparator = argsSeparator;
		this.settings = settings;
		externalToolInvoker = new ExternalToolInvoker();
		preferences = new SharedRootProblemPreference();
	}

	/**
	 * Returns {@code false} because this checker cannot run "as you type" by default.
	 * @return {@code false}.
	 */
	@Override
	public boolean runInEditor() {
		return false;
	}

	@Override
	public boolean processResource(IResource resource) {
		process(resource);
		return false;
	}

	private void process(IResource resource) {
		try {
			InvocationParameters parameters = parametersProvider.createParameters(resource);
			if (parameters != null) {
				invokeExternalTool(parameters);
			}
		} catch (Throwable error) {
			logResourceProcessingFailure(error, resource);
		}
	}

	private void invokeExternalTool(InvocationParameters parameters) throws Throwable {
		updateConfigurationSettingsFromPreferences(parameters.getActualFile());
		IConsoleParser[] parsers = new IConsoleParser[] { createErrorParserManager(parameters) };
		try {
			externalToolInvoker.invoke(parameters, settings, argsSeparator, parsers);
		} catch (InvocationFailure error) {
			handleInvocationFailure(error, parameters);
		}
	}

	private void updateConfigurationSettingsFromPreferences(IResource fileToProcess) {
		IProblem problem = getProblemById(getReferenceProblemId(), fileToProcess);
		MapProblemPreference preferences = (MapProblemPreference) problem.getPreference();
		settings.updateValuesFrom(preferences);
	}

	private ErrorParserManager createErrorParserManager(InvocationParameters parameters) {
		IProject project = parameters.getActualFile().getProject();
		URI workingDirectory = URIUtil.toURI(parameters.getWorkingDirectory());
		return new ErrorParserManager(project, workingDirectory, this, getParserIDs());
	}

	/**
	 * @return the IDs of the parsers to use to parse the output of the external tool.
	 */
	protected abstract String[] getParserIDs();

	/**
	 * Handles a failure reported when invoking the external tool. This implementation simply
	 * logs the failure.
	 * @param error the reported failure.
	 * @param parameters the parameters passed to the external tool executable.
	 */
	protected void handleInvocationFailure(InvocationFailure error, InvocationParameters parameters) {
		logResourceProcessingFailure(error, parameters.getActualFile());
	}

	private void logResourceProcessingFailure(Throwable error, IResource resource) {
		String location = resource.getLocation().toOSString();
		String msg = String.format("Unable to process resource %s", location); //$NON-NLS-1$
		Activator.log(msg, error);
	}

	/**
	 * Returns the id of the problem used as reference to obtain this checker's preferences. All
	 * preferences in a external-tool-based checker are shared among its defined problems.
	 * @return  the id of the problem used as reference to obtain this checker's preferences.
	 */
	protected abstract String getReferenceProblemId();

	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		getLaunchModePreference(problem).enableInLaunchModes(
				CheckerLaunchMode.RUN_ON_DEMAND,
				CheckerLaunchMode.RUN_ON_FILE_OPEN,
				CheckerLaunchMode.RUN_ON_FILE_SAVE);
		addPreference(problem, settings.getPath());
		addPreference(problem, settings.getArgs());
	}

	private void addPreference(IProblemWorkingCopy problem, SingleConfigurationSetting<?> setting) {
		IProblemPreference descriptor = (IProblemPreference) setting.getDescriptor();
		addPreference(problem, descriptor, setting.getDefaultValue());
	}

	@Override
	protected void setDefaultPreferenceValue(IProblemWorkingCopy problem, String key,
			Object defaultValue) {
		MapProblemPreference map = getTopLevelPreference(problem);
		map.setChildValue(key, defaultValue);
	}

	@Override
	public RootProblemPreference getTopLevelPreference(IProblem problem) {
		RootProblemPreference map = (RootProblemPreference) problem.getPreference();
		if (map == null) {
			map = preferences;
			if (problem instanceof IProblemWorkingCopy) {
				((IProblemWorkingCopy) problem).setPreference(map);
			}
		}
		return map;
	}

	@Deprecated
	@Override
	public void addMarker(IResource file, int lineNumber, String description, int severity,
			String variableName) {
		addMarker(new ProblemMarkerInfo(file, lineNumber, description, severity, variableName));
	}

	@Override
	public void addMarker(ProblemMarkerInfo info) {
		reportProblem(getReferenceProblemId(), createProblemLocation(info), info.description);
	}

	protected IProblemLocation createProblemLocation(ProblemMarkerInfo info) {
		IProblemLocationFactory factory = CodanRuntime.getInstance().getProblemLocationFactory();
		return factory.createProblemLocation(
				(IFile) info.file, info.startChar, info.endChar, info.lineNumber);
	}
}
