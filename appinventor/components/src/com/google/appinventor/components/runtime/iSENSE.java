package com.google.appinventor.components.runtime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

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
import com.google.appinventor.components.runtime.util.AsyncCallbackPair;
import com.google.appinventor.components.runtime.util.AsynchUtil;
import com.google.appinventor.components.runtime.util.YailList;

import edu.uml.cs.isense.api.API;
import edu.uml.cs.isense.api.UploadInfo;
import edu.uml.cs.isense.objects.RDataSet;
import edu.uml.cs.isense.objects.RPerson;
import edu.uml.cs.isense.objects.RProjectField;

@UsesPermissions(permissionNames = "android.permission.INTERNET")
@UsesLibraries(libraries = "isense.jar, httpmime.jar")
@DesignerComponent(version = YaVersion.ISENSE_COMPONENT_VERSION,
                   description = "A component that provides a high-level interface to iSENSEProject.org",
                   category = ComponentCategory.SOCIAL,
                   nonVisible = true,
                   iconName = "images/isense.png")
@SimpleObject
public class iSENSE extends AndroidNonvisibleComponent implements Component {
  private UploadInfo uInfo;
  private String Email;
  private String Password;
  private String ContributorKey;
  private String YourName;
  private API api;
  private int LoginType = -1;
  private Handler handler;

  public iSENSE(ComponentContainer container) {
    super(container.$form());
    Log.i("iSENSE", "Starting? " + container.toString());
    LoginType(Component.iSENSE_LOGIN_TYPE_EMAIL + "");
    api = API.getInstance();
    handler = new Handler();
    uInfo = new UploadInfo();
  }

  // Block Properties
  // ProjectID
  @SimpleProperty(description = "iSENSE Project ID", category = PropertyCategory.BEHAVIOR)
  public int ProjectID() {
    return uInfo.projectId;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty
  public void ProjectID(int ProjectID) {
    uInfo.projectId = ProjectID;
  }

  // UserName
  @SimpleProperty(description = "iSENSE Email", category = PropertyCategory.BEHAVIOR)
  public String Email() {
    return Email;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty
  public void Email(String Email) {
    this.Email = Email;
  }

  // Password
  @SimpleProperty(description = "iSENSE Password", category = PropertyCategory.BEHAVIOR)
  public String Password() {
    return Password;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty
  public void Password(String Password) {
    this.Password = Password;
  }

  // Contributor Key
  @SimpleProperty(description = "iSENSE Contributor Key", category = PropertyCategory.BEHAVIOR)
  public String ContributorKey() {
    return ContributorKey;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty
  public void ContributorKey(String ContributorKey) {
    this.ContributorKey = ContributorKey;
  }

  // Name
  @SimpleProperty(description = "iSENSE Your Name", category = PropertyCategory.BEHAVIOR)
  public String YourName() {
    return YourName;
  }

  @DesignerProperty(editorType = PropertyTypeConstants.PROPERTY_TYPE_STRING, defaultValue = "")
  @SimpleProperty
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
  @SimpleProperty
  public void LoginType(String LoginType) {
    this.LoginType = Integer.parseInt(LoginType);
  }

  // Block Functions
  // Upload Data Set
  @SimpleFunction(description = "Upload Data Set to iSENSE")
  public void UploadDataSet(final String DataSetName, final YailList Fields, final YailList Data) {
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        // Get fields from project
        ArrayList<RProjectField> projectFields = api.getProjectFields(uInfo.projectId);
        JSONObject jData = new JSONObject();
        for (int i = 0; i < Fields.size(); i++) {
          for (int j = 0; j < projectFields.size(); j++) {
            if (Fields.get(i + 1).equals(projectFields.get(j).name)) {
              try {
                String sdata = Data.get(i + 1).toString();
                jData.put("" + projectFields.get(j).field_id, new JSONArray().put(sdata));
              } catch (JSONException e) {
                UploadDataSetResult();
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
            UploadDataSetResult();
            return;
          }
          uInfo = api.uploadDataSet(uInfo.projectId, jData, DataSetName);
          // login with contribution key
        } else if (LoginType == iSENSE_LOGIN_TYPE_KEY) {
          Calendar cal = Calendar.getInstance();
          SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy HH:mm:ss aaa");
          String date = " - " + sdf.format(cal.getTime()).toString();
          uInfo = api.uploadDataSet(uInfo.projectId,
              jData,
              DataSetName + date,
              ContributorKey,
              YourName);
        }
        Log.i("iSENSE", "JSON Upload: " + jData.toString());
        Log.i("iSENSE", "Dataset ID: " + uInfo.dataSetId);
        UploadDataSetResult();
      }
    });
  }

  // Upload Dataset Result (calls events in UI thread)
  private void UploadDataSetResult() {
    AsyncCallbackPair<Integer> myCallback = new AsyncCallbackPair<Integer>() {
      public void onSuccess(final Integer result) {
        handler.post(new Runnable() {
          public void run() {
            UploadDataSetSucceeded(result);
          }
        });
      }

      public void onFailure(final String message) {
        handler.post(new Runnable() {
          public void run() {
            UploadDataSetFailed(message);
          }
        });
      }
    };
    if (uInfo.dataSetId == -1) {
      myCallback.onFailure(uInfo.errorMessage);
    } else {
      myCallback.onSuccess(uInfo.dataSetId);
    }
  }

  // Get Dataset By Field
  @SimpleFunction(description = "Get the Data Sets for the current project")
  public YailList GetDataSetsByField(final String Field) {
    String FieldID = null;
    ArrayList<RDataSet> project_data = api.getDataSets(uInfo.projectId);
    ArrayList<RDataSet> rdata = new ArrayList<RDataSet>();
    ArrayList<RProjectField> projectFields = api.getProjectFields(uInfo.projectId);
    ArrayList<String> fdata = new ArrayList<String>();
    for (RProjectField f : projectFields) {
      if (f.name.equals(Field)) {
        FieldID = f.field_id + "";
      }
    }
    for (RDataSet r : project_data) {
      rdata.add(api.getDataSet(r.ds_id));
    }
    for (RDataSet r : rdata) {
      try {
        Log.i("iSENSE", "fdata:" + r.data.getString(FieldID));
        JSONArray jadata = new JSONArray();
        jadata = r.data.getJSONArray(FieldID);
        for (int i = 0; i < jadata.length(); i++) {
          fdata.add(jadata.getString(i));
        }
      } catch (JSONException e) {
        e.printStackTrace();
      }
    }
    return YailList.makeList(fdata);
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
    AsynchUtil.runAsynchronously(new Runnable() {
      public void run() {
        java.io.File pic = new java.io.File(android.net.Uri.parse(Photo).getPath());
        if (!pic.exists()) {
          UploadPhotoToDataSetResult();
          return;
        }
        // if !exists return with error
        // login with email
        if (LoginType == iSENSE_LOGIN_TYPE_EMAIL) {
          RPerson user = api.createSession(Email, Password);
          if (user == null) {
            UploadPhotoToDataSetResult();
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
        Log.i("iSENSE", "MediaID: " + uInfo.mediaId);
        UploadPhotoToDataSetResult();
      }
    });
  }

  // Upload Photo to Dataset Result (calls events in UI thread)
  private void UploadPhotoToDataSetResult() {
    AsyncCallbackPair<Integer> myCallback = new AsyncCallbackPair<Integer>() {
      public void onSuccess(final Integer result) {
        handler.post(new Runnable() {
          public void run() {
            UploadPhotoToDataSetSucceeded(result);
          }
        });
      }

      public void onFailure(final String result) {
        handler.post(new Runnable() {
          public void run() {
            UploadPhotoToDataSetFailed(result);
          }
        });
      }
    };
    if (uInfo.mediaId == -1) {
      myCallback.onFailure(uInfo.errorMessage);
    } else {
      myCallback.onSuccess(uInfo.dataSetId);
    }
  }

  @SimpleFunction(description = "logcat")
  public void LogToCat(String catify) {
    Log.i("iSENSE", catify);
  }

  // Block Events
  @SimpleEvent(description = "iSENSE Upload DataSet Succeeded")
  public void UploadDataSetSucceeded(int result) {
    EventDispatcher.dispatchEvent(this, "UploadDataSetSucceeded", result);
  }

  @SimpleEvent(description = "iSENSE Upload DataSet Failed")
  public void UploadDataSetFailed(String result) {
    EventDispatcher.dispatchEvent(this, "UploadDataSetFailed", result);
  }

  @SimpleEvent(description = "iSENSE Upload Photo To DataSet Succeeded")
  public void UploadPhotoToDataSetSucceeded(int result) {
    EventDispatcher.dispatchEvent(this, "UploadPhotoToDataSetSucceeded", result);
  }

  @SimpleEvent(description = "iSENSE Upload Photo To DataSet Failed")
  public void UploadPhotoToDataSetFailed(String result) {
    EventDispatcher.dispatchEvent(this, "UploadPhotoToDataSetFailed", result);
  }
}
