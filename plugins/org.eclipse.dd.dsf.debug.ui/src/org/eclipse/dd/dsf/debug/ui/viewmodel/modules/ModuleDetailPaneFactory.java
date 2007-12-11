package org.eclipse.dd.dsf.debug.ui.viewmodel.modules;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jface.viewers.IStructuredSelection;

public class ModuleDetailPaneFactory implements IDetailPaneFactory {
	public static final String MODULE_DETAIL_PANE_ID = ModuleDetailPane.ID;
	public IDetailPane createDetailPane(String paneID) {
		return new ModuleDetailPane();
	}

	public String getDefaultDetailPane(IStructuredSelection selection) {
		return null;
	}

	public String getDetailPaneDescription(String paneID) {
		if (paneID.equals(ModuleDetailPane.ID)){
			return ModuleDetailPane.DESCRIPTION;
		}
		return null;
	}

	public String getDetailPaneName(String paneID) {
		if (paneID.equals(ModuleDetailPane.ID)){
			return ModuleDetailPane.NAME;
		}
		return null;
	}

	public Set getDetailPaneTypes(IStructuredSelection selection) {
		Set possibleIDs = new HashSet(1);
		possibleIDs.add(ModuleDetailPane.ID);
		return possibleIDs;
	}

}
