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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigNature;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredPathContainer;
import org.eclipse.cdt.make.internal.ui.MakeUIPlugin;
import org.eclipse.cdt.make.ui.IMakeHelpContextIds;
import org.eclipse.cdt.ui.dialogs.AbstractCOptionPage;
import org.eclipse.cdt.ui.dialogs.ICOptionContainer;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
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
import org.eclipse.ui.help.WorkbenchHelp;

/**
 * A dialog to set scanner config discovery options
 * 
 * @author vhirsl
 */
public class DiscoveryOptionsBlock extends AbstractCOptionPage {

	private static final String PREFIX_BP = "BuildPathInfoBlock"; //$NON-NLS-1$
	private static final String SC_GROUP_LABEL = PREFIX_BP + ".scGroup.label"; //$NON-NLS-1$
	private static final String SC_ENABLED_LABEL = PREFIX_BP + ".scGroup.enabled.label"; //$NON-NLS-1$
	private static final String MISSING_BUILDER_MSG = "ScannerConfigOptionsDialog.label.missingBuilderInformation"; //$NON-NLS-1$

	private static final String PREFIX = "ScannerConfigOptionsDialog"; //$NON-NLS-1$
	private static final String DIALOG_TITLE = PREFIX + ".title"; //$NON-NLS-1$
	private static final String DIALOG_DESCRIPTION = PREFIX + ".description"; //$NON-NLS-1$
	private static final String SI_BUILD_PARSER_GROUP = PREFIX + ".siBuilder.parser.group_label"; //$NON-NLS-1$ 
	private static final String ENABLE_SI_BUILD_PARSER = PREFIX + ".siBuilder.parser.enable.label"; //$NON-NLS-1$
	private static final String SI_BUILD_PARSER_LABEL = PREFIX + ".siBuilder.parser.label"; //$NON-NLS-1$
	private static final String SI_PROVIDER_CMD_GROUP = PREFIX + ".siProvider.cmd.group_label"; //$NON-NLS-1$
	private static final String ENABLE_SI_PROVIDER_COMMAND = PREFIX + ".siProvider.cmd.enable.label"; //$NON-NLS-1$
	private static final String SI_PROVIDER_CMD_USE_DEFAULT = PREFIX + ".siProvider.cmd.use_default"; //$NON-NLS-1$
	private static final String SI_PROVIDER_CMD_LABEL = PREFIX + ".siProvider.cmd.label"; //$NON-NLS-1$
	private static final String SI_PROVIDER_PARSER_LABEL = PREFIX + ".siProvider.parser.label"; //$NON-NLS-1$
	private static final String SI_PROVIDER_CMD_ERROR_MESSAGE = PREFIX + ".siProvider.cmd.error_message"; //$NON-NLS-1$
	private static final String SI_PROBLEM_GROUP = PREFIX + ".siProblem.group.label"; //$NON-NLS-1$
	private static final String ENABLE_SI_PROBLEM_GENERATION = PREFIX + ".siProblem.generation.enable.label"; //$NON-NLS-1$

	private Button scEnabledButton;
	private boolean needsSCNature = false;

	private Button defESIProviderCommandButton;
	private Text esiProviderCommand;
	private Button enableBuilderParserButton;
	private Combo makeBuilderSIParserComboBox;
	private Button enableProviderCommandButton;
	private Combo esiProviderParserComboBox;
	private Button enableProblemGenerationButton;

	private Preferences fPrefs;
	private IScannerConfigBuilderInfo fBuildInfo;
	private boolean fInitialized = false;
	private Map builderParsers = new HashMap();
	private String initialBuilderParserId = null;
	private Map providerParsers = new HashMap();
	private String initialProviderParserId = null;
	private boolean fCreatePathContainer = false;

	public DiscoveryOptionsBlock() {
		super(MakeUIPlugin.getResourceString(DIALOG_TITLE));
		setDescription(MakeUIPlugin.getResourceString(DIALOG_DESCRIPTION));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#setContainer(org.eclipse.cdt.ui.dialogs.ICOptionContainer)
	 */
	public void setContainer(ICOptionContainer container) {
		super.setContainer(container);

		fPrefs = getContainer().getPreferences();
		IProject project = getContainer().getProject();

		fInitialized = true;
		if (project != null) {
			try {
				fBuildInfo = MakeCorePlugin.createScannerConfigBuildInfo(project, ScannerConfigBuilder.BUILDER_ID);
			} catch (CoreException e) {
				fInitialized = false;
				fBuildInfo = MakeCorePlugin.createScannerConfigBuildInfo(fPrefs, ScannerConfigBuilder.BUILDER_ID, true);
			}
		} else {
			fCreatePathContainer = true;
			fBuildInfo = MakeCorePlugin.createScannerConfigBuildInfo(fPrefs, ScannerConfigBuilder.BUILDER_ID, false);
		}
		retrieveSIConsoleParsers();
		initialBuilderParserId = fBuildInfo.getMakeBuilderConsoleParserId(); //$NON-NLS-1$
		initialProviderParserId = fBuildInfo.getESIProviderConsoleParserId(); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performApply(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void performApply(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		IWorkspace workspace = MakeUIPlugin.getWorkspace();

		// To avoid multi-build
		IWorkspaceRunnable operation = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				IScannerConfigBuilderInfo buildInfo;
				IProject project = getContainer().getProject();
				if (project != null) {
					if (needsSCNature) {
						ScannerConfigNature.addScannerConfigNature(getContainer().getProject());
						needsSCNature = false;
						fCreatePathContainer = true;
					}
					buildInfo = MakeCorePlugin.createScannerConfigBuildInfo(project, ScannerConfigBuilder.BUILDER_ID);
					if (fCreatePathContainer) {
						createDiscoveredPathContainer(project, monitor);
						// create a new discovered scanner config store
						MakeCorePlugin.getDefault().getDiscoveryManager().removeDiscoveredInfo(project);
						fCreatePathContainer = false;
					}
				} else {
					buildInfo = MakeCorePlugin.createScannerConfigBuildInfo(fPrefs, ScannerConfigBuilder.BUILDER_ID, false);
				}

				buildInfo.setAutoDiscoveryEnabled(isScannerConfigDiscoveryEnabled());
				if (isScannerConfigDiscoveryEnabled()) {
					buildInfo.setMakeBuilderConsoleParserEnabled(isBuilderParserEnabled());
					if (isBuilderParserEnabled()) {
						buildInfo.setMakeBuilderConsoleParserId((String)builderParsers.get(makeBuilderSIParserComboBox.getText()));
					}
					buildInfo.setESIProviderCommandEnabled(isProviderCommandEnabled());
					if (isProviderCommandEnabled()) {
						buildInfo.setUseDefaultESIProviderCmd(useDefaultESIProviderCmd());
						if (!useDefaultESIProviderCmd()) {
							storeSIProviderCommandLine(buildInfo);
						}
						buildInfo.setESIProviderConsoleParserId((String)providerParsers.get(esiProviderParserComboBox.getText()));
					}
					buildInfo.setSIProblemGenerationEnabled(isProblemGenerationEnabled());
				}
			}
		};
		if (getContainer().getProject() != null) {
			workspace.run(operation, monitor);
		} else {
			operation.run(monitor);
		}
	}
	/**
	 * @param project
	 * @param monitor
	 * @throws CModelException
	 */
	private void createDiscoveredPathContainer(IProject project, IProgressMonitor monitor) throws CModelException {
		IPathEntry container = CoreModel.newContainerEntry(DiscoveredPathContainer.CONTAINER_ID);
		ICProject cProject = CoreModel.getDefault().create(project);
		if (cProject != null) {
			IPathEntry[] entries = cProject.getRawPathEntries();
			List newEntries = new ArrayList(Arrays.asList(entries));
			if (!newEntries.contains(container)) {
				newEntries.add(container);
				cProject.setRawPathEntries((IPathEntry[])newEntries.toArray(new IPathEntry[newEntries.size()]), monitor);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#performDefaults()
	 */
	public void performDefaults() {
		IScannerConfigBuilderInfo info;
		// Populate with the default values
		if (getContainer().getProject() != null) {
			// get the preferences
			info = MakeCorePlugin.createScannerConfigBuildInfo(fPrefs, ScannerConfigBuilder.BUILDER_ID, false);
		} else {
			// get the defaults
			info = MakeCorePlugin.createScannerConfigBuildInfo(fPrefs, ScannerConfigBuilder.BUILDER_ID, true);
		}

		setScannerConfigDiscoveryEnabled(info.isAutoDiscoveryEnabled());
		enableBuilderParserButton.setSelection(info.isMakeBuilderConsoleParserEnabled());
		makeBuilderSIParserComboBox.setText(getParserName(builderParsers, info.getMakeBuilderConsoleParserId()));
		enableProviderCommandButton.setSelection(info.isESIProviderCommandEnabled());
		defESIProviderCommandButton.setSelection(info.isDefaultESIProviderCmd());
		setESIProviderCommandFrom(info);
		esiProviderParserComboBox.setText(getParserName(providerParsers, info.getESIProviderConsoleParserId()));

		enableAllControls();
	}

	/**
	 * @param parsers
	 * @param consoleParserId
	 * @return
	 */
	private String getParserName(Map parsers, String consoleParserId) {
		for (Iterator i = parsers.keySet().iterator(); i.hasNext();) {
			String parserName = (String)i.next();
			String parserId = (String)parsers.get(parserName);
			if (parserId.equals(consoleParserId)) {
				return parserName;
			}
		}
		return consoleParserId;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		// Create the composite control for the tab
		int tabColumns = 2;
		Font font = parent.getFont();
		Composite composite = ControlFactory.createComposite(parent, tabColumns);
		((GridLayout)composite.getLayout()).makeColumnsEqualWidth = false;
		composite.setFont(font);
		setControl(composite);

		WorkbenchHelp.setHelp(getControl(), IMakeHelpContextIds.SCANNER_CONFIG_DISCOVERY_OPTIONS);

		// Create a group for scanner config discovery
		if (createScannerConfigControls(composite, tabColumns)) {
			createBuildOutputParserControls(composite);
			createAfterBuildCmdControls(composite);
			createProblemGenerationControls(composite);
			// enable controls depending on the state of auto discovery
			enableAllControls();
		}
	}

	/**
	 * @param composite
	 */
	private boolean createScannerConfigControls(Composite parent, int numColumns) {
		// Check if it is an old project
		IProject project = getContainer().getProject();
		boolean showMissingBuilder = false;
		try {
			if (project != null && project.hasNature(MakeProjectNature.NATURE_ID)
					&& !project.hasNature(ScannerConfigNature.NATURE_ID)) {
				needsSCNature = true; // an old project
			}
		} catch (CoreException e) {
			showMissingBuilder = true;
		}

		Group scGroup = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(SC_GROUP_LABEL), numColumns);
		scGroup.setFont(parent.getFont());
		((GridData)scGroup.getLayoutData()).grabExcessHorizontalSpace = true;
		((GridData)scGroup.getLayoutData()).horizontalSpan = numColumns;
		((GridData)scGroup.getLayoutData()).horizontalAlignment = GridData.FILL;

		if (showMissingBuilder || (!needsSCNature && !fInitialized)) {
			ControlFactory.createLabel(scGroup, MakeUIPlugin.getResourceString(MISSING_BUILDER_MSG));
			return false;
		}

		// Add checkbox
		scEnabledButton = ControlFactory.createCheckBox(scGroup, MakeUIPlugin.getResourceString(SC_ENABLED_LABEL));
		scEnabledButton.setFont(parent.getFont());
		((GridData)scEnabledButton.getLayoutData()).horizontalSpan = numColumns;
		((GridData)scEnabledButton.getLayoutData()).grabExcessHorizontalSpace = true;
		// VMIR* old projects will have discovery disabled by default
		scEnabledButton.setSelection(needsSCNature ? false : fBuildInfo.isAutoDiscoveryEnabled());
		scEnabledButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				enableAllControls();
			}
		});
		//		handleScannerConfigEnable(); Only if true in VMIR*
		return true;
	}

	/**
	 * Fills console parser maps
	 */
	private void retrieveSIConsoleParsers() {
		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(MakeCorePlugin.getUniqueIdentifier(),
				MakeCorePlugin.SI_CONSOLE_PARSER_SIMPLE_ID);
		if (ep != null) {
			IExtension[] extensions = ep.getExtensions();
			for (int i = 0; i < extensions.length; ++i) {
				String parserId = extensions[i].getUniqueIdentifier();
				String label = extensions[i].getLabel();
				IConfigurationElement[] elements = extensions[i].getConfigurationElements();
				String commandId = elements[0].getAttribute("commandId"); //$NON-NLS-1$
				if (commandId.equals("makeBuilder") || commandId.equals("all")) { //$NON-NLS-1$//$NON-NLS-2$
					builderParsers.put(label, parserId);
				}
				if (commandId.equals("externalScannerInfoProvider") || commandId.equals("all")) { //$NON-NLS-1$//$NON-NLS-2$
					providerParsers.put(label, parserId);
				}
			}
		}
	}

	private void createBuildOutputParserControls(Composite parent) {
		//		ControlFactory.insertSpace(parent, 1, 10);
		Group bopGroup = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(SI_BUILD_PARSER_GROUP), 2);
		((GridLayout)bopGroup.getLayout()).marginHeight = 5;
		((GridLayout)bopGroup.getLayout()).marginWidth = 5;
		((GridData)bopGroup.getLayoutData()).verticalAlignment = GridData.FILL;
		((GridData)bopGroup.getLayoutData()).horizontalSpan = 2;

		enableBuilderParserButton = ControlFactory.createCheckBox(bopGroup, MakeUIPlugin.getResourceString(ENABLE_SI_BUILD_PARSER));
		((GridData)enableBuilderParserButton.getLayoutData()).horizontalSpan = 2;
		boolean enabledBuilderParser = fBuildInfo.isMakeBuilderConsoleParserEnabled();
		enableBuilderParserButton.setSelection(enabledBuilderParser);
		enableBuilderParserButton.addSelectionListener(new SelectionAdapter() {

			public void widgetSelected(SelectionEvent e) {
				makeBuilderSIParserComboBox.setEnabled(isBuilderParserEnabled());
			}
		});

		Label label = ControlFactory.createLabel(bopGroup, MakeUIPlugin.getResourceString(SI_BUILD_PARSER_LABEL));
		((GridData)label.getLayoutData()).grabExcessHorizontalSpace = false;

		makeBuilderSIParserComboBox = new Combo(bopGroup, SWT.DROP_DOWN | SWT.READ_ONLY);

		// fill the combobox and set the initial value
		for (Iterator items = builderParsers.keySet().iterator(); items.hasNext();) {
			String parser = (String)items.next();
			makeBuilderSIParserComboBox.add(parser);
			if (initialBuilderParserId.equals(builderParsers.get(parser))) {
				makeBuilderSIParserComboBox.setText(parser);
			}
		}
		makeBuilderSIParserComboBox.setEnabled(enabledBuilderParser);
	}

	private void createAfterBuildCmdControls(Composite parent) {
		Group abcGroup = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(SI_PROVIDER_CMD_GROUP), 2);
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
				getContainer().updateContainer();
			}
		});

		createESIProviderCmdControls(abcGroup);

		Label label = ControlFactory.createLabel(abcGroup, MakeUIPlugin.getResourceString(SI_PROVIDER_PARSER_LABEL));
		((GridData)label.getLayoutData()).grabExcessHorizontalSpace = false;

		esiProviderParserComboBox = new Combo(abcGroup, SWT.DROP_DOWN | SWT.READ_ONLY);

		// fill the combobox and set the initial value
		for (Iterator items = providerParsers.keySet().iterator(); items.hasNext();) {
			String parser = (String)items.next();
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
		Label label = ControlFactory.createLabel(parent, MakeUIPlugin.getResourceString(SI_PROVIDER_CMD_LABEL));
		((GridData) (label.getLayoutData())).horizontalAlignment = GridData.BEGINNING;
		((GridData) (label.getLayoutData())).grabExcessHorizontalSpace = false;
		esiProviderCommand = ControlFactory.createTextField(parent, SWT.SINGLE | SWT.BORDER);
		((GridData) (esiProviderCommand.getLayoutData())).horizontalAlignment = GridData.FILL;
		((GridData) (esiProviderCommand.getLayoutData())).grabExcessHorizontalSpace = true;
		setESIProviderCommandFrom(fBuildInfo);
		if (fBuildInfo.isDefaultESIProviderCmd()) {
			esiProviderCommand.setEnabled(false);
		}
		esiProviderCommand.addListener(SWT.Modify, new Listener() {

			public void handleEvent(Event e) {
				getContainer().updateContainer();
			}
		});
		defESIProviderCommandButton.setSelection(fBuildInfo.isDefaultESIProviderCmd());
	}

	/**
	 * @param composite
	 */
	private void createProblemGenerationControls(Composite parent) {
		Group problemGroup = ControlFactory.createGroup(parent, MakeUIPlugin.getResourceString(SI_PROBLEM_GROUP), 2);
		((GridData)problemGroup.getLayoutData()).horizontalSpan = 2;

		enableProblemGenerationButton = ControlFactory.createCheckBox(problemGroup,
				MakeUIPlugin.getResourceString(ENABLE_SI_PROBLEM_GENERATION));
		((GridData)enableProblemGenerationButton.getLayoutData()).horizontalSpan = 2;
		((GridData)enableProblemGenerationButton.getLayoutData()).horizontalAlignment = GridData.FILL_HORIZONTAL;
		boolean enabledProblemGeneration = fBuildInfo.isSIProblemGenerationEnabled();
		enableProblemGenerationButton.setSelection(enabledProblemGeneration);
		enableProblemGenerationButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				getContainer().updateContainer();
			}
		});
		
	}

	/**
	 * @param buildInfo
	 */
	private void setESIProviderCommandFrom(IScannerConfigBuilderInfo buildInfo) {
		IPath sCommand = buildInfo.getESIProviderCommand();
		if (sCommand != null) {
			StringBuffer cmd = new StringBuffer(sCommand.toOSString());
			String args = buildInfo.getESIProviderArguments();
			if (args != null && args.length() > 0) {
				cmd.append(' ');
				cmd.append(args);
			}
			esiProviderCommand.setText(cmd.toString());
		}
	}

	/**
	 * @param enable
	 */
	private void enableAllControls() {
		enableBuilderParserButton.setEnabled(isScannerConfigDiscoveryEnabled());
		makeBuilderSIParserComboBox.setEnabled(isScannerConfigDiscoveryEnabled() && isBuilderParserEnabled());
		enableProviderCommandButton.setEnabled(isScannerConfigDiscoveryEnabled());
		defESIProviderCommandButton.setEnabled(isScannerConfigDiscoveryEnabled() && isProviderCommandEnabled());
		esiProviderCommand.setEnabled(isScannerConfigDiscoveryEnabled() && isProviderCommandEnabled()
				&& !useDefaultESIProviderCmd());
		esiProviderParserComboBox.setEnabled(isScannerConfigDiscoveryEnabled() && isProviderCommandEnabled());
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

	private boolean isProblemGenerationEnabled() {
		return enableProblemGenerationButton.getSelection();
	}

	/**
	 * Retrieves the state of scanner config discovery
	 * 
	 * @return boolean
	 */
	private boolean isScannerConfigDiscoveryEnabled() {
		return scEnabledButton.getSelection();
	}

	/**
	 * Enables or disables the scanner config discovery
	 * 
	 * @param enabled
	 *            (boolean)
	 */
	private void setScannerConfigDiscoveryEnabled(boolean enabled) {
		scEnabledButton.setSelection(enabled);
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
		} else {
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
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.dialogs.ICOptionPage#isValid()
	 */
	public boolean isValid() {
		if (isProviderCommandEnabled() == true && useDefaultESIProviderCmd() == false) {
			String cmd = getSIProviderCommandLine();
			if (cmd == null || cmd.length() == 0) {
				return false;
			}
		}
		return true;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
	 */
	public String getErrorMessage() {
		if (!isValid()) {
			return MakeUIPlugin.getResourceString(SI_PROVIDER_CMD_ERROR_MESSAGE);
		}
		return null;
	}
}