/*******************************************************************************
 * Copyright (c) 2019 Marco Stornelli
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;

public class FormatterTagTabPage extends FormatterTabPage {

	private final String PREVIEW = createPreviewHeader(FormatterMessages.FormatterModifyDialog_offOn_preview_header)
			+ "void method1()   {  doSomething();  }\n\n// @formatter:off\n" //$NON-NLS-1$
			+ "void method2()   {  doSomething();  }\n// @formatter:on\n\n" //$NON-NLS-1$
			+ "void method3()   {  doSomething();  }\n\n" //$NON-NLS-1$
			+ "/* @formatter:off                                           */\n\nvoid\nfoo()\n;"; //$NON-NLS-1$

	private StringPreference fOnTag;
	private StringPreference fOffTag;
	private CheckboxPreference fUseTag;
	private TranslationUnitPreview fPreview;

	public FormatterTagTabPage(IModificationListener modifyListener, Map<String, String> workingValues) {
		super(modifyListener, workingValues);
	}

	@Override
	protected void initializePage() {
		fPreview.setPreviewText(PREVIEW);
	}

	@Override
	protected void doUpdatePreview() {
		super.doUpdatePreview();
		fPreview.update();
	}

	@Override
	protected void doCreatePreferences(Composite composite, int numColumns) {
		final Group generalGroup = createGroup(numColumns, composite,
				FormatterMessages.ModifyDialog_tabpage_formatter_tag_title);
		createLabel(numColumns, generalGroup, FormatterMessages.FormatterModifyDialog_offOn_description);
		fUseTag = createCheckboxPref(generalGroup, numColumns,
				FormatterMessages.FormatterModifyDialog_offOn_pref_enable,
				DefaultCodeFormatterConstants.FORMATTER_USE_COMMENT_TAG, FALSE_TRUE);
		fUseTag.addObserver(new Observer() {
			@Override
			public void update(Observable arg0, Object arg1) {
				fOnTag.setEnabled(fUseTag.getChecked());
				fOffTag.setEnabled(fUseTag.getChecked());
			}
		});
		PreferenceValidator validator = new PreferenceValidator() {
			@Override
			public String validate(String value) {
				if (value != null && !value.isEmpty()) {
					if (Character.isWhitespace(value.charAt(0))) {
						return FormatterMessages.FormatterModifyDialog_offOn_error_startsWithWhitespace;
					} else if (Character.isWhitespace(value.charAt(value.length() - 1))) {
						return FormatterMessages.FormatterModifyDialog_offOn_error_endsWithWhitespace;
					}
					return null;
				}
				return FormatterMessages.FormatterModifyDialog_offOn_error_empty;
			}
		};
		fOffTag = createStringPref(generalGroup, numColumns, FormatterMessages.FormatterModifyDialog_offOn_pref_off_tag,
				DefaultCodeFormatterConstants.FORMATTER_COMMENT_OFF_TAG);
		fOffTag.setValidator(validator);
		fOnTag = createStringPref(generalGroup, numColumns, FormatterMessages.FormatterModifyDialog_offOn_pref_on_tag,
				DefaultCodeFormatterConstants.FORMATTER_COMMENT_ON_TAG);
		fOnTag.setValidator(validator);
	}

	@Override
	protected CPreview doCreateCPreview(Composite parent) {
		fPreview = new TranslationUnitPreview(fWorkingValues, parent);
		return fPreview;
	}

}
