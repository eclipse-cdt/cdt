/*******************************************************************************
 * Copyright (c) 2007, 2011 Intel Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Intel Corporation - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.ui.newui;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.internal.ui.newui.Messages;
import org.eclipse.core.variables.IStringVariable;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * Displays CDT variables dialog with ability to filter.
 *
 * @noextend This class is not intended to be subclassed by clients.
 */
public class BuildVarListDialog extends ElementListSelectionDialog {

	private static final String TYPE = Messages.BuildVarListDialog_0;
	private IStringVariable[] sysVars = VariablesPlugin.getDefault().getStringVariableManager().getVariables();

	private Text text;
	private Label type;
	private static final String LIST_DESCRIPTION = Messages.BuildVarListDialog_11;

	public BuildVarListDialog(Shell parent, Object[] input) {
		super(parent, new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ICdtVariable)
					return ((ICdtVariable) element).getName();
				return super.getText(element);
			}
		});
		setShellStyle(getShellStyle() | SWT.RESIZE);
		setMessage(Messages.StringVariableSelectionDialog_message);
		setMultipleSelection(false);
		setElements(input);
	}

	@Override
	protected Control createDialogArea(Composite container) {
		Composite c = (Composite) super.createDialogArea(container);

		type = new Label(c, SWT.NONE);
		type.setFont(container.getFont());
		type.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label desc = new Label(c, SWT.NONE);
		desc.setFont(c.getFont());
		desc.setText(Messages.StringVariableSelectionDialog_columnDescription);
		desc.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		text = new Text(c, SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		text.setFont(container.getFont());
		text.setEditable(false);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 50;
		text.setLayoutData(gd);

		//bug 189413 - adding label to build variable list for accessiblity
		if (fFilteredList != null) {
			fFilteredList.getAccessible().addAccessibleListener(new AccessibleAdapter() {
				@Override
				public void getName(AccessibleEvent e) {
					e.result = LIST_DESCRIPTION;
				}
			});
		}

		return c;
	}

	@Override
	protected void handleSelectionChanged() {
		super.handleSelectionChanged();
		Object[] objects = getSelectedElements();
		String descr = null;
		if (objects.length == 1) {
			ICdtVariable v = (ICdtVariable) objects[0];
			// update type name
			type.setText(TYPE + " " + typeIntToString(v.getValueType())); //$NON-NLS-1$
			// search in system variables list
			for (int i = 0; i < sysVars.length; i++) {
				if (v.getName() == sysVars[i].getName()) {
					descr = sysVars[i].getDescription();
					break;
				}
			}
		}
		if (descr == null)
			descr = Messages.BuildVarListDialog_10;
		text.setText(descr);
	}

	public static String typeIntToString(int type) {
		String stringType;
		switch (type) {
		case ICdtVariable.VALUE_TEXT_LIST:
			stringType = Messages.BuildVarListDialog_1;
			break;
		case ICdtVariable.VALUE_PATH_FILE:
			stringType = Messages.BuildVarListDialog_2;
			break;
		case ICdtVariable.VALUE_PATH_FILE_LIST:
			stringType = Messages.BuildVarListDialog_3;
			break;
		case ICdtVariable.VALUE_PATH_DIR:
			stringType = Messages.BuildVarListDialog_4;
			break;
		case ICdtVariable.VALUE_PATH_DIR_LIST:
			stringType = Messages.BuildVarListDialog_5;
			break;
		case ICdtVariable.VALUE_PATH_ANY:
			stringType = Messages.BuildVarListDialog_6;
			break;
		case ICdtVariable.VALUE_PATH_ANY_LIST:
			stringType = Messages.BuildVarListDialog_7;
			break;
		case ICdtVariable.VALUE_TEXT:
		default:
			stringType = Messages.BuildVarListDialog_8;
			break;
		}
		return stringType;
	}

}
