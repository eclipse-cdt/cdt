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

package org.eclipse.cdt.internal.core.search.indexing;

/**
 * @author bgheorgh
*/

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.index.IDocument;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * A SourceIndexer indexes source files using the parser. The following items are indexed:
 * Declarations:
 * - Classes
 * - Structs
 * - Unions
 * References:
 * - Classes
 * - Structs
 * - Unions
 */
public class SourceIndexer extends AbstractIndexer {
	
	//TODO: Indexer, add additional file types
	//Header files: "h" , "hh", "hpp"
	//Use the CModelManager defined file types
	//public static final String[] FILE_TYPES= new String[] {"cpp","c", "cc", "cxx"}; //$NON-NLS-1$
	
	//protected DefaultProblemFactory problemFactory= new DefaultProblemFactory(Locale.getDefault());
	IFile resourceFile;
		
	SourceIndexer(IFile resourceFile)	{
		this.resourceFile = resourceFile;
	}
	/**
	 * Returns the file types the <code>IIndexer</code> handles.
	 */
	public String[] getFileTypes(){
		return CModelManager.sourceExtensions;
	}
	
	protected void indexFile(IDocument document) throws IOException {
		// Add the name of the file to the index
		output.addDocument(document);
		// Create a new Parser
		SourceIndexerRequestor requestor = new SourceIndexerRequestor(this, document);
		
		//Get the scanner info
		IProject currentProject = resourceFile.getProject();
		IScannerInfo scanInfo = new ScannerInfo();
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(currentProject);
		if (provider != null){
		  IScannerInfo buildScanInfo = provider.getScannerInformation(currentProject);
		  if (buildScanInfo != null){
			scanInfo = new ScannerInfo(buildScanInfo.getDefinedSymbols(), buildScanInfo.getIncludePaths());
		  }
		}
		
		//C or CPP?
		ParserLanguage language = CoreModel.getDefault().hasCCNature(currentProject) ? ParserLanguage.CPP : ParserLanguage.C;
		
		IParser parser = null;
		
		try
		{
			
			BufferedInputStream inStream = new BufferedInputStream(resourceFile.getContents());
			parser = ParserFactory.createParser( 
							ParserFactory.createScanner( new BufferedReader(new InputStreamReader(inStream)), resourceFile.getLocation().toOSString(), scanInfo, ParserMode.COMPLETE_PARSE, language, requestor, ParserUtil.getScannerLogService() ), 
							requestor, ParserMode.COMPLETE_PARSE, language, ParserUtil.getParserLogService() );
		} catch( ParserFactoryError pfe )
		{
		} catch (CoreException e) {
		}
		
		try{
			boolean retVal = parser.parse();
			
			if (!retVal)
				org.eclipse.cdt.internal.core.model.Util.log(null, "Failed to index " + resourceFile.getFullPath(), ICLogConstants.CDT); //$NON-NLS-1$

			if (AbstractIndexer.VERBOSE){
				if (!retVal)
					AbstractIndexer.verbose("PARSE FAILED " + resourceFile.getName().toString()); //$NON-NLS-1$
				else
					AbstractIndexer.verbose("PARSE SUCCEEDED " + resourceFile.getName().toString());			 //$NON-NLS-1$
			}	
		}
		catch ( VirtualMachineError vmErr){
			if (vmErr instanceof OutOfMemoryError){
				org.eclipse.cdt.internal.core.model.Util.log(null, "Out Of Memory error: " + vmErr.getMessage() + " on File: " + resourceFile.getName(), ICLogConstants.CDT); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		catch ( Exception ex ){
			if (ex instanceof IOException)
				throw (IOException) ex;
		}
		finally{
			//Release all resources
			parser=null;
			currentProject = null;
			requestor = null;
			provider = null;
			scanInfo=null;
		}
	}
	/**
	 * Sets the document types the <code>IIndexer</code> handles.
	 */
	
	public void setFileTypes(String[] fileTypes){}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.indexing.AbstractIndexer#getResourceFile()
	 */
	public IFile getResourceFile() {
		return resourceFile;
	}
	
}
