package org.eclipse.cdt.debug.internal.ui.commands;

import org.eclipse.cdt.debug.internal.ui.actions.LoadModuleSymbolsActionDelegate;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class LoadSymbolsCommandHandler extends AbstractHandler implements IHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part = HandlerUtil.getActivePart(event);
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		LoadModuleSymbolsActionDelegate delegate = new LoadModuleSymbolsActionDelegate();
		IAction action = new Action() {		};// fake action
		delegate.selectionChanged(action, selection);
		delegate.setActivePart(action, part);
		if (action.isEnabled()) 
			delegate.run(action);
		return null;
	}
}
