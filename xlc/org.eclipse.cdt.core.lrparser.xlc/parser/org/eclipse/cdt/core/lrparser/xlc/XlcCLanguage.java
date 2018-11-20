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

import static org.eclipse.cdt.core.lrparser.xlc.XlcCPPLanguage.getPref;
import static org.eclipse.cdt.core.lrparser.xlc.XlcCPPLanguage.getProject;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.lrparser.xlc.preferences.XlcPref;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.internal.core.lrparser.xlc.c.XlcCParser;
import org.eclipse.core.resources.IProject;

/**
 *
 * @author Mike Kucera
 */
public class XlcCLanguage extends GCCLanguage {

	public static final String ID = "org.eclipse.cdt.core.lrparser.xlc.c"; //$NON-NLS-1$

	private static XlcCLanguage DEFAULT = new XlcCLanguage();

	public static XlcCLanguage getDefault() {
		return DEFAULT;
	}

	@Override
	protected IParser<IASTTranslationUnit> getParser(IScanner scanner, IIndex index, Map<String, String> properties) {
		IProject project = getProject(properties);
		boolean supportVectors = getPref(XlcPref.SUPPORT_VECTOR_TYPES, project);
		boolean supportDecimals = getPref(XlcPref.SUPPORT_DECIMAL_FLOATING_POINT_TYPES, project);

		return new XlcCParser(scanner, new XlcCTokenMap(supportVectors, supportDecimals), getBuiltinBindingsProvider(),
				index, properties);
	}

	@Override
	public String getId() {
		return ID;
	}

	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return XlcCScannerExtensionConfiguration.getInstance();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Object getAdapter(Class adapter) {
		if (ICLanguageKeywords.class.equals(adapter))
			return XlcKeywords.ALL_C_KEYWORDS;

		return super.getAdapter(adapter);
	}

}