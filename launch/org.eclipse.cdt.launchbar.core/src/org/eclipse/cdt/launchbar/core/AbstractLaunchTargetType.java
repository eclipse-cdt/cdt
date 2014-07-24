/*******************************************************************************
 * Copyright (c) 2014 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Elena Laskavaia - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launchbar.core;

/**
 * Convenience implementation of ILaunchTargetType, provides equals and hashcode methods based on id.
 */
public abstract class AbstractLaunchTargetType implements ILaunchTargetType {
	private ILaunchBarManager manager;

	@Override
	public void init(ILaunchBarManager barmanager) {
		this.manager = barmanager;
	}

	public ILaunchBarManager getManager() {
		return manager;
	}

	@Override
	public abstract String getId();

	@Override
	public int hashCode() {
		return 7 + getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj.getClass().equals(getClass())))
			return false;
		ILaunchDescriptorType other = (ILaunchDescriptorType) obj;
		if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	@Override
	public ILaunchTarget getTarget(String id) {
		if (id == null)
			return null;
		ILaunchTarget[] targets = getTargets();
		for (int i = 0; i < targets.length; i++) {
			ILaunchTarget target = targets[i];
			if (target.getId().equals(id))
				return target;
		}
		return null;
	}
}
