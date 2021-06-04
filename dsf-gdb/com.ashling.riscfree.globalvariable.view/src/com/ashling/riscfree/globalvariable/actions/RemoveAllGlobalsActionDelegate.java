package com.ashling.riscfree.globalvariable.actions;
import org.eclipse.cdt.dsf.datamodel.IDMContext;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.actions.ActionDelegate;

import com.ashling.riscfree.globalvariable.view.dsf.IGlobalVariableService;
import com.ashling.riscfree.globalvariable.view.utils.GlobalVariableServiceUtil;
 
/**
 * A delegate for the "Remove All Globals" action.
 */
/**
 * @implNote Copied and modified from  RemoveAllGlobalsActionDelegate.java in CDT
 * @author vinod.appu
 *
 */
public class RemoveAllGlobalsActionDelegate extends ActionDelegate implements IViewActionDelegate, IDebugEventSetListener {

	private IAction fAction;

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	@Override
	public void init( IViewPart view ) {
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#init(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void init( IAction action ) {
		DebugPlugin.getDefault().addDebugEventListener(this);
		fAction = action;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		update();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.actions.ActionDelegate#dispose()
	 */
	@Override
	public void dispose() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		fAction = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	@Override
	public void run(IAction action) {
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
					globalVariableService.removeAllGlobals(((IAdaptable) element).getAdapter(IDMContext.class));
				}
			}
		}
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

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse.debug.core.DebugEvent[])
	 */
	@Override
	public void handleDebugEvents( DebugEvent[] events ) {
		// The ICGlobalVariableManager will fire a target content-changed 
		// event when a global is added or removed. Update the enable/disable 
		// state of this action accordingly
		
		if (fAction != null) {
			for (int i = 0; i < events.length; i++) {
				final DebugEvent event = events[i];
				if (event.getSource() instanceof IDebugTarget
						&& event.getKind() == DebugEvent.CHANGE
						&& event.getDetail() == DebugEvent.CONTENT ) {
					update();
					break;
				}
			}
		}
	}
}
