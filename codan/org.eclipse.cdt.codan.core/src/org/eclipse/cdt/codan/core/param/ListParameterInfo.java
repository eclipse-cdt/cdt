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

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Alena
 * 
 */
public class ListParameterInfo extends SingleParameterInfo {
	protected ArrayList<IProblemParameterInfo> list = new ArrayList<IProblemParameterInfo>(
			1);

	/**
	 * @param key
	 * @param label
	 */
	public ListParameterInfo(String key, String label) {
		super(key, label);
	}

	@Override
	public ParameterType getType() {
		return ParameterType.TYPE_LIST;
	}

	@Override
	public void setType(ParameterType type) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get parameter into for element equal to key's int value,
	 * 
	 * @throws NumberFormatException
	 *             if key is not number
	 * @throws ArrayIndexOutOfBoundsException
	 *             is index is out of bound
	 */
	@Override
	public IProblemParameterInfo getElement(String key)
			throws NumberFormatException, ArrayIndexOutOfBoundsException {
		if (key == null) {
			// special case if all element are the same return first, if key is
			// null
			return list.get(0);
		}
		Integer iv = Integer.valueOf(key);
		if (iv.intValue() >= list.size() && list.size() == 1) {
			// special case if all element are the same return first
			return list.get(0);
		}
		return list.get(iv.intValue());
	}

	/**
	 * Set i'th element of parameter info, if all are the same i is 0
	 * 
	 * @param i
	 * @param info
	 */
	public void setElement(int i, IProblemParameterInfo info) {
		while (i >= list.size()) {
			list.add(null);
		}
		list.set(i, info);
	}

	public IProblemParameterInfo getElement(int i) {
		return list.get(i);
	}

	@Override
	public Iterator<IProblemParameterInfo> getIterator() {
		return list.iterator();
	}
}
