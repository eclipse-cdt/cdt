/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.settings.model.util;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalMap {
	private ThreadLocal<Map<Object, Object>> fLocal = new ThreadLocal<>();

	public Object get(Object key) {
		Map<Object, Object> map = getMap(false);
		return map != null ? map.get(key) : null;
	}

	public void set(Object key, Object value) {
		if (value == null)
			clear(key);
		else {
			Map<Object, Object> map = getMap(true);
			map.put(key, value);
		}
	}

	public void clear(Object key) {
		Map<Object, Object> map = getMap(false);
		if (map != null) {
			map.remove(key);
		}
		//		if(map == null)
		//			fLocal.set(null);
	}

	private Map<Object, Object> getMap(boolean create) {
		Map<Object, Object> map = fLocal.get();
		if (map == null && create) {
			map = new HashMap<>();
			fLocal.set(map);
		}
		return map;
	}
}
