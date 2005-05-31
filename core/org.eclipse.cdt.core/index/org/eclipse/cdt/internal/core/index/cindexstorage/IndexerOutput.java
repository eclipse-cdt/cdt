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
	
	protected void addRef(int indexedFileNumber, char [][] name, char suffix, int type, int offset, int offsetLength, int offsetType) {
		if (indexedFileNumber == 0) {
			throw new IllegalStateException();
		}
		
		if (offsetLength <= 0)
			offsetLength = 1;
		
		index.addRef(
				encodeTypeEntry(name, suffix, type),
                indexedFileNumber, offset, offsetLength, offsetType);
	}  
	
	protected void addRef(int indexedFileNumber, char[][] name, int meta_kind, int ref, int offset, int offsetLength, int offsetType) {
		if (indexedFileNumber == 0) {
			throw new IllegalStateException();
		}
		
		if (offsetLength <= 0)
			offsetLength = 1;
		
		index.addRef(
				encodeEntry(name, meta_kind, ref), 
                indexedFileNumber, offset, offsetLength, offsetType);
		
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
	    addRef(indexedFileNumber,  name, IIndex.INCLUDE, IIndex.REFERENCE, offset,offsetLength, offsetType);
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

	public void addIndexEntry(IIndexEntry indexEntry) {
		
		if (indexEntry == null)
			return;
		
	  if (indexEntry instanceof ITypeEntry){
			ITypeEntry typeEntry = (ITypeEntry) indexEntry;
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
			
			addRef(indexedFileNumber,  name, ICIndexStorageConstants.typeConstants[type_kind], entryType, nameOffset,nameOffsetLength, nameOffsetType);
			
		} else if (indexEntry instanceof IFunctionEntry) {
			IFunctionEntry functionEntry = (IFunctionEntry) indexEntry;
			int indexedFileNumber=functionEntry.getFileNumber();
			int meta_type = functionEntry.getMetaKind();
			int entryType = functionEntry.getEntryType();
			int modifiers = functionEntry.getModifiers();
			char[][] sig=functionEntry.getSignature();
			char[][]name=functionEntry.getFullName();
			int nameOffset=functionEntry.getNameOffset();
			int nameOffsetLength=functionEntry.getNameLength();
			int nameOffsetType=functionEntry.getNameOffsetType();
			addRef(indexedFileNumber, name, meta_type, entryType, nameOffset,nameOffsetLength, nameOffsetType);
		} 	
		else if (indexEntry instanceof INamedEntry){
			INamedEntry nameEntry = (INamedEntry) indexEntry;
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
			addRef(indexedFileNumber,  name, meta_type, entryType, nameOffset,nameOffsetLength, nameOffsetType);
		}
	}

}
