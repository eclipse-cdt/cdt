/*******************************************************************************
 * Copyright (c) 2006, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson AB		  - Modules view for DSF implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.modules;

import java.util.Map;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.ConfinedToDsfExecutor;
import org.eclipse.cdt.dsf.concurrent.DsfRunnable;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules;
import org.eclipse.cdt.dsf.debug.service.IRunControl;
import org.eclipse.cdt.dsf.debug.service.IModules.IModuleDMContext;
import org.eclipse.cdt.dsf.debug.service.IModules.IModuleDMData;
import org.eclipse.cdt.dsf.debug.service.IModules.ISymbolDMContext;
import org.eclipse.cdt.dsf.debug.ui.IDsfDebugUIConstants;
import org.eclipse.cdt.dsf.internal.ui.DsfUILabelImage;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.concurrent.ViewerDataRequestMonitor;
import org.eclipse.cdt.dsf.ui.viewmodel.VMDelta;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IElementPropertiesProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.IPropertiesUpdate;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelAttribute;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelColumnInfo;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelFont;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.PropertiesBasedLabelProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.StaleDataLabelBackground;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.resource.JFaceResources;

/**
 * @since 1.0
 */    
public class ModulesVMNode extends AbstractDMVMNode
    implements IElementLabelProvider, IElementPropertiesProvider
{
    /**
     * Marker type for the modules VM context.  It allows action enablement 
     * expressions to check for module context type.
     */
    public class ModuleVMContext extends DMVMContext {
        protected ModuleVMContext(IDMContext dmc) {
            super(dmc);
        }
    }
    
    /**
     * @since 2.0
     */    
    public static final String PROP_IS_LOADED = "is_loaded";  //$NON-NLS-1$
    
    
    /**
     * The label provider delegate.  This VM node will delegate label updates to this provider
     * which can be created by sub-classes. 
     *  
     * @since 2.0
     */    
    private IElementLabelProvider fLabelProvider;

    /**
     * Creates the label provider delegate.  This VM node will delegate label 
     * updates to this provider which can be created by sub-classes.   
     *  
     * @return Returns the label provider for this node. 
     *  
     * @since 2.0
     */    
    protected IElementLabelProvider createLabelProvider() {
        PropertiesBasedLabelProvider provider = new PropertiesBasedLabelProvider();

        provider.setColumnInfo(
            PropertiesBasedLabelProvider.ID_COLUMN_NO_COLUMNS, 
            new LabelColumnInfo(new LabelAttribute[] { 
                new LabelText(MessagesForModulesVM.ModulesVMNode_No_columns__text_format, new String[] { PROP_NAME }),
                new DsfUILabelImage(IDsfDebugUIConstants.IMG_OBJS_SHARED_LIBRARY_SYMBOLS_LOADED) {
                    { setPropertyNames(new String[] { PROP_IS_LOADED }); }
                    
                    @Override
                    public boolean checkProperty(String propertyName, IStatus status, Map<String,Object> properties) {
                        if (PROP_IS_LOADED.equals(propertyName)) {
                            return Boolean.TRUE.equals( properties.get(propertyName) );
                        }
                        return super.checkProperty(propertyName, status, properties);
                    };
                },
                new DsfUILabelImage(IDsfDebugUIConstants.IMG_OBJS_SHARED_LIBRARY_SYMBOLS_UNLOADED),
                new StaleDataLabelBackground(),
                new LabelFont(JFaceResources.getFontDescriptor(IDebugUIConstants.PREF_VARIABLE_TEXT_FONT).getFontData()[0])
            }));
        
        return provider;
    }

    public ModulesVMNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, IModuleDMContext.class);
        
        fLabelProvider = createLabelProvider();
    }
    
    @Override
    public String toString() {
        return "ModulesVMNode(" + getSession().getId() + ")";  //$NON-NLS-1$ //$NON-NLS-2$
    }
    
    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        IModules modulesService = getServicesTracker().getService(IModules.class);
        final ISymbolDMContext symDmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), ISymbolDMContext.class) ;
        
        if (modulesService == null || symDmc == null) {
            handleFailedUpdate(update);
            return;
        }
        
        modulesService.getModules(
            symDmc,
            new ViewerDataRequestMonitor<IModuleDMContext[]>(getSession().getExecutor(), update) { 
                @Override
                public void handleCompleted() {
                    if (!isSuccess()) {
                        update.done();
                        return;
                    }
                    fillUpdateWithVMCs(update, getData());
                    update.done();
                }}); 
    }
    
    @Override
    protected IDMVMContext createVMContext(IDMContext dmc) {
        return new ModuleVMContext(dmc);
    }
    
    /*
     * @since 2.0
     */    
    public void update(final IPropertiesUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    updatePropertiesInSessionThread(updates);
                }});
        } catch (RejectedExecutionException e) {
            for (IPropertiesUpdate update : updates) {
                handleFailedUpdate(update);
            }
        }
    }
    
    public void update(final ILabelUpdate[] updates) {
        fLabelProvider.update(updates);
    }
    
    /**
     * @since 2.0
     */    
    @ConfinedToDsfExecutor("getSession().getExecutor()")
    protected void updatePropertiesInSessionThread(final IPropertiesUpdate[] updates) {
        IModules modulesService = getServicesTracker().getService(IModules.class);
        for (final IPropertiesUpdate update : updates) {
            final IModuleDMContext dmc = findDmcInPath(update.getViewerInput(), update.getElementPath(), IModuleDMContext.class);
            // If either update or service are not valid, fail the update and exit.
            if ( modulesService == null || dmc == null ) {
            	handleFailedUpdate(update);
                return;
            }
            
            modulesService.getModuleData(
                dmc, 
                new ViewerDataRequestMonitor<IModuleDMData>(getSession().getExecutor(), update) { 
                    @Override
                    protected void handleSuccess() {
                        fillModuleDataProperties(update, getData());
                        update.done();
                    }
                });
        }
    }

    /**
     * @since 2.0
     */
    protected void fillModuleDataProperties(IPropertiesUpdate update, IModuleDMData data) {
        update.setProperty(PROP_NAME, data.getName());
        update.setProperty(PROP_IS_LOADED, data.isSymbolsLoaded());
    }    

    public int getDeltaFlags(Object e) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            return IModelDelta.CONTENT;
        } 
        return IModelDelta.NO_CHANGE;
    }

    public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset, RequestMonitor rm) {
        if (e instanceof IRunControl.ISuspendedDMEvent) {
            // Create a delta that indicates all groups have changed
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        } 
        
        rm.done();
    }
}
