/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.core.index.sourceindexer;

/**
 * @author bgheorgh
*/

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICLogConstants;
import org.eclipse.cdt.core.index.IIndexDelta;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.parser.CodeReader;
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
import org.eclipse.cdt.internal.core.index.impl.IndexDelta;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

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
public class SourceIndexerRunner extends AbstractIndexer {
	 
	IFile resourceFile;
	private SourceIndexer indexer;
	
	/**
	 * @param resource
	 * @param out
	 */
	public SourceIndexerRunner(IFile resource, SourceIndexer indexer) {
		this.indexer = indexer;
		this.resourceFile = resource;
	}
	
	protected void indexFile(IDocument document) throws IOException {
		// Add the name of the file to the index
		output.addDocument(document);
		// Create a new Parser 
		SourceIndexerRequestor requestor = new SourceIndexerRequestor(this, resourceFile);
		
		int problems = indexer.indexProblemsEnabled( resourceFile.getProject() );
		requestor.setProblemMarkersEnabled( problems );
		requestor.requestRemoveMarkers( resourceFile, null );
		
		//Get the scanner info
		IProject currentProject = resourceFile.getProject();
		IScannerInfo scanInfo = new ScannerInfo();
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(currentProject);
		if (provider != null){
		  IScannerInfo buildScanInfo = provider.getScannerInformation(resourceFile);
		  if (buildScanInfo != null){
			scanInfo = new ScannerInfo(buildScanInfo.getDefinedSymbols(), buildScanInfo.getIncludePaths());
		  }
		}
		
		//C or CPP?
		ParserLanguage language = CoreModel.hasCCNature(currentProject) ? ParserLanguage.CPP : ParserLanguage.C;
		
		IParser parser = null;

		InputStream contents = null;
		try {
			contents = resourceFile.getContents();
			CodeReader reader = new CodeReader(resourceFile.getLocation().toOSString(), resourceFile.getCharset(), contents);
			parser = ParserFactory.createParser( 
							ParserFactory.createScanner(reader, scanInfo, ParserMode.COMPLETE_PARSE, language, requestor, ParserUtil.getScannerLogService(), null ), 
							requestor, ParserMode.COMPLETE_PARSE, language, ParserUtil.getParserLogService() );
		} catch( ParserFactoryError pfe ){
		} catch (CoreException e) {
		} finally {
			if (contents != null) {
				contents.close();
			}
		}
		
		try{
			boolean retVal = parser.parse();
	
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
			//if the user disable problem reporting since we last checked, don't report the collected problems
			if( indexer.indexProblemsEnabled( resourceFile.getProject() ) != 0 )
				requestor.reportProblems();
			
			//Report events
			ArrayList filesTrav = requestor.getFilesTraversed();
			IndexDelta indexDelta = new IndexDelta(resourceFile.getProject(),filesTrav, IIndexDelta.INDEX_FINISHED_DELTA);
			indexer.notifyListeners(indexDelta);
			//Release all resources
			parser=null;
			currentProject = null;
			requestor = null;
			provider = null;
			scanInfo=null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.search.indexing.AbstractIndexer#getResourceFile()
	 */
	public IFile getResourceFile() {
		return resourceFile;
	}

	/**
	 * @param fullPath
	 * @param path
	 */
	public boolean haveEncounteredHeader(IPath fullPath, Path path) {
		return indexer.haveEncounteredHeader(fullPath, path);
	}
}
