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

import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;
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

	public void addEnumtorDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.ENUMTOR, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}
	
	public void addEnumtorRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.ENUMTOR, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}
	
	public void addMacroDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.MACRO, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}
	
	public void addMacroRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.MACRO, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}
	
	public void addFieldDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.FIELD, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}
	
	public void addFieldRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.FIELD, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}
	
	public void addMethodDecl(int indexedFileNumber, char[][] name, /*char[][] parameterTypes,*/ int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.METHOD, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}
	
	public void addMethodDefn(int indexedFileNumber, char[][] name, /*char[][] parameterTypes,*/ int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.METHOD, IIndex.DEFINITION, offset,offsetLength, offsetType);
	}
	
	public void addMethodRef(int indexedFileNumber, char[][] name, /*char[][] parameterTypes,*/int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.METHOD, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}
	
	public void addFunctionDecl(int indexedFileNumber, char[][] name,/*char[][] parameterTypes,*/ int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.FUNCTION, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}
	
	public void addFunctionDefn(int indexedFileNumber, char[][] name, /*char[][] parameterTypes,*/ int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.FUNCTION, IIndex.DEFINITION, offset,offsetLength, offsetType);
	}
	
	public void addFunctionRef(int indexedFileNumber, char[][] name, /*char[][] parameterTypes,*/int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.FUNCTION, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}
	
	public void addNamespaceDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.NAMESPACE, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}
	
	public void addNamespaceRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.NAMESPACE, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}
	
	public void addIncludeRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, IIndex.INCLUDE, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}

	public void addStructDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, STRUCT_SUFFIX, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}

	public void addStructRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, STRUCT_SUFFIX, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}

	public void addTypedefDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, TYPEDEF_SUFFIX, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}

	public void addTypedefRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, TYPEDEF_SUFFIX, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}

	public void addUnionDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, UNION_SUFFIX, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}

	public void addUnionRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, UNION_SUFFIX, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}

	public void addVariableDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, VAR_SUFFIX, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}

	public void addVariableRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, VAR_SUFFIX, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}
	
	public void addClassDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, CLASS_SUFFIX, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}

	public void addClassRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, CLASS_SUFFIX, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}
	
	public void addEnumDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ENUM_SUFFIX, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}

	public void addEnumRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ENUM_SUFFIX, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}
	
	public void addDerivedDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, DERIVED_SUFFIX, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}

	public void addDerivedRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, DERIVED_SUFFIX, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}
	
	public void addFriendDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, FRIEND_SUFFIX, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}

	public void addFriendRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, FRIEND_SUFFIX, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}
	
	public void addFwd_ClassDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, FWD_CLASS_SUFFIX, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}

	public void addFwd_ClassRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, FWD_CLASS_SUFFIX, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}
	
	public void addFwd_StructDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, FWD_STRUCT_SUFFIX, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}

	public void addFwd_StructRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, FWD_STRUCT_SUFFIX, IIndex.REFERENCE, offset,offsetLength, offsetType);
	}
	
	public void addFwd_UnionDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, FWD_UNION_SUFFIX, IIndex.DECLARATION, offset,offsetLength, offsetType);
	}

	public void addFwd_UnionRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, FWD_UNION_SUFFIX, IIndex.REFERENCE, offset,offsetLength, offsetType);
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
	
//	   public static final char[] encodeEntry(char[][] elementName, int entryType, int encodeType) {
//	        // Temporarily
//	        if (elementName == null) {
//	            return "NPE".toCharArray(); //$NON-NLS-1$
//	        }
//	        int pos, nameLength = 0;
//	        for (int i=0; i < elementName.length; i++){
//	            char[] namePart = elementName[i];
//	            nameLength += namePart.length;
//	        }
//	        char[][] encodedTypeNames = null;
//	        if (encodeType == IIndex.DECLARATION) {
//	            encodedTypeNames = IIndexEncodingConstants.encodedTypeNames_Decl;
//	        }
//	        else if (encodeType == IIndex.REFERENCE) {
//	            encodedTypeNames = IIndexEncodingConstants.encodedTypeNames_Ref;
//	        }
//	        char[] encodedTypeName = encodedTypeNames[entryType];
//	        
//	        //char[] has to be of size - [type length + length of the name (including qualifiers) + 
//	        //separators (need one less than fully qualified name length)
//	        char[] result = new char[encodedTypeName.length + nameLength + elementName.length - 1];
//	        System.arraycopy(encodedTypeName, 0, result, 0, pos = encodedTypeName.length);
//	        if (elementName.length > 0) {
//	        //Extract the name first
//	            char [] tempName = elementName[elementName.length-1];
//	            System.arraycopy(tempName, 0, result, pos, tempName.length);
//	            pos += tempName.length;
//	        }
//	        //Extract the qualifiers
//	        for (int i=elementName.length - 2; i>=0; i--){
//	            result[pos++] = SEPARATOR;
//	            char [] tempName = elementName[i];
//	            System.arraycopy(tempName, 0, result, pos, tempName.length);
//	            pos+=tempName.length;               
//	        }
//	        
//	        if (AbstractIndexer.VERBOSE)
//	            AbstractIndexer.verbose(new String(result));
//	            
//	        return result;
//	    }


}
