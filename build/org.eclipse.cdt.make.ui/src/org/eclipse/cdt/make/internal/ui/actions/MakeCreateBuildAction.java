/*
 * Created on 18-Aug-2003
 *
 * Copyright (c) 2002,2003 QNX Software Systems Ltd.
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.internal.ui.actions;

import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.ui.actions.CreateBuildAction;

public class MakeCreateBuildAction extends CreateBuildAction {

	protected String getBuilderID() {
		return MakeBuilder.BUILDER_ID;
	}

}
