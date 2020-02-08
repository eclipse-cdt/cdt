/*******************************************************************************
 * Copyright (c) 2017-2018 Martin Weber.
 *
 * Content is provided to you under the terms and conditions of the Eclipse Public License Version 2.0 "EPL".
 * A copy of the EPL is available at http://www.eclipse.org/legal/epl-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.is.core.internal.ui;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.cdt.cmake.is.core.language.settings.providers.CompileCommandsJsonParser;
import org.eclipse.cdt.ui.language.settings.providers.AbstractLanguageSettingProviderOptionPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Option page for CompileCommandsJsonParse
 *
 * @author Martin Weber
 */
public class CompileCommandsJsonParserOptionPage extends AbstractLanguageSettingProviderOptionPage {

	private Text pattern;
	private Button b_versionsEnabled;

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		// normally should be handled by LanguageSettingsProviderTab
		final String text = pattern.getText();
		try {
			Pattern.compile(text);
		} catch (PatternSyntaxException ex) {
			// BUG in CDT: core exceptions thrown here are not visible to users. CDT-WTF
			// IStatus status = new Status(IStatus.ERROR, Plugin.PLUGIN_ID,
			// IStatus.OK,
			// "invalid suffix pattern in CMAKE_EXPORT_COMPILE_COMMANDS Parser", ex);
			// throw new CoreException(status);

			throw new PatternSyntaxException(
					"Invalid suffix pattern in CMAKE_EXPORT_COMPILE_COMMANDS Parser:\n" + ex.getDescription(),
					ex.getPattern(), ex.getIndex());
		}
	}

	@Override
	public void performDefaults() {
		// normally should be handled by LanguageSettingsProviderTab
		b_versionsEnabled.setSelection(false);
	}

	@Override
	public void createControl(Composite parent) {
		final boolean enabled = parent.isEnabled();
		final CompileCommandsJsonParser provider = (CompileCommandsJsonParser) getProvider();

		final Composite composite = new Composite(parent, SWT.NONE);
		{
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			layout.marginWidth = 1;
			layout.marginHeight = 1;
			layout.marginRight = 1;
			composite.setLayout(layout);
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		}

		final Group gr = createGroup(composite, SWT.FILL, 2, "For compilers with version in name", 2);

		b_versionsEnabled = createCheckbox(gr, SWT.BEGINNING, 2, "&Also try with version suffix");
		b_versionsEnabled.setToolTipText("Can recognize gcc-12.9.2, clang++-7.5.4, ...");
		b_versionsEnabled.setEnabled(enabled);
		b_versionsEnabled.setSelection(provider.isVersionPatternEnabled());
		{
			Label label = new Label(gr, SWT.NONE);
			label.setEnabled(enabled);
			label.setText("&Suffix pattern:");
			GridData gd = new GridData(SWT.BEGINNING);
			gd.horizontalSpan = 1;
			label.setLayoutData(gd);
		}

		pattern = new Text(gr, SWT.SINGLE | SWT.BORDER);
		pattern.setToolTipText("Specify a Java regular expression pattern here");
		pattern.setEnabled(enabled && b_versionsEnabled.getSelection());
		final String compilerPattern = provider.getVersionPattern();
		pattern.setText(compilerPattern != null ? compilerPattern : "");
		{
			GridData gd = new GridData();
			gr.setLayoutData(gd);
			gd.horizontalSpan = 1;
			gd.grabExcessHorizontalSpace = true;
			gd.horizontalAlignment = SWT.FILL;
			pattern.setLayoutData(gd);
		}
		pattern.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				final String text = ((Text) e.widget).getText();
				provider.setVersionPattern(text);
			}
		});
		pattern.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				final String text = pattern.getText();
				try {
					Pattern.compile(text);
//          provider.setVersionPattern(text); has no effect here
				} catch (PatternSyntaxException ex) {
					// swallow exception here, but re-check with error dialog in performApply(),
					// since
					// provider.setVersionPattern(() has no effect when called in performApply()-
					// CDT-WTF
				}
			}
		});

		// to adjust sensitivity...
		b_versionsEnabled.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				boolean selected = ((Button) event.widget).getSelection();
				provider.setVersionPatternEnabled(selected);
				pattern.setEnabled(selected);
			}
		});

		setControl(composite);
	}

	/**
	 * Creates a checkbox button.
	 *
	 * @param parent
	 * @param horizontalAlignment how control will be positioned horizontally within
	 *                            a cell of the parent's grid layout, one of:
	 *                            SWT.BEGINNING (or SWT.LEFT), SWT.CENTER, SWT.END
	 *                            (or SWT.RIGHT), or SWT.FILL
	 * @param horizontalSpan      number of column cells in the parent's grid layout
	 *                            that the control will take up.
	 * @param text                text to display on the checkbox
	 */
	static Button createCheckbox(Composite parent, int horizontalAlignment, int horizontalSpan, String text) {
		Button b = new Button(parent, SWT.CHECK);
		b.setText(text);
		GridData gd = new GridData(horizontalAlignment, SWT.CENTER, false, false);
		gd.horizontalSpan = horizontalSpan;
		b.setLayoutData(gd);
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
		GridData gd = new GridData(horizontalAlignment, SWT.CENTER, true, false);
		gd.horizontalSpan = horizontalSpan;
		gr.setLayoutData(gd);
		return gr;
	}
}
