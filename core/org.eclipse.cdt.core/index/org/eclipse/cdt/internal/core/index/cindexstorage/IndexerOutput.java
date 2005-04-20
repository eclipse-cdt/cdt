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

import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;
import org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer;

/**
 * An indexerOutput is used by an indexer to add files and word references to
 * an inMemoryIndex. 
 */

public class IndexerOutput implements IIndexerOutput, ICIndexStorageConstants, ICSearchConstants {   
	
	static final public char CLASS_SUFFIX = 'C';		// CLASS
	static final public char DERIVED_SUFFIX = 'D';		// DERIVED
	static final public char ENUM_SUFFIX = 'E';			// ENUM 
	static final public char FRIEND_SUFFIX = 'F';		// FRIEND 
	static final public char FWD_CLASS_SUFFIX = 'G';	// FWD_CLASS
	static final public char FWD_STRUCT_SUFFIX = 'H';	// FWD_STRUCT
	static final public char FWD_UNION_SUFFIX = 'I';	// FWD_UNION
	static final public char STRUCT_SUFFIX = 'S';		// STRUCT
	static final public char TYPEDEF_SUFFIX = 'T';		// TYPEDEF 
	static final public char UNION_SUFFIX = 'U';		// UNION 
	static final public char VAR_SUFFIX = 'V';			// VAR
	
	final public static char SEPARATOR= '/';
	
	static public char[] TYPE_REF= "typeRef/".toCharArray(); //$NON-NLS-1$	
	static public char[] TYPE_DECL = "typeDecl/".toCharArray(); //$NON-NLS-1$
	static public char[] TYPE_ALL = "type".toCharArray(); //$NON-NLS-1$	
	static public char[] NAMESPACE_REF= "namespaceRef/".toCharArray(); //$NON-NLS-1$
	static public char[] NAMESPACE_DECL= "namespaceDecl/".toCharArray(); //$NON-NLS-1$	
	static public char[] NAMESPACE_ALL = "namespace".toCharArray(); //$NON-NLS-1$	
	static public char[] FIELD_REF= "fieldRef/".toCharArray(); //$NON-NLS-1$	
	static public char[] FIELD_DECL= "fieldDecl/".toCharArray(); //$NON-NLS-1$
	static public char[] FIELD_ALL= "field".toCharArray(); //$NON-NLS-1$	
	static public char[] ENUMTOR_REF= "enumtorRef/".toCharArray(); //$NON-NLS-1$	
	static public char[] ENUMTOR_DECL = "enumtorDecl/".toCharArray(); //$NON-NLS-1$
	static public char[] ENUMTOR_ALL = "enumtor".toCharArray(); //$NON-NLS-1$	
	static public char[] METHOD_REF= "methodRef/".toCharArray(); //$NON-NLS-1$	
	static public char[] METHOD_DECL= "methodDecl/".toCharArray(); //$NON-NLS-1$
	static public char[] METHOD_ALL= "method".toCharArray(); //$NON-NLS-1$		
	static public char[] MACRO_DECL = "macroDecl/".toCharArray(); //$NON-NLS-1$	
	static public char[] MACRO_REF = "macroRef/".toCharArray(); //$NON-NLS-1$	
	static public char[] INCLUDE_REF = "includeRef/".toCharArray(); //$NON-NLS-1$	
	static public char[] FUNCTION_REF= "functionRef/".toCharArray(); //$NON-NLS-1$	
	static public char[] FUNCTION_DECL= "functionDecl/".toCharArray(); //$NON-NLS-1$
	static public char[] FUNCTION_ALL= "function".toCharArray(); //$NON-NLS-1$	

	protected InMemoryIndex index;
	/**
	 * IndexerOutput constructor comment.
	 */
	public IndexerOutput(InMemoryIndex index) {
		this.index= index;
	}
	
	protected void addRef(int indexedFileNumber, char [][] name, int type, LimitTo limit, int offset, int offsetLength, int offsetType) {
		if (indexedFileNumber == 0) {
			throw new IllegalStateException();
		}
		
		if (offsetLength <= 0)
			offsetLength = 1;
		
		index.addRef(
				encodeTypeEntry(name, type, limit),
                indexedFileNumber, offset, offsetLength, offsetType);
	}  
	
	protected void addRef(int indexedFileNumber, char[][] name, char[] prefix, int prefixLength, int offset, int offsetLength, int offsetType) {
		if (indexedFileNumber == 0) {
			throw new IllegalStateException();
		}
		
		if (offsetLength <= 0)
			offsetLength = 1;
		
		index.addRef(
				encodeEntry(name, prefix, prefixLength), 
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

	 /**
     * Type entries are encoded as follow: 'typeDecl/' ('C' | 'S' | 'U' | 'E' ) '/'  TypeName ['/' Qualifier]* 
     */
     protected static final char[] encodeTypeEntry( char[][] fullTypeName, int typeType, LimitTo encodeType){ 

        int pos = 0, nameLength = 0;
        for (int i=0; i<fullTypeName.length; i++){
            char[] namePart = fullTypeName[i];
            nameLength+= namePart.length;
        }
        
        char [] result = null;
        if( encodeType == ICSearchConstants.REFERENCES ){
            //char[] has to be of size - [type decl length + length of the name + separators + letter]
            result = new char[TYPE_REF.length + nameLength + fullTypeName.length + 1 ];
            System.arraycopy(TYPE_REF, 0, result, 0, pos = TYPE_REF.length);
        
        } else if( encodeType == ICSearchConstants.DECLARATIONS ){
            //char[] has to be of size - [type decl length + length of the name + separators + letter]
            result = new char[TYPE_DECL.length + nameLength + fullTypeName.length + 1 ];
            System.arraycopy(TYPE_DECL, 0, result, 0, pos = TYPE_DECL.length);
        }
        switch (typeType)
        {
            case(TYPE_CLASS):
            result[pos++] = CLASS_SUFFIX;
            break;
            
            case(TYPE_STRUCT):
            result[pos++] = STRUCT_SUFFIX;
            break;
            
            case(TYPE_UNION):
            result[pos++] = UNION_SUFFIX;
            break;
            
            case(TYPE_ENUM):
            result[pos++] = ENUM_SUFFIX;
            break;
            
            case (TYPE_VAR):
            result[pos++] = VAR_SUFFIX;
            break;
            
            case (TYPE_TYPEDEF):
            result[pos++] = TYPEDEF_SUFFIX;
            break;
            
            case(TYPE_DERIVED):
            result[pos++]= DERIVED_SUFFIX;
            break;
            
            case(TYPE_FRIEND):
            result[pos++]= FRIEND_SUFFIX;
            break;
            
            case(TYPE_FWD_CLASS):
            result[pos++]= FWD_CLASS_SUFFIX;
            break;
            
            case (TYPE_FWD_STRUCT):
            result[pos++]= FWD_STRUCT_SUFFIX;
            break;
            
            case (TYPE_FWD_UNION):
            result[pos++]= FWD_UNION_SUFFIX;
            break;
        }
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
     protected static final char[] encodeEntry(char[][] elementName, char[] prefix, int prefixSize){ 
         int pos, nameLength = 0;
         for (int i=0; i<elementName.length; i++){
             char[] namePart = elementName[i];
             nameLength+= namePart.length;
         }
         //char[] has to be of size - [type length + length of the name (including qualifiers) + 
         //separators (need one less than fully qualified name length)
         char[] result = new char[prefixSize + nameLength + elementName.length - 1 ];
         System.arraycopy(prefix, 0, result, 0, pos = prefix.length);
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

 	public static final char[] bestTypePrefix( SearchFor searchFor, LimitTo limitTo, char[] typeName, char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == DECLARATIONS ){
			prefix = TYPE_DECL;
		} else if( limitTo == REFERENCES ){
			prefix = TYPE_REF;
		} else {
			return TYPE_ALL;
		}
					
		char classType = 0;
		
		if( searchFor == ICSearchConstants.CLASS ){
			classType = CLASS_SUFFIX;
		} else if ( searchFor == ICSearchConstants.STRUCT ){
			classType = STRUCT_SUFFIX;
		} else if ( searchFor == ICSearchConstants.UNION ){
			classType = UNION_SUFFIX;
		} else if ( searchFor == ICSearchConstants.ENUM ){
			classType = ENUM_SUFFIX;
		} else if ( searchFor == ICSearchConstants.TYPEDEF ){
			classType = TYPEDEF_SUFFIX;
		} else if ( searchFor == ICSearchConstants.DERIVED){
			classType = DERIVED_SUFFIX;
		} else if ( searchFor == ICSearchConstants.FRIEND){
			classType = FRIEND_SUFFIX;
		} else if ( searchFor == ICSearchConstants.FWD_CLASS) {
			classType = FWD_CLASS_SUFFIX;
		} else if ( searchFor == ICSearchConstants.FWD_STRUCT) {
			classType = FWD_STRUCT_SUFFIX;
		} else if ( searchFor == ICSearchConstants.FWD_UNION) {
			classType = FWD_UNION_SUFFIX;
		} else {
			//could be TYPE or CLASS_STRUCT, best we can do for these is the prefix
			return prefix;
		}
		
		return bestPrefix( prefix, classType, typeName, containingTypes, matchMode, isCaseSensitive );
	}
	
	public static final char[] bestNamespacePrefix(LimitTo limitTo, char[] namespaceName, char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = NAMESPACE_REF;
		} else if ( limitTo == DECLARATIONS ) {
			prefix = NAMESPACE_DECL;
		} else {
			return NAMESPACE_ALL;
		}
		
		return bestPrefix( prefix, (char) 0, namespaceName, containingTypes, matchMode, isCaseSensitive );
	}	
		
	public static final char[] bestVariablePrefix( LimitTo limitTo, char[] varName, char[][] containingTypes, int matchMode, boolean isCaseSenstive ){
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = TYPE_REF;
		} else if( limitTo == DECLARATIONS ){
			prefix = TYPE_DECL;
		} else {
			return TYPE_ALL;
		}
		
		return bestPrefix( prefix, VAR_SUFFIX, varName, containingTypes, matchMode, isCaseSenstive );	
	}

	public static final char[] bestFieldPrefix( LimitTo limitTo, char[] fieldName,char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = FIELD_REF;
		} else if( limitTo == DECLARATIONS ){
			prefix = FIELD_DECL;
		} else {
			return FIELD_ALL;
		}
		
		return bestPrefix( prefix, (char)0, fieldName, containingTypes, matchMode, isCaseSensitive );
	}  
	
	public static final char[] bestEnumeratorPrefix( LimitTo limitTo, char[] enumeratorName,char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = ENUMTOR_REF;
		} else if( limitTo == DECLARATIONS ){
			prefix = ENUMTOR_DECL;
		} else if (limitTo == ALL_OCCURRENCES){
			return ENUMTOR_ALL;
		}
		else {
			//Definitions
			return "noEnumtorDefs".toCharArray(); //$NON-NLS-1$
		}
		
		return bestPrefix( prefix, (char)0, enumeratorName, containingTypes, matchMode, isCaseSensitive );
	}  

	public static final char[] bestMethodPrefix( LimitTo limitTo, char[] methodName,char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = METHOD_REF;
		} else if( limitTo == DECLARATIONS ){
			prefix = METHOD_DECL;
		} else if( limitTo == DEFINITIONS ){
			//TODO prefix = METHOD_DEF;
			return METHOD_ALL;	
		} else {
			return METHOD_ALL;
		}
		
		return bestPrefix( prefix, (char)0, methodName, containingTypes, matchMode, isCaseSensitive );
	}  
	
	public static final char[] bestFunctionPrefix( LimitTo limitTo, char[] functionName, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = FUNCTION_REF;
		} else if( limitTo == DECLARATIONS ){
			prefix = FUNCTION_DECL;
		} else if ( limitTo == DEFINITIONS ){
			//TODO prefix = FUNCTION_DEF;
			return FUNCTION_ALL;
		} else {
			return FUNCTION_ALL;
		}
		return bestPrefix( prefix, (char)0, functionName, null, matchMode, isCaseSensitive );
	}  
		
	public static final char[] bestPrefix( char [] prefix, char optionalType, char[] name, char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char[] 	result = null;
		int 	pos    = 0;
		
		int wildPos, starPos = -1, questionPos;
		
		//length of prefix + separator
		int length = prefix.length;
		
		//add length for optional type + another separator
		if( optionalType != 0 )
			length += 2;
		
		if (!isCaseSensitive){
			//index is case sensitive, thus in case attempting case insensitive search, cannot consider
			//type name.
			name = null;
		} else if( matchMode == PATTERN_MATCH && name != null ){
			int start = 0;

			char [] temp = new char [ name.length ];
			boolean isEscaped = false;
			int tmpIdx = 0;
			for( int i = 0; i < name.length; i++ ){
				if( name[i] == '\\' ){
					if( !isEscaped ){
						isEscaped = true;
						continue;
					} 
					isEscaped = false;		
				} else if( name[i] == '*' && !isEscaped ){
					starPos = i;
					break;
				} 
				temp[ tmpIdx++ ] = name[i];
			}
			
			name = new char [ tmpIdx ];
			System.arraycopy( temp, 0, name, 0, tmpIdx );				
		
			//starPos = CharOperation.indexOf( '*', name );
			questionPos = CharOperation.indexOf( '?', name );

			if( starPos >= 0 ){
				if( questionPos >= 0 )
					wildPos = ( starPos < questionPos ) ? starPos : questionPos;
				else 
					wildPos = starPos;
			} else {
				wildPos = questionPos;
			}
			 
			switch( wildPos ){
				case -1 : break;
				case 0  : name = null;	break;
				default : name = CharOperation.subarray( name, 0, wildPos ); break;
			}
		}
		//add length for name
		if( name != null ){
			length += name.length;
		} else {
			//name is null, don't even consider qualifications.
			result = new char [ length ];
			System.arraycopy( prefix, 0, result, 0, pos = prefix.length );
			if( optionalType != 0){
				result[ pos++ ] = optionalType;
				result[ pos++ ] = SEPARATOR; 
			}
			return result;
		}
		 		
		//add the total length of the qualifiers
		//we don't want to mess with the contents of this array (treat it as constant)
		//so check for wild cards later.
		if( containingTypes != null ){
			for( int i = 0; i < containingTypes.length; i++ ){
				if( containingTypes[i].length > 0 ){
					length += containingTypes[ i ].length;
					length++; //separator
				}
			}
		}
		
		//because we haven't checked qualifier wild cards yet, this array might turn out
		//to be too long. So fill a temp array, then check the length after
		char [] temp = new char [ length ];
		
		System.arraycopy( prefix, 0, temp, 0, pos = prefix.length );
		
		if( optionalType != 0 ){
			temp[ pos++ ] = optionalType;
			temp[ pos++ ] = SEPARATOR;
		}
		
		System.arraycopy( name, 0, temp, pos, name.length );
		pos += name.length;
		
		if( containingTypes != null ){
			for( int i = containingTypes.length - 1; i >= 0; i-- ){
				if( matchMode == PATTERN_MATCH ){
					starPos     = CharOperation.indexOf( '*', containingTypes[i] );
					questionPos = CharOperation.indexOf( '?', containingTypes[i] );

					if( starPos >= 0 ){
						if( questionPos >= 0 )
							wildPos = ( starPos < questionPos ) ? starPos : questionPos;
						else 
							wildPos = starPos;
					} else {
						wildPos = questionPos;
					}
					
					if( wildPos >= 0 ){
						temp[ pos++ ] = SEPARATOR;
						System.arraycopy( containingTypes[i], 0, temp, pos, wildPos );
						pos += starPos;
						break;
					}
				}
				
				if( containingTypes[i].length > 0 ){
					temp[ pos++ ] = SEPARATOR;
					System.arraycopy( containingTypes[i], 0, temp, pos, containingTypes[i].length );
					pos += containingTypes[i].length;
				}
			}
		}
	
		if( pos < length ){
			result = new char[ pos ];
			System.arraycopy( temp, 0, result, 0, pos );	
		} else {
			result = temp;
		}
		
		return result;
	}

	/**
	 * @param _limitTo
	 * @param simpleName
	 * @param _matchMode
	 * @param _caseSensitive
	 * @return
	 */
	public static final char[] bestMacroPrefix( LimitTo limitTo, char[] macroName, int matchMode, boolean isCaseSenstive ){
		//since we only index macro declarations we already know the prefix
		char [] prefix = null;
		if( limitTo == DECLARATIONS ){
			prefix = MACRO_DECL;
		} else {
			return null;
		}
		
		return bestPrefix( prefix,  (char)0, macroName, null, matchMode, isCaseSenstive );	
	}
	
	/**
	 * @param _limitTo
	 * @param simpleName
	 * @param _matchMode
	 * @param _caseSensitive
	 * @return
	 */
	public static final char[] bestIncludePrefix( LimitTo limitTo, char[] incName, int matchMode, boolean isCaseSenstive ){
		//since we only index macro declarations we already know the prefix
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = INCLUDE_REF;
		} else {
			return null;
		}
		
		return bestPrefix( prefix,  (char)0, incName, null, matchMode, isCaseSenstive );	
	}

	public void addEnumtorDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, ENUMTOR_DECL, ENUMTOR_DECL.length, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addEnumtorRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, ENUMTOR_REF, ENUMTOR_REF.length, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addMacroDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, MACRO_DECL, MACRO_DECL.length, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addMacroRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, MACRO_REF, MACRO_REF.length, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addFieldDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, FIELD_DECL, FIELD_DECL.length, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addFieldRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, FIELD_REF, FIELD_REF.length, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addMethodDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, METHOD_DECL, METHOD_DECL.length, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addMethodRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, METHOD_REF, METHOD_REF.length, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addFunctionDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, FUNCTION_DECL, FUNCTION_DECL.length, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addFunctionRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, FUNCTION_REF, FUNCTION_REF.length, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addNamespaceDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, NAMESPACE_DECL, NAMESPACE_DECL.length, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addNamespaceRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, NAMESPACE_REF, NAMESPACE_REF.length, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addIncludeRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
	    addRef(indexedFileNumber,  name, INCLUDE_REF, INCLUDE_REF.length, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addStructDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_STRUCT , ICSearchConstants.DECLARATIONS, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addStructRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_STRUCT , ICSearchConstants.REFERENCES, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addTypedefDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_TYPEDEF, ICSearchConstants.DECLARATIONS, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addTypedefRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_TYPEDEF, ICSearchConstants.REFERENCES, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addUnionDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_UNION, ICSearchConstants.DECLARATIONS, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addUnionRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_UNION, ICSearchConstants.REFERENCES, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addVariableDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_VAR, ICSearchConstants.DECLARATIONS, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addVariableRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_VAR, ICSearchConstants.REFERENCES, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addClassDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_CLASS, ICSearchConstants.DECLARATIONS, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addClassRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_CLASS, ICSearchConstants.REFERENCES, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addEnumDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_ENUM, ICSearchConstants.DECLARATIONS, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addEnumRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_ENUM, ICSearchConstants.REFERENCES, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addDerivedDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_DERIVED, ICSearchConstants.DECLARATIONS, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addDerivedRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_DERIVED, ICSearchConstants.REFERENCES, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addFriendDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_FRIEND, ICSearchConstants.DECLARATIONS, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addFriendRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_FRIEND, ICSearchConstants.REFERENCES, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addVarDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_VAR, ICSearchConstants.DECLARATIONS, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addVarRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_VAR, ICSearchConstants.REFERENCES, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addFwd_ClassDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_FWD_CLASS, ICSearchConstants.DECLARATIONS, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addFwd_ClassRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_FWD_CLASS, ICSearchConstants.REFERENCES, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addFwd_StructDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_FWD_STRUCT, ICSearchConstants.DECLARATIONS, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addFwd_StructRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_FWD_STRUCT, ICSearchConstants.REFERENCES, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
	
	public void addFwd_UnionDecl(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_FWD_UNION, ICSearchConstants.DECLARATIONS, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}

	public void addFwd_UnionRef(int indexedFileNumber, char[][] name, int offset, int offsetLength, int offsetType) {
		addRef(indexedFileNumber,  name, ICIndexStorageConstants.TYPE_FWD_UNION, ICSearchConstants.REFERENCES, offset,offsetLength, ICIndexStorageConstants.OFFSET);
	}
}
