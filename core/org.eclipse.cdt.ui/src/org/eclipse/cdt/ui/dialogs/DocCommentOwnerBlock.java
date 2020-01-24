/*******************************************************************************
 * Copyright (c) 2008, 2020 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Marco Stornelli <marco.stornelli@gmail.com> - Bug 333134
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - ongoing support
 *******************************************************************************/
package org.eclipse.cdt.ui.dialogs;

import org.eclipse.cdt.doxygen.core.DoxygenPreferences;
import org.eclipse.cdt.internal.ui.dialogs.DocCommentOwnerArea;
import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.osgi.framework.FrameworkUtil;

/**
 * Project property page for setting documentation comment owner.
 * <em>This class is not intended for use outside of CDT</em>
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class DocCommentOwnerBlock extends AbstractCOptionPage {
	private static String EDITOR_PREF_PAGE_ID = "org.eclipse.cdt.ui.preferences.CEditorPreferencePage"; //$NON-NLS-1$

	/**
	 * @deprecated will throw {@link NullPointerException} on attempt to access
	 */
	@Deprecated
	protected DocCommentOwnerComposite fDocComboComposite;
	protected DocCommentOwnerManager fManager;

	protected Button fCheckbox;
	protected Link fLink;

	private DocCommentOwnerArea docCommentOwnerArea;
	private final DoxygenPreferences doxygenPreferences;

	public DocCommentOwnerBlock() {
		fManager = DocCommentOwnerManager.getInstance();
		doxygenPreferences = EclipseContextFactory
				.getServiceContext(FrameworkUtil.getBundle(getClass()).getBundleContext())
				.get(DoxygenPreferences.class);
	}

	void handleCheckBox() {
		docCommentOwnerArea.setEnabled(fCheckbox.getSelection());
		fLink.setVisible(!fCheckbox.getSelection());
	}

	@Override
	public void createControl(final Composite parent) {
		Composite pane = new Composite(parent, SWT.NONE);
		pane.setLayout(new GridLayout(2, true));
		pane.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).span(2, 1).create());

		setControl(pane);

		fCheckbox = ControlFactory.createCheckBox(pane,
				DialogsMessages.DocCommentOwnerBlock_EnableProjectSpecificSettings);
		fCheckbox.setLayoutData(GridDataFactory.fillDefaults().grab(true, false).create());
		fCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleCheckBox();
			}
		});

		fLink = new Link(pane, SWT.NONE);
		fLink.setText(DialogsMessages.PreferenceScopeBlock_preferenceLink);
		fLink.setLayoutData(
				GridDataFactory.fillDefaults().align(GridData.CENTER, GridData.BEGINNING).grab(true, false).create());
		fLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				PreferencesUtil.createPreferenceDialogOn(parent.getShell(), EDITOR_PREF_PAGE_ID, null, null).open();
			}
		});

		String dsc = DialogsMessages.DocCommentOwnerBlock_SelectDocToolDescription;
		String msg = DialogsMessages.DocCommentOwnerBlock_DocToolLabel;
		IProject project = getProject();
		docCommentOwnerArea = new DocCommentOwnerArea(pane, doxygenPreferences.metadata(), dsc, msg);
		fCheckbox.setSelection(fManager.projectDefinesOwnership(project));
		IDocCommentOwner prjOwner = DocCommentOwnerManager.getInstance().getCommentOwner(project);
		docCommentOwnerArea.initialize(prjOwner, doxygenPreferences.projectStorage(project));
		handleCheckBox();
	}

	@Override
	public void performApply(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		if (!fCheckbox.getSelection())
			fManager.setCommentOwner(project, null, true);
		else {
			IDocCommentOwner newOwner = docCommentOwnerArea.getSelectedDocCommentOwner();
			fManager.setCommentOwner(project, newOwner, true);
		}
		docCommentOwnerArea.apply(doxygenPreferences.projectStorage(project));
	}

	public IProject getProject() {
		ICOptionContainer container = getContainer();
		if (container != null) {
			if (container instanceof ICOptionContainerExtension) {
				try {
					return ((ICOptionContainerExtension) container).getProjectHandle();
				} catch (Exception e) {
					return null;
				}
			}
			return container.getProject();
		}
		return null;
	}

	@Override
	public void performDefaults() {
	}
}
