/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.core.dom.lrparser;

import org.eclipse.cdt.core.dom.parser.IScannerExtensionConfiguration;
import org.eclipse.cdt.core.parser.IMacro;
import org.eclipse.cdt.core.parser.util.CharArrayIntMap;


/**
 * A minimalistic scanner configuration for the LR parser.
 * 
 * @author Mike Kucera
 *
 */
public class ScannerExtensionConfiguration implements IScannerExtensionConfiguration {

	public CharArrayIntMap getAdditionalKeywords() {
		return null;
	}

	public IMacro[] getAdditionalMacros() {
		return null;
	}

	public CharArrayIntMap getAdditionalPreprocessorKeywords() {
		return null;
	}

	public boolean initializeMacroValuesTo1() {
		return false;
	}

	public boolean support$InIdentifiers() {
		return true;
	}

	public char[] supportAdditionalNumericLiteralSuffixes() {
		return null;
	}

	public boolean supportMinAndMaxOperators() {
		return false;
	}

}
