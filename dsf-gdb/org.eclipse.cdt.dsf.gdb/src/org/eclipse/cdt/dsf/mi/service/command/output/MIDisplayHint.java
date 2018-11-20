/*******************************************************************************
 * Copyright (c) 2010 Verigy and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Jens Elmenthaler (Verigy) - Added Full GDB pretty-printing support (bug 302121)
 *******************************************************************************/
package org.eclipse.cdt.dsf.mi.service.command.output;

/**
 * Some utilities around the display hint provided by the python pretty printers
 * via MI.
 *
 * @since 4.0
 */
public class MIDisplayHint {

	public static final MIDisplayHint NONE = new MIDisplayHint(GdbDisplayHint.GDB_DISPLAY_HINT_NONE, ""); //$NON-NLS-1$

	/**
	 * The set of display hints that are of particular interest to DSF GDB.
	 */
	public enum GdbDisplayHint {

		/**
		 * No hint given.
		 */
		GDB_DISPLAY_HINT_NONE(null),

		/**
		 * Display an expression or variable as string. Strings don't have children.
		 */
		GDB_DISPLAY_HINT_STRING("string"), //$NON-NLS-1$

		/**
		 * Display an expression or variable as array.
		 */
		GDB_DISPLAY_HINT_ARRAY("array"), //$NON-NLS-1$

		/**
		 * Display an expression or variable as map. This means each child with an
		 * odd index is a key, each child with an even index is the corresponding
		 * value.
		 */
		GDB_DISPLAY_HINT_MAP("map"), //$NON-NLS-1$

		/**
		 * A user defined hint. It has no further meaning to gdb.
		 */
		GDB_DISPLAY_USER_DEFINED(null);

		private final String miToken;

		private GdbDisplayHint(String miToken) {
			this.miToken = miToken;
		}

		/**
		 * @return The string that is used by MI to denote this display hint, if
		 *         any.
		 */
		public String getMIToken() {
			return miToken;
		}
	}

	private final GdbDisplayHint gdbHint;

	private final String displayHint;

	private MIDisplayHint(GdbDisplayHint gdbHint, String hint) {
		this.gdbHint = gdbHint;
		this.displayHint = hint;
	}

	/**
	 * Create the hint from the given string.
	 *
	 * @param text The string representation to parse in order to initialize from.
	 */
	public MIDisplayHint(String text) {
		gdbHint = parseDisplayHint(text);
		displayHint = text.trim();
	}

	/**
	 * @return The display hint as returned by the pretty printer printer.
	 */
	public String getDisplayHint() {
		return displayHint;
	}

	/**
	 * @return One of the display hints that are of particular interest to DSF GDB.
	 */
	public GdbDisplayHint getGdbDisplayHint() {
		return gdbHint;
	}

	/**
	 * @return If <code>true</code>, the variable is definitely a collection,
	 * if <code>false</code>, it still might be a collection.
	 */
	public boolean isCollectionHint() {
		switch (getGdbDisplayHint()) {
		case GDB_DISPLAY_HINT_ARRAY:
		case GDB_DISPLAY_HINT_MAP:
			return true;
		}

		return false;
	}

	/**
	 * @param text
	 *            The snipped from the MI response.
	 * @return The decoded display hint predefined by gdb.
	 */
	private static GdbDisplayHint parseDisplayHint(String text) {

		String hint = text.trim();

		for (GdbDisplayHint gdbHint : GdbDisplayHint.values()) {
			String miToken = gdbHint.getMIToken();
			if (miToken != null && miToken.equals(hint)) {
				return gdbHint;
			}
		}

		return GdbDisplayHint.GDB_DISPLAY_USER_DEFINED;
	}
}
