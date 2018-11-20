/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.index;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.internal.qt.core.Activator;

/**
 * A container class to interpret and store value of the the qmake version.
 */
public final class QMakeVersion implements IQtVersion {

	// QMAKE_VERSION looks like 2.01a or 3.0
	private static final Pattern REGEXP = Pattern.compile("([\\d]+)\\.([\\d]+).*");

	// parses major and minor version numbers only
	public static QMakeVersion create(String version) {
		if (version == null) {
			return null;
		}

		Matcher m = REGEXP.matcher(version.trim());
		if (!m.matches()) {
			return null;
		}

		try {
			int major = Integer.parseInt(m.group(1));
			int minor = Integer.parseInt(m.group(2));
			return new QMakeVersion(major, minor);
		} catch (NumberFormatException e) {
			Activator.log(e);
		}
		return null;
	}

	private final int major;
	private final int minor;

	private QMakeVersion(int major, int minor) {
		this.major = major;
		this.minor = minor;
	}

	@Override
	public int getMajor() {
		return major;
	}

	@Override
	public int getMinor() {
		return minor;
	}

}
