/*******************************************************************************
 * Copyright (c) 2018 Red Hat Inc
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - Initial Contribution
 *******************************************************************************/
package org.eclipse.cdt.internal.meson.ui.wizards;

import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.cdt.internal.meson.ui.SWTImagesFactory;
import org.eclipse.cdt.meson.core.IMesonConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * A Wizard dialog page to allow a user to specify environment variables
 * and options for a ninja command to be run against the active
 * build configuration for the project.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class RunNinjaPage extends WizardPage {

	private ICBuildConfiguration config;
	private Text envText;
	private Text ninjaArgs;

	public RunNinjaPage(ICBuildConfiguration config) {
		super(WizardMessages.RunNinjaPage_name);
		setDescription(WizardMessages.RunNinjaPage_description);
		setTitle(WizardMessages.RunNinjaPage_title);
		setImageDescriptor(SWTImagesFactory.DESC_MESON);
		this.config = config;
	}

	@Override
	public void createControl(Composite parent) {

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		composite.setLayout(new GridLayout(1, true));

		Label envLabel = new Label(composite, SWT.NONE);
		envLabel.setText(WizardMessages.RunNinjaPage_env_label);
		envLabel.setLayoutData(new GridData());

		envText = new Text(composite, SWT.BORDER);
		String lastEnv = config.getProperty(IMesonConstants.NINJA_ENV);
		if (lastEnv == null) {
			lastEnv = ""; //$NON-NLS-1$
		}
		envText.setToolTipText(WizardMessages.RunNinjaPage_env_description);
		envText.setText(lastEnv);
		GridData gdata = new GridData(SWT.FILL, SWT.FILL, true, false);
		envText.setLayoutData(gdata);

		Label argLabel = new Label(composite, SWT.NONE);
		argLabel.setText(WizardMessages.RunNinjaPage_options_label);
		argLabel.setLayoutData(new GridData());

		ninjaArgs = new Text(composite, SWT.BORDER);
		String lastNinjaArgs = config.getProperty(IMesonConstants.NINJA_ARGUMENTS);
		if (lastNinjaArgs == null) {
			lastNinjaArgs = ""; //$NON-NLS-1$
		}
		ninjaArgs.setToolTipText(WizardMessages.RunNinjaPage_options_description);
		ninjaArgs.setText(lastNinjaArgs);
		GridData gdata2 = new GridData(SWT.FILL, SWT.FILL, true, false);
		ninjaArgs.setLayoutData(gdata2);

		setControl(composite);
	}

	/**
	 * Return the user-specified environment variables (NAME=VALUE pairs)
	 * @return the environment String
	 */
	public String getEnvStr() {
		return envText.getText();
	}

	/**
	 * Return the user-specified ninja arguments
	 * @return the ninja arg String
	 */
	public String getNinjaArgs() {
		return ninjaArgs.getText();
	}

}
