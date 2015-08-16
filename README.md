# RadioBerry
Ham Radio cape for Raspberry PI

Radioberry is a seperate thread within the Hermes-Lite project.

Main purpose of the sub-project:

- ham radio for everyone
- learning; from noob to guru 


Radioberry build up by:

- Raspberry PI
- Radio extension board (cape)

Multiple configurations:

-) Config A
PC powersdr  discovers 	Radioberry which implements hpsdr protocol.

-) Config B
Browser connects via html to Radioberry which implements webserver including dsp and radio control.
Using websockets, webaudio driver, html5, css3, jquery.

-) Config C
Using LVDS ; bringing the raw ADC data to the RPI.


Planning to make one extension board which makes experimenting for config A, B and C possible.

For the config A and B i want to use the java programming language.


