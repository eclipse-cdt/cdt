/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Sergey Prigogin (Google)
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.text.spelling.engine;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Platform wide read-only locale sensitive dictionary for spell checking.
 */
public class LocaleSensitiveSpellDictionary extends AbstractSpellDictionary {
	/** The locale of this dictionary */
	private final Locale fLocale;

	/** The location of the dictionaries */
	private final URL fLocation;

	/**
	 * Creates a new locale sensitive spell dictionary.
	 *
	 * @param locale     The locale for this dictionary
	 * @param location   The location of the locale sensitive dictionaries
	 */
	public LocaleSensitiveSpellDictionary(final Locale locale, final URL location) {
		fLocation = location;
		fLocale = locale;
	}

	/**
	 * Returns the locale of this dictionary.
	 *
	 * @return The locale of this dictionary
	 */
	public final Locale getLocale() {
		return fLocale;
	}

	/*
	 * @see org.eclipse.cdt.internal.ui.text.spelling.engine.AbstractSpellDictionary#getURL()
	 */
	@Override
	protected final URL getURL() throws MalformedURLException {
		return new URL(fLocation, fLocale.toString() + ".dictionary"); //$NON-NLS-1$
	}
}
