# First-run database key concurrency review — 2026-10-07

## Evidence and limits

Run 37617768784, revision 9b8757c: arithmetic/build succeeded and API 30
installed-upgrade/UI/preview checks passed. API 35 seeded the immutable 10.2
baseline successfully, but the candidate could not reopen its encrypted database
(SQLCipher code 26 / page-one HMAC failure). Both APK signing identities matched.
The baseline log records database opens on instrumentation thread 2304 and main
thread 2270 within 45 ms. The log does not establish either caller's Java stack
or prove that this specific failure was caused by different keys.

Code inspection found a real first-creation race in the unchanged baseline
DatabaseKey.read: two callers can both observe an absent wrapped key, generate
different secrets, and overwrite the persisted secret. The candidate serializes
the entire operation within the application's single process. No manifest
component currently declares a separate process. Future multi-process use needs
cross-process locking, not just this monitor.

No key rotation, database deletion, data clearing, encryption-format change,
or silent recovery is introduced. This prevents a first-run race; it cannot
repair an already damaged/mismatched key/database pair.

## Verification changes

- A test uses isolated preference names and nonexistent isolated database paths
  for three rounds of sixteen simultaneous key readers. It checks one shared
  secret, persisted reread, and unchanged live key without logging key material.
- The shared test runner disables ReminderReceiver and initializes the fixture
  key in onCreate on the main thread, before instrumentation tests start.
  Runner.onStart alone cannot cancel work already dispatched on the main thread.
  Only test APK sources are shared; the immutable baseline application is not edited.
- A separate force-stop/restart baseline-reopen phase checks schema 15 and all
  saved rows, settings and attachment hash BEFORE candidate installation. A broken
  fixture now fails at its own boundary instead of being labelled migration loss.
- The original same-package non-clearing upgrade and all existing suites remain.
  The runner restores the receiver's previous state after each suite.

These changes require fresh API 30 and API 35 CI evidence. The failed run remains
recorded; a green new run does not retrospectively prove its exact root cause.

Run 37661104002 compiled successfully, but fixture setup failed on both APIs:
`pm disable-user` is a package-level state, rejected for a component. Corrected
to component-level `pm disable`. No instrumentation or migration ran in that
attempt; this is not evidence of another database failure.

Run 37661807249 also rejected shell component mutation (state 2). Removed this
approach entirely. Test-runner onCreate now owns fixture preparation using the
app's existing component permission, with no adb-root escalation. Both rejected
runs stopped before instrumentation. The baseline application source remains
unchanged; its test runner is shared explicitly in the workflow alongside the
upgrade fixture. This deliberately serializes baseline fixture creation, while
the separate candidate concurrency regression still tests simultaneous readers.

CI 37662577692 (21fccf6) completed successfully on API30 and API35. Both
baseline seed/reopen and candidate upgrade/concurrency suites passed.
Test-only signing remains in use; this is not production-signature evidence.
