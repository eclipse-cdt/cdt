/*******************************************************************************
 * Copyright (c) 2000, 2016 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.internal.ui.CUIMessages;
import org.eclipse.jface.text.source.DefaultAnnotationHover;

/**
 * Determines all annotations for the given line and collects, concatenates, and formats
 * their messages in HTML.
 *
 * @since 4.0
 */
public class HTMLAnnotationHover extends DefaultAnnotationHover {

	/*
	 * Formats a message as HTML text.
	 */
	@Override
	protected String formatSingleMessage(String message) {
		StringBuilder buffer = new StringBuilder();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addParagraph(buffer, HTMLPrinter.convertToHTMLContent(message));
		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}

	/*
	 * Formats several message as HTML text.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected String formatMultipleMessages(List messages) {
		StringBuilder buffer = new StringBuilder();
		HTMLPrinter.addPageProlog(buffer);
		HTMLPrinter.addParagraph(buffer,
				HTMLPrinter.convertToHTMLContent(CUIMessages.CAnnotationHover_multipleMarkers));

		HTMLPrinter.startBulletList(buffer);
		Iterator<String> e = messages.iterator();
		while (e.hasNext())
			HTMLPrinter.addBullet(buffer, HTMLPrinter.convertToHTMLContent(e.next()));
		HTMLPrinter.endBulletList(buffer);

		HTMLPrinter.addPageEpilog(buffer);
		return buffer.toString();
	}
}
