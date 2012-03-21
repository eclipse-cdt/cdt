/*******************************************************************************
 * Copyright (c) 2011 Mentor Graphics and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Mentor Graphics - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.dsf.debug.ui.actions;

import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.debug.internal.ui.breakpoints.CBreakpointContext;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.breakpoints.IToggleBreakpointsTargetCExtension;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.DisassemblySelection;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblyPart;
import org.eclipse.cdt.dsf.debug.internal.ui.disassembly.provisional.IDisassemblySelection;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension2;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.texteditor.SimpleMarkerAnnotation;

/**
 * Base class for toggle breakpoint targets for the disassembly part.
 * @since 2.2
 */
public abstract class AbstractDisassemblyBreakpointsTarget 
    implements IToggleBreakpointsTargetExtension2, IToggleBreakpointsTargetCExtension
{

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleLineBreakpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
		assert part instanceof IDisassemblyPart && selection instanceof ITextSelection;

		if ( !(selection instanceof IDisassemblySelection) ) {
			selection = new DisassemblySelection( (ITextSelection)selection, (IDisassemblyPart)part );
		}
		IDisassemblySelection disassemblySelection = (IDisassemblySelection)selection;
		int line = disassemblySelection.getStartLine();
		IBreakpoint[] bp = getBreakpointsAtLine( (IDisassemblyPart)part, line );
		if ( bp == null || bp.length == 0 ) {
			insertBreakpoint(part, disassemblySelection, false);
		}
		else {
			for( int i = 0; i < bp.length; i++ ) {
				bp[i].delete();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleLineBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleLineBreakpoints( IWorkbenchPart part, ISelection selection ) {
		return part instanceof IDisassemblyPart && selection instanceof ITextSelection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleMethodBreakpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleMethodBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleMethodBreakpoints( IWorkbenchPart part, ISelection selection ) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#toggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleWatchpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTarget#canToggleWatchpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleWatchpoints( IWorkbenchPart part, ISelection selection ) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#toggleBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public void toggleBreakpoints( IWorkbenchPart part, ISelection selection ) throws CoreException {
		toggleLineBreakpoints( part, selection );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension#canToggleBreakpoints(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	@Override
	public boolean canToggleBreakpoints( IWorkbenchPart part, ISelection selection ) {
		return canToggleLineBreakpoints( part, selection );
	}

	/**
     * @since 2.3
     */
	@Override
	public boolean canToggleBreakpointsWithEvent(IWorkbenchPart part, ISelection selection, Event event) {
	    return canToggleBreakpoints(part, selection);
	}
	
    /**
     * @since 2.3
     */
	@Override
	public void toggleBreakpointsWithEvent(IWorkbenchPart part, ISelection selection, Event event) throws CoreException {
	    assert part instanceof IDisassemblyPart && selection instanceof ITextSelection;

	    boolean mod1 = event != null && (event.stateMask & SWT.MOD1) > 0;
	    boolean mod2 = event != null && (event.stateMask & SWT.MOD2) > 0;
        if ( !(selection instanceof IDisassemblySelection) ) {
            selection = new DisassemblySelection( (ITextSelection)selection, (IDisassemblyPart)part );
        }
        IDisassemblySelection disassemblySelection = (IDisassemblySelection)selection;
        int line = disassemblySelection.getStartLine();
        IBreakpoint[] bp = getBreakpointsAtLine( (IDisassemblyPart)part, line );
        if ( bp == null || bp.length == 0 ) {
            insertBreakpoint(part, disassemblySelection, mod1);
        }
        else {
            if(mod2) {
                toggleBreakpointEnabled(bp[0]);
                return;
            } else if (mod1 && bp[0] instanceof ICBreakpoint) {
                CDebugUIUtils.editBreakpointProperties(part, (ICBreakpoint)bp[0]);
                return;
            }
                    
            for( int i = 0; i < bp.length; i++ ) {
                bp[i].delete();
            }
        }
	}

    /**
     * @since 2.3
     */
    @Override
    public boolean canCreateLineBreakpointsInteractive(IWorkbenchPart part, ISelection selection) {
        return canToggleLineBreakpoints(part, selection);
    }

    /**
     * @since 2.3
     */
    @Override
    public void createLineBreakpointsInteractive(IWorkbenchPart part, ISelection selection) throws CoreException {
        assert part instanceof IDisassemblyPart && selection instanceof ITextSelection;
        
        if ( !(selection instanceof IDisassemblySelection) ) {
            selection = new DisassemblySelection( (ITextSelection)selection, (IDisassemblyPart)part );
        }
        IDisassemblySelection disassemblySelection = (IDisassemblySelection)selection;
        insertBreakpoint(part, disassemblySelection, true);
    }
    
    /**
     * @since 2.3
     */
    @Override
    public boolean canCreateFunctionBreakpointInteractive(IWorkbenchPart part, ISelection selection) {
        return false;
    }

    /**
     * @since 2.3
     */
    @Override
    public void createFunctionBreakpointInteractive(IWorkbenchPart part, ISelection selection) throws CoreException {
    }
    
    /**
     * @since 2.3
     */
    @Override
    public boolean canCreateWatchpointsInteractive(IWorkbenchPart part, ISelection selection) {
        return false;
    }
    
    /**
     * @since 2.3
     */
    @Override
    public void createWatchpointsInteractive(IWorkbenchPart part, ISelection selection) throws CoreException {
    }
    

	private void toggleBreakpointEnabled(IBreakpoint bp) {
	    try {
	        bp.setEnabled(!bp.isEnabled());
	    } catch (CoreException e) {
	        CDebugUIPlugin.log(e.getStatus());
	    }
	}
	
	protected abstract void createLineBreakpoint( String sourceHandle, IResource resource, int lineNumber ) throws CoreException;

	/**
     * @since 2.3
     */
	protected void createLineBreakpointInteractive(IWorkbenchPart part, String sourceHandle, IResource resource, int lineNumber ) throws CoreException {
	    createLineBreakpoint(sourceHandle, resource, lineNumber);
	}

	protected abstract void createAddressBreakpoint( IResource resource, IAddress address ) throws CoreException;

	/**
     * @since 2.3
     */
	protected void createAddressBreakpointInteractive(IWorkbenchPart part, IResource resource, IAddress address ) throws CoreException {
	    createAddressBreakpoint(resource, address);
	}

	private IBreakpoint[] getBreakpointsAtLine( IDisassemblyPart part, int line ) {
		List<IBreakpoint> breakpoints = new ArrayList<IBreakpoint>();
		IAnnotationModel annotationModel = part.getTextViewer().getAnnotationModel();
		IDocument document = part.getTextViewer().getDocument();
		if ( annotationModel != null ) {
			Iterator<?> iterator = annotationModel.getAnnotationIterator();
			while( iterator.hasNext() ) {
				Object object = iterator.next();
				if ( object instanceof SimpleMarkerAnnotation ) {
					SimpleMarkerAnnotation markerAnnotation = (SimpleMarkerAnnotation)object;
					IMarker marker = markerAnnotation.getMarker();
					try {
						if ( marker.isSubtypeOf( IBreakpoint.BREAKPOINT_MARKER ) ) {
							Position position = annotationModel.getPosition( markerAnnotation );
							int bpLine = document.getLineOfOffset( position.getOffset() );
							if ( line == bpLine ) {
								IBreakpoint breakpoint = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint( marker );
								if ( breakpoint != null ) {
									breakpoints.add( breakpoint );
								}
							}
						}
					}
					catch( CoreException e ) {
					}
					catch( BadLocationException e ) {
					}
				}
			}
		}
		IBreakpoint[] breakpointsArray = new IBreakpoint[breakpoints.size()];
		return breakpoints.toArray( breakpointsArray );
	}

	private void insertBreakpoint(IWorkbenchPart part, IDisassemblySelection selection, boolean interactive) throws CoreException {
		IAddress address = selection.getStartAddress();
		if ( address == null ) {
			return;
		}
		URI fileUri = selection.getSourceLocationURI();
		if ( fileUri != null ) {
			String filePath = null;
			IResource resource = selection.getSourceFile();
			if ( resource != null ) {
				final IPath location = resource.getLocation();
				if ( location == null ) {
					return;
				}
				filePath = location.toOSString();
			}
			else {
				resource = ResourcesPlugin.getWorkspace().getRoot();
				filePath = URIUtil.toPath( fileUri ).toOSString();
			}
			int srcLine = selection.getSourceLine();
			if (interactive) {
			    createLineBreakpointInteractive(part, filePath, resource, srcLine + 1 );
			} else {
                createLineBreakpoint( filePath, resource, srcLine + 1 );
			}			    
		}
		else {
			IResource resource = ResourcesPlugin.getWorkspace().getRoot();
            if (interactive) {
                createAddressBreakpointInteractive(part, resource, address );
            } else {
                createAddressBreakpoint( resource, address );
            }
		}
    }
	
    /**
     * Opens the properties dialog for the given breakpoint. This method can be
     * used on an existing breakpoint or on a blank breakpoint which doesn't
     * have an associated marker yet.
     * 
     * @param bp
     *            The breakpoint to edit. This breakpoint may not have an
     *            associated marker yet.
     * @param part
     *            Workbench part where the action was invoked.
     * @param resource
     *            Workbench resource to create the breakpoint on.
     * @param attributes
     *            Breakpoint attributes to show in properties dialog. If the
     *            breakpoint already exists, this attribute map can be used to
     *            override the attributes currently in the breakpoint. Can be
     *            <code>null</code>.
     * @since 2.3
     */
    protected void openBreakpointPropertiesDialog(ICBreakpoint bp, IWorkbenchPart part, IResource resource,
        Map<String, Object> attributes) 
    {
        ISelection debugContext = DebugUITools.getDebugContextManager()
            .getContextService(part.getSite().getWorkbenchWindow()).getActiveContext(part.getSite().getId());
        CBreakpointContext bpContext = new CBreakpointContext(bp, debugContext, resource, attributes);

        PreferenceDialog dialog = PreferencesUtil.createPropertyDialogOn(part.getSite().getShell(), bpContext, null,
            null, null);
        if (dialog != null) {
            dialog.open();
        }
    }

}
