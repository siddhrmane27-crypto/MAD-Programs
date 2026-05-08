package com.example.lostandfounditems.services;

import com.example.lostandfounditems.models.ItemModel;
import com.example.lostandfounditems.models.UserModel;
import com.example.lostandfounditems.utils.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FirestoreService {
    private final FirebaseFirestore db;

    public FirestoreService() {
        db = FirebaseFirestore.getInstance();
    }

    public interface ItemsCallback {
        void onSuccess(List<ItemModel> items);
        void onFailure(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onFailure(String error);
    }

    public interface UserCallback {
        void onSuccess(UserModel user);
        void onFailure(String error);
    }

    public interface UsersCallback {
        void onSuccess(List<UserModel> users);
        void onFailure(String error);
    }

    public void saveUser(UserModel user, SimpleCallback callback) {
        db.collection(Constants.COLLECTION_USERS)
                .document(user.getUserId())
                .set(user)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getUser(String userId,
                        com.google.android.gms.tasks.OnSuccessListener<UserModel> onSuccess,
                        com.google.android.gms.tasks.OnFailureListener onFailure) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) onSuccess.onSuccess(doc.toObject(UserModel.class));
                    else onSuccess.onSuccess(null);
                })
                .addOnFailureListener(onFailure);
    }

    public void addItem(ItemModel item, SimpleCallback callback) {
        String docId = db.collection(Constants.COLLECTION_ITEMS).document().getId();
        item.setItemId(docId);
        db.collection(Constants.COLLECTION_ITEMS)
                .document(docId)
                .set(item)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // No composite index needed — only orderBy timestamp (single field)
    // Only shows verified items to regular users
    public void getAllItems(ItemsCallback callback) {
        db.collection(Constants.COLLECTION_ITEMS)
                .whereEqualTo("verified", true)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<ItemModel> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) list.add(doc.toObject(ItemModel.class));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Uses composite index (type + verified + timestamp) — sorts in-memory as fallback
    public void getLostItems(ItemsCallback callback) {
        db.collection(Constants.COLLECTION_ITEMS)
                .whereEqualTo("type", Constants.TYPE_LOST)
                .whereEqualTo("verified", true)
                .get()
                .addOnSuccessListener(snap -> {
                    List<ItemModel> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) list.add(doc.toObject(ItemModel.class));
                    Collections.sort(list, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Uses composite index (type + verified + timestamp) — sorts in-memory as fallback
    public void getFoundItems(ItemsCallback callback) {
        db.collection(Constants.COLLECTION_ITEMS)
                .whereEqualTo("type", Constants.TYPE_FOUND)
                .whereEqualTo("verified", true)
                .get()
                .addOnSuccessListener(snap -> {
                    List<ItemModel> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) list.add(doc.toObject(ItemModel.class));
                    Collections.sort(list, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // Uses composite index (userId + timestamp) — sorts in-memory as fallback
    public void getUserItems(String userId, ItemsCallback callback) {
        db.collection(Constants.COLLECTION_ITEMS)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(snap -> {
                    List<ItemModel> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) list.add(doc.toObject(ItemModel.class));
                    Collections.sort(list, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getItemById(String itemId,
                            com.google.android.gms.tasks.OnSuccessListener<ItemModel> onSuccess,
                            com.google.android.gms.tasks.OnFailureListener onFailure) {
        db.collection(Constants.COLLECTION_ITEMS)
                .document(itemId)
                .get()
                .addOnSuccessListener(doc -> onSuccess.onSuccess(doc.toObject(ItemModel.class)))
                .addOnFailureListener(onFailure);
    }

    public void deleteItem(String itemId, SimpleCallback callback) {
        db.collection(Constants.COLLECTION_ITEMS)
                .document(itemId)
                .delete()
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateItem(String itemId, String title, String description,
                           String category, String location, SimpleCallback callback) {
        db.collection(Constants.COLLECTION_ITEMS)
                .document(itemId)
                .update("title", title,
                        "description", description,
                        "category", category,
                        "location", location)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void markResolved(String itemId, boolean resolved, SimpleCallback callback) {
        db.collection(Constants.COLLECTION_ITEMS)
                .document(itemId)
                .update("resolved", resolved)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    // ========== ADMIN METHODS ==========

    public void checkAdminStatus(String userId, UserCallback callback) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        UserModel user = doc.toObject(UserModel.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure("User not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getPendingItems(ItemsCallback callback) {
        db.collection(Constants.COLLECTION_ITEMS)
                .whereEqualTo("verified", false)
                .get()
                .addOnSuccessListener(snap -> {
                    List<ItemModel> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) list.add(doc.toObject(ItemModel.class));
                    // Sort in memory to avoid composite index requirement
                    Collections.sort(list, (a, b) -> Long.compare(b.getTimestamp(), a.getTimestamp()));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void verifyItem(String itemId, boolean verified, SimpleCallback callback) {
        db.collection(Constants.COLLECTION_ITEMS)
                .document(itemId)
                .update("verified", verified)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getAllUsers(UsersCallback callback) {
        db.collection(Constants.COLLECTION_USERS)
                .get()
                .addOnSuccessListener(snap -> {
                    List<UserModel> list = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snap) list.add(doc.toObject(UserModel.class));
                    callback.onSuccess(list);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void updateUserAdminStatus(String userId, boolean isAdmin, SimpleCallback callback) {
        db.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .update("isAdmin", isAdmin)
                .addOnSuccessListener(v -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
}
