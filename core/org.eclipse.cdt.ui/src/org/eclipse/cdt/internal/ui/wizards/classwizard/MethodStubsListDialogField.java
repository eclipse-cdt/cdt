/*******************************************************************************
 * Copyright (c) 2004, 2016 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import java.util.List;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.cdt.internal.ui.wizards.classwizard.IMethodStub.EImplMethod;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;

public class MethodStubsListDialogField extends CheckedListDialogField<IMethodStub> {
	// column properties
	private static final String CP_NAME = "name"; //$NON-NLS-1$
	private static final String CP_ACCESS = "access"; //$NON-NLS-1$
	private static final String CP_VIRTUAL = "virtual"; //$NON-NLS-1$
	private static final String CP_IMPL = "impl"; //$NON-NLS-1$
	static final Integer INDEX_YES = 0;
	static final Integer INDEX_NO = 1;
	static final Integer INDEX_PUBLIC = 0;
	static final Integer INDEX_PROTECTED = 1;
	static final Integer INDEX_PRIVATE = 2;

	static final Integer INDEX_DEFINITION = EImplMethod.DEFINITION.ordinal();
	static final Integer INDEX_INLINE = EImplMethod.INLINE.ordinal();
	static final Integer INDEX_DEFAULT = EImplMethod.DEFAULT.ordinal();
	static final Integer INDEX_DELETED = EImplMethod.DELETED.ordinal();

	private final class CellHandler implements ICellModifier {
		@Override
		public boolean canModify(Object element, String property) {
			if (element instanceof IMethodStub) {
				IMethodStub stub = (IMethodStub) element;
				if (property.equals(CP_ACCESS)) {
					return stub.canModifyAccess();
				} else if (property.equals(CP_VIRTUAL)) {
					return stub.canModifyVirtual();
				} else if (property.equals(CP_IMPL)) {
					return stub.canModifyImplementation();
				}
			}
			return false;
		}

		@Override
		public Object getValue(Object element, String property) {
			if (!(element instanceof IMethodStub))
				return null;

			IMethodStub stub = (IMethodStub) element;
			if (property.equals(CP_ACCESS)) {
				if (stub.getAccess() == ASTAccessVisibility.PRIVATE) {
					return INDEX_PRIVATE;
				} else if (stub.getAccess() == ASTAccessVisibility.PROTECTED) {
					return INDEX_PROTECTED;
				} else {
					return INDEX_PUBLIC;
				}
			} else if (property.equals(CP_VIRTUAL)) {
				if (stub.isVirtual())
					return INDEX_YES;
				return INDEX_NO;
			} else if (property.equals(CP_IMPL)) {
				if (stub.isInline())
					return INDEX_INLINE;
				else if (stub.isDefault())
					return INDEX_DEFAULT;
				else if (stub.isDeleted())
					return INDEX_DELETED;
				return INDEX_DEFINITION;
			}
			return null;
		}

		@Override
		public void modify(Object element, String property, Object value) {
			IMethodStub stub = null;
			if (element instanceof IMethodStub) {
				stub = (IMethodStub) element;
			} else if (element instanceof Item) {
				Object data = ((Item) element).getData();
				if (data instanceof IMethodStub)
					stub = (IMethodStub) data;
			}
			if (stub != null) {
				if (property.equals(CP_ACCESS) && value instanceof Integer) {
					Integer access = (Integer) value;
					if (access.equals(INDEX_PRIVATE)) {
						stub.setAccess(ASTAccessVisibility.PRIVATE);
					} else if (access.equals(INDEX_PROTECTED)) {
						stub.setAccess(ASTAccessVisibility.PROTECTED);
					} else {
						stub.setAccess(ASTAccessVisibility.PUBLIC);
					}
					refresh();
				} else if (property.equals(CP_VIRTUAL) && value instanceof Integer) {
					Integer yesno = (Integer) value;
					stub.setVirtual(yesno.equals(INDEX_YES));
					refresh();
				} else if (property.equals(CP_IMPL) && value instanceof Integer) {
					EImplMethod m = EImplMethod.values()[(int) value];
					stub.setImplMethod(m);
					refresh();
				}
			}
		}
	}

	public MethodStubsListDialogField(String title, IListAdapter<IMethodStub> listAdapter) {
		super(listAdapter, null, new MethodStubsLabelProvider());
		setLabelText(title);

		String[] headers = new String[] { NewClassWizardMessages.MethodStubsDialogField_headings_name,
				NewClassWizardMessages.MethodStubsDialogField_headings_access,
				NewClassWizardMessages.MethodStubsDialogField_headings_virtual,
				NewClassWizardMessages.MethodStubsDialogField_headings_implementation };
		ColumnLayoutData[] columns = new ColumnLayoutData[] { new ColumnWeightData(70, 30),
				new ColumnWeightData(40, 30), new ColumnWeightData(30, 25), new ColumnWeightData(50, 30), };
		setTableColumns(new ListDialogField.ColumnsDescription(columns, headers, true));
	}

	@Override
	protected boolean managedButtonPressed(int index) {
		super.managedButtonPressed(index);
		return false;
	}

	@Override
	protected TableViewer createTableViewer(Composite parent) {
		TableViewer viewer = super.createTableViewer(parent);
		Table table = viewer.getTable();
		table.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				e.result = NewClassWizardMessages.NewClassCreationWizardPage_methodStubs_label;
			}
		});

		CellEditor virtualCellEditor = new ComboBoxCellEditor(table,
				new String[] { /* INDEX_YES */BaseClassesLabelProvider.getYesNoText(true),
						/* INDEX_NO */ BaseClassesLabelProvider.getYesNoText(false) },
				SWT.READ_ONLY);

		CellEditor implCellEditor = new ComboBoxCellEditor(table,
				new String[] { /* INDEX_DEFINITION */BaseClassesLabelProvider.getImplText(EImplMethod.DEFINITION),
						/* INDEX_INLINE */BaseClassesLabelProvider.getImplText(EImplMethod.INLINE),
						/* INDEX_DEFAULT */BaseClassesLabelProvider.getImplText(EImplMethod.DEFAULT),
						/* INDEX_DELETED */BaseClassesLabelProvider.getImplText(EImplMethod.DELETED) },
				SWT.READ_ONLY);

		CellEditor accessCellEditor = new ComboBoxCellEditor(table,
				new String[] { /* INDEX_PUBLIC */BaseClassesLabelProvider.getAccessText(ASTAccessVisibility.PUBLIC),
						/* INDEX_PROTECTED */BaseClassesLabelProvider.getAccessText(ASTAccessVisibility.PROTECTED),
						/* INDEX_PRIVATE */BaseClassesLabelProvider.getAccessText(ASTAccessVisibility.PRIVATE) },
				SWT.READ_ONLY);

		viewer.setCellEditors(new CellEditor[] { null, accessCellEditor, virtualCellEditor, implCellEditor });
		viewer.setColumnProperties(new String[] { CP_NAME, CP_ACCESS, CP_VIRTUAL, CP_IMPL });
		viewer.setCellModifier(new CellHandler());
		return viewer;
	}

	public void addMethodStub(IMethodStub methodStub, boolean checked) {
		addElement(methodStub);
		setChecked(methodStub, checked);
	}

	public IMethodStub[] getMethodStubs() {
		List<IMethodStub> allStubs = getElements();
		return allStubs.toArray(new IMethodStub[allStubs.size()]);
	}

	public IMethodStub[] getCheckedMethodStubs() {
		List<IMethodStub> checkedStubs = getCheckedElements();
		return checkedStubs.toArray(new IMethodStub[checkedStubs.size()]);
	}
}
