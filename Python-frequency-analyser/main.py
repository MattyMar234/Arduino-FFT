import pyaudio
import struct
import numpy as np
import matplotlib.pyplot as plt
from matplotlib.animation import FuncAnimation
import librosa
import scipy.fftpack
import queue
import os
import wave
import time

NUM_HARMONICS = 4  # Numero di armonici da considerare nell'HPS

QUEUE_SIZE = 20  # Numero di chunk nella coda
window = np.blackman(1024 * 4)
CHUNK = 1024 * 4 
RATE = 44100           #quanti cambioni volgio
POWER_THRESHOLD = (10**6)/3
MAX_FLEGS = 3

"""p = pyaudio.PyAudio()

for i in range(0, p.get_device_count()):
    print(i, p.get_device_info_by_index(i)['name'])"""


class AudioStream:

    CHUNK = 1024 * 4            #quanti cambioni volgio
    FORMAT = pyaudio.paInt16    #da quanti byte è composto un campione //pyaudio.paInt16
    CHANNELS = 1                #numero di canali da utilizzare
    SAMPLE_FREQ = 44100
    DEVICE_INDEX = 0

    DEFAULT_DEVICE = "Microfono (Realtek(R) Audio)"

    def __init__(self) -> None:
        self.p = pyaudio.PyAudio()

        info = self.p.get_host_api_info_by_index(0)
        numdevices = info.get('deviceCount')

        for i in range(0, numdevices):
            if (self.p.get_device_info_by_host_api_device_index(0, i).get('maxInputChannels')) > 0:
                print("Input Device found: id ", i, " - ", self.p.get_device_info_by_host_api_device_index(0, i).get('name'))

            if(AudioStream.DEFAULT_DEVICE in self.p.get_device_info_by_host_api_device_index(0, i).get('name')):
                AudioStream.DEVICE_INDEX = i

        print("Selected index Device: ", AudioStream.DEVICE_INDEX)        
        self.openStream()


    def __init__(self, index:int) -> None:
        self.p = pyaudio.PyAudio()

        info = self.p.get_host_api_info_by_index(0)
        numdevices = info.get('deviceCount')

        if index >= numdevices:
            print("Selected index out of range")
            exit(0)
        

        print("Selected index Device: ", self.p.get_device_info_by_host_api_device_index(0, index))        
        AudioStream.DEVICE_INDEX = index

        self.openStream()
          

    def openStream(self) -> None:
        self.stream = self.p.open(format = AudioStream.FORMAT, channels=AudioStream.CHANNELS, rate = AudioStream.SAMPLE_FREQ, #stream_callback=self.callback,
            input=True, output=True, frames_per_buffer=AudioStream.CHUNK, input_device_index= AudioStream.DEVICE_INDEX, )

    def sampleSound(self) -> np.array:
        if not self.stream.is_active():
            return None

        sampledData = self.stream.read(AudioStream.CHUNK)#AudioStream.CHUNK*2
        #sampledData_int = np.array(struct.unpack(str(len(sampledData_hex)) + 'B', sampledData_hex), dtype='b')[::2] + 255

        #numpy_array = np.frombuffer(sampledData_hex, dtype=np.float16)

        waveData = wave.struct.unpack("%dh"%(AudioStream.CHUNK), sampledData)
        npArrayData = np.array(waveData)

        return npArrayData   
        
    def closeStream(self):
        if self.stream.is_active():
            self.stream.stop_stream()
            self.stream.close()
            self.p.terminate()

    def callback(self, indata, frames, time, status):
        global windowSamples
        if status:
            print(status)
        if any(indata):
            windowSamples = np.concatenate((windowSamples,indata[:, 0])) # append new samples
            windowSamples = windowSamples[len(indata[:, 0]):] # remove old samples
            magnitudeSpec = abs( scipy.fftpack.fft(windowSamples)[:len(windowSamples)//2] )

            for i in range(int(62/(AudioStream.SAMPLE_FREQ/WINDOW_SIZE))):
                magnitudeSpec[i] = 0 #suppress mains hum
 
            maxInd = np.argmax(magnitudeSpec)
            maxFreq = maxInd * (AudioStream.SAMPLE_FREQ/WINDOW_SIZE)
            closestNote, closestPitch = self.find_closest_note(maxFreq)

            os.system('cls' if os.name=='nt' else 'clear')
            print(f"Closest note: {closestNote} {maxFreq:.1f}/{closestPitch:.1f}")
        else:
            print('no input')

    
    def find_closest_note(self, pitch):
        i = int(np.round(np.log2(pitch/CONCERT_PITCH)*12))
        closest_note = ALL_NOTES[i%12] + str(4 + (i + 9) // 12)
        closest_pitch = CONCERT_PITCH*2**(i/12)
        return closest_note, closest_pitch
       
        

def main():

    input_stream = AudioStream()
    

    fig, ax = plt.subplots()
    x = np.arange(0, 2 * input_stream.CHUNK, 2)

    ax.set_ylim(0, 400)
    ax.set_xlim(0, input_stream.CHUNK*2)


    line, = ax.plot(x, np.random.rand(input_stream.CHUNK))
    plt.show(block=False)

    while KeyboardInterrupt:
        data = input_stream.sampleSound()

        line.set_ydata(data)
        fig.canvas.draw()
        fig.canvas.flush_events()

#https://gist.github.com/denisb411/cbe1dce9bc01e770fa8718e4f0dc7367
def main2():

    input_stream = AudioStream(2)

    #configurazione del grafico
    plt.ion()
    fig = plt.figure(figsize=(10,8))
    ax1 = fig.add_subplot(211)
    ax2 = fig.add_subplot(212)

    fig_spec, ax_spec = plt.subplots()
    #ax_spec = fig.add_subplot(213)

    x1 = np.arange(0, 2 * input_stream.CHUNK*QUEUE_SIZE, 2)
    x2 = np.arange(0, 2 * input_stream.CHUNK, 2)

    ax1.set_ylim(-8192,8192)
    ax1.set_xlim(0, CHUNK*QUEUE_SIZE)
    ax1.grid()

    #ax2.set_xlim(0, 22000)
    #ax2.set_ylim(0, 10000)
    ax2.axis([0,RATE/8,0,(10**6)*2])
    ax2.grid()

    #specgram
    spec = ax_spec.specgram([], Fs=RATE, NFFT=CHUNK, noverlap=CHUNK//2)
    ax_spec.set_ylim(0, RATE/2)
    
    
    line_Audio, = ax1.plot(x1, np.random.rand(input_stream.CHUNK*QUEUE_SIZE))
    line_FFT, = ax2.plot(x2, np.random.rand(input_stream.CHUNK))
    audio_queue = np.zeros(CHUNK*QUEUE_SIZE, dtype=np.int16)

    #ani = FuncAnimation(fig, update, init_func=init, blit=True)
    line_Audio.set_data(range(len(audio_queue)), audio_queue)
    plt.show(block=False)
    
    frequency_labels = []
    ax_spec.clear()

    while KeyboardInterrupt:
        t1 = time.time()

        #data = np.frombuffer(stream.read(CHUNK), dtype=np.int16)
        npArrayData = input_stream.sampleSound()

        if len(audio_queue) < input_stream.CHUNK*QUEUE_SIZE:
            audio_queue = np.concatenate((audio_queue, npArrayData))
        else:
            audio_queue[:-CHUNK] = audio_queue[CHUNK:]
            audio_queue[-CHUNK:] = npArrayData

        line_Audio.set_data(range(len(audio_queue)), audio_queue)
         

        fft_result = np.fft.fft(npArrayData)
        fft_freqs = np.fft.fftfreq(len(fft_result), d=1.0/RATE)
        fft_magnitudes = np.abs(fft_result)
        
        line_FFT.set_data(fft_freqs[:len(fft_freqs)//2], fft_magnitudes[:len(fft_magnitudes)//2])
       

        for label in frequency_labels:
            label.remove()
        frequency_labels.clear()

        # Trova le frequenze sopra la soglia di potenza
        for i in range(len(fft_freqs) // 2):
            if fft_magnitudes[i] > POWER_THRESHOLD:
                label = ax2.annotate(f'{fft_freqs[i]:.1f} Hz', xy=(fft_freqs[i], fft_magnitudes[i]), textcoords='offset points', xytext=(0,10), ha='center')
                frequency_labels.append(label)
                
                if len(frequency_labels) >= MAX_FLEGS:
                    break

        # Calcola la potenza del segnale
        signal_power = np.sum(np.square(npArrayData)) / len(npArrayData)
        
        if signal_power > POWER_THRESHOLD:
                        
            # Calcola lo spettrogramma
            ax_spec.clear()
            spec = ax_spec.specgram(npArrayData, Fs=RATE, NFFT=CHUNK, noverlap=CHUNK//2)

            # Calcola l'HPS
            hps = np.copy(spec[0])
            for harmonic in range(2, NUM_HARMONICS + 1):
                hps *= np.roll(spec[0], harmonic, axis=0)

            # Trova la frequenza dominante nell'HPS
            hps_freqs = spec[1]
            dominant_freq = hps_freqs[np.argmax(np.max(hps, axis=1))]

            ax_spec.imshow(10 * np.log10(spec[0]), origin='lower', extent=[0, len(npArrayData)/RATE, 0, RATE/2], aspect='auto', cmap='inferno')
            ax_spec.axhline(dominant_freq, color='white', linestyle='dashed')
            #plt.pause(0.0001)"""

            print("hps_freqs: ", dominant_freq)

        """print("took %.02f ms"%((time.time()-t1)*1000))
        # use quadratic interpolation around the max
        if which != len(fftData)-1:
            y0,y1,y2 = np.log(fftData[which-1:which+2:])
            x1 = (y2 - y0) * .5 / (2 * y1 - y2 - y0)
            # find the frequency and output it
            thefreq = (which+x1)*RATE/CHUNK
            print("The freq is %f Hz." % (thefreq))
        else:
            thefreq = which*RATE/CHUNK
            print("The freq is %f Hz." % (thefreq))"""

        fig.canvas.draw()
        fig.canvas.flush_events()






    input_stream.closeStream()




        

    

    

if __name__ == "__main__":
    main2()
