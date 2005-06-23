/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.ctagsindexer;

import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.cdt.core.ICDescriptor;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.IConsoleParser;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.domsourceindexer.AbstractIndexerRunner;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;

/**
 * @author Bogdan Gheorghe
 */
public class CTagsIndexerRunner extends AbstractIndexerRunner {
	private CTagsIndexer indexer;
	String ctagsLocation;
    /**
     * @param resource
     * @param indexer
     */
    public CTagsIndexerRunner(IFile resource, CTagsIndexer indexer) {
        this.resourceFile = resource;
        this.indexer = indexer;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.internal.core.index.sourceindexer.AbstractIndexer#indexFile(org.eclipse.cdt.internal.core.index.IDocument)
     */
    protected void indexFile(IFile file) throws IOException {
    	IndexedFileEntry indFile =output.addIndexedFile(file.getFullPath().toString());
       
    	String[] args = {
		        "--excmd=number",  //$NON-NLS-1$
		        "--format=2", //$NON-NLS-1$
				"--sort=no",  //$NON-NLS-1$
				"--fields=aiKlmnsSz", //$NON-NLS-1$
				"--c-types=cdefgmnpstuvx", //$NON-NLS-1$
				"--c++-types=cdefgmnpstuvx", //$NON-NLS-1$
				"--languages=c,c++", //$NON-NLS-1$
				"-f", "-", resourceFile.getName()}; //$NON-NLS-1$ //$NON-NLS-2$
    	
            IConsole console = CCorePlugin.getDefault().getConsole(null);
            console.start(resourceFile.getProject());
            OutputStream cos;
			try {
				cos = console.getOutputStream();
			} catch (CoreException e1) {
				return;
			}

            String errMsg = null;
            CommandLauncher launcher = new CommandLauncher();
            
			//Remove any existing problem markers
			try {
				resourceFile.getProject().deleteMarkers(ICModelMarker.INDEXER_MARKER, true,IResource.DEPTH_ZERO);
			} catch (CoreException e) {} 
			
            long startTime=0;
            if (AbstractIndexerRunner.TIMING)
                startTime = System.currentTimeMillis();
            
            CTagsConsoleParser parser = new CTagsConsoleParser(this);
            IConsoleParser[] parsers = { parser };
            ConsoleOutputSniffer sniffer = new ConsoleOutputSniffer(parsers);
            
            OutputStream consoleOut = (sniffer == null ? cos : sniffer.getOutputStream());
            OutputStream consoleErr = (sniffer == null ? cos : sniffer.getErrorStream());
            
            IPath fileDirectory = resourceFile.getRawLocation().removeLastSegments(1);
            
            IPath ctagsExecutable = new Path("ctags"); //$NON-NLS-1$
	         if (!useDefaultCTags()){
	        	 //try to read the executable path from the descriptor
	        	 if (getCTagsLocation()){
	        		 ctagsExecutable = new Path(ctagsLocation);
	        	 }
	         }
	         
            Process p = launcher.execute(ctagsExecutable, args, null, fileDirectory); //$NON-NLS-1$
            if (p != null) {
                try {
                    // Close the input of the Process explicitely.
                    // We will never write to it.
                    p.getOutputStream().close();
                } catch (IOException e) {}
                if (launcher.waitAndRead(consoleOut, consoleErr, new NullProgressMonitor()) != CommandLauncher.OK) {
                    errMsg = launcher.getErrorMessage();
                }
            }
            else {
                errMsg = launcher.getErrorMessage();
				indexer.createProblemMarker(CCorePlugin.getResourceString("CTagsIndexMarker.CTagsMissing"), resourceFile.getProject()); //$NON-NLS-1$
            }

            consoleOut.close();
            consoleErr.close();
            cos.close();
            
            if (AbstractIndexerRunner.TIMING){
                System.out.println("CTagsIndexer Total Time: " + (System.currentTimeMillis() - startTime)); //$NON-NLS-1$
                System.out.flush();
            }  
    }

    	private boolean useDefaultCTags(){
		try {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(indexer.getProject(), false);
			if (cdesc == null)
				return true;
			
			ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
			if (cext.length > 0) {
				for (int i = 0; i < cext.length; i++) {
						String orig = cext[i].getExtensionData("ctagslocationtype"); //$NON-NLS-1$
						if (orig != null){
							if (orig.equals(CTagsIndexer.CTAGS_PATH_DEFAULT))
								return true;
							else if (orig.equals(CTagsIndexer.CTAGS_PATH_SPECIFIED))
								return false;
						}
				}
			}
		} catch (CoreException e) {}
	
		return false;
	}
	
	private boolean getCTagsLocation() {
		try {
			ICDescriptor cdesc = CCorePlugin.getDefault().getCProjectDescription(indexer.getProject(), false);
			if (cdesc == null)
				return false;
			
			ICExtensionReference[] cext = cdesc.get(CCorePlugin.INDEXER_UNIQ_ID);
			if (cext.length > 0) {
				for (int i = 0; i < cext.length; i++) {
						String orig = cext[i].getExtensionData("ctagslocation"); //$NON-NLS-1$
						if (orig != null){
							ctagsLocation=orig;
							return true;
						}
				}
			}
		} catch (CoreException e) {}
		
		return false;
	}
	
    protected void addMarkers(IFile tempFile, IFile originator, Object problem, Object location) {}
	
	
    
    
    
}
