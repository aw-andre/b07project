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

    // Alert types
    public static final String TYPE_RED_ZONE = "RED_ZONE_DAY";
    public static final String TYPE_RAPID_RESCUE = "RAPID_RESCUE";
    public static final String TYPE_TRIAGE_ESCALATION = "TRIAGE_ESCALATION";
    public static final String TYPE_INVENTORY_LOW = "INVENTORY_LOW";
    public static final String TYPE_INVENTORY_EXPIRED = "INVENTORY_EXPIRED";

    private static AlertManager instance;

    private final Context appContext;
    private final String parentId;

    private final DatabaseReference rootRef;
    private final DatabaseReference childrenRef;
    private final DatabaseReference inventoryRef;
    private final DatabaseReference alertsRef;

    // listener structures
    private final Map<String, ChildListeners> childListeners = new HashMap<>();

    // for throttling alerts
    private final Map<String, Long> lastAlertTime = new HashMap<>();

    private ValueEventListener parentChildListener;
    private OnAlertListener uiListener;

    // callback for UI if needed
    public interface OnAlertListener {
        void onAlert(AlertRecord alert);
    }

    // ----------------------------------------------------------------------------------------
    // Singleton init
    // ----------------------------------------------------------------------------------------
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

    // ----------------------------------------------------------------------------------------
    // Attach/detach parent-level child list listener
    // ----------------------------------------------------------------------------------------
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

                // clean removed children
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

    // ----------------------------------------------------------------------------------------
    // Attach per-child listeners
    // ----------------------------------------------------------------------------------------
    private void attachChildListeners(String childId) {
        ChildListeners cl = new ChildListeners();
        cl.childId = childId;

        // PEF listener
        cl.pefRef = childrenRef.child(childId).child("logs").child("pef");
        cl.pefListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                handlePefUpdate(childId, snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        cl.pefRef.addValueEventListener(cl.pefListener);

        // rescue listener
        cl.rescueRef = childrenRef.child(childId).child("logs")
                .child("medication").child("rescue");
        cl.rescueListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                handleRescueUpdate(childId, snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        cl.rescueRef.addValueEventListener(cl.rescueListener);

        // triage listener
        cl.triageRef = childrenRef.child(childId).child("logs").child("triage");
        cl.triageListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                handleTriageUpdate(childId, snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        };
        cl.triageRef.addValueEventListener(cl.triageListener);

        // inventory listener
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

        if (cl.pefRef != null && cl.pefListener != null)
            cl.pefRef.removeEventListener(cl.pefListener);

        if (cl.rescueRef != null && cl.rescueListener != null)
            cl.rescueRef.removeEventListener(cl.rescueListener);

        if (cl.triageRef != null && cl.triageListener != null)
            cl.triageRef.removeEventListener(cl.triageListener);

        if (cl.inventoryRef != null && cl.inventoryListener != null)
            cl.inventoryRef.removeEventListener(cl.inventoryListener);
    }

    private void detachAllChildListeners() {
        for (String id : new HashMap<>(childListeners).keySet()) {
            detachChildListeners(id);
        }
    }

    // ----------------------------------------------------------------------------------------
    // ALERT HANDLERS
    // ----------------------------------------------------------------------------------------

    private void handlePefUpdate(String childId, DataSnapshot pefSnap) {
        final long[] latestTs = { -1 };
        final Long[] latestPef = { null };

        for (DataSnapshot row : pefSnap.getChildren()) {
            Long ts = row.child("timestamp").getValue(Long.class);
            Long val = row.child("pef").getValue(Long.class);
            if (ts == null || val == null) continue;

            if (ts > latestTs[0]) {
                latestTs[0] = ts;
                latestPef[0] = val;
            }
        }

        if (latestPef[0] == null) return;

        childrenRef.child(childId).child("pb")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot s) {
                        Long pb = s.getValue(Long.class);
                        if (pb == null || pb == 0) return;

                        double pct = (latestPef[0] * 100.0) / pb;

                        if (pct < 50.0) {
                            pushAlert(childId, TYPE_RED_ZONE,
                                    "PEF is in the RED ZONE for child " + childId,
                                    "HIGH",
                                    60 * 60 * 1000L);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {}
                });

    }

    private void handleRescueUpdate(String childId, DataSnapshot rescueSnap) {
        long now = System.currentTimeMillis();
        long win = 3L * 60 * 60 * 1000;

        int count = 0;
        for (DataSnapshot row : rescueSnap.getChildren()) {
            Long ts = row.child("timestamp").getValue(Long.class);
            if (ts != null && ts >= now - win) count++;
        }

        if (count >= 3) {
            pushAlert(childId, TYPE_RAPID_RESCUE,
                    "Rapid rescue usage (≥3 in 3 hours)",
                    "HIGH",
                    60 * 60 * 1000L);
        }
    }

    private void handleTriageUpdate(String childId, DataSnapshot triageSnap) {
        for (DataSnapshot row : triageSnap.getChildren()) {
            Boolean trouble = row.child("troubleBreathing").getValue(Boolean.class);
            if (trouble != null && trouble) {
                pushAlert(childId, TYPE_TRIAGE_ESCALATION,
                        "Triage escalation: trouble breathing",
                        "CRITICAL",
                        10 * 60 * 1000L);
                return;
            }
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
            pushAlert(childId, TYPE_INVENTORY_LOW + "_" + type,
                    type + " medication low: " + amountLeft + " doses left",
                    "MEDIUM",
                    6L * 60 * 60 * 1000L);
        }

        if (expiry != null && now >= expiry) {
            pushAlert(childId, TYPE_INVENTORY_EXPIRED + "_" + type,
                    type + " medication is expired",
                    "HIGH",
                    24L * 60 * 60 * 1000L);
        }
    }

    private void handlePostDoseUpdate(String childId, DataSnapshot medicationSnap) {

        long latestTs = -1;
        String latestPost = null;

        // Loop rescue and controller nodes
        for (DataSnapshot medType : medicationSnap.getChildren()) {

            for (DataSnapshot doseSnap : medType.getChildren()) {
                Long ts = doseSnap.child("timestamp").getValue(Long.class);
                String post = doseSnap.child("post").getValue(String.class);

                if (ts == null || post == null) continue;

                if (ts > latestTs) {
                    latestTs = ts;
                    latestPost = post;
                }
            }
        }

        if (latestPost == null) return;

        if (latestPost.equalsIgnoreCase("worse")) {
            pushAlert(
                    childId,
                    TYPE_WORSE_AFTER_DOSE,
                    "Child " + childId + " is worse after medication dose",
                    "HIGH",
                    60 * 60 * 1000L   // throttle: 1 hour
            );
        }
    }

    // ----------------------------------------------------------------------------------------
    // PUSH ALERT
    // ----------------------------------------------------------------------------------------
    private void pushAlert(String childId,
                           String type,
                           String message,
                           String severity,
                           long throttleMs) {

        long now = System.currentTimeMillis();
        String key = childId + "_" + type;

        Long last = lastAlertTime.get(key);
        if (last != null && now - last < throttleMs) return;

        lastAlertTime.put(key, now);

        AlertRecord alert = new AlertRecord();
        alert.childId = childId;
        alert.type = type;
        alert.message = message;
        alert.severity = severity;
        alert.timestamp = now;

        alertsRef.push().setValue(alert);
        Log.d(TAG, "Alert: " + type + " for child " + childId);

        if (uiListener != null) uiListener.onAlert(alert);
    }

    // ----------------------------------------------------------------------------------------
    // POJOS
    // ----------------------------------------------------------------------------------------
    public static class AlertRecord {
        public String childId;
        public String type;
        public String message;
        public String severity;
        public long timestamp;

        public AlertRecord() {}
    }

    private static class ChildListeners {
        String childId;

        DatabaseReference pefRef;
        ValueEventListener pefListener;

        DatabaseReference rescueRef;
        ValueEventListener rescueListener;

        DatabaseReference triageRef;
        ValueEventListener triageListener;

        DatabaseReference inventoryRef;
        ValueEventListener inventoryListener;
    }

    public static final String TYPE_WORSE_AFTER_DOSE = "WORSE_AFTER_DOSE";
}
