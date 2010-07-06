/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.ManagedBuilderUIImages;
import org.eclipse.cdt.ui.newui.CDTPrefUtil;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.dialogs.PreferencesUtil;

/**
 * "Select Configurations" page of C/C++ New Project Wizard
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CDTConfigWizardPage extends WizardPage {

	public static final String PAGE_ID = "org.eclipse.cdt.managedbuilder.ui.wizard.CConfigWizardPage"; //$NON-NLS-1$

	private static final Image IMG_CONFIG = ManagedBuilderUIImages.get(ManagedBuilderUIImages.IMG_BUILD_CONFIG);
	private static final String TITLE = Messages.CConfigWizardPage_0; 
	private static final String MESSAGE = Messages.CConfigWizardPage_1; 
	private static final String COMMENT = Messages.CConfigWizardPage_12; 
	private static final String EMPTY_STR = "";  //$NON-NLS-1$

	private Table table;
	private CheckboxTableViewer tv;
	private Label l_projtype;
	private Label l_chains;
	private Composite parent;

	private String propertyId;
	private String errorMessage = null;
	private String message = MESSAGE;
	public boolean isVisible = false;
	private MBSWizardHandler handler;
	public boolean pagesLoaded = false;
	private IToolChain[] visitedTCs = null;
	IWizardPage[] customPages = null;

	public CDTConfigWizardPage(MBSWizardHandler h) {
		super(Messages.CDTConfigWizardPage_0); 
		setPageComplete(false);
		handler = h;
		setWizard(h.getWizard());
	}

	public CfgHolder[] getCfgItems(boolean getDefault) {
		CfgHolder[] its;
		if (getDefault || table == null || !isVisited())
			its = getDefaultCfgs(handler);
		else {
			ArrayList<CfgHolder> out = new ArrayList<CfgHolder>(table.getItemCount());
			for (TableItem ti : table.getItems()) {
				if (ti.getChecked())
					out.add((CfgHolder)ti.getData());
			}
			its = out.toArray(new CfgHolder[out.size()]);
		}
		return its;
	}

	public void createControl(Composite p) {
		parent = new Composite(p, SWT.NONE);
		parent.setFont(parent.getFont());
		parent.setLayout(new GridLayout());
		parent.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite c1 = new Composite(parent, SWT.NONE);
		c1.setLayout(new GridLayout(2, false));
		c1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		setupLabel(c1, Messages.CConfigWizardPage_4, GridData.BEGINNING); 
		l_projtype = setupLabel(c1, EMPTY_STR, GridData.FILL_HORIZONTAL);
		setupLabel(c1, Messages.CConfigWizardPage_5, GridData.BEGINNING); 
		l_chains = setupLabel(c1, EMPTY_STR, GridData.FILL_HORIZONTAL);
		setupLabel(c1, Messages.CConfigWizardPage_6, GridData.BEGINNING); 
		setupLabel(c1, EMPTY_STR, GridData.BEGINNING);

		Composite c2 = new Composite(parent, SWT.NONE);
		c2.setLayout(new GridLayout(2, false));
		c2.setLayoutData(new GridData(GridData.FILL_BOTH));

		table = new Table(c2, SWT.BORDER | SWT.CHECK | SWT.V_SCROLL);
		GridData gd = new GridData(GridData.FILL_BOTH);
		table.setLayoutData(gd);

		tv = new CheckboxTableViewer(table);
		tv.setContentProvider(new IStructuredContentProvider() {
			public Object[] getElements(Object inputElement) {
				return (Object[])inputElement;
			}
			public void dispose() {}
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
		});
		tv.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				return element == null ? EMPTY_STR : ((CfgHolder)element).getName();
			}
			@Override
			public Image getImage(Object element) { return IMG_CONFIG; }
		});
		tv.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				setPageComplete(isCustomPageComplete());
				update();
			}});
		Composite c = new Composite(c2, SWT.NONE);
		c.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		c.setLayout(new GridLayout());

		Button b1 = new Button(c, SWT.PUSH);
		b1.setText(Messages.CConfigWizardPage_7); 
		b1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tv.setAllChecked(true);
				setPageComplete(isCustomPageComplete());
				update();
			}});

		Button b2 = new Button(c, SWT.PUSH);
		b2.setText(Messages.CConfigWizardPage_8); 
		b2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				tv.setAllChecked(false);
				setPageComplete(isCustomPageComplete());
				update();
			}});

		// dummy placeholder
		new Label(c, 0).setLayoutData(new GridData(GridData.FILL_BOTH));

		Button b3 = new Button(c, SWT.PUSH);
		b3.setText(Messages.CConfigWizardPage_13); 
		b3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b3.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				advancedDialog();
			}});

		Group gr = new Group(parent, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gr.setLayoutData(gd);
		gr.setLayout(new FillLayout());
		Label lb = new Label(gr, SWT.NONE);
		lb.setText(COMMENT);

		setControl(parent);
	}

	static public CfgHolder[] getDefaultCfgs(MBSWizardHandler handler) {
		String id = handler.getPropertyId();
		IProjectType pt = handler.getProjectType();
		ArrayList<CfgHolder> out = new ArrayList<CfgHolder>();
		for (IToolChain tc : handler.getSelectedToolChains()){
			CfgHolder[] cfgs = null;
			if (id != null)
				cfgs = CfgHolder.cfgs2items(ManagedBuildManager.getExtensionConfigurations(tc, MBSWizardHandler.ARTIFACT, id));
			else if (pt != null)
				cfgs = CfgHolder.cfgs2items(ManagedBuildManager.getExtensionConfigurations(tc, pt));
			else { // Create default configuration for StdProject
				cfgs = new CfgHolder[1];
				cfgs[0] = new CfgHolder(tc, null);
			}
			if (cfgs == null) return null;

			for (int j=0; j<cfgs.length; j++) {
				if (cfgs[j].isSystem() || (handler.supportedOnly() && !cfgs[j].isSupported())) continue;
				out.add(cfgs[j]);
			}
		}
		return out.toArray(new CfgHolder[out.size()]);
	}

	/**
	 * Checks whether we've already worked with
	 * given set of selected toolchains
	 *
	 * @return true if toolchain(s) is the same as before
	 */
	private boolean isVisited() {
		if (table == null || handler == null)
			return false;

		return Arrays.equals(
				handler.getSelectedToolChains(),
				visitedTCs);
	}

	/**
	 * Returns whether this page's controls currently all contain valid
	 * values.
	 *
	 * @return <code>true</code> if all controls are valid, and
	 *   <code>false</code> if at least one is invalid
	 */
	public boolean isCustomPageComplete() {
		if (!isVisited())
			return true;

		if (table.getItemCount() == 0) {
			errorMessage = Messages.CConfigWizardPage_10; 
			message = errorMessage;
			return false;
		}
		if (tv.getCheckedElements().length == 0) {
			errorMessage = Messages.CConfigWizardPage_11; 
			message = errorMessage;
			return false;
		}
		errorMessage = null;
		message = MESSAGE;
		return true;
	}

	@Override
	public void setVisible(boolean visible) {
		parent.setVisible(visible);
		isVisible = visible;
		if (visible && handler != null && !isVisited()) {
			tv.setInput(CfgHolder.unique(getDefaultCfgs(handler)));
			tv.setAllChecked(true);
			String s = EMPTY_STR;
			visitedTCs = handler.getSelectedToolChains();
			for (int i=0; i < visitedTCs.length; i++) {
				s = s + ((visitedTCs[i] == null) ?
						"" : //$NON-NLS-1$
							visitedTCs[i].getUniqueRealName());
				if (i < visitedTCs.length - 1) s = s + "\n";  //$NON-NLS-1$
			}
			l_chains.setText(s);
			l_projtype.setText(handler.getName());
			setPageComplete(isCustomPageComplete());
			l_chains.getParent().pack();
		}
		if (visible) {
			parent.getParent().layout(true, true);
			update();
		}
	}

	private Label setupLabel(Composite c, String name, int mode) {
		Label l = new Label(c, SWT.WRAP);
		l.setText(name);
		GridData gd = new GridData(mode);
		gd.verticalAlignment = SWT.TOP;
		l.setLayoutData(gd);
		Composite p = l.getParent();
		l.setFont(p.getFont());
		return l;
	}

	@Override
	public String getName() { return TITLE; }
	@Override
	public Control getControl() { return parent; }
	@Override
	public String getErrorMessage() { return errorMessage; }
	@Override
	public String getMessage() { return message; }
	@Override
	public String getTitle()   { return TITLE; }

	protected void update() {
		getWizard().getContainer().updateButtons();
		getWizard().getContainer().updateMessage();
		getWizard().getContainer().updateTitleBar();
	}

	/**
	 * Edit properties
	 */
	private void advancedDialog() {
		if (getWizard() instanceof ICDTCommonProjectWizard) {
			ICDTCommonProjectWizard nmWizard = (ICDTCommonProjectWizard)getWizard();
			IProject newProject = nmWizard.getProject(true, false);
			if (newProject != null) {
				boolean oldManage = CDTPrefUtil.getBool(CDTPrefUtil.KEY_NOMNG);
				// disable manage configurations button
				CDTPrefUtil.setBool(CDTPrefUtil.KEY_NOMNG, true);
				try {
					int res = PreferencesUtil.createPropertyDialogOn(getWizard().getContainer().getShell(), newProject, propertyId, null, null).open();
					if (res != Window.OK) {
						// if user presses cancel, remove the project.
						nmWizard.performCancel();
					}
				} finally {
					CDTPrefUtil.setBool(CDTPrefUtil.KEY_NOMNG, oldManage);
				}
			}
		}
	}

	@Override
	public IWizardPage getNextPage() {
		pagesLoaded = true;
		return MBSCustomPageManager.getNextPage(PAGE_ID);
	}
}
