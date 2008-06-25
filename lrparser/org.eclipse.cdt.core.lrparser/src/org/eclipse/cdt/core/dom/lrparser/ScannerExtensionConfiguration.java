/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

import org.eclipse.cdt.core.dom.parser.AbstractScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;


/**
 * A minimalistic scanner configuration for the LR parser.
 * 
 * @author Mike Kucera
 *
 */
public class ScannerExtensionConfiguration extends AbstractScannerExtensionConfiguration {

	@Override
	public CharArrayIntMap getAdditionalKeywords() {
		return null;
	}

	@Override
	public IMacro[] getAdditionalMacros() {
		return null;
	}

	@Override
	public CharArrayIntMap getAdditionalPreprocessorKeywords() {
		return null;
	}

	@Override
	public boolean initializeMacroValuesTo1() {
		return false;
	}

	@Override
	public boolean support$InIdentifiers() {
		return true;
	}

	@Override
	public char[] supportAdditionalNumericLiteralSuffixes() {
		return null;
	}

	@Override
	public boolean supportMinAndMaxOperators() {
		return false;
	}

}
