/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.make.ui.dialogs;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.internal.ui.MessageLine;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * A dialog to set scanner config discovery options
 * 
 * @author vhirsl
 */
public class ScannerConfigOptionsDialog extends Dialog {
	private static final String PREFIX = "ScannerConfigOptionsDialog"; //$NON-NLS-1$
	private static final String DIALOG_TITLE = PREFIX + ".title"; //$NON-NLS-1$
	private static final String SI_BUILD_PARSER_GROUP = PREFIX + ".siBuilder.parser.group_label"; //$NON-NLS-1$ 
	private static final String ENABLE_SI_BUILD_PARSER = PREFIX + ".siBuilder.parser.enable.label"; //$NON-NLS-1$
	private static final String SI_BUILD_PARSER_LABEL = PREFIX + ".siBuilder.parser.label"; //$NON-NLS-1$
	private static final String SI_PROVIDER_CMD_GROUP = PREFIX + ".siProvider.cmd.group_label"; //$NON-NLS-1$
	private static final String ENABLE_SI_PROVIDER_COMMAND = PREFIX + ".siProvider.cmd.enable.label";	//$NON-NLS-1$
	private static final String SI_PROVIDER_CMD_USE_DEFAULT = PREFIX + ".siProvider.cmd.use_default"; //$NON-NLS-1$
	private static final String SI_PROVIDER_CMD_LABEL = PREFIX + ".siProvider.cmd.label"; //$NON-NLS-1$
	private static final String SI_PROVIDER_PARSER_LABEL = PREFIX + ".siProvider.parser.label"; //$NON-NLS-1$
	private static final String SI_PROVIDER_CMD_ERROR_MESSAGE = PREFIX + ".siProvider.cmd.error_message"; //$NON-NLS-1$

	private MessageLine fStatusLine;
	private Button defESIProviderCommandButton;
	private Text esiProviderCommand;
	private Button enableBuilderParserButton;
	private Combo makeBuilderSIParserComboBox;
	private Button enableProviderCommandButton;
	private Combo esiProviderParserComboBox;

	private ICOptionContainer fContainer;
	private Preferences fPrefs;
	private IScannerConfigBuilderInfo fBuildInfo;
	private boolean fInitialized;
	private Map builderParsers = new HashMap();
	private String initialBuilderParserId = null;
	private Map providerParsers = new HashMap();
	private String initialProviderParserId = null;
	
	/**
	 * Local store for Scanner Config discovery setting
	 * 
	 * @author vhirsl
	 */
	protected class LocalStore implements IScannerConfigBuilderInfo {
		private boolean fAutoDiscoveryEnabled;
		private boolean fMakeBuilderConsoleParserEnabled;
		private String fMakeBuilderConsoleParserId;
		private boolean fESIProviderCommandEnabled;
		private boolean fDefaultESIProviderCmd;
		private IPath fESIProviderCommand;
		private String fESIProviderArguments;
		private String fESIProviderConsoleParserId;

		public LocalStore(IScannerConfigBuilderInfo info) {
			try {
				setAutoDiscoveryEnabled(info.isAutoDiscoveryEnabled());
				setMakeBuilderConsoleParserEnabled(info.isMakeBuilderConsoleParserEnabled());
				setMakeBuilderConsoleParserId(info.getMakeBuilderConsoleParserId());
				setESIProviderCommandEnabled(info.isESIProviderCommandEnabled());
				setUseDefaultESIProviderCmd(info.isDefaultESIProviderCmd());
				setESIProviderCommand(info.getESIProviderCommand());
				setESIProviderArguments(info.getESIProviderArguments());
				setESIProviderConsoleParserId(info.getESIProviderConsoleParserId());
			} catch (CoreException e) {
			}
		}
		
		public boolean isAutoDiscoveryEnabled() {
			return fAutoDiscoveryEnabled;
		}
		public void setAutoDiscoveryEnabled(boolean enabled) throws CoreException {
			fAutoDiscoveryEnabled = enabled;
		}

		public boolean isMakeBuilderConsoleParserEnabled() {
			return fMakeBuilderConsoleParserEnabled;
		}
		public void setMakeBuilderConsoleParserEnabled(boolean enabled) throws CoreException {
			fMakeBuilderConsoleParserEnabled = enabled;
		}

		public String getMakeBuilderConsoleParserId() {
			return fMakeBuilderConsoleParserId;
		}
		public void setMakeBuilderConsoleParserId(String parserId) throws CoreException {
			fMakeBuilderConsoleParserId = new String(parserId);
			
		}

		public boolean isESIProviderCommandEnabled() {
			return fESIProviderCommandEnabled;
		}
		public void setESIProviderCommandEnabled(boolean enabled) throws CoreException {
			fESIProviderCommandEnabled = enabled;
		}

		public boolean isDefaultESIProviderCmd() {
			return fDefaultESIProviderCmd;
		}
		public void setUseDefaultESIProviderCmd(boolean on) throws CoreException {
			fDefaultESIProviderCmd = on;
		}

		public IPath getESIProviderCommand() {
			return fESIProviderCommand;
		}
		public void setESIProviderCommand(IPath command) throws CoreException {
			fESIProviderCommand = new Path(command.toString());
		}

		public String getESIProviderArguments() {
			return fESIProviderArguments;
		}
		public void setESIProviderArguments(String args) throws CoreException {
			fESIProviderArguments = new String(args);
		}

		public String getESIProviderConsoleParserId() {
			return fESIProviderConsoleParserId;
		}
		public void setESIProviderConsoleParserId(String parserId) throws CoreException {
			fESIProviderConsoleParserId = new String(parserId);
		}
	}

	/**
	 * A temporary page to retrieve SC options from preferences
	 * Not to be shown
	 *  
	 * @param container
	 */
	ScannerConfigOptionsDialog(ICOptionContainer container) {
		super(null);
		fInitialized = false;
		fContainer = container;
		fPrefs = fContainer.getPreferences();
		IScannerConfigBuilderInfo fInfo = MakeCorePlugin.createScannerConfigBuildInfo(fPrefs, ScannerConfigBuilder.BUILDER_ID, false);
		// Create local store
		fBuildInfo = new LocalStore(fInfo);
	}
	/**
	 * @param parentShell
	 * @param container
	 */
	ScannerConfigOptionsDialog(Shell parentShell, ICOptionContainer container) {
		super(parentShell);
		fInitialized = true;
		fContainer = container;
		IProject project = fContainer.getProject();
		fPrefs = fContainer.getPreferences();

		IScannerConfigBuilderInfo fInfo;
		if (project != null) {
			try {
				fInfo = MakeCorePlugin.createScannerConfigBuildInfo(project, ScannerConfigBuilder.BUILDER_ID);
			}
			catch (CoreException e) {
				fInitialized = false;
				fInfo = MakeCorePlugin.createScannerConfigBuildInfo(fPrefs, ScannerConfigBuilder.BUILDER_ID, true);
			}
		}
		else {
			fInfo = MakeCorePlugin.createScannerConfigBuildInfo(fPrefs, ScannerConfigBuilder.BUILDER_ID, false);
		}
		retrieveSIConsoleParsers();
		initialBuilderParserId = fInfo.getMakeBuilderConsoleParserId();	//$NON-NLS-1$
		initialProviderParserId = fInfo.getESIProviderConsoleParserId();	//$NON-NLS-1$
		
		// Create local store
		fBuildInfo = new LocalStore(fInfo);
	}
	
	/**
	 * Fills console parser maps
	 */
	private void retrieveSIConsoleParsers() {
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(MakeCorePlugin.SI_CONSOLE_PARSER_SIMPLE_ID);
		if (ep != null) {
			IExtension[] extensions = ep.getExtensions();
			for (int i = 0; i < extensions.length; ++i) {
				String parserId = extensions[i].getUniqueIdentifier();
				String label = extensions[i].getLabel();
				IConfigurationElement[] elements = extensions[i].getConfigurationElements();
				String commandId = elements[0].getAttribute("commandId");	//$NON-NLS-1$
				if (commandId.equals("makeBuilder") || commandId.equals("all")) {	//$NON-NLS-1$//$NON-NLS-2$
					builderParsers.put(label, parserId);
				}
				if (commandId.equals("externalScannerInfoProvider") || commandId.equals("all")) {	//$NON-NLS-1$//$NON-NLS-2$
					providerParsers.put(label, parserId);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	protected void configureShell(Shell newShell) {
		newShell.setText(getTitle(DIALOG_TITLE));
		super.configureShell(newShell);
	}

	/**
	 * @return MakeUIPlugin resource string
	 */
	private String getTitle(String title) {
		return MakeUIPlugin.getResourceString(title);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		initializeDialogUnits(composite);
		
		// create message line
		fStatusLine = new MessageLine(composite);
		fStatusLine.setAlignment(SWT.LEFT);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
//		gd.widthHint = convertWidthInCharsToPixels(50);
		fStatusLine.setLayoutData(gd);
		fStatusLine.setMessage(getTitle(DIALOG_TITLE));
		
		createBuildOutputParserControls(composite);
		createAfterBuildCmdControls(composite);
		// enable controls depending on the state of auto discovery
		enableAllControls();
		
		return composite;
	}

	private void createBuildOutputParserControls(Composite parent) {
//		ControlFactory.insertSpace(parent, 1, 10);
		Group bopGroup = ControlFactory.createGroup(parent, 
			MakeUIPlugin.getResourceString(SI_BUILD_PARSER_GROUP), 2);
		((GridLayout)bopGroup.getLayout()).marginHeight = 5;
		((GridLayout)bopGroup.getLayout()).marginWidth = 5;
		((GridData)bopGroup.getLayoutData()).verticalAlignment = GridData.FILL;

		enableBuilderParserButton = ControlFactory.createCheckBox(bopGroup, 
			MakeUIPlugin.getResourceString(ENABLE_SI_BUILD_PARSER));
		((GridData)enableBuilderParserButton.getLayoutData()).horizontalSpan = 2;
		boolean enabledBuilderParser = fBuildInfo.isMakeBuilderConsoleParserEnabled();
		enableBuilderParserButton.setSelection(enabledBuilderParser);
		enableBuilderParserButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				makeBuilderSIParserComboBox.setEnabled(isBuilderParserEnabled());
			}
		});
			
		Label label = ControlFactory.createLabel(bopGroup, 
				MakeUIPlugin.getResourceString(SI_BUILD_PARSER_LABEL));
		((GridData)label.getLayoutData()).grabExcessHorizontalSpace = false;
		
		makeBuilderSIParserComboBox = new Combo(bopGroup, SWT.DROP_DOWN | SWT.READ_ONLY);

		// fill the combobox and set the initial value
		Iterator items = builderParsers.keySet().iterator();
		while (items.hasNext()) {
			String parser = (String) items.next();
			makeBuilderSIParserComboBox.add(parser);
			if (initialBuilderParserId.equals(builderParsers.get(parser))) {
				makeBuilderSIParserComboBox.setText(parser);
			}
		}
		makeBuilderSIParserComboBox.setEnabled(enabledBuilderParser);
	}
	
	private void createAfterBuildCmdControls(Composite parent) {
		Group abcGroup = ControlFactory.createGroup(parent, 
				MakeUIPlugin.getResourceString(SI_PROVIDER_CMD_GROUP), 2);
		((GridData)abcGroup.getLayoutData()).horizontalSpan = 2;
		
		enableProviderCommandButton = ControlFactory.createCheckBox(abcGroup, 
				MakeUIPlugin.getResourceString(ENABLE_SI_PROVIDER_COMMAND));
		((GridData)enableProviderCommandButton.getLayoutData()).horizontalSpan = 2;
		((GridData)enableProviderCommandButton.getLayoutData()).horizontalAlignment = GridData.FILL_HORIZONTAL;
		boolean enabledProviderCommand = fBuildInfo.isESIProviderCommandEnabled();
		enableProviderCommandButton.setSelection(enabledProviderCommand);
		enableProviderCommandButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				defESIProviderCommandButton.setEnabled(isProviderCommandEnabled());
				esiProviderCommand.setEnabled(isProviderCommandEnabled() && !useDefaultESIProviderCmd());
				esiProviderParserComboBox.setEnabled(isProviderCommandEnabled());
			}
		});
		
		createESIProviderCmdControls(abcGroup);

		Label label = ControlFactory.createLabel(abcGroup, 
				MakeUIPlugin.getResourceString(SI_PROVIDER_PARSER_LABEL));
		((GridData)label.getLayoutData()).grabExcessHorizontalSpace = false;
		
		esiProviderParserComboBox = new Combo(abcGroup, SWT.DROP_DOWN | SWT.READ_ONLY);

		// fill the combobox and set the initial value
		Iterator items = providerParsers.keySet().iterator();
		while (items.hasNext()) {
			String parser = (String) items.next();
			esiProviderParserComboBox.add(parser);
			if (initialProviderParserId.equals(providerParsers.get(parser))) {
				esiProviderParserComboBox.setText(parser);
			}
		}
		defESIProviderCommandButton.setEnabled(enabledProviderCommand);
		esiProviderCommand.setEnabled(enabledProviderCommand && !useDefaultESIProviderCmd());
		esiProviderParserComboBox.setEnabled(enabledProviderCommand);
	}
	
	private void createESIProviderCmdControls(Composite parent) {
		defESIProviderCommandButton = ControlFactory.createCheckBox(parent, 
				MakeUIPlugin.getResourceString(SI_PROVIDER_CMD_USE_DEFAULT));
		defESIProviderCommandButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				esiProviderCommand.setEnabled(!useDefaultESIProviderCmd());
			}
		});
		((GridData) (defESIProviderCommandButton.getLayoutData())).horizontalAlignment = GridData.FILL_HORIZONTAL;
		((GridData) (defESIProviderCommandButton.getLayoutData())).horizontalSpan = 2;
		Label label = ControlFactory.createLabel(parent, 
				MakeUIPlugin.getResourceString(SI_PROVIDER_CMD_LABEL));
		((GridData) (label.getLayoutData())).horizontalAlignment = GridData.BEGINNING;
		((GridData) (label.getLayoutData())).grabExcessHorizontalSpace = false;
		esiProviderCommand = ControlFactory.createTextField(parent, SWT.SINGLE | SWT.BORDER);
		((GridData) (esiProviderCommand.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (esiProviderCommand.getLayoutData())).grabExcessHorizontalSpace = true;
		IPath sCommand = fBuildInfo.getESIProviderCommand();
		if (sCommand != null) {
			StringBuffer cmd = new StringBuffer(sCommand.toOSString());
			String args = fBuildInfo.getESIProviderArguments();
			if (args != null && args.length() > 0) { 
				cmd.append(' ');
				cmd.append(args);
			}
			esiProviderCommand.setText(cmd.toString());
		}
		if (fBuildInfo.isDefaultESIProviderCmd()) {
			esiProviderCommand.setEnabled(false);
		}
		esiProviderCommand.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				handleProviderCommandModify();
			}
		});
		defESIProviderCommandButton.setSelection(fBuildInfo.isDefaultESIProviderCmd());
	}

	/**
	 * 
	 */
	protected void handleProviderCommandModify() {
		String newCommand = esiProviderCommand.getText().trim();
		if (newCommand.length() == 0) {
			fStatusLine.setErrorMessage(getTitle(SI_PROVIDER_CMD_ERROR_MESSAGE));
			getButton(IDialogConstants.OK_ID).setEnabled(false);
		}
		else {
			fStatusLine.setErrorMessage(null);
			getButton(IDialogConstants.OK_ID).setEnabled(true);
		}
	}

	/**
	 * @param enable
	 */
	private void enableAllControls() {
		enableBuilderParserButton.setEnabled(true);
		makeBuilderSIParserComboBox.setEnabled(isBuilderParserEnabled());
		enableProviderCommandButton.setEnabled(true);
		defESIProviderCommandButton.setEnabled(isProviderCommandEnabled());
		esiProviderCommand.setEnabled(isProviderCommandEnabled() && !useDefaultESIProviderCmd());
		esiProviderParserComboBox.setEnabled(isProviderCommandEnabled());
	}

	private boolean useDefaultESIProviderCmd() {
		return defESIProviderCommandButton.getSelection();
	}

	private String getSIProviderCommandLine() {
		return esiProviderCommand.getText().trim();
	}

	private boolean isBuilderParserEnabled() {
		return enableBuilderParserButton.getSelection();
	}

	private boolean isProviderCommandEnabled() {
		return enableProviderCommandButton.getSelection();
	}

	/**
	 * Retrieves the state of scanner config discovery
	 * 
	 * @return boolean
	 */
	public boolean isScannerConfigDiscoveryEnabled() {
		return fBuildInfo.isAutoDiscoveryEnabled();
	}
	
	/**
	 * Enables or disables the scanner config discovery
	 * 
	 * @param enabled (boolean)
	 */
	public void setScannerConfigDiscoveryEnabled(boolean enabled) {
		try {
			fBuildInfo.setAutoDiscoveryEnabled(enabled);
		} 
		catch (CoreException e) {
		}
	}
	
	/**
	 * Called by BuildPathInfoBlock.performApply
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		IProject project = fContainer.getProject();
		IScannerConfigBuilderInfo buildInfo;
		if (project != null) {
			buildInfo = MakeCorePlugin.createScannerConfigBuildInfo(project, ScannerConfigBuilder.BUILDER_ID);
		}
		else {
			buildInfo = MakeCorePlugin.createScannerConfigBuildInfo(fPrefs, ScannerConfigBuilder.BUILDER_ID, false);
		}

		buildInfo.setAutoDiscoveryEnabled(fBuildInfo.isAutoDiscoveryEnabled());
		if (fBuildInfo.isAutoDiscoveryEnabled()) {
			buildInfo.setMakeBuilderConsoleParserEnabled(fBuildInfo.isMakeBuilderConsoleParserEnabled());
			if (fBuildInfo.isMakeBuilderConsoleParserEnabled()) {
				buildInfo.setMakeBuilderConsoleParserId(fBuildInfo.getMakeBuilderConsoleParserId());
			}
			buildInfo.setESIProviderCommandEnabled(fBuildInfo.isESIProviderCommandEnabled());
			if (fBuildInfo.isESIProviderCommandEnabled()) {
				buildInfo.setUseDefaultESIProviderCmd(fBuildInfo.isDefaultESIProviderCmd());
				if (!fBuildInfo.isDefaultESIProviderCmd()) {
					buildInfo.setESIProviderCommand(fBuildInfo.getESIProviderCommand());
					buildInfo.setESIProviderArguments(fBuildInfo.getESIProviderArguments());
				}
				buildInfo.setESIProviderConsoleParserId(fBuildInfo.getESIProviderConsoleParserId());
			}
		}
	}

	/**
	 * @param buildInfo
	 * @throws CoreException
	 */
	private void storeSIProviderCommandLine(IScannerConfigBuilderInfo buildInfo) throws CoreException {
		String esiProviderLine = getSIProviderCommandLine();
		int start = 0;
		int end = -1;
		if (esiProviderLine.startsWith("\"")) { //$NON-NLS-1$
			start = 1;
			end = esiProviderLine.indexOf('"', 1);
		}
		else {
			end = esiProviderLine.indexOf(' ');
		}
		IPath path;
		if (end == -1) {
			path = new Path(esiProviderLine);
		} else {
			path = new Path(esiProviderLine.substring(start, end));
		}
		buildInfo.setESIProviderCommand(path);
		String args = ""; //$NON-NLS-1$
		if (end != -1) {
			args = esiProviderLine.substring(end + 1);
		}
		buildInfo.setESIProviderArguments(args);
	}

	/**
	 * Called by BuildPathInfoBlock.performDefaults
	 */
	public void performDefaults() {
		IScannerConfigBuilderInfo buildInfo;
		// Populate with the default values
		if (fContainer.getProject() != null) {
			// get the preferences
			buildInfo = MakeCorePlugin.createScannerConfigBuildInfo(fPrefs, ScannerConfigBuilder.BUILDER_ID, false);
		}
		else {
			// get the defaults
			buildInfo = MakeCorePlugin.createScannerConfigBuildInfo(fPrefs, ScannerConfigBuilder.BUILDER_ID, true);
		}
		
		fBuildInfo = new LocalStore(buildInfo);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {
		// Store UI values to the LocalStore
		try {
			fBuildInfo.setAutoDiscoveryEnabled(isScannerConfigDiscoveryEnabled());
			fBuildInfo.setMakeBuilderConsoleParserEnabled(isBuilderParserEnabled());
			fBuildInfo.setMakeBuilderConsoleParserId((String)builderParsers.get(makeBuilderSIParserComboBox.getText()));
			fBuildInfo.setESIProviderCommandEnabled(isProviderCommandEnabled());
			fBuildInfo.setUseDefaultESIProviderCmd(useDefaultESIProviderCmd());
			storeSIProviderCommandLine(fBuildInfo);
			fBuildInfo.setESIProviderConsoleParserId((String)providerParsers.get(esiProviderParserComboBox.getText()));
		} catch (CoreException e) {
		}
		super.okPressed();
	}

	/**
	 * @return true if successfully initialized, false if not
	 */
	public boolean isInitialized() {
		return fInitialized;
	}
}