/*******************************************************************************
 * Copyright (c) 2026 vogella GmbH and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Iterator;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.testplugin.ResourceTestHelper;
import org.eclipse.cdt.ui.tests.BaseUITestCase5;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests that the folding ruler column uses the configured editor background.
 */
public class FoldingRulerColorTest extends BaseUITestCase5 {

	private static final String PROJECT = "FoldingRulerColorTest";
	private static final RGB BACKGROUND = new RGB(30, 31, 34);

	private ICProject fCProject;
	private CEditor fEditor;

	@BeforeEach
	protected void setUpFoldingRulerColorTest() throws Exception {
		CUIPlugin.getDefault().getPreferenceStore().setValue(PreferenceConstants.EDITOR_FOLDING_ENABLED, true);
		IPreferenceStore editorStore = EditorsUI.getPreferenceStore();
		editorStore.setValue(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT, false);
		PreferenceConverter.setValue(editorStore, AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND, BACKGROUND);

		fCProject = EditorTestHelper.createCProject(PROJECT, "resources/folding");
		fEditor = (CEditor) EditorTestHelper
				.openInEditor(ResourceTestHelper.findFile("/" + PROJECT + "/src/FoldingTest.cpp"), true);
	}

	@AfterEach
	protected void tearDownFoldingRulerColorTest() throws Exception {
		EditorTestHelper.closeAllEditors();
		if (fCProject != null)
			CProjectHelper.delete(fCProject);
		CUIPlugin.getDefault().getPreferenceStore().setToDefault(PreferenceConstants.EDITOR_FOLDING_ENABLED);
		IPreferenceStore editorStore = EditorsUI.getPreferenceStore();
		editorStore.setToDefault(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT);
		editorStore.setToDefault(AbstractTextEditor.PREFERENCE_COLOR_BACKGROUND);
	}

	@Test
	public void testProjectionRulerColumnUsesEditorBackground() throws Exception {
		assertEquals(BACKGROUND, fEditor.getViewer().getTextWidget().getBackground().getRGB());

		// ProjectionRulerColumn is package private in JFace, so match it by name
		IVerticalRulerColumn projectionColumn = null;
		CompositeRuler ruler = (CompositeRuler) fEditor.getAdapter(IVerticalRulerInfo.class);
		Iterator<IVerticalRulerColumn> columns = ruler.getDecoratorIterator();
		while (columns.hasNext()) {
			IVerticalRulerColumn column = columns.next();
			if (column.getClass().getSimpleName().equals("ProjectionRulerColumn")) {
				projectionColumn = column;
			}
		}
		assertNotNull(projectionColumn, "no folding ruler column");
		assertEquals(BACKGROUND, projectionColumn.getControl().getBackground().getRGB());
	}
}
