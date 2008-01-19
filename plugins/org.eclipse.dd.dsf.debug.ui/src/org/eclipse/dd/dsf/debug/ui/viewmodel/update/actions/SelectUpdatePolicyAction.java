/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.ui.viewmodel.update.actions;

import org.eclipse.dd.dsf.debug.ui.viewmodel.actions.AbstractVMProviderActionDelegate;
import org.eclipse.dd.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.update.ICachingVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.update.IVMUpdatePolicy;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IViewPart;

/**
 * 
 */
public class SelectUpdatePolicyAction extends AbstractVMProviderActionDelegate implements IMenuCreator {

    
    class SelectPolicy extends Action {
        private ICachingVMProvider fVMProvider;
        private IVMUpdatePolicy fUpdatePolicy;
        
        @Override
        public void run() {
            if (isChecked()) {
                fVMProvider.setActiveUpdatePolicy(fUpdatePolicy);
            }
        }

        public SelectPolicy(ICachingVMProvider provider, IVMUpdatePolicy updatePolicy) {
            super(updatePolicy.getName(), IAction.AS_RADIO_BUTTON);
            fVMProvider = provider;
            fUpdatePolicy = updatePolicy;
        }        
    }


    public Menu getMenu(Control parent) {
        // Never called
        return null;
    }
    
    @Override
    public void init(IViewPart view) {
        super.init(view);
        getAction().setEnabled(getVMProvider() instanceof ICachingVMProvider);
    }

    @Override
    public void init(IAction action) {
        super.init(action);
        action.setMenuCreator(this);
    }
    
    public void run(IAction action) {
        // Do nothing, this is a pull-down menu
    }

    @Override
    public void selectionChanged(IAction action, ISelection selection) {
        if (action != getAction()) {
            action.setMenuCreator(this);
        }
        super.selectionChanged(action, selection);
        getAction().setEnabled(getVMProvider() instanceof ICachingVMProvider);
    }
    
    @Override
    public void debugContextChanged(DebugContextEvent event) {
        super.debugContextChanged(event);
        getAction().setEnabled(getVMProvider() instanceof ICachingVMProvider);
    }
    
    public Menu getMenu(Menu parent) {
        Menu menu = new Menu(parent);
        menu.addMenuListener(new MenuAdapter() {
            @Override
            public void menuShown(MenuEvent e) {
                Menu m = (Menu)e.widget;
                MenuItem[] items = m.getItems();
                for (int i=0; i < items.length; i++) {
                    items[i].dispose();
                }
                fillMenu(m);
            }
        });     
        return menu;
    }

    private void fillMenu(Menu menu) {
        
        IVMUpdatePolicy[] updatePolicies = new IVMUpdatePolicy[0];
        IVMUpdatePolicy activePolicy = null;
        IVMProvider provider = getVMProvider();
        if (provider instanceof ICachingVMProvider) {
            ICachingVMProvider cachingProvider = (ICachingVMProvider)provider;
            updatePolicies = cachingProvider.getAvailableUpdatePolicies();
            activePolicy = cachingProvider.getActiveUpdatePolicy();

            for (IVMUpdatePolicy updatePolicy : updatePolicies) {
                SelectPolicy action = new SelectPolicy(cachingProvider, updatePolicy);
                if (updatePolicy.equals(activePolicy)) {
                    action.setChecked(true);
                } 
                ActionContributionItem item = new ActionContributionItem(action);
                item.fill(menu, -1);
            }
        }
    }

}
