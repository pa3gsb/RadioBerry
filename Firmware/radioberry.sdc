
set_time_format -unit ns -decimal_places 3

create_clock -name ad9866_clk -period 73.728MHz	[get_ports ad9866_clk] 
create_clock -name clk_10mhz -period 10.000MHz [get_ports clk_10mhz] 
create_clock -name spi_sck -period 15.625MHz [get_ports spi_sck] 
create_clock -name spi_ce0 -period 0.400MHz [get_ports {spi_ce[0]}]
create_clock -name spi_ce1 -period 0.400MHz [get_ports {spi_ce[1]}]

create_clock -name {spi_slave:spi_slave_rx2_inst|done} -period 0.400MHz [get_registers {spi_slave:spi_slave_rx2_inst|done}]
create_clock -name {spi_slave:spi_slave_rx_inst|done} -period 0.400MHz [get_registers {spi_slave:spi_slave_rx_inst|done}]


derive_pll_clocks

derive_clock_uncertainty

set_clock_groups -asynchronous \
						-group {ad9866_clk } \
						-group {	clk_10mhz } \
						-group {	spi_ce0 } \
						-group {	spi_ce1 } \
						-group {	spi_slave:spi_slave_rx_inst|done } \
						-group {	spi_slave:spi_slave_rx2_inst|done }
					
				

#*************************************************************************************************************
# set input delay
#*************************************************************************************************************
set_input_delay 1.000 -clock ad9866_clk {ad9866_adio[*] ad9866_sdo spi_sck spi_mosi}

#*************************************************************************************************************
# set output delay
#*************************************************************************************************************
set_output_delay 1.000 -clock ad9866_clk {ad9866_adio[*] spi_miso rx1_FIFOEmpty rx2_FIFOEmpty txFIFOFull ad9866_rxclk ad9866_txclk ad9866_sclk ad9866_sdio}

set_max_delay -from spi_slave:spi_slave_rx2_inst|treg[47] -to spi_miso 15
set_max_delay -from spi_slave:spi_slave_rx_inst|treg[47]	-to spi_miso 14

set_max_delay -from transmitter:transmitter_inst|out_data[*]	-to ad9866_adio[*] 12
set_max_delay -from spi_ce[0]	-to spi_slave:spi_slave_rx2_inst|treg[*] 3
set_max_delay -from spi_ce[0]	-to spi_slave:spi_slave_rx_inst|treg[*] 3
set_max_delay -from spi_ce[1]	-to spi_slave:spi_slave_rx2_inst|treg[*] 3

set_max_delay -from spi_mosi	-to spi_slave:spi_slave_rx_inst|rdata[0] 2
set_max_delay -from spi_mosi 	-to spi_slave:spi_slave_rx2_inst|rdata[0] 2
set_max_delay -from spi_mosi 	-to spi_slave:spi_slave_rx_inst|rreg[0] 2
set_max_delay -from spi_mosi 	-to spi_slave:spi_slave_rx2_inst|rreg[0] 2


set_max_delay -from rxFIFO:rxFIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[46] -to spi_slave:spi_slave_rx_inst|treg[46] 3
set_max_delay -from rxFIFO:rxFIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[47] -to spi_slave:spi_slave_rx_inst|treg[47] 3
set_max_delay -from rxFIFO:rxFIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[11] -to spi_slave:spi_slave_rx_inst|treg[11] 3
set_max_delay -from rxFIFO:rxFIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[4]  -to spi_slave:spi_slave_rx_inst|treg[4] 3
set_max_delay -from rxFIFO:rxFIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[22] -to spi_slave:spi_slave_rx_inst|treg[22] 3
set_max_delay -from rxFIFO:rxFIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[21] -to spi_slave:spi_slave_rx_inst|treg[21] 3
set_max_delay -from rxFIFO:rxFIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[20] -to spi_slave:spi_slave_rx_inst|treg[20] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[44] -to spi_slave:spi_slave_rx2_inst|treg[44] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[24] -to spi_slave:spi_slave_rx2_inst|treg[24] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[26] -to spi_slave:spi_slave_rx2_inst|treg[26] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[23] -to spi_slave:spi_slave_rx2_inst|treg[23] 3
set_max_delay -from rxFIFO:rxFIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[23] -to spi_slave:spi_slave_rx_inst|treg[23] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[25] -to spi_slave:spi_slave_rx2_inst|treg[25] 3
set_max_delay -from rxFIFO:rxFIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[1] -to spi_slave:spi_slave_rx_inst|treg[4] 3

set_max_delay -from spi_slave:spi_slave_rx_inst|rdata[22] -to txFIFO:txFIFO_inst|dcfifo:dcfifo_component|dcfifo_jek1:auto_generated|altsyncram_3l31:fifo_ram|ram_block3a20~porta_datain_reg0	3
set_max_delay -from spi_slave:spi_slave_rx_inst|rdata[4]	-to txFIFO:txFIFO_inst|dcfifo:dcfifo_component|dcfifo_jek1:auto_generated|altsyncram_3l31:fifo_ram|ram_block3a4~porta_datain_reg0 3
set_max_delay -from rxFIFO:rxFIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[1] -to spi_slave:spi_slave_rx_inst|treg[1] 3
set_max_delay -from rxFIFO:rxFIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[37]	-to spi_slave:spi_slave_rx_inst|treg[37] 3
set_max_delay -from rxFIFO:rxFIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[41]	-to spi_slave:spi_slave_rx_inst|treg[41] 3
set_max_delay -from rxFIFO:rxFIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[42]	-to spi_slave:spi_slave_rx_inst|treg[42] 3
set_max_delay -from rxFIFO:rxFIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[43]	-to spi_slave:spi_slave_rx_inst|treg[43] 3

set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[10]	-to spi_slave:spi_slave_rx2_inst|treg[10] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[12]	-to spi_slave:spi_slave_rx2_inst|treg[12] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[13]	-to spi_slave:spi_slave_rx2_inst|treg[13] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[14]	-to spi_slave:spi_slave_rx2_inst|treg[14] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[15]	-to spi_slave:spi_slave_rx2_inst|treg[15] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[16]	-to spi_slave:spi_slave_rx2_inst|treg[16] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[19]	-to spi_slave:spi_slave_rx2_inst|treg[19] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[21]	-to spi_slave:spi_slave_rx2_inst|treg[21] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[28]	-to spi_slave:spi_slave_rx2_inst|treg[28] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[45]	-to spi_slave:spi_slave_rx2_inst|treg[45] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[46]	-to spi_slave:spi_slave_rx2_inst|treg[46] 3

set_max_delay -from spi_slave:spi_slave_rx_inst|rdata[16] -to txFIFO:txFIFO_inst|dcfifo:dcfifo_component|dcfifo_jek1:auto_generated|altsyncram_3l31:fifo_ram|ram_block3a16~porta_datain_reg0 3
set_max_delay -from spi_slave:spi_slave_rx_inst|rdata[18] -to txFIFO:txFIFO_inst|dcfifo:dcfifo_component|dcfifo_jek1:auto_generated|altsyncram_3l31:fifo_ram|ram_block3a16~porta_datain_reg0 3

set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[30] -to spi_slave:spi_slave_rx2_inst|treg[30] 3

set_max_delay -from reset_handler:reset_handler_inst|reset	-to spi_slave:spi_slave_rx_inst|rreg[20] 5
set_max_delay -from reset_handler:reset_handler_inst|reset	-to spi_slave:spi_slave_rx_inst|rreg[38] 5
set_max_delay -from reset_handler:reset_handler_inst|reset	-to spi_slave:spi_slave_rx_inst|rreg[39] 5	

set_max_delay -from rxFIFO:rxFIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[*] -to spi_slave:spi_slave_rx_inst|treg[*] 3
set_max_delay -from rxFIFO:rx2_FIFO_inst|dcfifo:dcfifo_component|dcfifo_bqj1:auto_generated|altsyncram_hl31:fifo_ram|q_b[*] -to spi_slave:spi_slave_rx2_inst|treg[*] 3
#*************************************************************************************************************
# Set False Path
#*************************************************************************************************************
# don't need fast paths to the LEDs and adhoc outputs so set false paths so Timing will be ignored
set_false_path -from * -to { DEBUG_LED* ptt_out filter[*]  ad9866_mode ad9866_pga[*] ad9866_rst_n ad9866_sen_n ad9866_rxen ad9866_txen}

#don't need fast paths from the following inputs
set_false_path -from {ptt_in} -to *

set_false_path -from reset_handler:reset_handler_inst|reset -to *


