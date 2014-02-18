package org.eclipse.cdt.internal.ui.refactoring.pullup.ui;

public interface HasActions {

	public TargetActions getActions();
	
	public String getSelectedAction();
	
	public void setSelectedAction(String action);
}
