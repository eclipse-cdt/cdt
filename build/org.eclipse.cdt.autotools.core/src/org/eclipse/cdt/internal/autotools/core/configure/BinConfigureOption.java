/*******************************************************************************
 * Copyright (c) 2009, 2016 Red Hat Inc. and others.
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

	@Override
	public boolean isParmSet() {
		return value;
	}

	@Override
	public String getParameter() {
		if (isParmSet())
			return getParameterName();
		else
			return ""; //$NON-NLS-1$
	}

	@Override
	public String getValue() {
		return Boolean.toString(value);
	}

	@Override
	public void setValue(String value) {
		boolean oldValue = this.value;
		if (value.equals("true")) //$NON-NLS-1$
			this.value = true;
		else
			this.value = false;
		if (this.value != oldValue)
			cfg.setDirty(true);
	}

	@Override
	public IConfigureOption copy(AutotoolsConfiguration config) {
		return new BinConfigureOption(name, config, value);
	}

	@Override
	public int getType() {
		return BIN;
	}
}
