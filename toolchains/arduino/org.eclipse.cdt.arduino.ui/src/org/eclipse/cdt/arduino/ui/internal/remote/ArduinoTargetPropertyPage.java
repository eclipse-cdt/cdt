package org.eclipse.cdt.arduino.ui.internal.remote;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.cdt.arduino.core.Board;
import org.eclipse.cdt.arduino.core.IArduinoBoardManager;
import org.eclipse.cdt.arduino.core.IArduinoRemoteConnection;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.cdt.serial.SerialPort;
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
	
	private Board[] boards;

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		IRemoteConnection remoteConnection = (IRemoteConnection) getElement().getAdapter(IRemoteConnection.class);
		IArduinoRemoteConnection arduinoRemote = remoteConnection.getService(IArduinoRemoteConnection.class);

		Label portLabel = new Label(comp, SWT.NONE);
		portLabel.setText("Serial Port:");

		portSelector = new Combo(comp, SWT.READ_ONLY);
		portSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		String currentPort = arduinoRemote.getPortName();
		int i = 0, portSel = -1;
		try {
			for (String port : SerialPort.list()) {
				portSelector.add(port);
				if (port.equals(currentPort)) {
					portSel = i;
				} else {
					portSel = portSel < 0 ? 0 : portSel;
				}
				i++;
			}
		} catch (IOException e) {
			Activator.log(e);
		}
		if (portSel >= 0) {
			portSelector.select(portSel);
		} else {
			setMessage("No serial ports", ERROR);
			setValid(false);
		}

		Label boardLabel = new Label(comp, SWT.NONE);
		boardLabel.setText("Board type:");

		boardSelector = new Combo(comp, SWT.READ_ONLY);
		boardSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Board currentBoard = arduinoRemote.getBoard();
		IArduinoBoardManager boardManager = Activator.getService(IArduinoBoardManager.class);
		Collection<Board> boardList = boardManager.getBoards();
		boards = new Board[boardList.size()];
		i = 0;
		int boardSel = 0;
		for (Board board : boardList) {
			boards[i] = board;
			boardSelector.add(board.getName());
			if (board.equals(currentBoard)) {
				boardSel = i;
			}
			i++;
		}
		boardSelector.select(boardSel);

		return comp;
	}

	@Override
	public boolean performOk() {
		IRemoteConnection remoteConnection = (IRemoteConnection) getElement().getAdapter(IRemoteConnection.class);
		IRemoteConnectionWorkingCopy workingCopy = remoteConnection.getWorkingCopy();

		String portName = portSelector.getItem(portSelector.getSelectionIndex());
		workingCopy.setAttribute(IArduinoRemoteConnection.PORT_NAME, portName);

		Board board = boards[boardSelector.getSelectionIndex()];
		workingCopy.setAttribute(IArduinoRemoteConnection.BOARD_ID, board.getId());

		try {
			workingCopy.save();
		} catch (RemoteConnectionException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
		return true;
	}
	
}
