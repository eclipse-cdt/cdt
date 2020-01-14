/*******************************************************************************
 * Copyright (c) 2008, 2020 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Marco Stornelli <marco.stornelli@gmail.com> - Bug 333134
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 333134
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import static org.eclipse.cdt.internal.ui.text.doctools.DoxygenPreferences.DEF_DOXYGEN_NEW_LINE_AFTER_BRIEF;
import static org.eclipse.cdt.internal.ui.text.doctools.DoxygenPreferences.DEF_DOXYGEN_USE_BRIEF_TAG;
import static org.eclipse.cdt.internal.ui.text.doctools.DoxygenPreferences.DEF_DOXYGEN_USE_JAVADOC_TAGS;
import static org.eclipse.cdt.internal.ui.text.doctools.DoxygenPreferences.DEF_DOXYGEN_USE_PRE_POST_TAGS;
import static org.eclipse.cdt.internal.ui.text.doctools.DoxygenPreferences.DEF_DOXYGEN_USE_STRUCTURED_COMMANDS;
import static org.eclipse.cdt.internal.ui.text.doctools.DoxygenPreferences.DOXYGEN_NEW_LINE_AFTER_BRIEF;
import static org.eclipse.cdt.internal.ui.text.doctools.DoxygenPreferences.DOXYGEN_USE_BRIEF_TAG;
import static org.eclipse.cdt.internal.ui.text.doctools.DoxygenPreferences.DOXYGEN_USE_JAVADOC_TAGS;
import static org.eclipse.cdt.internal.ui.text.doctools.DoxygenPreferences.DOXYGEN_USE_PRE_POST_TAGS;
import static org.eclipse.cdt.internal.ui.text.doctools.DoxygenPreferences.DOXYGEN_USE_STRUCTURAL_COMMANDS;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.internal.ui.text.doctools.DoxygenPreferences;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * <em>This class is not intended for use outside of CDT</em>
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DocCommentOwnerComposite extends Composite {
	protected DocCommentOwnerCombo fDocCombo;
	protected Label desc, comboLabel;
	protected Group group;

	private final Map<String, Button> buttons;
	private final Map<String, Boolean> defaults;

	public DocCommentOwnerComposite(Composite parent, IDocCommentOwner initialOwner, String description, String label) {
		super(parent, SWT.NONE);
		buttons = new LinkedHashMap<>();
		defaults = new LinkedHashMap<>();
		GridLayout gl = new GridLayout();
		gl.marginHeight = gl.marginWidth = 0;
		setLayout(gl);

		group = ControlFactory.createGroup(this, DialogsMessages.DocCommentOwnerComposite_DocumentationToolGroupTitle,
				2);
		group.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());

		desc = new Label(group, SWT.WRAP);
		GridData gd = GridDataFactory.fillDefaults().grab(false, false).span(2, 1).create();
		gd.widthHint = 150;
		desc.setText(description);
		desc.setLayoutData(gd);

		comboLabel = new Label(group, SWT.NONE);
		comboLabel.setText(label);

		fDocCombo = new DocCommentOwnerCombo(group, SWT.NONE, initialOwner) {
		};
		gd = GridDataFactory.fillDefaults().grab(true, false).create();
		fDocCombo.setLayoutData(gd);
		fDocCombo.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> recheckButtons()));
		createCheckBox(group, DialogsMessages.DocCommentOwnerComposite_UseBriefTagTitle, DOXYGEN_USE_BRIEF_TAG,
				DEF_DOXYGEN_USE_BRIEF_TAG);
		createCheckBox(group, DialogsMessages.DocCommentOwnerComposite_UseStructuralCommandsTitle,
				DOXYGEN_USE_STRUCTURAL_COMMANDS, DEF_DOXYGEN_USE_STRUCTURED_COMMANDS);
		createCheckBox(group, DialogsMessages.DocCommentOwnerComposite_UseTagJavadocStyleTitle,
				DOXYGEN_USE_JAVADOC_TAGS, DEF_DOXYGEN_USE_JAVADOC_TAGS);
		createCheckBox(group, DialogsMessages.DocCommentOwnerComposite_NewLineAfterBriefTitle,
				DOXYGEN_NEW_LINE_AFTER_BRIEF, DEF_DOXYGEN_NEW_LINE_AFTER_BRIEF);
		createCheckBox(group, DialogsMessages.DocCommentOwnerComposite_AddPrePostTagsTitle, DOXYGEN_USE_PRE_POST_TAGS,
				DEF_DOXYGEN_USE_PRE_POST_TAGS);
	}

	private Button createCheckBox(Composite parent, String label, String key, boolean value) {
		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(label);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 0;
		gd.horizontalSpan = 2;
		checkBox.setLayoutData(gd);
		buttons.put(key, checkBox);
		defaults.put(key, value);
		return checkBox;
	}

	public IDocCommentOwner getSelectedDocCommentOwner() {
		return fDocCombo.getSelectedDocCommentOwner();
	}

	/**
	 * Initializes widget values from the given preference instance
	 *
	 * @param preferences the preferences to initialize from
	 *
	 * @since 6.7
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void initialize(DoxygenPreferences preferences) {
		buttons.entrySet().stream().forEach(e -> initialize(e.getValue(), preferences, e.getKey()));
	}

	void initialize(Button button, DoxygenPreferences pref, String key) {
		button.setSelection(pref.getBoolean(key, defaults.get(key)));
	}

	/**
	 * Apply widget values to the given preference instance
	 *
	 * @param preferences the preferences to apply to
	 *
	 * @since 6.7
	 *
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public void apply(DoxygenPreferences preferences) {
		buttons.entrySet().stream().forEach(e -> preferences.putBoolean(e.getKey(), e.getValue().getSelection()));
	}

	@Override
	public void setEnabled(boolean enabled) {
		desc.setEnabled(enabled);
		comboLabel.setEnabled(enabled);
		fDocCombo.setEnabled(enabled);
		group.setEnabled(enabled);
		recheckButtons();
	}

	void recheckButtons() {
		boolean doxygenEnabled = fDocCombo.isEnabled() && DocCommentOwnerManager.DOXYGEN_CDT_DOC_ONWER_ID
				.equals(fDocCombo.getSelectedDocCommentOwner().getID());
		buttons.values().forEach(b -> b.setEnabled(doxygenEnabled));
	}

}
