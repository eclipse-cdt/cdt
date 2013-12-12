/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.cdt.internal.qt.ui.assist;

import org.eclipse.cdt.qt.ui.QtUIPlugin;
import org.eclipse.cdt.ui.text.ICCompletionProposal;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateProposal;

public class QtTemplateProposal extends TemplateProposal implements ICCompletionProposal {

	// The Qt proposals are made more relevant than the default built- proposals.
	private static int BASE_RELEVANCE = 1100;

	public QtTemplateProposal(Template template, TemplateContext context, IRegion region) {
		this(template, context, region, 0);
	}

	public QtTemplateProposal(Template template, TemplateContext context, IRegion region, int relevance) {
		super(template, context, region, QtUIPlugin.getQtLogo(), BASE_RELEVANCE + relevance);
	}

	@Override
	public String getIdString() {
		return getDisplayString();
	}
}
