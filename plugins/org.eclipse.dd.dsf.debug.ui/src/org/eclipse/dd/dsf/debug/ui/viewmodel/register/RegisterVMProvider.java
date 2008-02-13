/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.ui.viewmodel.register;

import org.eclipse.dd.dsf.debug.ui.viewmodel.numberformat.FormattedValuePreferenceStore;
import org.eclipse.dd.dsf.debug.ui.viewmodel.update.BreakpointHitUpdatePolicy;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.RootDMVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.update.AutomaticUpdatePolicy;
import org.eclipse.dd.dsf.ui.viewmodel.update.IVMUpdatePolicy;
import org.eclipse.dd.dsf.ui.viewmodel.update.ManualUpdatePolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

/**
 *  Provides the VIEW MODEL for the DEBUG MODEL REGISTER view.
 */
@SuppressWarnings("restriction")
public class RegisterVMProvider extends AbstractDMVMProvider
    implements IPropertyChangeListener
{
    /*
     *  Current default for register formatting.
     */
    public RegisterVMProvider(AbstractVMAdapter adapter, IPresentationContext context, DsfSession session) {
        super(adapter, context, session);

        context.addPropertyChangeListener(this);
        
        /*
         *  Create the register data access routines.
         */
        SyncRegisterDataAccess regAccess = new SyncRegisterDataAccess(session) ;
        
        /*
         *  Create the top level node to deal with the root selection.
         */
        IRootVMNode rootNode = new RootDMVMNode(this);
        
        /*
         *  Create the Group nodes next. They represent the first level shown in the view.
         */
        IVMNode registerGroupNode = new RegisterGroupVMNode(this, getSession(), regAccess);
        addChildNodes(rootNode, new IVMNode[] { registerGroupNode });
        
        /*
         * Create the next level which is the registers themselves.
         */
        IVMNode registerNode = new RegisterVMNode(FormattedValuePreferenceStore.getDefault(), this, getSession(), regAccess);
        addChildNodes(registerGroupNode, new IVMNode[] { registerNode });
        
        /*
         * Create the next level which is the bitfield level.
         */
        IVMNode bitFieldNode = new RegisterBitFieldVMNode(FormattedValuePreferenceStore.getDefault(), this, getSession(), regAccess);
        addChildNodes(registerNode, new IVMNode[] { bitFieldNode });
        
        /*
         *  Now set this schema set as the layout set.
         */
        setRootNode(rootNode);
    }

    @Override
    protected IVMUpdatePolicy[] createUpdateModes() {
        return new IVMUpdatePolicy[] { new AutomaticUpdatePolicy(), new ManualUpdatePolicy(), new BreakpointHitUpdatePolicy() };
    }
    
    @Override
    public void dispose() {
        getPresentationContext().removePropertyChangeListener(this);
        super.dispose();
    }

    @Override
    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
        return new RegisterColumnPresentation();
    }
    
    @Override
    public String getColumnPresentationId(IPresentationContext context, Object element) {
        return RegisterColumnPresentation.ID;
    }
    
    public void propertyChange(PropertyChangeEvent event) {
        handleEvent(event);
    }
}
