/*******************************************************************************
 * Copyright (c) 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.IndexerView;

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
    private boolean sort=true;
    private boolean displayFullName=true;
    private boolean navigate=false;
    private int totalNumberOfFilesIndexed=0;
    
    public IndexerNodeParent(IEntryResult result, String [] fileMap, IndexerView.ViewContentProvider view) {
        super(result, fileMap);
        
        // create an IndexerFilterManager using the FilterIndexerViewDialog (since all of the work is done there anyways)
        FilterIndexerViewDialog dialog = new FilterIndexerViewDialog(CTestPlugin.getStandardDisplay().getActiveShell(), this, view.getProjectName());
        dialog.readSettings(dialog.getDialogSettings());
        
        filterManager = dialog.createFilterManager();
        pageSize = dialog.getPageSize();
        
        this.view = view;
    }

    public IndexerNodeLeaf[] getChildren() {
        // if there is nothing to display return an empty list
        if (children.length == 0) return EMPTY_INDEXER_NODE_LEAVES;

        // navigate is used to determine if the array should be traversed or not (button pressed or first loading)
        if (!navigate) {
            return (IndexerNodeLeaf[])ArrayUtil.removeNulls(IndexerNodeLeaf.class, childrenToDisplay);
        } else {
            navigate = false;
        }
        
        // obey the bounds of the list!
        if (!firstDisplay && (!isForward && lastBackwardDisplayed==0 ||
                isForward && lastForwardDisplayed==children.length-1)) {
            if (isForward)
                view.setDisplayForwards(false);
            else
                view.setDisplayBackwards(false);

            return (IndexerNodeLeaf[])ArrayUtil.removeNulls(IndexerNodeLeaf.class, childrenToDisplay);
        }
        
        if (firstDisplay && children.length > 1) {
            if (sort) {// sort children based on name
                quickSort(children, 0, children.length - 1, true);
            } else {// sort children based on word
                quickSort(children, 0, children.length - 1, false);
            }
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
    
    private static void quickSort(IndexerNodeLeaf[] list, int left, int right, boolean sortName) {
        int original_left= left;
        int original_right= right;
        String mid=null;
        if (sortName) {
            mid= list[(left + right) / 2].getName().toUpperCase();
        } else {
            mid= new String(list[(left + right) / 2].getResult().getName()).toUpperCase();
        }
        do {
            String compareL = null;
            String compareR = null;
            if (sortName) {
                compareL = list[left].getName().toUpperCase();
                compareR = list[right].getName().toUpperCase();
            } else {
                compareL = new String(list[left].getResult().getName()).toUpperCase();
                compareR = new String(list[right].getResult().getName()).toUpperCase();
            }
            while (compareL.compareTo(mid) < 0) {
                left++;
                if (sortName) {
                    compareL = list[left].getName().toUpperCase();
                } else {
                    compareL = new String(list[left].getResult().getName()).toUpperCase();
                }
            }
            while (mid.compareTo(compareR) < 0) {
                right--;
                if (sortName) {
                    compareR = list[right].getName().toUpperCase();
                } else {
                    compareR = new String(list[right].getResult().getName()).toUpperCase();
                }
            }
            if (left <= right) {
                IndexerNodeLeaf tmp= list[left];
                list[left]= list[right];
                list[right]= tmp;
                left++;
                right--;
            }
        } while (left <= right);
        if (original_left < right) {
            quickSort(list, original_left, right, sortName);
        }
        if (left < original_right) {
            quickSort(list, left, original_right, sortName);
        }
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
    
    public void setFilterManager(boolean [] filters, String filterName) {
        this.filterManager = new IndexerFilterManager(filters, filterName);
    }
    
    public void reset() {
        navigate=true;
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

    public boolean isSort() {
        return sort;
    }

    public void setSort(boolean sort) {
        this.sort = sort;
    }

    public boolean isDisplayFullName() {
        return displayFullName;
    }

    public void setDisplayFullName(boolean displayFullName) {
        this.displayFullName = displayFullName;
    }

    public boolean isNavigate() {
        return navigate;
    }

    public void setNavigate(boolean navigate) {
        this.navigate = navigate;
    }

	public int getTotalNumberOfFilesIndexed() {
		return totalNumberOfFilesIndexed;
	}

	public void setTotalNumberOfFilesIndexed(int totalNumberOfFilesIndexed) {
		this.totalNumberOfFilesIndexed = totalNumberOfFilesIndexed;
	}
}
