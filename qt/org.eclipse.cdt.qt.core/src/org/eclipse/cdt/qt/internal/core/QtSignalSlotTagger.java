/*
 * Copyright (c) 2013 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.eclipse.cdt.qt.internal.core;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.IASTProblemHolder;
import org.eclipse.cdt.core.dom.ast.IASTSimpleDeclaration;
import org.eclipse.cdt.core.dom.ast.IBinding;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPMethod;
import org.eclipse.cdt.core.dom.ast.tag.IBindingTagger;
import org.eclipse.cdt.core.dom.ast.tag.ITag;
import org.eclipse.cdt.core.dom.ast.tag.ITagWriter;
import org.eclipse.cdt.core.dom.ast.tag.IWritableTag;
import org.eclipse.cdt.qt.core.QtKeywords;
import org.eclipse.cdt.qt.core.QtPlugin;

/**
 * Finds all functions that are marked as Qt signals or slots and tags them in
 * the index. There are two ways that Qt understands for marking a function as a
 * signal or slot: 1) With a macro in the function's visibility label 2) With a
 * macro before the function itself E.g., both of these cases are valid:
 * 
 * <pre>
 * class T
 * {
 * private:
 *     Q_SLOT void some_slot();
 * 
 * signals:
 *     void some_signal();
 * };
 * </pre>
 * 
 * The 6 applicable macros are signals, Q_SIGNALS, Q_SIGNAL, slots, Q_SLOTS, and
 * Q_SLOT.
 */
public class QtSignalSlotTagger implements IBindingTagger {
	private static ICPPASTVisibilityLabel findVisibilityLabel(
			ICPPMethod method, IASTNode ast) {
		// the visibility cannot be found without an ast
		if (ast == null)
			return null;

		IASTNode methodDecl = ast;
		ICPPASTCompositeTypeSpecifier classType = null;
		while (methodDecl != null && classType == null) {
			IASTNode parent = methodDecl.getParent();
			if (parent instanceof ICPPASTCompositeTypeSpecifier)
				classType = (ICPPASTCompositeTypeSpecifier) parent;
			else
				methodDecl = parent;
		}

		if (methodDecl == null || classType == null)
			return null;

		ICPPASTVisibilityLabel lastLabel = null;
		for (IASTDeclaration decl : classType.getMembers()) {
			if (decl instanceof ICPPASTVisibilityLabel)
				lastLabel = (ICPPASTVisibilityLabel) decl;
			else if (decl == methodDecl)
				return lastLabel;
		}

		return null;
	}

	private static byte getBitset(IASTNodeLocation... locations) {
		for (IASTNodeLocation location : locations)
			if (location instanceof IASTMacroExpansionLocation) {
				IASTMacroExpansionLocation macroExpansion = (IASTMacroExpansionLocation) location;
				IASTPreprocessorMacroExpansion exp = macroExpansion
						.getExpansion();
				String macro = exp.getMacroReference().toString();

				if (QtKeywords.Q_SIGNAL.equals(macro)
						|| QtKeywords.Q_SIGNALS.equals(macro)
						|| QtKeywords.SIGNALS.equals(macro))
					return QtPlugin.SignalSlot_Mask_signal;
				if (QtKeywords.Q_SLOT.equals(macro)
						|| QtKeywords.Q_SLOTS.equals(macro)
						|| QtKeywords.SLOTS.equals(macro))
					return QtPlugin.SignalSlot_Mask_slot;
			}

		return 0;
	}

	private static byte getBitset(IASTNode... nodes) {
		byte bitset = 0;
		for (IASTNode node : nodes)
			if (node != null)
				for (IASTNodeLocation loc : node.getNodeLocations())
					bitset |= getBitset(loc);

		return bitset;
	}

	private static IASTNode getSimpleDecl(IASTNode node) {
		while (node != null && !(node instanceof IASTSimpleDeclaration))
			node = node.getParent();
		return node;
	}

	private byte getQtMarkers(ICPPMethod method, IASTName ast) {
		byte bitset = 0;
		if (ast == null)
			return bitset;

		// Look for macros on the previous visibility label.
		bitset |= getBitset(findVisibilityLabel(method, ast));

		// Look for macros on this function. See Bug 401696 for a better
		// description of why it needs
		// to work this why. Briefly, the parser does not associate empty macros
		// with the function when
		// they are the first thing in the declaration. E.g.,
		// #define X
		// void func1() {}
		// X void func2() {}
		// Could also look like:
		// void func1() {} X
		// void func2() {}
		//
		// The following code instead looks at the parents and children to find
		// all node locations between
		// the declarators.
		//
		// We first look at parents to find the closest SimpleDeclaration. We
		// then look at that node's parent
		// to find the node that is right before the target. Then we look at all
		// node locations between the
		// end of that previous node and the end of the target node.

		// find the closest containing SimpleDecl
		IASTNode simpleDecl = getSimpleDecl(ast);
		IASTNode parent = simpleDecl == null ? null : simpleDecl.getParent();
		if (parent == null)
			return bitset;

		// find the declaration before the target
		IASTNode previous = null;
		IASTNode[] children = parent.getChildren();
		if (children.length > 1)
			for (int i = 1; i < children.length; ++i) {
				if (children[i] == simpleDecl) {
					// if we haven't found a SimpleDecl, then find the nearest
					// previous non-problem node
					for (int j = i - 1; previous == null && j >= 0; --j)
						if (!(children[j] instanceof IASTProblemHolder))
							previous = children[j];
					break;
				}
				if (children[i] instanceof IASTSimpleDeclaration)
					previous = children[i];
			}

		// Signals/slots can only be declared inside of classes, so all cases we
		// care about have a
		// previous child, even if it is only the Base-class specifier.
		if (previous == null)
			return bitset;

		IASTFileLocation prevLocation = previous.getFileLocation();
		int prev_off = prevLocation.getNodeOffset();
		int prev_end = prevLocation.getNodeOffset()
				+ prevLocation.getNodeLength();

		// Figure out where the target node ends.
		int end = ast.getFileLocation().getNodeOffset()
				+ ast.getFileLocation().getNodeLength();

		// Examine all locations that appear after the previous node and before
		// the target node.
		boolean found_previous = false;
		for (IASTNodeLocation loc : parent.getNodeLocations()) {
			int o = loc.getNodeOffset();
			int e = loc.getNodeOffset() + loc.getNodeLength();

			// if the previous node has already been found, process this one
			if (found_previous)
				bitset |= getBitset(loc);

			// otherwise see if this is the previous node
			else if (o <= prev_off && e >= prev_end)
				found_previous = true;

			// stop processing when we're processed to the end of the target
			if (e >= end)
				break;
		}

		return bitset;
	}

	@Override
	public ITag process(ITagWriter tagWriter, IBinding binding, IASTName ast) {
		// only methods a be signals or slots
		if (!(binding instanceof ICPPMethod))
			return null;

		// Find all qt marker macros associated with this node.
		ICPPMethod method = (ICPPMethod) binding;
		byte bitset = getQtMarkers(method, ast);

		// create and store the bitset if needed
		if (bitset != 0) {
			IWritableTag tag = tagWriter.createTag(
					QtPlugin.SIGNAL_SLOT_TAGGER_ID, 1);
			if (tag != null && tag.putByte(0, bitset))
				return tag;
		}

		return null;
	}
}
