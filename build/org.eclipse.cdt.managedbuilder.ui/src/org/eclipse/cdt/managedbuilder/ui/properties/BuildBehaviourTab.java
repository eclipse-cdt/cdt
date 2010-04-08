/*******************************************************************************
 * Copyright (c) 2007, 2010  Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICMultiItemsHolder;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildProcessManager;
import org.eclipse.cdt.managedbuilder.internal.core.Builder;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.MultiConfiguration;
import org.eclipse.cdt.newmake.core.IMakeBuilderInfo;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.ICPropertyProvider;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.accessibility.AccessibleListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class BuildBehaviourTab extends AbstractCBuildPropertyTab {
	
	private static final int TRI_STATES_SIZE = 4;
	// Widgets
	//3
	private Button b_stopOnError; // 3
	private Button b_parallel;    // 3

	private Button b_parallelOpt;
	private Button b_parallelNum;
	private Spinner parallelProcesses;

	private Label  title2;
	private Button b_autoBuild; //3
	private Text   t_autoBuild;
	private Button b_cmdBuild; //3
	private Text   t_cmdBuild;
	private Button b_cmdClean; // 3
	private Text   t_cmdClean;	

	private IBuilder bldr;
	private IConfiguration icfg;
	private boolean canModify = true;
	
	protected final int cpuNumber = BuildProcessManager.checkCPUNumber(); 
	
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));

		// Build setting group
		Group g3 = setupGroup(usercomp, Messages.getString("BuilderSettingsTab.9"), 2, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
		GridLayout gl = new GridLayout(2, true);
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		g3.setLayout(gl);
		
		Composite c1 = new Composite(g3, SWT.NONE);
		setupControl(c1, 1, GridData.FILL_BOTH);
		GridData gd = (GridData)c1.getLayoutData();
		gd.verticalSpan = 2;
		gd.verticalIndent = 0;
		c1.setLayoutData(gd);
		gl = new GridLayout(1, false);
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		c1.setLayout(gl);
		
		b_stopOnError = setupCheck(c1, Messages.getString("BuilderSettingsTab.10"), 1, GridData.BEGINNING); //$NON-NLS-1$
		
		Composite c2 = new Composite(g3, SWT.NONE);
		setupControl(c2, 1, GridData.FILL_BOTH);
		gl = new GridLayout(1, false);
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		c2.setLayout(gl);
		
		b_parallel = setupCheck(c2, Messages.getString("BuilderSettingsTab.11"), 1, GridData.BEGINNING); //$NON-NLS-1$

		Composite c3 = new Composite(g3, SWT.NONE);
		setupControl(c3, 1, GridData.FILL_BOTH);
		gl = new GridLayout(2, false);
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		c3.setLayout(gl);
		
		b_parallelOpt= new Button(c3, SWT.RADIO);
		b_parallelOpt.setText(Messages.getString("BuilderSettingsTab.12")); //$NON-NLS-1$
		setupControl(b_parallelOpt, 2, GridData.BEGINNING);
		((GridData)(b_parallelOpt.getLayoutData())).horizontalIndent = 15;
		b_parallelOpt.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				setParallelDef(b_parallelOpt.getSelection());
				updateButtons();
		 }});
		
		b_parallelNum= new Button(c3, SWT.RADIO);
		b_parallelNum.setText(Messages.getString("BuilderSettingsTab.13")); //$NON-NLS-1$
		setupControl(b_parallelNum, 1, GridData.BEGINNING);
		((GridData)(b_parallelNum.getLayoutData())).horizontalIndent = 15;
		b_parallelNum.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				setParallelDef(!b_parallelNum.getSelection());
				updateButtons();
		 }});

		parallelProcesses = new Spinner(c3, SWT.BORDER);
		setupControl(parallelProcesses, 1, GridData.BEGINNING);
		parallelProcesses.setValues(cpuNumber, 1, 10000, 0, 1, 10);
		parallelProcesses.addSelectionListener(new SelectionAdapter () {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setParallelNumber(parallelProcesses.getSelection());
				updateButtons();
			}
		});
		
		// Workbench behaviour group
		AccessibleListener makeTargetLabelAccessibleListener = new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = Messages.getString("BuilderSettingsTab.16"); //$NON-NLS-1$
			}
		};
		Group g4 = setupGroup(usercomp, Messages.getString("BuilderSettingsTab.14"), 3, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
		setupLabel(g4, Messages.getString("BuilderSettingsTab.15"), 1, GridData.BEGINNING); //$NON-NLS-1$
		title2 = setupLabel(g4, Messages.getString("BuilderSettingsTab.16"), 2, GridData.BEGINNING); //$NON-NLS-1$
		b_autoBuild = setupCheck(g4, Messages.getString("BuilderSettingsTab.17"), 1, GridData.BEGINNING); //$NON-NLS-1$
		t_autoBuild = setupBlock(g4, b_autoBuild);
		t_autoBuild.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (canModify)
					setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_AUTO, t_autoBuild.getText());
			}} );
		t_autoBuild.getAccessible().addAccessibleListener(makeTargetLabelAccessibleListener);
		setupLabel(g4, Messages.getString("BuilderSettingsTab.18"), 3, GridData.BEGINNING); //$NON-NLS-1$
		b_cmdBuild = setupCheck(g4, Messages.getString("BuilderSettingsTab.19"), 1, GridData.BEGINNING); //$NON-NLS-1$
		t_cmdBuild = setupBlock(g4, b_cmdBuild);
		t_cmdBuild.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (canModify)
					setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL, t_cmdBuild.getText());
			}} );
		t_cmdBuild.getAccessible().addAccessibleListener(makeTargetLabelAccessibleListener);
		b_cmdClean = setupCheck(g4, Messages.getString("BuilderSettingsTab.20"), 1, GridData.BEGINNING); //$NON-NLS-1$
		t_cmdClean = setupBlock(g4, b_cmdClean);
		t_cmdClean.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (canModify)
					setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_CLEAN, t_cmdClean.getText());
			}} );
		t_cmdClean.getAccessible().addAccessibleListener(makeTargetLabelAccessibleListener);
	}

	/**
	 * 
	 * @return:
	 * Mode 0:
	 *    0: bld.isManagedBuildOn()
	 *    1: bld.isDefaultBuildCmd()
	 *    2: bld.canKeepEnvironmentVariablesInBuildfile()
	 *    3: bld.keepEnvironmentVariablesInBuildfile()
	 * Mode 1: 
	 *    0: isStopOnError
	 *    1: supportsStopOnError(true)
	 *    2: bld.supportsStopOnError(false)  
	 *    3: cfg.getInternalBuilderParallel()
	 * Mode 2:
	 *    0: b.isAutoBuildEnable()
	 *    1: b.isIncrementalBuildEnabled()
	 *    2: b.isCleanBuildEnabled()
	 *    3: getParallelDef()   
	 */
	 static int[] calc3states(ICPropertyProvider p, 
			 IConfiguration c,
			 int mode) {
		if (p.isMultiCfg() &&
			c instanceof ICMultiItemsHolder) 
		{ 
			boolean p0 = (mode == 0);
			boolean p1 = (mode == 1);
			
			IConfiguration[] cfs = (IConfiguration[])((ICMultiItemsHolder)c).getItems();
			IBuilder b = cfs[0].getBuilder();
			int[]   res = new int[TRI_STATES_SIZE];
			boolean[] x = new boolean[TRI_STATES_SIZE];
			x[0] = p0 ? b.isManagedBuildOn() : 
				(p1 ? b.isStopOnError() : b.isAutoBuildEnable());
			x[1] = p0 ? b.isDefaultBuildCmd(): 
				(p1 ? b.supportsStopOnError(true) : b.isIncrementalBuildEnabled() );
			x[2] = p0 ? b.canKeepEnvironmentVariablesInBuildfile() : 
				(p1 ? b.supportsStopOnError(false) : b.isCleanBuildEnabled());
			x[3] = p0 ? b.keepEnvironmentVariablesInBuildfile() : 
				( p1 ? ((Configuration)cfs[0]).getInternalBuilderParallel() : getParallelDef(c));
			for (int i=1; i<cfs.length; i++) {
				b = cfs[i].getBuilder();
				if (x[0] != (p0 ? b.isManagedBuildOn() : 
					(p1 ? b.isStopOnError() : b.isAutoBuildEnable())))
					res[0] = TRI_UNKNOWN;
				if (x[1] != (p0 ? b.isDefaultBuildCmd() : 
					(p1 ? b.supportsStopOnError(true) : b.isIncrementalBuildEnabled())))
					res[1] = TRI_UNKNOWN;
				if (x[2] != (p0 ? b.canKeepEnvironmentVariablesInBuildfile() : 
					(p1 ? b.supportsStopOnError(false) : b.isCleanBuildEnabled())))
					res[2] = TRI_UNKNOWN;
				if (x[3] != (p0 ? b.keepEnvironmentVariablesInBuildfile() : 
					(p1 ? ((Configuration)cfs[i]).getInternalBuilderParallel() : getParallelDef(c))))
					res[3] = TRI_UNKNOWN;
			}
			for (int i=0; i<TRI_STATES_SIZE; i++) {
				if (res[i] != TRI_UNKNOWN)
					res[i] = x[i] ? TRI_YES : TRI_NO;
			}
			return res;
		} else
			return null;
	}
	
	/**
	 * sets widgets states
	 */
	@Override
	protected void updateButtons() {
		bldr = icfg.getEditableBuilder();
		canModify = false;
		int[] extStates = calc3states(page, icfg, 1);
		
		if (extStates != null) {
			setTriSelection(b_stopOnError, extStates[0]);
			b_stopOnError.setEnabled(
					extStates[1] == TRI_YES &&
					extStates[2] == TRI_YES);
		} else {
			setTriSelection(b_stopOnError, bldr.isStopOnError());
			b_stopOnError.setEnabled(
					bldr.supportsStopOnError(true) &&
					bldr.supportsStopOnError(false));
		} 
		// parallel
		if (extStates == null) // no extended states
			setTriSelection(b_parallel, getInternalBuilderParallel());
		else
			setTriSelection(b_parallel, extStates[3]);
		
		int n = getParallelNumber();
		if (n < 0) n = -n;
		parallelProcesses.setSelection(n);

		b_parallel.setVisible(bldr.supportsParallelBuild());
		b_parallelOpt.setVisible(bldr.supportsParallelBuild());
		b_parallelNum.setVisible(bldr.supportsParallelBuild());
		parallelProcesses.setVisible(bldr.supportsParallelBuild());

		extStates = calc3states(page, icfg, 2);
		if (extStates == null) {
			setTriSelection(b_autoBuild, bldr.isAutoBuildEnable());
			setTriSelection(b_cmdBuild, bldr.isIncrementalBuildEnabled());
			setTriSelection(b_cmdClean, bldr.isCleanBuildEnabled());
			b_parallelOpt.setSelection(getParallelDef(icfg));
			b_parallelNum.setSelection(!getParallelDef(icfg));
		} else {
			setTriSelection(b_autoBuild, extStates[0]);
			setTriSelection(b_cmdBuild, extStates[1]);
			setTriSelection(b_cmdClean, extStates[2]);
			if (extStates[3] == TRI_UNKNOWN) {
				b_parallelOpt.setSelection(false);
				b_parallelNum.setSelection(false);
			} else {
				b_parallelOpt.setSelection(getParallelDef(icfg));
				b_parallelNum.setSelection(!getParallelDef(icfg));
			}
		}
		
		if (page.isMultiCfg()) {
			MultiConfiguration mc = (MultiConfiguration)icfg;
			t_autoBuild.setText(mc.getBuildAttribute(IBuilder.BUILD_TARGET_AUTO, EMPTY_STR));
			t_cmdBuild.setText(mc.getBuildAttribute(IBuilder.BUILD_TARGET_INCREMENTAL, EMPTY_STR));
			t_cmdClean.setText(mc.getBuildAttribute(IBuilder.BUILD_TARGET_CLEAN, EMPTY_STR));
		} else {
			t_autoBuild.setText(bldr.getBuildAttribute(IBuilder.BUILD_TARGET_AUTO, EMPTY_STR));
			t_cmdBuild.setText(bldr.getBuildAttribute(IBuilder.BUILD_TARGET_INCREMENTAL, EMPTY_STR));
			t_cmdClean.setText(bldr.getBuildAttribute(IBuilder.BUILD_TARGET_CLEAN, EMPTY_STR));
		}
		
		boolean external = ! isInternalBuilderEnabled(); 
		boolean parallel = b_parallel.getSelection();

		b_parallelNum.setEnabled(parallel);
		b_parallelOpt.setEnabled(parallel);
		parallelProcesses.setEnabled(parallel & b_parallelNum.getSelection());
		
		title2.setVisible(external);
		t_autoBuild.setVisible(external);
		((Control)t_autoBuild.getData()).setVisible(external);
		t_cmdBuild.setVisible(external);
		((Control)t_cmdBuild.getData()).setVisible(external);
		t_cmdClean.setVisible(external);
		((Control)t_cmdClean.getData()).setVisible(external);
		
		if (external) {
			checkPressed(b_autoBuild, false);
			checkPressed(b_cmdBuild, false);
			checkPressed(b_cmdClean, false);
		}
		canModify = true;
	}
	
	/**
	 * Sets up text + corresponding button
	 * Checkbox can be implemented either by Button or by TriButton
	 */
	private Text setupBlock(Composite c, Control check) {
		Text t = setupText(c, 1, GridData.FILL_HORIZONTAL);
		Button b = setupButton(c, VARIABLESBUTTON_NAME, 1, GridData.END);
		b.setData(t); // to get know which text is affected
		t.setData(b); // to get know which button to enable/disable
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buttonVarPressed(event);
			}});
		if (check != null) check.setData(t);
		return t;
	}
	
	/*
	 * Unified handler for "Variables" buttons
	 */
	private void buttonVarPressed(SelectionEvent e) {
		Widget b = e.widget;
		if (b == null || b.getData() == null) return; 
		if (b.getData() instanceof Text) {
			String x = AbstractCPropertyTab.getVariableDialog(usercomp.getShell(), getResDesc().getConfiguration());
			if (x != null) ((Text)b.getData()).insert(x);
		}
	}
	
	@Override
	public void checkPressed(SelectionEvent e) {
		checkPressed((Control)e.widget, true);
		updateButtons();
	}
	
	private void checkPressed(Control b, boolean needsUpdate) {	
		if (b == null) return;
		
		boolean val = false;
		if (b instanceof Button) val = ((Button)b).getSelection();
		
		if (b.getData() instanceof Text) {
			Text t = (Text)b.getData();
			t.setEnabled(val);
			if (t.getData() != null && t.getData() instanceof Control) {
				Control c = (Control)t.getData();
				c.setEnabled(val);
			}
		}
		if (needsUpdate)
			setValue(b, val);
	}

	/*
	 * Performs common settings for all controls
	 * (Copy from config to widgets)
	 * @param cfgd - 
	 */
	
	@Override
	public void updateData(ICResourceDescription cfgd) {
		if (cfgd == null) return;
		icfg = getCfg(cfgd.getConfiguration());
		updateButtons();
	}

	@Override
	public void performApply(ICResourceDescription src, ICResourceDescription dst) {
		apply(src, dst, page.isMultiCfg());
	}

	static void apply(ICResourceDescription src, ICResourceDescription dst, boolean multi) {
		if (multi) {
			ICMultiConfigDescription mc1 = (ICMultiConfigDescription)src.getConfiguration();
			ICMultiConfigDescription mc2 = (ICMultiConfigDescription)dst.getConfiguration();
			ICConfigurationDescription[] cds1 = (ICConfigurationDescription[])mc1.getItems();
			ICConfigurationDescription[] cds2 = (ICConfigurationDescription[])mc2.getItems();
			for (int i=0; i<cds1.length; i++) 
				applyToCfg(cds1[i], cds2[i]);
		} else 
			applyToCfg(src.getConfiguration(), dst.getConfiguration());
	}
	
	private static void applyToCfg(ICConfigurationDescription c1, ICConfigurationDescription c2) {
		Configuration cfg01 = (Configuration)getCfg(c1);
		Configuration cfg02 = (Configuration)getCfg(c2);
		cfg02.enableInternalBuilder(cfg01.isInternalBuilderEnabled());
		copyBuilders(cfg01.getBuilder(), cfg02.getEditableBuilder());
	}
	
	static void copyBuilders(IBuilder b1, IBuilder b2) {  	
		try {
			b2.setUseDefaultBuildCmd(b1.isDefaultBuildCmd());
			if (!b1.isDefaultBuildCmd()) {
				b2.setCommand(b1.getCommand());
				b2.setArguments(b1.getArguments());
			} else {
				b2.setCommand(null);
				b2.setArguments(null);
			}
			b2.setStopOnError(b1.isStopOnError());
			b2.setParallelBuildOn(b1.isParallelBuildOn());
			b2.setParallelizationNum(b1.getParallelizationNum());
			if (b2.canKeepEnvironmentVariablesInBuildfile())
				b2.setKeepEnvironmentVariablesInBuildfile(b1.keepEnvironmentVariablesInBuildfile());
			((Builder)b2).setBuildPath(((Builder)b1).getBuildPathAttribute());
		
			b2.setAutoBuildEnable((b1.isAutoBuildEnable()));
			b2.setBuildAttribute(IBuilder.BUILD_TARGET_AUTO, (b1.getBuildAttribute(IBuilder.BUILD_TARGET_AUTO, EMPTY_STR)));
			b2.setCleanBuildEnable(b1.isCleanBuildEnabled());
			b2.setBuildAttribute(IBuilder.BUILD_TARGET_CLEAN, (b1.getBuildAttribute(IBuilder.BUILD_TARGET_CLEAN, EMPTY_STR)));
			b2.setIncrementalBuildEnable(b1.isIncrementalBuildEnabled());
			b2.setBuildAttribute(IBuilder.BUILD_TARGET_INCREMENTAL, (b1.getBuildAttribute(IBuilder.BUILD_TARGET_INCREMENTAL, EMPTY_STR)));
		
			b2.setManagedBuildOn(b1.isManagedBuildOn());
		} catch (CoreException ex) {
			ManagedBuilderUIPlugin.log(ex);
		}
	}
	// This page can be displayed for project only
	@Override
	public boolean canBeVisible() {
		return page.isForProject() || page.isForPrefs();
	}
	
	@Override
	public void setVisible (boolean b) {
		super.setVisible(b);
	}

	
	@Override
	protected void performDefaults() {
		if (icfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[])((IMultiConfiguration)icfg).getItems();
			for (int i=0; i<cfs.length; i++) {
				IBuilder b = cfs[i].getEditableBuilder();
				copyBuilders(b.getSuperClass(), b);
			}
		} else 
			copyBuilders(bldr.getSuperClass(), bldr);
		updateData(getResDesc());
	}
	
	private static boolean getParallelDef(IConfiguration cfg) {
		if (cfg instanceof Configuration) 
			return ((Configuration)cfg).getParallelDef();
		if (cfg instanceof IMultiConfiguration)
			return ((IMultiConfiguration)cfg).getParallelDef();
		return false;
	}
	
	private void setParallelDef(boolean def) {
		if (icfg instanceof Configuration) 
			((Configuration)icfg).setParallelDef(def);
		if (icfg instanceof IMultiConfiguration)
			((IMultiConfiguration)icfg).setParallelDef(def);
	}
	
	private int getParallelNumber() {
		if (icfg instanceof Configuration) 
			return ((Configuration)icfg).getParallelNumber();
		if (icfg instanceof IMultiConfiguration)
			return ((IMultiConfiguration)icfg).getParallelNumber();
		return 0;
	}
	private void setParallelNumber(int num) {
		if (icfg instanceof Configuration) 
			((Configuration)icfg).setParallelNumber(num);
		if (icfg instanceof IMultiConfiguration)
			((IMultiConfiguration)icfg).setParallelNumber(num);
	}
	
	private boolean getInternalBuilderParallel() {
		if (icfg instanceof Configuration) 
			return ((Configuration)icfg).getInternalBuilderParallel();
		if (icfg instanceof IMultiConfiguration)
			return ((IMultiConfiguration)icfg).getInternalBuilderParallel();
		return false;
	}

	private boolean isInternalBuilderEnabled() {
		if (icfg instanceof Configuration) 
			return ((Configuration)icfg).isInternalBuilderEnabled();
		if (icfg instanceof IMultiConfiguration)
			return ((IMultiConfiguration)icfg).isInternalBuilderEnabled();
		return false;
	}
	
	private void setBuildAttribute(String name, String value) {
		try {
			if (icfg instanceof IMultiConfiguration) {
				IConfiguration[] cfs = (IConfiguration[])((IMultiConfiguration)icfg).getItems();
				for (int i=0; i<cfs.length; i++) {
					IBuilder b = cfs[i].getEditableBuilder();
					b.setBuildAttribute(name, value);
				}
			} else {
				icfg.getEditableBuilder().setBuildAttribute(name, value);
			}
		} catch (CoreException e) {
			ManagedBuilderUIPlugin.log(e);
		}		
	}
	
	private void setValue(Control b, boolean val) {
		try {
			if (icfg instanceof IMultiConfiguration) {
				IConfiguration[] cfs = (IConfiguration[])((IMultiConfiguration)icfg).getItems();
				for (int i=0; i<cfs.length; i++) {
					IBuilder bld = cfs[i].getEditableBuilder();
					if (b == b_autoBuild) {
						bld.setAutoBuildEnable(val);				
					} else if (b == b_cmdBuild) {
						bld.setIncrementalBuildEnable(val);				
					} else if (b == b_cmdClean) {
						bld.setCleanBuildEnable(val);
					} else if (b == b_stopOnError) {
						bld.setStopOnError(val);
					} else if (b == b_parallel) {
						bld.setParallelBuildOn(val);
					}
				}
			} else {
				if (b == b_autoBuild) {
					bldr.setAutoBuildEnable(val);				
				} else if (b == b_cmdBuild) {
					bldr.setIncrementalBuildEnable(val);				
				} else if (b == b_cmdClean) {
					bldr.setCleanBuildEnable(val);
				} else if (b == b_stopOnError) {
					bldr.setStopOnError(val);
				} else if (b == b_parallel) {
					bldr.setParallelBuildOn(val);
				}
			}
		} catch (CoreException e) {
			ManagedBuilderUIPlugin.log(e);
		}
	}
}
