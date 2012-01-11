/*******************************************************************************
 * Copyright (c) 2010, 2011 Alena Laskavaia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Alena Laskavaia  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.checkers.ui.CheckersUiActivator;
import org.eclipse.cdt.codan.internal.checkers.ui.Messages;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * Quick fix for catch by value
 */
public class CatchByReferenceQuickFix extends AbstractCodanCMarkerResolution {
	@Override
	public String getLabel() {
		return Messages.CatchByReferenceQuickFix_Message;
	}

	@Override
	public void apply(IMarker marker, IDocument document) {
		applyCatchByReferenceQuickFix(marker, document, false);
	}

	static void applyCatchByReferenceQuickFix(IMarker marker, IDocument document, boolean addConst) {
		try {
			int left = marker.getAttribute(IMarker.CHAR_START, -1);
			int right = marker.getAttribute(IMarker.CHAR_END, -1);
			String inStr = document.get(left, right - left);
			document.replace(left, right - left, getCatchByReferenceString(inStr, addConst));
		} catch (BadLocationException e) {
			CheckersUiActivator.log(e);
		}
	}
	
	/**
	 * Returns a catch by reference string from a catch by value string
	 */
	static private String getCatchByReferenceString(String inStr, boolean addConst) {
		StringBuilder stringBuilder = new StringBuilder(inStr.length() + 10);
		if (addConst) {
			stringBuilder.append("const ");	 //$NON-NLS-1$
		}
		
		String typename;
		int space = inStr.lastIndexOf(' ');
		boolean hasDeclName = space != -1;
		if (hasDeclName) {
			typename = inStr.substring(0,space);
		} else {
			typename = inStr;
		}
		stringBuilder.append(typename);
		
		stringBuilder.append(" &"); //$NON-NLS-1$
		
		if (hasDeclName) {
			stringBuilder.append(" "); //$NON-NLS-1$
			String declname = inStr.substring(space+1);
			stringBuilder.append(declname);
		}
		
		return stringBuilder.toString();
	}
}
