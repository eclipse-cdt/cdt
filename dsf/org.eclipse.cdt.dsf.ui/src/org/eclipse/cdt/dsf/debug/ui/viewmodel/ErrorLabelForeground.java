/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel;

import org.eclipse.cdt.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.cdt.dsf.ui.viewmodel.properties.LabelForeground;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.JFacePreferences;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.themes.ITheme;
import org.eclipse.ui.themes.IThemeManager;

/**
 * Label attribute that sets the label color to the standard workbench 
 * error color.  The color is activated when the property update contains
 * a status with error codes: {@link IDsfStatusConstants#INTERNAL_ERROR}, 
 * {@link IDsfStatusConstants#REQUEST_FAILED}, or 
 * {@link IDsfStatusConstants#NOT_SUPPORTED}.
 * 
 * @since 2.2
 */
public class ErrorLabelForeground extends LabelForeground {

    private static final RGB DEFAULT_COLOR = new RGB(255, 0, 0); 
    
    public ErrorLabelForeground() {
        super(DEFAULT_COLOR);
    }
    
    @Override
    public boolean isEnabled(IStatus status, java.util.Map<String,Object> properties) {
        return !status.isOK() && status.getCode() >= IDsfStatusConstants.NOT_SUPPORTED;
    }
    
    @Override
    public RGB getForeground() {
        IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
        ITheme currentTheme = themeManager.getCurrentTheme();
         
        ColorRegistry colorRegistry = currentTheme.getColorRegistry();
        
        Color color = colorRegistry.get(JFacePreferences.ERROR_COLOR);
        
        if (color != null) {
            return color.getRGB();
        }
        return super.getForeground();
    }
}
