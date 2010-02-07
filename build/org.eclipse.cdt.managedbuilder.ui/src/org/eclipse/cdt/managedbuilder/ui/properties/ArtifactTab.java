/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Intel Corporation - Initial API and implementation
 *    James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.Set;
import java.util.TreeSet;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiItemsHolder;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
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


/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ArtifactTab extends AbstractCBuildPropertyTab {

	public static final String PROPERTY = ManagedBuildManager.BUILD_ARTEFACT_TYPE_PROPERTY_ID;

	private Label l4;
	private Combo t2, t3, t4;
	private Combo c1;
	private int savedPos = -1; // current project type
	private IConfiguration fCfg; 
	private IBuildPropertyValue[] values;
	private ITool tTool;
	private boolean canModify = true;
	
	private enum FIELD {NAME, EXT, PREF}
	
	private Set<String> set2 = new TreeSet<String>();
	private Set<String> set3 = new TreeSet<String>();
	private Set<String> set4 = new TreeSet<String>();

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));

		Label l1 = new Label(usercomp, SWT.NONE);
		l1.setLayoutData(new GridData(GridData.BEGINNING));
		l1.setText(Messages.getString("ArtifactTab.0")); //$NON-NLS-1$
		c1 = new Combo(usercomp, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);
		c1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		c1.addSelectionListener(new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			typeChanged();
		}});
		c1.setOrientation(SWT.LEFT_TO_RIGHT);
		
		Label l2 = new Label(usercomp, SWT.NONE);
		l2.setLayoutData(new GridData(GridData.BEGINNING));
		l2.setText(Messages.getString("ArtifactTab.1")); //$NON-NLS-1$
		t2 = setCombo(FIELD.NAME, set2);
		t2.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (canModify)
					fCfg.setArtifactName(t2.getText());
			}} );
		
		Label l3 = new Label(usercomp, SWT.NONE);
		l3.setLayoutData(new GridData(GridData.BEGINNING));
		l3.setText(Messages.getString("ArtifactTab.2")); //$NON-NLS-1$
		t3 = setCombo(FIELD.EXT, set3);
		t3.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (canModify)
					fCfg.setArtifactExtension(t3.getText());
			}} );
		
		l4 = new Label(usercomp, SWT.NONE);
		l4.setLayoutData(new GridData(GridData.BEGINNING));
		l4.setText(Messages.getString("ArtifactTab.3")); //$NON-NLS-1$
		t4 = setCombo(FIELD.PREF, set4);
		t4.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (canModify) {
					if(tTool != null)
						tTool.setOutputPrefixForPrimaryOutput(t4.getText());
					else if (fCfg instanceof IMultiConfiguration)
						((IMultiConfiguration)fCfg).setOutputPrefixForPrimaryOutput(t4.getText());
				}
			}} );

		updateData(getResDesc());
	}

	private void typeChanged() {
		int n = c1.getSelectionIndex();
		if (n != savedPos) {
			setProjectType(n);
			savedPos = n;
			updateData(getResDesc());
		}
	}

	private void setProjectType(int n) {
		try {
			String s = values[n].getId();
			fCfg.setBuildArtefactType(s);
		} catch (BuildException ex) {
			ManagedBuilderUIPlugin.log(ex);
		}
	}
	
	@Override
	public void updateData(ICResourceDescription cfgd) {
		if (cfgd == null) return;
		fCfg = getCfg();
		if (page.isMultiCfg()) {
			values = ((IMultiConfiguration)fCfg).getSupportedValues(PROPERTY);
		} else {
			values = fCfg.getBuildProperties().getSupportedValues(PROPERTY);
		}
		c1.removeAll();
		c1.setData(values);
		for (int i=0; i<values.length; i++) {
			c1.add(values[i].getName());
		}
		c1.setText(EMPTY_STR);
		IBuildPropertyValue pv = fCfg.getBuildArtefactType();
		if (pv != null) {
			String s = pv.getId();
			for (int i=0; i<values.length; i++) {
				if (s.equals(values[i].getId())) {
					c1.select(i);
					savedPos = i;
					break;
				}
			}
		}
		
		updateCombo(t2);
		updateCombo(t3);
		updateCombo(t4);
		
		String s = fCfg.getArtifactName();
		if (! page.isMultiCfg() && (s == null || s.trim().length() == 0)) {
			s = getResDesc().getConfiguration().getProjectDescription().getName();
			getCfg().setArtifactName(s);
		}
		
		canModify = false;
		
		t2.setText(s);
		t3.setText(fCfg.getArtifactExtension());
		
		if (page.isMultiCfg()) {
			if (l4 != null) 
				l4.setVisible(true);
			if (t4 != null) {
				t4.setVisible(true);
				t4.setText(((IMultiConfiguration)fCfg).getToolOutputPrefix());
			}
		} else {
			tTool = fCfg.calculateTargetTool();
			if(tTool != null){
				if (l4 != null) 
					l4.setVisible(true);
				if (t4 != null) {
					t4.setVisible(true);
					t4.setText(tTool.getOutputPrefix());
				}
			} else {
				if (l4 != null) 
					l4.setVisible(false);
				if (t4 != null) 
					t4.setVisible(false);
			}
		}
		canModify = true;
	}
	
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		IConfiguration cfg1 = getCfg(src.getConfiguration());
		IConfiguration cfg2 = getCfg(dst.getConfiguration());
		cfg2.setArtifactName(cfg1.getArtifactName());
		cfg2.setArtifactExtension(cfg1.getArtifactExtension());
		
		ITool t1 = cfg1.calculateTargetTool();
		ITool t2 = cfg2.calculateTargetTool();
		if (t1 != null && t2 != null) 
			t2.setOutputPrefixForPrimaryOutput(t1.getOutputPrefix());
			
		try {
			IBuildPropertyValue bv = cfg1.getBuildArtefactType();
			if (bv != null)
				cfg2.setBuildArtefactType(bv.getId());
		} catch (BuildException e) {
			ManagedBuilderUIPlugin.log(e);
		}
	}
	
	@Override
	protected void performDefaults() {
		fCfg.setArtifactName(fCfg.getManagedProject().getDefaultArtifactName());
		fCfg.setArtifactExtension(null);
		// workaround for bad extension setting (always "exe"):
		// set wrong project type temporary 
		// and then set right one back
		if (c1.getItemCount() > 1) {
			int right = c1.getSelectionIndex();
			int wrong = (right == 0) ? 1 : 0;
			setProjectType(wrong);
			setProjectType(right);
		}
		if (tTool != null)
			tTool.setOutputPrefixForPrimaryOutput(null);
		else if (fCfg instanceof IMultiConfiguration)
			((IMultiConfiguration)fCfg).setOutputPrefixForPrimaryOutput(null);
		updateData(getResDesc());
	}

	@Override
	public boolean canBeVisible() {
		if (page.isForProject()) {
			if (page.isMultiCfg()) {
				ICMultiItemsHolder mih = (ICMultiItemsHolder)getCfg();
				IConfiguration[] cfs = (IConfiguration[])mih.getItems();
				for (int i=0; i<cfs.length; i++) {
					if (cfs[i].getBuilder().isManagedBuildOn())
						return true;
				}
				return false;
			} else
				return getCfg().getBuilder().isManagedBuildOn();
		}
		else
			return false;
	}
	@Override
	protected void updateButtons() {} // Do nothing. No buttons to update.
	
	private Combo setCombo(FIELD field, Set<String> set) {
		Combo combo = new Combo(usercomp, SWT.BORDER);
		combo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		combo.setData(ENUM, field);
		combo.setData(SSET, set);
		updateCombo(combo);
		return combo;
	}
	
	@SuppressWarnings("unchecked")
	private void updateCombo(Combo combo) {
		FIELD field = (FIELD)combo.getData(ENUM);
		Set<String> set   = (Set<String>)combo.getData(SSET);
		if (field == null || set == null)
			return;
		
		canModify = false;
		String oldStr = combo.getText();
		combo.removeAll();
		for (ICConfigurationDescription cf : page.getCfgsEditable()) {
			IConfiguration c = getCfg(cf);
			String s = null;
			switch (field) {
			case NAME:
				s = c.getArtifactName(); 
				break;
			case EXT:
				s = c.getArtifactExtension();
				break;
			case PREF:
				ITool t = c.calculateTargetTool();
				if(t != null)
					s = t.getOutputPrefix();
			}
			if (s != null && s.trim().length() > 0)
				set.add(s.trim());
		}
		if (set.size() > 0) 
			combo.setItems(set.toArray(new String[set.size()]));
		combo.setText(oldStr);
		canModify = true;
	}
}
