// Copyright 2012 Google Inc. All Rights Reserved.

package org.eclipse.cdt.codan.core.model;

import java.util.List;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.externaltool.AbstractOutputParser;
import org.eclipse.cdt.codan.core.externaltool.ConfigurationSettings;
import org.eclipse.cdt.codan.core.externaltool.IArgsSeparator;
import org.eclipse.cdt.codan.core.externaltool.IConsolePrinterProvider;
import org.eclipse.cdt.codan.core.externaltool.IInvocationParametersProvider;
import org.eclipse.cdt.codan.core.externaltool.IProblemDisplay;
import org.eclipse.cdt.codan.core.externaltool.ISupportedResourceVerifier;
import org.eclipse.cdt.codan.core.externaltool.InvocationFailure;
import org.eclipse.cdt.codan.core.externaltool.InvocationParameters;
import org.eclipse.cdt.codan.core.externaltool.SingleConfigurationSetting;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.param.MapProblemPreference;
import org.eclipse.cdt.codan.core.param.RootProblemPreference;
import org.eclipse.cdt.codan.core.param.SharedRootProblemPreference;
import org.eclipse.cdt.codan.internal.core.externaltool.ExternalToolInvoker;
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
 * @author alruiz@google.com (Alex Ruiz)
 *
 * @since 2.1
 */
public abstract class AbstractExternalToolBasedChecker extends AbstractCheckerWithProblemPreferences
		implements IProblemDisplay {
	private static final boolean DO_NOT_TRAVERSE_CHILDREN = false;

	private final IInvocationParametersProvider parametersProvider;
	private final ISupportedResourceVerifier supportedResourceVerifier;
	private final IArgsSeparator argsSeparator;
	private final ConfigurationSettings settings;
	private final ExternalToolInvoker externalToolInvoker;
	private final RootProblemPreference preferences;

	/**
	 * Constructor.
	 * @param parametersProvider provides the parameters to pass when invoking the external tool.
	 * @param supportedResourceVerifier indicates whether a resource can be processed by the
	 *        external tool.
	 * @param argsSeparator separates the arguments to pass to the external tool executable. These
	 *        arguments are stored in a single {@code String}.
	 * @param consolePrinterProvider creates an Eclipse console that uses the name of an external
	 *        tool as its own.
	 * @param settings user-configurable external tool configuration settings.
	 */
	public AbstractExternalToolBasedChecker(IInvocationParametersProvider parametersProvider,
			ISupportedResourceVerifier supportedResourceVerifier, IArgsSeparator argsSeparator,
			IConsolePrinterProvider consolePrinterProvider, ConfigurationSettings settings) {
		this.parametersProvider = parametersProvider;
		this.supportedResourceVerifier = supportedResourceVerifier;
		this.argsSeparator = argsSeparator;
		this.settings = settings;
		externalToolInvoker = new ExternalToolInvoker(consolePrinterProvider);
		preferences = new SharedRootProblemPreference();
	}

	/**
	 * Indicates whether this checker can process the given resource. For more details, please
	 * see <code>{@link ISupportedResourceVerifier#isSupported(IResource)}</code>.
	 * @param resource the given resource.
	 * @return {@code true} if this checker can process the given resource, {@code false} otherwise.
	 */
	@Override
	public boolean enabledInContext(IResource resource) {
		return supportedResourceVerifier.isSupported(resource);
	}

	/**
	 * Indicates whether this checker is enabled to run while the user types. By default, this
	 * method returns {@code false}.
	 * <p>
	 * Running command-line based checkers while the user types is unnecessary and wasteful, since
	 * command-line tools are expensive to call (they run in a separate process) and they cannot
	 * see unsaved changes.
	 * </p>
	 * @return {@code false}.
	 */
	@Override
	public boolean runInEditor() {
		return false;
	}

	@Override
	public boolean processResource(IResource resource) {
		process(resource);
		return DO_NOT_TRAVERSE_CHILDREN;
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
		List<AbstractOutputParser> parsers = createParsers(parameters);
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

	/**
	 * Creates instances of <code>{@link AbstractOutputParser}</code>.
	 * @param parameters the parameters to pass when invoking an external tool.
	 */
	protected abstract List<AbstractOutputParser> createParsers(InvocationParameters parameters);

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
		CodanCorePlugin.log(msg, error);
	}

	/** {@inheritDoc} */
	@Override
	public void reportProblem(IProblemLocation location, String description) {
		String severity = null;
		reportProblem(location, description, severity);
	}

	/** {@inheritDoc} */
	@Override
	public void reportProblem(IProblemLocation location, String description, String severity) {
		super.reportProblem(getReferenceProblemId(), location, description);
	}

	/**
	 * Returns the id of the problem used as reference to obtain this checker's preferences. All
	 * preferences in a external-tool-based checker are shared among its defined problems.
	 * @return  the id of the problem used as reference to obtain this checker's preferences.
	 */
	protected abstract String getReferenceProblemId();

	/**
	 * Initializes the preferences of the given problem.  All preferences in a external-tool-based
	 * checker are shared among its defined problems.
	 * @param problem the given problem.
	 */
	@Override
	public void initPreferences(IProblemWorkingCopy problem) {
		super.initPreferences(problem);
		addPreference(problem, settings.getPath());
		addPreference(problem, settings.getArgs());
		addPreference(problem, settings.getShouldDisplayOutput());
	}

	private void addPreference(IProblemWorkingCopy problem, SingleConfigurationSetting<?> setting) {
		IProblemPreference descriptor = (IProblemPreference) setting.getDescriptor();
		addPreference(problem, descriptor, setting.getDefaultValue());
	}

	/** {@inheritDoc} */
	@Override
	protected void setDefaultPreferenceValue(IProblemWorkingCopy problem, String key,
			Object defaultValue) {
		MapProblemPreference map = getTopLevelPreference(problem);
		map.setChildValue(key, defaultValue);
	}

	/** {@inheritDoc} */
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
}
