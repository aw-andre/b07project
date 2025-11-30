package com.example.b07_group_project.alerts;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.b07_group_project.FirebaseDatabaseManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class AlertManager {

    private static final String TAG = "AlertManager";

    private static AlertManager instance;

    private final Context appContext;
    private final String parentId;

    private final DatabaseReference rootRef;
    private final DatabaseReference childrenRef;
    private final DatabaseReference inventoryRef;
    private final DatabaseReference alertsRef;

    private final Map<String, ChildListeners> childListeners = new HashMap<>();

    private final Map<String, Long> lastAlertTime = new HashMap<>();

    private ValueEventListener parentChildListener;
    private OnAlertListener uiListener;

    public interface OnAlertListener {
        void onAlert(AlertRecord alert);
    }

    public static synchronized AlertManager init(Context context, String parentId) {
        if (instance == null) {
            instance = new AlertManager(context.getApplicationContext(), parentId);
        }
        return instance;
    }

    public static AlertManager getInstance() {
        return instance;
    }

    private AlertManager(Context context, String parentId) {
        this.appContext = context;
        this.parentId = parentId;

        rootRef = FirebaseDatabaseManager.getInstance().getDatabaseReference();
        childrenRef = rootRef.child("children");
        inventoryRef = rootRef.child("inventory");
        alertsRef = rootRef.child("alerts").child(parentId);

        attachParentChildListener();
    }

    public void stop() {
        detachParentChildListener();
        detachAllChildListeners();
    }

    public void setOnAlertListener(OnAlertListener listener) {
        this.uiListener = listener;
    }

    private void attachParentChildListener() {
        if (parentChildListener != null) return;

        parentChildListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                Map<String, Boolean> found = new HashMap<>();

                for (DataSnapshot childSnap : snapshot.getChildren()) {
                    String childId = childSnap.getKey();
                    if (childId == null) continue;

                    found.put(childId, true);

                    if (!childListeners.containsKey(childId)) {
                        attachChildListeners(childId);
                    }
                }

                for (String id : new HashMap<>(childListeners).keySet()) {
                    if (!found.containsKey(id)) {
                        detachChildListeners(id);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Child list cancelled: " + error.getMessage());
            }
        };

        childrenRef.orderByChild("parentId")
                .equalTo(parentId)
                .addValueEventListener(parentChildListener);
    }

    private void detachParentChildListener() {
        if (parentChildListener != null) {
            childrenRef.removeEventListener(parentChildListener);
            parentChildListener = null;
        }
    }

    private void attachChildListeners(String childId) {
        ChildListeners cl = new ChildListeners();
        cl.childId = childId;

        cl.rescueRef = childrenRef.child(childId).child("logs").child("medication").child("rescue");
        cl.rescueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                handleRapidRescue(childId, snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        cl.rescueRef.addValueEventListener(cl.rescueListener);

        cl.postRef = childrenRef.child(childId).child("logs").child("post");
        cl.postListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                handleWorseAfterDose(childId, snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        cl.postRef.addValueEventListener(cl.postListener);

        cl.inventoryRef = inventoryRef.child(childId);
        cl.inventoryListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                handleInventoryUpdate(childId, snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        cl.inventoryRef.addValueEventListener(cl.inventoryListener);

        childListeners.put(childId, cl);
    }

    private void detachChildListeners(String childId) {
        ChildListeners cl = childListeners.remove(childId);
        if (cl == null) return;

        if (cl.rescueRef != null && cl.rescueListener != null)
            cl.rescueRef.removeEventListener(cl.rescueListener);

        if (cl.postRef != null && cl.postListener != null)
            cl.postRef.removeEventListener(cl.postListener);

        if (cl.inventoryRef != null && cl.inventoryListener != null)
            cl.inventoryRef.removeEventListener(cl.inventoryListener);
    }

    private void detachAllChildListeners() {
        for (String id : new HashMap<>(childListeners).keySet()) {
            detachChildListeners(id);
        }
    }

    private void handleRapidRescue(String childId, DataSnapshot rescueSnap) {
        long now = System.currentTimeMillis();
        long window = 3L * 60 * 60 * 1000;

        int count = 0;
        for (DataSnapshot row : rescueSnap.getChildren()) {
            Long ts = row.child("timestamp").getValue(Long.class);
            if (ts != null && ts >= now - window) count++;
        }

        if (count >= 3) {
            pushAlert(childId,
                    "Rapid rescue usage (≥3 times within 3 hours)");
        }
    }

    private void handleWorseAfterDose(String childId, DataSnapshot postSnap) {
        long latest = -1;
        String latestPost = null;

        for (DataSnapshot row : postSnap.getChildren()) {
            Long ts = row.child("timestamp").getValue(Long.class);
            String status = row.child("post").getValue(String.class);
            if (ts == null || status == null) continue;

            if (ts > latest) {
                latest = ts;
                latestPost = status;
            }
        }

        if (latestPost == null) return;

        if (latestPost.equalsIgnoreCase("worse")) {
            pushAlert(childId,
                    "Child reported feeling worse after medication");
        }
    }

    private void handleInventoryUpdate(String childId, DataSnapshot invSnap) {
        checkInventory(childId, "rescue", invSnap.child("rescue"));
        checkInventory(childId, "controller", invSnap.child("controller"));
    }

    private void checkInventory(String childId, String type, DataSnapshot snap) {
        Long amountLeft = snap.child("amountLeft").getValue(Long.class);
        Long expiry = snap.child("expiryDate").getValue(Long.class);

        long now = System.currentTimeMillis();

        if (amountLeft != null && amountLeft <= 20) {
            pushAlert(childId,
                    type + " medication low (" + amountLeft + " left)");
        }

        if (expiry != null && now >= expiry) {
            pushAlert(childId,
                    type + " medication is expired");
        }
    }


    private void pushAlert(String childId, String message) {

        long now = System.currentTimeMillis();
        String key = childId + "_" + message;

        Long last = lastAlertTime.get(key);
        if (last != null && now - last < 10 * 60 * 1000L) return;

        lastAlertTime.put(key, now);

        String id = alertsRef.push().getKey();
        if (id == null) return;

        Map<String, Object> alertData = new HashMap<>();
        alertData.put("message", message);
        alertData.put("timestamp", now);
        alertData.put("seen", false);
        alertData.put("childId", childId);

        alertsRef.child(id).setValue(alertData);

        AlertRecord record = new AlertRecord();
        record.message = message;
        record.timestamp = now;
        record.childId = childId;

        if (uiListener != null) uiListener.onAlert(record);
    }

    public static class AlertRecord {
        public String message;
        public long timestamp;
        public String childId;
    }

    private static class ChildListeners {
        String childId;

        DatabaseReference rescueRef;
        ValueEventListener rescueListener;

        DatabaseReference postRef;
        ValueEventListener postListener;

        DatabaseReference inventoryRef;
        ValueEventListener inventoryListener;
    }
}
