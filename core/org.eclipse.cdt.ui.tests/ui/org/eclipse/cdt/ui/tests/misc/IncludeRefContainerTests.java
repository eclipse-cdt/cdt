package org.eclipse.cdt.ui.tests.misc;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IIncludeReference;
import org.eclipse.cdt.core.testplugin.util.BaseTestCase5;
import org.eclipse.cdt.internal.ui.cview.IncludeRefContainer;
import org.eclipse.cdt.internal.ui.cview.IncludeRefContainer.Representation;
import org.eclipse.cdt.internal.ui.cview.IncludeReferenceProxy;
import org.eclipse.core.runtime.Path;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class IncludeRefContainerTests extends BaseTestCase5 {

	IIncludeReference mkinc(String path) {
		var m = mock(IIncludeReference.class);
		var p = new Path(path);
		when(m.getPath()).thenReturn(p);
		when(m.toString()).thenReturn(path);
		try {
			Mockito.when(m.getChildren()).thenReturn(new ICElement[0]);
		} catch (CModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return m;
	}

	String[] incPaths0 = new String[] {
			// @formatter:off
			"/usr/include",
			"/usr/include/c++/11",
			"/usr/include/c++/11/backward",
			"/usr/include/x86_64-linux-gnu/",
			"/usr/include/x86_64-linux-gnu/c++/11",
			"/usr/lib/gcc/x86_64-linux-gnu/11/include",
			"/usr/local/include",
			// @formatter:on

	};

	String[] incPaths1 = new String[] {
			// @formatter:off
			"/usr/include",
			"/usr/include/c++/11",
			"/usr/include/c++/11/backward",
			"/usr/include/x86_64-linux-gnu/",
			"/usr/include/x86_64-linux-gnu/c++/11",
			"/usr/lib/gcc/x86_64-linux-gnu/11/include",
			"/someotherpath",
			// @formatter:on

	};

	String[] incPaths2 = new String[] {
			// @formatter:off
			"/some/common/prefix/usr/include",
			"/some/common/prefix/usr/include/c++/11",
			"/some/common/prefix/usr/include/c++/11/backward",
			"/some/common/prefix/usr/include/x86_64-linux-gnu/",
			"/some/common/prefix/usr/include/x86_64-linux-gnu/c++/11",
			"/some/common/prefix/usr/lib/gcc/x86_64-linux-gnu/11/include",
			// @formatter:on

	};

	String[] pWin(String[] p) {
		return Arrays.stream(p).map(x -> "C:" + x).collect(Collectors.toList()).toArray(new String[0]);
	}

	String[] pWinExp(String[] pArr) {
		var rv = new ArrayList<String>();
		for (var p : pArr) {
			if (p.charAt(2) != '/') {
				rv.add(p);
			} else {
				rv.add(p.substring(0, 2) + "C:" + p.substring(2, p.length()));
			}
		}
		return rv.toArray(new String[0]);
	}

	String[][] incPaths = new String[][] { incPaths0, incPaths1, incPaths2, pWin(incPaths0), pWin(incPaths1),
			pWin(incPaths2) };

	IIncludeReference[] arrToRef(String[] incp) {
		return Arrays.stream(incp).map(x -> mkinc(x)).collect(Collectors.toList()).toArray(new IIncludeReference[0]);
	}

	String[] refToStr(Object[] oa) {
		return refToStr(oa, 0).toArray(new String[0]);
	}

	List<String> refToStr(Object[] ref, int level) {
		var rv = new ArrayList<String>();
		for (var o : ref) {
			if (!(o instanceof IncludeReferenceProxy))
				continue;
			var r = (IncludeReferenceProxy) o;
			var p = "  ".repeat(level);
			p += r.isIncludePath() ? "I " : "D ";
			p += r.toString();
			rv.add(p);
			rv.addAll(refToStr(r.getChildren(null), level + 1));
		}
		return rv;
	}

	IncludeRefContainer getIncRefCont(String[] paths) {
		var incList = arrToRef(paths);
		var proj = mock(ICProject.class);
		try {
			when(proj.getIncludeReferences()).thenReturn(incList);
		} catch (CModelException e) {
			e.printStackTrace();
			return null;
		}
		return new IncludeRefContainer(proj);
	}

	// Sanety check, whether both have the same number of includes
	void incCountCheck(String msg, String[] exp, String[] act) {
		long expcnt = exp.length;
		long actcnt = Arrays.stream(act).filter(x -> x.strip().startsWith("I")).count();
		Assert.assertEquals(msg, expcnt, actcnt);
	}

	@Test
	public void List() throws CModelException {
		for (int i = 0; i < incPaths.length; i++) {
			var irc = getIncRefCont(incPaths[i]);
			irc.representation = Representation.List;

			var ref = irc.getChildren(null);
			var refstr = refToStr(ref);
			String[] exp = Arrays.stream(incPaths[i]).map(x -> "I " + x).collect(Collectors.toList())
					.toArray(new String[0]);
			Assert.assertArrayEquals("IncP" + i, exp, refstr);
			incCountCheck("IncP" + i, incPaths[i], refstr);
		}
	}

	@Test
	public void SingleRoot() {
		var sing = new String[] { "/usr", "", "/some/common/prefix/usr", "C:/usr", "C:", "C:/some/common/prefix/usr"};
		for (int i = 0; i < incPaths.length; i++) {
			var irc = getIncRefCont(incPaths[i]);
			irc.representation = Representation.Single;

			var ref = irc.getChildren(null);
			var refstr = refToStr(ref);

			var exp = new ArrayList<String>();

			if (!sing[i].isEmpty()) {
				exp.add("D " + sing[i]);
			}
			final var expSeg = new Path(sing[i]).segmentCount();

			var t = Arrays.stream(incPaths[i])
					.map(x -> ((expSeg == 0) ? "I " : "  I ") + new Path(x).removeFirstSegments(expSeg).toString())
					.collect(Collectors.toList());

			exp.addAll(t);
			Assert.assertArrayEquals("IncP" + i, exp.toArray(new String[0]), refstr);
			incCountCheck("IncP" + i, incPaths[i], refstr);
		}
	}

	@Test
	public void Compact() {
		var exp0 = new String[] {
				// @formatter:off
				"D /usr",
				"  I include",
				"  D include",
				"    D c++",
				"      I 11",
				"      I 11/backward",
				"    I x86_64-linux-gnu/",
				"    I x86_64-linux-gnu/c++/11",
				"  I lib/gcc/x86_64-linux-gnu/11/include",
				"  I local/include"
				// @formatter:on
		};
		var exp1 = new String[] {
				// @formatter:off
				"I /someotherpath",
				"D /usr",
				"  I include",
				"  D include",
				"    D c++",
				"      I 11",
				"      I 11/backward",
				"    I x86_64-linux-gnu/",
				"    I x86_64-linux-gnu/c++/11",
				"  I lib/gcc/x86_64-linux-gnu/11/include"
				// @formatter:on
		};
		var exp1win = new String[] {
				// @formatter:off
				"D C:",
				"  I someotherpath",
				"  D usr",
				"    I include",
				"    D include",
				"      D c++",
				"        I 11",
				"        I 11/backward",
				"      I x86_64-linux-gnu/",
				"      I x86_64-linux-gnu/c++/11",
				"    I lib/gcc/x86_64-linux-gnu/11/include"
				// @formatter:on
		};
		var exp2 = new String[] {
				// @formatter:off
				"D /some/common/prefix/usr",
				"  I include",
				"  D include",
				"    D c++",
				"      I 11",
				"      I 11/backward",
				"    I x86_64-linux-gnu/",
				"    I x86_64-linux-gnu/c++/11",
				"  I lib/gcc/x86_64-linux-gnu/11/include"
				// @formatter:on
		};

		var exp = new String[][] { exp0, exp1, exp2, pWinExp(exp0), exp1win, pWinExp(exp2) };

		for (int i = 0; i < incPaths.length; i++) {
			var irc = getIncRefCont(incPaths[i]);
			irc.representation = Representation.Compact;

			var ref = irc.getChildren(null);
			var refstr = refToStr(ref);
			String msg = "IncP" + i + "\n" + Arrays.stream(refstr).collect(Collectors.joining("\",\n\"", "\"", "\"\n"));
			Assert.assertArrayEquals(msg, exp[i], refstr);
			incCountCheck("IncP" + i, incPaths[i], refstr);
		}
	}

	@Test
	public void Smart() {
		var exp0 = new String[] {
				// @formatter:off
				"I /usr/include",
				"D /usr/include",
				"  I c++/11",
				"  I c++/11/backward",
				"  I x86_64-linux-gnu/",
				"  I x86_64-linux-gnu/c++/11",
				"I /usr/lib/gcc/x86_64-linux-gnu/11/include",
				"I /usr/local/include"
				// @formatter:on
		};
		var exp1 = new String[] {
				// @formatter:off
				"I /someotherpath",
				"I /usr/include",
				"D /usr/include",
				"  I c++/11",
				"  I c++/11/backward",
				"  I x86_64-linux-gnu/",
				"  I x86_64-linux-gnu/c++/11",
				"I /usr/lib/gcc/x86_64-linux-gnu/11/include"
				// @formatter:on
		};
		var exp1Win = new String[] {
				// @formatter:off
				"D C:",
				"  I someotherpath",
				"  I usr/include",
				"  D usr/include",
				"    I c++/11",
				"    I c++/11/backward",
				"    I x86_64-linux-gnu/",
				"    I x86_64-linux-gnu/c++/11",
				"  I usr/lib/gcc/x86_64-linux-gnu/11/include"
				// @formatter:on
		};

		var exp2 = new String[] {
				// @formatter:off
				"D /some/common/prefix/usr",
				"  I include",
				"  D include",
				"    I c++/11",
				"    I c++/11/backward",
				"    I x86_64-linux-gnu/",
				"    I x86_64-linux-gnu/c++/11",
				"  I lib/gcc/x86_64-linux-gnu/11/include"
				// @formatter:on
		};

		var exp = new String[][] { exp0, exp1, exp2, pWinExp(exp0), exp1Win, pWinExp(exp2) };

		for (int i = 0; i < incPaths.length; i++) {
			var irc = getIncRefCont(incPaths[i]);
			irc.representation = Representation.Smart;

			var ref = irc.getChildren(null);
			var refstr = refToStr(ref);
			String msg = "IncP" + i + "\n" + Arrays.stream(refstr).collect(Collectors.joining("\",\n\"", "\"", "\"\n"));
			Assert.assertArrayEquals(msg + i, exp[i], refstr);
			incCountCheck("IncP" + i, incPaths[i], refstr);
		}
	}

}
