/**
 * TODO List
 * General
 * 	   [x] Bug in InsertionPoint: 'withinClass' attribute is determined by situation in
 * 		   source class. It must be determined by whether the node is to be inserted 
 *         within a class definition in the target class.
 *     [ ] PullUpHelper.findClass() should deal with multiple results by at least 
 *         reporting a refactoring error
 *         [ ] solved by only searching for declarations instead of definitions?
 * PullUp:
 *     [-] add possibility to declare method stubs in subclasses
 *     	   [-] fix problem with qualified names
 *     		   PullUpHelper now has a method which may not be fully correct
 *     		   but works on first simple examples
 *         [ ] preferably declare stubs in cpp file 
 *         [-] there is a problem when hitting 'back' in refactoring dialog. new settings
 *             made then are ignored
 *     [ ] add possibility to select which members should be removed from which classes
 *     [x] Create pure virtual declaration when 'declaring
 *         virtual'
 *     [ ] Declaring methods pure virtual that have a definition but no declaration does
 *     	   not work
 *     	   [ ] is this even allowed? 
 *     [ ] Misformatted warning string when pulling definition up into abstract base class
 * PushDown
 * 	   [ ] Check state shit in select target dialog
 *     [ ] If action is 'existing definition' and the source method has no definition,
 *     	   change the action to 'declare stub'
 *     [ ] Make push down to classes which already use the member mandatory 
 *     [ ] Check if removing the code from class breaks existing code
 *     [ ] Create pure virtual declaration when 'leaving virtual'
 *         [ ] Inspect call sites of members which are to be pushed down
 * 
 */
package org.eclipse.cdt.internal.ui.refactoring.pullup;