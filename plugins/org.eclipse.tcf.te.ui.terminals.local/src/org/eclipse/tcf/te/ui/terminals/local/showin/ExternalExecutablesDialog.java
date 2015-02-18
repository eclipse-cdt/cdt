/*******************************************************************************
 * Copyright (c) 2014, 2015 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.te.ui.terminals.local.showin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.te.ui.terminals.local.help.IContextHelpIds;
import org.eclipse.tcf.te.ui.terminals.local.nls.Messages;
import org.eclipse.tcf.te.ui.terminals.local.showin.interfaces.IExternalExecutablesProperties;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.Bundle;

/**
 * External executables dialog implementation.
 */
public class ExternalExecutablesDialog extends TrayDialog {
	private String contextHelpId = null;
	private final boolean edit;

	private Text name;
	/* default */ Text path;
	private Text args;
	/* default */ Text icon;
	private Button translate;

	/* default */ String last_filter_path = null;
	/* default */ String last_filter_icon = null;

	private Map<String, String> executableData;

	/**
     * Constructor.
     *
	 * @param shell The parent shell or <code>null</code>.
     */
	public ExternalExecutablesDialog(Shell shell, boolean edit) {
	    super(shell);
	    this.edit = edit;

		this.contextHelpId = IContextHelpIds.EXTERNAL_EXECUTABLES_DIALOG;
		setHelpAvailable(true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected final Control createDialogArea(Composite parent) {
		if (contextHelpId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, contextHelpId);
		}

		// Let the super implementation create the dialog area control
		Control control = super.createDialogArea(parent);
		// Setup the inner panel as scrollable composite
		if (control instanceof Composite) {
			ScrolledComposite sc = new ScrolledComposite((Composite)control, SWT.V_SCROLL);

			GridLayout layout = new GridLayout(1, true);
			layout.marginHeight = 0; layout.marginWidth = 0;
			layout.verticalSpacing = 0; layout.horizontalSpacing = 0;

			sc.setLayout(layout);
			sc.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

			sc.setExpandHorizontal(true);
			sc.setExpandVertical(true);

			Composite composite = new Composite(sc, SWT.NONE);
			composite.setLayout(new GridLayout());

			// Setup the dialog area content
			createDialogAreaContent(composite);

			sc.setContent(composite);
			sc.setMinSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

			// Return the scrolled composite as new dialog area control
			control = sc;
		}

		return control;
	}

	/**
	 * Creates the dialog area content.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 */
    protected void createDialogAreaContent(Composite parent) {
    	Assert.isNotNull(parent);

	    setDialogTitle(edit ? Messages.ExternalExecutablesDialog_title_edit : Messages.ExternalExecutablesDialog_title_add);

        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0; layout.marginWidth = 0;
        panel.setLayout(layout);
        GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, true);
        layoutData.widthHint = convertWidthInCharsToPixels(50);
        panel.setLayoutData(layoutData);

        Label label = new Label(panel, SWT.HORIZONTAL);
        label.setText(Messages.ExternalExecutablesDialog_field_name);
        layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        label.setLayoutData(layoutData);

        name = new Text(panel, SWT.HORIZONTAL | SWT.SINGLE | SWT.BORDER);
        layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        layoutData.widthHint = convertWidthInCharsToPixels(30);
        name.setLayoutData(layoutData);
        name.addModifyListener(new ModifyListener() {
        	@Override
        	public void modifyText(ModifyEvent e) {
        		validate();
        	}
        });

        label = new Label(panel, SWT.HORIZONTAL);
        label.setText(Messages.ExternalExecutablesDialog_field_path);
        layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        label.setLayoutData(layoutData);

        Composite panel2 = new Composite(panel, SWT.NONE);
        layout = new GridLayout(2, false);
        layout.marginHeight = 0; layout.marginWidth = 0;
        panel2.setLayout(layout);
        layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        panel2.setLayoutData(layoutData);

        path = new Text(panel2, SWT.HORIZONTAL | SWT.SINGLE | SWT.BORDER);
        layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        layoutData.widthHint = convertWidthInCharsToPixels(30);
        path.setLayoutData(layoutData);
        path.addModifyListener(new ModifyListener() {
        	@Override
        	public void modifyText(ModifyEvent e) {
        		validate();
        	}
        });

        Button button = new Button(panel2, SWT.PUSH);
        button.setText(Messages.ExternalExecutablesDialog_button_browse);
        layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        layoutData.widthHint = convertWidthInCharsToPixels(10);
        button.setLayoutData(layoutData);
        button.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);

    			String selectedFile = path.getText();
    			if (selectedFile != null && selectedFile.trim().length() > 0) {
    				IPath filePath = new Path(selectedFile);
    				// If the selected file points to an directory, use the directory as is
    				IPath filterPath = filePath.toFile().isDirectory() ? filePath : filePath.removeLastSegments(1);
    				while (filterPath != null && filterPath.segmentCount() > 1 && !filterPath.toFile().exists()) {
    					filterPath = filterPath.removeLastSegments(1);
    				}
    				String filterFileName = filePath.toFile().isDirectory() || !filePath.toFile().exists() ? null : filePath.lastSegment();

    				if (filterPath != null && !filterPath.isEmpty()) dialog.setFilterPath(filterPath.toString());
    				if (filterFileName != null) dialog.setFileName(filterFileName);
    			} else {
    				String workspace = null;
    				Bundle bundle = Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
    				if (bundle != null && bundle.getState() != Bundle.UNINSTALLED && bundle.getState() != Bundle.STOPPING) {
    					workspace = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
    				}

    				String filterPath = last_filter_path != null ? last_filter_path : workspace;
    				dialog.setFilterPath(filterPath);
    			}

    			selectedFile = dialog.open();
    			if (selectedFile != null) {
    				last_filter_path = dialog.getFilterPath();
    				path.setText(selectedFile);
    			}
        	}
		});

        label = new Label(panel, SWT.HORIZONTAL);
        label.setText(Messages.ExternalExecutablesDialog_field_args);
        layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        label.setLayoutData(layoutData);

        args = new Text(panel, SWT.HORIZONTAL | SWT.SINGLE | SWT.BORDER);
        layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        layoutData.widthHint = convertWidthInCharsToPixels(30);
        args.setLayoutData(layoutData);
        args.addModifyListener(new ModifyListener() {
        	@Override
        	public void modifyText(ModifyEvent e) {
        		validate();
        	}
        });

        label = new Label(panel, SWT.HORIZONTAL);
        label.setText(Messages.ExternalExecutablesDialog_field_icon);
        layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        label.setLayoutData(layoutData);

        panel2 = new Composite(panel, SWT.NONE);
        layout = new GridLayout(2, false);
        layout.marginHeight = 0; layout.marginWidth = 0;
        panel2.setLayout(layout);
        layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        panel2.setLayoutData(layoutData);

        icon = new Text(panel2, SWT.HORIZONTAL | SWT.SINGLE | SWT.BORDER);
        layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        layoutData.widthHint = convertWidthInCharsToPixels(30);
        icon.setLayoutData(layoutData);
        icon.addModifyListener(new ModifyListener() {
        	@Override
        	public void modifyText(ModifyEvent e) {
        		validate();
        	}
        });

        button = new Button(panel2, SWT.PUSH);
        button.setText(Messages.ExternalExecutablesDialog_button_browse);
        layoutData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
        layoutData.widthHint = convertWidthInCharsToPixels(10);
        button.setLayoutData(layoutData);
        button.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		FileDialog dialog = new FileDialog(getShell(), SWT.OPEN);

    			String selectedFile = icon.getText();
    			if (selectedFile != null && selectedFile.trim().length() > 0) {
    				IPath filePath = new Path(selectedFile);
    				// If the selected file points to an directory, use the directory as is
    				IPath filterPath = filePath.toFile().isDirectory() ? filePath : filePath.removeLastSegments(1);
    				while (filterPath != null && filterPath.segmentCount() > 1 && !filterPath.toFile().exists()) {
    					filterPath = filterPath.removeLastSegments(1);
    				}
    				String filterFileName = filePath.toFile().isDirectory() || !filePath.toFile().exists() ? null : filePath.lastSegment();

    				if (filterPath != null && !filterPath.isEmpty()) dialog.setFilterPath(filterPath.toString());
    				if (filterFileName != null) dialog.setFileName(filterFileName);
    			} else {
    				String workspace = null;
    				Bundle bundle = Platform.getBundle("org.eclipse.core.resources"); //$NON-NLS-1$
    				if (bundle != null && bundle.getState() != Bundle.UNINSTALLED && bundle.getState() != Bundle.STOPPING) {
    					workspace = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().getRoot().getLocation().toOSString();
    				}

    				String filterPath = last_filter_icon != null ? last_filter_icon : workspace;
    				dialog.setFilterPath(filterPath);
    			}

    			selectedFile = dialog.open();
    			if (selectedFile != null) {
    				last_filter_icon = dialog.getFilterPath();
    				icon.setText(selectedFile);
    			}
        	}
		});

        translate = new Button(panel, SWT.CHECK);
        translate.setText(Messages.ExternalExecutablesDialog_field_translate);
        layoutData = new GridData(SWT.FILL, SWT.TOP, true, false);
        layoutData.horizontalSpan = 2;
        translate.setLayoutData(layoutData);
        translate.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		validate();
        	}
		});

        if (executableData != null) {
        	String value = executableData.get(IExternalExecutablesProperties.PROP_NAME);
        	name.setText(value != null && !"".equals(value.trim()) ? value : ""); //$NON-NLS-1$ //$NON-NLS-2$
        	value = executableData.get(IExternalExecutablesProperties.PROP_PATH);
        	path.setText(value != null && !"".equals(value.trim()) ? value : ""); //$NON-NLS-1$ //$NON-NLS-2$
        	value = executableData.get(IExternalExecutablesProperties.PROP_ARGS);
        	args.setText(value != null && !"".equals(value.trim()) ? value : ""); //$NON-NLS-1$ //$NON-NLS-2$
        	value = executableData.get(IExternalExecutablesProperties.PROP_ICON);
        	icon.setText(value != null && !"".equals(value.trim()) ? value : ""); //$NON-NLS-1$ //$NON-NLS-2$
        	value = executableData.get(IExternalExecutablesProperties.PROP_TRANSLATE);
        	translate.setSelection(value != null ? Boolean.parseBoolean(value) : false);
        }
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TrayDialog#createButtonBar(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createButtonBar(Composite parent) {
	    Control control = super.createButtonBar(parent);
	    validate();
	    return control;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButton(org.eclipse.swt.widgets.Composite, int, java.lang.String, boolean)
	 */
	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		if (IDialogConstants.OK_ID == id && !edit) {
			label = Messages.ExternalExecutablesDialog_button_add;
		}
	    return super.createButton(parent, id, label, defaultButton);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tcf.te.ui.jface.dialogs.CustomTrayDialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		if (name != null && path != null) {
			// Extract the executable properties
			if (executableData == null) executableData = new HashMap<String, String>();

			String value = name.getText();
			if (value != null && !"".equals(value.trim())) { //$NON-NLS-1$
				executableData.put(IExternalExecutablesProperties.PROP_NAME, value);
			} else {
				executableData.remove(IExternalExecutablesProperties.PROP_NAME);
			}

			value = path.getText();
			if (value != null && !"".equals(value.trim())) { //$NON-NLS-1$
				executableData.put(IExternalExecutablesProperties.PROP_PATH, value);
			} else {
				executableData.remove(IExternalExecutablesProperties.PROP_PATH);
			}

			value = args.getText();
			if (value != null && !"".equals(value.trim())) { //$NON-NLS-1$
				executableData.put(IExternalExecutablesProperties.PROP_ARGS, value);
			} else {
				executableData.remove(IExternalExecutablesProperties.PROP_ARGS);
			}

			value = icon.getText();
			if (value != null && !"".equals(value.trim())) { //$NON-NLS-1$
				executableData.put(IExternalExecutablesProperties.PROP_ICON, value);
			} else {
				executableData.remove(IExternalExecutablesProperties.PROP_ICON);
			}

			if (translate.getSelection()) {
				executableData.put(IExternalExecutablesProperties.PROP_TRANSLATE, Boolean.TRUE.toString());
			} else {
				executableData.remove(IExternalExecutablesProperties.PROP_TRANSLATE);
			}
		} else {
			executableData = null;
		}
	    super.okPressed();
	}

	@Override
	protected void cancelPressed() {
		// If the user pressed cancel, the dialog needs to return null
		executableData = null;
	    super.cancelPressed();
	}

	/**
	 * Returns the executable properties the user entered.
	 *
	 * @return The executable properties or <code>null</code>.
	 */
	public Map<String, String> getExecutableData() {
		return executableData;
	}

	/**
	 * Set or reset the executable properties. This method has effect
	 * only if called before opening the dialog.
	 *
	 * @param data The executable properties or <code>null</code>.
	 */
	public void setExecutableData(Map<String, String> data) {
		if (data == null) {
			executableData = data;
		} else {
			executableData = new HashMap<String, String>(data);
		}
	}

	/**
	 * Validate the dialog.
	 */
	public void validate() {
		boolean valid = true;

		if (name != null && !name.isDisposed()) {
			valid = !"".equals(name.getText()); //$NON-NLS-1$
		}

		if (path != null && !path.isDisposed()) {
			String value = path.getText();
			if (!"".equals(value)) { //$NON-NLS-1$
				File f = new File(value);
				valid |= f.isAbsolute() && f.canRead();
			} else {
				valid = false;
			}
		}

		if (icon != null && !icon.isDisposed()) {
			String value = icon.getText();
			if (!"".equals(value)) { //$NON-NLS-1$
				File f = new File(value);
				valid |= f.isAbsolute() && f.canRead();
			}
		}

		Button okButton = getButton(IDialogConstants.OK_ID);
		if (okButton != null) okButton.setEnabled(valid);
	}

	/**
	 * Sets the title for this dialog.
	 *
	 * @param title The title.
	 */
	public void setDialogTitle(String title) {
		if (getShell() != null && !getShell().isDisposed()) {
			getShell().setText(title);
		}
	}
}
