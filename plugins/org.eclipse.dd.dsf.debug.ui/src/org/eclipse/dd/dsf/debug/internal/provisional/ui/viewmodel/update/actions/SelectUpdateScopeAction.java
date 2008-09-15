/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.update.actions;

import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.actions.AbstractVMProviderActionDelegate;
import org.eclipse.dd.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.update.ICachingVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.update.ICachingVMProviderExtension;
import org.eclipse.dd.dsf.ui.viewmodel.update.IVMUpdateScope;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;

/**
 * @since 1.1
 */
public class SelectUpdateScopeAction extends AbstractVMProviderActionDelegate  {

    private final String fUpdateScopeId;
    
    public SelectUpdateScopeAction(String policyId) {
        fUpdateScopeId = policyId;
    }
    
    protected String getUpdateScopeId() {
        return fUpdateScopeId;
    }
    
    @Override
    public void init(IViewPart view) {
        super.init(view);
        update();
    }
    
    public void run(IAction action) {
    	if(action.isChecked())
    	{
	        IVMProvider provider = getVMProvider();
	        if (provider instanceof ICachingVMProvider) {
	            ICachingVMProviderExtension cachingProvider = (ICachingVMProviderExtension)provider;
	            IVMUpdateScope policy = getScopeFromProvider(cachingProvider, getUpdateScopeId());
	            if (policy != null) {
	                cachingProvider.setActiveUpdateScope(policy);
	            }
	        }
    	}
    }
    
    private IVMUpdateScope getScopeFromProvider(ICachingVMProviderExtension provider, String id) {
        for (IVMUpdateScope policy : provider.getAvailableUpdateScopes()) {
            if (policy.getID().equals(id)) {
                return policy;
            }
        }
        return null;
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        super.selectionChanged(action, selection);
        update();
    }
    
    @Override
    public void debugContextChanged(DebugContextEvent event) {
        super.debugContextChanged(event);
        update();
    }
    
    protected void update() {
        IVMProvider provider = getVMProvider();
        if (provider instanceof ICachingVMProviderExtension) {
            getAction().setEnabled(true);
            IVMUpdateScope activeScope = ((ICachingVMProviderExtension)provider).getActiveUpdateScope();
            getAction().setChecked( activeScope != null && getUpdateScopeId().equals(activeScope.getID()) );
        } else {
            getAction().setEnabled(false);
        }
    }
}
