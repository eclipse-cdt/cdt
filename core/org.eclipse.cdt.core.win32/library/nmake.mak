TARGET = ..\os\win32\x86\winreg.dll

OBJS = winreg.obj

CPPFLAGS = /nologo /I C:\Java\jdk1.5.0_06\include /I C:\Java\jdk1.5.0_06\include\win32 /DUNICODE

$(TARGET):	$(OBJS)
	link /nologo /dll /out:$(TARGET) $(OBJS) advapi32.lib user32.lib
