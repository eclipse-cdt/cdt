/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui.text;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public interface IMakefileColorManager {
	public static final RGB MAKE_COMMENT = new RGB(128, 0, 0);
	public static final RGB MAKE_KEYWORD = new RGB(128, 128, 0);
	public static final RGB MAKE_FUNCTION = new RGB(128, 0, 128);
	public static final RGB MAKE_MACRO_VAR = new RGB(0, 0, 128);
	public static final RGB MAKE_META_DATA = new RGB(0, 128, 0);
	public static final RGB MAKE_DEFAULT = new RGB(0, 0, 0);

	public static final RGB MAKE_FORM_FOREGROUND = Display.getCurrent().getSystemColor(SWT.COLOR_LIST_FOREGROUND).getRGB();
	public static final RGB MAKE_FORM_BACKGROUND = new RGB(0xff, 0xff, 0xff); //RGB(0xff, 0xfe, 0xf9);
	public static final RGB MAKE_DEFAULT_PAGE_HEADER = new RGB(0x48, 0x70, 0x98);
	public static final RGB MAKE_HYPERLINK_TEXT = new RGB(0, 0, 128);
	public static final RGB MAKE_CONTROL_BORDER = new RGB(195, 191, 179);

	void dispose();
	Color getColor(RGB rgb);

}
