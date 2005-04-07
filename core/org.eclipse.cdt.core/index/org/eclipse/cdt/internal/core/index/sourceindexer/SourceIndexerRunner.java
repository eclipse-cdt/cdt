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
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParser;
import org.eclipse.cdt.core.parser.IProblem;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.cdt.core.parser.ParserFactory;
import org.eclipse.cdt.core.parser.ParserFactoryError;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.cdt.core.parser.ParserMode;
import org.eclipse.cdt.core.parser.ParserUtil;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.impl.IndexDelta;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
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
	
	protected void indexFile(IFile file) throws IOException {
		// Add the name of the file to the index
		IndexedFileEntry indFile =output.addIndexedFile(file.getFullPath().toString());
        
		// Create a new Parser 
		SourceIndexerRequestor requestor = new SourceIndexerRequestor(this, resourceFile);
		
		int problems = indexer.indexProblemsEnabled( resourceFile.getProject() );
		setProblemMarkersEnabled( problems );
		requestRemoveMarkers( resourceFile, null );
		
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
		    long startTime = 0;
            
            if (AbstractIndexer.TIMING)
                startTime = System.currentTimeMillis();
            
			boolean retVal = parser.parse();
			
	        if (AbstractIndexer.TIMING){
	            long currentTime = System.currentTimeMillis() - startTime;
	            System.out.println("Source Indexer - Index Time for " + resourceFile.getName() + ": " + currentTime + " ms"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	            long tempTotaltime = indexer.getTotalIndexTime() + currentTime;
	            indexer.setTotalIndexTime(tempTotaltime);
	            System.out.println("Source Indexer - Total Index Time: " + tempTotaltime + " ms"); //$NON-NLS-1$ //$NON-NLS-2$
	            
	        }
	        
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
				reportProblems();
			
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer#addMarkers(org.eclipse.core.resources.IFile, org.eclipse.core.resources.IFile, java.lang.Object)
     */
    protected void addMarkers(IFile tempFile, IFile originator, Object problem) {
        if (problem instanceof IProblem) {
            IProblem iProblem = (IProblem) problem;
            
            try {
               //we only ever add index markers on the file, so DEPTH_ZERO is far enough
               IMarker[] markers = tempFile.findMarkers(ICModelMarker.INDEXER_MARKER, true,IResource.DEPTH_ZERO);
               
               boolean newProblem = true;
               
               if (markers.length > 0) {
                   IMarker tempMarker = null;
                   Integer tempInt = null; 
                   String tempMsgString = null;
                   
                   for (int i=0; i<markers.length; i++) {
                       tempMarker = markers[i];
                       tempInt = (Integer) tempMarker.getAttribute(IMarker.LINE_NUMBER);
                       tempMsgString = (String) tempMarker.getAttribute(IMarker.MESSAGE);
                       if (tempInt != null && tempInt.intValue()==iProblem.getSourceLineNumber() &&
                           tempMsgString.equalsIgnoreCase( INDEXER_MARKER_PREFIX + iProblem.getMessage())) 
                       {
                           newProblem = false;
                           break;
                       }
                   }
               }
               
               if (newProblem) {
                   IMarker marker = tempFile.createMarker(ICModelMarker.INDEXER_MARKER);
                   int start = iProblem.getSourceStart();
                   int end = iProblem.getSourceEnd();
                   if (end <= start)
                       end = start + 1;
                   marker.setAttribute(IMarker.LOCATION, iProblem.getSourceLineNumber());
                   marker.setAttribute(IMarker.MESSAGE, INDEXER_MARKER_PREFIX + iProblem.getMessage());
                   marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
                   marker.setAttribute(IMarker.LINE_NUMBER, iProblem.getSourceLineNumber());
                   marker.setAttribute(IMarker.CHAR_START, start);
                   marker.setAttribute(IMarker.CHAR_END, end); 
                   marker.setAttribute(INDEXER_MARKER_ORIGINATOR, originator.getFullPath().toString() );
               }
               
            } catch (CoreException e) {
                // You need to handle the cases where attribute value is rejected
            }
        }
    }
}
