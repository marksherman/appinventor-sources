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
    var history = [];       // 0-indexed, unlike frame numbers, which are 1-indexed
    var length = 0;
    var currentFrame = 0;   // first frame is 1, not yet loaded is 0
    var status = "";

    var injectBlocks = function (blocksXML){
        Blockly.mainWorkspace.clear(); // Remove any existing blocks before we add new ones.
        Blockly.Xml.domToWorkspace(Blockly.mainWorkspace, Blockly.Xml.textToDom(blocksXML));
    };

    var timeString = function(seconds_elapsed){
        seconds = seconds_elapsed % 60;
        minutes = Math.floor(seconds_elapsed / 60) % 60;
        hours = Math.floor(seconds_elapsed / 60 / 60);
        str = "";
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
        str = "Frame " + currentFrame + " of " + length + "   time: " +
            timeString(history[currentFrame - 1]['seconds_elapsed']);

        if('interval' in history[currentFrame-1]){
            str += "   interval: " + timeString(history[currentFrame-1]['interval']);
        }

        status = str;
    };

    var printStatus = function () {
        console.log(status);
    };

    var load = function (framenum){
        if (framenum <= length && framenum > 0) {
            injectBlocks(history[framenum - 1]['contents']['Screen1/blocks']);
            currentFrame = framenum;
        }
        updateStatusString();
        printStatus();
        top.ResearchTools_setPlaybackStatus(status);
    };

    var loadProjectFile = function (filename){
        fetch("http://localhost:8000/" + filename)
            .then(function(result){return result.json();})
            .then(function(jsontext){
                history = jsontext;
                length = history.length;
                load(1);
            });
    };

    return {
        length: function () { return length },
        load: load,
        next: function () { load( currentFrame + 1 ) },
        prev: function () { load( currentFrame - 1 ) },
        first: function () { load(1) },
        last: function () { load( length ) },
        start: loadProjectFile,
        status: function () { return status }
    };

};
