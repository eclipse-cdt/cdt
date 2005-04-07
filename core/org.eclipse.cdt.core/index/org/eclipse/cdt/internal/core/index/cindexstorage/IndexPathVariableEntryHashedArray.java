/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.cindexstorage;

import java.util.ArrayList;

public final class IndexPathVariableEntryHashedArray {

private IndexPathVariableEntry elements[];
private int elementSize; // number of elements in the table
private int threshold;
private int lastId;
private ArrayList replacedElements;

public IndexPathVariableEntryHashedArray(int size) {
	if (size < 7) size = 7;
	this.elements = new IndexPathVariableEntry[2 * size + 1];
	this.elementSize = 0;
	this.threshold = size + 1; // size is the expected number of elements
	this.lastId = 1; //start at 2; 1 is reserved for workspace variable
	this.replacedElements = null;
}

public IndexPathVariableEntry add(String pathVarName, String path){
	return add(new IndexPathVariableEntry(pathVarName, path, ++lastId));
}

/** 
 * Adds the IndexPathVariableEntry to the list, hashed by path
 * @param pathVar
 */ 
private IndexPathVariableEntry add(IndexPathVariableEntry pathVar) {
	int length = elements.length;
	String path = pathVar.getPathVariablePath();
	int index = (path.hashCode() & 0x7FFFFFFF) % length;
	IndexPathVariableEntry current;
	while ((current = elements[index]) != null) {
		if (current.getPathVariablePath().equals(path)) {
			if (replacedElements == null) replacedElements = new ArrayList(5);
			replacedElements.add(current);
			return elements[index] = pathVar;
		}
		if (++index == length) index = 0;
	}
	elements[index] = pathVar;

	// assumes the threshold is never equal to the size of the table
	if (++elementSize > threshold) grow();
	return pathVar;
}

public IndexPathVariableEntry[] asArray() {
    IndexPathVariableEntry[] array = new IndexPathVariableEntry[lastId];
	for (int i = 0, length = elements.length; i < length; i++) {
	    IndexPathVariableEntry current = elements[i];
		if (current != null)
			array[current.getId() - 1] = current;
	}
	if (replacedElements != null) {
		for (int i = 0, length = replacedElements.size(); i < length; i++) {
		    IndexPathVariableEntry current = (IndexPathVariableEntry) replacedElements.get(i);
			array[current.getId() - 1] = current;
		}
	}
	return array;
}

/**
 * Returns corresponing IndexPathVariableEntry for the given path or
 * null if it hasn't been added to the array
 * @param path
 * @return
 */
public IndexPathVariableEntry get(String path) {
	int length = elements.length;
	int index = (path.hashCode() & 0x7FFFFFFF) % length;
	IndexPathVariableEntry current;
	while ((current = elements[index]) != null) {
		if (current.getPathVariablePath().equals(path)) return current;
		if (++index == length) index = 0;
	}
	return null;
}

private void grow() {
	IndexPathVariableEntryHashedArray newArray = new IndexPathVariableEntryHashedArray(elementSize * 2); // double the number of expected elements
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
	IndexPathVariableEntry[] files = asArray();
	for (int i = 0, length = files.length; i < length; i++)
		s += files[i].toString() + "\n"; 	//$NON-NLS-1$
	return s;
}
}

