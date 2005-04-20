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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.internal.core.index.IIndex;
import org.eclipse.cdt.internal.core.index.IIndexer;
import org.eclipse.cdt.internal.core.index.IIndexerOutput;
import org.eclipse.cdt.internal.core.index.cindexstorage.ICIndexStorageConstants;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

public class CTagsFileReader {
	
	String filename = null;
	List list = null;
    IProject project;
	IIndex index;
	CTagsIndexer indexer;
	
	public CTagsFileReader(IProject project,String filename, CTagsIndexer indexer) {
		this.filename = filename;
		this.project = project;
		this.indexer = indexer;
	}

	public void parse() {
		BufferedReader reader = null;
		try{
			reader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex){
			indexer.createProblemMarker(CCorePlugin.getResourceString("CTagsIndexMarker.fileMissing") + " - " + filename, project);  //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		
		CTagsHeader header = new CTagsHeader();
		// Skip the header.
		
		try {
			header.parse(reader);
		} catch (IOException e) {
			indexer.createProblemMarker(e.getMessage(), project);
			return;
		}
		
		String s;
		String currentFileName = null;
		IFile currentFile = null;
		CTagsConsoleParser parser = new CTagsConsoleParser(null);
		MiniIndexer indexer = null;
		
		//Make sure we have an index before proceeding
		if (index == null)
		    return;
		
		try {
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
		} catch (IOException e){}
	}
	
	class MiniIndexer implements IIndexer {
		
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
			
	        
	    	if (kind.equals(CTagsConsoleParser.CLASS)){
	    		output.addClassDecl(fileNum, fullName, lineNumber, 1, ICIndexStorageConstants.LINE);
	    	} else if (kind.equals(CTagsConsoleParser.MACRO)){
	    		output.addMacroDecl(fileNum, fullName, lineNumber, 1, ICIndexStorageConstants.LINE);
	    	} else if (kind.equals(CTagsConsoleParser.ENUMERATOR)){
	    		output.addEnumtorDecl(fileNum, fullName, lineNumber, 1, ICIndexStorageConstants.LINE);
	    	} else if (kind.equals(CTagsConsoleParser.FUNCTION)){
	    		output.addFunctionDecl(fileNum, fullName, lineNumber, 1, ICIndexStorageConstants.LINE);
	    	} else if (kind.equals(CTagsConsoleParser.ENUM)){
	    		output.addEnumDecl(fileNum, fullName, lineNumber, 1, ICIndexStorageConstants.LINE);
	    	} else if (kind.equals(CTagsConsoleParser.MEMBER)){
	    		output.addFieldDecl(fileNum, fullName, lineNumber, 1, ICIndexStorageConstants.LINE);
	    	} else if (kind.equals(CTagsConsoleParser.NAMESPACE)){
	    		output.addNamespaceDecl(fileNum, fullName, lineNumber, 1, ICIndexStorageConstants.LINE);
	    	} else if (kind.equals(CTagsConsoleParser.PROTOTYPE)){
	    		output.addFunctionDecl(fileNum, fullName, lineNumber, 1, ICIndexStorageConstants.LINE);
	    	    //type = ICSearchConstants.DEFINITIONS;
	    	} else if (kind.equals(CTagsConsoleParser.STRUCT)){
	    		output.addStructDecl(fileNum, fullName, lineNumber, 1, ICIndexStorageConstants.LINE);
	    	} else if (kind.equals(CTagsConsoleParser.TYPEDEF)){
	    		output.addTypedefDecl(fileNum, fullName, lineNumber, 1, ICIndexStorageConstants.LINE);
	    	} else if (kind.equals(CTagsConsoleParser.UNION)){
	    		output.addUnionDecl(fileNum, fullName, lineNumber, 1, ICIndexStorageConstants.LINE);
	    	} else if (kind.equals(CTagsConsoleParser.VARIABLE)){
	    		output.addVariableDecl(fileNum, fullName, lineNumber, 1, ICIndexStorageConstants.LINE);
	    	} else if (kind.equals(CTagsConsoleParser.EXTERNALVAR)){
	    	
	    	}
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
