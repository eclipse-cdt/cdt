package org.eclipse.cdt.launch.internal.ui;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ICProjectDescriptor;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.launch.ICDTLaunchConfigurationConstants;
import org.eclipse.core.boot.BootLoader;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

/*
 * (c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 */
public abstract class CLaunchConfigurationTab extends AbstractLaunchConfigurationTab {

	/**
	 * Returns the current C element context from which to initialize
	 * default settings, or <code>null</code> if none.
	 * 
	 * @return C element context.
	 */
	protected ICElement getContext(ILaunchConfigurationWorkingCopy config) throws CoreException {
		IWorkbenchPage page = LaunchUIPlugin.getActivePage();
		if (page != null) {
			ISelection selection = page.getSelection();
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection ss = (IStructuredSelection) selection;
				if (!ss.isEmpty()) {
					Object obj = ss.getFirstElement();
					if (obj instanceof ICElement) {
						ICProjectDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(((ICElement)obj).getCProject().getProject());
						if ( descriptor.getPlatform().equals(getPlatform(config)) )
							return (ICElement) obj;
					}
					if (obj instanceof IResource) {
						ICElement ce = CoreModel.getDefault().create((IResource) obj);
						if (ce == null) {
							IProject pro = ((IResource) obj).getProject();
							ce = CoreModel.getDefault().create(pro);
						}
						if (ce != null) {
							ICProjectDescriptor descriptor = CCorePlugin.getDefault().getCProjectDescription(ce.getCProject().getProject());
							if ( descriptor.getPlatform().equals(getPlatform(config)) )
								return ce;
						}
					}
				}
			}
			IEditorPart part = page.getActiveEditor();
			if (part != null) {
				IEditorInput input = part.getEditorInput();
				return (ICElement) input.getAdapter(ICElement.class);
			}
		}
		return null;
	}

	/**
	 * Set the C project attribute based on the ICElement.
	 */
	protected void initializeCProject(ICElement cElement, ILaunchConfigurationWorkingCopy config) {
		ICProject cProject = cElement.getCProject();
		String name = null;
		if (cProject != null && cProject.exists()) {
			name = cProject.getElementName();
		}
		config.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, name);
		
	}
	
	protected String getPlatform(ILaunchConfiguration config) throws CoreException {
		String platform = BootLoader.getOS();
		return config.getAttribute(ICDTLaunchConfigurationConstants.ATTR_CDT_PLATFORM, platform);
	}	
}
