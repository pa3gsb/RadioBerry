/*
	  Radioberry protocol
	  
	  
	  2016 Johan PA3GSB
*/

#include <bcm2835.h>
#include <stdlib.h>
#include <stdio.h>
#include <semaphore.h>
#include <pthread.h>

#include <string.h>
#include <errno.h>
#include <math.h>

#include "radioberry.h"
#include "audio.h"
#include "channel.h"
#include "mode.h"
#include "radio.h"

#define OUTPUT_BUFFER_SIZE 1024
#define SPEED_48K                 0x00
#define SPEED_96K                 0x01
#define SPEED_192K                0x02

#define BUFFER_SIZE 1024

#define TRUE 1;
#define FALSE 0;

double drive=50;

static int output_buffer_size;
static int buffer_size=BUFFER_SIZE;

static int running;

static int samples=0;
static int txsamples=0;

static short tx_sample;
static int left_tx_sample;
static int right_tx_sample;

static int left_rx_sample;
static int right_rx_sample;


static int sampleSpeed = 0x00;

static double iqinputbuffer[BUFFER_SIZE*2];
static double micinputbuffer[BUFFER_SIZE*2];
static double audiooutputbuffer[BUFFER_SIZE*2];
static double micoutputbuffer[BUFFER_SIZE*2];

unsigned char iqdata[6];
unsigned char tx_iqdata[6];

static pthread_t radioberry_thread_id;
static void start_radioberry_thread();
static void *radioberry_thread(void* arg);

static void setSampleSpeed();
static void handleReceiveStream();

struct timeval t0;
struct timeval t1;
struct timeval t20;
struct timeval t21;
float elapsed;

void spiWriter();
void spiReader();


int sample_rate = 48000;

double mic_gain;

double tune_drive;
double drive;
unsigned char drive_level;
unsigned char prev_drive_level;

double mic_gain=1.5;
double volume=0.99;
int rx_dither=0;
int rx_random=0;
int rx_preamp=0;
int attenuation=0;
long rx_frequency=1008000;

static int rxcount =0;
static int txcount =0;

int local_audio = 0;
int local_microphone = 0;

void setVolume(double volume){
	volume = volume;
}
void setRX_Dither(int dither){
	rx_dither = dither;
}
void setRX_Random(int random){
	rx_random = random;
}
void setRX_Attenuation(int attenuation){
	attenuation = attenuation;
}
void setRX_Frequency(int freq){
	rx_frequency = freq;
}

float timedifference_msec(struct timeval t0, struct timeval t1)
{
    return (t1.tv_sec - t0.tv_sec) * 1000.0f + (t1.tv_usec - t0.tv_usec) / 1000.0f;
}

int isTransmitting() {
  return FALSE;
}

void radioberry_protocol_init() {
	int i;

	fprintf(stderr,"radioberry_protocol_init\n");

	audio_get_cards(0);	//output devices
	audio_get_cards(1);	//input devices

	fprintf(stderr,"****sample rate: =%d\n", sample_rate);
	
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
	default:
	  fprintf(stderr,"Invalid sample rate: %d. Defaulting to 48K.\n",sample_rate);
	  output_buffer_size=OUTPUT_BUFFER_SIZE;
	  break;
	}

	fprintf(stderr,"radioberry_protocol: buffer size: =%d\n", buffer_size);
  
  	if (!bcm2835_init()){
		fprintf(stderr,"radioberry_protocol: spi bus could not be initialized. \n");
		exit(-1);
	}
	
	bcm2835_gpio_fsel(RPI_BPLUS_GPIO_J8_33 , BCM2835_GPIO_FSEL_INPT);	
	bcm2835_gpio_fsel(RPI_BPLUS_GPIO_J8_38, BCM2835_GPIO_FSEL_INPT);
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
	
	local_audio=1;
	if(audio_open_output()!=0) {
      fprintf(stderr,"audio_open_output failed\n");
      local_audio=0;
    }
	
	local_microphone = 1;
    if(audio_open_input()!=0) {
      fprintf(stderr,"audio_open_input failed\n");
      local_microphone=0;
    }

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
 
	samples = 0;
	txsamples=0;
	running=1;

	gettimeofday(&t20, 0);
	gettimeofday(&t0, 0);
	
	bcm2835_gpio_fsel(RPI_BPLUS_GPIO_J8_33 , BCM2835_GPIO_FSEL_INPT);	
	bcm2835_gpio_fsel(RPI_BPLUS_GPIO_J8_38 , BCM2835_GPIO_FSEL_INPT);	
	bcm2835_gpio_fsel(RPI_BPLUS_GPIO_J8_40 , BCM2835_GPIO_FSEL_OUTP);
	
	while(running) {
		if(isTransmitting()) {
			bcm2835_gpio_write(RPI_BPLUS_GPIO_J8_40, HIGH);	// ptt on
		}
		else 
		{
			bcm2835_gpio_write(RPI_BPLUS_GPIO_J8_40, LOW);	// ptt off
			spiReader();
			handleReceiveStream();
		}
	}
}

int correctSampleValue(int value) {

	if(value>32767) {
		value=32767;
    } else if(value<-32767) {
      value=-32767;
    }
	
	return value;
}

void *radioberry_protocol_process_local_mic(unsigned char *buffer,int le) {
int b;
int leftmicsample;
double leftmicsampledouble;
double gain;

if(isTransmitting()) {
    b=0;
    int i;
    for(i=0;i<1024;i++) {
		leftmicsample  = (int)((unsigned char)buffer[b++] & 0xFF);
		leftmicsample  |= (int)((signed char) buffer[b++]) << 8;
		//rightmicsample=leftmicsample;
		
		leftmicsampledouble=(double)leftmicsample/32767.0; // 16 bit sample 2^16-1
        micinputbuffer[i*2]=leftmicsampledouble*mic_gain;
        micinputbuffer[(i*2)+1]=leftmicsampledouble*mic_gain;
	}
	int error;
	fexchange0(CHANNEL_TX, micinputbuffer, micoutputbuffer, &error);
	if(error!=0) {
		fprintf(stderr,"fexchange0 (CHANNEL_TX) returned error: %d\n", error);
	}
	Spectrum0(1, CHANNEL_TX, 0, 0, micoutputbuffer);

	gain=65535.0*255.0/(double)drive;
	int j;
	for(j=0;j<1024;j++) { 
		left_tx_sample=(int)(micoutputbuffer[j*2]*gain);
		left_tx_sample = correctSampleValue(left_tx_sample);
		right_tx_sample=(int)(micoutputbuffer[(j*2)+1]*gain);
		right_tx_sample = correctSampleValue(right_tx_sample);

		tx_iqdata[0] = 0;
		tx_iqdata[1] = drive / 6.4;  // convert drive level from 0-255 to 0-39 )
		if (prev_drive_level != drive) {
			printf("drive level %d - corrected drive level %d \n", drive_level, tx_iqdata[1]);
			prev_drive_level = drive; 
		}		
		tx_iqdata[2] = left_tx_sample>>8; 
		tx_iqdata[3] = left_tx_sample;
		tx_iqdata[4] = right_tx_sample>>8;
		tx_iqdata[5] = right_tx_sample;

		spiWriter();
	}
}
}

static void handleReceiveStream() {
	int error;
	int left_sample;
	int right_sample;
	int mic_sample;
	float left_sample_float;
	float right_sample_float;
  
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
		//fprintf(stderr,".");
		// process the input
		fexchange0(CHANNEL_RX, iqinputbuffer, audiooutputbuffer, &error);
		if(error!=0) {
			samples=0;
			fprintf(stderr,"fexchange2 (CHANNEL_RX0) returned error: %d\n", error);
		}
		
		if(local_audio) {
			
			int j;
			for(j=0;j<output_buffer_size;j++) {
				left_rx_sample=(short)(audiooutputbuffer[j*2]*32767.0*volume); 
				right_rx_sample=(short)(audiooutputbuffer[(j*2)+1]*32767.0*volume);
				audio_write(left_rx_sample,right_rx_sample);
			}
         }
		
		Spectrum0(1, CHANNEL_RX, 0, 0, iqinputbuffer);
		samples=0;
	}
}

void setSampleSpeed() {
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
      }
}

void radioberry_protocol_stop() {
  
	running=FALSE;

	bcm2835_spi_end();
	bcm2835_close();
	
	audio_close_input();
	audio_close_output();
}

void spiReader() {
	while ( bcm2835_gpio_lev(RPI_BPLUS_GPIO_J8_33 ) == HIGH) {}; // wait till rxFIFO buffer is filled with at least one element

	iqdata[0] = (sampleSpeed & 0x03);
	iqdata[1] = (((rx_random << 6) & 0x40) | ((rx_dither <<5) & 0x20) |  (attenuation & 0x1F));
	iqdata[2] = ((rx_frequency >> 24) & 0xFF);
	iqdata[3] = ((rx_frequency >> 16) & 0xFF);
	iqdata[4] = ((rx_frequency >> 8) & 0xFF);
	iqdata[5] = (rx_frequency & 0xFF);
			
	bcm2835_spi_transfern(iqdata, 6);
	//firmware: tdata(56'h00010203040506) -> 0-1-2-3-4-5-6 (element 0 contains 0; second element contains 1)
	rxcount ++;
	if (rxcount == 48000) {
		rxcount = 0;
		gettimeofday(&t1, 0);
		elapsed = timedifference_msec(t0, t1);
		printf("Code rx mode spi executed in %f milliseconds.\n", elapsed);
		gettimeofday(&t0, 0);
	}
}

void spiWriter() {

	while ( bcm2835_gpio_lev(RPI_BPLUS_GPIO_J8_38 ) == HIGH) {};

	bcm2835_spi_transfern(tx_iqdata, 6);
	
	txcount ++;
	if (txcount == 48000) {
		txcount = 0;
		gettimeofday(&t21, 0);
		float elapsd = timedifference_msec(t20, t21);
		printf("Code tx mode spi executed in %f milliseconds.\n", elapsd);
		gettimeofday(&t20, 0);
	}
}

