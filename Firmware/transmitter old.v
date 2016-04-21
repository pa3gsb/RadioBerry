//------------------------------------------------------------------------------------------------------------------------------------------------------------
//                          Transmitter code 
//------------------------------------------------------------------------------------------------------------------------------------------------------------
/*  
  This program interfaces the AD9744 ADC.
  The data to the AD9744  is in 14 bit parallel format and 
  is sent at the negative edge of the (122.88MHz) clk_122.
  
  The 48kHz I and Q data from the host is interpolated by 2560 in a CIC filter to 
  give a data rate of 122.88MHz. The I and Q data is put in a txFIFO and filled by
  the host sending udp messages.
  
  The data is processed by a CORDIC NCO and passed to the AD9744 DAC. 
  
  Used penelope.v as base from the hpsdr project.
   
  Change log:
			14-01-2014 - First version. 
							Johan PA3GSB

*/

/* 
	The gain distribution of the transmitter code is as follows.
	Since the CIC interpolating filters do not interpolate by 2^n they have an overall loss.
	
	The overall gain in the interpolating filter is ((RM)^N)/R.  So in this case its 2560^4.
	This is normalised by dividing by ceil(log2(2560^4)).
	
	In which case the normalized gain would be (2560^4)/(2^46) = .6103515625
	
	The CORDIC has an overall gain of 1.647.
	
	Since the CORDIC takes 16 bit I & Q inputs but output needs to be truncated to 14 bits, in order to
	interface to the DAC, the gain is reduced by 1/4 to 0.41175
	
	We need to be able to drive to DAC to its full range in order to maximise the S/N ratio and 
	minimise the amount of PA gain.  We can increase the output of the CORDIC by multiplying it by 4.
	This is simply achieved by setting the CORDIC output width to 16 bits and assigning bits [13:0] to the DAC.
	
	The gain distripution is now:
	
	0.61 * 0.41174 * 4 = 1.00467 
	
	This means that the DAC output will wrap if a full range 16 bit I/Q signal is received. 
	This can be prevented by reducing the output of the CIC filter.
	
	If we subtract 1/128 of the CIC output from itself the level becomes
	
	1 - 1/128 = 0.9921875
	
	Hence the overall gain is now 
	
	0.61 * 0.9921875 * 0.41174 * 4 = 0.996798
*/
module transmitter(
	reset,
	clk,                  
	frequency,
	afTxFIFO, 
	afTxFIFOEmpty, 
	afTxFIFOReadStrobe,
	out_data,
	PTT,
	LED);

input wire reset;
input wire clk;
input [31:0] frequency;
input  wire [31:0]afTxFIFO; 	
input wire afTxFIFOEmpty;
output wire afTxFIFOReadStrobe;
output reg [13:0] out_data;
input wire PTT;
output wire LED;

//------------------------------------------------------------------------------------------------------------------------------------------------------------
//                          Reset
//------------------------------------------------------------------------------------------------------------------------------------------------------------
reg transmit_reset;
reg [10:0] transmit_reset_cnt;

always @(posedge clk)
begin
  if (!transmit_reset_cnt[10])
    transmit_reset_cnt <= transmit_reset_cnt + 1'b1;

  transmit_reset <= transmit_reset_cnt[10] ? 1'b0 : 1'b1;
end

//------------------------------------------------------------------------------------------------------------------------------------------------------------
//                          Read IQ data from txFIFO
//------------------------------------------------------------------------------------------------------------------------------------------------------------

wire pulse;
pulsegen pulse_inst (.sig(ce_out_i), .rst(transmit_reset), .clk(clk), .pulse(pulse)); 

assign afTxFIFOReadStrobe = !afTxFIFOEmpty ? pulse : 1'b0;

reg  [31:0] tx_IQ_data;
always @(posedge clk)
begin
	if (transmit_reset)
		tx_IQ_data <= 32'b0;

	if (afTxFIFOReadStrobe) 
		tx_IQ_data <= afTxFIFO;
end	

//------------------------------------------------------------------------------------------------------------------------------------------------------------
//                          Interpolating CIC filter  R = 2560  N = 5
//------------------------------------------------------------------------------------------------------------------------------------------------------------
reg signed [15:0]cic_i;
reg signed [15:0]cic_q;
wire ce_out_i;
wire ce_out_q;


// latch I&Q data on strobe from CIC filters
always @ (posedge clk)
begin 
	if (ce_out_i)
		cic_i = tx_IQ_data[15:0];
	if (ce_out_q)
		cic_q = tx_IQ_data[31:16];	
end 

wire signed [15:0] cic_out_i;
wire signed [15:0] cic_out_q;
wire signed [15:0] out_i;
wire signed [15:0] out_q; 

cicint cic_I(.clk(clk), .clk_enable(1'b1), .reset(transmit_reset), .filter_in(cic_i),
             .filter_out(cic_out_i), .ce_out(ce_out_i));
             
cicint cic_Q(.clk(clk), .clk_enable(1'b1), .reset(transmit_reset), .filter_in(cic_q),
             .filter_out(cic_out_q), .ce_out(ce_out_q));

// multiply CIC outputs by 0.9921875, >>> is the Veriloig arrithmetic shift right
// i.e. output  = input - input/128 = input * 0.9921875
assign out_i = cic_out_i  - (cic_out_i >>> 7);
assign out_q = cic_out_q  - (cic_out_q >>> 7);

//------------------------------------------------------------------------------------------------------------------------------------------------------------
//                         CORDIC NCO  Code rotates input at set frequency and produces I & Q 
//------------------------------------------------------------------------------------------------------------------------------------------------------------ 
wire [17:0] i_out; 
wire [17:0] q_out; 
wire [31:0] phase;
wire signed [14:0] cordic_i_out;

wire signed [14:0] gated;

assign gated = PTT ? cordic_i_out : 14'd0;


cpl_cordic #(.OUT_WIDTH(16)) cordic_inst (.clock(clk), 
										  .frequency(frequency), 
										  .in_data_I(out_q), .in_data_Q(out_i), 
										  .out_data_I(cordic_i_out), .out_data_Q());   // NOTE:  I and Q inputs reversed to give correct sideband out    

/* 
  We can use either the I or Q output from the CORDIC directly to drive the DAC.

    exp(jw) = cos(w) + j sin(w)

  When multplying two complex sinusoids f1 and f2, you get only f1+f2, no
  difference frequency.

      Z = exp(j*f1) * exp(j*f2) = exp(j*(f1+f2))
        = cos(f1 + f2) + j sin(f1 + f2)
*/
always @ (negedge clk) out_data[13:0] = gated[13:0];   
  
//------------------------------------------------------------------------------------------------------------------------------------------------------------
//                        Heartbeat (LED flashes twice as fast when PTT active)
//------------------------------------------------------------------------------------------------------------------------------------------------------------
reg[26:0]counter;
always @(posedge clk) counter = counter + 1'b1;
assign LED = PTT ? counter[24] : counter[26];  
 
endmodule