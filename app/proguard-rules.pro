# Room / Hilt generate code that must be kept.
# Compose + AndroidX consumer rules are shipped with the libraries.
-keepattributes *Annotation*

# Keep our domain models (used with Room + reflection-free serialization later).
-keep class com.meddiktat.domain.model.** { *; }
