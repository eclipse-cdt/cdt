/**********************************************************************
 * Copyright (c) 2005 IBM Canada and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation 
 **********************************************************************/
package org.eclipse.cdt.ui.tests.IndexerView;

import java.io.IOException;

import org.eclipse.cdt.core.browser.PathUtil;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.internal.core.index.cindexstorage.IndexedFileEntry;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.BlocksIndexInput;
import org.eclipse.cdt.internal.core.index.cindexstorage.io.IndexInput;
import org.eclipse.cdt.internal.ui.search.CSearchQuery;
import org.eclipse.cdt.internal.ui.search.CSearchResult;
import org.eclipse.cdt.internal.ui.search.NewSearchResultCollector;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;

/**
 * @author dsteffle
 */
public class IndexerQuery extends CSearchQuery implements ISearchQuery {
    private static final int DEFAULT_MATCH_INFO_SIZE = 4;
    private static final String BLANK_STRING = ""; //$NON-NLS-1$
    private CSearchResult _result;
    private IndexerNodeLeaf leaf=null;
    private String queryLabel = null;
    
    /**
     * 
     */
    public IndexerQuery(IndexerNodeLeaf leaf, String queryLabel, String pattern) {
        super(CTestPlugin.getWorkspace(), pattern, false, null, null, null, queryLabel, null);
        this.leaf = leaf;
        this.queryLabel = queryLabel;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.search.ui.ISearchQuery#run(org.eclipse.core.runtime.IProgressMonitor)
     */
    public IStatus run(IProgressMonitor monitor)
            throws OperationCanceledException {
        
        final CSearchResult textResult= (CSearchResult) getSearchResult();
        
        IProgressMonitor mainSearchPM= new SubProgressMonitor(monitor, 1000);
        NewSearchResultCollector collector = new NewSearchResultCollector(textResult, mainSearchPM);
        
        collector.aboutToStart();
        
        MatchInfo[] matches = generateMatchInfo();
        for (int i=0; i<matches.length; i++) {
            try {
                if ( matches[i] != null ) {
				  Object fileResource = null;
				  IResource tempResource = matches[i].getResource();
				  IPath tempPath = matches[i].getPath();
				  //Determine whether this match is internal (ie. has a resource) or
				  //external (ie. has a path) and create a match based on the result 
				  if (tempResource != null)
					  fileResource =tempResource;
				  else
					  fileResource = tempPath;
                  collector.acceptMatch( createMatch(fileResource, matches[i].getStart(), 
                          matches[i].getEnd(), matches[i].getName(), matches[i].getPath()) );
                }
            } catch (CoreException ce) {}
        }
                
        mainSearchPM.done();
        collector.done();
        
        return new Status(IStatus.OK, CTestPlugin.getPluginId(), 0, BLANK_STRING, null); //$NON-NLS-1$  
    }
    
    private MatchInfo[] generateMatchInfo() {
        IndexInput input = new BlocksIndexInput(leaf.indexFile);
        IEntryResult entryResult = leaf.getResult();
        MatchInfo[] matches = new MatchInfo[DEFAULT_MATCH_INFO_SIZE];
        try {
            input.open();
           
            int[] references = entryResult.getFileReferences();
            int[][]offsets = entryResult.getOffsets();
            int[][]offsetLengths = entryResult.getOffsetLengths();
            if (offsets != null){
                for (int j=0; j<offsets.length; j++){
                    for (int k=0; k<offsets[j].length; k++){
                        MatchInfo match = new MatchInfo();
                        if (references.length > j-1) {
                            IndexedFileEntry file = input.getIndexedFile(references[j]);
                            if (file != null){
								IPath filePath = new Path(file.getPath());
								//If we can verify that the file exists within the workspace, we'll use it
								//to open the appropriate editor - if not we can just set the path and we'll
								//use the external editor mechanism
                                IFile tempFile = ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
								if (tempFile != null && tempFile.exists())
									match.setResource(tempFile);
								else {
									match.setPath(PathUtil.getWorkspaceRelativePath(file.getPath()));
								}
								
                            }
                        }
                        int start=0;
                        int end=0;
                        try {
                        start=Integer.valueOf(String.valueOf(offsets[j][k]).substring(1)).intValue();
                        end=start+offsetLengths[j][k];
                        } catch (NumberFormatException nfe) {}
                        
                        match.setStart(start) ;
                        match.setEnd(end);
                        match.setName(leaf.getName());
                        
                        matches = (MatchInfo[])ArrayUtil.append(MatchInfo.class, matches, match);
                    }
                }
            }
            
        } catch (IOException e) {
        }

        return matches;
    }
    
    private class MatchInfo {
        private IPath path=null;
        private int start=0;
        private int end=0;
        private String name=null;
		private IResource  resource=null;
		
        public IPath getPath() {
            return path;
        }
        public void setPath(IPath path) {
            this.path = path;
        }
        public int getEnd() {
            return end;
        }
        public void setEnd(int end) {
            this.end = end;
        }
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public int getStart() {
            return start;
        }
        public void setStart(int start) {
            this.start = start;
        }
		public IResource getResource() {
			return resource;
		}
		public void setResource(IResource resource) {
			this.resource = resource;
		}
    }
    
     public IMatch createMatch( Object fileResource, int start, int end, String name, IPath referringElement ) {
        BasicSearchMatch result = new BasicSearchMatch();
        if( fileResource instanceof IResource )
            result.resource = (IResource) fileResource;
        else if( fileResource instanceof IPath )
            result.path = (IPath) fileResource;
            
        result.startOffset = start;
        result.endOffset = end;
        result.parentName = BLANK_STRING; //$NON-NLS-1$
        result.referringElement = referringElement;
        
        result.name = name;
    
        result.type = ICElement.C_FIELD; // TODO Devin static for now, want something like BasicSearchResultCollector#setElementInfo
        result.visibility = ICElement.CPP_PUBLIC; // TODO Devin static for now, want something like BasicSearchResultCollector#setElementInfo
        result.returnType = BLANK_STRING;
        
        return result;
    }    


    /* (non-Javadoc)
     * @see org.eclipse.search.ui.ISearchQuery#getLabel()
     */
    public String getLabel() {
        return queryLabel;
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.ISearchQuery#canRerun()
     */
    public boolean canRerun() {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.ISearchQuery#canRunInBackground()
     */
    public boolean canRunInBackground() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.search.ui.ISearchQuery#getSearchResult()
     */
    public ISearchResult getSearchResult() {
        if (_result == null)
            _result= new CSearchResult(this);
        return _result;
    }

}
