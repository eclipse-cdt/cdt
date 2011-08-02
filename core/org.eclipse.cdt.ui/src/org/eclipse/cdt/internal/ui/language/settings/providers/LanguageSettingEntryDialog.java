/*******************************************************************************
 * Copyright (c) 2010, 2010 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev (Quoin Inc.) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language.settings.providers;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.core.settings.model.CIncludeFileEntry;
import org.eclipse.cdt.core.settings.model.CIncludePathEntry;
import org.eclipse.cdt.core.settings.model.CLibraryFileEntry;
import org.eclipse.cdt.core.settings.model.CLibraryPathEntry;
import org.eclipse.cdt.core.settings.model.CMacroEntry;
import org.eclipse.cdt.core.settings.model.CMacroFileEntry;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.AbstractPropertyDialog;

import org.eclipse.cdt.internal.ui.ImageCombo;
import org.eclipse.cdt.internal.ui.newui.LanguageSettingsImages;
import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class LanguageSettingEntryDialog extends AbstractPropertyDialog {
	private static final String SLASH = "/"; //$NON-NLS-1$

	private ICConfigurationDescription cfgDescription;
	private IProject project;
	private ICLanguageSettingEntry entry;
	private boolean clearValue;
	private int kind;

	private Label iconComboKind;
	private ImageCombo comboKind;
	private ImageCombo comboPathCategory;
	private Label labelInput;
	public Text inputName;
	private Label checkBoxValue;
	public Text inputValue;
	private Button buttonBrowse;
	private Button buttonVars;
	private Button checkBoxBuiltIn;
	private Button checkBoxFramework;

	private Button checkBoxAllCfgs;
	private Button checkBoxAllLangs;

	private Button buttonOk;
	private Button buttonCancel;


	private static final int COMBO_INDEX_INCLUDE_PATH = 0;
	private static final int COMBO_INDEX_MACRO = 1;
	private static final int COMBO_INDEX_INCLUDE_FILE = 2;
	private static final int COMBO_INDEX_MACRO_FILE = 3;
	private static final int COMBO_INDEX_LIBRARY_PATH = 4;
	private static final int COMBO_INDEX_LIBRARY_FILE = 5;

	final private String [] comboKindItems = {
			"Include Directory",
			"Preprocessor Macro",
			"Include File",
			"Preprocessor Macros File",
			"Library Path",
			"Library",
	};
	final private Image[] comboKindImages = {
			CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER),
			CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_MACRO),
			CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_TUNIT_HEADER),
			CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_MACROS_FILE),
			CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_LIBRARY_FOLDER),
			CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_LIBRARY),
	};

	private static final int COMBO_PATH_INDEX_PROJECT = 0;
	private static final int COMBO_PATH_INDEX_WORKSPACE = 1;
	private static final int COMBO_PATH_INDEX_FILESYSTEM = 2;

	final private String [] pathCategories = {
			"Project-Relative",
			"Workspace Path",
			"Filesystem",
	};
	final private Image[] pathCategoryImages = {
			CDTSharedImages.getImage(CDTSharedImages.IMG_ETOOL_PROJECT),
			CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_WORKSPACE),
			CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_FILESYSTEM),
	};



	private ICLanguageSettingEntry[] entries;
	private Composite comp1;

	public LanguageSettingEntryDialog(Shell parent, ICConfigurationDescription cfgDescription, int kind) {
		super(parent, "");
		this.cfgDescription = cfgDescription;
		this.project = cfgDescription.getProjectDescription().getProject();
		this.entry = null;
		this.clearValue = true;
		this.kind = kind;
	}

	/**
	 * This constructor is intended to be used with {@code clearValue=true} for "Add" dialogs
	 * where provided entry is used as a template.
	 */
	public LanguageSettingEntryDialog(Shell parent, ICConfigurationDescription cfgDescription, ICLanguageSettingEntry entry, boolean clearValue) {
		super(parent, "");
		this.cfgDescription = cfgDescription;
		this.project = cfgDescription.getProjectDescription().getProject();
		this.entry = entry;
		this.kind = entry!=null ? entry.getKind() : ICSettingEntry.INCLUDE_PATH;
		this.clearValue = clearValue;
	}

	/**
	 * This constructor is used for "Edit" dialogs to edit provided entry
	 */
	public LanguageSettingEntryDialog(Shell parent, ICConfigurationDescription cfgDescription, ICLanguageSettingEntry entry) {
		this(parent, cfgDescription, entry, false);
	}

	private int comboIndexToKind(int index) {
		int kind=0;
		switch (index) {
		case COMBO_INDEX_INCLUDE_PATH:
			kind = ICSettingEntry.INCLUDE_PATH;
			break;
		case COMBO_INDEX_MACRO:
			kind = ICSettingEntry.MACRO;
			break;
		case COMBO_INDEX_INCLUDE_FILE:
			kind = ICSettingEntry.INCLUDE_FILE;
			break;
		case COMBO_INDEX_MACRO_FILE:
			kind = ICSettingEntry.MACRO_FILE;
			break;
		case COMBO_INDEX_LIBRARY_PATH:
			kind = ICSettingEntry.LIBRARY_PATH;
			break;
		case COMBO_INDEX_LIBRARY_FILE:
			kind = ICSettingEntry.LIBRARY_FILE;
			break;
		}
		return kind;
	}

	private int kindToComboIndex(int kind) {
		int index=0;
		switch (kind) {
		case ICSettingEntry.INCLUDE_PATH:
			index = COMBO_INDEX_INCLUDE_PATH;
			break;
		case ICSettingEntry.MACRO:
			index = COMBO_INDEX_MACRO;
			break;
		case ICSettingEntry.INCLUDE_FILE:
			index = COMBO_INDEX_INCLUDE_FILE;
			break;
		case ICSettingEntry.MACRO_FILE:
			index = COMBO_INDEX_MACRO_FILE;
			break;
		case ICSettingEntry.LIBRARY_PATH:
			index = COMBO_INDEX_LIBRARY_PATH;
			break;
		case ICSettingEntry.LIBRARY_FILE:
			index = COMBO_INDEX_LIBRARY_FILE;
			break;
		}
		return index;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		parent.setLayout(new GridLayout(4, false));
		GridData gd;

		// Composite comp1
		comp1 = new Composite (parent, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = SWT.TOP;
		gd.horizontalSpan = 7;
		comp1.setLayoutData(gd);
		comp1.setLayout(new GridLayout(7, false));

		// Icon for kind
		iconComboKind = new Label (comp1, SWT.NONE);
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		gd.horizontalAlignment = SWT.RIGHT;
		iconComboKind.setLayoutData(gd);
		iconComboKind.setText("Select Kind:");
		int kindToComboIndex = kindToComboIndex(kind);
		iconComboKind.setImage(comboKindImages[kindToComboIndex]);

		// Combo for the setting entry kind
		comboKind = new ImageCombo(comp1, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		for (int i = 0; i < comboKindItems.length; i++) {
			comboKind.add(comboKindItems[i], comboKindImages[i]);
		}
		comboKind.setText(comboKindItems[kindToComboIndex]);

		comboKind.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateImages();
				setButtons();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);

			}
		});
		comboKind.setEnabled(clearValue);


		//
		// Icon for path category
		final Label comboPathCategoryIcon = new Label (comp1, SWT.NONE);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
		gd.verticalAlignment = SWT.TOP;
		gd.widthHint = 15;
		comboPathCategoryIcon.setLayoutData(gd);
		comboPathCategoryIcon.setText("");

		// Combo for path category
		comboPathCategory = new ImageCombo(comp1, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		for (int i = 0; i < pathCategories.length; i++) {
			comboPathCategory.add(pathCategories[i], pathCategoryImages[i]);
		}
		int pcindex = COMBO_PATH_INDEX_PROJECT;
		if (entry!=null) {
			if ( (entry.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) == 0) {
				pcindex = COMBO_PATH_INDEX_FILESYSTEM;
			} else {
				if (entry.getName().startsWith(SLASH)) {
					pcindex = COMBO_PATH_INDEX_WORKSPACE;
				} else {
					pcindex = COMBO_PATH_INDEX_PROJECT;
				}
			}

		}
		comboPathCategory.setText(pathCategories[pcindex]);
		gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		gd.verticalAlignment = SWT.TOP;
		gd.horizontalSpan = 4;
		comboPathCategory.setLayoutData(gd);

		comboPathCategory.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				updateImages();
				setButtons();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);

			}
		});

		// Dir/File/Name label
		labelInput = new Label(comp1, SWT.NONE);
		labelInput.setText("Dir:");
		gd = new GridData();
		labelInput.setLayoutData(gd);

		// Dir/File/Name input
		inputName = new Text(comp1, SWT.SINGLE | SWT.BORDER);
		if (entry!=null && !clearValue) {
			inputName.setText(entry.getName());
		}
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.widthHint = 200;
		inputName.setLayoutData(gd);
		inputName.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				setButtons();
			}});

		inputName.setFocus();
		inputName.setSelection(0, inputName.getText().length());

		// Value label
		checkBoxValue = new Label(comp1, SWT.NONE);
		checkBoxValue.setText("Value:");
		gd = new GridData();
		checkBoxValue.setLayoutData(gd);

		// Path button
		buttonBrowse = new Button(comp1, SWT.PUSH);
		buttonBrowse.setText("...");
		buttonBrowse.setImage(pathCategoryImages[0]);
		buttonBrowse.setLayoutData(new GridData());
		buttonBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(event);
			}
		});

		// Variables button
		buttonVars = new Button(comp1, SWT.PUSH);
		buttonVars.setText(AbstractCPropertyTab.VARIABLESBUTTON_NAME);
		buttonVars.setLayoutData(new GridData());
		buttonVars.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(event);
			}
		});

		// Value input. Located after the other controls to get sufficient width
		int comboPathWidth = comboPathCategory.computeSize(SWT.DEFAULT, SWT.NONE).x;
		inputValue = new Text(comp1, SWT.SINGLE | SWT.BORDER);
		if (entry!=null && !clearValue) {
			inputValue.setText(entry.getValue());
		}
		gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		gd.widthHint = comboPathWidth;
		inputValue.setLayoutData(gd);

		if (entry!=null && kind==ICSettingEntry.MACRO && !clearValue) {
			inputValue.setFocus();
			inputValue.setSelection(0, inputValue.getText().length());
		}

		// Checkboxes
		Composite compCheckboxes = new Composite (parent, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = SWT.TOP;
		gd.horizontalSpan = 4;
		compCheckboxes.setLayoutData(gd);
		compCheckboxes.setLayout(new GridLayout(1, false));

		// Checkbox "Built-In"
		checkBoxBuiltIn = new Button(compCheckboxes, SWT.CHECK);
		checkBoxBuiltIn.setText("Treat as Built-In (Ignore during build)");
		checkBoxBuiltIn.setSelection(entry!=null && (entry.getFlags()&ICSettingEntry.BUILTIN)!=0);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		checkBoxBuiltIn.setLayoutData(gd);
		checkBoxBuiltIn.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				updateImages();
				setButtons();
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		// Checkbox "Framework"
		checkBoxFramework = new Button(compCheckboxes, SWT.CHECK);
		checkBoxFramework.setText("Framework folder (Mac only)");
		checkBoxFramework.setSelection(entry!=null && (entry.getFlags()&ICSettingEntry.FRAMEWORKS_MAC)!=0);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		checkBoxFramework.setLayoutData(gd);
		checkBoxFramework.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				updateImages();
				setButtons();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		// Separator
		@SuppressWarnings("unused")
		Label separator = new Label(compCheckboxes, SWT.SEPARATOR | SWT.HORIZONTAL | SWT.SHADOW_NONE);

		// Checkbox "All configurations"
		checkBoxAllCfgs = new Button(compCheckboxes, SWT.CHECK);
		checkBoxAllCfgs.setText(Messages.IncludeDialog_2);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		checkBoxAllCfgs.setLayoutData(gd);
		checkBoxAllCfgs.setEnabled(false);
		checkBoxAllCfgs.setToolTipText("Not implemented yet");

		// Checkbox "All languages"
		checkBoxAllLangs = new Button(compCheckboxes, SWT.CHECK);
		checkBoxAllLangs.setText(Messages.IncludeDialog_3);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		checkBoxAllLangs.setLayoutData(gd);
		checkBoxAllLangs.setEnabled(false);
		checkBoxAllLangs.setToolTipText("Not implemented yet");

		// Buttons
		Composite compButtons = new Composite (parent, SWT.FILL);
		gd = new GridData(SWT.RIGHT, SWT.BOTTOM, false, false);
		gd.horizontalSpan = 4;
		gd.grabExcessVerticalSpace = true;
		compButtons.setLayoutData(gd);
		compButtons.setLayout(new GridLayout(4, false));

		 // placeholder
		Label placeholder = new Label(compButtons, 0);
		placeholder.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));

		// Button OK
		buttonOk = new Button(compButtons, SWT.PUSH);
		buttonOk.setText(IDialogConstants.OK_LABEL);
		gd = new GridData();
		gd.widthHint = buttonVars.computeSize(SWT.DEFAULT,SWT.NONE).x;
		buttonOk.setLayoutData(gd);
		buttonOk.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(event);
			}
		});

		// Button Cancel
		buttonCancel = new Button(compButtons, SWT.PUSH);
		buttonCancel.setText(IDialogConstants.CANCEL_LABEL);
		gd = new GridData();
		gd.widthHint = buttonVars.computeSize(SWT.DEFAULT, SWT.NONE).x;
		buttonCancel.setLayoutData(gd);
		buttonCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(event);
			}
		});

		parent.getShell().setDefaultButton(buttonOk);
		parent.pack();

		updateImages();
		setButtons();
		return parent;
	}

	private void setButtons() {
		int kindSelectionIndex = comboKind.getSelectionIndex();
		boolean isMacroSelected = kindSelectionIndex==COMBO_INDEX_MACRO;
		comboPathCategory.setVisible(!isMacroSelected);
		buttonBrowse.setVisible(!isMacroSelected);
		buttonVars.setVisible(!isMacroSelected);
		checkBoxValue.setVisible(isMacroSelected);
		inputValue.setVisible(isMacroSelected);

		((GridData)checkBoxValue.getLayoutData()).exclude = !isMacroSelected;
		((GridData)inputValue.getLayoutData()).exclude = !isMacroSelected;

		((GridData)buttonBrowse.getLayoutData()).exclude = isMacroSelected;
		((GridData)buttonVars.getLayoutData()).exclude = isMacroSelected;

		switch (kindSelectionIndex) {
		case COMBO_INDEX_INCLUDE_PATH:
		case COMBO_INDEX_LIBRARY_PATH:
			labelInput.setText("Path:");
			break;
		case COMBO_INDEX_INCLUDE_FILE:
		case COMBO_INDEX_MACRO_FILE:
		case COMBO_INDEX_LIBRARY_FILE:
			labelInput.setText("File:");
			break;
		case COMBO_INDEX_MACRO:
		default:
			labelInput.setText("Name:");
		}

		inputValue.setEnabled(isMacroSelected);

		int indexPathKind = comboPathCategory.getSelectionIndex();
		boolean isProjectSelected = indexPathKind==COMBO_PATH_INDEX_PROJECT;
		boolean isWorkspaceSelected = indexPathKind==COMBO_PATH_INDEX_WORKSPACE;
		boolean isFilesystemSelected = indexPathKind==COMBO_PATH_INDEX_FILESYSTEM;

		String path = inputName.getText();
		if (path.trim().length()==0) {
			buttonOk.setEnabled(false);
		} else {
			buttonOk.setEnabled((isProjectSelected && !path.startsWith(SLASH)) ||
					(isWorkspaceSelected && path.startsWith(SLASH)) || isFilesystemSelected);
		}

		buttonVars.setEnabled(isFilesystemSelected);

		comp1.layout(true);
	}

	@Override
	public void buttonPressed(SelectionEvent e) {
		String s=null;
		if (e.widget.equals(buttonOk)) {
			String name = inputName.getText();
			text1 = name;
			String value = inputValue.getText();
			check1 = checkBoxAllCfgs.getSelection();
			check3 = checkBoxAllLangs.getSelection();
			result = true;

			int flagBuiltIn = checkBoxBuiltIn.getSelection() ? ICSettingEntry.BUILTIN : 0;
			int flagFramework = checkBoxFramework.getSelection() ? ICSettingEntry.FRAMEWORKS_MAC : 0;
			int indexPathKind = comboPathCategory.getSelectionIndex();
			int kind = comboKind.getSelectionIndex();
			boolean isProjectPath = indexPathKind==COMBO_PATH_INDEX_PROJECT;
			boolean isWorkspacePath = (kind!=COMBO_INDEX_MACRO) && (isProjectPath || indexPathKind==COMBO_PATH_INDEX_WORKSPACE);
			int flagWorkspace = isWorkspacePath ? ICSettingEntry.VALUE_WORKSPACE_PATH : 0;
			int flags = flagBuiltIn | flagWorkspace | flagFramework;

			ICLanguageSettingEntry entry=null;
			switch (comboKind.getSelectionIndex()) {
			case COMBO_INDEX_INCLUDE_PATH:
				entry = new CIncludePathEntry(name, flags);
				break;
			case COMBO_INDEX_MACRO:
				// note that value=null is not supported by CMacroEntry
				entry = new CMacroEntry(name, value, flags);
				break;
			case COMBO_INDEX_INCLUDE_FILE:
				entry = new CIncludeFileEntry(name, flags);
				break;
			case COMBO_INDEX_MACRO_FILE:
				entry = new CMacroFileEntry(name, flags);
				break;
			case COMBO_INDEX_LIBRARY_PATH:
				entry = new CLibraryPathEntry(name, flags);
				break;
			case COMBO_INDEX_LIBRARY_FILE:
				entry = new CLibraryFileEntry(name, flags);
				break;
			default:
				result = false;
			}

			entries = new ICLanguageSettingEntry[] {entry};
			shell.dispose();
		} else if (e.widget.equals(buttonCancel)) {
			shell.dispose();
		} else if (e.widget.equals(buttonBrowse)) {
			boolean isDirectory = false;
			boolean isFile = false;
			switch (comboKind.getSelectionIndex()) {
			case COMBO_INDEX_INCLUDE_PATH:
			case COMBO_INDEX_LIBRARY_PATH:
				isDirectory = true;
				break;
			case COMBO_INDEX_INCLUDE_FILE:
			case COMBO_INDEX_MACRO_FILE:
			case COMBO_INDEX_LIBRARY_FILE:
				isFile = true;
				break;
			case COMBO_INDEX_MACRO:
				break;
			}

			if (isDirectory) {
				switch (comboPathCategory.getSelectionIndex()) {
				case COMBO_PATH_INDEX_WORKSPACE:
					s = AbstractCPropertyTab.getWorkspaceDirDialog(shell, inputName.getText());
					break;
				case COMBO_PATH_INDEX_PROJECT:
					s = AbstractCPropertyTab.getProjectDirDialog(shell, inputName.getText(), project);
					break;
				case COMBO_PATH_INDEX_FILESYSTEM:
					s = AbstractCPropertyTab.getFileSystemDirDialog(shell, inputName.getText());
					break;
				}
			} else if (isFile) {
				switch (comboPathCategory.getSelectionIndex()) {
				case COMBO_PATH_INDEX_WORKSPACE:
					s = AbstractCPropertyTab.getWorkspaceFileDialog(shell, inputName.getText());
					break;
				case COMBO_PATH_INDEX_PROJECT:
					s = AbstractCPropertyTab.getProjectFileDialog(shell, inputName.getText(), project);
					break;
				case COMBO_PATH_INDEX_FILESYSTEM:
					s = AbstractCPropertyTab.getFileSystemFileDialog(shell, inputName.getText());
					break;
				}
			}

			if (s != null) {
				s = strip_wsp(s);
				if (comboPathCategory.getSelectionIndex()==COMBO_PATH_INDEX_PROJECT && s.startsWith(SLASH+project.getName()+SLASH)) {
					s=s.substring(project.getName().length()+2);
				}
				inputName.setText(s);
			}
		} else if (e.widget.equals(buttonVars)) {
			s = AbstractCPropertyTab.getVariableDialog(shell, cfgDescription);
			if (s != null) inputName.insert(s);
		}
	}

	public ICLanguageSettingEntry[] getEntries() {
		return entries;
	}

	private void updateImages() {
		int indexEntryKind = comboKind.getSelectionIndex();
		int indexPathKind = comboPathCategory.getSelectionIndex();
		shell.setText("Add " + comboKindItems[indexEntryKind]);

		int kind = comboIndexToKind(indexEntryKind);
		int flagBuiltin = checkBoxBuiltIn.getSelection() ? ICSettingEntry.BUILTIN : 0;
		int flagFramework = checkBoxFramework.getSelection() ? ICSettingEntry.FRAMEWORKS_MAC : 0;
		boolean isWorkspacePath = indexPathKind==COMBO_PATH_INDEX_PROJECT || indexPathKind==COMBO_PATH_INDEX_WORKSPACE;
		int flagWorkspace = isWorkspacePath ? ICSettingEntry.VALUE_WORKSPACE_PATH : 0;
		int flags = flagBuiltin | flagWorkspace | flagFramework;
		Image image = LanguageSettingsImages.getImage(kind, flags, indexPathKind==COMBO_PATH_INDEX_PROJECT);

		iconComboKind.setImage(image);
		shell.setImage(image);

		buttonBrowse.setImage(pathCategoryImages[indexPathKind]);
	}

}
