package org.eclipse.cdt.ui.build.wizards;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class ConfigurationContentProvider implements IStructuredContentProvider {
	// The contents of the parent of the table is a list of configurations
	public Object[] getElements(Object parent) {
		// The content is a list of configurations
		IConfiguration[] configs = ((ITarget) parent).getConfigurations();
		return (configs.length == 0) ? new Object[0] : configs;
	}

	public void dispose() {
	}

	public void inputChanged(
		Viewer viewer,
		Object oldInput,
		Object newInput) {
	}
}
