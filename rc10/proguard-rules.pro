# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in ${sdk.dir}/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the ProGuard
# include property in project.properties.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class com.renegade.rc.TSFragment {
#   public *;
#}

#-keepclassmembers class com.squareup.picasso.* {
#   public *;
#}

-dontwarn com.squareup.**

#-keep class * extends java.util.ListResourceBundle {
#    protected Object[][] getContents();
#}
#
#-keep public class com.google.android.gms.common.internal.safeparcel.SafeParcelable {
#    public static final *** NULL;
#}
#
#-keepnames @com.google.android.gms.common.annotation.KeepName class *
#-keepclassmembernames class * {
#    @com.google.android.gms.common.annotation.KeepName *;
#}
#
#-keepnames class * implements android.os.Parcelable {
#    public static final ** CREATOR;
#}