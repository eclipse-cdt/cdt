/*******************************************************************************
 * Copyright (c) 2009, 2016 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core.configure;

public class InternalConfigureOption extends AbstractConfigurationOption {

	private String value;
	
	public InternalConfigureOption(String name, AutotoolsConfiguration cfg) {
		super(name, cfg);
		this.value = ""; //$NON-NLS-1$
	}
	
	public InternalConfigureOption(String name, String msgName, AutotoolsConfiguration cfg) {
		super(name, msgName, cfg);
		this.value = ""; //$NON-NLS-1$
	}
	
	private InternalConfigureOption(String name, AutotoolsConfiguration cfg,
			String value) {
		super(name, cfg);
		this.value = value;
	}
	
	@Override
	public String getValue() {
		return value;
	}
	
	@Override
	public void setValue(String newValue) {
		if (!newValue.equals(value)) {
			cfg.setDirty(true);
			value = newValue;
		}
	}

	@Override
	public boolean isParmSet() {
		return false;
	}
	
	@Override
	public String getParameter() {
		return "";
	}
	
	@Override
	public IConfigureOption copy(AutotoolsConfiguration config) {
		return new InternalConfigureOption(name, config, value);
	}

	@Override
	public int getType() {
		return INTERNAL;
	}
}
