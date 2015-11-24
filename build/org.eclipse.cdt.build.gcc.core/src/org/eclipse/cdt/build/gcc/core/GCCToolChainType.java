/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.gcc.core;

import org.eclipse.cdt.build.core.IToolChain;
import org.eclipse.cdt.build.core.IToolChainType;
import org.osgi.service.prefs.Preferences;

public class GCCToolChainType implements IToolChainType {

	public static final String ID = "org.eclipse.cdt.build.gcc"; //$NON-NLS-1$

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public IToolChain getToolChain(String name, Preferences properties) {
		// TODO Auto-generated method stub
		return null;
	}

}
