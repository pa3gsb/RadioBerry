/*!
 * frequencydisplay JavaScript Library v1.0
 *
 * Author: Johan Maas PA3GSB
 */
 
 var vfo = {};
 var rit = {};
 
 var display;
 var displayRIT;

vfo.display = function() {
		  display = new SegmentDisplay("vfo");
		  display.pattern         = "##.###.###";
		  display.displayAngle    = 6;
		  display.digitHeight     = 20;
		  display.digitWidth      = 14;
		  display.digitDistance   = 2.5;
		  display.segmentWidth    = 2;
		  display.segmentDistance = 0.3;
		  display.segmentCount    = 7;
		  display.cornerType      = 1;
		  display.colorOn         = "#00ff00";
		  display.colorOff        = "#000000";
		  display.draw();
};	  
		 
		  
rit.display = function() {
		  displayRIT = new SegmentDisplay("rit");
		  displayRIT.pattern         = "##.###.###";
		  displayRIT.displayAngle    = 6;
		  displayRIT.digitHeight     = 20;
		  displayRIT.digitWidth      = 14;
		  displayRIT.digitDistance   = 2.5;
		  displayRIT.segmentWidth    = 2;
		  displayRIT.segmentDistance = 0.3;
		  displayRIT.segmentCount    = 7;
		  displayRIT.cornerType      = 1;
		  displayRIT.colorOn         = "#00ff00";
		  displayRIT.colorOff        = "#000000";
		  displayRIT.draw();
};