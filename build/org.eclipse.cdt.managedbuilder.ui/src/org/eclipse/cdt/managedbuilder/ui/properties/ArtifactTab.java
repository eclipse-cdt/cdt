/*******************************************************************************
 * Copyright (c) 2007 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildProperty;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.IBuildObjectProperties;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;


public class ArtifactTab extends AbstractCBuildPropertyTab {

	public static final String PROPERTY = ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID; 
	private Label l4;
	private Text t2, t3, t4;
	private Combo c1;
	private int savedPos = -1; // current project type
	private IConfiguration fCfg; 
	private IBuildObjectProperties fProp; 
	private IBuildPropertyValue[] values;
	private ITool targetTool;
	
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));
		
		Label l1 = new Label(usercomp, SWT.NONE);
		l1.setLayoutData(new GridData(GridData.BEGINNING));
		l1.setText(Messages.getString("ArtifactTab.0")); //$NON-NLS-1$
		c1 = new Combo(usercomp, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);
		c1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		c1.addSelectionListener(new SelectionAdapter() {
		public void widgetSelected(SelectionEvent e) {
			typeChanged();
		}});
		
		Label l2 = new Label(usercomp, SWT.NONE);
		l2.setLayoutData(new GridData(GridData.BEGINNING));
		l2.setText(Messages.getString("ArtifactTab.1")); //$NON-NLS-1$
		t2 = new Text(usercomp, SWT.BORDER);
		t2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		t2.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fCfg.setArtifactName(t2.getText());
			}} );
		Label l3 = new Label(usercomp, SWT.NONE);
		l3.setLayoutData(new GridData(GridData.BEGINNING));
		l3.setText(Messages.getString("ArtifactTab.2")); //$NON-NLS-1$
		t3 = new Text(usercomp, SWT.BORDER);
		t3.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		t3.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fCfg.setArtifactExtension(t3.getText());
			}} );
		
		l4 = new Label(usercomp, SWT.NONE);
		l4.setLayoutData(new GridData(GridData.BEGINNING));
		l4.setText(Messages.getString("ArtifactTab.3")); //$NON-NLS-1$
		t4 = new Text(usercomp, SWT.BORDER);
		t4.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		t4.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if(targetTool != null)
					targetTool.setOutputPrefixForPrimaryOutput(t4.getText());
			}} );
		updateData(getResDesc());
	}

	private void typeChanged() {
		if (fProp == null) return;
		int n = c1.getSelectionIndex();
		if (n != savedPos) {
			savedPos = n;
			try {
				fProp.setProperty(PROPERTY, values[n].getId());
			} catch (CoreException ex) {
				ManagedBuilderUIPlugin.log(ex);
			}
			updateData(getResDesc());
		}
	}
	
	public void updateData(ICResourceDescription cfgd) {
		if (cfgd == null) return;
		fCfg = getCfg();

		fProp = fCfg.getBuildProperties();
		values = fProp.getSupportedValues(PROPERTY);
		c1.removeAll();
		c1.setData(values);
		for (int i=0; i<values.length; i++) {
			c1.add(values[i].getName());
		}
		c1.setText(EMPTY_STR);
		IBuildProperty pr = fProp.getProperty(PROPERTY);
		if (pr != null) {
			String s = pr.getValue().getId();
			for (int i=0; i<values.length; i++) {
				if (s.equals(values[i].getId())) {
					c1.select(i);
					savedPos = i;
					break;
				}
			}
		}
		
		String s = fCfg.getArtifactName();
		if (s == null || s.trim().length() == 0) {
			s = getResDesc().getConfiguration().getProjectDescription().getName();
			getCfg().setArtifactName(CWizardHandler.removeSpaces(s));
		}
		t2.setText(s);
		t3.setText(fCfg.getArtifactExtension());
		
		targetTool = fCfg.calculateTargetTool();
		if(targetTool != null){
			if (l4 != null) l4.setVisible(true);
			if (t4 != null) {
				t4.setVisible(true);
				t4.setText(targetTool.getOutputPrefix());
			}
		} else {
			if (l4 != null) l4.setVisible(false);
			if (t4 != null) t4.setVisible(false);
		}
	}
	
	public void performApply(ICResourceDescription src, ICResourceDescription dst) {
		IConfiguration cfg1 = getCfg(src.getConfiguration());
		IConfiguration cfg2 = getCfg(dst.getConfiguration());
		cfg2.setArtifactName(cfg1.getArtifactName());
		cfg2.setArtifactExtension(cfg1.getArtifactExtension());
		
		ITool t1 = cfg1.calculateTargetTool();
		ITool t2 = cfg2.calculateTargetTool();
		if (t1 != null && t2 != null) 
			t2.setOutputPrefixForPrimaryOutput(t1.getOutputPrefix());
			
		try {
			IBuildProperty bp = cfg1.getBuildProperties().getProperty(PROPERTY);
			if (bp != null) {
				IBuildPropertyValue bv = bp.getValue();
				if (bv != null) {
					String s = bv.getId();
					cfg2.getBuildProperties().setProperty(PROPERTY, s);
				}
			}
		} catch (CoreException e) {
			CUIPlugin.getDefault().log(e);
		}
	}
	
	public void performDefaults() {
		fCfg.setArtifactName(fCfg.getManagedProject().getDefaultArtifactName());
		fCfg.setArtifactExtension(null);
		if (targetTool != null)
			targetTool.setOutputPrefixForPrimaryOutput(null);
		updateData(getResDesc());
	}

	public boolean canBeVisible() {
		if (page.isForProject())
			return getCfg().getBuilder().isManagedBuildOn();
		else
			return false;
	}
	protected void updateButtons() {} // Do nothing. No buttons to update.
}
