/**
 * Copyright (c) 2006 Wind River Systems and others. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Eclipse Public
 * License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: Wind River Systems - initial API and implementation
 */
package org.eclipse.dd.dsf.debug.ui.viewmodel.variable;

import org.eclipse.dd.dsf.debug.service.IFormattedValues;
import org.eclipse.dd.dsf.debug.ui.viewmodel.DebugViewSelectionRootLayoutNode;
import org.eclipse.dd.dsf.debug.ui.viewmodel.formatsupport.IFormattedValuePreferenceStore;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMRootLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMProviderWithCache;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentationFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

@SuppressWarnings("restriction")
public class VariableVMProvider extends AbstractDMVMProviderWithCache implements
		IColumnPresentationFactory, IFormattedValuePreferenceStore {
    
    private String defaultFormatId = IFormattedValues.NATURAL_FORMAT;

	public VariableVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
        super(adapter, context, session);

        /*
         *  Create the variable data access routines.
         */
        SyncVariableDataAccess varAccess = new SyncVariableDataAccess() ;

        /*
         *  Create the top level node to deal with the root selection.
         */
        IVMRootLayoutNode debugViewSelection = new DebugViewSelectionRootLayoutNode(this);
        
        /*
         *  Create the local variables nodes next. They represent the first level shown in the view.
         */
        IVMLayoutNode localsNode = new VariableLocalsLayoutNode(this, this, getSession(), varAccess);
        debugViewSelection.setChildNodes(new IVMLayoutNode[] { localsNode });
        
        /*
         * Create the next level which represents members of structs/unions/enums and elements of arrays.
         */
        IVMLayoutNode subExpressioNode = new VariableSubExpressionsLayoutNode(this, getSession(), varAccess);
        localsNode.setChildNodes(new IVMLayoutNode[] { subExpressioNode });
        
        /*
         *  Now set this schema set as the layout set.
         */
        setRootLayoutNode(debugViewSelection);
    }

    @Override
    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
        return new VariableColumnPresentation();
    }
    
    @Override
    public String getColumnPresentationId(IPresentationContext context, Object element) {
        return VariableColumnPresentation.ID;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.ui.viewmodel.formatsupport.IFormattedValuePreferenceStore#getDefaultFormatId()
     */
    public String getDefaultFormatId() {
        return defaultFormatId;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.dd.dsf.debug.ui.viewmodel.formatsupport.IFormattedValuePreferenceStore#setDefaultFormatId(java.lang.String)
     */
    public void setDefaultFormatId(String id) {
        defaultFormatId = id;
    }
}
