/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.numberformat;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.IDebugVMConstants;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.actions.VMCommandUtils;
import org.eclipse.dd.dsf.debug.service.IFormattedValues;
import org.eclipse.dd.dsf.ui.viewmodel.IVMProvider;
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
@SuppressWarnings("restriction")
public class NumberFormatsContribution extends CompoundContributionItem implements IWorkbenchContribution {
    
    private static final Map<String, String> FORMATS = new LinkedHashMap<String, String>(); 
    static {
        FORMATS.put(IFormattedValues.NATURAL_FORMAT, MessagesForNumberFormat.NumberFormatContribution_Natural_label);
        FORMATS.put(IFormattedValues.HEX_FORMAT, MessagesForNumberFormat.NumberFormatContribution_Hex_label);
        FORMATS.put(IFormattedValues.DECIMAL_FORMAT, MessagesForNumberFormat.NumberFormatContribution_Decimal_label);
        FORMATS.put(IFormattedValues.OCTAL_FORMAT, MessagesForNumberFormat.NumberFormatContribution_Octal_label);
        FORMATS.put(IFormattedValues.BINARY_FORMAT, MessagesForNumberFormat.NumberFormatContribution_Binary_label);
        FORMATS.put(IFormattedValues.STRING_FORMAT, MessagesForNumberFormat.NumberFormatContribution_String_label);
    }
    
    private class SelectNumberFormatAction extends Action {
        private final IPresentationContext fContext;
        private final String fFormatId;
        SelectNumberFormatAction(IPresentationContext context, String formatId) {
            super(FORMATS.get(formatId), AS_RADIO_BUTTON);
            fContext = context;
            fFormatId = formatId;
        }

        @Override
        public void run() {
            if (isChecked()) {
                fContext.setProperty(IDebugVMConstants.CURRENT_FORMAT_STORAGE, fFormatId);
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
        IVMProvider provider = VMCommandUtils.getActiveVMProvider(fServiceLocator);

        // If no part or selection, disable all.
        if (provider == null) {
            return NO_BREAKPOINT_TYPES_CONTRIBUTION_ITEMS;
        }
        
        IPresentationContext context = provider.getPresentationContext(); 
        Object activeId = context.getProperty(IDebugVMConstants.CURRENT_FORMAT_STORAGE);
        if (activeId == null) {
            activeId = IFormattedValues.NATURAL_FORMAT;
        }
        
        List<Action> actions = new ArrayList<Action>(FORMATS.size());
        for (String formatId : FORMATS.keySet()) {
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
    
    public void initialize(IServiceLocator serviceLocator) {
        fServiceLocator = serviceLocator;
    }
}
