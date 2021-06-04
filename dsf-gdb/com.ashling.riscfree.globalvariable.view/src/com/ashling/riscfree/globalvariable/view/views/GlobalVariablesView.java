package com.ashling.riscfree.globalvariable.view.views;

import org.eclipse.debug.internal.ui.views.variables.VariablesView;

public class GlobalVariablesView extends VariablesView {
	public static final String GLOBAL_VARIABLE_VIEW = "com.ashling.riscfree.globalvariable.view";

	@Override
	protected String getPresentationContextId() {
		return GLOBAL_VARIABLE_VIEW;
	}
}
