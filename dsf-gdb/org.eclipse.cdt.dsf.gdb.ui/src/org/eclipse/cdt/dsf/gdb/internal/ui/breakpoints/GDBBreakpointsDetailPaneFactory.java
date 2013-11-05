package org.eclipse.cdt.dsf.gdb.internal.ui.breakpoints;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.debug.ui.IDetailPane;
import org.eclipse.debug.ui.IDetailPaneFactory;
import org.eclipse.jface.viewers.IStructuredSelection;

public class GDBBreakpointsDetailPaneFactory implements IDetailPaneFactory {

	@Override
	public Set<String> getDetailPaneTypes(IStructuredSelection selection) {
		Set<String> possibleIDs = new HashSet<String>(1);
		possibleIDs.add(GDBBreakpointsDetailPane.ID);
		return possibleIDs;
	}

	@Override
	public String getDefaultDetailPane(IStructuredSelection selection) {
		return null;
	}

	@Override
	public IDetailPane createDetailPane(String paneID) {
		return new GDBBreakpointsDetailPane2();
	}

	@Override
	public String getDetailPaneName(String paneID) {
		return (GDBBreakpointsDetailPane2.ID.equals(paneID)) ? GDBBreakpointsDetailPane2.NAME : null;
	}

	@Override
	public String getDetailPaneDescription(String paneID) {
		return (GDBBreakpointsDetailPane2.ID.equals(paneID)) ? GDBBreakpointsDetailPane2.DESCRIPTION : null;
	}

}
