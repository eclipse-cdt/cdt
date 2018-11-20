/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.gdb.viewmodel;

import java.util.Map;

import org.eclipse.cdt.debug.ui.IPinProvider.IPinElementColorDescriptor;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ExecutionContextLabelText;
import org.eclipse.cdt.dsf.debug.ui.viewmodel.launch.ILaunchVMConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch.GdbExecutionContextLabelText;
import org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch.IGdbLaunchVMConstants;
import org.eclipse.cdt.dsf.gdb.internal.ui.viewmodel.launch.ThreadVMNode;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.cdt.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelAttribute;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelColumnInfo;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelImage;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelText;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.PropertiesBasedLabelProvider;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;

@SuppressWarnings("restriction")
public class GdbExtendedThreadVMNode extends ThreadVMNode {
	public GdbExtendedThreadVMNode(AbstractDMVMProvider provider, DsfSession session) {
		super(provider, session);
	}

	@Override
	protected IElementLabelProvider createLabelProvider() {
		PropertiesBasedLabelProvider provider = new PropertiesBasedLabelProvider();

		provider.setColumnInfo(PropertiesBasedLabelProvider.ID_COLUMN_NO_COLUMNS,
				new LabelColumnInfo(new LabelAttribute[] {
						// Text is made of the thread name followed by its state and state change reason.
						new GdbExecutionContextLabelText(GdbExtendedVMMessages.ThreadVMNode_No_columns__text_format,
								new String[] { ExecutionContextLabelText.PROP_NAME_KNOWN, PROP_NAME,
										ExecutionContextLabelText.PROP_ID_KNOWN, ILaunchVMConstants.PROP_ID,
										IGdbLaunchVMConstants.PROP_OS_ID_KNOWN, IGdbLaunchVMConstants.PROP_OS_ID,
										IGdbLaunchVMConstants.PROP_CORES_ID_KNOWN, IGdbLaunchVMConstants.PROP_CORES_ID,
										ILaunchVMConstants.PROP_IS_SUSPENDED,
										ExecutionContextLabelText.PROP_STATE_CHANGE_REASON_KNOWN,
										ILaunchVMConstants.PROP_STATE_CHANGE_REASON,
										ExecutionContextLabelText.PROP_STATE_CHANGE_DETAILS_KNOWN,
										ILaunchVMConstants.PROP_STATE_CHANGE_DETAILS }),
						new LabelText(GdbExtendedVMMessages.ThreadVMNode_No_columns__Error__label, new String[0]),
						/* RUNNING THREAD - RED PIN */
						new LabelImage(
								CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_RUNNING_R_PINNED)) {
							{
								setPropertyNames(new String[] { ILaunchVMConstants.PROP_IS_SUSPENDED,
										IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
										IGdbLaunchVMConstants.PROP_PIN_COLOR });
							}

							@Override
							public boolean isEnabled(IStatus status, Map<String, Object> properties) {
								Boolean prop = (Boolean) properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED);
								Boolean pin_prop = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT);
								Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR);
								return (prop != null && pin_prop != null && pin_color_prop != null)
										? !prop.booleanValue() && pin_prop.booleanValue()
												&& pin_color_prop.equals(IPinElementColorDescriptor.RED)
										: false;
							}
						},
						/* RUNNING THREAD - GREEN PIN */
						new LabelImage(
								CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_RUNNING_G_PINNED)) {
							{
								setPropertyNames(new String[] { ILaunchVMConstants.PROP_IS_SUSPENDED,
										IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
										IGdbLaunchVMConstants.PROP_PIN_COLOR });
							}

							@Override
							public boolean isEnabled(IStatus status, Map<String, Object> properties) {
								Boolean prop = (Boolean) properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED);
								Boolean pin_prop = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT);
								Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR);
								return (prop != null && pin_prop != null && pin_color_prop != null)
										? !prop.booleanValue() && pin_prop.booleanValue()
												&& pin_color_prop.equals(IPinElementColorDescriptor.GREEN)
										: false;
							}
						},
						/* RUNNING THREAD - BLUE PIN */
						new LabelImage(
								CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_RUNNING_B_PINNED)) {
							{
								setPropertyNames(new String[] { ILaunchVMConstants.PROP_IS_SUSPENDED,
										IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
										IGdbLaunchVMConstants.PROP_PIN_COLOR });
							}

							@Override
							public boolean isEnabled(IStatus status, Map<String, Object> properties) {
								Boolean prop = (Boolean) properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED);
								Boolean pin_prop = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT);
								Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR);
								return (prop != null && pin_prop != null && pin_color_prop != null)
										? !prop.booleanValue() && pin_prop.booleanValue()
												&& pin_color_prop.equals(IPinElementColorDescriptor.BLUE)
										: false;
							}
						},
						/* RUNNING THREAD - NO PIN */
						new LabelImage(DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_RUNNING)) {
							{
								setPropertyNames(new String[] { ILaunchVMConstants.PROP_IS_SUSPENDED });
							}

							@Override
							public boolean isEnabled(IStatus status, java.util.Map<String, Object> properties) {
								// prop has been seen to be null during session shutdown [313823]
								Boolean prop = (Boolean) properties.get(ILaunchVMConstants.PROP_IS_SUSPENDED);
								return (prop != null) ? !prop.booleanValue() : false;
							}
						},
						/* SUSPENDED THREAD - RED PIN */
						new LabelImage(
								CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_SUSPENDED_R_PINNED)) {
							{
								setPropertyNames(new String[] { IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
										IGdbLaunchVMConstants.PROP_PIN_COLOR });
							}

							@Override
							public boolean isEnabled(IStatus status, Map<String, Object> properties) {
								Boolean pin_prop = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT);
								Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR);
								return (pin_prop != null && pin_color_prop != null) ? pin_prop.booleanValue()
										&& pin_color_prop.equals(IPinElementColorDescriptor.RED) : false;
							}
						},
						/* SUSPENDED THREAD - GREEN PIN */
						new LabelImage(
								CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_SUSPENDED_G_PINNED)) {
							{
								setPropertyNames(new String[] { IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
										IGdbLaunchVMConstants.PROP_PIN_COLOR });
							}

							@Override
							public boolean isEnabled(IStatus status, Map<String, Object> properties) {
								Boolean pin_prop = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT);
								Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR);
								return (pin_prop != null && pin_color_prop != null) ? pin_prop.booleanValue()
										&& pin_color_prop.equals(IPinElementColorDescriptor.GREEN) : false;
							}
						},
						/* SUSPENDED THREAD - BLUE PIN */
						new LabelImage(
								CDTSharedImages.getImageDescriptor(CDTSharedImages.IMG_THREAD_SUSPENDED_B_PINNED)) {
							{
								setPropertyNames(new String[] { IGdbLaunchVMConstants.PROP_PINNED_CONTEXT,
										IGdbLaunchVMConstants.PROP_PIN_COLOR });
							}

							@Override
							public boolean isEnabled(IStatus status, Map<String, Object> properties) {
								Boolean pin_prop = (Boolean) properties.get(IGdbLaunchVMConstants.PROP_PINNED_CONTEXT);
								Object pin_color_prop = properties.get(IGdbLaunchVMConstants.PROP_PIN_COLOR);
								return (pin_prop != null && pin_color_prop != null) ? pin_prop.booleanValue()
										&& pin_color_prop.equals(IPinElementColorDescriptor.BLUE) : false;
							}
						},
						/* SUSPENDED THREAD - NO PIN */
						new LabelImage(
								DebugUITools.getImageDescriptor(IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED)), }));
		return provider;
	}

}
