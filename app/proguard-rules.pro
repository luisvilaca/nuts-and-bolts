# Credential Manager loads its Play Services provider by reflection (Google sign-in for sync); R8 would drop it.
-if class androidx.credentials.CredentialManager
-keep class androidx.credentials.playservices.** { *; }
