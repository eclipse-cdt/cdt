package org.eclipse.cdt.arduino.ui.internal.remote;

import java.io.IOException;
import java.util.Collection;

import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoard;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPackage;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoPlatform;
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

		IRemoteConnection remoteConnection = (IRemoteConnection) getElement().getAdapter(IRemoteConnection.class);
		ArduinoRemoteConnection arduinoRemote = remoteConnection.getService(ArduinoRemoteConnection.class);

		Label portLabel = new Label(comp, SWT.NONE);
		portLabel.setText(Messages.ArduinoTargetPropertyPage_0);

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
			setMessage(Messages.ArduinoTargetPropertyPage_1, ERROR);
			setValid(false);
		}

		Label boardLabel = new Label(comp, SWT.NONE);
		boardLabel.setText(Messages.ArduinoTargetPropertyPage_2);

		boardSelector = new Combo(comp, SWT.READ_ONLY);
		boardSelector.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		try {
			ArduinoBoard currentBoard = arduinoRemote.getBoard();
			Collection<ArduinoBoard> boardList = ArduinoManager.instance.getBoards();
			boards = new ArduinoBoard[boardList.size()];
			i = 0;
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
		IRemoteConnection remoteConnection = (IRemoteConnection) getElement().getAdapter(IRemoteConnection.class);
		IRemoteConnectionWorkingCopy workingCopy = remoteConnection.getWorkingCopy();

		String portName = portSelector.getItem(portSelector.getSelectionIndex());
		workingCopy.setAttribute(ArduinoRemoteConnection.PORT_NAME, portName);

		ArduinoBoard board = boards[boardSelector.getSelectionIndex()];
		workingCopy.setAttribute(ArduinoRemoteConnection.BOARD_NAME, board.getName());
		ArduinoPlatform platform = board.getPlatform();
		workingCopy.setAttribute(ArduinoRemoteConnection.PLATFORM_NAME, platform.getName());
		ArduinoPackage pkg = platform.getPackage();
		workingCopy.setAttribute(ArduinoRemoteConnection.PACKAGE_NAME, pkg.getName());

		try {
			workingCopy.save();
		} catch (RemoteConnectionException e) {
			Activator.getDefault().getLog().log(e.getStatus());
		}
		return true;
	}

}
