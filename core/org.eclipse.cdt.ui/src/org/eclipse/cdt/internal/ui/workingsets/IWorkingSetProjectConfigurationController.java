/*******************************************************************************
 * Copyright (c) 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.internal.ui.workingsets;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/**
 * Protocol for the view controller for the project configurations pane of the working set configurations
 * dialog. It takes care of coordinating the user gestures in that pane with the working-set configuration
 * model and vice-versa.
 *
 * @author Christian W. Damus (cdamus)
 *
 * @since 6.0
 *
 */
public interface IWorkingSetProjectConfigurationController {
	/**
	 * Queries the project configuration in the Working Set Configurations pane that I control.
	 *
	 * @return the new working set configuration selection. May be <code>null</code> if there is no selection
	 */
	IWorkingSetProjectConfiguration.ISnapshot getProjectConfiguration();

	/**
	 * Notifies me that the check state of some element that I control in the sub-tree of my
	 * {@linkplain #getProjectConfiguration() project configuration} has changed its check-state. The
	 * controller context can be used to pass back reactions such as to veto the check-state change or to
	 * signal that some level of UI refresh is required.
	 *
	 * @param element
	 *            an element that has been checked or unchecked
	 * @param checked
	 *            whether the <tt>element</tt> is now checked
	 * @param context
	 *            the controller context, used to communicate vetos, refreshes, etc.
	 */
	void checkStateChanged(Object element, boolean checked, IControllerContext context);

	/**
	 * Computes the initial check-box settings for my tree according to the current state of my
	 * {@linkplain #getProjectConfiguration() project configuration}.
	 *
	 * @param context
	 *            context in which I can set initial check-states of my elements
	 */
	void updateCheckState(IControllerContext context);

	/**
	 * Obtains a content provider for the structure rooted at my {@linkplain #getProjectConfiguration()
	 * project configuration}. Note that this method will only be called once, and that the caller takes
	 * responsibility for {@linkplain IContentProvider#dispose() disposing} the content provider.
	 *
	 * @return my content provider
	 */
	ITreeContentProvider getContentProvider();

	/**
	 * <p>
	 * Obtains a label provider for the structure rooted at my {@linkplain #getProjectConfiguration() project
	 * configuration}. Note that this method will only be called once, and that the caller takes
	 * responsibility for {@linkplain IBaseLabelProvider#dispose() disposing} the label provider.
	 * </p>
	 * <p>
	 * The viewer argument is useful to obtain information about default font and colors, for label providers
	 * that implement the optional {@link IFontProvider} and/or {@link IColorProvider} interfaces.
	 * </p>
	 *
	 * @param viewer
	 *            the viewer for which I will provide labels
	 *
	 * @return my label provider
	 */
	ILabelProvider getLabelProvider(Viewer viewer);

	//
	// Nested types
	//

	/**
	 * An interface provided by the Manage Working Set Configurations dialog infrastructure to
	 * {@link IWorkingSetProjectConfigurationController}s for communication of state changes back to the UI.
	 *
	 * @noimplement This interface is not intended to be implemented by clients.
	 * @noextend This interface is not intended to be extended by clients.
	 *
	 * @author Christian W. Damus (damus)
	 *
	 * @since 6.0
	 */
	interface IControllerContext {
		/**
		 * Queries whether the current working set configuration context is a read-only one. In such cases, I
		 * should probably disallow check-state changes and other editing.
		 *
		 * @return whether the current working set configuration is read-only
		 *
		 * @see IWorkingSetConfiguration.ISnapshot#isReadOnly()
		 */
		boolean isReadOnly();

		/**
		 * Sets the check state of an element in the Project Configurations pane under the authority of the
		 * controller. This is particularly useful for setting the
		 * {@linkplain IWorkingSetProjectConfigurationController#updateCheckState(IControllerContext) initial
		 * check state} of a controller and for
		 * {@linkplain IWorkingSetProjectConfigurationController#checkStateChanged(Object, boolean, IControllerContext)
		 * vetoing check state changes}.
		 *
		 * @param element
		 *            the element to update checked
		 * @param checked
		 *            whether the element should be checked
		 *
		 * @see IWorkingSetProjectConfigurationController#checkStateChanged(Object, boolean,
		 *      IControllerContext)
		 * @see IWorkingSetProjectConfigurationController#updateCheckState(IControllerContext)
		 * @see #setGrayed(Object, boolean)
		 */
		void setChecked(Object element, boolean checked);

		/**
		 * Sets the gray state of an element in the Project Configurations pane under the authority of the
		 * controller. This is particularly useful for setting the
		 * {@linkplain IWorkingSetProjectConfigurationController#updateCheckState(IControllerContext) initial
		 * check state} of a controller and for
		 * {@linkplain IWorkingSetProjectConfigurationController#checkStateChanged(Object, boolean, IControllerContext)
		 * responding to check state changes}.
		 *
		 * @param element
		 *            the element to update checked
		 * @param checked
		 *            whether the element should be checked
		 *
		 * @see IWorkingSetProjectConfigurationController#checkStateChanged(Object, boolean,
		 *      IControllerContext)
		 * @see IWorkingSetProjectConfigurationController#updateCheckState(IControllerContext)
		 * @see #setChecked(Object, boolean)
		 */
		void setGrayed(Object element, boolean grayed);

		/**
		 * Requests an update of the visual appearance of the specified element. The element may be any
		 * element under my control, or even the {@link IWorkingSetConfiguration} or {@link IWorkingSetProxy}
		 * that owns my project configuration.
		 *
		 * @param element
		 *            an element to update
		 */
		void update(Object element);

		/**
		 * Notifies that the specified project configuration's activation state has changed. That is, that it
		 * is now activated when previously it was not, or vice-versa.
		 *
		 * @param project
		 *            configuration the project configuration that changed
		 */
		void activationStateChanged(IWorkingSetProjectConfiguration projectConfiguration);
	}
}