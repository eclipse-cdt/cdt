/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.dialogs.cpaths;

import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.jface.viewers.ViewerSorter;

public class CPListElementSorter extends ViewerSorter {

	private static final int SOURCE = 0;
	private static final int PROJECT = 1;
	private static final int LIBRARY = 2;
	private static final int VARIABLE = 3;
	private static final int CONTAINER = 4;
	private static final int OTHER = 5;

	/*
	 * @see ViewerSorter#category(Object)
	 */
	public int category(Object obj) {
		if (obj instanceof CPListElement) {
			switch (((CPListElement) obj).getEntryKind()) {
				case IPathEntry.CDT_LIBRARY:
					return LIBRARY;
				case IPathEntry.CDT_PROJECT:
					return PROJECT;
				case IPathEntry.CDT_SOURCE:
					return SOURCE;
				case IPathEntry.CDT_VARIABLE:
					return VARIABLE;
				case IPathEntry.CDT_CONTAINER:
					return CONTAINER;
			}
		}
		return OTHER;
	}

}
