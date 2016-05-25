package com.google.appinventor.components.runtime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList; 

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity; 
import android.util.Log;
import android.os.Handler;
import android.content.Context; 
import android.net.ConnectivityManager; 
import android.net.NetworkInfo; 

import com.google.appinventor.components.annotations.DesignerComponent;
import com.google.appinventor.components.annotations.DesignerProperty;
import com.google.appinventor.components.annotations.PropertyCategory;
import com.google.appinventor.components.annotations.SimpleEvent;
import com.google.appinventor.components.annotations.SimpleFunction;
import com.google.appinventor.components.annotations.SimpleObject;
import com.google.appinventor.components.annotations.SimpleProperty;
import com.google.appinventor.components.annotations.UsesLibraries;
import com.google.appinventor.components.annotations.UsesPermissions;
import com.google.appinventor.components.common.ComponentCategory;
import com.google.appinventor.components.common.PropertyTypeConstants;
import com.google.appinventor.components.common.YaVersion;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.YailList;

import edu.uml.cs.isense.api.API;
import edu.uml.cs.isense.api.UploadInfo;
import edu.uml.cs.isense.objects.RDataSet;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.objects.RProjectField;

@DesignerComponent(version = YaVersion.ISENSE_COMPONENT_VERSION,
    description = "A component that provides a high-level interface to iSENSEProject.org",
    category = ComponentCategory.SOCIAL,
    nonVisible = true,
    iconName = "images/isense.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET,android.permission.ACCESS_NETWORK_STATE")
@UsesLibraries(libraries = "isense.jar")

public final class iSENSE extends AndroidNonvisibleComponent implements Component {

  class DataObject {
    int projectId;
    JSONObject data;
    String dataName; 
    String conKey;
    String conName; 

    DataObject(int projectId, JSONObject data, String dataName) {
      this.projectId = projectId; 
      this.data = data; 
      this.dataName = dataName; 
    }

    DataObject(int projectId, JSONObject data, String dataName, String conKey, String conName) {
      this.projectId = projectId; 
      this.data = data;
      this.dataName = dataName; 
      this.conKey = conKey; 
      this.conName = conName; 
    }  
  }

  private int ProjectID;
  private int dataSetID = -1;
  private int mediaID = -1;
  private int LoginType;
  private String Email;
  private String Password;
  private String ContributorKey;
  private String YourName;
  private LinkedList<DataObject> pending; 
  private final API api;
  private final Handler androidUIHandler;
  private static Activity activity; 

  public iSENSE(ComponentContainer container) {
    super(container.$form());
    Log.i("iSENSE", "Starting? " + container.toString());
    LoginType(Component.iSENSE_LOGIN_TYPE_EMAIL + "");
    api = API.getInstance();
    ProjectID(-1); 
    Email(""); 
    Password(""); 
    ContributorKey(""); 
    YourName(""); 
    pending = new LinkedList<DataObject>(); 
    androidUIHandler = new Handler();
    activity = container.$context(); 

    // Create background thread to upload data sets
    androidUIHandler.post(new Runnable() {
      public void run() {
        while(true) {
          // check for internet connectivity
          ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE); 
          NetworkInfo mobi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE); 
          NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI); 
          if (mobi.isConnected() || wifi.isConnected()) { // we have data! 
            // Any data to upload? 
            if (pending.size() > 1) {
              DataObject dob = pending.peek(); 
              UploadInfo uInfo = new UploadInfo(); 
              int dataSetID; 
              // if no conKey, then email/password
              if (dob.conKey == null) {
                uInfo = api.uploadDataSet(dob.projectId, dob.data, dob.dataName); 
              } else { // contributor key
                Calendar cal = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss aaa");
                String date = " - " + sdf.format(cal.getTime()).toString();
                uInfo = api.uploadDataSet(dob.projectId, dob.data, dob.dataName, dob.conKey, dob.conName); 
              }
              dataSetID = uInfo.dataSetId; 
              Log.i("iSENSE", "JSON Upload: " + dob.data.toString()); 
              Log.i("iSENSE", "Dataset ID: " + dataSetID); 
              if (dataSetID == -1) {
                UploadDataSetFailed(); 
              } else { // else success! 
                pending.remove(); 
                UploadDataSetSucceeded(dataSetID); 
              }
            }
          } else { // no data connection; sleep for a second
            try {
              Thread.sleep(1000); 
            } catch (InterruptedException e) {} 
          }
        }
      }
    });
  }

  // Block Properties
  // ProjectID
  @SimpleProperty(description = "iSENSE Project ID", category = PropertyCategory.BEHAVIOR)
    public int ProjectID() {
      return ProjectID;
    }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description = "iSENSE Project ID", category = PropertyCategory.BEHAVIOR)
    public void ProjectID(int ProjectID) {
      this.ProjectID = ProjectID;
    }

  // UserName
  @SimpleProperty(description = "iSENSE Email", category = PropertyCategory.BEHAVIOR)
    public String Email() {
      return Email;
    }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description = "iSENSE Email", category = PropertyCategory.BEHAVIOR)
    public void Email(String Email) {
      this.Email = Email;
    }

  // Password
  @SimpleProperty(description = "iSENSE Password", category = PropertyCategory.BEHAVIOR)
    public String Password() {
      return Password;
    }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description = "iSENSE Password", category = PropertyCategory.BEHAVIOR)
    public void Password(String Password) {
      this.Password = Password;
    }

  // Contributor Key
  @SimpleProperty(description = "iSENSE Contributor Key", category = PropertyCategory.BEHAVIOR)
    public String ContributorKey() {
      return ContributorKey;
    }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description = "iSENSE Contributor Key", category = PropertyCategory.BEHAVIOR)
    public void ContributorKey(String ContributorKey) {
      this.ContributorKey = ContributorKey;
    }

  // Name
  @SimpleProperty(description = "iSENSE Your Name", category = PropertyCategory.BEHAVIOR)
    public String YourName() {
      return YourName;
    }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
    @SimpleProperty(description = "iSENSE Your Name", category = PropertyCategory.BEHAVIOR)
    public void YourName(String YourName) {
      this.YourName = YourName;
    }

  // Login Type
  @SimpleProperty(category = PropertyCategory.APPEARANCE,
      description = "This selects how you will login to iSENSEProject.org.  Either an email or a contributor key")
    public int LoginType() {
      return LoginType;
    }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_ISENSE_LOGIN_TYPE,
      defaultValue = Component.iSENSE_LOGIN_TYPE_EMAIL + "")
    @SimpleProperty(description = "iSENSE Login Type", category = PropertyCategory.APPEARANCE)
    public void LoginType(String LoginType) {
      this.LoginType = Integer.parseInt(LoginType);
    }

  // Block Functions
  // Upload Data Set in Background
  @SimpleFunction(description = "Upload Data Set to iSENSE")
    public void UploadDataSet(final String DataSetName, final YailList Fields, final YailList Data) {
      // Get fields from project
      ArrayList<RProjectField> projectFields = api.getProjectFields(ProjectID);
      JSONObject jData = new JSONObject();
      DataObject dob; 
      for (int i = 0; i < Fields.size(); i++) {
        for (int j = 0; j < projectFields.size(); j++) {
          if (Fields.get(i + 1).equals(projectFields.get(j).name)) {
            try {
              String sdata = Data.get(i + 1).toString();
              jData.put("" + projectFields.get(j).field_id, new JSONArray().put(sdata));
            } catch (JSONException e) {
              UploadDataSetFailed();
              e.printStackTrace();
              return;
            }
          }
        }
      }
      // login with email
      if (LoginType == iSENSE_LOGIN_TYPE_EMAIL) {
        RPerson user = api.createSession(Email, Password);
        if (user == null) {
          UploadDataSetFailed();
          return;
        }
        dob = new DataObject(ProjectID, jData, DataSetName); 
        // login with contribution key
      } else if (LoginType == iSENSE_LOGIN_TYPE_KEY) {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss aaa");
        String date = " - " + sdf.format(cal.getTime()).toString();
        dob = new DataObject(ProjectID, jData, DataSetName + date, ContributorKey, YourName); 
      } else {
        UploadDataSetFailed(); 
        return;
      }
      pending.add(dob); 
    }

  // Background thread? 


  // Upload Data Set Immediately
  @SimpleFunction(description = "Upload Data Set immediately to iSENSE")
    public void UploadDataSetImmediately(final String DataSetName, final YailList Fields, final YailList Data) {
      androidUIHandler.post(new Runnable() {
        public void run() {
          // Get fields from project
          ArrayList<RProjectField> projectFields = api.getProjectFields(ProjectID);
          JSONObject jData = new JSONObject();
          UploadInfo uInfo = new UploadInfo(); 
          for (int i = 0; i < Fields.size(); i++) {
            for (int j = 0; j < projectFields.size(); j++) {
              if (Fields.get(i + 1).equals(projectFields.get(j).name)) {
                try {
                  String sdata = Data.get(i + 1).toString();
                  jData.put("" + projectFields.get(j).field_id, new JSONArray().put(sdata));
                } catch (JSONException e) {
                  UploadDataSetImmediatelyFailed();
                  e.printStackTrace();
                  return;
                }
              }
            }
          }
          // login with email
          if (LoginType == iSENSE_LOGIN_TYPE_EMAIL) {
            RPerson user = api.createSession(Email, Password);
            if (user == null) {
              UploadDataSetImmediatelyFailed();
              return;
            }
            uInfo = api.uploadDataSet(ProjectID, jData, DataSetName);
            // login with contribution key
          } else if (LoginType == iSENSE_LOGIN_TYPE_KEY) {
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss aaa");
            String date = " - " + sdf.format(cal.getTime()).toString();
            uInfo = api.uploadDataSet(ProjectID,
                jData,
                DataSetName + date,
                ContributorKey,
                YourName);
          }
          dataSetID = uInfo.dataSetId;
          Log.i("iSENSE", "JSON Upload: " + jData.toString());
          Log.i("iSENSE", "Dataset ID: " + dataSetID);
          if (dataSetID == -1) {
            UploadDataSetImmediatelyFailed();
          } else {
            UploadDataSetImmediatelySucceeded(dataSetID); 
          }
        }
      });
    }

  // Get Dataset By Field
  @SimpleFunction(description = "Get the Data Sets for the current project")
    public YailList GetDataSetsByField(final String Field) {
      ArrayList<String> result = api.getDataSetsByField(ProjectID, Field);
      return YailList.makeList(result); 
    }

  // Get Time (formatted for iSENSE Upload)
  @SimpleFunction(description = "Gets the current time. It is formated correctly for iSENSE")
    public String GetTime() {
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
      return sdf.format(cal.getTime()).toString();
    }

  // Upload Photo To Dataset
  @SimpleFunction(description = "Uploads a photo to a dataset")
    public void UploadPhotoToDataSet(final int DataSetID, final String Photo) {
      androidUIHandler.post(new Runnable() {
        public void run() {
          java.io.File pic = new java.io.File(android.net.Uri.parse(Photo).getPath());
          UploadInfo uInfo = new UploadInfo();
          if (!pic.exists()) {
            UploadPhotoToDataSetFailed();
            return;
          }
          // if !exists return with error
          // login with email
          if (LoginType == iSENSE_LOGIN_TYPE_EMAIL) {
            RPerson user = api.createSession(Email, Password);
            if (user == null) {
              UploadPhotoToDataSetFailed(); 
              return;
            }
            uInfo = api.uploadMedia(DataSetID, pic, API.TargetType.DATA_SET);
            // login with contribution key
          } else if (LoginType == iSENSE_LOGIN_TYPE_KEY) {
            uInfo = api.uploadMedia(DataSetID,
              pic,
              API.TargetType.DATA_SET,
              ContributorKey,
              YourName);
          }
          mediaID = uInfo.mediaId;
          Log.i("iSENSE", "MediaID: " + mediaID);
          if (mediaID == -1) {
            UploadPhotoToDataSetFailed();
          } else {
            UploadPhotoToDataSetSucceeded(mediaID);
          }
        }
      });
    }

  @SimpleFunction(description = "logcat")
    public void LogToCat(String catify) {
      Log.i("iSENSE", catify);
    }

  // Block Events
  @SimpleEvent(description = "iSENSE Immediate Upload Data Set Succeeded")
    public void UploadDataSetImmediatelySucceeded(int datasetId) {
      EventDispatcher.dispatchEvent(this, "UploadDataSetImmediatelySucceeded", datasetId);
    }

  @SimpleEvent(description = "iSENSE Immediate Upload Data Set Failed")
    public void UploadDataSetImmediatelyFailed() {
      EventDispatcher.dispatchEvent(this, "UploadDataSetImmediatelyFailed");
    }

  @SimpleEvent(description = "iSENSE Upload Data Set Succeeded")
    public void UploadDataSetSucceeded(int datasetId) {
      EventDispatcher.dispatchEvent(this, "UploadDataSetSucceeded", datasetId);
    }

  @SimpleEvent(description = "iSENSE Upload Data Set Failed")
    public void UploadDataSetFailed() {
      EventDispatcher.dispatchEvent(this, "UploadDataSetFailed");
    }

  @SimpleEvent(description = "iSENSE Upload Photo To Data Set Succeeded")
    public void UploadPhotoToDataSetSucceeded(int datasetId) {
      EventDispatcher.dispatchEvent(this, "UploadPhotoToDataSetSucceeded", datasetId);
    }

  @SimpleEvent(description = "iSENSE Upload Photo To Data Set Failed")
    public void UploadPhotoToDataSetFailed() {
      EventDispatcher.dispatchEvent(this, "UploadPhotoToDataSetFailed");
    }
}
