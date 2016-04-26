# WebRTCDemo
WebRTC demo application to run on OpenShift 3 based on NextRTC implementation 

## Summary
This simple WebRTC video chat application can run on OpenShift v3. The required OpenShift v3 artifacts (WildFly 8.1 source-to-image and JGroups Gossip image) are available here: [WebRTCDemo OpenShift v3 Files](https://github.com/darmaigabor/WebRTCDemo_OpenShift_v3_Files).
Features implemented:
* Create a WebRTC video chat room and enable the establishment of a 2-party video conversation by using WebSocket (tested on Google Chrome 50.x). The application is implemented in Spring framework and runs on WildFly 8.1
* Enable OpenShift scaling: the parties belonging to the same video conversation will connect to the same server instance (POD) with WebSocket connection and the created conversation descriptions will be available for all PODs (data synchronization is done using Infinispan cache configured to use JGroups TUNNEL via Gossip Router)

## Installation
### Preparing phase I., create OpenShift resources on master node
Download the OpenShift v3 artifacts from [here](https://github.com/darmaigabor/WebRTCDemo_OpenShift_v3_Files) as a zip file and copy the zip content to the OpenShift master node.
Login to OpenShift and Docker on the master node with a user having cluster-admin role:

```
oc login
```

Set the current project name to "default" and get your security token:

```
oc project default
oc whoami -t
```

Get Docker Registry IP address and store it in DOCKER_REGISTRY_IP environment variable for later use:

```
export DOCKER_REGISTRY_IP=$(oc get svc docker-registry --template "{{ .spec.portalIP }}")
```

Login to Docker:

```
docker login -u <the same user ID that was used to login to OpenShift> -p <the token got from oc whoami -t previously> -e <arbitrary email addree> $DOCKER_REGISTRY_IP:5000
```

Create a new OpenShift project called webrtcdemo:

```
oc new-project webrtcdemo
```

Set the new project as your actual working project:

```
oc project webrtcdemo
```

Build Gossip Router image (move to the jgroups-gossip folder that was created when you copied the downloaded zip content to the master node):

```
docker build -t $DOCKER_REGISTRY_IP:5000/webrtcdemo/wildfly81distcache:v1 .
```

Build WildFly 8.1 source-to-image (move to the wildfly81-dist-cache folder that was created when you copied the downloaded zip content to the master node):  

```
docker build -t $DOCKER_REGISTRY_IP:5000/webrtcdemo/gossip:v1 .
```

Push the created images to the Docker registry:

```
docker push $DOCKER_REGISTRY_IP:5000/webrtcdemo/gossip:v1
docker push $DOCKER_REGISTRY_IP:5000/webrtcdemo/wildfly81distcache:v1
```

### Preparing phase II., create OpenShift resources on the node runing HAProxy
Modify the standard HAProxy router to support URL parameter based load-balancing (WebSockets belonging to the same conversation have to be connected to the same POD, roundrobin or leastconn load-balancing policy cannot work).
For this find the node where the router POD is running and login to that node. Login to OpenShift and Docker (see the steps above). Set the currently active project to the default project and find the HaProxy's Docker coontainer ID:

```
oc project default
docker ps | grep haproxy
```

Open an interactive shell to the running HAPRoxy container:

```
docker exec -ti <container ID retreived from by the previous command's output> bash
```

Open the haproxy-config.template file for modification:

```
vi haproxy-config.template
```

Modify the content in the following way:


Find this content in the file:
```
backend be_http_{{$cfgIdx}}
                 {{ else }}
 backend be_edge_http_{{$cfgIdx}}
                 {{ end }}
   mode http
   option redispatch
   option forwardfor
```
Replace this line:
```
   balance leastconn
```
With this:
```
   {{ if (eq $cfgIdx "webrtcdemo_webrtcdemo-route") }}
   balance url_param convId
   {{ else }}
   balance leastconn
   {{ end }}
```
Keep that part:
```
   timeout check 5000ms
   http-request set-header X-Forwarded-Host %[req.hdr(host)]
   http-request set-header X-Forwarded-Port %[dst_port]
   http-request set-header X-Forwarded-Proto https if { ssl_fc }
   {{ if (eq $cfg.TLSTermination "") }}
     cookie OPENSHIFT_{{$cfgIdx}}_SERVERID insert indirect nocache httponly
     http-request set-header X-Forwarded-Proto http
   {{ else }}
```
End replace this line:
```
     cookie OPENSHIFT_EDGE_{{$cfgIdx}}_SERVERID insert indirect nocache httponly secure
```
With this part:
```
     {{ if (eq $cfgIdx "webrtcdemo_webrtcdemo-route") }}
     #no cookie deifinition
     {{ else }}
     cookie OPENSHIFT_EDGE_{{$cfgIdx}}_SERVERID insert indirect nocache httponly secure
     {{ end }}
```

Save the file and exit the container. Create a local image from this modified running container:

```
docker commit <container ID retreived from by the previous command's output> $DOCKER_REGISTRY_IP:5000/default/origin-haproxy-router:webrtcdemo_route
```

Push the created image to the Docker registry:

```
docker push $DOCKER_REGISTRY_IP:5000/default/origin-haproxy-router:webrtcdemo_route
```

Remove the existing HAProxy router service from OpenShift:

```
oc delete services router
oc delete dc router
```

Create a new router based on the previously created HAProxy image:

```
oadm router  --credentials=/etc/origin/master/openshift-router.kubeconfig  --service-account=router --images="$DOCKER_REGISTRY_IP:5000/default/origin-haproxy-router:webrtcdemo_route"
```

Check if the reouter POD has started properly (a router POD has to be in RUNNING state after about 30 seconds):

```
oc get pods
```

### Build and configure the appication on the master node
On the OpenShift master node move to the wildfly81-dist-cache folder that was created when you copied the downloaded zip content to the master node. 
If you have to use HTTP proxy to access the internet from OpenShift nodes then edit BuildConfiguration part in the webrtcdemo-template.json file according to [OpenShift's documentation](https://docs.openshift.com/enterprise/3.0/admin_guide/http_proxies.html#configuring-default-templates-for-proxies). Internet access is needed for the build process because the source files reside on GitHub.
Set the currently active project to webrtcdemo and create all OpenShift resources required for the WebRTC demo application:

```
oc project webrtcdemo
```
Replace the "hard-coded" Docker Registry IP with the actual value:
```
sed -i -e 's/172.30.169.170/'$DOCKER_REGISTRY_IP'/g' webrtcdemo-template.json
```
Create the application:
```
oc new-app -f webrtcdemo-template.json
```

After about 2 minutes one running webrtcdemo and one active gossip POD must be in RUNNING state.

## Test the application

The application will listen to webrtcdemo.openshift.local host name. You have to map this host name to the IP address of the node that actually runs the HAProxy POD in your desktop's hosts file. 
Be sure that you have access to that IP address on TCP port 443 (no blocking firewall is in between your desktop and that IP address)! 
In order to access your application go to [https://webrtcdemo.openshift.local](https://webrtcdemo.openshift.local). [Be sure that you use a browser that supports WebRTC](http://iswebrtcreadyyet.com)
and your desktop computer has a working camera device connected! 
This demo solution was tested with Google Chrome 50.x. 

To make a test video chat follow these steps:
* Open your bwoser with [https://webrtcdemo.openshift.local](https://webrtcdemo.openshift.local)
* Give your name in the input field
* Click "Wait somebody to join..."
* The browser will ask you to allow the access to the camera and the microphone. Allow the access to them! Your camera's view has to be shown on the page
* A video chat room with your name is created and other people can join to the opened conversation (video chat room). Currently only 2-party conversations are allowed by the application
* Open a new tab in your browser and go the [https://webrtcdemo.openshift.local](https://webrtcdemo.openshift.local)
* You should see the name of your previously created video chat room under "People waiting for your to join:"
* Give your name in the input field (choose another name that you have used in the step above)
* Click the pink camera icon next to the name of the chat room
* The two parties have to be connected
* In a separate browser tab check the following URL: [https://webrtcdemo.openshift.local/conversationdetails/list](https://webrtcdemo.openshift.local/conversationdetails/list). The result JSON should contain the details of your video conversation (the name of the POD handling the conversation and the names of the parties)

Alternatively you can try the 2-party video conversation from 2 desktop computers (instead of using one desktop with one browser and two tabs).

If you want to rebuild your application (e.g. because of changes in the source code on GitHub) then go to the master node, login to OpenShift, set the currently active project to webrtcdemo and type the following:

```
oc start-build webrtcdemo-build
```

After about 2 minutes the new version of the application should be available.

## Remove the application from the project

In order to remove all application related OpenShift artifacts (route, services, deployment configs, build config) from the project issue the following commands:

```
oc project webrtcdemo
oc delete routes webrtcdemo-route
oc delete services webrtcdemo-service
oc delete dc webrtcdemo
oc delete bc webrtcdemo-build
oc delete services gossip-service
oc delete dc gossip
```

After the artifats are successfully deleted the application can be fully re-created by calling:

```
oc project webrtcdemo
oc new-app -f webrtcdemo-template.json
```
