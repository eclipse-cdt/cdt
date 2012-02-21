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

import java.io.File;

import org.eclipse.cdt.codan.core.param.MapProblemPreference;
import org.eclipse.cdt.codan.internal.core.externaltool.ArgsSetting;
import org.eclipse.cdt.codan.internal.core.externaltool.PathSetting;
import org.eclipse.cdt.codan.internal.core.externaltool.ShouldDisplayOutputSetting;

/**
 * User-configurable external tool settings.
 *
 * @author alruiz@google.com (Alex Ruiz)
 *
 * @since 2.1
 */
public final class ConfigurationSettings {
	private final PathSetting path;
	private final ArgsSetting args;
	private final ShouldDisplayOutputSetting shouldDisplayOutput;
	private final String externalToolName;

	/**
	 * Constructor.
	 * @param externalToolName the name of the external tool, to be displayed to the user.
	 * @param defaultPath the default path of the external tool.
	 * @param defaultArgs the default arguments to pass when invoking the external tool.
	 * @param defaultShouldDisplayOutput indicates whether output of an external tool should be
	 *        displayed in an Eclipse console.
	 */
	public ConfigurationSettings(String externalToolName, File defaultPath, String defaultArgs,
			boolean defaultShouldDisplayOutput) {
		this.externalToolName = externalToolName;
		this.path = new PathSetting(externalToolName, defaultPath);
		this.args = new ArgsSetting(externalToolName, defaultArgs);
		this.shouldDisplayOutput = new ShouldDisplayOutputSetting(defaultShouldDisplayOutput);
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
	public SingleConfigurationSetting<File> getPath() {
		return path;
	}

	/**
	 * Returns the setting that specifies the arguments to pass when invoking the external tool.
	 * @return the setting that specifies the arguments to pass when invoking the external tool.
	 */
	public SingleConfigurationSetting<String> getArgs() {
		return args;
	}

	/**
	 * Returns the setting that specifies whether the output of the external tools should be
	 * displayed in an Eclipse console.
	 * @return the shouldDisplayOutput the setting that specifies whether the output of the external
	 *         tools should be displayed in an Eclipse console.
	 */
	public SingleConfigurationSetting<Boolean> getShouldDisplayOutput() {
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
