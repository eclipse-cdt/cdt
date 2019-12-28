/*******************************************************************************
 * Copyright (c) 2018 Manish Khurana , Nathan Ridge and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.lsp.core.tests.cquery;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.cquery.CqueryInactiveRegions;
import org.eclipse.cdt.cquery.CquerySemanticHighlights;
import org.eclipse.cdt.cquery.ExtendedSymbolKindType;
import org.eclipse.cdt.cquery.HighlightSymbol;
import org.eclipse.cdt.cquery.IndexingProgressStats;
import org.eclipse.cdt.cquery.StorageClass;
import org.eclipse.cdt.cquery.SymbolRole;
import org.eclipse.cdt.lsp.core.Server2ClientProtocolExtension;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.json.JsonRpcMethod;
import org.eclipse.lsp4j.jsonrpc.json.MessageJsonHandler;
import org.eclipse.lsp4j.jsonrpc.messages.NotificationMessage;
import org.eclipse.lsp4j.jsonrpc.services.ServiceEndpoints;
import org.junit.Assert;
import org.junit.Test;

public class CqueryJsonParseTest {
	Map<String, JsonRpcMethod> methods = ServiceEndpoints.getSupportedMethods(Server2ClientProtocolExtension.class);
	private MessageJsonHandler jsonHandler = new MessageJsonHandler(methods);

	private void assertParse(final String json, final NotificationMessage expectedResult) {
		Assert.assertEquals(expectedResult.toString(), jsonHandler.parseMessage(json).toString());
	}

	@Test
	public void testProgress() {
		String json = "{\"jsonrpc\": \"2.0\",\"method\": \"$cquery/progress\",\"params\": {" //$NON-NLS-1$
				+ "\"indexRequestCount\": 4,\"doIdMapCount\": 5,\"loadPreviousIndexCount\": 6," //$NON-NLS-1$
				+ "\"onIdMappedCount\": 7,\"onIndexedCount\": 8,\"activeThreads\": 9}}"; //$NON-NLS-1$

		IndexingProgressStats expectedIndex = new IndexingProgressStats(4, 5, 6, 7, 8, 9);
		NotificationMessage expectedResult = new NotificationMessage();
		expectedResult.setJsonrpc("2.0"); //$NON-NLS-1$
		expectedResult.setMethod("$cquery/progress"); //$NON-NLS-1$
		expectedResult.setParams(expectedIndex);
		assertParse(json, expectedResult);
	}

	@Test
	public void testSetInactiveRegions() {
		String json = "{\"jsonrpc\": \"2.0\",\"method\": \"$cquery/setInactiveRegions\",\"params\": {" //$NON-NLS-1$
				+ "\"uri\": \"file:///home/foobar.cpp\",\"inactiveRegions\": [{\"start\": {\"line\": " //$NON-NLS-1$
				+ "25,\"character\": 4},\"end\": {\"line\": 25,\"character\": 10}},{\"start\": {\"line\"" //$NON-NLS-1$
				+ ": 35,\"character\": 8},\"end\": {\"line\": 35,\"character\": 15}}]}}"; //$NON-NLS-1$

		URI uri = URI.create("file:///home/foobar.cpp"); //$NON-NLS-1$
		Position pos1 = new Position(25, 4);
		Position pos2 = new Position(25, 10);
		Position pos3 = new Position(35, 8);
		Position pos4 = new Position(35, 15);
		Range range1 = new Range(pos1, pos2);
		Range range2 = new Range(pos3, pos4);
		List<Range> regions = new ArrayList<>();
		regions.add(range1);
		regions.add(range2);
		CqueryInactiveRegions expectedRegions = new CqueryInactiveRegions(uri, regions);

		NotificationMessage expectedResult = new NotificationMessage();
		expectedResult.setJsonrpc("2.0"); //$NON-NLS-1$
		expectedResult.setMethod("$cquery/setInactiveRegions"); //$NON-NLS-1$
		expectedResult.setParams(expectedRegions);
		assertParse(json, expectedResult);
	}

	@Test
	public void testPublishSemanticHighlighting() {
		String json = "{\"jsonrpc\": \"2.0\",\"method\": \"$cquery/publishSemanticHighlighting\"," //$NON-NLS-1$
				+ "\"params\": {\"uri\": \"file:///home/foobar.cpp\",\"symbols\": [{\"stableId\": 21," //$NON-NLS-1$
				+ "\"parentKind\": 8,\"kind\": 0,\"storage\": 3,\"role\": 1,\"ranges\": [{\"start\": {\"line\": 41," //$NON-NLS-1$
				+ "\"character\": 1},\"end\": {\"line\": 41,\"character\": 5}}]},{\"stableId\": 19," //$NON-NLS-1$
				+ "\"parentKind\": 12,\"kind\": 253,\"storage\": 5,\"role\": 4,\"ranges\": [{\"start\": {\"line\": 39," //$NON-NLS-1$
				+ "\"character\": 9},\"end\": {\"line\": 39,\"character\": 10}}]}]}}"; //$NON-NLS-1$

		URI uri = URI.create("file:///home/foobar.cpp"); //$NON-NLS-1$
		Position pos1 = new Position(41, 1);
		Position pos2 = new Position(41, 5);
		Position pos3 = new Position(39, 9);
		Position pos4 = new Position(39, 10);
		Range range1 = new Range(pos1, pos2);
		Range range2 = new Range(pos3, pos4);
		List<Range> ranges1 = new ArrayList<>();
		List<Range> ranges2 = new ArrayList<>();
		ranges1.add(range1);
		ranges2.add(range2);
		ExtendedSymbolKindType parentKind1 = new ExtendedSymbolKindType(8);
		ExtendedSymbolKindType parentKind2 = new ExtendedSymbolKindType(12);
		ExtendedSymbolKindType kind1 = new ExtendedSymbolKindType(0);
		ExtendedSymbolKindType kind2 = new ExtendedSymbolKindType(253);
		StorageClass storage1 = StorageClass.Static;
		StorageClass storage2 = StorageClass.Auto;
		int role1 = SymbolRole.Declaration;
		int role2 = SymbolRole.Reference;
		HighlightSymbol symbol1 = new HighlightSymbol(21, parentKind1, kind1, storage1, role1, ranges1);
		HighlightSymbol symbol2 = new HighlightSymbol(19, parentKind2, kind2, storage2, role2, ranges2);
		List<HighlightSymbol> symbols = new ArrayList<>();
		symbols.add(symbol1);
		symbols.add(symbol2);
		CquerySemanticHighlights exceptedHighlights = new CquerySemanticHighlights(uri, symbols);

		NotificationMessage expectedResult = new NotificationMessage();
		expectedResult.setJsonrpc("2.0"); //$NON-NLS-1$
		expectedResult.setMethod("$cquery/publishSemanticHighlighting"); //$NON-NLS-1$
		expectedResult.setParams(exceptedHighlights);
		assertParse(json, expectedResult);
	}
}
