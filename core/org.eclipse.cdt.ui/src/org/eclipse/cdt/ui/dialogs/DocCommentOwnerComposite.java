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

import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.osgi.service.prefs.Preferences;

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
	/**
	 * @since 6.7
	 */
	protected Button useBriefTag;
	/**
	 * @since 6.7
	 */
	protected Button useStructuredCommands;
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
		useBriefTag = ControlFactory.createCheckBox(group, DialogsMessages.DocCommentOwnerComposite_UseBriefTagTitle);
		useStructuredCommands = addCheckBox(group, DialogsMessages.DocCommentOwnerComposite_UseStructuredCommandsTitle);
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

	/**
	 * @since 6.7
	 */
	public void loadValues(IProject project) {
		Preferences prefs = DocCommentOwnerManager.getInstance().getPreferences(project);
		newLineAfterBrief.setSelection(prefs.getBoolean(DocCommentOwnerManager.DOXYGEN_NEW_LINE_AFTER_BRIEF,
				DocCommentOwnerManager.DEF_DOXYGEN_NEW_LINE_AFTER_BRIEF));
		useBriefTag.setSelection(prefs.getBoolean(DocCommentOwnerManager.DOXYGEN_USE_BRIEF_TAG,
				DocCommentOwnerManager.DEF_DOXYGEN_USE_BRIEF_TAG));
		useJavadocStyle.setSelection(prefs.getBoolean(DocCommentOwnerManager.DOXYGEN_USE_JAVADOC_TAGS,
				DocCommentOwnerManager.DEF_DOXYGEN_USE_JAVADOC_TAGS));
		usePrePostTag.setSelection(prefs.getBoolean(DocCommentOwnerManager.DOXYGEN_USE_PRE_POST_TAGS,
				DocCommentOwnerManager.DEF_DOXYGEN_USE_PRE_POST_TAGS));
		useStructuredCommands.setSelection(prefs.getBoolean(DocCommentOwnerManager.DOXYGEN_USE_STRUCTURED_COMMANDS,
				DocCommentOwnerManager.DEF_DOXYGEN_USE_STRUCTURED_COMMANDS));
	}

	/**
	 * @since 6.7
	 */
	public void performOk(IProject project) {
		Preferences prefs = DocCommentOwnerManager.getInstance().getPreferences(project);
		prefs.putBoolean(DocCommentOwnerManager.DOXYGEN_NEW_LINE_AFTER_BRIEF, newLineAfterBrief.getSelection());
		prefs.putBoolean(DocCommentOwnerManager.DOXYGEN_USE_BRIEF_TAG, useBriefTag.getSelection());
		prefs.putBoolean(DocCommentOwnerManager.DOXYGEN_USE_JAVADOC_TAGS, useJavadocStyle.getSelection());
		prefs.putBoolean(DocCommentOwnerManager.DOXYGEN_USE_PRE_POST_TAGS, usePrePostTag.getSelection());
		prefs.putBoolean(DocCommentOwnerManager.DOXYGEN_USE_STRUCTURED_COMMANDS, useStructuredCommands.getSelection());
	}

	@Override
	public void setEnabled(boolean enabled) {
		desc.setEnabled(enabled);
		comboLabel.setEnabled(enabled);
		fDocCombo.setEnabled(enabled);
		group.setEnabled(enabled);
		useBriefTag.setEnabled(enabled);
		useStructuredCommands.setEnabled(enabled);
		useJavadocStyle.setEnabled(enabled);
		newLineAfterBrief.setEnabled(enabled);
		usePrePostTag.setEnabled(enabled);
	}
}
