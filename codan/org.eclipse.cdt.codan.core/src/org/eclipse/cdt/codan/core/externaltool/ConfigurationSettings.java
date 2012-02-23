/*******************************************************************************
 * Copyright (c) 2012 Google, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alex Ruiz  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.externaltool;

import static org.eclipse.cdt.codan.core.externaltool.Messages.ConfigurationSettings_args_format;
import static org.eclipse.cdt.codan.core.externaltool.Messages.ConfigurationSettings_path_format;
import static org.eclipse.cdt.codan.core.externaltool.Messages.ConfigurationSettings_should_display_output;

import java.io.File;

import org.eclipse.cdt.codan.core.param.MapProblemPreference;

/**
 * User-configurable external tool settings.
 *
 * @author alruiz@google.com (Alex Ruiz)
 */
public class ConfigurationSettings {
	private final PathSetting path;
	private final ArgsSetting args;
	private final ShouldDisplayOutputSetting shouldDisplayOutput;
	private final String externalToolName;

	/**
	 * Constructor.
	 * <p>
	 * <strong>Note:</strong> this constructor uses {@code false} as the default value of the
	 * <code>{@link ShouldDisplayOutputSetting}</code> to create.
	 * </p>
	 * @param externalToolName the name of the external tool, to be displayed to the user.
	 * @param defaultPath the default path of the external tool.
	 * @param defaultArgs the default arguments to pass when invoking the external tool.
	 */
	public ConfigurationSettings(String externalToolName, File defaultPath, String defaultArgs) {
		this.externalToolName = externalToolName;
		String pathLabel = String.format(ConfigurationSettings_path_format, externalToolName);
		this.path = new PathSetting(pathLabel, defaultPath);
		String argsLabel = String.format(ConfigurationSettings_args_format, externalToolName);
		this.args = new ArgsSetting(argsLabel, defaultArgs);
		String shouldDisplayOutputLabel = ConfigurationSettings_should_display_output;
		this.shouldDisplayOutput = new ShouldDisplayOutputSetting(shouldDisplayOutputLabel, false);
	}

	/**
	 * Constructor.
	 * @param externalToolName the name of the external tool, to be displayed to the user.
	 * @param path specifies the path and name of the external tool to invoke.
	 * @param args specifies the arguments to pass when invoking the external tool.
	 * @param shouldDisplayOutput specifies whether the output of the external tools should be
	 *        displayed in an Eclipse console.
	 */
	public ConfigurationSettings(String externalToolName, PathSetting path, ArgsSetting args,
			ShouldDisplayOutputSetting shouldDisplayOutput) {
		this.externalToolName = externalToolName;
		this.path = path;
		this.args = args;
		this.shouldDisplayOutput = shouldDisplayOutput;
	}

	/**
	 * Returns the name of the external tool, to be displayed to the user.
	 * @return the name of the external tool, to be displayed to the user.
	 */
	public String getExternalToolName() {
		return externalToolName;
	}

	/**
	 * Returns the setting that specifies the path and name of the external tool to invoke.
	 * @return the setting that specifies the path and name of the external tool to invoke.
	 */
	public PathSetting getPath() {
		return path;
	}

	/**
	 * Returns the setting that specifies the arguments to pass when invoking the external tool.
	 * @return the setting that specifies the arguments to pass when invoking the external tool.
	 */
	public ArgsSetting getArgs() {
		return args;
	}

	/**
	 * Returns the setting that specifies whether the output of the external tools should be
	 * displayed in an Eclipse console.
	 * @return the shouldDisplayOutput the setting that specifies whether the output of the external
	 *         tools should be displayed in an Eclipse console.
	 */
	public ShouldDisplayOutputSetting getShouldDisplayOutput() {
		return shouldDisplayOutput;
	}

	/**
	 * Updates the values of the configuration settings value with the ones stored in the given
	 * preference map.
	 * @param preferences the given preference map that may contain the values to set.
	 * @throws ClassCastException if any of the values to set is not of the same type as the one
	 *         supported by a setting.
	 */
	public void updateValuesFrom(MapProblemPreference preferences) {
		path.updateValue(preferences);
		args.updateValue(preferences);
		shouldDisplayOutput.updateValue(preferences);
	}
}
