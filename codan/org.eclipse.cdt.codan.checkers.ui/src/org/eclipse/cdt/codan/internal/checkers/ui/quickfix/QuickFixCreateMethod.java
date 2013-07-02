package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.cdt.codan.ui.AbstractCodanCMarkerResolution;
import org.eclipse.cdt.ui.refactoring.actions.CreateMethodAction;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.text.IDocument;

public class QuickFixCreateMethod extends AbstractCodanCMarkerResolution {

	@Override
	public String getLabel() {
		return Messages.QuickFixCreateMethod_Label;
	}

	@Override
	public void apply(IMarker marker, IDocument document) {
		run(marker);
	}
	
	public void run(IMarker marker) {
		CreateMethodAction action = new CreateMethodAction();
		action.run(marker);
	}
}
