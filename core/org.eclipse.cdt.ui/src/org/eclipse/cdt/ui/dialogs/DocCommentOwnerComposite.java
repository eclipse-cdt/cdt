/*******************************************************************************
 * Copyright (c) 2008, 2009 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import java.util.Optional;

import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.internal.ui.text.doctools.DoxygenPreferences;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
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
public class DocCommentOwnerComposite extends Composite implements SelectionListener {
	protected DocCommentOwnerCombo fDocCombo;
	protected Label desc, comboLabel;
	protected Group group;
	/**
	 * @since 6.7
	 */
	protected Button useBriefTag;
	/**
	 * @since 6.7
	 */
	protected Button useStructuralCommands;
	/**
	 * @since 6.7
	 */
	protected Button useJavadocStyle;
	/**
	 * @since 6.7
	 */
	protected Button newLineAfterBrief;
	/**
	 * @since 6.7
	 */
	protected Button usePrePostTag;

	public DocCommentOwnerComposite(Composite parent, IDocCommentOwner initialOwner, String description, String label) {
		super(parent, SWT.NONE);
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
		fDocCombo.addSelectionListener(this);
		useBriefTag = addCheckBox(group, DialogsMessages.DocCommentOwnerComposite_UseBriefTagTitle);
		useStructuralCommands = addCheckBox(group, DialogsMessages.DocCommentOwnerComposite_UseStructuralCommandsTitle);
		useJavadocStyle = addCheckBox(group, DialogsMessages.DocCommentOwnerComposite_UseTagJavadocStyleTitle);
		newLineAfterBrief = addCheckBox(group, DialogsMessages.DocCommentOwnerComposite_NewLineAfterBriefTitle);
		usePrePostTag = addCheckBox(group, DialogsMessages.DocCommentOwnerComposite_AddPrePostTagsTitle);
	}

	private Button addCheckBox(Composite parent, String label) {
		Button checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText(label);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalIndent = 0;
		gd.horizontalSpan = 2;
		checkBox.setLayoutData(gd);
		return checkBox;
	}

	public IDocCommentOwner getSelectedDocCommentOwner() {
		return fDocCombo.getSelectedDocCommentOwner();
	}

	private void initialize(Button button, DoxygenPreferences pref, String key, boolean defaultValue) {
		button.setSelection(pref.getBooleanPref(key, defaultValue));
	}

	/**
	 * @since 6.7
	 */
	public void loadValues(IProject project) {
		DoxygenPreferences pref = new DoxygenPreferences(Optional.ofNullable(project));
		initialize(newLineAfterBrief, pref, DoxygenPreferences.DOXYGEN_NEW_LINE_AFTER_BRIEF,
				DoxygenPreferences.DEF_DOXYGEN_NEW_LINE_AFTER_BRIEF);
		initialize(useBriefTag, pref, DoxygenPreferences.DOXYGEN_USE_BRIEF_TAG,
				DoxygenPreferences.DEF_DOXYGEN_USE_BRIEF_TAG);
		initialize(useJavadocStyle, pref, DoxygenPreferences.DOXYGEN_USE_JAVADOC_TAGS,
				DoxygenPreferences.DEF_DOXYGEN_USE_JAVADOC_TAGS);
		initialize(usePrePostTag, pref, DoxygenPreferences.DOXYGEN_USE_PRE_POST_TAGS,
				DoxygenPreferences.DEF_DOXYGEN_USE_PRE_POST_TAGS);
		initialize(useStructuralCommands, pref, DoxygenPreferences.DOXYGEN_USE_STRUCTURAL_COMMANDS,
				DoxygenPreferences.DEF_DOXYGEN_USE_STRUCTURED_COMMANDS);
	}

	/**
	 * @since 6.7
	 */
	public void loadValues() {
		loadValues(null);
	}

	/**
	 * @since 6.7
	 */
	public void performOk(IProject project) {
		DoxygenPreferences pref = new DoxygenPreferences(Optional.ofNullable(project));
		pref.putBooleanPref(DoxygenPreferences.DOXYGEN_NEW_LINE_AFTER_BRIEF, newLineAfterBrief.getSelection());
		pref.putBooleanPref(DoxygenPreferences.DOXYGEN_USE_BRIEF_TAG, useBriefTag.getSelection());
		pref.putBooleanPref(DoxygenPreferences.DOXYGEN_USE_JAVADOC_TAGS, useJavadocStyle.getSelection());
		pref.putBooleanPref(DoxygenPreferences.DOXYGEN_USE_PRE_POST_TAGS, usePrePostTag.getSelection());
		pref.putBooleanPref(DoxygenPreferences.DOXYGEN_USE_STRUCTURAL_COMMANDS, useStructuralCommands.getSelection());
	}

	/**
	 * @since 6.7
	 */
	public void performOk() {
		performOk(null);
	}

	@Override
	public void setEnabled(boolean enabled) {
		desc.setEnabled(enabled);
		comboLabel.setEnabled(enabled);
		fDocCombo.setEnabled(enabled);
		group.setEnabled(enabled);
		if (fDocCombo.isEnabled() && DocCommentOwnerManager.DOXYGEN_CDT_DOC_ONWER_ID
				.equals(fDocCombo.getSelectedDocCommentOwner().getID())) {
			useBriefTag.setEnabled(true);
			useStructuralCommands.setEnabled(true);
			useJavadocStyle.setEnabled(true);
			newLineAfterBrief.setEnabled(true);
			usePrePostTag.setEnabled(true);
		} else {
			useBriefTag.setEnabled(false);
			useStructuralCommands.setEnabled(false);
			useJavadocStyle.setEnabled(false);
			newLineAfterBrief.setEnabled(false);
			usePrePostTag.setEnabled(false);
		}
	}

	@Override
	public void widgetSelected(SelectionEvent e) {
		if (DocCommentOwnerManager.DOXYGEN_CDT_DOC_ONWER_ID.equals(fDocCombo.getSelectedDocCommentOwner().getID())) {
			useBriefTag.setEnabled(true);
			useStructuralCommands.setEnabled(true);
			useJavadocStyle.setEnabled(true);
			newLineAfterBrief.setEnabled(true);
			usePrePostTag.setEnabled(true);
		} else {
			useBriefTag.setEnabled(false);
			useStructuralCommands.setEnabled(false);
			useJavadocStyle.setEnabled(false);
			newLineAfterBrief.setEnabled(false);
			usePrePostTag.setEnabled(false);
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		useBriefTag.setEnabled(false);
		useStructuralCommands.setEnabled(false);
		useJavadocStyle.setEnabled(false);
		newLineAfterBrief.setEnabled(false);
		usePrePostTag.setEnabled(false);
	}
}
