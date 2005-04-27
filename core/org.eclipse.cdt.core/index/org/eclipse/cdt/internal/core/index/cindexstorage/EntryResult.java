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

import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;


public class EntryResult implements IEntryResult {
	private char[] word;
	private int[]  fileRefs;
	private int[][] offsets;
	private int[][] offsetLengths;
	
public EntryResult(char[] word, int[] refs, int[][] offsets, int[][] offsetLengths) {
	this.word = word;
	this.fileRefs = refs;
	this.offsets = offsets;
	this.offsetLengths = offsetLengths;
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
	buffer.append(" }, offsets={"); //$NON-NLS-1$
	for (int i = 0; i < offsets.length; i++){
		if (i > 0) buffer.append(',');
		buffer.append(' ');
		buffer.append('['); 
		for (int j=0; j<offsets[i].length; j++){
		    if (j > 0) buffer.append(',');
			buffer.append(' ');
		    buffer.append(offsets[i][j]);
		}
		buffer.append(']'); 
	}
	buffer.append(" }"); //$NON-NLS-1$
	return buffer.toString();
}
/* (non-Javadoc)
 * @see org.eclipse.cdt.internal.core.index.IEntryResult#getOffsets()
 */
public int[][] getOffsets() {
    return offsets;
}
/* (non-Javadoc)
 * @see org.eclipse.cdt.internal.core.index.IEntryResult#getOffsetLengths()
 */
public int[][] getOffsetLengths() {
	return offsetLengths;
}
/* (non-Javadoc)
 * @see org.eclipse.cdt.internal.core.index.IEntryResult#extractSimpleName()
 */
public String extractSimpleName() {
	return EntryResult.extractSimpleName(getWord());
}
/* (non-Javadoc)
 * @see org.eclipse.cdt.internal.core.index.IEntryResult#extractSimpleName(char[])
 */
public static String extractSimpleName(char[] word) {
	String aWord = new String(word);
	String longName=null;
	
	String typeDecl = Index.getDescriptionOf(IIndex.TYPE, IIndex.ANY, IIndex.DECLARATION);
	String typeDef = Index.getDescriptionOf(IIndex.TYPE, IIndex.ANY, IIndex.DEFINITION);
	String typeRef = Index.getDescriptionOf(IIndex.TYPE, IIndex.ANY, IIndex.REFERENCE);
	
	if (aWord.indexOf(typeDecl) == 0) {
		longName = aWord.substring(aWord.indexOf(IndexerOutput.SEPARATOR, typeDecl.length())+1);
	} else if (aWord.indexOf(typeRef) == 0) {
		longName = aWord.substring(aWord.indexOf(IndexerOutput.SEPARATOR, typeRef.length())+1); 
	} else if (aWord.indexOf(typeDef) == 0) {
		longName = aWord.substring(aWord.indexOf(IndexerOutput.SEPARATOR, typeDef.length())+1); 
	} else {
		longName = aWord.substring(aWord.indexOf(IndexerOutput.SEPARATOR)+1); 
	}

	int sepPos=longName.indexOf(IndexerOutput.SEPARATOR);
	if (sepPos>=0)
		return aWord.substring(0, longName.indexOf(IndexerOutput.SEPARATOR));
	else
		return longName;
}

}

