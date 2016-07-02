CC=gcc
LINK=gcc

OPTIONS=-g 

LIBS=-lrt -lm -lbcm2835 -lwiringPi -lwiringPiDev -lpthread -lpulse-simple -lpulse

COMPILE=$(CC) $(OPTIONS) $(INCLUDES)

PROGRAM=hermeslite

SOURCES= \
hermeslite.c \
audio.c 

HEADERS= \
port.h \
audio.h


OBJS= \
hermeslite.o \
audio.o 

all: prebuild $(PROGRAM) $(HEADERS) $(SOURCES) 

prebuild:
	rm -f version.o

$(PROGRAM): $(OBJS) 
	$(LINK) -o $(PROGRAM) $(OBJS) $(LIBS)

.c.o:
	$(COMPILE) -c -o $@ $<


clean:
	-rm -f *.o
	-rm -f $(PROGRAM)

