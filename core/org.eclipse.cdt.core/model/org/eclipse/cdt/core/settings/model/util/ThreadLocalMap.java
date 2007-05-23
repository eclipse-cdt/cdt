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
package org.eclipse.cdt.core.settings.model.util;

import java.util.HashMap;
import java.util.Map;

public class ThreadLocalMap {
	private ThreadLocal fLocal = new ThreadLocal();
	
	public Object get(Object key){
		Map map = getMap(false);
		return map != null ? map.get(key) : null;
	}

	public void set(Object key, Object value){
		if(value == null)
			clear(key);
		else {
			Map map = getMap(true);
			map.put(key, value);
		}
	}

	public void clear(Object key){
		Map map = getMap(false);
		if(map != null){
			map.remove(key);
			if(map == null)
				fLocal.set(null);
		}
	}

	private Map getMap(boolean create){
		Map map = (Map)fLocal.get();
		if(map == null && create){
			map = new HashMap();
			fLocal.set(map);
		}
		return map;
	}
}
