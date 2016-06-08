package com.google.appinventor.components.runtime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList; 
import java.io.File; 
import java.net.URL; 

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity; 
import android.util.Log;
import android.os.Handler;
import android.content.Context; 
import android.net.ConnectivityManager; 
import android.net.NetworkInfo; 
import android.os.AsyncTask; 
import android.net.Uri; 

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
import com.google.appinventor.components.runtime.util.MediaUtil; 

import edu.uml.cs.isense.api.API;
import edu.uml.cs.isense.api.UploadInfo;
import edu.uml.cs.isense.objects.RDataSet;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.objects.RProjectField;


@DesignerComponent(version = YaVersion.ISENSEPUBLISHER_COMPONENT_VERSION,
    description = "A component that provides a high-level interface to iSENSEProject.org",
    category = ComponentCategory.CONNECTIVITY,
    nonVisible = true,
    iconName = "images/isense.png")
@SimpleObject
@UsesPermissions(permissionNames = "android.permission.INTERNET,android.permission.ACCESS_NETWORK_STATE")
@UsesLibraries(libraries = "isense.jar")

public final class iSENSEPublisher extends AndroidNonvisibleComponent implements Component {

  private int ProjectID;
  private int dataSetID = -1;
  private int mediaID = -1;
  private String ContributorKey;
  private LinkedList<DataObject> pending; 
  private final API api;
  private final Handler androidUIHandler;
  private static Activity activity; 
  private static Form form; 

  public iSENSEPublisher(ComponentContainer container) {
    super(container.$form());
    Log.i("iSENSE", "Starting? " + container.toString());
    api = API.getInstance();
    ProjectID(-1); 
    ContributorKey(""); 
    pending = new LinkedList<DataObject>(); 
    androidUIHandler = new Handler();
    activity = container.$context(); 
    form = container.$form(); 
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

  // Block Functions
  // Upload Data Set in Background
  @SimpleFunction(description = "Upload Data Set to iSENSE")
    public void UploadDataSet(final String DataSetName, final YailList Fields, final YailList Data) {
      // ensure that the lists are the same size 
      if (Fields.size() != Data.size()) {
        UploadDataSetFailed(); 
      } 
      // Create new "DataObject" and add to upload queue
      DataObject dob = new DataObject(DataSetName, Fields, Data);
      pending.add(dob);  
      new UploadTask().execute(); 
    }

  // Private class that gives us a data structure with info for uploading a dataset
  class DataObject {

    String name; 
    YailList fields; 
    YailList data; 

    DataObject(String name, YailList fields, YailList data) {
      this.name = name; 
      this.fields = fields;
      this.data = data; 
    }
  }

  // Private asynchronous task class that allows background uploads
  private class UploadTask extends AsyncTask<Void, Void, Integer> {

    // This is what actually runs in the background thread, so it's safe to block
    protected Integer doInBackground(Void... v) {

      // Sleep while we don't have a wifi connection or a mobile connection
      ConnectivityManager cm = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE); 

      boolean wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected(); 
      boolean mobi = false; 

      if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null) {
        mobi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected(); 
      }

      while (!(wifi||mobi)) {
        try {
          Log.i("iSENSE", "No internet connection; sleeping for one second"); 
          Thread.sleep(1000); 
        } catch (InterruptedException e) {
          Log.e("iSENSE", "Thread Interrupted!"); 
        }
        wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected(); 
        if (cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE) != null) { 
          mobi = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnected(); 
        }
      } 

      // Active internet connection detected; proceed with upload 
      DataObject dob = pending.peek(); 
      UploadInfo uInfo = new UploadInfo(); 

      // Get fields from project
      ArrayList<RProjectField> projectFields = api.getProjectFields(ProjectID);
      JSONObject jData = new JSONObject();
      for (int i = 0; i < dob.fields.size(); i++) {
        for (int j = 0; j < projectFields.size(); j++) {
          if (dob.fields.get(i + 1).equals(projectFields.get(j).name)) {
            try {
              String sdata = dob.data.get(i + 1).toString();
              jData.put("" + projectFields.get(j).field_id, new JSONArray().put(sdata));
            } catch (JSONException e) {
              UploadDataSetFailed();
              e.printStackTrace();
              return -1;
            }
          }
        }
      }

      // login with contributor key
      Calendar cal = Calendar.getInstance();
      SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss aaa");
      String date = " - " + sdf.format(cal.getTime()).toString();
      uInfo = api.uploadDataSet(ProjectID, jData, dob.name + date, ContributorKey, form.Title()); 

      int dataSetId = uInfo.dataSetId; 
      Log.i("iSENSE", "JSON Upload: " + jData.toString()); 
      Log.i("iSENSE", "Dataset ID: " + dataSetId); 
      return dataSetId; 
    } 

    // After background thread execution, UI handler runs this 
    protected void onPostExecute(Integer result) {
      DataObject dob = pending.remove();
      if (result == -1) {
        UploadDataSetFailed(); 
      } else {
        UploadDataSetSucceeded(result); 
      }
    }

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

  // Get Number of Pending Uploads (Advanced Feature)
  @SimpleFunction(description = "Gets number of pending background uploads. Advanced feature.")
    public int GetNumberPendingUploads() {
      return pending.size(); 
    }

  // Upload Photo To Dataset
  // Note that this function has never worked correctly and is still in development
  @SimpleFunction(description = "Uploads a photo to a dataset")
    public void UploadPhotoToDataSet(final int DataSetID, final String Photo) {
      androidUIHandler.post(new Runnable() {
        public void run() {
          // Validate photo
          String path = ""; 
          String[] pathtokens = Photo.split("/"); 
          // If camera photo 
          if (pathtokens[0].equals("file:")) {
            try {
              path = new File(new URL(Photo).toURI()).getAbsolutePath(); 
            } catch (Exception e) {
              Log.e("iSENSE", "Malformed URL or URI!"); 
            }
          } else { // Assets photo
            path = "/sdcard/AppInventor/assets/" + Photo; 
          } 
          File pic = new File(path); 
          if (!pic.exists()) {
            Log.e("iSENSE", "picture does not exist!"); 
            UploadPhotoToDataSetFailed();
            return;
          }
          Log.i("iSENSE", "Trying to upload: " + path); 
          UploadInfo uInfo = new UploadInfo(); 
          uInfo = api.uploadMedia(DataSetID,
              pic,
              API.TargetType.DATA_SET,
              ContributorKey,
              form.Title());
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

  @SimpleEvent(description = "iSENSE Upload Data Set Succeeded")
    public void UploadDataSetSucceeded(int datasetId) {
      EventDispatcher.dispatchEvent(this, "UploadDataSetSucceeded", datasetId);
    }

  @SimpleEvent(description = "iSENSE Upload Data Set Failed")
    public void UploadDataSetFailed() {
      EventDispatcher.dispatchEvent(this, "UploadDataSetFailed");
    }

  @SimpleEvent(description = "iSENSE Upload Photo To Data Set Succeeded")
    public void UploadPhotoToDataSetSucceeded(int mediaId) {
      EventDispatcher.dispatchEvent(this, "UploadPhotoToDataSetSucceeded", mediaId);
    }

  @SimpleEvent(description = "iSENSE Upload Photo To Data Set Failed")
    public void UploadPhotoToDataSetFailed() {
      EventDispatcher.dispatchEvent(this, "UploadPhotoToDataSetFailed");
    }
}
