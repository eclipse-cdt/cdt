/*******************************************************************************
 * Copyright (c) 2011 Red Hat Inc.
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

public class FlagValueConfigureOption extends BinConfigureOption implements IFlagConfigureValueOption {

	private String flags;
	
	public FlagValueConfigureOption(String name, String msgName, AutotoolsConfiguration cfg,
			String flags) {
		super(name, msgName, cfg);
		this.flags = flags;
	}
	
	private FlagValueConfigureOption(String name, AutotoolsConfiguration cfg, String value,
			String flags) {
		super(name, cfg);
		this.setValue(value);
		this.flags = flags;
	}

	public ArrayList<String> getParameters() {
		return new ArrayList<String>();
	}
	
	public String getParameter() {
		return "";
	}

	public IConfigureOption copy(AutotoolsConfiguration cfg) {
		return new FlagValueConfigureOption(name, cfg, getValue(), flags);
	}

	public int getType() {
		return FLAGVALUE;
	}

	public String getFlags() {
		return flags;
	}

	public boolean isFlagValue() {
		return true;
	}
	
}
