/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wainer dos Santos Moschetta - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

/**
 * This class represents a a list of environment variables as NAME="VALUE"
 *
 * @since 2.0
 *
 */
public class VariableConfigureOption extends AbstractConfigurationOption {

	private String value;

	public VariableConfigureOption(String name, AutotoolsConfiguration cfg) {
		super(name, cfg);
		this.value = ""; //$NON-NLS-1$
	}

	public VariableConfigureOption(String name, String transformedName, AutotoolsConfiguration autotoolsConfiguration) {
		super(name, transformedName, autotoolsConfiguration);
		this.value = ""; //$NON-NLS-1$
	}

	public VariableConfigureOption(String name, AutotoolsConfiguration cfg, String value) {
		super(name, cfg);
		this.value = value;
	}

	@Override
	public String getParameter() {
		if (isParmSet())
			return this.value;
		return ""; //$NON-NLS-1$
	}

	@Override
	public boolean isParmSet() {
		return !this.value.isEmpty();
	}

	@Override
	public IConfigureOption copy(AutotoolsConfiguration cfg) {
		return new VariableConfigureOption(name, cfg, value);
	}

	@Override
	public void setValue(String newValue) {
		if (!newValue.equals(value)) {
			cfg.setDirty(true);
			value = newValue;
		}
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public int getType() {
		return ENVVAR;
	}

}
