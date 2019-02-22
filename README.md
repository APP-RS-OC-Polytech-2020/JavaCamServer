# JavaCamServer
This is a simple-ish server made with Java that uses [WebCam API](https://github.com/sarxos/webcam-capture) by @sarxos to push images to another server. This program pokes the receiving server (using a java socket and JSON) to let him know he's ready and then initiate an HTTP connexion to push images.

This project uses Maven for the few dependencies, and is somewhat capable of building a standalone jar.


