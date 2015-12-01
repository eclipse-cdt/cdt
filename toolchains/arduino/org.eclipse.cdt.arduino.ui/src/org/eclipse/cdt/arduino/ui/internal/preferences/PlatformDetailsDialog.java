/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.preferences;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoard;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPlatform;
import org.eclipse.cdt.arduino.ui.internal.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class PlatformDetailsDialog extends Dialog {

	private final ArduinoPlatform platform;

	protected PlatformDetailsDialog(Shell parentShell, ArduinoPlatform platform) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.platform = platform;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite control = (Composite) super.createDialogArea(parent);

		Text text = new Text(control, SWT.BORDER | SWT.READ_ONLY | SWT.MULTI | SWT.V_SCROLL);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		StringBuilder str = new StringBuilder();

		str.append(Messages.PlatformDetailsDialog_0);
		str.append(platform.getName());
		str.append('\n');

		str.append(Messages.PlatformDetailsDialog_1);
		List<ArduinoBoard> boards = platform.getBoards();
		Collections.sort(boards, new Comparator<ArduinoBoard>() {
			@Override
			public int compare(ArduinoBoard o1, ArduinoBoard o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		for (ArduinoBoard board : platform.getBoards()) {
			str.append("   "); //$NON-NLS-1$
			str.append(board.getName());
			str.append('\n');
		}

		text.setText(str.toString());
		return control;
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
	}

}
