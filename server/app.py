import os
import numpy as np
import tensorflow as tf
import librosa
from librosa import display
from matplotlib import pyplot as plt
from flask import Flask, escape, request, render_template

app = Flask(__name__)

@app.route('/')
def index():
    return 'Hello'

@app.route('/uploadfile', methods=['POST'])
def uploadfile():
    uploaded_file = request.files['file']
    if uploaded_file.filename != '':
        uploaded_file.save(uploaded_file.filename)
        
        file_path = konversi(uploaded_file.filename)

        prediksi = prediksi_gambar(file_path)
        print(prediksi)

        return prediksi

def prediksi_gambar(path):
    model = tf.keras.models.load_model('/Users/macalwi/Downloads/MIC.h5')

    CATEGORIES = ['Akordion', 'Angklung', 'Kompang', 'Rebab']

    img = tf.keras.preprocessing.image.load_img(
        path, target_size=(200, 200)
    )

    img_array = tf.keras.preprocessing.image.img_to_array(img)
    img_array = tf.expand_dims(img_array, 0)

    prediksi = model.predict(img_array)
    akurasi = tf.nn.softmax(prediksi[0])

    return '{}, {}'.format(CATEGORIES[np.argmax(prediksi)], np.max(akurasi))
    
def konversi(fileName):
    plt.switch_backend('Agg')

    y, sr = librosa.load(fileName, sr=None) # Load the wav sound
    ps = librosa.feature.melspectrogram(y=y, sr=sr)
    librosa.display.specshow(librosa.power_to_db(ps, ref=np.max))

    plt.subplots_adjust(top = 1, bottom = 0, right = 1, left = 0, 
                hspace = 0, wspace = 0)

    plt.savefig('{}.png'.format(fileName))

    return '{}.png'.format(fileName)


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000)
