/*******************************************************************************
 * Copyright (c) 2008 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.dialogs;

import org.eclipse.cdt.internal.ui.refactoring.utils.VisibilityEnum;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
  * Composite to query for a name and visibility.
  *
  * @author Thomas Corbat
  *
  */
public class NameAndVisibilityComposite extends Composite {

	private LabeledTextField constantName;
	private final String labelName;
	private final VisibilitySelectionPanel visibilityPanel;

	public NameAndVisibilityComposite(Composite parent, String labelName, String defaultName) {
		this(parent, labelName, VisibilityEnum.v_public, defaultName);
	}

	public NameAndVisibilityComposite(Composite parent, String labelName, VisibilityEnum defaultVisibility,
			String defaultName) {

		super(parent, SWT.NONE);

		this.labelName = labelName;

		setLayout(new GridLayout());

		createNewMethodNameComposite(this, defaultName);
		visibilityPanel = new VisibilitySelectionPanel(this, defaultVisibility, SWT.NONE);
	}

	public Text getConstantNameText() {
		return constantName.getText();
	}

	public Group getVisibiltyGroup() {
		return visibilityPanel.getGroup();
	}

	public void visibilityPanelsetVisible(boolean visible) {
		visibilityPanel.setVisible(visible);
	}

	private void createNewMethodNameComposite(Composite control, String defaultName) {
		Composite methodNameComposite = new Composite(control, SWT.NONE);
		FillLayout compositeLayout = new FillLayout(SWT.HORIZONTAL);
		GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gridData.horizontalAlignment = GridData.FILL;
		methodNameComposite.setLayoutData(gridData);
		methodNameComposite.setLayout(compositeLayout);
		constantName = new LabeledTextField(methodNameComposite, labelName, defaultName);
	}
}
