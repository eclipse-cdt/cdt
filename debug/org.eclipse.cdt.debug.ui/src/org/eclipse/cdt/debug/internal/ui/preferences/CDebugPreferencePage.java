/*******************************************************************************
 * Copyright (c) 2004, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Ken Ryall (Nokia) - 207675
 * Mathias Kunter - Support for different charsets (bug 370462)
 * Jonah Graham (Kichwa Coders) - Remove CDI
*******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.preferences;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.dialogs.EncodingFieldEditor;
import org.osgi.service.prefs.BackingStoreException;

/**
 * Preference page for debug preferences that apply specifically to C/C++
 * Debugging.
 */
public class CDebugPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private IWorkbench fWorkbench;

	private EncodingFieldEditor fCharsetEditor;

	private EncodingFieldEditor fWideCharsetEditor;

	private PropertyChangeListener fPropertyChangeListener;

	private Button fShowBinarySourceFilesButton;

	private Button fShowSourceNotFoundAllTime;
	private Button fShowSourceNotFoundOnlyCaseSourceNotFound;
	private Button fShowSourceNotFoundNever;

	protected class PropertyChangeListener implements IPropertyChangeListener {

		private boolean fHasStateChanged = false;

		@Override
		public void propertyChange(PropertyChangeEvent event) {
			if (event.getProperty().equals(ICDebugPreferenceConstants.PREF_SHOW_HEX_VALUES)) {
				fHasStateChanged = true;
			} else if (event.getProperty().equals(ICDebugPreferenceConstants.PREF_SHOW_CHAR_VALUES)) {
				fHasStateChanged = true;
			} else if (event.getProperty().equals(FieldEditor.VALUE)) {
				fHasStateChanged = true;
			} else if (event.getProperty().equals(FieldEditor.IS_VALID)) {
				setValid(fCharsetEditor.isValid() && fWideCharsetEditor.isValid());
				if (!fCharsetEditor.isValid()) {
					setErrorMessage(PreferenceMessages.getString("CDebugPreferencePage.19")); //$NON-NLS-1$
				} else if (!fWideCharsetEditor.isValid()) {
					setErrorMessage(PreferenceMessages.getString("CDebugPreferencePage.20")); //$NON-NLS-1$
				} else {
					setErrorMessage(null);
				}
			}

		}

		protected boolean hasStateChanged() {
			return fHasStateChanged;
		}
	}

	/**
	 * Constructor for CDebugPreferencePage.
	 */
	public CDebugPreferencePage() {
		super();
		setPreferenceStore(CDebugUIPlugin.getDefault().getPreferenceStore());
		getPreferenceStore().addPropertyChangeListener(getPropertyChangeListener());
		setDescription(PreferenceMessages.getString("CDebugPreferencePage.3")); //$NON-NLS-1$
	}

	@Override
	protected Control createContents(Composite parent) {
		getWorkbench().getHelpSystem().setHelp(getControl(), ICDebugHelpContextIds.C_DEBUG_PREFERENCE_PAGE);
		// The main composite
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		composite.setLayout(layout);
		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);
		createSpacer(composite, 1);
		createCharsetSettingPreferences(composite);
		createSpacer(composite, 1);
		createShowSourceNotFoundEditor(composite);
		createSpacer(composite, 1);
		createBinarySettings(composite);
		setValues();
		return composite;
	}

	/**
	 * Set the values of the component widgets based on the values in the
	 * preference store
	 */
	private void setValues() {
		// Set the charset editors.

		// Create a temporary preference store.
		PreferenceStore ps = new PreferenceStore();

		// Get the default charset and the default wide charset.
		String defaultCharset = DefaultScope.INSTANCE.getNode(CDebugCorePlugin.PLUGIN_ID)
				.get(ICDebugConstants.PREF_DEBUG_CHARSET, null);
		if (defaultCharset != null) {
			ps.setDefault(ICDebugConstants.PREF_DEBUG_CHARSET, defaultCharset);
		}
		String defaultWideCharset = DefaultScope.INSTANCE.getNode(CDebugCorePlugin.PLUGIN_ID)
				.get(ICDebugConstants.PREF_DEBUG_WIDE_CHARSET, null);
		if (defaultWideCharset != null) {
			ps.setDefault(ICDebugConstants.PREF_DEBUG_WIDE_CHARSET, defaultWideCharset);
		}

		/*
		 * Get the charset and the wide charset. If they're unset, use the
		 * default instead. Note that we have to call the setValue() function of
		 * the PreferenceStore even if we want to use the default. This is to
		 * ensure proper display of the encoding field editor.
		 */
		String charset = InstanceScope.INSTANCE.getNode(CDebugCorePlugin.PLUGIN_ID)
				.get(ICDebugConstants.PREF_DEBUG_CHARSET, null);
		if (charset != null) {
			ps.setValue(ICDebugConstants.PREF_DEBUG_CHARSET, charset);
		} else if (defaultCharset != null) {
			ps.setValue(ICDebugConstants.PREF_DEBUG_CHARSET, defaultCharset);
		}
		String wideCharset = InstanceScope.INSTANCE.getNode(CDebugCorePlugin.PLUGIN_ID)
				.get(ICDebugConstants.PREF_DEBUG_WIDE_CHARSET, null);
		if (wideCharset != null) {
			ps.setValue(ICDebugConstants.PREF_DEBUG_WIDE_CHARSET, wideCharset);
		} else if (defaultWideCharset != null) {
			ps.setValue(ICDebugConstants.PREF_DEBUG_WIDE_CHARSET, defaultWideCharset);
		}

		// Initialize the encoding field editors with the values from the
		// preference store.
		fCharsetEditor.setPreferenceStore(ps);
		fCharsetEditor.load();
		fWideCharsetEditor.setPreferenceStore(ps);
		fWideCharsetEditor.load();

		// Tell the encoding field editors to check the "Default" option if
		// we're currently using the default values.
		if (charset == null) {
			fCharsetEditor.loadDefault();
		}
		if (wideCharset == null) {
			fWideCharsetEditor.loadDefault();
		}

		// Set the values for the remaining preferences.
		fShowBinarySourceFilesButton.setSelection(Platform.getPreferencesService().getBoolean(CCorePlugin.PLUGIN_ID,
				CCorePreferenceConstants.SHOW_SOURCE_FILES_IN_BINARIES, true, null));

		// Set the values for show source not found editor
		String showEditor = Platform.getPreferencesService().getString(CCorePlugin.PLUGIN_ID,
				CCorePreferenceConstants.SHOW_SOURCE_NOT_FOUND_EDITOR,
				CCorePreferenceConstants.SHOW_SOURCE_NOT_FOUND_EDITOR_DEFAULT, null);
		setShowEditorButtons(showEditor);
	}

	@Override
	public void init(IWorkbench workbench) {
		fWorkbench = workbench;
	}

	protected PropertyChangeListener getPropertyChangeListener() {
		if (fPropertyChangeListener == null) {
			fPropertyChangeListener = new PropertyChangeListener();
		}
		return fPropertyChangeListener;
	}

	/**
	 * Set the default preferences for this page.
	 */
	public static void initDefaults(IPreferenceStore store) {
		store.setDefault(ICDebugPreferenceConstants.PREF_SHOW_HEX_VALUES, false);
	}

	@Override
	public void dispose() {
		super.dispose();
		getPreferenceStore().removePropertyChangeListener(getPropertyChangeListener());
	}

	private void createCharsetSettingPreferences(Composite parent) {
		// Create containing composite
		Composite formatComposite = ControlFactory.createComposite(parent, 2);
		((GridLayout) formatComposite.getLayout()).marginWidth = 0;
		((GridLayout) formatComposite.getLayout()).marginHeight = 0;

		// Create charset editor
		Composite charsetComposite = ControlFactory.createComposite(formatComposite, 1);
		fCharsetEditor = new EncodingFieldEditor(ICDebugConstants.PREF_DEBUG_CHARSET, "", //$NON-NLS-1$
				PreferenceMessages.getString("CDebugPreferencePage.18"), charsetComposite); //$NON-NLS-1$
		fCharsetEditor.setPropertyChangeListener(getPropertyChangeListener());

		// Create wide charset editor
		Composite wideCharsetComposite = ControlFactory.createComposite(formatComposite, 1);
		fWideCharsetEditor = new EncodingFieldEditor(ICDebugConstants.PREF_DEBUG_WIDE_CHARSET, "", //$NON-NLS-1$
				PreferenceMessages.getString("CDebugPreferencePage.16"), wideCharsetComposite); //$NON-NLS-1$
		fWideCharsetEditor.setPropertyChangeListener(getPropertyChangeListener());
	}

	private void createBinarySettings(Composite parent) {
		fShowBinarySourceFilesButton = createCheckButton(parent,
				PreferenceMessages.getString("CDebugPreferencePage.15")); //$NON-NLS-1$
	}

	private void createShowSourceNotFoundEditor(Composite composite) {
		Group buttonGroupForSourceNotFound = new Group(composite, SWT.SHADOW_ETCHED_IN);
		{
			GridLayout gridLayout = new GridLayout();
			gridLayout.numColumns = 1;
			buttonGroupForSourceNotFound.setLayout(gridLayout);
			buttonGroupForSourceNotFound.setText(PreferenceMessages.getString("CDebugPreferencePage.24")); //$NON-NLS-1$
			Label preferenceText = new Label(buttonGroupForSourceNotFound, SWT.READ_ONLY | SWT.WRAP);
			preferenceText.setText(PreferenceMessages.getString("CDebugPreferencePage.26")); //$NON-NLS-1$
		}

		{
			fShowSourceNotFoundAllTime = createRadioButton(buttonGroupForSourceNotFound,
					PreferenceMessages.getString("CDebugPreferencePage.22")); //$NON-NLS-1$
			fShowSourceNotFoundAllTime.setToolTipText(PreferenceMessages.getString("CDebugPreferencePage.25")); //$NON-NLS-1$
			fShowSourceNotFoundOnlyCaseSourceNotFound = createRadioButton(buttonGroupForSourceNotFound,
					PreferenceMessages.getString("CDebugPreferencePage.23")); //$NON-NLS-1$
			fShowSourceNotFoundNever = createRadioButton(buttonGroupForSourceNotFound,
					PreferenceMessages.getString("CDebugPreferencePage.21")); //$NON-NLS-1$
		}
	}

	/**
	 * Creates a button with the given label and sets the default configuration
	 * data.
	 */
	private Button createCheckButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.CHECK | SWT.LEFT);
		button.setText(label);
		// FieldEditor GridData
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}

	private Button createRadioButton(Composite parent, String label) {
		Button button = new Button(parent, SWT.RADIO | SWT.LEFT);
		button.setText(label);
		// FieldEditor GridData
		GridData data = new GridData();
		button.setLayoutData(data);
		return button;
	}

	protected void createSpacer(Composite composite, int columnSpan) {
		Label label = new Label(composite, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = columnSpan;
		label.setLayoutData(gd);
	}

	@Override
	public boolean performOk() {
		storeValues();
		if (getPropertyChangeListener().hasStateChanged()) {
			refreshViews();
		}

		try {
			InstanceScope.INSTANCE.getNode(CDebugUIPlugin.PLUGIN_ID).flush();
			InstanceScope.INSTANCE.getNode(CDebugCorePlugin.PLUGIN_ID).flush();
		} catch (BackingStoreException e) {
			// No operation
		}
		return true;
	}

	/**
	 * Refresh the variables and expression views as changes have occurred that
	 * affects these views.
	 */
	private void refreshViews() {
		BusyIndicator.showWhile(getShell().getDisplay(), () -> {
			// Refresh interested views
			IWorkbenchWindow[] windows = CDebugUIPlugin.getDefault().getWorkbench().getWorkbenchWindows();
			IWorkbenchPage page = null;
			for (int i = 0; i < windows.length; i++) {
				page = windows[i].getActivePage();
				if (page != null) {
					refreshViews(page, IDebugUIConstants.ID_EXPRESSION_VIEW);
					refreshViews(page, IDebugUIConstants.ID_VARIABLE_VIEW);
					refreshViews(page, IDebugUIConstants.ID_REGISTER_VIEW);
				}
			}
		});
	}

	/**
	 * Refresh all views in the given workbench page with the given view id
	 */
	protected void refreshViews(IWorkbenchPage page, String viewID) {
		IViewPart part = page.findView(viewID);
		if (part != null) {
			IDebugView adapter = part.getAdapter(IDebugView.class);
			if (adapter != null) {
				Viewer viewer = adapter.getViewer();
				if (viewer instanceof StructuredViewer) {
					((StructuredViewer) viewer).refresh();
				}
			}
		}
	}

	/**
	 * Store the preference values based on the state of the component widgets
	 */
	private void storeValues() {
		// Store the charset.
		if (fCharsetEditor.presentsDefaultValue()) {
			InstanceScope.INSTANCE.getNode(CDebugCorePlugin.PLUGIN_ID).remove(ICDebugConstants.PREF_DEBUG_CHARSET);
		} else {
			fCharsetEditor.store();
			InstanceScope.INSTANCE.getNode(CDebugCorePlugin.PLUGIN_ID).put(ICDebugConstants.PREF_DEBUG_CHARSET,
					fCharsetEditor.getPreferenceStore().getString(ICDebugConstants.PREF_DEBUG_CHARSET));
		}

		// Store the wide charset.
		if (fWideCharsetEditor.presentsDefaultValue()) {
			InstanceScope.INSTANCE.getNode(CDebugCorePlugin.PLUGIN_ID).remove(ICDebugConstants.PREF_DEBUG_WIDE_CHARSET);
		} else {
			fWideCharsetEditor.store();
			InstanceScope.INSTANCE.getNode(CDebugCorePlugin.PLUGIN_ID).put(ICDebugConstants.PREF_DEBUG_WIDE_CHARSET,
					fWideCharsetEditor.getPreferenceStore().getString(ICDebugConstants.PREF_DEBUG_WIDE_CHARSET));
		}

		// Store the other preferences.
		InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).putBoolean(
				CCorePreferenceConstants.SHOW_SOURCE_FILES_IN_BINARIES, fShowBinarySourceFilesButton.getSelection());

		// Store the show source file editor
		if (fShowSourceNotFoundAllTime.getSelection()) {
			InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).put(
					CCorePreferenceConstants.SHOW_SOURCE_NOT_FOUND_EDITOR,
					CCorePreferenceConstants.SHOW_SOURCE_NOT_FOUND_EDITOR_ALL_THE_TIME);
		}
		if (fShowSourceNotFoundOnlyCaseSourceNotFound.getSelection()) {
			InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).put(
					CCorePreferenceConstants.SHOW_SOURCE_NOT_FOUND_EDITOR,
					CCorePreferenceConstants.SHOW_SOURCE_NOT_FOUND_EDITOR_SOMETIMES);
		}
		if (fShowSourceNotFoundNever.getSelection()) {
			InstanceScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).put(
					CCorePreferenceConstants.SHOW_SOURCE_NOT_FOUND_EDITOR,
					CCorePreferenceConstants.SHOW_SOURCE_NOT_FOUND_EDITOR_NEVER);
		}
	}

	/**
	 * Sets the default preferences.
	 *
	 * @see PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		setDefaultValues();
		super.performDefaults();
	}

	private void setShowEditorButtons(String value) {
		switch (value) {
		case CCorePreferenceConstants.SHOW_SOURCE_NOT_FOUND_EDITOR_SOMETIMES:
			fShowSourceNotFoundOnlyCaseSourceNotFound.setSelection(true);
			break;
		case CCorePreferenceConstants.SHOW_SOURCE_NOT_FOUND_EDITOR_NEVER:
			fShowSourceNotFoundNever.setSelection(true);
			break;
		case CCorePreferenceConstants.SHOW_SOURCE_NOT_FOUND_EDITOR_ALL_THE_TIME:
		default:
			fShowSourceNotFoundAllTime.setSelection(true);
			break;
		}
	}

	private void setDefaultValues() {
		fCharsetEditor.loadDefault();
		fWideCharsetEditor.loadDefault();
		fShowBinarySourceFilesButton.setSelection(DefaultScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID)
				.getBoolean(CCorePreferenceConstants.SHOW_SOURCE_FILES_IN_BINARIES, true));
		String defaultSourceShow = DefaultScope.INSTANCE.getNode(CCorePlugin.PLUGIN_ID).get(
				CCorePreferenceConstants.SHOW_SOURCE_NOT_FOUND_EDITOR_DEFAULT,
				CCorePreferenceConstants.SHOW_SOURCE_NOT_FOUND_EDITOR_ALL_THE_TIME);
		setShowEditorButtons(defaultSourceShow);
	}

	private IWorkbench getWorkbench() {
		return fWorkbench;
	}
}
