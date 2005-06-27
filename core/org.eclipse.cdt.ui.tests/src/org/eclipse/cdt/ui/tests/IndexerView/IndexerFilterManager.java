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

/**
 * @author dsteffle
 */
public class IndexerFilterManager {
    private static final String BLANK_STRING = ""; //$NON-NLS-1$
    private static final String COMMA_SEPARATOR = ","; //$NON-NLS-1$
    boolean [] filters = null;
    String nameFilter = null;
    String [] nameFilters;
    
    public IndexerFilterManager(boolean [] filters, String nameFilter) {
        this.filters = filters;
        this.nameFilter = nameFilter;
        nameFilters = nameFilter.split(COMMA_SEPARATOR);
        for(int i=0; i<nameFilters.length; i++) {
            if (nameFilters != null) nameFilters[i] = nameFilters[i].trim();
        }
        
    }
    
    public boolean isFiltered(IndexerNodeLeaf leaf) {   
        if (!filters[leaf.getFiltersType()]) 
        	return false;
        if (leaf.getName() != null && nameFilters != null && nameFilters.length > 0) {
            for(int l=0; l<nameFilters.length; l++)
                if (nameFilters[l].equals(BLANK_STRING) || leaf.getShortName().matches(nameFilters[l]))
                    return true;
            return false;
        }
        return true;
    }
}
