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

import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.update.provisional.ICachingVMProviderExtension;
import org.eclipse.cdt.dsf.debug.internal.ui.viewmodel.update.provisional.IVMUpdateScope;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.VMHandlerUtils;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
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
 * Dynamic menu contribution that shows available update scopes 
 * in the current view.
 * 
 * @since 1.1
 */
public class UpdateScopesContribution extends CompoundContributionItem implements IWorkbenchContribution {
    
    private class SelectUpdateScopeAction extends Action {
        private final ICachingVMProviderExtension fProvider;
        private final IVMUpdateScope fScope;
        SelectUpdateScopeAction(ICachingVMProviderExtension provider, IVMUpdateScope scope) {
            super(scope.getName(), AS_RADIO_BUTTON);
            fProvider = provider;
            fScope = scope;
        }

        @Override
        public void run() {
            if (isChecked()) {
                fProvider.setActiveUpdateScope(fScope);
            }
        }
    }
 
    private IServiceLocator fServiceLocator;

    private static IContributionItem[] NO_BREAKPOINT_TYPES_CONTRIBUTION_ITEMS = new IContributionItem[] { 
    	new ContributionItem() {
            @Override
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.NONE);
				item.setEnabled(false);
				item.setText(MessagesForVMActions.UpdateScopesContribution_EmptyScopesList_label);
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
        if (provider == null || !(provider instanceof ICachingVMProviderExtension)) {
            return NO_BREAKPOINT_TYPES_CONTRIBUTION_ITEMS;
        }
        ICachingVMProviderExtension cachingProvider = (ICachingVMProviderExtension)provider;
        
        IVMUpdateScope[] scopes = cachingProvider.getAvailableUpdateScopes();
        IVMUpdateScope activeScope = cachingProvider.getActiveUpdateScope();
        
        List<Action> actions = new ArrayList<Action>(scopes.length);
        for (IVMUpdateScope scope : scopes) {
            Action action = new SelectUpdateScopeAction(cachingProvider, scope);
            if (scope.getID().equals(activeScope.getID())) {
                action.setChecked(true);
            }
            actions.add(action);
        }
        
        if ( actions.isEmpty() ) {
            return NO_BREAKPOINT_TYPES_CONTRIBUTION_ITEMS;
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
