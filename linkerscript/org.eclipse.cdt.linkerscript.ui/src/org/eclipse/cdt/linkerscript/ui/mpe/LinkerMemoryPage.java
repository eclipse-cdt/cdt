package org.eclipse.cdt.linkerscript.ui.mpe;

import org.eclipse.cdt.linkerscript.ui.form.MemoryTableViewer;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

public class LinkerMemoryPage extends FormPage {
	public static final String ID = "memory.section"; //$NON-NLS-1$
	private MemoryTableViewer memoryTableViewer;

	public LinkerMemoryPage(FormEditor editor) {
		super(editor, ID, "Memory");
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		ScrolledForm form = managedForm.getForm();
		form.setText("Linker Script Settings");
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());

		Composite body = managedForm.getForm().getBody();
		body.setLayout(GridLayoutFactory.fillDefaults().create());
		Section section = toolkit.createSection(body, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText("Memory Regions");
		section.setDescription("Specify memory regions by defining the location and size.");
		section.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		section.setLayout(GridLayoutFactory.fillDefaults().create());
		memoryTableViewer = new MemoryTableViewer(section, toolkit);
		memoryTableViewer.setInput(new Model(((MultiPageLinkerScriptEditor) getEditor())::getXtextDocument));
		section.setClient(memoryTableViewer.getControl());
		memoryTableViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

	}
}
