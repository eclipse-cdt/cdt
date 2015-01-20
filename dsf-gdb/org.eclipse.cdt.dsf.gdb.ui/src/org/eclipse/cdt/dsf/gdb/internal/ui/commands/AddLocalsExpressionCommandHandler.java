package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionManager;

/**
 * Handling of adding group of locals into expression view
 *  
 * @since 2.4
 */
public class AddLocalsExpressionCommandHandler extends AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IExpressionManager expressionManager = DebugPlugin.getDefault().getExpressionManager();
		expressionManager.addExpression(expressionManager.newWatchExpression(getExpression()));
		return null;
	}

	protected String getExpression() {
		return "=*"; //$NON-NLS-1$
	}
}
