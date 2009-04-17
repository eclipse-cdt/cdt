/*******************************************************************************
 * Copyright (c) 2009 Andrew Gvozdev
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Andrew Gvozdev  - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.codan.checkers.sample;

import org.eclipse.core.resources.IMarker;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class MarkerResolutionGenerator implements IMarkerResolutionGenerator {

       @Override
       public IMarkerResolution[] getResolutions(IMarker marker) {
               return new IMarkerResolution[] { new QuickFixAssignmentInCondition() };
       }

}