-keep class com.isax.launcher.skills.** { *; }
-keep class * implements com.isax.launcher.skills.IsaxSkill { *; }
-keepclasseswithmembers class * {
    native <methods>;
}
