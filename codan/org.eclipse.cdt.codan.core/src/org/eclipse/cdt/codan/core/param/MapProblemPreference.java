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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.codan.core.model.AbstractCheckerWithProblemPreferences;

/**
 * MapProblemPreference - for checker that needs more than one preferences and
 * they all differently "named".
 * For example checker for parameter names shadowing would have two boolean
 * options:
 * "check contructors" and
 * "check setters". In this case you use this type.
 * {@link AbstractCheckerWithProblemPreferences} class has map as default top
 * level parameter preference.
 * 
 * @noextend This class is not intended to be extended by clients.
 */
public class MapProblemPreference extends AbstractProblemPreference implements
		IProblemPreferenceCompositeValue, IProblemPreferenceCompositeDescriptor {
	protected LinkedHashMap<String, IProblemPreference> hash = new LinkedHashMap<String, IProblemPreference>();

	public MapProblemPreference() {
		super();
	}

	/**
	 * @param key
	 *            - key for itself
	 * @param label
	 *            - label for this group of parameters
	 */
	public MapProblemPreference(String key, String label) {
		setKey(key);
		setLabel(label);
	}

	@Override
	public PreferenceType getType() {
		return PreferenceType.TYPE_MAP;
	}

	@Override
	public void setType(PreferenceType type) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Get parameter into for element by key
	 * 
	 */
	public IProblemPreference getChildDescriptor(String key) {
		return hash.get(key);
	}

	/**
	 * Put parameter info into the map for element with the key equals to
	 * info.getKey()
	 * 
	 * @param i
	 * @param info
	 */
	public IProblemPreference addChildDescriptor(IProblemPreference desc) {
		desc.setParent(this);
		hash.put(desc.getKey(), desc);
		return desc;
	}

	public IProblemPreference[] getChildDescriptors() {
		return hash.values().toArray(
				new IProblemPreference[hash.values().size()]);
	}

	public Object getChildValue(String key) {
		IProblemPreference childInfo = getChildDescriptor(key);
		return childInfo.getValue();
	}

	public void setChildValue(String key, Object value) {
		getChildDescriptor(key).setValue(value);
	}

	public void removeChildValue(String key) {
		hash.remove(key);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Object clone() {
		MapProblemPreference map = (MapProblemPreference) super.clone();
		map.hash = (LinkedHashMap<String, IProblemPreference>) hash.clone();
		return map;
	}

	public String exportValue() {
		StringBuffer buf = new StringBuffer("{"); //$NON-NLS-1$
		for (Iterator<String> iterator = hash.keySet().iterator(); iterator
				.hasNext();) {
			String key = iterator.next();
			IProblemPreference d = hash.get(key);
			buf.append(key + "=>" + d.exportValue()); //$NON-NLS-1$
			if (iterator.hasNext())
				buf.append(","); //$NON-NLS-1$
		}
		return buf.toString() + "}"; //$NON-NLS-1$
	}

	public void importValue(String str) {
		StreamTokenizer tokenizer = getImportTokenizer(str);
		try {
			importValue(tokenizer);
		} catch (IllegalArgumentException e) {
			throw new IllegalArgumentException(str + ":" + e.toString(), e); //$NON-NLS-1$
		}
	}

	/**
	 * @param tokenizer
	 */
	@Override
	public void importValue(StreamTokenizer tokenizer) {
		int token;
		try {
			token = tokenizer.nextToken();
			String chara = String.valueOf((char) token);
			if (token != '{') {
				throw new IllegalArgumentException(chara);
			}
			while (true) {
				token = tokenizer.nextToken();
				String key = tokenizer.sval;
				token = tokenizer.nextToken();
				if (token != '=')
					throw new IllegalArgumentException(chara);
				token = tokenizer.nextToken();
				if (token != '>')
					throw new IllegalArgumentException(chara);
				IProblemPreference desc = getChildDescriptor(key);
				if (desc != null && desc instanceof AbstractProblemPreference) {
					((AbstractProblemPreference) desc).importValue(tokenizer);
					setChildValue(key, desc.getValue());
				}
				token = tokenizer.nextToken();
				if (token == '}')
					break;
				if (token != ',')
					throw new IllegalArgumentException(chara);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	public void removeChildDescriptor(IProblemPreference info) {
		hash.remove(info);
	}

	public int size() {
		return hash.size();
	}

	public void clear() {
		hash.clear();
	}

	@Override
	public String toString() {
		return hash.values().toString();
	}

	@Override
	public Object getValue() {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		for (Iterator<IProblemPreference> iterator = hash.values().iterator(); iterator
				.hasNext();) {
			IProblemPreference pref = iterator.next();
			map.put(pref.getKey(), pref.getValue());
		}
		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setValue(Object value) {
		Map<String, Object> map = (Map<String, Object>) value;
		LinkedHashMap<String, IProblemPreference> hash2 = (LinkedHashMap<String, IProblemPreference>) hash
				.clone();
		hash.clear();
		for (Iterator<String> iterator = map.keySet().iterator(); iterator
				.hasNext();) {
			String key = iterator.next();
			Object value2 = map.get(key);
			if (value2 instanceof IProblemPreference) {
				hash.put(key, (IProblemPreference) value2);
			} else {
				IProblemPreference pref = hash2.get(key);
				pref.setValue(value2);
				hash.put(key, pref);
			}
		}
	}
}
