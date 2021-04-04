import os
import numpy as np
import tensorflow as tf
import librosa
from librosa import display
from matplotlib import pyplot as plt
from flask import Flask, escape, request, render_template

app = Flask(__name__)

@ app.route('/')
def index():
    return 'Hello, World !'

@ app.route('/uploadfile', methods = ['POST'])
def uploadfile():
    uploaded_file = request.files['file']
    
    if uploaded_file.filename != '':
        uploaded_file.save(uploaded_file.filename)
    
    file_path = conversion(uploaded_file.filename)

    prediction = predict_image(file_path)

    return prediction

def conversion(filename):
    y, sr = librosa.load(filename, sr=None)
    ps = librosa.feature.melspectrogram(y=y, sr=sr)
    librosa.display.specshow(librosa.power_to_db(ps, ref=np.max))
    
    plt.subplots_adjust(top=1, bottom=0, right=1, left=0, hspace=0, wspace=0)
    plt.savefig('{}.png'.format(filename))
    plt.close()
    
    return '{}.png'.format(filename)

def predict_image(path):
    model = tf.keras.models.load_model('/model_path')

    CATEGORIES = ['Akordion', 'Angklung', 'Kompang', 'Rebab']

    img = tf.keras.preprocessing.image.load_img(path, target_size=(200, 200))

    img_array = tf.keras.preprocessing.image.img_to_array(img)
    img_array = tf.expand_dims(img_array, 0)

    prediction = model.predict(img_array)
    accuracy = tf.nn.softmax(prediction[0])

    return '{}, {}'.format(CATEGORIES[np.argmax(prediction)], np.max(accuracy))

if __name__ == '__main__':
    app.run(host = '0.0.0.0', port=5000)