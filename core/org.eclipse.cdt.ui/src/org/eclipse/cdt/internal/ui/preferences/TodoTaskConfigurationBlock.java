/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CCorePreferenceConstants;
import org.eclipse.cdt.core.model.ICProject;

import org.eclipse.cdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.cdt.internal.ui.util.PixelConverter;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.cdt.internal.ui.dialogs.IStatusChangeListener;

/**
  */
public class TodoTaskConfigurationBlock extends OptionsConfigurationBlock {

	private static final String PREF_TRANSLATION_TASK_TAGS= CCorePreferenceConstants.TRANSLATION_TASK_TAGS;
	private static final String PREF_TRANSLATION_TASK_PRIORITIES= CCorePreferenceConstants.TRANSLATION_TASK_PRIORITIES;
	
	private static final String PRIORITY_HIGH= CCorePlugin.TRANSLATION_TASK_PRIORITY_HIGH;
	private static final String PRIORITY_NORMAL= CCorePlugin.TRANSLATION_TASK_PRIORITY_NORMAL;
	private static final String PRIORITY_LOW= CCorePlugin.TRANSLATION_TASK_PRIORITY_LOW;		
	
	public static class TodoTask {
		public String name;
		public String priority;
	}
	
	private static class TodoTaskLabelProvider extends LabelProvider implements ITableLabelProvider {
	
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage(Object element) {
			return null; 
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText(Object element) {
			return getColumnText(element, 0);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			TodoTask task= (TodoTask) element;
			if (columnIndex == 0) {
				return task.name;
			}
			if (PRIORITY_HIGH.equals(task.priority)) {
				return PreferencesMessages.getString("TodoTaskConfigurationBlock.markers.tasks.high.priority"); //$NON-NLS-1$
			} else if (PRIORITY_NORMAL.equals(task.priority)) {
				return PreferencesMessages.getString("TodoTaskConfigurationBlock.markers.tasks.normal.priority"); //$NON-NLS-1$
			} else if (PRIORITY_LOW.equals(task.priority)) {
				return PreferencesMessages.getString("TodoTaskConfigurationBlock.markers.tasks.low.priority"); //$NON-NLS-1$
			}
			return ""; //$NON-NLS-1$
		}

	}
	
	private PixelConverter fPixelConverter;

	private IStatus fTaskTagsStatus;
	private ListDialogField fTodoTasksList;

	public TodoTaskConfigurationBlock(IStatusChangeListener context, ICProject project) {
		super(context, project);
						
		TaskTagAdapter adapter=  new TaskTagAdapter();
		String[] buttons= new String[] {
			/* 0 */ PreferencesMessages.getString("TodoTaskConfigurationBlock.markers.tasks.add.button"), //$NON-NLS-1$
			/* 1 */ PreferencesMessages.getString("TodoTaskConfigurationBlock.markers.tasks.remove.button"), //$NON-NLS-1$
			null,
			/* 3 */ PreferencesMessages.getString("TodoTaskConfigurationBlock.markers.tasks.edit.button"), //$NON-NLS-1$
		};
		fTodoTasksList= new ListDialogField(adapter, buttons, new TodoTaskLabelProvider());
		fTodoTasksList.setDialogFieldListener(adapter);
		fTodoTasksList.setLabelText(PreferencesMessages.getString("TodoTaskConfigurationBlock.markers.tasks.label")); //$NON-NLS-1$
		fTodoTasksList.setRemoveButtonIndex(1);
		
		String[] columnsHeaders= new String[] {
			PreferencesMessages.getString("TodoTaskConfigurationBlock.markers.tasks.name.column"), //$NON-NLS-1$
			PreferencesMessages.getString("TodoTaskConfigurationBlock.markers.tasks.priority.column"), //$NON-NLS-1$
		};
		
		fTodoTasksList.setTableColumns(new ListDialogField.ColumnsDescription(columnsHeaders, true));
		unpackTodoTasks();
		if (fTodoTasksList.getSize() > 0) {
			fTodoTasksList.selectFirstElement();
		} else {
			fTodoTasksList.enableButton(3, false);
		}
		
		fTaskTagsStatus= new StatusInfo();		
	}
	
	protected final String[] getAllKeys() {
		return new String[] {
			PREF_TRANSLATION_TASK_TAGS, PREF_TRANSLATION_TASK_PRIORITIES
		};	
	}	
	
	public class TaskTagAdapter implements IListAdapter, IDialogFieldListener {

		private boolean canEdit(ListDialogField field) {
			return field.getSelectedElements().size() == 1;
		}

		public void customButtonPressed(ListDialogField field, int index) {
			doTodoButtonPressed(index);
		}

		public void selectionChanged(ListDialogField field) {
			field.enableButton(3, canEdit(field));
		}
			
		public void doubleClicked(ListDialogField field) {
			if (canEdit(field)) {
				doTodoButtonPressed(3);
			}
		}

		public void dialogFieldChanged(DialogField field) {
			validateSettings(PREF_TRANSLATION_TASK_TAGS, null);
		}			
		
	}
		
	protected Control createContents(Composite parent) {
		fPixelConverter= new PixelConverter(parent);
		setShell(parent.getShell());
		
		Composite markersComposite= createMarkersTabContent(parent);
		
		validateSettings(null, null);
	
		return markersComposite;
	}

	private Composite createMarkersTabContent(Composite folder) {
		
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		
		Composite markersComposite= new Composite(folder, SWT.NULL);
		markersComposite.setLayout(layout);
		
		fTodoTasksList.doFillIntoGrid(markersComposite, 3);
		LayoutUtil.setHorizontalSpan(fTodoTasksList.getLabelControl(null), 2);
		
		Table table= fTodoTasksList.getTableViewer().getTable();
		GridData data= (GridData)fTodoTasksList.getListControl(null).getLayoutData();
		data.grabExcessHorizontalSpace= true;
		data.verticalAlignment= 0;
		data.heightHint= SWTUtil.getTableHeightHint(table, 6);

		return markersComposite;
	}

	protected void validateSettings(String changedKey, String newValue) {
		if (changedKey != null) {
			if (PREF_TRANSLATION_TASK_TAGS.equals(changedKey)) {
				fTaskTagsStatus= validateTaskTags();
			} else {
				return;
			}
		} else {
			fTaskTagsStatus= validateTaskTags();
		}		
		IStatus status= fTaskTagsStatus; //StatusUtil.getMostSevere(new IStatus[] { fTaskTagsStatus });
		fContext.statusChanged(status);
	}
	
	private IStatus validateTaskTags() {
		return new StatusInfo();
	}	

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.preferences.OptionsConfigurationBlock#performOk(boolean)
	 */
	public boolean performOk(boolean enabled) {
		packTodoTasks();
		return super.performOk(enabled);
	}


	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		String title= PreferencesMessages.getString("TodoTaskConfigurationBlock.needsbuild.title"); //$NON-NLS-1$
		String message;
		if (fProject == null) {
			message= PreferencesMessages.getString("TodoTaskConfigurationBlock.needsfullbuild.message"); //$NON-NLS-1$
		} else {
			message= PreferencesMessages.getString("TodoTaskConfigurationBlock.needsprojectbuild.message"); //$NON-NLS-1$
		}	
		return new String[] { title, message };
	}
	
	protected String[] getFullReParseDialogStrings(boolean workspaceSettings) {
		String title= PreferencesMessages.getString("TodoTaskConfigurationBlock.needsparse.title"); //$NON-NLS-1$
		String message;
		if (fProject == null) {
			message= PreferencesMessages.getString("TodoTaskConfigurationBlock.needsfullparse.message"); //$NON-NLS-1$
		} else {
			message= PreferencesMessages.getString("TodoTaskConfigurationBlock.needsprojectparse.message"); //$NON-NLS-1$
		}	
		return new String[] { title, message };
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.ui.preferences.OptionsConfigurationBlock#updateControls()
	 */
	protected void updateControls() {
		unpackTodoTasks();
	}
	
	private void unpackTodoTasks() {
		String currTags= (String) fWorkingValues.get(PREF_TRANSLATION_TASK_TAGS);	
		String currPrios= (String) fWorkingValues.get(PREF_TRANSLATION_TASK_PRIORITIES);
		String[] tags= getTokens(currTags, ","); //$NON-NLS-1$
		String[] prios= getTokens(currPrios, ","); //$NON-NLS-1$
		ArrayList elements= new ArrayList(tags.length);
		for (int i= 0; i < tags.length; i++) {
			TodoTask task= new TodoTask();
			task.name= tags[i].trim();
			task.priority= (i < prios.length) ? prios[i] : PRIORITY_NORMAL;
			elements.add(task);
		}
		fTodoTasksList.setElements(elements);
	}
	
	private void packTodoTasks() {
		StringBuffer tags= new StringBuffer();
		StringBuffer prios= new StringBuffer();
		List list= fTodoTasksList.getElements();
		for (int i= 0; i < list.size(); i++) {
			if (i > 0) {
				tags.append(',');
				prios.append(',');
			}
			TodoTask elem= (TodoTask) list.get(i);
			tags.append(elem.name);
			prios.append(elem.priority);
		}
		fWorkingValues.put(PREF_TRANSLATION_TASK_TAGS, tags.toString());
		fWorkingValues.put(PREF_TRANSLATION_TASK_PRIORITIES, prios.toString());
	}
		
	void doTodoButtonPressed(int index) {
		TodoTask edited= null;
		if (index != 0) {
			edited= (TodoTask) fTodoTasksList.getSelectedElements().get(0);
		}
		
		TodoTaskInputDialog dialog= new TodoTaskInputDialog(getShell(), edited, fTodoTasksList.getElements());
		if (dialog.open() == Window.OK) {
			if (edited != null) {
				fTodoTasksList.replaceElement(edited, dialog.getResult());
			} else {
				fTodoTasksList.addElement(dialog.getResult());
			}
		}
	}
}
