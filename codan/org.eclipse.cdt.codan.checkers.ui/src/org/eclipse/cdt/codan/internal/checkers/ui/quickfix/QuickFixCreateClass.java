package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.internal.core.model.CodanProblemMarker;
import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.ui.wizards.NewClassCreationWizard;
import org.eclipse.cdt.ui.wizards.NewClassCreationWizardPage;
import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class QuickFixCreateClass extends AbstractCodanCMarkerResolution{

	@Override
	public String getLabel() {
		return Messages.QuickFixCreateClass_Label;
	}

	@Override
	public void apply(IMarker marker, IDocument document) {
		run(marker);
	}
	
	public void run(final IMarker marker) {
		
		try {
			IFile file = FileBuffers.getWorkspaceFileAtLocation(marker.getResource().getLocation());
			ITranslationUnit tu = (ITranslationUnit) CCorePlugin.getDefault().getCoreModel().create(file);
			
			int lineNumber = marker.getAttribute(IMarker.LINE_NUMBER, 0);
			ICElement element = tu.getElementAtLine(lineNumber);
			NewClassCreationWizard wizard = new NewClassCreationWizard() {
			    @Override
				public void addPages() {
			        super.addPages();
			        if (super.getPageCount() == 1) {
			        	IWizardPage page = super.getPages()[0];
			        	
			        	if (page instanceof NewClassCreationWizardPage) {
			        		NewClassCreationWizardPage p = (NewClassCreationWizardPage) page;
			        		String className = getClassName();
			        		if (className != null) {
			        			p.setClassName(className, true);
			        			p.setNamespaceSelection(false, true);
			        			p.setNamespaceText("", true); //$NON-NLS-1$
			        			p.setHeaderFileText(NLS.bind(Messages.QuickFixCreateClass_HeaderFileText, className), true);
			        			p.setSourceFileText(NLS.bind(Messages.QuickFixCreateClass_SourceFileText, className), true);
			        		}
			        	}
			        }
			    }

				private String getClassName() {
					
					return CodanProblemMarker.getProblemArgument(marker, 0);
				}
			};
			
			wizard.init(PlatformUI.getWorkbench(), new StructuredSelection(element));
			WizardDialog wd = new WizardDialog(null, wizard);
			wd.open();			
		} catch (CModelException e) {

			e.printStackTrace();
		}
	}
}
