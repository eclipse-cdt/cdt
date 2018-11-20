package org.eclipse.cdt.arduino.ui.internal.terminal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.arduino.core.internal.remote.ArduinoRemoteConnection;
import org.eclipse.cdt.arduino.ui.internal.Activator;
import org.eclipse.cdt.arduino.ui.internal.Messages;
import org.eclipse.cdt.serial.BaudRate;
import org.eclipse.cdt.serial.ByteSize;
import org.eclipse.cdt.serial.Parity;
import org.eclipse.cdt.serial.StopBits;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionType;
import org.eclipse.remote.core.IRemoteServicesManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tm.internal.terminal.provisional.api.AbstractSettingsPage;
import org.eclipse.tm.terminal.connector.cdtserial.connector.SerialSettings;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanel;
import org.eclipse.tm.terminal.view.ui.interfaces.IConfigurationPanelContainer;

public class ArduinoTerminalSettingsPage extends AbstractSettingsPage {

	private final ArduinoTerminalSettings settings;
	private final IConfigurationPanel panel;
	private final IDialogSettings dialogSettings;

	private Combo boardCombo;
	private Label portNameLabel;
	private Combo baudRateCombo;
	private Combo byteSizeCombo;
	private Combo parityCombo;
	private Combo stopBitsCombo;

	private String boardName;
	private String portName;
	private BaudRate baudRate;
	private ByteSize byteSize;
	private Parity parity;
	private StopBits stopBits;

	private IRemoteConnectionType arduinoType;

	public ArduinoTerminalSettingsPage(ArduinoTerminalSettings settings, IConfigurationPanel panel) {
		this.settings = settings;
		this.panel = panel;
		setHasControlDecoration(true);

		dialogSettings = DialogSettings.getOrCreateSection(Activator.getDefault().getDialogSettings(),
				this.getClass().getSimpleName());

		boardName = dialogSettings.get(ArduinoTerminalSettings.BOARD_ATTR);
		portName = dialogSettings.get(SerialSettings.PORT_NAME_ATTR);

		String baudRateStr = dialogSettings.get(SerialSettings.BAUD_RATE_ATTR);
		if (baudRateStr == null || baudRateStr.isEmpty()) {
			baudRate = BaudRate.getDefault();
		} else {
			String[] rates = BaudRate.getStrings();
			for (int i = 0; i < rates.length; ++i) {
				if (baudRateStr.equals(rates[i])) {
					baudRate = BaudRate.fromStringIndex(i);
					break;
				}
			}
		}

		String byteSizeStr = dialogSettings.get(SerialSettings.BYTE_SIZE_ATTR);
		if (byteSizeStr == null || byteSizeStr.isEmpty()) {
			byteSize = ByteSize.getDefault();
		} else {
			String[] sizes = ByteSize.getStrings();
			for (int i = 0; i < sizes.length; ++i) {
				if (byteSizeStr.equals(sizes[i])) {
					byteSize = ByteSize.fromStringIndex(i);
					break;
				}
			}
		}

		String parityStr = dialogSettings.get(SerialSettings.PARITY_ATTR);
		if (parityStr == null || parityStr.isEmpty()) {
			parity = Parity.getDefault();
		} else {
			String[] parities = Parity.getStrings();
			for (int i = 0; i < parities.length; ++i) {
				if (parityStr.equals(parities[i])) {
					parity = Parity.fromStringIndex(i);
					break;
				}
			}
		}

		String stopBitsStr = dialogSettings.get(SerialSettings.STOP_BITS_ATTR);
		if (stopBitsStr == null || stopBitsStr.isEmpty()) {
			stopBits = StopBits.getDefault();
		} else {
			String[] bits = StopBits.getStrings();
			for (int i = 0; i < bits.length; ++i) {
				if (stopBitsStr.equals(bits[i])) {
					stopBits = StopBits.fromStringIndex(i);
					break;
				}
			}
		}

	}

	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginWidth = gridLayout.marginHeight = 0;
		GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayout(gridLayout);
		comp.setLayoutData(gridData);

		Label boardLabel = new Label(comp, SWT.NONE);
		boardLabel.setText(Messages.ArduinoTerminalSettingsPage_BoardName);

		boardCombo = new Combo(comp, SWT.READ_ONLY);
		boardCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		IRemoteServicesManager manager = Activator.getService(IRemoteServicesManager.class);
		arduinoType = manager.getConnectionType(ArduinoRemoteConnection.TYPE_ID);
		List<IRemoteConnection> connections = new ArrayList<>(arduinoType.getConnections());
		Collections.sort(connections, (o1, o2) -> {
			return o1.getName().compareToIgnoreCase(o2.getName());
		});
		for (IRemoteConnection connection : connections) {
			boardCombo.add(connection.getName());
		}
		boardCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validate();
				updatePortLabel();
			}
		});

		Label portLabel = new Label(comp, SWT.NONE);
		portLabel.setText(Messages.ArduinoTerminalSettingsPage_SerialPort);

		portNameLabel = new Label(comp, SWT.NONE);
		portNameLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		Label baudRateLabel = new Label(comp, SWT.NONE);
		baudRateLabel.setText(Messages.ArduinoTerminalSettingsPage_BaudRate);

		baudRateCombo = new Combo(comp, SWT.READ_ONLY);
		baudRateCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String baudRateStr : BaudRate.getStrings()) {
			baudRateCombo.add(baudRateStr);
		}

		Label byteSizeLabel = new Label(comp, SWT.NONE);
		byteSizeLabel.setText(Messages.ArduinoTerminalSettingsPage_DataSize);

		byteSizeCombo = new Combo(comp, SWT.READ_ONLY);
		byteSizeCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String byteSizeStr : ByteSize.getStrings()) {
			byteSizeCombo.add(byteSizeStr);
		}

		Label parityLabel = new Label(comp, SWT.NONE);
		parityLabel.setText(Messages.ArduinoTerminalSettingsPage_Parity);

		parityCombo = new Combo(comp, SWT.READ_ONLY);
		parityCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String parityStr : Parity.getStrings()) {
			parityCombo.add(parityStr);
		}

		Label stopBitsLabel = new Label(comp, SWT.NONE);
		stopBitsLabel.setText(Messages.ArduinoTerminalSettingsPage_StopBits);

		stopBitsCombo = new Combo(comp, SWT.READ_ONLY);
		stopBitsCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		for (String stopBitsStr : StopBits.getStrings()) {
			stopBitsCombo.add(stopBitsStr);
		}

		loadSettings();
	}

	void validate() {
		IConfigurationPanelContainer container = panel.getContainer();
		container.validate();
	}

	void updatePortLabel() {
		if (boardCombo.getSelectionIndex() < 0) {
			return;
		}

		String boardName = boardCombo.getItem(boardCombo.getSelectionIndex());
		IRemoteConnection connection = arduinoType.getConnection(boardName);
		if (connection != null) {
			ArduinoRemoteConnection board = connection.getService(ArduinoRemoteConnection.class);
			portName = board.getPortName();
			portNameLabel.setText(portName);
		} else {
			portName = null;
			portNameLabel.setText(Messages.ArduinoTerminalSettingsPage_UnknownPort);
		}
	}

	@Override
	public void loadSettings() {
		String boardName = settings.getBoardName();
		if (boardName == null || boardName.isEmpty()) {
			boardName = this.boardName;
		}
		if (boardName != null && !boardName.isEmpty()) {
			int i = 0;
			for (String name : boardCombo.getItems()) {
				if (boardName.equals(name)) {
					boardCombo.select(i);
					break;
				}
				i++;
			}
		} else if (boardCombo.getItemCount() > 0) {
			boardCombo.select(0);
		}

		updatePortLabel();

		BaudRate baudRate = settings.getBaudRate();
		if (baudRate == null) {
			baudRate = this.baudRate;
		}
		baudRateCombo.select(BaudRate.getStringIndex(baudRate));

		ByteSize byteSize = settings.getByteSize();
		if (byteSize == null) {
			byteSize = this.byteSize;
		}
		byteSizeCombo.select(ByteSize.getStringIndex(byteSize));

		Parity parity = settings.getParity();
		if (parity == null) {
			parity = this.parity;
		}
		parityCombo.select(Parity.getStringIndex(parity));

		StopBits stopBits = settings.getStopBits();
		if (stopBits == null) {
			stopBits = this.stopBits;
		}
		stopBitsCombo.select(StopBits.getStringIndex(stopBits));
	}

	@Override
	public void saveSettings() {
		if (boardCombo.getSelectionIndex() < 0) {
			return;
		}

		settings.setBoardName(boardCombo.getItem(boardCombo.getSelectionIndex()));
		settings.setPortName(portNameLabel.getText());
		settings.setBaudRate(BaudRate.fromStringIndex(baudRateCombo.getSelectionIndex()));
		settings.setByteSize(ByteSize.fromStringIndex(byteSizeCombo.getSelectionIndex()));
		settings.setParity(Parity.fromStringIndex(parityCombo.getSelectionIndex()));
		settings.setStopBits(StopBits.fromStringIndex(stopBitsCombo.getSelectionIndex()));

		dialogSettings.put(ArduinoTerminalSettings.BOARD_ATTR, boardCombo.getItem(boardCombo.getSelectionIndex()));
		dialogSettings.put(SerialSettings.PORT_NAME_ATTR, portNameLabel.getText());
		dialogSettings.put(SerialSettings.BAUD_RATE_ATTR, BaudRate.getStrings()[baudRateCombo.getSelectionIndex()]);
		dialogSettings.put(SerialSettings.BYTE_SIZE_ATTR, ByteSize.getStrings()[byteSizeCombo.getSelectionIndex()]);
		dialogSettings.put(SerialSettings.PARITY_ATTR, Parity.getStrings()[parityCombo.getSelectionIndex()]);
		dialogSettings.put(SerialSettings.STOP_BITS_ATTR, StopBits.getStrings()[stopBitsCombo.getSelectionIndex()]);
	}

	@Override
	public boolean validateSettings() {
		if (boardCombo.getSelectionIndex() < 0 && boardCombo.getText().isEmpty()) {
			return false;
		}
		return true;
	}

}
