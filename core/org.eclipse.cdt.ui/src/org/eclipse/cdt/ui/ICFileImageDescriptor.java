package org.eclipse.cdt.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * C Image Descriptor Provider
 * @since 8.1
 */
public interface ICFileImageDescriptor {

	/**
	 * @return ImageDescriptor for a C source file
	 */
	public ImageDescriptor getCImageDescriptor();

	/**
	 * @return ImageDescriptor for a C++ source file
	 */
	public ImageDescriptor getCXXImageDescriptor();

	/**
	 * @return ImageDescriptor for a C/C++ header file
	 */
	public ImageDescriptor getHeaderImageDescriptor();

	/**
	 * Checks whether the descriptor can be used for the given project.
	 * @param project
	 * @return true if the descriptor can be used for the given project.
	 */
	public boolean isEnabled(IProject project);

}
