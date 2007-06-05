/*******************************************************************************
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * David Dykstal (IBM) - initial contribution.
 *******************************************************************************/
package org.eclipse.rse.internal.tests.framework.scripting;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.ImageData;

/**
 * This kind of ScriptContext only prints information on the PrintStream it is
 * given. Useful for making sure that instructions read properly.
 */
public class ConsoleContext extends ScriptContext {

	private PrintWriter transcript;

	/**
	 * @param home
	 * @param out The print stream on which to write messages.
	 */
	public ConsoleContext(PrintStream out, URL home) {
		super(home);
		this.transcript = new PrintWriter(out);
	}

	/**
	 * A show operation will resolve a name to an image and show that image
	 * in the current environment.
	 * @param imageName the name of the image to resolve and show.
	 */
	public void show(String imageName) {
		log("showing " + imageName); //$NON-NLS-1$
		String message = "image not found"; //$NON-NLS-1$
		URL imageURL = getResourceURL(imageName);
		if (imageURL != null) {
			ImageDescriptor descriptor = ImageDescriptor.createFromURL(imageURL);
			ImageData data = descriptor.getImageData();
			if (data != null) {
				String type = "Unknown"; //$NON-NLS-1$
				switch (data.type) {
					case SWT.IMAGE_GIF:
						type = "GIF"; //$NON-NLS-1$
						break;
					case SWT.IMAGE_JPEG:
						type = "JPEG"; //$NON-NLS-1$
						break;
					case SWT.IMAGE_PNG:
						type = "PNG"; //$NON-NLS-1$
						break;
					default:
						type = "Other"; //$NON-NLS-1$
				}
				message = type + "(" + Integer.toString(data.width) + " x " + Integer.toString(data.height) + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			}
		}
		log(message);
	}

	/**
	 * A tell operation will show a string in the environment.
	 * @param text the String to show.
	 */
	public void tell(String text) {
		log(text);
	}

	/**
	 * A pause operation will stop and wait for a "continue" or "fail" indication
	 * from the environment.
	 * @param text the message to display during the pause
	 */
	public void pause(String text) {
		if (text.length() == 0) text = "pausing"; //$NON-NLS-1$
		log(text);
	}

	private void log(String message) {
		transcript.println(message);
		transcript.flush();
	}

}
