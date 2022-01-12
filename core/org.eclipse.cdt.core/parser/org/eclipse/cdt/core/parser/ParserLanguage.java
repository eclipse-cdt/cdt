/*******************************************************************************
 * Copyright (c) 2003, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 *     Mike Kucera (IBM) - convert to Java 5 enum
 *******************************************************************************/
package org.eclipse.cdt.core.parser;

/**
 * Enumeration of base languages supported by CDT.
 */
public enum ParserLanguage {
	C {
		@Override
		public boolean isCPP() {
			return false;
		}

		@Override
		public String toString() {
			return "C"; //$NON-NLS-1$
		}
	},

	CPP {
		@Override
		public boolean isCPP() {
			return true;
		}

		@Override
		public String toString() {
			return "C++"; //$NON-NLS-1$
		}
	};

	public abstract boolean isCPP();
}
