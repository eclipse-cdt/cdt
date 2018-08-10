package org.eclipse.lsp4e.cpp.language;

import org.eclipse.cdt.internal.ui.text.CAutoIndentStrategy;
import org.eclipse.cdt.ui.CUIPlugin;

public class AutoIndentStrategyCPP extends CAutoIndentStrategy {

	public AutoIndentStrategyCPP() {
		super(CUIPlugin.getDefault().getTextTools().getDocumentPartitioning(), null, true);
	}

}
