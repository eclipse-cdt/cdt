package org.eclipse.cdt.linkerscript.ui.form;

import java.util.LinkedHashMap;

import org.eclipse.cdt.linkerscript.linkerScript.OutputSection;
import org.eclipse.cdt.linkerscript.linkerScript.Statement;
import org.eclipse.cdt.linkerscript.linkerScript.StatementAssignment;
import org.eclipse.cdt.linkerscript.linkerScript.StatementInputSection;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.DetailsPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IDetailsPageProvider;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.MasterDetailsBlock;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

public class SectionMasterDetailsBlock extends MasterDetailsBlock {

	private SectionTreeViewer sectionTreeViewer;
	private ILinkerScriptModel model;

	/**
	 * Map of types to pages, order is important as object will be tested
	 * against each key in order
	 */
	private static LinkedHashMap<Class<? extends EObject>, IDetailsPage> pages = new LinkedHashMap<>();

	public SectionMasterDetailsBlock(ILinkerScriptModel model) {
		this.model = model;

		pages.put(OutputSection.class, new OutputSectionDetailsPage(model));
		pages.put(StatementAssignment.class, new StatementAssignmentDetailsPage(model));
		pages.put(StatementInputSection.class, new StatementInputSectionDetailsPage(model));
		pages.put(Statement.class, new StatementDetailsPage(model));
	}

	@Override
	protected void createMasterPart(IManagedForm managedForm, Composite parent) {
		ScrolledForm form = managedForm.getForm();
		form.setText("Linker Script Sections Settings");
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());

		Composite body = parent;//managedForm.getForm().getBody();
		body.setLayout(GridLayoutFactory.fillDefaults().create());
		Section section = toolkit.createSection(body, Section.DESCRIPTION | Section.TITLE_BAR);
		section.setText("Defined Sections"); // TODO better text here
		// TODO better text here
		section.setDescription("Specify sections and other statements");
		section.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());
		section.setLayout(GridLayoutFactory.fillDefaults().create());
		sectionTreeViewer = new SectionTreeViewer(section, toolkit);
		sectionTreeViewer.setInput(model);
		section.setClient(sectionTreeViewer.getControl());
		sectionTreeViewer.getControl().setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		SectionPart sectionPart = new SectionPart(section);
		sectionTreeViewer.addSelectionChangedListener(event -> {
			managedForm.fireSelectionChanged(sectionPart, event.getSelection());
		});
	}

	@Override
	protected void registerPages(DetailsPart detailsPart) {
		detailsPart.setPageLimit(0);
		pages.forEach(detailsPart::registerPage);
		detailsPart.setPageProvider(new IDetailsPageProvider() {

			@Override
			public Object getPageKey(Object object) {
				return model.readModel(object, EObject.class, null, t -> {
					for (Class<? extends EObject> clazz : pages.keySet()) {
						if (clazz.isInstance(t)) {
							return clazz;
						}
					}
					return null;
				});
			}

			@Override
			public IDetailsPage getPage(Object key) {
				// pages are all static for now
				return null;
			}
		});

	}

	@Override
	protected void createToolBarActions(IManagedForm managedForm) {
		// TODO Auto-generated method stub

	}

}
