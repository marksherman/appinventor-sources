'use strict';

goog.provide('Blockly.Playback');

Blockly.Playback.player = null;

Blockly.Playback.start = function (filename){
    if(Blockly.Playback.player === null){
        Blockly.Playback.player = Blockly.Playback.init();
    }
    Blockly.Playback.player.start(filename);
};

Blockly.Playback.init = function (){
    var history = [];
    var length = 0;
    var currentFrame = null;   // first frame is 0, not yet loaded is null
    var status = "";

    var injectBlocks = function (blocksXML){
        Blockly.mainWorkspace.clear(); // Remove any existing blocks before we add new ones.
        Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, Blockly.Xml.textToDom(blocksXML));
    };

    var updateCurrentFrame = function(frameNum){
        currentFrame = frameNum;
        top.ResearchTools_setPlaybackFrameNumber(frameNum);
    };

    var timeString = function(seconds_elapsed){
        var seconds = seconds_elapsed % 60;
        var minutes = Math.floor(seconds_elapsed / 60) % 60;
        var hours = Math.floor(seconds_elapsed / 60 / 60);
        var str = "";

        if(hours > 0){
            str += hours + "h";
            if(minutes < 10){
                str += "0";
            }
        }
        str += minutes + ":";
        if(seconds < 10){
            str += "0";
        }
        str += seconds;

        return str;
    };

    var updateStatusString = function () {
        var str = "FRAME " + currentFrame + " of " + (length - 1) + "   TIME: " +
            timeString(history[currentFrame]['seconds_elapsed']);

        if('interval' in history[currentFrame]){
            str += "   INTERVAL: " + timeString(history[currentFrame]['interval']);
        }

        status = str;
    };

    var printStatus = function () {
        console.log(status);
        top.ResearchTools_setPlaybackStatus(status);
    };

    var load = function (framenum){
        if (framenum < length && framenum >= 0) {
            injectBlocks(history[framenum]['contents']['Screen1/blocks']);
            updateCurrentFrame(framenum);
        }
        updateStatusString();
        printStatus();
    };

    var loadProjectFile = function (filename){
        var fetchurl = '';
        if (filename.startsWith('http')) {
            fetchurl = filename;
        } else {
            fetchurl = "http://localhost:8000/" + filename
        }
        fetch(fetchurl)
            .then(function(result){return result.json();})
            .then(function(jsontext){
                history = jsontext;
                length = history.length;
                load(0);
            });
    };

    return {
        length: function () { return length },
        load: load,
        next: function () { load(currentFrame + 1) },
        prev: function () { load(currentFrame - 1) },
        first: function () { load(0) },
        last: function () { load(length - 1) },
        start: loadProjectFile,
        status: function () { return status }
    };

};
