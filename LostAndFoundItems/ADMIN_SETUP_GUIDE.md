# Admin Panel Setup Guide

## Overview
The Lost & Found app now includes an **Admin Verification System** where all posted items must be approved by an admin before they become visible to regular users.

## Features Added

### 1. **Admin Role System**
- Users can have admin privileges
- First registered user automatically becomes admin
- Admin status stored in Firestore user profile

### 2. **Item Verification**
- All new items start as "unverified"
- Only verified items are visible to regular users
- Admins can see all pending (unverified) items

### 3. **Admin Panel**
- Accessible only to admin users
- Shows all pending items awaiting verification
- Approve or reject items with one click
- Rejected items are permanently deleted

## How It Works

### For Regular Users:
1. Register/Login to the app
2. Post lost or found items
3. Items are submitted for admin review
4. Once approved, items become visible to all users
5. Browse only verified items in Home, Lost, and Found tabs

### For Admin Users:
1. First user to register automatically becomes admin
2. Admin icon appears in the toolbar
3. Click "Admin Panel" to see pending items
4. Review each item:
   - **Approve**: Item becomes visible to all users
   - **Reject**: Item is permanently deleted
5. Admin can also use all regular user features

## Technical Implementation

### Modified Files:

#### Models:
- **UserModel.java**: Added `isAdmin` boolean field
- **ItemModel.java**: Added `verified` boolean field (default: false)

#### Services:
- **FirestoreService.java**: 
  - Updated queries to filter verified items for regular users
  - Added admin methods: `getPendingItems()`, `verifyItem()`, `checkAdminStatus()`
  
- **FirebaseAuthService.java**: 
  - Added `getCurrentUserId()` method

#### Activities:
- **MainActivity.java**: 
  - Checks admin status on startup
  - Shows admin menu option for admin users
  
- **AdminPanelActivity.java**: 
  - New activity for admin panel
  - Lists all pending items
  - Handles approve/reject actions
  
- **RegisterActivity.java**: 
  - Checks if user is first to register
  - Automatically grants admin privileges to first user

#### Adapters:
- **AdminItemAdapter.java**: 
  - RecyclerView adapter for pending items
  - Shows approve/reject buttons

#### Layouts:
- **activity_admin_panel.xml**: Admin panel layout
- **item_admin_pending.xml**: Pending item card layout
- **admin_menu.xml**: Admin menu with panel access

## Database Structure

### Users Collection:
```
users/{userId}
  - userId: string
  - name: string
  - email: string
  - isAdmin: boolean  // NEW FIELD
```

### Items Collection:
```
items/{itemId}
  - itemId: string
  - title: string
  - description: string
  - category: string
  - type: string (Lost/Found)
  - location: string
  - imageUrl: string (Base64)
  - userId: string
  - userEmail: string
  - timestamp: long
  - resolved: boolean
  - verified: boolean  // NEW FIELD (default: false)
```

## Making Additional Users Admin

Currently, only the first registered user becomes admin automatically. To make additional users admin:

### Option 1: Manually in Firebase Console
1. Go to Firebase Console
2. Navigate to Firestore Database
3. Open the `users` collection
4. Find the user document
5. Add/Edit field: `isAdmin` = `true`

### Option 2: Future Enhancement
You can add an admin management screen where existing admins can promote other users to admin status using the `updateUserAdminStatus()` method already available in FirestoreService.

## Testing the Admin System

1. **Clear app data** or use a new Firebase project
2. **Register first user** - This user becomes admin automatically
3. **Login as admin** - You'll see the admin icon in toolbar
4. **Register second user** (different email) - This is a regular user
5. **Login as regular user** - Post a lost/found item
6. **Switch back to admin** - Open Admin Panel to see pending item
7. **Approve the item** - It now appears in regular user's view
8. **Test rejection** - Post another item and reject it as admin

## Security Notes

- Admin status is checked on the client side
- For production, add Firebase Security Rules to enforce admin-only operations
- Consider adding server-side verification for critical admin actions

## Firebase Security Rules (Recommended)

Add these rules to your Firestore:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users can read their own profile
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
    
    // Only show verified items to regular users
    match /items/{itemId} {
      allow read: if resource.data.verified == true || 
                     get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isAdmin == true;
      allow create: if request.auth != null;
      allow update, delete: if request.auth.uid == resource.data.userId ||
                               get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isAdmin == true;
    }
  }
}
```

## Future Enhancements

1. **Admin Dashboard**: Statistics on pending/approved/rejected items
2. **Admin Management**: Allow admins to promote/demote other users
3. **Notification System**: Notify users when their items are approved/rejected
4. **Audit Log**: Track all admin actions
5. **Bulk Actions**: Approve/reject multiple items at once
6. **Item Reports**: Allow users to report inappropriate items

## Troubleshooting

### Admin menu not showing:
- Ensure you're logged in as the first registered user
- Check Firestore to verify `isAdmin: true` in user document
- Restart the app after registration

### Items not appearing after approval:
- Check internet connection
- Pull to refresh the list
- Verify item's `verified` field is `true` in Firestore

### Can't access admin panel:
- Only users with `isAdmin: true` can access
- Check Firebase Console to verify admin status
- Ensure you're logged in

## Support

For issues or questions, check:
1. Firebase Console for data verification
2. Android Logcat for error messages
3. Firestore Security Rules for permission issues
