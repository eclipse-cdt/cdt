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
package org.eclipse.cdt.managedbuilder.ui.newui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.BuildProcessManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.newmake.core.IMakeBuilderInfo;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;


public class BuilderSettingsTab extends AbstractCBuildPropertyTab {
	// Widgets
	//1
	Button b_useDefault;
	Combo  c_builderType;
	Text   t_buildCmd; 
	//2
	Button b_genMakefileAuto;
	Button b_expandVars;
	//3
	Button b_stopOnError;
	Button b_parallel;
	Button b_parallelOpt;
	Button b_parallelNum;
	Spinner parallelProcesses;
	//4 
	Label  title2;
	Button b_autoBuild;
	Text   t_autoBuild;
	Button b_cmdBuild;
	Text   t_cmdBuild;
	Button b_cmdClean;
	Text   t_cmdClean;	
	//5
	Text   t_dir;
	Button b_dirWsp;
	Button b_dirFile;
	Button b_dirVars;

	protected final int cpuNumber = BuildProcessManager.checkCPUNumber(); 
//	Configuration cfg = null;
	IBuilder bld;
	Configuration cfg;
//	BuildMacroProvider bmp = (BuildMacroProvider)ManagedBuildManager.getBuildMacroProvider();
	
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(1, false));
		
		// Builder group
		Group g1 = setupGroup(usercomp, Messages.getString("BuilderSettingsTab.0"), 3, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
		setupLabel(g1, Messages.getString("BuilderSettingsTab.1"), 1, GridData.BEGINNING); //$NON-NLS-1$
		c_builderType = new Combo(g1, SWT.DROP_DOWN | SWT.BORDER);
		setupControl(c_builderType, 2, GridData.FILL_HORIZONTAL);
		c_builderType.add(Messages.getString("BuilderSettingsTab.2")); //$NON-NLS-1$
		c_builderType.add(Messages.getString("BuilderSettingsTab.3")); //$NON-NLS-1$
		c_builderType.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(SelectionEvent event) {
				cfg.enableInternalBuilder(c_builderType.getSelectionIndex() == 1);
		    	setState();
		 }});
		
		b_useDefault = setupCheck(g1, Messages.getString("BuilderSettingsTab.4"), 3, GridData.BEGINNING); //$NON-NLS-1$
		
		setupLabel(g1, Messages.getString("BuilderSettingsTab.5"), 1, GridData.BEGINNING); //$NON-NLS-1$
		t_buildCmd = setupBlock(g1, b_useDefault);
		t_buildCmd.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
	        	String fullCommand = t_buildCmd.getText().trim();
	        	String buildCommand = parseMakeCommand(fullCommand);
	        	String buildArgs = parseMakeArgs(fullCommand);
	        	if(!buildCommand.equals(bld.getCommand()) 
	        			|| !buildArgs.equals(bld.getArguments())){
		        	bld.setCommand(buildCommand);
		        	bld.setArguments(buildArgs);
		        }
			}});
				
		Group g2 = setupGroup(usercomp, Messages.getString("BuilderSettingsTab.6"), 2, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
		((GridLayout)(g2.getLayout())).makeColumnsEqualWidth = true;
		
		b_genMakefileAuto = setupCheck(g2, Messages.getString("BuilderSettingsTab.7"), 1, GridData.BEGINNING); //$NON-NLS-1$
		b_expandVars  = setupCheck(g2, Messages.getString("BuilderSettingsTab.8"), 1, GridData.BEGINNING); //$NON-NLS-1$

		// Build setting group
		Group g3 = setupGroup(usercomp, Messages.getString("BuilderSettingsTab.9"), 2, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
		((GridLayout)(g3.getLayout())).makeColumnsEqualWidth = true;
		
		Composite c1 = new Composite(g3, SWT.NONE);
		setupControl(c1, 1, GridData.FILL_BOTH);
		c1.setLayout(new GridLayout(1, false));
		
		b_stopOnError = setupCheck(c1, Messages.getString("BuilderSettingsTab.10"), 1, GridData.BEGINNING); //$NON-NLS-1$
		Composite c2 = new Composite(g3, SWT.NONE);
		setupControl(c2, 1, GridData.FILL_BOTH);
		c2.setLayout(new GridLayout(2, false));
		
		b_parallel = setupCheck(c2, Messages.getString("BuilderSettingsTab.11"), 2, GridData.BEGINNING); //$NON-NLS-1$
		
		b_parallelOpt= new Button(c2, SWT.RADIO);
		b_parallelOpt.setText(Messages.getString("BuilderSettingsTab.12")); //$NON-NLS-1$
		setupControl(b_parallelOpt, 2, GridData.BEGINNING);
		((GridData)(b_parallelOpt.getLayoutData())).horizontalIndent = 15;
		b_parallelOpt.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(SelectionEvent event) {
				cfg.setParallelDef(b_parallelOpt.getSelection());
				setState();
		 }});
		
		b_parallelNum= new Button(c2, SWT.RADIO);
		b_parallelNum.setText(Messages.getString("BuilderSettingsTab.13")); //$NON-NLS-1$
		setupControl(b_parallelNum, 1, GridData.BEGINNING);
		((GridData)(b_parallelNum.getLayoutData())).horizontalIndent = 15;
		b_parallelNum.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(SelectionEvent event) {
				cfg.setParallelDef(!b_parallelNum.getSelection());
				setState();
		 }});

		parallelProcesses = new Spinner(c2, SWT.BORDER);
		setupControl(parallelProcesses, 1, GridData.BEGINNING);
		parallelProcesses.setValues(cpuNumber, 1, 10000, 0, 1, 10);
		parallelProcesses.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				cfg.setParallelNumber(parallelProcesses.getSelection());
				setState();
			}
		});

		// Workbench behaviour group
		Group g4 = setupGroup(usercomp, Messages.getString("BuilderSettingsTab.14"), 3, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
		setupLabel(g4, Messages.getString("BuilderSettingsTab.15"), 1, GridData.BEGINNING); //$NON-NLS-1$
		title2 = setupLabel(g4, Messages.getString("BuilderSettingsTab.16"), 2, GridData.BEGINNING); //$NON-NLS-1$
		b_autoBuild = setupCheck(g4, Messages.getString("BuilderSettingsTab.17"), 1, GridData.BEGINNING); //$NON-NLS-1$
		t_autoBuild = setupBlock(g4, b_autoBuild);
		t_autoBuild.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try {
					bld.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_AUTO, t_autoBuild.getText());
				} catch (CoreException ex) {}
			}} );
		setupLabel(g4, Messages.getString("BuilderSettingsTab.18"), 3, GridData.BEGINNING); //$NON-NLS-1$
		b_cmdBuild = setupCheck(g4, Messages.getString("BuilderSettingsTab.19"), 1, GridData.BEGINNING); //$NON-NLS-1$
		t_cmdBuild = setupBlock(g4, b_cmdBuild);
		t_cmdBuild.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try { 
					bld.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_INCREMENTAL, t_cmdBuild.getText());
				} catch (CoreException ex) {}
			}} );
		b_cmdClean = setupCheck(g4, Messages.getString("BuilderSettingsTab.20"), 1, GridData.BEGINNING); //$NON-NLS-1$
		t_cmdClean = setupBlock(g4, b_cmdClean);
		t_cmdClean.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				try { 
					bld.setBuildAttribute(IMakeBuilderInfo.BUILD_TARGET_CLEAN, t_cmdClean.getText());
				} catch (CoreException ex) {}
			}} );

		// Build location group
		Group g5 = setupGroup(usercomp, Messages.getString("BuilderSettingsTab.21"), 2, GridData.FILL_HORIZONTAL); //$NON-NLS-1$
		setupLabel(g5, Messages.getString("BuilderSettingsTab.22"), 1, GridData.BEGINNING); //$NON-NLS-1$
		t_dir = setupText(g5, 1, GridData.FILL_HORIZONTAL);
		t_dir.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				bld.setBuildPath(t_dir.getText());
			}} );
		Composite c = new Composite(g5, SWT.NONE);
		setupControl(c, 2, GridData.FILL_HORIZONTAL);
		FormLayout f = new FormLayout();
		c.setLayout(f);
		b_dirVars = setupBottomButton(c, VARIABLESBUTTON_NAME, null);
		b_dirFile = setupBottomButton(c, FILESYSTEMBUTTON_NAME, b_dirVars);
		b_dirWsp = setupBottomButton(c, WORKSPACEBUTTON_NAME, b_dirFile);
	}

	void setManagedBuild(boolean enable) {
		try {
			bld.setManagedBuildOn(enable);
			page.informPages(MANAGEDBUILDSTATE, null);
			setState();
		} catch (CoreException ex) {}
	}
	
	/**
	 * sets widgets states
	 */
	void setState() {
		bld = cfg.getEditableBuilder();
		
		b_genMakefileAuto.setEnabled(cfg.supportsBuild(true));
		b_genMakefileAuto.setSelection(bld.isManagedBuildOn());
		b_useDefault.setSelection(bld.isDefaultBuildCmd());

		c_builderType.select(cfg.isInternalBuilderEnabled() ? 1 : 0);
		c_builderType.setEnabled(
				cfg.canEnableInternalBuilder(true) &&
				cfg.canEnableInternalBuilder(false));
		
		t_buildCmd.setText(getMC());
		
		b_stopOnError.setSelection(bld.isStopOnError());
		b_stopOnError.setEnabled(
				bld.supportsStopOnError(true) &&
				bld.supportsStopOnError(false));
		// parallel
		b_parallel.setSelection(cfg.getInternalBuilderParallel());
		b_parallelOpt.setSelection(cfg.getParallelDef());
		b_parallelNum.setSelection(!cfg.getParallelDef());
		int n = cfg.getParallelNumber();
		if (n < 0) n = -n;
		parallelProcesses.setSelection(n);

		b_parallel.setVisible(bld.supportsParallelBuild());
		b_parallelOpt.setVisible(bld.supportsParallelBuild());
		b_parallelNum.setVisible(bld.supportsParallelBuild());
		parallelProcesses.setVisible(bld.supportsParallelBuild());
		
		if(!bld.canKeepEnvironmentVariablesInBuildfile())
			b_expandVars.setEnabled(false);
		else {
			b_expandVars.setEnabled(true);
			b_expandVars.setSelection(!bld.keepEnvironmentVariablesInBuildfile());
		}
		t_dir.setText(bld.getBuildPath());
		//	cfg.getBuildData().getBuilderCWD().toOSString());
		
		boolean mbOn = bld.isManagedBuildOn();
		t_dir.setEnabled(!mbOn);
		b_dirVars.setEnabled(!mbOn);
		b_dirWsp.setEnabled(!mbOn);
		b_dirFile.setEnabled(!mbOn);
		
		b_autoBuild.setSelection(bld.isAutoBuildEnable());
		t_autoBuild.setText(bld.getAutoBuildTarget());
		b_cmdBuild.setSelection(bld.isIncrementalBuildEnabled());
		t_cmdBuild.setText(bld.getIncrementalBuildTarget());
		b_cmdClean.setSelection(bld.isCleanBuildEnabled());
		t_cmdClean.setText(bld.getCleanBuildTarget());
		
		boolean external = (c_builderType.getSelectionIndex() == 0);
		boolean parallel = b_parallel.getSelection();
		
		b_useDefault.setEnabled(external);
		t_buildCmd.setEnabled(external);
		((Control)t_buildCmd.getData()).setEnabled(external & ! b_useDefault.getSelection());
		
		b_genMakefileAuto.setEnabled(external && cfg.supportsBuild(true));
		b_expandVars.setEnabled(external && b_genMakefileAuto.getSelection());
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
			checkPressed(b_useDefault);
			checkPressed(b_autoBuild);
			checkPressed(b_cmdBuild);
			checkPressed(b_cmdClean);
		}
	}
	
	Button setupBottomButton(Composite c, String name, Control x) {
		Button b = new Button(c, SWT.PUSH);
		b.setText(name); 
		FormData fd = new FormData();
		fd.width = BUTTON_WIDTH;
		fd.top = new FormAttachment(0, 2);
		if (x != null) 
			fd.right = new FormAttachment(x, -5);
		else
			fd.right = new FormAttachment(100, 0);
		b.setLayoutData(fd);
		b.setData(t_dir);
		b.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent event) {
	        	buttonVarPressed(event);
	        }});
		return b;
	}
	
	/**
	 * Sets up text + corresponding button
	 */
	Text setupBlock(Composite c, Button check) {
		Text t = setupText(c, 1, GridData.FILL_HORIZONTAL);
		Button b = setupButton(c, VARIABLESBUTTON_NAME, 1, GridData.END);
		b.setData(t); // to get know which text is affected
		t.setData(b); // to get know which button to enable/disable
		b.addSelectionListener(new SelectionAdapter() {
	        public void widgetSelected(SelectionEvent event) {
	        	buttonVarPressed(event);
	        }});
		if (check != null) check.setData(t);
		return t;
	}
	
	/*
	 * Unified handler for "Variables" buttons
	 */
	void buttonVarPressed(SelectionEvent e) {
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
			} else { 
				x = AbstractCPropertyTab.getVariableDialog(usercomp.getShell(), getResDesc().getConfiguration());
				if (x != null) ((Text)b.getData()).insert(x);
			}
		}
	}
	
    public void checkPressed(SelectionEvent e) {
    	checkPressed((Button)e.widget);
    	setState();
    }
	
	void checkPressed(Button b) {	
		if (b == null) return;
		
		boolean val = b.getSelection();
		if (b.getData() instanceof Text) {
			Text t = (Text)b.getData();
			if (b == b_useDefault) { val = !val; }
			t.setEnabled(val);
			if (t.getData() != null && t.getData() instanceof Control) {
				Control c = (Control)t.getData();
				c.setEnabled(val);
			}
		}
		try {
			if (b == b_autoBuild) {
				bld.setAutoBuildEnable(val);				
			} else if (b == b_cmdBuild) {
				bld.setIncrementalBuildEnable(val);				
			} else if (b == b_cmdClean) {
				bld.setCleanBuildEnable(val);
			} else if (b == b_useDefault) {
				bld.setUseDefaultBuildCmd(!val);
			} else if (b == b_genMakefileAuto) {
				setManagedBuild(val);
			} else if (b == b_expandVars) {
				if(bld.canKeepEnvironmentVariablesInBuildfile()) 
					bld.setKeepEnvironmentVariablesInBuildfile(!val);
			} else if (b == b_stopOnError) {
				bld.setStopOnError(val);
			} else if (b == b_parallel) {
				bld.setParallelBuildOn(val);
			}
		} catch (CoreException e) {}
	}

	/**
	 * get make command
	 * @return
	 */
	private String getMC() {
		String makeCommand = bld.getBuildCommand().toOSString();
		String makeArgs = bld.getBuildArguments();
		if (makeArgs != null) {	makeCommand += " " + makeArgs; } //$NON-NLS-1$
		return makeCommand;
	}
	/**
	 * Performs common settings for all controls
	 * (Copy from config to widgets)
	 * @param cfgd - 
	 */
	
	public void updateData(ICResourceDescription cfgd) {
		if (cfgd == null) return;
		IConfiguration icfg = getCfg(cfgd.getConfiguration());
		if (!(icfg instanceof Configuration)) return;
		cfg = (Configuration)icfg;
		setState();
	}

	public void performApply(ICResourceDescription src, ICResourceDescription dst) {
		Configuration cfg01 = (Configuration)getCfg(src.getConfiguration());
		Configuration cfg02 = (Configuration)getCfg(dst.getConfiguration());
		cfg02.enableInternalBuilder(cfg01.isInternalBuilderEnabled());
		copyBuilders(cfg01.getBuilder(), cfg02.getEditableBuilder());
	}
	
	private void copyBuilders(IBuilder b1, IBuilder b2) {  	
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
			b2.setBuildPath(null);
		
			b2.setAutoBuildEnable((b1.isAutoBuildEnable()));
			b2.setBuildAttribute(IBuilder.BUILD_TARGET_AUTO, (b1.getBuildAttribute(IBuilder.BUILD_TARGET_AUTO, EMPTY_STR)));
			b2.setCleanBuildEnable(b1.isCleanBuildEnabled());
			b2.setBuildAttribute(IBuilder.BUILD_TARGET_CLEAN, (b1.getBuildAttribute(IBuilder.BUILD_TARGET_CLEAN, EMPTY_STR)));
			b2.setIncrementalBuildEnable(b1.isIncrementalBuildEnabled());
			b2.setBuildAttribute(IBuilder.BUILD_TARGET_INCREMENTAL, (b1.getBuildAttribute(IBuilder.BUILD_TARGET_INCREMENTAL, EMPTY_STR)));
		
			b2.setManagedBuildOn(b1.isManagedBuildOn());
		} catch (CoreException ex) {
			//TODO: log
		}
	}

	/* (non-Javadoc)
	 * @param rawCommand
	 * @return
	 */
	private String parseMakeArgs(String rawCommand) {
		StringBuffer result = new StringBuffer();		
		
		// Parse out the command
		String actualCommand = parseMakeCommand(rawCommand);
		
		// The flags and targets are anything not in the command
		String arguments = rawCommand.substring(actualCommand.length());

		// If there aren't any, we can stop
		if (arguments.length() == 0) {
			return result.toString().trim();
		}

		String[] tokens = arguments.trim().split("\\s"); //$NON-NLS-1$
		/*
		 * Cases to consider
		 * --<flag>					Sensible, modern single flag. Add to result and continue.
		 * -<flags>					Flags in single token, add to result and stop
		 * -<flag_with_arg> ARG		Flag with argument. Add next token if valid arg.
		 * -<mixed_flags> ARG		Mix of flags, one takes arg. Add next token if valid arg.
		 * -<flag_with_arg>ARG		Corrupt case where next token should be arg but isn't
		 * -<flags> [target]..		Flags with no args, another token, add flags and stop.
		 */
		Pattern flagPattern = Pattern.compile("C|f|I|j|l|O|W"); //$NON-NLS-1$
		// Look for a '-' followed by 1 or more flags with no args and exactly 1 that expects args
		Pattern mixedFlagWithArg = Pattern.compile("-[^CfIjloW]*[CfIjloW]{1}.+"); //$NON-NLS-1$
		for (int i = 0; i < tokens.length; ++i) {
			String currentToken = tokens[i];
			if (currentToken.startsWith("--")) { //$NON-NLS-1$
				result.append(currentToken);
				result.append(" "); //$NON-NLS-1$
			} else if (currentToken.startsWith("-")) { //$NON-NLS-1$
				// Is there another token
				if (i + 1 >= tokens.length) {
					//We are done
					result.append(currentToken);
				} else {
					String nextToken = tokens[i + 1];
					// Are we expecting arguments
					Matcher flagMatcher = flagPattern.matcher(currentToken);
					if (!flagMatcher.find()) {
						// Evalutate whether the next token should be added normally
						result.append(currentToken);
						result.append(" "); //$NON-NLS-1$
					} else {
						// Look for the case where there is no space between flag and arg
						if (mixedFlagWithArg.matcher(currentToken).matches()) {
							// Add this single token and keep going
							result.append(currentToken);
							result.append(" ");							 //$NON-NLS-1$
						} else {
							// Add this token and the next one right now
							result.append(currentToken);
							result.append(" "); //$NON-NLS-1$
							result.append(nextToken);
							result.append(" "); //$NON-NLS-1$
							// Skip the next token the next time through, though
							++i;
						}
					}
				}
			}
		}
		
		return result.toString().trim();
	}

	/* (non-Javadoc)
	 * 
	 * @param string
	 * @return
	 */
	private String parseMakeCommand(String rawCommand) {
		StringBuffer command = new StringBuffer();
		
		// Try to separate out the command from the arguments 
		String[] result = rawCommand.split("\\s"); //$NON-NLS-1$
		/*
		 * Here are the cases to consider:
		 * 	cmd								First segment is last segment, assume is command
		 * 	cmd [flags]						First segment is the command
		 * 	path/cmd [flags]				Same as above
		 * 	path with space/make [flags]	Must append each segment up-to flags as command
		 */
		for (int i = 0; i < result.length; ++i) {
			// Get the segment
			String cmdSegment = result[i];
			// If there is not another segment, we found the end
			if (i + 1 >= result.length) {
				command.append(cmdSegment);
			} else {
				// See if the next segment is the start of the flags
				String nextSegment = result[i + 1];
				if (nextSegment.startsWith("-")) { //$NON-NLS-1$
					// we have found the end of the command
					command.append(cmdSegment);
					break;
				} else {
					command.append(cmdSegment);
					// Add the whitespace back
					command.append(" "); //$NON-NLS-1$
				}
			}
		}
		return command.toString().trim();
	}
	// This page can be displayed for project only
	public boolean canBeVisible() {
		return page.isForProject() || page.isForPrefs();
	}
	
	public void setVisible (boolean b) {
		super.setVisible(b);
	}

	protected void performDefaults() {
		copyBuilders(bld.getSuperClass(), bld);
		updateData(getResDesc());
	}
}
