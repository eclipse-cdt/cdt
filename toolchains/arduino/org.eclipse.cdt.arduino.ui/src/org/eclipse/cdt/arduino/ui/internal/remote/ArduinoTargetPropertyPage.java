/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.arduino.ui.internal.remote;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoard;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.cdt.arduino.ui.internal.Messages;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.dialogs.PropertyPage;

public class ArduinoTargetPropertyPage extends PropertyPage {

	private Combo portSelector;
	private Combo boardSelector;

	private ArduinoBoard[] boards;

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		IRemoteConnection remoteConnection = getElement().getAdapter(IRemoteConnection.class);
		ArduinoRemoteConnection arduinoRemote = remoteConnection.getService(ArduinoRemoteConnection.class);

		Label portLabel = new Label(comp, SWT.NONE);
		portLabel.setText(Messages.ArduinoTargetPropertyPage_0);

		portSelector = new Combo(comp, SWT.NONE);
		portSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		String currentPort = arduinoRemote.getPortName();
		try {
			int i = 0;
			int portIdx = -1;
			String[] ports = SerialPort.list();
			for (String port : ports) {
				portSelector.add(port);
				if (port.equals(currentPort))
					portIdx = i;
				++i;
			}
			if (portIdx >= 0)
				portSelector.select(portIdx);
			if (ports.length == 0)
				portSelector.setText(Messages.ArduinoTargetPropertyPage_1);
		} catch (IOException e) {
			Activator.log(e);
		}

		Label boardLabel = new Label(comp, SWT.NONE);
		boardLabel.setText(Messages.ArduinoTargetPropertyPage_2);

		boardSelector = new Combo(comp, SWT.READ_ONLY);
		boardSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		try {
			ArduinoBoard currentBoard = arduinoRemote.getBoard();
			Collection<ArduinoBoard> boardList = Activator.getService(ArduinoManager.class).getInstalledBoards();
			boards = new ArduinoBoard[boardList.size()];
			int i = 0;
			int boardSel = 0;
			for (ArduinoBoard board : boardList) {
				boards[i] = board;
				boardSelector.add(board.getName());
				if (board.equals(currentBoard)) {
					boardSel = i;
				}
				i++;
			}
			boardSelector.select(boardSel);
		} catch (CoreException e) {
			Activator.log(e);
		}

		return comp;
	}

	@Override
	public boolean performOk() {
		IRemoteConnection remoteConnection = getElement().getAdapter(IRemoteConnection.class);
		IRemoteConnectionWorkingCopy workingCopy = remoteConnection.getWorkingCopy();

		ArduinoBoard board = boards[boardSelector.getSelectionIndex()];
		ArduinoRemoteConnection.setBoardId(workingCopy, board);

		int idx = portSelector.getSelectionIndex();
		if (idx >= 0) {
			ArduinoRemoteConnection.setPortName(workingCopy, portSelector.getItem(idx));
		}

		try {
			workingCopy.save();
		} catch (RemoteConnectionException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
		return true;
	}

}
