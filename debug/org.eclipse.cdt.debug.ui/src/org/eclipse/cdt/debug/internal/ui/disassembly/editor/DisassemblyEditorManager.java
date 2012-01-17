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

package org.eclipse.cdt.debug.internal.ui.disassembly.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextListener;
import org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextProvider;
import org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextService;
import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.internal.ui.disassembly.viewer.DisassemblyDocumentProvider;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.ui.ISourcePresentation;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.progress.UIJob;

public class DisassemblyEditorManager implements IWindowListener, IDisassemblyContextListener, IPartListener2 {

    private Map<Object, IEditorPart> fEditorParts;
    private Map<Object, String> fOpenDisassemblyPolicy;
    private Map<Object, ISteppingModeTarget> fSteppingModePolicy;
    private DisassemblyDocumentProvider fDocumentProvider;
    
    public DisassemblyEditorManager() {
        fDocumentProvider = new DisassemblyDocumentProvider();
        fEditorParts = new HashMap<Object, IEditorPart>();
        fOpenDisassemblyPolicy = new HashMap<Object, String>();
        fSteppingModePolicy = new HashMap<Object, ISteppingModeTarget>();
        CDebugUIPlugin.getDefault().getWorkbench().addWindowListener( this );
        IWorkbenchWindow window = CDebugUIPlugin.getDefault().getWorkbench().getActiveWorkbenchWindow();
        if ( window != null ) {
            window.getPartService().addPartListener( this );
        }
        getDisassemblyManager().addDisassemblyContextListener( this );
    }

    public void dispose() {
        getDisassemblyManager().removeDisassemblyContextListener( this );
        CDebugUIPlugin.getDefault().getWorkbench().removeWindowListener( this );
        fSteppingModePolicy.clear();
        fOpenDisassemblyPolicy.clear();
        fEditorParts.clear();
        fDocumentProvider.dispose();
    }

    public DisassemblyDocumentProvider getDocumentProvider() {
        return fDocumentProvider;
    }

    public void openEditor( IWorkbenchPage page, Object debugContext ) throws DebugException {
        Object disassemblyContext = getDiassemblyContext( debugContext );
        if ( disassemblyContext != null ) {
            IEditorPart editor = fEditorParts.get( disassemblyContext );
            if ( editor == null ) {
                ISourcePresentation sp = getSourcePresentation();
                if ( sp == null ) {
                    throw new DebugException( new Status( IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), 0, "No disassembly editor found", null ) );
                }
                IEditorInput input = sp.getEditorInput( debugContext );
                try {
                    editor = IDE.openEditor( page, input, sp.getEditorId( input, disassemblyContext ) );
                    fEditorParts.put( disassemblyContext, editor );
                    ISteppingModeTarget steppingModeTarget = getSteppingModeTarget( debugContext );
                    if ( steppingModeTarget != null ) {
                        if ( ICDebugConstants.PREF_VALUE_STEP_MODE_CONTEXT.equals( 
                                CDebugCorePlugin.getDefault().getPluginPreferences().getString( ICDebugConstants.PREF_STEP_MODE ) ) )
                            steppingModeTarget.enableInstructionStepping( true );
                        fSteppingModePolicy.put( disassemblyContext, steppingModeTarget );
                    }
                }
                catch( PartInitException e ) {
                    throw new DebugException( e.getStatus() );
                }
            }
            page.activate( editor );
        }
    }

    public IEditorPart findEditor( IWorkbenchPage page, Object debugContext ) {
        Object disassemblyContext = getDiassemblyContext( debugContext );
        return ( disassemblyContext != null ) ? fEditorParts.get( disassemblyContext ) : null;
    }

    public String getOpenDisassemblyMode( Object debugContext ) {
        String mode = MessageDialogWithToggle.NEVER;
        Object disassemblyContext = getDiassemblyContext( debugContext );
        if ( disassemblyContext != null ) {
            // shouldn't happen
            mode = fOpenDisassemblyPolicy.get( disassemblyContext );
            if ( mode == null ) {
                IPreferenceStore prefs = CDebugUIPlugin.getDefault().getPreferenceStore();
                mode = prefs.getString( IInternalCDebugUIConstants.PREF_OPEN_DISASSEMBLY_MODE );
                fOpenDisassemblyPolicy.put( disassemblyContext, mode );
            }
        }
        return mode;
    }

    public void setOpenDisassemblyMode( Object debugContext, String mode ) {
        Object disassemblyContext = getDiassemblyContext( debugContext );
        if ( disassemblyContext == null )
            return;
        fOpenDisassemblyPolicy.put( disassemblyContext, mode );
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
	public void partActivated( IWorkbenchPartReference partRef ) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partBroughtToTop(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
	public void partBroughtToTop( IWorkbenchPartReference partRef ) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partClosed(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
	public void partClosed( IWorkbenchPartReference partRef ) {
        if ( isDisassemblyEditorPart( partRef ) ) {
            IWorkbenchPart part = partRef.getPart( false );
            if ( part instanceof IEditorPart ) {
                IEditorInput input = ((IEditorPart)part).getEditorInput();
                if ( input instanceof DisassemblyEditorInput ) {
                    Object disassemblyContext = ((DisassemblyEditorInput)input).getDisassemblyContext();
                    fEditorParts.remove( disassemblyContext );
                    ISteppingModeTarget steppingModeTarget = fSteppingModePolicy.remove( disassemblyContext );
                    if ( steppingModeTarget != null 
                         && ICDebugConstants.PREF_VALUE_STEP_MODE_CONTEXT.equals( 
                                 CDebugCorePlugin.getDefault().getPluginPreferences().getString( ICDebugConstants.PREF_STEP_MODE ) ) )
                           steppingModeTarget.enableInstructionStepping( false );
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partDeactivated(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
	public void partDeactivated( IWorkbenchPartReference partRef ) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partHidden(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
	public void partHidden( IWorkbenchPartReference partRef ) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partInputChanged(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
	public void partInputChanged( IWorkbenchPartReference partRef ) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partOpened(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
	public void partOpened( IWorkbenchPartReference partRef ) {
        if ( isDisassemblyEditorPart( partRef ) ) {
            IWorkbenchPart part = partRef.getPart( false );
            if ( part instanceof IEditorPart ) {
                IEditorInput input = ((IEditorPart)part).getEditorInput();
                if ( input instanceof DisassemblyEditorInput ) {
                    Object disassemblyContext = ((DisassemblyEditorInput)input).getDisassemblyContext();
                    fEditorParts.put( disassemblyContext, (IEditorPart)part );
                }
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
     */
    @Override
	public void partVisible( IWorkbenchPartReference partRef ) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWindowListener#windowActivated(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
	public void windowActivated( IWorkbenchWindow window ) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWindowListener#windowClosed(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
	public void windowClosed( IWorkbenchWindow window ) {
        window.getPartService().removePartListener( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWindowListener#windowDeactivated(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
	public void windowDeactivated( IWorkbenchWindow window ) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.IWindowListener#windowOpened(org.eclipse.ui.IWorkbenchWindow)
     */
    @Override
	public void windowOpened( IWorkbenchWindow window ) {
        window.getPartService().addPartListener( this );
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextListener#contextAdded(java.lang.Object)
     */
    @Override
	public void contextAdded( Object context ) {
        // TODO Auto-generated method stub
        
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextListener#contextRemoved(java.lang.Object)
     */
    @Override
	public void contextRemoved( Object context ) {
        final IEditorPart editor = fEditorParts.remove( context );
        if ( editor != null ) {
            UIJob job = new UIJob( editor.getSite().getShell().getDisplay(), "Closing disassembly" ) { //$NON-NLS-1$

                /* (non-Javadoc)
                 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
                 */
                @Override
                public IStatus runInUIThread( IProgressMonitor monitor ) {
                    editor.getSite().getPage().closeEditor( editor, false );
                    return Status.OK_STATUS;
                }
            };
            job.setSystem( true );
            job.schedule();
        }
    }

    private IDisassemblyContextService getDisassemblyManager() {
        return CDebugCorePlugin.getDefault().getDisassemblyContextService();
    }

    private boolean isDisassemblyEditorPart( IWorkbenchPartReference partRef ) {
        // TODO: check all editors contributed via the extension point
        return ( partRef.getId().equals( ICDebugUIConstants.ID_DEFAULT_DISASSEMBLY_EDITOR ) );
    }

    private ISourcePresentation getSourcePresentation() {

        return new ISourcePresentation() {

            @Override
			public IEditorInput getEditorInput( Object element ) {
                Object disassemblyContext = getDiassemblyContext( element );
                return new DisassemblyEditorInput( element, disassemblyContext );
            }

            @Override
			public String getEditorId( IEditorInput input, Object element ) {
                return ICDebugUIConstants.ID_DEFAULT_DISASSEMBLY_EDITOR;
            }
        };
    }

    protected Object getDiassemblyContext( Object element ) {
        IDisassemblyContextProvider adapter = getDisassemblyContextProvider( element );
        return ( adapter != null ) ? adapter.getDisassemblyContext( element ) : null;
    }

    private IDisassemblyContextProvider getDisassemblyContextProvider( Object element ) {
        IDisassemblyContextProvider adapter = null;
        if ( element instanceof IDisassemblyContextProvider ) {
            adapter = (IDisassemblyContextProvider)element;
        }
        else if ( element instanceof IAdaptable ) {
            IAdaptable adaptable = (IAdaptable)element;
            adapter = (IDisassemblyContextProvider)adaptable.getAdapter( IDisassemblyContextProvider.class );
        }
        return adapter;
    }

    private ISteppingModeTarget getSteppingModeTarget( Object debugContext ) {
        if ( debugContext instanceof ISteppingModeTarget )
            return (ISteppingModeTarget)debugContext;
        if ( debugContext instanceof IAdaptable )
            return (ISteppingModeTarget)((IAdaptable)debugContext).getAdapter( ISteppingModeTarget.class );
        return null;
    }
}
