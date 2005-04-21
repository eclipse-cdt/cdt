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

import java.util.Collection;

/**
 * @author dsteffle
 */
public class IndexerFilterManager {
    private static final String BLANK_STRING = ""; //$NON-NLS-1$
    private static final String COMMA_SEPARATOR = ","; //$NON-NLS-1$
    Collection filters = null;
    String nameFilter = null;
    
    public IndexerFilterManager(Collection filters, String nameFilter) {
        this.filters = filters;
        this.nameFilter = nameFilter;
    }
    
    public boolean isFiltered(IndexerNodeLeaf leaf) {
        String[] nameFilters = nameFilter.split(COMMA_SEPARATOR);
        for(int i=0; i<nameFilters.length; i++) {
            if (nameFilters != null) nameFilters[i] = nameFilters[i].trim();
        }
        
        if (!filters.contains(new Integer(leaf.getFiltersType()))) return false;
        if (leaf.getName() != null && nameFilters != null && nameFilters.length > 0) {
            boolean matchesPattern=false;
            for(int l=0; l<nameFilters.length; l++) {
                if (nameFilters[l].equals(BLANK_STRING) || leaf.getShortName().matches(nameFilters[l])) {
                    matchesPattern=true;
                    break;
                }
            }
            if (!matchesPattern) 
                return false;
        }
        
        
        return true;
    }
}
