/*******************************************************************************
 * Copyright (c) 2018 Marc-Andre Laperle.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.cdt.ui.tests.text;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.internal.ui.editor.asm.AsmTextEditor;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;

import junit.framework.TestCase;

/**
 * Tests for the AsmTextEditor
 */
public class AsmTextEditorTest extends TestCase {
	private static final String ASM_EDITOR_ID = "org.eclipse.cdt.ui.editor.asm.AsmEditor";
	private ICProject fCProject;

	@Override
	protected void setUp() throws Exception {
		fCProject = CProjectHelper.createCCProject("AsmTextEditorTestProject", "");
	}

	@Override
	protected void tearDown() throws Exception {
		if (fCProject != null)
			CProjectHelper.delete(fCProject);
	}

	public void testAlignConstSaveAction_Bug11111() throws Exception {
		IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(CUIPlugin.PLUGIN_ID);
		boolean oldValue = prefs.getBoolean(PreferenceConstants.ALIGN_ALL_CONST, false);
		prefs.putBoolean(PreferenceConstants.ALIGN_ALL_CONST, true);
		ByteArrayInputStream stream = new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8));
		String filename = "test.asm";
		IFile file = fCProject.getProject().getFile(filename);
		file.create(stream, true, null);
		AsmTextEditor editor = (AsmTextEditor) EditorTestHelper.openInEditor(file, ASM_EDITOR_ID, true);
		editor.doSave(null);
		EditorTestHelper.closeEditor(editor);
		prefs.putBoolean(PreferenceConstants.ALIGN_ALL_CONST, oldValue);
	}
}
