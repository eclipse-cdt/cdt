/********************************************************************************
 * Copyright (c) 2023, 2024 Renesas Electronics Corp. and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui.compilationdatabase;

import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIPlugin;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.ui.dialogs.ICOptionContainerExtension;
import org.eclipse.cdt.ui.dialogs.PreferenceScopeBlock;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

/**

 * @noextend This class is not intended to be subclasses by clients.
 * @since 9.5
 */
public class CompilationDatabaseGeneratorBlock extends AbstractCOptionPage {

	private static final String PREF_PAGE_ID = "org.eclipse.cdt.managedbuilder.internal.ui.compilationdatabase.JsonCdbGeneratorPreferencePage"; //$NON-NLS-1$
	private final String ENABLE_FILE_GENERATION = "generateFile"; //$NON-NLS-1$
	private Button generateFileCheckbox;
	private IPreferenceStore preferenceStore;
	private PreferenceScopeBlock fPrefScopeBlock;

	protected CompilationDatabaseGeneratorBlock() {
		preferenceStore = ManagedBuilderUIPlugin.getDefault().getPreferenceStore();
		performDefaults();
	}

	@Override
	public void createControl(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		if (getProject() != null || getContainer() instanceof ICOptionContainerExtension) {
			fPrefScopeBlock = new PreferenceScopeBlock(PREF_PAGE_ID) {
				@Override
				protected void onPreferenceScopeChange() {
					generateFileCheckbox.setEnabled(preferenceStore.getBoolean(ENABLE_FILE_GENERATION));
				}
			};
			fPrefScopeBlock.createControl(composite);
			fPrefScopeBlock.setInstanceScope();
		}
		Group cdbGeneratorOptions = new Group(composite, SWT.NONE);
		cdbGeneratorOptions.setLayout(new FillLayout(SWT.HORIZONTAL));
		cdbGeneratorOptions.setText(Messages.JsonCdbGeneratorPreferencePage_description);
		cdbGeneratorOptions.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		generateFileCheckbox = new Button(cdbGeneratorOptions, SWT.CHECK);
		generateFileCheckbox.setSelection(preferenceStore.getBoolean(ENABLE_FILE_GENERATION));
		generateFileCheckbox.setText(Messages.JsonCdbGeneratorPreferencePage_generateCompilationdatabase);
		generateFileCheckbox.addListener(SWT.Selection, e -> {
			boolean newValue = generateFileCheckbox.getSelection();
			preferenceStore.setValue(ENABLE_FILE_GENERATION, newValue);
		});

	}

	@Override
	public void performDefaults() {
		preferenceStore.setDefault(ENABLE_FILE_GENERATION, false);
	}

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		this.performApply(monitor);
	}

	private IProject getProject() {
		ICOptionContainer container = getContainer();
		if (container != null) {
			if (container instanceof ICOptionContainerExtension) {
				try {
					return ((ICOptionContainerExtension) container).getProjectHandle();
				} catch (Exception e) {
					ManagedBuilderUIPlugin.log(e);
				}
			}
			return container.getProject();
		}
		return null;
	}
}
