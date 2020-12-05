/*******************************************************************************
 * Copyright (c) 2020 Martin Weber.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.cmake.ui.internal.properties;

import java.io.IOException;
import java.util.Objects;

import org.eclipse.cdt.cmake.core.internal.Activator;
import org.eclipse.cdt.cmake.core.properties.ICMakeProperties;
import org.eclipse.cdt.cmake.core.properties.ICMakePropertiesController;
import org.eclipse.cdt.core.build.ICBuildConfiguration;
import org.eclipse.core.resources.IBuildConfiguration;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.dialogs.FilteredResourcesSelectionDialog;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page for CMake projects.
 */
public class CMakePropertyPage extends PropertyPage {
	public CMakePropertyPage() {
	}

	private ICMakePropertiesController propertiesController;
	private ICMakeProperties properties;
	private Text t_cacheFile;
	private Text t_freeStyleArgs;
	private Button b_warnNoDev;
	private Button b_debugTryCompile;
	private Button b_debug;
	private Button b_trace;
	private Button b_warnUnitialized;
	private Button b_warnUnused;
	private ComboViewer cmb_buildType;

	@Override
	protected Control createContents(Composite parent) {

		final IProject project = (IProject) getElement();
		try {
			IBuildConfiguration config = project.getActiveBuildConfig();
			ICBuildConfiguration cconfig = config.getAdapter(ICBuildConfiguration.class);
			propertiesController = cconfig.getAdapter(ICMakePropertiesController.class);
			properties = propertiesController.get();
		} catch (IOException | CoreException ex) {
			ErrorDialog.openError(parent.getShell(), Messages.CMakePropertyPage_failed_to_load_properties, null,
					Activator.errorStatus(ex.getMessage(), ex));
		}

		setTitle(Messages.CMakePropertyPage_lbl_cmdline_options);
		Composite composite = new Composite(parent, SWT.NONE);
		{
			Composite grp = new Composite(composite, SWT.NONE);
			final Label lbl = new Label(grp, SWT.NONE);
			lbl.setText(Messages.CMakePropertyPage_lbl_build_type);
			cmb_buildType = new ComboViewer(grp, SWT.DROP_DOWN);
			cmb_buildType.setContentProvider(ArrayContentProvider.getInstance());
			cmb_buildType.setInput(new String[] { "RelWithDebInfo", "Debug", "Release", "MinSizeRel", "None" }); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
			GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(grp);
		}
		{
			final Composite grp = new Composite(composite, SWT.NONE);
			grp.setLayout(new FillLayout(SWT.VERTICAL));

			b_warnNoDev = new Button(grp, SWT.CHECK);
			b_warnNoDev.setText(Messages.CMakePropertyPage_Wno_dev);
			b_warnUnitialized = new Button(grp, SWT.CHECK);
			b_warnUnitialized.setText(Messages.CMakePropertyPage_Wuninitialized);
			b_warnUnused = new Button(grp, SWT.CHECK);
			b_warnUnused.setText(Messages.CMakePropertyPage_Wunused_vars);
			b_debugTryCompile = new Button(grp, SWT.CHECK);
			b_debugTryCompile.setText(Messages.CMakePropertyPage_debug_trycompile);
			b_debug = new Button(grp, SWT.CHECK);
			b_debug.setText(Messages.CMakePropertyPage_debug_output);
			b_trace = new Button(grp, SWT.CHECK);
			b_trace.setText(Messages.CMakePropertyPage_trace);
		}
		{
			// cmake prepopulate cache group...
			final Group grp = new Group(composite, SWT.NONE);
			grp.setText(Messages.CMakePropertyPage_lbl_preprop_cache);
			{
				final Label lbl = new Label(grp, SWT.NONE);
				lbl.setText(Messages.CMakePropertyPage_lbl_file);
			}
			t_cacheFile = new Text(grp, SWT.BORDER);

			final Button b_browseCacheFile = new Button(grp, SWT.NONE);
			b_browseCacheFile.setText(Messages.CMakePropertyPage_lbl_browse);
			GridDataFactory.defaultsFor(b_browseCacheFile).span(2, 1).align(SWT.END, SWT.CENTER)
					.applyTo(b_browseCacheFile);
			b_browseCacheFile.addListener(SWT.Selection, evt -> {
				FilteredResourcesSelectionDialog dialog = new FilteredResourcesSelectionDialog(t_cacheFile.getShell(),
						false, project, IResource.FILE);
				dialog.setTitle(Messages.CMakePropertyPage_lbl_select_file);
				dialog.setInitialPattern("init*.cmake", FilteredItemsSelectionDialog.FULL_SELECTION); //$NON-NLS-1$
				dialog.open();
				IFile file = (IFile) dialog.getFirstResult();
				if (file != null) {
					t_cacheFile.setText(file.getProjectRelativePath().toPortableString());
				}
			});
			GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(grp);
		}

		// extra arguments
		{
			final Group grp1 = new Group(composite, SWT.NONE);
			grp1.setText(Messages.lbl_other_cmd_args);

			t_freeStyleArgs = new Text(grp1, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
			t_freeStyleArgs.setToolTipText(Messages.CMakePropertyPage_help_has_overrides);
			final Button btnVar = new Button(grp1, SWT.NONE);
			btnVar.setText(Messages.lbl_insert_variable);
			GridDataFactory.defaultsFor(btnVar).align(SWT.END, SWT.CENTER).applyTo(btnVar);
			btnVar.addListener(SWT.Selection, evt -> {
				StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
				dialog.open();
				String variableExpression = dialog.getVariableExpression();
				if (variableExpression != null) {
					t_freeStyleArgs.insert(variableExpression);
				}
			});
			GridLayoutFactory.swtDefaults().generateLayout(grp1);
			final Group grp = grp1;
			GridDataFactory.defaultsFor(grp).align(SWT.FILL, SWT.FILL).span(2, 1).grab(true, true).applyTo(grp);
		}

		GridLayoutFactory.swtDefaults().generateLayout(composite);

		updateView(properties);

		return composite;
	}

	@Override
	public boolean performOk() {
		updateModel(properties);
		try {
			propertiesController.save(properties);
			return true;
		} catch (IOException ex) {
			ErrorDialog.openError(getShell(), Messages.CMakePropertyPage_failed_to_save_properties, null,
					Activator.errorStatus(ex.getMessage(), ex));
			return false;
		}
	}

	@Override
	protected void performDefaults() {
		properties.reset(false);
		updateView(properties);
		super.performDefaults();
	}

	/**
	 * Updates displayed values according to the data model edited by this object.
	 * @param model
	 */
	private void updateView(ICMakeProperties model) {
		cmb_buildType.getCombo().setText(Objects.toString(model.getBuildType(), "")); //$NON-NLS-1$
		b_warnNoDev.setSelection(model.isWarnNoDev());
		b_debugTryCompile.setSelection(model.isDebugTryCompile());
		b_debug.setSelection(model.isDebugOutput());
		b_trace.setSelection(model.isTrace());
		b_warnUnitialized.setSelection(model.isWarnUnitialized());
		b_warnUnused.setSelection(model.isWarnUnused());
		t_cacheFile.setText(Objects.toString(model.getCacheFile(), "")); //$NON-NLS-1$
		t_freeStyleArgs.setText(model.getExtraArguments());
	}

	/**
	 * Updates the data model edited by this object from the displayed values.
	 */
	private void updateModel(ICMakeProperties model) {
		IStructuredSelection selection = cmb_buildType.getStructuredSelection();
		model.setBuildType(
				selection.isEmpty() ? cmb_buildType.getCombo().getText() : (String) selection.getFirstElement());
		model.setWarnNoDev(b_warnNoDev.getSelection());
		model.setDebugTryCompile(b_debugTryCompile.getSelection());
		model.setDebugOutput(b_debug.getSelection());
		model.setTrace(b_trace.getSelection());
		model.setWarnUnitialized(b_warnUnitialized.getSelection());
		model.setWarnUnused(b_warnUnused.getSelection());
		model.setCacheFile(t_cacheFile.getText().isBlank() ? null : t_cacheFile.getText());
		model.setExtraArguments(t_freeStyleArgs.getText().trim());
	}
}
