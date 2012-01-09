/*******************************************************************************
 * Copyright (c) 2004, 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.wizards.classwizard;

import java.util.List;

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

import org.eclipse.cdt.core.parser.ast.ASTAccessVisibility;

import org.eclipse.cdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.cdt.internal.ui.wizards.dialogfields.ListDialogField;

public class BaseClassesListDialogField extends ListDialogField<IBaseClassInfo> {
    
    // column properties
    private static final String CP_NAME = "name"; //$NON-NLS-1$
    private static final String CP_ACCESS = "access"; //$NON-NLS-1$
    private static final String CP_VIRTUAL = "virtual"; //$NON-NLS-1$
    static final Integer INDEX_YES = new Integer(0);
    static final Integer INDEX_NO = new Integer(1);
    static final Integer INDEX_PUBLIC = new Integer(0);
    static final Integer INDEX_PROTECTED = new Integer(1);
    static final Integer INDEX_PRIVATE = new Integer(2);
    
    private final class CellHandler implements ICellModifier {
        @Override
		public boolean canModify(Object element, String property) {
            return (element instanceof IBaseClassInfo)
            	&& (property.equals(CP_ACCESS) || property.equals(CP_VIRTUAL));
        }
        
        @Override
		public Object getValue(Object element, String property) {
            if (!(element instanceof IBaseClassInfo))
                return null;
            
            IBaseClassInfo baseClass = (IBaseClassInfo) element;
            if (property.equals(CP_ACCESS)) {
                if (baseClass.getAccess() == ASTAccessVisibility.PRIVATE) {
                    return INDEX_PRIVATE;
                } else if (baseClass.getAccess() == ASTAccessVisibility.PROTECTED) {
                    return INDEX_PROTECTED;
                } else {
                    return INDEX_PUBLIC;
                }
            } else if (property.equals(CP_VIRTUAL)) {
            	if (baseClass.isVirtual())
            		return INDEX_YES;
            	return INDEX_NO;
            }
            return null;
        }
        
        @Override
		public void modify(Object element, String property, Object value) {
            IBaseClassInfo baseClass = null;
            if (element instanceof IBaseClassInfo) {
                baseClass = (IBaseClassInfo) element;
            } else if (element instanceof Item) {
                Object data = ((Item)element).getData();
                if (data instanceof IBaseClassInfo)
                    baseClass = (IBaseClassInfo) data;
            }
            if (baseClass != null) {
                if (property.equals(CP_ACCESS) && value instanceof Integer) {
                    Integer access = (Integer)value;
                    if (access.equals(INDEX_PRIVATE)) {
                        baseClass.setAccess(ASTAccessVisibility.PRIVATE);
                    } else if (access.equals(INDEX_PROTECTED)) {
                        baseClass.setAccess(ASTAccessVisibility.PROTECTED);
                    } else {
                        baseClass.setAccess(ASTAccessVisibility.PUBLIC);
                    }
                    refresh();
                } else if (property.equals(CP_VIRTUAL) && value instanceof Integer) {
                    Integer yesno = (Integer)value;
                    baseClass.setVirtual(yesno.equals(INDEX_YES));
                    refresh();
                }
            }
        }
    }
    
    public BaseClassesListDialogField(String title, IListAdapter<IBaseClassInfo> listAdapter) {
        super(listAdapter,
        	new String[] {
                NewClassWizardMessages.BaseClassesListDialogField_buttons_add, 
                NewClassWizardMessages.BaseClassesListDialogField_buttons_remove, 
                NewClassWizardMessages.BaseClassesListDialogField_buttons_up, 
                NewClassWizardMessages.BaseClassesListDialogField_buttons_down
        	}, new BaseClassesLabelProvider());
        setRemoveButtonIndex(1);
        setUpButtonIndex(2);
        setDownButtonIndex(3);
        setLabelText(title);
        
        String[] headers = new String[] {
        	NewClassWizardMessages.BaseClassesListDialogField_headings_name, 
        	NewClassWizardMessages.BaseClassesListDialogField_headings_access, 
        	NewClassWizardMessages.BaseClassesListDialogField_headings_virtual
        };
        ColumnLayoutData[] columns = new ColumnLayoutData[] {
        	new ColumnWeightData(70, 30),
        	new ColumnWeightData(30, 30),
        	new ColumnWeightData(25, 25),
        };
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
        table.getAccessible().addAccessibleListener(
            new AccessibleAdapter() {                       
                @Override
				public void getName(AccessibleEvent e) {
                        e.result = NewClassWizardMessages.NewClassCreationWizardPage_baseClasses_label; 
                }
            }
        );
        
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
            virtualCellEditor
		});
        viewer.setColumnProperties(new String[] {
            CP_NAME,
            CP_ACCESS,
            CP_VIRTUAL
        });
        viewer.setCellModifier(new CellHandler());
        return viewer;
    }
    
    public void addBaseClass(IBaseClassInfo baseClass) {
        addElement(baseClass);
    }
    
    public IBaseClassInfo[] getBaseClasses() {
	    List<IBaseClassInfo> baseClasses = getElements();
	    return baseClasses.toArray(new IBaseClassInfo[baseClasses.size()]);
    }
}
