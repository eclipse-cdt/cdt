/*******************************************************************************
 * Copyright (c) 2010 Tomasz Wesolowski
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tomasz Wesolowski - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.ui;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.link.LinkedModeModel;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.link.LinkedPosition;
import org.eclipse.jface.text.link.LinkedPositionGroup;

/**
 * 
 * Collection of utils methods for location/text manipulations
 * 
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 1.1
 * 
 */
public final class JFaceTextUtils {
	/**
	 * Enters the linked mode and creates a singular LinkedPosition around a
	 * given IASTFileLocation in a given Document.
	 * 
	 * @param location
	 *        the location to wrap
	 * @param document
	 *        the document to use
	 */
	public static void markLocationForInsert(IASTFileLocation location,
			ITextViewer viewer) {
		IDocument document = viewer.getDocument();
		LinkedPosition pos = new LinkedPosition(document,
				location.getNodeOffset(), location.getNodeLength());
		LinkedModeModel model = new LinkedModeModel();
		LinkedPositionGroup group = new LinkedPositionGroup();
		try {
			group.addPosition(pos);
			model.addGroup(group);
			model.forceInstall();
		} catch (BadLocationException e) {
			return;
		}
		LinkedModeUI ui = new LinkedModeUI(model, new ITextViewer[] { viewer });
		ui.enter();
	}
}