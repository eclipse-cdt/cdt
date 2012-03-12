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

public class BinConfigureOption extends AbstractConfigurationOption {

	private boolean value;
	
	public BinConfigureOption(String name, AutotoolsConfiguration cfg) {
		super(name, cfg);
	}

	public BinConfigureOption(String name, String msgName, AutotoolsConfiguration cfg) {
		super(name, msgName, cfg);
	}

	private BinConfigureOption(String name, AutotoolsConfiguration cfg, boolean value) {
		super(name, cfg);
		this.value = value;
	}
	
	public boolean isParmSet() {
		return value;
	}
	
	public String getParameter() {
	   if (isParmSet())
		   return getParameterName();
	   else
		   return ""; // $NON-NLS-1$
	}

	public String getValue() {
		return Boolean.toString(value);
	}
	
	public void setValue(String value) {
		boolean oldValue = this.value;
		if (value.equals("true")) // $NON-NLS-1$
			this.value = true;
		else
			this.value = false;
		if (this.value != oldValue)
			cfg.setDirty(true);
	}
	
	public IConfigureOption copy(AutotoolsConfiguration config) {
		return new BinConfigureOption(name, config, value);
	}

	public int getType() {
		return BIN;
	}
}
