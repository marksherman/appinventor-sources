package com.google.appinventor.client;

import com.google.appinventor.client.editor.youngandroid.BlocklyPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

/**
 * Created by mark on 12/13/16.
 */
public class ResearchToolsDialog extends DialogBox {

    private final Button firstButton;
    private final Button prevButton;
    private final Button nextButton;
    private final Button lastButton;
    private final Button startButton;
    private final Button closeButton;
    private final Label infoLabel;
    private final TextBox raterIdTextBox;
    private final TextBox projectIdTextBox;
    private final CheckBox solutionDebugSK;
    private final CheckBox solutionDebugS1;
    private final CheckBox solutionDebugS2;
    private final CheckBox solutionDebugLL;
    private final CheckBox solutionTemperature;

    private int frameNumber;

    public void setStatusText(String newStatus){
        infoLabel.setText(newStatus);
    }

    public void setFrameNumber(int frameNum){
        frameNumber = frameNum;
        //trigger load of new coding data here, pray we don't get inconsistent
        //this way we know for sure the frame here matches what blockly is displaying
        // Logic: by clicking Next or Prev, we commit current coding data to db with send.
        // The next frame is loaded in blockly, which triggers this update when complete.
        // There are cases where blockly will not advance, and this needs to accept that.
        raterIdTextBox.setText(String.valueOf(frameNum));
    }

    public ResearchToolsDialog() {
        setText("Research Tools");
        setModal(false);

        frameNumber = -1;

        firstButton = new Button("|<");
        prevButton = new Button("Prev");
        nextButton = new Button("Next");
        lastButton = new Button(">|");
        infoLabel = new Label("Frame 0 of 0 time: 0:00");
        raterIdTextBox = new TextBox();
        projectIdTextBox = new TextBox();

        solutionDebugSK = new CheckBox("Shake");
        solutionDebugS1 = new CheckBox("Speak literal");
        solutionDebugS2 = new CheckBox("Speak variable");
        solutionDebugLL = new CheckBox("Last label");

        solutionTemperature = new CheckBox("Temperature solved");

        startButton = new Button("Start");
        closeButton = new Button("close");

        raterIdTextBox.setText("RaterID");
        raterIdTextBox.setTitle("Rater ID. This is your code name.");
        projectIdTextBox.setText("ProjectID");
        projectIdTextBox.setTitle("Project ID. This is the code for the project you are assigned to rate.");

        solutionDebugSK.setTitle("Shake: shaking message text changed to \"Stop Shaking Me\"");
        solutionDebugS1.setTitle("Speak literal: SpeakButton message text changed to \"How are you?\"");
        solutionDebugS2.setTitle("Speak variable: SpeakButton message references TextBox1_UserInput");
        solutionDebugLL.setTitle("Last label: SaidLastLabel is updated when SpeakButton is used");
        solutionTemperature.setTitle("Temperature: Expression implements conversion formula and appears to work");

        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                ResearchToolsDialog.this.hide();
            }
        });

        startButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                BlocklyPanel.startPlayback("playback/debug2/HamadanMouse.json");
            }
        });

        nextButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                BlocklyPanel.playbackNext();
            }
        });

        prevButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                BlocklyPanel.playbackPrev();
            }
        });

        firstButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                BlocklyPanel.playbackFirst();
            }
        });

        lastButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                BlocklyPanel.playbackLast();
            }
        });

        HorizontalPanel startPanel = new HorizontalPanel();
        startPanel.add(raterIdTextBox);
        startPanel.add(projectIdTextBox);
        startPanel.add(startButton);
        startPanel.add(closeButton);

        HorizontalPanel navigationPanel = new HorizontalPanel();
        navigationPanel.setVerticalAlignment(HasVerticalAlignment.ALIGN_MIDDLE);
        navigationPanel.add(firstButton);
        navigationPanel.add(prevButton);
        navigationPanel.add(infoLabel);
        navigationPanel.add(nextButton);
        navigationPanel.add(lastButton);

        HorizontalPanel codingPanelDebug = new HorizontalPanel();
        codingPanelDebug.add(solutionDebugSK);
        codingPanelDebug.add(solutionDebugS1);
        codingPanelDebug.add(solutionDebugS2);
        codingPanelDebug.add(solutionDebugLL);

        HorizontalPanel codingPanelTemperature = new HorizontalPanel();
        codingPanelTemperature.add(solutionTemperature);

        VerticalPanel panel = new VerticalPanel();
        panel.add(navigationPanel);
        panel.add(codingPanelDebug);
        panel.add(codingPanelTemperature);
        panel.add(startPanel);

        setWidget(panel);

    }
}
