/**********************************************************************
 * Copyright (c) 2002,2005 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * QNX Software Systems - Move to Make plugin
 * Intel Corp - Use in Managed Make system
***********************************************************************/
package org.eclipse.cdt.managedbuilder.internal.ui;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.macros.BuildMacroProvider;
import org.eclipse.cdt.managedbuilder.ui.properties.BuildPropertyPage;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class BuildSettingsBlock extends AbstractCOptionPage {

	/*
	 * String constants
	 */
	private static final String PREFIX = "BuildSettingsBlock";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String SETTINGS_LABEL = LABEL + ".Settings";	//$NON-NLS-1$
	private static final String GROUP = LABEL + ".makecmdgroup";	//$NON-NLS-1$
	private static final String DEF_BTN = LABEL + ".makecmddef";	//$NON-NLS-1$
	private static final String OUTPUT_GROUP = LABEL + ".output.group";	//$NON-NLS-1$
	private static final String OUTPUT_EXT = LABEL + ".output.extension";	//$NON-NLS-1$
	private static final String OUTPUT_NAME = LABEL + ".output.name";	//$NON-NLS-1$
	private static final String MACROS_GROUP = LABEL + ".macros.group";	//$NON-NLS-1$
	private static final String PACROS_EXPAND_BTN = LABEL + ".macros.expand";	//$NON-NLS-1$

	private static final String EMPTY_STRING = new String();
	
	/*
	 * Dialog widgets
	 */
	protected Text buildArtifactExt;
	protected Text buildArtifactName;
	protected Button makeCommandDefault;
	protected Text makeCommandEntry;
	protected Button buildMacrosExpand;
	protected Group buildMacrosExpandGroup;

	/*
	 * Bookeeping variables
	 */
	private BuildPropertyPage parent;
	// The name of the build artifact
	private String artifactExt;
	private String artifactName;
	// The make command associated with the target
	private String makeCommand;
	// State of the check box on exit
	private boolean useDefaultMake;
	// Has the page been changed?
	private boolean dirty = false;

	private ModifyListener widgetModified = new ModifyListener() {
	    public void modifyText(ModifyEvent e) {
	        setDirty(true);
	    }
	};
	
	
	/*
	 *  Constructor
	 */
	public BuildSettingsBlock(BuildPropertyPage parent)
	{
		super(ManagedBuilderUIMessages.getResourceString(SETTINGS_LABEL));
		super.setContainer(parent);
		this.parent = parent;
	}

	public void createControl(Composite parent)  {
		Composite comp = new Composite(parent, SWT.NULL);
		comp.setFont(parent.getFont());
		comp.setLayout(new GridLayout(1, true));
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		setControl(comp);
		
		// Create a group for the build output
		createBuildArtifactGroup(comp);
	
		// Create the make command group area
		createMakeCommandGroup(comp);
		
		// Create the build macros usage configuration area
		createExpandMacrosGroup(comp);
	}

	/* (non-Javadoc)
	 * Creates the group that contains the build artifact name controls.
	 */
	private void createBuildArtifactGroup(Composite parent) {
		final Group outputGroup = new Group(parent, SWT.NONE);
		outputGroup.setFont(parent.getFont());
		outputGroup.setText(ManagedBuilderUIMessages.getResourceString(OUTPUT_GROUP));
		outputGroup.setLayout(new GridLayout(3, false));
		outputGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL)); 

		// Three labels
		final Label nameLabel = new Label(outputGroup, SWT.LEFT);
		nameLabel.setFont(outputGroup.getFont());
		nameLabel.setText(ManagedBuilderUIMessages.getResourceString(OUTPUT_NAME));
		nameLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final Label placeHolder = new Label(outputGroup, SWT.CENTER);
		placeHolder.setText(new String());
		placeHolder.setLayoutData(new GridData());
		
		final Label extLabel = new Label(outputGroup, SWT.LEFT);
		extLabel.setFont(outputGroup.getFont());
		extLabel.setText(ManagedBuilderUIMessages.getResourceString(OUTPUT_EXT));
		extLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Now we need two text widgets separated by a label
		buildArtifactName = new Text(outputGroup, SWT.SINGLE | SWT.BORDER);
		buildArtifactName.setFont(outputGroup.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
		buildArtifactName.setLayoutData(data);
		buildArtifactName.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				buildArtifactName = null;
			}
		});
		buildArtifactName.getAccessible().addAccessibleListener(new AccessibleAdapter(){
			public void getName(AccessibleEvent e) {
				e.result = ManagedBuilderUIMessages.getResourceString(OUTPUT_NAME);
			}
		});
		buildArtifactName.addModifyListener(widgetModified);
		
		final Label dotLabel = new Label(outputGroup, SWT.CENTER);
		dotLabel.setFont(outputGroup.getFont());
		dotLabel.setText(new String(".")); //$NON-NLS-1$
		dotLabel.setLayoutData(new GridData());

		buildArtifactExt = new Text(outputGroup, SWT.SINGLE | SWT.BORDER);
		buildArtifactExt.setFont(outputGroup.getFont());
		data = new GridData(GridData.FILL_HORIZONTAL);
		data.widthHint = (IDialogConstants.ENTRY_FIELD_WIDTH / 2);
		buildArtifactExt.setLayoutData(data);
		buildArtifactExt.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent e) {
				buildArtifactExt = null;
			}
		});
		buildArtifactExt.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			public void getName(AccessibleEvent e) {
				e.result = ManagedBuilderUIMessages.getResourceString(OUTPUT_EXT);
			}
		});
		buildArtifactExt.addModifyListener(widgetModified);
	}

	/* (non-Javadoc)
	 * Creates the group control for the make command
	 * @param parent
	 */
	private void createMakeCommandGroup(Composite parent) {
		final Group makeCommandGroup = new Group(parent, SWT.NONE);
		makeCommandGroup.setFont(parent.getFont());
		makeCommandGroup.setText(ManagedBuilderUIMessages.getResourceString(GROUP));
		makeCommandGroup.setLayout(new GridLayout(1, true));
		makeCommandGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		makeCommandDefault = new Button(makeCommandGroup, SWT.CHECK | SWT.LEFT);
		makeCommandDefault.setFont(makeCommandGroup.getFont());
		makeCommandDefault.setText(ManagedBuilderUIMessages.getResourceString(DEF_BTN));
		makeCommandDefault.setBackground(makeCommandGroup.getBackground());
		makeCommandDefault.setForeground(makeCommandGroup.getForeground());
		makeCommandDefault.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				handleUseDefaultPressed();
				setDirty(true);
			}
		});
		makeCommandDefault.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				makeCommandDefault = null;
			}
		});
		
		makeCommandEntry = new Text(makeCommandGroup, SWT.SINGLE | SWT.BORDER);
		makeCommandEntry.setFont(makeCommandGroup.getFont());
		makeCommandEntry.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		makeCommandEntry.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				makeCommandEntry = null;
			}
		});
		makeCommandEntry.addModifyListener(widgetModified);
	}

	/* (non-Javadoc)
	 * Creates the group containing the check-box that allow user to specify 
	 * whether the environment variable macros should be expanded or kept in the makefile
	 * @param parent
	 */
	private void createExpandMacrosGroup(Composite parent) {
		buildMacrosExpandGroup = new Group(parent, SWT.NONE);
		buildMacrosExpandGroup.setFont(parent.getFont());
		buildMacrosExpandGroup.setText(ManagedBuilderUIMessages.getResourceString(MACROS_GROUP));
		buildMacrosExpandGroup.setLayout(new GridLayout(1, true));
		buildMacrosExpandGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buildMacrosExpand = new Button(buildMacrosExpandGroup, SWT.CHECK | SWT.LEFT);
		buildMacrosExpand.setFont(buildMacrosExpandGroup.getFont());
		buildMacrosExpand.setText(ManagedBuilderUIMessages.getResourceString(PACROS_EXPAND_BTN));
		buildMacrosExpand.setBackground(buildMacrosExpandGroup.getBackground());
		buildMacrosExpand.setForeground(buildMacrosExpandGroup.getForeground());
		buildMacrosExpand.addSelectionListener(new SelectionAdapter () {
			public void widgetSelected(SelectionEvent e) {
				setDirty(true);
			}
		});
		buildMacrosExpand.addDisposeListener(new DisposeListener() {
			public void widgetDisposed(DisposeEvent event) {
				buildMacrosExpand = null;
			}
		});
	}
	
	protected void initializeValues() {
		setValues();
	}

	public void updateValues() {
		setValues();	
		useDefaultMake = !parent.getSelectedConfiguration().hasOverriddenBuildCommand();
		makeCommandDefault.setSelection(useDefaultMake);
		makeCommandEntry.setEditable(!makeCommandDefault.getSelection());
	}

	protected void setValues() {
		artifactName = parent.getSelectedConfiguration().getArtifactName();
		buildArtifactName.setText(artifactName);
		artifactExt = parent.getSelectedConfiguration().getArtifactExtension();
		buildArtifactExt.setText(artifactExt);
		makeCommand = parent.getSelectedConfiguration().getBuildCommand();
		String makeArgs = parent.getSelectedConfiguration().getBuildArguments();
		if (makeArgs != null) {
			makeCommand += " " + makeArgs; //$NON-NLS-1$
		}
		makeCommandEntry.setText(makeCommand);
		
		BuildMacroProvider provider = (BuildMacroProvider)ManagedBuildManager.getBuildMacroProvider();
		if(!provider.canKeepMacrosInBuildfile(this.parent.getSelectedConfiguration()))
			buildMacrosExpandGroup.setVisible(false);
		else {
			buildMacrosExpandGroup.setVisible(true);
			buildMacrosExpand.setSelection(provider.areMacrosExpandedInBuildfile(parent.getSelectedConfiguration()));
		}

		setDirty(false);
	}

	public void removeValues(String id) {
		// Nothing to do...
	}

	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
		
		// Display a "Confirm" dialog box, since:
		//   1.  The defaults are immediately applied
		//   2.  The action cannot be undone
		Shell shell = ManagedBuilderUIPlugin.getDefault().getShell();
		boolean shouldDefault = MessageDialog.openConfirm(shell,
					ManagedBuilderUIMessages.getResourceString("BuildSettingsBlock.defaults.title"), //$NON-NLS-1$
					ManagedBuilderUIMessages.getResourceString("BuildSettingsBlock.defaults.message")); //$NON-NLS-1$
		if (!shouldDefault) return;
		
		IConfiguration config = parent.getSelectedConfiguration();
		config.setArtifactName(config.getManagedProject().getDefaultArtifactName());
		config.setArtifactExtension(null);
		IBuilder builder = config.getToolChain().getBuilder();
		if (!builder.isExtensionElement()) {
			config.getToolChain().removeLocalBuilder();
		}
		
		// Save the information that was reset
		ManagedBuildManager.setDefaultConfiguration(parent.getProject(), parent.getSelectedConfiguration());
		ManagedBuildManager.saveBuildInfo(parent.getProject(), false);
		
		//set the expand macros state to false
		BuildMacroProvider provider = (BuildMacroProvider)ManagedBuildManager.getBuildMacroProvider();
		provider.expandMacrosInBuildfile(config,false);
		
		setValues();
		makeCommandDefault.setSelection(true);
		makeCommandEntry.setEditable(false);	
		
		setDirty(false);
	}
	
	/*
	 *  (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		useDefaultMake = makeCommandDefault.getSelection();
		makeCommand = makeCommandEntry.getText().trim();
		artifactName = buildArtifactName.getText().trim();
		artifactExt = buildArtifactExt.getText().trim();
		
		IConfiguration selectedConfiguration = parent.getSelectedConfiguration();
		IBuilder builder = selectedConfiguration.getToolChain().getBuilder();
		boolean setBuilderValues = false;
		
		// Set the build output name
		if (!selectedConfiguration.getArtifactName().equals(artifactName)) {
			setBuilderValues = true;
		}
		// Set the build output extension
		if (!selectedConfiguration.getArtifactExtension().equals(artifactExt)) {
			setBuilderValues = true;
		}
		// Set the new make command
		String makeCommandOnly = null;
		String makeArguments = null;
		if (useDefaultMake) {
			if (!builder.isExtensionElement()) {
				setBuilderValues = true;
			}
		} else {
			// Parse for command and arguments
			String rawCommand = makeCommand;
			makeCommandOnly = parseMakeCommand(rawCommand);
			if (!selectedConfiguration.getBuildCommand().equals(makeCommandOnly)) {
				setBuilderValues = true;
			}
			makeArguments = parseMakeArgs(rawCommand);
			if (!selectedConfiguration.getBuildArguments().equals(makeArguments)) {
				setBuilderValues = true;
			}
		}

		if (setBuilderValues) {
			//  If the configuration does not already have a "local" builder, we
			//  need to create it.
			if (builder.isExtensionElement()) {
				IToolChain tc = selectedConfiguration.getToolChain();
				int nnn = ManagedBuildManager.getRandomNumber();
				String subId = builder.getId() + "." + nnn;		//$NON-NLS-1$
				String name = builder.getName() + "." + selectedConfiguration.getName(); 	//$NON-NLS-1$
				tc.createBuilder(builder, subId, name, false);
			}
			
			//  Set the builder values
			selectedConfiguration.setArtifactName(artifactName);
			selectedConfiguration.setArtifactExtension(artifactExt);
			selectedConfiguration.setBuildCommand(makeCommandOnly);
			selectedConfiguration.setBuildArguments(makeArguments);
		}
		
		BuildMacroProvider provider = (BuildMacroProvider)ManagedBuildManager.getBuildMacroProvider();
		if(provider.canKeepMacrosInBuildfile(this.parent.getSelectedConfiguration()))
			provider.expandMacrosInBuildfile(selectedConfiguration,buildMacrosExpand.getSelection());
		
		setDirty(false);
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
		boolean hasSpace = false;
		
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
					hasSpace = true;
				}
			}
		}
		
//		if (hasSpace == true) {
//			return "\"" + command.toString().trim() + "\"";
//		} else {
			return command.toString().trim();
//		}
	}

	public IPreferenceStore getPreferenceStore() {
		return null;
	}
	/* (non-Javadoc)
	 * Initialize the "Use default command" field
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			useDefaultMake = !parent.getSelectedConfiguration().hasOverriddenBuildCommand();
			makeCommandDefault.setSelection(useDefaultMake);
			makeCommandEntry.setEditable(!makeCommandDefault.getSelection());
		}
		super.setVisible(visible);
	}

	/* (non-Javadoc)
	 * Event handler for the use default check box in the make command group
	 */
	protected void handleUseDefaultPressed() {
		// If the state of the button is unchecked, then we want to enable the edit widget
		boolean checked = makeCommandDefault.getSelection();
		if (checked == true) {
		    //  TODO: This should NOT change the configuration immediately -
		    //        it should set an intermediate variable and wait for OK/Apply
			parent.getSelectedConfiguration().setBuildCommand(null);
			parent.getSelectedConfiguration().setBuildArguments(null);
			makeCommandEntry.setEditable(false);
		} else {
			makeCommandEntry.setEditable(true);
		}
		setValues();
	}

	/**
	 * Sets the "dirty" state
	 */
	public void setDirty(boolean b) {
	    dirty = b;
	}

	/**
	 * Returns the "dirty" state
	 */
	public boolean isDirty() {
	    return dirty;
	}

}
