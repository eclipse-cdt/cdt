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

import java.util.List;

import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.refactoring.includes.HeaderSubstitutionMap;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * The preference block for configuring header file substitution rules.
 */
public class HeaderSubstitutionBlock extends OptionsConfigurationBlock {
	static final Key KEY_HEADER_SUBSTITUTION = getCDTUIKey(PreferenceConstants.INCLUDES_HEADER_SUBSTITUTION);

	private static Key[] ALL_KEYS = { KEY_HEADER_SUBSTITUTION };

	private class HeaderMapLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider {

		public HeaderMapLabelProvider() {
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
			HeaderSubstitutionMap map = (HeaderSubstitutionMap) element;
			if (columnIndex == 0) {
				return map.getName();
			}
			return map.isCppOnly() ? PreferencesMessages.HeaderSubstitutionBlock_cpp_only
					: PreferencesMessages.HeaderSubstitutionBlock_c_and_cpp;
		}

		@Override
		public Font getFont(Object element) {
			return null;
		}
	}

	private static final int IDX_ADD = 0;
	private static final int IDX_EDIT = 1;
	private static final int IDX_REMOVE = 2;
	private static final int IDX_UP = 3;
	private static final int IDX_DOWN = 4;

	private final ListDialogField<HeaderSubstitutionMap> fHeaderMapsList;
	private IStatus fStatus;

	public HeaderSubstitutionBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, ALL_KEYS, container);

		ListAdapter adapter = new ListAdapter();
		String[] buttons = new String[] { PreferencesMessages.HeaderSubstitutionBlock_add_button,
				PreferencesMessages.HeaderSubstitutionBlock_edit_button,
				PreferencesMessages.HeaderSubstitutionBlock_remove_button,
				PreferencesMessages.HeaderSubstitutionBlock_up_button,
				PreferencesMessages.HeaderSubstitutionBlock_down_button, };
		fHeaderMapsList = new ListDialogField<>(adapter, buttons, new HeaderMapLabelProvider());
		fHeaderMapsList.setLabelText(PreferencesMessages.HeaderSubstitutionBlock_header_substitution_maps);
		fHeaderMapsList.setDialogFieldListener(adapter);
		fHeaderMapsList.setRemoveButtonIndex(IDX_REMOVE);
		fHeaderMapsList.setUpButtonIndex(IDX_UP);
		fHeaderMapsList.setDownButtonIndex(IDX_DOWN);

		String[] columnsHeaders = new String[] { PreferencesMessages.HeaderSubstitutionBlock_name_column_title,
				PreferencesMessages.HeaderSubstitutionBlock_languages_column_title, };

		ColumnLayoutData[] columnData = new ColumnLayoutData[] { new ColumnWeightData(5), new ColumnWeightData(2), };
		fHeaderMapsList.setTableColumns(new ListDialogField.ColumnsDescription(columnData, columnsHeaders, true));

		loadHeaderMaps();
		if (fHeaderMapsList.getSize() > 0) {
			fHeaderMapsList.selectFirstElement();
		} else {
			fHeaderMapsList.enableButton(IDX_EDIT, false);
		}

		fStatus = new StatusInfo();
	}

	private void loadHeaderMaps() {
		String str = getValue(KEY_HEADER_SUBSTITUTION);
		if (str == null)
			return;
		List<HeaderSubstitutionMap> maps = HeaderSubstitutionMap.deserializeMaps(str);
		fHeaderMapsList.setElements(maps);
	}

	@Override
	protected boolean processChanges(IWorkbenchPreferenceContainer container) {
		boolean result = super.processChanges(container);
		List<HeaderSubstitutionMap> maps = fHeaderMapsList.getElements();
		String str = HeaderSubstitutionMap.serializeMaps(maps);
		setValue(KEY_HEADER_SUBSTITUTION, str);
		return result;
	}

	@Override
	protected void settingsUpdated() {
		if (fHeaderMapsList != null)
			loadHeaderMaps();
		super.settingsUpdated();
	}

	public class ListAdapter implements IListAdapter<HeaderSubstitutionMap>, IDialogFieldListener {
		private boolean canEdit(List<HeaderSubstitutionMap> selectedElements) {
			return selectedElements.size() == 1;
		}

		@Override
		public void customButtonPressed(ListDialogField<HeaderSubstitutionMap> field, int index) {
			onButtonPressed(field, index);
		}

		@Override
		public void selectionChanged(ListDialogField<HeaderSubstitutionMap> field) {
			List<HeaderSubstitutionMap> selectedElements = field.getSelectedElements();
			field.enableButton(IDX_EDIT, canEdit(selectedElements));
		}

		@Override
		public void doubleClicked(ListDialogField<HeaderSubstitutionMap> field) {
			if (canEdit(field.getSelectedElements())) {
				onButtonPressed(field, IDX_EDIT);
			}
		}

		@Override
		public void dialogFieldChanged(DialogField field) {
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		setShell(parent.getShell());
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.numColumns = 2;

		PixelConverter conv = new PixelConverter(parent);

		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(layout);
		composite.setFont(parent.getFont());

		LayoutUtil.setHorizontalSpan(fHeaderMapsList.getLabelControl(composite), 2);

		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = conv.convertWidthInCharsToPixels(50);
		Control listControl = fHeaderMapsList.getListControl(composite);
		listControl.setLayoutData(data);

		Control buttonsControl = fHeaderMapsList.getButtonBox(composite);
		buttonsControl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));

		validateSettings(null, null, null);

		return composite;
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		if (!areSettingsEnabled()) {
			return;
		}

		fStatus = new StatusInfo();
		fContext.statusChanged(fStatus);
	}

	private void onButtonPressed(ListDialogField<HeaderSubstitutionMap> field, int index) {
		HeaderSubstitutionMap edited = null;
		if (index != IDX_ADD) {
			edited = field.getSelectedElements().get(0);
		}
		if (index == IDX_ADD || index == IDX_EDIT) {
			HeaderSubstitutionMapEditDialog dialog = new HeaderSubstitutionMapEditDialog(getShell(), edited,
					field.getElements());
			if (dialog.open() == Window.OK) {
				if (edited != null) {
					field.replaceElement(edited, dialog.getResult());
				} else {
					field.addElement(dialog.getResult());
				}
			}
		}
	}
}
