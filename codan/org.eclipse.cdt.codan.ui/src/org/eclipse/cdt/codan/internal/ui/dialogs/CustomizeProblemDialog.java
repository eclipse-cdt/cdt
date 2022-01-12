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
package org.eclipse.cdt.codan.internal.ui.dialogs;

import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemWorkingCopy;
import org.eclipse.cdt.codan.core.param.RootProblemPreference;
import org.eclipse.cdt.codan.internal.core.model.CodanProblem;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.cdt.codan.internal.ui.widgets.CustomizeProblemComposite;
import org.eclipse.cdt.codan.internal.ui.widgets.ParametersComposite;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog that allows to customise problems
 *
 */
public class CustomizeProblemDialog extends TitleAreaDialog {
	private CustomizeProblemComposite comp;
	private IProblem problem;
	private IProblem[] problems;
	private IResource resource;
	private boolean combined;

	/**
	 * @param parentShell
	 * @param selectedProblem
	 * @param iResource
	 */
	public CustomizeProblemDialog(Shell parentShell, IProblem[] selectedProblems, IResource resource) {
		super(parentShell);
		this.combined = false;
		this.problems = selectedProblems;
		this.problem = buildCombined(selectedProblems);
		this.resource = resource;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/**
	 * @param selectedProblems
	 * @return
	 */
	private IProblem buildCombined(IProblem[] selectedProblems) {
		if (selectedProblems.length == 0)
			return null;
		IProblem one = selectedProblems[0];
		if (selectedProblems.length == 1) {
			return one;
		}
		combined = true;
		CodanProblem problem = new CodanProblem("multi", getTitle()); //$NON-NLS-1$
		problem.setMessagePattern(ParametersComposite.NO_CHANGE);
		problem.setPreference(new RootProblemPreference());
		problem.setSeverity(one.getSeverity());
		problem.setEnabled(one.isEnabled());
		if (one.getPreference() instanceof RootProblemPreference) {
			RootProblemPreference onepref = (RootProblemPreference) one.getPreference();
			RootProblemPreference pref = (RootProblemPreference) problem.getPreference();
			pref.addChildDescriptor(onepref.getLaunchModePreference());
			pref.addChildDescriptor(onepref.getScopePreference());
		}
		return problem;
	}

	/**
	 * Stores edit values into problem working copy
	 *
	 * @param problem
	 *        - problem working copy
	 */
	public void save(IProblemWorkingCopy problem) {
		comp.save(problem);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		getShell().setText(CodanUIMessages.CustomizeProblemDialog_Title);
		setTitle(getTitle());
		setMessage(CodanUIMessages.CustomizeProblemDialog_Message);
		Composite area = (Composite) super.createDialogArea(parent);
		comp = new CustomizeProblemComposite(area, problem, resource, combined);
		GridData ld = new GridData(GridData.FILL_BOTH);
		ld.minimumHeight = 300;
		comp.setLayoutData(ld);
		return area;
	}

	/**
	 * @return
	 */
	public String getTitle() {
		if (problems.length == 1)
			return problem.getName();
		String b = ""; //$NON-NLS-1$
		for (int i = 0; i < problems.length; i++) {
			IProblem p = problems[i];
			if (i != 0)
				b += ", "; //$NON-NLS-1$
			b += p.getName();
		}
		return b;
	}

	@Override
	protected void okPressed() {
		for (int i = 0; i < problems.length; i++) {
			IProblemWorkingCopy wc = (IProblemWorkingCopy) problems[i];
			save(wc);
		}
		super.okPressed();
	}
}
