package org.eclipse.cdt.internal.core.build.managed;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ResourceBuildInfo {

	private IResource owner;
	private Map targetMap;
	private List targets;

	public ResourceBuildInfo() {
		targetMap = new HashMap();
		targets = new ArrayList();
	}
	
	public ResourceBuildInfo(IResource owner, Element element) {
		this();
		
		Node child = element.getFirstChild();
		while (child != null) {
			if (child.getNodeName().equals("target")) {
				new Target(this, (Element)child);
			}
			child = child.getNextSibling();
		}
	}

	public IResource getOwner() {
		return owner;
	}
	
	public Target getTarget(String id) {
		return (Target)targetMap.get(id);
	}

	public List getTargets() {
		return targets;	
	}
	
	public void addTarget(Target target) {
		targetMap.put(target.getId(), target);
		targets.add(target);
	}
	
	public void serialize(Document doc, Element element) {
		for (int i = 0; i < targets.size(); ++i) {
			Element targetElement = doc.createElement("target");
			element.appendChild(targetElement);
			((Target)targets.get(i)).serialize(doc, targetElement);
		}
	}
}
