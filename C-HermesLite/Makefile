CC=gcc
CFLAGS = -g 
# uncomment this for SunOS
# LIBS = -lsocket -lnsl

LIBS    = -lbcm2835 -lwiringPi -lwiringPiDev -lpthread -lm

all: hermeslite

hermeslite: hermeslite.o 
	$(CC) -o hermeslite hermeslite.o $(LIBS)


hermeslite.o: hermeslite.c port.h

clean:
	rm -f hermeslite hermeslite.o hermeslite.o 
