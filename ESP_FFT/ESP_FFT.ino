#include "arduinoFFT.h" // Standard Arduino FFT library

#define SAMPLES 128              //Must be a power of 2
#define SAMPLING_FREQUENCY 10000 //Hz, must be 10000 or less due to ADC conversion time. Determines maximum frequency that can be analysed by the FFT.
#define peckValue 250

#define analogPin A0

byte peak[] = {0,0,0,0,0,0,0};
double vReal[SAMPLES];
double vImag[SAMPLES];

arduinoFFT FFT = arduinoFFT(vReal, vImag, SAMPLES, SAMPLING_FREQUENCY);

unsigned long newTime = 0;
unsigned long oldTime = 0;
unsigned int sampling_period_us = 0;
unsigned long microseconds = 0;

// variables:
int sensorValue = 0;   // the sensor value
int sensorMin = 4096;  // minimum sensor value
int sensorMax = 0;     // maximum sensor value

void setup() 
{
  Serial.begin(115200);
  Serial.println("initilization...");
  //attachInterrupt(analogPin, ADC_Interrupt_Rutine, CHANGE);
  sampling_period_us = round(1000000 * (1.0 / SAMPLING_FREQUENCY));

  /*newTime = millis();
  Serial.print("Sensor calibration value: ");

  while (millis() < (newTime + 5000)) {
    sensorValue = analogRead(analogPin);

    // record the maximum sensor value
    if (sensorValue > sensorMax) {
      sensorMax = sensorValue;
    }

    // record the minimum sensor value
    if (sensorValue < sensorMin) {
      sensorMin = sensorValue;
    }
  }

  Serial.print("sensorMin: ");
  Serial.print(sensorMin);
  Serial.print(" sensorMax: ");
  Serial.println(sensorMax);*/
}


void loop() 
{
  
  for (int i = 0; i < SAMPLES; i++) {
    newTime = micros()-oldTime;
    oldTime = newTime;

    // in case the sensor value is outside the range seen during calibration
    sensorValue = analogRead(analogPin);
    
    vReal[i] =  sensorValue;
    vImag[i] = 0;

    while (micros() < (newTime + sampling_period_us)) { /* do nothing to wait */ }
  }



  FFT.DCRemoval();
  /*FFT.Windowing(FFT_WIN_TYP_HAMMING, FFT_FORWARD);
  FFT.Compute(FFT_FORWARD);
  FFT.ComplexToMagnitude();*/
  FFT.Windowing(vReal, SAMPLES, FFT_WIN_TYP_HAMMING, FFT_FORWARD);
  FFT.Compute(vReal, vImag, SAMPLES, FFT_FORWARD);
  FFT.ComplexToMagnitude(vReal, vImag, SAMPLES);

  double peak = FFT.MajorPeak(vReal, SAMPLES, SAMPLING_FREQUENCY);
  

  unsigned int index  = (peak * SAMPLES) / (1.0 * SAMPLING_FREQUENCY);

  if(vReal[index] > peckValue) {
    String s = "f:"+String(peak);
    Serial.println(s);
  
    //attachInterrupt(analogPin, ADC_Interrupt_Rutine, CHANGE);
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
