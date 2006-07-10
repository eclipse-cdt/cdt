/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.ui.dialogs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;

import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.preferences.PreferencesMessages;

/**
 * 
 */
public class CodeFormatterBlock {

	private HashMap idMap = new HashMap();
	Preferences fPrefs;
	protected Combo fFormatterCombo;
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_ID="id"; //$NON-NLS-1$
	// This is a hack until we have a default Formatter.
	// For now it is comment out in the plugin.xml
	private static final String NONE=PreferencesMessages.getString("CodeFormatterPreferencePage.emptyName"); //$NON-NLS-1$


	public CodeFormatterBlock(Preferences prefs) {
		fPrefs = prefs;
		initializeFormatters();
	}

	public void performOk() {
		String text = fFormatterCombo.getText();
		String selection = (String)idMap.get(text);
		if (selection != null && selection.length() > 0) {
			HashMap options = CCorePlugin.getOptions();
			String formatterID = (String)options.get(CCorePreferenceConstants.CODE_FORMATTER);
			if (formatterID == null || !formatterID.equals(selection)) {
				options.put(CCorePreferenceConstants.CODE_FORMATTER, selection);
				CCorePlugin.setOptions(options);
			}
		} else {
			// simply reset to the default one.
			performDefaults();
		}
	}

	public void performDefaults() {
		HashMap optionsDefault = CCorePlugin.getDefaultOptions();
		HashMap options = CCorePlugin.getOptions();
		String formatterID = (String)optionsDefault.get(CCorePreferenceConstants.CODE_FORMATTER);
		options.put(CCorePreferenceConstants.CODE_FORMATTER, formatterID);
		CCorePlugin.setOptions(options);

		fFormatterCombo.clearSelection();
		fFormatterCombo.setText(NONE);
		Iterator iterator = idMap.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry entry = (Map.Entry)iterator.next();
			String val = (String)entry.getValue();
			if (val != null && val.equals(formatterID)) {
				fFormatterCombo.setText((String)entry.getKey());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public Control createControl(Composite parent) {
		Composite control = ControlFactory.createComposite(parent, 2);
		((GridLayout) control.getLayout()).makeColumnsEqualWidth = false;
		((GridLayout) control.getLayout()).marginWidth = 5;

		PlatformUI.getWorkbench().getHelpSystem().setHelp(control, ICHelpContextIds.CODEFORMATTER_PREFERENCE_PAGE);

		ControlFactory.createEmptySpace(control, 2);

		Label label = ControlFactory.createLabel(control, PreferencesMessages.getString("CodeFormatterPreferencePage.selectionName")); //$NON-NLS-1$
		label.setLayoutData(new GridData());
		fFormatterCombo = new Combo(control, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd = new GridData(GridData.GRAB_HORIZONTAL);
		gd.grabExcessHorizontalSpace = true;
		fFormatterCombo.setLayoutData(gd);
		fFormatterCombo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleFormatterChanged();
			}
		});
		Iterator items = idMap.keySet().iterator();
		while (items.hasNext()) {
			fFormatterCombo.add((String) items.next());
		}

		initDefault();
		handleFormatterChanged();
		return control;
	}

	public void handleFormatterChanged() {	
		// TODO: UI part.
	}

	public void initDefault() {
		boolean init = false;
		String selection = CCorePlugin.getOption(CCorePreferenceConstants.CODE_FORMATTER);
		if (selection != null) {
			Iterator iterator = idMap.entrySet().iterator();
			while (iterator.hasNext()) {
				Map.Entry entry = (Map.Entry)iterator.next();
				String val = (String)entry.getValue();
				if (val != null && val.equals(selection)) {
					fFormatterCombo.setText((String)entry.getKey());
					init = true;
				}
			}
		}
		if (!init) {
			fFormatterCombo.setText(NONE);
		}
	}

	private void initializeFormatters() {
		idMap = new HashMap();
		idMap.put(NONE, null);
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID, CCorePlugin.FORMATTER_EXTPOINT_ID);
		if (point != null) {
			IExtension[] exts = point.getExtensions();
			for (int i = 0; i < exts.length; i++) {
		 		IConfigurationElement[] elements = exts[i].getConfigurationElements();
		 		for (int j = 0; j < elements.length; ++j) {
		 			String name = elements[j].getAttribute(ATTR_NAME);
		 			idMap.put(name, elements[j].getAttribute(ATTR_ID));
		 		}
			}
		}
	}

}
