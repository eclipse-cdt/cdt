/*******************************************************************************
 * Copyright (c) 2004, 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM - Initial API and implementation
 *     Ed Swartz (Nokia)
 *     Markus Schorn (Wind River Systems)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.core.dom.parser.c;

import java.util.Map;

import org.eclipse.cdt.core.dom.parser.GNUScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.GCCKeywords;
import org.eclipse.cdt.core.parser.IGCCToken;
import org.eclipse.cdt.core.parser.IScannerInfo;

/**
 * Configures the preprocessor for parsing c-sources as accepted by gcc.
 */
public class GCCScannerExtensionConfiguration extends GNUScannerExtensionConfiguration {
	private static enum CompilerType {
		GCC, MSVC
	}

	private static final int VERSION_4_2 = version(4, 2);
	private static final int VERSION_4_7 = version(4, 7);
	private static GCCScannerExtensionConfiguration CONFIG = new GCCScannerExtensionConfiguration();
	private static GCCScannerExtensionConfiguration CONFIG_4_2 = new GCCScannerExtensionConfiguration(VERSION_4_2);
	private static GCCScannerExtensionConfiguration CONFIG_4_7 = new GCCScannerExtensionConfiguration(VERSION_4_7);
	private static GCCScannerExtensionConfiguration CONFIG_MSVC = new GCCScannerExtensionConfiguration(
			CompilerType.MSVC, 0 /* version is ignored for now */);

	/**
	 * @since 5.1
	 */
	public static GCCScannerExtensionConfiguration getInstance() {
		return CONFIG;
	}

	/**
	 * @since 5.5
	 */
	public static GCCScannerExtensionConfiguration getInstance(IScannerInfo info) {
		if (info != null) {
			try {
				final Map<String, String> definedSymbols = info.getDefinedSymbols();

				String mscVer = definedSymbols.get("_MSC_VER"); //$NON-NLS-1$
				if (mscVer != null && Integer.valueOf(mscVer) > 0) {
					return CONFIG_MSVC;
				}

				int major = Integer.valueOf(definedSymbols.get("__GNUC__")); //$NON-NLS-1$
				int minor = Integer.valueOf(definedSymbols.get("__GNUC_MINOR__")); //$NON-NLS-1$
				int version = version(major, minor);
				if (version >= VERSION_4_7) {
					return CONFIG_4_7;
				}
				if (version >= VERSION_4_2) {
					return CONFIG_4_2;
				}
			} catch (Exception e) {
				// Fall-back to the default configuration.
			}
		}
		return CONFIG;
	}

	public GCCScannerExtensionConfiguration() {
		this(0);
	}

	/**
	 * @since 5.5
	 */
	public GCCScannerExtensionConfiguration(int version) {
		this(CompilerType.GCC, version);
	}

	/**
	 * @since 6.9
	 */
	@SuppressWarnings("nls")
	public GCCScannerExtensionConfiguration(CompilerType compiler, int version) {
		addMacro("__null", "(void *)0");
		addMacro("__builtin_offsetof(T,m)", "((size_t) &((T *)0)->m)");

		if (compiler != CompilerType.MSVC) {
			// MSVC only defines this when compiling in C mode and /Za is used.
			addMacro("__STDC__", "1");
		}

		if (version >= VERSION_4_2) {
			addKeyword(GCCKeywords.cp_decimal32, IGCCToken.t_decimal32);
			addKeyword(GCCKeywords.cp_decimal64, IGCCToken.t_decimal64);
			addKeyword(GCCKeywords.cp_decimal128, IGCCToken.t_decimal128);
		}
		if (version >= VERSION_4_7) {
			addKeyword(GCCKeywords.cp__float128, IGCCToken.t__float128);
			addKeyword(GCCKeywords.cp__int128, IGCCToken.t__int128);
		}
	}

	@Override
	public boolean supportMinAndMaxOperators() {
		return false;
	}
}
