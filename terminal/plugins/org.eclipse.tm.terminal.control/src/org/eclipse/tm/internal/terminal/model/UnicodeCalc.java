/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tm.internal.terminal.model;

import java.util.ArrayList;
import java.util.List;

public class UnicodeCalc {

	/**
	 * Calculate the number of columns this code point renders as
	 * @param codePoint
	 * @return number of columns required to render the code point
	 */
	public static int width(int codePoint) {
		// TODO
		switch (codePoint) {
		case '⌚':
			return 2;
		case 0x1F411: // 🐑
		case 0x1D400: // 𝐀
			return 2;
		default:
			return 1;
		}
	}

	public static int width(char[] chars) {
		return width(chars, 0, chars.length);
	}

	public static int width(char[] chars, int start, int length) {
		String string = new String(chars, start, length);
		return string.codePoints().map(UnicodeCalc::width).sum();
	}

	public static int width(int[] input) {
		return width(input, 0, input.length);
	}

	public static int width(int[] input, int start, int length) {
		int width = 0;
		for (int i = start; i < start + length; i++) {
			width += width(input[i]);
		}
		return width;
	}

	/**
	 * Return the chars representing columns number of width
	 */
	public static int[] getColumns(char[] chars, int columns) {
		return getColumns(chars, 0, columns);
	}

	/**
	 * Return the chars representing columns number of width
	 */
	public static int[] getColumns(char[] chars, int start, int columns) {
		String string = new String(chars, start, chars.length - start);
		int[] input = string.codePoints().toArray();
		List<Integer> output = new ArrayList<>();
		int c = 0;
		int i = 0;
		while (c < columns) {
			int codePoint = input[i++];
			int width = width(codePoint);
			if (c + width <= columns) {
				output.add(codePoint);
				c += width;
			} else {
				break;
			}
		}
		return output.stream().mapToInt(Integer::intValue).toArray();
	}

}
