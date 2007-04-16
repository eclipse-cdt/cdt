/**********************************************************************
 * Copyright (c) 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *     QNX Software Systems - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.debug.gdbjtag.ui;

import org.eclipse.cdt.debug.gdbjtag.core.GDBJtagConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * @author Doug Schaefer
 *
 */
public class GDBJtagStartupTab extends AbstractLaunchConfigurationTab {

	Text initCommands;
	Button loadImage;
	Text imageFileName;
	Button defaultRun;
	Text runCommands;

	public String getName() {
		return "Startup";
	}

	public Image getImage() {
		return GDBJtagImages.getStartupTabImage();
	}
	
	public void createControl(Composite parent) {
		ScrolledComposite sc = new ScrolledComposite(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		setControl(sc);

		Composite comp = new Composite(sc, SWT.NONE);
		sc.setContent(comp);
		GridLayout layout = new GridLayout();
		comp.setLayout(layout);

		createInitGroup(comp);
		createLoadGroup(comp);
		createRunGroup(comp);
		
		sc.setMinSize(comp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
	}

	public void createInitGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setText("Initialization Commands");

		initCommands = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		initCommands.setLayoutData(gd);
		initCommands.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		Button varsButton = new Button(group, SWT.NONE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		varsButton.setLayoutData(gd);
		varsButton.setText("Variables...");
		varsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleVarsButtonSelected(initCommands);
			}
		});
	}
	
	private void createLoadGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		layout.numColumns = 3;
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		group.setLayoutData(gd);
		group.setText("Load Image");
		
		loadImage = new Button(group, SWT.CHECK);
		loadImage.setText("Automatically load image");
		gd = new GridData();
		gd.horizontalSpan = 3;
		loadImage.setLayoutData(gd);
		loadImage.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				loadImageChanged();
			}
		});
		
		Label label = new Label(group, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 3;
		label.setLayoutData(gd);
		label.setText("Image file name:");
		
		imageFileName = new Text(group, SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		imageFileName.setLayoutData(gd);

		Button button = new Button(group, SWT.NONE);
		button.setText("Browse...");
		button = new Button(group, SWT.NONE);
		button.setText("Workspace...");
	}
	
	private void loadImageChanged() {
		imageFileName.setEnabled(loadImage.getSelection());
	}
	
	public void createRunGroup(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		group.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		group.setLayoutData(gd);
		group.setText("Run Commands");

		defaultRun = new Button(group, SWT.CHECK);
		defaultRun.setText("Use default run command");
		
		runCommands = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 100;
		runCommands.setLayoutData(gd);
		runCommands.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent evt) {
				updateLaunchConfigurationDialog();
			}
		});
		
		Button varsButton = new Button(group, SWT.NONE);
		gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
		varsButton.setLayoutData(gd);
		varsButton.setText("Variables...");
		varsButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleVarsButtonSelected(runCommands);
			}
		});
	}
	
	private void handleVarsButtonSelected(Text text) {
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		dialog.open();
		text.append(dialog.getVariableExpression());
	}
	
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			initCommands.setText(configuration.getAttribute(GDBJtagConstants.LAUNCH_ATTR_INIT_COMMANDS, "")); //$NON-NLS-1$
			runCommands.setText(configuration.getAttribute(GDBJtagConstants.LAUNCH_ATTR_RUN_COMMANDS, "")); //$NON-NLS-1$)
		} catch (CoreException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
	}

	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(GDBJtagConstants.LAUNCH_ATTR_INIT_COMMANDS, initCommands.getText());
		configuration.setAttribute(GDBJtagConstants.LAUNCH_ATTR_RUN_COMMANDS, runCommands.getText());
	}

	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(GDBJtagConstants.LAUNCH_ATTR_INIT_COMMANDS, ""); //$NON-NLS-1$
		configuration.setAttribute(GDBJtagConstants.LAUNCH_ATTR_RUN_COMMANDS, ""); //$NON-NLS-1$
	}

}
