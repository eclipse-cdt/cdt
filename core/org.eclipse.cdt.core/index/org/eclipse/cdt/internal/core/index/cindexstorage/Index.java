/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.cindexstorage;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.ICDTIndexer;
import org.eclipse.cdt.core.index.IIndexDelta;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.CharOperation;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IIndexerRunner;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.BlocksIndexInput;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.BlocksIndexOutput;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.IndexInput;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.IndexOutput;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.MergeFactory;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.SimpleIndexInput;
import org.eclipse.cdt.internal.core.index.impl.IndexDelta;
import org.eclipse.cdt.internal.core.index.impl.Int;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;

/**
 * An Index is used to create an index on the disk, and to make queries. It uses a set of 
 * indexers and a mergeFactory. The index fills an inMemoryIndex up 
 * to it reaches a certain size, and then merges it with a main index on the disk.
 * <br> <br>
 * The changes are only taken into account by the queries after a merge.
 */

public class Index implements IIndex, ICIndexStorageConstants, ICSearchConstants {
	/**
	 * Maximum size of the index in memory.
	 */
	public static final int MAX_FOOTPRINT= 10000000;

	/**
	 * Index in memory, who is merged with mainIndex each times it 
	 * reaches a certain size.
	 */
	protected InMemoryIndex addsIndex;
	protected IndexInput addsIndexInput;

	/**
	 * State of the indexGenerator: addsIndex empty <=> MERGED, or
	 * addsIndex not empty <=> CAN_MERGE
	 */
	protected int state;

	/**
	 * Files removed form the addsIndex.
	 */
	protected Map removedInAdds;

	/**
	 * Files removed form the oldIndex.
	 */
	protected Map removedInOld;
	protected static final int CAN_MERGE= 0;
	protected static final int MERGED= 1;
	private File indexFile;
	
	private ICDTIndexer indexer = null;
	

	/**
	 * String representation of this index.
	 */
	public String toString;
	
	public Index(String indexName, String toString, boolean reuseExistingFile, ICDTIndexer indexer) throws IOException {
		super();
		state= MERGED;
		indexFile= new File(indexName);
		this.toString = toString;
		this.indexer = indexer;
		initialize(reuseExistingFile);
	}
	/**
	 * Indexes the given document, using the appropriate indexer registered in the indexerRegistry.
	 * If the document already exists in the index, it overrides the previous one. The changes will be 
	 * taken into account after a merge.
	 */
	public void add(IFile file, IIndexerRunner indexer) throws IOException {
		if (timeToMerge()) {
			merge();
		}
		IndexedFileEntry indexedFile= addsIndex.getIndexedFile(file.getFullPath().toString());
		if (indexedFile != null /*&& removedInAdds.get(document.getName()) == null*/
			)
			remove(indexedFile, MergeFactory.ADDS_INDEX);
		IndexerOutput output= new IndexerOutput(addsIndex);
		indexer.index(file, output);
		state= CAN_MERGE;
	}
	/**
	 * Returns true if the index in memory is not empty, so 
	 * merge() can be called to fill the mainIndex with the files and words
	 * contained in the addsIndex. 
	 */
	protected boolean canMerge() {
		return state == CAN_MERGE;
	}
	/**
	 * Initialises the indexGenerator.
	 */
	public void empty() throws IOException {

		if (indexFile.exists()){
			indexFile.delete();
			//initialisation of mainIndex
			InMemoryIndex mainIndex= new InMemoryIndex();
			IndexOutput mainIndexOutput= new BlocksIndexOutput(indexFile);
			if (!indexFile.exists())
				mainIndex.save(mainIndexOutput);
		}

		//initialisation of addsIndex
		addsIndex= new InMemoryIndex();
		addsIndexInput= new SimpleIndexInput(addsIndex);

		//vectors who keep track of the removed Files
		removedInAdds= new HashMap(11);
		removedInOld= new HashMap(11);
	}
	/**
	 * @see IIndex#getIndexFile
	 */
	public File getIndexFile() {
		return indexFile;
	}
	/**
	 * @see IIndex#getNumDocuments
	 */
	public int getNumDocuments() throws IOException {
		//save();
		IndexInput input= new BlocksIndexInput(indexFile);
		try {
			input.open();
			return input.getNumFiles();
		} finally {
			input.close();
		}		
	}
	/**
	 * @see IIndex#getNumWords
	 */
	public int getNumWords() throws IOException {
		//save();
		IndexInput input= new BlocksIndexInput(indexFile);
		try {
			input.open();
			return input.getNumWords();
		} finally {
			input.close();
		}		
	}
	/**
	 * @see IIndex#getNumWords
	 */
	public int getNumIncludes() throws IOException {
		//save();
		IndexInput input= new BlocksIndexInput(indexFile);
		try {
			input.open();
			return input.getNumIncludes();
		} finally {
			input.close();
		}		
	}
	/**
	 * Returns the path corresponding to a given document number
	 */
	public String getPath(int documentNumber) throws IOException {
		//save();
		IndexInput input= new BlocksIndexInput(indexFile);
		try {
			input.open();
			IndexedFileEntry file = input.getIndexedFile(documentNumber);
			if (file == null) return null;
			return file.getPath();
		} finally {
			input.close();
		}		
	}
	
	/**
	 * Returns the path list 
	 */
	public String [] getDocumentList() throws IOException {
		IndexInput input= new BlocksIndexInput(indexFile);
		try {
			input.open();
			int num = input.getNumFiles();
			String [] result = new String[num+1];
			for(int i = 1; i < num+1; i++) {
				//i+1 since reference encoding starts at 1
				IndexedFileEntry file = input.getIndexedFile(i);
				if (file != null) 
					result[i] = file.getPath();
				else 
					result[i] = null;
			}
			
			return result;
		} finally {
			input.close();
		}		
	}
	/**
	 * see IIndex.hasChanged
	 */
	public boolean hasChanged() {
		return canMerge();
	}
	/**
	 * Initialises the indexGenerator.
	 */
	public void initialize(boolean reuseExistingFile) throws IOException {
		//initialisation of addsIndex
		addsIndex= new InMemoryIndex();
		addsIndexInput= new SimpleIndexInput(addsIndex);

		//vectors who keep track of the removed Files
		removedInAdds= new HashMap(11);
		removedInOld= new HashMap(11);

		// check whether existing index file can be read
		if (reuseExistingFile && indexFile.exists()) {
			IndexInput mainIndexInput= new BlocksIndexInput(indexFile);
			try {
				mainIndexInput.open();
			} catch(IOException e) {
				BlocksIndexInput input = (BlocksIndexInput)mainIndexInput;
				try {
					input.setOpened(true);
					input.close();
				} finally {
					input.setOpened(false);
				}
				indexFile.delete();
				mainIndexInput = null;
				throw e;
			}
			mainIndexInput.close();
		} else {
			InMemoryIndex mainIndex= new InMemoryIndex();			
			IndexOutput mainIndexOutput= new BlocksIndexOutput(indexFile);
			mainIndex.save(mainIndexOutput);
		}
	}
	/**
	 * Merges the in memory index and the index on the disk, and saves the results on the disk.
	 */
	protected void merge() throws IOException {
		//initialisation of tempIndex
		File tempFile= new File(indexFile.getAbsolutePath() + "TempVA"); //$NON-NLS-1$

		IndexInput mainIndexInput= new BlocksIndexInput(indexFile);
		BlocksIndexOutput tempIndexOutput= new BlocksIndexOutput(tempFile);

		try {
			//invoke a mergeFactory
			new MergeFactory(
				mainIndexInput, 
				addsIndexInput, 
				tempIndexOutput, 
				removedInOld, 
				removedInAdds).merge();
			
			//rename the file created to become the main index
			File mainIndexFile= (File) mainIndexInput.getSource();
			File tempIndexFile= (File) tempIndexOutput.getDestination();
			boolean deleted = mainIndexFile.delete();
			
			int counter=0;
			while (!deleted && counter<5){
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {}
				counter++;
				deleted=mainIndexFile.delete();
			}
			
			tempIndexFile.renameTo(mainIndexFile);
		} finally {		
			//initialise remove vectors and addsindex, and change the state
			removedInAdds.clear();
			removedInOld.clear();
			addsIndex.init();
			addsIndexInput= new SimpleIndexInput(addsIndex);
			state= MERGED;
			//flush the CDT log
			CCorePlugin.getDefault().cdtLog.flushLog();
			
			//Send out notification to listeners;
			IndexDelta indexDelta = new IndexDelta(null,null,IIndexDelta.MERGE_DELTA);
			indexer.notifyListeners(indexDelta);
		}
	}

	public IEntryResult[] queryEntries(char[] prefix) throws IOException {
		//save();
		IndexInput input= new BlocksIndexInput(indexFile);
		try {
			return input.queryEntriesPrefixedBy(prefix);
		} finally {
			input.close();
		}
	}
	/**
	 * @see IIndex#queryInDocumentNames
	 */
	public IQueryResult[] queryInDocumentNames(String word) throws IOException {
		//save();
		IndexInput input= new BlocksIndexInput(indexFile);
		try {
			return input.queryInDocumentNames(word);
		} finally {
			input.close();
		}
	}
	/**
	 * @see IIndex#queryPrefix
	 */
	public IQueryResult[] queryPrefix(char[] prefix) throws IOException {
		//save();
		IndexInput input= new BlocksIndexInput(indexFile);
		try {
			return input.queryFilesReferringToPrefix(prefix);
		} finally {
			input.close();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.sourcedependency.IDependencyTree#getFileDepencies(int)
	 */
	public String[] getFileDependencies(IPath filePath) throws IOException {
//		List tempFileReturn = new ArrayList();
//		
//		IndexedFile indexFile = addsIndex.getIndexedFile(filePath.toString());
//
//		if (indexFile == null)
//		 return new String[0];
//		 
//		int fileNum = indexFile.getFileNumber();
//		IncludeEntry[] tempEntries = addsIndex.getIncludeEntries();
//		for (int i=0; i<tempEntries.length; i++)
//		{
//			int[] fileRefs = tempEntries[i].getRefs();
//			for (int j=0; j<fileRefs.length; j++)
//			{
//				if (fileRefs[j] == fileNum)
//				{ 
//					char[] tempFile = tempEntries[i].getFile();
//					StringBuffer tempString = new StringBuffer();
//					tempString.append(tempFile);
//					tempFileReturn.add(tempString.toString());
//					break;
//				}
//			}
//		}
//		return (String []) tempFileReturn.toArray(new String[tempFileReturn.size()]);
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.index.IIndex#getFileDependencies(org.eclipse.core.resources.IFile)
	 */
	public String[] getFileDependencies(IFile file) throws IOException  {
		IndexInput input= new BlocksIndexInput(indexFile);
		int fileNum=0;
		List tempFileReturn = new ArrayList();
		try {
			input.open();
			IndexedFileEntry inFile = input.getIndexedFile(file.getFullPath().toString());
			fileNum =inFile.getFileID();
	
			IncludeEntry[] tempEntries = input.queryIncludeEntries(fileNum);
			for (int i=0; i<tempEntries.length; i++)
			{
				char[] tempFile = tempEntries[i].getFile();
				StringBuffer tempString = new StringBuffer();
				tempString.append(tempFile);
				tempFileReturn.add(tempString.toString());
			}
		}
		finally{input.close();}
		return (String []) tempFileReturn.toArray(new String[tempFileReturn.size()]);
	}
	/**
	 * @see IIndex#remove
	 */
	public void remove(String documentName) throws IOException {
		IndexedFileEntry file= addsIndex.getIndexedFile(documentName);
		if (file != null) {
			//the file is in the adds Index, we remove it from this one
			Int lastRemoved= (Int) removedInAdds.get(documentName);
			if (lastRemoved != null) {
				int fileNum= file.getFileID();
				if (lastRemoved.value < fileNum)
					lastRemoved.value= fileNum;
			} else
				removedInAdds.put(documentName, new Int(file.getFileID()));
		} else {
			//we remove the file from the old index
			removedInOld.put(documentName, new Int(1));
		}
		state= CAN_MERGE;
	}
	/**
	 * Removes the given document from the given index (MergeFactory.ADDS_INDEX for the
	 * in memory index, MergeFactory.OLD_INDEX for the index on the disk).
	 */
	protected void remove(IndexedFileEntry file, int index) throws IOException {
		String name= file.getPath();
		if (index == MergeFactory.ADDS_INDEX) {
			Int lastRemoved= (Int) removedInAdds.get(name);
			if (lastRemoved != null) {
				if (lastRemoved.value < file.getFileID())
					lastRemoved.value= file.getFileID();
			} else
				removedInAdds.put(name, new Int(file.getFileID()));
		} else if (index == MergeFactory.OLD_INDEX)
			removedInOld.put(name, new Int(1));
		else
			throw new Error();
		state= CAN_MERGE;
	}
	/**
	 * @see IIndex#save
	 */
	public void save() throws IOException {
		if (canMerge())
			merge();
	}
	/**
	 * Returns true if the in memory index reaches a critical size, 
	 * to merge it with the index on the disk.
	 */
	protected boolean timeToMerge() {
		return (addsIndex.getFootprint() >= MAX_FOOTPRINT);
	}
	public String toString() {
		String str = this.toString;
		if (str == null)
			str = super.toString();
		str += "(length: " + getIndexFile().length() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		return str;
	}
	
	public org.eclipse.cdt.core.index.ICDTIndexer  getIndexer(){
		return (org.eclipse.cdt.core.index.ICDTIndexer) indexer;
	}
	public IEntryResult[] queryEntries(char[] prefix, char optionalType, char[] name, char[][] containingTypes, int matchMode, boolean isCaseSensitive)  throws IOException {
		return queryEntries(Index.bestPrefix(prefix, optionalType, name, containingTypes, matchMode, isCaseSensitive));
	}
	
 	public static final char[] bestTypePrefix( SearchFor searchFor, LimitTo limitTo, char[] typeName, char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == DECLARATIONS ){
			prefix = encodeEntry(IIndex.TYPE, ANY, DECLARATION);
		} else if (limitTo == DEFINITIONS){
		    prefix = encodeEntry(IIndex.TYPE, ANY, DEFINITION);
		} else if( limitTo == REFERENCES ){
			prefix = encodeEntry(IIndex.TYPE, ANY, REFERENCE);
		} else {
			return encodeEntry(IIndex.TYPE, ANY, ANY);
		}
					
		char classType = 0;
		
		if( searchFor == ICSearchConstants.CLASS ){
			classType = typeConstants[IIndex.TYPE_CLASS];
		} else if ( searchFor == ICSearchConstants.STRUCT ){
			classType = typeConstants[IIndex.TYPE_STRUCT];
		} else if ( searchFor == ICSearchConstants.UNION ){
			classType = typeConstants[IIndex.TYPE_UNION];
		} else if ( searchFor == ICSearchConstants.ENUM ){
			classType = typeConstants[IIndex.TYPE_ENUM];
		} else if ( searchFor == ICSearchConstants.TYPEDEF ){
			classType = typeConstants[IIndex.TYPE_TYPEDEF];
		} else if ( searchFor == ICSearchConstants.DERIVED){
			classType = typeConstants[IIndex.TYPE_DERIVED];
		} else if ( searchFor == ICSearchConstants.FRIEND){
			classType = typeConstants[IIndex.TYPE_FRIEND];
		} else {
			//could be TYPE or CLASS_STRUCT, best we can do for these is the prefix
			return prefix;
		}
		
		return bestPrefix( prefix, classType, typeName, containingTypes, matchMode, isCaseSensitive );
	}
	
	public static final char[] bestNamespacePrefix(LimitTo limitTo, char[] namespaceName, char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = encodeEntry(IIndex.NAMESPACE, ANY, REFERENCE);
		} else if ( limitTo == DEFINITIONS ) {
			prefix = encodeEntry(IIndex.NAMESPACE, ANY, DEFINITION);
		} else {
			return encodeEntry(IIndex.NAMESPACE, ANY, ANY);
		}
		
		return bestPrefix( prefix, (char) 0, namespaceName, containingTypes, matchMode, isCaseSensitive );
	}	
		
	public static final char[] bestVariablePrefix( LimitTo limitTo, char[] varName, char[][] containingTypes, int matchMode, boolean isCaseSenstive ){
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = encodeEntry(IIndex.VAR, ANY, REFERENCE);
		} else if( limitTo == DECLARATIONS ){
			prefix = encodeEntry(IIndex.VAR, ANY, DECLARATION);
		} else if( limitTo == DEFINITIONS ){
				prefix = encodeEntry(IIndex.VAR, ANY, DEFINITION);
		} else {
			return encodeEntry(IIndex.VAR, ANY, ANY);
		}
		
		return bestPrefix( prefix, (char)0, varName, containingTypes, matchMode, isCaseSenstive );	
	}

	public static final char[] bestFieldPrefix( LimitTo limitTo, char[] fieldName,char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = encodeEntry(IIndex.FIELD, ANY, REFERENCE);
		} else if( limitTo == DECLARATIONS ){
			prefix = encodeEntry(IIndex.FIELD, ANY, DECLARATION);
		} else if( limitTo == DEFINITIONS ){
			prefix = encodeEntry(IIndex.FIELD, ANY, DEFINITION);
		} else {
			return encodeEntry(IIndex.FIELD, ANY, ANY);
		}
		
		return bestPrefix( prefix, (char)0, fieldName, containingTypes, matchMode, isCaseSensitive );
	}  

	public static final char[] bestEnumeratorPrefix( LimitTo limitTo, char[] enumeratorName,char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = encodeEntry(IIndex.ENUMTOR, ANY, REFERENCE);
		} else if( limitTo == DECLARATIONS ){
			prefix = encodeEntry(IIndex.ENUMTOR, ANY, DECLARATION);
		} else if ( limitTo == DEFINITIONS ) {
			prefix = encodeEntry(IIndex.ENUMTOR, ANY, DEFINITION);
		} else if (limitTo == ALL_OCCURRENCES){
			return encodeEntry(IIndex.ENUMTOR, ANY, ANY);
		}
		
		return bestPrefix( prefix, (char)0, enumeratorName, containingTypes, matchMode, isCaseSensitive );
	}  
	
	public static final char[] bestMethodPrefix( LimitTo limitTo, char[] methodName,char[][] containingTypes, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = encodeEntry(IIndex.METHOD, ANY, REFERENCE);
		} else if( limitTo == DECLARATIONS ){
			prefix = encodeEntry(IIndex.METHOD, ANY, DECLARATION);
		} else if( limitTo == DEFINITIONS ){
			return encodeEntry(IIndex.METHOD, ANY, DEFINITION);	
		} else {
			return encodeEntry(IIndex.METHOD, ANY, ANY);
		}
		
		return bestPrefix( prefix, (char)0, methodName, containingTypes, matchMode, isCaseSensitive );
	}  
	
	public static final char[] bestFunctionPrefix( LimitTo limitTo, char[] functionName, int matchMode, boolean isCaseSensitive) {
		char [] prefix = null;
		if( limitTo == REFERENCES ){
			prefix = encodeEntry(IIndex.FUNCTION, ANY, REFERENCE);
		} else if( limitTo == DECLARATIONS ){
			prefix = encodeEntry(IIndex.FUNCTION, ANY, DECLARATION);
		} else if ( limitTo == DEFINITIONS ){
			return encodeEntry(IIndex.FUNCTION, ANY, DEFINITION);
		} else {
			return encodeEntry(IIndex.FUNCTION, ANY, ANY);
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
			prefix = Index.encodeEntry(IIndex.MACRO, IIndex.ANY, IIndex.DECLARATION);
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
			prefix = encodeEntry(IIndex.INCLUDE, IIndex.ANY, IIndex.REFERENCE);
		} else {
			return null;
		}
		
		return bestPrefix( prefix,  (char)0, incName, null, matchMode, isCaseSenstive );	
	}

	public static String getDescriptionOf (int meta_kind, int kind, int ref) {
		StringBuffer buff = new StringBuffer();
		buff.append(encodings[meta_kind]);
		buff.append(encodingTypes[ref]);
		if(kind != 0) {
			buff.append(typeConstants[kind]);
			buff.append(SEPARATOR);
		}
		return buff.toString();
	}
	public static char [] encodeEntry (int meta_kind, int kind, int ref, String name) {
//		if( kind == ANY && ref == ANY )
//			return encodings[meta_kind];
		StringBuffer buff = new StringBuffer();
		buff.append(encodings[meta_kind]);
		buff.append(encodingTypes[ref]);
		if(kind != 0) {
			buff.append(typeConstants[kind]);
			buff.append( SEPARATOR );
		}
		buff.append ( name ); 

		return buff.toString().toCharArray();
	}
	
	public static char [] encodeEntry (int meta_kind, int kind, int ref) {
		StringBuffer buff = new StringBuffer();
		buff.append(encodings[meta_kind]);
		buff.append(encodingTypes[ref]);
		if(kind != 0)
			buff.append(typeConstants[kind]);
		return buff.toString().toCharArray();
	}
	public IEntryResult[] getEntries(int meta_kind, int kind, int ref, String name) throws IOException {	
		return queryEntries(encodeEntry(meta_kind, kind, ref, name));
	}
	public IEntryResult[] getEntries(int meta_kind, int kind, int ref) throws IOException {
		return queryEntries(encodeEntry(meta_kind, kind, ref));
	}
	
	public IQueryResult[] getPrefix(int meta_kind, int kind, int ref, String name) throws IOException {
		return queryPrefix(encodeEntry(meta_kind, kind, ref, name));
	}
	public IQueryResult[] getPrefix(int meta_kind, int kind, int ref) throws IOException {
		return queryPrefix(encodeEntry(meta_kind, kind, ref));
	}
	
}
