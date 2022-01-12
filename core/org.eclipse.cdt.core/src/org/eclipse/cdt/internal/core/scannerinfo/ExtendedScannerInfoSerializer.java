/*******************************************************************************
 * Copyright (c) 2021 Kichwa Coders Canada Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.internal.core.scannerinfo;

import java.lang.reflect.Type;

import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IncludeExportPatterns;

import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Serializer for {@link ExtendedScannerInfo}
 *
 * @see ShadowExtendedScannerInfo
 * @see ShadowIncludeExportPatterns
 * @see ShadowPattern
 */
public class ExtendedScannerInfoSerializer implements JsonSerializer<ExtendedScannerInfo> {
	@Override
	public JsonElement serialize(ExtendedScannerInfo info, Type typeOfInfo, JsonSerializationContext context) {
		ShadowExtendedScannerInfo shadowInfo = new ShadowExtendedScannerInfo();
		IncludeExportPatterns includeExportPatterns = info.getIncludeExportPatterns();
		if (includeExportPatterns != null) {
			shadowInfo.includeExportPatterns = new ShadowIncludeExportPatterns();
			if (includeExportPatterns.getIncludeExportPattern() != null) {
				shadowInfo.includeExportPatterns.includeExportPattern = new ShadowPattern(
						includeExportPatterns.getIncludeExportPattern().pattern());
			}
			if (includeExportPatterns.getIncludeBeginExportsPattern() != null) {
				shadowInfo.includeExportPatterns.includeBeginExportPattern = new ShadowPattern(
						includeExportPatterns.getIncludeBeginExportsPattern().pattern());
			}
			if (includeExportPatterns.getIncludeEndExportsPattern() != null) {
				shadowInfo.includeExportPatterns.includeEndExportPattern = new ShadowPattern(
						includeExportPatterns.getIncludeEndExportsPattern().pattern());
			}

		}

		shadowInfo.definedSymbols = info.getDefinedSymbols();
		shadowInfo.includePaths = info.getIncludePaths();

		return context.serialize(shadowInfo);
	}
}
