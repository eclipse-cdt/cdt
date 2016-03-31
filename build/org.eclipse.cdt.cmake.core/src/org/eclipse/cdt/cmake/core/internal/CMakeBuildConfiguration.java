/*******************************************************************************
 * Copyright (c) 2015, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.cmake.core.internal;

import org.eclipse.cdt.core.build.CBuildConfiguration;
import org.eclipse.cdt.core.build.IToolChain;
import org.eclipse.core.resources.IBuildConfiguration;

public class CMakeBuildConfiguration extends CBuildConfiguration {

	public CMakeBuildConfiguration(IBuildConfiguration config) {
		super(config);
	}

	public CMakeBuildConfiguration(IBuildConfiguration config, IToolChain toolChain) {
		super(config, toolChain);
	}

}
