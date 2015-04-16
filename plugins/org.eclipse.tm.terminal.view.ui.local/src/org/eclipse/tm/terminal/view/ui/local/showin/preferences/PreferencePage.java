/*******************************************************************************
 * Copyright (c) 2014 - 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.terminal.view.ui.local.showin.preferences;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.core.variables.IStringVariableManager;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.terminal.view.ui.controls.NoteCompositeHelper;
import org.eclipse.tm.terminal.view.ui.local.activator.UIPlugin;
import org.eclipse.tm.terminal.view.ui.local.nls.Messages;
import org.eclipse.tm.terminal.view.ui.local.showin.ExternalExecutablesDialog;
import org.eclipse.tm.terminal.view.ui.local.showin.ExternalExecutablesManager;
import org.eclipse.tm.terminal.view.ui.local.showin.interfaces.IExternalExecutablesProperties;
import org.eclipse.tm.terminal.view.ui.local.showin.interfaces.IPreferenceKeys;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.framework.Bundle;

/**
 * Terminals top preference page implementation.
 */
public class PreferencePage extends org.eclipse.jface.preference.PreferencePage implements IWorkbenchPreferencePage {
	/* default */ TableViewer viewer;
	private Button addButton;
	private Button editButton;
	private Button removeButton;
	/* default */ Combo workingDir;
	private Button browseButton;

	private Button variablesButton;
	private boolean hasVariablesButton = false;

	/* default */ Text command;
	private Button commandBrowseButton;
	private Text arguments;

	/* default */ final List<Map<String, String>> executables = new ArrayList<Map<String, String>>();
	/* default */ final Map<String, Image> images = new HashMap<String, Image>();

	/* default */ static final Object[] NO_ELEMENTS = new Object[0];

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
		Bundle bundle = Platform.getBundle("org.eclipse.debug.ui"); //$NON-NLS-1$
		if (bundle != null && bundle.getState() != Bundle.UNINSTALLED && bundle.getState() != Bundle.STOPPING) {
			hasVariablesButton = true;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(final Composite parent) {
		final GC gc = new GC(parent);
		gc.setFont(JFaceResources.getDialogFont());

		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		GridData layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		panel.setLayoutData(layoutData);

		Label label = new Label(panel, SWT.HORIZONTAL);
		label.setText(Messages.PreferencePage_label);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		if (!Platform.OS_WIN32.equals(Platform.getOS())) {
			Group group = new Group(panel, SWT.NONE);
			group.setText(Messages.PreferencePage_command_label);
			group.setLayout(new GridLayout(2, false));
			group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

			command = new Text(group, SWT.SINGLE | SWT.BORDER);
			command.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
			command.addModifyListener(new ModifyListener() {
				@Override
				public void modifyText(ModifyEvent e) {
					boolean valid = true;
					String message = null;

					String text = command.getText();
					if (text != null && !"".equals(text.trim())) { //$NON-NLS-1$
						IPath p = new Path(text.trim());
						valid = p.toFile().isFile() && p.toFile().canRead() && p.toFile().canExecute();
						if (!valid) message = Messages.PreferencePage_command_invalid;
					}

					setValid(valid);
					setErrorMessage(message);
				}
			});

			commandBrowseButton = new Button(group, SWT.PUSH);
			commandBrowseButton.setText(Messages.PreferencePage_command_button_browse);
			layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
			layoutData.widthHint = Dialog.convertWidthInCharsToPixels(gc.getFontMetrics(), 14);
			commandBrowseButton.setLayoutData(layoutData);
			commandBrowseButton.addSelectionListener(new SelectionAdapter() {
				@Override
                public void widgetSelected(SelectionEvent e) {
					FileDialog dialog = new FileDialog(parent.getShell(), SWT.OPEN);

					String text = command.getText();
					if (text != null && !"".equals(text.trim())) { //$NON-NLS-1$
						IPath p = new Path(text);

						if (p.toFile().isFile() || !p.toFile().exists()) {
							dialog.setFilterPath(p.removeLastSegments(1).toOSString());
							dialog.setFileName(p.lastSegment());
						} else if (p.toFile().isDirectory()) {
							dialog.setFilterPath(p.toOSString());
						}
					}

					String selected = dialog.open();
					if (selected != null) {
						IPath sp = new Path(selected);
						command.setText(sp.toOSString());
					}
				}
			});

			String cmd = UIPlugin.getScopedPreferences().getString(IPreferenceKeys.PREF_LOCAL_TERMINAL_DEFAULT_SHELL_UNIX);
			if (cmd != null && !"".equals(cmd)) { //$NON-NLS-1$
				command.setText(new Path(cmd).toOSString());
			}

			Composite argsPanel = new Composite(group, SWT.NONE);
			GridLayout layout = new GridLayout(2, false);
			layout.marginHeight = 0; layout.marginWidth = 0;
			argsPanel.setLayout(layout);
			layoutData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
			layoutData.horizontalSpan = 2;
			argsPanel.setLayoutData(layoutData);

			label = new Label(argsPanel, SWT.NONE);
			label.setText(Messages.PreferencePage_command_arguments_label);

			arguments = new Text(argsPanel, SWT.SINGLE | SWT.BORDER);
			arguments.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

			String args = UIPlugin.getScopedPreferences().getString(IPreferenceKeys.PREF_LOCAL_TERMINAL_DEFAULT_SHELL_UNIX_ARGS);
			if (args != null && !"".equals(args)) { //$NON-NLS-1$
				arguments.setText(args);
			}

			NoteCompositeHelper.createNoteComposite(group.getFont(), group, Messages.PreferencePage_command_note_label, Messages.PreferencePage_command_note_text);
		}

		Group group = new Group(panel, SWT.NONE);
		group.setText(Messages.PreferencePage_workingDir_label);
		group.setLayout(new GridLayout(hasVariablesButton ? 3 : 2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		workingDir = new Combo(group, SWT.DROP_DOWN);
		Bundle bundle = Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
		if (bundle != null && bundle.getState() != Bundle.UNINSTALLED && bundle.getState() != Bundle.STOPPING) {
			workingDir.setItems(new String[] { Messages.PreferencePage_workingDir_userhome_label, Messages.PreferencePage_workingDir_eclipsehome_label, Messages.PreferencePage_workingDir_eclipsews_label });
		} else {
			workingDir.setItems(new String[] { Messages.PreferencePage_workingDir_userhome_label, Messages.PreferencePage_workingDir_eclipsehome_label });
		}
		workingDir.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		workingDir.select(0);
		workingDir.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				boolean valid = true;
				String message = null;

				String text = workingDir.getText();
				if (text != null && !"".equals(text.trim()) //$NON-NLS-1$
							&& !Messages.PreferencePage_workingDir_userhome_label.equals(text)
							&& !Messages.PreferencePage_workingDir_eclipsehome_label.equals(text)
							&& !Messages.PreferencePage_workingDir_eclipsews_label.equals(text)) {
					try {
						// Resolve possible dynamic variables
						IStringVariableManager vm = VariablesPlugin.getDefault().getStringVariableManager();
						String resolved = vm.performStringSubstitution(text.trim());

						IPath p = new Path(resolved);
						valid = p.toFile().canRead() && p.toFile().isDirectory();
						if (!valid) message = Messages.PreferencePage_workingDir_invalid;
					} catch (CoreException ex) {
						valid = false;
						message = ex.getLocalizedMessage();
					}
				}

				setValid(valid);
				setErrorMessage(message);
			}
		});

		browseButton = new Button(group, SWT.PUSH);
		browseButton.setText(Messages.PreferencePage_workingDir_button_browse);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		layoutData.widthHint = Dialog.convertWidthInCharsToPixels(gc.getFontMetrics(), 14);
		browseButton.setLayoutData(layoutData);
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IPath uh = null;
				IPath eh = null;
				IPath ew = null;

				// HOME
				String home = System.getProperty("user.home"); //$NON-NLS-1$
				if (home != null && !"".equals(home)) uh = new Path(home); //$NON-NLS-1$

				// ECLIPSE_HOME
				String eclipseHomeLocation = System.getProperty("eclipse.home.location"); //$NON-NLS-1$
				if (eclipseHomeLocation != null) {
					try {
						URI uri = URIUtil.fromString(eclipseHomeLocation);
						File f = URIUtil.toFile(uri);
						eh = new Path(f.getAbsolutePath());
					} catch (URISyntaxException ex) { /* ignored on purpose */ }
				}

				// ECLIPSE_WORKSPACE
				Bundle bundle = Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
				if (bundle != null && bundle.getState() != Bundle.UNINSTALLED && bundle.getState() != Bundle.STOPPING) {
			        if (org.eclipse.core.resources.ResourcesPlugin.getWorkspace() != null
			        	            && org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot() != null
			        	            && org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getLocation() != null) {
			        	ew = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getLocation();
			        }
				}

				DirectoryDialog dialog = new DirectoryDialog(parent.getShell(), SWT.OPEN);

				// Determine the filter path
				String text = workingDir.getText();
				if (Messages.PreferencePage_workingDir_userhome_label.equals(text)) {
					dialog.setFilterPath(uh.toOSString());
				} else if (Messages.PreferencePage_workingDir_eclipsehome_label.equals(text)) {
					dialog.setFilterPath(eh.toOSString());
				} else if (Messages.PreferencePage_workingDir_eclipsews_label.equals(text)) {
					dialog.setFilterPath(ew.toOSString());
				} else if (text != null && !"".equals(text.trim())) { //$NON-NLS-1$
					try {
						// Resolve possible dynamic variables
						IStringVariableManager vm = VariablesPlugin.getDefault().getStringVariableManager();
						String resolved = vm.performStringSubstitution(text.trim());
						dialog.setFilterPath(resolved);
					} catch (CoreException ex) {
						if (Platform.inDebugMode()) {
							UIPlugin.getDefault().getLog().log(ex.getStatus());
						}
					}
				}

				String selected = dialog.open();
				if (selected != null) {
					IPath sp = new Path(selected);

					if (uh.equals(sp)) {
						workingDir.select(0);
					} else if (eh.equals(sp)) {
						workingDir.select(1);
					} else if (ew.equals(sp)) {
						workingDir.select(2);
					} else {
						workingDir.setText(sp.toOSString());
					}
				}
			}
		});

		if (hasVariablesButton) {
			variablesButton = new Button(group, SWT.PUSH);
			variablesButton.setText(Messages.PreferencePage_workingDir_button_variables);
			layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
			layoutData.widthHint = Dialog.convertWidthInCharsToPixels(gc.getFontMetrics(), 14);
			variablesButton.setLayoutData(layoutData);
			variablesButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					org.eclipse.debug.ui.StringVariableSelectionDialog dialog = new org.eclipse.debug.ui.StringVariableSelectionDialog(getShell());
					dialog.open();
					String expression = dialog.getVariableExpression();
					if (expression != null) {
						if ("${eclipse_home}".equals(expression)) { //$NON-NLS-1$
							workingDir.select(1);
						} else if ("${workspace_loc}".equals(expression)) { //$NON-NLS-1$
							workingDir.select(2);
						} else {
							workingDir.setText(expression);
						}
					}
				}
			});
		}

		String initialCwd = UIPlugin.getScopedPreferences().getString(IPreferenceKeys.PREF_LOCAL_TERMINAL_INITIAL_CWD);
		if (initialCwd == null || IPreferenceKeys.PREF_INITIAL_CWD_USER_HOME.equals(initialCwd) || "".equals(initialCwd.trim())) { //$NON-NLS-1$
			workingDir.select(0);
		} else if (IPreferenceKeys.PREF_INITIAL_CWD_ECLIPSE_HOME.equals(initialCwd) || "${eclipse_home}".equals(initialCwd)) { //$NON-NLS-1$
			workingDir.select(1);
		} else if (IPreferenceKeys.PREF_INITIAL_CWD_ECLIPSE_WS.equals(initialCwd) || "${workspace_loc}".equals(initialCwd)) { //$NON-NLS-1$
			workingDir.select(2);
		} else {
			workingDir.setText(new Path(initialCwd).toOSString());
		}

		NoteCompositeHelper.createNoteComposite(group.getFont(), group, Messages.PreferencePage_workingDir_note_label, Messages.PreferencePage_workingDir_note_text);

		group = new Group(panel, SWT.NONE);
		group.setText(Messages.PreferencePage_executables_label);
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));

		viewer = new TableViewer(group, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);

		Table table = viewer.getTable();
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

        TableColumn column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.PreferencePage_executables_column_name_label);
        column = new TableColumn(table, SWT.LEFT);
        column.setText(Messages.PreferencePage_executables_column_path_label);

		ColumnViewerToolTipSupport.enableFor(viewer);

		TableLayout tableLayout = new TableLayout();
		tableLayout.addColumnData(new ColumnWeightData(35));
		tableLayout.addColumnData(new ColumnWeightData(65));
		table.setLayout(tableLayout);

		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.heightHint = Dialog.convertHeightInCharsToPixels(gc.getFontMetrics(), 10);
		table.setLayoutData(layoutData);

		Composite buttonsPanel = new Composite(group, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0; layout.marginWidth = 0;
		buttonsPanel.setLayout(layout);
		buttonsPanel.setLayoutData(new GridData(SWT.LEAD, SWT.BEGINNING, false, false));

		addButton = new Button(buttonsPanel, SWT.PUSH);
		addButton.setText(Messages.PreferencePage_executables_button_add_label);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, false, false);
		layoutData.widthHint = Dialog.convertWidthInCharsToPixels(gc.getFontMetrics(), 10);
		addButton.setLayoutData(layoutData);
		addButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ExternalExecutablesDialog dialog = new ExternalExecutablesDialog(PreferencePage.this.getShell(), false);
				if (dialog.open() == Window.OK) {
					// Get the executable properties and add it to the the list
					Map<String, String> executableData = dialog.getExecutableData();
					if (executableData != null && !executables.contains(executableData)) {
						executables.add(executableData);
						viewer.refresh();
					}
				}
			}
		});

		editButton = new Button(buttonsPanel, SWT.PUSH);
		editButton.setText(Messages.PreferencePage_executables_button_edit_label);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.widthHint = Dialog.convertWidthInCharsToPixels(gc.getFontMetrics(), 10);
		editButton.setLayoutData(layoutData);
		editButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
            @Override
			public void widgetSelected(SelectionEvent e) {
				ISelection s = viewer.getSelection();
				if (s instanceof IStructuredSelection && !s.isEmpty()) {
					Object element = ((IStructuredSelection)s).getFirstElement();
					if (element instanceof Map) {
						final Map<String, String> m = (Map<String, String>)element;
						ExternalExecutablesDialog dialog = new ExternalExecutablesDialog(PreferencePage.this.getShell(), true);
						dialog.setExecutableData(m);
						if (dialog.open() == Window.OK) {
							Map<String, String> executableData = dialog.getExecutableData();
							if (executableData != null) {
								m.clear();
								m.putAll(executableData);
								viewer.refresh();
							}
						}
					}
				}
			}
		});

		removeButton = new Button(buttonsPanel, SWT.PUSH);
		removeButton.setText(Messages.PreferencePage_executables_button_remove_label);
		layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.widthHint = Dialog.convertWidthInCharsToPixels(gc.getFontMetrics(), 10);
		removeButton.setLayoutData(layoutData);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
            @Override
			public void widgetSelected(SelectionEvent e) {
				ISelection s = viewer.getSelection();
				if (s instanceof IStructuredSelection && !s.isEmpty()) {
					Iterator<?> iterator = ((IStructuredSelection)s).iterator();
					while (iterator.hasNext()) {
						Object element = iterator.next();
						if (element instanceof Map) {
							Map<String, Object> m = (Map<String, Object>)element;
							executables.remove(m);
						}
						viewer.refresh();
					}
				}
			}
		});

		viewer.setContentProvider(new IStructuredContentProvider() {
			@Override
			public Object[] getElements(Object inputElement) {
				if (inputElement instanceof List && !((List<?>)inputElement).isEmpty()) {
					return ((List<?>)inputElement).toArray();
				}
				return NO_ELEMENTS;
			}

			@Override
			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}

			@Override
			public void dispose() {
			}
		});

		viewer.setLabelProvider(new ITableLabelProvider() {
			@SuppressWarnings("unchecked")
			@Override
			public String getColumnText(Object element, int columnIndex) {
				if (element instanceof Map) {
                    Map<String, Object> m = (Map<String, Object>)element;

                    switch (columnIndex) {
                    case 0:
                    	return (String)m.get(IExternalExecutablesProperties.PROP_NAME);
                    case 1:
                    	return (String)m.get(IExternalExecutablesProperties.PROP_PATH);
                    }
				}
				return null;
			}

			@SuppressWarnings("unchecked")
            @Override
			public Image getColumnImage(Object element, int columnIndex) {
				Image i = null;

				if (element instanceof Map) {
					switch (columnIndex) {
					case 0:
	                    Map<String, Object> m = (Map<String, Object>)element;
						String icon = (String) m.get(IExternalExecutablesProperties.PROP_ICON);
						if (icon != null) {
							i = images.get(icon);
							if (i == null) {
								ImageData id = ExternalExecutablesManager.loadImage(icon);
								if (id != null) {
									ImageDescriptor d = ImageDescriptor.createFromImageData(id);
									if (d != null) i = d.createImage();
									if (i != null) images.put(icon, i);
								}
							}
						}
						break;
					case 1:
						break;
					}
				}

				return i;
			}

			@Override
			public void removeListener(ILabelProviderListener listener) {
			}

			@Override
			public boolean isLabelProperty(Object element, String property) {
				return false;
			}

			@Override
			public void dispose() {
			}

			@Override
			public void addListener(ILabelProviderListener listener) {
			}
		});

		List<Map<String, String>> l = ExternalExecutablesManager.load();
		if (l != null) executables.addAll(l);

		viewer.setInput(executables);

		viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				updateButtons();
			}
		});

		updateButtons();

		gc.dispose();

		return panel;
	}

	/**
	 * Updates the button states.
	 */
	protected void updateButtons() {
		if (viewer != null) {
			addButton.setEnabled(true);

			ISelection selection = viewer.getSelection();

			boolean hasSelection = selection != null && !selection.isEmpty();
			int count = selection instanceof IStructuredSelection ? ((IStructuredSelection)selection).size() : 0;

			editButton.setEnabled(hasSelection && count == 1);
			removeButton.setEnabled(hasSelection && count > 0);
		} else {
			addButton.setEnabled(false);
			editButton.setEnabled(false);
			removeButton.setEnabled(false);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		if (!Platform.OS_WIN32.equals(Platform.getOS())) {
			command.setText(""); //$NON-NLS-1$
			arguments.setText(""); //$NON-NLS-1$
		}

		String initialCwd = UIPlugin.getScopedPreferences().getDefaultString(IPreferenceKeys.PREF_LOCAL_TERMINAL_INITIAL_CWD);
		if (initialCwd == null || IPreferenceKeys.PREF_INITIAL_CWD_USER_HOME.equals(initialCwd) || "".equals(initialCwd.trim())) { //$NON-NLS-1$
			workingDir.select(0);
		} else if (IPreferenceKeys.PREF_INITIAL_CWD_ECLIPSE_HOME.equals(initialCwd) || "${eclipse_home}".equals(initialCwd)) { //$NON-NLS-1$
			workingDir.select(1);
		} else if (IPreferenceKeys.PREF_INITIAL_CWD_ECLIPSE_WS.equals(initialCwd) || "${workspace_loc}".equals(initialCwd)) { //$NON-NLS-1$
			workingDir.select(2);
		} else {
			workingDir.setText(new Path(initialCwd).toOSString());
		}

		executables.clear();
		List<Map<String, String>> l = ExternalExecutablesManager.load();
		if (l != null) executables.addAll(l);
		viewer.refresh();

	    super.performDefaults();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		if (!Platform.OS_WIN32.equals(Platform.getOS())) {
			String text = command.getText();
			IPath p = new Path(text.trim());
			UIPlugin.getScopedPreferences().putString(IPreferenceKeys.PREF_LOCAL_TERMINAL_DEFAULT_SHELL_UNIX, p.toFile().isFile() && p.toFile().canRead() && p.toFile().canExecute() ? p.toOSString() : null);

			text = arguments.getText();
			UIPlugin.getScopedPreferences().putString(IPreferenceKeys.PREF_LOCAL_TERMINAL_DEFAULT_SHELL_UNIX_ARGS, !"".equals(text.trim()) ? text.trim() : null); //$NON-NLS-1$
		}

		String text = workingDir.getText();
		if (text == null || Messages.PreferencePage_workingDir_userhome_label.equals(text) || "".equals(text.trim())) { //$NON-NLS-1$
			UIPlugin.getScopedPreferences().putString(IPreferenceKeys.PREF_LOCAL_TERMINAL_INITIAL_CWD, null);
		} else if (Messages.PreferencePage_workingDir_eclipsehome_label.equals(text)) {
			UIPlugin.getScopedPreferences().putString(IPreferenceKeys.PREF_LOCAL_TERMINAL_INITIAL_CWD, IPreferenceKeys.PREF_INITIAL_CWD_ECLIPSE_HOME);
		} else if (Messages.PreferencePage_workingDir_eclipsews_label.equals(text)) {
			UIPlugin.getScopedPreferences().putString(IPreferenceKeys.PREF_LOCAL_TERMINAL_INITIAL_CWD, IPreferenceKeys.PREF_INITIAL_CWD_ECLIPSE_WS);
		} else {
			try {
				// Resolve possible dynamic variables
				IStringVariableManager vm = VariablesPlugin.getDefault().getStringVariableManager();
				String resolved = vm.performStringSubstitution(text.trim());

				IPath p = new Path(resolved);
				UIPlugin.getScopedPreferences().putString(IPreferenceKeys.PREF_LOCAL_TERMINAL_INITIAL_CWD, p.toFile().canRead() && p.toFile().isDirectory() ? text.trim() : null);
			} catch (CoreException e) {
				if (Platform.inDebugMode()) {
					UIPlugin.getDefault().getLog().log(e.getStatus());
				}
			}
		}

		ExternalExecutablesManager.save(executables);

	    return super.performOk();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		for (Image i : images.values()) {
			i.dispose();
		}
		images.clear();
	    super.dispose();
	}
}
