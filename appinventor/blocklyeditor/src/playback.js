'use strict';

goog.provide('Blockly.Playback');

Blockly.Playback.start = function (filename){
    var history = [];       // 0-indexed, unlike frame numbers, which are 1-indexed
    var length = 0;
    var currentFrame = 0;   // first frame is 1, not yet loaded is 0

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

    var printStatus = function () {
        str = "Frame " + currentFrame + " of " + length + "   time: " +
            timeString(history[currentFrame - 1]['seconds_elapsed']);

        if('interval' in history[currentFrame-1]){
            str += "   interval: " + timeString(history[currentFrame-1]['interval']);
        }

        console.log(str);
    };

    var load = function (framenum){
        if (framenum <= length && framenum > 0) {
            injectBlocks(history[framenum - 1]['contents']['Screen1/blocks']);
            currentFrame = framenum;
        }
        printStatus()
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

    loadProjectFile(filename);

    return {
        length: function () { return length },
        load: load,
        next: function () { load( currentFrame + 1 ) },
        prev: function () { load( currentFrame - 1 ) },
        last: function () { load( length ) },
        start: loadProjectFile
    };

};
