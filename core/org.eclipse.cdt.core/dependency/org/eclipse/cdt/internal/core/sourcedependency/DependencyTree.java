/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

package org.eclipse.cdt.internal.core.sourcedependency;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.parser.IPreprocessor;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.index.IDocument;
import org.eclipse.cdt.internal.core.index.IQueryResult;
import org.eclipse.cdt.internal.core.index.impl.IndexedFile;
import org.eclipse.cdt.internal.core.sourcedependency.impl.InMemoryTree;
import org.eclipse.cdt.internal.core.sourcedependency.impl.IncludeEntry;
import org.eclipse.core.runtime.IPath;


public class DependencyTree implements IDependencyTree {
	
	protected InMemoryTree addsTree;
	
	public DependencyTree(String treeName, String string, boolean b) throws IOException{
		initialize();
	}

	public DependencyTree() throws IOException {
		initialize();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.sourcedependency.IDependencyTree#empty()
	 */
	public void empty() throws IOException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.sourcedependency.IDependencyTree#getIndexFile()
	 */
	public File getIndexFile() {
		// TODO Auto-generated method stub
		return null;
	}
	/**
	 * Returns the number of referencing files in this tree. 
	 */
	public int getNumDocuments() throws IOException {
		return addsTree.getNumFiles();
	}
	/**
	 * Returns the number of include entries in this tree.
	 * @return
	 * @throws IOException
	 */
	public int getNumIncludes() throws IOException {
		return addsTree.getNumIncludes();
	}
	/**
	 * Returns the path corresponding to a given document number
	 */
	public String getPath(int documentNumber) throws IOException {
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.sourcedependency.IDependencyTree#hasChanged()
	 */
	public boolean hasChanged() {
		// TODO Auto-generated method stub
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.sourcedependency.IDependencyTree#query(java.lang.String)
	 */
	public IQueryResult[] query(String word) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.sourcedependency.IDependencyTree#queryInDocumentNames(java.lang.String)
	 */
	public IQueryResult[] queryInDocumentNames(String word)
		throws IOException {
		// TODO Auto-generated method stub
		return null;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.sourcedependency.IDependencyTree#save()
	 */
	public void save() throws IOException {
		// TODO Auto-generated method stub
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.sourcedependency.IDependencyTree#remove(java.lang.String)
	 */
	public void remove(String documentName) throws IOException {
		// TODO Auto-generated method stub
	}
	/**
	 * Add the file that will be preprocessed to the tree, create a new
	 * preprocessor output and preprocess!
	 */
	public void add(IDocument document, String docPath, IScannerInfo newInfo, ParserLanguage language) throws IOException  {
		IndexedFile indexedFile= addsTree.getIndexedFile(document.getName());
		//if (indexedFile != null)
			//remove(indexedFile, 0);
		PreprocessorOutput output= new PreprocessorOutput(addsTree);
		DependencyRequestor depReq = new DependencyRequestor(output,document);
		
		output.addDocument(document);
		
		IPreprocessor preprocessor = ParserFactory.createPreprocessor( new StringReader( document.getStringContent() ),docPath , newInfo, ParserMode.COMPLETE_PARSE, language, depReq);
		preprocessor.process();
	}
	/**
	 * Initialises the indexGenerator.
	 */
	public void initialize() throws IOException {
		//initialisation of addsTree
		addsTree= new InMemoryTree();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.sourcedependency.IDependencyTree#getFileDepencies(int)
	 */
	public String[] getFileDependencies(IPath filePath) throws IOException {
		List tempFileReturn = new ArrayList();
		IndexedFile indexFile = addsTree.getIndexedFile(filePath.toString());
		int fileNum = indexFile.getFileNumber();
		IncludeEntry[] tempEntries = addsTree.getIncludeEntries();
		for (int i=0; i<tempEntries.length; i++)
		{
			int[] fileRefs = tempEntries[i].getRefs();
		    for (int j=0; j<fileRefs.length; j++)
		    {
		    	if (fileRefs[j] == fileNum)
		    	{ 
		    		//System.out.println(filePath.toString() + " references " + y[i].toString());
		    		char[] tempFile = tempEntries[i].getFile();
		    		StringBuffer tempString = new StringBuffer();
		    		tempString.append(tempFile);
		    		tempFileReturn.add(tempString.toString());
		    		break;
		    	}
		    }
		}
		return (String []) tempFileReturn.toArray(new String[tempFileReturn.size()]);
	}
	//TODO: BOG Debug Method Take out
	public void printIncludeEntries(){
	 IncludeEntry[] tempEntries = addsTree.getIncludeEntries();
	 for (int i=0; i<tempEntries.length; i++){
	 	System.out.println(tempEntries[i].toString());
	 }
	}
	//TODO: BOG Debug Method Take out
	public void printIndexedFiles() {
		IndexedFile[] tempFiles = addsTree.getIndexedFiles();
		for (int i=0;i<tempFiles.length;i++){
			System.out.println(tempFiles[i].toString());
		}
		
	}
}
