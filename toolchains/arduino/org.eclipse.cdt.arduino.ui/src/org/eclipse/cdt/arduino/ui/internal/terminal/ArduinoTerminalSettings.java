package org.eclipse.cdt.arduino.ui.internal.terminal;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.terminal.connector.cdtserial.connector.SerialSettings;

public class ArduinoTerminalSettings extends SerialSettings {

	public static final String BOARD_ATTR = "arduino.board"; //$NON-NLS-1$

	private String boardName;

	@Override
	public void load(ISettingsStore store) {
		super.load(store);

		boardName = store.get(BOARD_ATTR);
	}

	@Override
	public void save(ISettingsStore store) {
		super.save(store);

		store.put(BOARD_ATTR, boardName);
	}

	public String getBoardName() {
		return boardName;
	}

	public void setBoardName(String boardName) {
		this.boardName = boardName;
	}

}
