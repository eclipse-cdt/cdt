/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.text;

import java.io.File;
import java.net.URI;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.SourceViewer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.IPDOMManager;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.index.ResourceContainerRelativeLocationConverter;
import org.eclipse.cdt.core.index.URIRelativeLocationConverter;
import org.eclipse.cdt.core.index.provider.IPDOMDescriptor;
import org.eclipse.cdt.core.index.provider.IReadOnlyPDOMProvider;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.core.testplugin.util.TestSourceReader;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;

import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.index.provider.IndexProviderManager;
import org.eclipse.cdt.internal.core.index.provider.ReadOnlyPDOMProviderBridge;

import org.eclipse.cdt.internal.ui.editor.CEditor;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlighting;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingPresenter;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightings;

/**
 * Derived from JDT.
 *
 * @since 4.0
 */
public class AbstractSemanticHighlightingTest extends TestCase {
	
	protected static class SemanticHighlightingTestSetup extends TestSetup {

		private ICProject fCProject;
		private final String fTestFilename;
		
		public SemanticHighlightingTestSetup(Test test, String testFilename) {
			super(test);
			fTestFilename= testFilename;
		}
		
		protected void setUp() throws Exception {
			super.setUp();
			
			String sdkCode= 
				"void SDKFunction();\n"+
				"class SDKClass { public: SDKMethod(); };\n\n";
			
			final File sdk= createExternalSDK(sdkCode);
			sdk.deleteOnExit();

			fCProject= EditorTestHelper.createCProject(PROJECT, LINKED_FOLDER);

			importExternalSDK(sdk, fCProject);

			fEditor= (CEditor) EditorTestHelper.openInEditor(ResourceTestHelper.findFile(fTestFilename), true);
			fSourceViewer= EditorTestHelper.getSourceViewer(fEditor);
			assertTrue(EditorTestHelper.joinReconciler(fSourceViewer, 500, 10000, 100));
			EditorTestHelper.joinBackgroundActivities();
		}

		private static void importExternalSDK(final File sdk, final ICProject associatedProject) {
			final URI baseURI= new File("c:/ExternalSDK/").toURI();
			IndexProviderManager ipm= CCoreInternals.getPDOMManager().getIndexProviderManager();
			ipm.addIndexProvider(new ReadOnlyPDOMProviderBridge(
					new IReadOnlyPDOMProvider() {
						public IPDOMDescriptor[] getDescriptors(
								ICConfigurationDescription config) {
							return new IPDOMDescriptor[] {
									new IPDOMDescriptor() {
										public IIndexLocationConverter getIndexLocationConverter() {
											return new URIRelativeLocationConverter(baseURI);
										}

										public IPath getLocation() {
											return new Path(sdk.getAbsolutePath());
										}

									}
							};
						}
						public boolean providesFor(ICProject project)
						throws CoreException {
							return associatedProject.equals(project);
						}
					}
			));
		}

		private static File createExternalSDK(final String code) throws Exception {
			final File sdk= File.createTempFile("foo", "bar");

			ICProject cproject= CProjectHelper.createCCProject("foo"+System.currentTimeMillis(), null, IPDOMManager.ID_FAST_INDEXER);
			TestSourceReader.createFile(cproject.getProject(), new Path("/this.h"), code);
			CCorePlugin.getIndexManager().joinIndexer(5000, new NullProgressMonitor());

			ResourceContainerRelativeLocationConverter cvr= new ResourceContainerRelativeLocationConverter(cproject.getProject());
			CCoreInternals.getPDOMManager().exportProjectPDOM(cproject, sdk, cvr);
			assertTrue(sdk.exists());

			CProjectHelper.delete(cproject);
			return sdk;
		}

		protected String getTestFilename() {
			return fTestFilename;
		}

		protected void tearDown () throws Exception {
			EditorTestHelper.closeEditor(fEditor);
			
			IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
			store.setToDefault(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED);
			
			SemanticHighlighting[] semanticHighlightings= SemanticHighlightings.getSemanticHighlightings();
			for (int i= 0, n= semanticHighlightings.length; i < n; i++) {
				String enabledPreferenceKey= SemanticHighlightings.getEnabledPreferenceKey(semanticHighlightings[i]);
				if (!store.isDefault(enabledPreferenceKey))
					store.setToDefault(enabledPreferenceKey);
			}

			if (fCProject != null)
				CProjectHelper.delete(fCProject);
			
			super.tearDown();
		}
	}
	
	public static final String LINKED_FOLDER= "resources/semanticHighlighting";
	
	public static final String PROJECT= "SHTest";
	
	private static CEditor fEditor;
	
	private static SourceViewer fSourceViewer;

	private String fCurrentHighlighting;

	protected void setUp() throws Exception {
		super.setUp();
		disableAllSemanticHighlightings();
		EditorTestHelper.runEventQueue(1000);
	}
	
	protected void assertEqualPositions(Position[] expected, Position[] actual) {
		assertEquals(expected.length, actual.length);
		for (int i= 0, n= expected.length; i < n; i++) {
			assertEquals(expected[i].isDeleted(), actual[i].isDeleted());
			assertEquals(expected[i].getOffset(), actual[i].getOffset());
			assertEquals(expected[i].getLength(), actual[i].getLength());
		}
	}

	protected Position createPosition(int line, int column, int length) throws BadLocationException {
		IDocument document= fSourceViewer.getDocument();
		return new Position(document.getLineOffset(line) + column, length);
	}

	String toString(Position[] positions) throws BadLocationException {
		StringBuffer buf= new StringBuffer();
		buf.append("// "+fCurrentHighlighting+'\n');
		IDocument document= fSourceViewer.getDocument();
		buf.append("Position[] expected= new Position[] {\n");
		for (int i= 0, n= positions.length; i < n; i++) {
			Position position= positions[i];
			int line= document.getLineOfOffset(position.getOffset());
			int column= position.getOffset() - document.getLineOffset(line);
			buf.append("\tcreatePosition(" + line + ", " + column + ", " + position.getLength() + "),\n");
		}
		buf.append("};\n");
		return buf.toString();
	}

	protected Position[] getSemanticHighlightingPositions() throws BadPositionCategoryException {
		SemanticHighlightingManager manager= (SemanticHighlightingManager) new Accessor(fEditor, CEditor.class).get("fSemanticManager");
		SemanticHighlightingPresenter presenter= (SemanticHighlightingPresenter) new Accessor(manager, manager.getClass()).get("fPresenter");
		String positionCategory= (String) new Accessor(presenter, presenter.getClass()).invoke("getPositionCategory", new Object[0]);
		IDocument document= fSourceViewer.getDocument();
		return document.getPositions(positionCategory);
	}

	protected void setUpSemanticHighlighting(String semanticHighlighting) {
		fCurrentHighlighting= semanticHighlighting;
		enableSemanticHighlighting(semanticHighlighting);
		// give enough time to finish updating the highlighting positions
		EditorTestHelper.runEventQueue(1000);
	}

	private void enableSemanticHighlighting(String preferenceKey) {
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(getEnabledPreferenceKey(preferenceKey), true);
	}

	private String getEnabledPreferenceKey(String preferenceKey) {
		return PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + preferenceKey + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED_SUFFIX;
	}
	
	private static void disableAllSemanticHighlightings() {
		IPreferenceStore store= CUIPlugin.getDefault().getPreferenceStore();
		store.setValue(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED, true);
		SemanticHighlighting[] semanticHilightings= SemanticHighlightings.getSemanticHighlightings();
		for (int i= 0, n= semanticHilightings.length; i < n; i++) {
			SemanticHighlighting semanticHilighting= semanticHilightings[i];
			if (store.getBoolean(SemanticHighlightings.getEnabledPreferenceKey(semanticHilighting)))
				store.setValue(SemanticHighlightings.getEnabledPreferenceKey(semanticHilighting), false);
		}
	}
}
