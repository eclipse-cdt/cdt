/*******************************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences.formatter;

import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public abstract class FormatterTabPage extends ModifyDialogTabPage {
	private final static String SHOW_INVISIBLE_PREFERENCE_KEY = CUIPlugin.PLUGIN_ID
			+ ".formatter_page.show_invisible_characters"; //$NON-NLS-1$

	/**
	 * Constant array for boolean true/false selection.
	 *
	 * @since 5.3
	 */
	protected static String[] TRUE_FALSE = { DefaultCodeFormatterConstants.TRUE, DefaultCodeFormatterConstants.FALSE };

	/**
	 * Constant array for boolean true/false selection.
	 *
	 * @since 5.3
	 */
	protected static String[] FALSE_TRUE = { DefaultCodeFormatterConstants.FALSE, DefaultCodeFormatterConstants.TRUE };

	/**
	 * Constant array for insert / not_insert.
	 */
	protected static String[] DO_NOT_INSERT_INSERT = { CCorePlugin.DO_NOT_INSERT, CCorePlugin.INSERT };

	private CPreview fPreview;
	private final IDialogSettings fDialogSettings;
	private Button fShowInvisibleButton;

	public FormatterTabPage(IModifyDialogTabPage.IModificationListener modifyListener,
			Map<String, String> workingValues) {
		super(modifyListener, workingValues);
		fDialogSettings = CUIPlugin.getDefault().getDialogSettings();
	}

	@Override
	protected Composite doCreatePreviewPane(Composite composite, int numColumns) {
		createLabel(numColumns - 1, composite, FormatterMessages.ModifyDialogTabPage_preview_label_text);

		fShowInvisibleButton = new Button(composite, SWT.CHECK);
		fShowInvisibleButton.setText(FormatterMessages.FormatterTabPage_ShowInvisibleCharacters_label);
		fShowInvisibleButton.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
		fShowInvisibleButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fPreview.showInvisibleCharacters(fShowInvisibleButton.getSelection());
				fDialogSettings.put(SHOW_INVISIBLE_PREFERENCE_KEY, fShowInvisibleButton.getSelection());
				doUpdatePreview();
			}
		});
		fShowInvisibleButton.setSelection(isShowInvisible());

		fPreview = doCreateCPreview(composite);
		fDefaultFocusManager.add(fPreview.getControl());
		fPreview.showInvisibleCharacters(fShowInvisibleButton.getSelection());

		final GridData gd = createGridData(numColumns, GridData.FILL_BOTH, 0);
		gd.widthHint = 0;
		gd.heightHint = 0;
		fPreview.getControl().setLayoutData(gd);

		return composite;
	}

	private boolean isShowInvisible() {
		return fDialogSettings.getBoolean(SHOW_INVISIBLE_PREFERENCE_KEY);
	}

	@Override
	protected void doUpdatePreview() {
		boolean showInvisible = isShowInvisible();
		fPreview.showInvisibleCharacters(showInvisible);
		fShowInvisibleButton.setSelection(showInvisible);
	}
}