/*******************************************************************************
 * Copyright (c) 2023 ArSysOp.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov (ArSysOp) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.compilationdatabase;

import org.eclipse.cdt.managedbuilder.internal.core.CommonBuilder;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIPlugin;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.OsgiPreferenceMetadataStore;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public final class PreferenceConfigurationArea extends ConfigurationArea {

	private final Button generateCDBFileButton;
	private final String ENABLE_FILE_GENERATION = CommonBuilder.COMPILATION_DATABASE_ENABLEMENT;
	private IPreferenceStore preferenceStore;

	public PreferenceConfigurationArea(Composite parent, PreferencesMetadata metadata, boolean isProjectScope) {
		super(1);
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setLayout(GridLayoutFactory.fillDefaults().numColumns(columns).create());
		this.generateCDBFileButton = createButton(metadata.generateCDBFile(), composite, SWT.CHECK, 0);
		preferenceStore = ManagedBuilderUIPlugin.getDefault().getPreferenceStore();
	}

	@Override
	public void load(Object options, boolean enable) {
		if (options instanceof PreferenceOptions editorOptions) {
			if (generateCDBFileButton != null) {
				generateCDBFileButton.setSelection(editorOptions.generateCDB());
				generateCDBFileButton.setEnabled(enable);
			}
		}

	}

	@Override
	public void store(IEclipsePreferences prefs) {
		OsgiPreferenceMetadataStore store = new OsgiPreferenceMetadataStore(prefs);
		buttons.entrySet().forEach(e -> store.save(e.getValue().getSelection(), e.getKey()));
		preferenceStore.setValue(ENABLE_FILE_GENERATION, generateCDBFileButton.getSelection());
	}

}
