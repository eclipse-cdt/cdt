Usage: 
By default, torture testing is disabled. To enable it, create a 'TortureTest.properties' in 'org.eclipse.cdt.ui.tests\parser\org\eclipse\cdt\core\parser\resources'. 

If you don't have GCC testsuites, it does nothing. Then go and grab your latest version of GCC testsuites 
(for instance, ftp://ftp.fu-berlin.de/unix/gnu/gcc/gcc-3.3/gcc-testsuite-3.3.tar.gz). 
Unpack testsuites under 
 
    org.eclipse.cdt.core.tests/resources/parser/TortureTest/default
 
or elsewhere, but then you'll need to create a 'TortureTest.properties'. 
That's it, you can run TortureTest in JUnit Plugin mode. Don't run all ui.tests with torture-test enabled, as apparently it is included several times (anyone knows why?)
, and it's A LOT of test cases.

You can copy the rest of the file to create a TortureTest.properties and uncomment out/edit the default values as specified here.  

# By default, torture testing is disabled
# Uncomment to enable
#enabled=true

# Default location is org.eclipse.cdt.core.tests/resources/parser/TortureTest/default
#source=/your/gcc/testsuite/installation/directory

# Chunks for reading files
#stepSize=25000

# Timeout for individual cases, ms
# Need a large enough value, as some files are non-trivial
#timeOut=60000

# Quick parse, or not
#quickParse=true
