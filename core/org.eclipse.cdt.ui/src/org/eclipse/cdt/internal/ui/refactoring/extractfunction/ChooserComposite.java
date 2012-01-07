/*******************************************************************************
 * Copyright (c) 2008, 2009 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.extractfunction;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import org.eclipse.cdt.core.dom.ast.IASTDeclarator;

import org.eclipse.cdt.internal.ui.refactoring.NodeContainer.NameInformation;

public class ChooserComposite extends Composite {
	private static final String COLUMN_RETURN = Messages.ChooserComposite_Return;
	private static final String COLUMN_REFERENCE = Messages.ChooserComposite_CallByRef;
	private static final String COLUMN_NAME = Messages.ChooserComposite_Name;
	private static final String COLUMN_TYPE = Messages.ChooserComposite_Type;

	private Button voidReturn;

	private final ExtractFunctionInputPage ip;

	public ChooserComposite(Composite parent, final ExtractFunctionInformation info,
			ExtractFunctionInputPage ip) {
		super(parent, SWT.NONE);

		this.ip = ip;
		
		GridLayout layout = new GridLayout();		
		setLayout(layout);

		boolean hasNoPredefinedReturnValue = true;
		if (info.getInScopeDeclaredVariable() != null) {
			hasNoPredefinedReturnValue = false;
		}

		final ArrayList<Button> returnButtons = new ArrayList<Button>();
		final ArrayList<Button> referenceButtons = new ArrayList<Button>();

		final Table table = new Table(parent, SWT.BORDER | SWT.MULTI | SWT.FILL);
		
		GridData tableLayoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		table.setLayoutData(tableLayoutData);
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		addColumnToTable(table, COLUMN_TYPE);
		addColumnToTable(table, COLUMN_NAME);
		addColumnToTable(table, COLUMN_REFERENCE);
		addColumnToTable(table, Messages.ChooserComposite_const);
		if (!info.isExtractExpression()) {
			addColumnToTable(table, COLUMN_RETURN);
		}
		addColumnToTable(table, ""); //$NON-NLS-1$
		
		for (int i = 0; i < info.getAllUsedNames().size(); i++) {
			if (!info.getAllUsedNames().get(i).isDeclarationInScope()) {
				TableItem item = new TableItem(table, SWT.NONE);

				TableEditor editor = new TableEditor(table);
				int columnIndex = 0;

				final NameInformation name = info.getAllUsedNames().get(i);

				// Text
				item.setText(columnIndex++, name.getType());
				item.setText(columnIndex++, name.getName().toString());

				// Button
				editor = new TableEditor(table);
				final Button referenceButton = new Button(table, SWT.CHECK);
				if (name.hasReferenceOperartor((IASTDeclarator) name.getDeclaration().getParent())) {
					referenceButton.setSelection(true);
					referenceButton.setEnabled(false);
				} else {
					referenceButton.setSelection(name.isReference());
				}
				referenceButton.setBackground(table.getBackground());
				referenceButton.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						name.setUserSetIsReference(referenceButton.getSelection());
						onVisibilityOrReturnChange(info.getAllUsedNames());
					}

					@Override
					public void widgetSelected(SelectionEvent e) {
						widgetDefaultSelected(e);
					}
				});
				referenceButton.pack();
				editor.minimumWidth = referenceButton.getSize().x;
				editor.horizontalAlignment = SWT.CENTER;
				referenceButtons.add(referenceButton);
				editor.setEditor(referenceButton, item, columnIndex++);
				
				// Cosnt Button
				editor = new TableEditor(table);
				final Button constButton = new Button(table, SWT.CHECK);
				
				constButton.setSelection(name.isConst());
				constButton.setEnabled(!name.isWriteAccess());

				constButton.setBackground(table.getBackground());
				constButton.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						name.setConst(constButton.getSelection());
						onVisibilityOrReturnChange(info.getAllUsedNames());
					}

					@Override
					public void widgetSelected(SelectionEvent e) {
						widgetDefaultSelected(e);
					}
				});
				constButton.pack();
				editor.minimumWidth = constButton.getSize().x;
				editor.horizontalAlignment = SWT.CENTER;
//				referenceButtons.add(referenceButton);
				editor.setEditor(constButton, item, columnIndex++);

				if (info.isExtractExpression())
					continue; // Skip the return radiobutton
					
				// Button
				editor = new TableEditor(table);
				final Button returnButton = new Button(table, SWT.RADIO);
				returnButton.setSelection(name.isReturnValue());
				name.setUserSetIsReference(name.isReference());
				returnButton.setEnabled(hasNoPredefinedReturnValue);
				returnButton.setBackground(table.getBackground());
				returnButton.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						name.setUserSetIsReturnValue(returnButton.getSelection());
						if (returnButton.getSelection()) {
							referenceButton.setSelection(false);
							referenceButton.notifyListeners(SWT.Selection, new Event());
						} else if (name.isReference()) {
							referenceButton.setSelection(true);
							referenceButton.notifyListeners(SWT.Selection, new Event());
						}
						onVisibilityOrReturnChange(info.getAllUsedNames());
					}

					@Override
					public void widgetSelected(SelectionEvent e) {
						widgetDefaultSelected(e);
					}
				});
				returnButton.pack();
				editor.minimumWidth = returnButton.getSize().x;
				editor.horizontalAlignment = SWT.CENTER;
				returnButtons.add(returnButton);
				editor.setEditor(returnButton, item, columnIndex++);
			}
		}
		
		if (!info.isExtractExpression()) {
			voidReturn = new Button(parent, SWT.CHECK | SWT.LEFT);
			voidReturn.setText(Messages.ChooserComposite_NoReturnValue);
			voidReturn.setEnabled(hasNoPredefinedReturnValue);
			voidReturn.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					info.setReturnVariable(null);
	
					for (Button button : returnButtons) {
						if (voidReturn.getSelection()) {
							button.setSelection(false);
							button.notifyListeners(SWT.Selection, new Event());
						}
						button.setEnabled(!voidReturn.getSelection());
					}
				}
	
				@Override
				public void widgetSelected(SelectionEvent e) {
					widgetDefaultSelected(e);
				}
			});
		}
		
		layout();
	}

	private void addColumnToTable(final Table table, String string) {
		TableColumn column = new TableColumn(table, SWT.NONE);
		column.setText(string);
		column.setWidth(100);
	}
	
	void onVisibilityOrReturnChange(ArrayList<NameInformation> name) {
		String variableUsedAfterBlock = null;
		for (NameInformation information : name) {
			if (information.isUsedAfterReferences() 
					&& !(information.isUserSetIsReference() || information.isUserSetIsReturnValue())) {
				variableUsedAfterBlock = information.getName().toString();
			}
		}
		
		ip.errorWithAfterUsedVariable(variableUsedAfterBlock);
	}
}
