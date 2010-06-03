/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.widgets;

import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.param.FileScopeProblemPreference;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.core.param.MapProblemPreference;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.cdt.codan.internal.ui.preferences.FileScopePreferencePage;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Composite to show problem scope
 * 
 */
public class FileScopeComposite extends Composite {
	private FileScopePreferencePage page;
	private IProblem problem;
	private PreferenceStore prefStore;
	private FileScopeProblemPreference scope;

	/**
	 * @param parent
	 * @param problem
	 * @param resource
	 * @param style
	 */
	public FileScopeComposite(Composite parent, final IProblem problem,
			IResource resource) {
		super(parent, SWT.NONE);
		if (problem == null)
			throw new NullPointerException();
		this.setLayout(new GridLayout(2, false));
		this.problem = problem;
		this.prefStore = new PreferenceStore();
		IProblemPreference info = problem.getPreference();
		FileScopeProblemPreference scopeIn = null;
		if (info == null
				|| (!(info instanceof MapProblemPreference))
				|| ((scopeIn = (FileScopeProblemPreference) ((MapProblemPreference) info)
						.getChildDescriptor(FileScopeProblemPreference.KEY)) == null)) {
			Label label = new Label(this, 0);
			label.setText(CodanUIMessages.ParametersComposite_None);
			return;
		}
		scope = (FileScopeProblemPreference) scopeIn.clone();
		scope.setResource(resource);
		initPrefStore();
		page = new FileScopePreferencePage(scope);
		page.setPreferenceStore(prefStore);
		page.noDefaultAndApplyButton();
		page.createControl(parent);
		page.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
	}

	public void save(IProblemWorkingCopy problem) {
		if (page != null)
			page.performOk();
		savePrefStore();
	}

	private void savePrefStore() {
		if (scope == null)
			return;
		String key = scope.getQualifiedKey();
		((MapProblemPreference) problem.getPreference()).setChildValue(
				FileScopeProblemPreference.KEY, scope);
		prefStore.setValue(key, scope.exportValue());
	}

	private void initPrefStore() {
		if (scope == null)
			return;
		String key = scope.getQualifiedKey();
		prefStore.setValue(key, scope.exportValue());
	}

	/**
	 * @return the problem
	 */
	public IProblem getProblem() {
		return problem;
	}
}
