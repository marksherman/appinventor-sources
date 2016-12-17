package com.google.appinventor.client;

import com.google.appinventor.client.editor.youngandroid.BlocklyPanel;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
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
    private final CheckBox flailingCheckBox;

    private int frameNumber;
    private String projectID;
    private String raterID;
    private boolean raterIdOk;

    private static final int STATUS_CODE_OK = 200;
    private static final String raterIdStartValue = "RaterID";
    private static final String projectIdStartValue = "ProjectID";

    public void setStatusText(String newStatus){
        infoLabel.setText(newStatus);
    }

    public void setFrameNumber(int frameNum){
        frameNumber = frameNum;
        /*
        trigger load of new coding data here, pray we don't get inconsistent
        this way we know for sure the frame here matches what blockly is displaying
        Logic: by clicking Next or Prev, we commit current coding data to db with send.
        The next frame is loaded in blockly, which triggers this update when complete.
        There are cases where blockly will not advance, and this needs to accept that.
        */
        getRatingsCodes();
    }

    private void startPlayback(){
        if(raterIdOk) {
            //format: http://msp.cs.uml.edu/playback/t/RangoonBear.json
            String startUrl = "http://msp.cs.uml.edu/playback/";
            startUrl = startUrl.concat(projectID).concat(".json");
            //startUrl = "playback/d/AcapulcoDeer.json"; // local debug line
            BlocklyPanel.startPlayback(startUrl);
        } else {
            infoLabel.setText("Can't start: raterID invalid");
        }
    }

    public ResearchToolsDialog() {
        setText("Research Tools");
        setModal(false);

        frameNumber = -1;
        projectID = null;
        raterID = null;
        raterIdOk = false;

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

        flailingCheckBox = new CheckBox("Flailing");

        startButton = new Button("Start");
        closeButton = new Button("close");

        raterIdTextBox.setText(raterIdStartValue);
        raterIdTextBox.setTitle("Rater ID. This is your code name.");
        projectIdTextBox.setText(projectIdStartValue);
        projectIdTextBox.setTitle("Project ID. This is the code for the project you are assigned to rate.");

        solutionDebugSK.setTitle("Shake: shaking message text changed to \"Stop Shaking Me\"");
        solutionDebugS1.setTitle("Speak literal: SpeakButton message text changed to \"How are you?\"");
        solutionDebugS2.setTitle("Speak variable: SpeakButton message references TextBox1_UserInput");
        solutionDebugLL.setTitle("Last label: SaidLastLabel is updated when SpeakButton is used");
        solutionTemperature.setTitle("Temperature: Expression implements conversion formula and appears to work");

        flailingCheckBox.setTitle("Check if this frame appears to demonstrate flailing behavior.");
        flailingCheckBox.setEnabled(false);

        closeButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                ResearchToolsDialog.this.hide();
            }
        });

        startButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                projectID = projectIdTextBox.getText();
                raterID = raterIdTextBox.getText();

                checkRaterID(); //also calls startPlayback

            }
        });

        nextButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                putRatingsCodes(new RequestCallback() {

                    public void onError(Request request, Throwable exception) {
                        infoLabel.setText(exception.getMessage());
                    }

                    public void onResponseReceived(Request request, Response response) {
                        if(STATUS_CODE_OK == response.getStatusCode()) {
                            // Data saved - now OK to change frame
                            BlocklyPanel.playbackNext();
                        } else {
                            infoLabel.setText(response.getStatusText());
                        }
                    }
                });
            }
        });

        prevButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                putRatingsCodes(new RequestCallback() {

                    public void onError(Request request, Throwable exception) {
                        infoLabel.setText(exception.getMessage());
                    }

                    public void onResponseReceived(Request request, Response response) {
                        if(STATUS_CODE_OK == response.getStatusCode()) {
                            // Data saved - now OK to change frame
                            BlocklyPanel.playbackPrev();
                        } else {
                            infoLabel.setText(response.getStatusText());
                        }
                    }
                });
            }
        });

        firstButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                putRatingsCodes(new RequestCallback() {

                    public void onError(Request request, Throwable exception) {
                        infoLabel.setText(exception.getMessage());
                    }

                    public void onResponseReceived(Request request, Response response) {
                        if(STATUS_CODE_OK == response.getStatusCode()) {
                            // Data saved - now OK to change frame
                            BlocklyPanel.playbackFirst();
                        } else {
                            infoLabel.setText(response.getStatusText());
                        }
                    }
                });
            }
        });

        lastButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                putRatingsCodes(new RequestCallback() {

                    public void onError(Request request, Throwable exception) {
                        infoLabel.setText(exception.getMessage());
                    }

                    public void onResponseReceived(Request request, Response response) {
                        if(STATUS_CODE_OK == response.getStatusCode()) {
                            // Data saved - now OK to change frame
                            BlocklyPanel.playbackLast();
                        } else {
                            infoLabel.setText(response.getStatusText());
                        }
                    }
                });
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
        codingPanelTemperature.add(flailingCheckBox);

        VerticalPanel panel = new VerticalPanel();
        panel.add(navigationPanel);
        panel.add(codingPanelDebug);
        panel.add(codingPanelTemperature);
        panel.add(startPanel);

        setWidget(panel);

    }

    private void clearCodes() {
        solutionDebugSK.setValue(false);
        solutionDebugS1.setValue(false);
        solutionDebugS2.setValue(false);
        solutionDebugLL.setValue(false);
        solutionTemperature.setValue(false);
        flailingCheckBox.setValue(false);
    }

    private static final String SK_key = "0_SHK";
    private static final String S1_key = "1_SP1";
    private static final String S2_key = "2_SP2";
    private static final String LL_key = "3_LL";
    private static final String Temperature_key = "4_TMP";
    private static final String AnySolution_key = "SOLUTION";
    private static final String flailing_key = "FLAIL";

    private JSONObject codesToJSON() {
        JSONObject codes = new JSONObject();

        codes.put(SK_key, JSONBoolean.getInstance(solutionDebugSK.getValue()));
        codes.put(S1_key, JSONBoolean.getInstance(solutionDebugS1.getValue()));
        codes.put(S2_key, JSONBoolean.getInstance(solutionDebugS2.getValue()));
        codes.put(LL_key, JSONBoolean.getInstance(solutionDebugLL.getValue()));
        codes.put(Temperature_key, JSONBoolean.getInstance(solutionTemperature.getValue()));

        codes.put(AnySolution_key, JSONBoolean.getInstance(
                        solutionDebugSK.getValue() || solutionDebugS1.getValue() ||
                        solutionDebugS2.getValue() || solutionDebugLL.getValue() ||
                        solutionTemperature.getValue()
        ));

        codes.put(flailing_key, JSONBoolean.getInstance(flailingCheckBox.getValue()));

        return codes;
    }

    private void loadCodesFromJSON(JSONObject codes){
        solutionDebugSK.setValue(codes.get(SK_key).isBoolean().booleanValue());
        solutionDebugS1.setValue(codes.get(S1_key).isBoolean().booleanValue());
        solutionDebugS2.setValue(codes.get(S2_key).isBoolean().booleanValue());
        solutionDebugLL.setValue(codes.get(LL_key).isBoolean().booleanValue());
        solutionTemperature.setValue(codes.get(Temperature_key).isBoolean().booleanValue());
        flailingCheckBox.setValue(codes.get(flailing_key).isBoolean().booleanValue());
    }

    private static final String baseURL = "https://mark-dissertation-coding.firebaseio.com";

    private static String buildURL(String projectID, String raterID, int frameNumber) {
        StringBuilder url = new StringBuilder(baseURL);
        url.append("/ratings/");
        url.append(projectID).append("/");
        url.append(raterID).append("/");
        url.append(frameNumber).append(".json");

        return url.toString();
    }
    private void getRatingsCodes() {
        // https://mark-dissertation-coding.firebaseio.com/ratings/d0/mark/1.json

        String url = buildURL(projectID, raterID, frameNumber);
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

        try {
            Request response = builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    infoLabel.setText(exception.getMessage());
                }

                public void onResponseReceived(Request request, Response response) {
                    if (STATUS_CODE_OK == response.getStatusCode()) {
                        // handle OK response from the server
                        if(response.getText().contentEquals("null")) {
                            clearCodes();
                        } else {
                            loadCodesFromJSON(JSONParser.parseStrict(response.getText()).isObject());
                        }
                    } else {
                        // handle non-OK response from the server
                        infoLabel.setText(response.getStatusText());
                    }
                }
            });
        } catch (RequestException e) {
            projectIdTextBox.setText(e.getMessage());
        }
    }

    private void putRatingsCodes(RequestCallback callback) {

        String url = buildURL(projectID, raterID, frameNumber);
        RequestBuilder builder = new RequestBuilder(RequestBuilder.PUT, url);
        String postData = codesToJSON().toString();

        try {
            builder.setHeader("Content-Type", "application/x-www-form-urlencoded");
            Request response = builder.sendRequest(postData, callback);
        } catch (RequestException e) {
            projectIdTextBox.setText(e.getMessage());
        }
    }

    private void checkRaterID() {
        raterIdOk = false;

        String url = "https://mark-dissertation-coding.firebaseio.com/raters/";
        url = url.concat(raterID).concat(".json");
        RequestBuilder builder = new RequestBuilder(RequestBuilder.GET, url);

        try {
            Request response = builder.sendRequest(null, new RequestCallback() {
                public void onError(Request request, Throwable exception) {
                    infoLabel.setText(exception.getMessage());
                }

                public void onResponseReceived(Request request, Response response) {
                    if (STATUS_CODE_OK == response.getStatusCode()) {
                        // handle OK response from the server
                        if(response.getText().contentEquals("true")) {
                            raterIdOk = true;
                        } else {
                            raterIdOk = false;
                            infoLabel.setText("Invalid RaterID.");
                        }
                    } else {
                        // handle non-OK response from the server
                        infoLabel.setText(response.getStatusText());
                    }
                    startPlayback();
                }
            });
        } catch (RequestException e) {
            infoLabel.setText(e.getMessage());
        }
    }

}
