#include "arduinoFFT.h" // Standard Arduino FFT library

#define SAMPLES 256              //Must be a power of 2
#define SAMPLING_FREQUENCY 10000 //Hz, must be 10000 or less due to ADC conversion time. Determines maximum frequency that can be analysed by the FFT.
#define amplitude 50

byte peak[] = {0,0,0,0,0,0,0};
double vReal[SAMPLES];
double vImag[SAMPLES];
unsigned long newTime, oldTime;
unsigned int sampling_period_us;
unsigned long microseconds;

arduinoFFT FFT = arduinoFFT(vReal, vImag, SAMPLES, SAMPLING_FREQUENCY);

void setup() {
  
  Serial.begin(115200);
  sampling_period_us = round(1000000 * (1.0 / SAMPLING_FREQUENCY));

}

void loop() 
{
  for (int i = 0; i < SAMPLES; i++) {
    newTime = micros()-oldTime;
    oldTime = newTime;
    vReal[i] = analogRead(A0); // A conversion takes about 1mS on an ESP8266
    vImag[i] = 0;
    while (micros() < (newTime + sampling_period_us)) { /* do nothing to wait */ }
  }

  FFT.DCRemoval();
  FFT.Windowing(FFT_WIN_TYP_HAMMING, FFT_FORWARD);
  FFT.Compute(FFT_FORWARD);
  FFT.ComplexToMagnitude();

  double peak = FFT.MajorPeak(vReal, SAMPLES, SAMPLING_FREQUENCY);
  /*FFT.Windowing(vReal, SAMPLES, FFT_WIN_TYP_HAMMING, FFT_FORWARD);
  FFT.Compute(vReal, vImag, SAMPLES, FFT_FORWARD);
  FFT.ComplexToMagnitude(vReal, vImag, SAMPLES);*/

  unsigned int index  = (peak * SAMPLES) / (1.0 * SAMPLING_FREQUENCY);

  if(vReal[index] > 200) {
    Serial.print("peak: ");
    Serial.println(peak);
  }
  
  /*for (int i = 2; i < (SAMPLES/2); i++){ // Don't use sample 0 and only first SAMPLES/2 are usable. Each array eleement represents a frequency and its value the amplitude.
   //Serial.print((i * 1.0 * SAMPLING_FREQUENCY) / SAMPLES, 1);
   //Serial.print(" ");
   Serial.println(vReal[i]);//vReal[i]/amplitude

   /*
    if (i<=5 )             displayBand(0,(int)vReal[i]/amplitude); // 125Hz
      if (i >5   && i<=12 )  displayBand(1,(int)vReal[i]/amplitude); // 250Hz
      if (i >12  && i<=32 )  displayBand(2,(int)vReal[i]/amplitude); // 500Hz
      if (i >32  && i<=62 )  displayBand(3,(int)vReal[i]/amplitude); // 1000Hz
      if (i >62  && i<=105 ) displayBand(4,(int)vReal[i]/amplitude); // 2000Hz
      if (i >105 && i<=120 ) displayBand(5,(int)vReal[i]/amplitude); // 4000Hz
      if (i >120 && i<=146 ) displayBand(6,(int)vReal[i]/amplitude); // 8000Hz
    */
  //}
}
