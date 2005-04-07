/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.internal.core.index.ctagsindexer;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IIndexer;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;
import org.eclipse.cdt.internal.core.index.cindexstorage.ICIndexStorageConstants;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.domsourceindexer.IndexEncoderUtil;
import org.eclipse.cdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.cdt.internal.core.search.indexing.IIndexEncodingConstants;
import org.eclipse.cdt.internal.core.search.indexing.IIndexEncodingConstants.EntryType;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

public class CTagsFileReader {
	String filename = null;
	List list = null;
    IProject project;
	IIndex index;
	
	public CTagsFileReader(IProject project,String filename) {
		this.filename = filename;
		this.project = project;
	}

	public void parse() throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader(filename));
		CTagsHeader header = new CTagsHeader();
		// Skip the header.
		header.parse(reader);
		
		String s;
		String currentFileName = null;
		IFile currentFile = null;
		CTagsConsoleParser parser = new CTagsConsoleParser(null);
		MiniIndexer indexer = null;
		
		//Make sure we have an index before proceeding
		if (index == null)
		    return;
		
		while ((s = reader.readLine()) != null) {
		   CTagEntry tagEntry = parser.processLineReturnTag(s);
		   
		   String fileName = tagEntry.fileName;
		   
		   if (currentFileName == null ||
		      (!currentFileName.equals(fileName))){
		      currentFileName = fileName; 
		      currentFile = (IFile) project.findMember(fileName);
		      indexer = new MiniIndexer(currentFile);
		      index.add(currentFile,indexer);
		   }
		  
		   //encode new tag in current file
		   char[][] fullName = parser.getQualifiedName(tagEntry);
		   //encode name
		   String lineNumber = (String) tagEntry.tagExtensionField.get(CTagsConsoleParser.LINE);
		   indexer.addToOutput(fullName,(String)tagEntry.tagExtensionField.get(CTagsConsoleParser.KIND), Integer.parseInt(lineNumber));
		}
	}

	class MiniIndexer implements IIndexer, IIndexConstants {
		
	    IIndexerOutput output;
	    IFile currentFile;
	    /**
         * @param currentFile
         */
        public MiniIndexer(IFile currentFile) {
            this.currentFile = currentFile;
        }
        public void addToOutput(char[][]fullName, String kind, int lineNumber){
        	if (kind == null)
        	  return;
        	
	        IndexedFileEntry mainIndexFile = this.output.getIndexedFile(currentFile.getFullPath().toString());
			int fileNum = 0;
	        if (mainIndexFile != null)
				fileNum = mainIndexFile.getFileID();
			
	        EntryType entryType = null;
	        
	    	ICSearchConstants.LimitTo type = ICSearchConstants.DECLARATIONS;
	        
	        if (kind.equals(CTagsConsoleParser.CLASS)){
	    	    entryType = IIndexEncodingConstants.CLASS;
	    	} else if (kind.equals(CTagsConsoleParser.MACRO)){
	    	    entryType = IIndexEncodingConstants.MACRO;
	    	} else if (kind.equals(CTagsConsoleParser.ENUMERATOR)){
	    	    entryType = IIndexEncodingConstants.ENUMERATOR;
	    	} else if (kind.equals(CTagsConsoleParser.FUNCTION)){
	    	    entryType = IIndexEncodingConstants.FUNCTION;
	    	} else if (kind.equals(CTagsConsoleParser.ENUM)){
	    	    entryType = IIndexEncodingConstants.ENUM;
	    	} else if (kind.equals(CTagsConsoleParser.MEMBER)){
	    	    entryType = IIndexEncodingConstants.FIELD;
	    	} else if (kind.equals(CTagsConsoleParser.NAMESPACE)){
	    	    entryType = IIndexEncodingConstants.NAMESPACE;
	    	} else if (kind.equals(CTagsConsoleParser.PROTOTYPE)){
	    	    entryType = IIndexEncodingConstants.FUNCTION;
	    	} else if (kind.equals(CTagsConsoleParser.STRUCT)){
	    	    entryType = IIndexEncodingConstants.STRUCT;
	    	} else if (kind.equals(CTagsConsoleParser.TYPEDEF)){
	    	    entryType = IIndexEncodingConstants.TYPEDEF;
	    	} else if (kind.equals(CTagsConsoleParser.UNION)){
	    	    entryType = IIndexEncodingConstants.UNION;
	    	} else if (kind.equals(CTagsConsoleParser.VARIABLE)){
	    	    entryType = IIndexEncodingConstants.VAR;
	    	} else if (kind.equals(CTagsConsoleParser.EXTERNALVAR)){
	    	
	    	}
	    	
	    	if (entryType != null)
	    	    output.addRef(fileNum, IndexEncoderUtil.encodeEntry(fullName,entryType,type), lineNumber, 1, ICIndexStorageConstants.LINE);
	    }
        /* (non-Javadoc)
         * @see org.eclipse.cdt.internal.core.index.IIndexer#index(org.eclipse.cdt.internal.core.index.IDocument, org.eclipse.cdt.internal.core.index.IIndexerOutput)
         */
        public void index(IFile file, IIndexerOutput output) throws IOException {
            this.output = output;
            IndexedFileEntry indFile =output.addIndexedFile(file.getFullPath().toString());
        }

        /* (non-Javadoc)
         * @see org.eclipse.cdt.internal.core.index.IIndexer#shouldIndex(org.eclipse.core.resources.IFile)
         */
        public boolean shouldIndex(IFile file) {
            return true;
        }  
	}

    /**
     * @param index
     */
    public void setIndex(IIndex index) {
        this.index = index;
    }

}
