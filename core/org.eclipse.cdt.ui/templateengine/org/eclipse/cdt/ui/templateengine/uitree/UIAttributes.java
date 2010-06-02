/*******************************************************************************
 * Copyright (c) 2007, 2008 Symbian Software Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Bala Torati (Symbian) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.templateengine.uitree;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.templateengine.TemplateEngineHelper;
import org.eclipse.cdt.core.templateengine.TemplateInfo;

/**
 * Every UIElement will be associated with attributes. This class extends
 * HashMap. It just provides a convenient way to store Key , value pairs. This
 * class is for clarity in usage. We need not use HashMap for attributes,
 * instead we can use UIAttributes for attributes.
 */
public class UIAttributes extends HashMap<String, String> {
	private static final long serialVersionUID = 0000000000L;
	private TemplateInfo templateInfo;
	
	UIAttributes(TemplateInfo templateInfo) {
		this.templateInfo = templateInfo;
	}
	
	@Override
	public String put(String key, String value) {
		value = TemplateEngineHelper.externalizeTemplateString(templateInfo, value);
		return super.put(key, value);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> map) {
		for(String key : map.keySet()) {
			String value = map.get(key);
			value = TemplateEngineHelper.externalizeTemplateString(templateInfo, value);
			super.put(key, value);
		}
	}
}
