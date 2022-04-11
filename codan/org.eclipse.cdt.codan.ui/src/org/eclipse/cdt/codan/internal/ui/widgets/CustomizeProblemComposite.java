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

import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.core.resources.IResource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Composite for problem customisable parameters
 *
 */
public class CustomizeProblemComposite extends Composite {
	private Composite parametersTab;
	private IProblem problem;
	private ParametersComposite problemsComposite;
	private FileScopeComposite scopeComposite;
	private IResource resource;
	private LaunchingTabComposite launchingComposite;

	/**
	 * @param parent
	 * @param selectedProblem
	 * @param resource
	 * @param style
	 */
	public CustomizeProblemComposite(Composite parent, IProblem selectedProblem, IResource resource, boolean combined) {
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(1, false));
		this.problem = selectedProblem;
		this.resource = resource;
		final CTabFolder tabFolder = new CTabFolder(this, SWT.TOP);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		// createMainTab(tabFolder);
		createParamtersTab(tabFolder, combined);
		createScopeTab(tabFolder);
		createLaunchingTab(tabFolder);
		tabFolder.setSelection(0);
	}

	public void save(IProblemWorkingCopy problem) {
		problemsComposite.save(problem);
		scopeComposite.save(problem);
		launchingComposite.save(problem);
	}

	/**
	 * @param tabFolder
	 */
	private void createParamtersTab(CTabFolder tabFolder, boolean combined) {
		CTabItem tabItem1 = new CTabItem(tabFolder, SWT.NULL);
		tabItem1.setText(CodanUIMessages.CustomizeProblemComposite_TabParameters);
		parametersTab = new Composite(tabFolder, SWT.NONE);
		tabItem1.setControl(parametersTab);
		parametersTab.setLayout(new GridLayout());
		problemsComposite = new ParametersComposite(parametersTab, problem, combined);
		problemsComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));
	}

	/**
	 * @param tabFolder
	 */
	private void createScopeTab(CTabFolder tabFolder) {
		CTabItem tabItem1 = new CTabItem(tabFolder, SWT.NULL);
		tabItem1.setText(CodanUIMessages.CustomizeProblemComposite_TabScope);
		Composite comp = new Composite(tabFolder, SWT.NONE);
		tabItem1.setControl(comp);
		comp.setLayout(new GridLayout());
		scopeComposite = new FileScopeComposite(comp, problem, resource);
		scopeComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));
	}

	private void createLaunchingTab(CTabFolder tabFolder) {
		CTabItem tabItem1 = new CTabItem(tabFolder, SWT.NULL);
		tabItem1.setText(CodanUIMessages.CustomizeProblemComposite_LaunchingTab);
		Composite comp = new Composite(tabFolder, SWT.NONE);
		tabItem1.setControl(comp);
		comp.setLayout(new GridLayout());
		launchingComposite = new LaunchingTabComposite(comp, problem, resource);
		launchingComposite.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false));
	}
}
