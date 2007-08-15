/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Randy Rohrbach (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.ui.viewmodel.detailpanesupport;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jface.viewers.IStructuredSelection;

/**
 *  This provides a simple Detail Pane Factory for the core debug views for DSF.
 */

@SuppressWarnings("restriction")
public class DetailPaneFactory implements IDetailPaneFactory {

    public static final String DSF_DETAIL_PANE_ID   = Messages.getString("DetailPaneFactory.0"); //$NON-NLS-1$
    public static final String DSF_DETAIL_PANE_NAME = Messages.getString("DetailPaneFactory.1"); //$NON-NLS-1$
    public static final String DSF_DETAIL_PANE_DESC = Messages.getString("DetailPaneFactory.2");  //$NON-NLS-1$
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#createDetailsArea(java.lang.String)
     */
    public IDetailPane createDetailPane(String id) {
        return new DetailPane();
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getDetailsTypes(org.eclipse.jface.viewers.IStructuredSelection)
     */
    @SuppressWarnings("unchecked")
    public Set getDetailPaneTypes(IStructuredSelection selection) {
        Set<String> possibleIDs = new HashSet<String>(1);
        possibleIDs.add(DSF_DETAIL_PANE_ID);
        return possibleIDs;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.IDetailPaneFactory#getDefaultDetailPane(java.util.Set, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public String getDefaultDetailPane(IStructuredSelection selection) {
        return DSF_DETAIL_PANE_ID;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getName(java.lang.String)
     */
    public String getDetailPaneName(String id) {
        if (id.equals(DSF_DETAIL_PANE_ID)){
            return DSF_DETAIL_PANE_NAME;
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.views.variables.IDetailsFactory#getDescription(java.lang.String)
     */
    public String getDetailPaneDescription(String id) {
        if (id.equals(DSF_DETAIL_PANE_ID)){
            return DSF_DETAIL_PANE_DESC;
        }
        return null;
    }

}
