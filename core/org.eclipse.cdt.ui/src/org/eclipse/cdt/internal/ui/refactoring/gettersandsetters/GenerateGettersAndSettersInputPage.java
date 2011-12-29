/*******************************************************************************
 * Copyright (c) 2008, 2011 Institute for Software, HSR Hochschule fuer Technik  
 * Rapperswil, University of applied sciences and others
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 *  
 * Contributors: 
 *     Institute for Software - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.gettersandsetters;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
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
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.ContainerCheckedTreeViewer;
import org.eclipse.ui.dialogs.PreferencesUtil;

import org.eclipse.cdt.ui.CUIPlugin;

import org.eclipse.cdt.internal.ui.preferences.NameStylePreferencePage;
import org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.AccessorDescriptor.AccessorKind;

public class GenerateGettersAndSettersInputPage extends UserInputWizardPage implements IPreferenceChangeListener {
	private GetterSetterContext context;
	private ContainerCheckedTreeViewer variableSelectionView;
	private GetterSetterLabelProvider labelProvider;

	public GenerateGettersAndSettersInputPage(GetterSetterContext context) {
		super(Messages.GenerateGettersAndSettersInputPage_Name); 
		this.context = context;
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(CUIPlugin.PLUGIN_ID);
		// We are listening for changes in the Name Style preferences
		node.addPreferenceChangeListener(this);
	}

	@Override
	public void createControl(Composite parent) {
		setTitle(Messages.GenerateGettersAndSettersInputPage_Name);
		setMessage(Messages.GenerateGettersAndSettersInputPage_Header);
		
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		createTree(comp);
		GridData gd = new GridData(GridData.FILL_BOTH);
		variableSelectionView.getTree().setLayoutData(gd);
		
		Composite buttonContainer = createButtonComposite(comp);
		gd = new GridData();
		gd.verticalAlignment = SWT.TOP;
		buttonContainer.setLayoutData(gd);

		final Button definitionSeparate = new Button(comp, SWT.CHECK);
		definitionSeparate.setText(Messages.GenerateGettersAndSettersInputPage_SeparateDefinition);
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.heightHint = 40;
		definitionSeparate.setLayoutData(gd);
		definitionSeparate.setSelection(context.isDefinitionSeparate());
		definitionSeparate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				context.setDefinitionSeparate(definitionSeparate.getSelection());
			}
		});

		Link link= new Link(comp, SWT.WRAP);
		link.setText(Messages.GenerateGettersAndSettersInputPage_LinkDescription);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String id = NameStylePreferencePage.PREF_ID;
				PreferencesUtil.createPreferenceDialogOn(getShell(), id, new String[] { id }, null).open();
			}
		});
		link.setToolTipText(Messages.GenerateGettersAndSettersInputPage_LinkTooltip);

		gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.grabExcessHorizontalSpace = true;
		link.setLayoutData(gd);

		setControl(comp);
	}

	private Composite createButtonComposite(Composite comp) {
		Composite btComp = new Composite(comp, SWT.NONE);
		FillLayout layout = new FillLayout(SWT.VERTICAL);
		layout.spacing = 4;
		btComp.setLayout(layout);
		
		Button selectAll = new Button(btComp, SWT.PUSH);
		selectAll.setText(Messages.GenerateGettersAndSettersInputPage_SelectAll);
		selectAll.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Object[] items = context.getElements(null);
				for (Object treeItem : items) {
					variableSelectionView.setChecked(treeItem, true);
				}
				updateSelectedFunctions();
			}
		});
		
		Button deselectAll = new Button(btComp, SWT.PUSH);
		deselectAll.setText(Messages.GenerateGettersAndSettersInputPage_DeselectAll);
		deselectAll.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				for (Object treeItem : context.getElements(null)) {
					variableSelectionView.setChecked(treeItem, false);
				}
				updateSelectedFunctions();
			}
		});
		
		Button selectGetter = new Button(btComp, SWT.PUSH);
		selectGetter.setText(Messages.GenerateGettersAndSettersInputPage_SelectGetters);
		selectGetter.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectAccessors(AccessorKind.GETTER);
			}
		});
		
		Button selectSetter = new Button(btComp, SWT.PUSH);
		selectSetter.setText(Messages.GenerateGettersAndSettersInputPage_SelectSetters);
		selectSetter.addSelectionListener(new SelectionAdapter(){
			@Override
			public void widgetSelected(SelectionEvent e) {
				selectAccessors(AccessorKind.SETTER);
			}
		});
		
		return btComp;
	}
	
	private void selectAccessors(AccessorKind kind) {
		for (Object treeItem : context.getElements(null)) {
			if (treeItem instanceof FieldDescriptor) {
				FieldDescriptor field = (FieldDescriptor) treeItem;
				Object[] children = context.getChildren(field);
				for (Object child : children) {
					if (child instanceof AccessorDescriptor) {
						AccessorDescriptor accessor = (AccessorDescriptor) child;
						if (accessor.getKind() == kind) {
							variableSelectionView.setChecked(accessor, true);
						}
					}
				}
			}
		}
		updateSelectedFunctions();
	}

	private void createTree(Composite comp) {
		variableSelectionView = new ContainerCheckedTreeViewer(comp, SWT.BORDER);
		labelProvider = new GetterSetterLabelProvider();
		variableSelectionView.setContentProvider(context);
		variableSelectionView.setLabelProvider(labelProvider);

		variableSelectionView.setAutoExpandLevel(3);
		variableSelectionView.setInput(context);
		if (context.selectedName != null) {
			String rawSignature = context.selectedName.getRawSignature();
			for (Object obj : variableSelectionView.getVisibleExpandedElements()) {
				if (obj instanceof FieldDescriptor) {
					if (obj.toString().contains(rawSignature)) {
						variableSelectionView.setSubtreeChecked(obj, true);
					}
				}
			}
		}

		updateSelectedFunctions();

		variableSelectionView.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				updateSelectedFunctions();
			}
		});
	}

	private void updateSelectedFunctions() {
		context.selectedAccessors.clear();
		for (Object element : variableSelectionView.getCheckedElements()) {
			if (element instanceof AccessorDescriptor) {
				context.selectedAccessors.add((AccessorDescriptor) element);
			}
		}
		setPageComplete(!context.selectedAccessors.isEmpty());
	}

	@Override
	public void preferenceChange(PreferenceChangeEvent event) {
		if (variableSelectionView.getTree().isDisposed()) {
			return;
		}
		
		if (GetterSetterNameGenerator.getGenerateGetterSettersPreferenceKeys().contains(event.getKey())) {
			context.recreateFieldDescriptors();
			variableSelectionView.refresh();
			variableSelectionView.setInput(context); // Set input to trigger node expansion.
			for (Object element : context.selectedAccessors) {
				variableSelectionView.setChecked(element, true);
			}
		}
	}

	@Override
	public void dispose() {
		IEclipsePreferences node = InstanceScope.INSTANCE.getNode(CUIPlugin.PLUGIN_ID);
		node.removePreferenceChangeListener(this);
		super.dispose();
	}
}
