/*******************************************************************************
 * Copyright (c) 2009,2010 QNX Software Systems
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
public class ListProblemPreference extends AbstractProblemPreference implements
		IProblemPreferenceCompositeValue, IProblemPreferenceCompositeDescriptor {
	public static final String COMMON_DESCRIPTOR_KEY = "#"; //$NON-NLS-1$
	protected ArrayList<Object> list = new ArrayList<Object>();
	protected IProblemPreference childDescriptor;

	/**
	 * @param key
	 * @param label
	 */
	public ListProblemPreference(String key, String label) {
		setKey(key);
		setLabel(label);
	}

	@Override
	public PreferenceType getType() {
		return PreferenceType.TYPE_LIST;
	}

	@Override
	public void setType(PreferenceType type) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Set child descriptor (all elements have the same)
	 * 
	 * @param i
	 * @param info
	 * @return
	 */
	public IProblemPreference setChildDescriptor(IProblemPreference info) {
		childDescriptor = info;
		childDescriptor.setValue(null);
		((AbstractProblemPreference) childDescriptor)
				.setKey(COMMON_DESCRIPTOR_KEY);
		return info;
	}

	/**
	 * Sets common descriptor for all elements, if value if not null sets the
	 * value for its key also. Do not make assumptions of values of desc after
	 * you pass it to this function.
	 * 
	 * @return read only preference matching the key
	 */
	public IProblemPreference addChildDescriptor(IProblemPreference desc) {
		Object value = desc.getValue();
		String key = desc.getKey();
		setChildDescriptor(desc);
		setChildValue(key, value);
		return getChildDescriptor(key);
	}

	public IProblemPreference getChildDescriptor() {
		return childDescriptor;
	}

	public IProblemPreference getChildDescriptor(int i) {
		Object value = list.get(i);
		AbstractProblemPreference desc = (AbstractProblemPreference) childDescriptor
				.clone();
		desc.setKey(String.valueOf(i));
		desc.setValue(value);
		return desc;
	}

	/**
	 * Get read only problem preference for element equal to key's int value.
	 * If key is null or # return generic descriptor with null value.
	 * 
	 * @throws NumberFormatException
	 *             if key is not number
	 */
	public IProblemPreference getChildDescriptor(String key)
			throws NumberFormatException {
		if (key == null || key.equals(COMMON_DESCRIPTOR_KEY)) {
			// return common descriptor
			return getChildDescriptor();
		}
		Integer iv = Integer.valueOf(key);
		if (iv.intValue() >= list.size()) {
			// create one
			AbstractProblemPreference clone = (AbstractProblemPreference) childDescriptor
					.clone();
			clone.setKey(key);
			return clone;
		}
		return getChildDescriptor(iv.intValue());
	}

	public IProblemPreference[] getChildDescriptors() {
		IProblemPreference[] res = new IProblemPreference[list.size()];
		for (int i = 0; i < res.length; i++) {
			res[i] = getChildDescriptor(i);
		}
		return res;
	}

	public Object getChildValue(String key) {
		IProblemPreference childInfo = getChildDescriptor(key);
		return childInfo.getValue();
	}

	public void setChildValue(String key, Object value) {
		int i = Integer.valueOf(key).intValue();
		setChildValue(i, value);
	}

	/**
	 * @param i
	 * @param value
	 */
	protected void setChildValue(int i, Object value) {
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

	public void addChildValue(Object value) {
		list.add(value);
	}

	public void removeChildValue(String key) {
		int index = Integer.parseInt(key);
		list.remove(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		ListProblemPreference list1 = (ListProblemPreference) super.clone();
		list1.list = (ArrayList<Object>) list.clone();
		return list1;
	}

	public String exportValue() {
		StringBuffer buf = new StringBuffer("("); //$NON-NLS-1$
		for (Iterator<Object> iterator = list.iterator(); iterator.hasNext();) {
			IProblemPreference d = (IProblemPreference) childDescriptor.clone();
			d.setValue(iterator.next());
			buf.append(d.exportValue());
			if (iterator.hasNext())
				buf.append(","); //$NON-NLS-1$
		}
		return buf.toString() + ")"; //$NON-NLS-1$
	}

	public void importValue(String str) {
		StreamTokenizer tokenizer = getImportTokenizer(str);
		try {
			importValue(tokenizer);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(str, e);
		}
	}

	/**
	 * @param tokenizer
	 */
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
			if (token != ')')
				tokenizer.pushBack();
			else
				return;
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

	public void removeChildDescriptor(IProblemPreference info) {
		throw new UnsupportedOperationException();
	}

	public int size() {
		return list.size();
	}

	public void clear() {
		list.clear();
	}

	@Override
	public Object getValue() {
		return getValues();
	}

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

	public Object[] getValues() {
		return list.toArray(new Object[list.size()]);
	}
}
