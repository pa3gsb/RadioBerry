/*
	  Radioberry protocol
	  
	  Working with pihpsdr.
	  
	  2016 Johan PA3GSB
*/

#include "audio.h"
#include <bcm2835.h>
#include <gtk/gtk.h>
#include <stdlib.h>
#include <stdio.h>

#include <pthread.h>

#include <string.h>
#include <errno.h>
#include <math.h>

#include "band.h"
#include "channel.h"
#include "discovered.h"
#include "mode.h"
#include "filter.h"
#include "old_protocol.h"
#include "radio.h"
#include "toolbar.h"

#define OUTPUT_BUFFER_SIZE 1024
#define SPEED_48K                 0x00
#define SPEED_96K                 0x01
#define SPEED_192K                0x02
#define SPEED_384K                0x03

static int output_buffer_size;
static int buffer_size=BUFFER_SIZE;

static int receiver;
static int display_width;

static int running;

static int samples=0;

static int sampleSpeed =0;

static double iqinputbuffer[BUFFER_SIZE*2];
static double micinputbuffer[BUFFER_SIZE*2];
static double audiooutputbuffer[BUFFER_SIZE*2];

unsigned char iqdata[6];

static pthread_t radioberry_thread_id;
static void start_radioberry_thread();
static void *radioberry_thread(void* arg);

static void setSampleSpeed();

struct timeval t0;
struct timeval t1;
float elapsed;

float timedifference_msec(struct timeval t0, struct timeval t1)
{
    return (t1.tv_sec - t0.tv_sec) * 1000.0f + (t1.tv_usec - t0.tv_usec) / 1000.0f;
}

void radioberry_protocol_init(int rx,int pixels) {
  int i;

  fprintf(stderr,"radioberry_protocol_init\n");
  receiver=rx;
  display_width=pixels;
 
	switch(sample_rate) {
	case 48000:
	  output_buffer_size=OUTPUT_BUFFER_SIZE;
	  break;
	case 96000:
	  output_buffer_size=OUTPUT_BUFFER_SIZE/2;
	  break;
	case 192000:
	  output_buffer_size=OUTPUT_BUFFER_SIZE/4;
	  break;
	case 384000:
	  output_buffer_size=OUTPUT_BUFFER_SIZE/8;
	  break;
	default:
	  fprintf(stderr,"Invalid sample rate: %d. Defaulting to 48K.\n",sample_rate);
	  break;
	}

	fprintf(stderr,"radioberry_protocol: buffer size: =%d\n", buffer_size);
  
  	if (!bcm2835_init()){
		fprintf(stderr,"radioberry_protocol: spi bus could not be initialized. \n");
		exit(-1);
	}
	
	bcm2835_gpio_fsel(RPI_BPLUS_GPIO_J8_33 , BCM2835_GPIO_FSEL_INPT);	
	bcm2835_gpio_fsel(RPI_BPLUS_GPIO_J8_40 , BCM2835_GPIO_FSEL_OUTP);
	bcm2835_gpio_write(RPI_BPLUS_GPIO_J8_40, LOW);	// ptt off

	bcm2835_spi_begin();
	bcm2835_spi_setBitOrder(BCM2835_SPI_BIT_ORDER_LSBFIRST);      
	bcm2835_spi_setDataMode(BCM2835_SPI_MODE3);                   
	bcm2835_spi_setClockDivider(BCM2835_SPI_CLOCK_DIVIDER_16); 
	bcm2835_spi_chipSelect(BCM2835_SPI_CS0);                      
	bcm2835_spi_setChipSelectPolarity(BCM2835_SPI_CS0, LOW); 
	 

	printf("init done \n");
  
	setSampleSpeed();

	audio_init();
	start_radioberry_thread();
}


static void start_radioberry_thread() {
  int rc;

  fprintf(stderr,"radioberry_protocol starting radioberry thread\n");

  rc=pthread_create(&radioberry_thread_id,NULL,radioberry_thread,NULL);
  if(rc != 0) {
    fprintf(stderr,"radioberry_protocol: pthread_create failed on radioberry_thread: rc=%d\n", rc);
    exit(-1);
  }
}

static void *radioberry_thread(void* arg) {
  unsigned char buffer[2048];
  fprintf(stderr, "radioberry_protocol: radioberry_thread\n");
  
  int left_sample;
  int right_sample;
  int mic_sample;
  float left_sample_float;
  float right_sample_float;
  samples = 0;
  running=1;
  int holdf =0;
  
  	int count =0;
	gettimeofday(&t0, 0);
	
	
  while(running) {
			while ( bcm2835_gpio_lev(RPI_BPLUS_GPIO_J8_33 ) == HIGH) {}; // wait till rxFIFO buffer is filled with at least one element //usleep(1000);

			if (holdf != ddsFrequency){
				fprintf(stderr,"radioberry_protocol: freq: =%d\n", ddsFrequency);
				holdf = ddsFrequency;
			}
			iqdata[0] = (sampleSpeed & 0x03);
			iqdata[1] = (((rx_random << 6) & 0x40) | ((rx_dither <<5) & 0x20) |  (attenuation & 0x1F));
			iqdata[2] = ((ddsFrequency >> 24) & 0xFF);
			iqdata[3] = ((ddsFrequency >> 16) & 0xFF);
			iqdata[4] = ((ddsFrequency >> 8) & 0xFF);
			iqdata[5] = (ddsFrequency & 0xFF);
					
			bcm2835_spi_transfern(iqdata, 6);
			//firmware: tdata(56'h00010203040506) -> 0-1-2-3-4-5-6 (element 0 contains 0; second element contains 1)
			
			left_sample   = (int)((signed char) iqdata[0]) << 16;
			left_sample  += (int)((unsigned char)iqdata[1]) << 8;
			left_sample  += (int)((unsigned char)iqdata[2]);
			right_sample  = (int)((signed char) iqdata[3]) << 16;
			right_sample += (int)((unsigned char)iqdata[4]) << 8;
			right_sample += (int)((unsigned char)iqdata[5]);
			
			left_sample_float=(float)left_sample/8388607.0; // 24 bit sample 2^23-1
			right_sample_float=(float)right_sample/8388607.0; // 24 bit sample 2^23-1
			iqinputbuffer[samples*2]=(double)left_sample_float;
			iqinputbuffer[(samples*2)+1]=(double)right_sample_float;
			samples++;
            
			if(samples==buffer_size) {
				//fprintf(stderr,"+");
				int error;
				// process the input
				fexchange0(CHANNEL_RX0, iqinputbuffer, audiooutputbuffer, &error);
				if(error!=0) {
					samples=0;
					fprintf(stderr,"fexchange2 (CHANNEL_RX0) returned error: %d\n", error);
				}
				
				audio_write(audiooutputbuffer,1024);
				
				Spectrum0(1, CHANNEL_RX0, 0, 0, iqinputbuffer);
				
				samples=0;
			}
			
			count ++;
			if (count == 48000) {
				count = 0;
				gettimeofday(&t1, 0);
				elapsed = timedifference_msec(t0, t1);
				printf("Code rx mode spi executed in %f milliseconds.\n", elapsed);
				gettimeofday(&t0, 0);
			}
  }
}

static void setSampleSpeed() {
     switch(sample_rate) {
        case 48000:
          sampleSpeed=SPEED_48K;
          break;
        case 96000:
          sampleSpeed=SPEED_96K;
          break;
        case 192000:
          sampleSpeed=SPEED_192K;
          break;
        case 384000:
          sampleSpeed=SPEED_384K;
          break;
      }
}

void radioberry_protocol_stop() {
  
	running=FALSE;

	bcm2835_spi_end();
	bcm2835_close();
}