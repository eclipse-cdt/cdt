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

package org.eclipse.cdt.core.parser.tests.scannerinfo;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;

import org.eclipse.cdt.core.parser.ExtendedScannerInfo;
import org.eclipse.cdt.core.parser.IExtendedScannerInfo;
import org.eclipse.cdt.internal.core.scannerinfo.ExtendedScannerInfoSerializer;
import org.eclipse.cdt.internal.core.scannerinfo.IExtendedScannerInfoDeserializer;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ExtendedScannerInfoSerializerDeserializerTest {

	private static class Container {
		IExtendedScannerInfo info;
	}

	private Gson createGson() {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(IExtendedScannerInfo.class, new IExtendedScannerInfoDeserializer());
		gsonBuilder.registerTypeAdapter(ExtendedScannerInfo.class, new ExtendedScannerInfoSerializer());
		Gson gson = gsonBuilder.create();
		return gson;
	}

	@Test
	public void test() {
		String input = "" //
				+ "{" //
				+ "\"info\": {\n" //
				+ "    \"includeExportPatterns\": {\n" //
				+ "        \"includeExportPattern\": {\n" //
				+ "            \"pattern\": \"pattern1\"\n" //
				+ "        },\n" //
				+ "        \"includeBeginExportPattern\": {\n" //
				+ "            \"pattern\": \"pattern2\"\n" //
				+ "        },\n" //
				+ "        \"includeEndExportPattern\": {\n" //
				+ "            \"pattern\": \"pattern3\"\n" //
				+ "        }\n" //
				+ "   },\n" //
				+ "    \"definedSymbols\": {\n" //
				+ "        \"__STDC__\": \"1\",\n" //
				+ "        \"__INT64_MAX__\": \"0x7fffffffffffffffL\"\n" //
				+ "   },\n" //
				+ "    \"includePaths\": [\n" //
				+ "        \"/usr/local/include\",\n" //
				+ "        \"/usr/include\"\n" //
				+ "    ]\n" //
				+ "}" //
				+ "}";

		Gson createGson = createGson();
		Container fromJson = createGson.fromJson(input, Container.class);
		ExtendedScannerInfo info = (ExtendedScannerInfo) fromJson.info;
		assertEquals(Map.of("__STDC__", "1", "__INT64_MAX__", "0x7fffffffffffffffL"), info.getDefinedSymbols());
		assertArrayEquals(new String[] { "/usr/local/include", "/usr/include" }, info.getIncludePaths());
		assertEquals("pattern1", info.getIncludeExportPatterns().getIncludeExportPattern().pattern());
		assertEquals("pattern2", info.getIncludeExportPatterns().getIncludeBeginExportsPattern().pattern());
		assertEquals("pattern3", info.getIncludeExportPatterns().getIncludeEndExportsPattern().pattern());

		// default values for the rest
		assertArrayEquals(new String[0], info.getIncludeFiles());
		assertArrayEquals(new String[0], info.getLocalIncludePath());
		assertArrayEquals(new String[0], info.getMacroFiles());
		assertNotNull(info.getParserSettings());

		Container container = new Container();
		container.info = info;
		String json = createGson.toJson(container);
		assertEquals(input.replaceAll("\\s", ""), json);
	}

	@Test
	public void testDefaults() {
		String input = "{\"info\":{}}";

		Gson createGson = createGson();
		Container fromJson = createGson.fromJson(input, Container.class);
		ExtendedScannerInfo info = (ExtendedScannerInfo) fromJson.info;

		// default values
		assertEquals(Map.of(), info.getDefinedSymbols());
		assertArrayEquals(new String[0], info.getIncludePaths());
		assertNull(info.getIncludeExportPatterns());

		assertArrayEquals(new String[0], info.getIncludeFiles());
		assertArrayEquals(new String[0], info.getLocalIncludePath());
		assertArrayEquals(new String[0], info.getMacroFiles());
		assertNotNull(info.getParserSettings());
	}

}
