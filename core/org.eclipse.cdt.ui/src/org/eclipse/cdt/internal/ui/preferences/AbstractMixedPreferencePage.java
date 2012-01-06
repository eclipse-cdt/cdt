/*******************************************************************************
 * Copyright (c) 2011 Jens Elmenthaler and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Jens Elmenthaler - initial API and implementation
 *                       (http://bugs.eclipse.org/173458, camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import org.eclipse.cdt.core.CCorePlugin;

/**
 * A preference that on preference from the UI plugin, as well the CDT core.
 * 
 * Currently only supporting boolean preferences.
 */
public abstract class AbstractMixedPreferencePage extends AbstractPreferencePage {

	protected OverlayPreferenceStore corePrefsOverlayStore;

	private Map<Button, String> corePrefsCheckBoxes = new HashMap<Button, String>();
	private SelectionListener corePrefsCheckBoxListener = new SelectionListener() {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		@Override
		public void widgetSelected(SelectionEvent e) {
			Button button = (Button) e.widget;
			corePrefsOverlayStore.setValue(corePrefsCheckBoxes.get(button), button.getSelection());
		}
	};

	public AbstractMixedPreferencePage() {
		corePrefsOverlayStore = new OverlayPreferenceStore(new ScopedPreferenceStore(InstanceScope.INSTANCE,
				CCorePlugin.PLUGIN_ID), createCorePrefsOverlayStoreKeys());
	}

	protected Button addCorePrefsCheckBox(Composite parent, String label, String key, int indentation) {
		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(label);

		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = indentation;
		gd.horizontalSpan = 2;
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(corePrefsCheckBoxListener);

		corePrefsCheckBoxes.put(checkBox, key);

		return checkBox;
	}

	protected abstract OverlayPreferenceStore.OverlayKey[] createCorePrefsOverlayStoreKeys();
	
	@Override
	protected void initializeFields() {
		super.initializeFields();

		Iterator<Button> e = corePrefsCheckBoxes.keySet().iterator();
		while (e.hasNext()) {
			Button b = e.next();
			String key = corePrefsCheckBoxes.get(b);
			b.setSelection(corePrefsOverlayStore.getBoolean(key));
		}
	}
	
	@Override
	public boolean performOk() {
		corePrefsOverlayStore.propagate();
		return super.performOk();
	}
	
	@Override
	protected void performDefaults() {
		corePrefsOverlayStore.loadDefaults();
		super.performDefaults();
	}

	@Override
	public void dispose() {
		if (corePrefsOverlayStore != null) {
			corePrefsOverlayStore.stop();
			corePrefsOverlayStore = null;
		}
		super.dispose();
	}
}
