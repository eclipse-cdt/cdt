/*******************************************************************************
 * Copyright (c) 2009, 2015 Red Hat Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

import java.util.ArrayList;

public class ConfigureOptionCategory implements IConfigureOption {

	private String name;

	public ConfigureOptionCategory(String name) {
		this.name = name;
	}

	@Override
	public IConfigureOption copy(AutotoolsConfiguration config) {
		return new ConfigureOptionCategory(name);
	}

	@Override
	public String getDescription() {
		return ConfigureMessages.getConfigureDescription(name);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getParameter() {
		return "";
	}

	@Override
	public ArrayList<String> getParameters() {
		return new ArrayList<>();
	}

	@Override
	public String getToolTip() {
		return "";
	}

	@Override
	public String getValue() {
		return "null";
	}

	@Override
	public boolean isCategory() {
		return true;
	}

	@Override
	public boolean isParmSet() {
		return false;
	}

	@Override
	public void setValue(String value) {
		// Do nothing..nothing to set
	}

	@Override
	public boolean isMultiArg() {
		return false;
	}

	@Override
	public boolean isFlag() {
		return false;
	}

	@Override
	public boolean isFlagValue() {
		return false;
	}

	@Override
	public int getType() {
		return CATEGORY;
	}
}
