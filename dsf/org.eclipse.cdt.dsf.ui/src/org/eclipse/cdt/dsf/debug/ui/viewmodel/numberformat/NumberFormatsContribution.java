/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Winnie Lai (Texas Instruments) - Individual Element Number Format (Bug 202556)
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.numberformat;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.cdt.dsf.debug.service.IFormattedValues;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.IDebugVMConstants;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.actions.VMHandlerUtils;
import org.eclipse.cdt.dsf.ui.viewmodel.IVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
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
 * Dynamic menu contribution that shows available number formats 
 * in the current view.
 * 
 * @since 1.1
 */
public class NumberFormatsContribution extends CompoundContributionItem implements IWorkbenchContribution {
    
    protected static final List<String> FORMATS = new LinkedList<String>(); 
    static {
        FORMATS.add(IFormattedValues.NATURAL_FORMAT);
        FORMATS.add(IFormattedValues.HEX_FORMAT);
        FORMATS.add(IFormattedValues.DECIMAL_FORMAT);
        FORMATS.add(IFormattedValues.OCTAL_FORMAT);
        FORMATS.add(IFormattedValues.BINARY_FORMAT);
        FORMATS.add(IFormattedValues.STRING_FORMAT);
    }
    
    private class SelectNumberFormatAction extends Action {
        private final IPresentationContext fContext;
        private final String fFormatId;
        SelectNumberFormatAction(IPresentationContext context, String formatId) {
            super(FormattedValueVMUtil.getFormatLabel(formatId), AS_RADIO_BUTTON);
            fContext = context;
            fFormatId = formatId;
        }

        @Override
        public void run() {
            if (isChecked()) {
                fContext.setProperty(IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE, fFormatId);
            }
        }
    }
 
    protected IServiceLocator fServiceLocator;

    private static IContributionItem[] NO_BREAKPOINT_TYPES_CONTRIBUTION_ITEMS = new IContributionItem[] { 
    	new ContributionItem() {
            @Override
			public void fill(Menu menu, int index) {
				MenuItem item = new MenuItem(menu, SWT.NONE);
				item.setEnabled(false);
				item.setText(MessagesForNumberFormat.NumberFormatContribution_EmptyFormatsList_label);
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
        if (provider == null) {
            return NO_BREAKPOINT_TYPES_CONTRIBUTION_ITEMS;
        }
        
        IPresentationContext context = provider.getPresentationContext(); 
        Object activeId = context.getProperty(IDebugVMConstants.PROP_FORMATTED_VALUE_FORMAT_PREFERENCE);
        if (activeId == null) {
            activeId = IFormattedValues.NATURAL_FORMAT;
        }
        
        List<Action> actions = new ArrayList<Action>(FORMATS.size());
        for (String formatId : FORMATS) {
            Action action = new SelectNumberFormatAction(context, formatId);
            if (formatId.equals(activeId)) {
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
    
    @Override
	public void initialize(IServiceLocator serviceLocator) {
        fServiceLocator = serviceLocator;
    }
}
