/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.impl;

import org.eclipse.cdt.core.parser.util.ObjectSet;
import org.eclipse.cdt.internal.core.CharOperation;

public class WordEntry {
	protected char[] fWord;
	protected int fNumRefs;
	protected int[] fRefs;
	protected int[] fRefsIndexFlags;
	
	public WordEntry() {
		this(CharOperation.NO_CHAR);
	}
	public WordEntry(char[] word) {
		fWord= word;
		fNumRefs= 0;
		fRefs= new int[1];
		fRefsIndexFlags = new int[1];
	}
	/**
	 * Adds a reference and records the change in footprint.
	 */
	public int addRef(int fileNum, int indexFlags) {
		if (fNumRefs > 0 && fRefs[fNumRefs - 1] == fileNum) {
			return 0;
		}
		if (fNumRefs < fRefs.length) {
			int tempNumRefs = fNumRefs;
			fRefs[fNumRefs++]= fileNum;
			//Add index flags
			fRefsIndexFlags[tempNumRefs]=indexFlags;
			return 0;
		} 

		// For rt.jar, 73265 word entries are created. 51997 have 1 ref, then 9438, 3738, 1980, 1214, 779, 547, 429, 371 etc.
		int newSize= fNumRefs < 4 ? 4 : fNumRefs * 2; // so will start @ 1, grow to 4, 8, 16, 32, 64 etc.
		System.arraycopy(fRefs, 0, fRefs= new int[newSize], 0, fNumRefs);
		// Resize the index flags array at this time as well
		System.arraycopy(fRefsIndexFlags,0,fRefsIndexFlags=new int[newSize],0,fNumRefs);
		
		int tempNumRefs=fNumRefs;
		fRefs[fNumRefs++]= fileNum;
		fRefsIndexFlags[tempNumRefs]=indexFlags;
		
		return (newSize - fNumRefs + 1) * 4;
	}
	/**
	 * Adds a set of references and records the change in footprint.
	 */
	public void addRefs(int[] refs, int[] indexRefs) {
		int[] newRefs= new int[fNumRefs + refs.length];
		int[] newIndexRefs = new int[fNumRefs + indexRefs.length];
		int pos1= 0;
		int pos2= 0;
		int posNew= 0;
		int compare;
		int r1= 0;
		int r2= 0;
		int i1=0;
		int i2=0;
		while (pos1 < fNumRefs || pos2 < refs.length) {
			if (pos1 >= fNumRefs) {
				r2= refs[pos2];
				i2= indexRefs[pos2];
				compare= -1;
			} else if (pos2 >= refs.length) {
				compare= 1;
				r1= fRefs[pos1];
				i1= fRefsIndexFlags[pos1];
			} else {
				r1= fRefs[pos1];
				r2= refs[pos2];
				
				i1=fRefsIndexFlags[pos1];
				i2=indexRefs[pos2];
				compare= r2 - r1;
			}
			if (compare > 0) {
				newRefs[posNew]= r1;
				newIndexRefs[posNew]=i1;
				posNew++;
				pos1++;
			} else {
				if (r2 != 0) {
					newRefs[posNew]= r2;
					newIndexRefs[posNew]=i2;
					posNew++;
				}
				pos2++;
			}
		}
		fRefs= newRefs;
		fNumRefs= posNew;
		fRefsIndexFlags=newIndexRefs;
	}
	/**
	 * Returns the size of the wordEntry
	 */
	public int footprint() {
		//Size of Object + (number of fields * size of Fields) + (Size of ArrayObject + (Number of chars * sizeof Chars)) + 
		//(Size of ArrayObject + (Number of refs * sizeof int)) + (Size of ArrayObject + (Number of indexRefs * sizeof int)) 
		return 8 + (4 * 4) + (8 + fWord.length * 2) + (8 + fRefs.length * 4) + (8 + fRefsIndexFlags.length * 4);
	}
	/**
	 * Returns the number of references, e.g. the number of files this word appears in.
	 */
	public int getNumRefs() {
		return fNumRefs;
	}
	/**
	 * returns the file number in the i position in the list of references.
	 */
	public int getRef(int i) {
		if (i < fNumRefs) return fRefs[i];
		throw new IndexOutOfBoundsException();
	}
	/**
	 * returns the index bit field in the i position
	 */
	public int getIndexFlag(int i) {
		if (i < fNumRefs) return fRefsIndexFlags[i];
		throw new IndexOutOfBoundsException();
	}
	/**
	 * Returns the references of the wordEntry (the number of the files it appears in).
	 */
	public int[] getRefs() {
		int[] result= new int[fNumRefs];
		System.arraycopy(fRefs, 0, result, 0, fNumRefs);
		return result;
	}
	/**
	 * Returns the wordEntry's references index flags
	 */
	public int[] getRefsIndexFlags() {
		int[] result= new int[fNumRefs];
		System.arraycopy(fRefsIndexFlags, 0, result, 0, fNumRefs);
		return result;
	}
	/**
	 * returns the word of the wordEntry.
	 */
	public char[] getWord() {
		return fWord;
	}
	/**
	 * Changes the references of the wordEntry to match the mapping. For example,<br>
	 * if the current references are [1 3 4]<br>
	 * and mapping is [1 2 3 4 5]<br>
	 * in references 1 becomes mapping[1] = 2, 3->4, and 4->5<br>
	 * => references = [2 4 5].<br>
	 */
	public void mapRefs(int[] mappings) {
		int position= 0;
		int position2= 0;
		
		for (int i= 0; i < fNumRefs; i++) {
			//Take care that the reference is actually within the bounds of the mapping
			int map= -1;
			int map2= -1;
			
			if(fRefs[i] >= 0 && fRefs[i] < mappings.length) 
				map= mappings[fRefs[i]];
			if (map != -1 && map != 0)
				fRefs[position++]= map;
			
			if (fRefsIndexFlags[i] >= 0 && fRefsIndexFlags[i] < mappings.length)
				map2 = mappings[fRefsIndexFlags[i]];
			if (map2 != -1 && map2 != 0)
				fRefsIndexFlags[position2++] = map2;
			
		}
		fNumRefs= position;

		//to be changed!
		System.arraycopy(fRefs, 0, (fRefs= new int[fNumRefs]), 0, fNumRefs);
		System.arraycopy(fRefsIndexFlags, 0, (fRefsIndexFlags = new int[fNumRefs]),0,fNumRefs);
		
		Util.sort(fRefs, fRefsIndexFlags);
	}
	/**
	 * Clears the wordEntry.
	 */
	public void reset(char[] word) {
		for (int i= fNumRefs; i-- > 0;) {
			fRefs[i]= 0;
			fRefsIndexFlags[i]=0;
		}
		fNumRefs= 0;
		fWord= word;
	}
	public String toString() {
		return new String(fWord);
	}
	/**
	 * @param word
	 */
	public void catRefs(WordEntry word) {
		int[] wordFileRefs = word.fRefs;
		int[] wordIndexFlags=word.fRefsIndexFlags;
		ObjectSet set = new ObjectSet(4);
		
 		for (int i=0; i<wordFileRefs.length; i++){
			//if (wordIndexFlags[i] != 0)
			//	set.put(new Integer(wordIndexFlags[i]));
 			if (wordFileRefs[i] != 0)
 				set.put(new Integer(wordFileRefs[i]));
		}
		
 		int[] mergedArray = new int[set.size()];
 		for (int i=0; i<set.size(); i++){
 			mergedArray[i] = ((Integer) set.keyAt(i)).intValue();
 		}
 		
 		System.arraycopy(mergedArray,0,(fRefs = new int[set.size()]),0,set.size());
 		fNumRefs = set.size();
 		
 		Util.sort(fRefs);
	}
}

