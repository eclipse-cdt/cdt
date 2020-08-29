/*******************************************************************************
 * Copyright (c) 2020 ArSysOp and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.ui.properties;

import java.util.Optional;

import org.eclipse.cdt.lsp.core.Activator;
import org.eclipse.cdt.lsp.core.preferences.LanguageServerPreferenceMetadata;
import org.eclipse.cdt.lsp.internal.core.preferences.LanguageServerDefaults;
import org.eclipse.cdt.lsp.internal.ui.LspUiMessages;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.framework.FrameworkUtil;
import org.osgi.service.prefs.BackingStoreException;

public final class LanguageServerPropertyPage extends PropertyPage {

	private LanguageServerPreferenceMetadata metadata;
	private Button prefer;

	public LanguageServerPropertyPage() {
		this.metadata = new LanguageServerDefaults();
	}

	private void addHeaderSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);
		Label warning = new Label(composite, SWT.NONE);
		warning.setText(LspUiMessages.LanguageServerPropertyPage_w_ls_experimental);
	}

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	private void addSettingsSection(Composite parent) {
		PreferenceMetadata<Boolean> option = metadata.preferLanguageServer();
		Composite composite = createDefaultComposite(parent);
		prefer = new Button(composite, SWT.CHECK);
		prefer.setLayoutData(new GridData());
		prefer.setText(option.name());
		prefer.setToolTipText(option.description());
	}

	private void load() {
		Optional<IProject> project = project();
		PreferenceMetadata<Boolean> option = metadata.preferLanguageServer();
		if (project.isPresent()) {
			prefer.setSelection(Platform.getPreferencesService().getBoolean(Activator.PLUGIN_ID, option.identifer(),
					option.defaultValue(), new IScopeContext[] { new ProjectScope(project.get()) }));
		} else {
			prefer.setSelection(option.defaultValue());
		}
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);
		addHeaderSection(composite);
		addSeparator(composite);
		addSettingsSection(composite);
		load();
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);
		composite.setLayoutData(GridDataFactory.fillDefaults().create());
		return composite;
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		prefer.setSelection(metadata.preferLanguageServer().defaultValue());
	}

	@Override
	public boolean performOk() {
		Optional<IProject> project = project();
		if (project.isPresent()) {
			IEclipsePreferences node = new ProjectScope(project.get()).getNode(Activator.PLUGIN_ID);
			node.putBoolean(metadata.preferLanguageServer().identifer(), prefer.getSelection());
			try {
				node.flush();
				return true;
			} catch (BackingStoreException e) {
				Platform.getLog(FrameworkUtil.getBundle(getClass())).error(e.getMessage(), e);
			}
		}
		return false;
	}

	private Optional<IProject> project() {
		return Optional.ofNullable(getElement())//
				.filter(IProject.class::isInstance)//
				.map(IProject.class::cast);
	}

}