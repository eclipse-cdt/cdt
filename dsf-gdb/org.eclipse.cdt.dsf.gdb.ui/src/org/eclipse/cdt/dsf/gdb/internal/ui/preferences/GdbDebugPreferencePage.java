/*******************************************************************************
 * Copyright (c) 2009, 2018 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ericsson - initial API and implementation
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *     Sergey Prigogin (Google)
 *     Anton Gorenkov - A preference to use RTTI for variable types determination (Bug 377536)
 *     IBM Corporation
 *     Marc Khouzam (Ericsson) - Add preference for aggressive breakpoint filtering (Bug 360735)
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui.preferences;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.dsf.debug.internal.ui.preferences.IntegerWithBooleanFieldEditor;
import org.eclipse.cdt.dsf.debug.internal.ui.preferences.StringWithBooleanFieldEditor;
import org.eclipse.cdt.dsf.gdb.IGdbDebugPreferenceConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.GdbUIPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.IGdbUIConstants;
import org.eclipse.cdt.dsf.gdb.service.command.CustomTimeoutsMap;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.fieldassist.ControlDecoration;
import org.eclipse.jface.fieldassist.FieldDecorationRegistry;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ICellEditorListener;
import org.eclipse.jface.viewers.ICellEditorValidator;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

/**
 * A preference page for settings that are currently only supported in GDB.
 */
public class GdbDebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	private static final int DEFAULT_GDB_COMMAND_LABEL_WIDTH_HINT = 300;

	/**
	 * A vehicle in order to be able to register a selection listener with
	 * a {@link BooleanFieldEditor}.
	 */
	private class ListenableBooleanFieldEditor extends BooleanFieldEditor {

		public ListenableBooleanFieldEditor(String name, String labelText, int style, Composite parent) {
			super(name, labelText, style, parent);
		}

		@Override
		public Button getChangeControl(Composite parent) {
			return super.getChangeControl(parent);
		}
	}

	class AdvancedTimeoutSettingsDialog extends TitleAreaDialog {

		class CommandTimeoutEntry {

			String fCommand;
			Integer fTimeout;

			CommandTimeoutEntry(String command, Integer timeout) {
				fCommand = command;
				fTimeout = timeout;
			}
		}

		class CellEditorListener implements ICellEditorListener {

			CellEditor fEditor;

			public CellEditorListener(CellEditor editor) {
				super();
				fEditor = editor;
			}

			@Override
			public void editorValueChanged(boolean oldValidState, boolean newValidState) {
				if (newValidState) {
					setErrorMessage(null);
				} else {
					setErrorMessage(fEditor.getErrorMessage());
				}
				updateDialogButtons();
			}

			@Override
			public void cancelEditor() {
			}

			@Override
			public void applyEditorValue() {
				validate();
				updateDialogButtons();
			}
		}

		abstract class AbstractEditingSupport extends EditingSupport {

			public AbstractEditingSupport(ColumnViewer viewer) {
				super(viewer);
			}

			@Override
			protected void setValue(Object element, Object value) {
				if (element instanceof CommandTimeoutEntry && value instanceof String) {
					if (processValue((CommandTimeoutEntry) element, (String) value)) {
						fViewer.refresh(element);
						validate();
						updateDialogButtons();
					}
				}
			}

			@Override
			protected Object getValue(Object element) {
				if (element instanceof CommandTimeoutEntry) {
					return doGetValue((CommandTimeoutEntry) element);
				}
				return null;
			}

			@Override
			protected CellEditor getCellEditor(Object element) {
				final CellEditor editor = new TextCellEditor((Composite) getViewer().getControl());
				editor.setValidator(getValidator());
				editor.addListener(new CellEditorListener(editor));
				return editor;
			}

			@Override
			protected boolean canEdit(Object element) {
				return (element instanceof CommandTimeoutEntry);
			}

			abstract boolean processValue(CommandTimeoutEntry entry, String value);

			abstract Object doGetValue(CommandTimeoutEntry entry);

			abstract ICellEditorValidator getValidator();
		}

		private TableViewer fViewer;
		private Button fAddButton;
		private Button fDeleteButton;

		private List<CommandTimeoutEntry> fEntries;

		final private ICellEditorValidator fCommandValidator = new ICellEditorValidator() {

			@Override
			public String isValid(Object value) {
				if (value instanceof String && ((String) value).trim().length() == 0) {
					return MessagesForPreferences.GdbDebugPreferencePage_Command_field_can_not_be_empty;
				}
				return null;
			}
		};

		final private ICellEditorValidator fTimeoutValidator = new ICellEditorValidator() {

			@Override
			public String isValid(Object value) {
				if (value instanceof String) {
					try {
						int intValue = Integer.decode((String) value).intValue();
						if (intValue < 0)
							return MessagesForPreferences.GdbDebugPreferencePage_Timeout_value_can_not_be_negative;
					} catch (NumberFormatException e) {
						return MessagesForPreferences.GdbDebugPreferencePage_Invalid_timeout_value;
					}
				}
				return null;
			}
		};

		AdvancedTimeoutSettingsDialog(Shell parentShell, Set<Map.Entry<String, Integer>> entries) {
			super(parentShell);
			setShellStyle(getShellStyle() | SWT.RESIZE);
			fEntries = new LinkedList<>();
			for (Map.Entry<String, Integer> entry : entries) {
				fEntries.add(new CommandTimeoutEntry(entry.getKey(), entry.getValue()));
			}
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			getShell().setText(MessagesForPreferences.GdbDebugPreferencePage_Advanced_Timeout_Settings);
			setTitle(MessagesForPreferences.GdbDebugPreferencePage_Advanced_timeout_dialog_title);
			setTitleImage(GdbUIPlugin.getImage(IGdbUIConstants.IMG_WIZBAN_ADVANCED_TIMEOUT_SETTINGS));
			setMessage(MessagesForPreferences.GdbDebugPreferencePage_Advanced_timeout_dialog_message);

			Composite control = (Composite) super.createDialogArea(parent);
			Composite comp = new Composite(control, SWT.NONE);
			GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			GridLayout layout = new GridLayout(2, false);
			layout.marginLeft = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
			comp.setLayout(layout);
			comp.setLayoutData(gd);

			fViewer = new TableViewer(comp, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			final Table table = fViewer.getTable();
			gd = new GridData(SWT.FILL, SWT.FILL, true, true);
			gd.horizontalIndent = FieldDecorationRegistry.getDefault().getMaximumDecorationWidth();
			table.setLayoutData(gd);

			ControlDecoration decoration = new ControlDecoration(table, SWT.TOP | SWT.LEFT, control);
			decoration.setImage(FieldDecorationRegistry.getDefault()
					.getFieldDecoration(FieldDecorationRegistry.DEC_INFORMATION).getImage());
			decoration.setDescriptionText(
					MessagesForPreferences.GdbDebugPreferencePage_Advanced_timeout_settings_dialog_tooltip);
			fViewer.addDoubleClickListener(new IDoubleClickListener() {
				@Override
				public void doubleClick(DoubleClickEvent event) {
					okPressed();
				}
			});

			fViewer.addSelectionChangedListener(new ISelectionChangedListener() {

				@Override
				public void selectionChanged(SelectionChangedEvent event) {
					updateDialogButtons();
				}
			});

			Composite btnComp = new Composite(comp, SWT.NONE);
			btnComp.setLayout(new GridLayout());
			btnComp.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));

			fAddButton = new Button(btnComp, SWT.PUSH);
			fAddButton.setText(MessagesForPreferences.GdbDebugPreferencePage_Add_button);
			fAddButton.setFont(JFaceResources.getDialogFont());
			setButtonLayoutData(fAddButton);
			fAddButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					addNewEntry();
				}
			});

			fDeleteButton = new Button(btnComp, SWT.PUSH);
			fDeleteButton.setText(MessagesForPreferences.GdbDebugPreferencePage_Delete_button);
			fDeleteButton.setFont(JFaceResources.getDialogFont());
			setButtonLayoutData(fDeleteButton);
			fDeleteButton.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					deleteEntries();
				}
			});

			table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			table.setHeaderVisible(true);
			table.setLinesVisible(true);

			TableViewerColumn commandColumn = new TableViewerColumn(fViewer, SWT.LEFT);
			commandColumn.getColumn().setText(MessagesForPreferences.GdbDebugPreferencePage_Command_column_name);
			commandColumn.setLabelProvider(createCommandLabelProvider());
			commandColumn.setEditingSupport(createCommandEditingSupport(fViewer));

			TableViewerColumn timeoutColumn = new TableViewerColumn(fViewer, SWT.LEFT);
			timeoutColumn.getColumn().setText(MessagesForPreferences.GdbDebugPreferencePage_Timeout_column_name);
			timeoutColumn.setLabelProvider(createTimeoutLabelProvider());
			timeoutColumn.setEditingSupport(createTimeoutEditingSupport(fViewer));

			fViewer.setContentProvider(createCustomTimeoutsContentProvider());

			table.addControlListener(new ControlAdapter() {

				@Override
				public void controlResized(ControlEvent e) {
					Rectangle area = table.getClientArea();
					if (area.width > 0) {
						TableColumn[] cols = table.getColumns();
						cols[0].setWidth(area.width * 50 / 100);
						cols[1].setWidth(area.width * 50 / 100);
						table.removeControlListener(this);
					}
				}
			});

			fViewer.setInput(fEntries);

			updateDialogButtons();

			return control;
		}

		void updateDialogButtons() {
			if (fViewer != null && fDeleteButton != null) {
				fDeleteButton.setEnabled(!fViewer.getSelection().isEmpty());
			}
			Button okButton = getButton(IDialogConstants.OK_ID);
			if (okButton != null)
				okButton.setEnabled(getErrorMessage() == null);
		}

		void addNewEntry() {
			CommandTimeoutEntry newEntry = new CommandTimeoutEntry("", Integer.valueOf(0)); //$NON-NLS-1$
			fEntries.add(newEntry);
			fViewer.refresh();
			fViewer.setSelection(new StructuredSelection(newEntry));
			validateEntry(newEntry);
			updateDialogButtons();
			fViewer.editElement(newEntry, 0);
		}

		void deleteEntries() {
			IStructuredSelection sel = (IStructuredSelection) fViewer.getSelection();
			if (!sel.isEmpty())
				fEntries.removeAll(sel.toList());
			fViewer.refresh();
			validate();
			updateDialogButtons();
		}

		CustomTimeoutsMap getResult() {
			CustomTimeoutsMap map = new CustomTimeoutsMap();
			for (CommandTimeoutEntry entry : fEntries) {
				map.put(entry.fCommand, entry.fTimeout);
			}
			return map;
		}

		void validate() {
			for (CommandTimeoutEntry entry : fEntries) {
				validateEntry(entry);
			}
		}

		void validateEntry(CommandTimeoutEntry entry) {
			String errorMessage = fCommandValidator.isValid(entry.fCommand);
			setErrorMessage(
					(errorMessage != null) ? errorMessage : fTimeoutValidator.isValid(entry.fTimeout.toString()));
		}

		IStructuredContentProvider createCustomTimeoutsContentProvider() {
			return new IStructuredContentProvider() {

				@Override
				public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
				}

				@Override
				public void dispose() {
				}

				@Override
				public Object[] getElements(Object inputElement) {
					if (inputElement instanceof List<?>) {
						@SuppressWarnings("unchecked")
						List<CommandTimeoutEntry> list = (List<CommandTimeoutEntry>) inputElement;
						return list.toArray(new Object[list.size()]);
					}
					return null;
				}
			};
		}

		ColumnLabelProvider createCommandLabelProvider() {
			return new ColumnLabelProvider() {

				/* (non-Javadoc)
				 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
				 */
				@Override
				public String getText(Object element) {
					if (element instanceof CommandTimeoutEntry) {
						return ((CommandTimeoutEntry) element).fCommand;
					}
					return super.getText(element);
				}
			};
		}

		ColumnLabelProvider createTimeoutLabelProvider() {
			return new ColumnLabelProvider() {

				/* (non-Javadoc)
				 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
				 */
				@Override
				public String getText(Object element) {
					if (element instanceof CommandTimeoutEntry) {
						return ((CommandTimeoutEntry) element).fTimeout.toString();
					}
					return super.getText(element);
				}
			};
		}

		EditingSupport createCommandEditingSupport(ColumnViewer viewer) {
			return new AbstractEditingSupport(viewer) {

				@Override
				boolean processValue(CommandTimeoutEntry entry, String value) {
					entry.fCommand = value;
					return true;
				}

				@Override
				Object doGetValue(CommandTimeoutEntry entry) {
					return entry.fCommand;
				}

				@Override
				ICellEditorValidator getValidator() {
					return fCommandValidator;
				}
			};
		}

		EditingSupport createTimeoutEditingSupport(ColumnViewer viewer) {
			return new AbstractEditingSupport(viewer) {

				@Override
				boolean processValue(CommandTimeoutEntry entry, String value) {
					try {
						entry.fTimeout = Integer.decode(value);
						return true;
					} catch (NumberFormatException e) {
						// Shouldn't happen, validator takes care of this case.
					}
					return false;
				}

				@Override
				Object doGetValue(CommandTimeoutEntry entry) {
					return entry.fTimeout.toString();
				}

				@Override
				ICellEditorValidator getValidator() {
					return fTimeoutValidator;
				}
			};
		}
	}

	private IntegerWithBooleanFieldEditor fCommandTimeoutField;
	private Button fTimeoutAdvancedButton;

	private CustomTimeoutsMap fCustomTimeouts;

	public GdbDebugPreferencePage() {
		super(FLAT);
		IPreferenceStore store = GdbUIPlugin.getDefault().getPreferenceStore();
		setPreferenceStore(store);
		setDescription(MessagesForPreferences.GdbDebugPreferencePage_description);
		fCustomTimeouts = new CustomTimeoutsMap();
	}

	@Override
	public void init(IWorkbench workbench) {
	}

	@Override
	protected void initialize() {
		super.initialize();
		initializeCustomTimeouts();
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		updateTimeoutButtons();
		PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
				GdbUIPlugin.PLUGIN_ID + ".dsfgdb_preference_page"); //$NON-NLS-1$
	}

	@Override
	protected void createFieldEditors() {
		final Composite parent = getFieldEditorParent();
		final GridLayout layout = new GridLayout();
		layout.marginWidth = 0;
		parent.setLayout(layout);

		final Group group1 = new Group(parent, SWT.NONE);
		group1.setText(MessagesForPreferences.GdbDebugPreferencePage_defaults_label);
		GridLayout groupLayout = new GridLayout(3, false);
		group1.setLayout(groupLayout);
		group1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final StringFieldEditor stringFieldEditorCommand = new StringFieldEditor(
				IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_COMMAND,
				MessagesForPreferences.GdbDebugPreferencePage_GDB_debugger, group1);

		stringFieldEditorCommand.fillIntoGrid(group1, 2);
		GridData stringFieldLayoutData = (GridData) stringFieldEditorCommand.getTextControl(group1).getLayoutData();
		stringFieldLayoutData.widthHint = DEFAULT_GDB_COMMAND_LABEL_WIDTH_HINT;

		addField(stringFieldEditorCommand);
		Button browsebutton = new Button(group1, SWT.PUSH);
		browsebutton.setText(MessagesForPreferences.GdbDebugPreferencePage_Browse_button);
		browsebutton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowseButtonSelected(MessagesForPreferences.GdbDebugPreferencePage_GDB_debugger_dialog_title,
						stringFieldEditorCommand);
			}
		});
		setButtonLayoutData(browsebutton);

		final StringFieldEditor stringFieldEditorGdbInit = new StringFieldEditor(
				IGdbDebugPreferenceConstants.PREF_DEFAULT_GDB_INIT,
				MessagesForPreferences.GdbDebugPreferencePage_GDB_command_file, group1);

		stringFieldEditorGdbInit.fillIntoGrid(group1, 2);
		addField(stringFieldEditorGdbInit);
		browsebutton = new Button(group1, SWT.PUSH);
		browsebutton.setText(MessagesForPreferences.GdbDebugPreferencePage_Browse_button);
		browsebutton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowseButtonSelected(MessagesForPreferences.GdbDebugPreferencePage_GDB_command_file_dialog_title,
						stringFieldEditorGdbInit);
			}
		});
		setButtonLayoutData(browsebutton);

		final StringWithBooleanFieldEditor enableStopAtMain = new StringWithBooleanFieldEditor(
				IGdbDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN,
				IGdbDebugPreferenceConstants.PREF_DEFAULT_STOP_AT_MAIN_SYMBOL,
				MessagesForPreferences.GdbDebugPreferencePage_Stop_on_startup_at, group1);
		enableStopAtMain.fillIntoGrid(group1, 3);
		addField(enableStopAtMain);

		fCommandTimeoutField = new IntegerWithBooleanFieldEditor(IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT,
				IGdbDebugPreferenceConstants.PREF_COMMAND_TIMEOUT_VALUE,
				MessagesForPreferences.GdbDebugPreferencePage_Command_timeout, group1);
		fCommandTimeoutField.setValidRange(0, Integer.MAX_VALUE);
		fCommandTimeoutField.fillIntoGrid(group1, 2);
		addField(fCommandTimeoutField);

		fTimeoutAdvancedButton = new Button(group1, SWT.PUSH);
		fTimeoutAdvancedButton.setText(MessagesForPreferences.GdbDebugPreferencePage_Advanced_button);
		fTimeoutAdvancedButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleAdvancedButtonSelected(MessagesForPreferences.GdbDebugPreferencePage_GDB_debugger_dialog_title);
			}
		});
		setButtonLayoutData(fTimeoutAdvancedButton);

		final ListenableBooleanFieldEditor enableNonStop = new ListenableBooleanFieldEditor(
				IGdbDebugPreferenceConstants.PREF_DEFAULT_NON_STOP,
				MessagesForPreferences.GdbDebugPreferencePage_Non_stop_mode, SWT.NONE, group1);
		enableNonStop.fillIntoGrid(group1, 3);
		addField(enableNonStop);

		if (Platform.getOS().startsWith("win")) { //$NON-NLS-1$
			BooleanFieldEditor externalConsoleField = new BooleanFieldEditor(
					IGdbDebugPreferenceConstants.PREF_EXTERNAL_CONSOLE,
					MessagesForPreferences.GdbDebugPreferencePage_external_console, group1);

			externalConsoleField.fillIntoGrid(group1, 3);
			addField(externalConsoleField);
		}

		final StringWithBooleanFieldEditor remoteTimeout = new StringWithBooleanFieldEditor(
				IGdbDebugPreferenceConstants.PREF_DEFAULT_REMOTE_TIMEOUT_ENABLED,
				IGdbDebugPreferenceConstants.PREF_DEFAULT_REMOTE_TIMEOUT_VALUE,
				MessagesForPreferences.GdbDebugPreferencePage_remoteTimeout_label, group1);
		remoteTimeout.getCheckboxControl(group1)
				.setToolTipText(MessagesForPreferences.GdbDebugPreferencePage_remoteTimeout_tooltip);
		remoteTimeout.getTextControl(group1)
				.setToolTipText(MessagesForPreferences.GdbDebugPreferencePage_remoteTimeout_tooltip);
		remoteTimeout.fillIntoGrid(group1, 3);
		addField(remoteTimeout);

		group1.setLayout(groupLayout);

		final Group group2 = new Group(parent, SWT.NONE);
		group2.setText(MessagesForPreferences.GdbDebugPreferencePage_general_behavior_label);
		groupLayout = new GridLayout(3, false);
		group2.setLayout(groupLayout);
		group2.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		BooleanFieldEditor boolField = new BooleanFieldEditor(IGdbDebugPreferenceConstants.PREF_AUTO_TERMINATE_GDB,
				MessagesForPreferences.GdbDebugPreferencePage_autoTerminateGdb_label, group2);

		boolField.fillIntoGrid(group2, 3);
		addField(boolField);
		// Need to set layout again.
		group2.setLayout(groupLayout);

		boolField = new BooleanFieldEditor(IGdbDebugPreferenceConstants.PREF_USE_INSPECTOR_HOVER,
				MessagesForPreferences.GdbDebugPreferencePage_useInspectorHover_label, group2);

		boolField.fillIntoGrid(group2, 3);
		addField(boolField);
		// need to set layout again
		group2.setLayout(groupLayout);

		boolField = new BooleanFieldEditor(IGdbDebugPreferenceConstants.PREF_HIDE_RUNNING_THREADS,
				MessagesForPreferences.GdbDebugPreferencePage_hideRunningThreads, group2);

		boolField.fillIntoGrid(group2, 3);
		addField(boolField);
		// Need to set layout again.
		group2.setLayout(groupLayout);

		boolField = new BooleanFieldEditor(IGdbDebugPreferenceConstants.PREF_AGGRESSIVE_BP_FILTER,
				MessagesForPreferences.GdbDebugPreferencePage_useAggressiveBpFilter, group2);

		boolField.fillIntoGrid(group2, 3);
		addField(boolField);
		// Need to set layout again.
		group2.setLayout(groupLayout);

		boolField = new BooleanFieldEditor(IGdbDebugPreferenceConstants.PREF_TRACES_ENABLE,
				MessagesForPreferences.GdbDebugPreferencePage_enableTraces_label, group2);

		boolField.fillIntoGrid(group2, 1);
		addField(boolField);
		group2.setLayout(groupLayout);

		// The field below sets the size of the buffer for the gdb traces.
		// It is located to the right of the previous field (which shows or not the
		// gdb traces) and uses its label.  However, we don't want to tightly
		// couple the two fields using IntegerWithBooleanFieldEditor because
		// we want the gdb traces limit to stay enabled even when the gdb traces
		// are not actually shown (when the above preference is not selected).
		// The reason is that since the gdb traces record even when they are not
		// shown, we want the user to be able to set the limit on those consoles,
		// even if they are not currently shown.
		final IntegerFieldEditor gdbTracesLimit = new IntegerFieldEditor(
				IGdbDebugPreferenceConstants.PREF_MAX_GDB_TRACES, "", // Empty title as we reuse the string of the previous field //$NON-NLS-1$
				group2);

		// Instead of using Integer.MAX_VALUE which is some obscure number,
		// using 2 billion is nice and readable.
		gdbTracesLimit.setValidRange(10000, 2000000000);
		gdbTracesLimit.fillIntoGrid(group2, 2);
		addField(gdbTracesLimit);
		// Need to set layout again.
		group2.setLayout(groupLayout);

		// The field below sets the number of lines a message can be.
		final StringWithBooleanFieldEditor gdbMaxLines = new StringWithBooleanFieldEditor(
				IGdbDebugPreferenceConstants.PREF_MAX_MI_OUTPUT_LINES_ENABLE,
				IGdbDebugPreferenceConstants.PREF_MAX_MI_OUTPUT_LINES,
				MessagesForPreferences.GdbDebugPreferencePage_enableMaxMessageLines_label, group2);
		gdbMaxLines.fillIntoGrid(group2, 3);
		addField(gdbMaxLines);
		// Need to set layout again.
		group2.setLayout(groupLayout);

		boolField = new BooleanFieldEditor(IGdbDebugPreferenceConstants.PREF_USE_RTTI,
				MessagesForPreferences.GdbDebugPreferencePage_use_rtti_label1 + " \n" //$NON-NLS-1$
						+ MessagesForPreferences.GdbDebugPreferencePage_use_rtti_label2,
				group2);

		boolField.fillIntoGrid(group2, 3);
		addField(boolField);
		// need to set layout again
		group2.setLayout(groupLayout);

		Group group = new Group(parent, SWT.NONE);
		group.setText(MessagesForPreferences.GdbDebugPreferencePage_prettyPrinting_label);
		groupLayout = new GridLayout(3, false);
		group.setLayout(groupLayout);
		group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		final ListenableBooleanFieldEditor enablePrettyPrintingField = new ListenableBooleanFieldEditor(
				IGdbDebugPreferenceConstants.PREF_ENABLE_PRETTY_PRINTING,
				MessagesForPreferences.GdbDebugPreferencePage_enablePrettyPrinting_label1 + " \n" //$NON-NLS-1$
						+ MessagesForPreferences.GdbDebugPreferencePage_enablePrettyPrinting_label2,
				SWT.NONE, group);

		enablePrettyPrintingField.fillIntoGrid(group, 3);
		addField(enablePrettyPrintingField);

		final Composite indentHelper = new Composite(group, SWT.NONE);
		GridLayout helperLayout = new GridLayout(3, false);
		indentHelper.setLayout(helperLayout);
		GridData helperData = new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1);
		helperData.horizontalIndent = 20;
		indentHelper.setLayoutData(helperData);

		final IntegerFieldEditor childCountLimitField = new IntegerFieldEditor(
				IGdbDebugPreferenceConstants.PREF_INITIAL_CHILD_COUNT_LIMIT_FOR_COLLECTIONS,
				MessagesForPreferences.GdbDebugPreferencePage_initialChildCountLimitForCollections_label, indentHelper);

		childCountLimitField.setValidRange(1, 10000);
		childCountLimitField.fillIntoGrid(indentHelper, 3);

		boolean prettyPrintingEnabled = getPreferenceStore()
				.getBoolean(IGdbDebugPreferenceConstants.PREF_ENABLE_PRETTY_PRINTING);
		childCountLimitField.setEnabled(prettyPrintingEnabled, indentHelper);

		addField(childCountLimitField);

		enablePrettyPrintingField.getChangeControl(group).addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean enabled = enablePrettyPrintingField.getBooleanValue();
				childCountLimitField.setEnabled(enabled, indentHelper);
			}
		});

		// need to set layouts again
		indentHelper.setLayout(helperLayout);
		group.setLayout(groupLayout);
	}

	private void handleBrowseButtonSelected(final String dialogTitle, final StringFieldEditor stringFieldEditor) {
		FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
		dialog.setText(dialogTitle);
		String gdbCommand = stringFieldEditor.getStringValue().trim();
		int lastSeparatorIndex = gdbCommand.lastIndexOf(File.separator);
		if (lastSeparatorIndex != -1) {
			dialog.setFilterPath(gdbCommand.substring(0, lastSeparatorIndex));
		}
		String res = dialog.open();
		if (res == null) {
			return;
		}
		stringFieldEditor.setStringValue(res);
	}

	private void handleAdvancedButtonSelected(String dialogTitle) {
		AdvancedTimeoutSettingsDialog dialog = new AdvancedTimeoutSettingsDialog(getShell(),
				fCustomTimeouts.entrySet());
		if (dialog.open() == Window.OK) {
			fCustomTimeouts = dialog.getResult();
		}
	}

	@Override
	protected void adjustGridLayout() {
		// do nothing
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (event.getSource().equals(fCommandTimeoutField) && event.getNewValue() instanceof Boolean) {
			fTimeoutAdvancedButton.setEnabled(((Boolean) event.getNewValue()).booleanValue());
		}
		super.propertyChange(event);
	}

	@Override
	protected void performDefaults() {
		IPreferenceStore store = getPreferenceStore();
		if (store != null) {
			String memento = store.getDefaultString(IGdbDebugPreferenceConstants.PREF_COMMAND_CUSTOM_TIMEOUTS);
			fCustomTimeouts.initializeFromMemento(memento);
		}
		super.performDefaults();
		updateTimeoutButtons();
	}

	@Override
	public boolean performOk() {
		getPreferenceStore().setValue(IGdbDebugPreferenceConstants.PREF_COMMAND_CUSTOM_TIMEOUTS,
				fCustomTimeouts.getMemento());
		return super.performOk();
	}

	private void updateTimeoutButtons() {
		fTimeoutAdvancedButton.setEnabled(fCommandTimeoutField.getBooleanValue());
	}

	private void initializeCustomTimeouts() {
		IPreferenceStore store = getPreferenceStore();
		if (store != null) {
			String memento = store.getString(IGdbDebugPreferenceConstants.PREF_COMMAND_CUSTOM_TIMEOUTS);
			fCustomTimeouts.initializeFromMemento(memento);
		}
	}
}
