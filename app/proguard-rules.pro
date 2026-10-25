############
# Keep correct stacktrace
-keepattributes SourceFile,LineNumberTable
-keepnames class io.gresse.hugo.anecdote.**

############
# EventBus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

############
# Gson: application classes serialized/deserialized with reflection
-keepattributes Signature
-keep class io.gresse.hugo.anecdote.anecdote.model.** { *; }
-keep class io.gresse.hugo.anecdote.api.model.** { *; }
