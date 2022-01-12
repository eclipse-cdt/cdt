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

import org.eclipse.tm.terminal.model.TerminalStyle;

final class FastDataSource extends AbstractLineOrientedDataSource {
	char lines[][] = new char[][] {
			"123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 ".toCharArray(),
			"abcdefghi abcdefghi abcdefghi abcdefghi abcdefghi abcdefghi abcdefghi abcdefghi ".toCharArray(), };

	int pos;

	@Override
	public char[] dataSource() {
		return lines[pos % lines.length];
	}

	@Override
	public TerminalStyle getStyle() {
		return null;
	}

	@Override
	public void next() {
		pos++;
	}
}