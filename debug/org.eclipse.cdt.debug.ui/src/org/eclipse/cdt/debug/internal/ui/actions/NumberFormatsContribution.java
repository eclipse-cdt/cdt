/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.CVariableFormat;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

/**
 * Dynamic menu contribution that shows available number formats 
 * in the current view.
 * 
 * @since 6.0
 */
public class NumberFormatsContribution extends CompoundContributionItem implements IWorkbenchContribution {
	static final String CURRENT_FORMAT = "current_format";

	private static final Map<CVariableFormat, String> FORMATS = new LinkedHashMap<CVariableFormat, String>();
	static {
		FORMATS.put(CVariableFormat.NATURAL, "Natural");
		FORMATS.put(CVariableFormat.DECIMAL, "Decimal");
		FORMATS.put(CVariableFormat.HEXADECIMAL, "Hexadecimal");
		//FORMATS.put(CVariableFormat.OCTAL, "Octal");
		FORMATS.put(CVariableFormat.BINARY, "Binary");
	}

	private class SelectNumberFormatAction extends Action {
		private final CVariableFormat fFormat;
		private VariableFormatActionDelegate delegate;

		public VariableFormatActionDelegate getDelegate() {
			return delegate;
		}

		SelectNumberFormatAction(CVariableFormat format) {
			super(FORMATS.get(format), AS_RADIO_BUTTON);

			fFormat = format;
			delegate = new VariableFormatActionDelegate(fFormat);
		}

		void selectionChanged(ISelection sel) {
			delegate.selectionChanged(this, sel);
		}

		@Override
		public void run() {
			if (isChecked()) {
				delegate.run(this);
			}
		}
	}

	private IServiceLocator fServiceLocator;

	private static IContributionItem[] NO_ITEMS = new IContributionItem[] { new ContributionItem() {
		@Override
		public void fill(Menu menu, int index) {
			MenuItem item = new MenuItem(menu, SWT.NONE);
			item.setEnabled(false);
			item.setText("Empty");
		}

		@Override
		public boolean isEnabled() {
			return false;
		}
	} };

	@Override
	protected IContributionItem[] getContributionItems() {
		ISelectionService service = (ISelectionService) fServiceLocator.getService(ISelectionService.class);
		ISelection selection = service.getSelection();


		List<Action> actions = new ArrayList<Action>(FORMATS.size());

		for (CVariableFormat formatId : FORMATS.keySet()) {
			SelectNumberFormatAction action = new SelectNumberFormatAction(formatId);
			action.selectionChanged(selection);
			actions.add(action);
		}

		if (actions.isEmpty()) { return NO_ITEMS; }

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
