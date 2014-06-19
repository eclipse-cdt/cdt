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

import org.eclipse.cdt.launchbar.core.ILaunchTargetType;

public class LocalTargetType implements ILaunchTargetType {

	private final LocalTarget target;

	public LocalTargetType() {
		target = new LocalTarget(this);
	}

	@Override
	public String getId() {
		return "org.eclipse.cdt.launchbar.target.local";
	}

	public LocalTarget getTarget() {
		return target;
	}

}
