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

import java.io.IOException;
import java.io.StringReader;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.internal.core.index.IDocument;
import org.eclipse.cdt.internal.core.parser.ScannerInfo;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

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
	public static final String[] FILE_TYPES= new String[] {"cpp","c", "cc", "cxx"}; //$NON-NLS-1$
	
	//protected DefaultProblemFactory problemFactory= new DefaultProblemFactory(Locale.getDefault());
	IFile resourceFile;
		
	SourceIndexer(IFile resourceFile)	{
		this.resourceFile = resourceFile;
	}
	/**
	 * Returns the file types the <code>IIndexer</code> handles.
	 */
	public String[] getFileTypes(){
		return FILE_TYPES;
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
		
		IParser parser = ParserFactory.createParser( 
							ParserFactory.createScanner( new StringReader( document.getStringContent() ), resourceFile.getLocation().toOSString(), scanInfo, ParserMode.COMPLETE_PARSE, language, requestor ), 
							requestor, ParserMode.COMPLETE_PARSE, language );
		
		try{
			boolean retVal = parser.parse();
			
			if (VERBOSE){
				if (!retVal)
					AbstractIndexer.verbose("PARSE FAILED " + resourceFile.getName().toString());
				else
					AbstractIndexer.verbose("PARSE SUCCEEDED " + resourceFile.getName().toString());			
			}	
		}
		catch( Exception e ){
			System.out.println( "Parse Exception in SourceIndexer" ); 
			e.printStackTrace();
		}
	}
	/**
	 * Sets the document types the <code>IIndexer</code> handles.
	 */
	
	public void setFileTypes(String[] fileTypes){}
}
