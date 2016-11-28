package org.eclipse.cdt.linkerscript.ui.form;

import org.eclipse.cdt.linkerscript.linkerScript.Statement;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IDetailsPage;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

public class StatementDetailsPage extends AbstractFormPart implements IDetailsPage {

	private ILinkerScriptModel model;
	private Text text;
	private String fInputURI;

	public StatementDetailsPage(ILinkerScriptModel model) {
		this.model = model;
	}

	@Override
	public void selectionChanged(IFormPart part, ISelection selection) {
		IStructuredSelection ssel = (IStructuredSelection) selection;
		if (ssel.size() == 1) {
			fInputURI = (String) ssel.getFirstElement();
		} else {
			fInputURI = null;
		}

		String t = model.readModel(fInputURI, Statement.class, "",
				sec -> NodeModelUtils.getTokenText(NodeModelUtils.getNode(sec)));
		text.setText(t);
	}

	@Override
	public void createContents(Composite parent) {
		parent.setLayout(GridLayoutFactory.fillDefaults().create());

		FormToolkit toolkit = getManagedForm().getToolkit();
		Section section = toolkit.createSection(parent, Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);
		section.setText("Details");
		section.setDescription("Details about Statement");
		section.setLayout(GridLayoutFactory.fillDefaults().create());
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING));

		text = toolkit.createText(section, "Initial Value", SWT.MULTI);
		text.setLayoutData(GridDataFactory.fillDefaults().grab(true, true).create());

		section.setClient(text);
	}

}
