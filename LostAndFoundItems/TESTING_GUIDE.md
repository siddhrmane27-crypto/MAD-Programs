# 🧪 Admin Panel Testing Guide

## ✅ App Successfully Installed!
The app is now installed on your device: **CPH2729 - 16**

---

## 📋 Testing Checklist

### Test 1: Create Admin User (First User)

**Steps:**
1. **Open the app** on your device
2. You'll see the **Splash Screen** → then **Login Screen**
3. Click **"Register"** or **"Sign Up"** link
4. Fill in the registration form:
   ```
   Name: Admin User
   Email: admin@test.com
   Password: admin123
   ```
5. Click **"Register"** button
6. Wait for registration to complete
7. **✅ CHECK**: You should see a toast message: **"Welcome Admin! You have admin privileges."**
8. You'll be taken to the **Main Screen**

**Verify Admin Access:**
- Look at the **top toolbar**
- **✅ CHECK**: You should see an **Admin icon** (looks like a settings/manage icon)
- This icon confirms you are an admin!

---

### Test 2: Post an Item as Admin

**Steps:**
1. Click the **+ (FAB)** button at the bottom right
2. Fill in the item details:
   ```
   Title: Lost Wallet
   Description: Black leather wallet lost near library
   Category: Wallet
   Type: Lost
   Location: Main Library
   ```
3. Click **"Select Image"** and choose any image from your gallery
4. Click **"Submit"** button
5. Wait for success message
6. Go back to **Home tab**

**What Happens:**
- Since you're an admin, you can approve your own items
- But first, let's test the admin panel!

---

### Test 3: Access Admin Panel

**Steps:**
1. From the **Main Screen**, look at the **top toolbar**
2. Click the **Admin icon** (settings/manage icon)
3. You'll be taken to the **Admin Panel**

**What You'll See:**
- **Title**: "Admin Panel"
- **List of pending items** (items waiting for approval)
- Your "Lost Wallet" item should be listed here
- Each item shows:
  - Item image
  - Title and description
  - Category and location
  - Posted by email
  - **Two buttons**: "Reject" (red) and "Approve" (green)

---

### Test 4: Approve an Item

**Steps:**
1. In the **Admin Panel**, find your "Lost Wallet" item
2. Review the details
3. Click the **"Approve"** button (green)
4. **✅ CHECK**: Toast message: **"Item approved successfully"**
5. The item disappears from the pending list
6. Press **Back** button to return to Main Screen
7. Go to **Home tab**

**Verify:**
- **✅ CHECK**: Your "Lost Wallet" item now appears in the Home tab!
- **✅ CHECK**: It also appears in the "Lost Items" tab
- This proves the verification system works!

---

### Test 5: Create Regular User (Second User)

**Steps:**
1. **Logout** from admin account:
   - Go to **Profile tab**
   - Click **"Logout"** button
2. You're back at the **Login Screen**
3. Click **"Register"** again
4. Fill in different details:
   ```
   Name: Regular User
   Email: user@test.com
   Password: user123
   ```
5. Click **"Register"** button
6. You'll be taken to the **Main Screen**

**Verify Regular User:**
- Look at the **top toolbar**
- **✅ CHECK**: There should be **NO admin icon**
- This confirms you're a regular user!

---

### Test 6: Post Item as Regular User

**Steps:**
1. Click the **+ (FAB)** button
2. Fill in the item details:
   ```
   Title: Found Mobile Phone
   Description: iPhone found in cafeteria
   Category: Mobile
   Type: Found
   Location: College Cafeteria
   ```
3. Select an image
4. Click **"Submit"** button
5. Wait for success message
6. Go to **Home tab**

**What You'll See:**
- **✅ CHECK**: Your "Found Mobile Phone" item is **NOT visible** in Home tab
- **✅ CHECK**: It's also **NOT visible** in Found Items tab
- This is correct! It needs admin approval first!

---

### Test 7: Admin Approves Regular User's Item

**Steps:**
1. **Logout** from regular user account
2. **Login** as admin:
   ```
   Email: admin@test.com
   Password: admin123
   ```
3. Click the **Admin icon** in toolbar
4. You'll see the **Admin Panel**

**What You'll See:**
- **✅ CHECK**: "Found Mobile Phone" item is in the pending list
- You can see who posted it: user@test.com

**Approve the Item:**
1. Click **"Approve"** button on the "Found Mobile Phone" item
2. **✅ CHECK**: Toast message: "Item approved successfully"
3. Item disappears from pending list
4. Press **Back** to Main Screen
5. Check **Home tab** and **Found Items tab**
6. **✅ CHECK**: "Found Mobile Phone" now appears!

---

### Test 8: Test Rejection

**Steps:**
1. **Logout** and **Login** as regular user (user@test.com)
2. Post another item:
   ```
   Title: Test Item to Reject
   Description: This will be rejected
   Category: Others
   Type: Lost
   Location: Test Location
   ```
3. Submit the item
4. **Logout** and **Login** as admin (admin@test.com)
5. Open **Admin Panel**
6. Find "Test Item to Reject"
7. Click **"Reject"** button (red)
8. **✅ CHECK**: Toast message: "Item rejected and deleted"
9. Item disappears from pending list
10. **Logout** and **Login** as regular user
11. Check all tabs

**Verify:**
- **✅ CHECK**: The rejected item does NOT appear anywhere
- It's permanently deleted!

---

### Test 9: Test Item Editing (Bonus)

**Steps:**
1. Login as any user
2. Go to **Profile tab**
3. You'll see your posted items
4. Click on any item
5. In **Item Details** screen, click **"Edit"** button
6. Modify the title or description
7. Click **"Update"** button
8. **✅ CHECK**: Changes are saved
9. Go back and verify changes appear

---

### Test 10: Test Mark as Resolved (Bonus)

**Steps:**
1. Login as any user
2. Go to **Home tab**
3. Click on one of your items
4. In **Item Details** screen, click **"Mark as Resolved"** button
5. **✅ CHECK**: Button changes to "Mark as Unresolved"
6. Go back to Home tab
7. **✅ CHECK**: Item now has a **purple "Resolved" badge**

---

## 🎯 Quick Test Summary

### What Works:
✅ First user becomes admin automatically  
✅ Admin icon appears for admin users  
✅ Admin panel shows pending items  
✅ Approve button makes items visible  
✅ Reject button deletes items  
✅ Regular users can't see unverified items  
✅ Regular users don't have admin access  
✅ Items appear after approval  

---

## 🐛 Troubleshooting

### Problem: Admin icon not showing
**Solution:**
- Make sure you're the first registered user
- Try logging out and logging back in
- Check Firebase Console → Firestore → users collection → verify `isAdmin: true`

### Problem: Items not appearing after approval
**Solution:**
- Pull down to refresh the list
- Go to another tab and come back
- Restart the app

### Problem: Can't see pending items in admin panel
**Solution:**
- Make sure items were posted by regular users
- Check internet connection
- Verify you're logged in as admin

### Problem: App crashes
**Solution:**
- Check Android Logcat for errors
- Verify Firebase is properly configured
- Ensure `google-services.json` is in place

---

## 📱 Visual Guide

### Admin User View:
```
┌─────────────────────────┐
│  Lost & Found  [⚙️]     │ ← Admin icon here!
├─────────────────────────┤
│                         │
│  [Home] [Lost] [Found]  │
│                         │
│  📦 Items appear here   │
│                         │
└─────────────────────────┘
```

### Regular User View:
```
┌─────────────────────────┐
│  Lost & Found           │ ← No admin icon
├─────────────────────────┤
│                         │
│  [Home] [Lost] [Found]  │
│                         │
│  📦 Only verified items │
│                         │
└─────────────────────────┘
```

### Admin Panel:
```
┌─────────────────────────┐
│  ← Admin Panel          │
├─────────────────────────┤
│  ┌───────────────────┐  │
│  │ 📱 Found Mobile   │  │
│  │ Posted by: user@  │  │
│  │ [Reject] [Approve]│  │
│  └───────────────────┘  │
│                         │
│  ┌───────────────────┐  │
│  │ 💼 Lost Wallet    │  │
│  │ Posted by: admin@ │  │
│  │ [Reject] [Approve]│  │
│  └───────────────────┘  │
└─────────────────────────┘
```

---

## 🎓 Testing Tips

1. **Use two different emails** for admin and regular user
2. **Take screenshots** of each step for your project report
3. **Test on different devices** if possible
4. **Clear app data** to start fresh: Settings → Apps → Lost & Found → Clear Data
5. **Check Firebase Console** to see data in real-time

---

## 📊 Expected Results Table

| Action | Admin User | Regular User |
|--------|-----------|--------------|
| See admin icon | ✅ Yes | ❌ No |
| Access admin panel | ✅ Yes | ❌ No |
| Post items | ✅ Yes | ✅ Yes |
| See own unverified items | ✅ Yes (in admin panel) | ❌ No |
| See verified items | ✅ Yes | ✅ Yes |
| Approve items | ✅ Yes | ❌ No |
| Reject items | ✅ Yes | ❌ No |
| Edit own items | ✅ Yes | ✅ Yes |
| Delete own items | ✅ Yes | ✅ Yes |

---

## 🎬 Video Demo Script

If you need to record a demo:

1. **Introduction** (30 sec)
   - Show app icon and name
   - Explain the purpose

2. **Admin Registration** (1 min)
   - Register first user
   - Show admin icon appears

3. **Regular User Registration** (1 min)
   - Register second user
   - Show no admin icon

4. **Post Item as Regular User** (1 min)
   - Post an item
   - Show it doesn't appear

5. **Admin Approval** (2 min)
   - Login as admin
   - Open admin panel
   - Approve the item
   - Show it now appears

6. **Rejection Demo** (1 min)
   - Post another item
   - Reject it as admin
   - Show it's deleted

7. **Conclusion** (30 sec)
   - Summarize features
   - Show final app state

---

## ✅ Testing Complete!

Once you've completed all tests, you can confidently say:
- ✅ Admin system works perfectly
- ✅ Verification system prevents spam
- ✅ Regular users have limited access
- ✅ Admin can control all content
- ✅ App is ready for demonstration!

---

**Need Help?** Check the logs:
```bash
# View Android logs
adb logcat | grep "LostAndFound"
```

**Good luck with your testing! 🚀**
