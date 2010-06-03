/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.VMHandlerUtils;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.ICachingVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.update.IVMUpdatePolicy;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Dynamic menu contribution that shows available update policies 
 * in the current view.
 * 
 * @since 1.1
 */
public class UpdatePoliciesContribution extends CompoundContributionItem implements IWorkbenchContribution {
    
    private class SelectUpdatePolicyAction extends Action {
        private final ICachingVMProvider fProvider;
        private final IVMUpdatePolicy fPolicy;
        SelectUpdatePolicyAction(ICachingVMProvider provider, IVMUpdatePolicy policy) {
            super(policy.getName(), AS_RADIO_BUTTON);
            fProvider = provider;
            fPolicy = policy;
        }

        @Override
        public void run() {
            if (isChecked()) {
                fProvider.setActiveUpdatePolicy(fPolicy);
            }
        }
    }
 
    private IServiceLocator fServiceLocator;

    private static IContributionItem[] NO_UPDATE_POLICIES_CONTRIBUTION_ITEMS = new IContributionItem[] { 
    	new ContributionItem() {
            @Override
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.NONE);
				item.setEnabled(false);
				item.setText(MessagesForVMActions.UpdatePoliciesContribution_EmptyPoliciesList_label);
			}
	
            @Override
			public boolean isEnabled() {
				return false;
			}
    	}
    };
    
    @Override
    protected IContributionItem[] getContributionItems() {
        IVMProvider provider = VMHandlerUtils.getActiveVMProvider(fServiceLocator);

        // If no part or selection, disable all.
        if (provider == null || !(provider instanceof ICachingVMProvider)) {
            return NO_UPDATE_POLICIES_CONTRIBUTION_ITEMS;
        }
        ICachingVMProvider cachingProvider = (ICachingVMProvider)provider;
        
        IVMUpdatePolicy[] policies = cachingProvider.getAvailableUpdatePolicies();
        IVMUpdatePolicy activePolicy = cachingProvider.getActiveUpdatePolicy();
        
        List<Action> actions = new ArrayList<Action>(policies.length);
        for (IVMUpdatePolicy policy : policies) {
            Action action = new SelectUpdatePolicyAction(cachingProvider, policy);
            if (policy.getID().equals(activePolicy.getID())) {
                action.setChecked(true);
            }
            actions.add(action);
        }
        
        if ( actions.isEmpty() ) {
            return NO_UPDATE_POLICIES_CONTRIBUTION_ITEMS;
        }
        
        IContributionItem[] items = new IContributionItem[actions.size()];
        for (int i = 0; i < actions.size(); i++) {
            items[i] = new ActionContributionItem(actions.get(i));
        }
        return items;
    }
    
    public void initialize(IServiceLocator serviceLocator) {
        fServiceLocator = serviceLocator;
    }
}
