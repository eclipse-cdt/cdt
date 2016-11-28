package org.eclipse.cdt.linkerscript.ui.mpe;

import org.eclipse.cdt.linkerscript.ui.form.SectionMasterDetailsBlock;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;

public class LinkerSectionPage extends FormPage {
	public static final String ID = "sections.section"; //$NON-NLS-1$
	private SectionMasterDetailsBlock sectionMasterDetailsBlock;

	public LinkerSectionPage(FormEditor editor) {
		super(editor, ID, "Sections");
		sectionMasterDetailsBlock = new SectionMasterDetailsBlock(
				new Model(((MultiPageLinkerScriptEditor) getEditor())::getXtextDocument));
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		sectionMasterDetailsBlock.createContent(managedForm);

	}
}
