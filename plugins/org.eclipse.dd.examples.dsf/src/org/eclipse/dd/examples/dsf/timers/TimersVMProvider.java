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
package org.eclipse.dd.examples.dsf.timers;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.RootVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.DefaultVMModelProxyStrategy;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IColumnPresentation;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * 
 */
@SuppressWarnings("restriction")
public class TimersVMProvider extends AbstractDMVMProvider {

    /**
     * The object to be set to the viewer that shows contents supplied by this provider.
     * @see org.eclipse.jface.viewers.TreeViewer#setInput(Object)  
     */
    private final IAdaptable fViewerInputObject = 
        new IAdaptable() {
            /**
             * The input object provides the viewer access to the viewer model adapter.
             */
            @SuppressWarnings("unchecked")
            public Object getAdapter(Class adapter) {
                if ( adapter.isInstance(getVMAdapter()) ) {
                    return getVMAdapter();
                }
                return null;
            }
            
            @Override
            public String toString() {
                return "Timers View Root"; //$NON-NLS-1$
            }
        };

    private DefaultVMModelProxyStrategy fModelProxyStrategy;

        
    /** Enumeration of possible layouts for the timers view model */
    public enum ViewLayout { ALARMS_AT_TOP, TIMERS_AT_TOP }
    
    public TimersVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext, DsfSession session) {
        super(adapter, presentationContext, session);
        setViewLayout(ViewLayout.ALARMS_AT_TOP);   
    }
    
    
    public Object getViewerInputObject() {
        return fViewerInputObject;
    }
    
    /** 
     * Configures a new layout for the timers view model.
     * @param layout New layout to use.
     */
    public void setViewLayout(ViewLayout layout) {
        if (layout == ViewLayout.ALARMS_AT_TOP) {
            IRootVMNode root = new RootVMNode(this); 
            IVMNode alarmsNode = new AlarmsVMNode(this, getSession());
            IVMNode timersNode0 = new TimersVMNode(this, getSession());
            addChildNodes(root, new IVMNode[] { alarmsNode, timersNode0 });
            IVMNode timersNode = new TimersVMNode(this, getSession());
            addChildNodes(alarmsNode, new IVMNode[] { timersNode });
            IVMNode alarmStatusNode = new AlarmStatusVMNode(this, getSession());
            addChildNodes(timersNode, new IVMNode[] { alarmStatusNode });
            setRootNode(root);
        } else if (layout == ViewLayout.TIMERS_AT_TOP) {
            IRootVMNode root = new RootVMNode(this); 
            IVMNode timersNode = new TimersVMNode(this, getSession());
            addChildNodes(root, new IVMNode[] { timersNode });
            IVMNode alarmsNode = new AlarmsVMNode(this, getSession());
            addChildNodes(timersNode, new IVMNode[] { alarmsNode });
            IVMNode alarmStatusNode = new AlarmStatusVMNode(this, getSession());
            addChildNodes(alarmsNode, new IVMNode[] { alarmStatusNode });
            setRootNode(root);
        }
        
        /* TODO: replace with an event
            fModelProxyStrategy.fireModelChanged(
                new ModelDelta(getRootElement(), IModelDelta.CONTENT));
        */
    }

    @Override
    public IColumnPresentation createColumnPresentation(IPresentationContext context, Object element) {
        return new TimersViewColumnPresentation();
    }

    @Override
    public String getColumnPresentationId(IPresentationContext context, Object element) {
        return TimersViewColumnPresentation.ID;
    }
    
}
