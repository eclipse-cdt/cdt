/*******************************************************************************
 * Copyright (c) 2015 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.arduino.core.internal.remote;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.arduino.core.ArduinoHome;
import org.eclipse.cdt.arduino.core.Board;
import org.eclipse.cdt.arduino.core.IArduinoBoardManager;

public class ArduinoBoardManager implements IArduinoBoardManager {

	private Map<String, Board> boards;

	@Override
	public Board getBoard(String id) {
		init();
		return boards.get(id);
	}

	@Override
	public Collection<Board> getBoards() {
		init();
		List<Board> sortedBoards = new ArrayList<Board>(boards.values());
		Collections.sort(sortedBoards, new Comparator<Board>() {
			@Override
			public int compare(Board arg0, Board arg1) {
				return arg0.getName().compareTo(arg1.getName());
			}
		});
		return sortedBoards;
	}

	private void init() {
		if (boards != null)
			return;
		boards = new HashMap<>();
		File home = ArduinoHome.getArduinoHome();
		if (!home.isDirectory())
			return;

		File archRoot = new File(home, "hardware/arduino"); //$NON-NLS-1$
		for (File archDir : archRoot.listFiles()) {
			File boardFile = new File(archDir, "boards.txt"); //$NON-NLS-1$
			loadBoardFile(archDir.getName(), boardFile);
		}
	}

	private void loadBoardFile(String arch, File boardFile) {
		try {
			Properties boardProps = new Properties();
			boardProps.load(new FileInputStream(boardFile));
			Enumeration<?> i = boardProps.propertyNames();
			while (i.hasMoreElements()) {
				String propertyName = (String) i.nextElement();
				String[] names = propertyName.split("\\."); //$NON-NLS-1$
				if (names.length == 2 && names[1].equals("name")) { //$NON-NLS-1$
					boards.put(names[0], new Board(names[0], boardProps));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
