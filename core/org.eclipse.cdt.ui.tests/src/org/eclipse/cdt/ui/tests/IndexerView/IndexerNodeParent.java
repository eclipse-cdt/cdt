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

import java.io.File;
import java.util.Collection;

import org.eclipse.cdt.core.parser.util.ArrayUtil;
import org.eclipse.cdt.internal.core.index.IEntryResult;
import org.eclipse.cdt.ui.testplugin.CTestPlugin;

/**
 * @author dsteffle
 */
public class IndexerNodeParent extends IndexerNodeLeaf {
    public static final int PAGE_SIZE = 40;
    private int pageSize=PAGE_SIZE;
    private static final IndexerNodeLeaf[] EMPTY_INDEXER_NODE_LEAVES = new IndexerNodeLeaf[0];
    private IndexerNodeLeaf[] children = EMPTY_INDEXER_NODE_LEAVES;
    private IndexerNodeLeaf[] childrenToDisplay = new IndexerNodeLeaf[PAGE_SIZE];
    private int lastBackwardDisplayed = 0;
    private int lastForwardDisplayed=0;
    private boolean isForward=true;
    private boolean firstDisplay=true;
    private IndexerFilterManager filterManager = null;
    private IndexerView.ViewContentProvider view=null;
    
    public IndexerNodeParent(IEntryResult result, File indexerFile, IndexerView.ViewContentProvider view) {
        super(result, indexerFile);
        
        // create an IndexerFilterManager using the FilterIndexerViewDialog (since all of the work is done there anyways)
        FilterIndexerViewDialog dialog = new FilterIndexerViewDialog(CTestPlugin.getStandardDisplay().getActiveShell(), this, view.getProjectName());
        dialog.readSettings(dialog.getDialogSettings());
        
        filterManager = dialog.createFilterManager();
        
        try {
            pageSize = Integer.valueOf(dialog.getPageSize()).intValue();
        } catch (NumberFormatException e) {}
        
        this.view = view;
    }

    public IndexerNodeLeaf[] getChildren() {
        // if there is nothing to display return an empty list
        if (children.length == 0) return EMPTY_INDEXER_NODE_LEAVES;
        // obey the bounds of the list!
        if (!firstDisplay && (!isForward && lastBackwardDisplayed==0 ||
                isForward && lastForwardDisplayed==children.length-1)) {
            if (isForward)
                view.setDisplayForwards(false);
            else
                view.setDisplayBackwards(false);

            return (IndexerNodeLeaf[])ArrayUtil.removeNulls(IndexerNodeLeaf.class, childrenToDisplay);
        }
        
        int start=0;
        if (isForward) {
            if (lastForwardDisplayed==0) start=0;
            else start=lastForwardDisplayed+1;
        } else {
            if (lastBackwardDisplayed==0) start=0;
            else start=lastBackwardDisplayed-1;
        }
        boolean shouldDisplay=true;
        int numAdded=0;
        
        int i=start, j=(isForward?0:pageSize-1);
        IndexerNodeLeaf[] temp = new IndexerNodeLeaf[pageSize];
        boolean tempIsUseful=false;
        while(numAdded<pageSize && i<children.length && i >=0) {
            // only add the child to the children to display if it matches the current filters set on the view
            shouldDisplay = filterManager.isFiltered(children[i]);
            
            if (shouldDisplay) {
                tempIsUseful=true;
                
                temp[j] = children[i];
                numAdded++;
                if (isForward) lastForwardDisplayed=i;
                else if (j==pageSize-1) lastForwardDisplayed=i;
                if (j==0) lastBackwardDisplayed=i;

                // move the index to the next entry in the array to store the next valid child to display 
                if (isForward) {
                    if (j+1<temp.length) j++;
                } else {
                    if (j-1>=0) j--;
                }
            }

            shouldDisplay=true; // reset this value
            
            // move the index to the next child to analyze
            if (isForward) {
                i++;
            } else {
                i--;
            }
        }

        // if there is useful content on the next page, return it, otherwise just return what is being displayed
        if (tempIsUseful) {
            childrenToDisplay = new IndexerNodeLeaf[pageSize]; // blank the old array being displayed
            // copy the temp array into the actual array
            for(int k=0, l=0; k<temp.length; k++) {
                if (temp[k] != null) {
                    childrenToDisplay[k] = temp[k];
                    l++;
                }
            }
            
            if (isForward) {
                if (firstDisplay)
                    view.setDisplayForwards(true);
                else
                    view.setDisplayBackwards(true);
            } else {
                view.setDisplayForwards(true);
            }
        } else {
            if (isForward)
                view.setDisplayForwards(false);
            else
                view.setDisplayBackwards(false);
        }
        
        firstDisplay=false;
        
        return (IndexerNodeLeaf[])ArrayUtil.removeNulls(IndexerNodeLeaf.class, childrenToDisplay);
    }
    
    public void setChildren(IndexerNodeLeaf[] children) {
        this.children = children;
    }
    
    public boolean hasChildren() {
        return (children!=null && children.length > 0);
    }
    
    public int getFullLength() {
        return children.length;
    }
    
    public void setIsForward(boolean direction) {
        isForward = direction;
    }
    
    public void setFilterManager(Collection filters, String filterName) {
        this.filterManager = new IndexerFilterManager(filters, filterName);
    }
    
    public void reset() {
        lastBackwardDisplayed = 0;
        lastForwardDisplayed=0;
        isForward=true;
        firstDisplay=true;
        childrenToDisplay = EMPTY_INDEXER_NODE_LEAVES;
    }
    
    public void setPageSize(int size) {
        pageSize = size;
    }
    
    public void setView(IndexerView.ViewContentProvider view) {
        this.view = view;
    }

    public int getFilteredCount() {
        int filteredCount=0;
        for(int i=0; i<children.length; i++) {
            if (filterManager.isFiltered(children[i])) filteredCount++;
        }
        return filteredCount;
    }
}
