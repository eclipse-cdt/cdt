/*******************************************************************************
 * Copyright (c) 2011, 2015 Red Hat Inc.
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

public class FlagValueConfigureOption extends BinConfigureOption implements IFlagConfigureValueOption {

	private String flags;

	public FlagValueConfigureOption(String name, String msgName, AutotoolsConfiguration cfg, String flags) {
		super(name, msgName, cfg);
		this.flags = flags;
	}

	private FlagValueConfigureOption(String name, AutotoolsConfiguration cfg, String value, String flags) {
		super(name, cfg);
		this.setValue(value);
		this.flags = flags;
	}

	@Override
	public ArrayList<String> getParameters() {
		return new ArrayList<>();
	}

	@Override
	public String getParameter() {
		return "";
	}

	@Override
	public IConfigureOption copy(AutotoolsConfiguration cfg) {
		return new FlagValueConfigureOption(name, cfg, getValue(), flags);
	}

	@Override
	public int getType() {
		return FLAGVALUE;
	}

	@Override
	public String getFlags() {
		return flags;
	}

	@Override
	public boolean isFlagValue() {
		return true;
	}

}
