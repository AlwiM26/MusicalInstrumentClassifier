import librosa 
import os
import numpy as np
from librosa import display
from matplotlib import pyplot as plt

# directory for dataset
rootdir = '/dir' #put your dataset directory here

# counter for the filename
counter = 0

#first loop through all the class name, each class stored in different directory
for dir in os.listdir(rootdir):
    # directory variable store each directory for each class
    directory = os.path.join(rootdir, dir)

    # loop through all file in the current directory
    for filename in os.listdir(directory):
        # load the wav sound to the librosa library
        y, sr = librosa.load(os.path.join(directory, filename), sr=None) 
        ps = librosa.feature.melspectrogram(y=y, sr=sr)
        librosa.display.specshow(librosa.power_to_db(ps, ref=np.max))

        # adjust image to remove the white padding on the spectrogram image
        plt.subplots_adjust(top=1, bottom=0, right=1, left=0, hspace=0, wspace=0)
        plt.savefig('{}/{}.png'.format(dir, counter))

        # close the image
        plt.close()

        counter += 1
    # reset the counter for another class
    counter = 0