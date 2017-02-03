/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Inc. - initial API
 *******************************************************************************/
package org.eclipse.cdt.internal.errorparsers;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;

public class FixitManager {
	
	private static FixitManager instance;
	
	Map<IMarker, Fixit> fixitMap = new HashMap<>();
	
	private FixitManager() {
		
	}
	
	public static FixitManager getInstance() {
		if (instance == null)
			instance = new FixitManager();
		return instance;
	}
	
	public void addMarker(IMarker marker, String range, String value) {
		Fixit f = new Fixit(range, value);
		fixitMap.put(marker, f);
	}
	
	public void removeMarker(IMarker marker) {
		fixitMap.remove(marker);
	}
	
	public boolean hasFixit(IMarker marker) {
		return fixitMap.containsKey(marker);
	}
	
	public Fixit findFixit(IMarker marker) {
		return fixitMap.get(marker);
	}
}
