/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.launchbar.core.internal.target;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.launchbar.core.internal.Activator;
import org.eclipse.launchbar.core.target.ILaunchTarget;
import org.eclipse.launchbar.core.target.ILaunchTargetWorkingCopy;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class LaunchTarget extends PlatformObject implements ILaunchTarget {
	private final String typeId;
	private final String id;
	final Preferences attributes;

	/**
	 * This should only be used to create the null target. There are no attributes supported on the
	 * null target.
	 */
	public LaunchTarget(String typeId, String id) {
		this.typeId = typeId;
		this.id = id;
		this.attributes = null;
	}

	public LaunchTarget(String typeId, String id, Preferences attributes) {
		if (typeId == null || id == null || attributes == null)
			throw new NullPointerException();
		this.typeId = typeId;
		this.id = id;
		this.attributes = attributes;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getTypeId() {
		return typeId;
	}

	@Override
	public ILaunchTargetWorkingCopy getWorkingCopy() {
		return new LaunchTargetWorkingCopy(this);
	}

	@Override
	public String getAttribute(String key, String defValue) {
		if (attributes != null) {
			return attributes.get(key, defValue);
		} else {
			return defValue;
		}
	}

	@Override
	public Map<String, String> getAttributes() {
		Map<String, String> attrs = new HashMap<>();
		if (attributes != null) {
			try {
				for (String key : attributes.keys()) {
					String value = attributes.get(key, null);
					if (value != null) {
						attrs.put(key, value);
					}
				}
			} catch (BackingStoreException e) {
				Activator.log(e);
			}
		}
		return attrs;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id.hashCode();
		result = prime * result + typeId.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LaunchTarget other = (LaunchTarget) obj;
		if (!id.equals(other.id))
			return false;
		if (!typeId.equals(other.typeId))
			return false;
		return true;
	}
}
