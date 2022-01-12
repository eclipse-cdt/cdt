/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
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
 *     Intel corp.
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.io.IOException;
import java.io.Reader;

/**
 * Provides a set of convenience methods for creating HTML pages.
 */
public class HTMLPrinter {

	private static final String LB = "<"; //$NON-NLS-1$
	private static final String CB = "</"; //$NON-NLS-1$
	private static final String RB = ">"; //$NON-NLS-1$

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

	public static void insertPageProlog(StringBuffer buffer, int position) {
		buffer.insert(position, "<html><body text=\"#000000\" bgcolor=\"#FFFF88\"><font size=-1>"); //$NON-NLS-1$
	}

	public static void insertPageProlog(StringBuilder buffer, int position) {
		buffer.insert(position, "<html><body text=\"#000000\" bgcolor=\"#FFFF88\"><font size=-1>"); //$NON-NLS-1$
	}

	public static void addPageProlog(StringBuffer buffer) {
		insertPageProlog(buffer, buffer.length());
	}

	public static void addPageProlog(StringBuilder buffer) {
		insertPageProlog(buffer, buffer.length());
	}

	public static void addPageEpilog(StringBuffer buffer) {
		buffer.append("</font></body></html>"); //$NON-NLS-1$
	}

	public static void addPageEpilog(StringBuilder buffer) {
		buffer.append("</font></body></html>"); //$NON-NLS-1$
	}

	public static void startBulletList(StringBuffer buffer) {
		buffer.append("<ul>"); //$NON-NLS-1$
	}

	public static void startBulletList(StringBuilder buffer) {
		buffer.append("<ul>"); //$NON-NLS-1$
	}

	public static void endBulletList(StringBuffer buffer) {
		buffer.append("</ul>"); //$NON-NLS-1$
	}

	public static void endBulletList(StringBuilder buffer) {
		buffer.append("</ul>"); //$NON-NLS-1$
	}

	private static void addTag(StringBuffer buffer, String bullet, String tag) {
		if (bullet != null && tag != null) {
			buffer.append(LB);
			buffer.append(tag);
			buffer.append(RB);
			buffer.append(bullet);
			buffer.append(CB);
			buffer.append(tag);
			buffer.append(RB);
		}
	}

	private static void addTag(StringBuilder buffer, String bullet, String tag) {
		if (bullet != null && tag != null) {
			buffer.append(LB);
			buffer.append(tag);
			buffer.append(RB);
			buffer.append(bullet);
			buffer.append(CB);
			buffer.append(tag);
			buffer.append(RB);
		}
	}

	public static void addBullet(StringBuffer buffer, String bullet) {
		addTag(buffer, bullet, "li"); //$NON-NLS-1$
	}

	public static void addBullet(StringBuilder buffer, String bullet) {
		addTag(buffer, bullet, "li"); //$NON-NLS-1$
	}

	public static void addSmallHeader(StringBuffer buffer, String header) {
		addTag(buffer, header, "h5"); //$NON-NLS-1$
	}

	public static void addSmallHeader(StringBuilder buffer, String header) {
		addTag(buffer, header, "h5"); //$NON-NLS-1$
	}

	public static void addParagraph(StringBuffer buffer, String paragraph) {
		if (paragraph != null) {
			buffer.append("<p>"); //$NON-NLS-1$
			buffer.append(paragraph);
		}
	}

	public static void addParagraph(StringBuilder buffer, String paragraph) {
		if (paragraph != null) {
			buffer.append("<p>"); //$NON-NLS-1$
			buffer.append(paragraph);
		}
	}

	public static void addParagraph(StringBuffer buffer, Reader paragraphReader) {
		if (paragraphReader != null)
			addParagraph(buffer, read(paragraphReader));
	}

	public static void addParagraph(StringBuilder buffer, Reader paragraphReader) {
		if (paragraphReader != null)
			addParagraph(buffer, read(paragraphReader));
	}

}