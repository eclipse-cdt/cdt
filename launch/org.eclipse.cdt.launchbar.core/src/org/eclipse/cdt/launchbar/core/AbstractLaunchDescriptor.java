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
 * Convenience implementation of ILaunchDescriptor
 * 
 * @author elaskavaia
 *
 */
public abstract class AbstractLaunchDescriptor implements ILaunchDescriptor {

	@Override
	public abstract String getName();

	@Override
	public abstract ILaunchDescriptorType getType();

	public String getId() {
		return getName() + "." + getType().getId();
	}

	@Override
	public int hashCode() {
		return 17 + getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AbstractLaunchDescriptor))
			return false;
		AbstractLaunchDescriptor other = (AbstractLaunchDescriptor) obj;
		if (!getId().equals(other.getId()))
			return false;
		return true;
	}
}
