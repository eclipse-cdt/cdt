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
import java.util.List;

public abstract class AbstractConfigurationOption implements IConfigureOption {

	protected String name;
	private String msgName;
	protected AutotoolsConfiguration cfg;

	public AbstractConfigurationOption(String name, AutotoolsConfiguration cfg) {
		this(name, name, cfg);
	}

	public AbstractConfigurationOption(String name, String msgName, AutotoolsConfiguration cfg) {
		this.name = name;
		this.msgName = msgName;
		this.cfg = cfg;
	}

	@Override
	public String getDescription() {
		return ConfigureMessages.getConfigureDescription(msgName);
	}

	@Override
	public String getToolTip() {
		return ConfigureMessages.getConfigureTip(msgName);
	}

	public String getMsgName() {
		return msgName;
	}

	@Override
	public String getName() {
		return name;
	}

	public AutotoolsConfiguration getCfg() {
		return cfg;
	}

	@Override
	public List<String> getParameters() {
		List<String> parameters = new ArrayList<>();
		if (isParmSet())
			parameters.add(getParameter());
		return parameters;
	}

	public String getParameterName() {
		return "--" + getName();
	}

	@Override
	public boolean isCategory() {
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
	public boolean isMultiArg() {
		return false;
	}
}
