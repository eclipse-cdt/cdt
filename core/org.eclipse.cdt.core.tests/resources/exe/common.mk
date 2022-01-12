ifndef QCONFIG
QCONFIG=qconfig.mk
endif
include $(QCONFIG)
USEFILE=
LIBS+=socket
include $(MKFILES_ROOT)/qtargets.mk
