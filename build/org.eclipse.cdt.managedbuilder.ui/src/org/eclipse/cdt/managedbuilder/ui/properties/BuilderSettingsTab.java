/*******************************************************************************
 * Copyright (c) 2007, 2010 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Intel Corporation - Initial API and implementation
 * IBM Corporation
 * Dmitry Kozlov (CodeSourcery) - save build output preferences
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.internal.core.BuildOutputLogger;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IMultiConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.MultiConfiguration;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.CDTPropertyManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;


/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class BuilderSettingsTab extends AbstractCBuildPropertyTab {
	// Widgets
	//1
	private Button b_useDefault;
	private Combo  c_builderType;
	private Text   t_buildCmd; 
	//2
	private Button b_genMakefileAuto;
	private Button b_expandVars;
	//5
	private Text   t_dir;
	private Button b_dirWsp;
	private Button b_dirFile;
	private Button b_dirVars;
	private Group group_dir;
	private Text   saveBuildFilename;
	private Button saveBuildFileButton;
	private Button saveBuildCheckbox;
	private Group buildOutputGroup;
	
	private IBuilder bldr;
	private IConfiguration icfg;
	private boolean canModify = true;
	
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));

		// Builder group
		Group g1 = setupGroup(usercomp, Messages.getString("BuilderSettingsTab.0"), 3, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
		setupLabel(g1, Messages.getString("BuilderSettingsTab.1"), 1, GridData.BEGINNING); //$NON-NLS-1$
		c_builderType = new Combo(g1, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.BORDER);
		setupControl(c_builderType, 2, GridData.FILL_HORIZONTAL);
		c_builderType.add(Messages.getString("BuilderSettingsTab.2")); //$NON-NLS-1$
		c_builderType.add(Messages.getString("BuilderSettingsTab.3")); //$NON-NLS-1$
		c_builderType.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				enableInternalBuilder(c_builderType.getSelectionIndex() == 1);
				updateButtons();
		 }});
		
		b_useDefault = setupCheck(g1, Messages.getString("BuilderSettingsTab.4"), 3, GridData.BEGINNING); //$NON-NLS-1$
		
		setupLabel(g1, Messages.getString("BuilderSettingsTab.5"), 1, GridData.BEGINNING); //$NON-NLS-1$
		t_buildCmd = setupBlock(g1, b_useDefault);
		t_buildCmd.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (! canModify)
					return;
	        	String fullCommand = t_buildCmd.getText().trim();
	        	String buildCommand = parseMakeCommand(fullCommand);
	        	String buildArgs = fullCommand.substring(buildCommand.length()).trim();
	        	if(!buildCommand.equals(bldr.getCommand()) 
	        			|| !buildArgs.equals(bldr.getArguments())){
		        	setCommand(buildCommand);
		        	setArguments(buildArgs);
		        }
			}});
				
		Group g2 = setupGroup(usercomp, Messages.getString("BuilderSettingsTab.6"), 2, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
		((GridLayout)(g2.getLayout())).makeColumnsEqualWidth = true;
		
		b_genMakefileAuto = setupCheck(g2, Messages.getString("BuilderSettingsTab.7"), 1, GridData.BEGINNING); //$NON-NLS-1$
		b_expandVars  = setupCheck(g2, Messages.getString("BuilderSettingsTab.8"), 1, GridData.BEGINNING); //$NON-NLS-1$

		// Build location group
		group_dir = setupGroup(usercomp, Messages.getString("BuilderSettingsTab.21"), 2, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
		setupLabel(group_dir, Messages.getString("BuilderSettingsTab.22"), 1, GridData.BEGINNING); //$NON-NLS-1$
		t_dir = setupText(group_dir, 1, GridData.FILL_HORIZONTAL);
		t_dir.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (canModify)
					setBuildPath(t_dir.getText());
			}} );
		Composite c = new Composite(group_dir, SWT.NONE);
		setupControl(c, 2, GridData.FILL_HORIZONTAL);
		GridLayout f = new GridLayout(4, false);
		c.setLayout(f);
		Label dummy = new Label(c, 0);
		dummy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		b_dirWsp = setupBottomButton(c, WORKSPACEBUTTON_NAME);
		b_dirFile = setupBottomButton(c, FILESYSTEMBUTTON_NAME);
		b_dirVars = setupBottomButton(c, VARIABLESBUTTON_NAME);
		
		// Save build output group
		if ( page.isForProject() ) {
			buildOutputGroup = setupGroup(usercomp, 
					Messages.getString("BuilderSettingsTab.23"), //$NON-NLS-1$ 
					3, GridData.FILL_HORIZONTAL); 
			@SuppressWarnings("unused")
			Label l = setupLabel(buildOutputGroup, Messages.getString("BuilderSettingsTab.24"), 1, GridData.BEGINNING); //$NON-NLS-1$
			saveBuildFilename = setupText(buildOutputGroup, 1, GridData.FILL_HORIZONTAL);
			saveBuildFilename.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if ( BuildOutputLogger.canWriteToFile(saveBuildFilename.getText())) {
					saveBuildCheckbox.setEnabled(true);
					//saveBuildCheckbox.setSelection(true);
				} else {
					saveBuildCheckbox.setEnabled(false);
					saveBuildCheckbox.setSelection(false);
				}
				icfg.setBuildLogFilename(saveBuildFilename.getText());
				icfg.setSavingBuildLog(saveBuildCheckbox.getSelection());
			}} );
			saveBuildFileButton = new Button(buildOutputGroup, SWT.PUSH);
			saveBuildFileButton.setText(Messages.getString("BuilderSettingsTab.25")); //$NON-NLS-1$
			saveBuildFileButton.setData(saveBuildFilename);
			saveBuildFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buttonVarPressed(event);
			}});
			saveBuildCheckbox = new Button(buildOutputGroup, SWT.CHECK);
			saveBuildCheckbox.setText(Messages.getString("BuilderSettingsTab.26")); //$NON-NLS-1$
			saveBuildCheckbox.setEnabled(false);
			saveBuildCheckbox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					if ( ((Control)event.widget) == saveBuildCheckbox ) {
						icfg.setSavingBuildLog(!icfg.isSavingBuildLog());
					}
				}});
		}
	}

	private void setManagedBuild(boolean enable) {
		setManagedBuildOn(enable);
		page.informPages(MANAGEDBUILDSTATE, null);
		updateButtons();
	}
	
	/**
	 * sets widgets states
	 */
	@Override
	protected void updateButtons() {
		bldr = icfg.getEditableBuilder();
		
		canModify = false; // avoid extra update from modifyListeners
		int[] extStates = BuildBehaviourTab.calc3states(page, icfg, 0);

		b_genMakefileAuto.setEnabled(icfg.supportsBuild(true));
		if (extStates == null) { // no extended states available
			BuildBehaviourTab.setTriSelection(b_genMakefileAuto, 
				bldr.isManagedBuildOn());
			BuildBehaviourTab.setTriSelection(b_useDefault, 
				bldr.isDefaultBuildCmd());
			// b_expandVars.setGrayed(false);
			if(!bldr.canKeepEnvironmentVariablesInBuildfile())
				b_expandVars.setEnabled(false);
			else {
				b_expandVars.setEnabled(true);
				BuildBehaviourTab.setTriSelection(b_expandVars, 
					!bldr.keepEnvironmentVariablesInBuildfile());
			}
		} else {
			BuildBehaviourTab.setTriSelection(b_genMakefileAuto, extStates[0]);
			BuildBehaviourTab.setTriSelection(b_useDefault, extStates[1]);
			if(extStates[2] != BuildBehaviourTab.TRI_YES)
				b_expandVars.setEnabled(false);
			else {
				b_expandVars.setEnabled(true);
				BuildBehaviourTab.setTriSelection(b_expandVars, extStates[3]);
			}
		}
		c_builderType.select(isInternalBuilderEnabled() ? 1 : 0);
		c_builderType.setEnabled(
				canEnableInternalBuilder(true) &&
				canEnableInternalBuilder(false));
		
		t_buildCmd.setText(getMC());
		
		if (page.isMultiCfg()) {
			group_dir.setVisible(false);
		} else {
			group_dir.setVisible(true);
			t_dir.setText(bldr.getBuildPath());
			boolean mbOn = bldr.isManagedBuildOn();
			t_dir.setEnabled(!mbOn);
			b_dirVars.setEnabled(!mbOn);
			b_dirWsp.setEnabled(!mbOn);
			b_dirFile.setEnabled(!mbOn);
		}		
		boolean external = (c_builderType.getSelectionIndex() == 0);
		
		b_useDefault.setEnabled(external);
		t_buildCmd.setEnabled(external);
		((Control)t_buildCmd.getData()).setEnabled(external & ! b_useDefault.getSelection());
		
		b_genMakefileAuto.setEnabled(external && icfg.supportsBuild(true));
		if (b_expandVars.getEnabled())
			b_expandVars.setEnabled(external && b_genMakefileAuto.getSelection());
		
		if (external) { // just set relatet text widget state,
			checkPressed(b_useDefault, false); // do not update 
		}
		
		if ( page.isForProject() ) {
			if ( page.isMultiCfg() ) {
				buildOutputGroup.setVisible(false);
			} else {
				boolean b = icfg.isSavingBuildLog();
				buildOutputGroup.setVisible(true);
				String s = icfg.getBuildLogFilename();
				if ( s != null ) { 
					saveBuildFilename.setText(s);                                   
				} else {
					saveBuildFilename.setText(""); //$NON-NLS-1$
				}
				
				if ( s != null && BuildOutputLogger.canWriteToFile(s) ) {
					saveBuildCheckbox.setSelection(b);
					icfg.setSavingBuildLog(b);
				} else {
					saveBuildCheckbox.setEnabled(false);
					saveBuildCheckbox.setSelection(false);
					icfg.setSavingBuildLog(false);
				}
			}
		}		
		canModify = true;
	}
	
	private Button setupBottomButton(Composite c, String name) {
		Button b = new Button(c, SWT.PUSH);
		b.setText(name);
		GridData fd = new GridData(GridData.CENTER);
		fd.minimumWidth = BUTTON_WIDTH; 
		b.setLayoutData(fd);
		b.setData(t_dir);
		b.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buttonVarPressed(event);
			}});
		return b;
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
			String x = null;
			if (b.equals(b_dirWsp)) {
				x = getWorkspaceDirDialog(usercomp.getShell(), EMPTY_STR);
				if (x != null) ((Text)b.getData()).setText(x);
			} else if (b.equals(b_dirFile)) {
				x = getFileSystemDirDialog(usercomp.getShell(), EMPTY_STR);
				if (x != null) ((Text)b.getData()).setText(x);
			} else if (b.equals(saveBuildFileButton)) {
				FileDialog dialog = new FileDialog(usercomp.getShell(), SWT.SAVE);
				dialog.setText(FILESYSTEM_FILE_DIALOG_TITLE);
				x = dialog.open();
				if (x != null) ((Text)b.getData()).setText(x);
			} else { 
				x = AbstractCPropertyTab.getVariableDialog(usercomp.getShell(), getResDesc().getConfiguration());
				if (x != null) ((Text)b.getData()).insert(x);
			}
		}
	}
	
	@Override
	public void checkPressed(SelectionEvent e) {
		checkPressed((Control)e.widget, true);
		updateButtons();
	}
	
	private void checkPressed(Control b, boolean needUpdate) {	
		if (b == null) return;
		
		boolean val = false;
		if (b instanceof Button) val = ((Button)b).getSelection();
		
		if (b.getData() instanceof Text) {
			Text t = (Text)b.getData();
			if (b == b_useDefault) { val = !val; }
			t.setEnabled(val);
			if (t.getData() != null && t.getData() instanceof Control) {
				Control c = (Control)t.getData();
				c.setEnabled(val);
			}
		}
		// call may be used just to set text state above
		// in this case, settings update is not required
		if (! needUpdate)	
			return;
		
		if (b == b_useDefault) {
			setUseDefaultBuildCmd(!val);
		} else if (b == b_genMakefileAuto) {
			setManagedBuild(val);
		} else if (b == b_expandVars) {
			if(bldr.canKeepEnvironmentVariablesInBuildfile()) 
				setKeepEnvironmentVariablesInBuildfile(!val);
		}
	}

	/**
	 * get make command
	 * @return
	 */
	private String getMC() {
		String makeCommand = bldr.getCommand();
		String makeArgs = bldr.getArguments();
		if (makeArgs != null) {	makeCommand += " " + makeArgs; } //$NON-NLS-1$
		return makeCommand;
	}
	/**
	 * Performs common settings for all controls
	 * (Copy from config to widgets)
	 * @param cfgd - 
	 */
	@Override	
	public void updateData(ICResourceDescription cfgd) {
		if (cfgd == null) return;
		icfg = getCfg(cfgd.getConfiguration());
		if (icfg.getBuildLogFilename() == null) {
			// In this case this tab is loaded for the first time, 
			// and we need to populate configuration with values stored 
			// in Preferences. This is horrible workaround because we need 
			// to use this settings in build console which is in cdt ui,
			// hence IConfiguration is not accessible there and this values 
			// have be stored separately.
			// That is why we have convention that IConfiguration have 
			// buildLogFilename set to null this means that it is not loaded.
			// Otherwise it is loaded and doesn't require reloading.
			// performOk, performApply and performDefaults shouldn't never write
			// null values to Preferences to distinct this case from overs.
			//
			// To properly fix this problem without this workaround we need  
			// to make IConfiguration accessible for buildconsole package
			loadBuildLogSettings(page.getProject(), icfg);
		}
		updateButtons();
	}

	private static void loadBuildLogSettings(IProject project, IConfiguration icfg) {
		BuildOutputLogger.SaveBuildOutputPreferences bp = BuildOutputLogger.readSaveBuildOutputPreferences(project, icfg.getName());
		icfg.setBuildLogFilename(bp.fileName);                                               		                
		icfg.setSavingBuildLog(bp.isSaving);
	}

	@Override	
	public void performApply(ICResourceDescription src, ICResourceDescription dst) {
		BuildBehaviourTab.apply(src, dst, page.isMultiCfg());
		BuildOutputLogger.SaveBuildOutputPreferences bp = new BuildOutputLogger.SaveBuildOutputPreferences();
		bp.fileName = icfg.getBuildLogFilename();
		bp.isSaving = icfg.isSavingBuildLog();
		BuildOutputLogger.writeSaveBuildOutputPreferences(page.getProject(), icfg.getName(), bp);
	}
	
	@Override
	public void performOK() {
		if ( page.isForProject() ) {
			// Saving for all configurations
			ICProjectDescription pd = CDTPropertyManager.getProjectDescription(page.getProject());
			if ( pd != null ) {
				ICConfigurationDescription cfgs[] = pd.getConfigurations();
				if ( cfgs != null ) {
					for (ICConfigurationDescription cd : cfgs) {
						IConfiguration c = ManagedBuildManager.getConfigurationForDescription(cd);
						BuildOutputLogger.SaveBuildOutputPreferences bp = new BuildOutputLogger.SaveBuildOutputPreferences(); 
						bp.fileName = c.getBuildLogFilename();
						bp.isSaving = c.isSavingBuildLog(); 
						BuildOutputLogger.writeSaveBuildOutputPreferences(page.getProject(), c.getName(), bp);
						c.setBuildLogFilename(null);
						c.setSavingBuildLog(false);
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * 
	 * @param string
	 * @return
	 */
	private String parseMakeCommand(String rawCommand) {
		String[] result = rawCommand.split("\\s"); //$NON-NLS-1$
		if (result != null && result.length > 0)
			return result[0];
		else
			return rawCommand;
		
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
		icfg.setBuildLogFilename(""); //$NON-NLS-1$
		icfg.setSavingBuildLog(false);
		if (icfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[])((IMultiConfiguration)icfg).getItems();
			for (int i=0; i<cfs.length; i++) {
				IBuilder b = cfs[i].getEditableBuilder();
				BuildBehaviourTab.copyBuilders(b.getSuperClass(), b);
			}
		} else {
			BuildBehaviourTab.copyBuilders(bldr.getSuperClass(), bldr);
		}
		updateData(getResDesc());
	}
	
	private boolean canEnableInternalBuilder(boolean v) {
		if (icfg instanceof Configuration) 
			return ((Configuration)icfg).canEnableInternalBuilder(v);
		if (icfg instanceof IMultiConfiguration)
			return ((IMultiConfiguration)icfg).canEnableInternalBuilder(v);
		return false;
	}
	
	private void enableInternalBuilder(boolean v) {
		if (icfg instanceof Configuration) 
			((Configuration)icfg).enableInternalBuilder(v);
		if (icfg instanceof IMultiConfiguration)
			((IMultiConfiguration)icfg).enableInternalBuilder(v);
	}
	
	private boolean isInternalBuilderEnabled() {
		if (icfg instanceof Configuration) 
			return ((Configuration)icfg).isInternalBuilderEnabled();
		if (icfg instanceof IMultiConfiguration)
			return ((MultiConfiguration)icfg).isInternalBuilderEnabled();
		return false;
	}
	
	private void setUseDefaultBuildCmd(boolean val) {
		try {
			if (icfg instanceof IMultiConfiguration) {
				IConfiguration[] cfs = (IConfiguration[])((IMultiConfiguration)icfg).getItems();
				for (int i=0; i<cfs.length; i++) {
					IBuilder b = cfs[i].getEditableBuilder();
					if (b != null)
						b.setUseDefaultBuildCmd(val);
				}
			} else {
				icfg.getEditableBuilder().setUseDefaultBuildCmd(val);
			}
		} catch (CoreException e) {
			ManagedBuilderUIPlugin.log(e);
		}
	}

	private void setKeepEnvironmentVariablesInBuildfile(boolean val) {
		if (icfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[])((IMultiConfiguration)icfg).getItems();
			for (int i=0; i<cfs.length; i++) {
				IBuilder b = cfs[i].getEditableBuilder();
				if (b != null)
					b.setKeepEnvironmentVariablesInBuildfile(val);
			}
		} else {
			icfg.getEditableBuilder().setKeepEnvironmentVariablesInBuildfile(val);
		}
	}

	private void setCommand(String buildCommand) {
		if (icfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[])((IMultiConfiguration)icfg).getItems();
			for (int i=0; i<cfs.length; i++) {
				IBuilder b = cfs[i].getEditableBuilder();
				b.setCommand(buildCommand);
			}
		} else {
			icfg.getEditableBuilder().setCommand(buildCommand);
		}
	}
	
	private void setArguments(String makeArgs) {
		if (icfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[])((IMultiConfiguration)icfg).getItems();
			for (int i=0; i<cfs.length; i++) {
				IBuilder b = cfs[i].getEditableBuilder();
				b.setArguments(makeArgs);
			}
		} else {
			icfg.getEditableBuilder().setArguments(makeArgs);
		}
	}

	private void setBuildPath(String path) {
		if (icfg instanceof IMultiConfiguration) {
			IConfiguration[] cfs = (IConfiguration[])((IMultiConfiguration)icfg).getItems();
			for (int i=0; i<cfs.length; i++) {
				IBuilder b = cfs[i].getEditableBuilder();
				b.setBuildPath(path);
			}
		} else {
			icfg.getEditableBuilder().setBuildPath(path);
		}
	}
	
	private void setManagedBuildOn(boolean on) {
		try {
			if (icfg instanceof IMultiConfiguration) {
				IConfiguration[] cfs = (IConfiguration[])((IMultiConfiguration)icfg).getItems();
				for (int i=0; i<cfs.length; i++) {
					IBuilder b = cfs[i].getEditableBuilder();
					b.setManagedBuildOn(on);
				}
			} else {
				icfg.getEditableBuilder().setManagedBuildOn(on);
			}
		} catch (CoreException e) {
			ManagedBuilderUIPlugin.log(e);
		}
	}
}
