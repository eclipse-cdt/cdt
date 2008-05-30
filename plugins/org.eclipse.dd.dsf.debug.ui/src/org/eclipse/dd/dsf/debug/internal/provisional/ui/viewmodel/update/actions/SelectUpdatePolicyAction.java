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
import org.eclipse.dd.dsf.ui.viewmodel.update.IVMUpdatePolicy;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewPart;

/**
 * 
 */
public class SelectUpdatePolicyAction extends AbstractVMProviderActionDelegate  {

    private final String fUpdatePolicyId;
    
    public SelectUpdatePolicyAction(String policyId) {
        fUpdatePolicyId = policyId;
    }
    
    protected String getUpdatePolicyId() {
        return fUpdatePolicyId;
    }
    
    @Override
    public void init(IViewPart view) {
        super.init(view);
        update();
    }
    
    public void run(IAction action) {
        IVMProvider provider = getVMProvider();
        if (provider instanceof ICachingVMProvider) {
            ICachingVMProvider cachingProvider = (ICachingVMProvider)provider;
            IVMUpdatePolicy policy = getPolicyFromProvider(cachingProvider, getUpdatePolicyId());
            if (policy != null) {
                cachingProvider.setActiveUpdatePolicy(policy);
            }
        }
    }
    
    private IVMUpdatePolicy getPolicyFromProvider(ICachingVMProvider provider, String id) {
        for (IVMUpdatePolicy policy : provider.getAvailableUpdatePolicies()) {
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
        if (provider instanceof ICachingVMProvider) {
            getAction().setEnabled(true);
            IVMUpdatePolicy activePolicy = ((ICachingVMProvider)provider).getActiveUpdatePolicy();
            getAction().setChecked( activePolicy != null && getUpdatePolicyId().equals(activePolicy.getID()) );
        } else {
            getAction().setEnabled(false);
        }
    }
}
