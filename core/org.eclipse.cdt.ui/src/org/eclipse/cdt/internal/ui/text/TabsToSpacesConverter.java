/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;

/**
 * Auto edit strategy that converts tabs into spaces.
 * <p>
 * This class is derived from the platform version adding a fix for bug 306333.
 * Can be removed when the bug is fixed.
 * </p>
 * @see org.eclipse.jface.text.TabsToSpacesConverter
 */
public class TabsToSpacesConverter implements IAutoEditStrategy {

	private int fTabRatio;
	private ILineTracker fLineTracker;


	public void setNumberOfSpacesPerTab(int ratio) {
		fTabRatio= ratio;
	}

	public void setLineTracker(ILineTracker lineTracker) {
		fLineTracker= lineTracker;
	}

	private int insertTabString(StringBuffer buffer, int offsetInLine) {

		if (fTabRatio == 0)
			return 0;

		int remainder= offsetInLine % fTabRatio;
		remainder= fTabRatio - remainder;
		for (int i= 0; i < remainder; i++)
			buffer.append(' ');
		return remainder;
	}

	@Override
	public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
		String text= command.text;
		if (text == null)
			return;

		int index= text.indexOf('\t');
		if (index > -1) {

			StringBuffer buffer= new StringBuffer();

			fLineTracker.set(command.text);
			int lines= fLineTracker.getNumberOfLines();

			try {

				for (int i= 0; i < lines; i++) {

					int offset= fLineTracker.getLineOffset(i);
					int endOffset= offset + fLineTracker.getLineLength(i);
					String line= text.substring(offset, endOffset);

					int position= 0;
					if (i == 0) {
						IRegion firstLine= document.getLineInformationOfOffset(command.offset);
						position= computeVisualLength(document.get(firstLine.getOffset(), command.offset - firstLine.getOffset()));
					}

					int length= line.length();
					for (int j= 0; j < length; j++) {
						char c= line.charAt(j);
						if (c == '\t') {
							position += insertTabString(buffer, position);
						} else {
							buffer.append(c);
							++ position;
						}
					}

				}

				command.text= buffer.toString();

			} catch (BadLocationException x) {
			}
		}
	}

	private int computeVisualLength(String text) {
		int length = text.length();
		int offset = 0;
		for (int i=0; i < length; ++i) {
			if (text.charAt(i) == '\t') {
				if (fTabRatio != 0)
					offset += fTabRatio - offset % fTabRatio;
			} else {
				++offset;
			}
		}
		return offset;
	}
}
