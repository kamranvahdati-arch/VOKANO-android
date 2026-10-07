# Calculation reference publication — agreed boundary, 2026-10-07

Status: design contract, NOT a deployed service or an implemented updater.
Current Android candidate reads reviewed data bundled with the APK; it makes no
network request and must not display a successful online-update claim.

## Owner decisions

- Only the owner-authorized administration panel may author reference values.
  Ordinary users enter case facts and externally assessed percentages/amounts,
  not reference annual rates or economic-index tables.
- Two draft routes: ingestion from official sources and direct owner entry with
  source attachments. Both require review before publication. An ingestion job
  must not publish silently. Human entry must not depend on ChatGPT availability.
- During international Internet disruption, domestic publication must continue
  if the domestic network and VOKANO infrastructure remain available. Auth,
  essential scripts/styles/fonts, signing service, API, object storage and
  operational dependencies cannot require an inaccessible foreign provider.
  A foreign-source fetch failure does not block manual drafting/publication.
- Server deployment is deferred until the owner supplies an operational server;
  no DNS, production platform or personal website changes are authorized here.

## Immutable publication model

One bundle has schema version, bundle ID, monotonic publication sequence, issue
time, data-coverage periods, supported engine versions, previous bundle ID,
payload digest, signature and signing-key ID. Each reference has its own stable
ID, revision ID, previous revision, kind, series/base-year, period, effective
interval, value/unit, source title/type/number/date/URL, evidence digest, review
status, author/reviewer identity and reason. Distinguish original official text
from a reviewed republication; do not relabel the latter as original evidence.

Numeric reference updates cannot introduce executable code. A rule-schema or
engine requirement unknown to the client must fail closed and require an app
update; it must not be interpreted as an arbitrary formula string.

Draft -> reviewed -> published is an audited server-side transition. Published
payloads cannot be edited or removed to rewrite history. Corrections publish a
new revision. Withdrawals stop new selection without destroying saved snapshots.
Authorization is enforced on the server; no local owner-name or magic code gate.
Signing authority is separate from the Android release key and backup keys.
Key rotation and revocation need an explicitly authenticated protocol and tests.

## Client activation

1. Fetch manifest and payload from the configured domestic HTTPS endpoint.
2. Verify signature, digest, size limits, schema/engine compatibility and
   monotonic sequence; reject unsigned, truncated, replayed or unsupported data.
3. Validate all rows, decimal precision, units, coverage and lineage. Reject
   inconsistent series/base-year mixes, duplicate active keys and incomplete data.
4. Activate the complete bundle atomically, retaining the previous valid bundle.
   Network/process failure must leave the last usable bundle intact.
5. Recalculation inserts a new snapshot. Historical reports continue reading
   their captured rules/references, never the newly selected global values.

Missing months/years are explicit errors, not nearest-year or extrapolated data.
Checking for an update can be automatic on connection plus a manual check button;
neither constitutes an automatic change to existing financial/case records.

## Deployment acceptance gates

Test owner authentication/authorization, unauthorized writes, approval bypass,
tampered signatures, partial downloads, replay, conflicting publication,
unsupported engines, interrupted activation, rollback to last valid data,
historical report stability and encrypted backup/restore. Test both API30/35.
With international routes blocked but domestic connectivity available, test
owner login, manual draft, approval, publication and client receipt end-to-end.
With the VOKANO server itself unavailable, retain drafts locally where supported
and the client's last valid bundle. Actual results, not design claims, determine
readiness. The shared contract may later serve web; this task builds no web app.
