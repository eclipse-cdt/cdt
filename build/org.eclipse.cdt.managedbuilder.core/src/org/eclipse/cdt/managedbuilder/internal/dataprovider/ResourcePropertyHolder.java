/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.dataprovider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.util.ResourceChangeHandlerBase;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;

class ResourcePropertyHolder extends ResourceChangeHandlerBase {
	private Map<String, Map<String, Boolean>> fRcMap = new HashMap<String, Map<String, Boolean>>();
	private boolean fProjectOnly;

	public ResourcePropertyHolder(boolean projectOnly){
		fProjectOnly = projectOnly;
	}

	private class ResourceMoveHandler implements IResourceMoveHandler {

		@Override
		public void done() {
		}

		@Override
		public void handleProjectClose(IProject project) {
			removeResourcePropertyMap(project);
		}

		@Override
		public boolean handleResourceMove(IResource fromRc, IResource toRc) {
			if(isValidResource(fromRc)){
				moveResourcePropertyMap(fromRc, toRc);
				return !fProjectOnly;
			}
			return false;
		}

		@Override
		public boolean handleResourceRemove(IResource rc) {
			if(isValidResource(rc)){
				removeResourcePropertyMap(rc);
				return !fProjectOnly;
			}
			return false;
		}

	}

	private boolean isValidResource(IResource rc){
		return !fProjectOnly || rc.getType() == IResource.PROJECT;
	}

	@Override
	protected IResourceMoveHandler createResourceMoveHandler(
			IResourceChangeEvent event) {
		return new ResourceMoveHandler();
	}

	protected String keyForResource(IResource rc){
		return rc.getFullPath().toString();
	}

	public synchronized Boolean getProperty(IResource rc, String propKey) throws IllegalArgumentException {
		if(!isValidResource(rc))
			throw new IllegalArgumentException();

		Map<String, Boolean> map = getResourcePropertyMap(rc, false);
		if(map == null)
			return null;

		return map.get(propKey);
	}

	private Map<String, Boolean> getResourcePropertyMap(IResource rc, boolean create){
		String key = keyForResource(rc);
		Map<String, Boolean> map = fRcMap.get(key);
		if(map == null && create){
			map = new HashMap<String, Boolean>();
			fRcMap.put(key, map);
		}

		return map;
	}

	private synchronized void removeResourcePropertyMap(IResource rc){
		String key = keyForResource(rc);
		fRcMap.remove(key);
	}

	private synchronized void moveResourcePropertyMap(IResource fromRc, IResource toRc){
		String fromKey = keyForResource(fromRc);
		String toKey = keyForResource(toRc);

		Map<String, Boolean> fromMap = fRcMap.remove(fromKey);
		if(fromMap != null){
			fRcMap.put(toKey, fromMap);
		} else {
			fRcMap.remove(toKey);
		}
	}

	public synchronized Boolean setProperty(IResource rc, String propKey, Boolean value) throws IllegalArgumentException {
		if(!isValidResource(rc))
			throw new IllegalArgumentException();

		if(value == null)
			return removeProperty(rc, propKey);

		Map<String, Boolean> map = getResourcePropertyMap(rc, true);
		return map.put(propKey, value);
	}

	private synchronized Boolean removeProperty(IResource rc, String propKey){
		Map<String, Boolean> map = getResourcePropertyMap(rc, false);

		if(map == null)
			return null;

		Boolean old = map.remove(propKey);

		if(map.size() == 0)
			removeResourcePropertyMap(rc);

		return old;
	}
}
