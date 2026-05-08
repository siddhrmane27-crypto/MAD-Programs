# Firebase Setup Instructions

Before building the app, you must connect it to your Firebase project.

## Steps

1. Go to https://console.firebase.google.com
2. Create a new project (e.g., "LostAndFoundApp")
3. Add an Android app with package name: `com.example.lostandfounditems`
4. Download the `google-services.json` file
5. Place it at: `app/google-services.json`

## Enable Firebase Services

In the Firebase Console, enable:
- **Authentication** → Sign-in method → Email/Password
- **Firestore Database** → Start in test mode (for development)
- **Storage** → Start in test mode (for development)

## Firestore Indexes

The app queries items ordered by `timestamp`. Create composite indexes if prompted:
- Collection: `items`, Fields: `type ASC`, `timestamp DESC`
- Collection: `items`, Fields: `userId ASC`, `timestamp DESC`

## Firestore Security Rules (Production)

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth.uid == userId;
    }
    match /items/{itemId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow delete: if request.auth.uid == resource.data.userId;
    }
  }
}
```
