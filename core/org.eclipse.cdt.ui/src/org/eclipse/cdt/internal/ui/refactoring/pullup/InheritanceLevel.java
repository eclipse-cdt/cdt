/*******************************************************************************
 * Copyright (c) 2013 Simon Taddiken
 * University of Bremen.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html  
 * 
 * Contributors: 
 *     Simon Taddiken (University of Bremen)
 ******************************************************************************/
package org.eclipse.cdt.internal.ui.refactoring.pullup;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPBase;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPClassType;
import org.eclipse.cdt.core.parser.Keywords;

/**
 * Class to represent an inheritance tree of a selected class. It has a unique predecessor
 * class and a collection of child classes. As by multiple inheritance in C++, there
 * may exist more than one instance of this class with the same predecessor. The 
 * inheritance tree is built by the
 * {@link PullUpPushDownBase#iterateInheritanceTree(InheritanceLevel, ICPPClassType, List, int) pull up/push down refactoring}
 * 
 * This class is mainly used to display the inheritance dependencies within the gui.
 * 
 * @author Simon Taddiken
 */
public class InheritanceLevel implements Comparable<InheritanceLevel> {
	
	/** Indentation per level in string representation of this object */
	private final static int INDENT = 4;

	/**
	 * InheritanceLevel of the base class of this level. <code>null</code> if this level
	 * does not have a base class
	 */
	private final InheritanceLevel predecessor;
	
	/** InheritanceLevels of direct subclasses of this level */
	private final List<InheritanceLevel> children;
	
	/** Type binding of the class this instance represents */
	private final ICPPClassType clazz;
	
	private final ICPPBase baseSpec;
	
	/** Distance from the root class */
	private final int level;
	
	/** Whether this is an entry for an abstract class */
	private final boolean isAbstract;
	
	
	
	/**
	 * Creates a new InheritanceLevel instance.
	 * 
	 * @param predecessor Super class InheritanceLevel of this on. May be 
	 * 			<code>null</code> if this is the root.
	 * @param clazz Type binding of the class representing this level.
	 * @param baseSpec The base specifier within our predecessor which declares this class
	 * 			as child.
	 * @param level The distance of this class from the root
	 */
	public InheritanceLevel(InheritanceLevel predecessor, 
			ICPPClassType clazz, ICPPBase baseSpec, 
			int level) {
		
		this.predecessor = predecessor;
		this.children = new ArrayList<InheritanceLevel>();
		this.clazz = clazz;
		this.baseSpec = baseSpec;
		this.level = level;
		this.isAbstract = baseSpec != null && 
				baseSpec.getBaseClass() instanceof ICPPClassType && 
				PullUpHelper.isPureAbstract((ICPPClassType) baseSpec.getBaseClass());
	}
	
	
	
	public int getLevel() {
		return this.level;
	}
	
	public void addChild(InheritanceLevel child) {
		assert child.predecessor == this;
		this.children.add(child);
	}
	
	
	
	public List<InheritanceLevel> getChildren() {
		return this.children;
	}
	
	
	
	public InheritanceLevel getPredecessor() {
		return this.predecessor;
	}
	
	
	
	public ICPPBase getBaseSpecifier() {
		return this.baseSpec;
	}
	
	
	
	
	/**
	 * Computes the maximum visibility that an inherited member can have in this class.
	 * C++ allows for visibility reduction when extending classes.
	 *  
	 * @return The maximum visibility an inherited member can have within this level.
	 */
	public int computeMaxVisibility() {
		// private   == 3
		// protected == 2
		// public    == 1
		if (this.predecessor == null) {
			// members from first inheritance level are always visible because they are
			// inherited directly
			return ICPPASTVisibilityLabel.v_public;
		}
		return Math.max(this.baseSpec.getVisibility(), 
				this.predecessor.computeMaxVisibility());
	}
	
	
	
	public ICPPClassType getClazz() {
		return this.clazz;
	}
	
	
	
	/**
	 * Whether our predecessor inherits this class 'virtual'
	 * @return Whether our predecessor inherits this class 'virtual'
	 */
	public boolean isVirtual() {
		return this.baseSpec.isVirtual();
	}
	
	
	
	@Override
	public int compareTo(InheritanceLevel o) {
		return this.level - o.level;
	}
	
	
	
	@Override
	public boolean equals(Object obj) {
		return obj == this || obj != null &&
				obj instanceof InheritanceLevel &&
				this.clazz.equals(((InheritanceLevel) obj).clazz);
	}
	
	
	
	/**
	 * Creates a String representation of this level suitable for displaying within the
	 * gui. The resulting String will be indented according to {@link #getLevel()}.
	 * @return String representation of this object.
	 */
	@Override
	public String toString() {
		final StringBuilder b = new StringBuilder();
		for (int i = 0; i < (this.level - 1) * INDENT; ++i) {
			b.append(" "); //$NON-NLS-1$
		}
		b.append(this.clazz.getName().toString());
		

		final List<String> details = new ArrayList<String>();
		final String v;
		if (this.baseSpec != null) {
			switch (this.baseSpec.getVisibility()) {
			case ICPPASTVisibilityLabel.v_private: v = Keywords.PRIVATE; break;
			case ICPPASTVisibilityLabel.v_protected: v = Keywords.PROTECTED; break;
			case ICPPASTVisibilityLabel.v_public: v = Keywords.PUBLIC; break;
			default: 
				throw new IllegalArgumentException(
					"illegal visibility value: " + this.baseSpec.getVisibility()); //$NON-NLS-1$
			}
			
			details.add(v);
		} else {
			v = ""; //$NON-NLS-1$
		}
		
		if (this.isAbstract) {
			details.add("abstract");
		}
		
		if (this.baseSpec != null && this.baseSpec.isVirtual()) {
			details.add(Keywords.VIRTUAL);
		}
		
		if (!details.isEmpty()) {
			b.append(" (");
			final Iterator<String> it = details.iterator();
			while (it.hasNext()) {
				b.append(it.next());
				if (it.hasNext()) {
					b.append(", ");
				}
			}
			b.append(")");
		}
		return b.toString();
	}
}