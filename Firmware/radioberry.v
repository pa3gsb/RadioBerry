// Project			: Radioberry
//
// Module			: Top level design radioberry.v
//
// Target Devices	: Cyclone III
//
// Tool 		 		: Quartus V12.1 Free WebEdition
//
//------------------------------------------------------------------------------------------------------------------------------------------------------------
// Description: 
//
//				Radioberry SDR firmware code.
//
//
// Johan Maas PA3GSB 
//
// Date:    27 December 2015
//				First version.
//------------------------------------------------------------------------------------------------------------------------------------------------------------

`include "timescale.v"

module radioberry(
clk_10mhz, 
ad9866_clk, ad9866_adio,ad9866_rxen,ad9866_rxclk,ad9866_txen,ad9866_txclk,ad9866_sclk,ad9866_sdio,ad9866_sdo,ad9866_sen_n,ad9866_rst_n,ad9866_mode,ad9866_pga,	
spi_sck, spi_mosi, spi_miso, spi_ce,   
DEBUG_LED1,DEBUG_LED2,DEBUG_LED3,DEBUG_LED4,
controlio);

input clk_10mhz;	
input ad9866_clk;
inout [11:0] ad9866_adio;
output ad9866_rxen;
output ad9866_rxclk;
output ad9866_txen;
output ad9866_txclk;
output ad9866_sclk;
output ad9866_sdio;
input  ad9866_sdo;
output ad9866_sen_n;
output ad9866_rst_n;
output ad9866_mode;
output [5:0] ad9866_pga;

// SPI connect to Raspberry PI SPI-0.
input spi_sck;
input spi_mosi; 
output spi_miso; 
input [1:0] spi_ce; 

output  wire  DEBUG_LED1;  
output  wire  DEBUG_LED2;  
output  wire  DEBUG_LED3;  
output  wire  DEBUG_LED4;  

output wire controlio;

// ADC vars...
wire adc_clock;		
reg [11:0]	adc;


//Debug	
//assign DEBUG_LED3 = 1'b0;	//Led off
//assign DEBUG_LED4 = 1'b1;	//Led on

assign DEBUG_LED3 = pll_locked;
assign DEBUG_LED4 = (rxfreq == 32'd4607000) ? 1'b1:1'b0;


//------------------------------------------------------------------------------------------------------------------------------------------------------------
//                         Receive Testing....compile time setup.
//
//		Testing ON: 
//			Testing mode is on; 	
//										The wire ad9866_present must be set to 0 (=false); 
//										Using the pllclock to set a clock as close to the 73.728 Mhz.
//										10 Mhz clock in divided by 25 and multiplied with 295 results in 73.750Mhz
//										This clock will be used iso of the ad9866 clock and also the ADC will be generated.
//										A pure CW signal with a frequency of around 4.609 Mhz will be received.
//		Testing OFF:
//			Testing mode is off; 
//										The wire ad9866_present must be set to 1 (=true); 
//										The AD9866 clock will be used and the actual ADC data will be used.

//------------------------------------------------------------------------------------------------------------------------------------------------------------
wire ad9866_present = 1'b0;
wire pll_locked;
wire test_ad9866_clk;

pllclock pllclock_inst(.inclk0(clk_10mhz), .c0(test_ad9866_clk), .locked(pll_locked));

assign controlio = adc_clock;

assign adc_clock = ad9866_present ? ad9866_clk : test_ad9866_clk;

reg [3:0] incnt;
always @ (posedge adc_clock)
  begin
	if (ad9866_present)
			adc <= ad9866_adio;
		else begin
			// Test sine wave
        case (incnt)
            4'h0 : adc = 12'h000;
            4'h1 : adc = 12'hfcb;
            4'h2 : adc = 12'hf9f;
            4'h3 : adc = 12'hf81;
            4'h4 : adc = 12'hf76;
            4'h5 : adc = 12'hf81;
            4'h6 : adc = 12'hf9f;
            4'h7 : adc = 12'hfcb;
            4'h8 : adc = 12'h000;
            4'h9 : adc = 12'h035;
            4'ha : adc = 12'h061;
            4'hb : adc = 12'h07f;
            4'hc : adc = 12'h08a;
            4'hd : adc = 12'h07f;
            4'he : adc = 12'h061;
            4'hf : adc = 12'h035;
        endcase
		end
		incnt <= incnt + 4'h1; 
	end

//------------------------------------------------------------------------------------------------------------------------------------------------------------
//                         Control
//------------------------------------------------------------------------------------------------------------------------------------------------------------
wire [5:0] speed;
// Decimation rates
localparam RATE48 = 6'd24;
localparam RATE96 = 6'd12;
localparam RATE192 = 6'd06;
localparam RATE384 = 6'd03;

localparam CICRATE = 6'd08;

assign 	speed =  RATE48;

assign ad9866_rxclk = ad9866_clk;
assign ad9866_txclk = ad9866_clk;

assign ad9866_txen = 'b0;
assign ad9866_rxen = 'b1;		// starting with rx mode...

assign ad9866_pga = 6'b011111;

wire [47:0] spi_recv;
wire spi_done;

always @ (negedge spi_sck)
begin
  if (reset)
		rxFIFOReadStrobe <= 1'b0;
  else	
		if (!rxFIFOEmpty && spi_done)
			rxFIFOReadStrobe <= 1'b1;
		else
			rxFIFOReadStrobe <= 1'b0;	
		
		if (spi_done)
			rxfreq <= spi_recv[31:0];
end
 
spi_slave spi_slave_inst(.rstb(!reset),.ten(1'b1),.tdata(rxDataFromFIFO),.mlb(1'b1),.ss(spi_ce[0]),.sck(spi_sck),.sdin(spi_mosi), .sdout(spi_miso),.done(spi_done),.rdata(spi_recv));


//------------------------------------------------------------------------------------------------------------------------------------------------------------
//                         Convert frequency to phase word 
//
//		Calculates  ratio = fo/fs = frequency/73.728Mhz where frequency is in MHz
//
//------------------------------------------------------------------------------------------------------------------------------------------------------------
wire   [31:0] sync_phase_word;
wire  [63:0] ratio;

reg[31:0] rxfreq;

localparam M2 = 32'd1954687338; 	// B57 = 2^57.   M2 = B57/CLK_FREQ = 73728000
localparam M3 = 32'd16777216;   	// M3 = 2^24, used to round the result
assign ratio = rxfreq * M2 + M3; 
assign sync_phase_word = ratio[56:25]; 

//------------------------------------------------------------------------------
//                           Software Reset Handler
//------------------------------------------------------------------------------
wire reset;
reset_handler reset_handler_inst(.clock(clk_10mhz), .reset(reset));

//------------------------------------------------------------------------------------------------------------------------------------------------------------
//                        Receiver module
//------------------------------------------------------------------------------------------------------------------------------------------------------------
wire	[23:0] rx_I;
wire	[23:0] rx_Q;
wire	rx_strobe;

receiver #(.CICRATE(CICRATE)) 
		receiver_inst(	.clock(adc_clock),
						.rate(speed), 
						.frequency(sync_phase_word),
						.out_strobe(rx_strobe),
						.in_data(adc),
						.out_data_I(rx_I),
						.out_data_Q(rx_Q));

//------------------------------------------------------------------------------------------------------------------------------------------------------------
//                          rxFIFO Handler (IQ Samples)
//------------------------------------------------------------------------------------------------------------------------------------------------------------
wire [47:0] rxDataFromFIFO;
wire rxFIFOEmpty;
reg rxFIFOReadStrobe;

rxFIFO rxFIFO_inst(	.aclr(reset),
							.wrclk(adc_clock),.data({rx_I, rx_Q}),.wrreq(rx_strobe),
							.rdclk(spi_sck),.q(rxDataFromFIFO),.rdreq(rxFIFOReadStrobe),.rdempty(rxFIFOEmpty));
						
						
//------------------------------------------------------------------------------
//                          Running...
//------------------------------------------------------------------------------
reg [26:0]counter;

always @(posedge clk_10mhz) 
begin
  if (reset)
    counter <= 26'b0;
  else
    counter <= counter + 1'b1;
end

assign {DEBUG_LED1,DEBUG_LED2} = counter[23:22];

endmodule