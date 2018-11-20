/*******************************************************************************
 * Copyright (c) 2007, 2016 Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.core;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.core.runtime.QualifiedName;

public class AutotoolsPropertyConstants {

	static final String PREFIX = AutotoolsPlugin.getUniqueIdentifier() + "."; //$NON-NLS-1$
	static final String PREFIX_COMPAT = "org.eclipse.linuxtools.cdt.autotools."; //$NON-NLS-1$
	public static final String AUTOMAKE_VERSION_STRING = "AutoconfEditorAutomakeVersion"; //$NON-NLS-1$
	public static final QualifiedName AUTOMAKE_VERSION = new QualifiedName(PREFIX, AUTOMAKE_VERSION_STRING);
	public static final QualifiedName AUTOMAKE_VERSION_COMPAT = new QualifiedName(PREFIX_COMPAT,
			AUTOMAKE_VERSION_STRING);
	public static final String AUTOCONF_VERSION_STRING = "AutoconfEditorAutoconfVersion"; //$NON-NLS-1$
	public static final QualifiedName AUTOCONF_VERSION = new QualifiedName(PREFIX, AUTOCONF_VERSION_STRING);
	public static final QualifiedName AUTOCONF_VERSION_COMPAT = new QualifiedName(PREFIX_COMPAT,
			AUTOCONF_VERSION_STRING);
	public static final String AUTOCONF_MACRO_VERSIONING = "AutoconfEditorMacroVersioning"; //$NON-NLS-1$
	public static final QualifiedName AUTOCONF_TOOL = new QualifiedName(PREFIX, "AutoconfToolPath"); //$NON-NLS-1$
	public static final QualifiedName AUTOMAKE_TOOL = new QualifiedName(PREFIX, "AutomakeToolPath"); //$NON-NLS-1$
	public static final QualifiedName ACLOCAL_TOOL = new QualifiedName(PREFIX, "AclocalToolPath"); //$NON-NLS-1$
	public static final QualifiedName AUTOHEADER_TOOL = new QualifiedName(PREFIX, "AutoheaderToolPath"); //$NON-NLS-1$
	public static final QualifiedName AUTORECONF_TOOL = new QualifiedName(PREFIX, "AutoreconfToolPath"); //$NON-NLS-1$
	public static final QualifiedName LIBTOOLIZE_TOOL = new QualifiedName(PREFIX, "LibtoolizePath"); //$NON-NLS-1$
	public static final QualifiedName CLEAN_DELETE = new QualifiedName(PREFIX, "CleanDelete"); //$NON-NLS-1$
	public static final QualifiedName CLEAN_MAKE_TARGET = new QualifiedName(PREFIX, "CleanMakeTarget"); //$NON-NLS-1$
	public static final QualifiedName SCANNER_USE_MAKE_W = new QualifiedName(PREFIX, "ScannerUseMakeW");
	public static final QualifiedName AUTO_BUILD_NAME = new QualifiedName(PREFIX, "AutoBuildName"); //$NON-NLS-1$
	public static final QualifiedName OPEN_INCLUDE = new QualifiedName(PREFIX, "IncludeResourceMapping"); //$NON-NLS-1$
	public static final QualifiedName OPEN_INCLUDE_P = new QualifiedName(PREFIX, "PersistentIncludeResourceMapping"); //$NON-NLS-1$
	public static final QualifiedName SCANNER_INFO_DIRTY = new QualifiedName(PREFIX, "ScannerInfoDirty"); // $NON-NLSp-1$

	public static final String[] fACVersions = { "2.13", "2.59", "2.61", "2.67", "2.68" }; //$NON-NLS-1$
	public static final String AC_VERSION_2_13 = fACVersions[0];
	public static final String AC_VERSION_2_59 = fACVersions[1];
	public static final String AC_VERSION_2_61 = fACVersions[2];
	public static final String AC_VERSION_2_67 = fACVersions[3];
	public static final String AC_VERSION_2_68 = fACVersions[4];
	public static final String LATEST_AC_VERSION = fACVersions[fACVersions.length - 1];

	public static final String[] fAMVersions = { "1.4-p6", "1.9.5", "1.9.6", "1.11.1" }; //$NON-NLS-1$
	public static final String LATEST_AM_VERSION = fAMVersions[fAMVersions.length - 1];

	public static final String CLEAN_MAKE_TARGET_DEFAULT = "distclean"; //$NON-NLS-1$

	public static final String TRUE = "true"; //$NON-NLS-1$
	public static final String FALSE = "false"; //$NON-NLS-1$
}
