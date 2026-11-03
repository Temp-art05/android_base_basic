# Giữ tên file + số dòng để stacktrace release (Crashlytics) còn đọc được,
# đồng thời đổi tên file gốc để không lộ cấu trúc package.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Firebase/GMS tham chiếu annotation không có trên classpath — R8 full mode báo missing class.
-dontwarn com.google.android.gms.common.annotation.NoNullnessRewrite

# Lib ads nạp AdMobAdapter + FirebaseAdForwarder bằng reflection (Class.forName) — giữ nguyên tên.
-keep class com.github.dinhn.ads.** { *; }
