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

import java.util.Objects;

import org.eclipse.cdt.cmake.core.properties.CMakeGenerator;
import org.eclipse.cdt.cmake.core.properties.IOsOverrides;
import org.eclipse.cdt.cmake.ui.internal.Activator;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

/**
 * Generic UI to control build-platform specific project properties for
 * {@code cmake}. Build-platform specific properties override generic properties and get are applied for
 * the operating system that is running the build.<br>
 * Note that the Eclipse target platform OS may be different from the operating system running the build
 * if the build runs in a container.
 *
 * @author Martin Weber
 * @param <P>
 *        the type that holds the OS specific properties.
 */
abstract class AbstractOverridesTab<P extends IOsOverrides> extends Composite {

	/** 'use exec from path' checkbox */
	private Button b_cmdFromPath;
	/** cmake executable */
	private Text t_cmd;
	/** browse files for cmake executable */
	private Button b_cmdBrowseFiles;
	/** variables in cmake executable text field */
	private Button b_cmdVariables;
	/** Combo that shows the generator names for cmake */
	private ComboViewer c_generator;
	/** txet showing extra cmake arguments */
	private Text t_freeStyleArgs;

	/**
	 */
	public AbstractOverridesTab(Composite parent, int style) {
		super(parent, style);
		createControls(parent);
	}

	/**
	 * Gets all sensible choices for cmake's generator option allowed on the build platform.
	 * The ordering of the returned element gets reflected in the GUI, so implementation may place
	 * the most relevant generators first.
	 *
	 * @return a non-empty array, never {@code null}.
	 */
	protected abstract CMakeGenerator[] getAvailableGenerators();

	protected void createControls(Composite parent) {
		// cmake executable group...
		{
			Group grp = new Group(this, SWT.NONE);
			grp.setText(Messages.AbstractOverridesTab_cmakeExecutable);
			GridDataFactory.defaultsFor(grp).align(SWT.FILL, SWT.CENTER).span(2, 1).applyTo(grp);

			b_cmdFromPath = new Button(grp, SWT.CHECK);
			b_cmdFromPath.setText(Messages.AbstractOverridesTab_onSystemPath);
			GridDataFactory.defaultsFor(b_cmdFromPath).align(SWT.BEGINNING, SWT.CENTER).span(2, 1)
					.applyTo(b_cmdFromPath);
			{
				Label lbl = new Label(grp, SWT.NONE);
				lbl.setText(Messages.AbstractOverridesTab_lbl_file);
				t_cmd = new Text(grp, SWT.SINGLE | SWT.BORDER);
			}
			{
				// "Filesystem", "Variables" dialog launcher buttons...
				Composite buttonBar = new Composite(grp, SWT.NONE);
				GridDataFactory.defaultsFor(buttonBar).span(2, 1).align(SWT.END, SWT.CENTER).applyTo(buttonBar);

				b_cmdBrowseFiles = new Button(buttonBar, SWT.PUSH);
				b_cmdBrowseFiles.setText(Messages.AbstractOverridesTab_lbl_file_system);
				b_cmdVariables = new Button(buttonBar, SWT.PUSH);
				b_cmdVariables.setText(Messages.lbl_insert_variable);
				GridLayoutFactory.fillDefaults().numColumns(2).generateLayout(buttonBar);

				b_cmdBrowseFiles.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						IDialogSettings settings = PlatformUI
								.getDialogSettingsProvider(Activator.getPlugin().getBundle()).getDialogSettings();
						FileDialog dialog = new FileDialog(t_cmd.getShell());
						dialog.setText(Messages.AbstractOverridesTab_lbl_select_cmake_executable);
						dialog.setFilterPath(settings.get("cmake_dir")); //$NON-NLS-1$
						String text = dialog.open();
						settings.put("cmake_dir", dialog.getFilterPath()); //$NON-NLS-1$
						if (text != null) {
							t_cmd.setText(text);
						}
					}
				});

				b_cmdVariables.addListener(SWT.Selection, evt -> {
					StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
					dialog.open();
					String variableExpression = dialog.getVariableExpression();
					if (variableExpression != null) {
						t_cmd.insert(variableExpression);
					}
				});
				// to adjust sensitivity...
				b_cmdFromPath.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent event) {
						final Button btn = (Button) event.widget;
						handleCommandEnabled(!btn.getSelection());
					}
				});
			}

			GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(grp);
		} // cmake executable group

		// buildscript generator combo...
		{
			Label lbl = new Label(this, SWT.NONE);
			lbl.setText(Messages.AbstractOverridesTab_lbl_generator);

			c_generator = new ComboViewer(this);
			Combo control = c_generator.getCombo();
			GridDataFactory.defaultsFor(control).hint(200, SWT.DEFAULT).applyTo(control);
			c_generator.setContentProvider(ArrayContentProvider.getInstance());
			c_generator.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element) {
					if (element instanceof CMakeGenerator) {
						return ((CMakeGenerator) element).getCMakeName();
					}
					return super.getText(element);
				}

			});
			final CMakeGenerator[] generators = getAvailableGenerators();
			c_generator.setInput(generators);
		} // makefile generator combo

		// extra arguments
		{
			final Group grp = new Group(this, SWT.NONE);
			grp.setText(Messages.lbl_other_cmd_args);
			GridDataFactory.defaultsFor(grp).align(SWT.FILL, SWT.FILL).span(2, 1).grab(true, true).applyTo(grp);

			t_freeStyleArgs = new Text(grp, SWT.BORDER | SWT.WRAP | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI);
			t_freeStyleArgs.setToolTipText(Messages.AbstractOverridesTab_tt_args_appended);

			final Button btnVar = new Button(grp, SWT.NONE);
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
			GridLayoutFactory.swtDefaults().generateLayout(grp);
		}

		GridLayoutFactory.swtDefaults().numColumns(2).generateLayout(this);
	}

	/**
	 * Updates displayed values according to the properties edited by this tab.
	 *
	 * @see #updateModel()
	 */
	public void updateView(P model) {
		Objects.requireNonNull(model);
		t_cmd.setText(model.getCommand());
		b_cmdFromPath.setSelection(model.getUseDefaultCommand());
		// adjust sensitivity...
		handleCommandEnabled(!model.getUseDefaultCommand());

		CMakeGenerator generator = model.getGenerator();
		c_generator.setSelection(new StructuredSelection(generator));
		t_freeStyleArgs.setText(String.join("\n", model.getExtraArguments())); //$NON-NLS-1$
	}

	/**
	 * Stores displayed values to the specified properties object.
	 *
	 * @see #updateView()
	 */
	public void updateModel(P model) {
		Objects.requireNonNull(model);
		model.setUseDefaultCommand(b_cmdFromPath.getSelection());
		String command = t_cmd.getText().trim();
		model.setCommand(command);

		final IStructuredSelection sel = (IStructuredSelection) c_generator.getSelection();
		model.setGenerator((CMakeGenerator) sel.getFirstElement());

		model.setExtraArguments(t_freeStyleArgs.getText().trim());
	}

	/**
	 * Changes sensitivity of controls to enter the cmake command. Necessary since
	 * Button.setSelection does not fire events.
	 *
	 * @param enabled
	 *        the new enabled state
	 */
	private void handleCommandEnabled(boolean enabled) {
		t_cmd.setEnabled(enabled);
		b_cmdBrowseFiles.setEnabled(enabled);
		b_cmdVariables.setEnabled(enabled);
	}

}
