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
import org.eclipse.cdt.internal.core.search.indexing.IndexManager;
import org.eclipse.cdt.internal.core.search.processing.JobManager;


public class EntryResult implements IEntryResult {
	private int[]  fileRefs;
	private int[][] offsets;
	private int[][] offsetLengths;
	
	private int meta_type;
	private int kind;
	private int reftype;
	private String longname;
	
public EntryResult(char[] word, int[] refs, int[][] offsets, int[][] offsetLengths) {
	this.fileRefs = refs;
	this.offsets = offsets;
	this.offsetLengths = offsetLengths;
	decode(word);
}
public boolean equals(Object anObject){
	
	if (this == anObject) {
		return true;
	}
	if ((anObject != null) && (anObject instanceof EntryResult)) {
		EntryResult anEntryResult = (EntryResult) anObject;
		if( this.meta_type != anEntryResult.meta_type || 
			this.kind != anEntryResult.kind ||
			this.reftype != anEntryResult.reftype ||
			! this.longname.equals(anEntryResult.longname))
			return false;

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
	return Index.encodeEntry(meta_type, kind, reftype, longname);
}
public int hashCode(){
	return CharOperation.hashCode(getWord());
}
public String toString(){
	StringBuffer buffer = new StringBuffer();
	buffer.append("EntryResult: " + getName() + "\n\tmeta="); //$NON-NLS-1$ //$NON-NLS-2$
	buffer.append(ICIndexStorageConstants.encodings[meta_type]);
	if(meta_type == IIndex.TYPE) {
		buffer.append(" type="); //$NON-NLS-1$
		buffer.append(ICIndexStorageConstants.typeConstantNames[kind]);
	}
	buffer.append(" Reference="); //$NON-NLS-1$
	buffer.append(ICIndexStorageConstants.encodingTypes[reftype]);
	
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
	buffer.append(" }\n"); //$NON-NLS-1$
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
	int sepPos=longname.indexOf(IndexerOutput.SEPARATOR);
	if (sepPos>=0)
		return longname.substring(0, sepPos);
	else
		return longname;
}

private void decode(char [] word) {
	int pos = 0;
	meta_type = 0;
	for (int i = 1; i < ICIndexStorageConstants.encodings.length; i ++){
		if (CharOperation.prefixEquals(ICIndexStorageConstants.encodings[i], word)) {
			meta_type = i;
			pos += ICIndexStorageConstants.encodings[i].length;
			break;
		}
	}
	
	for ( int i = 1; i < ICIndexStorageConstants.encodingTypes.length; i++) {		
		if (CharOperation.fragmentEquals(ICIndexStorageConstants.encodingTypes[i], word, pos, true)) {
			reftype = i;
			pos += ICIndexStorageConstants.encodingTypes[i].length;
			break;
		}
	}
	
	if (meta_type == IIndex.TYPE) {
		for (int i = 1; i < Index.typeConstants.length; i++) {
			if (word[pos] == Index.typeConstants[i]) {
				kind = i;
				pos++;
				break;
			}
		}
		// skip over separator
		if (word[pos] != ICIndexStorageConstants.SEPARATOR) {
			if (IndexManager.VERBOSE)
				JobManager.verbose("Invalid encoding encoutered while decoding Entry Result"); //$NON-NLS-1$
		}
		pos++;
	}
	else 
		kind = IIndex.ANY;
	
	longname = new String(word, pos, word.length - pos);
}

public String[] getEnclosingNames() {
	int slash=longname.indexOf(IndexerOutput.SEPARATOR);

	String[] enclosingNames= null;
	if (slash != -1 && slash + 1 < longname.length()) {
		char[][] temp = CharOperation.splitOn('/', CharOperation.subarray(longname.toCharArray(), slash + 1, -1));
		enclosingNames= new String[temp.length];
		for (int i = 0; i < temp.length; i++) {
			enclosingNames[i] = String.valueOf(temp[temp.length - i - 1]);
		}
	}
	return enclosingNames;
}
public int getMetaKind() {
	return meta_type;
}
public int getKind() {
	return kind;
}
public int getRefKind() {
	return reftype;
}
public String getName() {
	return longname;
}
public String getStringMetaKind() {
	return String.valueOf(ICIndexStorageConstants.encodings[meta_type]);
}
public String getStringKind() {
	return ICIndexStorageConstants.typeConstantNames[kind];
}
public String getStringRefKind() {
	return String.valueOf(ICIndexStorageConstants.encodingTypes[reftype]);
}

public String getDisplayString() {
	switch (meta_type) {
	case IIndex.FUNCTION:
	case IIndex.METHOD:
		int startReturn = longname.indexOf(")R/"); //$NON-NLS-1$
		int finishReturn = longname.indexOf("/R("); //$NON-NLS-1$
		int startParam = longname.indexOf("/)", finishReturn); //$NON-NLS-1$
		int finishParam = longname.indexOf("/(", startParam); //$NON-NLS-1$
		
		String functionName;
		String arguments = ""; //$NON-NLS-1$
		if (startParam + 2 < finishParam) 
			arguments = longname.substring(startParam + 3, finishParam);

		// TODO: flip arguments
		arguments = arguments.replace('/',',');
		
		arguments = '(' + arguments + ')';
		
		if (startReturn == -1 || finishReturn == -1) {
			// there is no return type !!!
			functionName = longname.substring(0, startParam -1);
			return functionName + arguments ;
		}
		else {
			String returnType = ""; //$NON-NLS-1$
			if (startReturn + 3 < finishReturn) {
				returnType = longname.substring(startReturn + 3, finishReturn);
			}
			functionName = longname.substring(0, startReturn -1);
			return functionName + arguments + ':' + returnType;
		}
		
	default:
		return longname;
	}
}

}

