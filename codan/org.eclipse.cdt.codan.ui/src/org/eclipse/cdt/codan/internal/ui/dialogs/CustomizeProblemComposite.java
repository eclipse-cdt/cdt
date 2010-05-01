/*******************************************************************************
 * Copyright (c) 2009 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.dialogs;

import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.internal.ui.CodnaUIMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Composite for problem customisable parameters
 * 
 */
public class CustomizeProblemComposite extends Composite {
	private Composite parametersTab;
	private IProblem problem;
	private ParametersComposite problemsComposite;

	/**
	 * @param parent
	 * @param selectedProblem
	 * @param style
	 */
	public CustomizeProblemComposite(Composite parent, IProblem selectedProblem) {
		super(parent, SWT.NONE);
	
		this.setLayout(new GridLayout(1, false));
		this.problem = selectedProblem;
		final TabFolder tabFolder = new TabFolder(this, SWT.TOP);
		tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH));
		// createMainTab(tabFolder);
		createParamtersTab(tabFolder);
		createScopeTab(tabFolder);
	}

	public void save(IProblemWorkingCopy problem) {
		problemsComposite.save(problem);
	}

	/**
	 * @param tabFolder
	 */
	private void createParamtersTab(TabFolder tabFolder) {
		TabItem tabItem1 = new TabItem(tabFolder, SWT.NULL);
		tabItem1.setText(CodnaUIMessages.CustomizeProblemComposite_TabParameters);
		parametersTab = new Composite(tabFolder, SWT.NONE);
		tabItem1.setControl(parametersTab);
		parametersTab.setLayout(new GridLayout());
		problemsComposite = new ParametersComposite(parametersTab, problem);
		problemsComposite.setLayoutData(new GridData(SWT.BEGINNING,SWT.BEGINNING, true, false));
	}

	/**
	 * @param tabFolder
	 */
	private void createScopeTab(TabFolder tabFolder) {
		TabItem tabItem1 = new TabItem(tabFolder, SWT.NULL);
		tabItem1.setText(CodnaUIMessages.CustomizeProblemComposite_TabScope);
		Composite comp = new Composite(tabFolder, SWT.NONE);
		tabItem1.setControl(comp);
		comp.setLayout(new GridLayout());
		Label label = new Label(comp, SWT.NONE);
		label.setText("Scope: TODO"); //$NON-NLS-1$
		label.setLayoutData(new GridData(GridData.FILL_BOTH));
	}
}
