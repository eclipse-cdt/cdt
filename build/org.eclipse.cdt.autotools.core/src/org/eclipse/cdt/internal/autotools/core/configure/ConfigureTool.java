/*******************************************************************************
 * Copyright (c) 2009 Red Hat Inc.
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

public class ConfigureTool extends AbstractConfigurationOption {

	private String value;
	
	public ConfigureTool(String name, AutotoolsConfiguration cfg) {
		super(name, cfg);
		this.value = name;
	}

	public ConfigureTool(String name, String transformedName, AutotoolsConfiguration cfg) {
		super(name, transformedName, cfg);
		this.value = name;
	}

	private ConfigureTool(String name, AutotoolsConfiguration cfg, String value) {
		super(name, cfg);
		this.value = value;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String newValue) {
		if (!newValue.equals(value)) {
			cfg.setDirty(true);
			value = newValue;
		}
	}

	public boolean isParmSet() {
		return false;
	}
	
	public String getParameter() {
		return ""; // $NON-NLS-1$
	}
	
	public ArrayList<String> getParameters() {
		return new ArrayList<String>();
	}
	
	public IConfigureOption copy(AutotoolsConfiguration config) {
		return new ConfigureTool(name, config, value);
	}

	public int getType() {
		return TOOL;
	}
}
