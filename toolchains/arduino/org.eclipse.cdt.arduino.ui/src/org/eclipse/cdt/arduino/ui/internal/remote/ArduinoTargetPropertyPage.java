/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

public class ArduinoTargetPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

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
		portSelector.setText(currentPort);
		try {
			for (String port : SerialPort.list()) {
				portSelector.add(port);
			}
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

		String portName = portSelector.getItem(portSelector.getSelectionIndex());
		ArduinoRemoteConnection.setPortName(workingCopy, portName);

		try {
			workingCopy.save();
		} catch (RemoteConnectionException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
		return true;
	}

}
