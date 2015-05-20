/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wainer dos Santos Moschetta - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

/**
 * This class represents a variable option as NAME=VALUE
 *
 * @since 1.5
 *
 */
public class VariableConfigureOption extends AbstractConfigurationOption {

	private String value;

	public VariableConfigureOption(String name, AutotoolsConfiguration cfg) {
		super(name, cfg);
		this.value = "";
	}

	public VariableConfigureOption(String name, String transformedName, AutotoolsConfiguration autotoolsConfiguration) {
		super(name, transformedName, autotoolsConfiguration);
		this.value = "";
	}

	public VariableConfigureOption(String name, AutotoolsConfiguration cfg, String value) {
		super(name, cfg);
		this.value = value;
	}

	@Override
	public String getParameter() {
		if (isParmSet())
			return this.name + "=" + this.value; // $NON-NLS-1$
		return "";
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
		return VARIABLE;
	}

}
