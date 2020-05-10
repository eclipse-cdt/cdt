/*******************************************************************************
 * Copyright (c) 2007, 2018 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * Contributors:
 * Michael Scharf (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.test.ui;

import static org.eclipse.tm.terminal.model.TerminalColor.BLACK;
import static org.eclipse.tm.terminal.model.TerminalColor.BLUE;
import static org.eclipse.tm.terminal.model.TerminalColor.RED;
import static org.eclipse.tm.terminal.model.TerminalColor.YELLOW;

import org.eclipse.tm.terminal.model.TerminalStyle;

final class LineCountingDataSource extends AbstractLineOrientedDataSource {
	TerminalStyle styleNormal = TerminalStyle.getStyle(BLACK, RED);

	TerminalStyle styles[] = new TerminalStyle[] { styleNormal, styleNormal.setBold(true), styleNormal.setForeground(BLUE),
			styleNormal.setForeground(YELLOW), styleNormal.setBold(true).setUnderline(true),
			styleNormal.setReverse(true), styleNormal.setReverse(true).setBold(true),
			styleNormal.setReverse(true).setUnderline(true) };

	int pos;

	@Override
	public char[] dataSource() {
		return (pos + " 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789")
				.toCharArray();
	}

	@Override
	public TerminalStyle getStyle() {
		return styles[pos % styles.length];
	}

	@Override
	public void next() {
		pos++;
	}
}