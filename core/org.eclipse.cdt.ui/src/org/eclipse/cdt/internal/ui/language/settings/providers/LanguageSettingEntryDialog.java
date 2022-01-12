/*******************************************************************************
 * Copyright (c) 2010, 2013 Andrew Gvozdev and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.language.settings.providers;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICLanguageSettingEntry;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.core.settings.model.util.CDataUtil;
import org.eclipse.cdt.internal.ui.ImageCombo;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.newui.AbstractCPropertyTab;
import org.eclipse.cdt.ui.newui.AbstractPropertyDialog;
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

/**
 * @noextend This class is not intended to be subclassed by clients.
 */
public class LanguageSettingEntryDialog extends AbstractPropertyDialog {
	private static final String SLASH = "/"; //$NON-NLS-1$

	private final ICConfigurationDescription cfgDescription;
	private final IProject project;
	private final ICLanguageSettingEntry initialEntry;
	private final int initialKind;
	private final boolean clearValue;

	private Composite compositeArea;
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
	private Button checkBoxSystem;
	private Button checkBoxFramework;

	private Button buttonOk;
	private Button buttonCancel;

	private static final int COMBO_INDEX_INCLUDE_DIR = 0;
	private static final int COMBO_INDEX_MACRO = 1;
	private static final int COMBO_INDEX_INCLUDE_FILE = 2;
	private static final int COMBO_INDEX_MACRO_FILE = 3;
	private static final int COMBO_INDEX_LIBRARY_DIR = 4;
	private static final int COMBO_INDEX_LIBRARY_FILE = 5;

	final private String[] comboKindItems = { Messages.LanguageSettingEntryDialog_IncludeDirectory,
			Messages.LanguageSettingEntryDialog_PreporocessorMacro, Messages.LanguageSettingEntryDialog_IncludeFile,
			Messages.LanguageSettingEntryDialog_PreprocessorMacroFile, Messages.LanguageSettingEntryDialog_LibraryPath,
			Messages.LanguageSettingEntryDialog_Library, };
	final private Image[] comboKindImages = { CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_INCLUDES_FOLDER),
			CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_MACRO),
			CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_TUNIT_HEADER),
			CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_MACROS_FILE),
			CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_LIBRARY_FOLDER),
			CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_LIBRARY), };

	private static final int COMBO_PATH_INDEX_PROJECT = 0;
	private static final int COMBO_PATH_INDEX_WORKSPACE = 1;
	private static final int COMBO_PATH_INDEX_FILESYSTEM = 2;

	final private String[] pathCategories = { Messages.LanguageSettingEntryDialog_ProjectPath,
			Messages.LanguageSettingEntryDialog_WorkspacePath, Messages.LanguageSettingEntryDialog_Filesystem, };
	final private Image[] pathCategoryImages = { CDTSharedImages.getImage(CDTSharedImages.IMG_ETOOL_PROJECT),
			CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_WORKSPACE),
			CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_FILESYSTEM), };

	private ICLanguageSettingEntry[] entries;

	public LanguageSettingEntryDialog(Shell parent, ICConfigurationDescription cfgDescription, int kind) {
		super(parent, ""); //$NON-NLS-1$
		this.cfgDescription = cfgDescription;
		this.project = cfgDescription.getProjectDescription().getProject();
		this.initialEntry = null;
		this.initialKind = kind;
		this.clearValue = true;
	}

	/**
	 * This constructor is intended to be used with {@code clearValue=true} for "Add" dialogs
	 * where provided entry is used as a template.
	 */
	public LanguageSettingEntryDialog(Shell parent, ICConfigurationDescription cfgDescription,
			ICLanguageSettingEntry entry, boolean clearValue) {
		super(parent, ""); //$NON-NLS-1$
		this.cfgDescription = cfgDescription;
		this.project = cfgDescription.getProjectDescription().getProject();
		this.initialEntry = entry;
		this.initialKind = entry != null ? entry.getKind() : ICSettingEntry.INCLUDE_PATH;
		this.clearValue = clearValue;
	}

	/**
	 * This constructor is used for "Edit" dialogs to edit provided entry
	 */
	public LanguageSettingEntryDialog(Shell parent, ICConfigurationDescription cfgDescription,
			ICLanguageSettingEntry entry) {
		this(parent, cfgDescription, entry, false);
	}

	private int comboIndexToKind(int index) {
		int kind = 0;
		switch (index) {
		case COMBO_INDEX_INCLUDE_DIR:
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
		case COMBO_INDEX_LIBRARY_DIR:
			kind = ICSettingEntry.LIBRARY_PATH;
			break;
		case COMBO_INDEX_LIBRARY_FILE:
			kind = ICSettingEntry.LIBRARY_FILE;
			break;
		}
		return kind;
	}

	private int kindToComboIndex(int kind) {
		int index = 0;
		switch (kind) {
		case ICSettingEntry.INCLUDE_PATH:
			index = COMBO_INDEX_INCLUDE_DIR;
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
			index = COMBO_INDEX_LIBRARY_DIR;
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

		// Composite for the dialog area
		compositeArea = new Composite(parent, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = SWT.TOP;
		gd.horizontalSpan = 7;
		compositeArea.setLayoutData(gd);
		compositeArea.setLayout(new GridLayout(7, false));

		// Icon for kind
		iconComboKind = new Label(compositeArea, SWT.NONE);
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		gd.horizontalAlignment = SWT.RIGHT;
		iconComboKind.setLayoutData(gd);
		iconComboKind.setText(Messages.LanguageSettingEntryDialog_SelectKind);
		int kindToComboIndex = kindToComboIndex(initialKind);
		iconComboKind.setImage(comboKindImages[kindToComboIndex]);

		// Combo for the setting entry kind
		comboKind = new ImageCombo(compositeArea, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		for (int i = 0; i < comboKindItems.length; i++) {
			comboKind.add(comboKindItems[i], comboKindImages[i]);
		}
		comboKind.setText(comboKindItems[kindToComboIndex]);

		comboKind.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateImages();
				setButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		comboKind.setEnabled(clearValue);

		// Icon for path category
		final Label comboPathCategoryIcon = new Label(compositeArea, SWT.NONE);
		gd = new GridData(GridData.GRAB_HORIZONTAL | GridData.HORIZONTAL_ALIGN_END);
		gd.verticalAlignment = SWT.TOP;
		gd.widthHint = 15;
		comboPathCategoryIcon.setLayoutData(gd);
		comboPathCategoryIcon.setText(""); //$NON-NLS-1$

		// Combo for path category
		comboPathCategory = new ImageCombo(compositeArea, SWT.DROP_DOWN | SWT.READ_ONLY | SWT.BORDER);
		for (int i = 0; i < pathCategories.length; i++) {
			comboPathCategory.add(pathCategories[i], pathCategoryImages[i]);
		}
		int pcindex = COMBO_PATH_INDEX_PROJECT;
		if (initialEntry != null) {
			if ((initialEntry.getFlags() & ICSettingEntry.VALUE_WORKSPACE_PATH) == 0) {
				pcindex = COMBO_PATH_INDEX_FILESYSTEM;
			} else {
				if (LanguageSettingsImages.isProjectRelative(initialEntry)) {
					pcindex = COMBO_PATH_INDEX_PROJECT;
				} else {
					pcindex = COMBO_PATH_INDEX_WORKSPACE;
				}
			}
		}
		comboPathCategory.setText(pathCategories[pcindex]);
		gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		gd.verticalAlignment = SWT.TOP;
		gd.horizontalSpan = 4;
		comboPathCategory.setLayoutData(gd);

		comboPathCategory.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateImages();
				setButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);

			}
		});

		// Dir/File/Name label
		labelInput = new Label(compositeArea, SWT.NONE);
		labelInput.setText(Messages.LanguageSettingEntryDialog_Directory);
		gd = new GridData();
		labelInput.setLayoutData(gd);

		// Dir/File/Name input
		inputName = new Text(compositeArea, SWT.SINGLE | SWT.BORDER);
		if (initialEntry != null && !clearValue) {
			String name = initialEntry.getName();
			if (pcindex == COMBO_PATH_INDEX_PROJECT && LanguageSettingsImages.isProjectRelative(initialEntry)) {
				name = LanguageSettingsImages.toProjectRelative(name);
			}
			inputName.setText(name);
		}
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.widthHint = 200;
		inputName.setLayoutData(gd);
		inputName.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setButtons();
			}
		});

		inputName.setFocus();
		inputName.setSelection(0, inputName.getText().length());

		// Value label
		checkBoxValue = new Label(compositeArea, SWT.NONE);
		checkBoxValue.setText(Messages.LanguageSettingEntryDialog_Value);
		gd = new GridData();
		checkBoxValue.setLayoutData(gd);

		// Path button
		buttonBrowse = new Button(compositeArea, SWT.PUSH);
		buttonBrowse.setText("..."); //$NON-NLS-1$
		buttonBrowse.setImage(pathCategoryImages[0]);
		buttonBrowse.setLayoutData(new GridData());
		buttonBrowse.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				buttonPressed(event);
			}
		});

		// Variables button
		buttonVars = new Button(compositeArea, SWT.PUSH);
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
		inputValue = new Text(compositeArea, SWT.SINGLE | SWT.BORDER);
		if (initialEntry != null && !clearValue) {
			inputValue.setText(initialEntry.getValue());
		}
		gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		gd.widthHint = comboPathWidth;
		inputValue.setLayoutData(gd);

		if (initialEntry != null && initialKind == ICSettingEntry.MACRO && !clearValue) {
			inputValue.setFocus();
			inputValue.setSelection(0, inputValue.getText().length());
		}

		// Checkboxes
		Composite compCheckboxes = new Composite(parent, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = SWT.TOP;
		gd.horizontalSpan = 4;
		compCheckboxes.setLayoutData(gd);
		compCheckboxes.setLayout(new GridLayout(1, false));

		// Checkbox "Built-In"
		checkBoxBuiltIn = new Button(compCheckboxes, SWT.CHECK);
		checkBoxBuiltIn.setText(Messages.LanguageSettingEntryDialog_BuiltInFlag);
		checkBoxBuiltIn.setSelection(initialEntry != null && (initialEntry.getFlags() & ICSettingEntry.BUILTIN) != 0);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		checkBoxBuiltIn.setLayoutData(gd);
		checkBoxBuiltIn.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateImages();
				setButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		// Checkbox "Contains system includes"
		checkBoxSystem = new Button(compCheckboxes, SWT.CHECK);
		checkBoxSystem.setText(Messages.LanguageSettingEntryDialog_ContainsSystemHeaders);
		checkBoxSystem.setSelection(initialEntry != null && (initialEntry.getFlags() & ICSettingEntry.LOCAL) == 0);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		checkBoxSystem.setLayoutData(gd);
		checkBoxSystem.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateImages();
				setButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		// Checkbox "Framework folder"
		checkBoxFramework = new Button(compCheckboxes, SWT.CHECK);
		checkBoxFramework.setText(Messages.LanguageSettingEntryDialog_FrameworkFolder);
		checkBoxFramework
				.setSelection(initialEntry != null && (initialEntry.getFlags() & ICSettingEntry.FRAMEWORKS_MAC) != 0);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		checkBoxFramework.setLayoutData(gd);
		checkBoxFramework.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateImages();
				setButtons();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		// Buttons
		Composite compButtons = new Composite(parent, SWT.FILL);
		gd = new GridData(SWT.RIGHT, SWT.BOTTOM, false, false);
		gd.horizontalSpan = 4;
		gd.grabExcessVerticalSpace = true;
		compButtons.setLayoutData(gd);
		compButtons.setLayout(new GridLayout(4, false));

		// Placeholder
		Label placeholder = new Label(compButtons, 0);
		placeholder.setLayoutData(new GridData(GridData.GRAB_HORIZONTAL));

		// Button OK
		buttonOk = new Button(compButtons, SWT.PUSH);
		buttonOk.setText(IDialogConstants.OK_LABEL);
		gd = new GridData();
		gd.widthHint = buttonVars.computeSize(SWT.DEFAULT, SWT.NONE).x;
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
		boolean isIncludeDirSelected = (kindSelectionIndex == COMBO_INDEX_INCLUDE_DIR);
		checkBoxSystem.setVisible(isIncludeDirSelected);
		checkBoxFramework.setVisible(isIncludeDirSelected);
		boolean isMacroSelected = (kindSelectionIndex == COMBO_INDEX_MACRO);
		comboPathCategory.setVisible(!isMacroSelected);
		buttonBrowse.setVisible(!isMacroSelected);
		buttonVars.setVisible(!isMacroSelected);
		checkBoxValue.setVisible(isMacroSelected);
		inputValue.setVisible(isMacroSelected);

		((GridData) checkBoxValue.getLayoutData()).exclude = !isMacroSelected;
		((GridData) inputValue.getLayoutData()).exclude = !isMacroSelected;

		((GridData) buttonBrowse.getLayoutData()).exclude = isMacroSelected;
		((GridData) buttonVars.getLayoutData()).exclude = isMacroSelected;

		switch (kindSelectionIndex) {
		case COMBO_INDEX_INCLUDE_DIR:
		case COMBO_INDEX_LIBRARY_DIR:
			labelInput.setText(Messages.LanguageSettingEntryDialog_Path);
			break;
		case COMBO_INDEX_INCLUDE_FILE:
		case COMBO_INDEX_MACRO_FILE:
		case COMBO_INDEX_LIBRARY_FILE:
			labelInput.setText(Messages.LanguageSettingEntryDialog_File);
			break;
		case COMBO_INDEX_MACRO:
		default:
			labelInput.setText(Messages.LanguageSettingEntryDialog_Name);
		}

		inputValue.setEnabled(isMacroSelected);

		if (isMacroSelected) {
			buttonOk.setEnabled(!inputName.getText().trim().isEmpty());
		} else {
			int indexPathKind = comboPathCategory.getSelectionIndex();
			boolean isProjectSelected = (indexPathKind == COMBO_PATH_INDEX_PROJECT);
			boolean isWorkspaceSelected = (indexPathKind == COMBO_PATH_INDEX_WORKSPACE);
			boolean isFilesystemSelected = (indexPathKind == COMBO_PATH_INDEX_FILESYSTEM);

			String path = inputName.getText().trim();
			if (path.isEmpty()) {
				buttonOk.setEnabled(false);
			} else {
				buttonOk.setEnabled((isProjectSelected && !path.startsWith(SLASH))
						|| (isWorkspaceSelected && path.startsWith(SLASH)) || isFilesystemSelected);
			}

			buttonVars.setEnabled(isFilesystemSelected);
		}

		compositeArea.layout(true);
	}

	@Override
	public void buttonPressed(SelectionEvent e) {
		String str = null;
		if (e.widget.equals(buttonOk)) {
			text1 = inputName.getText().trim();
			result = true;

			String name = text1;
			int flagBuiltIn = checkBoxBuiltIn.isVisible() && checkBoxBuiltIn.getSelection() ? ICSettingEntry.BUILTIN
					: 0;
			int flags = flagBuiltIn;

			int kind = comboKind.getSelectionIndex();
			if (kind != COMBO_INDEX_MACRO) {
				int flagSystem = checkBoxSystem.isVisible() && checkBoxSystem.getSelection() ? 0 : ICSettingEntry.LOCAL;
				int flagFramework = checkBoxFramework.isVisible() && checkBoxFramework.getSelection()
						? ICSettingEntry.FRAMEWORKS_MAC
						: 0;

				int indexPathKind = comboPathCategory.getSelectionIndex();
				boolean isProjectPath = indexPathKind == COMBO_PATH_INDEX_PROJECT;
				boolean isWorkspacePath = indexPathKind == COMBO_PATH_INDEX_WORKSPACE;
				int flagWorkspace = (isWorkspacePath || isProjectPath) ? ICSettingEntry.VALUE_WORKSPACE_PATH : 0;
				int flagResolved = isWorkspacePath && !name.contains("$") ? ICSettingEntry.RESOLVED : 0; //$NON-NLS-1$
				flags = flagBuiltIn | flagWorkspace | flagResolved | flagSystem | flagFramework;

				if (isProjectPath) {
					name = LanguageSettingsImages.fromProjectRelative(name);
				}
			}

			ICLanguageSettingEntry entry = null;
			switch (comboKind.getSelectionIndex()) {
			case COMBO_INDEX_INCLUDE_DIR:
				entry = CDataUtil.createCIncludePathEntry(name, flags);
				break;
			case COMBO_INDEX_MACRO:
				// Note that value=null is not supported by CMacroEntry
				String value = inputValue.getText().trim();
				entry = CDataUtil.createCMacroEntry(name, value, flags);
				break;
			case COMBO_INDEX_INCLUDE_FILE:
				entry = CDataUtil.createCIncludeFileEntry(name, flags);
				break;
			case COMBO_INDEX_MACRO_FILE:
				entry = CDataUtil.createCMacroFileEntry(name, flags);
				break;
			case COMBO_INDEX_LIBRARY_DIR:
				entry = CDataUtil.createCLibraryPathEntry(name, flags);
				break;
			case COMBO_INDEX_LIBRARY_FILE:
				entry = CDataUtil.createCLibraryFileEntry(name, flags);
				break;
			default:
				result = false;
			}

			entries = new ICLanguageSettingEntry[] { entry };
			shell.dispose();
		} else if (e.widget.equals(buttonCancel)) {
			shell.dispose();
		} else if (e.widget.equals(buttonBrowse)) {
			boolean isDirectory = false;
			boolean isFile = false;
			switch (comboKind.getSelectionIndex()) {
			case COMBO_INDEX_INCLUDE_DIR:
			case COMBO_INDEX_LIBRARY_DIR:
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
					str = AbstractCPropertyTab.getWorkspaceDirDialog(shell, inputName.getText().trim());
					break;
				case COMBO_PATH_INDEX_PROJECT:
					str = AbstractCPropertyTab.getProjectDirDialog(shell, inputName.getText().trim(), project);
					break;
				case COMBO_PATH_INDEX_FILESYSTEM:
					str = AbstractCPropertyTab.getFileSystemDirDialog(shell, inputName.getText().trim());
					break;
				}
			} else if (isFile) {
				switch (comboPathCategory.getSelectionIndex()) {
				case COMBO_PATH_INDEX_WORKSPACE:
					str = AbstractCPropertyTab.getWorkspaceFileDialog(shell, inputName.getText().trim());
					break;
				case COMBO_PATH_INDEX_PROJECT:
					str = AbstractCPropertyTab.getProjectFileDialog(shell, inputName.getText().trim(), project);
					break;
				case COMBO_PATH_INDEX_FILESYSTEM:
					str = AbstractCPropertyTab.getFileSystemFileDialog(shell, inputName.getText().trim());
					break;
				}
			}

			if (str != null) {
				str = strip_wsp(str);
				if (comboPathCategory.getSelectionIndex() == COMBO_PATH_INDEX_PROJECT
						&& str.startsWith(SLASH + project.getName() + SLASH)) {
					str = str.substring(project.getName().length() + 2);
				}
				inputName.setText(str);
			}
		} else if (e.widget.equals(buttonVars)) {
			str = AbstractCPropertyTab.getVariableDialog(shell, cfgDescription);
			if (str != null)
				inputName.insert(str);
		}
	}

	public ICLanguageSettingEntry[] getEntries() {
		return entries;
	}

	private void updateImages() {
		int indexEntryKind = comboKind.getSelectionIndex();
		int indexPathKind = comboPathCategory.getSelectionIndex();
		shell.setText(Messages.LanguageSettingEntryDialog_Add + comboKindItems[indexEntryKind]);

		int kind = comboIndexToKind(indexEntryKind);
		int flagBuiltin = checkBoxBuiltIn.getSelection() ? ICSettingEntry.BUILTIN : 0;
		int flagSystem = checkBoxSystem.getSelection() ? 0 : ICSettingEntry.LOCAL;
		int flagFramework = checkBoxFramework.getSelection() ? ICSettingEntry.FRAMEWORKS_MAC : 0;
		boolean isWorkspacePath = indexPathKind == COMBO_PATH_INDEX_PROJECT
				|| indexPathKind == COMBO_PATH_INDEX_WORKSPACE;
		int flagWorkspace = isWorkspacePath ? ICSettingEntry.VALUE_WORKSPACE_PATH | ICSettingEntry.RESOLVED : 0;
		int flags = flagBuiltin | flagWorkspace | flagSystem | flagFramework;
		Image image = LanguageSettingsImages.getImage(kind, flags, indexPathKind == COMBO_PATH_INDEX_PROJECT);

		iconComboKind.setImage(image);
		shell.setImage(image);

		buttonBrowse.setImage(pathCategoryImages[indexPathKind]);
	}
}
