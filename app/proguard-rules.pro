# Credential Manager loads its Play Services provider by reflection (Google sign-in for sync); R8 would drop it.
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }

# Room builds WorkManager's database (pulled in by Glance, the widget) by reflection through its no-argument
# constructor; R8 drops that constructor and the app crashes at launch.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
