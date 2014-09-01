/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Doug Schaefer
 *******************************************************************************/
package org.eclipse.cdt.launchbar.core.internal;

import org.eclipse.cdt.launchbar.core.ILaunchTarget;
import org.eclipse.cdt.launchbar.core.ILaunchTargetType;
import org.eclipse.core.runtime.PlatformObject;

/**
 * The launch target representing the machine we're running on.
 */
public class LocalTarget extends PlatformObject implements ILaunchTarget {

	private final LocalTargetType type;

	public LocalTarget(LocalTargetType type) {
		this.type = type;
	}

	@Override
	public String getName() {
		return Messages.LocalTarget_name;
	}

	@Override
	public ILaunchTargetType getType() {
		return type;
	}

	@Override
	public void setActive(boolean active) {
		// nothing to do, we have no active state
	}

}
