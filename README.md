# JavaCamServer
This is a simple-ish server made with Java that uses [WebCam API](https://github.com/sarxos/webcam-capture) by @sarxos to push images to another server. This program pokes the receiving server (using a java socket and JSON) to let him know he's ready and then initiate an HTTP connexion to push images.

Images are pushed as JPEG in a  HTTP multipart MPEG stream, no real encoding is done, nor is there any integrity check apart from standard TCP loss correction.

This project uses Maven for the few dependencies, and is somewhat capable of building a standalone jar. It depends on a bunch of stuff that is described in `pom.xml`.

# Usage
Run it with
```
java -jar ./JavaCamServer.jar -option...
```
Options are included in an helpful help, run it with:
`java -jar ./JavaCamServer.jar -help`

Snippet from help:
```
Usage: <main class> [-np] [-a=<STRserverAddress>] [-cp=<port>] [-f=<fps>]
                    [-h=<height>] [-n=<name>] [-p=<serverPort>] [-w=<width>]
  -a, --addr, --address=<STRserverAddress>
                            Adress of the server to poke (default: 193.48.125.70)
      -cp, --cliport, --clientport=<port>
                            Port we will be using to send our images (default: 50009)
  -f, --fps=<fps>           Number of frames per seconds, camera-dependant (default:
                              10)
  -h, --height=<height>     Height of the camera frame, camera-dependant (default:
                              480)
  -n, --name=<name>         Name of our sender, will figure in JSON (default: cam)
      -np, --nopoke         Switch to disable server poking (default: poke enabled)
  -p, --port=<serverPort>   Port of the server to poke (default: 50008)
  -w, --width=<width>       Width of the camera frame, camera-dependant (default:
                              640)
```

If you don't know what framerate/resolution your camera support, try what you're planning to use. The program will throw an error with the supported resolutions for your hardware if it isn't supported. Also, remember to use `log4j.properties`. One is included in the zip and repo, but ou can configure it to do the logging however you like. This project uses extensively this logging framework so you should see a lot of messages (with some not formatted, thrown from rebellious dependencies).

## Notes on the poking
In the options you'll see the option to add a "poke server", this was initially made so you could notify another server (specifically [this one](https://github.com/APP-RS-OC-Polytech-2020/JavaRepeatServer) as part of the project decribed [here](https://github.com/APP-RS-OC-Polytech-2020/Custodia-Meta) ) when your camera is ready to stream. The poke consist of a simple java socket (described in `PokeServerTask.java`) in which we write the following JSON message:
```
{
  "type":"init","
  "infoInit":"Coucou, j'ouvre un server, cya"
  "clientName":"$camName" //Name of our cam, defined at launch with --name
  "clientType":"Webcam"
  "ip":"$address" // Adress of our streaming server aka where we are hosting
  "port":"$port" // Port on which we are listening for incoming connexions
 }
 ```
 This poking behavior can be disabled using the `--nopoke` switch.
