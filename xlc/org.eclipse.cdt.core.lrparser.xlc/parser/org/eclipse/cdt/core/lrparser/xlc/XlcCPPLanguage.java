/*******************************************************************************
 * Copyright (c) 2009, 2015 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.xlc;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.IDOMTokenMap;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.LRParserProperties;
import org.eclipse.cdt.core.dom.lrparser.gnu.GPPLanguage;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.lrparser.xlc.preferences.XlcLanguagePreferences;
import org.eclipse.cdt.core.lrparser.xlc.preferences.XlcPref;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.internal.core.lrparser.xlc.cpp.XlcCPPParser;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;

/**
 *
 * @author Mike Kucera
 */
public class XlcCPPLanguage extends GPPLanguage {

	public static final String ID = "org.eclipse.cdt.core.lrparser.xlc.cpp"; //$NON-NLS-1$

	private static XlcCPPLanguage DEFAULT = new XlcCPPLanguage();

	public static XlcCPPLanguage getDefault() {
		return DEFAULT;
	}

	static IProject getProject(Map<String, String> properties) {
		String path = properties.get(LRParserProperties.TRANSLATION_UNIT_PATH);
		IFile[] file = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(path));

		IProject project = null;
		if (file != null && file.length > 0) {
			project = file[0].getProject();
		}

		return project;
	}

	static boolean getPref(XlcPref key, IProject project) {
		return Boolean.valueOf(XlcLanguagePreferences.get(key, project));
	}

	@Override
	protected IParser<IASTTranslationUnit> getParser(IScanner scanner, IIndex index, Map<String, String> properties) {
		IProject project = getProject(properties);
		boolean supportVectors = getPref(XlcPref.SUPPORT_VECTOR_TYPES, project);
		boolean supportDecimals = getPref(XlcPref.SUPPORT_DECIMAL_FLOATING_POINT_TYPES, project);
		boolean supportComplex = getPref(XlcPref.SUPPORT_COMPLEX_IN_CPP, project);
		boolean supportRestrict = getPref(XlcPref.SUPPORT_RESTRICT_IN_CPP, project);
		boolean supportStaticAssert = getPref(XlcPref.SUPPORT_STATIC_ASSERT, project);
		IDOMTokenMap tokenMap = new XlcCPPTokenMap(supportVectors, supportDecimals, supportComplex, supportRestrict,
				supportStaticAssert);

		XlcCPPParser parser = new XlcCPPParser(scanner, tokenMap, getBuiltinBindingsProvider(), index, properties);
		return parser;
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return XlcCPPScannerExtensionConfiguration.getInstance();
	}

	@Override
	public <T> T getAdapter(Class<T> adapter) {
		if (ICLanguageKeywords.class.equals(adapter))
			return adapter.cast(XlcKeywords.ALL_CPP_KEYWORDS);

		return super.getAdapter(adapter);
	}

}