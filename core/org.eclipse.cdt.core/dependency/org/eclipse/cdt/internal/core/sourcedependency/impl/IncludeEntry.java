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

package org.eclipse.cdt.internal.core.sourcedependency.impl;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.cdt.internal.core.CharOperation;

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
		tempBuffer.append("<Name: ");
		tempBuffer.append(fFile);
		tempBuffer.append(", Id: ");
		tempBuffer.append(fId);
		tempBuffer.append(", Refs:{");
		for (int i = 0; i < fRefs.length; i++){
			if (i > 0) tempBuffer.append(',');
			tempBuffer.append(' ');
			tempBuffer.append(fRefs[i]);
		}
		tempBuffer.append("}, Parents:{");
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
		tempBuffer.append("}, Children:{");
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
		tempBuffer.append("} >");
		return tempBuffer.toString();
	}
	
}
