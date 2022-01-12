/*******************************************************************************
 * Copyright (c) 2004, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.core.parser.util;

/**
 * @author Doug Schaefer
 * @noextend This class is not intended to be subclassed by clients.
 */
public class CharArrayIntMap extends CharTable {
	private int[] valueTable;
	public final int undefined;

	public CharArrayIntMap(int initialSize, int undefined) {
		super(initialSize);
		valueTable = new int[capacity()];
		this.undefined = undefined;
	}

	@Override
	protected void resize(int size) {
		int[] oldValueTable = valueTable;
		valueTable = new int[size];
		System.arraycopy(oldValueTable, 0, valueTable, 0, Math.min(size, oldValueTable.length));
		super.resize(size);
	}

	@Override
	public void clear() {
		super.clear();
		for (int i = 0; i < capacity(); i++)
			valueTable[i] = undefined;
	}

	@Override
	public Object clone() {
		CharArrayIntMap newMap = (CharArrayIntMap) super.clone();
		newMap.valueTable = new int[capacity()];
		System.arraycopy(valueTable, 0, newMap.valueTable, 0, valueTable.length);
		return newMap;
	}

	public int put(char[] key, int start, int length, int value) {
		int i = addIndex(key, start, length);
		int oldvalue = valueTable[i];
		valueTable[i] = value;
		return oldvalue;
	}

	public int put(char[] key, int value) {
		return put(key, 0, key.length, value);
	}

	public int get(char[] key, int start, int length) {
		int i = lookup(key, start, length);
		if (i >= 0)
			return valueTable[i];
		return undefined;
	}

	public int get(int pos) {
		if (pos < 0 || pos > currEntry)
			return undefined;
		return valueTable[pos];
	}

	public int getKeyLocation(char[] key, int start, int length) {
		int i = lookup(key, start, length);
		if (i >= 0)
			return i;
		return undefined;
	}

	public int get(char[] image) {
		return get(image, 0, image.length);
	}

	/**
	 * Puts all mappings of map into this map. The keys are not cloned.
	 * @since 5.0
	 */
	public void putAll(CharArrayIntMap map) {
		resize(size() + map.size());
		for (int i = 0; i <= map.currEntry; i++) {
			put(map.keyTable[i], map.valueTable[i]);
		}
	}
}
