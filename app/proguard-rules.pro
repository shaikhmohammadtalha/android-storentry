# ProGuard rules for Storentry

# Keep project-specific models and database entities to prevent key fields/constructors from being obfuscated or stripped.
-keep class com.shaikh.storentry.domain.model.** { *; }
-keep class com.shaikh.storentry.data.local.entity.** { *; }

# Firebase Crashlytics requirements for crash mapping
-keepattributes SourceFile,LineNumberTable
-keep public class com.google.firebase.crashlytics.** { *; }
