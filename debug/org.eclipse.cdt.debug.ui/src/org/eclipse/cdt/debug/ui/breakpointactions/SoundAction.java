/*******************************************************************************
 * Copyright (c) 2007, 2009 Nokia and others.
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
import java.io.IOException;
import java.io.StringReader;
import com.ibm.icu.text.MessageFormat;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.breakpointactions.AbstractBreakpointAction;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IBreakpoint;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

public class SoundAction extends AbstractBreakpointAction {

	static public void playSoundFile(final File soundFile) {

		class SoundPlayer extends Thread {

			public void run() {
				AudioInputStream soundStream;
				try {
					soundStream = AudioSystem.getAudioInputStream(soundFile);
					AudioFormat audioFormat = soundStream.getFormat();
					DataLine.Info dataLineInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
					SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(dataLineInfo);
					byte[] soundBuffer = new byte[5000];
					sourceDataLine.open(audioFormat);
					sourceDataLine.start();
					int dataCount = 0;

					while ((dataCount = soundStream.read(soundBuffer, 0, soundBuffer.length)) != -1) {
						if (dataCount > 0) {
							sourceDataLine.write(soundBuffer, 0, dataCount);
						}
					}
					sourceDataLine.drain();
					sourceDataLine.close();

				} catch (UnsupportedAudioFileException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (LineUnavailableException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}

		}
		
		if (soundFile.exists()) {
			new SoundPlayer().start();
		}
	}

	private File soundFile;

	public SoundAction() {
	}

	public IStatus execute(IBreakpoint breakpoint, IAdaptable context, IProgressMonitor monitor) {
		if (soundFile == null || !soundFile.exists()) {
			String errorMsg = MessageFormat.format(Messages.getString("SoundAction.error.0"), new Object[] {getSummary()}); //$NON-NLS-1$
			return new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, errorMsg, null);
		}

		playSoundFile(soundFile);
		return Status.OK_STATUS;
	}

	public String getDefaultName() {
		return Messages.getString("SoundAction.UntitledName"); //$NON-NLS-1$
	}

	public File getSoundFile() {
		return soundFile;
	}

	public String getSummary() {
		if (soundFile == null)
			return new String(""); //$NON-NLS-1$
		return soundFile.getAbsolutePath();
	}

	public String getTypeName() {
		return Messages.getString("SoundAction.ActionTypeName"); //$NON-NLS-1$
	}

	public String getMemento() {
		String soundData = new String(""); //$NON-NLS-1$
		if (soundFile != null) {
			DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder = null;
			try {
				docBuilder = dfactory.newDocumentBuilder();
				Document doc = docBuilder.newDocument();

				Element rootElement = doc.createElement("soundData"); //$NON-NLS-1$
				rootElement.setAttribute("file", soundFile.getAbsolutePath()); //$NON-NLS-1$

				doc.appendChild(rootElement);

				ByteArrayOutputStream s = new ByteArrayOutputStream();

				TransformerFactory factory = TransformerFactory.newInstance();
				Transformer transformer = factory.newTransformer();
				transformer.setOutputProperty(OutputKeys.METHOD, "xml"); //$NON-NLS-1$
				transformer.setOutputProperty(OutputKeys.INDENT, "yes"); //$NON-NLS-1$

				DOMSource source = new DOMSource(doc);
				StreamResult outputTarget = new StreamResult(s);
				transformer.transform(source, outputTarget);

				soundData = s.toString("UTF8"); //$NON-NLS-1$

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return soundData;
	}

	public void initializeFromMemento(String data) {
		Element root = null;
		DocumentBuilder parser;
		try {
			parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			parser.setErrorHandler(new DefaultHandler());
			root = parser.parse(new InputSource(new StringReader(data))).getDocumentElement();
			String value = root.getAttribute("file"); //$NON-NLS-1$
			if (value == null)
				throw new Exception();
			soundFile = new File(value);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getIdentifier() {
		return "org.eclipse.cdt.debug.ui.breakpointactions.SoundAction"; //$NON-NLS-1$
	}

	public void setSoundFile(File soundFile) {
		this.soundFile = soundFile;
	}

}
