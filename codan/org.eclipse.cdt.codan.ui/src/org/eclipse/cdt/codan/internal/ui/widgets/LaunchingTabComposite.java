/*******************************************************************************
 * Copyright (c) 2009, 2011 Alena Laskavaia
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.widgets;

import org.eclipse.cdt.codan.core.model.CheckerLaunchMode;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.param.LaunchModeProblemPreference;
import org.eclipse.cdt.codan.core.param.MapProblemPreference;
import org.eclipse.cdt.codan.core.param.RootProblemPreference;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.cdt.codan.internal.ui.preferences.LaunchModesPropertyPage;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Composite to show problem launchPref
 *
 */
public class LaunchingTabComposite extends Composite {
	private LaunchModesPropertyPage page;
	private IProblem problem;
	private PreferenceStore prefStore;
	private LaunchModeProblemPreference launchPref;

	/**
	 * @param parent
	 * @param problem
	 * @param resource
	 * @param style
	 */
	public LaunchingTabComposite(Composite parent, final IProblem problem, IResource resource) {
		super(parent, SWT.NONE);
		if (problem == null)
			throw new NullPointerException();
		this.setLayout(new GridLayout(2, false));
		this.problem = problem;
		this.prefStore = new PreferenceStore();
		IProblemPreference info = problem.getPreference();
		if (info == null || (!(info instanceof RootProblemPreference))) {
			Label label = new Label(this, 0);
			label.setText(CodanUIMessages.ParametersComposite_None);
			return;
		}
		LaunchModeProblemPreference launchModes = ((RootProblemPreference) info).getLaunchModePreference();
		launchPref = (LaunchModeProblemPreference) launchModes.clone();
		initPrefStore();
		page = new LaunchModesPropertyPage(problem, prefStore);
		page.noDefaultAndApplyButton();
		page.createControl(parent);
		page.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	public void save(IProblemWorkingCopy problem) {
		if (page != null)
			page.performOk();
		savePrefStore(problem);
	}

	private void savePrefStore(IProblemWorkingCopy problem) {
		if (launchPref == null)
			return;
		saveToPref(launchPref, page.getPreferenceStore());
		MapProblemPreference parentMap = (MapProblemPreference) problem.getPreference();
		parentMap.addChildDescriptor((IProblemPreference) launchPref.clone());
	}

	/**
	 * @param launchPref2
	 * @param preferenceStore
	 */
	private void saveToPref(LaunchModeProblemPreference launchPref, IPreferenceStore preferenceStore) {
		CheckerLaunchMode[] values = CheckerLaunchMode.values();
		for (int i = 0; i < values.length; i++) {
			CheckerLaunchMode checkerLaunchMode = values[i];
			String name = checkerLaunchMode.name();
			if (!preferenceStore.isDefault(name)) {
				boolean value = preferenceStore.getBoolean(name);
				launchPref.setRunningMode(checkerLaunchMode, value);
			}
		}
	}

	private void initPrefStore() {
		if (launchPref == null)
			return;
		CheckerLaunchMode[] values = CheckerLaunchMode.values();
		for (int i = 0; i < values.length; i++) {
			CheckerLaunchMode checkerLaunchMode = values[i];
			prefStore.setDefault(checkerLaunchMode.name(), true);
			prefStore.setValue(checkerLaunchMode.name(), launchPref.isRunningInMode(checkerLaunchMode));
		}
	}

	/**
	 * @return the problem
	 */
	public IProblem getProblem() {
		return problem;
	}
}
