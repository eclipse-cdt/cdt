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
 * "check constructors" and
 * "check setters". In this case you use this type.
 * {@link AbstractCheckerWithProblemPreferences} class has map as default top
 * level parameter preference.
 *
 * @noextend This class is not intended to be extended by clients.
 */
public class MapProblemPreference extends AbstractProblemPreference implements IProblemPreferenceCompositeValue,
		IProblemPreferenceCompositeDescriptor {
	protected LinkedHashMap<String, IProblemPreference> hash = new LinkedHashMap<String, IProblemPreference>();

	/**
	 * Default constructor
	 */
	public MapProblemPreference() {
		super();
	}

	/**
	 * @param key
	 *        - key for itself
	 * @param label
	 *        - label for this group of parameters
	 */
	public MapProblemPreference(String key, String label) {
		setKey(key);
		setLabel(label);
	}

	@Override
	public PreferenceType getType() {
		return PreferenceType.TYPE_MAP;
	}

	/**
	 * Get parameter preference for element by key
	 *
	 */
	@Override
	public IProblemPreference getChildDescriptor(String key) {
		return hash.get(key);
	}

	/**
	 * Adds or replaces child descriptor and value for the element with the key
	 * equals to desc.getKey(). The desc object would be put in the map, some of
	 * its field may be modified.
	 *
	 * @param desc
	 */
	@Override
	public IProblemPreference addChildDescriptor(IProblemPreference desc) {
		((AbstractProblemPreference) desc).setParent(this);
		hash.put(desc.getKey(), desc);
		return desc;
	}

	/**
	 * Return list of child descriptors. Client should threat returned value as
	 * read only,
	 * and not assume that modifying its elements would modify actual child
	 * values.
	 */
	@Override
	public IProblemPreference[] getChildDescriptors() {
		return hash.values().toArray(new IProblemPreference[hash.values().size()]);
	}

	/**
	 * Returns value of the child element by its key
	 */
	@Override
	public Object getChildValue(String key) {
		IProblemPreference childInfo = getChildDescriptor(key);
		return childInfo.getValue();
	}

	/**
	 * Set child value by its key
	 */
	@Override
	public void setChildValue(String key, Object value) {
		IProblemPreference pref = getChildDescriptor(key);
		if (pref == null)
			throw new IllegalArgumentException("Preference for " + key //$NON-NLS-1$
					+ " must exists before setting its value"); //$NON-NLS-1$
		pref.setValue(value);
		hash.put(key, pref); // cannot assume getChildDescriptor returns shared value
	}

	/**
	 * Removes child value and descriptor by key
	 */
	@Override
	public void removeChildValue(String key) {
		hash.remove(key);
	}

	@Override
	public Object clone() {
		MapProblemPreference map = (MapProblemPreference) super.clone();
		map.hash = new LinkedHashMap<String, IProblemPreference>();
		for (Iterator<String> iterator = hash.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			map.hash.put(key, (IProblemPreference) hash.get(key).clone());
		}
		return map;
	}

	@Override
	public String exportValue() {
		StringBuffer buf = new StringBuffer("{"); //$NON-NLS-1$
		for (Iterator<String> iterator = hash.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			IProblemPreference d = hash.get(key);
			if (d instanceof AbstractProblemPreference) {
				if (((AbstractProblemPreference) d).isDefault()) {
					continue;
				}
			}
			buf.append(key + "=>" + d.exportValue()); //$NON-NLS-1$
			if (iterator.hasNext())
				buf.append(","); //$NON-NLS-1$
		}
		return buf.toString() + "}"; //$NON-NLS-1$
	}

	@Override
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
			if (token != '{') {
				throw new IllegalArgumentException(String.valueOf((char) token));
			}
			while (true) {
				token = tokenizer.nextToken();
				if (token == '}')
					break;
				String key = tokenizer.sval;
				token = tokenizer.nextToken();
				if (token != '=')
					throw new IllegalArgumentException(String.valueOf((char) token));
				token = tokenizer.nextToken();
				if (token != '>')
					throw new IllegalArgumentException(String.valueOf((char) token));
				importChildValue(key, tokenizer);
				token = tokenizer.nextToken();
				if (token == '}')
					break;
				if (token != ',')
					throw new IllegalArgumentException(String.valueOf((char) token));
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(e);
		}
	}

	/**
	 * @param key
	 * @param tokenizer
	 * @return
	 * @throws IOException
	 * @since 2.0
	 */
	protected IProblemPreference importChildValue(String key, StreamTokenizer tokenizer) throws IOException {
		IProblemPreference desc = getChildDescriptor(key);
		if (desc != null && desc instanceof AbstractProblemPreference) {
			((AbstractProblemPreference) desc).importValue(tokenizer);
			setChildValue(key, desc.getValue());
		}
		return desc;
	}

	/**
	 * Removes child descriptor by its key
	 */
	@Override
	public void removeChildDescriptor(IProblemPreference info) {
		hash.remove(info.getKey());
	}

	/**
	 * @return size of the map
	 */
	public int size() {
		return hash.size();
	}

	/**
	 * Clears the map
	 */
	public void clear() {
		hash.clear();
	}

	@Override
	public String toString() {
		return hash.values().toString();
	}

	/**
	 * Value of this preference is a map key=>value of child preferences.
	 * Modifying this returned map would not change internal state of this
	 * object.
	 */
	@Override
	public Object getValue() {
		LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
		for (Iterator<IProblemPreference> iterator = hash.values().iterator(); iterator.hasNext();) {
			IProblemPreference pref = iterator.next();
			map.put(pref.getKey(), pref.getValue());
		}
		return map;
	}

	/**
	 * Set values for this object child elements. Elements are not present in
	 * this map would be removed.
	 * Preference descriptors for the keys must be set before calling this
	 * method, unless value if instanceof {@link IProblemPreference}.
	 *
	 * @param value - must be Map<String,Object>
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void setValue(Object value) {
		Map<String, Object> map = (Map<String, Object>) value;
		LinkedHashMap<String, IProblemPreference> hash2 = (LinkedHashMap<String, IProblemPreference>) hash.clone();
		hash.clear();
		for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext();) {
			String key = iterator.next();
			Object value2 = map.get(key);
			if (value2 instanceof IProblemPreference) {
				hash.put(key, (IProblemPreference) value2);
			} else {
				IProblemPreference pref = hash2.get(key);
				addChildDescriptor(pref);
				//setChildValue(key, value2);
				pref.setValue(value2);
				hash.put(key, pref);
			}
		}
	}
}
