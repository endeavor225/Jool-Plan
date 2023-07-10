package com.jool.plugin.mapping.mission;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.jool.plugin.mapping.interfaces.OnAsyncGridProcessComplete;
import com.jool.plugin.mapping.interfaces.OnAsyncOperationComplete;
import com.jool.plugin.mapping.interfaces.OnAsyncOperationCompleteBoolArray;
import com.jool.plugin.mapping.interfaces.OnAsyncSplitComplete;
import com.jool.plugin.mapping.interfaces.OnMissionProcessingComplete;
import com.jool.plugin.mapping.interfaces.OnMissionSplitComplete;
import com.jool.plugin.mapping.mapping.PointLatLngAlt;
import com.jool.plugin.mapping.utils.Constants;
import com.jool.plugin.mapping.utils.Utils;
import com.jool.plugin.mapping.utils.underscore.lodash.U;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;


public class MissionStep {
    private final String TAG = getClass().getSimpleName();
    private volatile CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> lists = new CopyOnWriteArrayList<>();
    public synchronized boolean hasNext(){
        return lists.size() > 0;
    }
    public boolean removeFirstOk(){
        try {
            synchronized (this){
                if (hasNext()){
                    lists.remove(0);
                    return true;
                }else {
                    return false;
                }
            }

        }catch (Exception e){
            Utils.toLog(TAG,"removeFirstOk",null, e);
        }catch (Error er){
            Utils.toLog(TAG,"removeFirstOk",er,null);
        }
        return false;
    }

    public synchronized int getSize()throws Exception,Error{
        return lists.size();
    }
    public CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> getMainList(){
        return lists;
    }
    public synchronized @Nullable CopyOnWriteArrayList<LatLng> getFirst() throws Exception,Error{
        if (hasNext()){
            return lists.get(0);
        }
        return null;
    }
    public boolean addOk(@NonNull CopyOnWriteArrayList<LatLng> newList){
        synchronized (this){
            try {
                if (Utils.isNull(newList)){
                    return false;
                }else {
                    if (newList.size() > Constants.MAX_WAYPOINT_ITEMS){
                        return false;
                    }else {
                        if (Utils.isNull(lists)){
                            return false;
                        }else {
                            lists.add(newList);
                            return true;
                        }

                    }
                }
            }catch (Exception e){
                Utils.toLog(TAG, "addOk",null,e);

            }catch (Error er){
                Utils.toLog(TAG, "addOk",er,null);
            }
            return false;
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void addAllOk(final @NonNull CopyOnWriteArrayList<LatLng> flyPoints, final @NonNull OnAsyncGridProcessComplete callBack){
        Utils.AsyncExecute(() ->{
            try {
                synchronized (MissionStep.this){
                    if (Utils.isNull(callBack)){
                        Utils.toLog(TAG, " addAllOk callBack is Null");
                        return;
                    }else {
                        if (Utils.isNull(flyPoints)){
                            callBack.onError("");
                            Utils.toLog(TAG, " addAllOk grid is Null");
                            return;
                        }else {
                            if (flyPoints.size() == 0){
                                callBack.onError("");
                                Utils.toLog(TAG, " addAllOk grid is Empty");
                                return;
                            }else {
                                // Create Array Copy Deep Copy
                                CopyOnWriteArrayList<LatLng> cloneList = new CopyOnWriteArrayList<>();
                                cloneList = copyCollectorArray(flyPoints);
                                if (cloneList.size() == 0){
                                    callBack.onError("");
                                    Utils.toLog(TAG, " addAllOk cloneList is Empty");
                                    return;
                                }else {
                                    // Split 3 array or 99 item
                                    // Case First Time use Empty bigAreaLeft
                                  lists.add(copyCollectorArray(cloneList));
                                  to99And120Hect(null, null, new OnMissionSplitComplete() {
                                    @Override
                                    public void onError(@Nullable String errorDetail) {
                                      if (Utils.isNull(errorDetail)){
                                        errorDetail = "";
                                      }
                                      callBack.onError("");
                                      Utils.toLog(TAG, " addAllOk to99And120Hect error is:"+errorDetail);
                                      return;
                                    }

                                    @Override
                                    public void onPerformSaveAndContinu(MissionStep himSelf, @NonNull final CopyOnWriteArrayList<LatLng> flyPointsAfterRebuild) {
                                      // Will Not Fired cause saveInPref = null = false we will save
                                      // later
                                    }

                                    @Override
                                    public void onSucces(final MissionStep himSelf, @NonNull final CopyOnWriteArrayList<LatLng> flyPointsAfterRebuild) {
                                      try {
                                        // After copy all item into lists  in to99And120Hect
                                        if (Utils.isNull(lists)){
                                          callBack.onError("");
                                          Utils.toLog(TAG, " addAllOk lists MainList Is null");
                                          return;
                                        }else {
                                          if (!isValid()){
                                            callBack.onError("");
                                            Utils.toLog(TAG, " addAllOk This is not valid");
                                            return;
                                          }else {
                                            callBack.onSucces(flyPointsAfterRebuild);
                                          }
                                        }
                                      }catch (Exception e){
                                        Utils.toLog(TAG, "addAllOk",null,e);
                                      }catch (Error er){
                                        Utils.toLog(TAG, "addAllOk",er,null);
                                      }
                                    }
                                  });

                                }


                            }
                        }
                    }
                }
            }catch (Exception e){
                Utils.toLog(TAG, "addAllOk",null,e);

            }catch (Error er){
                Utils.toLog(TAG, "addAllOk",er,null);
            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.N)
    public boolean isValid(){
        synchronized (this){
            try {
                if (hasNext()){
                    AtomicInteger badItems = new AtomicInteger();
                    for (CopyOnWriteArrayList<LatLng> item: lists) {
                        if (Utils.isNull(item)){
                            Utils.toLog(TAG,"isValid  item Is Null");
                            badItems.getAndIncrement();
                        }else {
                            for (LatLng son: item) {
                                if (Utils.isNull(son)){
                                    Utils.toLog(TAG,"isValid  item son is Null");
                                    badItems.getAndIncrement();
                                }
                            }
                        }
                        if (item.size() > Constants.MAX_WAYPOINT_ITEMS){
                            Utils.toLog(TAG,"isValid  item Is MAX_WAYPOINT_ITEMS Reached Above 99 Points for Waymission 1.0");
                            badItems.getAndIncrement();
                        }
                    }
                    if (badItems.get() > 0){
                        return false;
                    }else {
                        return true;
                    }
                }else {
                    toLog("isValid  Dont have Next ");
                }
            }catch (Exception e){
                Utils.toLog(TAG, "isValid",null,e);
            }catch (Error er){
                Utils.toLog(TAG, "isValid",er,null);
            }
            return false;
        }
    }

    public void onDestroy(){
        synchronized (this){
            try {
                if (Utils.isNull(lists)){
                    return;
                }else {
                    lists.clear();
                }
            }catch (Exception e){
                Utils.toLog(TAG, "onDestroy",null,e);
            }catch (Error er){
                Utils.toLog(TAG, "onDestroy",er,null);
            }

        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void fillFromString(@NonNull String data,@NonNull OnMissionProcessingComplete callBack)throws Exception,Error{
        Utils.AsyncExecute(() ->{
            synchronized (this){
                try {
                    if (Utils.isNull(callBack)){
                        Utils.toLog(TAG,"fillFromString  item Is callBack");
                        return;
                    }else {
                        try {
                            if (Utils.isNullOrEmpty(data)){
                                callBack.onError();
                                Utils.toLog(TAG,"fillFromString  data Is Null or Empty");
                                return;
                            }else {
                                JSONArray arrayMain = new JSONArray(data);
                                if (Utils.isNull(arrayMain)){
                                    callBack.onError();
                                    Utils.toLog(TAG,"fillFromString  arrayMain Is Null");
                                    return;
                                }else {
                                    for(int i = 0; i<arrayMain.length(); i++){
                                        JSONArray itemArray = arrayMain.getJSONArray(i);
                                        if (Utils.isNull(itemArray)){
                                            // skip
                                        }else {
                                            CopyOnWriteArrayList<LatLng> arrayOfLatLng =  new CopyOnWriteArrayList<>();
                                            for (int a = 0; a<itemArray.length(); a++){
                                                JSONObject obj = itemArray.getJSONObject(a);
                                                if (Utils.isNull(obj)){
                                                    // skip
                                                }else {
                                                    double latitude = obj.getDouble(Constants.JSON_KEY_LATITUDE);
                                                    double longitude = obj.getDouble(Constants.JSON_KEY_LONGITUDE);
                                                    LatLng latLng = new LatLng(latitude,longitude);
                                                    arrayOfLatLng.add(latLng);
                                                }
                                            }
                                            lists.add(arrayOfLatLng);
                                        }
                                    }
                                    if (!hasNext()){
                                        Utils.toLog(TAG,"fillFromString  list is Empty");
                                        callBack.onError();
                                        return;
                                    }else {
                                        callBack.onSucces(this);
                                        return;
                                    }

                                }
                            }
                        }catch (Exception e){
                            Utils.toLog(TAG, "fillFromString",null,e);
                        }catch (Error er){
                            Utils.toLog(TAG, "fillFromString",er,null);
                        }
                    }
                }catch (Exception e){
                    Utils.toLog(TAG, "fillFromString",null,e);
                }catch (Error er){
                    Utils.toLog(TAG, "fillFromString",er,null);
                }

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void toStringJSON(@NonNull OnAsyncOperationComplete callBack)throws Exception,Error{
        Utils.AsyncExecute(() ->{
            try {
                synchronized (this){
                    if (Utils.isNull(callBack)){
                        Utils.toLog(TAG,"toStringJSON callBack is Null");
                    }else {
                        String resNull = null;
                        if (!hasNext()){
                            callBack.onError("");
                            Utils.toLog(TAG,"toStringJSON  items Is Empty");
                            return;
                        }else {
                            JSONArray arrayMain = new JSONArray();
                            lists.forEach(item ->{
                                JSONArray itemArray = new JSONArray();
                                item.forEach(sonLatlng ->{
                                    JSONObject obj = new JSONObject();
                                    try {
                                        obj.put(Constants.JSON_KEY_LATITUDE,sonLatlng.latitude);
                                        obj.put(Constants.JSON_KEY_LONGITUDE,sonLatlng.longitude);
                                        itemArray.put(obj);
                                    }
                                    catch (Exception e){
                                        Utils.toLog(TAG, "toStringJSON",null,e);
                                    }catch (Error er){
                                        Utils.toLog(TAG, "toStringJSON",er,null);
                                    }
                                });
                                arrayMain.put(itemArray);
                            });
                            callBack.onSucces(arrayMain.toString());
                            return;
                        }
                    }

                }
            }catch (Exception e){
                Utils.toLog(TAG, "toStringJSON",null,e);
            }catch (Error er){
                Utils.toLog(TAG, "toStringJSON",er,null);
            }
        });


    }

    public void clearAll() {
        synchronized (this){
            try {
                lists.clear();
            }catch (Exception e){
                Utils.toLog(TAG, "clearAll",null,e);
            }catch (Error er){
                Utils.toLog(TAG, "clearAll",er,null);
            }
        }
    }

    public void replaceFirst(CopyOnWriteArrayList<LatLng> allpoints) {
        try {
            lists.set(0,allpoints);
        }catch (Exception e){
            Utils.toLog(TAG, "replaceFirst",null,e);
        }catch (Error er){
            Utils.toLog(TAG, "replaceFirst",er,null);
        }

    }

    private void toLog( @NonNull String msg){
        Utils.toLog(TAG, msg);
    }

  public void to99And120Hect(@Nullable Boolean caseErrorOrLowBattery,@Nullable Boolean saveInPref, @NonNull OnMissionSplitComplete callBack){
    if (Utils.isNull(caseErrorOrLowBattery)){
      caseErrorOrLowBattery = false;
    }
    if(Utils.isNull(saveInPref)){
      saveInPref = false;
    }
    Boolean finalCaseErrorOrLowBattery = caseErrorOrLowBattery;
    Boolean finalSaveInPref = saveInPref;
    Utils.AsyncExecute(() ->{
      if (Utils.isNull(callBack)){
        toLog("to99And120Hect callBack is Null ");
        return;
      }else {
        try {
          if (lists.size() == 0){
            if (finalCaseErrorOrLowBattery){
              //All Step Done do Nothing callBackSucces
              callBack.onSucces(MissionStep.this, new CopyOnWriteArrayList<>());
              return;
            }else {
              toLog("to99And120Hect Empty Main List");
              callBack.onError("Empty Main List");
              return;
            }
          }else {
            CopyOnWriteArrayList<LatLng> mainCopy = new CopyOnWriteArrayList<>();
            for (CopyOnWriteArrayList<LatLng> arrayItem: lists) {
              for (LatLng latLongItem:arrayItem){
                // Fix Same Address Pointer by Making Copy
                LatLng item = new LatLng(Utils.clone(latLongItem.latitude), Utils.clone(latLongItem.longitude));
                mainCopy.add(item);
              }
            }
            // Clean Main List lists.clear();
            clearAll();

            if (mainCopy.size() == 0){
              toLog("to99And120Hect mainCopy Should No be empty");
              callBack.onError("mainCopy Should No be empty");
              return;
            }else {
              final CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> listOfArray = new CopyOnWriteArrayList<>();
              CopyOnWriteArrayList<LatLng> listAfterAdd = new CopyOnWriteArrayList<>();
              CopyOnWriteArrayList<LatLng> listBeforeAdd = new CopyOnWriteArrayList<>();
              for(int i = 0; i<mainCopy.size(); i++){
                LatLng current = mainCopy.get(i);
                LatLng cloneCurrent = new LatLng(Utils.clone(current.latitude), Utils.clone(current.longitude));
                listAfterAdd.add(cloneCurrent);
                double afterArea = 0;
                if (listAfterAdd.size() > 2){
                  // Min 3 point to Compute area
                  afterArea = getArea(listAfterAdd);
                }
                if (listAfterAdd.size() == Constants.MAX_WAYPOINT_ITEMS || afterArea >= Constants.DJI_SDK_MAX_AREA){
                  listOfArray.add(copyCollectorArray(listBeforeAdd));
                  listBeforeAdd.clear();
                  listBeforeAdd= null;
                  listBeforeAdd = new CopyOnWriteArrayList<>();

                  listAfterAdd.clear();
                  listAfterAdd = null;
                  listAfterAdd = new CopyOnWriteArrayList<>();

                  listBeforeAdd.add(cloneCurrent);
                }else {
                  listBeforeAdd.add(cloneCurrent);
                }
              }
              if (listBeforeAdd.size() > 0){
                listOfArray.add(copyCollectorArray(listBeforeAdd));
                listBeforeAdd.clear();
                listBeforeAdd = null;
              }
              // Clean Main List lists.clear();
              clearAll();
              for (CopyOnWriteArrayList<LatLng> array: listOfArray) {
                boolean isAddedToMain = addOk(array);
                if (!isAddedToMain){
                  toLog(" convert120hTo97WaypointArray AddItem Failed");
                  callBack.onError("convert120hTo97WaypointArray AddItem Failed");
                  return;
                }
              }
              // Reverse Order Start by Big 77,32,2
              CopyOnWriteArrayList<LatLng> flyPointsAfterRebuild = new CopyOnWriteArrayList<>();
              for (CopyOnWriteArrayList<LatLng> arrayItem: lists) {
                for (LatLng latLongItem:arrayItem){
                  // Fix Same Address Pointer by Making Copy
                  LatLng item = new LatLng(Utils.clone(latLongItem.latitude), Utils.clone(latLongItem.longitude));
                  flyPointsAfterRebuild.add(item);
                }
              }
              if (!finalSaveInPref){
                callBack.onSucces(MissionStep.this,flyPointsAfterRebuild);
                return;
              }else {
                callBack.onPerformSaveAndContinu(MissionStep.this,flyPointsAfterRebuild);
                return;
              }
            }

          }
        }catch (Exception e){
          Utils.toLog(TAG, "to99And120Hect",null,e);
          callBack.onError("to99And120Hect Exception Happen");
        }catch (Error er){
          Utils.toLog(TAG, "to99And120Hect",er,null);
          callBack.onError("to99And120Hect Error Happen");
        }
      }
    });


  }

  private void convert120hTo97WaypointArray(CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> arrayOf100Hec, @NonNull OnAsyncOperationCompleteBoolArray callBack) {
    toLog("convert120hTo97WaypointArray Start");
    if (Utils.isNull(callBack)){
      toLog("convert120hTo97WaypointArray callBack is Null ");
      return;
    }else {
      Utils.AsyncExecute(() ->{
        try {
          if (Utils.isNull(arrayOf100Hec)){
            toLog("convert120hTo97WaypointArray arrayOf100Hec is Null");
            callBack.onResultNo("arrayOf100Hec is Null");
            return;
          }else {
            if (arrayOf100Hec.size() == 0){
              toLog("convert120hTo97WaypointArray arrayOf100Hec is Empty");
              callBack.onResultNo("arrayOf100Hec is Empty");
              return;
            }else {
              final CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> container = new CopyOnWriteArrayList<>();
              final CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> over97Array = new CopyOnWriteArrayList<>();
              String errMsg = "";
              // Good, Big, Big, Good, Big
              for (CopyOnWriteArrayList<LatLng> arrayItem:arrayOf100Hec) {
                if (arrayItem.size() > Constants.MAX_WAYPOINT_ITEMS){
                  over97Array.add(arrayItem);
                }
              }
              if (over97Array.size() == 0){
                // No Big Array over 97 point Found
                toLog("convert120hTo97WaypointArray No Big Array over 97 point Found");
                callBack.onResultYes(arrayOf100Hec);
                return;
              }else {
                CopyOnWriteArrayList<LatLng> firstOver97 = over97Array.get(0);
                CopyOnWriteArrayList<LatLng> copyFirstOver97 = copyCollectorArray(firstOver97);
                // Remove And Clean avoid infini cycle
                over97Array.remove(firstOver97);
                firstOver97 = null;
                splitArray(copyFirstOver97, Constants.MAX_WAYPOINT_ITEMS, over97Array, new OnAsyncSplitComplete() {
                  @Override
                  public void onResultNo(@Nullable  String errorDetail) {
                    if (Utils.isNull(errorDetail)){
                      errorDetail = "";
                    }else {
                      toLog("split120hec "+errorDetail);
                      callBack.onResultNo(errorDetail);
                    }
                  }

                  @Override
                  public void onResultYes(@NonNull CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> newMain, CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> bigArray) {
                    try {
                      // Good, Big, Big, Good, Big
                      // bigArea is really over97Array
                      for (CopyOnWriteArrayList<LatLng> splittedItem: newMain) {
                        // Case 98 found
                        if (splittedItem.size() > Constants.MAX_WAYPOINT_ITEMS){
                          if(Utils.isNull(bigArray)){
                            bigArray = new CopyOnWriteArrayList<>();
                          }
                          bigArray.add(splittedItem);
                        }else {
                          container.add(splittedItem);
                        }
                      }

                      if (bigArray.size() > 0){
                        // Take First And Split into Array of 97
                        CopyOnWriteArrayList<LatLng> firstBig = bigArray.get(0);
                        CopyOnWriteArrayList<LatLng> copyFirstBig = copyCollectorArray(firstBig);

                        // Remove And Clean avoid infini cycle
                        bigArray.remove(firstBig);
                        firstBig = null;
                        splitArray(copyFirstBig,Constants.MAX_WAYPOINT_ITEMS,bigArray,this);
                        return;
                      }else {
                        callBack.onResultYes(container);
                      }
                      return;
                    }catch (Exception e){
                      Utils.toLog(TAG, "splitArray",null,e);
                      callBack.onResultNo("split120hec Exception Happen");
                    }catch (Error er){
                      Utils.toLog(TAG, "splitArray",er,null);
                      callBack.onResultNo("split120hec Error Happen");
                    }
                  }
                });
                return;
              }
            }
          }
        }catch (Exception e){
          Utils.toLog(TAG, "convert120hTo97WaypointArray",null,e);
          callBack.onResultNo("Exception Happen");
        }catch (Error er){
          Utils.toLog(TAG, "convert120hTo97WaypointArray",er,null);
          callBack.onResultNo(" Error Happen");
        }
      });
    }

  }

  private void split120hec(@NonNull CopyOnWriteArrayList<LatLng> mainCopy, @NonNull OnAsyncOperationCompleteBoolArray callback)throws Error,Exception {
    if (Utils.isNull(callback)){
      toLog("split120hec callBack is Null");
      return;
    }else {
      toLog("split120hec Start");
      final CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> container = new CopyOnWriteArrayList<>();
      String errMsg = "";

      double area = getArea(mainCopy);
      if (area < Constants.DJI_SDK_MAX_AREA){
        // Below 100 Hectares
        container.add(mainCopy);
        callback.onResultYes(container);
        return;
      }else {
        int coef = mainCopy.size() / 2;
        // Case First Time use Empty bigAreaLeft
        splitArray(mainCopy,coef,new CopyOnWriteArrayList<>(),new OnAsyncSplitComplete(){
          @Override
          public void onResultNo(@Nullable String errorDetail) {
            if (Utils.isNull(errorDetail)){
              errorDetail = "";
            }else {
              toLog("split120hec "+errorDetail);
              callback.onResultNo(errorDetail);
            }
          }

          @Override
          public void onResultYes(@NonNull CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> newMain, CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> bigAreaArray) {
            try {
              // Good, Big, Big, Good, Big
              for (CopyOnWriteArrayList<LatLng> splittedItem: newMain) {
                double area = getArea(splittedItem);
                if (area > Constants.DJI_SDK_MAX_AREA){
                  // Stop case One found
                  if(Utils.isNull(bigAreaArray)){
                    bigAreaArray = new CopyOnWriteArrayList<>();
                  }
                  bigAreaArray.add(splittedItem);
                }else {
                  container.add(splittedItem);
                }
              }
              if (bigAreaArray.size()> 0){
                // Take First And Split into Array of 97
                CopyOnWriteArrayList<LatLng> firstBig = bigAreaArray.get(0);
                CopyOnWriteArrayList<LatLng> copyFirstBig = copyCollectorArray(firstBig);
                // Remove And Clean avoid infini cycle
                bigAreaArray.remove(firstBig);
                firstBig = null;
                splitArray(copyFirstBig,(copyFirstBig.size()/2),bigAreaArray,this);
                return;
              }else {
                callback.onResultYes(container);
              }

            }catch (Exception e){
              Utils.toLog(TAG, "splitArray",null,e);
              callback.onResultNo("splitArray Exception Happen");
            }catch (Error er){
              Utils.toLog(TAG, "splitArray",er,null);
              callback.onResultNo("splitArray Error Happen");
            }
          }
        });
      }
    }
  }

  private void splitArray(@NonNull CopyOnWriteArrayList<LatLng> listToSplit, int coef, CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> bigAreaLeft,@NonNull OnAsyncSplitComplete callback) throws Exception,Error{
      String errMsg = "";
    CopyOnWriteArrayList<CopyOnWriteArrayList<LatLng>> holder = new CopyOnWriteArrayList<>();
      if (Utils.isNull(callback)){
        errMsg = " callback is Null";
        toLog("splitArray "+errMsg);
        return;
      }else {
        if(Utils.isNull(listToSplit)){
          errMsg = "List is Null ";
          toLog("split120hec "+errMsg);
          callback.onResultNo(errMsg);
          return;
        }else {
          if (listToSplit.size() == 0){
            errMsg = "listToSplit is empty ";
            toLog("split120hec "+errMsg);
            callback.onResultNo(errMsg);
            return;
          }else {
            if (coef <= 0){
              errMsg = "Stop cause bad Coef should be greater than 1 ..current ="+Utils.clone(coef);
              toLog("split120hec "+errMsg);
              callback.onResultNo(errMsg);
              return;
            }else {
              if (listToSplit.size() == coef){
                // Too Small So will Not Split
                toLog("split120hec Too Small So will Not Split listToSplit.size:"+listToSplit.size()+ " coef:"+coef);
                holder.add(copyCollectorArray(listToSplit));
                callback.onResultYes(holder,bigAreaLeft);
                return;
              }else {
                CopyOnWriteArrayList<LatLng> collector = null;
                for (LatLng item: listToSplit) {
                  LatLng copyItem = new LatLng(Utils.clone(item.latitude),Utils.clone(item.longitude));
                  if (Utils.isNull(collector)){
                    collector =  new CopyOnWriteArrayList<>();
                  }

                  // Case Fill ok size = 98 and coef= 97
                  if (collector.size() > coef){
                    // Save 97 and add 1 to New Array
                    holder.add(copyCollectorArray(collector));

                    collector.clear();
                    collector = null;
                    collector =  new CopyOnWriteArrayList<>();
                    collector.add(copyItem);
                  }else {
                    collector.add(copyItem);
                  }
                }

                if (Utils.isNull(collector)){
                  // Cas No Enter in loop
                  collector =  new CopyOnWriteArrayList<>();
                }
                // Take Left after Split [1,2,3], [4,5,6], [7] <-
                if (collector.size() > 0){
                  // take [7]
                  holder.add(copyCollectorArray(collector));
                  collector.clear();
                  collector = null;
                }
                callback.onResultYes(holder,bigAreaLeft);
                return;
              }
            }
          }
        }

      }
  }

  private CopyOnWriteArrayList<LatLng> copyCollectorArray(CopyOnWriteArrayList<LatLng> collector)throws Exception,Error {
    // Copy to avoid Same Address Point when delete collector
    CopyOnWriteArrayList<LatLng> collectorCopy = new CopyOnWriteArrayList<>();
    for (LatLng waypoint: collector) {
      LatLng copyWaypoint = new LatLng(Utils.clone(waypoint.latitude), Utils.clone(waypoint.longitude));
      collectorCopy.add(copyWaypoint);
    }
    // Dont clean copy cause share same address in holder
    return collectorCopy;
  }

  public double getArea(CopyOnWriteArrayList<LatLng> mainCopy)throws Error,Exception {
    double res =  SphericalUtil.computeArea(mainCopy)* 0.0001;
    return res;
  }
}
