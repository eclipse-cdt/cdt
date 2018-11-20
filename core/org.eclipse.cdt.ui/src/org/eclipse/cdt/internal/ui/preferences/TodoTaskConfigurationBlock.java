/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.internal.corext.util.Messages;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;
import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
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
 * UI for editing task tags.
 */
public class TodoTaskConfigurationBlock extends OptionsConfigurationBlock {
	private static final Key PREF_TODO_TASK_TAGS = getCDTCoreKey(CCorePreferenceConstants.TODO_TASK_TAGS);
	private static final Key PREF_TODO_TASK_PRIORITIES = getCDTCoreKey(CCorePreferenceConstants.TODO_TASK_PRIORITIES);
	private static final Key PREF_TODO_TASK_CASE_SENSITIVE = getCDTCoreKey(
			CCorePreferenceConstants.TODO_TASK_CASE_SENSITIVE);

	private static final Key[] ALL_KEYS = new Key[] { PREF_TODO_TASK_TAGS, PREF_TODO_TASK_PRIORITIES,
			PREF_TODO_TASK_CASE_SENSITIVE };

	private static final String TASK_PRIORITY_HIGH = CCorePreferenceConstants.TASK_PRIORITY_HIGH;
	private static final String TASK_PRIORITY_NORMAL = CCorePreferenceConstants.TASK_PRIORITY_NORMAL;
	private static final String TASK_PRIORITY_LOW = CCorePreferenceConstants.TASK_PRIORITY_LOW;

	public static class TodoTask {
		public String name;
		public String priority;
	}

	private class TodoTaskLabelProvider extends LabelProvider implements ITableLabelProvider, IFontProvider {

		public TodoTaskLabelProvider() {
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
			TodoTask task = (TodoTask) element;
			if (columnIndex == 0) {
				String name = task.name;
				if (isDefaultTask(task)) {
					name = Messages.format(PreferencesMessages.TodoTaskConfigurationBlock_tasks_default, name);
				}
				return name;
			}
			if (TASK_PRIORITY_HIGH.equals(task.priority)) {
				return PreferencesMessages.TodoTaskConfigurationBlock_markers_tasks_high_priority;
			} else if (TASK_PRIORITY_NORMAL.equals(task.priority)) {
				return PreferencesMessages.TodoTaskConfigurationBlock_markers_tasks_normal_priority;
			} else if (TASK_PRIORITY_LOW.equals(task.priority)) {
				return PreferencesMessages.TodoTaskConfigurationBlock_markers_tasks_low_priority;
			}
			return ""; //$NON-NLS-1$
		}

		@Override
		public Font getFont(Object element) {
			if (isDefaultTask((TodoTask) element)) {
				return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
			}
			return null;
		}
	}

	private static class TodoTaskSorter extends ViewerComparator {
		@Override
		public int compare(Viewer viewer, Object e1, Object e2) {
			return getComparator().compare(((TodoTask) e1).name, ((TodoTask) e2).name);
		}
	}

	private static final int IDX_ADD = 0;
	private static final int IDX_EDIT = 1;
	private static final int IDX_REMOVE = 2;
	private static final int IDX_DEFAULT = 4;

	private IStatus fTaskTagsStatus;
	private final ListDialogField<TodoTask> fTodoTasksList;
	private final SelectionButtonDialogField fCaseSensitiveCheckBox;

	public TodoTaskConfigurationBlock(IStatusChangeListener context, IProject project,
			IWorkbenchPreferenceContainer container) {
		super(context, project, ALL_KEYS, container);

		TaskTagAdapter adapter = new TaskTagAdapter();
		String[] buttons = new String[] { PreferencesMessages.TodoTaskConfigurationBlock_markers_tasks_add_button,
				PreferencesMessages.TodoTaskConfigurationBlock_markers_tasks_edit_button,
				PreferencesMessages.TodoTaskConfigurationBlock_markers_tasks_remove_button, null,
				PreferencesMessages.TodoTaskConfigurationBlock_markers_tasks_setdefault_button, };
		fTodoTasksList = new ListDialogField<>(adapter, buttons, new TodoTaskLabelProvider());
		fTodoTasksList.setDialogFieldListener(adapter);
		fTodoTasksList.setRemoveButtonIndex(IDX_REMOVE);

		String[] columnsHeaders = new String[] {
				PreferencesMessages.TodoTaskConfigurationBlock_markers_tasks_name_column,
				PreferencesMessages.TodoTaskConfigurationBlock_markers_tasks_priority_column, };

		fTodoTasksList.setTableColumns(new ListDialogField.ColumnsDescription(columnsHeaders, true));
		fTodoTasksList.setViewerComparator(new TodoTaskSorter());

		fCaseSensitiveCheckBox = new SelectionButtonDialogField(SWT.CHECK);
		fCaseSensitiveCheckBox.setLabelText(PreferencesMessages.TodoTaskConfigurationBlock_casesensitive_label);
		fCaseSensitiveCheckBox.setDialogFieldListener(adapter);

		unpackTodoTasks();
		if (fTodoTasksList.getSize() > 0) {
			fTodoTasksList.selectFirstElement();
		} else {
			fTodoTasksList.enableButton(IDX_EDIT, false);
			fTodoTasksList.enableButton(IDX_DEFAULT, false);
		}

		fTaskTagsStatus = new StatusInfo();
	}

	public void setEnabled(boolean isEnabled) {
		fTodoTasksList.setEnabled(isEnabled);
		fCaseSensitiveCheckBox.setEnabled(isEnabled);
	}

	final boolean isDefaultTask(TodoTask task) {
		return fTodoTasksList.getIndexOfElement(task) == 0;
	}

	private void setToDefaultTask(TodoTask task) {
		List<TodoTask> elements = fTodoTasksList.getElements();
		elements.remove(task);
		elements.add(0, task);
		fTodoTasksList.setElements(elements);
		fTodoTasksList.enableButton(IDX_DEFAULT, false);
	}

	public class TaskTagAdapter implements IListAdapter<TodoTask>, IDialogFieldListener {
		private boolean canEdit(List<TodoTask> selectedElements) {
			return selectedElements.size() == 1;
		}

		private boolean canSetToDefault(List<TodoTask> selectedElements) {
			return selectedElements.size() == 1 && !isDefaultTask(selectedElements.get(0));
		}

		@Override
		public void customButtonPressed(ListDialogField<TodoTask> field, int index) {
			doTodoButtonPressed(index);
		}

		@Override
		public void selectionChanged(ListDialogField<TodoTask> field) {
			List<TodoTask> selectedElements = field.getSelectedElements();
			field.enableButton(IDX_EDIT, canEdit(selectedElements));
			field.enableButton(IDX_DEFAULT, canSetToDefault(selectedElements));
		}

		@Override
		public void doubleClicked(ListDialogField<TodoTask> field) {
			if (canEdit(field.getSelectedElements())) {
				doTodoButtonPressed(IDX_EDIT);
			}
		}

		@Override
		public void dialogFieldChanged(DialogField field) {
			updateModel(field);
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

		GridData data = new GridData(GridData.FILL_BOTH);
		data.widthHint = conv.convertWidthInCharsToPixels(50);
		Control listControl = fTodoTasksList.getListControl(composite);
		listControl.setLayoutData(data);

		Control buttonsControl = fTodoTasksList.getButtonBox(composite);
		buttonsControl.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_BEGINNING));

		fCaseSensitiveCheckBox.doFillIntoGrid(composite, 2);

		validateSettings(null, null, null);

		return composite;
	}

	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		if (!areSettingsEnabled()) {
			return;
		}

		if (changedKey != null) {
			if (PREF_TODO_TASK_TAGS.equals(changedKey)) {
				fTaskTagsStatus = validateTaskTags();
			} else {
				return;
			}
		} else {
			fTaskTagsStatus = validateTaskTags();
		}
		IStatus status = fTaskTagsStatus; //StatusUtil.getMostSevere(new IStatus[] { fTaskTagsStatus });
		fContext.statusChanged(status);
	}

	private IStatus validateTaskTags() {
		return new StatusInfo();
	}

	private void updateModel(DialogField field) {
		if (field == fTodoTasksList) {
			StringBuilder tags = new StringBuilder();
			StringBuilder prios = new StringBuilder();
			List<TodoTask> list = fTodoTasksList.getElements();
			for (int i = 0; i < list.size(); i++) {
				if (i > 0) {
					tags.append(',');
					prios.append(',');
				}
				TodoTask elem = list.get(i);
				tags.append(elem.name);
				prios.append(elem.priority);
			}
			setValue(PREF_TODO_TASK_TAGS, tags.toString());
			setValue(PREF_TODO_TASK_PRIORITIES, prios.toString());
			validateSettings(PREF_TODO_TASK_TAGS, null, null);
		} else if (field == fCaseSensitiveCheckBox) {
			String state = String.valueOf(fCaseSensitiveCheckBox.isSelected());
			setValue(PREF_TODO_TASK_CASE_SENSITIVE, state);
		}
	}

	@Override
	protected void updateControls() {
		unpackTodoTasks();
	}

	private void unpackTodoTasks() {
		String currTags = getValue(PREF_TODO_TASK_TAGS);
		String currPrios = getValue(PREF_TODO_TASK_PRIORITIES);
		String[] tags = getTokens(currTags, ","); //$NON-NLS-1$
		String[] prios = getTokens(currPrios, ","); //$NON-NLS-1$
		ArrayList<TodoTask> elements = new ArrayList<>(tags.length);
		for (int i = 0; i < tags.length; i++) {
			TodoTask task = new TodoTask();
			task.name = tags[i].trim();
			task.priority = (i < prios.length) ? prios[i] : TASK_PRIORITY_NORMAL;
			elements.add(task);
		}
		fTodoTasksList.setElements(elements);

		boolean isCaseSensitive = getBooleanValue(PREF_TODO_TASK_CASE_SENSITIVE);
		fCaseSensitiveCheckBox.setSelection(isCaseSensitive);
	}

	private void doTodoButtonPressed(int index) {
		TodoTask edited = null;
		if (index != IDX_ADD) {
			edited = fTodoTasksList.getSelectedElements().get(0);
		}
		if (index == IDX_ADD || index == IDX_EDIT) {
			TodoTaskInputDialog dialog = new TodoTaskInputDialog(getShell(), edited, fTodoTasksList.getElements());
			if (dialog.open() == Window.OK) {
				if (edited != null) {
					fTodoTasksList.replaceElement(edited, dialog.getResult());
				} else {
					fTodoTasksList.addElement(dialog.getResult());
				}
			}
		} else if (index == IDX_DEFAULT) {
			setToDefaultTask(edited);
		}
	}
}
