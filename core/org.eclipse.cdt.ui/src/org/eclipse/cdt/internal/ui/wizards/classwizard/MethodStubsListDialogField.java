/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import java.util.List;

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;

public class MethodStubsListDialogField extends CheckedListDialogField {
    
    // column properties
    private static final String CP_NAME = "name"; //$NON-NLS-1$
    private static final String CP_ACCESS = "access"; //$NON-NLS-1$
    private static final String CP_VIRTUAL = "virtual"; //$NON-NLS-1$
    private static final String CP_INLINE = "inline"; //$NON-NLS-1$
    static final Integer INDEX_YES = new Integer(0);
    static final Integer INDEX_NO = new Integer(1);
    static final Integer INDEX_PUBLIC = new Integer(0);
    static final Integer INDEX_PROTECTED = new Integer(1);
    static final Integer INDEX_PRIVATE = new Integer(2);
    
    private final class CellHandler implements ICellModifier {
        public boolean canModify(Object element, String property) {
            if (element instanceof IMethodStub) {
                IMethodStub stub = (IMethodStub) element;
                if (property.equals(CP_ACCESS)) {
                    return stub.canModifyAccess();
                } else if (property.equals(CP_VIRTUAL)) {
                    return stub.canModifyVirtual();
                } else if (property.equals(CP_INLINE)) {
                    return stub.canModifyInline();
                }
            }
            return false;
        }
        
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
	        } else if (property.equals(CP_INLINE)) {
	        	if (stub.isInline())
	        		return INDEX_YES;
        		return INDEX_NO;
	        }
            return null;
        }
        
        public void modify(Object element, String property, Object value) {
            IMethodStub stub = null;
            if (element instanceof IMethodStub) {
                stub = (IMethodStub)element;
            } else if (element instanceof Item) {
                Object data = ((Item)element).getData();
                if (data instanceof IMethodStub)
                    stub = (IMethodStub)data;
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
                } else if (property.equals(CP_INLINE) && value instanceof Integer) {
                    Integer yesno = (Integer) value;
                    stub.setInline(yesno.equals(INDEX_YES));
                    refresh();
                }
            }
        }
    }
    
    public MethodStubsListDialogField(String title, IListAdapter listAdapter) {
        super(listAdapter, null, new MethodStubsLabelProvider());
        setLabelText(title);
        
        String[] headers = new String[] {
        	NewClassWizardMessages.getString("MethodStubsDialogField.headings.name"), //$NON-NLS-1$
        	NewClassWizardMessages.getString("MethodStubsDialogField.headings.access"), //$NON-NLS-1$
        	NewClassWizardMessages.getString("MethodStubsDialogField.headings.virtual"), //$NON-NLS-1$
        	NewClassWizardMessages.getString("MethodStubsDialogField.headings.inline") //$NON-NLS-1$
        };
        ColumnLayoutData[] columns = new ColumnLayoutData[] {
        	new ColumnWeightData(70, 30),
        	new ColumnWeightData(40, 30),
        	new ColumnWeightData(30, 25),
        	new ColumnWeightData(30, 25),
        };
        setTableColumns(new ListDialogField.ColumnsDescription(columns, headers, true));
    }
    
    protected boolean managedButtonPressed(int index) {
        super.managedButtonPressed(index);
        return false;
    }
    
    protected TableViewer createTableViewer(Composite parent) {
        TableViewer viewer = super.createTableViewer(parent);
        Table table = viewer.getTable();
        
        CellEditor virtualCellEditor = new ComboBoxCellEditor(table,
        	new String[] {
                /* INDEX_YES */BaseClassesLabelProvider.getYesNoText(true),
                /* INDEX_NO */BaseClassesLabelProvider.getYesNoText(false)
        	}, SWT.READ_ONLY);

        CellEditor accessCellEditor = new ComboBoxCellEditor(table,
        	new String[] {
                /* INDEX_PUBLIC */BaseClassesLabelProvider.getAccessText(ASTAccessVisibility.PUBLIC),
                /* INDEX_PROTECTED */BaseClassesLabelProvider.getAccessText(ASTAccessVisibility.PROTECTED),
                /* INDEX_PRIVATE */BaseClassesLabelProvider.getAccessText(ASTAccessVisibility.PRIVATE)
            }, SWT.READ_ONLY);
        
        viewer.setCellEditors(new CellEditor[] {
            null,
            accessCellEditor,
            virtualCellEditor,
            virtualCellEditor
		});
        viewer.setColumnProperties(new String[] {
            CP_NAME,
            CP_ACCESS,
            CP_VIRTUAL,
            CP_INLINE
        });
        viewer.setCellModifier(new CellHandler());
        return viewer;
    }
    
    public void addMethodStub(IMethodStub methodStub, boolean checked) {
		addElement(methodStub);
		setChecked(methodStub, checked);
    }

    public IMethodStub[] getMethodStubs() {
	    List allStubs = getElements();
	    return (IMethodStub[]) allStubs.toArray(new IMethodStub[allStubs.size()]);
    }

    public IMethodStub[] getCheckedMethodStubs() {
	    List checkedStubs = getCheckedElements();
	    return (IMethodStub[]) checkedStubs.toArray(new IMethodStub[checkedStubs.size()]);
    }
}
