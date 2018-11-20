/*******************************************************************************
 * Copyright (c) 2011, 2012 Jens Elmenthaler and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Jens Elmenthaler - initial API and implementation
 *                       (http://bugs.eclipse.org/173458, camel case completion)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * A preference that on preference from the UI plugin, as well the CDT core.
 *
 * Currently only supporting boolean preferences.
 */
public abstract class AbstractMixedPreferencePage extends AbstractPreferencePage {

	protected OverlayPreferenceStore corePrefsOverlayStore;

	private Map<Button, String> corePrefsCheckBoxes = new HashMap<>();
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
		corePrefsOverlayStore = new OverlayPreferenceStore(CUIPlugin.getDefault().getCorePreferenceStore(),
				createCorePrefsOverlayStoreKeys());
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
