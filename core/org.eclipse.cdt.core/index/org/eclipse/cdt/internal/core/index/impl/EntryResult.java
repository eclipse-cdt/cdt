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
package org.eclipse.cdt.internal.core.index.impl;

import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IEntryResult;


public class EntryResult implements IEntryResult {
	private char[] word;
	private int[]  fileRefs;
	private int[] indexFlags;
	
public EntryResult(char[] word, int[] refs, int[] indexFlags) {
	this.word = word;
	this.fileRefs = refs;
	this.indexFlags = indexFlags;
}
public boolean equals(Object anObject){
	
	if (this == anObject) {
		return true;
	}
	if ((anObject != null) && (anObject instanceof EntryResult)) {
		EntryResult anEntryResult = (EntryResult) anObject;
		if (!CharOperation.equals(this.word, anEntryResult.word)) return false;

		int length;
		int[] refs, otherRefs;
		if ((length = (refs = this.fileRefs).length) != (otherRefs = anEntryResult.fileRefs).length) return false;
		for (int i =  0; i < length; i++){
			if (refs[i] != otherRefs[i]) return false;
		}
		
		int[] indexRefs, indexOtherRefs;
		if ((length = (indexRefs = this.indexFlags).length) != (indexOtherRefs = anEntryResult.indexFlags).length) return false;
		for (int i =  0; i < length; i++){
			if (indexRefs[i] != indexOtherRefs[i]) return false;
		}
		
		return true;
	}
	return false;
	
}
public int[] getFileReferences() {
	return fileRefs;
}
public char[] getWord() {
	return word;
}
public int hashCode(){
	return CharOperation.hashCode(word);
}
public String toString(){
	StringBuffer buffer = new StringBuffer(word.length * 2);
	buffer.append("EntryResult: word="); //$NON-NLS-1$
	buffer.append(word);
	buffer.append(", refs={"); //$NON-NLS-1$
	for (int i = 0; i < fileRefs.length; i++){
		if (i > 0) buffer.append(',');
		buffer.append(' ');
		buffer.append(fileRefs[i]);
	}
	buffer.append(" }"); //$NON-NLS-1$
	return buffer.toString();
}

public int[] getIndexFlags() {
	return indexFlags;
}
}

