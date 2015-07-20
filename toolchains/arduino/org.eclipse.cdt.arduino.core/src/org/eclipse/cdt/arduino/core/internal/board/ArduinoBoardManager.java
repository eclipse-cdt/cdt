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
package org.eclipse.cdt.arduino.core.internal.board;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.cdt.arduino.core.ArduinoHome;
import org.eclipse.cdt.arduino.core.Board;
import org.eclipse.cdt.arduino.core.IArduinoBoardManager;
import org.eclipse.cdt.arduino.core.internal.Activator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.google.gson.Gson;

public class ArduinoBoardManager implements IArduinoBoardManager {

	private Map<String, Board> boards;

	// TODO make this a preference
	private Path arduinoHome = Paths.get(System.getProperty("user.home"), ".arduinocdt"); //$NON-NLS-1$ //$NON-NLS-2$

	public void getPackageIndex(final Handler<PackageIndex> handler) {
		new Job("Fetching package index") {
			// Closeable isn't API yet but it's recommended.
			@SuppressWarnings("restriction")
			protected IStatus run(IProgressMonitor monitor) {
				try (CloseableHttpClient client = HttpClients.createDefault()) {
					HttpGet get = new HttpGet("http://downloads.arduino.cc/packages/package_index.json"); //$NON-NLS-1$
					try (CloseableHttpResponse response = client.execute(get)) {
						if (response.getStatusLine().getStatusCode() >= 400) {
							return new Status(IStatus.ERROR, Activator.getId(),
									response.getStatusLine().getReasonPhrase());
						} else {
							HttpEntity entity = response.getEntity();
							if (entity == null) {
								return new Status(IStatus.ERROR, Activator.getId(),
										"Package index missing from response");
							}
							Files.createDirectories(arduinoHome);
							Path indexPath = arduinoHome.resolve("package_index.json"); //$NON-NLS-1$
							Files.copy(entity.getContent(), indexPath, StandardCopyOption.REPLACE_EXISTING);
							try (FileReader reader = new FileReader(indexPath.toFile())) {
								PackageIndex index = new Gson().fromJson(reader, PackageIndex.class);
								handler.handle(index);
							}
						}
					}
				} catch (IOException e) {
					return new Status(IStatus.ERROR, Activator.getId(), e.getLocalizedMessage(), e);
				}
				return Status.OK_STATUS;
			}
		}.schedule();
	}

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
