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
 * Convenience implementation of ILaunchTarget, provides hooks for id, and equals and hashcode methods based on id.
 */
public abstract class AbstractLaunchTarget implements ILaunchTarget {
	private final String id;

	public AbstractLaunchTarget(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getName() {
		return id;
	}

	@Override
	public abstract ILaunchTargetType getType();

	@Override
	public void setActive() {
		// nothing to do
	}

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
}
