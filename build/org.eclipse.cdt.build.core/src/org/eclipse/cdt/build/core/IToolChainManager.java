/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.build.core;

import java.util.Collection;

import org.eclipse.launchbar.core.target.ILaunchTarget;

/**
 * The global toolchain manager. Accessed as an OSGi service.
 */
public interface IToolChainManager {

	IToolChainType getToolChainType(String id);

	IToolChain getToolChain(String typeId, String name);

	Collection<IToolChain> getToolChainsSupporting(ILaunchTarget target);

}
