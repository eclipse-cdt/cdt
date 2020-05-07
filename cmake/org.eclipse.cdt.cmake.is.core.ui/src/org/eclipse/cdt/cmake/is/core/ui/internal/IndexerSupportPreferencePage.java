/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.ui.internal;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.cdt.cmake.is.core.language.settings.providers.PreferenceConstants;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Preference page for indexer support.
 */
public class IndexerSupportPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Text pattern;
	private Button btnVersionsEnabled;
	private Button btnWithConsole;

	public IndexerSupportPreferencePage() {
		setDescription("Configure how macros and include paths get extracted from the compile_commands.json file");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI
	 * blocks needed to manipulate various types of preferences. Each field editor
	 * knows how to save and restore itself.
	 */
	@Override
	protected Control createContents(Composite parent) {
		IEclipsePreferences preferences = InstanceScope.INSTANCE.getNode("org.eclipse.cdt.cmake.is.core"); //$NON-NLS-1$
		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridDataFactory.swtDefaults().applyTo(composite);

		final Group gr = createGroup(composite, SWT.FILL, 1, "For compilers with version in name", 2);
		btnVersionsEnabled = createCheckbox(gr, SWT.BEGINNING, 2, "&Also try with version suffix");
		btnVersionsEnabled.setToolTipText("Can recognize gcc-12.9.2, clang++-7.5.4, ...");
		// TODO use OptionMetadata based implementation
		btnVersionsEnabled.setSelection(preferences.getBoolean(PreferenceConstants.P_PATTERN_ENABLED, false));
		{
			Label label = new Label(gr, SWT.NONE);
			label.setText("&Suffix pattern:");
			GridDataFactory.defaultsFor(label).applyTo(label);
		}

		pattern = new Text(gr, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.defaultsFor(pattern).applyTo(pattern);
		pattern.setToolTipText("Specify a Java regular expression pattern here");
		pattern.setEnabled(btnVersionsEnabled.getSelection());
		// TODO use OptionMetadata based implementation
		pattern.setText(preferences.get(PreferenceConstants.P_PATTERN, "-?\\d+(\\.\\d+)*"));
		pattern.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (btnVersionsEnabled.getSelection()) {
					final String text = pattern.getText();
					try {
						Pattern.compile(text);
						setErrorMessage(null);
					} catch (PatternSyntaxException ex) {
						String msg = String.format("Suffix pattern regular expression: %1$s in '%2$s' at index %3$d",
								ex.getDescription(), ex.getPattern(), ex.getIndex());
						setErrorMessage(msg);
					}
				}
			}
		});

		// to adjust sensitivity...
		btnVersionsEnabled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				boolean selected = ((Button) event.widget).getSelection();
				pattern.setEnabled(selected);
				if (!selected) {
					setErrorMessage(null);
				}
			}
		});

		btnWithConsole = createCheckbox(composite, SWT.BEGINNING, 1,
				"&Show output of compiler built-in detection in a console in the Console View");
		// TODO use OptionMetadata based implementation
		btnWithConsole.setSelection(preferences.getBoolean(PreferenceConstants.P_WITH_CONSOLE, false));

		return composite;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void performDefaults() {
		// TODO		IEclipsePreferences preferenceStore = InstanceScope.INSTANCE.getNode("org.eclipse.cdt.cmake.is.core"); //$NON-NLS-1$

		IPreferenceStore preferenceStore = getPreferenceStore();
		btnVersionsEnabled.setSelection(preferenceStore.getDefaultBoolean(PreferenceConstants.P_PATTERN_ENABLED));
		pattern.setText(preferenceStore.getDefaultString(PreferenceConstants.P_PATTERN));
		btnWithConsole.setSelection(preferenceStore.getDefaultBoolean(PreferenceConstants.P_WITH_CONSOLE));
		setErrorMessage(null);
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		// TODO IPreferenceStore preferenceStore = getPreferenceStore();
		IEclipsePreferences preferenceStore = InstanceScope.INSTANCE.getNode("org.eclipse.cdt.cmake.is.core"); //$NON-NLS-1$
		preferenceStore.putBoolean(PreferenceConstants.P_PATTERN_ENABLED, btnVersionsEnabled.getSelection());
		preferenceStore.put(PreferenceConstants.P_PATTERN, pattern.getText());
		preferenceStore.putBoolean(PreferenceConstants.P_WITH_CONSOLE, btnWithConsole.getSelection());
		return true;
	}

	/**
	 * Creates a check-box button.
	 *
	 * @param parent
	 * @param horizontalAlignment how control will be positioned horizontally within
	 *                            a cell of the parent's grid layout, one of:
	 *                            SWT.BEGINNING (or SWT.LEFT), SWT.CENTER, SWT.END
	 *                            (or SWT.RIGHT), or SWT.FILL
	 * @param horizontalSpan      number of column cells in the parent's grid layout
	 *                            that the control will take up.
	 * @param text                text to display on the check-box
	 */
	private static Button createCheckbox(Composite parent, int horizontalAlignment, int horizontalSpan, String text) {
		Button b = new Button(parent, SWT.CHECK);
		b.setText(text);
		GridDataFactory.defaultsFor(b).align(horizontalAlignment, SWT.CENTER).span(horizontalSpan, 1).grab(true, false)
				.applyTo(b);
		return b;
	}

	/**
	 * Creates a group with a grid layout.
	 *
	 * @param parent
	 * @param horizontalAlignment how control will be positioned horizontally within
	 *                            a cell of the parent's grid layout, one of:
	 *                            SWT.BEGINNING (or SWT.LEFT), SWT.CENTER, SWT.END
	 *                            (or SWT.RIGHT), or SWT.FILL
	 * @param horizontalSpan      number of column cells in the parent's grid layout
	 *                            that the control will take up.
	 * @param text                title text to display on the group
	 * @param numColumns          the number of columns in the grid inside the group
	 */
	private static Group createGroup(Composite parent, int horizontalAlignment, int horizontalSpan, String text,
			int numColumns) {
		Group gr = new Group(parent, SWT.NONE);
		gr.setLayout(new GridLayout(numColumns, false));
		gr.setText(text);
		GridDataFactory.defaultsFor(gr).align(horizontalAlignment, SWT.CENTER).span(horizontalSpan, 1).grab(true, false)
				.applyTo(gr);
		return gr;
	}
}