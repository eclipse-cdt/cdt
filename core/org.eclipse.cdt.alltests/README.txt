This plugin is a convenience for developers wanting to run all the junit3 tests 
in CDT from within the IDE with a single click. It contains a container test 
class and a launch configuration. If you're interested in running only a subset 
of the tests, simply comment out lines in AllTests.java.

Keep in mind some of the tests require a toolchain to be in PATH. So Windows
developers will need to have MinGW or Cygwin installed.