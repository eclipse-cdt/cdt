/*******************************************************************************
 * Copyright (c) 2011, 2011 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.make.core.scannerconfig;

import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.IErrorParser;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;

/**
 * Note: IErrorParser interface is used here to work around {@link ConsoleOutputSniffer} having
 * no access from CDT core to build packages.
 */
public interface ILanguageSettingsBuildOutputScanner extends ILanguageSettingsOutputScanner, IErrorParser {

	/**
	 * This method is expected to populate this.settingEntries with specific values
	 * parsed from supplied lines.
	 */
	public boolean processLine(String line, ErrorParserManager epm);
}
