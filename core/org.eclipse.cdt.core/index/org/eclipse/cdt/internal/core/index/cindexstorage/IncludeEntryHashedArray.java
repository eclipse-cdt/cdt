/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.index.cindexstorage;

import org.eclipse.cdt.internal.core.CharOperation;

public final class IncludeEntryHashedArray {

	public IncludeEntry elements[];
	public int elementSize; // number of elements in the table
	public int threshold;
	
	public IncludeEntryHashedArray(int size) {
		if (size < 7) size = 7;
		this.elements = new IncludeEntry[2 * size + 1];
		this.elementSize = 0;
		this.threshold = size + 1; // size is the expected number of elements
	}
	
	public IncludeEntry add(IncludeEntry entry) {
		int length = elements.length;
		char[] word = entry.getFile();
		int index = CharOperation.hashCode(word) % length;
		IncludeEntry current;
		while ((current = elements[index]) != null) {
			if (CharOperation.equals(current.getFile(), word)) return elements[index] = entry;
			if (++index == length) index = 0;
		}
		elements[index] = entry;
	
		// assumes the threshold is never equal to the size of the table
		if (++elementSize > threshold) grow();
		return entry;
	}
	
	public IncludeEntry[] asArray() {
		IncludeEntry[] array = new IncludeEntry[elementSize];
		for (int i = 0, j = 0, length = elements.length; i < length; i++) {
			IncludeEntry current = elements[i];
			if (current != null) array[j++] = current;
		}
		return array;
	}
	
	public IncludeEntry get(char[] word) {
		int length = elements.length;
		int index = CharOperation.hashCode(word) % length;
		IncludeEntry current;
		while ((current = elements[index]) != null) {
			if (CharOperation.equals(current.getFile(), word)) return current;
			if (++index == length) index = 0;
		}
		return null;
	}
	
	private void grow() {
		IncludeEntryHashedArray newArray = new IncludeEntryHashedArray(elementSize * 2); // double the number of expected elements
		for (int i = 0, length = elements.length; i < length; i++)
			if (elements[i] != null)
				newArray.add(elements[i]);
	
		this.elements = newArray.elements;
		this.elementSize = newArray.elementSize;
		this.threshold = newArray.threshold;
	}
	
	public String toString() {
		String s = ""; //$NON-NLS-1$
		IncludeEntry[] entries = asArray();
		for (int i = 0, length = entries.length; i < length; i++)
			s += entries[i].toString() + "\n"; 	//$NON-NLS-1$
		return s;
	}

}
