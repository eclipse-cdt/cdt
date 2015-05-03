package org.eclipse.cdt.arduino.ui.internal.remote;

import java.io.IOException;

import org.eclipse.cdt.arduino.core.Board;
import org.eclipse.cdt.arduino.core.IArduinoBoardManager;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.cdt.serial.SerialPort;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class NewArduinoTargetWizardPage extends WizardPage {

	String name;
	private Text nameText;

	String portName;
	private String[] portNames;
	private Combo portCombo;

	Board board;
	private Board[] boards;
	private Combo boardCombo;

	public NewArduinoTargetWizardPage() {
		super("NewArduinoTargetPage");
		setDescription("New Arduino Target settings");
		setTitle("New Arduino Target");
	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		Label nameLabel = new Label(comp, SWT.NONE);
		nameLabel.setText("Target name:");

		nameText = new Text(comp, SWT.BORDER | SWT.SINGLE);
		nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		nameText.setText("");
		nameText.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e) {
				updateStatus();
			}

			@Override
			public void keyPressed(KeyEvent e) {
			}
		});

		Label portLabel = new Label(comp, SWT.NONE);
		portLabel.setText("Serial port:");

		portCombo = new Combo(comp, SWT.READ_ONLY);
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
		portCombo.select(0);
		portCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus();
			}
		});

		IArduinoBoardManager boardManager = Activator.getService(IArduinoBoardManager.class);
		
		Label boardLabel = new Label(comp, SWT.NONE);
		boardLabel.setText("Board type:");

		boardCombo = new Combo(comp, SWT.READ_ONLY);
		boardCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		boards = boardManager.getBoards().toArray(new Board[0]);
		for (Board board : boards) {
			boardCombo.add(board.getName());
		}
		boardCombo.select(0);
		boardCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus();
			}
		});

		setControl(comp);
		setPageComplete(false);
	}

	private void updateStatus() {
		name = nameText.getText();

		int portIndex = portCombo.getSelectionIndex();
		portName = portIndex < 0 ? null : portNames[portIndex];

		int boardIndex = boardCombo.getSelectionIndex();
		board = boardIndex < 0 ? null : boards[boardIndex];

		setPageComplete(!name.isEmpty() && portName != null && board != null);
	}

}
