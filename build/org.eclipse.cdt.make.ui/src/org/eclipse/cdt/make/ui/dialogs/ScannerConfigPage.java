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

import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;

/**
 * Scanner Config settings page
 * 
 * @author vhirsl
 */
public class ScannerConfigPage extends AbstractCOptionPage {
	private static final String PREFIX = "ScannerConfigPage";	//$NON-NLS-1$
	private static final String LABEL = PREFIX + ".label";	//$NON-NLS-1$
	private static final String DESC = PREFIX + ".desc";	//$NON-NLS-1$
	private static final String ACTIVATE_AUTO_DISCOVERY = PREFIX + ".activate"; //$NON-NLS-1$
	private static final String SI_BUILD_PARSER_GROUP = PREFIX + ".siBuilder.parser.group_label"; //$NON-NLS-1$ 
	private static final String ENABLE_SI_BUILD_PARSER = PREFIX + ".siBuilder.parser.enable.label"; //$NON-NLS-1$
	private static final String SI_BUILD_PARSER_LABEL = PREFIX + ".siBuilder.parser.label"; //$NON-NLS-1$
	private static final String SI_PROVIDER_CMD_GROUP = PREFIX + ".siProvider.cmd.group_label"; //$NON-NLS-1$
	private static final String ENABLE_SI_PROVIDER_COMMAND = PREFIX + ".siProvider.cmd.enable.label";	//$NON-NLS-1$
	private static final String SI_PROVIDER_CMD_USE_DEFAULT = PREFIX + ".siProvider.cmd.use_default"; //$NON-NLS-1$
	private static final String SI_PROVIDER_CMD_LABEL = PREFIX + ".siProvider.cmd.label"; //$NON-NLS-1$
	private static final String SI_PROVIDER_PARSER_LABEL = PREFIX + ".siProvider.parser.label"; //$NON-NLS-1$
	private static final String SI_PROVIDER_CMD_ERROR_MESSAGE = PREFIX + ".siProvider.cmd.error_message"; //$NON-NLS-1$
	
	private boolean addTitle = false;

	private Button autoDiscovery;

	private Button defESIProviderCommandButton;
	private Text esiProviderCommand;
	
	private Button enableBuilderParserButton;
	private Combo makeBuilderSIParserComboBox;
	private Button enableProviderCommandButton;
	private Combo esiProviderParserComboBox;
	
	private IScannerConfigBuilderInfo fBuildInfo;
	private Map builderParsers = new HashMap();
	private String initialBuilderParserId = null;
	private Map providerParsers = new HashMap();
	private String initialProviderParserId = null;
	
	/**
	 * Default constructor
	 */
	public ScannerConfigPage() {
		super(MakeUIPlugin.getResourceString(LABEL));
		setDescription(MakeUIPlugin.getResourceString(DESC));
	}

	/**
	 * @param addTitle
	 */
	public ScannerConfigPage(boolean addTitle) {
		this();
		this.addTitle = addTitle;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#isValid()
	 */
	public boolean isValid() {
		if (!useDefaultESIProviderCmd()) {
			String cmd = getSIProviderCommandLine();
			if (cmd == null || cmd.length() == 0) {
				return false;
			}
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#setContainer(org.eclipse.cdt.ui.dialogs.ICOptionContainer)
	 */
	public void setContainer(ICOptionContainer container) {
		super.setContainer(container);
		IProject project = container.getProject();
		if (project != null) {
			try {
				fBuildInfo = MakeCorePlugin.createScannerConfigBuildInfo(project, ScannerConfigBuilder.BUILDER_ID);
			}
			catch (CoreException e) {
				fBuildInfo = MakeCorePlugin.createScannerConfigBuildInfo(container.getPreferences(), ScannerConfigBuilder.BUILDER_ID, true);
			}
		}
		else {
			fBuildInfo = MakeCorePlugin.createScannerConfigBuildInfo(container.getPreferences(), ScannerConfigBuilder.BUILDER_ID, false);
		}
		retrieveSIConsoleParsers();
		initialBuilderParserId = fBuildInfo.getMakeBuilderConsoleParserId();	//$NON-NLS-1$
		initialProviderParserId = fBuildInfo.getESIProviderConsoleParserId();	//$NON-NLS-1$
	}
	
	/**
	 * Fills console parser maps
	 */
	private void retrieveSIConsoleParsers() {
		IExtensionPoint ep = MakeCorePlugin.getDefault().getDescriptor().
			getExtensionPoint(MakeCorePlugin.SI_CONSOLE_PARSER_SIMPLE_ID);
		if (ep != null) {
			IExtension[] extensions = ep.getExtensions();
			for (int i = 0; i < extensions.length; ++i) {
				String parserId = extensions[i].getUniqueIdentifier();
				String label = extensions[i].getLabel();
				IConfigurationElement[] elements = (IConfigurationElement[]) extensions[i].getConfigurationElements();
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
	 * @see org.eclipse.cdt..dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		// install/deinstall the scanner configuration builder
		IProject project = getContainer().getProject();
		IScannerConfigBuilderInfo buildInfo;
		if (project != null) {
			if (autoDiscovery.getSelection()) {
				ScannerConfigNature.addScannerConfigNature(project);
				buildInfo = MakeCorePlugin.createScannerConfigBuildInfo(project, ScannerConfigBuilder.BUILDER_ID);
				buildInfo.setAutoDiscoveryEnabled(autoDiscovery.getSelection());
			}
			else {
				ScannerConfigNature.removeScannerConfigNature(project);
				return;
			}
		}
		else {
			buildInfo = MakeCorePlugin.createScannerConfigBuildInfo(getContainer().getPreferences(), ScannerConfigBuilder.BUILDER_ID, false);
			buildInfo.setAutoDiscoveryEnabled(autoDiscovery.getSelection());
		}

		buildInfo.setMakeBuilderConsoleParserEnabled(enableBuilderParser());
		if (enableBuilderParser()) {
			buildInfo.setMakeBuilderConsoleParserId(
					(String)builderParsers.get(makeBuilderSIParserComboBox.getText()));
		}

		buildInfo.setESIProviderCommandEnabled(enableProviderCommand());
		if (enableProviderCommand()) {
			buildInfo.setUseDefaultESIProviderCmd(useDefaultESIProviderCmd());
			if (!useDefaultESIProviderCmd()) {
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
			buildInfo.setESIProviderConsoleParserId(
					(String)providerParsers.get(esiProviderParserComboBox.getText()));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
		IScannerConfigBuilderInfo buildInfo;
		// Populate with the default value
		if (getContainer().getProject() != null) {
			// get the preferences
			buildInfo = MakeCorePlugin.createScannerConfigBuildInfo(getContainer().getPreferences(),
					ScannerConfigBuilder.BUILDER_ID, false);
		}
		else {
			// get the defaults
			buildInfo = MakeCorePlugin.createScannerConfigBuildInfo(getContainer().getPreferences(),
					ScannerConfigBuilder.BUILDER_ID, true);
		}
		
		autoDiscovery.setSelection(buildInfo.isAutoDiscoveryEnabled());

		enableBuilderParserButton.setSelection(buildInfo.isMakeBuilderConsoleParserEnabled());
		String builderParserId = buildInfo.getMakeBuilderConsoleParserId();
		for (Iterator i = builderParsers.keySet().iterator(); i.hasNext(); ) {
			String builderParser = (String) i.next();
			if (builderParserId.equals((String) builderParsers.get(builderParser))) {
				makeBuilderSIParserComboBox.setText(builderParser);
			}
		}

		enableProviderCommandButton.setSelection(buildInfo.isESIProviderCommandEnabled());
		defESIProviderCommandButton.setSelection(buildInfo.isDefaultESIProviderCmd());
		IPath sCommand = fBuildInfo.getESIProviderCommand();
		if (sCommand != null) {
			StringBuffer cmd = new StringBuffer(sCommand.toOSString());
			String args = buildInfo.getESIProviderArguments();
			if (args != null && !args.equals("")) { //$NON-NLS-1$
				cmd.append(' ');
				cmd.append(args);
			}
			esiProviderCommand.setText(cmd.toString());
		}
		enableProviderCommandButton.setSelection(buildInfo.isESIProviderCommandEnabled());
		String providerParserId = buildInfo.getESIProviderConsoleParserId();
		for (Iterator i = providerParsers.keySet().iterator(); i.hasNext(); ) {
			String providerParser = (String) i.next();
			if (providerParserId.equals((String) providerParsers.get(providerParser))) {
				esiProviderParserComboBox.setText(providerParser);
			}
		}
		// enable controls according to Auto Discovery button selection
		enableAllControls(enableAutoDiscovery());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite composite = ControlFactory.createComposite(parent, 1);
		setControl(composite);

		if (addTitle) {
			addTitle(composite);
		}
		addSCDiscoveryState(composite);
		addSeparator(composite);
		createBuildOutputParserControls(composite);
		createAfterBuildCmdControls(composite);
		// enable controls depending on the state of auto discovery
		enableAllControls(enableAutoDiscovery());
	}

	private void addTitle(Composite composite) {
		//Label for dialog title
		Label pathLabel = ControlFactory.createLabel(composite, 
				MakeUIPlugin.getResourceString(DESC));
	}
	
	private void addSCDiscoveryState(Composite parent) {
		//Checkbox for enabling the discovery
		ControlFactory.insertSpace(parent, 1, 10);
		autoDiscovery = ControlFactory.createCheckBox(parent, 
			MakeUIPlugin.getResourceString(ACTIVATE_AUTO_DISCOVERY));
		autoDiscovery.setSelection(fBuildInfo.isAutoDiscoveryEnabled());
		autoDiscovery.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				enableAllControls(enableAutoDiscovery());
				getContainer().updateContainer();
			}
		});
	}

	/**
	 * @param enable
	 */
	private void enableAllControls(boolean enable) {
		enableBuilderParserButton.setEnabled(enable);
		makeBuilderSIParserComboBox.setEnabled(enable && enableBuilderParser());
		enableProviderCommandButton.setEnabled(enable);
		defESIProviderCommandButton.setEnabled(enable && enableProviderCommand());
		esiProviderCommand.setEnabled(enable && enableProviderCommand() && !useDefaultESIProviderCmd());
		esiProviderParserComboBox.setEnabled(enable && enableProviderCommand());
	}

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
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
				makeBuilderSIParserComboBox.setEnabled(enableBuilderParser());
				getContainer().updateContainer();
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
				defESIProviderCommandButton.setEnabled(enableProviderCommand());
				esiProviderCommand.setEnabled(enableProviderCommand() && !useDefaultESIProviderCmd());
				esiProviderParserComboBox.setEnabled(enableProviderCommand());
				getContainer().updateContainer();
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
				getContainer().updateContainer();
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
		esiProviderCommand.addListener(SWT.Modify, new Listener() {
			public void handleEvent(Event e) {
				getContainer().updateContainer();
			}
		});
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
		defESIProviderCommandButton.setSelection(fBuildInfo.isDefaultESIProviderCmd());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
	 */
	public String getErrorMessage() {
		if (!useDefaultESIProviderCmd()) {
			String cmd = getSIProviderCommandLine();
			if (cmd == null || cmd.length() == 0) {
				return MakeUIPlugin.getResourceString(SI_PROVIDER_CMD_ERROR_MESSAGE);
//				return "Must enter a 'generate scanner info' command";	//$NON-NLS-1$
			}
		}
		return super.getErrorMessage();
	}

	private boolean enableAutoDiscovery() {
		return autoDiscovery.getSelection();
	}
	
	private boolean useDefaultESIProviderCmd() {
		return defESIProviderCommandButton.getSelection();
	}

	private String getSIProviderCommandLine() {
		return esiProviderCommand.getText().trim();
	}

	private boolean enableBuilderParser() {
		return enableBuilderParserButton.getSelection();
	}

	private boolean enableProviderCommand() {
		return enableProviderCommandButton.getSelection();
	}
}