package org.eclipse.cdt.ui.build.wizards;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/

import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

import org.eclipse.cdt.core.build.managed.IConfiguration;
import org.eclipse.cdt.core.build.managed.ITarget;
import org.eclipse.cdt.core.build.managed.ManagedBuildManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.CProjectWizard;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;

public class CProjectPlatformPage extends WizardPage {
	/*
	 * Bookeeping variables
	 */
	private CProjectWizard wizard;
	private ArrayList selectedConfigurations;
	protected ITarget selectedTarget;
	protected String[] targetNames;
	protected ArrayList targets;

	/*
	 * Dialog variables and string constants
	 */
	protected Combo platformSelection;
	protected CheckboxTableViewer tableViewer;
	private static final String PREFIX = "PlatformBlock"; //$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label"; //$NON-NLS-1$
	private static final String TIP = PREFIX + ".tip"; //$NON-NLS-1$
	private static final String PLATFORM_TIP = TIP + ".platform"; //$NON-NLS-1$
	private static final String PLATFORM_LABEL = LABEL + ".platform"; //$NON-NLS-1$
	private static final String CONFIG_LABEL = LABEL + ".configs"; //$NON-NLS-1$

	/**
	 * Constructor.
	 * @param wizard
	 * @param pageName
	 */
	public CProjectPlatformPage(CProjectWizard wizard, String pageName) {
		super(pageName);
		setPageComplete(false);
		this.wizard = wizard;
		populateTargets();
		selectedTarget = null;
		selectedConfigurations = new ArrayList(0);
	}

	/**
	 * @see org.eclipse.jface.wizard.IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage() {
		return validatePage();
	}

	/**
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		// Create the composite control for the tab
		Composite composite = ControlFactory.createComposite(parent, 6);

		// Create the platform selection label and combo widgets
		Label platformLabel = ControlFactory.createLabel(composite, CUIPlugin.getResourceString(PLATFORM_LABEL));
		platformLabel.setLayoutData(new GridData());

		platformSelection =	ControlFactory.createSelectCombo(composite, targetNames, null);
		platformSelection.setToolTipText(CUIPlugin.getResourceString(PLATFORM_TIP));
		platformSelection.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				handleTargetSelection();
			}
		});
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 5;
		platformSelection.setLayoutData(gd);

		// Create a check box table of valid configurations
		Label configLabel = ControlFactory.createLabel(composite, CUIPlugin.getResourceString(CONFIG_LABEL));
		configLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Table table = new Table(composite, SWT.CHECK | SWT.BORDER | SWT.MULTI
								| SWT.SINGLE | SWT.H_SCROLL	| SWT.V_SCROLL);
		gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = 6;
		table.setLayoutData(gd);
		table.setHeaderVisible(true);
		table.setLinesVisible(false);

		// Add a table layout to the table
		TableLayout tableLayout = new TableLayout();
		table.setHeaderVisible(false);
		table.setLayout(tableLayout);

		// Add the viewer
		tableViewer = new CheckboxTableViewer(table);
		tableViewer.setLabelProvider(new ConfigurationLabelProvider());
		tableViewer.setContentProvider(new ConfigurationContentProvider());
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent e) {
				// will default to false until a selection is made
				handleConfigurationSelectionChange();
			}
		});
		
		// Do the nasty
		setErrorMessage(null);
		setMessage(null);
		setControl(composite);
	}

	public IConfiguration[] getSelectedConfigurations() {
		return (IConfiguration[]) selectedConfigurations.toArray(new IConfiguration[selectedConfigurations.size()]);
	}

	/**
	 * Returns the name of the selected platform.
	 * 
	 * @return String containing platform name or <code>null</code> if an invalid selection
	 * has been made.
	 */
	public ITarget getSelectedTarget() {
		return selectedTarget;
	}

	private void handleConfigurationSelectionChange() {
		// Get the selections from the table viewer
		selectedConfigurations.clear();
		selectedConfigurations.addAll(Arrays.asList(tableViewer.getCheckedElements()));
	}

	/**
	 * Returns whether this page's controls currently all contain valid 
	 * values.
	 *
	 * @return <code>true</code> if all controls are valid, and
	 *   <code>false</code> if at least one is invalid
	 */
	protected void handleTargetSelection() {
		/*
		 * The index in the combo is the offset into the target list
		 */
		int index;
		if (platformSelection != null
			&& (index = platformSelection.getSelectionIndex()) != -1) {
			selectedTarget = (ITarget) targets.get(index);
		}
		populateConfigurations();
		setPageComplete(validatePage());
	}

	/**
	 * Populate the table viewer with the known configurations. 
	 * By default, all the configurations are selected.
	 */
	private void populateConfigurations() {
		// Make the root of the content provider the new target
		tableViewer.setInput(selectedTarget);
		tableViewer.setAllChecked(true);
		handleConfigurationSelectionChange();
	}

	private void populateTargetNames() {
		targetNames = new String[targets.size()];
		ListIterator iter = targets.listIterator();
		int index = 0;
		while (iter.hasNext()) {
			targetNames[index++] = ((ITarget) iter.next()).getName();
		}
	}

	private void populateTargets() {
		// Get a list of platforms defined by plugins
		ITarget[] allTargets = ManagedBuildManager.getDefinedTargets(null);
		targets = new ArrayList();
		// Add all of the concrete targets to the target list
		for (int index = 0; index < allTargets.length; ++index) {
			ITarget target = allTargets[index];
			if (!target.isAbstract() && !target.isTestTarget()) {
				targets.add(target);
			}
		}
		targets.trimToSize();
		populateTargetNames();
	}

	/**
	 * @return
	 */
	private boolean validatePage() {
		// TODO Auto-generated method stub
		return true;
	}
}
