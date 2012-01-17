/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.disassembly.commands;

import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.menus.UIElement;

/**
 * org.eclipse.cdt.debug.internal.ui.disassembly.commands.SetSteppingModeHandler: 
 * //TODO Add description.
 */
public class SetSteppingModeHandler extends AbstractHandler implements IElementUpdater {

    private static final String ID_PARAMETER_MODE = "com.arm.eclipse.rvd.ui.command.steppingMode.parameterMode"; //$NON-NLS-1$

    private String fCurrentValue = null;

    public SetSteppingModeHandler() {
        super();
        fCurrentValue = CDebugCorePlugin.getDefault().getPluginPreferences().getString( ICDebugConstants.PREF_STEP_MODE );
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#execute(org.eclipse.core.commands.ExecutionEvent)
     */
    @Override
	public Object execute( ExecutionEvent event ) throws ExecutionException {
        String param = event.getParameter( ID_PARAMETER_MODE );
        if ( param == null || param.equals( fCurrentValue ) )
            return null;
        
        fCurrentValue = param;
        CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_STEP_MODE, fCurrentValue );

        IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked( event );
        ICommandService service = (ICommandService)window.getService( ICommandService.class );
        service.refreshElements( event.getCommand().getId(), null );

        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.commands.IElementUpdater#updateElement(org.eclipse.ui.menus.UIElement, java.util.Map)
     */
    @Override
	@SuppressWarnings("unchecked")
    public void updateElement( UIElement element, Map parameters ) {
        String param = (String)parameters.get( ID_PARAMETER_MODE );
        if ( param != null ) {
            element.setChecked( ( fCurrentValue != null && fCurrentValue.equals( param ) ) );
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        IWorkbenchWindow window = CDebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        return ( window != null && getSteppingModeTarget( window ) != null );
    }

    /* (non-Javadoc)
     * @see org.eclipse.core.commands.AbstractHandler#isHandled()
     */
    @Override
    public boolean isHandled() {
        IWorkbenchWindow window = CDebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        return ( window != null && getSteppingModeTarget( window ) != null );
    }

    private ISteppingModeTarget getSteppingModeTarget( IWorkbenchWindow window ) {
        ISelection selection = DebugUITools.getDebugContextManager().getContextService( window ).getActiveContext();
        if ( selection instanceof IStructuredSelection ) {
            Object element = ((IStructuredSelection)selection).getFirstElement();
            if ( element instanceof IAdaptable )
                return (ISteppingModeTarget)((IAdaptable)element).getAdapter( ISteppingModeTarget.class );
        }
        return null;
    }
}
