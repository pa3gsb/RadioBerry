/*
      Hermes Lite 
	  
	  Radioberry implementation
	  
	  2016 Johan PA3GSB
*/

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <netdb.h>
#include <sys/socket.h>
#include <arpa/inet.h>
#include <math.h>
#include <semaphore.h>

#include <bcm2835.h>


void runHermesLite(void);
void sendPacket(void);
void handlePacket(char* buffer);
void readPackets(void);
void fillDiscoveryReplyMessage(void);
int isValidFrame(char* data);
void fillPacketToSend(void);
void init(void);
void generateIQ(void);
void put(unsigned char  value);
unsigned char get(void);
void *spiReader(void *arg);
void *packetreader(void *arg);


double timebase = 0.0;

	double amplitude;
	double noiseAmplitude;
	int vfo = 14250000;

sem_t empty;
sem_t full;
#define MAX 1800   
//115200
int buffer[MAX];
int fill = 0; 
int use  = 0;

static const int CHANNEL = 0;
int fdspi;
unsigned char iqdata[6];

#define SERVICE_PORT	1024

int nrx = 1; // n Receivers

int holdfreq = 0;
int freq = 4706000;

unsigned char SYNC = 0x7F;
int last_sequence_number = 0;

unsigned char hpsdrdata[1032];
unsigned char broadcastReply[60];
#define TIMEOUT_MS      100     

int running = 0;
int fd;									/* our socket */

struct sockaddr_in myaddr;				/* our address */
struct sockaddr_in remaddr;				/* remote address */
socklen_t addrlen = sizeof(remaddr);	/* length of addresses */
int recvlen;							/* # bytes received */

struct timeval t0;
	struct timeval t1;
	struct timeval t10;
	struct timeval t11;
	float elapsed;

float timedifference_msec(struct timeval t0, struct timeval t1)
{
    return (t1.tv_sec - t0.tv_sec) * 1000.0f + (t1.tv_usec - t0.tv_usec) / 1000.0f;
}

int main(int argc, char **argv)
{
	sem_init(&empty, 0, MAX); 
    sem_init(&full, 0, 0);    
	
	if (!bcm2835_init()){
		printf("init done failed \n");
        return 1;
	}
	
	printf("init done \n");
	
	pthread_t pid, pid2;
    pthread_create(&pid, NULL, spiReader, NULL);  
	pthread_create(&pid2, NULL, packetreader, NULL); 


	/* create a UDP socket */
	if ((fd = socket(AF_INET, SOCK_DGRAM, 0)) < 0) {
		perror("cannot create socket\n");
		return 0;
	}
	struct timeval timeout;      
    timeout.tv_sec = 0;
    timeout.tv_usec = TIMEOUT_MS;

	if (setsockopt(fd, SOL_SOCKET, SO_RCVTIMEO,(char*)&timeout,sizeof(timeout)) < 0)
		perror("setsockopt failed\n");
		
	/* bind the socket to any valid IP address and a specific port */
	memset((char *)&myaddr, 0, sizeof(myaddr));
	myaddr.sin_family = AF_INET;
	myaddr.sin_addr.s_addr = htonl(INADDR_ANY);
	myaddr.sin_port = htons(SERVICE_PORT);

	if (bind(fd, (struct sockaddr *)&myaddr, sizeof(myaddr)) < 0) {
		perror("bind failed");
		return 0;
	}
	runHermesLite();
	
	bcm2835_spi_end();
    bcm2835_close();
}

void runHermesLite() {
	printf("runHermesLite \n");
	
	int count = 0;
	gettimeofday(&t10, 0);
	for (;;) {
		
		if (running) {
			sendPacket();
			count ++;
			if (count == 762) {
				//usleep(10 * 1000);
				count = 0;
				gettimeofday(&t11, 0);
				elapsed = timedifference_msec(t10, t11);

				printf("Code packets executed in %f milliseconds.\n", elapsed);
				gettimeofday(&t10, 0);
			}
		}
	}
}
void *packetreader(void *arg) {
	while(1) {
		readPackets();
	}

}

void readPackets() {

	unsigned char buffer[2048];
	recvlen = recvfrom(fd, buffer, sizeof(buffer), 0, (struct sockaddr *)&remaddr, &addrlen);
	
	if (recvlen > 0) 
		handlePacket(buffer);
	
}

void handlePacket(char* buffer){

	if (buffer[2] == 2) {
		printf("Discovery packet received \n");
		printf("IP-address %d.%d.%d.%d  \n", 
							remaddr.sin_addr.s_addr&0xFF,
                            (remaddr.sin_addr.s_addr>>8)&0xFF,
                            (remaddr.sin_addr.s_addr>>16)&0xFF,
                            (remaddr.sin_addr.s_addr>>24)&0xFF);
		printf("Port %d \n", remaddr.sin_port);
		
		fillDiscoveryReplyMessage();
		
		if (sendto(fd, broadcastReply, sizeof(broadcastReply), 0, (struct sockaddr *)&remaddr, addrlen) < 0)
			printf("error sendto");
		
	} else if (buffer[2] == 4) {
			if (buffer[3] == 1 || buffer[3] == 3) {
				printf("Port %d \n", remaddr.sin_port);
				running = 1;
				printf("SDR Program sends Start command \n");
				return;
			} else {
				running = 0;
				last_sequence_number = 0;
				printf("SDR Program sends Stop command \n");
				return;
			}
		}
	if (isValidFrame(buffer)) {
			
		if ((buffer[11] & 0xFE)  == 0x00) {
			nrx = (((buffer[11 + 4] & 0x38) >> 3) + 1);
		}
		
		if ((buffer[523] & 0xFE)  == 0x00) {
			nrx = (((buffer[523 + 4] & 0x38) >> 3) + 1);
		}
		
		// select Command
		if ((buffer[11] & 0xFE) == 0x04)
        {
            freq = ((buffer[11 + 1] & 0xFF) << 24) + ((buffer[11+ 2] & 0xFF) << 16)
                    + ((buffer[11 + 3] & 0xFF) << 8) + (buffer[11 + 4] & 0xFF);
        }
        if ((buffer[523] & 0xFE) == 0x04)
        {
            freq = ((buffer[523 + 1] & 0xFF) << 24) + ((buffer[523+ 2] & 0xFF) << 16)
                    + ((buffer[523 + 3] & 0xFF) << 8) + (buffer[523 + 4] & 0xFF);
        }
	
	
		if (holdfreq != freq) {
			holdfreq = freq;
			printf("frequency %d en aantal rx %d \n", freq, nrx);
		}
	
	}
	
}

void sendPacket() {
	fillPacketToSend();
	
	if (sendto(fd, hpsdrdata, sizeof(hpsdrdata), 0, (struct sockaddr *)&remaddr, addrlen) < 0)
			printf("error sendto");
}


int isValidFrame(char* data) {
	return (data[8] == SYNC && data[9] == SYNC && data[10] == SYNC && data[520] == SYNC && data[521] == SYNC && data[522] == SYNC);
}

static int started=0;
void fillPacketToSend() {
		
		hpsdrdata[0] = 0xEF;
		hpsdrdata[1] = 0xFE;
		hpsdrdata[2] = 0x01;
		hpsdrdata[3] = 0x06;
		hpsdrdata[4] = ((last_sequence_number >> 24) & 0xFF);
		hpsdrdata[5] = ((last_sequence_number >> 16) & 0xFF);
		hpsdrdata[6] = ((last_sequence_number >> 8) & 0xFF);
		hpsdrdata[7] = (last_sequence_number & 0xFF);
		last_sequence_number++;

		int factor = (nrx - 1) * 6;
		int index;
		int frame = 0;
		for (frame; frame < 2; frame++) {
			int coarse_pointer = frame * 512; // 512 bytes total in each frame
			hpsdrdata[8 + coarse_pointer] = SYNC;
			hpsdrdata[9 + coarse_pointer] = SYNC;
			hpsdrdata[10 + coarse_pointer] = SYNC;
			hpsdrdata[11 + coarse_pointer] = 0x00; // c0
			hpsdrdata[12 + coarse_pointer] = 0x00; // c1
			hpsdrdata[13 + coarse_pointer] = 0x00; // c2
			hpsdrdata[14 + coarse_pointer] = 0x00; // c3
			hpsdrdata[15 + coarse_pointer] = 0x1D; // c4 //v2.9

			int j = 0;
			for (j; j < (504 / (8 + factor)); j++) {
				index = 16 + coarse_pointer + (j * (8 + factor));

				// Only (for now) supporting receiving mode....
				sem_wait(&full);            
				int i =0;
				for (i; i< 6; i++){
					hpsdrdata[index + i] = get(); // MSB comes first!!!!
				}
				sem_post(&empty);  
				
				
			}
		}
}

void fillDiscoveryReplyMessage() {
	int i = 0;
	for (i; i < 60; i++) {
		broadcastReply[i] = 0x00;
	}
	i = 0;
	broadcastReply[i++] = 0xEF;
	broadcastReply[i++] = 0xFE;
	broadcastReply[i++] = 0x02;

	broadcastReply[i++] =  0x00; // MAC
	broadcastReply[i++] =  0x01;
	broadcastReply[i++] =  0x02;
	broadcastReply[i++] =  0x03;
	broadcastReply[i++] =  0x04;
	broadcastReply[i++] =  0x05;
	broadcastReply[i++] =  29;
	broadcastReply[i++] =  1; // Hermes boardtype public static final
									// int DEVICE_HERMES_LITE = 6;
}

void *spiReader(void *arg) {
	if (!bcm2835_init()){
		printf("init done failed \n");
	}
	
	printf("init done \n");
	
	bcm2835_spi_begin();
    bcm2835_spi_setBitOrder(BCM2835_SPI_BIT_ORDER_LSBFIRST);      
    bcm2835_spi_setDataMode(BCM2835_SPI_MODE3);                   
    bcm2835_spi_setClockDivider(BCM2835_SPI_CLOCK_DIVIDER_8); 
    bcm2835_spi_chipSelect(BCM2835_SPI_CS0);                      
    bcm2835_spi_setChipSelectPolarity(BCM2835_SPI_CS0, LOW);      
	
	
	printf("versie %d  \n", bcm2835_version());
	
	
	bcm2835_gpio_fsel(RPI_BPLUS_GPIO_J8_33 , BCM2835_GPIO_FSEL_INPT);		
	
	int count =0;
	gettimeofday(&t0, 0);
	while(1) {
		iqdata[0] = 0;
		iqdata[1] = 0;
		iqdata[2] = ((freq >> 24) & 0xFF);
		iqdata[3] = ((freq >> 16) & 0xFF);
		iqdata[4] = ((freq >> 8) & 0xFF);
		iqdata[5] = (freq & 0xFF);
				
		bcm2835_spi_transfern(iqdata, 6);
		while ( bcm2835_gpio_lev(RPI_BPLUS_GPIO_J8_33 ) == LOW) 
		{
			iqdata[0] = 0;
			iqdata[1] = 0;
			iqdata[2] = ((freq >> 24) & 0xFF);
			iqdata[3] = ((freq >> 16) & 0xFF);
			iqdata[4] = ((freq >> 8) & 0xFF);
			iqdata[5] = (freq & 0xFF);
			bcm2835_spi_transfern(iqdata, 6);
		};
		
		sem_wait(&empty);
		int i =0;
		for (i; i< 6; i++){
				put(iqdata[i]);
		}
		sem_post(&full);
		
		count ++;
		if (count == 48000) {
			count = 0;
			gettimeofday(&t1, 0);
			elapsed = timedifference_msec(t0, t1);
			printf("Code spi executed in %f milliseconds.\n", elapsed);
			gettimeofday(&t0, 0);
		}
		
	}
	bcm2835_spi_end();
    bcm2835_close();
}

void put(unsigned char  value) {
    buffer[fill] = value;    
    fill = (fill + 1) % MAX; 
}

unsigned char get() {
    int tmp = buffer[use];   
    use = (use + 1) % MAX;   
    return tmp;
}
/*
void init(){
		int amplitudeDB = -73;
		amplitude = 1 / pow(sqrt(10.0), - (amplitudeDB / 10.0));
		
		int amplitudeNoiseDB = -90;
		noiseAmplitude = 1 / pow(sqrt(10), - (amplitudeNoiseDB / 10));

		timebase = 0.0;
}

void generateIQ() {

		int f1 = (int) freq - vfo;

		int idx = 0;

		if (f1 > 24000 || f1 < -24000) {
			amplitude = 0;
		} else
			amplitude = 1 / pow(sqrt(10.0), - (-73 / 10.0));
			
			
			double angle1 = f1 * 2 * 3.14 * timebase;
			float inlsamples =  0.0; //(noiseAmplitude * (2 * random() - 1) * 0x7fffff);
			float inrsamples =  0.0; //(noiseAmplitude * (2 * random() - 1) * 0x7fffff);
			inlsamples +=  (sin(angle1) * 0x7fffff * amplitude);
			inrsamples +=  (cos(angle1) * 0x7fffff * amplitude);
			int q = round(inrsamples); // * 0x7fffff
			int i = round(inlsamples);// * 0x7fffff

			iqdata[0] = (((i >> 16) & 0xff));
			iqdata[1] = (((i >> 8) & 0xff));
			iqdata[2] = ((i & 0xff));
			iqdata[3] = ( ((q >> 16) & 0xff));
			iqdata[4] = ( ((q >> 8) & 0xff));
			iqdata[5] = ((q & 0xff));
			
			timebase += (1.0/ 48000);
}
*/
