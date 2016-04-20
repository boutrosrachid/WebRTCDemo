/**
 * This library require adapter.js
 */
// 'use strict';
navigator.getUserMedia = getUserMedia;

function Message(signal, to, content, custom) {
	this.signal = signal;
	this.to = to;
	this.content = content;
	this.custom = custom;
};

function WebRTC(config) {

	if (WebRTC.instance == null) {
		WebRTC.instance = this;
	} else {
		return WebRTC.instance;
	}

	this.mediaConfig = config.mediaConfig !== undefined ? config.mediaConfig : null;
	this.type = config.type;

	this.signaling = new WebSocket(config.wsURL);
	this.peerConnections = {};
	this.localStream = null;
	this.signals = {};

	this.on = function(signal, operation) {
		this.signals[signal] = operation;
	};

	this.call = function(event, data) {
		for ( var signal in this.signals) {
			if (event === signal) {
				return this.signals[event](this, data);
			}
		}
		console.log('Event ' + event + ' do not have defined function');
	};

	this.join = function(convId, memberName) {
		var webRTC = this;
		navigator.getUserMedia(webRTC.mediaConfig, function(stream) {
			webRTC.localStream = stream;
			webRTC.call('localStream', {
				stream : stream
			});
			webRTC.request('join', null, convId, memberName);
		}, error);
	};

	this.create = function(convId, memberName) {
		var webRTC = this;
		navigator.getUserMedia(webRTC.mediaConfig, function(stream) {
			webRTC.localStream = stream;
			webRTC.call('localStream', {
				stream : stream
			});
			webRTC.request('create', null, convId, memberName);
		}, error);
	};

	this.request = function(signal, to, convId, memberName) {
		var msg2Send;
		if(memberName) {
			var params = {'memberName' : memberName};
			msg2Send = new Message(signal, to, convId, params);
		}
		else {
			msg2Send = new Message(signal, to, convId);
		}
		var req = JSON.stringify(msg2Send);
		console.log("res: " + req);
		var s = this.signaling;
		this.waitForConnection(function () {
			s.send(req);
		}, 1000);
	};

	this.waitForConnection = function (callback, interval) {
		if (ws.readyState === 1) {
			callback();
    		} else {
			var that = this;
			setTimeout(function () {
				that.waitForConnection(callback, interval);
			}, interval);
		}
	};
	
	this.signaling.onmessage = function(event) {
		console.log("req: " + event.data);
		var signal = JSON.parse(event.data);
		WebRTC.instance.call(signal.signal, signal);
	};

	this.signaling.onclose = function(event) {
		WebRTC.instance.call('close', event);
		WebRTC.instance = null;
	};

	this.signaling.onerror = function(event) {
		WebRTC.instance.call('error', event);
		WebRTC.instance = null;
	};

	this.preparePeerConnection = function(webRTC, member) {
		if (webRTC.peerConnections[member] == undefined) {
			var pc = new RTCPeerConnection(config.peerConfig);
			pc.onaddstream = function(evt) {
				webRTC.call('remoteStream', {
					member : member,
					stream : evt.stream
				});
			};
			pc.onicecandidate = function(evt) {
				handle(pc, evt);
				
				function handle(pc, evt){
					if((pc.signalingState || pc.readyState) == 'stable' 
						&& webRTC.peerConnections[member]['rem'] == true){
						handleCandidate(webRTC, evt.candidate, member, pc);
						return;
					} 
					setTimeout(function(){ handle(pc, evt); }, 2000);				
				}
			};
			webRTC.peerConnections[member] = {}
			webRTC.peerConnections[member]['pc'] = pc;
			webRTC.peerConnections[member]['rem'] = false;
		}
		return webRTC.peerConnections[member];
	};

	this.offerRequest = function(webRTC, from) {
		webRTC.offerResponse(webRTC, from);
	};

	this.offerResponse = function(webRTC, signal) {
		var pc = webRTC.preparePeerConnection(webRTC, signal.from);
		pc['pc'].addStream(webRTC.localStream);
		pc['pc'].createOffer(function(desc) {
			pc['pc'].setLocalDescription(desc, function() {
				webRTC.request('offerResponse', signal.from, desc.sdp);
			}, error, success);
		}, error);
	};

	this.answerRequest = function(webRTC, signal) {
		webRTC.answerResponse(webRTC, signal);
	};

	this.answerResponse = function(webRTC, signal) {
		var pc = webRTC.preparePeerConnection(webRTC, signal.from);
		pc['pc'].addStream(webRTC.localStream);
		pc['pc'].setRemoteDescription(new RTCSessionDescription({
			type : 'offer',
			sdp : signal.content
		}), function() {
			pc['rem'] = true;
			pc['pc'].createAnswer(function(desc) {
				pc['pc'].setLocalDescription(desc, function() {
					webRTC.request('answerResponse', signal.from, desc.sdp);
				}, error, success);
			}, success, webRTC.mediaConfig.sdpConstraints);
		}, error);
	};

	this.finalize = function(webRTC, signal) {
		var pc = webRTC.preparePeerConnection(webRTC, signal.from);
		pc['pc'].setRemoteDescription(new RTCSessionDescription({
			type : 'answer',
			sdp : signal.content
		}), function(){
			pc['rem'] = true;
		}, error);
	};

	this.close = function(webRTC, event) {
		webRTC.signaling.close();
	};
	
	this.candidate = function(webRTC, signal) {
		var pc = webRTC.preparePeerConnection(webRTC, signal.from);
		pc['pc'].addIceCandidate(new RTCIceCandidate(JSON.parse(signal.content.replace(new RegExp('\'', 'g'), '"'))), success, error);
	}

	this.init = function() {
		this.on('created', this.created);
		this.on('offerRequest', this.offerRequest);
		this.on('answerRequest', this.answerRequest);
		this.on('finalize', this.finalize);
		this.on('candidate', this.candidate);
		this.on('close', this.close);
		this.on('ping', function(){});
	};

	function handleCandidate(webRTC, candidate, member, destPC) {
		if (candidate) {
			webRTC.request('candidate', member, JSON.stringify(candidate));
		}
	};

	this.init();
};

WebRTC.instance = null;

WebRTC.onReady = function() {
	console.log('It is highly recommended to override method WebRTC.onReady');
};

// it works for new Chrome, Opera and FF
if (document.addEventListener) {
	document.addEventListener('DOMContentLoaded', function() {
		WebRTC.onReady();
	});
}

var error = function(error) {
	console.log('error ' + JSON.stringify(error));
};

var success = function(success) {
	console.log('success ' + JSON.stringify(success));
};
