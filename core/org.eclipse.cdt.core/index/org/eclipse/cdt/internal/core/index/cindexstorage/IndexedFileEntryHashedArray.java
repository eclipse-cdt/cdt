/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.cindexstorage;

import java.util.ArrayList;

public final class IndexedFileEntryHashedArray {

private IndexedFileEntry elements[];
private int elementSize; // number of elements in the table
private int threshold;
private int lastId;
private ArrayList replacedElements;

public IndexedFileEntryHashedArray(int size) {
	if (size < 7) size = 7;
	this.elements = new IndexedFileEntry[2 * size + 1];
	this.elementSize = 0;
	this.threshold = size + 1; // size is the expected number of elements
	this.lastId = 0;
	this.replacedElements = null;
}

public IndexedFileEntry add(String path){
	return add(new IndexedFileEntry(path, ++lastId));
}

private IndexedFileEntry add(IndexedFileEntry file) {
	int length = elements.length;
	String path = file.getPath();
	int index = (path.hashCode() & 0x7FFFFFFF) % length;
	IndexedFileEntry current;
	while ((current = elements[index]) != null) {
		if (current.getPath().equals(path)) {
			if (replacedElements == null) replacedElements = new ArrayList(5);
			replacedElements.add(current);
			return elements[index] = file;
		}
		if (++index == length) index = 0;
	}
	elements[index] = file;

	// assumes the threshold is never equal to the size of the table
	if (++elementSize > threshold) grow();
	return file;
}

public IndexedFileEntry[] asArray() {
    IndexedFileEntry[] array = new IndexedFileEntry[lastId];
	for (int i = 0, length = elements.length; i < length; i++) {
	    IndexedFileEntry current = elements[i];
		if (current != null)
			array[current.getFileID() - 1] = current;
	}
	if (replacedElements != null) {
		for (int i = 0, length = replacedElements.size(); i < length; i++) {
		    IndexedFileEntry current = (IndexedFileEntry) replacedElements.get(i);
			array[current.getFileID() - 1] = current;
		}
	}
	return array;
}

public IndexedFileEntry get(String path) {
	int length = elements.length;
	int index = (path.hashCode() & 0x7FFFFFFF) % length;
	IndexedFileEntry current;
	while ((current = elements[index]) != null) {
		if (current.getPath().equals(path)) return current;
		if (++index == length) index = 0;
	}
	return null;
}

private void grow() {
	IndexedFileEntryHashedArray newArray = new IndexedFileEntryHashedArray(elementSize * 2); // double the number of expected elements
	for (int i = 0, length = elements.length; i < length; i++)
		if (elements[i] != null)
			newArray.add(elements[i]);

	// leave replacedElements as is
	this.elements = newArray.elements;
	this.elementSize = newArray.elementSize;
	this.threshold = newArray.threshold;
}

public int size() {
	return elementSize + (replacedElements == null ? 0 : replacedElements.size());
}

public String toString() {
	String s = ""; //$NON-NLS-1$
	IndexedFileEntry[] files = asArray();
	for (int i = 0, length = files.length; i < length; i++)
		s += files[i].toString() + "\n"; 	//$NON-NLS-1$
	return s;
}
}

