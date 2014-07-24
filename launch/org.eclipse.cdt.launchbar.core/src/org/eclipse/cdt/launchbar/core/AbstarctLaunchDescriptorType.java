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
 * Abstract launch descriptor type provide convenience methods to implement hashcode, equals and store lanch bar manager object. It
 * is recommended to use this method instead of implementing interface
 */
public abstract class AbstarctLaunchDescriptorType implements ILaunchDescriptorType {
	private ILaunchBarManager manager;

	@Override
	public abstract String getId();


	@Override
	public void init(ILaunchBarManager barmanager) {
		this.manager = barmanager;
	}

	@Override
	public ILaunchBarManager getManager() {
		return manager;
	}

	@Override
	public int hashCode() {
		return 37 + getId().hashCode();
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
