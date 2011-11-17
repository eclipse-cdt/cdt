/*******************************************************************************
 * Copyright (c) 2007, 2009 Intel Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICMultiConfigDescription;
import org.eclipse.cdt.core.settings.model.ICSettingEntry;
import org.eclipse.cdt.ui.CDTSharedImages;

import org.eclipse.cdt.internal.ui.newui.Messages;

/**
 * A combined dialog which allows selecting file or folder from workspace or filesystem
 * and some more features. The dialog is used on "Paths and Symbols" properties page.
 * Note that currently it is used not only for include files/folders but for library
 * files/folders as well.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class IncludeDialog extends AbstractPropertyDialog {
	static final String[] FILTER_INCLUDE_FILE = new String[] {"*.h;*.hpp", "*"}; //$NON-NLS-1$ //$NON-NLS-2$
	static final String[] FILTER_LIBRARY_FILE = new String[] {"*.a;*.so;*.dll;*.lib", "*"}; //$NON-NLS-1$ //$NON-NLS-2$
	public String sdata;
	private Button b_add2confs;
	private Button b_add2langs;
	public Text text;
	private Button b_work;
	private Button b_file;
	private Button b_vars;
	private Button b_ok;
	private Button b_ko;
	private int mode;
	private Button c_wsp;
	private ICConfigurationDescription cfgd;
	private boolean isWsp = false;
	private int kind = 0;

	static final int NEW_FILE = 0;
	static final int NEW_DIR  = 1;
	static final int OLD_FILE = 2;
	static final int OLD_DIR  = 3;

	static final int DIR_MASK = 1;
	static final int OLD_MASK = 2;

	/**
	 * @since 5.3
	 */
	public IncludeDialog(Shell parent, int mode, String title, String data,
			ICConfigurationDescription cfgd, int flags, int kind) {

		super(parent, title);
		this.mode = mode;
		this.sdata = data;
		this.cfgd = cfgd;
		this.isWsp = (flags == ICSettingEntry.VALUE_WORKSPACE_PATH);
		this.kind = kind;
	}

	public IncludeDialog(Shell parent, int mode, String title, String data,
			ICConfigurationDescription cfgd, int flags) {

		this(parent, mode, title, data, cfgd, flags, 0);
	}

	@Override
	protected Control createDialogArea(Composite c) {
		c.setLayout(new GridLayout(2, false));
		GridData gd;

		Label l1 = new Label(c, SWT.NONE);
		if ((mode & DIR_MASK) == DIR_MASK)
			l1.setText(Messages.IncludeDialog_0);
		else
			l1.setText(Messages.IncludeDialog_1);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		l1.setLayoutData(gd);

		text = new Text(c, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.widthHint = 300;
		text.setLayoutData(gd);
		if ((mode & OLD_MASK) == OLD_MASK) { text.setText(sdata); }
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setButtons();
			}});

// Checkboxes
		Composite c1 = new Composite (c, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = SWT.TOP;
		c1.setLayoutData(gd);
		c1.setLayout(new GridLayout(1, false));

		b_add2confs = new Button(c1, SWT.CHECK);
		b_add2confs.setText(Messages.IncludeDialog_2);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		if (((mode & OLD_MASK) == OLD_MASK) ||
				(cfgd instanceof ICMultiConfigDescription)) {
			gd.heightHint = 1;
			b_add2confs.setVisible(false);
		}
		b_add2confs.setLayoutData(gd);

		b_add2langs = new Button(c1, SWT.CHECK);
		b_add2langs.setText(Messages.IncludeDialog_3);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		if ((mode & OLD_MASK) == OLD_MASK) {
			gd.heightHint = 1;
			b_add2langs.setVisible(false);
		}
		b_add2langs.setLayoutData(gd);

		c_wsp = new Button(c1, SWT.CHECK);
		c_wsp.setText(Messages.ExpDialog_4);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		c_wsp.setLayoutData(gd);
		c_wsp.setSelection(isWsp);
		c_wsp.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				c_wsp.setImage(getWspImage(c_wsp.getSelection()));
			}});
		c_wsp.setImage(getWspImage(isWsp));

// Buttons
		Composite c2 = new Composite (c, SWT.NONE);
		gd = new GridData(GridData.END);
		c2.setLayoutData(gd);
		c2.setLayout(new GridLayout(2, true));

		new Label(c2, 0).setLayoutData(new GridData()); // placeholder
		b_vars = setupButton(c2, AbstractCPropertyTab.VARIABLESBUTTON_NAME);

		new Label(c2, 0).setLayoutData(new GridData()); // placeholder
		b_work = setupButton(c2, AbstractCPropertyTab.WORKSPACEBUTTON_NAME);

		new Label(c2, 0).setLayoutData(new GridData()); // placeholder
		b_file = setupButton(c2, AbstractCPropertyTab.FILESYSTEMBUTTON_NAME);

		b_ok = setupButton(c2, IDialogConstants.OK_LABEL);
		b_ko = setupButton(c2, IDialogConstants.CANCEL_LABEL);

		c.getShell().setDefaultButton(b_ok);
		c.pack();

		// resize (bug #189333)
		int x = b_ko.getBounds().width * 3 + 10;
		int y = c.getBounds().width - 10;
		if (x > y) {
			((GridData)(text.getLayoutData())).widthHint = x;
			c.pack();
		}

		setButtons();
		return c;
	}

	private void setButtons() {
		b_ok.setEnabled(text.getText().trim().length() > 0);
	}

	@Override
	public void buttonPressed(SelectionEvent e) {
		String s;
		if (e.widget.equals(b_ok)) {
			text1 = text.getText();
			check1 = b_add2confs.getSelection();
			check2 = c_wsp.getSelection();
			check3 = b_add2langs.getSelection();
			result = true;
			shell.dispose();
		} else if (e.widget.equals(b_ko)) {
			shell.dispose();
		} else if (e.widget.equals(b_work)) {
			if ((mode & DIR_MASK)== DIR_MASK)
				s = AbstractCPropertyTab.getWorkspaceDirDialog(shell, text.getText());
			else
				s = AbstractCPropertyTab.getWorkspaceFileDialog(shell, text.getText());
			if (s != null) {
				s = strip_wsp(s);
				text.setText(s);
				c_wsp.setSelection(true);
				c_wsp.setImage(getWspImage(c_wsp.getSelection()));
			}
		} else if (e.widget.equals(b_file)) {
			if ((mode & DIR_MASK)== DIR_MASK) {
				s = AbstractCPropertyTab.getFileSystemDirDialog(shell, text.getText());
			} else {
				if (kind==ICSettingEntry.INCLUDE_FILE)
					s = AbstractCPropertyTab.getFileSystemFileDialog(shell, text.getText(), FILTER_INCLUDE_FILE);
				else if (kind==ICSettingEntry.LIBRARY_FILE)
					s = AbstractCPropertyTab.getFileSystemFileDialog(shell, text.getText(), FILTER_LIBRARY_FILE);
				else
					s = AbstractCPropertyTab.getFileSystemFileDialog(shell, text.getText());
			}
			if (s != null) {
				text.setText(s);
				c_wsp.setSelection(false);
				c_wsp.setImage(getWspImage(c_wsp.getSelection()));
			}
		} else if (e.widget.equals(b_vars)) {
			s = AbstractCPropertyTab.getVariableDialog(shell, cfgd);
			if (s != null) text.insert(s);
		}
	}

	static private Image getWspImage(boolean isWsp) {
		final Image IMG_WORKSPACE = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_WORKSPACE);
		final Image IMG_FILESYSTEM = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_FOLDER);
		return isWsp ? IMG_WORKSPACE : IMG_FILESYSTEM;
	}

}
