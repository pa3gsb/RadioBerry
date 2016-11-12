CC=gcc
LINK=gcc

OPTIONS=-g 

PROGRAM=radioberry

LIBS=-lrt -lm -lwdsp -lbcm2835 -lwiringPi -lwiringPiDev -lpthread

COMPILE=$(CC) $(OPTIONS) $(INCLUDES)

SOURCES= \
radio.c \
main.c \
frozen.c \
mongoose.c \
radioberry.c 

HEADERS= \
agc.h \
channel.h \
mode.h \
radio.h \
frozen.h \
mongoose.h 

OBJS= \
radio.o \
main.o \
frozen.o \
mongoose.o \
radioberry.o

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

