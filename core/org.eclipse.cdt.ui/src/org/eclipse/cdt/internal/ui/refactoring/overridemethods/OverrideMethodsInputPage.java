/******************************************************************************* 
 * Copyright (c) 2017 Pavel Marek 
 * All rights reserved. This program and the accompanying materials  
 * are made available under the terms of the Eclipse Public License v1.0  
 * which accompanies this distribution, and is available at  
 * http://www.eclipse.org/legal/epl-v10.html   
 *  
 * Contributors:  
 *      Pavel Marek - initial API and implementation 
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.overridemethods;

import java.util.List;

import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;

/**
 * This class represents the only InputPage that wizard for this code generation
 * is consists of. 
 * @author Pavel Marek
 */
public class OverrideMethodsInputPage extends UserInputWizardPage {
	private OverrideMethodsRefactoring fRefactoring;
	private CheckboxTreeViewer fTree;

	public OverrideMethodsInputPage(OverrideMethodsRefactoring refactoring) {
		super(Messages.OverrideMethodsInputPage_Name);
		this.fRefactoring = refactoring;
	}
	
	/**
	 * Adds "Select All" and "Deselect All" to given Composite.
	 * @param parent
	 */
	private void createButtons(Composite parent) {
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		layout.spacing = 4;
		buttonComposite.setLayout(layout);
		
		Button selAllButton= new Button(buttonComposite, SWT.PUSH);
		selAllButton.setText(Messages.OverrideMethodsInputPage_SelectAll);
		selAllButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				List<Method> allMethods= fRefactoring.getMethodContainer().getAllMethods();

				// Add all methods from container to PrintData.
				fTree.setCheckedElements(allMethods.toArray());
				fRefactoring.getPrintData().addMethods(allMethods);
				checkPageComplete();
			}
			
		});

		Button deselAllButton= new Button(buttonComposite, SWT.PUSH);
		deselAllButton.setText(Messages.OverrideMethodsInputPage_DeselectAll);
		deselAllButton.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				List<Method> allMethods= fRefactoring.getMethodContainer().getAllMethods();

				// Uncheck all methods from tree.
				for (Method method : allMethods) {
					fTree.setChecked(method, false);	
				}

				fRefactoring.getPrintData().removeMethods(allMethods);
				checkPageComplete();
			}
			
		});
	}
	
	private void createTree(Composite parent) {
		fTree = new ContainerCheckedTreeViewer(parent);
		fTree.setContentProvider(fRefactoring.getMethodContainer());
		fTree.setAutoExpandLevel(3);
		// Populate the tree.
		fTree.setInput(fRefactoring.getMethodContainer().getInitialInput());
		// Horizontal fill.
		fTree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		fTree.addCheckStateListener(new ICheckStateListener() {
			
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				VirtualMethodPrintData printData= fRefactoring.getPrintData();
				
				// ICPPClassType (parent) checked.
				if (event.getElement() instanceof ICPPClassType) {
					ICPPClassType parentClass= (ICPPClassType) event.getElement();
					VirtualMethodContainer methodContainer= fRefactoring.getMethodContainer();
					List<Method> selectedMethods= methodContainer.getMethods(parentClass);
					
					// Add (or remove) all methods that are displayed as children in the tree
					// to PrintData.
					if (event.getChecked()) {
						printData.addMethods(selectedMethods);
					}
					else {
						printData.removeMethods(selectedMethods);
					}
				}
				else if (event.getElement() instanceof Method){
					Method selectedMethod= (Method) event.getElement();
					
					if (event.getChecked()) {
						printData.addMethod(selectedMethod);
					}
					else {
						printData.removeMethod(selectedMethod);
					}
				}
				
				checkPageComplete();
			}
		});
		
		// Set all nodes (Methods) in the tree to unchecked.
		for (Method method : fRefactoring.getMethodContainer().getAllMethods()) {
			fTree.setChecked(method, false);
		}
	}

	@Override
	public void createControl(Composite parent) {
		setTitle(Messages.OverrideMethodsInputPage_Name);
		setMessage(Messages.OverrideMethodsInputPage_Header);
		
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));

		createTree(composite);
		// Create buttons.
		createButtons(composite);
		
		checkPageComplete();
		setControl(composite);
	}
	
	/**
	 * Sets page complete under certain conditions.
	 * 
	 * Note that if the page is complete, the "Preview" and "OK" buttons
	 * are enabled.
	 */
	private void checkPageComplete() {
		if (fRefactoring.getPrintData().isEmpty()) {
			setPageComplete(false);
		}
		else {
			setPageComplete(true);
		}
	}

}
