/*******************************************************************************
 * Copyright (c) 2010, 2012 Alena Laskavaia and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Alena Laskavaia - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.Checkers;
import org.eclipse.cdt.codan.core.model.IChecker;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.internal.core.CheckersRegistry;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceStore;

public class LaunchModesPropertyPage extends FieldEditorPreferencePage {
	private final List<FieldEditor> editors;
	private final boolean runInEditor;

	/**
	 * @param problem
	 * @param prefStore
	 */
	public LaunchModesPropertyPage(IProblem problem, PreferenceStore prefStore) {
		super(GRID);
		CheckersRegistry registry = CheckersRegistry.getInstance();
		IChecker checker = registry.getCheckerForProblem(problem);
		if (checker != null) {
			runInEditor = Checkers.canCheckerRunAsYouType(checker);
		} else {
			runInEditor = false;
		}
		setPreferenceStore(prefStore);
		editors = new ArrayList<>();
	}

	@Override
	public void noDefaultAndApplyButton() {
		super.noDefaultAndApplyButton();
	}

	@Override
	protected void createFieldEditors() {
		if (runInEditor) {
			addEditor(CheckerLaunchMode.RUN_AS_YOU_TYPE, CodanUIMessages.LaunchModesPropertyPage_RunAsYouType);
		}
		addEditor(CheckerLaunchMode.RUN_ON_FILE_OPEN, CodanUIMessages.LaunchModesPropertyPage_RunOnFileOpen);
		addEditor(CheckerLaunchMode.RUN_ON_FILE_SAVE, CodanUIMessages.LaunchModesPropertyPage_RunOnFileSave);
		addEditor(CheckerLaunchMode.RUN_ON_INC_BUILD, CodanUIMessages.LaunchModesPropertyPage_RunOnIncrementalBuild);
		addEditor(CheckerLaunchMode.RUN_ON_FULL_BUILD, CodanUIMessages.LaunchModesPropertyPage_RunOnFullBuild);
		addEditor(CheckerLaunchMode.RUN_ON_DEMAND, CodanUIMessages.LaunchModesPropertyPage_RunOnDemand);
	}

	private void addEditor(CheckerLaunchMode launchMode, String label) {
		addField(new BooleanFieldEditor(launchMode.name(), label, getFieldEditorParent()));
	}

	@Override
	protected void addField(FieldEditor editor) {
		editors.add(editor);
		super.addField(editor);
	}

	@Override
	public boolean performOk() {
		boolean result = super.performOk();
		return result;
	}
}
