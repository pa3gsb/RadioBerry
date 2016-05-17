using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace CSharp_NoiseFloor
{
    class SDRCATCommand
    {

        public String setFrequency(int freq)
        {
            String freq11 = String.Format("{0:00000000000}", freq);

            return "ZZFA" + freq11 + ";";    //"ZZFA00014320151;"
        }

        public String setAGCSlow()
        {
            return "ZZGT2;";        
        }

        public String setCWLMode()
        {
            return "ZZMD03;";       
        }

        public String setRXMeterToSignalAverage()
        {
            return "ZZMR1;";
        }

        public String getRxMeter()
        {
            return "ZZRM1;";
        }

    }
}
