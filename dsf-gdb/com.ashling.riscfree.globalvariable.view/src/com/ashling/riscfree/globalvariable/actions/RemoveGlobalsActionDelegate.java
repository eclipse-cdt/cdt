package com.ashling.riscfree.globalvariable.actions;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.debug.service.IExpressions.IExpressionDMContext;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.IDMVMContext;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;

import com.ashling.riscfree.globalvariable.view.dsf.IGlobalVariableService;
import com.ashling.riscfree.globalvariable.view.utils.GlobalVariableServiceUtil;
 
/**
 * A delegate for the "Remove Globals" action.
 */
/**
 * @implNote Copied and modified from  RemoveGlobalsActionDelegate.java in CDT
 * @author vinod.appu
 *
 */
public class RemoveGlobalsActionDelegate extends ActionDelegate implements IViewActionDelegate {

	private IAction fAction;

	private ISelection fSelection;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init( IViewPart view ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate2#init(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void init( IAction action ) {
		setAction( action );
		update();
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run( IAction action ) {
		ISelection selection = getSelection();
		if ( !(selection instanceof IStructuredSelection) )
			return;
		IStructuredSelection ss = (IStructuredSelection)selection;
		final Iterator it = ss.iterator();
		List<IExpressionDMContext> list = new ArrayList<>( ss.size() );
		while( it.hasNext() ) {
			Object element = it.next();
			IDMContext regContext = null;
			if (element instanceof IDMVMContext) {
				regContext = ((IDMVMContext) element).getDMContext();
				IExpressionDMContext globalVariableDMC = DMContexts.getAncestorOfType(regContext,
						IExpressionDMContext.class);
				if (globalVariableDMC != null) {
					list.add(globalVariableDMC);
				}
			}
		}
		if ( list.size() == 0 )
			return;
		final IExpressionDMContext[] globals = (IExpressionDMContext[])list.toArray( new IExpressionDMContext[list.size()] );
		

		IAdaptable element = DebugUITools.getDebugContext();
		if (element instanceof IAdaptable) {
			ILaunch launch = ((IAdaptable) element).getAdapter(ILaunch.class);
			if (launch != null && !launch.isTerminated()) {
				DsfSession dsfSession = null;
				if (launch instanceof GdbLaunch) {
					dsfSession = ((GdbLaunch) launch).getSession();
				}
				if (dsfSession != null) {
					IGlobalVariableService globalVariableService = GlobalVariableServiceUtil.INSTANCE
							.getGlobalVariablesService(dsfSession,
									((IAdaptable) element).getAdapter(IDMContext.class), launch);
					globalVariableService.removeGlobals(element.getAdapter(IDMContext.class), globals);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged( IAction action, ISelection selection ) {
		setSelection( selection );
		update();
	}

	protected IAction getAction() {
		return fAction;
	}

	protected ISelection getSelection() {
		return fSelection;
	}

	private void setAction( IAction action ) {
		fAction = action;
	}

	private void setSelection( ISelection selection ) {
		fSelection = selection;
	}

	/**
	 * Enables/disables the action based on whether there are any globals in the
	 * variables view.
	 */
	private void update() {
		final IAction action = fAction;
		if (action != null) {			
			final IAdaptable context = DebugUITools.getDebugContext();
			boolean enabled = false;
			if (context instanceof IAdaptable) {
				ILaunch launch = ((IAdaptable) context).getAdapter(ILaunch.class);
				if (launch != null) {
					enabled = !launch.isTerminated();
				}
			}
			action.setEnabled(enabled);
		}
	}
}
