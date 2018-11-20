/*
 * Copyright (c) 2013, 2015 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.eclipse.cdt.internal.qt.core.pdom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IASTDeclaration;
import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTMacroExpansionLocation;
import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorMacroExpansion;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTCompositeTypeSpecifier;
import org.eclipse.cdt.core.dom.ast.cpp.ICPPASTVisibilityLabel;
import org.eclipse.cdt.internal.qt.core.QtKeywords;
import org.eclipse.cdt.internal.qt.core.index.IQMethod;

/**
 * The AST for a QObject is separated into regions based on macro expansions.  These
 * regions determine the Qt kind for methods that are declared within them.
 * <p>
 * This utility class makes one pass over the C++ class specification to identify
 * all such regions.  It also provides an iterator that can be used while examining
 * the class spec's members.
 */
public class QtASTClass {

	private final Iterator<Region> regions;
	private final Iterator<Tag> tags;
	private final Iterator<Revision> revisions;
	private Region region;
	private Tag tag;
	private Revision revision;

	/**
	 * Must only be called with increasing offset.  Internal pointers may be advanced on
	 * each call.
	 */
	public IQMethod.Kind getKindFor(int offset) {

		// There are 3 steps:
		// 1) The tags counter must always be advanced.  Tags only apply to the next declaration
		//    and therefore the internal counter must always be advanced.  Multiple tags are
		//    collapsed to find the highest precedence value.
		// 2) The region counter is advanced to find a region that either contains the offset
		//    or is the first region after the offset.  Regions override tags, so we use the
		//    region kind if one is found.
		// 3) The final result is based on tags (if they were present).
		//
		// This precedence is based on experimentation with the moc (ver 63).  It
		// ignores macros tagging a single method when that method is declared within
		// a signal/slot region.  E.g., the following example has two signals and one slot:
		//
		// class Q : public QObject
		// {
		// Q_OBJECT
		// signals: void signal1();
		// Q_SLOT   void signal2(); /* Tagged with Q_SLOT, but the declaration is within the
		//                           * signals region, so the moc considers it a signal. */
		// public:
		// Q_SLOT   void slot1();
		// };

		// Consume all tags since the last declaration to find the highest precedence tag.
		IQMethod.Kind kind = IQMethod.Kind.Unspecified;
		while (tag != null && tag.offset < offset) {
			kind = getHigherPrecedence(kind, tag.kind);
			tag = tags.hasNext() ? tags.next() : null;
		}

		// Advance regions to find one that does not end before this offset.
		while (region != null && region.end < offset)
			region = regions.hasNext() ? regions.next() : null;

		// If the offset is within this region, then use its kind.
		if (region != null && region.contains(offset))
			kind = region.kind;

		return kind;
	}

	/**
	 * Must only be called with increasing offset.  Internal pointers may be advanced on
	 * each call.
	 */
	public Long getRevisionFor(int offset) {

		// Consume all revisions since the last declaration to find one (if any) that applies
		// to this declaration.
		Long rev = null;
		while (revision != null && revision.offset < offset) {
			rev = revision.revision;
			revision = revisions.hasNext() ? revisions.next() : null;
		}

		return rev;
	}

	private static IQMethod.Kind getHigherPrecedence(IQMethod.Kind kind1, IQMethod.Kind kind2) {
		switch (kind1) {
		case Unspecified:
			return kind2;
		case Invokable:
			switch (kind2) {
			case Slot:
			case Signal:
				return kind2;
			default:
				return kind1;
			}
		case Signal:
			if (kind2 == IQMethod.Kind.Slot)
				return kind2;
			return kind2;
		case Slot:
			return kind1;
		}
		return IQMethod.Kind.Unspecified;
	}

	public static QtASTClass create(ICPPASTCompositeTypeSpecifier spec) {

		// There is more detail in Bug 401696 describing why this needs to look at all
		// the node locations.  Briefly, the CDT parser does not associate empty macros
		// with the function when they are the first thing in the declaration.  E.g.,
		//
		//     #define X
		//       void func1() {}
		//     X void func2() {}
		//
		// Could also look like:
		//     void func1() {} X
		//     void func2() {}
		//
		// The nodes are processed in three stages which are described in detail below.  Only
		// the first stage looks at the nodes, the later stages just cleanup results from the
		// first walk over all node locations.

		// 1) Examine the locations to find all macro expansions.  This finds a beginning and
		//    highest possible end for the regions.  It also locates the offset for single-method
		//    tags (including resolving precedence).
		//    This allows single-method tags to overlap regions because regions may be shortened
		//    by a later step.
		ArrayList<Tag> tags = new ArrayList<>();
		ArrayList<Revision> revisions = new ArrayList<>();
		ArrayList<Region> regions = new ArrayList<>();
		Region currRegion = null;
		for (IASTNodeLocation location : spec.getNodeLocations()) {

			Tag tag = Tag.create(location);
			if (tag != null)
				tags.add(tag);

			Revision revision = Revision.create(location);
			if (revision != null)
				revisions.add(revision);

			Region region = Region.create(location);
			if (region != null) {
				if (currRegion != null)
					currRegion.end = region.begin;

				currRegion = region;
				regions.add(region);
			}
		}

		// 2) Make the regions smaller where visibility labels are introduced.
		if (!regions.isEmpty()) {
			Iterator<Region> iterator = regions.iterator();
			Region region = iterator.next();
			for (IASTDeclaration decl : spec.getMembers()) {

				// Ignore everything other than visibility labels.
				if (!(decl instanceof ICPPASTVisibilityLabel))
					continue;

				int offset = decl.getFileLocation().getNodeOffset();

				// Otherwise terminate all regions that start before this label and advance
				// to the first one that follows.
				while (region != null && region.begin < offset) {
					region.end = offset;
					region = iterator.hasNext() ? iterator.next() : null;
				}

				// Stop searching for visibility labels after the last region has been terminated.
				if (region == null)
					break;
			}
		}

		// 3) Eliminate tags that are within regions.
		if (!tags.isEmpty()) {
			Iterator<Tag> iterator = tags.iterator();
			Tag tag = iterator.next();
			for (Region region : regions) {

				// Keep all tags that are before the start of this region.
				while (tag != null && tag.offset < region.begin)
					tag = iterator.hasNext() ? iterator.next() : null;

				// Delete all tags that are within this region.
				while (tag != null && region.contains(tag.offset)) {
					iterator.remove();
					tag = iterator.hasNext() ? iterator.next() : null;
				}

				// Stop searching when there are no more tags to be examined.
				if (tag == null)
					break;
			}
		}

		return new QtASTClass(regions, tags, revisions);
	}

	private QtASTClass(List<Region> regions, List<Tag> tags, List<Revision> revisions) {
		this.regions = regions.iterator();
		this.tags = tags.iterator();
		this.revisions = revisions.iterator();

		this.region = this.regions.hasNext() ? this.regions.next() : null;
		this.tag = this.tags.hasNext() ? this.tags.next() : null;
		this.revision = this.revisions.hasNext() ? this.revisions.next() : null;
	}

	private static class Region {
		public final int begin;
		public int end = Integer.MAX_VALUE;
		public final IQMethod.Kind kind;

		public Region(int begin, IQMethod.Kind kind) {
			this.begin = begin;
			this.kind = kind;
		}

		public boolean contains(int offset) {
			return offset >= begin && offset < end;
		}

		/**
		 * Return a region for the given location or null if the location does not
		 * introduce a region.
		 */
		public static Region create(IASTNodeLocation location) {
			if (!(location instanceof IASTMacroExpansionLocation))
				return null;

			IASTMacroExpansionLocation macroLocation = (IASTMacroExpansionLocation) location;
			IASTFileLocation fileLocation = macroLocation.asFileLocation();
			if (fileLocation == null)
				return null;

			int offset = fileLocation.getNodeOffset();
			IASTPreprocessorMacroExpansion expansion = macroLocation.getExpansion();
			String macroName = getMacroName(expansion);
			if (QtKeywords.Q_SLOTS.equals(macroName) || QtKeywords.SLOTS.equals(macroName))
				return new Region(offset, IQMethod.Kind.Slot);
			if (QtKeywords.Q_SIGNALS.equals(macroName) || QtKeywords.SIGNALS.equals(macroName))
				return new Region(offset, IQMethod.Kind.Signal);
			return null;
		}
	}

	private static class Tag {
		public final int offset;
		public IQMethod.Kind kind;

		private Tag(int begin, IQMethod.Kind kind) {
			this.offset = begin;
			this.kind = kind;
		}

		/**
		 * Return a tag for the given location or null if the location does not
		 * introduce a tag.
		 */
		public static Tag create(IASTNodeLocation location) {
			if (!(location instanceof IASTMacroExpansionLocation))
				return null;

			IASTMacroExpansionLocation macroLocation = (IASTMacroExpansionLocation) location;
			IASTFileLocation fileLocation = macroLocation.asFileLocation();
			if (fileLocation == null)
				return null;

			int offset = fileLocation.getNodeOffset();
			IASTPreprocessorMacroExpansion expansion = macroLocation.getExpansion();
			String macroName = getMacroName(expansion);
			if (QtKeywords.Q_SLOT.equals(macroName))
				return new Tag(offset, IQMethod.Kind.Slot);
			if (QtKeywords.Q_SIGNAL.equals(macroName))
				return new Tag(offset, IQMethod.Kind.Signal);
			if (QtKeywords.Q_INVOKABLE.equals(macroName))
				return new Tag(offset, IQMethod.Kind.Invokable);
			return null;
		}
	}

	private static class Revision {
		private final int offset;
		private final Long revision;

		// This regular expression matches Q_REVISION macro expansions.  It allows C++ integer
		// literals as the expansion parameter.  The integer literal is provided in capture
		// group 1.  Hexadecimal and octal prefixes are included in the capture group.  Unsigned
		// and long suffixes are allowed but are excluded from the capture group.  The matcher's
		// input string should be trimmed and have all newlines replaced.
		private static final Pattern QREVISION_REGEX = Pattern
				.compile("^Q_REVISION\\s*\\(\\s*((?:0x)?[\\da-fA-F]+)[ulUL]*\\s*\\)$");

		public Revision(int offset, Long revision) {
			this.offset = offset;
			this.revision = revision;
		}

		/**
		 * Return a tag for the given location or null if the location does not
		 * introduce a tag.
		 */
		public static Revision create(IASTNodeLocation location) {
			if (!(location instanceof IASTMacroExpansionLocation))
				return null;

			IASTMacroExpansionLocation macroLocation = (IASTMacroExpansionLocation) location;
			IASTFileLocation fileLocation = macroLocation.asFileLocation();
			if (fileLocation == null)
				return null;

			int offset = fileLocation.getNodeOffset();
			IASTPreprocessorMacroExpansion expansion = macroLocation.getExpansion();
			String macroName = getMacroName(expansion);
			if (!QtKeywords.Q_REVISION.equals(macroName))
				return null;

			String raw = expansion.getRawSignature();
			if (raw == null)
				return null;

			// Trim leading and trailing whitespace and remove all newlines.
			Matcher m = QREVISION_REGEX.matcher(raw.trim().replaceAll("\\s+", ""));
			if (m.matches()) {
				try {
					return new Revision(offset, Long.parseLong(m.group(1)));
				} catch (NumberFormatException e) {
					// The number will be parsed incorrectly when the C++ client code does not
					// contain a valid integer.  We can't do anything about that, so the exception
					// is ignored.  A codan checker could notify the user of this problem.
				}
			}

			return null;
		}
	}

	/**
	 * Find and return the simple name of the macro that is being expanded or null if the name
	 * cannot be found.
	 */
	private static String getMacroName(IASTPreprocessorMacroExpansion expansion) {
		if (expansion == null)
			return null;

		IASTName name = expansion.getMacroReference();
		return name == null ? null : name.toString();
	}
}
