/*******************************************************************************
 * Copyright (c) 2011, 2014 Red Hat Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *     Red Hat Inc. - add support for specifying multiple flag names at once
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

import java.util.ArrayList;

public class FlagConfigureOption extends AbstractConfigurationOption {

	private String value;
	private ArrayList<String> children = 
			new ArrayList<String>();
	
	public FlagConfigureOption(String name, AutotoolsConfiguration cfg) {
		super(name, cfg);
		this.value = name;
	}
	
	public FlagConfigureOption(String name, String msgName, AutotoolsConfiguration cfg) {
		super(name, msgName, cfg);
		this.value = name;
	}
	
	@SuppressWarnings("unchecked")
	private FlagConfigureOption(String name, AutotoolsConfiguration cfg,
			String value, ArrayList<String> children) {
		super(name, cfg);
		this.value = value;
		this.children = (ArrayList<String>)children.clone();
	}
	
	public String getParameter() {
		StringBuffer parms = new StringBuffer();
		// Multiple flags are designated by putting multiple flags together using "|" as delimiter
		String[] flagNames = getValue().split("\\|"); //$NON-NLS-1$
		String flagSeparator = "";
		for (String flagName : flagNames) {
			parms.append(flagSeparator);
			flagSeparator = " "; //$NON-NLS-1$
			StringBuffer parm = new StringBuffer(flagName+"=\""); //$NON-NLS-1$
			boolean haveParm = false;
			if (isParmSet()) {
				String separator = "";
				for (int i = 0; i < children.size(); ++i) {
					String fvname = children.get(i);
					IConfigureOption o = cfg.getOption(fvname);
					if (o.isParmSet()) {
						if (o instanceof IFlagConfigureValueOption) {
							parm.append(separator + ((IFlagConfigureValueOption)o).getFlags()); //$NON-NLS-1$
							separator = " ";
							haveParm = true;
						}
					}
				}
				if (haveParm) {
					parm.append("\""); //$NON-NLS-1$
					parms.append(parm);
				}
			}
		}
		return parms.toString();
	}

	public String getParameterName() {
		return getName();
	}
	
	public boolean isParmSet() {
		for (int i = 0; i < children.size(); ++i) {
			String s = children.get(i);
			IConfigureOption o = cfg.getOption(s);
			if (o.isParmSet())
				return true;
		}
		return false;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public IConfigureOption copy(AutotoolsConfiguration config) {
		FlagConfigureOption f = new FlagConfigureOption(name, config, value, children);
		return f; 
	}

	public String getValue() {
		return value;
	}

	public int getType() {
		return FLAG;
	}
	
	public boolean isFlag() {
		return true;
	}
	
	public void addChild(String name) {
		children.add(name);
	}
	
	public ArrayList<String> getChildren() {
		return children;
	}
	
}
