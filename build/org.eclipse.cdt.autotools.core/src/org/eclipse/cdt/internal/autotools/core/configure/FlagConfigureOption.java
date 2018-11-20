/*******************************************************************************
 * Copyright (c) 2011, 2016 Red Hat Inc.
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
 *     Red Hat Inc. - add support for specifying multiple flag names at once
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

import java.util.ArrayList;
import java.util.List;

public class FlagConfigureOption extends AbstractConfigurationOption {

	private String value;
	private ArrayList<String> children = new ArrayList<>();

	public FlagConfigureOption(String name, AutotoolsConfiguration cfg) {
		super(name, cfg);
		this.value = name;
	}

	public FlagConfigureOption(String name, String msgName, AutotoolsConfiguration cfg) {
		super(name, msgName, cfg);
		this.value = name;
	}

	private FlagConfigureOption(String name, AutotoolsConfiguration cfg, String value, ArrayList<String> children) {
		super(name, cfg);
		this.value = value;
		this.children = new ArrayList<>(children);
	}

	@Override
	public String getParameter() {
		StringBuilder parms = new StringBuilder();
		// Multiple flags are designated by putting multiple flags together using "|" as delimiter
		String[] flagNames = getValue().split("\\|"); //$NON-NLS-1$
		String flagSeparator = "";
		for (String flagName : flagNames) {
			parms.append(flagSeparator);
			flagSeparator = " "; //$NON-NLS-1$
			StringBuilder parm = new StringBuilder(flagName).append("=\""); //$NON-NLS-1$
			boolean haveParm = false;
			if (isParmSet()) {
				String separator = ""; //$NON-NLS-1$
				for (int i = 0; i < children.size(); ++i) {
					String fvname = children.get(i);
					IConfigureOption o = cfg.getOption(fvname);
					if (o.isParmSet()) {
						if (o instanceof IFlagConfigureValueOption) {
							parm.append(separator).append(((IFlagConfigureValueOption) o).getFlags());
							separator = " "; //$NON-NLS-1$
							haveParm = true;
						}
					}
				}
				if (haveParm) {
					parm.append('"');
					parms.append(parm);
				}
			}
		}
		return parms.toString();
	}

	@Override
	public String getParameterName() {
		return getName();
	}

	@Override
	public boolean isParmSet() {
		for (int i = 0; i < children.size(); ++i) {
			String s = children.get(i);
			IConfigureOption o = cfg.getOption(s);
			if (o.isParmSet())
				return true;
		}
		return false;
	}

	@Override
	public void setValue(String value) {
		this.value = value;
	}

	@Override
	public IConfigureOption copy(AutotoolsConfiguration config) {
		return new FlagConfigureOption(name, config, value, children);
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public int getType() {
		return FLAG;
	}

	@Override
	public boolean isFlag() {
		return true;
	}

	public void addChild(String name) {
		children.add(name);
	}

	public List<String> getChildren() {
		return children;
	}

}
