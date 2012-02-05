/*******************************************************************************
 * Copyright (c) 2000, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.contentassist.SubjectControlContentAssistant;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.contentassist.ContentAssistHandler;

import org.eclipse.cdt.internal.corext.codemanipulation.StubUtility;

import org.eclipse.cdt.internal.ui.dialogs.TableTextCellEditor;
import org.eclipse.cdt.internal.ui.dialogs.TextFieldNavigationHandler;
import org.eclipse.cdt.internal.ui.util.SWTUtil;
import org.eclipse.cdt.internal.ui.util.TableLayoutComposite;

/**
 * A special control to edit and reorder method parameters.
 */
public class ChangeParametersControl extends Composite {

	public static enum Mode {
		EXTRACT_METHOD, EXTRACT_METHOD_FIXED_RETURN, CHANGE_METHOD_SIGNATURE, INTRODUCE_PARAMETER;
		
		public boolean canChangeTypes() {
			return this == CHANGE_METHOD_SIGNATURE;
		}

		public boolean canAddParameters() {
			return this == Mode.CHANGE_METHOD_SIGNATURE;
		}

		public boolean canChangeDefault() {
			return this == Mode.CHANGE_METHOD_SIGNATURE;
		}

		public boolean shouldShowDirection() {
			return this == Mode.EXTRACT_METHOD || this == Mode.EXTRACT_METHOD_FIXED_RETURN;
		}

		public boolean canChangeReturn() {
			return this == Mode.EXTRACT_METHOD;
		}
	}

	private static class NameInformationContentProvider implements IStructuredContentProvider {
		@Override
		@SuppressWarnings("unchecked")
		public Object[] getElements(Object inputElement) {
			return removeMarkedAsDeleted((List<NameInformation>) inputElement);
		}

		private NameInformation[] removeMarkedAsDeleted(List<NameInformation> params) {
			List<NameInformation> result= new ArrayList<NameInformation>(params.size());
			for (Iterator<NameInformation> iter= params.iterator(); iter.hasNext();) {
				NameInformation info= iter.next();
				if (!info.isDeleted())
					result.add(info);
			}
			return result.toArray(new NameInformation[result.size()]);
		}

		@Override
		public void dispose() {
			// do nothing
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// do nothing
		}
	}

	private class NameInformationLabelProvider extends LabelProvider
			implements ITableLabelProvider, ITableFontProvider {
		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			NameInformation info= (NameInformation) element;
			if (columnIndex == indexType) {
				return info.getTypeName();
			} else if (columnIndex == indexDirection) {
				return getDirectionLabel(info);
			} else if (columnIndex == indexName) {
				return info.getNewName();
			} else if (columnIndex == indexDefault) {
			    if (info.isAdded()) {
			        return info.getDefaultValue();
			    } else {
			        return "-"; //$NON-NLS-1$
			    }
			} else {
				throw new IllegalArgumentException(columnIndex + ": " + element); //$NON-NLS-1$
			}
		}

		@Override
		public Font getFont(Object element, int columnIndex) {
			NameInformation info= (NameInformation) element;
			if (info.isAdded()) {
				return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
			}
			return null;
		}
	}

	private class ParametersCellModifier implements ICellModifier {
		@Override
		public boolean canModify(Object element, String property) {
			Assert.isTrue(element instanceof NameInformation);
			if (property.equals(columnProperties[indexType])) {
				return fMode.canChangeTypes();
			} else if (property.equals(columnProperties[indexDirection])) {
				return fMode.canChangeReturn() && ((NameInformation) element).isOutput();
			} else if (property.equals(columnProperties[indexName])) {
				return true;
			} else if (property.equals(columnProperties[indexDefault])) {
				return ((NameInformation) element).isAdded();
			}
			Assert.isTrue(false);
			return false;
		}

		@Override
		public Object getValue(Object element, String property) {
			Assert.isTrue(element instanceof NameInformation);
			if (property.equals(columnProperties[indexType])) {
				return ((NameInformation) element).getTypeName();
			} else if (property.equals(columnProperties[indexDirection])) {
				return ((NameInformation) element).isReturnValue() ? INDEX_RETURN : INDEX_OUTPUT;
			} else if (property.equals(columnProperties[indexName])) {
				return ((NameInformation) element).getNewName();
			} else if (property.equals(columnProperties[indexDefault])) {
				return ((NameInformation) element).getDefaultValue();
			}
			Assert.isTrue(false);
			return null;
		}

		@Override
		public void modify(Object element, String property, Object value) {
			if (element instanceof TableItem)
				element= ((TableItem) element).getData();
			if (!(element instanceof NameInformation))
				return;
			String[] columnsToUpdate = new String[] { property };
			boolean unchanged;
			NameInformation parameterInfo= (NameInformation) element;
			if (property.equals(columnProperties[indexType])) {
				unchanged= parameterInfo.getTypeName().equals(value);
				parameterInfo.setTypeName((String) value);
			} else if (property.equals(columnProperties[indexDirection])) {
				columnsToUpdate = new String[] { property, columnProperties[indexType] };
				boolean isReturn = value.equals(INDEX_RETURN);
				unchanged= isReturn == parameterInfo.isReturnValue();
				if (!unchanged && isReturn) {
					for (NameInformation param : fParameters) {
						if (param != parameterInfo && param.isOutput()) {
							param.setReturnValue(false);
							ChangeParametersControl.this.fListener.parameterChanged(param);
							ChangeParametersControl.this.fTableViewer.update(param, columnsToUpdate);
							break;
						}
					}
				}
				parameterInfo.setReturnValue(isReturn);
			} else if (property.equals(columnProperties[indexName])) {
				unchanged= parameterInfo.getNewName().equals(value);
				parameterInfo.setNewName((String) value);
			} else if (property.equals(columnProperties[indexDefault])) {
				unchanged= parameterInfo.getDefaultValue().equals(value);
				parameterInfo.setDefaultValue((String) value);
			} else {
				throw new IllegalStateException();
			}
			if (!unchanged) {
				ChangeParametersControl.this.fListener.parameterChanged(parameterInfo);
				ChangeParametersControl.this.fTableViewer.update(parameterInfo, columnsToUpdate);
			}
		}
	}

	private static class DirectionCellEditor extends ComboBoxCellEditor {
		DirectionCellEditor(Table table) {
			super(table,
					new String[] {
						/* INDEX_OUTPUT */ Messages.ChangeParametersControl_output,
						/* INDEX_RETURN */ Messages.ChangeParametersControl_return},
					SWT.READ_ONLY);
		}

		@Override
		protected Control createControl(Composite parent) {
			final CCombo comboBox = (CCombo) super.createControl(parent);
			comboBox.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					fireApplyEditorValue();
				}
			});
			return comboBox;
		}
	}

	private final String[] columnProperties;
	private final int indexType;
	private final int indexDirection;
	private final int indexName;
	private final int indexDefault;

	private static final int ROW_COUNT= 7;

	static final Integer INDEX_OUTPUT = 0;
    static final Integer INDEX_RETURN = 1;

	private final Mode fMode;
	private final IParameterListChangeListener fListener;
	private List<NameInformation> fParameters;
	private final StubTypeContext fTypeContext;
	private final String[] fParamNameProposals;
	private ContentAssistHandler fNameContentAssistHandler;

	private TableViewer fTableViewer;
	private Button fUpButton;
	private Button fDownButton;
	private Button fEditButton;
	private Button fAddButton;
	private Button fRemoveButton;

	public ChangeParametersControl(Composite parent, int style, String label,
			IParameterListChangeListener listener, Mode mode, StubTypeContext typeContext) {
		this(parent, style, label, listener, mode, typeContext, new String[0]);
	}

	public ChangeParametersControl(Composite parent, int style, String label,
			IParameterListChangeListener listener, Mode mode) {
		this(parent, style, label, listener, mode, null, new String[0]);
	}

	public ChangeParametersControl(Composite parent, int style, String label,
			IParameterListChangeListener listener, Mode mode, String[] paramNameProposals) {
		this(parent, style, label, listener, mode, null, paramNameProposals);
	}

	/**
	 * @param label the label before the table or <code>null</code>
	 * @param typeContext the package in which to complete types
	 */
	private ChangeParametersControl(Composite parent, int style, String label,
			IParameterListChangeListener listener, Mode mode, StubTypeContext typeContext,
			String[] paramNameProposals) {
		super(parent, style);
		Assert.isNotNull(listener);
		fListener= listener;
		fMode= mode;
		fTypeContext= typeContext;
		fParamNameProposals= paramNameProposals;

		ArrayList<String> properties = new ArrayList<String>();
		indexType = properties.size();
		properties.add("type"); //$NON-NLS-1$

		if (fMode.shouldShowDirection()) {
			indexDirection = properties.size();
			properties.add("direction"); //$NON-NLS-1$
		} else {
			indexDirection = -1;
		}

		indexName = properties.size();
		properties.add("name"); //$NON-NLS-1$

		if (fMode.canChangeDefault()) {
			indexDefault = properties.size();
			properties.add("default"); //$NON-NLS-1$
		} else {
			indexDefault = -1;
		}
		columnProperties = properties.toArray(new String[properties.size()]);
		
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		setLayout(layout);

		if (label != null) {
			Label tableLabel= new Label(this, SWT.NONE);
			GridData labelGd= new GridData();
			labelGd.horizontalSpan= 2;
			tableLabel.setLayoutData(labelGd);
			tableLabel.setText(label);
		}

		createParameterList(this);
		createButtonComposite(this);
	}

	public void setInput(List<NameInformation> parameterInfos) {
		Assert.isNotNull(parameterInfos);
		fParameters= parameterInfos;
		fTableViewer.setInput(fParameters);
		if (fParameters.size() > 0)
			fTableViewer.setSelection(new StructuredSelection(fParameters.get(0)));
	}

	public void editParameter(NameInformation info) {
		fTableViewer.getControl().setFocus();
		if (!info.isDeleted()) {
			fTableViewer.setSelection(new StructuredSelection(info), true);
			updateButtonsEnabledState();
			editColumnOrNextPossible(indexName);
			return;
		}
	}

	// ---- Parameter table -----------------------------------------------------------------------------------

	private void createParameterList(Composite parent) {
		TableLayoutComposite layouter= new TableLayoutComposite(parent, SWT.NONE);
		addColumnLayoutData(layouter);

		final Table table= new Table(layouter, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		TableColumn tc;
		tc= new TableColumn(table, SWT.NONE, indexType);
		tc.setResizable(true);
		tc.setText(Messages.ChangeParametersControl_table_type);

		if (indexDirection >= 0) {
			tc= new TableColumn(table, SWT.NONE, indexDirection);
			tc.setResizable(true);
			tc.setText(Messages.ChangeParametersControl_table_direction);
		}

		tc= new TableColumn(table, SWT.NONE, indexName);
		tc.setResizable(true);
		tc.setText(Messages.ChangeParametersControl_table_name);

		if (indexDefault >= 0) {
			tc= new TableColumn(table, SWT.NONE, indexDefault);
			tc.setResizable(true);
			tc.setText(Messages.ChangeParametersControl_table_default_value);
		}

		GridData gd= new GridData(GridData.FILL_BOTH);
		gd.heightHint= SWTUtil.getTableHeightHint(table, ROW_COUNT);
		gd.widthHint= 40;
		layouter.setLayoutData(gd);

		fTableViewer= new TableViewer(table);
		fTableViewer.setUseHashlookup(true);
		fTableViewer.setContentProvider(new NameInformationContentProvider());
		fTableViewer.setLabelProvider(new NameInformationLabelProvider());
		fTableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtonsEnabledState();
			}
		});

		table.addTraverseListener(new TraverseListener() {
			@Override
			public void keyTraversed(TraverseEvent e) {
				if (e.detail == SWT.TRAVERSE_RETURN && e.stateMask == SWT.NONE) {
					editColumnOrNextPossible(0);
					e.detail= SWT.TRAVERSE_NONE;
				}
			}
		});
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.F2 && e.stateMask == SWT.NONE) {
					editColumnOrNextPossible(0);
					e.doit= false;
				}
			}
		});

		addCellEditors();
	}

	private static String getDirectionLabel(NameInformation parameter) {
		return parameter.isReturnValue() ?
					Messages.ChangeParametersControl_return :
			   parameter.isOutput() ?
					Messages.ChangeParametersControl_output :
					Messages.ChangeParametersControl_input;
	}

	private void editColumnOrNextPossible(int column) {
		NameInformation[] selected= getSelectedElements();
		if (selected.length != 1)
			return;
		int nextColumn= column;
		do {
			fTableViewer.editElement(selected[0], nextColumn);
			if (fTableViewer.isCellEditorActive())
				return;
			nextColumn= nextColumn(nextColumn);
		} while (nextColumn != column);
	}

	private void editColumnOrPrevPossible(int column) {
		NameInformation[] selected= getSelectedElements();
		if (selected.length != 1)
			return;
		int prevColumn= column;
		do {
			fTableViewer.editElement(selected[0], prevColumn);
			if (fTableViewer.isCellEditorActive())
			    return;
			prevColumn= prevColumn(prevColumn);
		} while (prevColumn != column);
	}

	private int nextColumn(int column) {
		return column >= getTable().getColumnCount() - 1 ? 0 : column + 1;
	}

	private int prevColumn(int column) {
		return column <= 0 ? getTable().getColumnCount() - 1 : column - 1;
	}

	private void addColumnLayoutData(TableLayoutComposite layouter) {
		for (int i = 0; i < columnProperties.length; i++) {
			layouter.addColumnData(new ColumnWeightData(10, true));
		}
	}

	private NameInformation[] getSelectedElements() {
		ISelection selection= fTableViewer.getSelection();
		if (selection == null)
			return new NameInformation[0];

		if (!(selection instanceof IStructuredSelection))
			return new NameInformation[0];

		List<?> selected= ((IStructuredSelection) selection).toList();
		return selected.toArray(new NameInformation[selected.size()]);
	}

	// ---- Button bar --------------------------------------------------------------------------------------

	private void createButtonComposite(Composite parent) {
		Composite buttonComposite= new Composite(parent, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		GridLayout gl= new GridLayout();
		gl.marginHeight= 0;
		gl.marginWidth= 0;
		buttonComposite.setLayout(gl);

		if (fMode.canAddParameters())
			fAddButton= createAddButton(buttonComposite);

		fEditButton= createEditButton(buttonComposite);

		if (fMode.canAddParameters())
			fRemoveButton= createRemoveButton(buttonComposite);

		if (buttonComposite.getChildren().length != 0)
			addSpacer(buttonComposite);

		fUpButton= createButton(buttonComposite, Messages.ChangeParametersControl_buttons_move_up, true);
		fDownButton= createButton(buttonComposite, Messages.ChangeParametersControl_buttons_move_down, false);

		updateButtonsEnabledState();
	}

	private void addSpacer(Composite parent) {
		Label label= new Label(parent, SWT.NONE);
		GridData gd= new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint= 5;
		label.setLayoutData(gd);
	}

	private void updateButtonsEnabledState() {
		fUpButton.setEnabled(canMove(true));
		fDownButton.setEnabled(canMove(false));
		if (fEditButton != null)
			fEditButton.setEnabled(getTableSelectionCount() == 1);
		if (fAddButton != null)
			fAddButton.setEnabled(true);
		if (fRemoveButton != null)
			fRemoveButton.setEnabled(getTableSelectionCount() != 0);
	}

	private int getTableSelectionCount() {
		return getTable().getSelectionCount();
	}

	private int getTableItemCount() {
		return getTable().getItemCount();
	}

	private Table getTable() {
		return fTableViewer.getTable();
	}

	private Button createEditButton(Composite buttonComposite) {
		Button button= new Button(buttonComposite, SWT.PUSH);
		button.setText(Messages.ChangeParametersControl_buttons_edit);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		SWTUtil.setButtonDimensionHint(button);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					NameInformation[] selected= getSelectedElements();
					Assert.isTrue(selected.length == 1);
					NameInformation parameterInfo= selected[0];
					ParameterEditDialog dialog= new ParameterEditDialog(getShell(), parameterInfo,
							fMode.canChangeTypes(), fMode.canChangeDefault(),
							fMode.canChangeReturn() && parameterInfo.isOutput());
					dialog.open();
					fListener.parameterChanged(parameterInfo);
					fTableViewer.update(parameterInfo, columnProperties);
				} finally {
					fTableViewer.getControl().setFocus();
				}
			}
		});
		return button;
	}

	private Button createAddButton(Composite buttonComposite) {
		Button button= new Button(buttonComposite, SWT.PUSH);
		button.setText(Messages.ChangeParametersControl_buttons_add);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		SWTUtil.setButtonDimensionHint(button);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Set<String> excludedParamNames= new HashSet<String>(fParameters.size());
				for (int i= 0; i < fParameters.size(); i++) {
					NameInformation info= fParameters.get(i);
					excludedParamNames.add(info.getNewName());
				}
				String newParamName= StubUtility.suggestParameterName("newParam", excludedParamNames, //$NON-NLS-1$
						fTypeContext != null ? fTypeContext.getTranslationUnit() : null);
				NameInformation newInfo= NameInformation.createInfoForAddedParameter("int", newParamName, "0"); //$NON-NLS-1$ //$NON-NLS-2$
				int insertIndex= fParameters.size();
				fParameters.add(insertIndex, newInfo);
				fListener.parameterAdded(newInfo);
				fTableViewer.refresh();
				fTableViewer.getControl().setFocus();
				fTableViewer.setSelection(new StructuredSelection(newInfo), true);
				updateButtonsEnabledState();
				editColumnOrNextPossible(0);
			}
		});
		return button;
	}

	private Button createRemoveButton(Composite buttonComposite) {
		final Button button= new Button(buttonComposite, SWT.PUSH);
		button.setText(Messages.ChangeParametersControl_buttons_remove);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		SWTUtil.setButtonDimensionHint(button);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index= getTable().getSelectionIndices()[0];
				NameInformation[] selected= getSelectedElements();
				for (int i= 0; i < selected.length; i++) {
					if (selected[i].isAdded()) {
						fParameters.remove(selected[i]);
					} else {
						selected[i].markAsDeleted();
					}
				}
				restoreSelection(index);
			}

			private void restoreSelection(int index) {
				fTableViewer.refresh();
				fTableViewer.getControl().setFocus();
				int itemCount= getTableItemCount();
				if (itemCount != 0) {
				    if (index >= itemCount)
				        index= itemCount - 1;
				    getTable().setSelection(index);
				}
				fListener.parameterListChanged();
				updateButtonsEnabledState();
			}
		});
		return button;
	}

	private Button createButton(Composite buttonComposite, String text, final boolean up) {
		Button button= new Button(buttonComposite, SWT.PUSH);
		button.setText(text);
		button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		SWTUtil.setButtonDimensionHint(button);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ISelection savedSelection= fTableViewer.getSelection();
				if (savedSelection == null)
					return;
				NameInformation[] selection= getSelectedElements();
				if (selection.length == 0)
					return;

				if (up) {
					moveUp(selection);
				} else {
					moveDown(selection);
				}
				fTableViewer.refresh();
				fTableViewer.setSelection(savedSelection);
				fListener.parameterListChanged();
				fTableViewer.getControl().setFocus();
			}
		});
		return button;
	}

	//---- editing -----------------------------------------------------------------------------------------------

	private void addCellEditors() {
		fTableViewer.setColumnProperties(columnProperties);

		ArrayList<CellEditor> editors = new ArrayList<CellEditor>();
		TableTextCellEditor cellEditorType= new TableTextCellEditor(fTableViewer, indexType);
		editors.add(cellEditorType);
		if (indexDirection >= 0) {
			ComboBoxCellEditor cellEditorDirection= new DirectionCellEditor(fTableViewer.getTable());
			editors.add(cellEditorDirection);
		}
		TableTextCellEditor cellEditorName= new TableTextCellEditor(fTableViewer, indexName);
		editors.add(cellEditorName);
		if (indexDefault >= 0) {
			TableTextCellEditor cellEditorDefault= new TableTextCellEditor(fTableViewer, indexDefault);
			editors.add(cellEditorDefault);
		}

		if (fParamNameProposals.length > 0) {
			SubjectControlContentAssistant assistant= installParameterNameContentAssist(cellEditorName.getText());
			cellEditorName.setContentAssistant(assistant);
		}

		for (int i = 0; i < editors.size(); i++) {
			final int editorColumn= i;
			final CellEditor editor = editors.get(i);
			// Support tabbing between columns while editing
			Control control = editor.getControl();
			control.addTraverseListener(new TraverseListener() {
				@Override
				public void keyTraversed(TraverseEvent e) {
					switch (e.detail) {
					case SWT.TRAVERSE_TAB_NEXT:
						editColumnOrNextPossible(nextColumn(editorColumn));
						e.detail= SWT.TRAVERSE_NONE;
						break;

					case SWT.TRAVERSE_TAB_PREVIOUS:
						editColumnOrPrevPossible(prevColumn(editorColumn));
						e.detail= SWT.TRAVERSE_NONE;
						break;
					}
				}
			});
			if (control instanceof Text) {
				TextFieldNavigationHandler.install((Text) control);
			}
		}

		cellEditorName.setActivationListener(new TableTextCellEditor.IActivationListener() {
			@Override
			public void activate() {
				NameInformation[] selected= getSelectedElements();
				if (selected.length == 1 && fNameContentAssistHandler != null) {
					fNameContentAssistHandler.setEnabled(selected[0].isAdded());
				}
			}
		});

		fTableViewer.setCellEditors(editors.toArray(new CellEditor[editors.size()]));
		fTableViewer.setCellModifier(new ParametersCellModifier());
	}

	//---- change order ----------------------------------------------------------------------------------------

	private void moveUp(NameInformation[] selection) {
		moveUp(fParameters, Arrays.asList(selection));
	}

	private void moveDown(NameInformation[] selection) {
		Collections.reverse(fParameters);
		moveUp(fParameters, Arrays.asList(selection));
		Collections.reverse(fParameters);
	}

	private static void moveUp(List<NameInformation> elements, List<NameInformation> move) {
		List<NameInformation> res= new ArrayList<NameInformation>(elements.size());
		List<NameInformation> deleted= new ArrayList<NameInformation>();
		NameInformation floating= null;
		for (Iterator<NameInformation> iter= elements.iterator(); iter.hasNext();) {
			NameInformation curr= iter.next();
			if (move.contains(curr)) {
				res.add(curr);
			} else if (curr.isDeleted()) {
				deleted.add(curr);
			} else {
				if (floating != null)
					res.add(floating);
				floating= curr;
			}
		}
		if (floating != null) {
			res.add(floating);
		}
		res.addAll(deleted);
		elements.clear();
		for (Iterator<NameInformation> iter= res.iterator(); iter.hasNext();) {
			elements.add(iter.next());
		}
	}

	private boolean canMove(boolean up) {
		int notDeletedInfosCount= getNotDeletedInfosCount();
		if (notDeletedInfosCount == 0)
			return false;
		int[] indc= getTable().getSelectionIndices();
		if (indc.length == 0)
			return false;
		int invalid= up ? 0 : notDeletedInfosCount - 1;
		for (int i= 0; i < indc.length; i++) {
			if (indc[i] == invalid)
				return false;
		}
		return true;
	}

	private int getNotDeletedInfosCount() {
		if (fParameters == null) // during initialization
			return 0;
		int result= 0;
		for (Iterator<NameInformation> iter= fParameters.iterator(); iter.hasNext();) {
			NameInformation info= iter.next();
			if (!info.isDeleted())
				result++;
		}
		return result;
	}

	private SubjectControlContentAssistant installParameterNameContentAssist(Text text) {
		return null;
		// TODO(sprigogin): Implement to support parameter name content assist. 
//		VariableNamesProcessor processor= new VariableNamesProcessor(fParamNameProposals);
//		SubjectControlContentAssistant contentAssistant= ControlContentAssistHelper.createCContentAssistant(processor);
//		fNameContentAssistHandler= ContentAssistHandler.createHandlerForText(text, contentAssistant);
//		return contentAssistant;
	}
}
