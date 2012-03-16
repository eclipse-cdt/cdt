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

public class ConfigureOptionCategory implements IConfigureOption {

	private String name;
	
	public ConfigureOptionCategory(String name) {
		this.name = name;
	}
	
	public IConfigureOption copy(AutotoolsConfiguration config) {
		return new ConfigureOptionCategory(name);
	}

	public String getDescription() {
		return ConfigureMessages.getConfigureDescription(name);
	}

	public String getName() {
		return name;
	}

	public String getParameter() {
		return "";
	}

	public ArrayList<String> getParameters() {
		return new ArrayList<String>();
	}
	
	public String getToolTip() {
		return "";
	}

	public String getValue() {
		return "null";
	}

	public boolean isCategory() {
		return true;
	}

	public boolean isParmSet() {
		return false;
	}

	public void setValue(String value) {
		// Do nothing..nothing to set
	}

	public boolean isMultiArg() {
		return false;
	}
	
	public boolean isFlag() {
		return false;
	}
	
	public boolean isFlagValue() {
		return false;
	}

	public int getType() {
		return CATEGORY;
	}
}
