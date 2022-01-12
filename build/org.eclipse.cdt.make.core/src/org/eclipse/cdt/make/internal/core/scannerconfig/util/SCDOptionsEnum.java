/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Gerhard Schaber (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig.util;

/**
 * Enumeration class for scanner configuration affecting command line options
 *
 * @author vhirsl
 */
public final class SCDOptionsEnum {

	public static final SCDOptionsEnum COMMAND = new SCDOptionsEnum(0); // gcc or similar command
	public static final int MIN = 1;
	public static final SCDOptionsEnum DEFINE = new SCDOptionsEnum(1); // -D name
	public static final SCDOptionsEnum UNDEFINE = new SCDOptionsEnum(2); // -U name
	public static final SCDOptionsEnum IDASH = new SCDOptionsEnum(3); // -I-
	public static final SCDOptionsEnum INCLUDE = new SCDOptionsEnum(4); // -I dir
	public static final SCDOptionsEnum NOSTDINC = new SCDOptionsEnum(5); // -nostdinc
	public static final SCDOptionsEnum NOSTDINCPP = new SCDOptionsEnum(6); // -nostdinc++
	public static final SCDOptionsEnum INCLUDE_FILE = new SCDOptionsEnum(7); // -include file
	public static final SCDOptionsEnum IMACROS_FILE = new SCDOptionsEnum(8); // -imacros file
	public static final SCDOptionsEnum IDIRAFTER = new SCDOptionsEnum(9); // -idirafter dir
	public static final SCDOptionsEnum ISYSTEM = new SCDOptionsEnum(10); // -isystem dir
	public static final SCDOptionsEnum IPREFIX = new SCDOptionsEnum(11); // -iprefix prefix
	public static final SCDOptionsEnum IWITHPREFIX = new SCDOptionsEnum(12); // -iwithprefix dir
	public static final SCDOptionsEnum IWITHPREFIXBEFORE = new SCDOptionsEnum(13); // -iwithprefixbefore dir
	public static final SCDOptionsEnum IQUOTE = new SCDOptionsEnum(14); // -iquote dir
	public static final int MAX = 14;

	private static final String[] SCDOPTION_STRING_VALS = { "cc", //$NON-NLS-1$
			"-D", //$NON-NLS-1$
			"-U", //$NON-NLS-1$
			"-I-", //$NON-NLS-1$
			"-I", //$NON-NLS-1$
			"-nostdinc", //$NON-NLS-1$
			"-nostdinc++", //$NON-NLS-1$
			"-include", //$NON-NLS-1$
			"-imacros", //$NON-NLS-1$
			"-idirafter", //$NON-NLS-1$
			"-isystem", //$NON-NLS-1$
			"-iprefix", //$NON-NLS-1$
			"-iwithprefix", //$NON-NLS-1$
			"-iwithprefixbefore", //$NON-NLS-1$
			"-iquote" //$NON-NLS-1$
	};
	private static final SCDOptionsEnum SCDOPTIONS[] = { COMMAND, DEFINE, UNDEFINE, IDASH, INCLUDE, NOSTDINC,
			NOSTDINCPP, INCLUDE_FILE, IMACROS_FILE, IDIRAFTER, ISYSTEM, IPREFIX, IWITHPREFIX, IWITHPREFIXBEFORE,
			IQUOTE };

	/**
	 *
	 */
	private SCDOptionsEnum(int val) {
		this._enum = val;
	}

	public int getEnumValue() {
		return _enum;
	}

	public static SCDOptionsEnum getSCDOptionsEnum(int val) {
		if (val >= 0 && val <= MAX) {
			return SCDOPTIONS[val];
		}
		return null;
	}

	public static SCDOptionsEnum getSCDOptionsEnum(String desc) {
		for (int i = 0; i <= MAX; ++i) {
			if (desc.equals(SCDOPTION_STRING_VALS[i])) {
				return SCDOPTIONS[i];
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object arg0) {
		if (arg0 == null)
			return false;
		if (arg0 == this)
			return true;
		if (arg0 instanceof SCDOptionsEnum)
			return (_enum == ((SCDOptionsEnum) arg0)._enum);
		return false;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return _enum * 17 + 11;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return SCDOPTION_STRING_VALS[_enum];
	}

	private final int _enum;
}
