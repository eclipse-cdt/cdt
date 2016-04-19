package org.eclipse.cdt.ui.tests.text;

import java.util.ListResourceBundle;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.SourceViewer;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.testplugin.CProjectHelper;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.testplugin.EditorTestHelper;
import org.eclipse.cdt.ui.testplugin.ResourceTestHelper;

import org.eclipse.cdt.internal.ui.actions.AlignConstAction;
import org.eclipse.cdt.internal.ui.editor.CEditor;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the Const Alignment.
 */
public class AlignConstActionTest extends TestCase {
	private static final String PROJECT= "AlignConstTests";

	private static final class EmptyBundle extends ListResourceBundle {
		@Override
		protected Object[][] getContents() {
			return new Object[0][];
		}
	}
	
	protected static class AlignConstTestSetup extends TestSetup {
		private ICProject fCProject;
		
		public AlignConstTestSetup(Test test) {
			super(test);
		}
		
		@Override
		protected void setUp() throws Exception {
			super.setUp();
			
			fCProject= EditorTestHelper.createCProject(PROJECT, "resources/constalign");
			fCProject.setOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, CCorePlugin.TAB);
		}

		@Override
		protected void tearDown () throws Exception {
			if (fCProject != null)
				CProjectHelper.delete(fCProject);
			
			super.tearDown();
		}
	}
	
	public static Test suite() {
		return new AlignConstTestSetup(new TestSuite(AlignConstActionTest.class));
	}
	
	private CEditor fEditor;
	private SourceViewer fSourceViewer;
	private IDocument fDocument;

	@Override
	protected void setUp() throws Exception {
		String filename= createFileName("Before");
		fEditor= (CEditor) EditorTestHelper.openInEditor(ResourceTestHelper.findFile(filename), true);
		fSourceViewer= EditorTestHelper.getSourceViewer(fEditor);
		fDocument= fSourceViewer.getDocument();
		
		// set preference to align right
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.PLACE_CONST_RIGHT_OF_TYPE, true);
	}
	
	@Override
	protected void tearDown() throws Exception {
		
		// set preference to align right
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.PLACE_CONST_RIGHT_OF_TYPE, false);
				
		EditorTestHelper.closeEditor(fEditor);
	}
	
	private void assertIndentResult() throws Exception {
		String afterFile= createFileName("After");
		String expected= ResourceTestHelper.read(afterFile).toString();
		
		new AlignConstAction(new EmptyBundle(), "prefix", fEditor, false).run();
		
		assertEquals(expected, fDocument.get());
	}
	
	private String createFileName(String qualifier) {
		String name= getName();
		name= name.substring(4, 5).toLowerCase() + name.substring(5);
		return "/" + PROJECT + "/src/" + name + "/" + qualifier + ".cpp";
	}
	
	private void selectAll() {
		fSourceViewer.setSelectedRange(0, fDocument.getLength());
	}
	
	public void testUnchanged() throws Exception {
		selectAll();
		assertIndentResult();
	}

	public void testSample() throws Exception {
		selectAll();
		assertIndentResult();
	}

	public void testComplex() throws Exception {
		selectAll();
		assertIndentResult();
	}

}
