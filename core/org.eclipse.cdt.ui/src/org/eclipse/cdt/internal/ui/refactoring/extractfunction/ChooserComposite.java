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

	private Button checkboxVoidReturn;

	public ChooserComposite(Composite parent, final ExtractFunctionInformation info,
			ExtractFunctionInputPage page) {
		super(parent, SWT.NONE);

		GridLayout layout = new GridLayout();		
		setLayout(layout);

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
		
		for (int i = 0; i < info.getParameterCandidates().size(); i++) {
			if (!info.getParameterCandidates().get(i).isDeclaredInSelection()) {
				TableItem item = new TableItem(table, SWT.NONE);

				TableEditor editor = new TableEditor(table);
				int columnIndex = 0;

				final NameInformation name = info.getParameterCandidates().get(i);

				// Text
				item.setText(columnIndex++, name.getType());
				item.setText(columnIndex++, name.getName().toString());

				// Button
				editor = new TableEditor(table);
				final Button buttonOutput = new Button(table, SWT.CHECK);
				if (name.hasReferenceOperator((IASTDeclarator) name.getDeclaration().getParent())) {
					buttonOutput.setSelection(true);
					buttonOutput.setEnabled(false);
				} else {
					buttonOutput.setSelection(name.isOutput() && !name.isReturnValue());
					buttonOutput.setEnabled(!name.mustBeOutput() && !name.isReturnValue());
				}
				buttonOutput.setBackground(table.getBackground());
				buttonOutput.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						widgetSelected(e);
					}

					@Override
					public void widgetSelected(SelectionEvent e) {
						if (buttonOutput.isEnabled()) {
							name.setIsOutput(buttonOutput.getSelection());
						}
					}
				});
				buttonOutput.pack();
				editor.minimumWidth = buttonOutput.getSize().x;
				editor.horizontalAlignment = SWT.CENTER;
				referenceButtons.add(buttonOutput);
				editor.setEditor(buttonOutput, item, columnIndex++);
				
				// Const button
				editor = new TableEditor(table);
				final Button buttonConst = new Button(table, SWT.CHECK);
				
				buttonConst.setSelection(name.isConst());
				buttonConst.setEnabled(!name.isWriteAccess());

				buttonConst.setBackground(table.getBackground());
				buttonConst.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						widgetSelected(e);
					}

					@Override
					public void widgetSelected(SelectionEvent e) {
						name.setConst(buttonConst.getSelection());
					}
				});
				buttonConst.pack();
				editor.minimumWidth = buttonConst.getSize().x;
				editor.horizontalAlignment = SWT.CENTER;
//				referenceButtons.add(referenceButton);
				editor.setEditor(buttonConst, item, columnIndex++);

				if (info.isExtractExpression())
					continue; // Skip the return radiobutton
					
				// Button
				editor = new TableEditor(table);
				final Button buttonReturn = new Button(table, SWT.RADIO);
				buttonReturn.setSelection(name.mustBeReturnValue());
				buttonReturn.setEnabled(info.getMandatoryReturnVariable() == null);
				buttonReturn.setBackground(table.getBackground());
				buttonReturn.addSelectionListener(new SelectionListener() {
					@Override
					public void widgetDefaultSelected(SelectionEvent e) {
						widgetSelected(e);
					}

					@Override
					public void widgetSelected(SelectionEvent e) {
						name.setReturnValue(buttonReturn.getSelection());
						if (buttonReturn.getSelection()) {
							buttonOutput.setSelection(false);
							buttonOutput.notifyListeners(SWT.Selection, new Event());
						} else if (name.mustBeOutput()) {
							buttonOutput.setSelection(true);
							buttonOutput.notifyListeners(SWT.Selection, new Event());
						}
					}
				});
				buttonReturn.pack();
				editor.minimumWidth = buttonReturn.getSize().x;
				editor.horizontalAlignment = SWT.CENTER;
				returnButtons.add(buttonReturn);
				editor.setEditor(buttonReturn, item, columnIndex++);
			}
		}
		
		if (!info.isExtractExpression()) {
			checkboxVoidReturn = new Button(parent, SWT.CHECK | SWT.LEFT);
			checkboxVoidReturn.setText(Messages.ChooserComposite_NoReturnValue);
			checkboxVoidReturn.setEnabled(info.getMandatoryReturnVariable() == null);
			checkboxVoidReturn.addSelectionListener(new SelectionListener() {
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
	
				@Override
				public void widgetSelected(SelectionEvent e) {
					info.setReturnVariable(null);
					
					for (Button button : returnButtons) {
						if (checkboxVoidReturn.getSelection()) {
							button.setSelection(false);
							button.notifyListeners(SWT.Selection, new Event());
						}
						button.setEnabled(!checkboxVoidReturn.getSelection());
					}
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
}
