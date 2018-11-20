/*******************************************************************************
 * Copyright (c) 2000 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui;

import java.io.IOException;
import java.io.Reader;

/**
 * Provides a set of convenience methods for creating HTML pages.
 */
public class HTMLPrinter {

	private HTMLPrinter() {
	}

	private static String replace(String text, char c, String s) {

		int previous = 0;
		int current = text.indexOf(c, previous);

		if (current == -1)
			return text;

		StringBuilder buffer = new StringBuilder();
		while (current > -1) {
			buffer.append(text.substring(previous, current));
			buffer.append(s);
			previous = current + 1;
			current = text.indexOf(c, previous);
		}
		buffer.append(text.substring(previous));

		return buffer.toString();
	}

	public static String convertToHTMLContent(String content) {
		content = replace(content, '<', "&lt;"); //$NON-NLS-1$
		return replace(content, '>', "&gt;"); //$NON-NLS-1$
	}

	public static String read(Reader rd) {

		StringBuilder buffer = new StringBuilder();
		char[] readBuffer = new char[2048];

		try {
			int n = rd.read(readBuffer);
			while (n > 0) {
				buffer.append(readBuffer, 0, n);
				n = rd.read(readBuffer);
			}
			return buffer.toString();
		} catch (IOException x) {
		}

		return null;
	}

	public static void insertPageProlog(StringBuilder buffer, int position) {
		buffer.insert(position, "<html><body text=\"#000000\" bgcolor=\"#FFFF88\"><font size=-1>"); //$NON-NLS-1$
	}

	public static void addPageProlog(StringBuilder buffer) {
		insertPageProlog(buffer, buffer.length());
	}

	public static void addPageEpilog(StringBuilder buffer) {
		buffer.append("</font></body></html>"); //$NON-NLS-1$
	}

	public static void startBulletList(StringBuilder buffer) {
		buffer.append("<ul>"); //$NON-NLS-1$
	}

	public static void endBulletList(StringBuilder buffer) {
		buffer.append("</ul>"); //$NON-NLS-1$
	}

	public static void addBullet(StringBuilder buffer, String bullet) {
		if (bullet != null) {
			buffer.append("<li>"); //$NON-NLS-1$
			buffer.append(bullet);
			buffer.append("</li>"); //$NON-NLS-1$
		}
	}

	public static void addSmallHeader(StringBuilder buffer, String header) {
		if (header != null) {
			buffer.append("<h5>"); //$NON-NLS-1$
			buffer.append(header);
			buffer.append("</h5>"); //$NON-NLS-1$
		}
	}

	public static void addParagraph(StringBuilder buffer, String paragraph) {
		if (paragraph != null) {
			buffer.append("<p>"); //$NON-NLS-1$
			buffer.append(paragraph);
		}
	}

	public static void addParagraph(StringBuilder buffer, Reader paragraphReader) {
		if (paragraphReader != null)
			addParagraph(buffer, read(paragraphReader));
	}
}