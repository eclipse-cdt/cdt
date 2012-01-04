/*******************************************************************************
 * Copyright (c) 2008, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - Ted Williams - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.ui.memory;

import org.eclipse.cdt.dsf.debug.internal.provisional.model.IMemoryBlockUpdatePolicyProvider;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * 
 * @since 2.0  Moved to different package, exists since 1.0.
 */
public class SelectUpdatePolicyAction implements IMenuCreator, IViewActionDelegate, IDebugContextListener, IActionDelegate2 {

	private IAction fAction = null;
    private IMemoryBlock fMemoryBlock = null;
	
    @Override
	public void dispose() {
		// do nothing
		
	}

	@Override
	public void runWithEvent(IAction action, Event event) {
		// do nothing
	}

	class SelectPolicy extends Action {
        
		String fID;
		String fDescription;

        public SelectPolicy(String id, String description) {
          fID = id;
          fDescription = description;
        }    
		
        @Override
		public String getText() {
			return fDescription;
		}

		@Override
		public void run() {
            ((IMemoryBlockUpdatePolicyProvider) fMemoryBlock).setUpdatePolicy(fID);
        }
    
    }


    @Override
	public Menu getMenu(Control parent) {
        // Never called
        return null;
    }
    
    protected IAction getAction() { return fAction; }
    
    @Override
	public void init(IViewPart view) {
    }

    @Override
	public void init(IAction action) {
    	fAction = action;
        action.setMenuCreator(this);
    }
    
    @Override
	public void run(IAction action) {
        // Do nothing, this is a pull-down menu
    }

    @Override
	public void selectionChanged(IAction action, ISelection selection) {
    	fMemoryBlock = null;
    	action.setEnabled(false);
    	if(selection instanceof IStructuredSelection)
    	{
    		if(((IStructuredSelection) selection).getFirstElement() instanceof IMemoryBlock)
    		{
    			fMemoryBlock = (IMemoryBlock) ((IStructuredSelection) selection).getFirstElement();
    			action.setMenuCreator(this);
    			action.setEnabled(true);
    		}
    		else if(((IStructuredSelection) selection).getFirstElement() instanceof IMemoryRendering)
    		{
    			fMemoryBlock = ((IMemoryRendering) ((IStructuredSelection) selection).getFirstElement()).getMemoryBlock();
    			action.setMenuCreator(this);
    			action.setEnabled(true);
    		}
    	}
    }
    
    @Override
	public void debugContextChanged(DebugContextEvent event) {
    }
    
    @Override
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
    	if(fMemoryBlock instanceof IMemoryBlockUpdatePolicyProvider)
    	{
    		IMemoryBlockUpdatePolicyProvider blockPolicy = (IMemoryBlockUpdatePolicyProvider) fMemoryBlock;
    		
    		String currentPolicy = blockPolicy.getUpdatePolicy();
    		
    		String policies[] = blockPolicy.getUpdatePolicies();
    		for(int i = 0; i < policies.length; i++)
    		{
		    	SelectPolicy action = new SelectPolicy(policies[i], blockPolicy.getUpdatePolicyDescription(policies[i]));
		    	ActionContributionItem item = new ActionContributionItem(action);
		    	action.setChecked(policies[i].equals(currentPolicy));
		    	
		    	item.fill(menu, -1);
    		}
    	}
    }

}

