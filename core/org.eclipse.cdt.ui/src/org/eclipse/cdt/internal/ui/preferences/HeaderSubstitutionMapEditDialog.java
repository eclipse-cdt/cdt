/*******************************************************************************
 * Copyright (c) 2013 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * 	   Sergey Prigogin (Google) - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.cdt.internal.corext.codemanipulation.IncludeInfo;
import org.eclipse.cdt.internal.ui.ICHelpContextIds;
import org.eclipse.cdt.internal.ui.dialogs.ResizableStatusDialog;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.refactoring.includes.HeaderSubstitutionMap;
import org.eclipse.cdt.internal.ui.refactoring.includes.IncludeMap;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ComboDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.Separator;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;

import com.ibm.icu.text.Collator;

/**
 * Dialog for editing a header file substitution map.
 */
public class HeaderSubstitutionMapEditDialog extends ResizableStatusDialog {
	private static final String UTF_8 = "UTF-8"; //$NON-NLS-1$
	private static final String TAG_HEADER_SUBSTITUTION_MAP = "header_substitution_map"; //$NON-NLS-1$
	private static final Collator COLLATOR = Collator.getInstance();
	private static final Comparator<HeaderSubstitutionRule> SOURCE_COMPARATOR = new Comparator<HeaderSubstitutionRule>() {
		@Override
		public int compare(HeaderSubstitutionRule r1, HeaderSubstitutionRule r2) {
			return COLLATOR.compare(r1.getSource(), r2.getSource());
		}
	};

	private class HeaderSubstitutionListField extends ListDialogField<HeaderSubstitutionRule> {

		HeaderSubstitutionListField(IListAdapter<HeaderSubstitutionRule> adapter, String[] buttons) {
			super(adapter, buttons, new HeaderSubstitutionLabelProvider());
		}

		@Override
		protected boolean getManagedButtonState(ISelection sel, int index) {
			if (index == IDX_REMOVE) {
				return !sel.isEmpty();
			} else if (index == IDX_UP) {
				return !sel.isEmpty() && canMoveUp();
			} else if (index == IDX_DOWN) {
				return !sel.isEmpty() && canMoveDown();
			}
			return true;
		}

		@Override
		protected boolean managedButtonPressed(int index) {
			if (index == IDX_REMOVE) {
				remove();
			} else if (index == IDX_UP) {
				up();
			} else if (index == IDX_DOWN) {
				down();
			} else {
				return false;
			}
			return true;
		}

		@Override
		protected boolean canMoveUp() {
			if (!isOkToUse(fTableControl))
				return false;

			int[] indc = fTable.getTable().getSelectionIndices();
			for (int i = 0; i < indc.length; i++) {
				int index = indc[i];
				if (index == 0 || SOURCE_COMPARATOR.compare(fElements.get(index), fElements.get(index - 1)) != 0) {
					return false;
				}
			}
			return true;
		}

		@Override
		protected boolean canMoveDown() {
			if (!isOkToUse(fTableControl))
				return false;

			int k = fElements.size() - 1;
			int[] indc = fTable.getTable().getSelectionIndices();
			for (int i = 0; i < indc.length; i++) {
				int index = indc[i];
				if (index == k || SOURCE_COMPARATOR.compare(fElements.get(index), fElements.get(index + 1)) != 0) {
					return false;
				}
			}
			return true;
		}

	}

	private class HeaderSubstitutionLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider {

		public HeaderSubstitutionLabelProvider() {
		}

		@Override
		public Image getImage(Object element) {
			return null;
		}

		@Override
		public String getText(Object element) {
			return getColumnText(element, 0);
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			HeaderSubstitutionRule substitution = (HeaderSubstitutionRule) element;
			return columnIndex == 0 ? substitution.getSource() : substitution.getTarget();
		}

		@Override
		public Font getFont(Object element) {
			return null;
		}
	}

	public class ListAdapter implements IListAdapter<HeaderSubstitutionRule>, IDialogFieldListener {
		private boolean canEdit(List<HeaderSubstitutionRule> selectedElements) {
			return selectedElements.size() == 1;
		}

		@Override
		public void customButtonPressed(ListDialogField<HeaderSubstitutionRule> field, int index) {
			onButtonPressed(field, index);
		}

		@Override
		public void selectionChanged(ListDialogField<HeaderSubstitutionRule> field) {
			List<HeaderSubstitutionRule> selectedElements = field.getSelectedElements();
			field.enableButton(IDX_EDIT, canEdit(selectedElements));
		}

		@Override
		public void doubleClicked(ListDialogField<HeaderSubstitutionRule> field) {
			if (canEdit(field.getSelectedElements())) {
				onButtonPressed(field, IDX_EDIT);
			}
		}

		@Override
		public void dialogFieldChanged(DialogField field) {
			updateButtonState();
		}
	}

	private static abstract class ButtonSelectionListener implements SelectionListener {
		@Override
		public void widgetDefaultSelected(SelectionEvent e) {
		}
	}

	private static final int IDX_ADD = 0;
	private static final int IDX_EDIT = 1;
	private static final int IDX_REMOVE = 2;
	private static final int IDX_UP = 3;
	private static final int IDX_DOWN = 4;

	private final StringDialogField fNameField;
	private final ComboDialogField fAppliesToField;
	private final HeaderSubstitutionListField fUnconditionalSubstitutionsField;
	private final HeaderSubstitutionListField fOptionalSubstitutionsField;

	private final Set<String> fExistingNames;
	private final boolean fNewMap;

	public HeaderSubstitutionMapEditDialog(Shell parent, HeaderSubstitutionMap map,
			List<HeaderSubstitutionMap> existingEntries) {
		super(parent);

		fExistingNames = new HashSet<>();
		for (HeaderSubstitutionMap exising : existingEntries) {
			if (!exising.equals(map)) {
				fExistingNames.add(exising.getName());
			}
		}

		if (map == null) {
			fNewMap = true;
			setTitle(PreferencesMessages.HeaderSubstitutionMapEditDialog_new_title);
		} else {
			fNewMap = false;
			setTitle(PreferencesMessages.HeaderSubstitutionMapEditDialog_edit_title);
		}

		ListAdapter adapter = new ListAdapter();

		fNameField = new StringDialogField();
		fNameField.setLabelText(PreferencesMessages.HeaderSubstitutionMapEditDialog_name);
		fNameField.setDialogFieldListener(adapter);

		String[] items = new String[] { PreferencesMessages.HeaderSubstitutionMapEditDialog_c_and_cpp,
				PreferencesMessages.HeaderSubstitutionMapEditDialog_cpp_only, };

		fAppliesToField = new ComboDialogField(SWT.READ_ONLY);
		fAppliesToField.setLabelText(PreferencesMessages.HeaderSubstitutionMapEditDialog_applies_to);
		fAppliesToField.setItems(items);

		String[] buttons = new String[] { PreferencesMessages.HeaderSubstitutionMapEditDialog_add_button,
				PreferencesMessages.HeaderSubstitutionMapEditDialog_edit_button,
				PreferencesMessages.HeaderSubstitutionMapEditDialog_remove_button, };
		fUnconditionalSubstitutionsField = new HeaderSubstitutionListField(adapter, buttons);
		fUnconditionalSubstitutionsField
				.setLabelText(PreferencesMessages.HeaderSubstitutionMapEditDialog_required_substitution);
		fUnconditionalSubstitutionsField.setDialogFieldListener(adapter);

		final String[] columnsHeaders = new String[] { PreferencesMessages.HeaderSubstitutionMapEditDialog_header,
				PreferencesMessages.HeaderSubstitutionMapEditDialog_replacement, };
		fUnconditionalSubstitutionsField.setTableColumns(new ListDialogField.ColumnsDescription(columnsHeaders, true));

		buttons = new String[] { PreferencesMessages.HeaderSubstitutionMapEditDialog_add_button2,
				PreferencesMessages.HeaderSubstitutionMapEditDialog_edit_button2,
				PreferencesMessages.HeaderSubstitutionMapEditDialog_remove_button2,
				PreferencesMessages.HeaderSubstitutionMapEditDialog_up_button,
				PreferencesMessages.HeaderSubstitutionMapEditDialog_down_button, };
		fOptionalSubstitutionsField = new HeaderSubstitutionListField(adapter, buttons);
		fOptionalSubstitutionsField
				.setLabelText(PreferencesMessages.HeaderSubstitutionMapEditDialog_optional_substitution);
		fOptionalSubstitutionsField.setDialogFieldListener(adapter);
		fOptionalSubstitutionsField.enableButton(IDX_EDIT, false);

		fOptionalSubstitutionsField.setTableColumns(new ListDialogField.ColumnsDescription(columnsHeaders, true));

		updateFromMap(map);

		adapter.selectionChanged(fUnconditionalSubstitutionsField);
		adapter.selectionChanged(fOptionalSubstitutionsField);
	}

	private void updateFromMap(HeaderSubstitutionMap map) {
		fNameField.setText(map != null ? map.getName() : createUniqueName());

		fAppliesToField.selectItem(map != null && map.isCppOnly() ? 1 : 0);

		if (map != null) {
			List<HeaderSubstitutionRule> substitutionRules = getSubstitutionRules(
					map.getUnconditionalSubstitutionMap());
			fUnconditionalSubstitutionsField.setElements(substitutionRules);
			substitutionRules = getSubstitutionRules(map.getOptionalSubstitutionMap());
			fOptionalSubstitutionsField.setElements(substitutionRules);
		}
	}

	private String createUniqueName() {
		for (int i = 1;; i++) {
			String name = NLS.bind(PreferencesMessages.HeaderSubstitutionMapEditDialog_default_map_name, i);
			if (!fExistingNames.contains(name))
				return name;
		}
	}

	private List<HeaderSubstitutionRule> getSubstitutionRules(IncludeMap map) {
		ArrayList<HeaderSubstitutionRule> result = new ArrayList<>();
		for (Entry<IncludeInfo, List<IncludeInfo>> entry : map.getMap().entrySet()) {
			String source = stripQuotes(entry.getKey().toString());
			for (IncludeInfo target : entry.getValue()) {
				boolean unconditional = map.isUnconditionalSubstitution();
				HeaderSubstitutionRule rule = new HeaderSubstitutionRule(source, stripQuotes(target.toString()),
						unconditional);
				result.add(rule);
			}

		}
		Collections.sort(result, SOURCE_COMPARATOR);
		return result;
	}

	private String stripQuotes(String str) {
		if (str.length() > 2 && str.charAt(0) == '"' && str.charAt(str.length() - 1) == '"')
			return str.substring(1, str.length() - 1);
		return str;
	}

	public HeaderSubstitutionMap getResult() {
		HeaderSubstitutionMap map = createEmptyMap();
		for (HeaderSubstitutionRule substitution : fUnconditionalSubstitutionsField.getElements()) {
			map.addMapping(substitution.getSource(), substitution.getTarget(), true);
		}
		for (HeaderSubstitutionRule substitution : fOptionalSubstitutionsField.getElements()) {
			map.addMapping(substitution.getSource(), substitution.getTarget(), false);
		}
		return map;
	}

	private HeaderSubstitutionMap createEmptyMap() {
		HeaderSubstitutionMap map = new HeaderSubstitutionMap(fAppliesToField.getSelectionIndex() != 0);
		map.setName(fNameField.getText().trim());
		return map;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);

		PixelConverter conv = new PixelConverter(composite);

		Composite inner = new Composite(composite, SWT.NONE);
		inner.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 4;
		inner.setLayout(layout);

		fNameField.doFillIntoGrid(inner, 3);
		if (fNewMap)
			fNameField.getTextControl(null).selectAll();
		Button button = new Button(inner, SWT.PUSH);
		button.setText(PreferencesMessages.HeaderSubstitutionMapEditDialog_import_button);
		button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		button.addSelectionListener(new ButtonSelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				importFromFile();
			}
		});

		fAppliesToField.doFillIntoGrid(inner, 3);
		button = new Button(inner, SWT.PUSH);
		button.setText(PreferencesMessages.HeaderSubstitutionMapEditDialog_export_button);
		button.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false));
		button.addSelectionListener(new ButtonSelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				exportToFile();
			}
		});

		Separator separator = new Separator(SWT.NONE);
		separator.doFillIntoGrid(inner, 4, conv.convertHeightInCharsToPixels(1));

		int minHeight = convertHeightInCharsToPixels(12);

		fUnconditionalSubstitutionsField.doFillIntoGrid(inner, 4);
		LayoutUtil.setHeightHint(fUnconditionalSubstitutionsField.getListControl(null), minHeight);

		fOptionalSubstitutionsField.doFillIntoGrid(inner, 4);
		LayoutUtil.setHeightHint(fOptionalSubstitutionsField.getListControl(null), minHeight);

		applyDialogFont(composite);

		return composite;
	}

	private void importFromFile() {
		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);
		dialog.setText(PreferencesMessages.HeaderSubstitutionMapEditDialog_import_title);
		// TODO(sprigogin): Add import from .imp files
		// (see http://code.google.com/p/include-what-you-use/wiki/IWYUMappings)
		dialog.setFilterExtensions(new String[] { "*.xml" }); //$NON-NLS-1$
		String path = dialog.open();

		if (path == null)
			return;

		try {
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(Path.fromOSString(path));
			InputStream stream = fileStore.openInputStream(EFS.NONE, null);
			InputStreamReader reader = new InputStreamReader(new BufferedInputStream(stream), UTF_8);
			try {
				HeaderSubstitutionMap map = HeaderSubstitutionMap.fromSerializedMemento(reader);
				updateFromMap(map);
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
				}
			}
		} catch (IOException e) {
			String title = PreferencesMessages.HeaderSubstitutionMapEditDialog_import_title;
			String message = e.getLocalizedMessage();
			if (message != null) {
				message = NLS.bind(PreferencesMessages.HeaderSubstitutionMapEditDialog_error_parse_message, message);
			} else {
				message = NLS.bind(PreferencesMessages.HeaderSubstitutionMapEditDialog_error_read_message, path);
			}
			MessageDialog.openError(getShell(), title, message);
		} catch (CoreException e) {
			MessageDialog.openError(getShell(), PreferencesMessages.HeaderSubstitutionMapEditDialog_import_title,
					e.getLocalizedMessage());
		}

		updateButtonState();
	}

	private void exportToFile() {
		HeaderSubstitutionMap map = getResult();

		FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
		dialog.setText(PreferencesMessages.HeaderSubstitutionMapEditDialog_export_title);
		dialog.setFilterExtensions(new String[] { "*.xml" }); //$NON-NLS-1$
		dialog.setFileName(map.getName() + ".xml"); //$NON-NLS-1$
		dialog.setOverwrite(true);
		String path = dialog.open();

		if (path == null)
			return;

		try {
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(Path.fromOSString(path));
			OutputStream stream = fileStore.openOutputStream(EFS.OVERWRITE, null);

			XMLMemento memento = XMLMemento.createWriteRoot(TAG_HEADER_SUBSTITUTION_MAP);
			map.saveToMemento(memento);
			Writer writer = new OutputStreamWriter(new BufferedOutputStream(stream), UTF_8);
			try {
				memento.save(writer);
			} finally {
				try {
					writer.close();
				} catch (IOException e) {
				}
			}
		} catch (IOException e) {
			MessageDialog.openError(getShell(), PreferencesMessages.HeaderSubstitutionMapEditDialog_export_title,
					PreferencesMessages.HeaderSubstitutionMapEditDialog_error_write_message);
		} catch (CoreException e) {
			MessageDialog.openError(getShell(), PreferencesMessages.HeaderSubstitutionMapEditDialog_export_title,
					e.getLocalizedMessage());
		}
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell,
				ICHelpContextIds.HEADER_SUBSTITUTION_MAP_EDIT_DIALOG);
	}

	private void onButtonPressed(ListDialogField<HeaderSubstitutionRule> field, int buttonId) {
		HeaderSubstitutionRule oldRule = null;
		if (buttonId == IDX_ADD) {
			oldRule = new HeaderSubstitutionRule("", "", field == fUnconditionalSubstitutionsField); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			oldRule = field.getSelectedElements().get(0);
		}

		switch (buttonId) {
		case IDX_ADD:
		case IDX_EDIT:
			HeaderSubstitutionEditDialog dialog = new HeaderSubstitutionEditDialog(getShell(), oldRule);
			if (dialog.open() != Window.OK)
				break;

			HeaderSubstitutionRule newRule = dialog.getResult();
			ListDialogField<HeaderSubstitutionRule> newField = newRule.isUnconditionalSubstitution()
					? fUnconditionalSubstitutionsField
					: fOptionalSubstitutionsField;
			if (buttonId == IDX_ADD) {
				newField.addElement(newRule);
			} else {
				if (newField == field) {
					field.replaceElement(oldRule, newRule);
				} else {
					field.removeElement(oldRule);
					newField.addElement(newRule);
				}
			}
			// Restore order.
			List<HeaderSubstitutionRule> elements = newField.getElements();
			Collections.sort(elements, SOURCE_COMPARATOR);
			newField.setElements(elements);

			// There can be no more than one unconditional substitution for any header file.
			// The unconditional and optional substitutions are mutually exclusive.
			for (HeaderSubstitutionRule rule : fUnconditionalSubstitutionsField.getElements()) {
				if (rule != newRule && rule.getSource().equals(newRule.getSource())) {
					fUnconditionalSubstitutionsField.removeElement(rule);
				}
			}
			if (newRule.isUnconditionalSubstitution()) {
				List<HeaderSubstitutionRule> rulesToDelete = null;
				for (HeaderSubstitutionRule rule : fOptionalSubstitutionsField.getElements()) {
					if (rule.getSource().equals(newRule.getSource())) {
						if (rulesToDelete == null)
							rulesToDelete = new ArrayList<>();
						rulesToDelete.add(rule);
					}
				}
				if (rulesToDelete != null)
					fOptionalSubstitutionsField.removeElements(rulesToDelete);
			}
			break;
		}

		updateButtonState();
	}

	private void updateButtonState() {
		IStatus status = StatusInfo.OK_STATUS;
		String name = fNameField.getText().trim();
		if (name.isEmpty()) {
			status = new StatusInfo(IStatus.WARNING, PreferencesMessages.HeaderSubstitutionMapEditDialog_enter_name);
		} else if (fExistingNames.contains(name)) {
			status = new StatusInfo(IStatus.WARNING,
					PreferencesMessages.HeaderSubstitutionMapEditDialog_duplicate_name);
		} else if (fUnconditionalSubstitutionsField.getElements().isEmpty()
				&& fOptionalSubstitutionsField.getElements().isEmpty()) {
			status = new StatusInfo(IStatus.WARNING, PreferencesMessages.HeaderSubstitutionMapEditDialog_map_is_empty);
		}
		updateStatus(status);
	}

	@Override
	protected void updateButtonsEnableState(IStatus status) {
		// OK button is disabled unless the status is OK.
		super.updateButtonsEnableState(status.isOK() ? status : new StatusInfo(IStatus.ERROR, null));
	}
}
