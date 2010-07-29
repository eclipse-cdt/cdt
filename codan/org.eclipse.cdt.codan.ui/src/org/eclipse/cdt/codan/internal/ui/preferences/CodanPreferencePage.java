/*******************************************************************************
 * Copyright (c) 2009, 2010 Alena Laskavaia 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.ui.preferences;

import java.text.MessageFormat;

import org.eclipse.cdt.codan.core.CodanCorePlugin;
import org.eclipse.cdt.codan.core.CodanRuntime;
import org.eclipse.cdt.codan.core.model.ICheckersRegistry;
import org.eclipse.cdt.codan.core.model.IProblem;
import org.eclipse.cdt.codan.core.model.IProblemProfile;
import org.eclipse.cdt.codan.core.param.IProblemPreference;
import org.eclipse.cdt.codan.internal.ui.CodanUIActivator;
import org.eclipse.cdt.codan.internal.ui.CodanUIMessages;
import org.eclipse.cdt.codan.internal.ui.dialogs.CustomizeProblemDialog;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */
public class CodanPreferencePage extends FieldEditorOverlayPage implements
		IWorkbenchPreferencePage {
	private IProblemProfile profile;
	private ISelectionChangedListener problemSelectionListener;
	private IProblem selectedProblem;
	private Group info;
	private Label infoDesc;
	private Label infoMessage;
	private Label infoParams;
	private Button infoButton;
	private ProblemsTreeEditor checkedTreeEditor;

	public CodanPreferencePage() {
		super(GRID);
		setPreferenceStore(new ScopedPreferenceStore(new InstanceScope(),
				CodanCorePlugin.PLUGIN_ID));
		// setDescription("Code Analysis Preference Page");
		problemSelectionListener = new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (info != null) {
					if (event.getSelection() instanceof ITreeSelection) {
						ITreeSelection s = (ITreeSelection) event
								.getSelection();
						if (s.getFirstElement() instanceof IProblem)
							setSelectedProblem((IProblem) s.getFirstElement());
						else
							setSelectedProblem(null);
					}
				}
			}
		};
	}

	@Override
	protected String getPageId() {
		return "org.eclipse.cdt.codan.internal.ui.preferences.CodanPreferencePage"; //$NON-NLS-1$
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common
	 * GUI blocks needed to manipulate various types of preferences. Each field
	 * editor knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		//		ScrolledComposite sc = new ScrolledComposite(getFieldEditorParent(),
		//				SWT.H_SCROLL | SWT.V_SCROLL);
		//		sc.setLayoutData(new GridData(GridData.FILL_BOTH));
		//		sc.setExpandHorizontal(true);
		//		sc.setExpandVertical(true);
		//		sc.setMinWidth(400);
		//		sc.setMinHeight(400);
		//		//		Composite pane = new Composite(sc, SWT.NONE);
		//		Button pane = new Button(sc, SWT.PUSH);
		//		pane.setText("push");
		//		sc.setContent(pane);
		//		GridLayout gl = new GridLayout();
		//		gl.marginHeight = 0;
		//		gl.marginWidth = 0;
		//		pane.setLayout(gl);
		checkedTreeEditor = new ProblemsTreeEditor(getFieldEditorParent(),
				profile);
		addField(checkedTreeEditor);
		checkedTreeEditor.getTreeViewer().addSelectionChangedListener(
				problemSelectionListener);
		checkedTreeEditor.getTreeViewer().addDoubleClickListener(
				new IDoubleClickListener() {
					public void doubleClick(DoubleClickEvent event) {
						openCustomizeDialog();
					}
				});
		GridData layoutData = new GridData(GridData.FILL, GridData.FILL, true,
				true);
		layoutData.heightHint = 400;
		checkedTreeEditor.getTreeViewer().getControl()
				.setLayoutData(layoutData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.codan.internal.ui.preferences.FieldEditorOverlayPage#
	 * createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		profile = isPropertyPage() ? getRegistry()
				.getResourceProfileWorkingCopy((IResource) getElement())
				: getRegistry().getWorkspaceProfile();
		Composite comp = (Composite) super.createContents(parent);
		createInfoControl(comp);
		return comp;
	}

	/**
	 * @param comp
	 */
	private void createInfoControl(Composite comp) {
		info = new Group(comp, SWT.NONE);
		info.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		info.setLayout(new GridLayout(3, false));
		info.setText(CodanUIMessages.CodanPreferencePage_Info);
		GridDataFactory gdLab = GridDataFactory.swtDefaults()
				.align(SWT.BEGINNING, SWT.BEGINNING).grab(false, false);
		GridDataFactory gdFact = GridDataFactory.swtDefaults()
				.align(SWT.BEGINNING, SWT.BEGINNING).grab(true, true);
		// message
		Label labelMessage = new Label(info, SWT.NONE);
		labelMessage.setText(CodanUIMessages.CodanPreferencePage_MessageLabel);
		labelMessage.setLayoutData(gdLab.create());
		infoMessage = new Label(info, SWT.WRAP);
		infoMessage.setLayoutData(gdFact.copy().span(2, 1).create());
		// description
		Label labelDesc = new Label(info, SWT.NONE);
		labelDesc.setText(CodanUIMessages.CodanPreferencePage_Description);
		labelDesc.setLayoutData(gdLab.create());
		infoDesc = new Label(info, SWT.WRAP);
		PixelConverter pixelConverter = new PixelConverter(comp);
		infoDesc.setLayoutData(gdFact
				.copy()
				.span(2, 1)
				.hint(pixelConverter.convertWidthInCharsToPixels(60),
						pixelConverter.convertHeightInCharsToPixels(3))
				.create());
		// params
		Label labelParams = new Label(info, SWT.NONE);
		labelParams.setText(CodanUIMessages.CodanPreferencePage_Parameters);
		labelParams.setLayoutData(gdLab.create());
		infoParams = new Label(info, SWT.NONE);
		infoParams.setLayoutData(gdFact.create());
		infoButton = new Button(info, SWT.PUSH);
		infoButton.setLayoutData(GridDataFactory.swtDefaults()
				.align(SWT.END, SWT.BEGINNING).create());
		infoButton.setText(CodanUIMessages.CodanPreferencePage_Customize);
		infoButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				openCustomizeDialog();
			}
		});
		restoreWidgetValues();
	}

	/**
	 * @param selection
	 */
	protected void setSelectedProblem(IProblem problem) {
		this.selectedProblem = problem;
		updateProblemInfo();
	}

	/**
	 * @return
	 */
	protected ICheckersRegistry getRegistry() {
		return CodanRuntime.getInstance().getCheckersRegistry();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performApply()
	 */
	@Override
	public boolean performOk() {
		saveWidgetValues();
		// if (isPropertyPage())
		getRegistry().updateProfile((IResource) getElement(), null);
		return super.performOk();
	}

	/**
	 * 
	 */
	private void saveWidgetValues() {
		CodanUIActivator
				.getDefault()
				.getDialogSettings()
				.put(getWidgetId(),
						selectedProblem == null ? "" : selectedProblem.getId()); //$NON-NLS-1$
	}

	private void restoreWidgetValues() {
		String id = CodanUIActivator.getDefault().getDialogSettings()
				.get(getWidgetId());
		if (id != null && id.length() > 0 && checkedTreeEditor != null) {
			checkedTreeEditor.getTreeViewer().setSelection(
					new StructuredSelection(profile.findProblem(id)), true);
		} else {
			setSelectedProblem(null);
		}
	}

	/**
	 * @return
	 */
	protected String getWidgetId() {
		return getPageId() + ".selection"; //$NON-NLS-1$
	}

	/**
	 * 
	 */
	private void updateProblemInfo() {
		if (selectedProblem == null) {
			infoMessage.setText(""); //$NON-NLS-1$
			infoDesc.setText(""); //$NON-NLS-1$
			infoParams.setText(""); //$NON-NLS-1$
			infoButton.setEnabled(false);
		} else {
			IProblemPreference pref = selectedProblem.getPreference();
			String description = selectedProblem.getDescription();
			if (description == null)
				description = CodanUIMessages.CodanPreferencePage_NoInfo;
			String messagePattern = selectedProblem.getMessagePattern();
			String message = CodanUIMessages.CodanPreferencePage_NoInfo;
			if (messagePattern != null) {
				message = MessageFormat.format(messagePattern, "X", "Y", "Z"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ 
			}
			infoMessage.setText(message);
			infoDesc.setText(description);
			infoParams
					.setText(pref == null ? CodanUIMessages.CodanPreferencePage_NoInfo
							: CodanUIMessages.CodanPreferencePage_HasPreferences);
			infoButton.setEnabled(true);
		}
		info.layout(true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
	}

	/**
	 * 
	 */
	protected void openCustomizeDialog() {
		CustomizeProblemDialog d = new CustomizeProblemDialog(getShell(),
				selectedProblem, (IResource) getElement());
		d.open();
	}
}