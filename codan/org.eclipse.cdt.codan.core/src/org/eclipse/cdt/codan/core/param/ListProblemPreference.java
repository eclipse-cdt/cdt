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

import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Alena
 * 
 */
public class ListProblemPreference extends AbstractProblemPreference implements
		IProblemPreferenceContainer {
	protected ArrayList<IProblemPreference> list = new ArrayList<IProblemPreference>(
			1);

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
	 * Get parameter into for element equal to key's int value,
	 * 
	 * @throws NumberFormatException
	 *             if key is not number
	 * @throws ArrayIndexOutOfBoundsException
	 *             is index is out of bound
	 */
	@Override
	public IProblemPreference getChildDescriptor(String key)
			throws NumberFormatException {
		if (key == null) {
			// special case if all element are the same return first, if key is
			// null
			return (IProblemPreference) getChildPreference(0).clone();
		}
		Integer iv = Integer.valueOf(key);
		if (iv.intValue() >= list.size()) {
			// special case if all element are the same return first clone
			IProblemPreference childInfo = (IProblemPreference) getChildPreference(
					0).clone();
			return childInfo;
		}
		return getChildPreference(iv.intValue());
	}

	/**
	 * Set i'th element of parameter info, if all are the same i is 0
	 * 
	 * @param i
	 * @param info
	 */
	public void setChildPreference(int i, IProblemPreference info) {
		if (info != null) {
			while (i >= list.size()) {
				list.add(null);
			}
			list.set(i, info);
		} else {
			while (i == list.size() - 1) {
				list.remove(i);
			}
		}
	}

	/**
	 * If all list elements have same info it is enough to set only first one
	 * (index 0)
	 */
	@Override
	public void addChildDescriptor(IProblemPreference info) {
		Integer iv = Integer.valueOf(info.getKey());
		IProblemPreference desc = (IProblemPreference) info.clone();
		desc.setParent(this);
		setChildPreference(iv, desc);
	}

	public IProblemPreference getChildPreference(int i) {
		return list.get(i);
	}

	@Override
	public IProblemPreference[] getChildDescriptors() {
		return list.toArray(new IProblemPreference[list.size()]);
	}

	public Object getChildValue(String key) {
		IProblemPreference childInfo = getChildDescriptor(key);
		return childInfo.getValue();
	}

	public void addChildValue(String key, Object value) {
		IProblemPreference pref = getChildDescriptor(key);
		pref.setValue(value);
		// because descriptor can be phantom  we have to set preference phisically 
		setChildPreference(Integer.parseInt(key), pref);
	}

	public void removeChildValue(String key) {
		int index = Integer.parseInt(key);
		list.remove(index);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		ListProblemPreference list1 = (ListProblemPreference) super.clone();
		list1.list = (ArrayList<IProblemPreference>) list.clone();
		return list1;
	}

	public String exportValue() {
		StringBuffer buf = new StringBuffer("("); //$NON-NLS-1$
		for (Iterator<IProblemPreference> iterator = list.iterator(); iterator
				.hasNext();) {
			IProblemPreference d = iterator.next();
			buf.append(d.exportValue());
			if (iterator.hasNext())
				buf.append(","); //$NON-NLS-1$
		}
		return buf.toString() + ")"; //$NON-NLS-1$
	}

	public void importValue(String str) {
		StreamTokenizer tokenizer = getImportTokenizer(str);
		int token;
		int index = 0;
		try {
			token = tokenizer.nextToken();
			if (token != '(')
				throw new IllegalArgumentException(str);
			while (true) {
				token = tokenizer.nextToken();
				String val = tokenizer.sval;
				String ik = String.valueOf(index);
				IProblemPreference desc = getChildDescriptor(ik);
				if (desc != null) {
					desc.importValue(val);
					addChildValue(ik, desc.getValue());
				}
				token = tokenizer.nextToken();
				if (token == ')')
					break;
				if (token != ',')
					throw new IllegalArgumentException(str);
				index++;
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(str);
		}
	}
}
