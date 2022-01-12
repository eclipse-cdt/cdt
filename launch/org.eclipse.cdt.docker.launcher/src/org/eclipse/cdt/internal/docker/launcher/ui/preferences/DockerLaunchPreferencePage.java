/*******************************************************************************
 * Copyright (c) 2015 Red Hat.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat - Initial Contribution
 *******************************************************************************/
package org.eclipse.cdt.internal.docker.launcher.ui.preferences;

import org.eclipse.cdt.docker.launcher.DockerLaunchUIPlugin;
import org.eclipse.cdt.internal.docker.launcher.Messages;
import org.eclipse.cdt.internal.docker.launcher.PreferenceConstants;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DockerLaunchPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private StringFieldEditor defaultImage;
	private BooleanFieldEditor keepContainerAfterLaunch;

	public DockerLaunchPreferencePage() {
		super(GRID);
		setPreferenceStore(DockerLaunchUIPlugin.getDefault().getPreferenceStore());
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		defaultImage = new StringFieldEditor(PreferenceConstants.DEFAULT_IMAGE, Messages.Default_Image,
				getFieldEditorParent());
		addField(defaultImage);

		keepContainerAfterLaunch = new BooleanFieldEditor(PreferenceConstants.KEEP_CONTAINER_AFTER_LAUNCH,
				Messages.Keep_Container_After_Launch, getFieldEditorParent());
		addField(keepContainerAfterLaunch);

	}

	@Override
	public void init(IWorkbench workbench) {
	}

}