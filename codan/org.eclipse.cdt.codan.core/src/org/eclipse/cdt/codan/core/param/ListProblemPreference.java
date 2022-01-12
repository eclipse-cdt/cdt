/*******************************************************************************
 * Copyright (c) 2009, 2016 QNX Software Systems
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    QNX Software Systems (Alena Laskavaia)  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.core.param;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * List implementation of IProblemPreference.
 *
 * @noextend This class is not intended to be extended by clients.
 */
public class ListProblemPreference extends AbstractProblemPreference
		implements IProblemPreferenceCompositeValue, IProblemPreferenceCompositeDescriptor {
	/**
	 * Constant that represent a key for "shared" child preference (descriptor)
	 * of all elements.
	 */
	public static final String COMMON_DESCRIPTOR_KEY = "#"; //$NON-NLS-1$
	protected ArrayList<Object> list = new ArrayList<>();
	protected IProblemPreference childDescriptor;

	/**
	 * @param key
	 *        - key to access this preference
	 * @param label
	 *        - label to be shown in UI
	 */
	public ListProblemPreference(String key, String label) {
		setKey(key);
		setLabel(label);
	}

	@Override
	public PreferenceType getType() {
		return PreferenceType.TYPE_LIST;
	}

	/**
	 * Sets child descriptor (all elements have the same). Value and key
	 * of it would be ignored and reset.
	 *
	 * @param desc
	 * @return set child descriptor
	 */
	public IProblemPreference setChildDescriptor(IProblemPreference desc) {
		childDescriptor = desc;
		if (desc != null) {
			childDescriptor.setValue(null);
			((AbstractProblemPreference) childDescriptor).setKey(COMMON_DESCRIPTOR_KEY);
		}
		return desc;
	}

	/**
	 * Sets common descriptor for all elements, if value if not null sets the
	 * value for its key also. Do not make assumptions of values of desc after
	 * you pass it to this function.
	 *
	 * @return read only preference matching the key
	 */
	@Override
	public IProblemPreference addChildDescriptor(IProblemPreference desc) {
		Object value = desc.getValue();
		String key = desc.getKey();
		setChildDescriptor(desc);
		setChildValue(key, value);
		return getChildDescriptor(key);
	}

	/**
	 * @return descriptor of the child elements
	 */
	public IProblemPreference getChildDescriptor() {
		return childDescriptor;
	}

	/**
	 * Returns cloned descriptor of the i'th child. Modifying return value would
	 * not affect internal state of the list element.
	 *
	 * @param i - index of the element
	 * @return child preference
	 */
	public IProblemPreference getChildDescriptor(int i) {
		Object value = list.get(i);
		AbstractProblemPreference desc = (AbstractProblemPreference) childDescriptor.clone();
		desc.setKey(String.valueOf(i));
		desc.setValue(value);
		return desc;
	}

	/**
	 * Get read only problem preference for element equal to key's int value.
	 * If key is null or # return generic descriptor with null value.
	 *
	 * @throws NumberFormatException if key is not number
	 */
	@Override
	public IProblemPreference getChildDescriptor(String key) throws NumberFormatException {
		if (key == null || key.equals(COMMON_DESCRIPTOR_KEY)) {
			// return common descriptor
			return getChildDescriptor();
		}
		Integer iv = Integer.valueOf(key);
		if (iv.intValue() >= list.size()) {
			// create one
			AbstractProblemPreference clone = (AbstractProblemPreference) childDescriptor.clone();
			clone.setKey(key);
			return clone;
		}
		return getChildDescriptor(iv.intValue());
	}

	/**
	 * Returns array of clones values of child preferences.
	 */
	@Override
	public IProblemPreference[] getChildDescriptors() {
		IProblemPreference[] res = new IProblemPreference[list.size()];
		for (int i = 0; i < res.length; i++) {
			res[i] = getChildDescriptor(i);
		}
		return res;
	}

	@Override
	public Object getChildValue(String key) {
		int index = Integer.parseInt(key);
		return getChildValue(index);
	}

	/**
	 * @param index - index of the element
	 * @return child value by index
	 */
	public Object getChildValue(int index) {
		return list.get(index);
	}

	@Override
	public void setChildValue(String key, Object value) {
		int i = Integer.valueOf(key).intValue();
		setChildValue(i, value);
	}

	/**
	 * @param i - index of the element
	 * @param value - value of the child element
	 */
	public void setChildValue(int i, Object value) {
		if (value != null) {
			while (i >= list.size()) {
				list.add(null);
			}
			list.set(i, value);
		} else {
			while (i == list.size() - 1) {
				list.remove(i);
			}
		}
	}

	/**
	 * Adds value to the list
	 *
	 * @param value
	 */
	public void addChildValue(Object value) {
		list.add(value);
	}

	/**
	 * Removes child value by key
	 */
	@Override
	public void removeChildValue(String key) {
		int index = Integer.parseInt(key);
		list.remove(index);
	}

	@Override
	public Object clone() {
		ListProblemPreference list1 = (ListProblemPreference) super.clone();
		list1.list = new ArrayList<>();
		list1.setChildDescriptor((IProblemPreference) getChildDescriptor().clone());
		for (Iterator<Object> iterator = list.iterator(); iterator.hasNext();) {
			Object value = iterator.next();
			list1.addChildValue(value);
		}
		return list1;
	}

	@Override
	public String exportValue() {
		StringBuilder buf = new StringBuilder("("); //$NON-NLS-1$
		for (Iterator<Object> iterator = list.iterator(); iterator.hasNext();) {
			IProblemPreference d = (IProblemPreference) childDescriptor.clone();
			d.setValue(iterator.next());
			buf.append(d.exportValue());
			if (iterator.hasNext())
				buf.append(","); //$NON-NLS-1$
		}
		return buf.toString() + ")"; //$NON-NLS-1$
	}

	@Override
	public void importValue(String str) {
		if (str.isEmpty())
			return;
		StreamTokenizer tokenizer = getImportTokenizer(str);
		try {
			importValue(tokenizer);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(str, e);
		}
	}

	@Override
	public void importValue(StreamTokenizer tokenizer) {
		clear();
		int token;
		int index = 0;
		try {
			token = tokenizer.nextToken();
			String chara = String.valueOf((char) token);
			if (token != '(')
				throw new IllegalArgumentException(chara);
			token = tokenizer.nextToken();
			if (token != ')') {
				tokenizer.pushBack();
			} else {
				return;
			}
			while (true) {
				String ik = String.valueOf(index);
				IProblemPreference desc = getChildDescriptor(ik);
				if (desc != null && desc instanceof AbstractProblemPreference) {
					((AbstractProblemPreference) desc).importValue(tokenizer);
					setChildValue(ik, desc.getValue());
				}
				token = tokenizer.nextToken();
				if (token == ')')
					break;
				if (token != ',')
					throw new IllegalArgumentException(chara);
				index++;
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * If info key is '#' resets common descriptor to null, otherwise removes value.
	 */
	@Override
	public void removeChildDescriptor(IProblemPreference info) {
		if (info.getKey().equals(COMMON_DESCRIPTOR_KEY)) {
			setChildDescriptor(null);
		} else {
			removeChildValue(info.getKey());
		}
	}

	/**
	 * @return children size
	 */
	public int size() {
		return list.size();
	}

	/**
	 * Removes all values from the list
	 */
	public void clear() {
		list.clear();
	}

	/**
	 * @return array of values of children elements.
	 */
	@Override
	public Object getValue() {
		return getValues();
	}

	/**
	 * Sets list value to values of array given as argument.
	 *
	 * @param value - must be Object[]
	 */
	@Override
	public void setValue(Object value) {
		Object[] values = (Object[]) value;
		if (Arrays.deepEquals(getValues(), values)) {
			return;
		}
		list.clear();
		for (int i = 0; i < values.length; i++) {
			Object object = values[i];
			list.add(object);
		}
	}

	@Override
	public String toString() {
		return childDescriptor + ":" + list.toString(); //$NON-NLS-1$
	}

	/**
	 * @return array of values of children elements.
	 */
	public Object[] getValues() {
		return list.toArray(new Object[list.size()]);
	}
}
