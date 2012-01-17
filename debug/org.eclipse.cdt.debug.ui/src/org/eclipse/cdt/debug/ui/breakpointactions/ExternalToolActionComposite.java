/*******************************************************************************
 * Copyright (c) 2007 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IPluginContribution;
import org.eclipse.ui.activities.WorkbenchActivityHelper;
import org.eclipse.ui.dialogs.ListDialog;

public class ExternalToolActionComposite extends Composite {

	/**
	 * A plug-in contribution (UI element) which contains a launch configuration
	 * type (Core element). Plug-in contributions are passed to the workbench
	 * activity support to filter elements from the UI.
	 */
	class LaunchConfigurationTypeContribution implements IPluginContribution {

		protected ILaunchConfigurationType type;

		/**
		 * Creates a new plug-in contribution for the given type
		 * 
		 * @param type
		 *            the launch configuration type
		 */
		public LaunchConfigurationTypeContribution(ILaunchConfigurationType type) {
			this.type = type;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IPluginContribution#getLocalId()
		 */
		@Override
		public String getLocalId() {
			return type.getIdentifier();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.IPluginContribution#getPluginId()
		 */
		@Override
		public String getPluginId() {
			return type.getPluginIdentifier();
		}

	}

	public class LaunchConfigurationSelectionDialog extends ListDialog {

		private ILaunchConfiguration[] launchConfigs;

		public LaunchConfigurationSelectionDialog(ExternalToolActionComposite composite, ILaunchConfiguration[] lcs) {
			super(composite.getShell());

			launchConfigs = lcs;

			this.setInput(composite);
			this.setContentProvider(new IStructuredContentProvider() {

				@Override
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}

				@Override
				public void dispose() {
				}

				@Override
				public Object[] getElements(Object parent) {
					return launchConfigs;
				}
			});

			this.setLabelProvider(new LabelProvider() {

				@Override
				public String getText(Object element) {
					return ((ILaunchConfiguration) element).getName();
				}
			});
		}

	}

	private Text extToolName;

	/**
	 * Create the composite
	 * 
	 * @param parent
	 * @param style
	 * @param page
	 */
	public ExternalToolActionComposite(Composite parent, int style, ExternalToolActionPage page) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		setLayout(gridLayout);

		final Label executeFileLabel = new Label(this, SWT.LEFT);
		executeFileLabel.setLayoutData(new GridData());
		executeFileLabel.setText(Messages.getString("ExternalToolActionComposite.ToolLabel")); //$NON-NLS-1$

		extToolName = new Text(this, SWT.READ_ONLY);
		extToolName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		extToolName.setText(page.getExternalToolAction().getExternalToolName());

		final ExternalToolActionComposite externalToolActionComposite = this;

		final Button browseButton = new Button(this, SWT.NONE);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				ILaunchConfiguration[] lcs = getLaunchConfigurations();
				LaunchConfigurationSelectionDialog dlg = new LaunchConfigurationSelectionDialog(externalToolActionComposite, lcs);
				dlg.setTitle(Messages.getString("ExternalToolActionComposite.DialogTitle")); //$NON-NLS-1$
				dlg.setMessage(Messages.getString("ExternalToolActionComposite.DialogMessage")); //$NON-NLS-1$
				if (lcs.length > 0) {
					ILaunchConfiguration[] initialSelection = new ILaunchConfiguration[1];
					String existingName = extToolName.getText();
					initialSelection[0] = lcs[0];
					if (existingName.length() > 0) {
						for (int i = 0; i < lcs.length; i++) {
							if (lcs[i].getName().equals(existingName)) {
								initialSelection[0] = lcs[i];
								break;
							}
						}
					}
					dlg.setInitialSelections(initialSelection);
				}
				dlg.setAddCancelButton(false);

				if (dlg.open() == Window.OK) {
					Object[] selectedTool = dlg.getResult();
					if (selectedTool.length > 0 && selectedTool[0] instanceof ILaunchConfiguration) {
						externalToolActionComposite.setExternalToolName(((ILaunchConfiguration) selectedTool[0]).getName());
					}
				}

			}
		});
		browseButton.setText(Messages.getString("ExternalToolActionComposite.ChooseButtonTitle")); //$NON-NLS-1$
		browseButton.setEnabled(getLaunchConfigurations().length > 0);

		final Button externalToolsButton = new Button(this, SWT.NONE);
		externalToolsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DebugUITools.openLaunchConfigurationDialogOnGroup(externalToolActionComposite.getShell(), new StructuredSelection(), "org.eclipse.ui.externaltools.launchGroup"); //$NON-NLS-1$
				browseButton.setEnabled(getLaunchConfigurations().length > 0);
			}
		});
		externalToolsButton.setText(Messages.getString("ExternalToolActionComposite.ExternalToolsButtonTitle")); //$NON-NLS-1$
		//
	}

	protected void setExternalToolName(String externalToolName) {
		extToolName.setText(externalToolName);
	}

	private boolean equalCategories(String c1, String c2) {
		if (c1 == null || c2 == null) {
			return c1 == c2;
		}
		return c1.equals(c2);
	}

	public ILaunchConfiguration[] getLaunchConfigurations() {
		ArrayList onlyExternalTools = new ArrayList();
		ILaunchManager lcm = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfiguration[] launchConfigurations;
		try {
			launchConfigurations = lcm.getLaunchConfigurations();
			for (int i = 0; i < launchConfigurations.length; i++) {

				ILaunchConfiguration config = launchConfigurations[i];
				ILaunchConfigurationType type = config.getType();
				boolean priv = false;
				try {
					priv = config.getAttribute(IDebugUIConstants.ATTR_PRIVATE, false);
				} catch (CoreException e) {
				}
				if (type != null) {
					if (!priv && type.supportsMode(ILaunchManager.RUN_MODE) && equalCategories(type.getCategory(), "org.eclipse.ui.externaltools") //$NON-NLS-1$
							&& !WorkbenchActivityHelper.filterItem(new LaunchConfigurationTypeContribution(type)))
						onlyExternalTools.add(launchConfigurations[i]);
				}

			}
		} catch (CoreException e) {
		}
		return (ILaunchConfiguration[]) onlyExternalTools.toArray(new ILaunchConfiguration[onlyExternalTools.size()]);
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public String getLaunchConfigName() {
		return extToolName.getText();
	}

}
