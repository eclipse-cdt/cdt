/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
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

public class WordEntry {
   
    //Prefix used for index encoding as defined in ICIndexStorageConstants
    private int encodings; 
    //Second part of encoding string as definined in ICIndexStorageConstants
    private int encodingType;
    //Used for type encodings only; defined in ICIndexStorageConstants
    private int typeConstant;
    
    //Fully qualified name for ref 
	private char[] word;
	
	//Number of file references for this word entry
	private int fileRefCount;
	//File reference id's
	private int[] fileRefs;
	
	//Offset arrays - each fileRef's position in the fileRef array is the
	//key into the offsets
	//Offsets are prefixed with LINE or OFFSET designation
	private int[][] offsets;
	//Number of offsets in each offset array
	private int[] offsetCount;
	
	public WordEntry() {
		this(CharOperation.NO_CHAR);
	}
	public WordEntry(char[] word) {
		this.word= word;
		fileRefCount= 0;
		fileRefs= new int[1];
		offsets = new int [1][1];
		offsetCount = new int[1];
	}	
	/**
	 * Adds a reference and records the change in footprint.
	 * @return returns the size increase for this instance
	 */
	public int addRef(int fileNum) {
	    //Ensure that this is a unique fileNum that hasn't been added
	    //already to the refs
		if (fileRefCount > 0 && fileAlreadyAdded(fileNum)) {
			return 0;
		}
		//Ensure that there is still space to add this ref - if so,
		//add the reference
		if (fileRefCount < fileRefs.length) {
			fileRefs[fileRefCount++]= fileNum;
			return 0;
		} 
		//Need to grow arrays - arrays will start at 1, grow to 4, 8, 16, 32, 64 etc.
		int newSize= fileRefCount < 4 ? 4 : fileRefCount * 2;
		//Grow the fileRefs array
		System.arraycopy(fileRefs, 0, fileRefs= new int[newSize], 0, fileRefCount);
		//Grow the offset array
		System.arraycopy(offsets, 0, offsets= new int[newSize][1], 0, fileRefCount);
		//Grow the offset count array
		System.arraycopy(offsetCount, 0, offsetCount= new int[newSize], 0, fileRefCount);
		//Add the new file reference
		fileRefs[fileRefCount++]= fileNum;
		return (newSize - fileRefCount + 1) * 4;
	}
    /**
     * Checks to see if this file number has already been added
     */
    private boolean fileAlreadyAdded(int fileNum) {
        for (int i=0; i<fileRefCount; i++){
          if (fileRefs[i] == fileNum)
              return true;
        }
        return false;
    }
    /**
	 * Adds a set of references and records the change in footprint.
     * @param passedOffsetCount
     * @param offsets
	 */
	public void addWordInfo(int[] refs, int[][] passedOffsets, int[] passedOffsetCount) {
		int[] newRefs= new int[fileRefCount + refs.length];
		int[][]  newOffsets = new int[fileRefCount + refs.length][];
		int[] newOffSetCount= new int[fileRefCount + refs.length];
		
		int pos1= 0;
		int pos2= 0;
		int posNew= 0;
		int compare;
		int r1= 0;
		int r2= 0;
		int i1=0;
		int i2=0;
		while (pos1 < fileRefCount || pos2 < refs.length) {
			if (pos1 >= fileRefCount) {
				r2= refs[pos2];
				compare= -1;
			} else if (pos2 >= refs.length) {
				compare= 1;
				r1= fileRefs[pos1];
			} else {
				r1= fileRefs[pos1];
				r2= refs[pos2];
				compare= r2 - r1;
			}
			if (compare > 0) {
				newRefs[posNew]= r1;
				newOffsets[posNew]= offsets[pos1];
				newOffSetCount[posNew]=offsetCount[pos1];
				posNew++;
				pos1++;
			} else {
				if (r2 != 0) {
					newRefs[posNew]= r2;
					newOffsets[posNew]=passedOffsets[pos2];
					newOffSetCount[posNew]=passedOffsetCount[pos2];
					posNew++;
				}
				pos2++;
			}
		}
		fileRefs= newRefs;
		offsets= newOffsets;
		offsetCount=newOffSetCount;
		fileRefCount= posNew;
	}
	
	/**
	 * Adds a reference and records the change in footprint.
	 * @return -1 if fileNumber not found, 0 if adding offset didn't result in 
	 * change of object size, new size of object if adding offset forced the expansion
	 * of the underlying array
	 */
	public int addOffset(int offset, int fileNum, int offsetType) {
	    //Get the position in the fileRefs array for this file number - the 
	    //position acts as an index into the offsets array
	    int filePosition = getPositionForFile(fileNum);
	    //File Number wasn't found
        if (filePosition == -1)
            return -1;
        //Get the array containing the offsets for this file
        int[] selectedOffsets = offsets[filePosition];
        //Get the offset count for this file
        int selectedOffsetCount = offsetCount[filePosition];
        
        //Encode the number with line/offset info
        int encodedNumber = getEncodedNumber(offsetType, offset);
        
        //Check to see if this offset has already been added to the file
        if (selectedOffsetCount > 0 && offsetAlreadyAdded(filePosition, encodedNumber)){
            return 0;
        }
        
    
        //If there is still space in the array, add the encoded offset, update 
        //the count
		if (selectedOffsetCount < selectedOffsets.length) {
		    selectedOffsets[selectedOffsetCount++]= encodedNumber;
		    offsetCount[filePosition] = selectedOffsetCount;
			return 0;
		} 
	
		//Grow the offset array - start @ 1, grow to 4, 8, 16, 32, 64 etc.
		int newSize= selectedOffsetCount < 4 ? 4 : selectedOffsetCount * 2; 
		System.arraycopy(selectedOffsets, 0, selectedOffsets= new int[newSize], 0, selectedOffsetCount);
	
		//Add the encoded offset to the newly grown array, update the count
		selectedOffsets[selectedOffsetCount++]= encodedNumber;
		offsetCount[filePosition] = selectedOffsetCount;
		//Put the newly grown array back in place
		offsets[filePosition]=selectedOffsets;
		
		return (newSize - fileRefCount + 1) * 4;
	}
	
	/**
     * @param offsetType
     * @param offset
     * @return
     */
    private int getEncodedNumber(int offsetType, int offset) {
        String offsetString = Integer.toString(offsetType) + Integer.toString(offset);
        return Integer.parseInt(offsetString);
    }
    /**
     * @param filePosition
     * @return
     */
    private boolean offsetAlreadyAdded(int filePosition, int offset) {
        int[] tempOffset = offsets[filePosition];
        for (int i=0; i<tempOffset.length; i++){
            if (tempOffset[i] == offset)
                return true;
        }
        return false;
    }
    /**
     * @param fileNum
     * @return
     */
    private int getPositionForFile(int fileNum) {
        for (int i=0; i<fileRefCount; i++){
            if (fileRefs[i] == fileNum)
                return i;
        }
        return -1;
    }
    /**
	 * Returns the size of the wordEntry
	 */
	public int footprint() {
		//Size of Object + (number of fields * size of Fields) + (Size of ArrayObject + (Number of chars * sizeof Chars)) + 
		//(Size of ArrayObject + (Number of refs * sizeof int)) 
		return 8 + (4 * 4) + (8 + word.length * 2) + (8 + fileRefs.length * 4);
	}
	/**
	 * Returns the number of references, e.g. the number of files this word appears in.
	 */
	public int getNumRefs() {
		return fileRefCount;
	}
	/**
	 * returns the file number in the i position in the list of references.
	 */
	public int getRef(int i) {
		if (i < fileRefCount) return fileRefs[i];
		throw new IndexOutOfBoundsException();
	}
	/**
	 * Returns the references of the wordEntry (the number of the files it appears in).
	 */
	public int[] getRefs() {
		int[] result= new int[fileRefCount];
		System.arraycopy(fileRefs, 0, result, 0, fileRefCount);
		return result;
	}
	/**
	 * Returns the offsets of the wordEntry 
	 */
	public int[][] getOffsets() {
		int[][] result= new int[fileRefCount][];
		for (int i=0; i<fileRefCount; i++){
		   int offsetLength =offsetCount[i];
		   int[] tempOffset = new int[offsetLength];
		   System.arraycopy(offsets[i], 0, tempOffset, 0, offsetLength);
		   result[i]=tempOffset;
		}
		return result;
	}
	/**
	 * returns offset count array
	 */
	public int[] getOffsetCount(){
		int[] result= new int[fileRefCount];
		System.arraycopy(offsetCount, 0, result, 0, fileRefCount);
		return result;
	}
	/**
	 * returns the word of the wordEntry.
	 */
	public char[] getWord() {
		return word;
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
	
		for (int i= 0; i < fileRefCount; i++) {
			//Take care that the reference is actually within the bounds of the mapping
			int map= -1;
	
			if(fileRefs[i] >= 0 && fileRefs[i] < mappings.length) 
				map= mappings[fileRefs[i]];
			if (map != -1 && map != 0)
				fileRefs[position++]= map;
		}
		fileRefCount= position;

		//Trim all arrays of excess flab
		System.arraycopy(fileRefs, 0, (fileRefs= new int[fileRefCount]), 0, fileRefCount);
		System.arraycopy(offsets, 0, (offsets = new int[fileRefCount][]), 0,fileRefCount);
		System.arraycopy(offsetCount, 0,(offsetCount=new int[fileRefCount]),0,fileRefCount);
		
		//Store original ref positions in order to generate map
		int[] originalRefs;
		System.arraycopy(fileRefs, 0, (originalRefs = new int[fileRefCount]),0,fileRefCount);
		//Sort file refs
		Util.sort(fileRefs);
		
		 //Sort the original file refs
	     int[] mapping = new int[fileRefs.length];
	     figureOutMapping(originalRefs, mapping);
	     mapOffsets(mapping);
	}
	
	/**
     * @param mapping
     */
    private void mapOffsets(int[] mapping) {
        int fileRefLength = fileRefs.length;
        int[][] tempOffsetsArray = new int[fileRefLength][];
        int[] tempOffsetLengthArray = new int[fileRefLength];
        
        for (int i=0; i<mapping.length; i++){
            int moveTo = mapping[i];
            tempOffsetsArray[moveTo] = offsets[i];
            tempOffsetLengthArray [moveTo] = offsetCount[i];
        }
        
        System.arraycopy(tempOffsetsArray, 0, offsets,0, fileRefLength);
        System.arraycopy(tempOffsetLengthArray, 0, offsetCount,0, fileRefLength);
    }
    private void figureOutMapping(int[] originalRefs, int[] mapping){
	    int position = 0;
        for (int i=0; i<originalRefs.length; i++){
            int currentRef = originalRefs[i];
            for (int j=0; j<fileRefs.length; j++){
                if (currentRef == fileRefs[j]){
                    mapping[position++] = j;
                    break;
                }
            }
        }
	}
	
	/**
	 * Clears the wordEntry.
	 */
	public void reset(char[] word) {
		for (int i= fileRefCount; i-- > 0;) {
			fileRefs[i]= 0;
		}
		fileRefCount= 0;
		this.word= word;
	}
	public String toString() {
		return new String(word);
	}
    /**
     * Returns the sorted offset entries for the given index
     * @return
     */
    public int[] getOffsets(int index) {
        int[] tempOffset = offsets[index];
        int offsetLength = offsetCount[index];
      
        int[] result= new int[offsetLength];
		System.arraycopy(tempOffset, 0, result, 0, offsetLength);
		Util.sort(result);
		return result;
    }
    /**
     * @param n
     * @param tempOffsetArray
     */
    public void setOffsets(int index, int[] tempOffsetArray) {
        int[] selectedOffsets = offsets[index];
        int tempOffsetArrayLength = tempOffsetArray.length;
        
        //Grow the offset array - start @ 1, grow to 4, 8, 16, 32, 64 etc.
		int newSize= tempOffsetArrayLength < 4 ? 4 : tempOffsetArrayLength * 2; 
		System.arraycopy(tempOffsetArray, 0, selectedOffsets= new int[newSize], 0, tempOffsetArrayLength);
		offsetCount[index] = tempOffsetArrayLength;
		//Put the newly grown array back in place
		offsets[index]=selectedOffsets;
    }
}

