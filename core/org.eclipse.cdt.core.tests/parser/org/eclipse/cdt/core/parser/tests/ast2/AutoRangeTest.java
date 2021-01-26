package org.eclipse.cdt.core.parser.tests.ast2;

import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPVariable;
import org.junit.Assert;

/*******************************************************************************
 * Copyright (c) 2021 Advantest Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

public class AutoRangeTest extends AST2CPPTestBase {

	public AutoRangeTest() {
	}

	public AutoRangeTest(String name) {
		super(name);
	}

	//	class Test {
	//	public:
	//	  void doSth() const {}
	//	};
	//
	//	template<typename T> struct sequence_wrapper {
	//	  T *mpElements;
	//	};
	//
	//	template<typename T> class sequence_iterator;
	//
	//	template<typename T> sequence_iterator<T> begin(sequence_wrapper<T> &r)
	//	{
	//	  return sequence_iterator<T>(r.mpElements);
	//	}
	//
	//	template<typename T> sequence_iterator<T> end(sequence_wrapper<T> &r)
	//	{
	//	  return sequence_iterator<T>(r.mpElements);
	//	}
	//
	//	template<typename T> class sequence_iterator {
	//	private:
	//
	//	  T *mpCurrentElement;
	//
	//	  sequence_iterator(T *pElements)
	//	  {
	//	    this->mpCurrentElement = pElements;
	//	  }
	//
	//	public:
	//
	//	  bool operator !=(const sequence_iterator<T> &i) const
	//	  {
	//	    return mpCurrentElement != i.mpCurrentElement;
	//	  }
	//
	//	  T& operator *(void) const
	//	  {
	//	    return *mpCurrentElement;
	//	  }
	//
	//	  sequence_iterator<T>&
	//	  operator ++(void)
	//	  {
	//	    ++mpCurrentElement;
	//	    return *static_cast<sequence_iterator<T>*>(this);
	//	  }
	//
	//	  friend sequence_iterator begin<T>(sequence_wrapper<T> &r);
	//	  friend sequence_iterator end<T>(sequence_wrapper<T> &r);
	//	};
	//
	//	int main()
	//	{
	//	  Test t;
	//	  sequence_wrapper<Test> sw { &t };
	//	  for (auto &&prxyyl : sw) {
	//	    prxyyl.doSth();
	//	  }
	//	  return 0;
	//	}
	public void testAutoReferenceType() throws Exception {
		parseAndCheckBindings();

		BindingAssertionHelper helper = getAssertionHelper();
		CPPVariable prxyyl = (CPPVariable) helper.assertNonProblem("auto &&prxyyl", "prxyyl");

		Assert.assertTrue(prxyyl.getType().toString().equals("Test"));
	}

}
