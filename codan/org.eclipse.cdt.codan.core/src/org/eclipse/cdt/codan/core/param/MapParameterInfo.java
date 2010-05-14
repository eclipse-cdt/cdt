/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

import java.util.HashMap;
import java.util.Iterator;

/**
 * HashParamterInfo - for checker that needs more than one parameter and they
 * all different "named".
 * For example checker has 2 optional boolean parameters. For example checker
 * for parameter names
 * shadowing would have two boolean options: "check contructors" and
 * "check setters". In this case you use this type.
 * 
 */
public class MapParameterInfo extends SingleParameterInfo {
	protected HashMap<String, IProblemParameterInfo> hash = new HashMap<String, IProblemParameterInfo>();

	public MapParameterInfo() {
		super(""); //$NON-NLS-1$
	}

	/**
	 * @param label
	 *            - label for this group of parameters
	 */
	public MapParameterInfo(String label) {
		super(label);
	}

	/**
	 * @param key
	 *            - key for itself
	 * @param label
	 *            - label for this group of parameters
	 */
	public MapParameterInfo(String key, String label) {
		super(key, label);
	}

	@Override
	public ParameterType getType() {
		return ParameterType.TYPE_MAP;
	}

	@Override
	public void setType(ParameterType type) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get parameter into for element by key
	 * 
	 */
	@Override
	public IProblemParameterInfo getElement(String key) {
		return hash.get(key);
	}

	/**
	 * Set parameter info for element with the key equals to info.getKey()
	 * 
	 * @param i
	 * @param info
	 */
	public void setElement(IProblemParameterInfo info) {
		hash.put(info.getKey(), info);
	}

	@Override
	public Iterator<IProblemParameterInfo> getIterator() {
		return hash.values().iterator();
	}
}
