/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.core.lrparser.xlc;

import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.lrparser.IParser;
import org.eclipse.cdt.core.dom.lrparser.gnu.GCCLanguage;
import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.parser.IScanner;
import org.eclipse.cdt.internal.core.lrparser.xlc.c.XlcCParser;

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
	protected IParser<IASTTranslationUnit> getParser(IScanner scanner, IIndex index, Map<String,String> properties) {
		boolean supportVectors = XlcCPPLanguage.supportVectors(properties);
		return new XlcCParser(scanner, new XlcCTokenMap(supportVectors), getBuiltinBindingsProvider(), index, properties);
	}
	
	public String getId() {
		return ID;
	}
	
	@Override
	protected IScannerExtensionConfiguration getScannerExtensionConfiguration() {
		return XlcScannerExtensionConfiguration.getInstance();
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		if(ICLanguageKeywords.class.equals(adapter))
			return XlcKeywords.C;
		
		return super.getAdapter(adapter);
	}
	
}