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
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IncludeExportPatterns;
import org.eclipse.cdt.internal.core.parser.ParserSettings2;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * Deserializer for {@link IExtendedScannerInfo} that deserializes into {@link ExtendedScannerInfo} as the concrete type.
 *
 * @see ShadowExtendedScannerInfo
 * @see ShadowIncludeExportPatterns
 * @see ShadowPattern
 */
public class IExtendedScannerInfoDeserializer implements JsonDeserializer<IExtendedScannerInfo> {

	@Override
	public IExtendedScannerInfo deserialize(JsonElement element, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		ShadowExtendedScannerInfo shadowInfo = (ShadowExtendedScannerInfo) context.deserialize(element,
				ShadowExtendedScannerInfo.class);

		ExtendedScannerInfo info = new ExtendedScannerInfo(shadowInfo.definedSymbols, shadowInfo.includePaths);
		if (shadowInfo.includeExportPatterns != null) {
			String includeExportPattern = null;
			String includeBeginExportPattern = null;
			String includeEndExportPattern = null;
			if (shadowInfo.includeExportPatterns.includeExportPattern != null) {
				includeExportPattern = shadowInfo.includeExportPatterns.includeExportPattern.pattern;
			}
			if (shadowInfo.includeExportPatterns.includeBeginExportPattern != null) {
				includeBeginExportPattern = shadowInfo.includeExportPatterns.includeBeginExportPattern.pattern;
			}
			if (shadowInfo.includeExportPatterns.includeEndExportPattern != null) {
				includeEndExportPattern = shadowInfo.includeExportPatterns.includeEndExportPattern.pattern;
			}
			IncludeExportPatterns patterns = new IncludeExportPatterns(includeExportPattern, includeBeginExportPattern,
					includeEndExportPattern);
			info.setIncludeExportPatterns(patterns);
		}
		info.setParserSettings(new ParserSettings2());
		return info;
	}

}
