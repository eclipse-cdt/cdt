ifndef QCONFIG
QCONFIG=qconfig.mk
endif
include $(QCONFIG)

NAME=spawner

include $(MKFILES_ROOT)/qtargets.mk

ifeq ($(OS),nto)
ifeq ($(IVEHOME),)
IVEHOME:=/opt/vame/ive/bin
endif
EXTRA_INCVPATH+=$(IVEHOME)/include
endif

