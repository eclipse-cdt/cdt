#######################################
# The list of libraries we are building seperated by spaces.
# The 'lib_' indicates that these build products will be installed
# in the $(libdir) directory. For example /usr/lib
lib_LTLIBRARIES = $(libName).la

#######################################
# Build information for each library

# Sources for $(libName)
$(libName)_la_SOURCES = $(libName).c

# Linker options libTestProgram
$(libName)_la_LDFLAGS = 

# Compiler options. Here we are adding the include directory
# to be searched for headers included in the source code.
$(libName)_la_CPPFLAGS = -I$(top_srcdir)/include

