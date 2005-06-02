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

import org.eclipse.cdt.internal.core.index.IFunctionEntry;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IIndexEntry;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;
import org.eclipse.cdt.internal.core.index.INamedEntry;
import org.eclipse.cdt.internal.core.index.ITypeEntry;
import org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer;

/**
 * An indexerOutput is used by an indexer to add files and word references to
 * an inMemoryIndex. 
 */

public class IndexerOutput implements ICIndexStorageConstants, IIndexerOutput { 
	protected InMemoryIndex index;
	/**
	 * IndexerOutput constructor comment.
	 */
	public IndexerOutput(InMemoryIndex index) {
		this.index= index;
	}
	
	protected void addRef(int indexedFileNumber, char [][] name, char suffix, int type, int offset, int offsetLength, int offsetType, int modifiers) {
		if (indexedFileNumber == 0) {
			throw new IllegalStateException();
		}
		
		if (offsetLength <= 0)
			offsetLength = 1;
		
		if (modifiers <=0)
			modifiers = 1;
		
		index.addRef(
				encodeTypeEntry(name, suffix, type),
                indexedFileNumber, offset, offsetLength, offsetType, modifiers);
	}  
	
	protected void addRef(int indexedFileNumber, char[][] name, int meta_kind, int ref, int offset, int offsetLength, int offsetType, int modifiers) {
		if (indexedFileNumber == 0) {
			throw new IllegalStateException();
		}
		
		if (offsetLength <= 0)
			offsetLength = 1;
		
		if (modifiers <=0)
			modifiers = 1;
		
		index.addRef(
				encodeEntry(name, meta_kind, ref), 
                indexedFileNumber, offset, offsetLength, offsetType, modifiers);
		
	}
	
	public void addRelatives(int indexedFileNumber, String inclusion, String parent) {
		if (indexedFileNumber == 0) {
			throw new IllegalStateException();
		}
		index.addRelatives(indexedFileNumber, inclusion, parent);	
	}

	public void addIncludeRef(int indexedFileNumber, char[] word) {
		if (indexedFileNumber == 0) {
			throw new IllegalStateException();
		}
			index.addIncludeRef(word, indexedFileNumber);	
	}

	public void addIncludeRef(int indexedFileNumber, String word) {
		addIncludeRef(indexedFileNumber, word.toCharArray());
	}
	
	public IndexedFileEntry getIndexedFile(String path) {
		return index.getIndexedFile(path);
	}
	
	/**
	 * Adds the file path to the index, creating a new file entry
	 * for it
	 */
	public IndexedFileEntry addIndexedFile(String path) {
		return index.addFile(path);
	}

	public void addIncludeRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.INCLUDE, IIndex.REFERENCE, offset,offsetLength, offsetType,1);
	}

	/**
	 * Type entries are encoded as follow: 'typeDecl/' ('C' | 'S' | 'U' | 'E' ) '/'  TypeName ['/' Qualifier]* 
	 */
	protected static final char[] encodeTypeEntry(char[][] fullTypeName, char suffix, int type) { 
	
	    int pos = 0, nameLength = 0;
	    for (int i=0; i<fullTypeName.length; i++){
	        char[] namePart = fullTypeName[i];
	        nameLength+= namePart.length;
	    }
	    
	    char [] result = null;
	    char [] declchar = encodingTypes[type];
	    char [] typechar = encodings[IIndex.TYPE];
	    
	    //char[] has to be of size - [typechar length + length of the name + separators + letter]
        result = new char[typechar.length + declchar.length + nameLength + fullTypeName.length + 1 ];
        System.arraycopy(typechar, 0, result, 0, typechar.length);
        System.arraycopy(declchar, 0, result, typechar.length, declchar.length);
        pos = typechar.length + declchar.length;
        
//	    if( type == IIndex.REFERENCE ){
//	        //char[] has to be of size - [type decl length + length of the name + separators + letter]
//	        result = new char[TYPE_REF.length + nameLength + fullTypeName.length + 1 ];
//	        System.arraycopy(TYPE_REF, 0, result, 0, pos = TYPE_REF.length);
//	    
//	    } else {
//	        //char[] has to be of size - [type decl length + length of the name + separators + letter]
//	        result = new char[TYPE_DECL.length + nameLength + fullTypeName.length + 1 ];
//	        System.arraycopy(TYPE_DECL, 0, result, 0, pos = TYPE_DECL.length);
//	    }
	    result[pos++] = suffix;
	    result[pos++] = SEPARATOR;
	    
	    //Encode in the following manner
	    //  [typeDecl info]/[typeName]/[qualifiers]
	    if (fullTypeName.length > 0){
	    //Extract the name first
	        char [] tempName = fullTypeName[fullTypeName.length-1];
	        System.arraycopy(tempName, 0, result, pos, tempName.length);
	        pos += tempName.length;
	    }
	    //Extract the qualifiers
	    for (int i=fullTypeName.length - 2; i >= 0; i--){
	        result[pos++] = SEPARATOR;
	        char [] tempName = fullTypeName[i];
	        System.arraycopy(tempName, 0, result, pos, tempName.length);
	        pos+=tempName.length;               
	    }
	    
	    if (AbstractIndexer.VERBOSE)
	        AbstractIndexer.verbose(new String(result));
	        
	    return result;
	}
	/**
	  * Namespace entries are encoded as follow: '[prefix]/' TypeName ['/' Qualifier]*
	  */
	protected static final char[] encodeEntry(char[][] elementName, int meta_kind, int ref) { 
	     int pos, nameLength = 0;
	     for (int i=0; i<elementName.length; i++){
	         char[] namePart = elementName[i];
	         nameLength+= namePart.length;
	     }
	     //char[] has to be of size - [type length + length of the name (including qualifiers) + 
	     //separators (need one less than fully qualified name length)
	     pos = encodings[meta_kind].length + encodingTypes[ref].length;
	     char[] result = new char[pos + nameLength + elementName.length - 1 ];
	     System.arraycopy(encodings[meta_kind], 0, result, 0, encodings[meta_kind].length);
	     System.arraycopy(encodingTypes[ref], 0, result, encodings[meta_kind].length, encodingTypes[ref].length);
	     if (elementName.length > 0){
	     //Extract the name first
	         char [] tempName = elementName[elementName.length-1];
	         System.arraycopy(tempName, 0, result, pos, tempName.length);
	         pos += tempName.length;
	     }
	     //Extract the qualifiers
	     for (int i=elementName.length - 2; i>=0; i--){
	         result[pos++] = SEPARATOR;
	         char [] tempName = elementName[i];
	         System.arraycopy(tempName, 0, result, pos, tempName.length);
	         pos+=tempName.length;               
	     }
	     
	     if (AbstractIndexer.VERBOSE)
	         AbstractIndexer.verbose(new String(result));
	         
	     return result;
	 }

	public void addIndexEntry(IIndexEntry indexEntry) throws IndexEntryNotSupportedException {
		
		if (indexEntry == null)
			return;
		
		throw new IndexEntryNotSupportedException("Index Entry type not supported - need to add handler"); //$NON-NLS-1$
	}

	public void addIndexEntry(ITypeEntry typeEntry) {
		int indexedFileNumber=typeEntry.getFileNumber();
		int meta_type = typeEntry.getMetaKind();
		int type_kind = typeEntry.getTypeKind();
		int entryType = typeEntry.getEntryType();
		int modifiers = typeEntry.getModifiers();
		
		char[][]name=typeEntry.getFullName();
		
		int nameOffset=typeEntry.getNameOffset();
		int nameOffsetLength=typeEntry.getNameLength();
		int nameOffsetType=typeEntry.getNameOffsetType();
		
		int elementOffset=typeEntry.getElementOffset();
		int elementOffsetLength=typeEntry.getElementLength();
		int elementOffsetType=typeEntry.getElementOffsetType();
		
		if (modifiers <= 0)
			modifiers = 1;
		
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.typeConstants[type_kind], entryType, nameOffset,nameOffsetLength, nameOffsetType, modifiers);
		
		IIndexEntry[] baseClasses = typeEntry.getBaseTypes();
		if (baseClasses != null &&
			baseClasses.length > 0){
			for (int i=0; i<baseClasses.length; i++){
				char[][] baseName= ((INamedEntry) baseClasses[i]).getFullName();
				addRef(indexedFileNumber, baseName, ICIndexStorageConstants.DERIVED_SUFFIX, IIndex.DECLARATION, nameOffset,nameOffsetLength, nameOffsetType, 1);
			}
		}
	}

	public void addIndexEntry(INamedEntry nameEntry) {
		int indexedFileNumber=nameEntry.getFileNumber();
		int meta_type = nameEntry.getMetaKind();
		int entryType = nameEntry.getEntryType();
		int modifiers = nameEntry.getModifiers();
		char[][]name=nameEntry.getFullName();
		int nameOffset=nameEntry.getNameOffset();
		int nameOffsetLength=nameEntry.getNameLength();
		int nameOffsetType=nameEntry.getNameOffsetType();
		int elementOffset=nameEntry.getElementOffset();
		int elementOffsetLength=nameEntry.getElementLength();
		int elementOffsetType=nameEntry.getElementOffsetType();
		
		if (modifiers <= 0)
			modifiers = 1;
		
		addRef(indexedFileNumber,  name, meta_type, entryType, nameOffset,nameOffsetLength, nameOffsetType, modifiers);
	}

	public void addIndexEntry(IFunctionEntry functionEntry) {
		int indexedFileNumber=functionEntry.getFileNumber();
		int meta_type = functionEntry.getMetaKind();
		int entryType = functionEntry.getEntryType();
		int modifiers = functionEntry.getModifiers();
		char[][] sig=functionEntry.getSignature();
		char[][]name=functionEntry.getFullName();
		char[]returnName=functionEntry.getReturnType();
		int sigL=0, nameL=0, returnNameL=0;
		
		int totalSize=0;
		
		//Get the size of the signature
		if (sig != null){
			sigL=sig.length;
			totalSize+=sigL + 2;
		}
		//Get the size of the name
		if (name != null ){
			nameL=name.length;
			totalSize+=nameL;
		}	
		//If return type is included it will be only 1 element
		if (returnName != null ){
			returnNameL=1;
			totalSize+=returnNameL + 2;
		}
		char[][] finalName = new char[totalSize][];

		int positionCounter=0;
		if (sig != null){
			char[][] startParm= new char[1][];
			char[] tempParm = {'('};
			startParm[0] = tempParm;
			
			char[][] endParm= new char[1][];
			char[] tempParm2 = {')'};
			endParm[0] = tempParm2;
			
			//Copy the signature delimiter in the final array starting where the name left off, length 1
			System.arraycopy(startParm, 0, finalName,positionCounter, 1);
			positionCounter+=1;
			//Copy the signature to the final array starting where the name left off + 1 for the delimiter, length signature length
			System.arraycopy(sig, 0, finalName, positionCounter, sigL);
			positionCounter+=sigL;
			//Copy the signature delimiter in the final array starting where the name left off, length 1
			System.arraycopy(endParm, 0, finalName, positionCounter, 1);
			positionCounter+=1;
		}
		
		if (returnName != null){
			char[][] startParm= new char[1][];
			String tempParm = "R("; //$NON-NLS-1$
			startParm[0] = tempParm.toCharArray();
			
			char[][] endParm= new char[1][];
			String tempParm2 = ")R"; //$NON-NLS-1$
			endParm[0] = tempParm2.toCharArray();
			
			char[][] tempReturn = new char[1][];
			tempReturn[0] = returnName;
			
			//Copy the signature delimiter in the final array starting where the name left off, length 1
			System.arraycopy(startParm, 0, finalName,positionCounter, 1);
			positionCounter+=1;
			//Copy the signature to the final array starting where the name left off + 1 for the delimiter, length signature length
			System.arraycopy(tempReturn, 0, finalName, positionCounter, returnNameL);
			positionCounter+=returnNameL;
			//Copy the signature delimiter in the final array starting where the name left off, length 1
			System.arraycopy(endParm, 0, finalName, positionCounter, 1);
			positionCounter+=1;
		}
		
		//copy name to first part of the array
		if (name != null)
			System.arraycopy(name, 0, finalName,positionCounter, nameL);
		
		int nameOffset=functionEntry.getNameOffset();
		int nameOffsetLength=functionEntry.getNameLength();
		int nameOffsetType=functionEntry.getNameOffsetType();
		
		if (modifiers <= 0)
			modifiers = 1;
		
		addRef(indexedFileNumber, finalName, meta_type, entryType, nameOffset,nameOffsetLength, nameOffsetType, modifiers);
	}

}
