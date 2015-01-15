package org.eclipse.cdt.debug.internal.ui.commands;

import org.eclipse.cdt.debug.internal.ui.actions.LoadSymbolsForAllActionDelegate;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class LoadAllSymbolsCommandHandler extends AbstractHandler implements IHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		final IWorkbenchPart part = HandlerUtil.getActivePart(event);
		LoadSymbolsForAllActionDelegate delegate = new LoadSymbolsForAllActionDelegate();
		if (part instanceof IViewPart) {
			delegate.init((IViewPart) part);
			delegate.run(null);
		}
		return null;
	}
}
