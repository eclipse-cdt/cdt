/*******************************************************************************
 * Copyright (c) 2007, 2009 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.internal.autotools.core.configure.AutotoolsConfigurationManager;
import org.eclipse.cdt.internal.autotools.core.configure.IAConfiguration;
import org.eclipse.cdt.ui.newui.AbstractPage;

public class AutotoolsConfigurePropertyPage extends AbstractPage {

	private ICConfigurationDescription cfgd;
	
	protected boolean isSingle() {
		return true;
	}
	
	/**
	 * Default constructor
	 */
	public AutotoolsConfigurePropertyPage() {
		super();
	}

	public ICConfigurationDescription getCfgd() {
		return cfgd;
	}
	
	public void getAllConfigurationData() {
		ICConfigurationDescription[] cfgds = getCfgsEditable();
		for (int i = 0; i < cfgds.length; ++i) {
			@SuppressWarnings("unused")
			// Following will trigger an option value handler check which will
			// clone a configuration if necessary
			CConfigurationData data = cfgds[i].getConfigurationData();
		}
	}
	
	public IAConfiguration getConfiguration(ICConfigurationDescription cfgd) {
		IAConfiguration acfg = AutotoolsConfigurationManager.getInstance().getTmpConfiguration(getProject(), cfgd);
		return acfg;
	}
	
	protected void cfgChanged(ICConfigurationDescription _cfgd) {
		cfgd = _cfgd;
		// Let super update all pages
		super.cfgChanged(_cfgd);
	}
}

