/*******************************************************************************
 * Copyright (c) 2008, 2012 QNX Software Systems and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * QNX Software Systems - catchpoints - bug 226689
 *******************************************************************************/
package org.eclipse.cdt.debug.ui.breakpoints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.internal.resources.Workspace;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;

public class CBreakpointUIContributionFactory {
	private static final String EXTENSION_POINT_NAME = "breakpointContribution"; //$NON-NLS-1$

	private static CBreakpointUIContributionFactory instance;
	protected ArrayList<ICBreakpointsUIContribution> contributions;

	private CBreakpointUIContributionFactory() {
		contributions = new ArrayList<ICBreakpointsUIContribution>();
		loadSubtypeContributions();
	}

	/**
	 * Calculates the breakpoint contributions for the given breakpoint.
	 * 
	 * @param breakpoint Breakpoint to find UI contributions for.
	 * @return non-null array of ICBreakpointsUIContribution 
	 * @throws CoreException if cannot get marker attributes from bearkpoint
	 */
	public ICBreakpointsUIContribution[] getBreakpointUIContributions(IBreakpoint breakpoint) throws CoreException {
		String debugModelId = breakpoint.getModelIdentifier();
		IMarker bmarker = breakpoint.getMarker();
		Map<String, Object> attributes = Collections.emptyMap();
		String markerType = CDIDebugModel.calculateMarkerType(breakpoint);
		if (bmarker != null) {
		    Map<String, Object> _attributes = bmarker.getAttributes();
		    attributes = _attributes;
	        markerType = bmarker.getType();
		}
		return getBreakpointUIContributions(debugModelId, markerType, attributes);
	}

    /**
     * Calculates the breakpoint contributions for the given breakpoint.
     * 
     * @param breakpoint Breakpoint to find UI contributions for.
     * @param attributes Attributes of the breakpoint
     * @return non-null array of ICBreakpointsUIContribution 
     * @throws CoreException if cannot get marker attributes from bearkpoint
     * @since 7.2
     */
	public ICBreakpointsUIContribution[] getBreakpointUIContributions(String[] debugModelIDs, IBreakpoint breakpoint, 
	    Map<String, Object> attributes) throws CoreException 
	{
        IMarker bmarker = breakpoint.getMarker();
        String markerType = CDIDebugModel.calculateMarkerType(breakpoint);
        if (bmarker != null) {
            markerType = bmarker.getType();
        }
        return getBreakpointUIContributions(debugModelIDs, markerType, attributes);
    }
	
	/**
	 * Default debug model ID list which will cause only the general UI contributions to be returned.
	 * @since 7.2
	 */
	public final static String[] DEBUG_MODEL_IDS_DEFAULT = new String[] {};
	
    /**
     * Calculates the breakpoint UI contributions for the given breakpoint.
     * 
     * @param debugModelId The debug model ID of the active debug context for 
     * which to calculate contributions.
     * @param breakpoint Breakpoint to find UI contributions for.
     * @param markerType Marker type of the breakpoint.
     * @param attributes Attributes of the breakpoint
     * @return non-null array of ICBreakpointsUIContribution 
     * @throws CoreException 
     * @throws CoreException if cannot get marker attributes from berakpoint
     */
	public ICBreakpointsUIContribution[] getBreakpointUIContributions(String debugModelId, String markerType,
			Map<String, Object> attributes) 
	{
	    return getBreakpointUIContributions(
	        debugModelId != null ? new String[] { debugModelId } : DEBUG_MODEL_IDS_DEFAULT, 
	        markerType, 
	        attributes);
	    
	}

    /**
     * Calculates the breakpoint UI contributions for the given breakpoint.
     * 
     * @param debugModelId The debug model IDs of the active debug context for 
     * which to calculate contributions.
     * @param breakpoint Breakpoint to find UI contributions for.
     * @param markerType Marker type of the breakpoint.
     * @param attributes Attributes of the breakpoint
     * @return non-null array of ICBreakpointsUIContribution 
     * @throws CoreException 
     * @throws CoreException if cannot get marker attributes from berakpoint
     * 
     * @since 7.2
     */
    public ICBreakpointsUIContribution[] getBreakpointUIContributions(String[] debugModelIds, String markerType,
            Map<String, Object> attributes) 
    {
        List<String> debugModelIdsList = Arrays.asList(debugModelIds);
        ArrayList<ICBreakpointsUIContribution> list = new ArrayList<ICBreakpointsUIContribution>();
        for (ICBreakpointsUIContribution con : contributions) {
            try {
                if (con.getDebugModelId() == null || 
                    con.getDebugModelId().equals(CDIDebugModel.getPluginIdentifier()) ||
                    debugModelIdsList.contains(con.getDebugModelId())) 
                {
                    String contributedMarkerType = con.getMarkerType();
                    if (isMarkerSubtypeOf(markerType, contributedMarkerType)) {
                        if (attributes == null || con.isApplicable(attributes)) {
                            list.add(con);
                        }
                    }
                }
            } catch (Exception e) {
                CDebugUIPlugin.log(e);
            }

        }
        return list.toArray(new ICBreakpointsUIContribution[list.size()]);
    }

	
	public boolean isMarkerSubtypeOf(String currentType, String type) throws CoreException {
		return getWorkspace().getMarkerManager().isSubtype(currentType, type);
	}

	private Workspace getWorkspace() {
		return (Workspace) CDebugUIPlugin.getWorkspace();
	}

	private void loadSubtypeContributions() {

		IExtensionPoint ep = Platform.getExtensionRegistry().getExtensionPoint(CDebugUIPlugin.getUniqueIdentifier(),
				EXTENSION_POINT_NAME);
		if (ep == null)
			return;
		IConfigurationElement[] elements = ep.getConfigurationElements();
		for (int i = 0; i < elements.length; i++) {
			IConfigurationElement configurationElement = elements[i];
			if (configurationElement.getName().equals(ICBreakpointsUIContribution.BREAKPOINT_LABELS) || 
			    configurationElement.getName().equals(ICBreakpointsUIContribution.BREAKPOINT_EDITORS)) 
			{
                String mainElement = configurationElement.getName(); 
				String modelId = configurationElement.getAttribute("debugModelId"); //$NON-NLS-1$
				String markerType = getRequired(configurationElement, "markerType"); //$NON-NLS-1$
				if (markerType == null)
					continue;
				IConfigurationElement[] children = configurationElement.getChildren("attribute"); //$NON-NLS-1$
				for (IConfigurationElement att : children) {
				    
					DefaultCBreakpointUIContribution adapter = new DefaultCBreakpointUIContribution(att);
					adapter.setMainElement(mainElement);
					adapter.setMarkerType(markerType);
					adapter.setDebugModelId(modelId);
					if (processAttribute(att, adapter) == false)
						continue;

				}

			}
		}
	}

	private boolean processAttribute(IConfigurationElement attrElement, 
	    DefaultCBreakpointUIContribution adapter) 
	{
		String attrId = getRequired(attrElement, "name"); //$NON-NLS-1$
		String attrLabel = getRequired(attrElement, "label"); //$NON-NLS-1$
		String fieldEditorClass = attrElement.getAttribute("fieldEditor"); //$NON-NLS-1$
        String fieldEditorFactoryClass = attrElement.getAttribute("fieldEditorFactory"); //$NON-NLS-1$
		String type = attrElement.getAttribute("type"); //$NON-NLS-1$
		String svisible = attrElement.getAttribute("visible"); //$NON-NLS-1$

		if (attrId == null) {
			return false;
		}
		if (attrLabel == null) {
			return false;
		}
		if (type == null) {
			type = "string"; //$NON-NLS-1$
		}
		boolean visible = true;
		if (svisible != null && svisible.equalsIgnoreCase("false")) { //$NON-NLS-1$
			visible = false;
		}
		adapter.setId(attrId);
		adapter.setLabel(attrLabel);
		adapter.setControlClass(fieldEditorClass);
        adapter.setFieldEditorFactory(fieldEditorFactoryClass);
		adapter.setType(type);
		adapter.setVisible(visible);
		addContribution(adapter);

		IConfigurationElement[] children = attrElement.getChildren("value"); //$NON-NLS-1$
		for (IConfigurationElement value : children) {
			processValue(value, adapter);
		}
		return true;
	}

	private void processValue(IConfigurationElement valueElement, DefaultCBreakpointUIContribution adapter) {
		String valueId = getRequired(valueElement, "value"); //$NON-NLS-1$
		String valueLabel = getRequired(valueElement, "label"); //$NON-NLS-1$
		if (valueId == null)
			return;
		if (valueLabel == null)
			return;
		adapter.addValue(valueId, valueLabel);
		IConfigurationElement[] children = valueElement.getChildren("attribute"); //$NON-NLS-1$
		for (IConfigurationElement att : children) {
			DefaultCBreakpointUIContribution adapter2 = new DefaultCBreakpointUIContribution(att);
			// inherit values
			adapter2.setMainElement(adapter.getMainElement());
			adapter2.setMarkerType(adapter.getMarkerType());
			adapter2.setDebugModelId(adapter.getDebugModelId());
			adapter2.addContionsAll(adapter.getConditions());
			// add value condition
			adapter2.addContionEquals(adapter.getId(), valueId);
			if (processAttribute(att, adapter2) == false)
				continue;
		}
	}

	public void addContribution(ICBreakpointsUIContribution contribution) {
		contributions.add(contribution);

	}

	public static CBreakpointUIContributionFactory getInstance() {
		if (instance == null) {
			instance = new CBreakpointUIContributionFactory();
		}
		return instance;
	}

	private static String getRequired(IConfigurationElement configurationElement, String name) {
		String elementValue = configurationElement.getAttribute(name);
		if (elementValue == null)
			CDebugUIPlugin.log(new Status(IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(),
					DebugPlugin.INTERNAL_ERROR, "Extension " //$NON-NLS-1$
							+ configurationElement.getDeclaringExtension().getUniqueIdentifier()
							+ " missing required attribute: " + name, null)); //$NON-NLS-1$
		return elementValue;
	}

}
