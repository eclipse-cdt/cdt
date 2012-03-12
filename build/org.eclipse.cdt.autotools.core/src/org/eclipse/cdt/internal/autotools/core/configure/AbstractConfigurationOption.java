/*******************************************************************************
 * Copyright (c) 2009, 2011 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

import java.util.ArrayList;

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
	
	public String getDescription() {
		return ConfigureMessages.getConfigureDescription(msgName);
	}

	public String getToolTip() {
		return ConfigureMessages.getConfigureTip(msgName);
	}

	public String getMsgName() {
		return msgName;
	}
	
	public String getName() {
		return name;
	}

	public AutotoolsConfiguration getCfg() {
		return cfg;
	}
	
	public ArrayList<String> getParameters() {
		ArrayList<String> parameters = new ArrayList<String>();
		if (isParmSet())
			parameters.add(getParameter());
		return parameters;
	}
	
	public String getParameterName() {
		return "--" + getName();
	}

	public boolean isCategory() {
		return false;
	}
	
	public boolean isFlag() {
		return false;
	}
	
	public boolean isFlagValue() {
		return false;
	}

	public boolean isMultiArg() {
		return false;
	}
}
