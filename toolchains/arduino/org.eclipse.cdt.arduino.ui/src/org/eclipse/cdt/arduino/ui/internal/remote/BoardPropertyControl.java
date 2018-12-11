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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.cdt.arduino.core.internal.HierarchicalProperties;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoBoard;
import org.eclipse.cdt.arduino.core.internal.board.ArduinoManager;
import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.cdt.arduino.ui.internal.Messages;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;

public class BoardPropertyControl extends Composite {

	private Combo portCombo;
	private String[] portNames;
	private String portName;

	private Combo boardCombo;
	private ArduinoBoard[] boards;
	private ArduinoBoard board;

	private List<SelectionListener> listeners = Collections.synchronizedList(new ArrayList<SelectionListener>());
	private List<Control> menuControls = new ArrayList<>();

	private Label programmerLabel;
	private Combo programmerCombo;

	public BoardPropertyControl(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Label portLabel = new Label(this, SWT.NONE);
		portLabel.setText(Messages.NewArduinoTargetWizardPage_4);

		portCombo = new Combo(this, SWT.NONE);
		portCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		try {
			portNames = SerialPort.list();
		} catch (IOException e) {
			portNames = new String[0];
			Activator.log(e);
		}
		for (String portName : portNames) {
			portCombo.add(portName);
		}
		if (portNames.length > 0) {
			portCombo.select(0);
			portName = portNames[0];
		} else {
			portName = ""; //$NON-NLS-1$
		}
		portCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				portName = portCombo.getText();
				fireSelection();
			}
		});

		Label boardLabel = new Label(this, SWT.NONE);
		boardLabel.setText(Messages.ArduinoTargetPropertyPage_2);

		boardCombo = new Combo(this, SWT.READ_ONLY);
		boardCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		try {
			List<ArduinoBoard> boardList = new ArrayList<>(
					Activator.getService(ArduinoManager.class).getInstalledBoards());
			Collections.sort(boardList, new Comparator<ArduinoBoard>() {
				@Override
				public int compare(ArduinoBoard o1, ArduinoBoard o2) {
					return o1.getName().compareTo(o2.getName());
				}
			});
			boards = boardList.toArray(new ArduinoBoard[boardList.size()]);

			for (ArduinoBoard board : boards) {
				boardCombo.add(board.getName());
			}

			boardCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					boardChanged();
					fireSelection();
				}
			});

			if (boards.length > 0) {
				// TODO use preference to remember the last selected board
				boardCombo.select(0);
				board = boards[0];
				updateBoardMenu();
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	public String getPortName() {
		return portName;
	}

	public ArduinoBoard getSelectedBoard() {
		return board;
	}

	public void addSelectionListener(SelectionListener listener) {
		listeners.add(listener);
	}

	private void updateBoardMenu() {
		HierarchicalProperties menus = board.getMenus();
		if (menus != null) {
			for (Entry<String, HierarchicalProperties> menuEntry : menus.getChildren().entrySet()) {
				Label label = new Label(this, SWT.NONE);
				label.setText(board.getPlatform().getMenuText(menuEntry.getKey()) + ':');
				label.setData(menuEntry.getKey());
				menuControls.add(label);

				Combo combo = new Combo(this, SWT.READ_ONLY);
				combo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
				menuControls.add(combo);

				List<String> ids = new ArrayList<>();
				for (Entry<String, HierarchicalProperties> valueEntry : menuEntry.getValue().getChildren().entrySet()) {
					String value = valueEntry.getValue().getValue();
					if (value != null) {
						combo.add(value);
						ids.add(valueEntry.getKey());
					}
				}
				combo.setData(ids);
				combo.select(0);
			}
		}

		try {
			HierarchicalProperties programmers = board.getPlatform().getProgrammers();
			if (programmers != null && programmers.getChildren() != null) {
				programmerLabel = new Label(this, SWT.NONE);
				programmerLabel.setText("Programmer:"); //$NON-NLS-1$

				programmerCombo = new Combo(this, SWT.READ_ONLY);
				programmerCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

				List<String> ids = new ArrayList<>();
				for (Entry<String, HierarchicalProperties> programmer : programmers.getChildren().entrySet()) {
					ids.add(programmer.getKey());
					String name = programmer.getValue().getChild("name").getValue(); //$NON-NLS-1$
					programmerCombo.add(name);
				}
				programmerCombo.setData(ids);
				programmerCombo.select(0);
			}
		} catch (CoreException e) {
			Activator.log(e);
		}
	}

	private void boardChanged() {
		int index = boardCombo.getSelectionIndex();
		ArduinoBoard newBoard = index < 0 ? null : boards[index];
		if (newBoard != board) {
			// Clear out old menus
			for (Control control : menuControls) {
				control.dispose();
			}
			menuControls.clear();
			if (programmerLabel != null) {
				programmerLabel.dispose();
				programmerCombo.dispose();
			}

			board = newBoard;
			updateBoardMenu();

			layout();
			getShell().pack();
			redraw();
		}
	}

	private void fireSelection() {
		for (SelectionListener listener : listeners) {
			Event event = new Event();
			event.widget = this;
			listener.widgetSelected(new SelectionEvent(event));
		}
	}

	public void apply(IRemoteConnectionWorkingCopy workingCopy) {
		ArduinoRemoteConnection.setBoardId(workingCopy, board);
		ArduinoRemoteConnection.setPortName(workingCopy, portName);

		String key = null;
		for (Control control : menuControls) {
			if (control instanceof Label) {
				key = (String) control.getData();
			} else if (control instanceof Combo) {
				Combo combo = (Combo) control;
				@SuppressWarnings("unchecked")
				String value = ((List<String>) combo.getData()).get(combo.getSelectionIndex());

				if (key != null) {
					ArduinoRemoteConnection.setMenuValue(workingCopy, key, value);
				}
			}
		}

		if (programmerCombo != null && !programmerCombo.isDisposed()) {
			@SuppressWarnings("unchecked")
			String programmer = ((List<String>) programmerCombo.getData()).get(programmerCombo.getSelectionIndex());
			ArduinoRemoteConnection.setProgrammer(workingCopy, programmer);
		}
	}

	public void updateFromOriginal(ArduinoRemoteConnection arduinoService) throws CoreException {
		// Set and select the portname
		portCombo.setText(arduinoService.getPortName());
		portName = arduinoService.getPortName();

		// Set and select the board
		boardCombo.setText(arduinoService.getBoard().getName());
		boardChanged();

		// Lock changing of board
		boardCombo.setEnabled(false);

		// Set the programmer
		if (!arduinoService.getProgrammer().isEmpty() && programmerCombo != null && !programmerCombo.isDisposed()) {
			programmerCombo.setText(board.getPlatform().getProgrammers().getChild(arduinoService.getProgrammer())
					.getChild("name").getValue()); //$NON-NLS-1$
		}

		// Set all properties based on the displayed Controls.
		String key = null;
		// Loop over all controls. Labels and corresponding combos are saved in order
		for (Control control : menuControls) {
			if (control instanceof Label) {
				// the label is the key
				key = (String) control.getData();
			} else if (control instanceof Combo) {
				if (key != null && !arduinoService.getMenuValue(key).isEmpty()) {
					String selectableValue = board.getMenus().getChild(key).getChild(arduinoService.getMenuValue(key))
							.getValue();
					((Combo) control).setText(selectableValue);
				}
				// reset key
				key = null;
			} else {
				// unexpected order - reset key
				key = null;
			}
		}
	}

}
