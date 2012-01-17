/*******************************************************************************
 * Copyright (c) 2007, 2011 Nokia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Nokia - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpointactions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.debug.core.breakpointactions.IBreakpointAction;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.swt.widgets.Composite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

public class SoundActionPage extends PlatformObject implements IBreakpointActionPage {

	private static final String SOUND_ACTION_RECENT = "SoundBehaviorDialog.recentSounds"; //$NON-NLS-1$

	private static boolean isWindows() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return (os != null && os.toLowerCase().startsWith("win")); //$NON-NLS-1$
	}

	private static boolean isMacOS() {
		String os = System.getProperty("os.name"); //$NON-NLS-1$
		return (os != null && os.toLowerCase().startsWith("mac")); //$NON-NLS-1$
	}

	private SoundActionComposite editor = null;
	private String mediaPath = ""; //$NON-NLS-1$

	private ArrayList<File> recentSounds = new ArrayList<File>();

	private SoundAction soundAction;

	public SoundActionPage() {
		if (isWindows())
			mediaPath = "C:\\WINNT\\Media\\"; //$NON-NLS-1$
		if (isMacOS())
			mediaPath = "/System/Library/Sounds"; //$NON-NLS-1$

		loadRecentSounds();
	}

	@Override
	public void actionDialogCanceled() {
	}

	@Override
	public void actionDialogOK() {
		saveRecentSounds();
		soundAction.setSoundFile(editor.getSoundFile());
	}

	public void addRecentSound(File soundFile) {
		String soundFilePath = soundFile.getAbsolutePath();
		int removeIndex = -1;
		int fileCount = 0;
		for (Iterator<File> iter = recentSounds.iterator(); iter.hasNext() && removeIndex < 0;) {
			File element = iter.next();
			if (element.getAbsolutePath().equals(soundFilePath))
				removeIndex = fileCount;
			fileCount++;
		}
		if (removeIndex >= 0)
			recentSounds.remove(removeIndex);
		recentSounds.add(soundFile);
		if (recentSounds.size() > 10)
			recentSounds.remove(0);

	}

	@Override
	public Composite createComposite(IBreakpointAction action, Composite composite, int style) {
		this.soundAction = (SoundAction) action;
		loadRecentSounds();
		if (soundAction.getSoundFile() == null && recentSounds.size() > 0)
			soundAction.setSoundFile(recentSounds.get(0));
		editor = new SoundActionComposite(composite, style, this);
		return editor;
	}

	public String getMediaPath() {
		return mediaPath;
	}

	public ArrayList<File> getRecentSounds() {
		return recentSounds;
	}

	public String getSummary() {
		if (soundAction.getSoundFile() == null)
			return ""; //$NON-NLS-1$
		return soundAction.getSoundFile().getAbsolutePath();
	}

	private void initializeRecentSounds() {

		if (isWindows()) {
			String defaultSounds[] = { "chimes.wav", "chord.wav", "ding.wav", "notify.wav", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
					"tada.wav" }; //$NON-NLS-1$

			for (int i = 0; i < defaultSounds.length; i++) {
				File soundFile = new File(mediaPath + defaultSounds[i]);
				if (soundFile.exists())
					recentSounds.add(soundFile);
			}
		}
		if (isMacOS()) {
			File macSounds = new File(mediaPath);
			File[] soundFiles = macSounds.listFiles();

			for (int i = 0; i < soundFiles.length; i++) {
				String fileExtension = new Path(soundFiles[i].getAbsolutePath()).getFileExtension();
				if (fileExtension.equalsIgnoreCase("aiff") || fileExtension.equalsIgnoreCase("wav")) //$NON-NLS-1$ //$NON-NLS-2$
					recentSounds.add(soundFiles[i]);

			}
		}
		saveRecentSounds();

	}

	private void loadRecentSounds() {
		String recentSoundData = CDebugUIPlugin.getDefault().getPreferenceStore().getString(SOUND_ACTION_RECENT);

		if (recentSoundData == null || recentSoundData.length() == 0) {
			initializeRecentSounds();
			return;
		}

		recentSounds = new ArrayList<File>();

		Element root = null;
		DocumentBuilder parser;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			root = parser.parse(new InputSource(new StringReader(recentSoundData))).getDocumentElement();

			NodeList nodeList = root.getChildNodes();
			int entryCount = nodeList.getLength();

			for (int i = 0; i < entryCount; i++) {
				Node node = nodeList.item(i);
				short type = node.getNodeType();
				if (type == Node.ELEMENT_NODE) {
					Element subElement = (Element) node;
					String nodeName = subElement.getNodeName();
					if (nodeName.equalsIgnoreCase("soundFileName")) { //$NON-NLS-1$
						String value = subElement.getAttribute("name"); //$NON-NLS-1$
						if (value == null)
							throw new Exception();

						File soundFile = new File(value);
						if (soundFile.exists()) {
							recentSounds.add(soundFile);
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		if (recentSounds.size() == 0)
			initializeRecentSounds();
	}

	public void saveRecentSounds() {
		String recentSoundData = ""; //$NON-NLS-1$

		DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = dfactory.newDocumentBuilder();
			Document doc = docBuilder.newDocument();

			Element rootElement = doc.createElement("recentSounds"); //$NON-NLS-1$
			doc.appendChild(rootElement);

			for (Iterator<File> iter = recentSounds.iterator(); iter.hasNext();) {
				File soundFile = iter.next();

				Element element = doc.createElement("soundFileName"); //$NON-NLS-1$
				element.setAttribute("name", soundFile.getAbsolutePath()); //$NON-NLS-1$
				rootElement.appendChild(element);

			}

			ByteArrayOutputStream s = new ByteArrayOutputStream();

			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
			transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

			DOMSource source = new DOMSource(doc);
			StreamResult outputTarget = new StreamResult(s);
			transformer.transform(source, outputTarget);

			recentSoundData = s.toString("UTF8"); //$NON-NLS-1$

		} catch (Exception e) {
			e.printStackTrace();
		}

		CDebugUIPlugin.getDefault().getPreferenceStore().setValue(SOUND_ACTION_RECENT, recentSoundData);
	}

	public SoundAction getSoundAction() {
		return soundAction;
	}

}
