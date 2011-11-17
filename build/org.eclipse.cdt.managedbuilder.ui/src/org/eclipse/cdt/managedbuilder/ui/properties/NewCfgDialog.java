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
 * James Blackburn (Broadcom Corp.)
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.ui.properties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.core.settings.model.extension.CConfigurationData;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyManager;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyType;
import org.eclipse.cdt.managedbuilder.buildproperties.IBuildPropertyValue;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IProjectType;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.Configuration;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedProject;
import org.eclipse.cdt.managedbuilder.internal.ui.Messages;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;
import org.eclipse.cdt.ui.newui.INewCfgDialog;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.osgi.util.NLS;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class NewCfgDialog implements INewCfgDialog {
	private static final String NULL = "[null]"; //$NON-NLS-1$
	private static final String SEPARATOR = " > "; //$NON-NLS-1$
	private static final String ART = MBSWizardHandler.ARTIFACT;
	private static final String NOT = Messages.NewCfgDialog_3;
	// Widgets
	private Text configName;
	private Text configDescription;
	private Combo cloneConfigSelector;
	private Combo realConfigSelector;
	private Button b_cloneFromProject;
	private Button b_cloneFromExtension;
	private Button b_importFromOtherProject;
	private Button b_importPredefined;
	private Combo importSelector;
	private Combo importDefSelector;
	private Label statusLabel;

	/** Default configurations defined in the toolchain description */
	private ICProjectDescription des;
	private IConfiguration[] cfgds;
	private IConfiguration[] rcfgs;
	private IConfiguration parentConfig;
	private String newName;
	private String newDescription;
	private String title;
	private Map<String, IConfiguration> imported;
	private Map<String, IConfiguration> importedDef;

	protected Shell parentShell;

	private class LocalDialog extends Dialog {
		LocalDialog(Shell parentShell) {
			super(parentShell);
			setShellStyle(getShellStyle()|SWT.RESIZE);
		}
		/* (non-Javadoc)
		 * Method declared on Dialog. Cache the name and base config selections.
		 * We don't have to worry that the index or name is wrong because we
		 * enable the OK button IFF those conditions are met.
		 */
		@Override
		protected void buttonPressed(int buttonId) {
			if (buttonId == IDialogConstants.OK_ID) {
				newName = configName.getText().trim();
				newDescription = configDescription.getText().trim();
				if (b_cloneFromProject.getSelection())
					parentConfig = cfgds[cloneConfigSelector.getSelectionIndex()];
				else if (b_cloneFromExtension.getSelection()) // real cfg
					parentConfig = rcfgs[realConfigSelector.getSelectionIndex()];
				else if (b_importFromOtherProject.getSelection())
					parentConfig = getConfigFromName(importSelector.getText(), imported);
				else if (b_importPredefined.getSelection())
					parentConfig = getConfigFromName(importDefSelector.getText(), importedDef);
				if (parentConfig != null)
					newConfiguration();
			} else {
				newName = null;
				newDescription = null;
				parentConfig = null;
			}
			super.buttonPressed(buttonId);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
		 */
		@Override
		protected void configureShell(Shell shell) {
			super.configureShell(shell);
			if (title != null)
				shell.setText(title);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.dialogs.Dialog#createButtonsForButtonBar(org.eclipse.swt.widgets.Composite)
		 */
		@Override
		protected void createButtonsForButtonBar(Composite parent) {
			super.createButtonsForButtonBar(parent);
			configName.setFocus();
			if (configName != null) {
				configName.setText(newName);
			}
			setButtons();
		}

		@Override
		protected Control createDialogArea(Composite parent) {

			Composite composite = new Composite(parent, SWT.NULL);
			composite.setFont(parent.getFont());
			composite.setLayout(new GridLayout(3, false));
			composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			// Create a group for the name & description

			final Group group1 = new Group(composite, SWT.NONE);
			group1.setFont(composite.getFont());
			GridLayout layout1 = new GridLayout(3, false);
			group1.setLayout(layout1);
			GridData gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			group1.setLayoutData(gd);

			// bug 187634: Add a label to warn user that configuration name will be used directly
			// as a directory name in the filesystem.
			Label warningLabel = new Label(group1, SWT.BEGINNING | SWT.WRAP);
			warningLabel.setFont(parent.getFont());
			warningLabel.setText(Messages.NewConfiguration_label_warning);
			gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false, 3, 1);
			gd.widthHint = 300;
			warningLabel.setLayoutData(gd);

			// Add a label and a text widget for Configuration's name
			final Label nameLabel = new Label(group1, SWT.LEFT);
			nameLabel.setFont(parent.getFont());
			nameLabel.setText(Messages.NewConfiguration_label_name);

			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 1;
			gd.grabExcessHorizontalSpace = false;
			nameLabel.setLayoutData(gd);

			configName = new Text(group1, SWT.SINGLE | SWT.BORDER);
			configName.setFont(group1.getFont());
			configName.setText(newName);
			configName.setFocus();
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			gd.horizontalSpan = 2;
			gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
			configName.setLayoutData(gd);
			configName.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					setButtons();
				}
			});

//			 Add a label and a text widget for Configuration's description
	        final Label descriptionLabel = new Label(group1, SWT.LEFT);
	        descriptionLabel.setFont(parent.getFont());
	        descriptionLabel.setText(Messages.NewConfiguration_label_description);

	        gd = new GridData(GridData.FILL_HORIZONTAL);
	        gd.horizontalSpan = 1;
			gd.grabExcessHorizontalSpace = false;
	        descriptionLabel.setLayoutData(gd);
	        configDescription = new Text(group1, SWT.SINGLE | SWT.BORDER);
	        configDescription.setFont(group1.getFont());
			configDescription.setText(newDescription);
			configDescription.setFocus();

	        gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
	        gd.horizontalSpan = 2;
	        gd.widthHint = IDialogConstants.ENTRY_FIELD_WIDTH;
	        configDescription.setLayoutData(gd);

			final Group group = new Group(composite, SWT.NONE);
			group.setFont(composite.getFont());
			group.setText(Messages.NewConfiguration_label_group);
			GridLayout layout = new GridLayout(2, false);
			group.setLayout(layout);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			group.setLayoutData(gd);

			b_cloneFromProject = new Button(group, SWT.RADIO);
			b_cloneFromProject.setText(Messages.NewCfgDialog_0);
			gd = new GridData(GridData.BEGINNING);
			b_cloneFromProject.setLayoutData(gd);
			b_cloneFromProject.setSelection(true);
			b_cloneFromProject.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setButtons();
				}
			});

			cloneConfigSelector = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
			cloneConfigSelector.setFont(group.getFont());
			cloneConfigSelector.setItems(getConfigNamesAndDescriptions(cfgds, false));
			int index = cloneConfigSelector.indexOf(newName);
			cloneConfigSelector.select(index < 0 ? 0 : index);
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			cloneConfigSelector.setLayoutData(gd);
			cloneConfigSelector.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setButtons();
				}
			});

			b_cloneFromExtension = new Button(group, SWT.RADIO);
			b_cloneFromExtension.setText(Messages.NewCfgDialog_1);
			gd = new GridData(GridData.BEGINNING);
			b_cloneFromExtension.setLayoutData(gd);
			b_cloneFromExtension.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setButtons();
				}
			});

			String[] extCfgs = getConfigNamesAndDescriptions(rcfgs, true);
			realConfigSelector = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
			realConfigSelector.setFont(group.getFont());
			realConfigSelector.setItems(extCfgs);
			index = realConfigSelector.indexOf(newName);
			realConfigSelector.select(index < 0 ? 0 : index);
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			realConfigSelector.setLayoutData(gd);
			realConfigSelector.setEnabled(false);
			realConfigSelector.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setButtons();
				}
			});

			if(extCfgs.length == 0)
				b_cloneFromExtension.setEnabled(false);

			/* import */
			b_importFromOtherProject = new Button(group, SWT.RADIO);
			b_importFromOtherProject.setText(Messages.NewCfgDialog_4);
			gd = new GridData(GridData.BEGINNING);
			b_importFromOtherProject.setLayoutData(gd);
			b_importFromOtherProject.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setButtons();
				}
			});
			importSelector = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
			importSelector.setFont(group.getFont());
			importSelector.setItems(getImportItems());
			importSelector.select(0);
			importSelector.setVisibleItemCount(Math.min(10, importSelector.getItemCount()));
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			importSelector.setLayoutData(gd);
			importSelector.setEnabled(false);
			importSelector.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setButtons();
				}
			});

			/* import predefined */
			b_importPredefined = new Button(group, SWT.RADIO);
			b_importPredefined.setText(Messages.NewCfgDialog_5);
			gd = new GridData(GridData.BEGINNING);
			b_importPredefined.setLayoutData(gd);
			b_importPredefined.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setButtons();
				}
			});
			importDefSelector = new Combo(group, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
			importDefSelector.setFont(group.getFont());
			importDefSelector.setItems(getImportDefItems());
			importDefSelector.select(0);
			importDefSelector.setVisibleItemCount(Math.min(10, importDefSelector.getItemCount()));
			gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_FILL);
			importDefSelector.setLayoutData(gd);
			importDefSelector.setEnabled(false);
			importDefSelector.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					setButtons();
				}
			});

			statusLabel = new Label(composite, SWT.CENTER);
			gd = new GridData(GridData.FILL_HORIZONTAL);
			gd.horizontalSpan = 3;
			statusLabel.setLayoutData(gd);
			statusLabel.setFont(composite.getFont());
			statusLabel.setForeground(JFaceResources.getColorRegistry().get(JFacePreferences.ERROR_COLOR));

			return composite;
		}

		/* (non-Javadoc)
		 * Update the status message and button state based on the input selected
		 * by the user
		 *
		 */
		private void setButtons() {
			String s = null;
			String currentName = configName.getText();
			// Trim trailing whitespace
			while (currentName.length() > 0 && Character.isWhitespace(currentName.charAt(currentName.length()-1))) {
				currentName = currentName.substring(0, currentName.length()-1);
			}
			// Make sure that the name is at least one character in length
			if (currentName.length() == 0) {
				// No error message, but cannot select OK
				s = "";	//$NON-NLS-1$
			} else if (cfgds.length == 0) {
				s = "";	//$NON-NLS-1$
				// Make sure the name is not a duplicate
			} else if (isDuplicateName(currentName)) {
				s = NLS.bind(Messages.NewConfiguration_error_duplicateName, currentName);
			} else if (isSimilarName(currentName)) {
				s = NLS.bind(Messages.NewConfiguration_error_caseName, currentName);
			} else if (!validateName(currentName)) {
				s = Messages.NewConfiguration_error_invalidName;
			}
			if (statusLabel == null) return;
			Button b = getButton(IDialogConstants.OK_ID);
			if (s != null) {
				statusLabel.setText(s);
				statusLabel.setVisible(true);
				if (b != null) b.setEnabled(false);
			} else {
				statusLabel.setVisible(false);
				if (b != null) b.setEnabled(true);
			}
			if (b_importFromOtherProject.getSelection() && importSelector.getSelectionIndex() == 0)
				if (b != null) b.setEnabled(false);
			if (b_importPredefined.getSelection() && importDefSelector.getSelectionIndex() == 0)
				if (b != null) b.setEnabled(false);

			cloneConfigSelector.setEnabled(b_cloneFromProject.getSelection());
			realConfigSelector.setEnabled(b_cloneFromExtension.getSelection());
			importSelector.setEnabled(b_importFromOtherProject.getSelection());
			importDefSelector.setEnabled(b_importPredefined.getSelection());
		}
	}

	@Override
	public int open() {
		if (parentShell == null) return 1;
		LocalDialog dlg = new LocalDialog(parentShell);
		return dlg.open();
	}

	/**
	 */
	public NewCfgDialog() {
		newName = new String();
		newDescription = new String();
	}

	@Override
	public void setShell(Shell shell) {
		parentShell = shell;
	}

	@Override
	public void setProject(ICProjectDescription prj) {
		des = prj;
		ICConfigurationDescription[] descs = des.getConfigurations();
		cfgds = new IConfiguration[descs.length];
		ArrayList<IConfiguration> lst = new ArrayList<IConfiguration>();
		for (int i = 0; i < descs.length; ++i) {
			cfgds[i] = ManagedBuildManager.getConfigurationForDescription(descs[i]);
			IConfiguration cfg = cfgds[i];
			for(; cfg != null && !cfg.isExtensionElement(); cfg = cfg.getParent()) {}
			if (cfg != null) {
				IProjectType pType = cfg.getProjectType();
				if(pType != null){
					IConfiguration[] cfs = pType.getConfigurations();
					for (IConfiguration c : cfs) {
						if (c != null && !lst.contains(c))
							lst.add(c);
					}
				}
			}
		}
		rcfgs = lst.toArray(new IConfiguration[lst.size()]);
	}

	@Override
	public void setTitle(String _title) {
		title = _title;
	}


	private String [] getConfigNamesAndDescriptions(IConfiguration[] arr, boolean check) {
		String [] names = new String[arr.length];
		for (int i = 0; i < arr.length; ++i)
			names[i] = getNameAndDescription(arr[i]);

		if (check) {
			boolean doubles = false;
			for (int i=0; i<names.length; i++) {
				for (int j=0; j<names.length; j++) {
					if (i != j && names[i].equals(names[j])) {
						doubles = true;
						break;
					}
				}
			}
			if (doubles) {
				for (int i=0; i<names.length; i++) {
					IToolChain tc = arr[i].getToolChain();
					String s = (tc == null) ? NULL : tc.getName();
					names[i] = names[i] + " : " + s; //$NON-NLS-1$
				}
			}
		}
		return names;
	}

	private String getNameAndDescription(IConfiguration cfg) {
		String name = cfg.getName();
		if (name == null) name = NULL;
		if ( (cfg.getDescription() == null) || cfg.getDescription().equals(""))	//$NON-NLS-1$
			return name;
		else
			return name + "( " + cfg.getDescription() +" )";	//$NON-NLS-1$	//$NON-NLS-2$
	}

	protected boolean isDuplicateName(String newName) {
		for (int i = 0; i < cfgds.length; i++) {
			if (cfgds[i].getName().equals(newName))
				return true;
		}
		return false;
	}

	protected boolean isSimilarName(String newName) {
		for (int i = 0; i < cfgds.length; i++) {
			if (cfgds[i].getName().equalsIgnoreCase(newName))
				return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * Checks the argument for leading whitespaces and invalid directory name characters.
	 * @param name
	 * @return <I>true</i> is the name is a valid directory name with no whitespaces
	 */
	private boolean validateName(String name) {
		// Names must be at least one character in length
		if (name.trim().length() == 0)
			return false;

		// Iterate over the name checking for bad characters
		char[] chars = name.toCharArray();
		// No whitespaces at the start of a name
		if (Character.isWhitespace(chars[0])) {
			return false;
		}
		for (int index = 0; index < chars.length; ++index) {
			// Config name must be a valid dir name too, so we ban "\ / : * ? " < >" in the names
			if (!Character.isLetterOrDigit(chars[index])) {
				switch (chars[index]) {
				case '/':
				case '\\':
				case ':':
				case '*':
				case '?':
				case '\"':
				case '<':
				case '>':
					return false;
				default:
					break;
				}
			}
		}
		return true;
	}

	/**
	 * Create a new configuration, using the values currently set in
	 * the dialog.
	 */
	private void newConfiguration() {
		String id = ManagedBuildManager.calculateChildId(parentConfig.getId(), null);
		IManagedProject imp = ManagedBuildManager.getBuildInfo(des.getProject()).getManagedProject();
		if (imp == null || !(imp instanceof ManagedProject)) return;
		ManagedProject mp = (ManagedProject) imp;
		try {
			ICConfigurationDescription cfgDes = null;
			Configuration config = new Configuration(mp, (Configuration)parentConfig, id, false, true);
			if (b_cloneFromProject.getSelection()) {
				ICConfigurationDescription base = ManagedBuildManager.getDescriptionForConfiguration(parentConfig);
				cfgDes = des.createConfiguration(id, newName, base);
				cfgDes.setDescription(newDescription);
			} else if (b_importFromOtherProject.getSelection()) {
				IResource owner = parentConfig.getOwner();
				if (owner!=null) {
					// need writable cfg description for cloning
					ICProjectDescription prjDesOther = CCorePlugin.getDefault().getProjectDescription(owner.getProject(), true);
					ICConfigurationDescription base = prjDesOther.getConfigurationByName(parentConfig.getName());
					if (base != null) {
						cfgDes = des.createConfiguration(id, newName, base);
						cfgDes.setDescription(newDescription);
					}
				}
			}
			if (cfgDes == null) {
				// when "Default" or "Predefined" selected or import from other project failed
				CConfigurationData data = config.getConfigurationData();
				cfgDes = des.createConfiguration(ManagedBuildManager.CFG_DATA_PROVIDER_ID, data);
			}
			if (cfgDes != null) {
				config.setConfigurationDescription(cfgDes);
				config.setName(newName);
				config.setDescription(newDescription);

				String target = config.getArtifactName();
				if (target == null || target.length() == 0)
					config.setArtifactName(mp.getDefaultArtifactName());

				// Export artifact info as needed by project references
				config.exportArtifactInfo();
			}
			if (cfgDes == null) {
				throw new CoreException(new Status(IStatus.ERROR,
					"org.eclipse.cdt.managedbuilder.ui", -1, //$NON-NLS-1$
					Messages.NewCfgDialog_2, null));
			}
		} catch (CoreException e) {
			ManagedBuilderUIPlugin.log(e);
		}
	}

	private String[] getImportItems() {
		imported = new HashMap<String, IConfiguration>();
		if (des != null) {
			IProject[] ps = des.getProject().getWorkspace().getRoot().getProjects();
			for (IProject p : ps) {
				ICProjectDescription prjd = CoreModel.getDefault().getProjectDescription(p, false);
				if (prjd == null)
					continue;
				ICConfigurationDescription[] cfgs = prjd.getConfigurations();
				if (cfgs == null || cfgs.length == 0)
					continue;
				for (ICConfigurationDescription d : cfgs) {
					IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(d);
					if (cfg != null)
						imported.put(p.getName() + SEPARATOR + d.getName(), cfg);
				}
			}
		}
		ArrayList<String> lst = new ArrayList<String>(imported.keySet());
		Collections.sort(lst);
		lst.add(0, NOT);
		return lst.toArray(new String[lst.size()]);
	}

	private String[] getImportDefItems() {
		importedDef = new HashMap<String, IConfiguration>();
		IBuildPropertyManager bpm = ManagedBuildManager.getBuildPropertyManager();
		IBuildPropertyType bpt = bpm.getPropertyType(ART);
		for (IBuildPropertyValue v : bpt.getSupportedValues()) {
			String id = v.getId();
			IToolChain[] tcs = ManagedBuildManager.getExtensionsToolChains(ART, id, false);
			if (tcs == null || tcs.length == 0) continue;
			for (IToolChain tc : tcs) {
				if (tc.isSystemObject() || tc.isAbstract() || ! tc.isSupported())
					continue;
				// prefix: "X" shown if toolchain is not supported by platform.
				String pre = ManagedBuildManager.isPlatformOk(tc) ? "  " : "X "; //$NON-NLS-1$ //$NON-NLS-2$
				for (IConfiguration c : ManagedBuildManager.getExtensionConfigurations(tc, ART, id)) {
					if (c.isSystemObject() || ! c.isSupported())
						continue;
					importedDef.put(pre + v.getName() + SEPARATOR + tc.getName() + SEPARATOR + c.getName(), c);
				}
			}
		}
		ArrayList<String> lst = new ArrayList<String>(importedDef.keySet());
		Collections.sort(lst);
		lst.add(0, NOT);
		return lst.toArray(new String[lst.size()]);
	}

	private IConfiguration getConfigFromName(String s, Map<String, IConfiguration> imp) {
		if (imp == null)
			return null;
		return imp.get(s);
	}
}
