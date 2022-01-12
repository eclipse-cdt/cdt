/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API
 *******************************************************************************/
package org.eclipse.cdt.internal.errorparsers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;

public class FixitManager implements IResourceChangeListener {

	private static FixitManager instance;

	private Map<IMarker, Fixit> fixitMap = new HashMap<>();
	private Map<IResource, Set<IMarker>> fixitResourceMap = new HashMap<>();

	private FixitManager() {
		// add resource change listener so we can remove any stored
		// markers if the resource is removed.
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	public static FixitManager getInstance() {
		if (instance == null)
			instance = new FixitManager();
		return instance;
	}

	public void addMarker(IMarker marker, String range, String value) {
		Fixit f = new Fixit(range, value);
		fixitMap.put(marker, f);
		IResource r = marker.getResource();
		// register marker for resource
		Set<IMarker> markerSet = fixitResourceMap.get(r);
		// create marker set if one doesn't yet exist
		if (markerSet == null) {
			markerSet = new HashSet<>();
			fixitResourceMap.put(r, markerSet);
		}
		markerSet.add(marker);
	}

	public void deleteMarker(IMarker marker) {
		fixitMap.remove(marker);
		IResource r = marker.getResource();
		Set<IMarker> markerSet = fixitResourceMap.get(r);
		// remove marker from registered markers for resource
		if (markerSet != null) {
			markerSet.remove(marker);
			// remove whole marker set if empty
			if (markerSet.isEmpty()) {
				fixitResourceMap.remove(r);
			}
		}
	}

	public void deleteMarkers(IMarker[] markers) {
		for (IMarker marker : markers) {
			deleteMarker(marker);
		}
	}

	public boolean hasFixit(IMarker marker) {
		return fixitMap.containsKey(marker);
	}

	public Fixit findFixit(IMarker marker) {
		return fixitMap.get(marker);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		try {
			// look for resource removals that remove a resource we have
			// saved a marker for
			if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
				event.getDelta().accept(new IResourceDeltaVisitor() {
					@Override
					public boolean visit(IResourceDelta delta) throws CoreException {
						// we only care about removal of an IResource we have registered
						if (delta.getKind() == IResourceDelta.REMOVED) {
							Set<IMarker> markerSet = fixitResourceMap.get(delta.getResource());
							if (markerSet != null) {
								for (IMarker marker : markerSet) {
									deleteMarker(marker);
								}
								return false;
							}
							return true;
						}
						return false;
					}
				});
			}
		} catch (CoreException e) {
			CCorePlugin.log(e); // should not happen
		}
	}
}
