/*******************************************************************************
 * Copyright (c) 2008, 2013 Institute for Software, HSR Hochschule fuer Technik
 * Rapperswil, University of applied sciences and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Institute for Software - initial API and implementation
 *     Sergey Prigogin (Google)
 *     Marc-Andre Laperle (Ericsson)
 *******************************************************************************/
package org.eclipse.cdt.ui.tests.refactoring.gettersandsetters;

import org.eclipse.cdt.internal.ui.refactoring.CRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.AccessorDescriptor.AccessorKind;
import org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.GenerateGettersAndSettersRefactoring;
import org.eclipse.cdt.internal.ui.refactoring.gettersandsetters.GetterSetterContext;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.tests.refactoring.RefactoringTestBase;

import junit.framework.Test;

/**
 * Tests for Generate Getters and Setters refactoring.
 */
public class GenerateGettersAndSettersTest extends RefactoringTestBase {
	private String[] selectedGetters;
	private String[] selectedSetters;
	private boolean definitionSeparate;
	private GenerateGettersAndSettersRefactoring refactoring;

	public GenerateGettersAndSettersTest() {
		super();
	}

	public GenerateGettersAndSettersTest(String name) {
		super(name);
	}

	public static Test suite() {
		return suite(GenerateGettersAndSettersTest.class);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		resetPreferences();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
		resetPreferences();
	}

	@Override
	protected CRefactoring createRefactoring() {
		if (ascendingVisibilityOrder) {
			getPreferenceStore().setValue(PreferenceConstants.CLASS_MEMBER_ASCENDING_VISIBILITY_ORDER,
					ascendingVisibilityOrder);
		}
		refactoring = new GenerateGettersAndSettersRefactoring(getSelectedTranslationUnit(), getSelection(),
				getCProject());
		return refactoring;
	}

	@Override
	protected void simulateUserInput() {
		GetterSetterContext context = refactoring.getContext();

		if (selectedGetters != null) {
			for (String name : selectedGetters) {
				context.selectAccessorForField(name, AccessorKind.GETTER);
			}
		}
		if (selectedSetters != null) {
			for (String name : selectedSetters) {
				context.selectAccessorForField(name, AccessorKind.SETTER);
			}
		}
		context.setDefinitionSeparate(definitionSeparate);
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char* name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char*   Name();
	//
	//	void	Print();
	//
	//	int	 /*$*/SocSecNo/*$$*/();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId() {
	//		return systemId;
	//	}
	//
	//	void setSystemId(int systemId) {
	//		this->systemId = systemId;
	//	}
	//};
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char* name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char*   Name();
	//
	//	void	Print();
	//
	//	int	 SocSecNo();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId() {
	//		return systemId;
	//	}
	//
	//	void setSystemId(int systemId) {
	//		this->systemId = systemId;
	//	}
	//
	//	char* getName() const {
	//		return name;
	//	}
	//};
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */

	//A.cpp
	//#include "A.h"
	//
	//int Person::SocSecNo() {
	//	return socSecNo;
	//}
	//
	//int main(int argc, char** argv) {
	//}
	//====================
	public void testOneGetterSelection() throws Exception {
		selectedGetters = new String[] { "name" };
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//namespace Personal {
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char* name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char*   Name();
	//
	//	void	Print();
	//
	//	int	 /*$*/SocSecNo/*$$*/();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId() {
	//		return systemId;
	//	}
	//
	//	void setSystemId(int systemId) {
	//		this->systemId = systemId;
	//	}
	//};
	//
	//}
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//namespace Personal {
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char* name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char*   Name();
	//
	//	void	Print();
	//
	//	int	 SocSecNo();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId() {
	//		return systemId;
	//	}
	//
	//	void setSystemId(int systemId) {
	//		this->systemId = systemId;
	//	}
	//
	//	char* getName() const {
	//		return name;
	//	}
	//};
	//
	//}
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */

	//A.cpp
	//#include "A.h"
	//
	//int Person::SocSecNo() {
	//	return socSecNo;
	//}
	//
	//int main(int argc, char** argv) {
	//}
	//====================
	public void testOneGetterSelectionWithNamespace() throws Exception {
		selectedGetters = new String[] { "name" };
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char *name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char   *Name();
	//
	//	void	Print();
	//
	//	int	 /*$*/SocSecNo/*$$*/();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId() {
	//		return systemId;
	//	}
	//
	//	void setSystemId(int systemId) {
	//		this->systemId = systemId;
	//	}
	//};
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char *name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char   *Name();
	//
	//	void	Print();
	//
	//	int	 SocSecNo();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId() {
	//		return systemId;
	//	}
	//
	//	void setSystemId(int systemId) {
	//		this->systemId = systemId;
	//	}
	//
	//	void setName(char *name) {
	//		this->name = name;
	//	}
	//};
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */

	//A.cpp
	//#include "A.h"
	//
	//int Person::SocSecNo() {
	//	return socSecNo;
	//}
	//
	//int main(int argc, char **argv) {
	//}
	//====================
	public void testOneSetterSelection() throws Exception {
		selectedSetters = new String[] { "name" };
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char *name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char   *Name();
	//
	//	void	Print();
	//
	//	int	 /*$*/SocSecNo/*$$*/();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId() {
	//		return systemId;
	//	}
	//
	//	void setSystemId(int systemId) {
	//		this->systemId = systemId;
	//	}
	//};
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char *name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char   *Name();
	//
	//	void	Print();
	//
	//	int	 SocSecNo();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId() {
	//		return systemId;
	//	}
	//
	//	void setSystemId(int systemId) {
	//		this->systemId = systemId;
	//	}
	//
	//	char* getName() const {
	//		return name;
	//	}
	//
	//	void setName(char *name) {
	//		this->name = name;
	//	}
	//};
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */

	//A.cpp
	//#include "A.h"
	//
	//int Person::SocSecNo() {
	//	return socSecNo;
	//}
	//
	//int main(int argc, char **argv) {
	//}
	//====================
	public void testGetterAndSetterSelection() throws Exception {
		selectedGetters = new String[] { "name" };
		selectedSetters = new String[] { "name" };
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char *name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char   *Name();
	//
	//	void	Print();
	//
	//	int	 /*$*/SocSecNo/*$$*/();
	//
	//	int	 GetUniqueId();
	//};
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char *name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char   *Name();
	//
	//	void	Print();
	//
	//	int	 SocSecNo();
	//
	//	int	 GetUniqueId();
	//
	//	char* getName() const {
	//		return name;
	//	}
	//
	//	void setName(char *name) {
	//		this->name = name;
	//	}
	//
	//	int getSystemId() const {
	//		return systemId;
	//	}
	//
	//	void setSystemId(int systemId) {
	//		this->systemId = systemId;
	//	}
	//};
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */

	//A.cpp
	//#include "A.h"
	//
	//int Person::SocSecNo() {
	//	return socSecNo;
	//}
	//
	//int main(int argc, char **argv) {
	//}
	//====================
	public void testMultipleSelection() throws Exception {
		selectedGetters = new String[] { "name", "systemId" };
		selectedSetters = new String[] { "name", "systemId" };
		assertRefactoringSuccess();
	}

	//GaS.h
	//#ifndef GAS_H_
	//#define GAS_H_
	//
	//class GaS {
	//public:
	//	GaS();
	//	virtual ~GaS();
	//	bool /*$*/ok/*$$*/;
	//	void method2();
	//
	//private:
	//	int i;
	//};
	//
	//#endif
	//====================
	//#ifndef GAS_H_
	//#define GAS_H_
	//
	//class GaS {
	//public:
	//	GaS();
	//	virtual ~GaS();
	//	bool ok;
	//	void method2();
	//
	//	int getI() const {
	//		return i;
	//	}
	//
	//	void setI(int i) {
	//		this->i = i;
	//	}
	//
	//	bool isOk() const {
	//		return ok;
	//	}
	//
	//	void setOk(bool ok) {
	//		this->ok = ok;
	//	}
	//
	//private:
	//	int i;
	//};
	//
	//#endif

	//GaS.cpp
	//#include "Getters.h"
	//
	//GaS::Getters() {
	//}
	//
	//GaS::~Getters() {
	//}
	public void testVisibilityOrder() throws Exception {
		selectedGetters = new String[] { "i", "ok" };
		selectedSetters = new String[] { "i", "ok" };
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//private:
	//	int /*$*/id/*$$*/;
	//};
	//
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//public:
	//	int getId() const {
	//		return id;
	//	}
	//
	//	void setId(int id) {
	//		this->id = id;
	//	}
	//
	//private:
	//	int id;
	//};
	//
	//#endif /* A_H_ */
	public void testNoMethods() throws Exception {
		selectedGetters = new String[] { "id" };
		selectedSetters = new String[] { "id" };
		assertRefactoringSuccess();
	}

	//A.h
	///*
	// * test.h
	// */
	//
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	////comment1
	//class test {
	//	int /*$*/i/*$$*/; //comment2
	//	char* b;
	//	//comment3
	//};
	//
	//#endif /* TEST_H_ */
	//====================
	///*
	// * test.h
	// */
	//
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	////comment1
	//class test {
	//	int i; //comment2
	//	char* b;
	//	//comment3
	//
	//public:
	//	int getI() const {
	//		return i;
	//	}
	//
	//	void setI(int i) {
	//		this->i = i;
	//	}
	//};
	//
	//#endif /* TEST_H_ */
	public void testNoMethodsAscendingVisibilityOrder() throws Exception {
		selectedGetters = new String[] { "i" };
		selectedSetters = new String[] { "i" };
		ascendingVisibilityOrder = true;
		assertRefactoringSuccess();
	}

	//A.h
	///*
	// * test.h
	// */
	//
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	////comment1
	//class test {
	//	void /*$*/test/*$$*/();
	//	//comment3
	//};
	//
	//#endif /* TEST_H_ */
	//====================
	///*
	// * test.h
	// */
	//
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	////comment1
	//class test {
	//	void test();
	//	//comment3
	//};
	//
	//#endif /* TEST_H_ */
	public void testNoFields() throws Exception {
		assertRefactoringFailure();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//struct FullName {
	//	const char *first;
	//	const char *last;
	//	FullName(const FullName &other);
	//	~FullName();
	//};
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	FullName name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char   *Name();
	//
	//	void	Print();
	//
	//	int	 /*$*/SocSecNo/*$$*/();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId();
	//
	//	void setSystemId(int systemId);
	//};
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//struct FullName {
	//	const char *first;
	//	const char *last;
	//	FullName(const FullName &other);
	//	~FullName();
	//};
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	FullName name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char   *Name();
	//
	//	void	Print();
	//
	//	int	 SocSecNo();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId();
	//
	//	void setSystemId(int systemId);
	//	const FullName& getName() const;
	//	void setName(const FullName &name);
	//};
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */

	//A.cpp
	//#include "A.h"
	//
	//int Person::SocSecNo() {
	//	return socSecNo;
	//}
	//
	//int main(int argc, char **argv) {
	//}
	//====================
	//#include "A.h"
	//
	//const FullName& Person::getName() const {
	//	return name;
	//}
	//
	//void Person::setName(const FullName &name) {
	//	this->name = name;
	//}
	//
	//int Person::SocSecNo() {
	//	return socSecNo;
	//}
	//
	//int main(int argc, char **argv) {
	//}
	public void testPassByReferenceSeparateDefinition() throws Exception {
		definitionSeparate = true;
		selectedGetters = new String[] { "name" };
		selectedSetters = new String[] { "name" };
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//namespace Personal {
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char* name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char*   Name();
	//
	//	void	Print();
	//
	//	int	 /*$*/SocSecNo/*$$*/();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId() {
	//		return systemId;
	//	}
	//
	//	void setSystemId(int systemId) {
	//		this->systemId = systemId;
	//	}
	//};
	//
	//}
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//namespace Personal {
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char* name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char*   Name();
	//
	//	void	Print();
	//
	//	int	 SocSecNo();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId() {
	//		return systemId;
	//	}
	//
	//	void setSystemId(int systemId) {
	//		this->systemId = systemId;
	//	}
	//
	//	char* getName() const;
	//};
	//
	//}
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */

	//A.cpp
	//#include "A.h"
	//
	//namespace Personal {
	//
	//int Person::SocSecNo() {
	//	return socSecNo;
	//}
	//
	//} // namespace Personal
	//
	//int main(int argc, char** argv) {
	//}
	//====================
	//#include "A.h"
	//
	//namespace Personal {
	//
	//char* Person::getName() const {
	//	return name;
	//}
	//
	//int Person::SocSecNo() {
	//	return socSecNo;
	//}
	//
	//} // namespace Personal
	//
	//int main(int argc, char** argv) {
	//}
	public void testOneGetterSelectionWithNamespaceSeparateDefinition() throws Exception {
		definitionSeparate = true;
		selectedGetters = new String[] { "name" };
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char *name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char   *Name();
	//
	//	void	Print();
	//
	//	int	 /*$*/SocSecNo/*$$*/();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId() {
	//		return systemId;
	//	}
	//
	//	void setSystemId(int systemId) {
	//		this->systemId = systemId;
	//	}
	//};
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char *name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char   *Name();
	//
	//	void	Print();
	//
	//	int	 SocSecNo();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId() {
	//		return systemId;
	//	}
	//
	//	void setSystemId(int systemId) {
	//		this->systemId = systemId;
	//	}
	//
	//	void setName(char *name);
	//};
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */

	//A.cpp
	//#include "A.h"
	//
	//int Person::SocSecNo() {
	//	return socSecNo;
	//}
	//
	//int main(int argc, char **argv) {
	//}
	//====================
	//#include "A.h"
	//
	//void Person::setName(char *name) {
	//	this->name = name;
	//}
	//
	//int Person::SocSecNo() {
	//	return socSecNo;
	//}
	//
	//int main(int argc, char **argv) {
	//}
	public void testOneSetterSelectionSeparateDefinition() throws Exception {
		definitionSeparate = true;
		selectedSetters = new String[] { "name" };
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char *name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char   *Name();
	//
	//	void	Print();
	//
	//	int	 /*$*/SocSecNo/*$$*/();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId() {
	//		return systemId;
	//	}
	//
	//	void setSystemId(int systemId) {
	//		this->systemId = systemId;
	//	}
	//};
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//private:
	//	int systemId;
	//
	//protected:
	//	char *name;
	//
	//public:
	//	const int socSecNo;
	//
	//	Person myFriend;
	//
	//	Person(int socSecNo); // constructor
	//
	//	~Person(); // destructor
	//
	//	char   *Name();
	//
	//	void	Print();
	//
	//	int	 SocSecNo();
	//
	//	int	 GetUniqueId();
	//
	//	int getSystemId() {
	//		return systemId;
	//	}
	//
	//	void setSystemId(int systemId) {
	//		this->systemId = systemId;
	//	}
	//
	//	char* getName() const;
	//	void setName(char *name);
	//};
	//
	//int gooo = 1;
	//
	//#endif /* A_H_ */

	//A.cpp
	//#include "A.h"
	//
	//int Person::SocSecNo() {
	//	return socSecNo;
	//}
	//
	//int main(int argc, char **argv) {
	//}
	//====================
	//#include "A.h"
	//
	//char* Person::getName() const {
	//	return name;
	//}
	//
	//void Person::setName(char *name) {
	//	this->name = name;
	//}
	//
	//int Person::SocSecNo() {
	//	return socSecNo;
	//}
	//
	//int main(int argc, char **argv) {
	//}
	public void testSelectionWithSeparateDefinition() throws Exception {
		definitionSeparate = true;
		selectedGetters = new String[] { "name" };
		selectedSetters = new String[] { "name" };
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//private:
	//	int /*$*/id/*$$*/;
	//};
	//
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class Person {
	//public:
	//	int getId() const;
	//	void setId(int id);
	//
	//private:
	//	int id;
	//};
	//
	//inline int Person::getId() const {
	//	return id;
	//}
	//
	//inline void Person::setId(int id) {
	//	this->id = id;
	//}
	//
	//#endif /* A_H_ */
	public void testNoMethodsSeparateDefinition_1() throws Exception {
		definitionSeparate = true;
		selectedGetters = new String[] { "id" };
		selectedSetters = new String[] { "id" };
		assertRefactoringSuccess();
	}

	//A.h
	///*
	// * test.h
	// */
	//
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	////comment1
	//class test {
	//	int /*$*/i/*$$*/; //comment2
	//	char* b;
	//	//comment3
	//};
	//
	//#endif /* TEST_H_ */
	//====================
	///*
	// * test.h
	// */
	//
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	////comment1
	//class test {
	//public:
	//	int getI() const;
	//	void setI(int i);
	//
	//private:
	//	int i; //comment2
	//	char* b;
	//	//comment3
	//};
	//
	//inline int test::getI() const {
	//	return i;
	//}
	//
	//inline void test::setI(int i) {
	//	this->i = i;
	//}
	//
	//#endif /* TEST_H_ */
	public void testNoMethodsSeparateDefinition_2() throws Exception {
		definitionSeparate = true;
		selectedGetters = new String[] { "i" };
		selectedSetters = new String[] { "i" };
		assertRefactoringSuccess();
	}

	//Test.h
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	//namespace foo {
	//
	//class Test {
	//	int /*$*/testField/*$$*/;
	//	void foo();
	//};
	//
	//} // namespace foo
	//
	//#endif
	//====================
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	//namespace foo {
	//
	//class Test {
	//public:
	//	int getTestField() const;
	//	void setTestField(int testField);
	//
	//private:
	//	int testField;
	//	void foo();
	//};
	//
	//} // namespace foo
	//
	//#endif

	//Test.cpp
	//#include "Test.h"
	//
	//namespace foo {
	//
	//void Test::foo() {
	//}
	//
	//}
	//====================
	//#include "Test.h"
	//
	//namespace foo {
	//
	//int Test::getTestField() const {
	//	return testField;
	//}
	//
	//void Test::setTestField(int testField) {
	//	this->testField = testField;
	//}
	//
	//void Test::foo() {
	//}
	//
	//}
	public void testBug323780() throws Exception {
		definitionSeparate = true;
		selectedGetters = new String[] { "testField" };
		selectedSetters = new String[] { "testField" };
		assertRefactoringSuccess();
	}

	//Test.h
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	//class Test {
	//	int /*$*/testField/*$$*/;
	//};
	//
	//#endif
	//====================
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	//class Test {
	//public:
	//	int getTestField() const;
	//	void setTestField(int testField);
	//
	//private:
	//	int testField;
	//};
	//
	//#endif

	//Test.cxx
	//====================
	//int Test::getTestField() const {
	//	return testField;
	//}
	//
	//void Test::setTestField(int testField) {
	//	this->testField = testField;
	//}
	public void testInsertDefinitionInEmptyImplementationFile_Bug337040_1() throws Exception {
		definitionSeparate = true;
		selectedGetters = new String[] { "testField" };
		selectedSetters = new String[] { "testField" };
		assertRefactoringSuccess();
	}

	//component_b/public_headers/Test.h
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	//class Test {
	//	int /*$*/testField/*$$*/;
	//};
	//
	//#endif
	//====================
	//#ifndef TEST_H_
	//#define TEST_H_
	//
	//class Test {
	//public:
	//	int getTestField() const;
	//	void setTestField(int testField);
	//
	//private:
	//	int testField;
	//};
	//
	//#endif

	//component_b/implementation/Test.cpp
	//====================
	//int Test::getTestField() const {
	//	return testField;
	//}
	//
	//void Test::setTestField(int testField) {
	//	this->testField = testField;
	//}

	//component_a/public_headers/Test.h
	//====================

	//component_a/implementation/Test.cpp
	//====================
	public void testInsertDefinitionInEmptyImplementationFile_Bug337040_2() throws Exception {
		definitionSeparate = true;
		selectedGetters = new String[] { "testField" };
		selectedSetters = new String[] { "testField" };
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//#define Typedef \
	//	typedef int Int
	//
	//class Test
	//{
	//public:
	//	Typedef;
	//
	//	void     Foo();
	//	Test();
	//
	//	int /*$*/test/*$$*/;
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//#define Typedef \
	//	typedef int Int
	//
	//class Test
	//{
	//public:
	//	Typedef;
	//
	//	void     Foo();
	//	Test();
	//
	//	int getTest() const {
	//		return test;
	//	}
	//
	//	int test;
	//};
	//#endif /* A_H_ */
	public void testClassWithMacro_Bug363244() throws Exception {
		selectedGetters = new String[] { "test" };
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//private:
	//	int /*$*/a[2]/*$$*/;
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class A {
	//public:
	//	const int* getA() const {
	//		return a;
	//	}
	//
	//private:
	//	int a[2];
	//};
	//#endif /* A_H_ */
	public void testGetterForAnArrayField_Bug319278() throws Exception {
		selectedGetters = new String[] { "a" };
		assertRefactoringSuccess();
	}

	//A.h
	//#ifndef A_H_
	//#define A_H_
	//
	//class getClass {
	//private:
	//	int /*$*/class_/*$$*/;
	//};
	//#endif /* A_H_ */
	//====================
	//#ifndef A_H_
	//#define A_H_
	//
	//class getClass {
	//public:
	//	int getClass1() const {
	//		return class_;
	//	}
	//
	//	void setClass(int clazz) {
	//		class_ = clazz;
	//	}
	//
	//private:
	//	int class_;
	//};
	//#endif /* A_H_ */
	public void testAvoidingReservedNames_Bug352258() throws Exception {
		selectedGetters = new String[] { "class_" };
		selectedSetters = new String[] { "class_" };
		assertRefactoringSuccess();
	}

	//A.h
	//class Foo {
	//	int a, *b, /*$*/c[2]/*$$*/;
	//};
	//====================
	//class Foo {
	//public:
	//	void setA(int a) {
	//		this->a = a;
	//	}
	//
	//	int* getB() const {
	//		return b;
	//	}
	//
	//	void setB(int *b) {
	//		this->b = b;
	//	}
	//
	//	const int* getC() const {
	//		return c;
	//	}
	//
	//private:
	//	int a, *b, c[2];
	//};
	public void testMultipleDeclarators_371840() throws Exception {
		selectedGetters = new String[] { "b", "c" };
		selectedSetters = new String[] { "a", "b" };
		assertRefactoringSuccess();
	}

	//A.h
	//namespace ns {
	//class Test {
	//class Foo {
	//	public:
	//		int /*$*/a/*$$*/;
	//	};
	//	};
	//}
	//====================
	//namespace ns {
	//class Test {
	//class Foo {
	//	public:
	//		int a;
	//
	//		int getA() const;
	//	};
	//	};
	//}

	//A.cpp
	//#include "A.h"
	//
	//====================
	//#include "A.h"
	//
	//int ns::Test::Foo::getA() const {
	//	return a;
	//}
	public void testNestedClasses_Bug316083() throws Exception {
		definitionSeparate = true;
		selectedGetters = new String[] { "a" };
		assertRefactoringSuccess();
	}

	//Bug551761.h
	//#ifndef BUG551761_H_
	//#define BUG551761_H_
	//
	//class Bug551761 {
	//private:
	//	int /*$*/aVar/*$$*/;
	//};
	//
	//#endif /* BUG551761_H_ */
	//
	//====================
	//#ifndef BUG551761_H_
	//#define BUG551761_H_
	//
	//class Bug551761 {
	//public:
	//	int getAVar() const {
	//		return aVar;
	//	}
	//
	//	void setAVar(int aVar) {
	//		this->aVar = aVar;
	//	}
	//
	//private:
	//	int aVar;
	//};
	//
	//#endif /* BUG551761_H_ */
	//
	public void testSingleLetterPrefix_Bug551761() throws Exception {
		selectedGetters = new String[] { "aVar" };
		selectedSetters = new String[] { "aVar" };
		assertRefactoringSuccess();
	}

}
