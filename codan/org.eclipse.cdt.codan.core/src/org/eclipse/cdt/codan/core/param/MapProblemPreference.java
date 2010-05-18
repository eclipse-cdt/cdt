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
	public void addChildDescriptor(IProblemPreference info) {
		IProblemPreference desc = (IProblemPreference) info.clone();
		desc.setParent(this);
		hash.put(info.getKey(), desc);
	}

	public IProblemPreference[] getChildDescriptors() {
		return hash.values().toArray(
				new IProblemPreference[hash.values().size()]);
	}

	public Object getChildValue(String key) {
		IProblemPreference childInfo = getChildDescriptor(key);
		return childInfo.getValue();
	}

	public void addChildValue(String key, Object value) {
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
		int token;
		try {
			token = tokenizer.nextToken();
			if (token != '{')
				throw new IllegalArgumentException(str);
			while (true) {
				token = tokenizer.nextToken();
				String key = tokenizer.sval;
				token = tokenizer.nextToken();
				if (token != '=')
					throw new IllegalArgumentException(str);
				token = tokenizer.nextToken();
				if (token != '>')
					throw new IllegalArgumentException(str);
				token = tokenizer.nextToken();
				String val = tokenizer.sval;
				IProblemPreference desc = getChildDescriptor(key);
				if (desc != null) {
					desc.importValue(val);
				} else {
					//putChildValue(key, val);
				}
				token = tokenizer.nextToken();
				if (token == '}')
					break;
				if (token != ',')
					throw new IllegalArgumentException(str);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException(str);
		}
	}

	public void removeChildDescriptor(IProblemPreference info) {
		hash.remove(info);
	}
}
