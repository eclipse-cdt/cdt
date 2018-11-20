/*******************************************************************************
 * Copyright (c) 2000, 2014 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Sergey Prigogin (Google)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.preferences.PreferencesAccess;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

/**
 * Allows to choose the formatter in a combo box.
 * If no formatter is contributed, nothing is shown.
 */
public class CustomCodeFormatterBlock extends Observable {
	private final Map<String, String> idMap = new HashMap<>();
	private IEclipsePreferences fPrefs;
	private String fDefaultFormatterId;
	private Combo fFormatterCombo;
	private static final String ATTR_NAME = "name"; //$NON-NLS-1$
	private static final String ATTR_ID = "id"; //$NON-NLS-1$
	private static final String DEFAULT = FormatterMessages.CustomCodeFormatterBlock_default_formatter;

	public CustomCodeFormatterBlock(IProject project, PreferencesAccess access) {
		final IScopeContext scope;
		final IEclipsePreferences defaults;
		if (project != null) {
			scope = access.getProjectScope(project);
			defaults = access.getInstanceScope().getNode(CCorePlugin.PLUGIN_ID);
		} else {
			scope = access.getInstanceScope();
			defaults = access.getDefaultScope().getNode(CCorePlugin.PLUGIN_ID);
		}
		fPrefs = scope.getNode(CCorePlugin.PLUGIN_ID);
		fDefaultFormatterId = defaults.get(CCorePreferenceConstants.CODE_FORMATTER, null);
		if (fDefaultFormatterId == null) {
			// Backward compatibility: use UI prefs
			IEclipsePreferences instance = access.getInstanceScope().getNode(CUIPlugin.PLUGIN_ID);
			fDefaultFormatterId = instance.get(CCorePreferenceConstants.CODE_FORMATTER, null);
			if (fDefaultFormatterId != null) {
				instance.remove(CCorePreferenceConstants.CODE_FORMATTER);
				if (project != null) {
					defaults.put(CCorePreferenceConstants.CODE_FORMATTER, fDefaultFormatterId);
				}
			}
		}
		initializeFormatters();
	}

	public void performOk() {
		if (fFormatterCombo == null) {
			return;
		}
		String text = fFormatterCombo.getText();
		String formatterId = idMap.get(text);
		if (formatterId != null && !formatterId.equals(fDefaultFormatterId)) {
			fPrefs.put(CCorePreferenceConstants.CODE_FORMATTER, formatterId);
		} else {
			// Simply reset to the default one.
			performDefaults();
		}
	}

	public void performDefaults() {
		fPrefs.remove(CCorePreferenceConstants.CODE_FORMATTER);

		if (fFormatterCombo == null) {
			return;
		}
		fFormatterCombo.clearSelection();
		String formatter = getFormatterById(fDefaultFormatterId);
		fFormatterCombo.setText(formatter);
		handleFormatterChanged();
	}

	public void enableProjectSpecificSettings(boolean useProjectSpecificSettings) {
		if (useProjectSpecificSettings) {
			if (fDefaultFormatterId != null)
				fPrefs.put(CCorePreferenceConstants.CODE_FORMATTER, fDefaultFormatterId);
		} else {
			initDefault();
		}
	}

	/**
	 * Returns the currently selected formatter id.
	 *
	 * @return the selected formatter id or {@code null} if the default is selected.
	 */
	public String getFormatterId() {
		if (fFormatterCombo == null) {
			return fPrefs.get(CCorePreferenceConstants.CODE_FORMATTER, fDefaultFormatterId);
		}
		return idMap.get(fFormatterCombo.getText());
	}

	public Control createContents(Composite parent) {
		if (idMap.size() == 1) {
			return parent; // No selector is needed since there is only one formatter.
		}
		Composite composite = ControlFactory.createGroup(parent,
				FormatterMessages.CustomCodeFormatterBlock_formatter_name, 1);
		((GridData) composite.getLayoutData()).horizontalSpan = 5;

		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, ICHelpContextIds.CODEFORMATTER_PREFERENCE_PAGE);

		fFormatterCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		fFormatterCombo.setFont(parent.getFont());
		fFormatterCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fFormatterCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleFormatterChanged();
			}
		});
		for (String item : idMap.keySet()) {
			fFormatterCombo.add(item);
		}

		final String noteTitle = FormatterMessages.CustomCodeFormatterBlock_formatter_note;
		final String noteMessage = FormatterMessages.CustomCodeFormatterBlock_contributed_formatter_warning;
		ControlFactory.createNoteComposite(JFaceResources.getDialogFont(), composite, noteTitle, noteMessage);

		initDefault();

		return composite;
	}

	private void handleFormatterChanged() {
		setChanged();
		String formatterId = getFormatterId();
		notifyObservers(formatterId);
	}

	private void initDefault() {
		if (fFormatterCombo == null) {
			return;
		}
		String formatterID = fPrefs.get(CCorePreferenceConstants.CODE_FORMATTER, fDefaultFormatterId);
		fFormatterCombo.setText(getFormatterById(formatterID));
	}

	private String getFormatterById(String formatterId) {
		String formatter = DEFAULT;
		if (formatterId != null) {
			for (Map.Entry<String, String> entry : idMap.entrySet()) {
				String val = entry.getValue();
				if (formatterId.equals(val)) {
					formatter = entry.getKey();
					break;
				}
			}
		}
		return formatter;
	}

	private void initializeFormatters() {
		idMap.clear();
		idMap.put(DEFAULT, CCorePreferenceConstants.DEFAULT_CODE_FORMATTER);
		IExtensionPoint point = Platform.getExtensionRegistry().getExtensionPoint(CCorePlugin.PLUGIN_ID,
				CCorePlugin.FORMATTER_EXTPOINT_ID);
		if (point != null) {
			IExtension[] exts = point.getExtensions();
			for (IExtension ext : exts) {
				IConfigurationElement[] elements = ext.getConfigurationElements();
				for (int j = 0; j < elements.length; ++j) {
					String name = elements[j].getAttribute(ATTR_NAME);
					String id = elements[j].getAttribute(ATTR_ID);
					idMap.put(name, id);
				}
			}
		}
	}
}
