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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.sourcedependency.Node;

/**
 * @author bgheorgh
 */
public class IncludeEntry {

  protected char[] fFile;
  protected int fId;
  protected int fNumRefs;
  protected int[] fRefs;
  //TODO: BOG Consider making these arrays...
  protected ArrayList fParent;
  protected ArrayList fChild;
  protected int fNumParent;
  protected int fNumChild;
     
  public IncludeEntry(int id) {
	this(CharOperation.NO_CHAR,id);
  }
		
  public IncludeEntry(char[] file, int id) {
	fFile = file;
	fNumRefs= 0;
	fRefs= new int[1];
	fId=id;
	
	fParent = new ArrayList(5);
	fChild = new ArrayList(5);
	fNumParent = 0;
	fNumChild = 0;
  }
  /**
   * Adds a reference and records the change in footprint.
   */
    public int addRef(int fileNum) {
      if (fNumRefs > 0 && fRefs[fNumRefs - 1] == fileNum) {
	   return 0;
      }
      if (fNumRefs < fRefs.length) {
   	    fRefs[fNumRefs++]= fileNum;
	    return 0;
      } 

      int newSize= fNumRefs < 4 ? 4 : fNumRefs * 2; // so will start @ 1, grow to 4, 8, 16, 32, 64 etc.
	  System.arraycopy(fRefs, 0, fRefs= new int[newSize], 0, fNumRefs);
	  fRefs[fNumRefs++]= fileNum;
	  return (newSize - fNumRefs + 1) * 4;
    }
	  
	public void addParent(int fileRef, int parentId){
		Node newParent = new Node(fileRef,parentId);
		fParent.add(newParent);
		fNumParent++;
	}
	/**
	 * @param is
	 */
	public void addRefs(int[] refs) {
		int[] newRefs= new int[fNumRefs + refs.length];
		int pos1= 0;
		int pos2= 0;
		int posNew= 0;
		int compare;
		int r1= 0;
		int r2= 0;
		while (pos1 < fNumRefs || pos2 < refs.length) {
			if (pos1 >= fNumRefs) {
				r2= refs[pos2];
				compare= -1;
			} else if (pos2 >= refs.length) {
				compare= 1;
				r1= fRefs[pos1];
			} else {
				r1= fRefs[pos1];
				r2= refs[pos2];
				compare= r2 - r1;
			}
			if (compare > 0) {
				newRefs[posNew]= r1;
				posNew++;
				pos1++;
			} else {
				if (r2 != 0) {
					newRefs[posNew]= r2;
					posNew++;
				}
				pos2++;
			}
		}
		fRefs= newRefs;
		fNumRefs= posNew;
		/*for (int i = 0; i < refs.length; i++)
		addRef(refs[i]);
		int[] newRefs = new int[fNumRefs];
		System.arraycopy(fRefs, 0, newRefs, 0, fNumRefs);
		fRefs = newRefs;
		Util.sort(fRefs);*/
		
	}
	
	public void addChild(int fileRef, int parentId){
		Node newChild = new Node(fileRef,parentId);
		fChild.add(newChild);
		fNumChild++;
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
	 * Returns the references of the includeEntry (the number of the files it appears in).
	 */
	public int[] getRefs() {
		int[] result= new int[fNumRefs];
		System.arraycopy(fRefs, 0, result, 0, fNumRefs);
		return result;
	}
	/**
	 * returns the word of the includeEntry.
	 */
	public char[] getFile() {
		return fFile;
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

		for (int i= 0; i < fNumRefs; i++) {
			//Take care that the reference is actually within the bounds of the mapping
			int map= -1;
			if(fRefs[i] >= 0 && fRefs[i] < mappings.length) 
				map= mappings[fRefs[i]];
			if (map != -1 && map != 0)
				fRefs[position++]= map;
		}
		fNumRefs= position;

		//to be changed!
		System.arraycopy(fRefs, 0, (fRefs= new int[fNumRefs]), 0, fNumRefs);
		Util.sort(fRefs);
	}
	/**
	 * Clears the includeEntry.
	 */
	public void reset(char[] word) {
		for (int i= fNumRefs; i-- > 0;) {
			fRefs[i]= 0;
		}
		fNumRefs= 0;
		fFile= word;
	}
	
	public int getID(){
		return fId;
	}
	
	public String toString() {
		StringBuffer tempBuffer = new StringBuffer();
		tempBuffer.append("<Name: "); //$NON-NLS-1$
		tempBuffer.append(fFile);
		tempBuffer.append(", Id: "); //$NON-NLS-1$
		tempBuffer.append(fId);
		tempBuffer.append(", Refs:{"); //$NON-NLS-1$
		for (int i = 0; i < fRefs.length; i++){
			if (i > 0) tempBuffer.append(',');
			tempBuffer.append(' ');
			tempBuffer.append(fRefs[i]);
		}
		tempBuffer.append("}, Parents:{"); //$NON-NLS-1$
		Iterator x = fParent.iterator();
		while (x.hasNext())
		{
			Node tempNode = (Node) x.next();
			tempBuffer.append(tempNode.toString());
			if (x.hasNext()) {
				tempBuffer.append(',');
				tempBuffer.append(' ');
			}
		}
		tempBuffer.append("}, Children:{"); //$NON-NLS-1$
		Iterator y = fChild.iterator();
		while (y.hasNext())
		{
			Node tempNode = (Node) y.next();
			tempBuffer.append(tempNode.toString());
			if (y.hasNext()) {
				tempBuffer.append(',');
				tempBuffer.append(' ');
			}
		}
		tempBuffer.append("} >"); //$NON-NLS-1$
		return tempBuffer.toString();
	}
}
