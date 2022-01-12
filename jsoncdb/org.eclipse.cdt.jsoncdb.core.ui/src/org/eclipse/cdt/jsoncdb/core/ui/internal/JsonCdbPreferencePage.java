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
package org.eclipse.cdt.jsoncdb.core.ui.internal;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.cdt.jsoncdb.core.IParserPreferences;
import org.eclipse.cdt.jsoncdb.core.IParserPreferencesAccess;
import org.eclipse.cdt.jsoncdb.core.IParserPreferencesMetadata;
import org.eclipse.core.runtime.preferences.PreferenceMetadata;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.jface.layout.GridDataFactory;
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
import org.osgi.framework.FrameworkUtil;

/**
 * Preference page for JSON Compilation Database Parser.
 */
public class JsonCdbPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Text pattern;
	private Button btnVersionsEnabled;
	private Button btnWithConsole;
	private final IParserPreferencesAccess prefsAccess;

	public JsonCdbPreferencePage() {
		prefsAccess = EclipseContextFactory.getServiceContext(FrameworkUtil.getBundle(getClass()).getBundleContext())
				.get(IParserPreferencesAccess.class);
		setDescription(Messages.JsonCdbPreferencePage_description);
	}

	/**
	 * Creates the field editors.
	 */
	@Override
	protected Control createContents(Composite parent) {
		final IParserPreferencesMetadata prefsMeta = prefsAccess.metadata();
		final IParserPreferences prefs = prefsAccess.getWorkspacePreferences();

		final Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		GridDataFactory.swtDefaults().applyTo(composite);

		final Group gr = createGroup(composite, SWT.FILL, 1, Messages.JsonCdbPreferencePage_label_version_suffix_group,
				2);
		btnVersionsEnabled = createCheckbox(gr, SWT.BEGINNING, 2, prefsMeta.tryVersionSuffix());
		btnVersionsEnabled.setSelection(prefs.getTryVersionSuffix());
		{
			Label label = new Label(gr, SWT.NONE);
			label.setText(Messages.JsonCdbPreferencePage_label_suffix_pattern);
			GridDataFactory.defaultsFor(label).applyTo(label);
		}

		pattern = new Text(gr, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.defaultsFor(pattern).applyTo(pattern);
		pattern.setToolTipText(prefsMeta.versionSuffixPattern().description());
		pattern.setEnabled(btnVersionsEnabled.getSelection());
		pattern.setText(prefs.getVersionSuffixPattern());
		pattern.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				if (btnVersionsEnabled.getSelection()) {
					final String text = pattern.getText();
					try {
						Pattern.compile(text);
						setErrorMessage(null);
					} catch (PatternSyntaxException ex) {
						String msg = String.format(Messages.JsonCdbPreferencePage_errmsg_suffix_regex,
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

		btnWithConsole = createCheckbox(composite, SWT.BEGINNING, 1, prefsMeta.allocateConsole());
		btnWithConsole.setSelection(prefs.getAllocateConsole());

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
		final IParserPreferencesMetadata prefsMeta = prefsAccess.metadata();
		btnVersionsEnabled.setSelection(prefsMeta.tryVersionSuffix().defaultValue());
		pattern.setText(prefsMeta.versionSuffixPattern().defaultValue());
		btnWithConsole.setSelection(prefsMeta.allocateConsole().defaultValue());
		setErrorMessage(null);
		super.performDefaults();
	}

	@Override
	public boolean performOk() {
		final IParserPreferences prefs = prefsAccess.getWorkspacePreferences();
		prefs.setTryVersionSuffix(btnVersionsEnabled.getSelection());
		prefs.setVersionSuffixPattern(pattern.getText());
		prefs.setAllocateConsole(btnWithConsole.getSelection());
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
	private static Button createCheckbox(Composite parent, int horizontalAlignment, int horizontalSpan,
			PreferenceMetadata<Boolean> option) {
		Button b = new Button(parent, SWT.CHECK);
		b.setText(option.name());
		b.setToolTipText(option.description());
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