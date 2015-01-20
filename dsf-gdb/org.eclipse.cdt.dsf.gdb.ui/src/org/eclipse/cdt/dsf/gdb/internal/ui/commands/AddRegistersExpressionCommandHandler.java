package org.eclipse.cdt.dsf.gdb.internal.ui.commands;

/**
 * Handling of adding group of registers into expression view
 *  
 * @since 2.4
 */
public class AddRegistersExpressionCommandHandler extends AddLocalsExpressionCommandHandler {
	@Override
	protected String getExpression() {
		return "=$*"; //$NON-NLS-1$
	}
}
