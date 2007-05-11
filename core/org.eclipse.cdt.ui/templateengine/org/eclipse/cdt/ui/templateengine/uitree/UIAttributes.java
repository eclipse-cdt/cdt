/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.uitree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.core.templateengine.TemplateEngineHelper;
import org.eclipse.cdt.core.templateengine.TemplateInfo;

/**
 * 
 * Every UIElement will be associated with attributes. This class extends
 * HashMap. It just provides a convenient way to store Key , value pairs. This
 * class is for clarity in usage. We need not use HashMap for attributes,
 * instead we can use UIAttributes for attributes.
 * 
 */

public class UIAttributes/*<K, V>*/ extends HashMap/*<String, String>*/ {

	private static final long serialVersionUID = 0000000000L;
	private TemplateInfo templateInfo;
	
	UIAttributes(TemplateInfo templateInfo) {
		this.templateInfo = templateInfo;
	}
	
	public Object/*V*/ put(Object/*K*/ key, Object/*V*/ value) {
		value = TemplateEngineHelper.externalizeTemplateString(templateInfo, (String)value);
		Object/*V*/ v = super.put(key, value);
		return v;
	}

	public void putAll(Map/*<? extends K, ? extends V>*/ map) {
		Collection keys = map.keySet();
		for (Iterator iterator = keys.iterator(); iterator.hasNext();) {
			Object key = iterator.next();
			Object value = map.get(key);
			value = TemplateEngineHelper.externalizeTemplateString(templateInfo, (String) value);
			super.put(key, value);
		}
	}

	public Object/*V*/ remove(Object key) {
		Object/*V*/ v = super.remove(key);
		return v;
	}
	
}
