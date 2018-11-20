/*******************************************************************************
 * Copyright (c) 2007, 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.properties;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.internal.autotools.core.AutotoolsPropertyConstants;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractCBuildPropertyTab;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class AutotoolsBuildPropertyPage extends AbstractCBuildPropertyTab {

	private String TRUE = "true"; //$NON-NLS-1$
	private String FALSE = "false"; //$NON-NLS-1$
	private String CLEAN_DELETE_LABEL = "CleanDelete.label"; //$NON-NLS-1$
	private String CLEAN_MAKE_LABEL = "CleanMake.label"; //$NON-NLS-1$
	private String CLEAN_MAKETARGET_LABEL = "CleanMakeTarget.label"; //$NON-NLS-1$
	private String CLEAN_MAKETARGET_TOOLTIP = "CleanMakeTarget.tooltip"; //$NON-NLS-1$
	private String AUTO_BUILDNAME_LABEL = "AutoBuildName.label"; //$NON-NLS-1$
	private String AUTO_BUILDNAME_TOOLTIP = "AutoBuildName.tooltip"; //$NON-NLS-1$

	protected Button fCleanDelete;
	protected Button fCleanMake;
	protected Button fAutoName;
	protected Text fCleanMakeTarget;

	private IProject getProject() {
		return (IProject) getCfg().getManagedProject().getOwner();
	}

	@Override
	public boolean canBeVisible() {
		return AutotoolsPlugin.hasTargetBuilder(getProject());
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		Composite composite = usercomp;
		// assume parent page uses griddata
		GridData gd = new GridData(
				GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL);
		composite.setLayoutData(gd);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		//PixelConverter pc= new PixelConverter(composite);
		//layout.verticalSpacing= pc.convertHeightInCharsToPixels(1) / 2;
		composite.setLayout(layout);

		Group g = new Group(composite, SWT.SHADOW_ETCHED_IN);
		g.setText(AutotoolsPropertyMessages.getString("CleanBehavior.title"));
		gd = new GridData(
				GridData.HORIZONTAL_ALIGN_CENTER | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		g.setLayoutData(gd);
		layout = new GridLayout();
		layout.numColumns = 2;
		g.setLayout(layout);

		fCleanDelete = new Button(g, SWT.RADIO);
		fCleanDelete.setText(AutotoolsPropertyMessages.getString(CLEAN_DELETE_LABEL));
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 2;
		fCleanDelete.setLayoutData(gd);
		fCleanMake = new Button(g, SWT.RADIO);
		fCleanMake.setText(AutotoolsPropertyMessages.getString(CLEAN_MAKE_LABEL));
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 2;
		fCleanMake.setLayoutData(gd);

		Label label = new Label(g, SWT.LEFT);
		label.setText(AutotoolsPropertyMessages.getString(CLEAN_MAKETARGET_LABEL));
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		label.setLayoutData(gd);

		fCleanMakeTarget = new Text(g, SWT.SINGLE | SWT.BORDER);
		fCleanMakeTarget.setText(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET_DEFAULT);
		fCleanMakeTarget.setToolTipText(AutotoolsPropertyMessages.getString(CLEAN_MAKETARGET_TOOLTIP));
		gd = new GridData(
				GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
		fCleanMakeTarget.setLayoutData(gd);

		fCleanDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fCleanMake.setSelection(false);
				fCleanDelete.setSelection(true);
				fCleanMakeTarget.setEnabled(false);
			}
		});

		fCleanMake.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fCleanDelete.setSelection(false);
				fCleanMake.setSelection(true);
				fCleanMakeTarget.setEnabled(true);
			}
		});

		fCleanMakeTarget.addModifyListener(e -> {
			if (fCleanMakeTarget.getText().isEmpty()) {
				// FIXME: should probably issue warning here, but how?
			}
		});

		fAutoName = new Button(composite, SWT.LEFT | SWT.CHECK);
		fAutoName.setText(AutotoolsPropertyMessages.getString(AUTO_BUILDNAME_LABEL));
		fAutoName.setToolTipText(AutotoolsPropertyMessages.getString(AUTO_BUILDNAME_TOOLTIP));
		gd = new GridData();
		gd.horizontalAlignment = GridData.BEGINNING;
		gd.horizontalSpan = 2;
		fAutoName.setLayoutData(gd);

		initialize();
	}

	@Override
	protected void performOK() {
		IProject project = getProject();
		if (fCleanDelete.getSelection()) {
			try {
				project.setPersistentProperty(AutotoolsPropertyConstants.CLEAN_DELETE, TRUE);
			} catch (CoreException ce) {
				// FIXME: what can we do here?
			}
		} else {
			try {
				project.setPersistentProperty(AutotoolsPropertyConstants.CLEAN_DELETE, FALSE);
			} catch (CoreException ce) {
				// FIXME: what can we do here?
			}
			try {
				project.setPersistentProperty(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET, fCleanMakeTarget.getText());
			} catch (CoreException ce) {
				// FIXME: what can we do here?
			}
		}

		if (fAutoName.getSelection()) {
			try {
				project.setPersistentProperty(AutotoolsPropertyConstants.AUTO_BUILD_NAME, TRUE);
			} catch (CoreException ce) {
				// FIXME: what can we do here?
			}
		} else {
			try {
				project.setPersistentProperty(AutotoolsPropertyConstants.AUTO_BUILD_NAME, FALSE);
			} catch (CoreException ce) {
				// FIXME: what can we do here?
			}
		}

	}

	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		performOK();
	}

	@Override
	protected void performDefaults() {
		fCleanDelete.setSelection(false);
		fCleanMake.setSelection(true);
		fCleanMakeTarget.setText(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET_DEFAULT);
		fCleanMakeTarget.setEnabled(true);
		fAutoName.setEnabled(true);
	}

	@Override
	public void updateData(ICResourceDescription cfgd) {
		// what to do here?
	}

	@Override
	public void updateButtons() {
		// what to do here?
	}

	private void initialize() {
		IProject project = getProject();
		String cleanDelete = null;
		String autoName = null;
		String cleanMakeTarget = null;
		try {
			cleanDelete = project.getPersistentProperty(AutotoolsPropertyConstants.CLEAN_DELETE);
			cleanMakeTarget = project.getPersistentProperty(AutotoolsPropertyConstants.CLEAN_MAKE_TARGET);
			autoName = project.getPersistentProperty(AutotoolsPropertyConstants.AUTO_BUILD_NAME);
		} catch (CoreException e) {
			// do nothing
		}

		if (cleanMakeTarget == null) {
			cleanMakeTarget = AutotoolsPropertyConstants.CLEAN_MAKE_TARGET_DEFAULT;
		}
		fCleanMakeTarget.setText(cleanMakeTarget);

		if (cleanDelete == null || cleanDelete.equals(FALSE)) {
			fCleanDelete.setSelection(false);
			fCleanMake.setSelection(true);
			fCleanMakeTarget.setEnabled(true);
		} else {
			fCleanDelete.setSelection(true);
			fCleanMake.setSelection(false);
			fCleanMakeTarget.setEnabled(false);
		}

		if (autoName == null || autoName.equals(TRUE))
			fAutoName.setSelection(true);
		else
			fAutoName.setSelection(false);
	}

}
