# 10.3 signed candidate validation

Owner explicitly authorized permanent signing and installed-upgrade testing on
2026-10-08. Set versionName=10.3/versionCode=16; application ID unchanged and
schema remains 16. This is a validation candidate, not a claim that all original
legal-calculation scope is complete. Prior verified source: 9454664.

The original permanent key is available privately; never place credentials or
key bytes in source, CI, reports or artifacts. Sign candidate and instrumentation
locally, compare the certificate to original production 10.2, then validate on
API30/35. Preserve original APK bytes and synthetic upgrade fixture evidence.

Still pending: resulting build, signature verification and permanently signed
installed upgrade; broader special legal paths noted in V10_3_CHECKPOINT.md.

## Current verified candidate — 2026-10-09

The earlier pending paragraph above is historical. Current candidate comes from
source740140610506ae3962ffaf0e18b27d5601c0bdf4, development run37910451155.
The signing certificate remains26055f09370416e9cd61c08246b80c57367470cdb6873e282b5b6521cf097202.
APK SHA256:b587ee25661f12cf49a2b14684e51e83cb064b67ca8026ce3b8916d8199de59d.
Manifest commitb9ff0e8. Permanent installed upgrade run37960845628 passed
API30 and API35 with the original10.2 APK and preserved synthetic office data.
Separate clean-install verification is tracked inV10_3_CHECKPOINT.md.
This is still a bounded validation candidate, not completion of all10.3 scope.
